package com.anatolf.tvchat.ui.chat;

import android.text.TextUtils;

import com.anatolf.tvchat.App;
import com.anatolf.tvchat.BuildConfig;
import com.anatolf.tvchat.model.FireBaseChatMessage;
import com.anatolf.tvchat.model.Message;
import com.anatolf.tvchat.utils.FirebaseConstants;
import com.anatolf.tvchat.utils.PrefsConstants;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.vk.sdk.VKSdk;
import com.vk.sdk.api.VKApiConst;
import com.vk.sdk.api.VKParameters;
import com.vk.sdk.api.VKRequest;
import com.vk.sdk.api.VKResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.security.MessageDigest;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import ru.ok.android.sdk.Odnoklassniki;

public class ChatModel {

    private FirebaseDatabase database;
    private DatabaseReference commentsRef;
    private DatabaseReference usersInChannelRef;

    private ArrayList<FireBaseChatMessage> fireBaseMessages = new ArrayList<>();
    private ArrayList<String> fireBaseIds = new ArrayList<>();

    private final ArrayList<Message> vkMessages = new ArrayList<>();
    private final ArrayList<Message> okMessages = new ArrayList<>();

    private Odnoklassniki odnoklassniki;


    private Set<String> blockIds = new HashSet<>();

    private String channel_id = "";
    private String firebase_channel_id = "";

    private Timer timer;
    private TimerTask timerTask;
    private Timer timer2;
    private TimerTask timerTask2;

    private GetMessageListener listener;

    ChatModel(String channel_id, String firebase_channel_id) {
        // подключили базу для отправки сообщения на fireBase в методе sendMessage()

        this.channel_id = channel_id;
        this.firebase_channel_id = firebase_channel_id;

        database = FirebaseDatabase.getInstance();
        commentsRef = database.getReference(FirebaseConstants.COMMENTS).child(channel_id); // channel_id - это "1TV", "2TV", "3TV"...
        odnoklassniki = App.getOdnoklassniki();
    }

    void setLike(Message message, boolean enabled) {


        final String current_user_id_vk = App.get().getPrefs().getString(PrefsConstants.USER_VK_ID, "");
        final String current_user_id_ok = App.get().getPrefs().getString(PrefsConstants.USER_Ok_ID, "");

        DatabaseReference likeRef = database.getReference(FirebaseConstants.COMMENTS)
                .child(channel_id)
                .child(message.getFireBase_id())
                .child(FirebaseConstants.LIKED_USERS);

        if (!TextUtils.isEmpty(current_user_id_vk)) {
            if (enabled) {
                likeRef.child(current_user_id_vk).setValue(true);
            } else {
                likeRef.child(current_user_id_vk).removeValue();
            }
        }

        if (!TextUtils.isEmpty(current_user_id_ok)) {
            if (enabled) {
                likeRef.child(current_user_id_ok).setValue(true);
            } else {
                likeRef.child(current_user_id_ok).removeValue();
            }
        }
    }

    void incrementOnlineUsersCountInChat() {
        if (!TextUtils.isEmpty(channel_id)) {
            usersInChannelRef = database.getReference(FirebaseConstants.CHANNELS)
                    .child(firebase_channel_id)
                    .child(FirebaseConstants.COUNT_USERS);

            final String current_user_id_vk = App.get().getPrefs().getString(PrefsConstants.USER_VK_ID, "");
            final String current_user_id_ok = App.get().getPrefs().getString(PrefsConstants.USER_Ok_ID, "");

            if (!TextUtils.isEmpty(current_user_id_vk)) {
                usersInChannelRef.child(current_user_id_vk).setValue(true);
            }
            if (!TextUtils.isEmpty(current_user_id_ok)) {
                usersInChannelRef.child(current_user_id_ok).setValue(true);
            }
        }
    }

    void decrementOnlineUsersCountInChat() {
        if (!TextUtils.isEmpty(channel_id)) {
            usersInChannelRef = database.getReference(FirebaseConstants.CHANNELS)
                    .child(firebase_channel_id)
                    .child(FirebaseConstants.COUNT_USERS);

            final String current_user_id_vk = App.get().getPrefs().getString(PrefsConstants.USER_VK_ID, "");
            final String current_user_id_ok = App.get().getPrefs().getString(PrefsConstants.USER_Ok_ID, "");

            if (!TextUtils.isEmpty(current_user_id_vk)) {
                usersInChannelRef.child(current_user_id_vk).removeValue();
            }
            if (!TextUtils.isEmpty(current_user_id_ok)) {
                usersInChannelRef.child(current_user_id_ok).removeValue();
            }
        }
    }

    void getAllMessages(final GetMessageListener listener) {
        clearMessages();

        this.listener = listener;

        commentsRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                final FireBaseChatMessage fireBaseChatMessage = dataSnapshot.getValue(FireBaseChatMessage.class);

                // общая проверка приходящих сообщений на дубликат (String s - это уникальный код предыдущего сообщения, но при дублировании можно ловить по нему)
                if (!blockIds.contains(s)) { // не пропускает дубликат (s пример: "LqbDjO_PdRZ9EI_o-V1")
                    blockIds.add(s);

                    if (!TextUtils.isEmpty(fireBaseChatMessage.user_id)) {
                        fireBaseMessages.add(fireBaseChatMessage);
                        fireBaseIds.add(dataSnapshot.getKey());
                    }


                    // если пользователь ещё не прошёл регистрацию Через ВК или ОК то:
                    if (!VKSdk.isLoggedIn() && TextUtils.isEmpty(odnoklassniki.getMAccessToken())) {

                        Date date = new Date(fireBaseChatMessage.timeStamp);
                        DateFormat formatter = new SimpleDateFormat("HH:mm:ss");
                        formatter.setTimeZone(TimeZone.getTimeZone("Europe/Moscow"));
                        String currentTime = formatter.format(date);

                        //  конструктор № 1: создаёт сообщения с рандомными Именами собеседников и рандомным Цветом сообщения:
                        Message singleMessage = new Message(
                                fireBaseChatMessage.message,
                                currentTime,
                                fireBaseChatMessage.liked_users,
                                dataSnapshot.getKey());

                        listener.onUpdateSingleMessage(singleMessage);
                    } else {

                        // Таймер с задержкой 0,5 секунды, чтобы получить ответы Vk, Ok (аватарки, имена)
                        if (timer != null && timerTask != null) {
                            timer.cancel();
                            timerTask.cancel();
                        }
                        timerTask = new TimerTask() {
                            @Override
                            public void run() {
                                createMessagesToShow();
                            }
                        };
                        timer = new Timer();
                        timer.schedule(timerTask, 500);
                    }
                }


                if (timer2 != null && timerTask2 != null) {
                    timer2.cancel();
                    timerTask2.cancel();
                }
                timerTask2 = new TimerTask() {
                    @Override
                    public void run() {
                        listener.onShowNewMessages();
                    }
                };
                timer2 = new Timer();
                timer2.schedule(timerTask2, 2000);

            }

            @Override
            public void onChildChanged(final DataSnapshot dataSnapshot, String s) {
                listener.onLikeChanged(dataSnapshot);
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    void clearMessages() {
        fireBaseMessages.clear();
        fireBaseIds.clear();
        vkMessages.clear();
        okMessages.clear();
    }


    private void createMessagesToShow() {
        // Делаем запрос, получаем ответ от ВК, заполняем временные Списки:
        getVkMessages(new OnCompleteMessagesListener() {
            @Override
            public void onComplete() {
                // Делаем запрос, получаем ответ от Ок и заполняем временные Списки:
                getOkMessages(new OnCompleteMessagesListener() {
                    @Override
                    public void onComplete() {
                        mergeAllMessages();
                    }
                });
            }
        });
    }


    private void mergeAllMessages() {
        // Log.d(TAG, "in mergeAllMessages()");

        for (int i = 0; i < fireBaseMessages.size(); i++) {

            if (fireBaseMessages.get(i).social_tag.equals("VK")) {
                for (int j = 0; j < vkMessages.size(); j++) {

                    Date date = new Date(fireBaseMessages.get(i).timeStamp);
                    DateFormat formatter = new SimpleDateFormat("HH:mm:ss");
                    formatter.setTimeZone(TimeZone.getTimeZone("Europe/Moscow"));
                    String timeToCompare = formatter.format(date);

                    // сравниваем по id и по времени отправки сообщения перед отображением
                    if (fireBaseMessages.get(i).user_id.equals(vkMessages.get(j).getId())
                            && timeToCompare.equals(vkMessages.get(j).getTime())) {


                        if (listener != null) {
                            listener.onUpdateSingleMessage(vkMessages.get(j));
                        }

                        final String current_user_id_vk = App.get().getPrefs().getString(PrefsConstants.USER_VK_ID, "");  // достали из SharedPreferences id_vk  пользователя

                        if (vkMessages.get(j).getId().equals(current_user_id_vk)) {  // для скролла вниз при добавлении юзером нового сообщения
                            if (listener != null) {
                                listener.onAddMyNewMessage();
                            }
                        }
                    }
                }
            }

            if (fireBaseMessages.get(i).social_tag.equals("OK")) {
                for (int j = 0; j < okMessages.size(); j++) {

                    Date date = new Date(fireBaseMessages.get(i).timeStamp);
                    DateFormat formatter = new SimpleDateFormat("HH:mm:ss");
                    formatter.setTimeZone(TimeZone.getTimeZone("Europe/Moscow"));
                    String timeToCompare = formatter.format(date);

                    // сравниваем по id и по времени отправки сообщения перед отображением
                    if (fireBaseMessages.get(i).user_id.equals(okMessages.get(j).getId())
                            && timeToCompare.equals(okMessages.get(j).getTime())) {


                        if (listener != null) {
                            listener.onUpdateSingleMessage(okMessages.get(j));
                        }

                        final String current_user_id_ok = App.get().getPrefs().getString(PrefsConstants.USER_Ok_ID, "");  // достали из SharedPreferences id_Ok  пользователя

                        if (okMessages.get(j).getId().equals(current_user_id_ok)) {   // для скролла вниз при добавлении юзером нового сообщения
                            if (listener != null) {
                                listener.onAddMyNewMessage();
                            }
                        }
                    }
                }
            }
        }

        // очищаем временные списки:
        fireBaseMessages.clear();
        fireBaseIds.clear();
        vkMessages.clear();
        okMessages.clear();

    }

    private void getOkMessages(final OnCompleteMessagesListener listener) {
        final ArrayList<String> jsonIdsOk = new ArrayList<>();
        final ArrayList<String> jsonFirstNamesOk = new ArrayList<>();
        //final ArrayList<String> jsonLastNamesOk = new ArrayList<>();
        final ArrayList<String> jsonAvatarsOk = new ArrayList<>();


        String okUsersIdsStr = "";
        for (int j = 0; j < fireBaseMessages.size(); j++) { // проверяем - если от Oк, то добавляем id к запросу
            if (fireBaseMessages.get(j).social_tag.equals("OK")) {
                okUsersIdsStr = okUsersIdsStr + fireBaseMessages.get(j).user_id + ",";
            }
        }

        if (TextUtils.isEmpty(okUsersIdsStr)) {
            if (listener != null) {
                listener.onComplete();
            }
            return;
        }

        Call call = App.getOdnoklassnikiService().fbDo(
                BuildConfig.OK_APP_KEY,
                "FIRST_NAME,LAST_NAME,PIC_1",
                "json",
                "users.getInfo",
                okUsersIdsStr,
                getSig(
                        BuildConfig.OK_APP_KEY,
                        "FIRST_NAME,LAST_NAME,PIC_1",
                        "json",
                        "users.getInfo",
                        okUsersIdsStr,
                        BuildConfig.OK_SECRET_KEY),  // secretKey App
                BuildConfig.OK_GLOBAL_ACCESS_TOKEN);  // global AccessToken App


        call.enqueue(new Callback() {
            @Override
            public void onResponse(Call call, Response response) {
                //Log.d(TAG, "Odnoklassniki - from my application, with Global Key, onResponse: " + response.toString());

                try {

                    JSONArray jsonArray = new JSONArray((ArrayList) response.body());
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject userInfo = jsonArray.getJSONObject(i);

                        String jsonId = userInfo.getString("uid");
                        String firstName = userInfo.getString("first_name");
                        String lastName = userInfo.getString("last_name");
                        String avatarPhoto = userInfo.getString("pic_1");

                        jsonIdsOk.add(jsonId);
                        jsonFirstNamesOk.add(firstName);
                        //jsonLastNamesOk.add(lastName);
                        jsonAvatarsOk.add(avatarPhoto);
                        //Log.d(TAG, "Одноклассники requestAsync: onSuccess, jsonId=  " + jsonId + ", firstName = " + firstName + ", lastName = " + lastName + ", avatarPhoto = " + avatarPhoto);
                    }

                    // достали из SharedPreferences id_vk  пользователя, для проверки Юзер (я/не я?)

                    final String current_user_id_ok = App.get().getPrefs().getString(PrefsConstants.USER_Ok_ID, "");

                    // второй цикл сравнивает каждый проход с id полученными от OК jsonId
                    for (int i = 0; i < fireBaseMessages.size(); i++) {
                        for (int j = 0; j < jsonIdsOk.size(); j++) {
                            if (fireBaseMessages.get(i).user_id.equals(jsonIdsOk.get(j))) {

                                String id = fireBaseMessages.get(i).user_id;
                                String message = fireBaseMessages.get(i).message;
                                HashMap<String, Boolean> liked_users = fireBaseMessages.get(i).liked_users;
                                String fire_base_Id = fireBaseIds.get(i);

                                Date date = new Date(fireBaseMessages.get(i).timeStamp);
                                DateFormat formatter = new SimpleDateFormat("HH:mm:ss");
                                formatter.setTimeZone(TimeZone.getTimeZone("Europe/Moscow"));
                                String currentTime = formatter.format(date);

                                String name = jsonFirstNamesOk.get(j);
                                String ava = jsonAvatarsOk.get(j);

                                boolean belongToCurrentUser; // флаг Юзер я, не я?
                                if (id.equals(current_user_id_ok)) {
                                    belongToCurrentUser = true;
                                } else {
                                    belongToCurrentUser = false;
                                }

                                Message singleMessage = new Message(
                                        id, message, currentTime, belongToCurrentUser,
                                        name, ava, liked_users, fire_base_Id);
                                okMessages.add(singleMessage);
                            }
                        }
                    }
                    // очищаем временные списки:
                    jsonIdsOk.clear();
                    jsonFirstNamesOk.clear();
                    //jsonLastNamesOk.clear();
                    jsonAvatarsOk.clear();

                    // Odnoklassniki messages Ready !!!
                    if (listener != null) {
                        listener.onComplete();
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void onFailure(Call call, Throwable t) {
                // todo view.sendError(t)
                //Log.d(TAG, "Odnoklassniki - from my application, with Global Key, ERROR onFailure:  " + t.getMessage());
            }
        });

    }

    private void getVkMessages(final OnCompleteMessagesListener listener) {

        final ArrayList<String> jsonIdsVk = new ArrayList<>();
        final ArrayList<String> jsonFirstNamesVk = new ArrayList<>();
        //final ArrayList<String> jsonLastNamesVk = new ArrayList<>();
        final ArrayList<String> jsonAvatarsVk = new ArrayList<>();

        String vkUsersIdsStr = "";
        for (int i = 0; i < fireBaseMessages.size(); i++) { // проверяем - если от Вк, то добавляем id к запросу
            if (fireBaseMessages.get(i).social_tag.equals("VK")) {
                vkUsersIdsStr = vkUsersIdsStr + fireBaseMessages.get(i).user_id + ",";
            }
        }

        if (TextUtils.isEmpty(vkUsersIdsStr)) {
            if (listener != null) {
                listener.onComplete();
            }
            return;
        }

        VKRequest vkRequest = new VKRequest(
                "users.get",
                VKParameters.from(
                        "access_token", BuildConfig.VK_GLOBAL_ACCESS_TOKEN,
                        VKApiConst.USER_IDS, vkUsersIdsStr,
                        VKApiConst.FIELDS, "sex,photo_50"));

        vkRequest.executeWithListener(new VKRequest.VKRequestListener() {
            @Override
            public void onComplete(VKResponse response) {
                //Log.d(TAG, "VK - from my application, with Global Key, onComplete: " + response.responseString);
                // получаем ответ от ApiVk и парсим json каждого участника чата
                try {
                    JSONObject jsonResponse = new JSONObject(response.responseString);
                    JSONArray jsonArray = jsonResponse.getJSONArray("response");
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject userInfo = jsonArray.getJSONObject(i);
                        String jsonId = userInfo.getString("id");
                        String firstName = userInfo.getString("first_name");
                        //String lastName = userInfo.getString("last_name");
                        String avatarPhoto = userInfo.getString("photo_50");

                        jsonIdsVk.add(jsonId);
                        jsonFirstNamesVk.add(firstName);
                        //jsonLastNamesVk.add(lastName);
                        jsonAvatarsVk.add(avatarPhoto);
                        //Log.d(TAG, "в Цикле getVkUsersInfo: jsonId = " + jsonId + ", Имя = " + firstName + ", АВА = " + avatarPhoto);
                    }


                    // достали из SharedPreferences id_vk  пользователя, для проверки Юзер (я/не я?)
                    final String current_user_id_vk = App.get().getPrefs().getString(PrefsConstants.USER_VK_ID, "");

                    // второй цикл сравнивает каждый проход с id полученными от ВК jsonId
                    for (int i = 0; i < fireBaseMessages.size(); i++) {
                        for (int j = 0; j < jsonIdsVk.size(); j++) {
                            if (fireBaseMessages.get(i).user_id.equals(jsonIdsVk.get(j))) {

                                String id = fireBaseMessages.get(i).user_id;
                                String message = fireBaseMessages.get(i).message;
                                HashMap<String, Boolean> liked_users = fireBaseMessages.get(i).liked_users;
                                String fire_base_Id = fireBaseIds.get(i);

                                Date date = new Date(fireBaseMessages.get(i).timeStamp);
                                DateFormat formatter = new SimpleDateFormat("HH:mm:ss");
                                formatter.setTimeZone(TimeZone.getTimeZone("Europe/Moscow"));
                                String currentTime = formatter.format(date);

                                String name = jsonFirstNamesVk.get(j);
                                String ava = jsonAvatarsVk.get(j);

                                boolean belongToCurrentUser; // флаг Юзер я, не я?
                                if (id.equals(current_user_id_vk)) {
                                    belongToCurrentUser = true;
                                } else {
                                    belongToCurrentUser = false;
                                }

                                Message singleMessage = new Message(
                                        id, message, currentTime, belongToCurrentUser,
                                        name, ava, liked_users, fire_base_Id);

                                vkMessages.add(singleMessage);
                            }
                        }
                    }
                    // очищаем временные списки:
                    jsonIdsVk.clear();
                    jsonFirstNamesVk.clear();
                    //jsonLastNamesVk.clear();
                    jsonAvatarsVk.clear();

                    // vk messages Ready !!!
                    if (listener != null) {
                        listener.onComplete();
                    }

                } catch (JSONException e) {
                    // todo view.sendError(t)
                    e.printStackTrace();
                }
            }
        });
    }

    private String getSig(String applicationKey,
                          String fields,
                          String format,
                          String method,
                          String uids,
                          String secretKey) {


        return toMD5("application_key=" + applicationKey +
                "fields=" + fields +
                "format=" + format +
                "method=" + method +
                "uids=" + uids +
                secretKey);

    }


    private String toMD5(String toEncrypt) {
        try {
            MessageDigest digest = MessageDigest.getInstance("md5");
            digest.update(toEncrypt.getBytes());
            byte[] bytes = digest.digest();
            StringBuilder sb = new StringBuilder();
            for (byte i : bytes) {
                sb.append(String.format("%02X", i));
            }
            return sb.toString().toLowerCase();
        } catch (Exception exc) {
            throw new IllegalStateException(exc);
        }

    }

    public boolean isNotAuth() {
        return !VKSdk.isLoggedIn() && TextUtils.isEmpty(odnoklassniki.getMAccessToken());
    }

    public void sendMessage(String message) {
        final String current_user_id_vk = App.get().getPrefs().getString(PrefsConstants.USER_VK_ID, "");
        final String current_user_id_ok = App.get().getPrefs().getString(PrefsConstants.USER_Ok_ID, "");

        //отправляет введённое сообщение в Firebase c тегом ВК
        if (message.length() > 0 && !TextUtils.isEmpty(current_user_id_vk)) {
            long time_stamp = System.currentTimeMillis();

            FireBaseChatMessage fireBaseChatMessage = new FireBaseChatMessage(
                    current_user_id_vk,
                    message,
                    time_stamp,
                    "VK",
                    new HashMap<String, Boolean>());

            commentsRef.push().setValue(fireBaseChatMessage);
        }

        //отправляет введённое сообщение в Firebase c тегом ОК
        if (message.length() > 0 && !TextUtils.isEmpty(current_user_id_ok)) {
            long time_stamp = System.currentTimeMillis();

            FireBaseChatMessage fireBaseChatMessage = new FireBaseChatMessage(
                    current_user_id_ok,
                    message,
                    time_stamp,
                    "OK",
                    new HashMap<String, Boolean>());

            commentsRef.push().setValue(fireBaseChatMessage);
        }
    }


    public interface GetMessageListener {
        void onShowNewMessages();

        void onUpdateSingleMessage(Message message);

        void onLikeChanged(DataSnapshot dataSnapshot);

        void onAddMyNewMessage();
    }


    public interface OnCompleteMessagesListener {
        void onComplete();
    }
}

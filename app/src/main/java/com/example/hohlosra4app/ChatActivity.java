package com.example.hohlosra4app;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.hohlosra4app.Model.Message;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.IgnoreExtraProperties;
import com.google.firebase.database.Query;
import com.vk.sdk.VKAccessToken;
import com.vk.sdk.VKCallback;
import com.vk.sdk.VKScope;
import com.vk.sdk.VKSdk;
import com.vk.sdk.api.VKApiConst;
import com.vk.sdk.api.VKError;
import com.vk.sdk.api.VKParameters;
import com.vk.sdk.api.VKRequest;
import com.vk.sdk.api.VKResponse;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.security.MessageDigest;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import ru.ok.android.sdk.Odnoklassniki;
import ru.ok.android.sdk.OkListener;
import ru.ok.android.sdk.OkRequestMode;
import ru.ok.android.sdk.util.OkAuthType;
import ru.ok.android.sdk.util.OkScope;

public class ChatActivity extends AppCompatActivity {
    private static final String TAG = "myLogs";

    private static final String USER_VK_ID = "user_shared_pref_id_key";
    private static final String USER_VK_EMAIL = "user_shared_pref_email_key";
    private static final String USER_VK_ACCESS_TOKEN = "user_shared_pref_access_token_key";

    private static final String USER_Ok_ID = "user_shared_pref_id_key_odnoklassniki";


    private EditText editText;
    private MessageAdapter messageAdapter;
    private ListView messagesView;

    private FirebaseDatabase database;
    private DatabaseReference myRef;

    // for registration window
    private RelativeLayout root_chat;

    private ArrayList<String> usersIds = new ArrayList<>();
    private ArrayList<Long> usersTimeStamps = new ArrayList<>();
    private ArrayList<String> usersMessages = new ArrayList<>();
    private ArrayList<String> usersSocialTags = new ArrayList<>();

    // for merge messages before showing
    final ArrayList<Message> vkMessages = new ArrayList<>();
    final ArrayList<Message> okMessages = new ArrayList<>();


    // for VK api
    private String[] scope = new String[]{VKScope.EMAIL, VKScope.FRIENDS, VKScope.PHOTOS};
    // for Ok api
    private Odnoklassniki odnoklassniki;

    SharedPreferences sPref;

    Set<String> blockIds = new HashSet<>();

    private String channel_id = "";
    int usersIntoChat = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //Log.d(TAG, "in onCreate ChatActivity");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        editText = (EditText) findViewById(R.id.editText);

        messageAdapter = new MessageAdapter(this);
        messagesView = (ListView) findViewById(R.id.messages_view);
        // при добавлении нового сообщения сразу же его отображает (скроллит список вниз)
        // ("вниз" установлено в activity_chat - android:stackFromBottom="true")
        messagesView.setTranscriptMode(ListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
        messagesView.setAdapter(messageAdapter);

        root_chat = findViewById(R.id.root_element_chat);

// получаем Интент из Майн
        Intent intentFromMain = getIntent();
        if (intentFromMain.hasExtra(MainActivity.CHANNEL_ID_EXTRA) && intentFromMain.hasExtra(MainActivity.USERS_IN_CHAT_EXTRA)) {
            channel_id = intentFromMain.getStringExtra(MainActivity.CHANNEL_ID_EXTRA);
            usersIntoChat = intentFromMain.getIntExtra(MainActivity.USERS_IN_CHAT_EXTRA, 0);

            Toast.makeText(ChatActivity.this,
                    "Мы в чате канала: " + channel_id + ", количество обсуждающих: " + usersIntoChat,
                    Toast.LENGTH_SHORT).show();
        }


        // подключили базу для отправки сообщения на fireBase в методе sendMessage()
        database = FirebaseDatabase.getInstance();
        //        database.setPersistenceEnabled(true);  // добавление элементов во время оффлайн
        myRef = database.getReference("Comments").child(channel_id); // channel_id - это "1TV", "2TV", "3TV"...
        //   myRef.removeValue();  // удалить из базы весь раздел "1TV"  и всё что внутри (комментарии "Comments")
        //  myRef = database.getReference("items").child("users").child("newUser"); // многопользовательская ветка

        odnoklassniki = App.getOdnoklassniki();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //Log.d(TAG, "in onActivityResult before registration Vk & Ok ");

        // проверяем зарегестрировался ли пользователь через ВК:
        if (!VKSdk.onActivityResult(requestCode, resultCode, data, new VKCallback<VKAccessToken>() {
            @Override
            public void onResult(VKAccessToken res) {
                // Пользователь успешно авторизовался через VK
                // Сохраняем его Id, email, access_token в SharedPreferences
                sPref = getPreferences(MODE_PRIVATE);
                SharedPreferences.Editor editor = sPref.edit();
                editor.putString(USER_VK_ID, res.userId);
                editor.putString(USER_VK_EMAIL, res.email);
                editor.putString(USER_VK_ACCESS_TOKEN, res.accessToken);
                editor.apply();

                Toast.makeText(ChatActivity.this,
                        "Теперь вы можете отправлять сообщения!",
                        Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(VKError error) {
                // Произошла ошибка авторизации (например, пользователь запретил авторизацию)
            }
        }))


            // проверяем зарегестрировался ли пользователь через ОК:
            if (Odnoklassniki.Companion.of(this).isActivityRequestOAuth(requestCode)) {  // web
                boolean isAuthCompleted = !TextUtils.isEmpty(data.getStringExtra("access_token"));
                if (isAuthCompleted) {
                    Log.d(TAG, "isActivityRequestOAuth: Пользователь успешно авторизовался через веб-форму Odnoklassniki (web)");

                    odnoklassniki.onAuthActivityResult(requestCode, resultCode, data, new OkListener() {
                        @Override
                        public void onSuccess(@NotNull JSONObject jsonObject) {
                            Log.d(TAG, "requestAsync: onSuccess " + jsonObject.toString());

                            // очищаем временные списки (или происходит дублирование всех сообщений)
                            usersIds.clear();
                            usersMessages.clear();
                            usersTimeStamps.clear();
                            usersSocialTags.clear();
                            vkMessages.clear();
                            okMessages.clear();


                            // берём информацию о текущем юзере ОК и записываем в SharedPreference:
                            odnoklassniki.requestAsync(
                                    "users.getCurrentUser",
                                    null,
                                    OkRequestMode.getDEFAULT(),  // EnumSet.of(OkRequestMode.SDK_SESSION)
                                    new OkListener() {
                                        @Override
                                        public void onSuccess(@NotNull JSONObject jsonObject) {
                                            Log.d(TAG, "Одноклассники, ответ при регистрации (web) onSuccess, jsonObject= " + jsonObject.toString());

                                            try {
                                                String jsonId = jsonObject.getString("uid");

                                                sPref = getPreferences(MODE_PRIVATE);
                                                SharedPreferences.Editor editor = sPref.edit();
                                                editor.putString(USER_Ok_ID, jsonId);
                                                editor.apply();

                                            } catch (JSONException e) {
                                                e.printStackTrace();
                                            }
                                        }

                                        @Override
                                        public void onError(@Nullable String s) {
                                            Log.d(TAG, "Odnoklassniki !!! requestAsync: onError " + s);
                                        }
                                    });

                        }

                        @Override
                        public void onError(@Nullable String s) {
                            Log.d(TAG, "(((((((((( requestAsync: Error " + s);

                        }
                    });


                } else {
                    Log.d(TAG, "isActivityRequestOAuth: авторизация через веб-форму Odnoklassniki НЕ ПРОШЛА !!!");
                }


            } else if (Odnoklassniki.Companion.of(this).isActivityRequestViral(requestCode)) { // native
                boolean isAuthCompleted = !TextUtils.isEmpty(data.getStringExtra("access_token"));
                if (isAuthCompleted) {
                    Log.d(TAG, "isActivityRequestViral: Пользователь успешно авторизовался через мобильное приложение Odnoklassniki (native)");

                    odnoklassniki.onAuthActivityResult(requestCode, resultCode, data, new OkListener() {
                        @Override
                        public void onSuccess(@NotNull JSONObject jsonObject) {
                            Log.d(TAG, "requestAsync: onSuccess " + jsonObject.toString());

                            // очищаем временные списки (или происходит дублирование всех сообщений)
                            usersIds.clear();
                            usersMessages.clear();
                            usersTimeStamps.clear();
                            usersSocialTags.clear();
                            vkMessages.clear();
                            okMessages.clear();


                            // берём информацию о текущем юзере ОК и записываем в SharedPreference:
                            odnoklassniki.requestAsync(
                                    "users.getCurrentUser",
                                    null,
                                    OkRequestMode.getDEFAULT(),  // EnumSet.of(OkRequestMode.SDK_SESSION)
                                    new OkListener() {
                                        @Override
                                        public void onSuccess(@NotNull JSONObject jsonObject) {
                                            Log.d(TAG, "Одноклассники, ответ при регистрации (native) onSuccess, jsonObject= " + jsonObject.toString());

                                            try {
                                                String jsonId = jsonObject.getString("uid");

                                                sPref = getPreferences(MODE_PRIVATE);
                                                SharedPreferences.Editor editor = sPref.edit();
                                                editor.putString(USER_Ok_ID, jsonId);
                                                editor.apply();

                                            } catch (JSONException e) {
                                                e.printStackTrace();
                                            }
                                        }

                                        @Override
                                        public void onError(@Nullable String s) {
                                            Log.d(TAG, "Odnoklassniki, requestAsync: onError " + s);
                                        }
                                    });

                        }

                        @Override
                        public void onError(@Nullable String s) {
                            Log.d(TAG, "requestAsync: Error " + s);

                        }
                    });


                } else {
                    Log.d(TAG, "isActivityRequestViral: авторизация через мобильное приложение Odnoklassniki НЕ ПРОШЛА !!!");
                }
            } else {
                super.onActivityResult(requestCode, resultCode, data);
            }

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


    private void getOkMessages(final OnCompleteMessagesListener listener) {
        final ArrayList<String> jsonIdsOk = new ArrayList<>();
        final ArrayList<String> jsonFirstNamesOk = new ArrayList<>();
        //final ArrayList<String> jsonLastNamesOk = new ArrayList<>();
        final ArrayList<String> jsonAvatarsOk = new ArrayList<>();

        String okUsersIdsStr = "";
        for (int j = 0; j < usersSocialTags.size(); j++) { // бежим по списку тегов, проверяем - если от Oк, то добавляем id к запросу
            if (usersSocialTags.get(j).equals("OK")) {
                okUsersIdsStr = okUsersIdsStr + usersIds.get(j) + ",";
            }
        }

        if(TextUtils.isEmpty(okUsersIdsStr)){
            if (listener != null) {
            listener.onComplete();
            }
            return;
        }

        Map<String, String> parameters = new HashMap<>();
        parameters.put("uids", okUsersIdsStr);
        parameters.put("fields", "FIRST_NAME,LAST_NAME,PIC_1");

        Log.d(TAG, "getOkMessages: usersSocialTags size = " + usersSocialTags.size() + ", ++Str Ids = " + okUsersIdsStr);

        Call call = App.getOdnoklassnikiService().fbDo(
                "CPNKFHJGDIHBABABA",
                "FIRST_NAME,LAST_NAME,PIC_1",
                "json",
                "users.getInfo",
                okUsersIdsStr,
                getSig(
                        "CPNKFHJGDIHBABABA",
                        "FIRST_NAME,LAST_NAME,PIC_1",
                        "json",
                        "users.getInfo",
                        okUsersIdsStr,
                        ""),  // todo secretKey App
                "");  // todo global AccessToken App


        call.enqueue(new Callback() {
            @Override
            public void onResponse(Call call, Response response) {
                //Log.d(TAG, "Odnoklassniki - from my application, with Global Key, onResponse: " + response.toString());

                try {

                    JSONArray jsonArray = new JSONArray((ArrayList)response.body());
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
                    // первый цикл бежит по всем списку id сообщений полученых от FireBase
                    // второй цикл сравнивает каждый проход с id полученными от ВК jsonId (списки jsonIds, jsonFirstNames, jsonAvatars - идентичны по размеру - для дальнейшего сопоставления)
                    for (int i = 0; i < usersIds.size(); i++) {
                        for (int j = 0; j < jsonIdsOk.size(); j++) {
                            if (usersIds.get(i).equals(jsonIdsOk.get(j))) {

                                String id = usersIds.get(i);
                                String message = usersMessages.get(i);

                                Date date = new Date(usersTimeStamps.get(i));
                                DateFormat formatter = new SimpleDateFormat("HH:mm:ss");
                                formatter.setTimeZone(TimeZone.getTimeZone("Europe/Moscow"));
                                String currentTime = formatter.format(date);

                                String name = jsonFirstNamesOk.get(j);
                                String ava = jsonAvatarsOk.get(j);

                                boolean belongToCurrentUser = false; // флаг Юзер я, не я?
//                            if (id.equals(current_user_id_vk)) {  // мой id вконтакте
//                                belongToCurrentUser = true;
//                            } else {
//                                belongToCurrentUser = false;
//                            }

                                //  для конструктора № 2: создаёт сообщения с аватарками из ОК и ВК:
                                Message singleMessage = new Message(id, message, currentTime, belongToCurrentUser, name, ava);
                                //messageAdapter.add(singleMessage);  // посылаем на отображение
                                okMessages.add(singleMessage);

                                Log.d(TAG, "в Цикле For ОДНОКЛАССНИКИ ___________________: id = " + id + ", message = " + message
                                        + ", currentTime = " + currentTime + ", name = "
                                        + name + ", ava = " + ava + ", belongToCurrentUser = " + belongToCurrentUser);

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
        for (int i = 0; i < usersSocialTags.size(); i++) { // бежим по списку тегов, проверяем - если от Вк, то добавляем id к запросу
            if (usersSocialTags.get(i).equals("VK")) {
                vkUsersIdsStr = vkUsersIdsStr + usersIds.get(i) + ",";
                //countUsersVk++;
            }
        }

        if(TextUtils.isEmpty(vkUsersIdsStr)){
            if (listener != null) {
                listener.onComplete();
            }
            return;
        }

        VKRequest vkRequest = new VKRequest(
                "users.get",
                VKParameters.from(
                        "access_token", "",  // TODO: PLACE THIS accsess token VK
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
                        Log.d(TAG, "в Цикле getVkUsersInfo: jsonId = " + jsonId + ", Имя = " + firstName + ", АВА = " + avatarPhoto);
                    }


                    // достали из SharedPreferences id_vk  пользователя, для проверки Юзер (я/не я?)
//                sPref = getPreferences(MODE_PRIVATE);
//                final String current_user_id_vk = sPref.getString(USER_VK_ID, "");

                    // первый цикл бежит по всем списку id сообщений полученых от FireBase
                    // второй цикл сравнивает каждый проход с id полученными от ВК jsonId (списки jsonIds, jsonFirstNames, jsonAvatars - идентичны по размеру - для дальнейшего сопоставления)
                    for (int i = 0; i < usersIds.size(); i++) {
                        for (int j = 0; j < jsonIdsVk.size(); j++) {
                            if (usersIds.get(i).equals(jsonIdsVk.get(j))) {

                                String id = usersIds.get(i);
                                String message = usersMessages.get(i);

                                Date date = new Date(usersTimeStamps.get(i));
                                DateFormat formatter = new SimpleDateFormat("HH:mm:ss");
                                formatter.setTimeZone(TimeZone.getTimeZone("Europe/Moscow"));
                                String currentTime = formatter.format(date);

                                String name = jsonFirstNamesVk.get(j);
                                String ava = jsonAvatarsVk.get(j);

                                boolean belongToCurrentUser = false; // флаг Юзер я, не я?
//                            if (id.equals(current_user_id_vk)) {  // мой id вконтакте
//                                belongToCurrentUser = true;
//                            } else {
//                                belongToCurrentUser = false;
//                            }

                                //  конструктор № 2: создаёт сообщения с аватарками из ВК:
                                Message singleMessage = new Message(id, message, currentTime, belongToCurrentUser, name, ava);
                                //messageAdapter.add(singleMessage);  // посылаем на отображение
                                vkMessages.add(singleMessage);
                                Log.d(TAG, "в Цикле For ВКОНТАКТЕ ____________________: id = " + id + ", message = " + message
                                        + ", currentTime = " + currentTime + ", name = "
                                        + name + ", ava = " + ava + ", belongToCurrentUser = " + belongToCurrentUser);

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
                    e.printStackTrace();
                }
            }
        });
    }

    public interface OnCompleteMessagesListener {
        void onComplete();
    }

    private void mergeAllMessages() {
        Log.d(TAG, "in mergeAllMessages()");

        for (int i = 0; i < usersSocialTags.size(); i++) {

            if (usersSocialTags.get(i).equals("VK")) {
                for (int j = 0; j < vkMessages.size(); j++) {

                    Date date = new Date(usersTimeStamps.get(i));
                    DateFormat formatter = new SimpleDateFormat("HH:mm:ss");
                    formatter.setTimeZone(TimeZone.getTimeZone("Europe/Moscow"));
                    String timeToCompare = formatter.format(date);

                    // сравниваем по id и по времени отправки сообщения перед отображением
                    if (usersIds.get(i).equals(vkMessages.get(j).getId()) && timeToCompare.equals(vkMessages.get(j).getTime())) {
                        messageAdapter.add(vkMessages.get(j));  // посылаем на отображение
                    }
                }
            }

            if (usersSocialTags.get(i).equals("OK")) {
                for (int j = 0; j < okMessages.size(); j++) {

                    Date date = new Date(usersTimeStamps.get(i));
                    DateFormat formatter = new SimpleDateFormat("HH:mm:ss");
                    formatter.setTimeZone(TimeZone.getTimeZone("Europe/Moscow"));
                    String timeToCompare = formatter.format(date);

                    // сравниваем по id и по времени отправки сообщения перед отображением
                    if (usersIds.get(i).equals(okMessages.get(j).getId()) && timeToCompare.equals(okMessages.get(j).getTime())) {
                        messageAdapter.add(okMessages.get(j));  // посылаем на отображение
                    }
                }
            }
        }

        // очищаем временные списки:
        usersIds.clear();
        usersMessages.clear();
        usersTimeStamps.clear();
        usersSocialTags.clear();
        vkMessages.clear();
        okMessages.clear();

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


    @Override
    protected void onResume() {
        super.onResume();

        // очищаем в адаптере Message список messages перед каждым перезапуском активити
        messageAdapter.messages.clear();
        // очищаем специальный список id, для дублирующих сообщений из FireBase
        blockIds.clear();
        showAllMessages();
    }

    Timer timer;
    TimerTask timerTask;

    private void showAllMessages() {

        // делаем запрос в базу данных firebase
        Query myQuery = myRef;
        //  Query myQuery = myRef.orderByChild("numberChannel").equalTo(111);   // редактирование: сортирует ответ по "numberChannel" и 111
        myQuery.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                /////// Получаем из FireBase из раздела "1TV", подраздела "Comments" по одному объекту FireBaseChatMessage
                /////// и отправляем его в messageAdapter для отображения
                FireBaseChatMessage fireBaseChatMessage = dataSnapshot.getValue(FireBaseChatMessage.class);

                // общая проверка приходящих сообщений на дубликат (String s - это уникальный код предыдущего сообщения, но при дублировании можно ловить по нему)
                //Log.d(TAG, "fireBaseChatMessage, S-code = " + s);
                if (!blockIds.contains(s)) { // не пропускает дубликат (s пример: "LqbDjO_PdRZ9EI_o-V1")
                    blockIds.add(s);

                    // берём приходящее сообщение из базы и добавляем все его параметры в листы:
                    if (!TextUtils.isEmpty(fireBaseChatMessage.user_id)) {
                        usersIds.add(fireBaseChatMessage.user_id);
                        usersTimeStamps.add(fireBaseChatMessage.timeStamp);
                        usersMessages.add(fireBaseChatMessage.message);
                        usersSocialTags.add(fireBaseChatMessage.social_tag);
                    }

                    // берём дату из fireBaseChatMessage и переводим её в "00:00:00" по Москве (для первого конструктора, ниже)
                    Date date = new Date(fireBaseChatMessage.timeStamp);
                    DateFormat formatter = new SimpleDateFormat("HH:mm:ss");
                    formatter.setTimeZone(TimeZone.getTimeZone("Europe/Moscow"));
                    String currentTime = formatter.format(date);


                    // если пользователь ещё не прошёл регистрацию Через ВК или ОК то:
                    if (!VKSdk.isLoggedIn() && TextUtils.isEmpty(odnoklassniki.getMAccessToken())) {
                        //  конструктор № 1: создаёт сообщения с рандомными Именами собеседников и рандомным Цветом сообщения:
                        Message singleMessage = new Message(dataSnapshot.getKey(), fireBaseChatMessage.message, currentTime, false);
                        messageAdapter.add(singleMessage);  // посылаем на отображение
                    } else {

                        // Таймер с задержкой 1 секунда, чтобы получить ответы из ApiVk (аватарки, имена)
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
                        timer.schedule(timerTask, 1000);
                    }
                }

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
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


    // По нажатию кнопки создаём в FireBase новое "сообщение" с введённым текстом из поля EditText
    public void sendMessage(final View view) {

        // Authorization (проверка, получен ли токен от какой либо из соц сетей)
        if (!VKSdk.isLoggedIn() && TextUtils.isEmpty(odnoklassniki.getMAccessToken())) {

// Алерт Дайлог авторизации:
            final AlertDialog.Builder dialog = new AlertDialog.Builder(this);
            dialog.setTitle("Войти:");
            dialog.setMessage("Чтобы отправлять сообщения, зарегестрируйтесь:");

            LayoutInflater inflater = LayoutInflater.from(this);
            View sign_in_window = inflater.inflate(R.layout.sing_in_window2, null);
            dialog.setView(sign_in_window);

            dialog.setNegativeButton("Отменить", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int which) {
                    Snackbar.make(root_chat, "Без регистрации Вы можете только наблюдать", Snackbar.LENGTH_SHORT).show();
                    dialogInterface.dismiss();
                }
            });
            dialog.create();
            final AlertDialog adTrueDialog = dialog.show();  // adTrueDialog для выхода из диалога после нажатия кнопок

// Кнопки в Алерт Дайлог для авторизации:
            final ImageButton btnRegVk = sign_in_window.findViewById(R.id.ib_vk_btn);
            final ImageButton btnRegOk = sign_in_window.findViewById(R.id.ib_ok_btn);
            final ImageButton btnRegMail = sign_in_window.findViewById(R.id.ib_fb_btn);

            // по кнопке "Через Вк"
            btnRegVk.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    VKSdk.login(ChatActivity.this, scope);
                    adTrueDialog.dismiss();
                }
            });

            // по кнопке "Через Ок"
            btnRegOk.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    odnoklassniki.requestAuthorization(ChatActivity.this,
                            "okauth://ok512000154078",
                            OkAuthType.ANY,
                            (OkScope.VALUABLE_ACCESS + ";" + OkScope.LONG_ACCESS_TOKEN));
                    adTrueDialog.dismiss();
                }
            });

            // по кнопке "Через Facebook"
            btnRegMail.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(ChatActivity.this,
                            "функция пока не доступна",
                            Toast.LENGTH_SHORT).show();
                    // сделать а возвращением результата // если через отдельную активити то ниже переход на заготовку
                    // startActivity(new Intent(ChatActivity.this, RegistrationActivity.class));
                    adTrueDialog.dismiss();
                }
            });
            return;
        }

        sPref = getPreferences(MODE_PRIVATE);
        final String current_user_id_vk = sPref.getString(USER_VK_ID, "");  // достали из SharedPreferences id_vk  пользователя

        sPref = getPreferences(MODE_PRIVATE);
        final String current_user_id_ok = sPref.getString(USER_Ok_ID, "");  // достали из SharedPreferences id_Ok  пользователя

        String message = editText.getText().toString();  // получаем сообщение с поля ввода
        Log.d(TAG, "перед отправкой на firebase: current_user_id_vk = " + current_user_id_vk + ", current_user_id_Ok = " + current_user_id_ok);

        //отправляет введённое сообщение в базу данных c тегом ВК
        if (message.length() > 0 && !TextUtils.isEmpty(current_user_id_vk)) {
            long time_stamp = System.currentTimeMillis();   // получаем время отправки сообщения
            //создаем экземпляр одного Cообщения Юзера
            FireBaseChatMessage fireBaseChatMessage = new FireBaseChatMessage(current_user_id_vk, message, time_stamp, "VK");
            // оправляем его в базу данных firebase
            myRef.push().setValue(fireBaseChatMessage);
            messagesView.smoothScrollToPosition(messageAdapter.getCount() - 1);
            editText.getText().clear();  //очищаем поле ввода
        }
        // или:
        //отправляет введённое сообщение в базу данных c тегом ОК
        if (message.length() > 0 && !TextUtils.isEmpty(current_user_id_ok)) {
            long time_stamp = System.currentTimeMillis();   // получаем время отправки сообщения
            //создаем экземпляр одного Cообщения Юзера
            FireBaseChatMessage fireBaseChatMessage = new FireBaseChatMessage(current_user_id_ok, message, time_stamp, "OK");
            // оправляем его в базу данных firebase
            myRef.push().setValue(fireBaseChatMessage);
            messagesView.smoothScrollToPosition(messageAdapter.getCount() - 1);
            editText.getText().clear();  //очищаем поле ввода
        }
    }


    // Класс FireBaseChatMessage - объект одного отправляемого Сообщения Юзера в выбранный чат (для добавления в базу данных firebase)
    @IgnoreExtraProperties
    static class FireBaseChatMessage implements Serializable {

        public String user_id; // = "12103322";
        public String message; // = "кукуепта";
        public long timeStamp; // = System.currentTimeMillis();
        public String social_tag; // = System.currentTimeMillis();

        public FireBaseChatMessage() {
        }

        public FireBaseChatMessage(String user_id, String message, long timeStamp, String social_tag) {
            this.user_id = user_id;
            this.message = message;
            this.timeStamp = timeStamp;
            this.social_tag = social_tag;
        }
    }
}

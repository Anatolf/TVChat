package com.anatolf.tvchat.ui.chat;

import android.text.TextUtils;

import com.anatolf.tvchat.App;
import com.anatolf.tvchat.BuildConfig;
import com.anatolf.tvchat.net.model.FireBaseChatMessage;
import com.anatolf.tvchat.net.model.Message;
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

import org.jetbrains.annotations.NotNull;
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
import java.util.Locale;
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

    private final ArrayList<FireBaseChatMessage> fireBaseMessages = new ArrayList<>();

    private final ArrayList<String> fireBaseIds = new ArrayList<>();
    private final Set<String> blockIds = new HashSet<>();
    private final ArrayList<Message> vkMessages = new ArrayList<>();
    private final ArrayList<Message> okMessages = new ArrayList<>();

    private Odnoklassniki odnoklassniki;

    private String channel_id;
    private String firebase_channel_id;

    private Timer timer;
    private TimerTask timerWaitTaskForSocialResponses;
    private Timer timer2;
    private TimerTask timerWaitTaskAfterSendNewMessage;

    private GetMessageListener listener;

    ChatModel(String channel_id, String firebase_channel_id) {

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

                if (!blockIds.contains(s)) { // blocked duplicate messages 
                    blockIds.add(s);

                    if (!TextUtils.isEmpty(fireBaseChatMessage.user_id)) {
                        fireBaseMessages.add(fireBaseChatMessage);
                        fireBaseIds.add(dataSnapshot.getKey());
                    }

                    if (!VKSdk.isLoggedIn() && TextUtils.isEmpty(odnoklassniki.getMAccessToken())) {

                        String currentTime = getCurrentTime(fireBaseChatMessage);

                        // without registration, make messages with incognito names and avatars
                        Message singleMessage = new Message(
                                fireBaseChatMessage.message,
                                currentTime,
                                fireBaseChatMessage.liked_users,
                                dataSnapshot.getKey());

                        listener.onUpdateSingleMessage(singleMessage);
                    } else {

                        if (timer != null && timerWaitTaskForSocialResponses != null) {
                            timer.cancel();
                            timerWaitTaskForSocialResponses.cancel();
                        }
                        timerWaitTaskForSocialResponses = new TimerTask() {
                            @Override
                            public void run() {
                                createMessagesToShow();
                            }
                        };
                        timer = new Timer();
                        timer.schedule(timerWaitTaskForSocialResponses, 500);
                    }
                }


                if (timer2 != null && timerWaitTaskAfterSendNewMessage != null) {
                    timer2.cancel();
                    timerWaitTaskAfterSendNewMessage.cancel();
                }
                timerWaitTaskAfterSendNewMessage = new TimerTask() {
                    @Override
                    public void run() {
                        listener.onShowNewMessages();
                    }
                };
                timer2 = new Timer();
                timer2.schedule(timerWaitTaskAfterSendNewMessage, 2000);

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
        blockIds.clear();
    }


    private void createMessagesToShow() {
        getVkMessages(new OnCompleteMessagesListener() {
            @Override
            public void onComplete() {
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

        for (int i = 0; i < fireBaseMessages.size(); i++) {

            if (fireBaseMessages.get(i).social_tag.equals("VK")) {
                for (int j = 0; j < vkMessages.size(); j++) {

                    String timeToCompare = getCurrentTime(fireBaseMessages.get(i));

                    if (fireBaseMessages.get(i).user_id.equals(vkMessages.get(j).getId())
                            && timeToCompare.equals(vkMessages.get(j).getTime())) {

                        if (listener != null) {
                            listener.onUpdateSingleMessage(vkMessages.get(j));
                        }

                        final String current_user_id_vk = App.get().getPrefs()
                                .getString(PrefsConstants.USER_VK_ID, "");

                        // to scroll down when user adds a new message
                        if (vkMessages.get(j).getId().equals(current_user_id_vk)) {
                            if (listener != null) {
                                listener.onAddMyNewMessage();
                            }
                        }
                    }
                }
            }

            if (fireBaseMessages.get(i).social_tag.equals("OK")) {
                for (int j = 0; j < okMessages.size(); j++) {

                    String timeToCompare = getCurrentTime(fireBaseMessages.get(i));

                    if (fireBaseMessages.get(i).user_id.equals(okMessages.get(j).getId())
                            && timeToCompare.equals(okMessages.get(j).getTime())) {


                        if (listener != null) {
                            listener.onUpdateSingleMessage(okMessages.get(j));
                        }

                        final String current_user_id_ok = App.get().getPrefs()
                                .getString(PrefsConstants.USER_Ok_ID, "");

                        // to scroll down when user adds a new message
                        if (okMessages.get(j).getId().equals(current_user_id_ok)) {
                            if (listener != null) {
                                listener.onAddMyNewMessage();
                            }
                        }
                    }
                }
            }
        }

        clearMessages();
    }

    private void getOkMessages(final OnCompleteMessagesListener listener) {
        final ArrayList<String> jsonIdsOk = new ArrayList<>();
        final ArrayList<String> jsonFirstNamesOk = new ArrayList<>();
        final ArrayList<String> jsonAvatarsOk = new ArrayList<>();


        String okUsersIdsStr = "";
        for (int j = 0; j < fireBaseMessages.size(); j++) {
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
                        String avatarPhoto = userInfo.getString("pic_1");

                        jsonIdsOk.add(jsonId);
                        jsonFirstNamesOk.add(firstName);
                        jsonAvatarsOk.add(avatarPhoto);
                    }

                    addMessages(okMessages, jsonIdsOk, jsonFirstNamesOk, jsonAvatarsOk,
                            PrefsConstants.USER_Ok_ID);

                    // Odnoklassniki messages Ready:
                    if (listener != null) {
                        listener.onComplete();
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call call, Throwable t) {
                t.printStackTrace();
            }
        });
    }


    private void addMessages(ArrayList<Message> messages, ArrayList<String> ids,
                             ArrayList<String> names, ArrayList<String> avatars, String userId) {

        final String current_user_id_ok = App.get().getPrefs().getString(userId, "");

        for (int i = 0; i < fireBaseMessages.size(); i++) {
            for (int j = 0; j < ids.size(); j++) {
                if (fireBaseMessages.get(i).user_id.equals(ids.get(j))) {

                    String id = fireBaseMessages.get(i).user_id;
                    String message = fireBaseMessages.get(i).message;
                    HashMap<String, Boolean> liked_users = fireBaseMessages.get(i).liked_users;
                    String fire_base_Id = fireBaseIds.get(i);

                    String currentTime = getCurrentTime(fireBaseMessages.get(i));

                    String name = names.get(j);
                    String ava = avatars.get(j);

                    boolean belongToCurrentUser = id.equals(current_user_id_ok);

                    Message singleMessage = new Message(
                            id, message, currentTime, belongToCurrentUser,
                            name, ava, liked_users, fire_base_Id);
                    messages.add(singleMessage);

                }
            }
        }
        ids.clear();
        names.clear();
        avatars.clear();
    }

    @NotNull
    private String getCurrentTime(FireBaseChatMessage fireBaseChatMessage) {
        Date date = new Date(fireBaseChatMessage.timeStamp);
        DateFormat formatter = new SimpleDateFormat("HH:mm:ss", Locale.ENGLISH);
        formatter.setTimeZone(TimeZone.getTimeZone("Europe/Moscow"));
        return formatter.format(date);
    }

    private void getVkMessages(final OnCompleteMessagesListener listener) {

        final ArrayList<String> jsonIdsVk = new ArrayList<>();
        final ArrayList<String> jsonFirstNamesVk = new ArrayList<>();
        final ArrayList<String> jsonAvatarsVk = new ArrayList<>();

        String vkUsersIdsStr = "";
        for (int i = 0; i < fireBaseMessages.size(); i++) {
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
                try {
                    JSONObject jsonResponse = new JSONObject(response.responseString);
                    JSONArray jsonArray = jsonResponse.getJSONArray("response");
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject userInfo = jsonArray.getJSONObject(i);
                        String jsonId = userInfo.getString("id");
                        String firstName = userInfo.getString("first_name");
                        String avatarPhoto = userInfo.getString("photo_50");

                        jsonIdsVk.add(jsonId);
                        jsonFirstNamesVk.add(firstName);
                        jsonAvatarsVk.add(avatarPhoto);
                    }

                    addMessages(vkMessages, jsonIdsVk, jsonFirstNamesVk, jsonAvatarsVk,
                            PrefsConstants.USER_VK_ID);

                    // vk messages Ready:
                    if (listener != null) {
                        listener.onComplete();
                    }

                } catch (JSONException e) {
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

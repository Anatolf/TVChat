package com.anatolf.tvchat.ui.chat;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.util.Log;

import com.anatolf.tvchat.App;
import com.anatolf.tvchat.BuildConfig;
import com.anatolf.tvchat.model.Message;
import com.google.firebase.database.DataSnapshot;
import com.vk.sdk.VKAccessToken;
import com.vk.sdk.VKCallback;
import com.vk.sdk.VKScope;
import com.vk.sdk.VKSdk;
import com.vk.sdk.api.VKError;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONException;
import org.json.JSONObject;

import ru.ok.android.sdk.Odnoklassniki;
import ru.ok.android.sdk.OkListener;
import ru.ok.android.sdk.OkRequestMode;
import ru.ok.android.sdk.util.OkAuthType;
import ru.ok.android.sdk.util.OkScope;

public class ChatPresenter {

    private ChatContractView view;
    private final ChatModel model;

    int countUnreadMessages = 0;


    public ChatPresenter() {
        this.model = new ChatModel(); // создаём модель (получает данные из FB)
    }

    public void attachView(ChatContractView view) {
        this.view = view;
    }

    public void detachView() {
        view = null;
    }

    public void getAllMessages() {
        this.model.getAllMessages(new ChatModel.GetMessageListener() {

            @Override
            public void onShowNewMessages() {
                countUnreadMessages++;
                if (view != null) {
                    view.showUnreadMessages();
                }
            }

            @Override
            public void onUpdateSingleMessage(Message message) {
                if (view != null) {
                    view.showNewSingleMessage(message);
                }
            }

            @Override
            public void onLikeChanged(DataSnapshot dataSnapshot) {
                if (view != null) {
                    view.showNewLikesCountMessages(dataSnapshot);
                }
            }

            @Override
            public void onAddMyNewMessage() {
                if (view != null) {
                    view.scrollDown();
                }
            }
        });
    }

    public void resetUnreadMessages() {
        countUnreadMessages = 0;
    }

    public void incrementOnlineUsersCountInChat() {
        model.incrementOnlineUsersCountInChat();
    }

    public void decrementOnlineUsersCountInChat() {
        model.decrementOnlineUsersCountInChat();
    }

    public void setLike(Message message, boolean like) {
        model.setLike(message, like);
    }

    public boolean isNotAuth() {
        return model.isNotAuth();
    }

    public void sendMessage(String message) {
        model.sendMessage(message);
    }

    public void loginVk(Activity activity) {
        VKSdk.login(activity, VKScope.EMAIL, VKScope.FRIENDS, VKScope.PHOTOS);
    }

    public void loginOk(Activity activity) {
        App.getOdnoklassniki().requestAuthorization(activity,
                "okauth://ok" + BuildConfig.OK_APP_ID,
                OkAuthType.ANY,
                (OkScope.VALUABLE_ACCESS + ";" + OkScope.LONG_ACCESS_TOKEN));
    }

    public void onAuthResult(int requestCode, int resultCode, Intent data) {
        // проверяем зарегестрировался ли пользователь через ВК:
        if (!VKSdk.onActivityResult(requestCode, resultCode, data, new VKCallback<VKAccessToken>() {
            @Override
            public void onResult(VKAccessToken res) {
                // Пользователь успешно авторизовался через VK
                // Сохраняем его Id, email, access_token в SharedPreferences
                SharedPreferences.Editor editor = App.get().getPrefs().edit();
                editor.putString(USER_VK_ID, res.userId);
                editor.putString(USER_VK_EMAIL, res.email);
                editor.putString(USER_VK_ACCESS_TOKEN, res.accessToken);
                editor.apply();

                if (view != null) {
                    view.showText("Теперь вы можете отправлять сообщения!");
                }
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

                    App.getOdnoklassniki().onAuthActivityResult(requestCode, resultCode, data, new OkListener() {
                        @Override
                        public void onSuccess(@NotNull JSONObject jsonObject) {
                            // Log.d(TAG, "requestAsync: onSuccess " + jsonObject.toString());

                            // очищаем временные списки (или происходит дублирование всех сообщений)
                            fireBaseMessages.clear();
                            fireBaseIds.clear();
                            vkMessages.clear();
                            okMessages.clear();


                            // берём информацию о текущем юзере ОК и записываем в SharedPreference:
                            App.getOdnoklassniki().requestAsync(
                                    "users.getCurrentUser",
                                    null,
                                    OkRequestMode.getDEFAULT(),
                                    new OkListener() {
                                        @Override
                                        public void onSuccess(@NotNull JSONObject jsonObject) {
                                            // Log.d(TAG, "Одноклассники, ответ при регистрации (web) onSuccess, jsonObject= " + jsonObject.toString());

                                            try {
                                                String jsonId = jsonObject.getString("uid");

                                                SharedPreferences.Editor editor = App.get().getPrefs().edit();
                                                editor.putString(USER_Ok_ID, jsonId);
                                                editor.apply();

                                            } catch (JSONException e) {
                                                e.printStackTrace();
                                            }
                                        }

                                        @Override
                                        public void onError(@Nullable String s) {
                                            // Log.d(TAG, "Odnoklassniki RequestAsync: onError " + s);
                                        }
                                    });

                        }

                        @Override
                        public void onError(@Nullable String s) {
                            Log.d(TAG, "Odnoklassniki RequestAsync: Error " + s);

                        }
                    });


                } else {
                    Log.d(TAG, "isActivityRequestOAuth: авторизация через веб-форму Odnoklassniki НЕ ПРОШЛА !!!");
                }


            } else if (Odnoklassniki.Companion.of(this).isActivityRequestViral(requestCode)) { // native
                boolean isAuthCompleted = !TextUtils.isEmpty(data.getStringExtra("access_token"));
                if (isAuthCompleted) {
                    Log.d(TAG, "isActivityRequestViral: Пользователь успешно авторизовался через мобильное приложение Odnoklassniki (native)");

                    App.getOdnoklassniki().onAuthActivityResult(requestCode, resultCode, data, new OkListener() {
                        @Override
                        public void onSuccess(@NotNull JSONObject jsonObject) {
                            Log.d(TAG, "requestAsync: onSuccess " + jsonObject.toString());

                            // очищаем временные списки (или происходит дублирование всех сообщений)
                            fireBaseMessages.clear();
                            fireBaseIds.clear();
                            vkMessages.clear();
                            okMessages.clear();


                            // берём информацию о текущем юзере ОК и записываем в SharedPreference:
                            App.getOdnoklassniki().requestAsync(
                                    "users.getCurrentUser",
                                    null,
                                    OkRequestMode.getDEFAULT(),  // EnumSet.of(OkRequestMode.SDK_SESSION)
                                    new OkListener() {
                                        @Override
                                        public void onSuccess(@NotNull JSONObject jsonObject) {
                                            Log.d(TAG, "Одноклассники, ответ при регистрации (native) onSuccess, jsonObject= " + jsonObject.toString());

                                            try {
                                                String jsonId = jsonObject.getString("uid");

                                                SharedPreferences.Editor editor = App.get().getPrefs().edit();
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
}

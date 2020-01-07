package com.anatolf.tvchat.ui.chat;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.anatolf.tvchat.App;
import com.anatolf.tvchat.BuildConfig;
import com.anatolf.tvchat.R;
import com.anatolf.tvchat.model.Channel;
import com.anatolf.tvchat.model.FireBaseChatMessage;
import com.anatolf.tvchat.model.Message;
import com.anatolf.tvchat.ui.main.MainActivity;
import com.anatolf.tvchat.utils.PrefsConstants;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.annotations.NotNull;
import com.squareup.picasso.Picasso;
import com.vk.sdk.VKAccessToken;
import com.vk.sdk.VKCallback;
import com.vk.sdk.VKScope;
import com.vk.sdk.VKSdk;
import com.vk.sdk.api.VKError;

import org.json.JSONException;
import org.json.JSONObject;

import ru.ok.android.sdk.Odnoklassniki;
import ru.ok.android.sdk.OkListener;
import ru.ok.android.sdk.OkRequestMode;
import ru.ok.android.sdk.util.OkAuthType;
import ru.ok.android.sdk.util.OkScope;

public class ChatActivity extends AppCompatActivity implements ChatContractView {

    public static final String TAG = "Chat Activity";
    private Toolbar toolbar;
    private ImageView icon_toolbar;
    private TextView head_text_toolbar;
    private EditText editText;
    private MessageAdapter messageAdapter;
    private RecyclerView messagesRecyclerView;
    private RecyclerView.LayoutManager rvLayoutManager;
    private FloatingActionButton mFloatingActionButton;
    private TextView floatingButtonText;
    private ImageView floatingButtonImage;
    private RelativeLayout root_chat;

    private boolean startActivity = false;
    private ChatPresenter presenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        initView();
    }

    private void initView() {
        editText = findViewById(R.id.editText);
        root_chat = findViewById(R.id.root_element_chat);
        Intent intentFromMain = getIntent();

        if (intentFromMain.hasExtra(MainActivity.CHANNEL_OBJECT_EXTRA)) {
            Channel channel = (Channel) intentFromMain
                    .getSerializableExtra(MainActivity.CHANNEL_OBJECT_EXTRA);

            presenter = new ChatPresenter(channel.channel_id, channel.firebase_channel_id);
            presenter.attachView(this); // связывает вью и презентер

            presenter.channel_name = channel.name;
            presenter.channel_image_url = channel.urlChannel;
        }

        toolbar = findViewById(R.id.custom_tool_bar);
        icon_toolbar = findViewById(R.id.image_tool_bar);
        head_text_toolbar = findViewById(R.id.head_text_tool_bar);
        head_text_toolbar.setText(presenter.channel_name);

        Picasso.get()
                .load(presenter.channel_image_url)
                .error(R.drawable.ic_launcher_foreground)
                .into(icon_toolbar);

        setSupportActionBar(toolbar);

        initMessageRecyclerView();
    }

    @Override
    protected void onResume() {
        super.onResume();
        startActivity = true;
        messageAdapter.messages.clear();
        // очищаем специальный список id, для дублирующих сообщений из FireBase

        presenter.getAllMessages();
        presenter.incrementOnlineUsersCountInChat();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        presenter.detachView();
    }


    private void initMessageRecyclerView() {

        messagesRecyclerView = findViewById(R.id.messages_view);
        rvLayoutManager = new LinearLayoutManager(this);
        ((LinearLayoutManager) rvLayoutManager).setStackFromEnd(true);  // auto scroll bottom
        messagesRecyclerView.setLayoutManager(rvLayoutManager);
        mFloatingActionButton = findViewById(R.id.floating_action_button);
        floatingButtonText = findViewById(R.id.floating_button_text);
        floatingButtonImage = findViewById(R.id.floating_button_image);

        messagesRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);

                if (!recyclerView.canScrollVertically(1)) {
                    mFloatingActionButton.hide();
                    floatingButtonText.setVisibility(View.INVISIBLE);
                    floatingButtonImage.setVisibility(View.INVISIBLE);
                    presenter.resetUnreadMessages();

                    if (mFloatingActionButton.getVisibility() != View.VISIBLE) {
                        floatingButtonImage.setVisibility(View.INVISIBLE);
                        floatingButtonText.setVisibility(View.INVISIBLE);
                        presenter.resetUnreadMessages();
                    }
                }
            }
        });

        MessageAdapter.OnLikeClickListener onLikeClickListener = new MessageAdapter.OnLikeClickListener() {
            @Override
            public void onLikeClick(final Message message) {
                presenter.setLike(message, true);
            }
        };

        MessageAdapter.OnCancelLikeClickListener onCancelLikeClickListener = new MessageAdapter.OnCancelLikeClickListener() {
            @Override
            public void onCancelLikeClick(final Message message) {
                presenter.setLike(message, false);
            }
        };

        messageAdapter = new MessageAdapter(onLikeClickListener, onCancelLikeClickListener);
        messagesRecyclerView.setAdapter(messageAdapter);
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //Log.d(TAG, "in onActivityResult before registration Vk & Ok ");

        // проверяем зарегестрировался ли пользователь через ВК:

        boolean isVkAuth = VKSdk.onActivityResult(requestCode, resultCode, data, new VKCallback<VKAccessToken>() {
            @Override
            public void onResult(VKAccessToken res) {
                // Пользователь успешно авторизовался через VK
                // Сохраняем его Id, email, access_token в SharedPreferences
                SharedPreferences.Editor editor = App.get().getPrefs().edit();
                editor.putString(PrefsConstants.USER_VK_ID, res.userId);
                editor.putString(PrefsConstants.USER_VK_EMAIL, res.email);
                editor.putString(PrefsConstants.USER_VK_ACCESS_TOKEN, res.accessToken);
                editor.apply();
                showText("Теперь вы можете отправлять сообщения!");
                presenter.getAllMessages();
            }

            @Override
            public void onError(VKError error) {
                showText("Ошибка: " + error.errorMessage);
            }
        });


        if (!isVkAuth) {
            // проверяем зарегестрировался ли пользователь через ОК:
            if (Odnoklassniki.Companion.of(this).isActivityRequestOAuth(requestCode) // Ok native
                    || Odnoklassniki.Companion.of(this).isActivityRequestViral(requestCode)) {  // Ok web
                onOkAuthCompleted(requestCode, resultCode, data);
            } else {
                super.onActivityResult(requestCode, resultCode, data);
            }
        }
    }

    private void onOkAuthCompleted(int requestCode, int resultCode, Intent data) {
        boolean isAuthCompleted = !TextUtils.isEmpty(data.getStringExtra("access_token"));
        if (isAuthCompleted) {
            App.getOdnoklassniki().onAuthActivityResult(requestCode, resultCode, data, new OkListener() {
                @Override
                public void onSuccess(@NotNull JSONObject jsonObject) {
                    presenter.cleanTempLists();

                    // берём информацию о текущем юзере ОК и записываем в SharedPreference:
                    App.getOdnoklassniki().requestAsync(
                            "users.getCurrentUser",
                            null,
                            OkRequestMode.getDEFAULT(),  // EnumSet.of(OkRequestMode.SDK_SESSION)
                            new OkListener() {
                                @Override
                                public void onSuccess(@NotNull JSONObject jsonObject) {
                                    // Log.d(TAG, "Одноклассники, ответ при регистрации (web) onSuccess, jsonObject= " + jsonObject.toString());
                                    try {
                                        String jsonId = jsonObject.getString("uid");
                                        SharedPreferences.Editor editor = App.get().getPrefs().edit();
                                        editor.putString(PrefsConstants.USER_Ok_ID, jsonId);
                                        editor.apply();
                                        presenter.getAllMessages();
                                        showText("Теперь вы можете отправлять сообщения!");
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }

                                @Override
                                public void onError(@Nullable String error) {
                                    showText("Ошибка: " + error);
                                }
                            });
                }

                @Override
                public void onError(@Nullable String error) {
                    showText("Ошибка: " + error);
                }
            });


        } else {
            showText("Ошибка: Авторизация через веб-форму Odnoklassniki НЕ ПРОШЛА");
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        presenter.decrementOnlineUsersCountInChat();
    }


    public void sendMessage(final View view) {

        // Authorization
        if (presenter.isNotAuth()) {

            final AlertDialog.Builder dialog = new AlertDialog.Builder(this);
            dialog.setTitle(getString(R.string.enter)); // todo sdelat ostalnie strings
            dialog.setMessage("Чтобы отправлять сообщения, зарегестрируйтесь:");

            LayoutInflater inflater = LayoutInflater.from(this);
            View sign_in_window = inflater.inflate(R.layout.dialog_registration, null);
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

            final ImageButton btnRegVk = sign_in_window.findViewById(R.id.ib_vk_btn);
            final ImageButton btnRegOk = sign_in_window.findViewById(R.id.ib_ok_btn);
            final ImageButton btnRegMail = sign_in_window.findViewById(R.id.ib_fb_btn);

            // по кнопке "Через Вк"
            btnRegVk.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    loginVk(ChatActivity.this);
                    adTrueDialog.dismiss();
                }
            });

            // по кнопке "Через Ок"
            btnRegOk.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    loginOk(ChatActivity.this);
                    adTrueDialog.dismiss();
                }
            });

            // по кнопке "Через Facebook"
            btnRegMail.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(ChatActivity.this,
                            "Функция пока не доступна",
                            Toast.LENGTH_SHORT).show();
                    adTrueDialog.dismiss();
                }
            });
            return;
        }

        presenter.sendMessage(editText.getText().toString());
        editText.getText().clear();
    }


    @Override
    public void showNewSingleMessage(Message message) {
        messageAdapter.add(message);
    }

    @Override
    public void showNewLikesCountMessages(@NonNull DataSnapshot dataSnapshot) {
        for (Message msg : messageAdapter.messages) {
            if (msg.getFireBase_id().equals(dataSnapshot.getKey())) {
                FireBaseChatMessage fireBaseChatMessage = dataSnapshot.getValue(FireBaseChatMessage.class);
                msg.setLikes(fireBaseChatMessage.liked_users);
                messageAdapter.notifyDataSetChanged();
            }
        }
    }

    @Override
    public void showUnreadMessages() {

        ChatActivity.this.runOnUiThread(new Runnable() {
            public void run() {

                int visibleItemCount = rvLayoutManager.getChildCount(); // 5
                int totalItemCount = rvLayoutManager.getItemCount();
                int pastVisibleItems = ((LinearLayoutManager) rvLayoutManager).findFirstCompletelyVisibleItemPosition();


                if ((totalItemCount - pastVisibleItems) > visibleItemCount) {  // если прокрутили вверх от самого низа больше чем на "5" items
                    //Log.d(TAG, "if showNewMessages()");
                    if (mFloatingActionButton.getVisibility() != View.VISIBLE) {
                        mFloatingActionButton.show();
                        floatingButtonText.setVisibility(View.VISIBLE);

                        mFloatingActionButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) { // скролл вниз + скрытие флоатинг баттон
                                messagesRecyclerView.smoothScrollToPosition(messageAdapter.getItemCount() - 1);
                                mFloatingActionButton.hide();
                                floatingButtonText.setVisibility(View.INVISIBLE);
                                floatingButtonImage.setVisibility(View.INVISIBLE);
                                presenter.resetUnreadMessages();
                                startActivity = false;
                                // за скрытие флоатинг баттон и всех её атрибутов
                                // при прокрутке пальцем вниз отвечает метод из initMessageRecyclerView onScrollStateChanged
                            }
                        });
                    }
                    if (startActivity) {
                        floatingButtonText.setVisibility(View.INVISIBLE);
                        floatingButtonImage.setVisibility(View.VISIBLE);
                        startActivity = false;
                    } else {
                        floatingButtonImage.setVisibility(View.INVISIBLE);
                        floatingButtonText.setText(String.valueOf(presenter.countUnreadMessages));
                    }

                } else {
                    if (mFloatingActionButton.getVisibility() == View.VISIBLE) {
                        floatingButtonText.setVisibility(View.INVISIBLE);
                        floatingButtonImage.setVisibility(View.INVISIBLE);
                        mFloatingActionButton.hide();
                    }
                    messagesRecyclerView.smoothScrollToPosition(messageAdapter.getItemCount() - 1);
                    startActivity = false;

                }
            }
        });
    }

    @Override
    public void scrollDown() {
        messagesRecyclerView.smoothScrollToPosition(messageAdapter.getItemCount() - 1);
    }

    @Override
    public void showText(String text) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }
}

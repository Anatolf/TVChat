package com.anatolf.tvchat.ui.chat;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.anatolf.tvchat.R;
import com.anatolf.tvchat.model.Channel;
import com.anatolf.tvchat.model.FireBaseChatMessage;
import com.anatolf.tvchat.model.Message;
import com.anatolf.tvchat.ui.main.MainActivity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DataSnapshot;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class ChatActivity extends AppCompatActivity implements ChatContractView {
    private static final String TAG = "myLogs";

    public static final String USER_VK_ID = "user_shared_pref_id_key";
    private static final String USER_VK_EMAIL = "user_shared_pref_email_key";
    private static final String USER_VK_ACCESS_TOKEN = "user_shared_pref_access_token_key";
    public static final String USER_Ok_ID = "user_shared_pref_id_key_odnoklassniki";

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

    private ArrayList<FireBaseChatMessage> fireBaseMessages = new ArrayList<>();
    private ArrayList<String> fireBaseIds = new ArrayList<>();


    private Set<String> blockIds = new HashSet<>();

    private String channel_id = "";
    private String channel_name = "";
    private String channel_image_url = "";
    private String firebase_channel_id = "";

    private boolean startActivity = false;

    private ChatPresenter presenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        initView();
        presenter = new ChatPresenter();
        presenter.attachView(this); // связывает вью и презентер
    }

    private void initView() {
        editText = findViewById(R.id.editText);
        root_chat = findViewById(R.id.root_element_chat);
        Intent intentFromMain = getIntent();

        if (intentFromMain.hasExtra(MainActivity.CHANNEL_OBJECT_EXTRA)) {
            Channel channel = (Channel) intentFromMain
                    .getSerializableExtra(MainActivity.CHANNEL_OBJECT_EXTRA);

            channel_id = channel.channel_id;
            channel_name = channel.name;
            channel_image_url = channel.urlChannel;
            firebase_channel_id = channel.firebase_channel_id;
        }

        toolbar = findViewById(R.id.custom_tool_bar);
        icon_toolbar = findViewById(R.id.image_tool_bar);
        head_text_toolbar = findViewById(R.id.head_text_tool_bar);
        head_text_toolbar.setText(channel_name);

        Picasso.get()
                .load(channel_image_url)
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
        blockIds.clear();

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

        messageAdapter = new MessageAdapter(
                this, onLikeClickListener, onCancelLikeClickListener);
        messagesRecyclerView.setAdapter(messageAdapter);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        presenter.onAuthResult(requestCode, resultCode, data);
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
                    presenter.loginVk(ChatActivity.this);
                    adTrueDialog.dismiss();
                }
            });

            // по кнопке "Через Ок"
            btnRegOk.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    presenter.loginOk(ChatActivity.this);
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

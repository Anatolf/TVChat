package com.example.hohlosra4app;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.IgnoreExtraProperties;
import com.google.firebase.database.Query;
import com.squareup.picasso.Picasso;
import com.vk.sdk.VKAccessToken;
import com.vk.sdk.VKCallback;
import com.vk.sdk.VKScope;
import com.vk.sdk.VKSdk;
import com.vk.sdk.api.VKApiConst;
import com.vk.sdk.api.VKError;
import com.vk.sdk.api.VKParameters;
import com.vk.sdk.api.VKRequest;
import com.vk.sdk.api.VKResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;

public class ChatActivity extends AppCompatActivity {
    private static final String TAG = "myLogs";

    private static final String USER_VK_ID = "user_shared_pref_id_key";
    private static final String USER_VK_EMAIL = "user_shared_pref_email_key";
    private static final String USER_VK_ACCESS_TOKEN = "user_shared_pref_access_token_key";

    private EditText editText;
    private MessageAdapter messageAdapter;
    private ListView messagesView;

    private FirebaseDatabase database;
    private DatabaseReference myRef;

    // for registration window
    private FirebaseAuth auth;
    private RelativeLayout root_chat;

    private ArrayList<String> usersIds = new ArrayList<>();
    private ArrayList<Long> usersTimeStamps = new ArrayList<>();
    private ArrayList<String> usersMessages = new ArrayList<>();


    // for VK api
    private String[] scope = new String[]{VKScope.EMAIL, VKScope.FRIENDS, VKScope.PHOTOS};

    SharedPreferences sPref;

    Set<String> blokIds = new HashSet<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
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

////////////////////// получаем Интент из Майн /////////////////////
        Intent intentFromMain = getIntent();
        if (intentFromMain.hasExtra(Intent.EXTRA_INDEX) && intentFromMain.hasExtra("count_users_into_this_chart")) {
            int indexChannel = intentFromMain.getIntExtra(Intent.EXTRA_INDEX, 0);
            int usersIntoChat = intentFromMain.getIntExtra("count_users_into_this_chart", 0);
            Toast.makeText(ChatActivity.this,
                    "мы в ChatActivity, Канал " + indexChannel + ", количество обсуждающих " + usersIntoChat,
                    Toast.LENGTH_SHORT).show();
        }
//end//////////////////////////////////////////////////////////////


        // подключили базу для отправки сообщения на fireBase в методе sendMessage()
        database = FirebaseDatabase.getInstance();
        //        database.setPersistenceEnabled(true);  // добавление элементов во время оффлайн
        myRef = database.getReference("Comments").child("1TV");
        //   myRef.removeValue();  // удалить из базы весь раздел "1TV"  и всё что внутри (комментарии "Comments")
        //  myRef = database.getReference("items").child("users").child("newUser"); // многопользовательская ветка

    }


//////////////////VK VK VK Response autorisation ///////////////////////
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
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
        })) {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private VKRequest createUserRequest() {

        String usersIdsStr = "";
        for (String userId : usersIds) {
            usersIdsStr = usersIdsStr + userId + ",";
        }

        return new VKRequest(
                "users.get",
                VKParameters.from(
                        VKApiConst.USER_IDS, usersIdsStr,
                        VKApiConst.FIELDS, "sex,photo_50"));
    }

    private void getUsersInfo() {

        createUserRequest().executeWithListener(new VKRequest.VKRequestListener() {
            @Override
            public void onComplete(VKResponse response) {
                //   Log.d(TAG, "onComplete: " + response.responseString);

                ArrayList<String> jsonIds = new ArrayList<>();
                ArrayList<String> jsonFirstNames = new ArrayList<>();
                //ArrayList<String> jsonLastNames = new ArrayList<>();
                ArrayList<String> jsonAvatars = new ArrayList<>();

                // получаем ответ от ApiVk и парсим json каждого участника чата
                try {
                    for (int i = 0; i < usersIds.size(); i++) {
                        JSONObject jsonResponse = new JSONObject(response.responseString);
                        JSONArray jsonArray = jsonResponse.getJSONArray("response");
                        JSONObject userInfo = jsonArray.getJSONObject(i);
                        String jsonId = userInfo.getString("id");
                        String firstName = userInfo.getString("first_name");
                        //String lastName = userInfo.getString("last_name");
                        String avatarPhoto = userInfo.getString("photo_50");

                        jsonIds.add(jsonId);
                        jsonFirstNames.add(firstName);
                        //jsonLastNames.add(lastName);
                        jsonAvatars.add(avatarPhoto);
                        //Log.d(TAG, "в Цикле getUsersInfo: jsonId = " + jsonId + ", Имя = " + firstName + ", АВА = " + avatarPhoto);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }

                // достали из SharedPreferences id_vk  пользователя, для проверки Юзер (я/не я?)
                sPref = getPreferences(MODE_PRIVATE);
                final String current_user_id_vk = sPref.getString(USER_VK_ID, "");

                // первый цикл бежит по всем списку id сообщений полученых от FireBase
                // второй цикл сравнивает каждый проход с id полученными от ВК jsonId (списки jsonIds, jsonFirstNames, jsonAvatars - идентичны по размеру - для дальнейшего сопоставления)
                for (int i = 0; i < usersIds.size(); i++) {
                    for (int j = 0; j < jsonIds.size(); j++) {
                        if (usersIds.get(i).equals(jsonIds.get(j))) {

                            String id = usersIds.get(i);
                            String message = usersMessages.get(i);

                            Date date = new Date(usersTimeStamps.get(i));
                            DateFormat formatter = new SimpleDateFormat("HH:mm:ss");
                            formatter.setTimeZone(TimeZone.getTimeZone("Europe/Moscow"));
                            String currentTime = formatter.format(date);

                            String name = jsonFirstNames.get(j);
                            String ava = jsonAvatars.get(j);

                            boolean belongToCurrentUser; // флаг Юзер я, не я?
                            if (id.equals(current_user_id_vk)) {  // мой id вконтакте
                                belongToCurrentUser = true;
                            } else {
                                belongToCurrentUser = false;
                            }

                            //  конструктор № 2: создаёт сообщения с аватарками из ВК:
                            Message singleMessage = new Message(id, message, currentTime, belongToCurrentUser, name, ava);
                            messageAdapter.add(singleMessage);  // посылаем на отображение

//                            Log.d(TAG, "в Цикле For: id = " + id + ", message = " + message
//                                    + ", currentTime = " + currentTime + ", name = "
//                                    + name + ", ava = " + ava + ", belongToCurrentUser = " + belongToCurrentUser);

                        }
                    }
                }

                // очищаем временные списки:
                usersIds.clear();
                usersMessages.clear();
                usersTimeStamps.clear();

                jsonIds.clear();
                jsonFirstNames.clear();
                //jsonLastNames.clear();
                jsonAvatars.clear();


            }

            @Override
            public void attemptFailed(VKRequest request, int attemptNumber, int totalAttempts) {
                // Log.d(TAG, "attemptFailed: ");
            }

            @Override
            public void onError(VKError error) {
                // Log.d(TAG, "onError: " + error.errorMessage);
            }

            @Override
            public void onProgress(VKRequest.VKProgressType progressType, long bytesLoaded, long bytesTotal) {
                // Log.d(TAG, "onProgress: ");
            }
        });
    }
// VK END ////////////////////////////////////////////


    @Override
    protected void onResume() {
        super.onResume();

        // очищаем в адаптере Message список messages перед каждым перезапуском активити
        messageAdapter.messages.clear();
        // очищаем специальный список id, для дублирующих сообщений из FireBase
        blokIds.clear();
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
                if (!blokIds.contains(s)) { // не пропускает дубликат (s пример: "LqbDjO_PdRZ9EI_o-V1")
                    blokIds.add(s);

                    // берём приходящее сообщение из базы и добавляем все его параметры в листы:
                    if (!TextUtils.isEmpty(fireBaseChatMessage.user_id)) {
                        usersIds.add(fireBaseChatMessage.user_id);
                        usersTimeStamps.add(fireBaseChatMessage.timeStamp);
                        usersMessages.add(fireBaseChatMessage.message);
                    }

                    // берём дату из fireBaseChatMessage и переводим её в "00:00:00" по Москве (для первого конструктора, ниже)
                    Date date = new Date(fireBaseChatMessage.timeStamp);
                    DateFormat formatter = new SimpleDateFormat("HH:mm:ss");
                    formatter.setTimeZone(TimeZone.getTimeZone("Europe/Moscow"));
                    String currentTime = formatter.format(date);

                    if (!VKSdk.isLoggedIn()) {  // если пользователь ещё не прошёл регистрацию Через ВК то:
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
                                getUsersInfo();
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
    public void sendMessage(View view) {

        if (!VKSdk.isLoggedIn()) {  /// || !одноклассники.isLoggedIn итд !почта.isLoggedIn

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
            final Button btnRegVk = sign_in_window.findViewById(R.id.reg_from_vk_btn);
            final Button btnRegOk = sign_in_window.findViewById(R.id.reg_from_ok_btn);
            final Button btnRegMail = sign_in_window.findViewById(R.id.reg_from_email_btn);

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
                    Toast.makeText(ChatActivity.this,
                            "функция пока не доступна",
                            Toast.LENGTH_SHORT).show();
                    adTrueDialog.dismiss();
                }
            });

            // по кнопке "Через Майл"
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
        String message = editText.getText().toString();  // получаем сообщение с поля ввода

        //отправляет введённое сообщение в базу данных
        if (message.length() > 0) {
            long time_stamp = System.currentTimeMillis();   // получаем время отправки сообщения
            //создаем экземпляр одного Cообщения Юзера
            FireBaseChatMessage fireBaseChatMessage = new FireBaseChatMessage(current_user_id_vk, message, time_stamp);
            // оправляем его в базу данных firebase
            myRef.push().setValue(fireBaseChatMessage);
            messagesView.smoothScrollToPosition(messageAdapter.getCount() - 1);
            editText.getText().clear();  //очищаем поле ввода
        }
    }


    // Класс FireBaseChatMessage - объект одного отправляемого Сообщения Юзера в выбранный чат (для добавления в базу данных firebase)
    @IgnoreExtraProperties
    static class FireBaseChatMessage implements Serializable {

        public String user_id; // = "pidor_na_androide";
        public String message; // = "кукуепта";
        public long timeStamp; // = System.currentTimeMillis();

        public FireBaseChatMessage() {
        }

        public FireBaseChatMessage(String user_id, String message, long timeStamp) {
            this.user_id = user_id;
            this.message = message;
            this.timeStamp = timeStamp;
        }
    }
}

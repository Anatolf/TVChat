package com.example.hohlosra4app;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
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
import com.vk.sdk.VKAccessToken;
import com.vk.sdk.VKCallback;
import com.vk.sdk.VKScope;
import com.vk.sdk.VKSdk;
import com.vk.sdk.api.VKApi;
import com.vk.sdk.api.VKApiConst;
import com.vk.sdk.api.VKError;
import com.vk.sdk.api.VKParameters;
import com.vk.sdk.api.VKRequest;
import com.vk.sdk.api.VKResponse;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TimeZone;

public class ChatActivity extends AppCompatActivity {
    private static final String TAG = "myLogs";

    private EditText editText;
    private MessageAdapter messageAdapter;
    private ListView messagesView;

    private FirebaseDatabase database;
    private DatabaseReference myRef;

// for registration window
    private FirebaseAuth auth;
    private RelativeLayout root_chat;

    private Set<String> usersIds = new HashSet<>();

// for VK api
    private String[] scope = new String[]{VKScope.EMAIL,VKScope.FRIENDS, VKScope.PHOTOS};


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
        if(intentFromMain.hasExtra(Intent.EXTRA_INDEX) && intentFromMain.hasExtra("count_users_into_this_chart")){
            int indexChannel = intentFromMain.getIntExtra(Intent.EXTRA_INDEX,0);
            int usersIntoChat = intentFromMain.getIntExtra("count_users_into_this_chart",0);
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

    private String accessToken_VK;
    private String email_VK;
    private String userId_VK;

/////////VK VK VK Response autorisation ///////////////////////
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (!VKSdk.onActivityResult(requestCode, resultCode, data, new VKCallback<VKAccessToken>() {

         //   VKRequest vkRequest = new VKRequest("photos.saveOwnerPhoto", VKParameters.from(VKApiConst.PHOTO, "album_id.profile"));

            @Override
            public void onResult(VKAccessToken res) {
                // Пользователь успешно авторизовался

                // тут мы получаем ответ от авторизовавшегося пользователя и создаём его объект Юзер, 
                accessToken_VK = res.accessToken;
                email_VK = res.email;
                userId_VK = res.userId;

                Toast.makeText(ChatActivity.this,
                        "Teper vi mozhete otpravlat soobsheniya!",
                        Toast.LENGTH_SHORT).show();

                Toast.makeText(ChatActivity.this,
                        "ema= "+ email_VK + ", use= " + userId_VK + ", tok= "+ accessToken_VK,
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
                Log.d(TAG, "onComplete: " + response.responseString);


               //] String jsonVk = response.json;
            }

            @Override
            public void attemptFailed(VKRequest request, int attemptNumber, int totalAttempts) {
                Log.d(TAG, "attemptFailed: ");
            }

            @Override
            public void onError(VKError error) {
                Log.d(TAG, "onError: " + error.errorMessage);
            }

            @Override
            public void onProgress(VKRequest.VKProgressType progressType, long bytesLoaded, long bytesTotal) {
                Log.d(TAG, "onProgress: ");
            }
        });
    }
// VK END ////////////////////////////////////////////


    @Override
    protected void onResume() {
        super.onResume();

        // очищаем в адаптере Message список messages перед каждым перезапуском активити
        messageAdapter.messages.clear();
        showAllMessages();
    }

    private void showAllMessages(){

        // делаем запрос в базу данных firebase
        Query myQuery = myRef;
        //  Query myQuery = myRef.orderByChild("numberChannel").equalTo(111);   // редактирование: сортирует ответ по "numberChannel" и 111
        myQuery.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                /////// Получаем из FireBase из раздела "1TV", подраздела "Comments" по одному объекту ChatMessage
                /////// и отправляем его в messageAdapter для отображения
                ChatMessage chatMessage = dataSnapshot.getValue(ChatMessage.class);

                    boolean belongToCurrentUser; // флаг Юзер я, не я?
                    if (chatMessage.user_id.equals("pidor na Androide")) {
                        belongToCurrentUser = true;
                    } else {
                        belongToCurrentUser = false;
                    }

                    // берём дату из chatMessage и переводим её в "00:00:00" по Москве
                Date date = new Date(chatMessage.timeStamp);
                DateFormat formatter = new SimpleDateFormat("HH:mm:ss");
                formatter.setTimeZone(TimeZone.getTimeZone("Europe/Moscow"));
                String currentTime = formatter.format(date);

                    // создаёт рандомных собеседников Имя и цвет сообщения, время передаём через memb для всех собеседников и себя
                    MemberData memb = new MemberData(getRandomName(), getRandomColor(), currentTime);
                    Message singleMessage = new Message(chatMessage.message, memb, belongToCurrentUser);

                    if (!TextUtils.isEmpty(chatMessage.user_id)) {
                        usersIds.add(chatMessage.user_id);
                    }


                    messageAdapter.add(singleMessage);

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) { }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) { }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) { }

            @Override
            public void onCancelled(DatabaseError databaseError) { }
        });
    }


    // По нажатию кнопки создаём в FireBase новое "сообщение" с введённым текстом из поля EditText
    public void sendMessage(View view) {

        if (!VKSdk.isLoggedIn()) {
            Toast.makeText(ChatActivity.this,
                    "Необходимо авторизоваться в Вконтакте",
                    Toast.LENGTH_SHORT).show();
            VKSdk.login(this, scope);
            return;
        }

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
                VKSdk.login(ChatActivity.this,scope);
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
                // сделать а возвращением результата //
                startActivity(new Intent(ChatActivity.this, RegistrationActivity.class));
                adTrueDialog.dismiss();
            }
        });


        final String user_id = userId_VK;  // имя пользователя "pidor na Androide"
        String message = editText.getText().toString();  // получаем сообщение с поля ввода
        long time_stamp = System.currentTimeMillis();   // время сообщения

        //отправляет введённое сообщение в базу данных
//        if (message.length() > 0) {
//                    //создаем экземпляр одного Cообщения Юзера
//            ChatMessage chatMessage = new ChatMessage(user_id,message, time_stamp);
//                    // оправляем его в базу данных firebase
//            myRef.push().setValue(chatMessage);
//            messagesView.smoothScrollToPosition(messageAdapter.getCount() -1);
//            editText.getText().clear();  //очищаем поле ввода
//        }
    }


    private String getRandomName() {
        String[] adjs = {"autumn", "hidden", "bitter", "misty", "silent", "empty", "dry", "dark", "summer", "icy", "delicate", "quiet", "white", "cool", "spring", "winter", "patient", "twilight", "dawn", "crimson", "wispy", "weathered", "blue", "billowing", "broken", "cold", "damp", "falling", "frosty", "green", "long", "late", "lingering", "bold", "little", "morning", "muddy", "old", "red", "rough", "still", "small", "sparkling", "throbbing", "shy", "wandering", "withered", "wild", "black", "young", "holy", "solitary", "fragrant", "aged", "snowy", "proud", "floral", "restless", "divine", "polished", "ancient", "purple", "lively", "nameless"};
        String[] nouns = {"waterfall", "river", "breeze", "moon", "rain", "wind", "sea", "morning", "snow", "lake", "sunset", "pine", "shadow", "leaf", "dawn", "glitter", "forest", "hill", "cloud", "meadow", "sun", "glade", "bird", "brook", "butterfly", "bush", "dew", "dust", "field", "fire", "flower", "firefly", "feather", "grass", "haze", "mountain", "night", "pond", "darkness", "snowflake", "silence", "sound", "sky", "shape", "surf", "thunder", "violet", "water", "wildflower", "wave", "water", "resonance", "sun", "wood", "dream", "cherry", "tree", "fog", "frost", "voice", "paper", "frog", "smoke", "star"};
        return (
            adjs[(int) Math.floor(Math.random() * adjs.length)] +
            "_" +
            nouns[(int) Math.floor(Math.random() * nouns.length)]
        );
    }

    private String getRandomColor() {
        Random r = new Random();
        StringBuffer sb = new StringBuffer("#");
        while(sb.length() < 7){
            sb.append(Integer.toHexString(r.nextInt()));
        }
        return sb.toString().substring(0, 7);
    }


    // Класс ChatMessage - объект одного отправляемого Сообщения Юзера в выбранный чат (для добавления в базу данных firebase)
    @IgnoreExtraProperties
    static class ChatMessage implements Serializable{

        public String user_id; // = "pidor_na_androide";
        public String message; // = "кукуепта";
        public long timeStamp; // = System.currentTimeMillis();

        public ChatMessage() {
        }

        public ChatMessage(String user_id, String message, long timeStamp) {
            this.user_id = user_id;
            this.message = message;
            this.timeStamp = timeStamp;
        }
    }
}






class MemberData {
    private String name;
    private String color;
    private String time;

    public MemberData(String name, String color, String time) {
        this.name = name;
        this.color = color;
        this.time = time;
    }

    public MemberData() {
    }

    public String getName() {
        return name;
    }

    public String getColor() {
        return color;
    }

    public String getTime() { return time; }

    @Override
    public String toString() {
        return "MemberData{" +
                "name='" + name + '\'' +
                ", color='" + color + '\'' +
                '}';
    }

}

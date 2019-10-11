package com.example.hohlosra4app;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.IgnoreExtraProperties;
import com.google.firebase.database.Query;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;
import java.util.TimeZone;

public class ChatActivity extends AppCompatActivity {
    private static final String TAG = "myLogs";

    private EditText editText;
    private MessageAdapter messageAdapter;
    private ListView messagesView;

    private FirebaseDatabase database;
    private DatabaseReference myRef;


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
        String user_id = "kakaya to pizda";  // имя пользователя "pidor na Androide"
        String message = editText.getText().toString();  // получаем сообщение с поля ввода
        long time_stamp = System.currentTimeMillis();   // время сообщения

        if (message.length() > 0) {
          //  scaledrone.publish(roomName, message);  //оригинальный метод

                    //создаем экземпляр одного Cообщения Юзера
            ChatMessage chatMessage = new ChatMessage(user_id,message, time_stamp);
                    // оправляем его в базу данных firebase
            myRef.push().setValue(chatMessage);
            messagesView.smoothScrollToPosition(messageAdapter.getCount() -1);
            editText.getText().clear();  //очищаем поле ввода
        }
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

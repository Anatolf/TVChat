package com.example.hohlosra4app;

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

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
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

import com.rengwuxian.materialedittext.MaterialEditText;
import com.vk.sdk.VKAccessToken;
import com.vk.sdk.VKAccessTokenTracker;
import com.vk.sdk.VKCallback;
import com.vk.sdk.VKScope;
import com.vk.sdk.VKSdk;
import com.vk.sdk.api.VKError;

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

    private String[] scope = new String[]{VKScope.EMAIL,VKScope.FRIENDS};

//    VKAccessTokenTracker vkAccessTokenTracker = new VKAccessTokenTracker() {
//        @Override
//        public void onVKAccessTokenChanged(VKAccessToken oldToken, VKAccessToken newToken) {
//            if (newToken == null) {
//// VKAccessToken is invalid
//                Intent intent = new Intent(ChatActivity.this,RegistrationActivity.class);
//                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP);
//                startActivity(intent);
//            }
//        }
//    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

//        vkAccessTokenTracker.startTracking();
        VKSdk.initialize(this);
        VKSdk.login(this, scope);
       // VK.login(activity, arrayListOf(VKScope.WALL, VKScope.PHOTOS));

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



/////////////// REGISTER SING IN ////////////////////////////

//        btnSingIn = findViewById(R.id.btnSingIn);
//        btnRegister = findViewById(R.id.btnRegister);
//
//        root = findViewById(R.id.root_element);
//
//        auth = FirebaseAuth.getInstance();
//        database = FirebaseDatabase.getInstance();
//        myRef = database.getReference("Users");

//        btnRegister.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                showRegisterWindow();
//            }
//        });
//        btnSingIn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                showSignInWindow();
//            }
//        });



// END ////////////////////////////////////////////////////////

    }

/////////VK VK VK ///////////////////////
    String ema;
    String use;
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (!VKSdk.onActivityResult(requestCode, resultCode, data, new VKCallback<VKAccessToken>() {
            @Override
            public void onResult(VKAccessToken res) {
// Пользователь успешно авторизовался
                ema = res.email;
                use = res.userId;

            }
            @Override
            public void onError(VKError error) {
// Произошла ошибка авторизации (например, пользователь запретил авторизацию)
            }
        })) {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }


// END ////////////////////////////////////////////
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

        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("Войти");
        dialog.setMessage("Чтобы отправлять сообщения, зарегестрируйтесь:");

        LayoutInflater inflater = LayoutInflater.from(this);
        View sign_in_window = inflater.inflate(R.layout.sing_in_window2, null);
        dialog.setView(sign_in_window);

//        final MaterialEditText email = sign_in_window.findViewById(R.id.email_field);
//        final MaterialEditText password = sign_in_window.findViewById(R.id.pass_field);
        final Button btnRegVk = sign_in_window.findViewById(R.id.reg_from_vk_btn);
        final Button btnRegOk = sign_in_window.findViewById(R.id.reg_from_ok_btn);
        final Button btnRegMail = sign_in_window.findViewById(R.id.reg_from_email_btn);

        // по кнопке "Через Вк"
        btnRegVk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {



                Toast.makeText(ChatActivity.this,
                        "ema= "+ ema + ", use= " + use,
                        Toast.LENGTH_SHORT).show();


            }
        });

        // по кнопке "Через Ок"
        btnRegOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(ChatActivity.this,
                        "функция пока не доступна",
                        Toast.LENGTH_SHORT).show();
            }
        });

        // по кнопке "Через Майл"
        btnRegMail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // сделать а возвращением результата //
                startActivity(new Intent(ChatActivity.this, RegistrationActivity.class));
            }
        });

        dialog.setNegativeButton("Отменить", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int which) {
                Snackbar.make(root_chat, "Без регистрации Вы можете только наблюдать", Snackbar.LENGTH_SHORT).show();
                dialogInterface.dismiss();
            }
        });
        dialog.setPositiveButton("Войти", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int which) {
//                if (TextUtils.isEmpty(email.getText().toString())) {
//                    Snackbar.make(root, "Введите вашу почту", Snackbar.LENGTH_SHORT).show();
//                    return;
//                }
//                if (password.getText().toString().length() < 5) {
//                    Snackbar.make(root, "Введите пароль длиннее 5 символов", Snackbar.LENGTH_SHORT).show();
//                    return;
//                }
                Snackbar.make(root_chat, "Мы нажали Войти", Snackbar.LENGTH_SHORT).show();

//                auth.signInWithEmailAndPassword(email.getText().toString(), password.getText().toString())
//                        .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
//                            @Override
//                            public void onSuccess(AuthResult authResult) {
//                                //    startActivity(new Intent(RegistrationActivity.this, MapActivity.class));
//                                Toast.makeText(RegistrationActivity.this,
//                                        "Тест Регистрации = Успешно! ",
//                                        Toast.LENGTH_SHORT).show();


                              //  finish();  // после завершения всех действий в диалоговом окне
                            }
//                        }).addOnFailureListener(new OnFailureListener() {
//                    @Override
//                    public void onFailure(@NonNull Exception e) {
//                        Snackbar.make(root, "Ошибка авторизации: " + e.getMessage(), Snackbar.LENGTH_SHORT).show();
//                    }
//                });
//            }
        });

        dialog.show();

        // переходит на активити регистрации Гоша Дударь
//        Intent intent = new Intent(ChatActivity.this, RegistrationActivity.class);
//        startActivity(intent);

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
  /////////////////// РЕГИСТРАЦИЯ И ВХОД //////////////////////////
//  private void showSignInWindow() {
//      AlertDialog.Builder dialog = new AlertDialog.Builder(this);
//      dialog.setTitle("Войти");
//      dialog.setMessage("Введите данные для входа");
//
//      LayoutInflater inflater = LayoutInflater.from(this);
//      View sign_in_window = inflater.inflate(R.layout.singin_window, null);
//      dialog.setView(sign_in_window);
//
//      final MaterialEditText email = sign_in_window.findViewById(R.id.email_field);
//      final MaterialEditText password = sign_in_window.findViewById(R.id.pass_field);
//
//
//      dialog.setNegativeButton("Отменить", new DialogInterface.OnClickListener() {
//          @Override
//          public void onClick(DialogInterface dialogInterface, int which) {
//              dialogInterface.dismiss();
//          }
//      });
//      dialog.setPositiveButton("Войти", new DialogInterface.OnClickListener() {
//          @Override
//          public void onClick(DialogInterface dialogInterface, int which) {
//              if (TextUtils.isEmpty(email.getText().toString())) {
//                  Snackbar.make(root, "Введите вашу почту", Snackbar.LENGTH_SHORT).show();
//                  return;
//              }
//              if (password.getText().toString().length() < 5) {
//                  Snackbar.make(root, "Введите пароль длиннее 5 символов", Snackbar.LENGTH_SHORT).show();
//                  return;
//              }
//
//              auth.signInWithEmailAndPassword(email.getText().toString(), password.getText().toString())
//                      .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
//                          @Override
//                          public void onSuccess(AuthResult authResult) {
//                             // startActivity(new Intent(ChatActivity.this, MapActivity.class));
//                              Toast.makeText(ChatActivity.this,
//                                      "Тест Регистрации = Успешно! ",
//                                      Toast.LENGTH_SHORT).show();
//                              finish();
//                          }
//                      }).addOnFailureListener(new OnFailureListener() {
//                  @Override
//                  public void onFailure(@NonNull Exception e) {
//                      Snackbar.make(root, "Ошибка авторизации: " + e.getMessage(), Snackbar.LENGTH_SHORT).show();
//                  }
//              });
//          }
//      });
//
//      dialog.show();
//
//  }
//
//    private void showRegisterWindow() {
//        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
//        dialog.setTitle("Зарегистрироваться");
//        dialog.setMessage("Введите данные для регистрации");
//
//        LayoutInflater inflater = LayoutInflater.from(this);
//        View register_window = inflater.inflate(R.layout.register_window, null);
//        dialog.setView(register_window);
//
//        final MaterialEditText email = register_window.findViewById(R.id.email_field);
//        final MaterialEditText password = register_window.findViewById(R.id.pass_field);
//        final MaterialEditText name = register_window.findViewById(R.id.name_field);
//        final MaterialEditText phoneNumber = register_window.findViewById(R.id.phone_field);
//
//        dialog.setNegativeButton("Отменить", new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialogInterface, int which) {
//                dialogInterface.dismiss();
//            }
//        });
//        dialog.setPositiveButton("Добавить", new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialogInterface, int which) {
//                if (TextUtils.isEmpty(email.getText().toString())) {
//                    Snackbar.make(root, "Введите вашу почту", Snackbar.LENGTH_SHORT).show();
//                    return;
//                }
//                if (password.getText().toString().length() < 5) {
//                    Snackbar.make(root, "Введите пароль длиннее 5 символов", Snackbar.LENGTH_SHORT).show();
//                    return;
//                }
//                if (TextUtils.isEmpty(name.getText().toString())) {
//                    Snackbar.make(root, "Введите ваше имя", Snackbar.LENGTH_SHORT).show();
//                    return;
//                }
//                if (TextUtils.isEmpty(phoneNumber.getText().toString())) {
//                    Snackbar.make(root, "Введите ваш телефон", Snackbar.LENGTH_SHORT).show();
//                    return;
//                }
//                // регистрация пользователя
//                auth.createUserWithEmailAndPassword(email.getText().toString(), password.getText().toString())
//                        .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
//                            @Override
//                            public void onSuccess(AuthResult authResult) {
//                                User user = new User();
//                                user.setEmail(email.getText().toString());
//                                user.setPassword(password.getText().toString());
//                                user.setName(name.getText().toString());
//                                user.setPhoneNumber(phoneNumber.getText().toString());
//
//                                myRef.child(FirebaseAuth.getInstance().getCurrentUser().getUid())
//                                        .setValue(user)
//                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
//                                            @Override
//                                            public void onSuccess(Void aVoid) {
//                                                Snackbar.make(root, "Пользователь добавлен", Snackbar.LENGTH_SHORT).show();
//                                            }
//                                        });
//                            }
//                        }).addOnFailureListener(new OnFailureListener() {
//                    @Override
//                    public void onFailure(@NonNull Exception e) {
//                        Snackbar.make(root, "Ошибка Косяк авторизации: " + e.getMessage(), Snackbar.LENGTH_SHORT).show();
//                    }
//                });
//            }
//        });
//
//        dialog.show();
//
//    }

  //end //////////////////////////////////////////////////////////
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

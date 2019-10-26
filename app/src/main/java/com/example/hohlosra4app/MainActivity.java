package com.example.hohlosra4app;


import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import com.example.hohlosra4app.Model.Channel;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;

import ru.ok.android.sdk.Odnoklassniki;
import ru.ok.android.sdk.OkListener;
import ru.ok.android.sdk.OkRequestMode;
import ru.ok.android.sdk.util.OkAuthType;
import ru.ok.android.sdk.util.OkScope;

public class MainActivity extends AppCompatActivity {

    public static final String TAG = "MainActivity";
    private RecyclerView rvTvChannelsList;
    private RecyclerView.LayoutManager rvLayoutManager;
    private TvChannelsAdapter tvChannelsAdapter;

    private FirebaseDatabase database;
    private DatabaseReference myRef;

     static final String CHANNEL_ID_EXTRA = "channel_id_extra";
     static final String USERS_IN_CHAT_EXTRA = "users_in_chart_extra";


    //Odnoklassniki odnoklassniki;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

// активити для заполнения базы)
//        Intent intent = new Intent(this,UploadChannelsToFirebaseActivity.class);
//        startActivity(intent);

        initRecyclerView();
        downloadChannelsFromFireBase();

        //testResponceOk();

    }

//    private void testResponceOk() {
//        odnoklassniki = Odnoklassniki.createInstance(this, "512000154078", "CPNKFHJGDIHBABABA");  // id "512000154078"
//        //btnTestOk = findViewById(R.id.test_ok_reg);
//        findViewById(R.id.test_ok_reg).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(final View view) {
//                odnoklassniki.requestAuthorization(MainActivity.this,
//                        "okauth://ok512000154078",
//                        OkAuthType.ANY,
//                        (OkScope.VALUABLE_ACCESS + ";" + OkScope.LONG_ACCESS_TOKEN));
//            }
//        });
//
//        //Map<String, String> parameters = new HashMap<>();
//        //parameters.put("key1", "value1");
//
////        Set<OkRequestMode> requestMode = new HashSet<>();
////        requestMode.add(OkRequestMode.SDK_SESSION);
//
//        odnoklassniki.requestAsync(
//                "users.getCurrentUser",
//                null,
//                OkRequestMode.getDEFAULT(),  // OkRequestMode.getDEFAULT()  // requestMode // EnumSet.of(OkRequestMode.SDK_SESSION)
//                new OkListener() {
//                    @Override
//                    public void onSuccess(@NotNull JSONObject jsonObject) {
//                        Log.d(TAG, "requestAsync: onSuccess " + jsonObject.toString());
//                    }
//
//                    @Override
//                    public void onError(@Nullable String s) {
//                        Log.d(TAG, "requestAsync: onError " + s);
//                    }
//                });
//
//    }

    private void downloadChannelsFromFireBase() {
        // подключаемся к базе
        database = FirebaseDatabase.getInstance();
        myRef = database.getReference("Channels");
        // делаем запрос в базу данных firebase
        Query myQuery = myRef;
        //  Query myQuery = myRef.orderByChild("numberChannel").equalTo(111);   // редактирование: сортирует ответ по "numberChannel" и 111
        myQuery.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
             //   UploadChannelsToFirebaseActivity.Channel channel = dataSnapshot.getValue(UploadChannelsToFirebaseActivity.Channel.class);
                Channel channel = dataSnapshot.getValue(Channel.class);

                // передаю в адаптер список ТВ-Каналов из Firebase (по одному):
                tvChannelsAdapter.setChannelsList(channel);

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


//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, @androidx.annotation.Nullable Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        if (Odnoklassniki.Companion.of(this).isActivityRequestOAuth(requestCode)) { // web Odnoklassniki.getInstance(this)
//            boolean isAuthCompleted = !TextUtils.isEmpty(data.getStringExtra("access_token"));
//            if (isAuthCompleted) {
//                Log.d(TAG, "isActivityRequestOAuth: Zaebis");
//            } else {
//                Log.d(TAG, "isActivityRequestOAuth: Pizdets");
//            }
//        } else if (Odnoklassniki.Companion.of(this).isActivityRequestViral(requestCode)) { // native
//            boolean isAuthCompleted = !TextUtils.isEmpty(data.getStringExtra("access_token"));
//            if (isAuthCompleted) {
//                Log.d(TAG, "isActivityRequestViral: Zaebis");
//            } else {
//                Log.d(TAG, "isActivityRequestViral: Pizdets");
//            }
//        } else {
//            super.onActivityResult(requestCode, resultCode, data);
//        }
//    }
//

    private void initRecyclerView(){

        // Attach RecyclerView xml item layout
        rvTvChannelsList = findViewById(R.id.rv_tv_channels);

        // User this to display items in Grid Layout with 2 columns
        rvLayoutManager = new GridLayoutManager(this, 2);

        // Attach layoutManager to recycler view
        rvTvChannelsList.setLayoutManager(rvLayoutManager);

// при нажатии на элемент:
        TvChannelsAdapter.OnChannelClickListener onChannelClickListener = new TvChannelsAdapter.OnChannelClickListener() {
            @Override
            public void onChannelClick(Channel channel, Integer usersIntoChat ) {

        // передаем в ChatActivity порядковый номер канала на который нажали и количество юзеров
                Intent intent = new Intent(MainActivity.this, ChatActivity.class);
                intent.putExtra(CHANNEL_ID_EXTRA, channel.channel_id);
                intent.putExtra(USERS_IN_CHAT_EXTRA, usersIntoChat);
                startActivity(intent);
            }
        };
// создаем адаптер с 2мя параметрами в конструкторе
        tvChannelsAdapter = new TvChannelsAdapter(MainActivity.this, onChannelClickListener);
        rvTvChannelsList.setAdapter(tvChannelsAdapter);
    }


}

package com.example.hohlosra4app;


import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.IgnoreExtraProperties;
import com.google.firebase.database.Query;

import java.io.Serializable;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    ArrayList<Channel> tvChannels = new ArrayList<>();

    private RecyclerView rvTvChannelsList;
    private RecyclerView.LayoutManager rvLayoutManager;
    private RecyclerViewAdapter tvChannelsAdapter;

    private FirebaseDatabase database;
    private DatabaseReference myRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

// активити для заполнения базы)
//        Intent intent = new Intent(this,UploadChannelsToFirebaseActivity.class);
//        startActivity(intent);

        initRecyclerView();
        downloadChannelsFromFireBase();

    }

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
                // adapter.add(channel);

                // наполняем ArrayList's твКаналами из Firebase
                tvChannels.add(channel);
                tvChannelsAdapter.setChannelAndUsersIntoList(channel);

//                // наполняем ArrayList's значениями из Firebase
//                assert channel != null;
//                nameTvChannelsList.add(channel.name);
//                usersIntoChatArrayList.add(channel.number);
//                // urlTvChannelsList.add(channel.urlChannel);
//
//                // отправляем их в ТВ-адаптер
//                tvChannelsAdapter.setChannelAndUsersIntoList(nameTvChannelsList,usersIntoChatArrayList);
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


    private void initRecyclerView(){

        // Attach RecyclerView xml item layout
        rvTvChannelsList = findViewById(R.id.rv_tv_channels);

        // User this to display items in Grid Layout with 2 columns
        rvLayoutManager = new GridLayoutManager(this, 2);

        // Attach layoutManager to recycler view
        rvTvChannelsList.setLayoutManager(rvLayoutManager);

// при нажатии на элемент:
        RecyclerViewAdapter.OnChannelClickListner onChannelClickListner = new RecyclerViewAdapter.OnChannelClickListner() {
            @Override
            public void onChannelClick(Integer channel, Integer usersIntoChat) {

        // передаем в ChatActivity порядковый номер канала на который нажали и количество юзеров
                Intent intent = new Intent(MainActivity.this, ChatActivity.class);
                intent.putExtra(Intent.EXTRA_INDEX, channel);
                intent.putExtra("count_users_into_this_chart", usersIntoChat);
                startActivity(intent);
            }
        };
// создаем адаптер с 2мя параметрами в конструкторе
        tvChannelsAdapter = new RecyclerViewAdapter(MainActivity.this, onChannelClickListner);
        rvTvChannelsList.setAdapter(tvChannelsAdapter);
    }


    // Класс Channel - один элемент с тремя параметрами (для добавления в базу данных firebase)
    @IgnoreExtraProperties
    static class Channel implements Serializable {
        public String name;
        public int number;
        public String urlChannel;

        public Channel() {
        }

        Channel(String name, int number, String url) {
            this.name = name;
            this.number = number;
            this.urlChannel = url;
        }
    }
}

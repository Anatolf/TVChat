package com.anatolf.tvchat;


import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import com.anatolf.tvchat.Model.Channel;
import com.anatolf.tvchat.R;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.squareup.picasso.Picasso;


public class MainActivity extends AppCompatActivity {
    //public static final String TAG = "MainActivity";

    private Toolbar toolbar;
    private ImageView icon_toolbar;
    private TextView head_text_toolbar;

    private RecyclerView rvTvChannelsList;
    private RecyclerView.LayoutManager rvLayoutManager;
    private TvChannelsAdapter tvChannelsAdapter;

    private FirebaseDatabase database;
    private DatabaseReference myRef;

    static final String CHANNEL_OBJECT_EXTRA = "channel_object_extra";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toolbar = findViewById(R.id.custom_tool_bar);
        icon_toolbar = findViewById(R.id.image_tool_bar);
        head_text_toolbar = findViewById(R.id.head_text_tool_bar);

// активити для заполнения базы)
//        Intent intent = new Intent(this,UploadChannelsToFirebaseActivity.class);
//        startActivity(intent);

        initTitleBar();
        initRecyclerView();
        downloadChannelsFromFireBase();
    }

    private void initTitleBar() {
        head_text_toolbar.setText(R.string.app_name);

        Picasso.get()
                .load(R.mipmap.ic_launcher)
                //.transform(new CircularTransformation(0)) // 0 - радиус по умолчанию делает максимальный кроп углов от квадрата
                .error(R.drawable.ic_launcher_foreground)
                .into(icon_toolbar);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
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

                Channel channel = dataSnapshot.getValue(Channel.class);
                channel.setFirebaseChannelId(dataSnapshot.getKey());

                // передаю в адаптер список ТВ-Каналов из Firebase (по одному):
                tvChannelsAdapter.setChannelsList(channel);

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                // обновляет количество пользователей в каналах:
                for (Channel ch : tvChannelsAdapter.tvChannelsList) {
                    if(ch.firebase_channel_id.equals(dataSnapshot.getKey())){
                        Channel channel = dataSnapshot.getValue(Channel.class);
                        ch.setCount_users(channel.count_users);
                        tvChannelsAdapter.notifyDataSetChanged();
                    }
                }

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


    private void initRecyclerView() {

        // Attach RecyclerView xml item layout
        rvTvChannelsList = findViewById(R.id.rv_tv_channels);

        // User this to display items in Grid Layout with 2 columns
        rvLayoutManager = new GridLayoutManager(this, 2);

        // Attach layoutManager to recycler view
        rvTvChannelsList.setLayoutManager(rvLayoutManager);

// при нажатии на элемент:
        TvChannelsAdapter.OnChannelClickListener onChannelClickListener = new TvChannelsAdapter.OnChannelClickListener() {
            @Override
            public void onChannelClick(Channel channel) {

                // передаем в ChatActivity channel
                Intent intent = new Intent(MainActivity.this, ChatActivity.class);
                intent.putExtra(CHANNEL_OBJECT_EXTRA, channel);
                startActivity(intent);
            }
        };
// создаем адаптер с 2мя параметрами в конструкторе
        tvChannelsAdapter = new TvChannelsAdapter(MainActivity.this, onChannelClickListener);
        rvTvChannelsList.setAdapter(tvChannelsAdapter);
    }


}

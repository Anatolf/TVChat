package com.example.hohlosra4app;


import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    ArrayList<String> tvChannelArrayList = new ArrayList<>();
    ArrayList<Integer> usersIntoChatArrayList = new ArrayList<>();

    private RecyclerView rvTvChannelsList;
    private RecyclerView.LayoutManager rvLayoutManager;
    private RecyclerViewAdapter tvChannelsAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initRecyclerView();
        loadChannels();
    }

    private void initRecyclerView(){

        // Attach RecyclerView xml item layout
        rvTvChannelsList = (RecyclerView) findViewById(R.id.rv_tv_channels);


        // User this to display items vertically
        //rvLayoutManager = new LinearLayoutManager(this);

        // User this to display items in Grid Layout with 2 columns
        rvLayoutManager = new GridLayoutManager(this, 2);

        // Attach layoutManager to recycler view
        rvTvChannelsList.setLayoutManager(rvLayoutManager);

        // Pass data to our custom adapter
        tvChannelsAdapter = new RecyclerViewAdapter(this, tvChannelArrayList, usersIntoChatArrayList);

        // Attach custom adapter to recycler view
        rvTvChannelsList.setAdapter(tvChannelsAdapter);


////////////////experiment/////////////////////////////
// при нажатии на элемент получаем его порядковый номер в Тоаст
        RecyclerViewAdapter.OnChannelClickListner onChannelClickListner = new RecyclerViewAdapter.OnChannelClickListner() {
            @Override
            public void onChannelClick(Integer channel, Integer usersIntoChat) {
//                Toast.makeText(MainActivity.this,
//                            "Into MainActivity: " + channel,
//                            Toast.LENGTH_SHORT).show();

                // передаем в ChatActivity порядковый номер канала на который нажали
                Intent intent = new Intent(MainActivity.this, ChatActivity.class);
                intent.putExtra(Intent.EXTRA_INDEX, channel);
                intent.putExtra("count_users_into_this_chart", usersIntoChat);
                startActivity(intent);
            }
        };

        tvChannelsAdapter = new RecyclerViewAdapter(onChannelClickListner);
        rvTvChannelsList.setAdapter(tvChannelsAdapter);

//end/////////////////////////////////////////////////
    }

    private void loadChannels(){
        // заполняем Демо лист значениями:
        // Каналы:
        tvChannelArrayList.add("TvChannel 1");
        tvChannelArrayList.add("TvChannel 2");
        tvChannelArrayList.add("TvChannel 3");
        tvChannelArrayList.add("TvChannel 4");
        tvChannelArrayList.add("TvChannel 5");
        tvChannelArrayList.add("TvChannel 6");
        tvChannelArrayList.add("TvChannel 7");
        tvChannelArrayList.add("TvChannel 8");
        tvChannelArrayList.add("TvChannel 9");
        tvChannelArrayList.add("TvChannel 10");

        // Юзеры в чате:
        usersIntoChatArrayList.add(45);
        usersIntoChatArrayList.add(0);
        usersIntoChatArrayList.add(0);
        usersIntoChatArrayList.add(6);
        usersIntoChatArrayList.add(123);
        usersIntoChatArrayList.add(0);
        usersIntoChatArrayList.add(0);
        usersIntoChatArrayList.add(15);
        usersIntoChatArrayList.add(0);
        usersIntoChatArrayList.add(0);

        tvChannelsAdapter.setChannelAndUsersIntoList(tvChannelArrayList,usersIntoChatArrayList);
    }
}

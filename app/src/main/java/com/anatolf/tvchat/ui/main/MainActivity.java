package com.anatolf.tvchat.ui.main;


import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.anatolf.tvchat.App;
import com.anatolf.tvchat.R;
import com.anatolf.tvchat.net.model.Channel;
import com.anatolf.tvchat.ui.chat.ChatActivity;
import com.anatolf.tvchat.ui.uploadchannels.UploadChannelsToFirebaseActivity;
import com.google.firebase.database.DataSnapshot;
import com.squareup.picasso.Picasso;


public class MainActivity extends AppCompatActivity implements MainContractView {

    public static final String CHANNEL_OBJECT_EXTRA = "channel_object_extra";

    private Toolbar toolbar;
    private ImageView icon_toolbar;
    private TextView head_text_toolbar;
    private RecyclerView rvTvChannels;
    private TvChannelsAdapter tvChannelsAdapter;

    private MainPresenter presenter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView();
        presenter = App.get().getMainPresenter();
        presenter.attachView(this);
        presenter.autoDownloadChannels();
    }

    private void initView() {
        setContentView(R.layout.activity_main);

        toolbar = findViewById(R.id.custom_tool_bar);
        icon_toolbar = findViewById(R.id.image_tool_bar);
        head_text_toolbar = findViewById(R.id.head_text_tool_bar);

        // Developers method
        // startActivityFillDb();

        initTitleBar();
        initChannelsRecyclerView();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        presenter.detachView();
    }

    private void initTitleBar() {
        head_text_toolbar.setText(R.string.app_name);

        Picasso.get()
                .load(R.drawable.ic_tvchat_255)
                .into(icon_toolbar);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
    }

    private void initChannelsRecyclerView() {

        RecyclerView.LayoutManager rvLayoutManager = new GridLayoutManager(this, 2);
        rvTvChannels = findViewById(R.id.rv_tv_channels);
        rvTvChannels.setLayoutManager(rvLayoutManager);

        TvChannelsAdapter.OnChannelClickListener onChannelClickListener = new TvChannelsAdapter.OnChannelClickListener() {
            @Override
            public void onChannelClick(Channel channel) {

                Intent intent = new Intent(MainActivity.this, ChatActivity.class);
                intent.putExtra(CHANNEL_OBJECT_EXTRA, channel);
                startActivity(intent);
            }
        };

        tvChannelsAdapter = new TvChannelsAdapter(MainActivity.this, onChannelClickListener);
        rvTvChannels.setAdapter(tvChannelsAdapter);
    }

    @Override
    public void showAddedChannel(Channel channel) {
        tvChannelsAdapter.setChannel(channel);
    }

    @Override
    public void showUsersCountOnline(DataSnapshot dataSnapshot) {
        updateUsersCountOnline(dataSnapshot);
    }

    private void updateUsersCountOnline(DataSnapshot dataSnapshot) {
        for (Channel ch : tvChannelsAdapter.tvChannelsList) {
            if(ch.firebase_channel_id.equals(dataSnapshot.getKey())){
                Channel channel = dataSnapshot.getValue(Channel.class);
                ch.setCount_users(channel.count_users);
                tvChannelsAdapter.notifyDataSetChanged();
            }
        }
    }

    private void startActivityFillDb() {
        Intent intent = new Intent(this, UploadChannelsToFirebaseActivity.class);
        startActivity(intent);
    }
}

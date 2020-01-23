package com.anatolf.tvchat.ui.fragment;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.anatolf.tvchat.App;
import com.anatolf.tvchat.R;
import com.anatolf.tvchat.net.model.Channel;
import com.anatolf.tvchat.ui.chat.ChatActivity;
import com.anatolf.tvchat.ui.main.MainContractView;
import com.anatolf.tvchat.ui.main.MainPresenter;
import com.anatolf.tvchat.ui.main.TvChannelsAdapter;
import com.google.firebase.database.DataSnapshot;


public class FragmentMain extends Fragment implements MainContractView {

    public static final String CHANNEL_OBJECT_EXTRA = "channel_object_extra";

    private TvChannelsAdapter tvChannelsAdapter;
    private MainPresenter presenter;

    public FragmentMain() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, container,false);

        initChannelsRecyclerView(view);

        Log.d("dfghjkl", "onCreateView: ");

        presenter = App.get().getMainPresenter();
        presenter.attachView(this);
        presenter.autoDownloadChannels();

        return view;
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        presenter.detachView();
    }


    private void initChannelsRecyclerView(View view) {

        RecyclerView.LayoutManager rvLayoutManager = new GridLayoutManager(getActivity(), 2);
        RecyclerView rvTvChannels = view.findViewById(R.id.rv_tv_channels);
        rvTvChannels.setLayoutManager(rvLayoutManager);

        TvChannelsAdapter.OnChannelClickListener onChannelClickListener = new TvChannelsAdapter.OnChannelClickListener() {
            @Override
            public void onChannelClick(Channel channel) {

                Intent intent = new Intent(getActivity(), ChatActivity.class);
                intent.putExtra(CHANNEL_OBJECT_EXTRA, channel);
                startActivity(intent);
            }
        };

        tvChannelsAdapter = new TvChannelsAdapter(getActivity(), onChannelClickListener);
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
}

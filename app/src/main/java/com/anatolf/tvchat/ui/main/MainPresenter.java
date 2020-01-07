package com.anatolf.tvchat.ui.main;

import com.anatolf.tvchat.net.model.Channel;
import com.google.firebase.database.DataSnapshot;

public class MainPresenter {

    private MainContractView view;
    private final MainModel model;

    public MainPresenter() {
        this.model = new MainModel();
    }

    public void attachView(MainContractView view) {
        this.view = view;
    }

    public void detachView() {
        view = null;
    }


    public void autoDownloadChannels() {
        model.downloadChannelsFromFireBase(new MainModel.FireBaseListener() {

            @Override
            public void onGetChannel(Channel channel) {
                if (view != null) {
                    view.showAddedChannel(channel);
                }
            }

            @Override
            public void onGetUsersCountOnline(DataSnapshot dataSnapshot) {
                if (view != null) {
                    view.showUsersCountOnline(dataSnapshot);
                }
            }
        });
    }
}

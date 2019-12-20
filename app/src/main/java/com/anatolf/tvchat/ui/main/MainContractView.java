package com.anatolf.tvchat.ui.main;

import com.anatolf.tvchat.model.Channel;
import com.google.firebase.database.DataSnapshot;

public interface MainContractView {
    void onGetChannel(Channel channel);
    void onGetUsersOnline(DataSnapshot dataSnapshot);
}

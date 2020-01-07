package com.anatolf.tvchat.ui.main;

import com.anatolf.tvchat.net.model.Channel;
import com.google.firebase.database.DataSnapshot;

public interface MainContractView {
    void showAddedChannel(Channel channel);
    void showUsersCountOnline(DataSnapshot dataSnapshot);
}

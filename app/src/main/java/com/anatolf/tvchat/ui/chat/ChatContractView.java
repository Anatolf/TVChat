package com.anatolf.tvchat.ui.chat;

import com.anatolf.tvchat.model.Message;
import com.google.firebase.database.DataSnapshot;

public interface ChatContractView {
    void showNewSingleMessage(Message message);
    void showNewLikesCountMessages(DataSnapshot dataSnapshot);
    void showUnreadMessages();
    void scrollDown();
    void showText(String text);
}

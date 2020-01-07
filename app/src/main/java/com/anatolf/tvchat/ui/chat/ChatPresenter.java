package com.anatolf.tvchat.ui.chat;

import com.anatolf.tvchat.model.Message;
import com.google.firebase.database.DataSnapshot;

public class ChatPresenter {

    private static final String TAG = "ChatPresenterLogs";

    private ChatContractView view;
    private final ChatModel model;

    int countUnreadMessages = 0;

    String channel_name = "";
    String channel_image_url = "";


    public ChatPresenter(String channel_id, String firebase_channel_id) {
        this.model = new ChatModel(channel_id, firebase_channel_id); // создаём модель (получает данные из FB)
    }

    public void attachView(ChatContractView view) {
        this.view = view;
    }

    public void detachView() {
        view = null;
    }

    public void getAllMessages() {
        this.model.getAllMessages(new ChatModel.GetMessageListener() {

            @Override
            public void onShowNewMessages() {
                countUnreadMessages++;
                if (view != null) {
                    view.showUnreadMessages();
                }
            }

            @Override
            public void onUpdateSingleMessage(Message message) {
                if (view != null) {
                    view.showNewSingleMessage(message);
                }
            }

            @Override
            public void onLikeChanged(DataSnapshot dataSnapshot) {
                if (view != null) {
                    view.showNewLikesCountMessages(dataSnapshot);
                }
            }

            @Override
            public void onAddMyNewMessage() {
                if (view != null) {
                    view.scrollDown();
                }
            }
        });
    }

    public void resetUnreadMessages() {
        countUnreadMessages = 0;
    }

    public void incrementOnlineUsersCountInChat() {
        model.incrementOnlineUsersCountInChat();
    }

    public void decrementOnlineUsersCountInChat() {
        model.decrementOnlineUsersCountInChat();
    }

    public void setLike(Message message, boolean like) {
        model.setLike(message, like);
    }

    public boolean isNotAuth() {
        return model.isNotAuth();
    }

    public void sendMessage(String message) {
        model.sendMessage(message);
    }

    public void cleanTempLists() {
        model.clearMessages();
    }
}

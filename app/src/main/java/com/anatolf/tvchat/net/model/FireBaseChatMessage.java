package com.anatolf.tvchat.net.model;

import com.google.firebase.database.IgnoreExtraProperties;

import java.io.Serializable;
import java.util.HashMap;

@IgnoreExtraProperties
public
class FireBaseChatMessage implements Serializable {

    public String user_id;
    public String message;
    public long timeStamp;
    public String social_tag;
    public HashMap<String, Boolean> liked_users;

    public FireBaseChatMessage() {
    }

    public FireBaseChatMessage(String user_id, String message,
                               long timeStamp, String social_tag,
                               HashMap<String, Boolean> liked_users) {

        this.user_id = user_id;
        this.message = message;
        this.timeStamp = timeStamp;
        this.social_tag = social_tag;
        this.liked_users = liked_users;
    }
}

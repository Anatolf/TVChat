package com.anatolf.tvchat.Model;

import com.google.firebase.database.IgnoreExtraProperties;

import java.io.Serializable;
import java.util.HashMap;

// Класс Channel - один элемент с тремя параметрами (для добавления в базу данных firebase)
@IgnoreExtraProperties
public class Channel implements Serializable {
    public String channel_id;
    public String name;
    public String urlChannel;
    public String firebase_channel_id;
    public HashMap<String, Boolean> count_users;

    public Channel() {
    }

    public void setFirebaseChannelId(String firebase_channel_id) {
        this.firebase_channel_id = firebase_channel_id;
    }

    public void setCount_users(HashMap<String, Boolean> count_users) {
        this.count_users = count_users;
    }

    public HashMap<String, Boolean> getCountUsersInChat() {
        return count_users;
    }

    public Channel(String channel_id, String name, String url) {
        this.channel_id = channel_id;
        this.name = name;
        this.urlChannel = url;
    }
}

package com.example.hohlosra4app.Model;

import com.google.firebase.database.IgnoreExtraProperties;

import java.io.Serializable;

// Класс Channel - один элемент с тремя параметрами (для добавления в базу данных firebase)
@IgnoreExtraProperties
public class Channel implements Serializable {
    public String channel_id;
    public String name;
    public int number;
    public String urlChannel;

    public Channel() {
    }

    public Channel(String channel_id, String name, int number, String url) {
        this.channel_id = channel_id;
        this.name = name;
        this.number = number;
        this.urlChannel = url;
    }
}

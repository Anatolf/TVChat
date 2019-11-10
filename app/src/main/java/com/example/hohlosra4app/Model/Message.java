package com.example.hohlosra4app.Model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Random;

public class Message implements Serializable {
    private String fireBase_id;
    private String id;
    private String text;
    private String time;
    private boolean belongsToCurrentUser;
    private String name;
    private String color;
    private String avatar;
    private HashMap<String,Boolean> liked_users;

    public Message(String text, String time, HashMap<String,Boolean> liked_users, String fireBase_id) {
        this.fireBase_id = fireBase_id;
        this.text = text;
        this.time = time;
        this.name = getRandomName();
        this.color = getRandomColor();
        this.liked_users = liked_users;

    }

    public Message(String id, String text, String time, boolean belongsToCurrentUser, String name,
                   String avatar, HashMap<String,Boolean> liked_users, String fireBase_id) {
        this.id = id;
        this.text = text;
        this.time = time;
        this.belongsToCurrentUser = belongsToCurrentUser;
        this.name = name;
        this.avatar = avatar;
        this.liked_users = liked_users;
        this.fireBase_id = fireBase_id;
    }

    public String getId() {
        return id;
    }

    public String getText() {
        return text;
    }

    public boolean isBelongsToCurrentUser() {
        return belongsToCurrentUser;
    }

    public String getName() {
        return name;
    }

    public String getColor() {
        return color;
    }

    public String getTime() {
        return time;
    }

    public String getAvatar() {
        return avatar;
    }

    public HashMap<String, Boolean> getLiked_users() {
        return liked_users;
    }

    public String getFireBase_id() {
        return fireBase_id;
    }

    public void setLikes(HashMap<String,Boolean> liked_users) {
        this.liked_users = liked_users;
    }

    static String getRandomName() {
        String[] adjs = {"autumn", "hidden", "bitter", "misty", "silent", "empty", "dry", "dark", "summer", "icy", "delicate", "quiet", "white", "cool", "spring", "winter", "patient", "twilight", "dawn", "crimson", "wispy", "weathered", "blue", "billowing", "broken", "cold", "damp", "falling", "frosty", "green", "long", "late", "lingering", "bold", "little", "morning", "muddy", "old", "red", "rough", "still", "small", "sparkling", "throbbing", "shy", "wandering", "withered", "wild", "black", "young", "holy", "solitary", "fragrant", "aged", "snowy", "proud", "floral", "restless", "divine", "polished", "ancient", "purple", "lively", "nameless"};
        String[] nouns = {"waterfall", "river", "breeze", "moon", "rain", "wind", "sea", "morning", "snow", "lake", "sunset", "pine", "shadow", "leaf", "dawn", "glitter", "forest", "hill", "cloud", "meadow", "sun", "glade", "bird", "brook", "butterfly", "bush", "dew", "dust", "field", "fire", "flower", "firefly", "feather", "grass", "haze", "mountain", "night", "pond", "darkness", "snowflake", "silence", "sound", "sky", "shape", "surf", "thunder", "violet", "water", "wildflower", "wave", "water", "resonance", "sun", "wood", "dream", "cherry", "tree", "fog", "frost", "voice", "paper", "frog", "smoke", "star"};
        return (
                adjs[(int) Math.floor(Math.random() * adjs.length)] +
                        "_" +
                        nouns[(int) Math.floor(Math.random() * nouns.length)]
        );
    }

    static String getRandomColor() {
        Random r = new Random();
        StringBuffer sb = new StringBuffer("#");
        while (sb.length() < 7) {
            sb.append(Integer.toHexString(r.nextInt()));
        }
        return sb.toString().substring(0, 7);
    }

    @Override
    public String toString() {
        return "Data{" +
                "name='" + name + '\'' +
                ", color='" + color + '\'' +
                '}';
    }
}

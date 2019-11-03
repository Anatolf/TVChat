package com.example.hohlosra4app.Model;

import java.io.Serializable;
import java.util.Random;

public class Message implements Serializable {
    private String id;
    private String text;
    private String time;
    private boolean belongsToCurrentUser;
    private String name;
    private String color;
    private String avatar;

    public Message(String id, String text, String time, boolean belongsToCurrentUser) {
        this.id = id;
        this.text = text;
        this.time = time;
        this.belongsToCurrentUser = belongsToCurrentUser;
        this.name = getRandomName();
        this.color = getRandomColor();
    }

    public Message(String id, String text, String time, boolean belongsToCurrentUser, String name, String avatar) {
        this.id = id;
        this.text = text;
        this.time = time;
        this.belongsToCurrentUser = belongsToCurrentUser;
        this.name = name;
        this.avatar = avatar;
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
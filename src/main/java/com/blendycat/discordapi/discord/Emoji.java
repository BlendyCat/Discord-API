package com.blendycat.discordapi.discord;

public class Emoji {

    private String name;
    private String id;

    public Emoji(String name, String id) {
        this.name = name;
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public String getID() {
        return id;
    }
}

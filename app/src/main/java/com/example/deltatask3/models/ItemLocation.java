package com.example.deltatask3.models;

import com.google.gson.annotations.SerializedName;

public class ItemLocation {

    private int id;
    private String name;
    @SerializedName("sprites")
    private Sprite sprite;
    private int mode;

    public ItemLocation(String name) {
        this.name = name;
    }

    public class Sprite{
        @SerializedName("default")
        private String default_sprite;

        public String getDefault_sprite() {
            return default_sprite;
        }
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Sprite getSprite() {
        return sprite;
    }

    public int getMode() {
        return mode;
    }

    public void setMode(int mode) {
        this.mode = mode;
    }
}

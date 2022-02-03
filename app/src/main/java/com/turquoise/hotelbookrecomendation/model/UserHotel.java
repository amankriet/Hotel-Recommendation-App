
package com.turquoise.hotelbookrecomendation.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.HashMap;

public class UserHotel {

    @SerializedName("name")
    @Expose
    private String name;
    @SerializedName("completed")
    @Expose
    private Boolean completed;
    @SerializedName("hotel")
    @Expose
    private Hotel hotel;
    @SerializedName("rooms")
    @Expose
    private int rooms;
    @SerializedName("guests")
    @Expose
    private int guests;
    @SerializedName("options")
    @Expose
    private HashMap<String, Boolean> options;

    private String favTags;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Boolean getCompleted() {
        return completed;
    }

    public void setCompleted(Boolean completed) {
        this.completed = completed;
    }

    public Hotel getHotel() {
        return hotel;
    }

    public void setHotel(Hotel hotel) {
        this.hotel = hotel;
    }

    public int getRooms() {
        return rooms;
    }

    public void setRooms(int rooms) {
        this.rooms = rooms;
    }

    public int getGuests() {
        return guests;
    }

    public void setGuests(int guests) {
        this.guests = guests;
    }

    public HashMap<String, Boolean> getOptions() {
        return options;
    }

    public void setOptions(HashMap<String, Boolean> options) {
        this.options = options;
    }

    public String getFavTags() {
        return favTags;
    }

    public void setFavTags(String favTags) {
        this.favTags+=favTags+"\n";
    }

}

package com.example.bookmytiffin;

import android.app.Application;

public class Current_Location extends Application {
    private double curr_lat = 18.5204,curr_long = 73.8567;
    private String curr_address="Kothrud, Pune";

    public double getCurr_lat() {
        return curr_lat;
    }

    public void setCurr_lat(double curr_lat) {
        this.curr_lat = curr_lat;
    }

    public double getCurr_long() {
        return curr_long;
    }

    public void setCurr_long(double curr_long) {
        this.curr_long = curr_long;
    }

    public String getCurr_address() {
        return curr_address;
    }

    public void setCurr_address(String curr_address) {
        this.curr_address = curr_address;
    }
}

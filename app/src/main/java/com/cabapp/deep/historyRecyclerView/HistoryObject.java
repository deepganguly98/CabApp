package com.cabapp.deep.historyRecyclerView;

/**
 * Created by Deep on 2/6/2018.
 */

public class HistoryObject {
    private String rideId;
    private String time;

    public HistoryObject(String rideId, String time) {
        this.rideId = rideId;
        this.time = time;
    }

    public String getRideId(){
        return rideId;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }
}

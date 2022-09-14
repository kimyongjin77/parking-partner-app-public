package com.yh.parkingpartner.model;

import java.util.ArrayList;

public class ReviewListRes {
    private String result;
    private int count;
    private ArrayList<Review> items;

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public ArrayList<Review> getItems() {
        return items;
    }

    public void setItems(ArrayList<Review> items) {
        this.items = items;
    }
}

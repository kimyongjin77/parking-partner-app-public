package com.yh.parkingpartner.model;

import java.io.Serializable;
import java.util.ArrayList;

public class PlaceListRes implements Serializable {

    private String status="";
    private String next_page_token="";
    private ArrayList<Place> candidates;
    private ArrayList<Place> results;

    public ArrayList<Place> getResults() {
        return results;
    }

    public void setResults(ArrayList<Place> results) {
        this.results = results;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getNext_page_token() {
        return next_page_token;
    }

    public void setNext_page_token(String next_page_token) {
        this.next_page_token = next_page_token;
    }

    public ArrayList<Place> getCandidates() {
        return candidates;
    }

    public void setCandidates(ArrayList<Place> candidates) {
        this.candidates = candidates;
    }
}

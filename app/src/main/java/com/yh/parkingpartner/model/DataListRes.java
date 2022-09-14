package com.yh.parkingpartner.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class DataListRes implements Serializable {

    private String result;
    private int count;
    private ArrayList<Data> items;

    int prk_id;                             //주차완료id
    String img_prk;                      //주차사진URL
    String  DetectedText;           //AWS 텍스트감지 결과 텍스트
    double Confidence;              //AWS 텍스트감지 결과 텍스트 확률

    public ArrayList<Data> getItems() {
        return items;
    }

    public void setItems(ArrayList<Data> items) {
        this.items = items;
    }

    public int getPrk_id() {
        return prk_id;
    }

    public void setPrk_id(int prk_id) {
        this.prk_id = prk_id;
    }

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

    public String getImg_prk() {
        return img_prk;
    }

    public void setImg_prk(String img_prk) {
        this.img_prk = img_prk;
    }

    public String getDetectedText() {
        return DetectedText;
    }

    public void setDetectedText(String detectedText) {
        DetectedText = detectedText;
    }

    public double getConfidence() {
        return Confidence;
    }

    public void setConfidence(double confidence) {
        Confidence = confidence;
    }
}

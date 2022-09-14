package com.yh.parkingpartner.model;

import java.io.Serializable;

public class Data implements Serializable {

    String prk_center_id;               //"15204-29391-00083-00-1",
    String prk_plce_nm;                 // "신동아홈타운",
    String prk_plce_adres;              // "인천광역시 서구 심곡동  325-25",
    double prk_plce_entrc_la;           // 126.6721880577,
    double prk_plce_entrc_lo;           // 37.5459130584,
    int prk_cmprt_co;                       // 주차장의 총 주차 구획 수 46,
    int pkfc_Available_ParkingLots_total;       // 주차가능 구획 수 0,
    int parking_chrge_bs_time;          // 기본시간 30,
    int parking_chrge_bs_chrg;          // 기본요금 0,
    int parking_chrge_adit_unit_time;       // 추가단위시간 10,
    int parking_chrge_adit_unit_chrge;      // 추가단위요금 0,
    int parking_chrge_one_day_chrge;      // 1일요금 0,
    float rating;                                           // 별점평균 0.0,

    int prk_id;                             //주차완료id
    int push_prk_id;                    //푸위알림용도 최종 주차완료id
    String start_prk_at;                //주차입차시간
    String img_prk;                      //주차사진URL
    String prk_area;                     //주차구역
    String end_prk;                      //주차출차시간
    String use_prk_at;                      //주차시간(분)
    int end_pay;                             //주차요금
    int charge;
    double distance;
    int available;

    public int getPush_prk_id() {
        return push_prk_id;
    }

    public void setPush_prk_id(int push_prk_id) {
        this.push_prk_id = push_prk_id;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public int getCharge() {
        return charge;
    }

    public void setCharge(int charge) {
        this.charge = charge;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(int distance) {
        this.distance = distance;
    }

    public int getAvailable() {
        return available;
    }

    public void setAvailable(int available) {
        this.available = available;
    }

    public int getPrk_id() {
        return prk_id;
    }

    public void setPrk_id(int prk_id) {
        this.prk_id = prk_id;
    }

    public String getStart_prk_at() {
        return start_prk_at;
    }

    public void setStart_prk_at(String start_prk_at) {
        this.start_prk_at = start_prk_at;
    }

    public String getImg_prk() {
        return img_prk;
    }

    public void setImg_prk(String img_prk) {
        this.img_prk = img_prk;
    }

    public String getPrk_area() {
        return prk_area;
    }

    public void setPrk_area(String prk_area) {
        this.prk_area = prk_area;
    }

    public String getEnd_prk() {
        return end_prk;
    }

    public void setEnd_prk(String end_prk) {
        this.end_prk = end_prk;
    }

    public int getEnd_pay() {
        return end_pay;
    }

    public void setEnd_pay(int end_pay) {
        this.end_pay = end_pay;
    }

    public float getRating() {
        return rating;
    }

    public void setRating(float rating) {
        this.rating = rating;
    }

    public int getParking_chrge_one_day_chrge() {
        return parking_chrge_one_day_chrge;
    }

    public void setParking_chrge_one_day_chrge(int parking_chrge_one_day_chrge) {
        this.parking_chrge_one_day_chrge = parking_chrge_one_day_chrge;
    }

    public String getPrk_center_id() {
        return prk_center_id;
    }

    public void setPrk_center_id(String prk_center_id) {
        this.prk_center_id = prk_center_id;
    }

    public String getPrk_plce_nm() {
        return prk_plce_nm;
    }

    public void setPrk_plce_nm(String prk_plce_nm) {
        this.prk_plce_nm = prk_plce_nm;
    }

    public String getPrk_plce_adres() {
        return prk_plce_adres;
    }

    public void setPrk_plce_adres(String prk_plce_adres) {
        this.prk_plce_adres = prk_plce_adres;
    }

    public double getPrk_plce_entrc_la() {
        return prk_plce_entrc_la;
    }

    public void setPrk_plce_entrc_la(double prk_plce_entrc_la) {
        this.prk_plce_entrc_la = prk_plce_entrc_la;
    }

    public String getUse_prk_at() {
        return use_prk_at;
    }

    public void setUse_prk_at(String use_prk_at) {
        this.use_prk_at = use_prk_at;
    }

    public double getPrk_plce_entrc_lo() {
        return prk_plce_entrc_lo;
    }

    public void setPrk_plce_entrc_lo(double prk_plce_entrc_lo) {
        this.prk_plce_entrc_lo = prk_plce_entrc_lo;
    }

    public int getPrk_cmprt_co() {
        return prk_cmprt_co;
    }

    public void setPrk_cmprt_co(int prk_cmprt_co) {
        this.prk_cmprt_co = prk_cmprt_co;
    }

    public int getPkfc_Available_ParkingLots_total() {
        return pkfc_Available_ParkingLots_total;
    }

    public void setPkfc_Available_ParkingLots_total(int pkfc_Available_ParkingLots_total) {
        this.pkfc_Available_ParkingLots_total = pkfc_Available_ParkingLots_total;
    }

    public int getParking_chrge_bs_time() {
        return parking_chrge_bs_time;
    }

    public void setParking_chrge_bs_time(int parking_chrge_bs_time) {
        this.parking_chrge_bs_time = parking_chrge_bs_time;
    }

    public int getParking_chrge_bs_chrg() {
        return parking_chrge_bs_chrg;
    }

    public void setParking_chrge_bs_chrg(int parking_chrge_bs_chrg) {
        this.parking_chrge_bs_chrg = parking_chrge_bs_chrg;
    }

    public int getParking_chrge_adit_unit_time() {
        return parking_chrge_adit_unit_time;
    }

    public void setParking_chrge_adit_unit_time(int parking_chrge_adit_unit_time) {
        this.parking_chrge_adit_unit_time = parking_chrge_adit_unit_time;
    }

    public int getParking_chrge_adit_unit_chrge() {
        return parking_chrge_adit_unit_chrge;
    }

    public void setParking_chrge_adit_unit_chrge(int parking_chrge_adit_unit_chrge) {
        this.parking_chrge_adit_unit_chrge = parking_chrge_adit_unit_chrge;
    }
}

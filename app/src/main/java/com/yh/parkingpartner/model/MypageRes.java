package com.yh.parkingpartner.model;

public class MypageRes {
    private String result;
    private int total_cnt;
    private int write_cnt;
    private int unwritten_cnt;

    public int getTotal_cnt() {
        return total_cnt;
    }

    public void setTotal_cnt(int total_cnt) {
        this.total_cnt = total_cnt;
    }

    public int getWrite_cnt() {
        return write_cnt;
    }

    public void setWrite_cnt(int write_cnt) {
        this.write_cnt = write_cnt;
    }

    public int getUnwritten_cnt() {
        return unwritten_cnt;
    }

    public void setUnwritten_cnt(int unwritten_cnt) {
        this.unwritten_cnt = unwritten_cnt;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }
}

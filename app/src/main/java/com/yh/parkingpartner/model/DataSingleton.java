package com.yh.parkingpartner.model;

import java.util.ArrayList;

public class DataSingleton {
    public ArrayList<Data> dataArrayList;
    private  static DataSingleton dataSingleton =null;

    private DataSingleton() {
        this.dataArrayList = new ArrayList<>();
    }

    public static DataSingleton getInstance(){
        if(dataSingleton == null){
            dataSingleton =new DataSingleton();
        }
        return dataSingleton;
    }
}

package com.yh.parkingpartner.api;

import com.yh.parkingpartner.model.DataListRes;
import com.yh.parkingpartner.model.User;
import com.yh.parkingpartner.model.UserRes;

import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;
import retrofit2.http.QueryMap;

public interface ApiFirstFragment {

    // 좌표기반 주변 주차장 가져오기 : /parking
//    lat = request.args['lat']
//    log = request.args['log']
    @GET("/parking")
    Call<DataListRes> aroundParkingLot(
            @Query("lat") double lat,
            @Query("log") double log
    );

    @GET("/parking")
    Call<DataListRes> aroundParkingLot(@QueryMap Map<String,Object> querys);

}

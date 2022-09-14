package com.yh.parkingpartner.api;

import com.yh.parkingpartner.model.DataListRes;

import java.util.Map;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;
import retrofit2.http.QueryMap;

public interface ApiParkingActivity {

    // 주차장 리스트 가져오는 API
    @GET("/parkingList")
    Call<DataListRes> getParkingList(@Query("lat") double lat,
                                     @Query("log") double log,
                                     @Query("order") String order,
                                     @Query("offset") int offset,
                                     @Query("limit") int limit);
}


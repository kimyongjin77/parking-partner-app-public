package com.yh.parkingpartner.api;

import com.yh.parkingpartner.model.DataListRes;
import com.yh.parkingpartner.model.Data;


import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;



public interface ApiThirdFragment {

    @GET("/parkLct/{prk_id}")
    Call<DataListRes> getDataList(@Header("Authorization") String token,
                                   @Path("prk_id") int prk_id);



}

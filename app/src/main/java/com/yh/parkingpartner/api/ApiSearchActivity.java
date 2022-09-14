package com.yh.parkingpartner.api;

import com.yh.parkingpartner.model.PlaceListRes;

import java.util.Map;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.QueryMap;

public interface ApiSearchActivity {
    // /maps/api/place/findplacefromtext/json
    @GET("/maps/api/place/findplacefromtext/json")
    Call<PlaceListRes> googleFindPlace(
            @QueryMap Map<String,Object> querys
    );

    // /maps/api/place/textsearch/json
    @GET("/maps/api/place/textsearch/json")
    Call<PlaceListRes> googleTextSearch(
            @QueryMap Map<String,Object> querys
    );
}

package com.yh.parkingpartner.api;

import com.yh.parkingpartner.model.PostRes;
import com.yh.parkingpartner.model.User;
import com.yh.parkingpartner.model.UserRes;

import java.util.Map;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Query;
import retrofit2.http.QueryMap;

public interface ApiLoginActivity {

    // 로그인 : /users/login
    @POST("/users/login")
    Call<UserRes> login(@Body User user);

    @POST("/users/login")
    Call<UserRes> login(@QueryMap Map<String,String> querys);

    @GET("/users/login")
    Call<UserRes> user_find(@Query("email") String email);
}

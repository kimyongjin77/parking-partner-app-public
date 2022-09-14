package com.yh.parkingpartner.api;

import com.yh.parkingpartner.model.PostRes;
import com.yh.parkingpartner.model.User;
import com.yh.parkingpartner.model.UserRes;

import java.util.Map;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.PartMap;
import retrofit2.http.QueryMap;

public interface ApiRegisterActivity {

    // 회원가입 : /users/register
    //파일은 용량이 크므로 알아서 쪼개서 보내라...
    @Multipart
    @POST("/users/register")
    Call<UserRes> register(
            @Part MultipartBody.Part img_profile,
            @Part("email") RequestBody email,
            @Part("password") RequestBody password,
            @Part("name") RequestBody name
    );

    @Multipart
    @POST("/users/register")
    Call<UserRes> register(
            @Part MultipartBody.Part img_profile,
            @PartMap Map<String,RequestBody> params
    );

    @Multipart
    @PUT("/users/register")
    Call<UserRes> userinfo_update(
            @Part MultipartBody.Part img_profile,
            @PartMap Map<String,RequestBody> params
    );

}

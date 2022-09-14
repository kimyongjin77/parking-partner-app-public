package com.yh.parkingpartner.api;

import com.yh.parkingpartner.model.MypageRes;
import com.yh.parkingpartner.model.ReviewListRes;
import com.yh.parkingpartner.model.UserRes;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface ApiMypageActivity {

    @GET("/mypage")
    Call<MypageRes> getMypage(@Header("Authorization") String token);

    @GET("/review")
    Call<ReviewListRes> getReviewList(@Header("Authorization") String token, @Query("order") String order, @Query("offset") int offset, @Query("limit") int limit);

    @POST("/users/logout")
    Call<UserRes> logout(@Header("Authorization") String token);
}

package com.yh.parkingpartner.api;

import com.yh.parkingpartner.model.PostRes;
import com.yh.parkingpartner.model.Review;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface ApiReviewActivity {

    @POST("/review")
    Call<PostRes> review(@Header("Authorization") String token, @Body Review review);

    @PUT("/review/{review_id}")
    Call<PostRes> updateReview(@Header("Authorization") String token, @Path("review_id") int reviewId, @Body Review review);
}

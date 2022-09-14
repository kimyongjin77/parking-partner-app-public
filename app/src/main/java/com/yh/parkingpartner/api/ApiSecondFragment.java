package com.yh.parkingpartner.api;

import com.yh.parkingpartner.model.Data;
import com.yh.parkingpartner.model.DataListRes;
import com.yh.parkingpartner.model.PostRes;
import com.yh.parkingpartner.model.User;
import com.yh.parkingpartner.model.UserRes;

import java.util.Map;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;
import retrofit2.http.QueryMap;

public interface ApiSecondFragment {

    // 좌표기반 가장 가까운 주차장 가져오기 : /end
//    lat = request.args['lat']
//    log = request.args['log']
    @GET("/end")
    Call<DataListRes> proximateParkingLot(
            @Query("lat") double lat,
            @Query("log") double log
    );

    @GET("/end")
    Call<DataListRes> proximateParkingLot(@QueryMap Map<String,Object> querys);

    // 사진 AWS 텍스트감지 : /upload
    // AWS 레코그니션 detection_text
    //파일은 용량이 크므로 알아서 쪼개서 보내라...
    @Multipart
    @POST("/upload")
    Call<DataListRes> parkingImgUpload(
            @Part MultipartBody.Part img_profile
    );

    // 주차완료저장 : /parkingComplete
    @POST("/parkingComplete")
    Call<DataListRes> parkingComplete(
            @Header("Authorization") String token,
            @Body Data data
    );

    // 주차완료수정 : /parkLct/<int:parking_id>
    @PUT("/parkLct/{parking_id}")
    Call<DataListRes> parkingUpdate(
            @Header("Authorization") String token,
            @Path("parking_id") int prk_id,
            @Body Data data
    );
}

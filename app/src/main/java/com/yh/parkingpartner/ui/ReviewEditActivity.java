package com.yh.parkingpartner.ui;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.yh.parkingpartner.R;
import com.yh.parkingpartner.api.ApiReviewActivity;
import com.yh.parkingpartner.api.NetworkClient;
import com.yh.parkingpartner.config.Config;
import com.yh.parkingpartner.model.PostRes;
import com.yh.parkingpartner.model.Review;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class ReviewEditActivity extends AppCompatActivity {

    String accessToken;
    int reviewId;
    int prkId;
    float rating;
    String content = null;

    Review review;

    TextView txtPrkNm;
    TextView txtEnd;
    RatingBar ratingBar;
    EditText txtContent;
    Button btnSave;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review_edit);

        // 액션바 타이틀 설정
        getSupportActionBar().setTitle("리뷰 저장");
        // 액션바 뒤로가기 버튼
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        review = (Review) getIntent().getSerializableExtra("review");

        SharedPreferences sp = getApplication().getSharedPreferences(Config.SP_NAME, MODE_PRIVATE);
        accessToken = sp.getString(Config.SP_KEY_ACCESS_TOKEN, "");

        reviewId = review.getId();
        prkId = review.getPrk_id();

        txtPrkNm = findViewById(R.id.txtPrkNm);
        txtEnd = findViewById(R.id.txtEnd);
        ratingBar = findViewById(R.id.ratingBar);
        txtContent = findViewById(R.id.txtContent);
        btnSave = findViewById(R.id.btnSave);

        txtPrkNm.setText(review.getPrk_plce_nm());
        txtEnd.setText(review.getEnd_prk().replace("T", " ").substring(0, 16));
        ratingBar.setRating(review.getRating());
        txtContent.setText(review.getContent());


        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                Retrofit retrofit = NetworkClient.getRetrofitClient(ReviewEditActivity.this, Config.PP_BASE_URL);
                ApiReviewActivity api = retrofit.create(ApiReviewActivity.class);

                rating = ratingBar.getRating();

                if (rating == 0) {
                    Toast.makeText(ReviewEditActivity.this, "별점은 최소 1점 이상 입력해주세요.", Toast.LENGTH_LONG).show();
                    return;
                }

                if (!txtContent.getText().toString().isEmpty()) {
                    content = txtContent.getText().toString();
                }

                Review reviewUpdate = new Review(rating, content);
                reviewUpdate.setRating(rating);
                reviewUpdate.setContent(content);

                Call<PostRes> call = api.updateReview("Bearer " + accessToken, reviewId, reviewUpdate);
                call.enqueue(new Callback<PostRes>() {
                    @Override
                    public void onResponse(Call<PostRes> call, Response<PostRes> response) {
                        if (response.isSuccessful()) {

                            Toast.makeText(ReviewEditActivity.this, "리뷰가 저장되었습니다.", Toast.LENGTH_LONG).show();

                            Intent intent = new Intent(ReviewEditActivity.this, MypageActivity.class);
                            startActivity(intent);
                            finish();
                        } else {
                            try{
                                JSONObject errorBody= new JSONObject(response.errorBody().string());
                                Toast.makeText(ReviewEditActivity.this,
//                                        "에러발생\n"+
//                                                "코드 : "+response.code()+"\n" +
                                                "에러 : "+errorBody.getString("error")
                                        , Toast.LENGTH_LONG).show();
                                Log.i("로그", "에러발생 : "+response.code()+", "+errorBody.getString("error"));
                            }catch (IOException | JSONException e){
                                Toast.makeText(ReviewEditActivity.this,
//                                        "에러발생\n"+
//                                                "코드 : "+response.code()+"\n" +
                                                "에러 : "+e.getMessage()
                                        , Toast.LENGTH_LONG).show();
                                e.printStackTrace();
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<PostRes> call, Throwable t) {
                        //통신실패 네트워크 자체 문제로 실패되는 경우
                        Toast.makeText(ReviewEditActivity.this, "시스템에러발생 : "+t.getMessage(), Toast.LENGTH_LONG).show();
                        Log.i("로그", "시스템에러발생 : "+t.getMessage());
                        t.printStackTrace();
                    }
                });
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();

        if(itemId == android.R.id.home) {
            Intent intent = new Intent(ReviewEditActivity.this, MypageActivity.class);
            startActivity(intent);
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        // super.onBackPressed();
        Intent intent = new Intent(ReviewEditActivity.this, MypageActivity.class);
        startActivity(intent);
        finish();
    }
}
package com.yh.parkingpartner.ui;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.model.LazyHeaders;
import com.yh.parkingpartner.R;
import com.yh.parkingpartner.adapter.AdapterMypageList;
import com.yh.parkingpartner.api.ApiMypageActivity;
import com.yh.parkingpartner.api.NetworkClient;
import com.yh.parkingpartner.config.Config;
import com.yh.parkingpartner.model.MypageRes;
import com.yh.parkingpartner.model.Review;
import com.yh.parkingpartner.model.ReviewListRes;
import com.yh.parkingpartner.model.UserRes;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class MypageActivity extends AppCompatActivity {

    String accessToken;
    int prkId;
    RecyclerView recyclerView;
    ProgressBar progressBar;
    AdapterMypageList adapter;
    ArrayList<Review> reviewList = new ArrayList<>();

    TextView txtEmail;
    TextView txtName;
    TextView txtResult;
    ImageView imgProfile;

    Button btnTotal;
    Button btnWrite;
    Button btnUnwritten;

    // 페이징 처리 멤버 변수
    int count = 0;
    String order = "total";
    int offset = 0;
    int limit = 25;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mypage);

        // 액션바 타이틀 설정
        getSupportActionBar().setTitle("마이페이지");
        // 액션바 뒤로가기 버튼
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        txtResult = findViewById(R.id.txtResult);
        txtEmail = findViewById(R.id.txtEmail);
        txtName = findViewById(R.id.txtName);
        imgProfile = findViewById(R.id.imgProfile);
        imgProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(MypageActivity.this, RegisterActivity.class);
                intent.putExtra("update", true);
                startActivity(intent);
            }
        });

        //SharedPreferences 를 이용해서, 앱 내의 저장소에 영구저장된 데이터를 읽어오는 방법
        SharedPreferences sp = getApplication().getSharedPreferences(Config.SP_NAME, MODE_PRIVATE);
        accessToken = sp.getString(Config.SP_KEY_ACCESS_TOKEN, "");
        txtEmail.setText(sp.getString(Config.SP_KEY_EMAIL, ""));
        txtName.setText(sp.getString(Config.SP_KEY_NAME, ""));
        if (!sp.getString(Config.SP_KEY_IMG_PROFILE, "").equals("")) {
            GlideUrl url = new GlideUrl(sp.getString(Config.SP_KEY_IMG_PROFILE, ""), new LazyHeaders.Builder().addHeader("User_Agent", "Android").build());
            Glide.with(MypageActivity.this).load(url).placeholder(R.drawable.ic_baseline_person_24).into(imgProfile);
        }

        btnTotal = (Button) findViewById(R.id.btnTotal);
        btnWrite = (Button) findViewById(R.id.btnWrite);
        btnUnwritten = (Button) findViewById(R.id.btnUnwritten);

        Retrofit retrofit = NetworkClient.getRetrofitClient(MypageActivity.this, Config.PP_BASE_URL);
        ApiMypageActivity api = retrofit.create(ApiMypageActivity.class);

        Call<MypageRes> call = api.getMypage("Bearer " + accessToken);
        call.enqueue(new Callback<MypageRes>() {
            @Override
            public void onResponse(Call<MypageRes> call, Response<MypageRes> response) {
                if(response.isSuccessful()) {
                    MypageRes data = response.body();
                    btnTotal.setText("총 : " + data.getTotal_cnt());
                    btnWrite.setText("작성 : " + data.getWrite_cnt());
                    btnUnwritten.setText("미작성 : " + data.getUnwritten_cnt());
                } else {
                    try{
                        JSONObject errorBody= new JSONObject(response.errorBody().string());
                        Toast.makeText(MypageActivity.this,
//                                "에러발생\n"+
//                                        "코드 : "+response.code()+"\n" +
                                        "에러 : "+errorBody.getString("error")
                                , Toast.LENGTH_LONG).show();
                        Log.i("로그", "에러발생 : "+response.code()+", "+errorBody.getString("error"));
                    }catch (IOException | JSONException e){
                        Toast.makeText(MypageActivity.this,
//                                "에러발생\n"+
//                                        "코드 : "+response.code()+"\n" +
                                        "에러 : "+e.getMessage()
                                , Toast.LENGTH_LONG).show();
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(Call<MypageRes> call, Throwable t) {
                //통신실패 네트워크 자체 문제로 실패되는 경우
                Toast.makeText(MypageActivity.this, "시스템에러발생 : "+t.getMessage(), Toast.LENGTH_LONG).show();
                Log.i("로그", "시스템에러발생 : "+t.getMessage());
                t.printStackTrace();
            }
        });

        btnTotal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                btnTotal.setEnabled(false);
                btnWrite.setEnabled(true);
                btnUnwritten.setEnabled(true);

                order = "total";

                getNetworkData();
            }
        });

        btnWrite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                btnTotal.setEnabled(true);
                btnWrite.setEnabled(false);
                btnUnwritten.setEnabled(true);

                order = "write";

                getNetworkData();
            }
        });

        btnUnwritten.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                btnTotal.setEnabled(true);
                btnWrite.setEnabled(true);
                btnUnwritten.setEnabled(false);

                order = "unwritten";

                getNetworkData();
            }
        });

        progressBar = findViewById(R.id.progressBar);
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(MypageActivity.this));
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                int lastPosition = ((LinearLayoutManager)recyclerView.getLayoutManager()).findLastCompletelyVisibleItemPosition();
                int totalCount = recyclerView.getAdapter().getItemCount();

                if(  lastPosition+1  == totalCount  ){

                    if(count == limit){
                        // 네트워크 통해서, 남아있는 데이터를 추가로 가져오기
                        addNetworkData();
                    }
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        //SharedPreferences 를 이용해서, 앱 내의 저장소에 영구저장된 데이터를 읽어오는 방법
        SharedPreferences sp = getApplication().getSharedPreferences(Config.SP_NAME, MODE_PRIVATE);
        txtName.setText(sp.getString(Config.SP_KEY_NAME, ""));
        if (!sp.getString(Config.SP_KEY_IMG_PROFILE, "").equals("")) {
            GlideUrl url = new GlideUrl(sp.getString(Config.SP_KEY_IMG_PROFILE, ""), new LazyHeaders.Builder().addHeader("User_Agent", "Android").build());
            Glide.with(MypageActivity.this).load(url).placeholder(R.drawable.ic_baseline_person_24).into(imgProfile);
        }
        getNetworkData();
    }

    // 데이터를 처음 가져올때만 실행하는 함수
    // 데이터의 초기화도 필요하다.
    private void getNetworkData() {
        reviewList.clear();
        count = 0;
        offset = 0;
        progressBar.setVisibility(View.VISIBLE);

        Retrofit retrofit = NetworkClient.getRetrofitClient(MypageActivity.this, Config.PP_BASE_URL);
        ApiMypageActivity api = retrofit.create(ApiMypageActivity.class);
        Call<ReviewListRes> call = api.getReviewList("Bearer " + accessToken, order, offset, limit);
        call.enqueue(new Callback<ReviewListRes>() {
            @Override
            public void onResponse(Call<ReviewListRes> call, Response<ReviewListRes> response) {
                if (response.isSuccessful()) {
                    ReviewListRes data = response.body();

                    count = data.getCount();
                    reviewList.addAll(data.getItems());
                    offset = offset + count;

                    progressBar.setVisibility(View.INVISIBLE);
                    adapter = new AdapterMypageList(MypageActivity.this, reviewList);
                    recyclerView.setAdapter(adapter);

                    txtResult.setText("");
                    if (count == 0) {
                        if (order.equals("write")) {
                            txtResult.setText("리뷰를 작성한 주차장 이력이 없습니다.");
                        } else if (order.equals("unwritten")) {
                            txtResult.setText("이용한 모든 주차장 리뷰를 작성하였습니다.");
                        } else {
                            txtResult.setText("주차장 사용 이력이 없습니다.");
                        }
                    }
                } else {
                    try{
                        JSONObject errorBody= new JSONObject(response.errorBody().string());
                        Toast.makeText(MypageActivity.this,
//                                "에러발생\n"+
//                                        "코드 : "+response.code()+"\n" +
                                        "에러 : "+errorBody.getString("error")
                                , Toast.LENGTH_LONG).show();
                        Log.i("로그", "에러발생 : "+response.code()+", "+errorBody.getString("error"));
                    }catch (IOException | JSONException e){
                        Toast.makeText(MypageActivity.this,
//                                "에러발생\n"+
//                                        "코드 : "+response.code()+"\n" +
                                        "에러 : "+e.getMessage()
                                , Toast.LENGTH_LONG).show();
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(Call<ReviewListRes> call, Throwable t) {
                //통신실패 네트워크 자체 문제로 실패되는 경우
                progressBar.setVisibility(View.INVISIBLE);
                Toast.makeText(MypageActivity.this, "시스템에러발생 : "+t.getMessage(), Toast.LENGTH_LONG).show();
                Log.i("로그", "시스템에러발생 : "+t.getMessage());
                t.printStackTrace();
            }
        });
    }

    // 처음이 아니라 더 가져오는 경우
    private void addNetworkData() {
        progressBar.setVisibility(View.VISIBLE);

        Retrofit retrofit = NetworkClient.getRetrofitClient(MypageActivity.this, Config.PP_BASE_URL);
        ApiMypageActivity api = retrofit.create(ApiMypageActivity.class);
        Call<ReviewListRes> call = api.getReviewList("Bearer " + accessToken, order, offset, limit);
        call.enqueue(new Callback<ReviewListRes>() {
            @Override
            public void onResponse(Call<ReviewListRes> call, Response<ReviewListRes> response) {
                if (response.isSuccessful()) {
                    progressBar.setVisibility(View.INVISIBLE);

                    ReviewListRes data = response.body();

                    txtResult.setText("");

                    count = data.getCount();
                    reviewList.addAll(data.getItems());
                    offset = offset + count;
                    adapter.notifyDataSetChanged();
                } else {
                    try{
                        JSONObject errorBody= new JSONObject(response.errorBody().string());
                        Toast.makeText(MypageActivity.this,
//                                "에러발생\n"+
//                                        "코드 : "+response.code()+"\n" +
                                        "에러 : "+errorBody.getString("error")
                                , Toast.LENGTH_LONG).show();
                        Log.i("로그", "에러발생 : "+response.code()+", "+errorBody.getString("error"));
                    }catch (IOException | JSONException e){
                        Toast.makeText(MypageActivity.this,
//                                "에러발생\n"+
//                                        "코드 : "+response.code()+"\n" +
                                        "에러 : "+e.getMessage()
                                , Toast.LENGTH_LONG).show();
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(Call<ReviewListRes> call, Throwable t) {
                //통신실패 네트워크 자체 문제로 실패되는 경우
                progressBar.setVisibility(View.INVISIBLE);
                Toast.makeText(MypageActivity.this, "시스템에러발생 : "+t.getMessage(), Toast.LENGTH_LONG).show();
                Log.i("로그", "시스템에러발생 : "+t.getMessage());
                t.printStackTrace();
            }
        });
    }

    // 상단바 로그아웃 메뉴
    @Override
    public boolean onCreateOptionsMenu(@NonNull Menu menu) {
        getMenuInflater().inflate(R.menu.mypage_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();

        if(itemId == android.R.id.home) {
            finish();
        } else if (itemId == R.id.menuLogout) {
            AlertDialog.Builder alert = new AlertDialog.Builder(MypageActivity.this);
            alert.setTitle("로그아웃");
            alert.setMessage("로그아웃 하시겠습니까?");
            alert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    SharedPreferences sp = getApplication().getSharedPreferences(Config.SP_NAME, MODE_PRIVATE);
                    prkId = sp.getInt(Config.SP_KEY_PRK_ID, 0);

                    Retrofit retrofit = NetworkClient.getRetrofitClient(MypageActivity.this, Config.PP_BASE_URL);
                    ApiMypageActivity api = retrofit.create(ApiMypageActivity.class);

                    Call<UserRes> call = api.logout("Bearer " + accessToken);

                    call.enqueue(new Callback<UserRes>() {
                        @Override
                        public void onResponse(Call<UserRes> call, Response<UserRes> response) {
                            if (response.isSuccessful()) {
                                SharedPreferences sp = getApplication().getSharedPreferences(Config.SP_NAME, MODE_PRIVATE);
                                SharedPreferences.Editor editor = sp.edit();
                                editor.putString("accessToken", "");
                                editor.putInt("prk_id", 0);
                                editor.apply();

                                // 로그아웃 시, 메인 액티비티 종료
                                MainActivity mainActivity = (MainActivity) MainActivity.activity;
                                mainActivity.finish();

                                // 로그아웃 시, 현재 액티비티 종료 후, 로그인 페이지로 이동
                                Intent intent = new Intent(MypageActivity.this, LoginActivity.class);
                                startActivity(intent);
                                finish();
                            } else {
                                try{
                                    JSONObject errorBody= new JSONObject(response.errorBody().string());
                                    Toast.makeText(MypageActivity.this,
//                                            "에러발생\n"+
//                                                    "코드 : "+response.code()+"\n" +
                                                    "에러 : "+errorBody.getString("error")
                                            , Toast.LENGTH_LONG).show();
                                    Log.i("로그", "에러발생 : "+response.code()+", "+errorBody.getString("error"));
                                }catch (IOException | JSONException e){
                                    Toast.makeText(MypageActivity.this,
//                                            "에러발생\n"+
//                                                    "코드 : "+response.code()+"\n" +
                                                    "에러 : "+e.getMessage()
                                            , Toast.LENGTH_LONG).show();
                                    e.printStackTrace();
                                }
                            }
                        }

                        @Override
                        public void onFailure(Call<UserRes> call, Throwable t) {
                            //통신실패 네트워크 자체 문제로 실패되는 경우
                            Toast.makeText(MypageActivity.this, "시스템에러발생 : "+t.getMessage(), Toast.LENGTH_LONG).show();
                            Log.i("로그", "시스템에러발생 : "+t.getMessage());
                            t.printStackTrace();
                        }
                    });
                }

            });
            alert.setNegativeButton("No", null);
            alert.show();
        }

        return super.onOptionsItemSelected(item);
    }

}
package com.yh.parkingpartner.ui;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.yh.parkingpartner.R;
import com.yh.parkingpartner.adapter.AdapterParkList;
import com.yh.parkingpartner.adapter.AdapterPlaceSearchList;
import com.yh.parkingpartner.api.ApiParkingActivity;
import com.yh.parkingpartner.api.NetworkClient;
import com.yh.parkingpartner.config.Config;
import com.yh.parkingpartner.model.Data;
import com.yh.parkingpartner.model.DataListRes;
import com.yh.parkingpartner.model.Place;
import com.yh.parkingpartner.model.PlaceListRes;
import com.yh.parkingpartner.model.ReviewListRes;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class ParkListActivity extends AppCompatActivity {

    String accessToken;

    Button btnDistance;
    Button btnCharge;
    Button btnSpot;

    RecyclerView recyclerView;

    ProgressBar progressBar;

    AdapterParkList adapter;

    ArrayList<Data> dataList = new ArrayList<>();


    // 페이징에 필요한 멤버변수
    int count = 0;
    String order = "distance";
    int offset = 0;
    int limit = 30;

    double latitude;
    double longitude;
    String title;


    //네트워크 처리 보여주는 프로그래스 다이얼로그
    ProgressDialog progressDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parklist);

        title = getIntent().getStringExtra("title");
        latitude = getIntent().getDoubleExtra("latitude",0);
        longitude = getIntent().getDoubleExtra("longitude",0);


        //액티비티 액션바 타이틀
        getSupportActionBar().setTitle(title);
        //액티비티 액션바 백버튼 셋팅
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        progressBar = findViewById(R.id.progressBar);
        progressBar.setVisibility(ProgressBar.INVISIBLE);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);

        //SharedPreferences 를 이용해서, 앱 내의 저장소에 영구저장된 데이터를 읽어오는 방법
        SharedPreferences sp = getApplication().getSharedPreferences(Config.SP_NAME, MODE_PRIVATE);
        accessToken = sp.getString(Config.SP_KEY_ACCESS_TOKEN, "");


        btnDistance = (Button) findViewById(R.id.btnDistance);
        btnCharge = (Button) findViewById(R.id.btnCharge);
        btnSpot = (Button) findViewById(R.id.btnSpot);


        btnDistance.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                btnDistance.setEnabled(false);
                btnCharge.setEnabled(true);
                btnSpot.setEnabled(true);

                order = "distance";

                getNetworkData();
            }
        });

        btnCharge.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                btnDistance.setEnabled(true);
                btnCharge.setEnabled(false);
                btnSpot.setEnabled(true);

                order = "charge";

                getNetworkData();
            }
        });

        btnSpot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                btnDistance.setEnabled(true);
                btnCharge.setEnabled(true);
                btnSpot.setEnabled(false);

                order = "available";

                getNetworkData();
            }
        });


        recyclerView.setLayoutManager(new LinearLayoutManager(ParkListActivity.this));

        //리스트를 맨 밑에까지 가면 알수 있는 방법,, 스크롤 리스너 이벤트
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                int lastPosition = ((LinearLayoutManager) recyclerView.getLayoutManager()).findLastCompletelyVisibleItemPosition();
                int totalCount = recyclerView.getAdapter().getItemCount();

                if (lastPosition + 1 == totalCount) {

                    if (count == limit) {
                        // 네트워크 통해서, 데이터를 더 불러오면 된다.
                        addNetworkData();
                    }
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        getNetworkData();
    }

    // 데이터를 처음 가져올때만 실행하는 함수
    // 데이터의 초기화도 필요하다.

    private void getNetworkData() {
        dataList.clear();
        count = 0;
        offset = 0;
        progressBar.setVisibility(View.VISIBLE);




        Retrofit retrofit = NetworkClient.getRetrofitClient(ParkListActivity.this, Config.PP_BASE_URL);
        ApiParkingActivity api = retrofit.create(ApiParkingActivity.class);


        Call<DataListRes> call = api.getParkingList(latitude,longitude,order,offset,limit);
        call.enqueue(new Callback<DataListRes>() {
            @Override
            public void onResponse(Call<DataListRes> call, Response<DataListRes> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful()) {
                    DataListRes data  = response.body();
                    dataList.addAll(data.getItems());

                    count = data.getCount();

                    offset = offset + count;

                    adapter = new AdapterParkList(ParkListActivity.this, dataList);

                    recyclerView.setAdapter(adapter);

                    if(count == 0) {
                        Toast.makeText(ParkListActivity.this, "주변에 주차장이 없습니다.", Toast.LENGTH_SHORT).show();
                    }

                }
            }
            ////////

            @Override
            public void onFailure(Call<DataListRes> call, Throwable t) {
                progressBar.setVisibility(View.GONE);

            }
        });

    }



    @Override
    public boolean onSupportNavigateUp() {
        //백버튼 눌렀을때 호출되는 콜백함수를 이용
//        onBackPressed();
        //전달할 데이터를 담는다.
        setResult(Activity.RESULT_CANCELED, new Intent());
        //액티비티는 종료한다.
        finish( );

        return  true;
    }

    // 처음이 아니라 더 가져오는 경우
    void addNetworkData() {

        progressBar.setVisibility(View.VISIBLE);

        Retrofit retrofit = NetworkClient.getRetrofitClient(ParkListActivity.this, Config.PP_BASE_URL);
        ApiParkingActivity api = retrofit.create(ApiParkingActivity.class);

        SharedPreferences sp = getApplication().getSharedPreferences(Config.SP_NAME, MODE_PRIVATE);
        String accessToken = sp.getString("accessToken", "");



        Call<DataListRes> call = api.getParkingList(latitude,longitude,order, offset, limit);
        call.enqueue(new Callback<DataListRes>() {
            @Override
            public void onResponse(Call<DataListRes> call, Response<DataListRes> response) {

                DataListRes data = response.body();

                if (response.isSuccessful()) {

                    count = response.body().getCount();
                    dataList.addAll(data.getItems());
                    offset = offset + count;
                    progressBar.setVisibility(View.INVISIBLE);
                    adapter.notifyDataSetChanged();

                }



            }

            @Override
            public void onFailure(Call<DataListRes> call, Throwable t) {
                dismissProgress();
            }

        });
    }

    void  setDataToRecyclerView(boolean onScrolled){
        progressBar.setVisibility(ProgressBar.VISIBLE);
        //리사이클러뷰 어댑터 데이터 연결
        if(onScrolled){
            if(adapter != null) {
                adapter.notifyDataSetChanged();
            }
        } else {
            adapter = new AdapterParkList(this,dataList);
            recyclerView.setAdapter(adapter);
        }

        progressBar.setVisibility(ProgressBar.GONE);
    }

    //프로그래스다이얼로그 표시
    void showProgress(String msg){
        progressDialog=new ProgressDialog(this);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setMessage(msg);
        progressDialog.show();
    }

    void dismissProgress() {
        progressDialog.dismiss();
    }
}
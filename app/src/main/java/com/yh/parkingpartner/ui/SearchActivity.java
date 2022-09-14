package com.yh.parkingpartner.ui;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.yh.parkingpartner.R;
import com.yh.parkingpartner.adapter.AdapterPlaceSearchList;
import com.yh.parkingpartner.api.ApiSearchActivity;
import com.yh.parkingpartner.api.NetworkClient;
import com.yh.parkingpartner.config.Config;
import com.yh.parkingpartner.model.Place;
import com.yh.parkingpartner.model.PlaceListRes;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class SearchActivity extends AppCompatActivity {

    EditText etxtSearch;
    ImageView imgSearchRun;
    ImageView imgSearchDel;
    RecyclerView recyclerView;
    ProgressBar progressBar;

    AdapterPlaceSearchList adapter;
    ArrayList<Place> dataList=new ArrayList<>();

    //네트워크 처리 보여주는 프로그래스 다이얼로그
    ProgressDialog progressDialog;

    String next_page_token="";
    String pagetoken="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        //액티비티 액션바 타이틀
        this.getSupportActionBar().setTitle("목적지 검색");
        //액티비티 액션바 백버튼 셋팅
        this.getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        progressBar=findViewById(R.id.progressBar);
        progressBar.setVisibility(ProgressBar.INVISIBLE);

        etxtSearch=findViewById(R.id.etxtSearch);
        imgSearchRun=findViewById(R.id.imgSearchRun);
        imgSearchDel=findViewById(R.id.imgSearchDel);
        recyclerView=findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        //리스트를 맨 밑에까지 가면 알수 있는 방법,, 스크롤 리스너 이벤트
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                //주의할 점:: 리시클러뷰의 하나가 한 화면에 들어오지 않으면
                //포지션을 찾지 못한다.
                int lastPosition=((LinearLayoutManager)recyclerView.getLayoutManager()).findLastCompletelyVisibleItemPosition();
                int totalCount=recyclerView.getAdapter().getItemCount();

                Log.i("로그", "onScrolled "+ lastPosition+", "+totalCount);

                //스크롤을 맨 끝까지 한 것~~
                if(!next_page_token.isEmpty() && totalCount > 0 && lastPosition > -1) {
                    if (lastPosition+1 == totalCount) {
                        Log.i("로그", "onScrolled 마지막까지 스크롤 됨");
                        getNetworkData(true);
                    }
                }
            }
        });

        imgSearchRun.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String keyWord=etxtSearch.getText().toString().trim();
                if(keyWord.isEmpty()){
                    Toast.makeText(getApplicationContext(), "검색어를 입력하세요.", Toast.LENGTH_LONG).show();
                    return;
                }
                getNetworkData(false);
            }
        });

        imgSearchDel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                etxtSearch.setText("");
                pagetoken="";
                next_page_token="";
                dataList.clear();
                setDataToRecyclerView(true);
                etxtSearch.requestFocus();
                InputMethodManager imm = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
                imm.showSoftInput(etxtSearch,0);
            }
        });

        etxtSearch.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                boolean handled=false;
                if (i == EditorInfo.IME_ACTION_SEARCH) {
                    String keyWord=textView.getText().toString().trim();
                    if(keyWord.isEmpty()){
                        Toast.makeText(getApplicationContext(), "검색어를 입력하세요.", Toast.LENGTH_LONG).show();
                        handled=true;
                    }else{
                        getNetworkData(false);
                        handled=false;
                    }
                }
                return handled;
            }
        });

        etxtSearch.setFocusableInTouchMode(true);
        etxtSearch.requestFocus();
        InputMethodManager imm = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
        imm.showSoftInput(etxtSearch,0);
    }

    //액티비티 액션바 백버튼 클릭 함수 오버라이딩 코딩
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

    private void getNetworkData(boolean onScrolled) {

        String keyWord=etxtSearch.getText().toString().trim();
        if(keyWord.isEmpty()){
            return;
        }

        if(onScrolled) {
            pagetoken=next_page_token;
        } else {
            next_page_token="";
            pagetoken="";
            dataList.clear();
        }

        //네트워크데이터를 보내고 있다는 프로그래스 다이얼로그를 먼저 띄운다..
        showProgress("검색 목록 가져오는 중...");

        //구글텍스트검색목록 가져오는 api 호출
        Retrofit retrofit= NetworkClient.getRetrofitClient(SearchActivity.this, Config.GG_BASE_URL);
        ApiSearchActivity api=retrofit.create(ApiSearchActivity.class);

//        Map<String, Object> params=new HashMap<>();
//        params.put("input", keyWord);
//        params.put("inputtype", "textquery");
//        params.put("fields", "formatted_address,name,geometry");
//        params.put("language", "ko");
//        params.put("key", Config.GG_API_KEY);
//        params.put("pagetoken", next_page_token);
//
//        Call<PlaceListRes> call = api.googleFindPlace(params);

        Map<String, Object> params=new HashMap<>();
        params.put("query", keyWord);
        params.put("language", "ko");
        params.put("key", Config.GG_API_KEY);
        params.put("pagetoken", next_page_token);

        Call<PlaceListRes> call = api.googleTextSearch(params);

        call.enqueue(new Callback<PlaceListRes>() {
            @Override
            public void onResponse(Call<PlaceListRes> call, Response<PlaceListRes> response) {
                Log.i("로그", response.toString());
                dismissProgress();
                //http상태코드 확인
                if(response.isSuccessful()) {
                    PlaceListRes placeListRes= response.body();
                    if (placeListRes.getStatus().equals("OK")) {
                        next_page_token=placeListRes.getNext_page_token();
                        dataList.addAll(placeListRes.getResults());
                        setDataToRecyclerView(onScrolled);
                    }
                }  else {
                    Toast.makeText(getApplicationContext(), "에러발생 : "+response.code(), Toast.LENGTH_LONG).show();
                    Log.i("로그", response.code()+"");
                    Log.i("로그", response.toString());
                }
            }

            @Override
            public void onFailure(Call<PlaceListRes> call, Throwable t) {
                //네트워크 자체 문제로 실패되는 경우
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
            adapter = new AdapterPlaceSearchList(this, dataList);
            recyclerView.setAdapter(adapter);
        }

        Log.i("로그","SearchActivity setDataToRecyclerView");
        progressBar.setVisibility(ProgressBar.GONE);
    }

    //프로그래스다이얼로그 표시
    void showProgress(String msg){
        progressDialog=new ProgressDialog(this);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setMessage(msg);
        progressDialog.show();
    }
    //프로그래스다이얼로그 숨기기
    void dismissProgress(){
        progressDialog.dismiss();
    }
}
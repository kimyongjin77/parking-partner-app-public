package com.yh.parkingpartner.ui;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.model.LazyHeaders;
import com.yh.parkingpartner.R;
import com.yh.parkingpartner.api.ApiThirdFragment;
import com.yh.parkingpartner.api.NetworkClient;
import com.yh.parkingpartner.config.Config;
import com.yh.parkingpartner.model.Data;
import com.yh.parkingpartner.model.DataListRes;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ThirdFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ThirdFragment extends Fragment {


    Fragment secondFragment;
    MainActivity mainActivity;
    ProgressDialog progressDialog;
    boolean blnCreatedView;
    ArrayList<Data> dataListRes = new ArrayList<>();
    Data data=new Data();
    ImageView Lct_prk_img;
    TextView Lct_prk_plce_nm;
    TextView Lct_prk_plce_nm2;
    TextView Lct_prk_plce_adres;
    TextView Lct_prk_plce_adres2;
    TextView Lct_start_prk_at;
    TextView Lct_start_prk_at2;
    TextView Lct_prk_area;
    TextView Lct_prk_area2;
    String accessToken;
    int prkId;
    String img_prk;
    private ProgressDialog dialog;
    int count = 0;
    String order = "total";
    int offset = 0;


    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public ThirdFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment SecondFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static SecondFragment newInstance(String param1, String param2) {
        SecondFragment fragment = new SecondFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

        mainActivity = (MainActivity) getActivity();
        mainActivity.getSupportActionBar().setTitle("주차 위치");
        ReadSharedPreferences();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_third, container, false);


        secondFragment = new SecondFragment();
        Lct_prk_img = rootView.findViewById(R.id.Lct_prk_img);
        Lct_prk_area = rootView.findViewById(R.id.Lct_prk_area);
        Lct_prk_area2 = rootView.findViewById(R.id.Lct_prk_area2);
        Lct_start_prk_at = rootView.findViewById(R.id.Lct_start_prk_at);
        Lct_start_prk_at2 = rootView.findViewById(R.id.Lct_start_prk_at2);
        Lct_prk_plce_adres = rootView.findViewById(R.id.Lct_prk_plce_adres);
        Lct_prk_plce_adres2 = rootView.findViewById(R.id.Lct_prk_plce_adres2);
        Lct_prk_plce_nm = rootView.findViewById(R.id.Lct_prk_plce_nm);
        Lct_prk_plce_nm2 = rootView.findViewById(R.id.Lct_prk_plce_nm2);


        // Inflate the layout for this fragment
        return rootView;
        }

        void ReadSharedPreferences(){
        //SharedPref소erences 를 이용해서, 앱 내의 저장에 영구저장된 데이터를 읽어오는 방법
        SharedPreferences sp = getActivity().getSharedPreferences(Config.SP_NAME, getActivity().MODE_PRIVATE);
        accessToken = sp.getString(Config.SP_KEY_ACCESS_TOKEN, "");
        prkId = sp.getInt(Config.SP_KEY_PRK_ID,0);
        Log.i("로그", "accessToken : " + accessToken);
        Log.i("로그", "prkId : " + prkId);

    }


    @Override
    public void onResume() {
        super.onResume();
        getNetworkData();
    }


    private void getNetworkData() {

        // 저장된 prkId(SP_KEY_PRK_ID) 가 없을 경우
        if (prkId == 0) {
            AlertDialog.Builder alert = new AlertDialog.Builder(getContext());
            alert.setTitle("주차 위치 정보 없음");
            alert.setMessage("주차 완료 후, 사용해주세요.");
            alert.setPositiveButton("Ok", null);
            alert.show();
            return;
        }
        showProgress("주차위치를 가져오는 중입니다.");

        dataListRes.clear();
        count = 1;

        Retrofit retrofit = NetworkClient.getRetrofitClient(getContext(), Config.PP_BASE_URL);
        ApiThirdFragment api = retrofit.create(ApiThirdFragment.class);
        Call<DataListRes> call = api.getDataList("Bearer " + accessToken, prkId);
        call.enqueue(new Callback<DataListRes>() {
            @Override
            public void onResponse(Call<DataListRes> call, Response<DataListRes> response) {
                if (response.isSuccessful()) {
                    dismissProgress();
                    Log.i("로그", "결과 : "+response.isSuccessful());


                    dataListRes = response.body().getItems();
                    Lct_prk_area2.setText(dataListRes.get(0).getPrk_area());
                    Lct_prk_plce_adres2.setText(dataListRes.get(0).getPrk_plce_adres());
                    Lct_prk_plce_nm2.setText(dataListRes.get(0).getPrk_plce_nm());
                    Lct_start_prk_at2.setText(dataListRes.get(0).getStart_prk_at().replace("T", " ").substring(0, 16));
                    Lct_prk_img.setScaleType(ImageView.ScaleType.CENTER_CROP);

                    //클라이드 라이브러리 사용
                    Lct_prk_img.setScaleType(ImageView.ScaleType.CENTER_CROP);
                    GlideUrl url=new GlideUrl(dataListRes.get(0).getImg_prk(), new LazyHeaders.Builder().addHeader("User-Agent", "Android").build());
                    Glide.with(getActivity()).load(url).into(Lct_prk_img);
                } else {
                    try{
                        JSONObject errorBody= new JSONObject(response.errorBody().string());
                        Toast.makeText(getActivity(),
//                                "에러발생\n"+
//                                        "코드 : "+response.code()+"\n" +
                                        "에러 : "+errorBody.getString("error")
                                , Toast.LENGTH_LONG).show();
                        Log.i("로그", "에러발생 : "+response.code()+", "+errorBody.getString("error"));
                    }catch (IOException | JSONException e){
                        Toast.makeText(getActivity(),
//                                "에러발생\n"+
//                                        "코드 : "+response.code()+"\n" +
                                        "에러 : "+e.getMessage()
                                , Toast.LENGTH_LONG).show();
                        e.printStackTrace();
                    }
                }



//                    DataListRes data = response.body();
//                    count = data.getCount();
//                    dataListRes.addAll(data.getItems());
            }


            @Override
            public void onFailure(Call<DataListRes> call, Throwable t) {

            }
        });
    }

    //프로그래스다이얼로그 표시
    void showProgress(String msg){
        progressDialog=new ProgressDialog(getContext());
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setMessage(msg);
        progressDialog.show();
    }
    //프로그래스다이얼로그 숨기기
    void dismissProgress(){
        progressDialog.dismiss();
    }

}
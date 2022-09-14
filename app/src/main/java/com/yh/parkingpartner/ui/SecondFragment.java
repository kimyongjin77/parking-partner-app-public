package com.yh.parkingpartner.ui;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.model.LazyHeaders;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.libraries.places.api.model.Place;
import com.yh.parkingpartner.R;
import com.yh.parkingpartner.api.ApiSecondFragment;
import com.yh.parkingpartner.api.NetworkClient;
import com.yh.parkingpartner.config.Config;
import com.yh.parkingpartner.model.Data;
import com.yh.parkingpartner.model.DataListRes;
import com.yh.parkingpartner.util.Util;

import org.apache.commons.io.IOUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class SecondFragment extends Fragment {

    MainActivity mainActivity;

    LocationManager locationManager;
    LocationListener locationListener;

    double nowLatitude;
    double nowLongitude;

    double orgLatitude;
    double orgLongitude;

    //네트워크 처리 보여주는 프로그래스 다이얼로그
    ProgressDialog progressDialog;

    boolean blnSearchParkingLot=true;
    boolean blnOnCreateView=false;
    boolean blnCamera=false;
    // 주차완료정보 관련
    Data data=new Data();

    LinearLayout linerLayoutMyLoc;
    ImageView imgMyLoc;
    TextView txtName;
    TextView txtAddr;
    ImageView imgParking;
    EditText etxtArea;
    Button btnSave;

    String accessToken;
    String name;
    String email;
    String img_profile;

    //사진관련된 변수들
    private File photoFile;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //SharedPreferences 를 읽어온다.
        readSharedPreferences();

        mainActivity = (MainActivity) getActivity();
        mainActivity.getSupportActionBar().setTitle("주차 완료");

        locationManager = (LocationManager) getActivity().getSystemService(getContext().LOCATION_SERVICE);
        //gps 로케이션 위치 받아오는 리스너
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(@NonNull Location location) {
//                Log.i("로그", "위도 : " + location.getLatitude() + ", 경도 : " + location.getLongitude());
                nowLatitude=location.getLatitude();
                nowLongitude=location.getLongitude();

                if(nowLatitude<=0){
                    nowLatitude=0;
                }

                if(nowLongitude<=0){
                    nowLongitude=0;
                }

                if(blnSearchParkingLot==false && nowLatitude>0 && nowLongitude>0){
                    blnSearchParkingLot=true;
                    dismissProgress();
                    getNetworkData(1);
                }
            }
        };

        //권한 없으면... 사용자에게 권한을 부여받아라..
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.

            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    100);
            return;
        }

        //3초간격 and 3미터이동(-1이면 사용하지 않음) 마다 위치정보 줘라..
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, -1, locationListener);
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, -1, locationListener);

    }

    //권한부여 결과처리
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(requestCode==100){
            if(ActivityCompat.checkSelfPermission(getContext(),
                    Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(getContext(),
                            Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){

                ActivityCompat.requestPermissions(getActivity(),
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                        100);

                return;
            } else {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, -1, locationListener);
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, -1, locationListener);
            }
        } else if(requestCode==1000) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(getContext(), "권한 허가 되었음", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), "아직 승인하지 않았음", Toast.LENGTH_SHORT).show();
            }
            return;
        } else if(requestCode==500) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(getContext(), "권한 허가 되었음", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), "아직 승인하지 않았음", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_second, container, false);

        blnOnCreateView=true;

        linerLayoutMyLoc = rootView.findViewById(R.id.linerLayoutMyLoc);
        imgMyLoc = rootView.findViewById(R.id.imgMyLoc);
        txtName = rootView.findViewById(R.id.txtName);
        txtAddr = rootView.findViewById(R.id.txtAddr);
        imgParking = rootView.findViewById(R.id.imgParking);
        etxtArea = rootView.findViewById(R.id.etxtArea);
        btnSave = rootView.findViewById(R.id.btnSave);

        imgParking.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(data.getPrk_center_id().isEmpty()){
                    Toast.makeText(getContext(), "현재 입차한 주차장정보를 수신하지 못했습니다.", Toast.LENGTH_SHORT).show();
                    return;
                }
                //카메라로 사진을 찍을 것인지, 앨범에서 사진을 가져올 것인지 선택할 수 있게 알러트 다이얼로그를 띄운다.
                showImageChoiceMethod();
            }
        });

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(data.getPrk_center_id().isEmpty()){
                    Toast.makeText(getContext(), "현재 입차한 주차장정보를 수신하지 못했습니다.", Toast.LENGTH_SHORT).show();
                    return;
                }

                if(data.getImg_prk().isEmpty()){
                    Toast.makeText(getContext(), "주차 사진이 없습니다.", Toast.LENGTH_SHORT).show();
                    return;
                }

                getNetworkData(3);
            }
        });

        imgMyLoc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                readSharedPreferences();
                displayCreateViewParkingLot();
                blnSearchParkingLot=false;
                showProgress("현재 GPS좌표를 수신 중입니다...");
            }
        });

        blnSearchParkingLot=(data.getPrk_id()==0 ? false : true);
        if(!blnSearchParkingLot){
            linerLayoutMyLoc.setVisibility(View.VISIBLE);
            showProgress("현재 GPS좌표를 수신 중입니다...");
        }else{
            linerLayoutMyLoc.setVisibility(View.GONE);
        }
        // Inflate the layout for this fragment
        return rootView;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i("로그", "SecondFragment.onDestroy locationManager 리슨너 해제");
        locationManager.removeUpdates(locationListener);
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        if(blnOnCreateView){
            blnOnCreateView=false;
            displayCreateViewParkingLot();
            return;
        }

        if(blnCamera){
            blnCamera=false;
            return;
        }else{
            //SharedPreferences 를 읽어온다.
            readSharedPreferences();
            displayCreateViewParkingLot();

            blnSearchParkingLot=(data.getPrk_id()==0 ? false : true);
            if(!blnSearchParkingLot){
                showProgress("현재 GPS좌표를 수신 중입니다...");
            }
            return;
        }
    }

    void readSharedPreferences(){
        //SharedPreferences 를 이용해서, 앱 내의 저장소에 영구저장된 데이터를 읽어오는 방법
        SharedPreferences sp = getActivity().getSharedPreferences(Config.SP_NAME, getActivity().MODE_PRIVATE);
        accessToken = sp.getString(Config.SP_KEY_ACCESS_TOKEN, "");
//        Log.i("로그", "accessToken : " + accessToken);
        name = sp.getString(Config.SP_KEY_NAME, "");
//        Log.i("로그", "name : " + name);
        email = sp.getString(Config.SP_KEY_EMAIL, "");
//        Log.i("로그", "email : " + email);
        img_profile = sp.getString(Config.SP_KEY_IMG_PROFILE, "");
//        Log.i("로그", "img_profile : " + img_profile);

        // 주차완료정보 관련
        // prk_id-주차ID
        data.setPrk_id(sp.getInt(Config.SP_KEY_PRK_ID, 0));
        // push_prk_id-최종 주차ID
        data.setPush_prk_id(sp.getInt(Config.SP_KEY_PUSH_PRK_ID,0));

        if(data.getPrk_id()!=0) {
            //prk_center_id-주차장ID
            data.setPrk_center_id(sp.getString(Config.SP_KEY_PRK_CENTER_ID, ""));
            //prk_plce_nm-주차장명
            data.setPrk_plce_nm(sp.getString(Config.SP_KEY_PRK_PLCE_NM, ""));
            //prk_plce_adres-주차장주소
            data.setPrk_plce_adres(sp.getString(Config.SP_KEY_PRK_PLCE_ADRES, ""));
            // start_prk_at-입차시간
            data.setStart_prk_at(sp.getString(Config.SP_KEY_START_PRK_AT, ""));
            // Img_prk-주차사진URL
            data.setImg_prk(sp.getString(Config.SP_KEY_IMG_PAK, ""));
            // prk_area-주차구역
            data.setPrk_area(sp.getString(Config.SP_KEY_PRK_AREA, ""));
            // parking_chrge_bs_time-기본시간
            data.setParking_chrge_bs_time(sp.getInt(Config.SP_KEY_PARKING_CHRGE_BS_TIME, 0));
            // parking_chrge_bs_chrg-기본요금
            data.setParking_chrge_bs_chrg(sp.getInt(Config.SP_KEY_PARKING_CHRGE_BS_CHRG, 0));
            // parking_chrge_adit_unit_time-추가단위시간
            data.setParking_chrge_adit_unit_time(sp.getInt(Config.SP_KEY_PARKING_CHRGE_ADIT_UNIT_TIME, 0));
            // parking_chrge_adit_unit_chrge-추가단위요금
            data.setParking_chrge_adit_unit_chrge(sp.getInt(Config.SP_KEY_PARKING_CHRGE_ADIT_UNIT_CHRGE, 0));
            // parking_chrge_one_day_chrge-1일요금
            data.setParking_chrge_one_day_chrge(sp.getInt(Config.SP_KEY_PARKING_CHRGE_ONE_DAY_CHRGE, 0));
        }else{
            //prk_center_id-주차장ID
            data.setPrk_center_id("");
            //prk_plce_nm-주차장명
            data.setPrk_plce_nm("");
            //prk_plce_adres-주차장주소
            data.setPrk_plce_adres("");
            // start_prk_at-입차시간
            data.setStart_prk_at("");
            // Img_prk-주차사진URL
            data.setImg_prk("");
            // prk_area-주차구역
            data.setPrk_area("");
            // parking_chrge_bs_time-기본시간
            data.setParking_chrge_bs_time(0);
            // parking_chrge_bs_chrg-기본요금
            data.setParking_chrge_bs_chrg(0);
            // parking_chrge_adit_unit_time-추가단위시간
            data.setParking_chrge_adit_unit_time(0);
            // parking_chrge_adit_unit_chrge-추가단위요금
            data.setParking_chrge_adit_unit_chrge(0);
            // parking_chrge_one_day_chrge-1일요금
            data.setParking_chrge_one_day_chrge(0);
        }
    }

    void writeSharedPreferences(){
        //SharedPreferences 를 이용해서, 앱 내의 저장소에 영구저장된 데이터를 읽어오는 방법
        SharedPreferences sp = getActivity().getSharedPreferences(Config.SP_NAME, getActivity().MODE_PRIVATE);
        //편집기를 만든다.
        SharedPreferences.Editor editor = sp.edit();
        //작성한다.
        // 주차완료정보 관련
        // prk_id-주차ID
        editor.putInt(Config.SP_KEY_PRK_ID, data.getPrk_id());
        // push_prk_id-최종 주차ID
        editor.putInt(Config.SP_KEY_PUSH_PRK_ID,data.getPush_prk_id());
        //prk_center_id-주차장ID
        editor.putString(Config.SP_KEY_PRK_CENTER_ID, data.getPrk_center_id());
        //prk_plce_nm-주차장명
        editor.putString(Config.SP_KEY_PRK_PLCE_NM, data.getPrk_plce_nm());
        //prk_plce_adres-주차장주소
        editor.putString(Config.SP_KEY_PRK_PLCE_ADRES, data.getPrk_plce_adres());
        // start_prk_at-입차시간
        editor.putString(Config.SP_KEY_START_PRK_AT, data.getStart_prk_at());
        // Img_prk-주차사진URL
        editor.putString(Config.SP_KEY_IMG_PAK, data.getImg_prk());
        // prk_area-주차구역
        editor.putString(Config.SP_KEY_PRK_AREA, data.getPrk_area());
        // parking_chrge_bs_time-기본시간
        editor.putInt(Config.SP_KEY_PARKING_CHRGE_BS_TIME, data.getParking_chrge_bs_time());
        // parking_chrge_bs_chrg-기본요금
        editor.putInt(Config.SP_KEY_PARKING_CHRGE_BS_CHRG, data.getParking_chrge_bs_chrg());
        // parking_chrge_adit_unit_time-추가단위시간
        editor.putInt(Config.SP_KEY_PARKING_CHRGE_ADIT_UNIT_TIME, data.getParking_chrge_adit_unit_time());
        // parking_chrge_adit_unit_chrge-추가단위요금
        editor.putInt(Config.SP_KEY_PARKING_CHRGE_ADIT_UNIT_CHRGE, data.getParking_chrge_adit_unit_chrge());
        // parking_chrge_one_day_chrge-1일요금
        editor.putInt(Config.SP_KEY_PARKING_CHRGE_ONE_DAY_CHRGE, data.getParking_chrge_one_day_chrge());

        //저장한다.
        editor.apply();

    }

    private void getNetworkData(int pApiGbn) {
        if(pApiGbn==1) {
            // 주차장 찾기
            orgLatitude = nowLatitude;
            orgLongitude = nowLongitude;
            if (orgLatitude == 0 || orgLongitude == 0) {
                return;
            }
            //네트워크데이터를 보내고 있다는 프로그래스 다이얼로그를 먼저 띄운다..
            showProgress("가까운 주차장을 수신 중입니다...");
        } else if(pApiGbn==2){
            // 선택사진 AWS 텍스트감지
            if(photoFile!=null) {
                //네트워크데이터를 보내고 있다는 프로그래스 다이얼로그를 먼저 띄운다..
                showProgress("주차사진을 저장 중입니다...");
            } else {
                return;
            }
        } else if(pApiGbn==3){
            // 저장
            if(data.getPrk_center_id().isEmpty()){
                Toast.makeText(getContext(), "현재 입차한 주차장정보를 수신하지 못했습니다.", Toast.LENGTH_SHORT).show();
                return;
            }

            if(data.getImg_prk().isEmpty()){
                Toast.makeText(getContext(), "주차 사진이 없습니다.", Toast.LENGTH_SHORT).show();
                return;
            }
            //주차구역 입력값 셋팅
            data.setPrk_area(etxtArea.getText().toString());

            showProgress("주차완료를 저장 중입니다...");
        }

        //api 호출
        Retrofit retrofit= NetworkClient.getRetrofitClient(getContext(), Config.PP_BASE_URL);
        ApiSecondFragment api=retrofit.create(ApiSecondFragment.class);

        //?lat = request.args['lat']&log = request.args['log']
        Map<String, Object> params=new HashMap<>();
        //db에 위도, 경도 데이터가 바뀌어서 임시로...
        params.put("lat", orgLatitude);
        params.put("log", orgLongitude);

        //body(form-data) img_prk(file)
        //@Multipart @Part MultipartBody.Part 파일 변수 만들기
        MultipartBody.Part photoBody=null;
        if(photoFile!=null){
            RequestBody fileBody=RequestBody.create(photoFile, MediaType.parse("image/*"));
            photoBody=MultipartBody.Part.createFormData("img_prk", photoFile.getName(), fileBody);
        }

        Call<DataListRes> call=null;
        if(pApiGbn==1) {
            call = api.proximateParkingLot(params);
        } else if(pApiGbn==2) {
            call = api.parkingImgUpload(photoBody);
        } else if(pApiGbn==3) {
            if(data.getPrk_id()==0) {
                data.setStart_prk_at(Util.getNowDateTime());
                call = api.parkingComplete("Bearer " + accessToken, data);
            }else{
                call = api.parkingUpdate("Bearer " + accessToken, data.getPrk_id(), data);
            }
        }

        call.enqueue(new Callback<DataListRes>() {
            @Override
            public void onResponse(Call<DataListRes> call, Response<DataListRes> response) {
                Log.i("로그", response.toString());
                dismissProgress();
                //http상태코드 확인
                if(response.isSuccessful()) {
                    DataListRes dataListRes=response.body();
                    if (dataListRes.getResult().equals("success")) {
                        if(pApiGbn==1) {
                            Toast.makeText(getContext(), dataListRes.getCount() + "개의 주차장 정보 수신 완료.", Toast.LENGTH_SHORT).show();
                            if(dataListRes.getCount() > 0) {
                                data = (Data) dataListRes.getItems().get(0);
                            }
                            displayParkingLot();
                        }else if(pApiGbn==2) {
                            Toast.makeText(getContext(), "주차사진 저장 완료.", Toast.LENGTH_SHORT).show();
                            data.setImg_prk(dataListRes.getImg_prk());
                            data.setPrk_area(dataListRes.getDetectedText());
                            displayParkingLot();
                        }else if(pApiGbn==3) {
//                            Toast.makeText(getContext(), "저장 완료.", Toast.LENGTH_LONG).show();
                            //알러트 다이얼로그(팝업)
                            AlertDialog.Builder alert=new AlertDialog.Builder(getContext());
                            if(data.getPrk_id()==0) {
                                data.setPrk_id(dataListRes.getPrk_id());
                                data.setPush_prk_id(dataListRes.getPrk_id());
                                alert.setTitle("주차완료 저장 성공");
                            }else{
                                alert.setTitle("주차완료 수정 성공");
                            }
                            writeSharedPreferences();
                            linerLayoutMyLoc.setVisibility(View.GONE);

                            alert.setMessage("홈화면으로 이동하시겠습니까?");
                            alert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    mainActivity.changeFragment(R.id.firstFragment, new FirstFragment());
                                }
                            });
                            alert.setNegativeButton("No", null);
                            //알러트 다이얼로그의 버튼을 안누르면, 화면이 넘어가지 않게..
                            alert.setCancelable(false);
                            //다이얼로그 화면에 보이기
                            alert.show();
                        }
                    }
                } else {
                    try{
                        JSONObject errorBody= new JSONObject(response.errorBody().string());
                        Toast.makeText(getContext(),
//                                "에러발생\n"+
//                                        "코드 : "+response.code()+"\n" +
                                        "에러 : "+errorBody.getString("error")
                                , Toast.LENGTH_LONG).show();
                        Log.i("로그", "에러발생 : "+response.code()+", "+errorBody.getString("error"));
                    }catch (IOException | JSONException e){
                        Toast.makeText(getContext(),
//                                "에러발생\n"+
//                                        "코드 : "+response.code()+"\n" +
                                        "에러 : "+e.getMessage()
                                , Toast.LENGTH_LONG).show();
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(Call<DataListRes> call, Throwable t) {
                //통신실패 네트워크 자체 문제로 실패되는 경우
                dismissProgress();
                Toast.makeText(getContext(), "시스템에러발생 : "+t.getMessage(), Toast.LENGTH_LONG).show();
                Log.i("로그", "시스템에러발생 : "+t.getMessage());
                t.printStackTrace();
            }
        });
    }

    void displayCreateViewParkingLot(){
        if(data.getPrk_id()!=0) {
            txtName.setText(data.getPrk_plce_nm());
            txtAddr.setText(data.getPrk_plce_adres());
            etxtArea.setText(data.getPrk_area());
            if (!data.getImg_prk().isEmpty()) {
                //클라이드 라이브러리 사용
                GlideUrl url = new GlideUrl(data.getImg_prk(), new LazyHeaders.Builder().addHeader("User-Agent", "Android").build());
                Glide.with(getActivity()).load(url).into(imgParking);
            }else{
                imgParking.setImageResource(R.drawable.ic_baseline_photo_camera_24);
                photoFile=null;
            }
        }else{
            txtName.setText("");
            txtAddr.setText("");
            etxtArea.setText("");
            imgParking.setImageResource(R.drawable.ic_baseline_photo_camera_24);
            photoFile=null;
        }
    }

    void displayParkingLot(){
        txtName.setText(data.getPrk_plce_nm());
        txtAddr.setText(data.getPrk_plce_adres());
        etxtArea.setText(data.getPrk_area());
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

    void showImageChoiceMethod(){
        // 실제는 카메라만 사용한다.
        // camera(); 만  코딩하고 나머지는 주석처리 할것
//        androidx.appcompat.app.AlertDialog.Builder builder= new androidx.appcompat.app.AlertDialog.Builder(getContext());
//        builder.setTitle("프로필이미지");
//        builder.setItems(R.array.alert_photo, new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialogInterface, int i) {
//                if(i==0){
//                    //카메라을 선택하면 카메라앱 실행 사진 찍기
//                    //사진을 찍어 저장된 이미지를 이미지뷰에 보여준다.
                    camera();
//                } else if(i==1){
//                    //앨범을 선택하면 앨범앱 실행 사진 선택
//                    //선택한 이미지를 이미지뷰에 보여준다.
//                    album();
//                }
//            }
//        });
//        androidx.appcompat.app.AlertDialog alert=builder.create();
//        alert.show();
    }

    private void camera(){
        int permissionCheck = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA);

        if(permissionCheck != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.CAMERA} , 1000);
            Toast.makeText(getActivity(), "카메라 권한 필요합니다.", Toast.LENGTH_SHORT).show();
            return;
        } else {
            Intent i = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if(i.resolveActivity(getActivity().getPackageManager())  != null  ){
                // 사진의 파일명을 만들기
                String fileName = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                photoFile = getPhotoFile(fileName);
                Uri fileProvider = FileProvider.getUriForFile(getActivity(), "com.yh.parkingpartner.fileprovider", photoFile);
                i.putExtra(MediaStore.EXTRA_OUTPUT, fileProvider);
                startActivityForResult(i, 100);
            } else{
                Toast.makeText(getActivity(), "이폰에는 카메라 앱이 없습니다.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private File getPhotoFile(String fileName) {
        File storageDirectory = getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        try{
            return File.createTempFile(fileName, ".jpg", storageDirectory);
        }catch (IOException e){
            e.printStackTrace();
            return null;
        }
    }

    private void album(){
        if(checkPermission()){
            displayFileChoose();
        }else{
            requestPermission();
        }
    }

    private void requestPermission() {
        if(ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE)){
            Toast.makeText(getActivity(), "권한 수락이 필요합니다.", Toast.LENGTH_SHORT).show();
        }else{
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 500);
        }
    }

    private boolean checkPermission(){
        int result = ContextCompat.checkSelfPermission(getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if(result == PackageManager.PERMISSION_DENIED){
            return false;
        }else{
            return true;
        }
    }

    private void displayFileChoose() {
        Intent i = new Intent();
        i.setType("image/*");
        i.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(i, "사진 선택"), 300);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        blnCamera=true;

        if(requestCode == 100 && resultCode == Activity.RESULT_OK){
            Bitmap photo = BitmapFactory.decodeFile(photoFile.getAbsolutePath());
            ExifInterface exif = null;
            try {
                exif = new ExifInterface(photoFile.getAbsolutePath());
            } catch (IOException e) {
                e.printStackTrace();
            }
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED);
            photo = rotateBitmap(photo, orientation);

            // 압축시킨다. 해상도 낮춰서
            OutputStream os;
            try {
                os = new FileOutputStream(photoFile);
                photo.compress(Bitmap.CompressFormat.JPEG, 50, os);
                os.flush();
                os.close();
            } catch (Exception e) {
                Log.e(getClass().getSimpleName(), "Error writing bitmap", e);
            }

            photo = BitmapFactory.decodeFile(photoFile.getAbsolutePath());
            imgParking.setImageBitmap(photo);
//            imgParking.setScaleType(ImageView.ScaleType.FIT_XY);

            //카메라로 사진을 찍었다.. AWS 텍스트탐지 API호출하자...
            if(photoFile!=null){
                getNetworkData(2);
            }

        }else if(requestCode == 300 && resultCode == Activity.RESULT_OK && data != null && data.getData() != null){
            Uri albumUri = data.getData( );
            String fileName = getFileName( albumUri );
            try {
                ParcelFileDescriptor parcelFileDescriptor = getActivity().getContentResolver( ).openFileDescriptor( albumUri, "r" );
                if ( parcelFileDescriptor == null ) return;
                FileInputStream inputStream = new FileInputStream( parcelFileDescriptor.getFileDescriptor( ) );
                photoFile = new File( getActivity().getCacheDir( ), fileName );
                FileOutputStream outputStream = new FileOutputStream( photoFile );
                IOUtils.copy( inputStream, outputStream );

                // 압축시킨다. 해상도 낮춰서
                Bitmap photo = BitmapFactory.decodeFile(photoFile.getAbsolutePath());
                OutputStream os;
                try {
                    os = new FileOutputStream(photoFile);
                    photo.compress(Bitmap.CompressFormat.JPEG, 60, os);
                    os.flush();
                    os.close();
                } catch (Exception e) {
                    Log.e(getClass().getSimpleName(), "Error writing bitmap", e);
                }

                imgParking.setImageBitmap( getBitmapAlbum( imgParking, albumUri ) );
                //imgParking.setScaleType(ImageView.ScaleType.FIT_XY);

                //사진을 선택했으면.. AWS 텍스트탐지 API호출하자...
                if(photoFile!=null){
                    getNetworkData(2);
                }

            } catch ( Exception e ) {
                e.printStackTrace( );
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public static Bitmap rotateBitmap(Bitmap bitmap, int orientation) {

        Matrix matrix = new Matrix();
        switch (orientation) {
            case ExifInterface.ORIENTATION_NORMAL:
                return bitmap;
            case ExifInterface.ORIENTATION_FLIP_HORIZONTAL:
                matrix.setScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_ROTATE_180:
                matrix.setRotate(180);
                break;
            case ExifInterface.ORIENTATION_FLIP_VERTICAL:
                matrix.setRotate(180);
                matrix.postScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_TRANSPOSE:
                matrix.setRotate(90);
                matrix.postScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_ROTATE_90:
                matrix.setRotate(90);
                break;
            case ExifInterface.ORIENTATION_TRANSVERSE:
                matrix.setRotate(-90);
                matrix.postScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_ROTATE_270:
                matrix.setRotate(-90);
                break;
            default:
                return bitmap;
        }
        try {
            Bitmap bmRotated = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
            bitmap.recycle();
            return bmRotated;
        }
        catch (OutOfMemoryError e) {
            e.printStackTrace();
            return null;
        }
    }

    //앨범에서 선택한 사진이름 가져오기
    public String getFileName( Uri uri ) {
        Cursor cursor = getActivity().getContentResolver( ).query( uri, null, null, null, null );
        try {
            if ( cursor == null ) return null;
            cursor.moveToFirst( );
            @SuppressLint("Range") String fileName = cursor.getString( cursor.getColumnIndex( OpenableColumns.DISPLAY_NAME ) );
            cursor.close( );
            return fileName;

        } catch ( Exception e ) {
            e.printStackTrace( );
            cursor.close( );
            return null;
        }
    }

    //이미지뷰에 뿌려질 앨범 비트맵 반환
    public Bitmap getBitmapAlbum( View targetView, Uri uri ) {
        try {
            ParcelFileDescriptor parcelFileDescriptor = getActivity().getContentResolver( ).openFileDescriptor( uri, "r" );
            if ( parcelFileDescriptor == null ) return null;
            FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor( );
            if ( fileDescriptor == null ) return null;

            int targetW = targetView.getWidth( );
            int targetH = targetView.getHeight( );

            BitmapFactory.Options options = new BitmapFactory.Options( );
            options.inJustDecodeBounds = true;

            BitmapFactory.decodeFileDescriptor( fileDescriptor, null, options );

            int photoW = options.outWidth;
            int photoH = options.outHeight;

            int scaleFactor = Math.min( photoW / targetW, photoH / targetH );
            if ( scaleFactor >= 8 ) {
                options.inSampleSize = 8;
            } else if ( scaleFactor >= 4 ) {
                options.inSampleSize = 4;
            } else {
                options.inSampleSize = 2;
            }
            options.inJustDecodeBounds = false;

            Bitmap reSizeBit = BitmapFactory.decodeFileDescriptor( fileDescriptor, null, options );

            ExifInterface exifInterface = null;
            try {
                if ( Build.VERSION.SDK_INT >= Build.VERSION_CODES.N ) {
                    exifInterface = new ExifInterface( fileDescriptor );
                }
            } catch ( IOException e ) {
                e.printStackTrace( );
            }

            int exifOrientation;
            int exifDegree = 0;

            //사진 회전값 구하기
            if ( exifInterface != null ) {
                exifOrientation = exifInterface.getAttributeInt( ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL );

                if ( exifOrientation == ExifInterface.ORIENTATION_ROTATE_90 ) {
                    exifDegree = 90;
                } else if ( exifOrientation == ExifInterface.ORIENTATION_ROTATE_180 ) {
                    exifDegree = 180;
                } else if ( exifOrientation == ExifInterface.ORIENTATION_ROTATE_270 ) {
                    exifDegree = 270;
                }
            }

            parcelFileDescriptor.close( );
            Matrix matrix = new Matrix( );
            matrix.postRotate( exifDegree );

            Bitmap reSizeExifBitmap = Bitmap.createBitmap( reSizeBit, 0, 0, reSizeBit.getWidth( ), reSizeBit.getHeight( ), matrix, true );
            return reSizeExifBitmap;

        } catch ( Exception e ) {
            e.printStackTrace( );
            return null;
        }
    }
}
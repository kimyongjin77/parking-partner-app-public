package com.yh.parkingpartner.ui;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.skt.Tmap.TMapTapi;
import com.yh.parkingpartner.R;
import com.yh.parkingpartner.api.ApiFirstFragment;
import com.yh.parkingpartner.api.NetworkClient;
import com.yh.parkingpartner.config.Config;
import com.yh.parkingpartner.model.Data;
import com.yh.parkingpartner.model.DataListRes;
import com.yh.parkingpartner.util.SkTmapApp;
import com.yh.parkingpartner.util.Util;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class FirstFragment extends Fragment
        implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    MainActivity mainActivity;
    MapView mapView;

    LocationManager locationManager;
    LocationListener locationListener;

    double nowLatitude;
    double nowLongitude;

    double orgLatitude;
    double orgLongitude;

    GoogleMap googleMap;

    //네트워크 처리 보여주는 프로그래스 다이얼로그
    ProgressDialog progressDialog;

    boolean blnCreatedView;
    ArrayList<Data> dataList=null;
    Place googleLibPlacesApiData;
    com.yh.parkingpartner.model.Place googleTextSearchApiData;

    ImageView imgMyLoc;
    ImageView imgListView;
    ImageView imgDestinationSerarch;

    ImageView imgRadius;
    TextView txtRadius;
    LinearLayout radiusSelector;
    Button btnRadius100;
    Button btnRadius200;
    Button btnRadius300;
    Button btnRadius500;
    Button btnRadius700;
    Button btnRadius1000;
    int radius;

    LinearLayout cardView;
    Button btnTmap;
    ImageView imgClose;

    TextView prk_center_id;
    TextView prk_plce_nm;
    TextView prk_plce_adres;
    TextView prk_cmprt_co;
    TextView pkfc_Available_ParkingLots_total;
    TextView parking_chrge_bs_time;
    TextView parking_chrge_bs_chrg;
    TextView parking_chrge_adit_unit_time;
    TextView parking_chrge_adit_unit_chrge;
    TextView parking_chrge_one_day_chrge;
    RatingBar avg_rating;

    SkTmapApp skTmapApp;

    int markerSelectedIndex;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        readSharedPreferences();

        mainActivity = (MainActivity) getActivity();
        mainActivity.getSupportActionBar().setTitle(R.string.MY_POSITION_ARROUND);
        blnCreatedView=true;
        skTmapApp=new SkTmapApp(getContext());

        locationManager = (LocationManager) getActivity().getSystemService(getContext().LOCATION_SERVICE);
        //gps 로케이션 위치 받아오는 리스너
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(@NonNull Location location) {
//                Log.i("로그", "onLocationChanged 위도 : " + location.getLatitude() + ", 경도 : " + location.getLongitude());
                nowLatitude=location.getLatitude();
                nowLongitude=location.getLongitude();

                if(nowLatitude<=0){
                    nowLatitude=0;
                }

                if(nowLongitude<=0){
                    nowLongitude=0;
                }

                if(googleMap!=null && blnCreatedView==false && nowLatitude>0 && nowLongitude>0){
                    blnCreatedView=true;
                    dismissProgress();
                    getNetworkData(Util.MAP_MY_ARROUND, null, null, null);
                }
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
                LocationListener.super.onStatusChanged(provider, status, extras);
                Log.i("로그", "onStatusChanged provider : " + provider + ", status : " + status);
            }

            @Override
            public void onProviderEnabled(@NonNull String provider) {
                LocationListener.super.onProviderEnabled(provider);
                Log.i("로그", "onProviderEnabled provider : " + provider);
            }

            @Override
            public void onProviderDisabled(@NonNull String provider) {
                LocationListener.super.onProviderDisabled(provider);
                Log.i("로그", "onProviderDisabled provider : " + provider);
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

        showProgress("현재 GPS좌표를 수신 중입니다...");
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
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_first, container, false);
        //ui find
        mapView = rootView.findViewById(R.id.map);

        imgMyLoc = rootView.findViewById(R.id.imgMyLoc);
        imgListView = rootView.findViewById(R.id.imgListView);
        imgDestinationSerarch = rootView.findViewById(R.id.imgDestinationSerarch);

        imgRadius= rootView.findViewById(R.id.imgRadius);
        txtRadius= rootView.findViewById(R.id.txtRadius);
        radiusSelector= rootView.findViewById(R.id.radiusSelector);
        btnRadius100= rootView.findViewById(R.id.btnRadius100);
        btnRadius200= rootView.findViewById(R.id.btnRadius200);
        btnRadius300= rootView.findViewById(R.id.btnRadius300);
        btnRadius500= rootView.findViewById(R.id.btnRadius500);
        btnRadius700= rootView.findViewById(R.id.btnRadius700);
        btnRadius1000= rootView.findViewById(R.id.btnRadius1000);

        View.OnClickListener onRadiusClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(view.getId()==R.id.btnRadius100){
                    radius=100;
                }else if(view.getId()==R.id.btnRadius200){
                    radius=200;
                }else if(view.getId()==R.id.btnRadius300){
                    radius=300;
                }else if(view.getId()==R.id.btnRadius500){
                    radius=500;
                }else if(view.getId()==R.id.btnRadius700){
                    radius=700;
                }else{
                    radius=1000;
                }

                txtRadius.setText(String.valueOf(radius / 1000.0));
                writeSharedPreferences();

                if(mainActivity.getSupportActionBar().getTitle().toString().equals(getString(R.string.MY_POSITION_ARROUND))){
                    getNetworkData(Util.MAP_MY_ARROUND, null, null, null);
                } else {
                    getNetworkData(Util.MAP_DESTINATION_ARROUND, googleLibPlacesApiData, googleTextSearchApiData, null);
                }
            }
        };

        btnRadius100.setOnClickListener(onRadiusClickListener);
        btnRadius200.setOnClickListener(onRadiusClickListener);
        btnRadius300.setOnClickListener(onRadiusClickListener);
        btnRadius500.setOnClickListener(onRadiusClickListener);
        btnRadius700.setOnClickListener(onRadiusClickListener);
        btnRadius1000.setOnClickListener(onRadiusClickListener);

        cardView = rootView.findViewById(R.id.cardView);
        btnTmap = rootView.findViewById(R.id.btnTmap);
        imgClose = rootView.findViewById(R.id.imgClose);

        prk_center_id = rootView.findViewById(R.id.prk_center_id);
        prk_plce_nm = rootView.findViewById(R.id.prk_plce_nm);
        prk_plce_adres = rootView.findViewById(R.id.prk_plce_adres);
        prk_cmprt_co = rootView.findViewById(R.id.prk_cmprt_co);
        pkfc_Available_ParkingLots_total = rootView.findViewById(R.id.pkfc_Available_ParkingLots_total);
        parking_chrge_bs_time = rootView.findViewById(R.id.parking_chrge_bs_time);
        parking_chrge_bs_chrg = rootView.findViewById(R.id.parking_chrge_bs_chrg);
        parking_chrge_adit_unit_time = rootView.findViewById(R.id.parking_chrge_adit_unit_time);
        parking_chrge_adit_unit_chrge = rootView.findViewById(R.id.parking_chrge_adit_unit_chrge);
        parking_chrge_one_day_chrge = rootView.findViewById(R.id.parking_chrge_one_day_chrge);
        avg_rating = rootView.findViewById(R.id.avg_rating);

        imgMyLoc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (nowLatitude <= 0 || nowLongitude <= 0) {
                    Log.i("로그", "getLastKnownLocation 호출");
                    Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    Log.i("로그", "getLastKnownLocation 위도 : " + location.getLatitude() + ", 경도 : " + location.getLongitude());

                    nowLatitude = location.getLatitude();
                    nowLongitude = location.getLongitude();
                }

                if (nowLatitude <= 0) {
                    nowLatitude = 0;
                }

                if (nowLongitude <= 0) {
                    nowLongitude = 0;
                }

                if (nowLatitude > 0 && nowLongitude > 0) {
                    getNetworkData(Util.MAP_MY_ARROUND, null, null, null);
                }else{
                    Toast.makeText(getContext(), "현 GPS위치를 찾지 못했습니다.", Toast.LENGTH_LONG).show();
                }
            }
        });

        imgListView.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                // 목록보기 액티비티 호출
                Intent intent=new Intent(getContext(), ParkListActivity.class);
                intent.putExtra("title", mainActivity.getSupportActionBar().getTitle());
                if(mainActivity.getSupportActionBar().getTitle().toString().equals(getString(R.string.MY_POSITION_ARROUND))){
                    intent.putExtra("latitude", nowLatitude);
                    intent.putExtra("longitude", nowLongitude);
                } else {
                    intent.putExtra("latitude", orgLatitude);
                    intent.putExtra("longitude", orgLongitude);
                }
                startActivityForResult(intent, Util.PARKLIST_ACTIVITY_REQUEST_CODE);
            }
        });

        imgDestinationSerarch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //목적지 검색 액티비티 호출
                Intent intent=new Intent(getContext(), SearchActivity.class);
                startActivityForResult(intent, Util.SEARCH_ACTIVITY_REQUEST_CODE);

//                //구글라이브러리 Use an intent to launch the autocomplete activity
//                Places.initialize(getContext(), Config.GG_API_KEY, Locale.KOREA);
//               // Set the fields to specify which types of place data to
//                // return after the user has made a selection.
//                List<Place.Field> fields = Arrays.asList(Place.Field.NAME
//                        ,Place.Field.ADDRESS
//                        ,Place.Field.PHONE_NUMBER
//                        ,Place.Field.LAT_LNG
//                );
//                // Start the autocomplete intent.
//                Intent intent = new Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY, fields)
//                        .setCountry("KR")
//                        .setHint("목적지 검색")
//                        .build(getContext());
//                startActivityForResult(intent, Util.AUTOCOMPLETE_REQUEST_CODE);
            }
        });

        imgRadius.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(radiusSelector.getVisibility()==View.GONE) {
                    radiusSelector.setVisibility(View.VISIBLE);
                }else{
                    radiusSelector.setVisibility(View.GONE);
                }
            }
        });

        imgClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cardView.setVisibility(View.GONE);
            }
        });

        btnTmap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //알러트 다이얼로그(팝업)
                AlertDialog.Builder alert=new AlertDialog.Builder(getContext());
                alert.setTitle(prk_plce_nm.getText().toString());
                alert.setMessage(prk_plce_adres.getText().toString()
                        +"\n\nTmap앱 길안내로 이동하시겠습니까?");
                alert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //인증 콜백이 안 터짐... 고로.. 인증 확인 제거...
//                        if(skTmapApp.isAuthentication()){
                            if(skTmapApp.checkTmapApplicationInstalled()){
                                //skTmapApp.searchProtal(prk_plce_nm.getText().toString());
                                skTmapApp.searchRoute(nowLatitude, nowLongitude, dataList.get(markerSelectedIndex));
                            }else{
                                skTmapApp.tmapInstall();
                            }
//                        }else{
//                            Toast.makeText(getContext(), "TMap 인증되지 않았습니다.", Toast.LENGTH_LONG).show();
//                        }
                    }
                });
                alert.setNegativeButton("No", null);
                //알러트 다이얼로그의 버튼을 안누르면, 화면이 넘어가지 않게..
                alert.setCancelable(false);
                //다이얼로그 화면에 보이기
                alert.show();
            }
        });

        //mapview 설정
        initMapview(savedInstanceState);
        //이벤트 등록
        return rootView;
    }

    //mapview 설정
    private void initMapview(Bundle savedInstanceState) {
        mapView.onCreate(savedInstanceState);
        mapView.onResume();
        mapView.getMapAsync(this);
        cardView.setVisibility(View.GONE);
        txtRadius.setText(String.valueOf(radius / 1000.0));
        radiusSelector.setVisibility(View.GONE);
        blnCreatedView=false;
    }

    @Override
    public void onMapReady(final GoogleMap googleMap) {
        this.googleMap=googleMap;
//        //지도타입
        googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
////        googleMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        //마커클릭 리스너 셋팅
        googleMap.setOnMarkerClickListener(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i("로그", "FirstFragment.onDestroy locationManager 리슨너 해제");
        locationManager.removeUpdates(locationListener);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == Util.AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == AutocompleteActivity.RESULT_OK) {
                googleLibPlacesApiData = Autocomplete.getPlaceFromIntent(data);
                Log.i("로그", "RESULT_OK Place : "
                        + googleLibPlacesApiData.getName()
                        + ", "+ googleLibPlacesApiData.getAddress()
                        + ", "+ googleLibPlacesApiData.getPhoneNumber()
                        + ", "+ googleLibPlacesApiData.getLatLng().latitude
                        + ", "+ googleLibPlacesApiData.getLatLng().longitude
                );

                getNetworkData(Util.MAP_DESTINATION_ARROUND, googleLibPlacesApiData, null, null);

            } else if (resultCode == AutocompleteActivity.RESULT_ERROR) {
                Status status = Autocomplete.getStatusFromIntent(data);
                Log.i("로그", "RESULT_ERROR Msg : "+status.getStatusMessage());
            } else if (resultCode == AutocompleteActivity.RESULT_CANCELED) {
                // The user canceled the operation.
                Log.i("로그", "RESULT_CANCELED");
            }
            return;
        } else if(requestCode == Util.SEARCH_ACTIVITY_REQUEST_CODE){
            Log.i("로그", "SEARCH_ACTIVITY_REQUEST_RETURN");
            if (resultCode == Activity.RESULT_OK) {
                Log.i("로그", "RESULT_OK");
                googleTextSearchApiData= (com.yh.parkingpartner.model.Place) data.getSerializableExtra("destination");
                Log.i("로그", googleTextSearchApiData.getName());
                Log.i("로그", googleTextSearchApiData.getFormatted_address());
                Log.i("로그", "위도 : "+googleTextSearchApiData.getGeometry().getLocation().getLat());
                Log.i("로그", "경도 : "+googleTextSearchApiData.getGeometry().getLocation().getLng());

                getNetworkData(Util.MAP_DESTINATION_ARROUND, null, googleTextSearchApiData, null);

            } else if (resultCode == Activity.RESULT_CANCELED) {
                Log.i("로그", "RESULT_CANCELED");
            }
            return;
        } else if(requestCode == Util.PARKLIST_ACTIVITY_REQUEST_CODE){
            Log.i("로그", "PARKLIST_ACTIVITY_REQUEST_CODE");
            if (resultCode == Activity.RESULT_OK) {
                Log.i("로그", "RESULT_OK");
                Data rtnData= (Data) data.getSerializableExtra("parklist");
                Log.i("로그", rtnData.getPrk_center_id());
                Log.i("로그", rtnData.getPrk_plce_nm());
                Log.i("로그", rtnData.getPrk_plce_adres());
                Log.i("로그", "위도 : "+rtnData.getPrk_plce_entrc_la());
                Log.i("로그", "경도 : "+rtnData.getPrk_plce_entrc_lo());

                getNetworkData(Util.MAP_ONE_PICK, null, null, rtnData);

            } else if (resultCode == Activity.RESULT_CANCELED) {
                Log.i("로그", "RESULT_CANCELED");
            }
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public boolean onMarkerClick(@NonNull Marker marker) {
        int index= (int) marker.getTag();
        Log.i("로그", "마커클릭 index : "+index);

        if(index>=0) {
            prk_center_id.setText(dataList.get(index).getPrk_center_id());
            prk_plce_nm.setText(dataList.get(index).getPrk_plce_nm());
            prk_plce_adres.setText(dataList.get(index).getPrk_plce_adres());
            prk_cmprt_co.setText(Util.myDecFormatter.format(dataList.get(index).getPrk_cmprt_co())+"개");
            pkfc_Available_ParkingLots_total.setText(Util.myDecFormatter.format(dataList.get(index).getPkfc_Available_ParkingLots_total())+"개");
            parking_chrge_bs_time.setText(Util.myDecFormatter.format(dataList.get(index).getParking_chrge_bs_time())+"분");
            parking_chrge_bs_chrg.setText(Util.myDecFormatter.format(dataList.get(index).getParking_chrge_bs_chrg())+"원");
            parking_chrge_adit_unit_time.setText(Util.myDecFormatter.format(dataList.get(index).getParking_chrge_adit_unit_time())+"분");
            parking_chrge_adit_unit_chrge.setText(Util.myDecFormatter.format(dataList.get(index).getParking_chrge_adit_unit_chrge())+"원");
            parking_chrge_one_day_chrge.setText(Util.myDecFormatter.format(dataList.get(index).getParking_chrge_one_day_chrge())+"원");
            avg_rating.setRating(dataList.get(index).getRating());

            cardView.setVisibility(View.VISIBLE);
            markerSelectedIndex=index;
        } else {
            cardView.setVisibility(View.GONE);
            markerSelectedIndex=-1;
        }
        return false;
    }

    private void getNetworkData(int pApiGbn
            , @Nullable Place destination
            , @Nullable com.yh.parkingpartner.model.Place pdestination
            , @Nullable Data parkingonepick) {

        cardView.setVisibility(View.GONE);
        radiusSelector.setVisibility(View.GONE);
        googleMap.clear();

        if(pApiGbn==Util.MAP_MY_ARROUND) {
            mainActivity.getSupportActionBar().setTitle(R.string.MY_POSITION_ARROUND);
            orgLatitude = nowLatitude;
            orgLongitude = nowLongitude;
            if (orgLatitude == 0 || orgLongitude == 0) {
                return;
            }
        } else if(pApiGbn==Util.MAP_DESTINATION_ARROUND) {
            if(destination!=null) {
                mainActivity.getSupportActionBar().setTitle(destination.getName() + getString(R.string.ARROUND));
                orgLatitude = destination.getLatLng().latitude;
                orgLongitude = destination.getLatLng().longitude;
            }else if(pdestination!=null){
                mainActivity.getSupportActionBar().setTitle(pdestination.getName() + getString(R.string.ARROUND));
                orgLatitude = pdestination.getGeometry().getLocation().getLat();
                orgLongitude = pdestination.getGeometry().getLocation().getLng();
            }
        } else {
            setMarker(pApiGbn,
                    parkingonepick.getPrk_plce_entrc_la(), parkingonepick.getPrk_plce_entrc_lo(),
                    destination, pdestination, parkingonepick);
            return;
        }

        //네트워크데이터를 보내고 있다는 프로그래스 다이얼로그를 먼저 띄운다..
        showProgress("주변 주차장을 수신 중입니다...");

        //목록 가져오는 api 호출
        Retrofit retrofit= NetworkClient.getRetrofitClient(getContext(), Config.PP_BASE_URL);
        ApiFirstFragment api=retrofit.create(ApiFirstFragment.class);

        //?lat = request.args['lat']&log = request.args['log']
        Map<String, Object> params=new HashMap<>();
        //db에 위도, 경도 데이터가 바뀌어서 임시로...
        params.put("lat", orgLatitude);
        params.put("log", orgLongitude);
        params.put("radius", radius);

        Call<DataListRes> call = api.aroundParkingLot(params);
        call.enqueue(new Callback<DataListRes>() {
            @Override
            public void onResponse(Call<DataListRes> call, Response<DataListRes> response) {
                Log.i("로그", response.toString());
                dismissProgress();
                //http상태코드 확인
                if(response.isSuccessful()) {
                    DataListRes dataListRes=response.body();
                    if (dataListRes.getResult().equals("success")) {
                        Toast.makeText(getContext(), dataListRes.getCount()+"개의 주차장 정보 수신 완료.", Toast.LENGTH_SHORT).show();
                        dataList=(ArrayList<Data>) dataListRes.getItems();
                        setMarker(pApiGbn, orgLatitude, orgLongitude, destination, pdestination, parkingonepick);
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

    void setMarker(int  pApiGbn, double pLatitude, double pLongitude
            , @Nullable Place destination
            , @Nullable com.yh.parkingpartner.model.Place pdestination
            , @Nullable Data parkingonepick) {

        Marker marker=null;
        Circle circle=null;
        LatLng orgLoc=null;

        showProgress("지도에 표시 중...");

        googleMap.clear();
        orgLoc=new LatLng(pLatitude, pLongitude);
        marker=googleMap.addMarker(new MarkerOptions().position(orgLoc));

        if(pApiGbn==Util.MAP_MY_ARROUND) {
            marker.setTag(-1);
            marker.setTitle(getString(R.string.MY_POSITION));
            marker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
        } else if(pApiGbn==Util.MAP_DESTINATION_ARROUND){
            marker.setTag(-1);
            if(destination!=null) {
                marker.setTitle(destination.getName());
                marker.setSnippet(destination.getAddress());
            } else {
                marker.setTitle(pdestination.getName());
                marker.setSnippet(pdestination.getFormatted_address());
            }
            marker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
        } else if(pApiGbn==Util.MAP_ONE_PICK){
            marker.setTag(0);
            marker.setTitle(parkingonepick.getPrk_plce_nm());
            marker.setSnippet(parkingonepick.getPrk_plce_adres());
            marker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
            marker.showInfoWindow();

            dataList.clear();
            dataList.add(parkingonepick);

            onMarkerClick(marker);
        } else {
            dismissProgress();
            return;
        }

        if(pApiGbn==Util.MAP_MY_ARROUND || pApiGbn==Util.MAP_DESTINATION_ARROUND){
            int index=-1;
            for(Data data : dataList){
                marker=googleMap.addMarker(new MarkerOptions().position(
                        new LatLng(data.getPrk_plce_entrc_la(), data.getPrk_plce_entrc_lo())));
                marker.setTag(++index);
                marker.setTitle(data.getPrk_plce_nm());
                marker.setSnippet(data.getPrk_plce_adres());
                marker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
            }

            circle=googleMap.addCircle(new CircleOptions().center(orgLoc));
            circle.setRadius(radius);
            circle.setStrokeWidth(3.0f);
        }
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(orgLoc, getZoomLevel(circle)));
        dismissProgress();
    }

    private float getZoomLevel(Circle circle) {
        float zoomLevel;
        if(circle!=null) {
            double radius = circle.getRadius();
            double scale = radius / 300;
            zoomLevel = (float) (16 - Math.log(scale) / Math.log(2));
        }else{
            zoomLevel=18;
        }
        Log.i("로그", "반경 : "+radius+", 줌레벨 : "+zoomLevel);
        return zoomLevel;
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

    void readSharedPreferences(){
        //SharedPreferences 를 이용해서, 앱 내의 저장소에 영구저장된 데이터를 읽어오는 방법
        SharedPreferences sp = getActivity().getSharedPreferences(Config.SP_NAME, getActivity().MODE_PRIVATE);
        radius = sp.getInt(Config.SP_KEY_DEFAULT_RADIUS, 500);
    }

    void writeSharedPreferences(){
        //SharedPreferences 를 이용해서, 앱 내의 저장소에 영구저장된 데이터를 읽어오는 방법
        SharedPreferences sp = getActivity().getSharedPreferences(Config.SP_NAME, getActivity().MODE_PRIVATE);
        //편집기를 만든다.
        SharedPreferences.Editor editor = sp.edit();
        //작성한다.
        editor.putInt(Config.SP_KEY_DEFAULT_RADIUS, radius);
        //저장한다.
        editor.apply();
    }

}
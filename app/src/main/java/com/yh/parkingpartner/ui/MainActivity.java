package com.yh.parkingpartner.ui;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.yh.parkingpartner.R;
import com.yh.parkingpartner.config.Config;
import com.yh.parkingpartner.util.AlarmUtil;
import com.yh.parkingpartner.util.Util;

import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    public static Activity activity;

    BottomNavigationView navigationView;
    String accessToken;
    String name;
    String email;
    String img_profile;

    private FragmentManager fragmentManager;
    private FragmentTransaction transaction;
    private FirstFragment firstFragment;
    private SecondFragment secondFragment;
    private Fragment thirdFragment;
    private Fragment fourthFragment;

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if(intent != null) {
            Log.i("로그", "onNewIntent intent");
            boolean blnNotification = intent.getBooleanExtra("notification", false);
            if (blnNotification) {
                changeFragment(R.id.fourthFragment, new FourthFragment());
            }
        }else{
            Log.i("로그", "onNewIntent intent=null");
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 로그아웃 시, MainActivity 종료할 때 필요
        activity = MainActivity.this;

        Util.setTimeZone("Asia/Seoul", Locale.KOREA);
        AlarmUtil.setAlarm(this, Util.NOTIFICATION_REQUEST_CODE);

        navigationView = findViewById(R.id.bottonNavigationView);

        fragmentManager = getSupportFragmentManager();
        transaction = fragmentManager.beginTransaction();
        firstFragment = new FirstFragment();
        secondFragment = new SecondFragment();
        thirdFragment = new ThirdFragment();
        fourthFragment = new FourthFragment();

        //SharedPreferences 를 이용해서, 앱 내의 저장소에 영구저장된 데이터를 읽어오는 방법
        SharedPreferences sp = getApplication().getSharedPreferences(Config.SP_NAME, MODE_PRIVATE);
        accessToken = sp.getString(Config.SP_KEY_ACCESS_TOKEN, "");
        Log.i("로그", "accessToken : " + accessToken);
        name = sp.getString(Config.SP_KEY_NAME, "");
        Log.i("로그", "name : " + name);
        email = sp.getString(Config.SP_KEY_EMAIL, "");
        Log.i("로그", "email : " + email);
        img_profile = sp.getString(Config.SP_KEY_IMG_PROFILE, "");
        Log.i("로그", "img_profile : " + img_profile);

        //없으면, 로그인 액티비티를 실행
        //있으면, ?
        if(accessToken.isEmpty()){
            Intent intent=new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        } else {
            boolean blnNotification=getIntent().getBooleanExtra("notification", false);
            if(blnNotification){
                changeFragment(R.id.fourthFragment, new FourthFragment());
            } else {
                transaction.replace(R.id.frameLayout, firstFragment).commitAllowingStateLoss();
            }
        }

        navigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                int itemId = item.getItemId();

                Fragment fragment = null;

                if(itemId == R.id.firstFragment){
                    fragment = firstFragment;
                }else if (itemId == R.id.secondFragment){
                    fragment = secondFragment;
                }else if (itemId == R.id.thirdFragment){
                    fragment = thirdFragment;
                }else if (itemId == R.id.fourthFragment) {
                    fragment = fourthFragment;
                }
                return loadFragment(fragment);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)    {
        getMenuInflater().inflate(R.menu.actionbar_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();

        if(itemId == R.id.my_page) {
            Intent intent = new Intent(MainActivity.this, MypageActivity.class);
            startActivity(intent);
        }else if(itemId == R.id.infor) {
            //알러트 다이얼로그(팝업)
            AlertDialog.Builder alert=new AlertDialog.Builder(MainActivity.this);
            alert.setTitle("앱 정보");
            String content="연희직업전문학교(서구청라점)\n"+
                    "클라우드 기반 인공지능 개발과 DevOps 실무과정\n"+
                    "2기 5팀 최종 프로젝트\n"+
                    "문의창구 : microchip1@naver.com\n\n"+
                    "관심 가져 주셔서 감사합니다."
                    ;
            alert.setMessage(content);
            alert.setPositiveButton("닫기", null);
            //알러트 다이얼로그의 버튼을 안누르면, 화면이 넘어가지 않게..
            alert.setCancelable(false);
            //다이얼로그 화면에 보이기
            alert.show();
        }

        return super.onOptionsItemSelected(item);
    }

    private boolean loadFragment(Fragment fragment) {
        if (fragment != null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout,fragment).commit();
            return true;
        }
        return false;
    }
    public boolean changeFragment(int itemId, Fragment fragment) {
        if (fragment != null) {
            navigationView.getMenu().findItem(itemId).setChecked(true);
            getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout,fragment).commit();
            return true;
        }
        return false;
    }

}
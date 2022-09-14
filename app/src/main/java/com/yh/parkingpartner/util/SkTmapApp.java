package com.yh.parkingpartner.util;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import com.skt.Tmap.TMapTapi;
import com.yh.parkingpartner.config.Config;
import com.yh.parkingpartner.model.Data;

import java.util.HashMap;

public class SkTmapApp {

    Context context;
    TMapTapi tmaptapi;
    boolean isAuthentication;

    public SkTmapApp(Context context) {
        this.context = context;
        this.tmaptapi = new TMapTapi(context);
        setAuthentication();
    }

    public boolean isAuthentication() {
        return isAuthentication;
    }

    public void setAuthentication(){
        tmaptapi.setSKTMapAuthentication (Config.TM_API_KEY);
        Log.i("로그", "setSKTMapAuthentication");
        tmaptapi.setOnAuthenticationListener(new TMapTapi.OnAuthenticationListenerCallback() {
            @Override
            public void SKTMapApikeySucceed() {
                Log.i("로그", "SKTMapApikeySucceed");
                isAuthentication=true;
            }
            @Override
            public void SKTMapApikeyFailed(String s) {
                Log.i("로그", "SKTMapApikeyFailed");
                isAuthentication=false;
            }
        });
    }

    public void searchProtal(String pDestination) {
        tmaptapi.invokeSearchPortal(pDestination);
    }

    public void searchRoute(double nowLatitude, double nowLongitude, Data pDestination) {
            HashMap<String, String> pathInfo = new HashMap<String, String>();
            pathInfo.put("rStName", "현위치");
            pathInfo.put("rStY", String.valueOf(nowLatitude)); //
            pathInfo.put("rStX", String.valueOf(nowLongitude));

            pathInfo.put("rGoName", pDestination.getPrk_plce_nm());
            pathInfo.put("rGoX", String.valueOf(pDestination.getPrk_plce_entrc_lo()));
            pathInfo.put("rGoY", String.valueOf(pDestination.getPrk_plce_entrc_la()));//rGoFlag
            // pathInfo.put("rGoFlag","8");
            tmaptapi.invokeRoute(pathInfo);
    }

    // 티맵 설치여부 확인
    public boolean checkTmapApplicationInstalled() {
        boolean isInstalled = tmaptapi.isTmapApplicationInstalled();
//        if (isInstalled) {
//            Toast.makeText(mContext, "TMap 이 설치되어 있습니다.", Toast.LENGTH_SHORT).show();
//        } else {
//            Toast.makeText(mContext, "TMap 이 설치되어 있지 않습니다.", Toast.LENGTH_SHORT).show();
//        }
        return isInstalled;
    }

    // 티맵 설치 페이지로 이동
    public void tmapInstall() {
            Uri uri = Uri.parse(tmaptapi.getTMapDownUrl().get(0));
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            context.startActivity(intent);
    }
}

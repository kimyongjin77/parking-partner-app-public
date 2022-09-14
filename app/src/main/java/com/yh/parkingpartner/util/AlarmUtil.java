package com.yh.parkingpartner.util;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.util.Log;

public class AlarmUtil {
    private static final long ONE_MINUTES = 1000 * 60 * 1; // 1분
    // 알람 추가 메소드
    public static void setAlarm(Context context, int requestCode){
        AlarmManager alarmMgr = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context.getApplicationContext(), NotificationUtil.class);
        // FLAG_CANCEL_CURRENT : 이전에 생성한 PendingIntent 는 취소하고 새롭게 만든다
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context
                , requestCode
                , intent
                , PendingIntent.FLAG_CANCEL_CURRENT | PendingIntent.FLAG_MUTABLE
        );

        alarmMgr.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP
                , SystemClock.elapsedRealtime() + ONE_MINUTES
                , ONE_MINUTES
                , pendingIntent);
        Log.i("로그", "AlarmUtil.setAlarm");
    }

    //알람 해제 메소드
    public static void cancelAlarm(Context context, int requestCode){
        AlarmManager alarmMgr = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, NotificationUtil.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, requestCode, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        alarmMgr.cancel(pendingIntent);
        Log.i("로그", "AlarmUtil.cancelAlarm");
    }
}
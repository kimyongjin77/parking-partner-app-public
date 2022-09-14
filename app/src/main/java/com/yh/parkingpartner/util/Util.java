package com.yh.parkingpartner.util;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class Util {
    public static final int MAP_MY_ARROUND=0;
    public static final int MAP_DESTINATION_ARROUND=1;
    public static final int MAP_ONE_PICK=2;

    public static final int NOTIFICATION_REQUEST_CODE = 0;
    public static final int AUTOCOMPLETE_REQUEST_CODE = 1;
    public static final int SEARCH_ACTIVITY_REQUEST_CODE = 2;
    public static final int PARKLIST_ACTIVITY_REQUEST_CODE = 3;

    public static final int NOTIFICATION_ID = 0;
    //todo : 테스트로 짧은 시간 설정... 실제로는 30분 정보로 셋팅 할 것
    public static final int NOTIFICATION_USE_PRK_AT = 30;     //입차시간 기준 경과 단위(분)

    public static DecimalFormat myDecFormatter = new DecimalFormat("###,###");
    public static DecimalFormat myFloatFormatter = new DecimalFormat("###,###.##");

    private static long mNow;
    private static Date mDate;
    private static SimpleDateFormat mFormat;
    private static TimeZone tz;                                        // 객체 생성

    public  static  void setTimeZone(String pTimeZone, Locale pLocale){
//        pTimeZone="Asia/Seoul";
//        pLocale=Locale.KOREA;
        mFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", pLocale);
        tz = TimeZone.getTimeZone(pTimeZone);       // TimeZone에 표준시 설정
        mFormat.setTimeZone(tz);                                //DateFormat에 TimeZone 설정
    }
    public static String getNowDateTime(){
        mNow = System.currentTimeMillis();
        mDate = new Date(mNow);
        return mFormat.format(mDate);
    }

    public static Date getStringToDateTime(String pDateTime) throws ParseException {
        Date date = mFormat.parse(pDateTime);
        return date;
    }

    public static String getDatetimeToString(Date pDateTime){
        return mFormat.format(pDateTime);
    }
}

package com.yf.wh.library.common;

import java.text.ParseException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import com.yf.wh.library.Application;
import com.yf.wh.library.R;

public class SysTimeManager {
    private final String TAG = getClass().getName();

    private static SysTimeManager instance;
    private List<SysTimeUpdateListener> timeUpdateListeners = new ArrayList<>();

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            // Log.d(TAG, "getAction = " + intent.getAction());
            exeUpdateTimeDateWeek();
        }
    };

    public void addSysTimeUpdateListener(SysTimeUpdateListener listener) {
        if (!timeUpdateListeners.contains(listener)) {
            timeUpdateListeners.add(listener);
        }
        exeUpdateTimeDateWeek();
    }

    public void removeSysTimeUpdateListener(SysTimeUpdateListener listener) {
        if (timeUpdateListeners.contains(listener)) {
            timeUpdateListeners.add(listener);
        }
    }

    public void exeUpdateTimeDateWeek() {
        for (SysTimeUpdateListener listener : timeUpdateListeners) {
            listener.updateTimeDateWeek(getNowDate() + "/" + getNowTime() + "/" + getNowWeek());
        }
    }

    public static synchronized SysTimeManager getInstance() {
        if (instance == null) {
            synchronized (SysTimeManager.class) {
                if (instance == null)
                    instance = new SysTimeManager();
            }
        }
        return instance;
    }

    private SysTimeManager() {

        IntentFilter iFilter = new IntentFilter();
        iFilter.addAction(Intent.ACTION_TIME_TICK);
        iFilter.addAction(Intent.ACTION_TIME_CHANGED);
        iFilter.addAction(Intent.ACTION_TIMEZONE_CHANGED);
        iFilter.addAction(Intent.ACTION_DATE_CHANGED);
        Application.getContext().registerReceiver(mReceiver, iFilter);
    }

    /**
     * @return 返回时间类型 yyyy.MM.dd
     */
    public String getNowDate() {
        Date currentTime = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy.MM.dd", Locale.getDefault());
        String dateString = formatter.format(currentTime);
        Log.d(TAG, "dateString = " + dateString);
        Log.d(TAG, "getEDate = " + getEDate(dateString));
        return dateString;
    }

    /**
     * @return 返回时间类型 yyyy-MM-dd
     */
    public String getNowDate(String dateStr) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy.MM.dd", Locale.getDefault());
        Date currentTime = new Date();
        try {
            currentTime = formatter.parse(dateStr);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        SimpleDateFormat formatter1 = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String dateString = null;
        if (currentTime != null) {
            dateString = formatter1.format(currentTime);
        }
        // Log.d(TAG, "dateString = " + dateString);
        return dateString;
    }

    /**
     * @return 返回时间类型 HH:mm
     */
    public String getNowTime() {
        String date = new Date().toString();
        String[] d = date.split(" ");
        String[] time = d[3].split(":");
        return time[0] + ":" + time[1];
    }

    public String getNowWeek() {
        Calendar calendar = Calendar.getInstance();
        int weekDay = -1;
        switch (calendar.get(Calendar.DAY_OF_WEEK)) {
            case Calendar.MONDAY:
                weekDay = 0;
                break;
            case Calendar.TUESDAY:
                weekDay = 1;
                break;
            case Calendar.WEDNESDAY:
                weekDay = 2;
                break;
            case Calendar.THURSDAY:
                weekDay = 3;
                break;
            case Calendar.FRIDAY:
                weekDay = 4;
                break;
            case Calendar.SATURDAY:
                weekDay = 5;
                break;
            case Calendar.SUNDAY:
                weekDay = 6;
                break;
        }
        if (weekDay == -1) {
            return "";
        }
        return Application.getContext().getResources().getStringArray(R.array.weeks)[weekDay];
    }

    /**
     * @return  time format 26 Apr 2006
     */
    public String getEDate(String str) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy.MM.dd", Locale.getDefault());
        ParsePosition pos = new ParsePosition(0);
        Date strtodate = formatter.parse(str, pos);
        String j = strtodate.toString();
        String[] k = j.split(" ");
        return k[2] + " " + k[1].toUpperCase(Locale.getDefault()) + " " + k[5];
    }

    public String getEDate() {
        String j = new Date().toString();
        String[] k = j.split(" ");
        return k[2] + " " + k[1] + " " + k[5];
    }

    public interface SysTimeUpdateListener {
        void updateTimeDateWeek(String dateTime);
    }
}

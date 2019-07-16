package com.shinnytech.futures.utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Created on 12/4/17.
 * Created by chenli.
 * Description: .
 */

public class TimeUtils {

    /**
     * date: 12/26/17
     * author: chenli
     * description: 用于activity_account的dataBinding
     */
    public static String getNowTime() {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy年MM月dd日", Locale.getDefault());
        Date curDate = new Date(System.currentTimeMillis());//获取当前时间
        return formatter.format(curDate);
    }

    public static String getNowTimeSecond() {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy年MM月dd日HH:mm:ss", Locale.getDefault());
        Date curDate = new Date(System.currentTimeMillis());//获取当前时间
        return formatter.format(curDate);
    }

    public static String getAmpTimeSecond() {
        SimpleDateFormat formatter = new SimpleDateFormat("HHmmss", Locale.getDefault());
        Date curDate = new Date(System.currentTimeMillis());//获取当前时间
        return formatter.format(curDate);
    }

    public static String getAmpTimeMinute() {
        SimpleDateFormat formatter = new SimpleDateFormat("HHmm", Locale.getDefault());
        Date curDate = new Date(System.currentTimeMillis());//获取当前时间
        return formatter.format(curDate);
    }

    public static String getAmpTimeHour() {
        SimpleDateFormat formatter = new SimpleDateFormat("HH", Locale.getDefault());
        Date curDate = new Date(System.currentTimeMillis());//获取当前时间
        return formatter.format(curDate);
    }

    public static String getAmpTimeWeekDay() {
        Calendar c = Calendar.getInstance();
        int dayOfWeek = c.get(Calendar.DAY_OF_WEEK);
        switch (dayOfWeek){
            case Calendar.SUNDAY:
                return "7";
            case Calendar.MONDAY:
                return "1";
            case Calendar.TUESDAY:
                return "2";
            case Calendar.WEDNESDAY:
                return "3";
            case Calendar.THURSDAY:
                return "4";
            case Calendar.FRIDAY:
                return "5";
            case Calendar.SATURDAY:
                return "6";
            default:
                return "";
        }
    }

    public static boolean isBetw2124(String insert_time) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date(Long.valueOf(insert_time) / 1000000));
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        if (hour >= 21 && hour <= 24) return true;
        return false;
    }

    public static boolean isBetw1620() {
        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        if (hour >= 16 && hour <= 20) return true;
        return false;
    }

}

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

    public static boolean isBetw2124(String insert_time) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date(Long.valueOf(insert_time) / 1000000));
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        if (hour >= 21 && hour <= 24) return true;
        return false;
    }

}

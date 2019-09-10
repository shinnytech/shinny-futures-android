package com.shinnytech.futures.utils;

import java.text.DateFormat;
import java.text.ParseException;
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

    public static final String YMD_FORMAT = "yyyy-MM-dd";

    public static final String YMD_FORMAT_2 = "yyyy.MM.dd";

    public static final String YMD_FORMAT_3 = "yyyy年MM月dd日";

    public static final String YMD_FORMAT_4 = "yyyyMMdd";

    public static final String YM_FORMAT = "yyyy-MM";

    public static final String MD_FORMAT = "MM-dd";

    public static final String MD_FORMAT_2 = "MM.dd";

    public static final String HMS_FORMAT = "HH:mm:ss";

    public static final String HMS_FORMAT_2 = "HHmmss";

    public static final String HM_FORMAT = "HH:mm";

    public static final String HM_FORMAT_2 = "HHmm";

    public static final String H_FORMAT = "HH";

    public static final String YMD_HMS_FORMAT = "yyyy-MM-dd HH:mm:ss";

    public static final String YMD_HMS_FORMAT_2 = "yyyy年MM月dd日\nHH:mm:ss";

    public static final String YMD_HMS_FORMAT_3 = "yyyy年MM月dd日HH:mm:ss";

    public static final String YMD_HMS_FORMAT_4 = "yyyyMMdd HH:mm:ss";

    public static final String YMD_HM_FORMAT = "yyyy-MM-dd HH:mm";

    public static final String YMD_HM_FORMAT_2 = "yyyy年MM月dd日 HH:mm";

    /**
     * date: 12/26/17
     * author: chenli
     * description: 用于activity_account的dataBinding
     */
    public static String getNowTime() {
        SimpleDateFormat formatter = new SimpleDateFormat(YMD_FORMAT_3, Locale.getDefault());
        Date curDate = new Date(System.currentTimeMillis());//获取当前时间
        return formatter.format(curDate);
    }

    public static String getNowTimeSecond() {
        SimpleDateFormat formatter = new SimpleDateFormat(YMD_HMS_FORMAT_3, Locale.getDefault());
        Date curDate = new Date(System.currentTimeMillis());//获取当前时间
        return formatter.format(curDate);
    }

    public static String getAmpTimeSecond() {
        SimpleDateFormat formatter = new SimpleDateFormat(HMS_FORMAT_2, Locale.getDefault());
        Date curDate = new Date(System.currentTimeMillis());//获取当前时间
        return formatter.format(curDate);
    }

    public static String getAmpTimeMinute() {
        SimpleDateFormat formatter = new SimpleDateFormat(HM_FORMAT_2, Locale.getDefault());
        Date curDate = new Date(System.currentTimeMillis());//获取当前时间
        return formatter.format(curDate);
    }

    public static String getAmpTimeHour() {
        SimpleDateFormat formatter = new SimpleDateFormat(H_FORMAT, Locale.getDefault());
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


    /**
     * date转换为字符串
     *
     * @param date
     * @param format 格式
     * @return
     */
    public static String date2String(Date date, String format) {
        if (null == format) {
            // default format
            format = YMD_HMS_FORMAT;
        }
        SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.getDefault());
        return sdf.format(date);
    }

    /**
     * 字符串转换为date
     *
     * @param date
     * @param format
     * @return
     */
    public static Date string2Date(String date, String format) {
        DateFormat inputDf = new SimpleDateFormat(format, Locale.getDefault());

        Date result = null;
        try {
            result = inputDf.parse(date);
        } catch (ParseException e) {
        }
        return result;
    }


}

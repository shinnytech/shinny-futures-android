package com.shinnytech.futures.utils;

import java.math.BigDecimal;

/**
 * date: 1/10/17
 * author: chenli
 * description: 基本数学运算帮助类
 * version:
 * state: done
 */
public class MathUtils {
    //默认除法运算精度
    private static final int DEFAULT_DIV_SCALE = 10;

    public static double add(double v1, double v2) {
        try {
            BigDecimal b1 = new BigDecimal(Double.toString(v1));
            BigDecimal b2 = new BigDecimal(Double.toString(v2));
            return b1.add(b2).doubleValue();
        } catch (Exception e) {
            return 0.0;
        }
    }

    public static String add(String v1, String v2) {
        try {
            BigDecimal b1 = new BigDecimal(v1);
            BigDecimal b2 = new BigDecimal(v2);
            return b1.add(b2).toString();
        } catch (Exception e) {
            return "-";
        }
    }

    public static double subtract(double v1, double v2) {
        try {
            BigDecimal b1 = new BigDecimal(Double.toString(v1));
            BigDecimal b2 = new BigDecimal(Double.toString(v2));
            return b1.subtract(b2).doubleValue();
        } catch (Exception e) {
            return 0.0;
        }
    }

    public static String subtract(String v1, String v2) {
        try {
            BigDecimal b1 = new BigDecimal(v1);
            BigDecimal b2 = new BigDecimal(v2);
            return b1.subtract(b2).toString();
        } catch (Exception e) {
            return "-";
        }
    }

    public static double multiply(double v1, double v2) {
        try {
            BigDecimal b1 = new BigDecimal(Double.toString(v1));
            BigDecimal b2 = new BigDecimal(Double.toString(v2));
            return b1.multiply(b2).doubleValue();
        } catch (Exception e) {
            return 0.0;
        }
    }

    public static String multiply(String v1, String v2) {
        try {
            BigDecimal b1 = new BigDecimal(v1);
            BigDecimal b2 = new BigDecimal(v2);
            return b1.multiply(b2).toString();
        } catch (Exception e) {
            return "-";
        }
    }

    public static double divide(double v1, double v2) {
        return divide(v1, v2, DEFAULT_DIV_SCALE);
    }

    public static double divide(double v1, double v2, int scale) {
        return divide(v1, v2, scale, BigDecimal.ROUND_HALF_EVEN);
    }

    public static double divide(double v1, double v2, int scale, int round_mode) {
        try {
            BigDecimal b1 = new BigDecimal(Double.toString(v1));
            BigDecimal b2 = new BigDecimal(Double.toString(v2));
            return b1.divide(b2, scale, round_mode).doubleValue();
        } catch (Exception e) {
            return 0.0;
        }

    }

    public static String divide(String v1, String v2) {
        try {
            return divide(v1, v2, DEFAULT_DIV_SCALE);
        } catch (Exception e) {
            return "-";
        }
    }

    public static String divide(String v1, String v2, int scale) {
        try {
            return divide(v1, v2, scale, BigDecimal.ROUND_HALF_EVEN);
        } catch (Exception e) {
            return "-";
        }
    }

    public static String divide(String v1, String v2, int scale, int round_mode) {
        try {
            BigDecimal b1 = new BigDecimal(v1);
            BigDecimal b2 = new BigDecimal(v2);
            return b1.divide(b2, scale, round_mode).toString();
        } catch (Exception e) {
            return "-";
        }
    }


    public static double round(double v, int scale) {
        return round(v, scale, BigDecimal.ROUND_HALF_EVEN);
    }

    public static double round(double v, int scale, int round_mode) {
        try {
            BigDecimal b = new BigDecimal(Double.toString(v));
            return b.setScale(scale, round_mode).doubleValue();
        } catch (Exception e) {
            return 0.0;
        }
    }

    public static String round(String v, int scale) {
        try {
            return round(v, scale, BigDecimal.ROUND_HALF_EVEN);
        } catch (Exception e) {
            return "-";
        }

    }

    public static String round(String v, int scale, int round_mode) {
        try {
            BigDecimal b = new BigDecimal(v);
            return b.setScale(scale, round_mode).toString();
        } catch (Exception e) {
            return "-";
        }
    }

    /**
     * 使用java正则表达式去掉多余的.与0
     */
    public static String subZeroAndDot(String s) {
        try {
            if (s.indexOf(".") > 0) {
                s = s.replaceAll("0+?$", "");//去掉多余的0
                s = s.replaceAll("[.]$", "");//如最后一位是.则去掉
            }
            return s;
        } catch (Exception e) {
            return s;
        }

    }

}
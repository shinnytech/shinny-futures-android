package com.shinnytech.futures.utils;

/**
 * Created on 12/4/17.
 * Created by chenli.
 * Description: .
 */

public class TDUtils {
    public static boolean isVisitor(String name, String password){
        if (name.matches("^游客_[0-9]{8}$") && password.matches("^游客_[0-9]{8}$"))return true;
        else return false;
    }
}

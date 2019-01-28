package com.shinnytech.futures.utils;

import android.content.Context;
import android.widget.Toast;

/**
 * date: 5/31/17
 * author: chenli
 * description: Toast帮助类
 * version:
 * state: done
 */
public class ToastNotificationUtils {

    private static Toast toast;

    public static void showToast(Context context,
                                 String content) {
        if (toast != null){
            toast.cancel();
        }
        toast = Toast.makeText(context, content, Toast.LENGTH_SHORT);
        toast.show();
    }
}

package com.xinyi.shinnyfutures.utils;

import android.app.NotificationManager;
import android.content.Context;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;

import com.xinyi.shinnyfutures.R;
import com.xinyi.shinnyfutures.application.BaseApplicationLike;
import com.xinyi.shinnyfutures.constants.CommonConstants;

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
        if (toast == null) {
            toast = Toast.makeText(context,
                    content,
                    Toast.LENGTH_SHORT);
        } else {
            toast.setText(content);
        }
        toast.show();
    }
}

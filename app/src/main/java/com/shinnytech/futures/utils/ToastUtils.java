package com.shinnytech.futures.utils;

import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.shinnytech.futures.R;

/**
 * date: 5/31/17
 * author: chenli
 * description: Toast帮助类
 * version:
 * state: done
 */
public class ToastUtils {

    private static Toast toast;

    public static void showToast(Context context,
                                 String content) {
        if (toast != null) {
            toast.cancel();
        }
        toast = Toast.makeText(context, content, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.TOP, 0, 0);
        View toastView = toast.getView();
        TextView toastMessage = toastView.findViewById(android.R.id.message);
        toastMessage.setTextSize(15);
        toastMessage.setTextColor(context.getResources().getColor(R.color.white));
        toastMessage.setGravity(Gravity.TOP);
        toastView.setBackground(context.getResources().getDrawable(R.drawable.application_toast_background));
        toast.show();
    }
}

package com.shinnytech.futures.utils;

import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.shinnytech.futures.R;

import static com.shinnytech.futures.constants.CommonConstants.LOGIN_FAIL;

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
        View toastView = View.inflate(context, R.layout.view_toast, null);
        TextView text = toastView.findViewById(R.id.toast);
        text.setText(content);
        toast = Toast.makeText(context, content, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.TOP, 0, 10);
        toast.setView(toastView);
        toast.show();
    }
}

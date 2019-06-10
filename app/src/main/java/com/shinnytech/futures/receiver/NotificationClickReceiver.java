package com.shinnytech.futures.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.shinnytech.futures.utils.SystemUtils;

public class NotificationClickReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        SystemUtils.setTopApp(context);
    }
}

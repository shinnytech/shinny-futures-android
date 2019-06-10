package com.shinnytech.futures.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;

import com.shinnytech.futures.application.BaseApplication;
import com.shinnytech.futures.utils.NetworkUtils;

/**
 * date: 7/9/17
 * author: chenli
 * description: 网络状态监听广播
 * version:
 * state:
 */
public class NetworkReceiver extends BroadcastReceiver {
    public static final String NETWORK_STATE = "com.text.android.network.state"; // An action name
    private static final String ACTION_BOOT = "android.intent.action.BOOT_COMPLETED";
    /**
     * 当前处于的网络
     * 0 ：null
     * 1 ：有网
     */
    private int mNetworkStatus;

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action.equals(ConnectivityManager.CONNECTIVITY_ACTION) || action.equals(ACTION_BOOT)) {
            if (!NetworkUtils.isNetworkConnected(BaseApplication.getContext())) {
                mNetworkStatus = 0;
            } else {
                mNetworkStatus = 1;
            }
            Intent intent1 = new Intent();
            intent1.putExtra("networkStatus", mNetworkStatus);
            intent1.setAction(NETWORK_STATE);
            BaseApplication.getContext().sendBroadcast(intent1);
        }
    }
}

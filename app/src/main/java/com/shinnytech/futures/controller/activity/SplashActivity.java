package com.shinnytech.futures.controller.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;

import com.shinnytech.futures.R;
import com.shinnytech.futures.application.BaseApplication;
import com.shinnytech.futures.constants.CommonConstants;
import com.shinnytech.futures.model.engine.DataManager;
import com.shinnytech.futures.utils.SPUtils;

import static com.shinnytech.futures.constants.CommonConstants.TD_MESSAGE_LOGIN;
import static com.shinnytech.futures.constants.CommonConstants.TD_MESSAGE_SETTLEMENT;
import static com.shinnytech.futures.model.service.WebSocketService.TD_BROADCAST_ACTION;

public class SplashActivity extends AppCompatActivity {
    private BroadcastReceiver mReceiverTransaction;
    private BroadcastReceiver mReceiverLogin;
    private boolean isToMainActivity = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        final Context context = BaseApplication.getContext();

        //新用户
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!SPUtils.contains(context, CommonConstants.CONFIG_LOGIN_DATE)) {
                    Intent loginIntent = new Intent(SplashActivity.this, LoginActivity.class);
                    SplashActivity.this.startActivity(loginIntent);
                    SplashActivity.this.finish();
                } else {
                    String date = (String) SPUtils.get(context, CommonConstants.CONFIG_LOGIN_DATE, "");
                    if (date.isEmpty()) {
                        Intent loginIntent = new Intent(SplashActivity.this, LoginActivity.class);
                        SplashActivity.this.startActivity(loginIntent);
                        SplashActivity.this.finish();
                    } else {
                        //ws连接超时判断
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                if (DataManager.getInstance().USER_ID.isEmpty()) {
                                    Intent loginIntent = new Intent(SplashActivity.this, LoginActivity.class);
                                    SplashActivity.this.startActivity(loginIntent);
                                    SplashActivity.this.finish();
                                }
                            }
                        }, 7000);
                    }
                }
            }
        }, 2000);


    }

    @Override
    protected void onResume() {
        super.onResume();
        registerBroaderCast();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mReceiverTransaction != null)
            LocalBroadcastManager.getInstance(this).unregisterReceiver(mReceiverTransaction);
        if (mReceiverLogin != null)
            LocalBroadcastManager.getInstance(this).unregisterReceiver(mReceiverLogin);
    }

    /**
     * date: 2019/4/24
     * author: chenli
     * description: 监听结算单弹出、登录成功事件
     */
    private void registerBroaderCast() {
        //交易服务器断线重连广播
        mReceiverTransaction = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String mDataString = intent.getStringExtra("msg");
                switch (mDataString) {
                    case TD_MESSAGE_SETTLEMENT:
                        isToMainActivity = false;
                        Intent intent1 = new Intent(context, ConfirmActivity.class);
                        startActivity(intent1);
                        SplashActivity.this.finish();
                        break;
                    default:
                        break;
                }
            }
        };
        LocalBroadcastManager.getInstance(this).registerReceiver(mReceiverTransaction, new IntentFilter(TD_BROADCAST_ACTION));

        mReceiverLogin = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String msg = intent.getStringExtra("msg");
                switch (msg) {
                    case TD_MESSAGE_LOGIN:
                        //登录成功
                        if (isToMainActivity) {
                            Intent mainIntent = new Intent(SplashActivity.this, MainActivity.class);
                            SplashActivity.this.startActivity(mainIntent);
                            SplashActivity.this.finish();
                        }
                        break;
                    default:
                        break;
                }
            }
        };
        LocalBroadcastManager.getInstance(this).registerReceiver(mReceiverLogin, new IntentFilter(TD_BROADCAST_ACTION));

    }
}

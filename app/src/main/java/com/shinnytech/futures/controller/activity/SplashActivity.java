package com.shinnytech.futures.controller.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;

import com.shinnytech.futures.R;
import com.shinnytech.futures.application.BaseApplication;
import com.shinnytech.futures.constants.CommonConstants;
import com.shinnytech.futures.utils.SPUtils;

import java.lang.ref.WeakReference;
import java.util.Timer;
import java.util.TimerTask;

import static com.shinnytech.futures.constants.CommonConstants.TD_MESSAGE_LOGIN_FAIL;
import static com.shinnytech.futures.constants.CommonConstants.TD_MESSAGE_LOGIN_SUCCEED;
import static com.shinnytech.futures.constants.CommonConstants.TD_OFFLINE;
import static com.shinnytech.futures.model.service.WebSocketService.TD_BROADCAST_ACTION;

public class SplashActivity extends AppCompatActivity {
    private BroadcastReceiver mReceiverLogin;
    private Handler mHandler;
    private Timer mTimer;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        mHandler = new MyHandler(this);
        mTimer = new Timer();
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                mHandler.sendEmptyMessage(0);
            }
        };
        mTimer.schedule(timerTask, 10000);

        final Context context = BaseApplication.getContext();
        //没有登录过
        if (!SPUtils.contains(context, CommonConstants.CONFIG_LOGIN_DATE)) {
            mHandler.sendEmptyMessageDelayed(0, 2000);
        } else {
            //关闭软件前退出登录
            String date = (String) SPUtils.get(context, CommonConstants.CONFIG_LOGIN_DATE, "");
            if (date.isEmpty()) mHandler.sendEmptyMessageDelayed(0, 2000);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerBroaderCast();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mReceiverLogin != null)
            LocalBroadcastManager.getInstance(this).unregisterReceiver(mReceiverLogin);
    }

    /**
     * date: 2019/4/24
     * author: chenli
     * description: 监听结算单弹出、登录成功事件
     */
    private void registerBroaderCast() {

        mReceiverLogin = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String msg = intent.getStringExtra("msg");
                switch (msg) {
                    case TD_MESSAGE_LOGIN_SUCCEED:
                        //登录成功
                        toMain();
                        break;
                    case TD_MESSAGE_LOGIN_FAIL:
                        //登录失败
                        toLogin();
                        break;
                    case TD_OFFLINE:
                        //超时检测，连接失败
                        toLogin();
                        break;
                    default:
                        break;
                }
            }
        };
        LocalBroadcastManager.getInstance(this).registerReceiver(mReceiverLogin, new IntentFilter(TD_BROADCAST_ACTION));

    }

    /**
     * date: 6/1/18
     * author: chenli
     * description: 点击登录后服务器返回处理
     * version:
     * state:
     */
    static class MyHandler extends Handler {
        WeakReference<SplashActivity> mActivityReference;

        MyHandler(SplashActivity activity) {
            mActivityReference = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            final SplashActivity activity = mActivityReference.get();
            if (activity != null) {
                switch (msg.what) {
                    case 0:
                        activity.toLogin();
                        break;
                    default:
                        break;
                }
            }
        }
    }

    private void toLogin(){
        mTimer.cancel();
        Intent loginIntent = new Intent(SplashActivity.this, LoginActivity.class);
        SplashActivity.this.startActivity(loginIntent);
        SplashActivity.this.finish();
    }

    private void toMain(){
        mTimer.cancel();
        Intent mainIntent = new Intent(SplashActivity.this, MainActivity.class);
        SplashActivity.this.startActivity(mainIntent);
        SplashActivity.this.finish();
    }
}

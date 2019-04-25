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
import com.shinnytech.futures.model.engine.DataManager;
import com.shinnytech.futures.utils.LogUtils;
import com.shinnytech.futures.utils.NetworkUtils;
import com.shinnytech.futures.utils.ToastNotificationUtils;

import static com.shinnytech.futures.constants.CommonConstants.MD_OFFLINE;
import static com.shinnytech.futures.constants.CommonConstants.MD_ONLINE;
import static com.shinnytech.futures.constants.CommonConstants.TD_MESSAGE_SETTLEMENT;
import static com.shinnytech.futures.constants.CommonConstants.TD_OFFLINE;
import static com.shinnytech.futures.constants.CommonConstants.TD_ONLINE;
import static com.shinnytech.futures.model.receiver.NetworkReceiver.NETWORK_STATE;
import static com.shinnytech.futures.model.service.WebSocketService.MD_BROADCAST_ACTION;
import static com.shinnytech.futures.model.service.WebSocketService.TD_BROADCAST_ACTION;

public class SplashActivity extends AppCompatActivity {
    private BroadcastReceiver mReceiverTransaction;
    private boolean isToMainActivity = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                /* Create an Intent that will start the Menu-Activity. */
                if (DataManager.getInstance().USER_ID.isEmpty()){
                    Intent mainIntent = new Intent(SplashActivity.this, LoginActivity.class);
                    SplashActivity.this.startActivity(mainIntent);
                }else if (isToMainActivity){
                    Intent mainIntent = new Intent(SplashActivity.this, MainActivity.class);
                    SplashActivity.this.startActivity(mainIntent);
                }

                SplashActivity.this.finish();
            }
        }, 3000);
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
    }

    /**
     * date: 2019/4/24
     * author: chenli
     * description: 监听结算单弹出事件
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
                        break;
                    default:
                        break;
                }
            }
        };
        LocalBroadcastManager.getInstance(this).registerReceiver(mReceiverTransaction, new IntentFilter(TD_BROADCAST_ACTION));
    }
}

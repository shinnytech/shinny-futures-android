package com.shinnytech.futures.controller.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;

import com.shinnytech.futures.R;
import com.shinnytech.futures.application.BaseApplication;
import com.shinnytech.futures.utils.NetworkUtils;
import com.shinnytech.futures.utils.SPUtils;
import com.shinnytech.futures.utils.SystemUtils;
import com.shinnytech.futures.utils.ToastUtils;

import java.lang.ref.WeakReference;
import java.util.Timer;
import java.util.TimerTask;

import static com.shinnytech.futures.application.BaseApplication.TD_BROADCAST_ACTION;
import static com.shinnytech.futures.constants.BroadcastConstants.TD_MESSAGE_LOGIN_TIMEOUT;
import static com.shinnytech.futures.constants.SettingConstants.CONFIG_IS_FIRM;
import static com.shinnytech.futures.constants.BroadcastConstants.TD_MESSAGE_LOGIN_FAIL;
import static com.shinnytech.futures.constants.BroadcastConstants.TD_MESSAGE_LOGIN_SUCCEED;
import static com.shinnytech.futures.utils.ScreenUtils.getStatusBarHeight;

public class SplashActivity extends AppCompatActivity {
    private static final int TIME_OUT = 1;
    private static final int EXIT_APP = 2;
    private BroadcastReceiver mReceiverLogin;
    private Handler mHandler;
    private Timer mTimer;
    private Context sContext;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        boolean isFirm = (boolean) SPUtils.get(BaseApplication.getContext(), CONFIG_IS_FIRM, true);
        changeStatusBarColor(isFirm);

        if (isTaskRoot()) {
            mHandler = new MyHandler(this);
            mTimer = new Timer();
            sContext = BaseApplication.getContext();
            mTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    mHandler.sendEmptyMessage(TIME_OUT);
                }
            }, 20000);
        } else finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkNetwork();
        registerBroaderCast();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mReceiverLogin != null)
            LocalBroadcastManager.getInstance(this).unregisterReceiver(mReceiverLogin);
    }

    /**
     * date: 7/7/17
     * author: chenli
     * description: 检查网络的状态
     */
    public void checkNetwork() {
        if (!NetworkUtils.isNetworkConnected(sContext)) {
            if (mTimer != null) {
                mTimer.cancel();
                mTimer = null;
            }
            AlertDialog.Builder dialog = new AlertDialog.Builder(this);
            dialog.setTitle("登录结果");
            dialog.setMessage("网络故障，无法连接到服务器");
            dialog.setCancelable(false);
            dialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    mHandler.sendEmptyMessage(EXIT_APP);
                }
            });
            dialog.show();
        }
    }

    private void changeStatusBarColor(boolean isFirm) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            Window w = getWindow();
            w.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            int statusBarHeight = getStatusBarHeight(BaseApplication.getContext());

            View view = new View(this);
            view.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            view.getLayoutParams().height = statusBarHeight;
            ((ViewGroup) w.getDecorView()).addView(view);
            if (isFirm) view.setBackground(getResources().getDrawable(R.color.colorPrimaryDark));
            else view.setBackground(getResources().getDrawable(R.color.login_simulation_hint));

        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();

            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);

            if (isFirm)
                window.setStatusBarColor(ContextCompat.getColor(this, R.color.colorPrimaryDark));
            else
                window.setStatusBarColor(ContextCompat.getColor(this, R.color.login_simulation_hint));
        }
    }

    /**
     * date: 2019/4/24
     * author: chenli
     * description: 监听登录成功、失败事件
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
                    case TD_MESSAGE_LOGIN_TIMEOUT:
                        //登录超时
                        toLogin();
                        break;
                    default:
                        break;
                }
            }
        };
        LocalBroadcastManager.getInstance(this).registerReceiver(mReceiverLogin, new IntentFilter(TD_BROADCAST_ACTION));

    }

    private void toLogin() {
        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
        }
        Intent loginIntent = new Intent(SplashActivity.this, LoginActivity.class);
        SplashActivity.this.startActivity(loginIntent);
        SplashActivity.this.finish();
    }

    private void toMain() {
        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
        }

        Intent mainIntent = new Intent(SplashActivity.this, MainActivity.class);
        SplashActivity.this.startActivity(mainIntent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        SplashActivity.this.finish();
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
                    case 1:
                        if (BaseApplication.getmTDWebSocket().isOpen()){
                            ToastUtils.showToast(activity.sContext, "登录失败，请重新登录");
                            activity.toLogin();
                        }
                        else {
                            ToastUtils.showToast(activity.sContext, "无法连接到服务器，请尝试重新打开");
                            activity.mHandler.sendEmptyMessageDelayed(EXIT_APP, 2000);
                        }
                        break;
                    case 2:
                        SystemUtils.exitApp(activity);
                        break;
                    default:
                        break;
                }
            }
        }
    }
}

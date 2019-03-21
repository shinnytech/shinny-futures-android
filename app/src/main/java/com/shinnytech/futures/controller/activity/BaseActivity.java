package com.shinnytech.futures.controller.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.TextView;

import com.shinnytech.futures.R;
import com.shinnytech.futures.application.BaseApplication;
import com.shinnytech.futures.model.engine.DataManager;
import com.shinnytech.futures.utils.NetworkUtils;
import com.umeng.analytics.MobclickAgent;

import static com.shinnytech.futures.constants.CommonConstants.OFFLINE;
import static com.shinnytech.futures.constants.CommonConstants.TD_MESSAGE;
import static com.shinnytech.futures.model.receiver.NetworkReceiver.NETWORK_STATE;
import static com.shinnytech.futures.model.service.WebSocketService.TD_BROADCAST_ACTION;

/**
 * date: 7/7/17
 * author: chenli
 * description: 活动基类，用于设置toolbar
 * version:
 * state: done
 */
public abstract class BaseActivity extends AppCompatActivity {
    protected Toolbar mToolbar;
    protected TextView mToolbarTitle;
    protected int mLayoutID = R.layout.view_toolbar;
    protected String mTitle;
    protected ViewDataBinding mViewDataBinding;
    protected Context sContext;
    protected BroadcastReceiver mReceiverLocal;
    protected BroadcastReceiver mReceiverNetwork;
    protected DataManager sDataManager;


    /**
     * date: 7/7/17
     * author: chenli
     * description: 注册账户广播，监听账户实时信息
     */
    protected void registerBroaderCast() {
        mReceiverNetwork = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                int networkStatus = intent.getIntExtra("networkStatus", 0);
                switch (networkStatus) {
                    case 0:
                        mToolbar.setBackgroundColor(ContextCompat.getColor(context, R.color.off_line));
                        mToolbarTitle.setTextColor(Color.BLACK);
                        mToolbarTitle.setText(OFFLINE);
                        mToolbarTitle.setTextSize(20);
                        break;
                    case 1:
                        mToolbar.setBackgroundColor(ContextCompat.getColor(context, R.color.black_dark));
                        mToolbarTitle.setTextColor(Color.WHITE);
                        mToolbarTitle.setText(mTitle);
                        mToolbarTitle.setTextSize(25);
                        break;
                    default:
                        break;
                }
            }
        };
        registerReceiver(mReceiverNetwork, new IntentFilter(NETWORK_STATE));

        mReceiverLocal = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String msg = intent.getStringExtra("msg");
                if (TD_MESSAGE.equals(msg)) refreshUI();
            }
        };
        LocalBroadcastManager.getInstance(this).registerReceiver(mReceiverLocal, new IntentFilter(TD_BROADCAST_ACTION));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mViewDataBinding = DataBindingUtil.setContentView(this, mLayoutID);
        mToolbar = findViewById(R.id.toolbar);
        mToolbarTitle = findViewById(R.id.title_toolbar);
        mToolbar.setTitle("");
        mToolbarTitle.setText(mTitle);
        setSupportActionBar(mToolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
        }
        sContext = BaseApplication.getContext();
        sDataManager = DataManager.getInstance();
        initData();
        initEvent();
        updateToolbarFromNetwork(sContext, mTitle);
    }

    protected void onResume() {
        super.onResume();
        MobclickAgent.onResume(this);
        refreshUI();
        registerBroaderCast();
    }

    protected void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
        if (mReceiverLocal != null)
            LocalBroadcastManager.getInstance(this).unregisterReceiver(mReceiverLocal);
        if (mReceiverNetwork != null) unregisterReceiver(mReceiverNetwork);
    }

    /**
     * date: 7/9/17
     * author: chenli
     * description: 检查网络状态，更新toolbar的显示
     */
    protected void updateToolbarFromNetwork(Context context, String title) {
        if (NetworkUtils.isNetworkConnected(context)) {
            mToolbar.setBackgroundColor(ContextCompat.getColor(context, R.color.black_dark));
            mToolbarTitle.setTextColor(Color.WHITE);
            mToolbarTitle.setText(title);
            mToolbarTitle.setTextSize(25);
        } else {
            mToolbar.setBackgroundColor(ContextCompat.getColor(context, R.color.off_line));
            mToolbarTitle.setTextColor(Color.BLACK);
            mToolbarTitle.setText(OFFLINE);
            mToolbarTitle.setTextSize(20);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case android.R.id.home:
                finish();
        }
        return super.onOptionsItemSelected(item);
    }

    protected abstract void initData();

    protected abstract void initEvent();

    protected abstract void refreshUI();

}
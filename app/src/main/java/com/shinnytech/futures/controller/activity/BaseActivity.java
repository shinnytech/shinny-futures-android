package com.shinnytech.futures.controller.activity;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.TextView;

import com.shinnytech.futures.R;
import com.shinnytech.futures.utils.NetworkUtils;
import com.umeng.analytics.MobclickAgent;

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
        initData();
        initEvent();
    }

    protected void onResume() {
        super.onResume();
        MobclickAgent.onResume(this);
    }

    protected void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
    }

    /**
     * date: 7/9/17
     * author: chenli
     * description: 检查网络状态，更新toolbar的显示
     */
    public void updateToolbarFromNetwork(Context context, String title) {
        if (NetworkUtils.isNetworkConnected(context)) {
            mToolbar.setBackgroundColor(ContextCompat.getColor(context, R.color.black_dark));
            mToolbarTitle.setTextColor(Color.WHITE);
            mToolbarTitle.setText(title);
            mToolbarTitle.setTextSize(25);
        } else {
            mToolbar.setBackgroundColor(ContextCompat.getColor(context, R.color.off_line));
            mToolbarTitle.setTextColor(Color.BLACK);
            mToolbarTitle.setText("交易、行情网络未连接！");
            mToolbarTitle.setTextSize(20);
        }
    }

    protected abstract void initData();

    protected abstract void initEvent();
}
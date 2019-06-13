package com.shinnytech.futures.controller.activity;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.shinnytech.futures.R;
import com.shinnytech.futures.amplitude.api.Amplitude;
import com.shinnytech.futures.application.BaseApplication;
import com.shinnytech.futures.model.engine.DataManager;
import com.shinnytech.futures.utils.NetworkUtils;
import com.shinnytech.futures.utils.SPUtils;

import org.json.JSONException;
import org.json.JSONObject;

import static com.shinnytech.futures.constants.CommonConstants.AMP_EVENT_CURRENT_PAGE;
import static com.shinnytech.futures.constants.CommonConstants.AMP_EVENT_PAGE_VALUE_ABOUT;
import static com.shinnytech.futures.constants.CommonConstants.AMP_EVENT_PAGE_VALUE_ACCOUNT;
import static com.shinnytech.futures.constants.CommonConstants.AMP_EVENT_PAGE_VALUE_CHANGE_PASSWORD;
import static com.shinnytech.futures.constants.CommonConstants.AMP_EVENT_PAGE_VALUE_FEED_BACK;
import static com.shinnytech.futures.constants.CommonConstants.AMP_EVENT_PAGE_VALUE_FUTURE_INFO;
import static com.shinnytech.futures.constants.CommonConstants.AMP_EVENT_PAGE_VALUE_MAIN;
import static com.shinnytech.futures.constants.CommonConstants.AMP_EVENT_PAGE_VALUE_OPTIONAL_SETTING;
import static com.shinnytech.futures.constants.CommonConstants.AMP_EVENT_PAGE_VALUE_SETTING;
import static com.shinnytech.futures.constants.CommonConstants.AMP_EVENT_PAGE_VALUE_TRANSFER;
import static com.shinnytech.futures.constants.CommonConstants.AMP_EVENT_TARGET_PAGE;
import static com.shinnytech.futures.constants.CommonConstants.AMP_SWITCH_PAGE;
import static com.shinnytech.futures.constants.CommonConstants.CONFIG_IS_FIRM;
import static com.shinnytech.futures.constants.CommonConstants.OFFLINE;
import static com.shinnytech.futures.constants.CommonConstants.TD_MESSAGE;
import static com.shinnytech.futures.service.WebSocketService.TD_BROADCAST_ACTION;

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
    protected DataManager sDataManager;


    /**
     * date: 7/7/17
     * author: chenli
     * description: 注册账户广播，监听账户实时信息
     */
    protected void registerBroaderCast() {

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
        boolean isFirm = (boolean) SPUtils.get(sContext, CONFIG_IS_FIRM, true);
        changeStatusBarColor(isFirm);
    }

    protected void onResume() {
        super.onResume();
        refreshUI();
        registerBroaderCast();
    }

    protected void onPause() {
        super.onPause();
        if (mReceiverLocal != null)
            LocalBroadcastManager.getInstance(this).unregisterReceiver(mReceiverLocal);
    }

    /**
     * date: 7/9/17
     * author: chenli
     * description: 检查网络状态，更新toolbar的显示
     */
    protected void updateToolbarFromNetwork(Context context, String title) {
        if (NetworkUtils.isNetworkConnected(context)) {
            mToolbar.setBackgroundColor(ContextCompat.getColor(context, R.color.toolbar));
            mToolbarTitle.setTextColor(Color.WHITE);
            mToolbarTitle.setText(title);
            mToolbarTitle.setTextSize(25);
        } else {
            mToolbar.setBackgroundColor(ContextCompat.getColor(context, R.color.login_simulation_hint));
            mToolbarTitle.setTextColor(Color.BLACK);
            mToolbarTitle.setText(OFFLINE);
            mToolbarTitle.setTextSize(20);
        }
    }

    protected void changeStatusBarColor(boolean isFirm) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            Window w = getWindow();
            w.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            int statusBarHeight = getStatusBarHeight(this);

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

    private int getStatusBarHeight(Activity context) {
        int result = 0;
        int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = context.getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case android.R.id.home:
                JSONObject jsonObject = new JSONObject();
                switch (mLayoutID) {
                    case R.layout.activity_setting:
                        try {
                            jsonObject.put(AMP_EVENT_CURRENT_PAGE, AMP_EVENT_PAGE_VALUE_SETTING);
                            jsonObject.put(AMP_EVENT_TARGET_PAGE, AMP_EVENT_PAGE_VALUE_MAIN);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        break;
                    case R.layout.activity_optional:
                        try {
                            jsonObject.put(AMP_EVENT_CURRENT_PAGE, AMP_EVENT_PAGE_VALUE_OPTIONAL_SETTING);
                            jsonObject.put(AMP_EVENT_TARGET_PAGE, AMP_EVENT_PAGE_VALUE_MAIN);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        break;
                    case R.layout.activity_account:
                        try {
                            jsonObject.put(AMP_EVENT_CURRENT_PAGE, AMP_EVENT_PAGE_VALUE_ACCOUNT);
                            jsonObject.put(AMP_EVENT_TARGET_PAGE, AMP_EVENT_PAGE_VALUE_MAIN);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        break;
                    case R.layout.activity_change_password:
                        try {
                            jsonObject.put(AMP_EVENT_CURRENT_PAGE, AMP_EVENT_PAGE_VALUE_CHANGE_PASSWORD);
                            jsonObject.put(AMP_EVENT_TARGET_PAGE, AMP_EVENT_PAGE_VALUE_MAIN);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        break;
                    case R.layout.activity_bank_transfer:
                        try {
                            jsonObject.put(AMP_EVENT_CURRENT_PAGE, AMP_EVENT_PAGE_VALUE_TRANSFER);
                            jsonObject.put(AMP_EVENT_TARGET_PAGE, AMP_EVENT_PAGE_VALUE_MAIN);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        break;
                    case R.layout.activity_feed_back:
                        try {
                            jsonObject.put(AMP_EVENT_CURRENT_PAGE, AMP_EVENT_PAGE_VALUE_FEED_BACK);
                            jsonObject.put(AMP_EVENT_TARGET_PAGE, AMP_EVENT_PAGE_VALUE_MAIN);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        break;
                    case R.layout.activity_about:
                        try {
                            jsonObject.put(AMP_EVENT_CURRENT_PAGE, AMP_EVENT_PAGE_VALUE_ABOUT);
                            jsonObject.put(AMP_EVENT_TARGET_PAGE, AMP_EVENT_PAGE_VALUE_MAIN);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        break;
                    case R.layout.activity_future_info:
                        try {
                            jsonObject.put(AMP_EVENT_CURRENT_PAGE, AMP_EVENT_PAGE_VALUE_FUTURE_INFO);
                            jsonObject.put(AMP_EVENT_TARGET_PAGE, AMP_EVENT_PAGE_VALUE_MAIN);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        break;
                    default:
                        break;
                }
                Amplitude.getInstance().logEvent(AMP_SWITCH_PAGE, jsonObject);
                finish();
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        JSONObject jsonObject = new JSONObject();
        switch (mLayoutID) {
            case R.layout.activity_setting:
                try {
                    jsonObject.put(AMP_EVENT_CURRENT_PAGE, AMP_EVENT_PAGE_VALUE_SETTING);
                    jsonObject.put(AMP_EVENT_TARGET_PAGE, AMP_EVENT_PAGE_VALUE_MAIN);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                break;
            case R.layout.activity_optional:
                try {
                    jsonObject.put(AMP_EVENT_CURRENT_PAGE, AMP_EVENT_PAGE_VALUE_OPTIONAL_SETTING);
                    jsonObject.put(AMP_EVENT_TARGET_PAGE, AMP_EVENT_PAGE_VALUE_MAIN);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                break;
            case R.layout.activity_account:
                try {
                    jsonObject.put(AMP_EVENT_CURRENT_PAGE, AMP_EVENT_PAGE_VALUE_ACCOUNT);
                    jsonObject.put(AMP_EVENT_TARGET_PAGE, AMP_EVENT_PAGE_VALUE_MAIN);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                break;
            case R.layout.activity_change_password:
                try {
                    jsonObject.put(AMP_EVENT_CURRENT_PAGE, AMP_EVENT_PAGE_VALUE_CHANGE_PASSWORD);
                    jsonObject.put(AMP_EVENT_TARGET_PAGE, AMP_EVENT_PAGE_VALUE_MAIN);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                break;
            case R.layout.activity_bank_transfer:
                try {
                    jsonObject.put(AMP_EVENT_CURRENT_PAGE, AMP_EVENT_PAGE_VALUE_TRANSFER);
                    jsonObject.put(AMP_EVENT_TARGET_PAGE, AMP_EVENT_PAGE_VALUE_MAIN);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                break;
            case R.layout.activity_feed_back:
                try {
                    jsonObject.put(AMP_EVENT_CURRENT_PAGE, AMP_EVENT_PAGE_VALUE_FEED_BACK);
                    jsonObject.put(AMP_EVENT_TARGET_PAGE, AMP_EVENT_PAGE_VALUE_MAIN);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                break;
            case R.layout.activity_about:
                try {
                    jsonObject.put(AMP_EVENT_CURRENT_PAGE, AMP_EVENT_PAGE_VALUE_ABOUT);
                    jsonObject.put(AMP_EVENT_TARGET_PAGE, AMP_EVENT_PAGE_VALUE_MAIN);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                break;
            case R.layout.activity_future_info:
                try {
                    jsonObject.put(AMP_EVENT_CURRENT_PAGE, AMP_EVENT_PAGE_VALUE_FUTURE_INFO);
                    jsonObject.put(AMP_EVENT_TARGET_PAGE, AMP_EVENT_PAGE_VALUE_MAIN);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                break;
            default:
                break;
        }
        Amplitude.getInstance().logEvent(AMP_SWITCH_PAGE, jsonObject);
        finish();
        return super.onKeyDown(keyCode, event);
    }

    protected abstract void initData();

    protected abstract void initEvent();

    protected abstract void refreshUI();

}
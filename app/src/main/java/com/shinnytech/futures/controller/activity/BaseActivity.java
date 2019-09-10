package com.shinnytech.futures.controller.activity;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.shinnytech.futures.R;
import com.shinnytech.futures.application.BaseApplication;
import com.shinnytech.futures.model.bean.searchinfobean.SearchEntity;
import com.shinnytech.futures.model.engine.DataManager;
import com.shinnytech.futures.model.engine.LatestFileManager;
import com.shinnytech.futures.utils.NetworkUtils;
import com.shinnytech.futures.utils.SPUtils;

import static com.shinnytech.futures.constants.SettingConstants.CONFIG_IS_FIRM;
import static com.shinnytech.futures.constants.CommonConstants.OFFLINE;

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
        Window window = getWindow();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            window.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS,
                    WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            int statusBarHeight = getStatusBarHeight(this);
            View view = new View(this);
            view.setLayoutParams(new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            view.getLayoutParams().height = statusBarHeight;
            ((ViewGroup) window.getDecorView()).addView(view);
            if (isFirm) view.setBackground(getResources().getDrawable(R.color.colorPrimaryDark));
            else view.setBackground(getResources().getDrawable(R.color.login_simulation_hint));

        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
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
                finish();
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK){
            finish();
        }
        return super.onKeyDown(keyCode, event);
    }

    /**
     * date: 2019/1/10
     * author: chenli
     * description: 订阅合约行情
     */
    protected void sendSubscribeQuote(String ins) {
        if (ins.contains("&") && ins.contains(" ")) {
            SearchEntity searchEntity = LatestFileManager.getSearchEntities().get(ins);
            if (searchEntity != null) {
                String leg1_symbol = searchEntity.getLeg1_symbol();
                String leg2_symbol = searchEntity.getLeg2_symbol();
                ins = ins + "," + leg1_symbol + "," + leg2_symbol;
            }
        }
        BaseApplication.getmMDWebSocket().sendSubscribeQuote(ins);
    }

    protected abstract void initData();

    protected abstract void initEvent();

}
package com.shinnytech.futures.controller.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;

import com.shinnytech.futures.BuildConfig;
import com.shinnytech.futures.R;
import com.shinnytech.futures.application.BaseApplication;
import com.shinnytech.futures.controller.MainActivityPresenter;
import com.shinnytech.futures.databinding.ActivityMainDrawerBinding;
import com.shinnytech.futures.model.amplitude.api.Amplitude;
import com.shinnytech.futures.model.amplitude.api.Identify;
import com.shinnytech.futures.model.engine.LatestFileManager;
import com.shinnytech.futures.utils.ToastNotificationUtils;

import static com.shinnytech.futures.constants.CommonConstants.AMP_USER_BROKER_ID;
import static com.shinnytech.futures.constants.CommonConstants.AMP_USER_PACKAGE_ID;
import static com.shinnytech.futures.constants.CommonConstants.AMP_USER_SCREEN_SIZE;
import static com.shinnytech.futures.constants.CommonConstants.DOMINANT;
import static com.shinnytech.futures.constants.CommonConstants.OFFLINE;
import static com.shinnytech.futures.constants.CommonConstants.OPTIONAL;
import static com.shinnytech.futures.constants.CommonConstants.POSITION_MENU_JUMP_TO_FUTURE_INFO_ACTIVITY;
import static com.shinnytech.futures.model.receiver.NetworkReceiver.NETWORK_STATE;

/**
 * date: 6/14/17
 * author: chenli
 * description: 问题1：viewpager会初始化两个fragment，如何让它不初始化屏幕外的fragment？运行过程中有两个fragment在同时运行
 * 问题2：如何合理减小navigationView的宽高，定制菜单字体
 * version:
 * state: basically done
 */

public class MainActivity extends BaseActivity {

    /**
     * 记录系统时间，用于退出时做判断
     */
    private long mExitTime = 0;
    private ActivityMainDrawerBinding mBinding;
    private MainActivityPresenter mMainActivityPresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mLayoutID = R.layout.activity_main_drawer;

        super.onCreate(savedInstanceState);
    }

    @Override
    protected void initData() {
        mBinding = (ActivityMainDrawerBinding) mViewDataBinding;
        mMainActivityPresenter = new MainActivityPresenter(this, sContext, mBinding, mToolbar, mToolbarTitle);
        if (LatestFileManager.getOptionalInsList().isEmpty()){
            mMainActivityPresenter.refreshQuotesNavigation(DOMINANT);
            mTitle = DOMINANT;
        } else{
            mMainActivityPresenter.refreshQuotesNavigation(OPTIONAL);
            mTitle = OPTIONAL;
        }
    }

    @Override
    protected void initEvent() {
        mMainActivityPresenter.registerListeners();
    }

    @Override
    protected void refreshUI() {
    }

    @Override
    protected void onResume() {
        super.onResume();
        mMainActivityPresenter.resetNavigationItem();
    }

    @Override
    protected void registerBroaderCast() {
        mReceiverNetwork = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                int networkStatus = intent.getIntExtra("networkStatus", 0);
                switch (networkStatus) {
                    case 0:
                        if (OFFLINE.equals(mToolbarTitle.getText().toString()))
                            mTitle = mToolbarTitle.getText().toString();
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
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.drawer_right, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.right_navigation) {
            if (mBinding.drawerLayout.isDrawerVisible(GravityCompat.END)) {
                mBinding.drawerLayout.closeDrawer(GravityCompat.END);
            } else {
                mBinding.drawerLayout.openDrawer(GravityCompat.END);
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * 处理返回键逻辑或者使用onBackPressed()
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (mBinding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            mBinding.drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        } else if (mBinding.drawerLayout.isDrawerOpen(GravityCompat.END)) {
            mBinding.drawerLayout.closeDrawer(GravityCompat.END);
            return true;
        } else {
            if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
                if ((System.currentTimeMillis() - mExitTime) > 2000) {
                    ToastNotificationUtils.showToast(BaseApplication.getContext(), getString(R.string.main_activity_exit));
                    mExitTime = System.currentTimeMillis();
                } else {
                    setResult(RESULT_CANCELED);
                    finish();
                }
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    /**
     * date: 6/21/17
     * author: chenli
     * description: 合约详情页返回,发送原来订阅合约
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        //很重要,决定了quoteFragment中的方法能不能被调用
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case POSITION_MENU_JUMP_TO_FUTURE_INFO_ACTIVITY:
                    if (BaseApplication.getWebSocketService() != null)
                        BaseApplication.getWebSocketService().sendSubscribeQuote(mMainActivityPresenter.getPreSubscribedQuotes());
                    break;
                default:
                    break;
            }
        }
    }

}
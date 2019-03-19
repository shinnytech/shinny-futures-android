package com.shinnytech.futures.controller.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.GravityCompat;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;

import com.shinnytech.futures.R;
import com.shinnytech.futures.application.BaseApplication;
import com.shinnytech.futures.constants.CommonConstants;
import com.shinnytech.futures.controller.fragment.QuoteFragment;
import com.shinnytech.futures.databinding.ActivityMainDrawerBinding;
import com.shinnytech.futures.controller.MainActivityPresenter;
import com.shinnytech.futures.model.bean.settingbean.NavigationRightEntity;
import com.shinnytech.futures.model.engine.DataManager;
import com.shinnytech.futures.model.engine.LatestFileManager;
import com.shinnytech.futures.utils.LogUtils;
import com.shinnytech.futures.utils.ToastNotificationUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import static com.shinnytech.futures.constants.CommonConstants.DOMINANT;
import static com.shinnytech.futures.constants.CommonConstants.LOGIN;
import static com.shinnytech.futures.constants.CommonConstants.LOGIN_JUMP_TO_LOG_IN_ACTIVITY;
import static com.shinnytech.futures.constants.CommonConstants.LOGOUT;
import static com.shinnytech.futures.constants.CommonConstants.OFFLINE;
import static com.shinnytech.futures.constants.CommonConstants.OPTIONAL;
import static com.shinnytech.futures.constants.CommonConstants.POSITION_MENU_JUMP_TO_FUTURE_INFO_ACTIVITY;
import static com.shinnytech.futures.constants.CommonConstants.TD_MESSAGE_BROKER_INFO;
import static com.shinnytech.futures.constants.CommonConstants.TD_MESSAGE_LOGIN;
import static com.shinnytech.futures.constants.CommonConstants.TD_OFFLINE;
import static com.shinnytech.futures.model.receiver.NetworkReceiver.NETWORK_STATE;
import static com.shinnytech.futures.model.service.WebSocketService.TD_BROADCAST_ACTION;

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
    private BroadcastReceiver mReceiverLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mLayoutID = R.layout.activity_main_drawer;
        if (LatestFileManager.getOptionalInsList().isEmpty())
            mTitle = DOMINANT;
        else mTitle = OPTIONAL;
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
    }

    @Override
    protected void initData() {
        mBinding = (ActivityMainDrawerBinding) mViewDataBinding;
        mMainActivityPresenter = new MainActivityPresenter(this, sContext, mBinding, mToolbar, mToolbarTitle);
        //检查是否第一次启动APP,弹出免责条款
        mMainActivityPresenter.checkResponsibility();
        //首先检查网络状态，网络断开就直接退出应用
        mMainActivityPresenter.checkNetwork();
    }

    @Override
    protected void initEvent() {
        mMainActivityPresenter.registerListeners();
    }

    @Override
    protected void refreshUI() {
    }

    //开机合约列表解析完毕刷新主力导航
    @Subscribe
    public void onEvent(String msg) {
        if (DOMINANT.equals(msg)) {
            if (LatestFileManager.getOptionalInsList().isEmpty())
                mMainActivityPresenter.refreshQuotesNavigation(DOMINANT);
            else mMainActivityPresenter.refreshQuotesNavigation(OPTIONAL);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mMainActivityPresenter.resetNavigationItem();
        //合约详情页登录切回主页刷新右导航栏
        if (sDataManager.IS_LOGIN){
            mMainActivityPresenter.mNavigationRightAdapter.updateItem(0, CommonConstants.LOGOUT);
            mMainActivityPresenter.mNavigationRightAdapter.addItem(3);
        }else {
            mMainActivityPresenter.mNavigationRightAdapter.updateItem(0, CommonConstants.LOGIN);
            mMainActivityPresenter.mNavigationRightAdapter.removeItem(3);
        }
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

        mReceiverLocal = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String msg = intent.getStringExtra("msg");
                if (TD_OFFLINE.equals(msg))refreshMenu(false);
            }
        };
        LocalBroadcastManager.getInstance(this).registerReceiver(mReceiverLocal, new IntentFilter(TD_BROADCAST_ACTION));

        mReceiverLogin = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String msg = intent.getStringExtra("msg");
                switch (msg) {
                    case TD_MESSAGE_LOGIN:refreshMenu(true);
                        break;
                    default:
                        break;
                }
            }
        };
        LocalBroadcastManager.getInstance(this).registerReceiver(mReceiverLogin, new IntentFilter(TD_BROADCAST_ACTION));
    }

    /**
     * date: 2019/3/17
     * author: chenli
     * description: 断线刷新右导航
     */
    private void refreshMenu(boolean isLogin) {
        DataManager.getInstance().IS_LOGIN = isLogin;
        if (!isLogin){
            mMainActivityPresenter.mNavigationRightAdapter.updateItem(0, CommonConstants.LOGIN);
            mMainActivityPresenter.mNavigationRightAdapter.removeItem(3);
        }else {
            mMainActivityPresenter.mNavigationRightAdapter.updateItem(0, CommonConstants.LOGOUT);
            mMainActivityPresenter.mNavigationRightAdapter.addItem(3);
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mReceiverLogin != null)LocalBroadcastManager.getInstance(this).unregisterReceiver(mReceiverLogin);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
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
        } else if (mBinding.drawerLayout.isDrawerOpen(GravityCompat.END)){
            mBinding.drawerLayout.closeDrawer(GravityCompat.END);
            return true;
        }else {
            if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
                if ((System.currentTimeMillis() - mExitTime) > 2000) {
                    ToastNotificationUtils.showToast(BaseApplication.getContext(), getString(R.string.main_activity_exit));
                    mExitTime = System.currentTimeMillis();
                } else {
                    this.finish();
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
        if (resultCode == RESULT_OK){
            switch (requestCode){
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
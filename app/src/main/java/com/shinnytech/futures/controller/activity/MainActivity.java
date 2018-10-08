package com.shinnytech.futures.controller.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.GravityCompat;
import android.view.KeyEvent;

import com.shinnytech.futures.R;
import com.shinnytech.futures.application.BaseApplicationLike;
import com.shinnytech.futures.databinding.ActivityMainDrawerBinding;
import com.shinnytech.futures.controller.MainActivityPresenter;
import com.shinnytech.futures.utils.ToastNotificationUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import static com.shinnytech.futures.constants.CommonConstants.DOMINANT;
import static com.shinnytech.futures.constants.CommonConstants.JUMP_TO_FUTURE_INFO_ACTIVITY;

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
    private Context sContext;
    private ActivityMainDrawerBinding mBinding;
    private MainActivityPresenter mMainActivityPresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mLayoutID = R.layout.activity_main_drawer;
        mTitle = DOMINANT;
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
    }

    @Override
    protected void initData() {
        mBinding = (ActivityMainDrawerBinding) mViewDataBinding;
        sContext = BaseApplicationLike.getContext();
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

    //开机合约列表解析完毕刷新主力导航
    @Subscribe
    public void onEvent(String msg) {
        if (DOMINANT.equals(msg)) mMainActivityPresenter.refreshQuotesNavigation(DOMINANT);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mMainActivityPresenter.resetNavigationItem();
        updateToolbarFromNetwork(sContext, mToolbarTitle.getText().toString());
        mMainActivityPresenter.registerBroaderCast();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mMainActivityPresenter.unRegisterBroaderCast();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    /**
     * 处理返回键逻辑或者使用onBackPressed()
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (mBinding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            mBinding.drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        } else {
            if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
                if ((System.currentTimeMillis() - mExitTime) > 2000) {
                    ToastNotificationUtils.showToast(BaseApplicationLike.getContext(), getString(R.string.main_activity_exit));
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
        if (requestCode == JUMP_TO_FUTURE_INFO_ACTIVITY && BaseApplicationLike.getWebSocketService() != null)
            BaseApplicationLike.getWebSocketService().sendSubscribeQuote(mMainActivityPresenter.getPreSubscribedQuotes());
    }

}
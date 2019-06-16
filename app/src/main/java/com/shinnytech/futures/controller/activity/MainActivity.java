package com.shinnytech.futures.controller.activity;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.shinnytech.futures.R;
import com.shinnytech.futures.amplitude.api.Amplitude;
import com.shinnytech.futures.application.BaseApplication;
import com.shinnytech.futures.controller.MainActivityPresenter;
import com.shinnytech.futures.controller.fragment.AccountFragment;
import com.shinnytech.futures.controller.fragment.LazyLoadFragment;
import com.shinnytech.futures.controller.fragment.QuoteFragment;
import com.shinnytech.futures.controller.fragment.QuotePagerFragment;
import com.shinnytech.futures.databinding.ActivityMainDrawerBinding;
import com.shinnytech.futures.model.engine.DataManager;
import com.shinnytech.futures.utils.NetworkUtils;
import com.shinnytech.futures.utils.SPUtils;
import com.shinnytech.futures.utils.ToastUtils;

import org.json.JSONException;
import org.json.JSONObject;

import static com.shinnytech.futures.constants.CommonConstants.AMP_EVENT_CURRENT_PAGE;
import static com.shinnytech.futures.constants.CommonConstants.AMP_EVENT_PAGE_VALUE_MAIN;
import static com.shinnytech.futures.constants.CommonConstants.AMP_EVENT_PAGE_VALUE_SEARCH;
import static com.shinnytech.futures.constants.CommonConstants.AMP_EVENT_TARGET_PAGE;
import static com.shinnytech.futures.constants.CommonConstants.AMP_SWITCH_PAGE;
import static com.shinnytech.futures.constants.CommonConstants.BACK_TO_ACCOUNT_DETAIL;
import static com.shinnytech.futures.constants.CommonConstants.CONFIG_VERSION_CODE;
import static com.shinnytech.futures.constants.CommonConstants.INS_BETWEEN_ACTIVITY;
import static com.shinnytech.futures.constants.CommonConstants.MAIN_ACTIVITY_TO_FUTURE_INFO_ACTIVITY;
import static com.shinnytech.futures.constants.CommonConstants.MAIN_ACTIVITY_TO_OPTIONAL_SETTING_ACTIVITY;
import static com.shinnytech.futures.constants.CommonConstants.MAIN_ACTIVITY_TO_SEARCH_ACTIVITY;
import static com.shinnytech.futures.constants.CommonConstants.MAIN_ACTIVITY_TO_TRANSFER_ACTIVITY;
import static com.shinnytech.futures.constants.CommonConstants.OFFLINE;
import static com.shinnytech.futures.constants.CommonConstants.OPTIONAL;

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
    private boolean mIsInit = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mLayoutID = R.layout.activity_main_drawer;
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void initData() {
        mBinding = (ActivityMainDrawerBinding) mViewDataBinding;
        mTitle = OPTIONAL;
        mMainActivityPresenter = new MainActivityPresenter(this, sContext, mBinding, mTitle, mToolbarTitle);
        checkResponsibility();
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
        if (!mIsInit) {
            int index = mBinding.vpContent.getCurrentItem();
            ((LazyLoadFragment) mMainActivityPresenter.getmViewPagerFragmentAdapter().getItem(index)).show();
        }
    }

    @Override
    protected void updateToolbarFromNetwork(Context context, String title) {
        if (NetworkUtils.isNetworkConnected(sContext)) {
            mToolbar.setBackgroundColor(ContextCompat.getColor(sContext, R.color.toolbar));
            mToolbarTitle.setTextColor(Color.WHITE);
            mToolbarTitle.setText(title);
            mToolbarTitle.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.mipmap.ic_exchange_down, 0);
        } else {
            mToolbar.setBackgroundColor(ContextCompat.getColor(sContext, R.color.login_simulation_hint));
            mToolbarTitle.setTextColor(Color.BLACK);
            mToolbarTitle.setText(OFFLINE);
            mToolbarTitle.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        int index = mBinding.vpContent.getCurrentItem();
        ((LazyLoadFragment) mMainActivityPresenter.getmViewPagerFragmentAdapter().getItem(index)).leave();
        mIsInit = false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main_menu, menu);
        getMenuInflater().inflate(R.menu.search, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.right_navigation:
                if (mBinding.drawerLayout.isDrawerVisible(GravityCompat.END)) {
                    mBinding.drawerLayout.closeDrawer(GravityCompat.END);
                } else {
                    mBinding.drawerLayout.openDrawer(GravityCompat.END);
                }
                return true;
            case R.id.search_quote:
                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put(AMP_EVENT_CURRENT_PAGE, AMP_EVENT_PAGE_VALUE_MAIN);
                    jsonObject.put(AMP_EVENT_TARGET_PAGE, AMP_EVENT_PAGE_VALUE_SEARCH);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                Amplitude.getInstance().logEvent(AMP_SWITCH_PAGE, jsonObject);
                mMainActivityPresenter.setPreSubscribedQuotes(sDataManager.getRtnData().getIns_list());
                Intent intent = new Intent(MainActivity.this, SearchActivity.class);
                startActivityForResult(intent, MAIN_ACTIVITY_TO_SEARCH_ACTIVITY);
                return true;
            default:
                break;
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
                    ToastUtils.showToast(BaseApplication.getContext(), getString(R.string.main_activity_exit));
                    mExitTime = System.currentTimeMillis();
                } else {
                    Intent startMain = new Intent(Intent.ACTION_MAIN);
                    startMain.addCategory(Intent.CATEGORY_HOME);
                    startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(startMain);
                }
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    /**
     * date: 1/16/18
     * author: chenli
     * description: 检查是否第一次启动APP,弹出免责条款框
     */
    public void checkResponsibility() {
        try {
            final float nowVersionCode = DataManager.getInstance().APP_CODE;
            float versionCode = (float) SPUtils.get(sContext, CONFIG_VERSION_CODE, 0.0f);
            if (nowVersionCode > versionCode) {
                final Dialog dialog = new Dialog(this, R.style.responsibilityDialog);
                View view = View.inflate(this, R.layout.view_dialog_responsibility, null);
                dialog.setContentView(view);
                dialog.setCanceledOnTouchOutside(false);
                dialog.setCancelable(false);
                dialog.show();
                view.findViewById(R.id.agree).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        SPUtils.putAndApply(MainActivity.this, CONFIG_VERSION_CODE, nowVersionCode);
                        dialog.dismiss();
                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * date: 6/21/17
     * author: chenli
     * description: 合约详情页返回,发送原来订阅合约:搜索页、合约详情页、自选管理菜单返回刷新行情列表
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        try {
            //交易页的超链接
            if (data != null) {
                boolean isAccountPage = data.getBooleanExtra(BACK_TO_ACCOUNT_DETAIL, false);
                String ins = data.getStringExtra(INS_BETWEEN_ACTIVITY);
                if (isAccountPage) {
                    mBinding.bottomNavigation.setSelectedItemId(R.id.trade);
                    AccountFragment accountFragment = (AccountFragment) mMainActivityPresenter.getmViewPagerFragmentAdapter().getItem(1);
                    if (accountFragment != null && accountFragment.getmBinding().accountFab != null) {
                        accountFragment.getmBinding().accountFab.show();
                        accountFragment.setIns(ins);
                    }

                }
            }

            //二级页、搜索页、自选管理页返回重新订阅行情
            if (requestCode == MAIN_ACTIVITY_TO_SEARCH_ACTIVITY
                    || requestCode == MAIN_ACTIVITY_TO_FUTURE_INFO_ACTIVITY
                    || requestCode == MAIN_ACTIVITY_TO_OPTIONAL_SETTING_ACTIVITY) {
                String mIns = mMainActivityPresenter.getPreSubscribedQuotes();
                QuoteFragment quoteFragment = ((QuotePagerFragment) mMainActivityPresenter.
                        getmViewPagerFragmentAdapter().getItem(0)).getCurrentItem();
                if (quoteFragment != null) quoteFragment.refreshTD();
                if (quoteFragment != null && OPTIONAL.equals(quoteFragment.getTitle())) {
                    quoteFragment.refreshOptional();
                } else if (mIns != null && !mIns.equals(sDataManager.getRtnData().getIns_list())) {
                    BaseApplication.getmMDWebSocket().sendSubscribeQuote(mIns);
                }
            }

            //二级页、银期转帐页刷新账户信息
            if (requestCode == MAIN_ACTIVITY_TO_TRANSFER_ACTIVITY
                    || requestCode == MAIN_ACTIVITY_TO_FUTURE_INFO_ACTIVITY) {
                AccountFragment accountFragment = (AccountFragment) mMainActivityPresenter.
                        getmViewPagerFragmentAdapter().getItem(1);
                if (accountFragment != null) accountFragment.refreshAccount();
            }

            //主页到搜索页的返回
            if (requestCode == MAIN_ACTIVITY_TO_SEARCH_ACTIVITY) {
                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put(AMP_EVENT_CURRENT_PAGE, AMP_EVENT_PAGE_VALUE_SEARCH);
                    jsonObject.put(AMP_EVENT_TARGET_PAGE, AMP_EVENT_PAGE_VALUE_MAIN);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                Amplitude.getInstance().logEvent(AMP_SWITCH_PAGE, jsonObject);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public MainActivityPresenter getmMainActivityPresenter() {
        return mMainActivityPresenter;
    }

    public ActivityMainDrawerBinding getmBinding() {
        return mBinding;
    }
}
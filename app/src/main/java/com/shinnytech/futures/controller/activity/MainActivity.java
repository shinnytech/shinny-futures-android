package com.shinnytech.futures.controller.activity;

import android.Manifest;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.GravityCompat;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.sfit.ctp.info.DeviceInfoManager;
import com.shinnytech.futures.R;
import com.shinnytech.futures.amplitude.api.Amplitude;
import com.shinnytech.futures.application.BaseApplication;
import com.shinnytech.futures.controller.fragment.AccountFragment;
import com.shinnytech.futures.controller.fragment.FutureInfoFragment;
import com.shinnytech.futures.controller.fragment.LazyLoadFragment;
import com.shinnytech.futures.controller.fragment.QuoteFragment;
import com.shinnytech.futures.controller.fragment.QuotePagerFragment;
import com.shinnytech.futures.databinding.ActivityMainDrawerBinding;
import com.shinnytech.futures.model.adapter.KlineDurationTitleAdapter;
import com.shinnytech.futures.model.adapter.ViewPagerFragmentAdapter;
import com.shinnytech.futures.model.bean.eventbusbean.AverageEvent;
import com.shinnytech.futures.model.bean.eventbusbean.SetUpEvent;
import com.shinnytech.futures.model.bean.futureinfobean.QuoteEntity;
import com.shinnytech.futures.model.bean.searchinfobean.SearchEntity;
import com.shinnytech.futures.model.engine.DataManager;
import com.shinnytech.futures.model.engine.LatestFileManager;
import com.shinnytech.futures.utils.Base64;
import com.shinnytech.futures.utils.NetworkUtils;
import com.shinnytech.futures.utils.SPUtils;
import com.shinnytech.futures.utils.SystemUtils;
import com.shinnytech.futures.utils.ToastUtils;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Map;

import static com.shinnytech.futures.application.BaseApplication.MD_BROADCAST_ACTION;
import static com.shinnytech.futures.application.BaseApplication.TD_BROADCAST_ACTION;
import static com.shinnytech.futures.constants.CommonConstants.ACCOUNT_DETAIL;
import static com.shinnytech.futures.constants.CommonConstants.AMP_EVENT_OPTIONAL_DIRECTION;
import static com.shinnytech.futures.constants.CommonConstants.AMP_EVENT_OPTIONAL_DIRECTION_VALUE_ADD;
import static com.shinnytech.futures.constants.CommonConstants.AMP_EVENT_OPTIONAL_DIRECTION_VALUE_DELETE;
import static com.shinnytech.futures.constants.CommonConstants.AMP_EVENT_OPTIONAL_INSTRUMENT_ID;
import static com.shinnytech.futures.constants.CommonConstants.AMP_OPTIONAL_FUTURE_INFO;
import static com.shinnytech.futures.constants.CommonConstants.CONFIG_AVERAGE_LINE;
import static com.shinnytech.futures.constants.CommonConstants.CONFIG_ORDER_LINE;
import static com.shinnytech.futures.constants.CommonConstants.CONFIG_POSITION_LINE;
import static com.shinnytech.futures.constants.CommonConstants.CONFIG_SYSTEM_INFO;
import static com.shinnytech.futures.constants.CommonConstants.CONFIG_VERSION_CODE;
import static com.shinnytech.futures.constants.CommonConstants.FUTURE_INFO_FRAGMENT_TO_CHART_SETTING_ACTIVITY;
import static com.shinnytech.futures.constants.CommonConstants.INS_BETWEEN_ACTIVITY;
import static com.shinnytech.futures.constants.CommonConstants.MAIN_ACTIVITY_TO_OPTIONAL_SETTING_ACTIVITY;
import static com.shinnytech.futures.constants.CommonConstants.MAIN_ACTIVITY_TO_SEARCH_ACTIVITY;
import static com.shinnytech.futures.constants.CommonConstants.MAIN_ACTIVITY_TO_SETTING_ACTIVITY;
import static com.shinnytech.futures.constants.CommonConstants.MAIN_ACTIVITY_TO_TRANSFER_ACTIVITY;
import static com.shinnytech.futures.constants.CommonConstants.MD_MESSAGE;
import static com.shinnytech.futures.constants.CommonConstants.MENU_TITLE_NAVIGATION;
import static com.shinnytech.futures.constants.CommonConstants.OFFLINE;
import static com.shinnytech.futures.constants.CommonConstants.OPTIONAL;
import static com.shinnytech.futures.constants.CommonConstants.TD_MESSAGE;

/**
 * date: 6/14/17
 * author: chenli
 * description: 问题1：viewpager会初始化两个fragment，如何让它不初始化屏幕外的fragment？运行过程中有两个fragment在同时运行
 * 问题2：如何合理减小navigationView的宽高，定制菜单字体
 * version:
 * state: basically done
 */

public class MainActivity extends BaseActivity {
    private static final int MY_PERMISSIONS_REQUEST = 1;
    public Menu menu;
    private long mExitTime = 0;
    private ActivityMainDrawerBinding mBinding;
    private MainActivityPresenter mMainActivityPresenter;
    private BroadcastReceiver mReceiverMarket;
    private BroadcastReceiver mReceiverTrade;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mLayoutID = R.layout.activity_main_drawer;
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void initData() {
        mBinding = (ActivityMainDrawerBinding) mViewDataBinding;
        mMainActivityPresenter = new MainActivityPresenter(this, sContext, mBinding, mToolbarTitle);
    }

    @Override
    protected void initEvent() {
        mMainActivityPresenter.registerListeners();
        checkResponsibility();
        checkPermissions();
        registerBroaderCast();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        String instrument_id = intent.getStringExtra(INS_BETWEEN_ACTIVITY);
        mMainActivityPresenter.switchToFutureInfo(instrument_id);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mReceiverMarket);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mReceiverTrade);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main_menu, menu);
        getMenuInflater().inflate(R.menu.search, menu);
        this.menu = menu;
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.right_navigation:
                String menuTitle = item.getTitle().toString();
                if (MENU_TITLE_NAVIGATION.equals(menuTitle)) {
                    if (mBinding.drawerLayout.isDrawerVisible(GravityCompat.END)) {
                        mBinding.drawerLayout.closeDrawer(GravityCompat.END);
                    } else {
                        mBinding.drawerLayout.openDrawer(GravityCompat.END);
                    }
                } else {
                    try {
                        JSONObject jsonObject = new JSONObject();
                        String mInstrumentId = mMainActivityPresenter.getmInstrumentId();
                        String name = mInstrumentId;
                        SearchEntity searchEntity = LatestFileManager.getSearchEntities().get(name);
                        if (searchEntity != null) name = searchEntity.getInstrumentName();
                        jsonObject.put(AMP_EVENT_OPTIONAL_INSTRUMENT_ID, mInstrumentId);
                        Map<String, QuoteEntity> insList = LatestFileManager.getOptionalInsList();
                        if (insList.containsKey(mInstrumentId)) {
                            jsonObject.put(AMP_EVENT_OPTIONAL_DIRECTION, AMP_EVENT_OPTIONAL_DIRECTION_VALUE_DELETE);
                            insList.remove(mInstrumentId);
                            LatestFileManager.saveInsListToFile(new ArrayList<>(insList.keySet()));
                            ToastUtils.showToast(BaseApplication.getContext(), name + "合约已移除");
                            item.setIcon(R.mipmap.ic_favorite_border_white_24dp);
                        } else {
                            jsonObject.put(AMP_EVENT_OPTIONAL_DIRECTION, AMP_EVENT_OPTIONAL_DIRECTION_VALUE_ADD);
                            QuoteEntity quoteEntity = new QuoteEntity();
                            quoteEntity.setInstrument_id(mInstrumentId);
                            insList.put(mInstrumentId, quoteEntity);
                            LatestFileManager.saveInsListToFile(new ArrayList<>(insList.keySet()));
                            ToastUtils.showToast(BaseApplication.getContext(), name + "合约已添加");
                            item.setIcon(R.mipmap.ic_favorite_white_24dp);
                        }
                        Amplitude.getInstance().logEventWrap(AMP_OPTIONAL_FUTURE_INFO, jsonObject);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                return true;
            case R.id.search_quote:
                mMainActivityPresenter.setPreSubscribedQuotes(sDataManager.getRtnData().getIns_list());
                Intent intent = new Intent(MainActivity.this, SearchActivity.class);
                startActivityForResult(intent, MAIN_ACTIVITY_TO_SEARCH_ACTIVITY);
                return true;
            case android.R.id.home:
                MenuItem menuItem = menu.getItem(1);
                menuItem.setIcon(R.mipmap.ic_menu);
                menuItem.setTitle(MENU_TITLE_NAVIGATION);
                getSupportActionBar().setDisplayHomeAsUpEnabled(false);
                getSupportActionBar().setHomeButtonEnabled(false);
                if (ACCOUNT_DETAIL.equals(mTitle)) mMainActivityPresenter.switchToAccount();
                else mMainActivityPresenter.switchToMarket();
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
        MenuItem menuItem = menu.getItem(1);
        String title = menuItem.getTitle().toString();
        if (MENU_TITLE_NAVIGATION.equals(title)){
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
                        SystemUtils.exitApp(sContext);
                    }
                    return true;
                }
            }
        }else {
            menuItem.setIcon(R.mipmap.ic_menu);
            menuItem.setTitle(MENU_TITLE_NAVIGATION);
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            getSupportActionBar().setHomeButtonEnabled(false);
            if (ACCOUNT_DETAIL.equals(mTitle)) mMainActivityPresenter.switchToAccount();
            else mMainActivityPresenter.switchToMarket();
            return true;
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

    /**
     * date: 2019/4/2
     * author: chenli
     * description: 穿透视监管动态权限检查
     */
    private void checkPermissions() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_PHONE_STATE)
                != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_PHONE_STATE,
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION},
                    MY_PERMISSIONS_REQUEST);
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST:
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 2
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED
                        && grantResults[1] == PackageManager.PERMISSION_GRANTED
                        && grantResults[2] == PackageManager.PERMISSION_GRANTED) {
                    getSystemInfo();
                }
                break;
            default:
                break;

        }
    }

    /**
     * date: 2019/5/30
     * author: chenli
     * description: 穿透式监管信息
     */
    private void getSystemInfo() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    byte[] info = DeviceInfoManager.getCollectInfo(MainActivity.this);
                    String encodeInfo = Base64.encode(info);
                    SPUtils.putAndApply(sContext, CONFIG_SYSTEM_INFO, encodeInfo);
                } catch (Exception e) {
                    SPUtils.putAndApply(sContext, CONFIG_SYSTEM_INFO, "");
                }
            }
        }).start();
    }

    /**
     * date: 2019/4/14
     * author: chenli
     * description: 注册广播
     */
    private void registerBroaderCast() {
        mReceiverMarket = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String mDataString = intent.getStringExtra("msg");
                switch (mDataString) {
                    case MD_MESSAGE:
                        refreshMD();
                        break;
                    default:
                        break;
                }
            }
        };
        LocalBroadcastManager.getInstance(this).registerReceiver(mReceiverMarket, new IntentFilter(MD_BROADCAST_ACTION));

        mReceiverTrade = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String mDataString = intent.getStringExtra("msg");
                switch (mDataString) {
                    case TD_MESSAGE:
                        refreshTD();
                        break;
                    default:
                        break;
                }
            }
        };
        LocalBroadcastManager.getInstance(this).registerReceiver(mReceiverTrade, new IntentFilter(TD_BROADCAST_ACTION));
    }

    /**
     * date: 2019/7/12
     * author: chenli
     * description: 刷新合约行情
     */
    public void refreshMD() {
        if (!mMainActivityPresenter.ismIsUpdate())return;
        int index = mBinding.vpContent.getCurrentItem();
        ViewPagerFragmentAdapter viewPagerFragmentAdapter = mMainActivityPresenter.getmViewPagerFragmentAdapter();
        LazyLoadFragment lazyLoadFragment = (LazyLoadFragment) viewPagerFragmentAdapter.getItem(index);
        lazyLoadFragment.refreshMD();
    }


    /**
     * date: 2019/7/12
     * author: chenli
     * description: 刷新账户信息
     */
    public void refreshTD() {
        if (!mMainActivityPresenter.ismIsUpdate())return;
        int index = mBinding.vpContent.getCurrentItem();
        ViewPagerFragmentAdapter viewPagerFragmentAdapter = mMainActivityPresenter.getmViewPagerFragmentAdapter();
        LazyLoadFragment lazyLoadFragment = (LazyLoadFragment) viewPagerFragmentAdapter.getItem(index);
        lazyLoadFragment.refreshTD();
    }

    /**
     * date: 6/21/17
     * author: chenli
     * description: 合约详情页返回,发送原来订阅合约:搜索页、合约详情页、自选管理菜单返回刷新行情列表
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        try {

            //二级页、搜索页、自选管理页返回重新订阅行情
            if (requestCode == MAIN_ACTIVITY_TO_SEARCH_ACTIVITY
                    || requestCode == MAIN_ACTIVITY_TO_OPTIONAL_SETTING_ACTIVITY) {
                QuoteFragment quoteFragment = ((QuotePagerFragment) mMainActivityPresenter.
                        getmViewPagerFragmentAdapter().getItem(0)).getCurrentItem();
                if (quoteFragment != null) {
                    quoteFragment.refreshTD();
                    if (OPTIONAL.equals(mToolbarTitle.getText().toString()))quoteFragment.show();
                }
            }

            //二级页、银期转帐页刷新账户信息
            if (requestCode == MAIN_ACTIVITY_TO_TRANSFER_ACTIVITY){
                AccountFragment accountFragment = (AccountFragment) mMainActivityPresenter.
                        getmViewPagerFragmentAdapter().getItem(1);
                if (accountFragment != null) accountFragment.refreshTD();
            }

            if (requestCode == FUTURE_INFO_FRAGMENT_TO_CHART_SETTING_ACTIVITY ||
                    requestCode == MAIN_ACTIVITY_TO_SETTING_ACTIVITY) {

                //刷新开关
                boolean mIsPosition = (boolean) SPUtils.get(sContext, CONFIG_POSITION_LINE, true);
                boolean mIsPending = (boolean) SPUtils.get(sContext, CONFIG_ORDER_LINE, true);
                boolean mIsAverage = (boolean) SPUtils.get(sContext, CONFIG_AVERAGE_LINE, true);
                SetUpEvent setUpEvent = new SetUpEvent();
                setUpEvent.setAverage(mIsAverage);
                setUpEvent.setPending(mIsPending);
                setUpEvent.setPosition(mIsPosition);
                EventBus.getDefault().post(setUpEvent);
                //重绘均线
                EventBus.getDefault().post(new AverageEvent());
                FutureInfoFragment futureInfoFragment = (FutureInfoFragment) mMainActivityPresenter.getmViewPagerFragmentAdapter().getItem(2);
                //刷新五档行情
                futureInfoFragment.updateMD5ViewVisibility();
                //刷新k线周期
                KlineDurationTitleAdapter klineDurationTitleAdapter = futureInfoFragment.getmKlineDurationTitleAdapter();
                klineDurationTitleAdapter.update();
                futureInfoFragment.switchDuration(klineDurationTitleAdapter.getDurationTitle());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public MainActivityPresenter getmMainActivityPresenter() {
        return mMainActivityPresenter;
    }

    public void setTitle(String title) {
        mTitle = title;
    }

}
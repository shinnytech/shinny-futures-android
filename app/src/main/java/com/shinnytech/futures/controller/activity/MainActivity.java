package com.shinnytech.futures.controller.activity;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.material.snackbar.Snackbar;
import com.shinnytech.futures.R;
import com.shinnytech.futures.amplitude.api.Amplitude;
import com.shinnytech.futures.application.BaseApplication;
import com.shinnytech.futures.controller.fragment.AccountFragment;
import com.shinnytech.futures.controller.fragment.LazyLoadFragment;
import com.shinnytech.futures.controller.fragment.QuoteFragment;
import com.shinnytech.futures.controller.fragment.QuotePagerFragment;
import com.shinnytech.futures.databinding.ActivityMainDrawerBinding;
import com.shinnytech.futures.model.adapter.ViewPagerFragmentAdapter;
import com.shinnytech.futures.model.bean.conditionorderbean.ConditionOrderEntity;
import com.shinnytech.futures.model.bean.conditionorderbean.ConditionUserEntity;
import com.shinnytech.futures.model.bean.futureinfobean.QuoteEntity;
import com.shinnytech.futures.model.bean.searchinfobean.SearchEntity;
import com.shinnytech.futures.model.engine.DataManager;
import com.shinnytech.futures.model.engine.LatestFileManager;
import com.shinnytech.futures.utils.NetworkUtils;
import com.shinnytech.futures.utils.SPUtils;
import com.shinnytech.futures.utils.SystemUtils;
import com.shinnytech.futures.utils.ToastUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Map;

import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import q.rorbin.badgeview.Badge;
import q.rorbin.badgeview.QBadgeView;

import static com.shinnytech.futures.application.BaseApplication.CO_BROADCAST_ACTION;
import static com.shinnytech.futures.application.BaseApplication.MD_BROADCAST_ACTION;
import static com.shinnytech.futures.application.BaseApplication.TD_BROADCAST_ACTION;
import static com.shinnytech.futures.constants.AmpConstants.AMP_EVENT_OPTIONAL_DIRECTION;
import static com.shinnytech.futures.constants.AmpConstants.AMP_EVENT_OPTIONAL_DIRECTION_VALUE_ADD;
import static com.shinnytech.futures.constants.AmpConstants.AMP_EVENT_OPTIONAL_DIRECTION_VALUE_DELETE;
import static com.shinnytech.futures.constants.AmpConstants.AMP_EVENT_OPTIONAL_INSTRUMENT_ID;
import static com.shinnytech.futures.constants.AmpConstants.AMP_OPTIONAL_FUTURE_INFO;
import static com.shinnytech.futures.constants.BroadcastConstants.CO_MESSAGE;
import static com.shinnytech.futures.constants.BroadcastConstants.MD_MESSAGE;
import static com.shinnytech.futures.constants.BroadcastConstants.TD_MESSAGE;
import static com.shinnytech.futures.constants.BroadcastConstants.TD_MESSAGE_BROKER_INFO;
import static com.shinnytech.futures.constants.BroadcastConstants.TD_MESSAGE_SETTLEMENT;
import static com.shinnytech.futures.constants.CommonConstants.ACCOUNT_DETAIL;
import static com.shinnytech.futures.constants.CommonConstants.INS_BETWEEN_ACTIVITY;
import static com.shinnytech.futures.constants.CommonConstants.MAIN_ACTIVITY_TO_OPTIONAL_SETTING_ACTIVITY;
import static com.shinnytech.futures.constants.CommonConstants.MAIN_ACTIVITY_TO_SEARCH_ACTIVITY;
import static com.shinnytech.futures.constants.CommonConstants.MAIN_ACTIVITY_TO_TRANSFER_ACTIVITY;
import static com.shinnytech.futures.constants.CommonConstants.MENU_TITLE_NAVIGATION;
import static com.shinnytech.futures.constants.CommonConstants.OFFLINE;
import static com.shinnytech.futures.constants.CommonConstants.OPTIONAL;
import static com.shinnytech.futures.constants.CommonConstants.SOURCE_ACTIVITY;
import static com.shinnytech.futures.constants.CommonConstants.SOURCE_ACTIVITY_FUTURE_INFO;
import static com.shinnytech.futures.constants.CommonConstants.SOURCE_ACTIVITY_MAIN;
import static com.shinnytech.futures.constants.ConditionConstants.CONDITION_STATUS_LIVE;
import static com.shinnytech.futures.constants.ConditionConstants.CONDITION_STATUS_SUSPEND;
import static com.shinnytech.futures.constants.SettingConstants.CONFIG_IS_CONDITION;
import static com.shinnytech.futures.constants.SettingConstants.CONFIG_VERSION_CODE;

/**
 * date: 6/14/17
 * author: chenli
 * description: 问题1：viewpager会初始化两个fragment，如何让它不初始化屏幕外的fragment？运行过程中有两个fragment在同时运行
 * 问题2：如何合理减小navigationView的宽高，定制菜单字体
 * version:
 * state: basically done
 */

public class MainActivity extends BaseActivity {
    private long mExitTime = 0;
    private ActivityMainDrawerBinding mBinding;
    private MainActivityPresenter mMainActivityPresenter;
    private BroadcastReceiver mReceiverMarket;
    private BroadcastReceiver mReceiverTrade;
    private BroadcastReceiver mReceiverCondition;
    private boolean mIsInit;
    private Badge mBadgeView;
    private boolean mIsShowConditionHint;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mLayoutID = R.layout.activity_main_drawer;
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void initData() {
        mBinding = (ActivityMainDrawerBinding) mViewDataBinding;
        mMainActivityPresenter = new MainActivityPresenter(this, sContext, mBinding, mToolbarTitle);
        mIsInit = true;
        mIsShowConditionHint = true;
    }

    @Override
    protected void initEvent() {
        mMainActivityPresenter.registerListeners();
        checkResponsibility();
        registerBroaderCast();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        String instrument_id = intent.getStringExtra(INS_BETWEEN_ACTIVITY);
        //多次退出登录可能会触发空指针异常
        if (instrument_id == null) return;
        mMainActivityPresenter.switchToFutureInfo(instrument_id);
    }

    @Override
    protected void onResume() {
        super.onResume();
        showSettlement();
        try {
            if (!mIsInit) {
                int index = mBinding.vpContent.getCurrentItem();
                ViewPagerFragmentAdapter viewPagerFragmentAdapter = mMainActivityPresenter.getmViewPagerFragmentAdapter();
                LazyLoadFragment lazyLoadFragment = (LazyLoadFragment) viewPagerFragmentAdapter.getItem(index);
                lazyLoadFragment.show();
            }
            mIsInit = false;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mReceiverMarket != null)
            LocalBroadcastManager.getInstance(this).unregisterReceiver(mReceiverMarket);
        if (mReceiverTrade != null)
            LocalBroadcastManager.getInstance(this).unregisterReceiver(mReceiverTrade);
        if (mReceiverCondition != null)
            LocalBroadcastManager.getInstance(this).unregisterReceiver(mReceiverCondition);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main_menu, menu);
        getMenuInflater().inflate(R.menu.search, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem menuItem = menu.findItem(R.id.right_navigation);
        FrameLayout rootView = (FrameLayout) menuItem.getActionView();
        ImageView view = rootView.findViewById(R.id.view_menu);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String menuTitle = menuItem.getTitle().toString();
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
                            view.setImageResource(R.mipmap.ic_favorite_border_white_24dp);
                        } else {
                            jsonObject.put(AMP_EVENT_OPTIONAL_DIRECTION, AMP_EVENT_OPTIONAL_DIRECTION_VALUE_ADD);
                            QuoteEntity quoteEntity = new QuoteEntity();
                            quoteEntity.setInstrument_id(mInstrumentId);
                            insList.put(mInstrumentId, quoteEntity);
                            LatestFileManager.saveInsListToFile(new ArrayList<>(insList.keySet()));
                            ToastUtils.showToast(BaseApplication.getContext(), name + "合约已添加");
                            view.setImageResource(R.mipmap.ic_favorite_white_24dp);
                        }
                        Amplitude.getInstance().logEventWrap(AMP_OPTIONAL_FUTURE_INFO, jsonObject);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        mBadgeView = new QBadgeView(sContext).bindTarget(view)
                .setBadgeNumber(-1)
                .setBadgeBackgroundColor(sContext.getResources().getColor(R.color.launch))
                .setBadgeGravity( Gravity.END | Gravity.TOP)
                .setBadgePadding(4, true)
                .setGravityOffset(8, 8, true)
                .setBadgeTextColor(sContext.getResources().getColor(R.color.white));
        refreshCO();
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.search_quote:
                mMainActivityPresenter.setPreSubscribedQuotes(sDataManager.getRtnData().getIns_list());
                Intent intent = new Intent(MainActivity.this, SearchActivity.class);
                int index = mMainActivityPresenter.getmBinding().vpContent.getCurrentItem();
                if (index == 2) intent.putExtra(SOURCE_ACTIVITY, SOURCE_ACTIVITY_FUTURE_INFO);
                else intent.putExtra(SOURCE_ACTIVITY, SOURCE_ACTIVITY_MAIN);
                startActivityForResult(intent, MAIN_ACTIVITY_TO_SEARCH_ACTIVITY);
                return true;
            case android.R.id.home:
                Menu menu = mToolbar.getMenu();
                if (menu == null || menu.size() == 0) return true;
                MenuItem menuItem = menu.findItem(R.id.right_navigation);
                FrameLayout rootView = (FrameLayout) menuItem.getActionView();
                ImageView view = rootView.findViewById(R.id.view_menu);
                view.setImageResource(R.mipmap.ic_menu);
                menuItem.setTitle(MENU_TITLE_NAVIGATION);
                refreshCO();
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
        Menu menu = mToolbar.getMenu();
        if (menu == null || menu.size() == 0) return true;
        MenuItem menuItem = menu.findItem(R.id.right_navigation);
        String title = menuItem.getTitle().toString();
        if (MENU_TITLE_NAVIGATION.equals(title)) {
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
                        SystemUtils.exitApp(this);
                    }
                    return true;
                }
            }
        } else {
            FrameLayout rootView = (FrameLayout) menuItem.getActionView();
            ImageView view = rootView.findViewById(R.id.view_menu);
            view.setImageResource(R.mipmap.ic_menu);
            menuItem.setTitle(MENU_TITLE_NAVIGATION);
            refreshCO();
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
    private void checkResponsibility() {
        try {
            final float nowVersionCode = DataManager.getInstance().APP_CODE;
            float versionCode = (float) SPUtils.get(sContext, CONFIG_VERSION_CODE, 0.0f);
            if (nowVersionCode > versionCode) {
                final Dialog dialog = new Dialog(this, R.style.AppTheme);
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
     * date: 2019/8/19
     * author: chenli
     * description: 条件单免责检查，入口：导航菜单、持仓长按(止盈止损/新建条件单)、交易页条件
     */
    public boolean checkConditionResponsibility() {
        final boolean[] isCondition = {(boolean) SPUtils.get(sContext, CONFIG_IS_CONDITION, false)};
        if (!isCondition[0]) {
            final Dialog dialog = new Dialog(this, R.style.AppTheme);
            View view = View.inflate(this, R.layout.view_dialog_responsibility_condition, null);
            dialog.setContentView(view);
            dialog.setCanceledOnTouchOutside(false);
            dialog.setCancelable(false);
            dialog.show();
            view.findViewById(R.id.agree).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    SPUtils.putAndApply(MainActivity.this, CONFIG_IS_CONDITION, true);
                    dialog.dismiss();
                    isCondition[0] = true;
                }
            });
            view.findViewById(R.id.disagree).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                }
            });
        }
        return isCondition[0];
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
     * date: 2019/6/15
     * author: chenli
     * description: 显示结算单
     */
    public void showSettlement() {
        String settlement = sDataManager.getBroker().getSettlement();
        boolean confirmed = sDataManager.getBroker().isConfirmed();
        if (settlement != null && !confirmed) {
            sDataManager.getBroker().setConfirmed(true);
            final Dialog dialog = new Dialog(this, R.style.AppTheme);
            View viewDialog = View.inflate(this, R.layout.view_dialog_confirm, null);
            dialog.setContentView(viewDialog);
            TextView textView = viewDialog.findViewById(R.id.settlement_info);
            textView.setText(settlement);
            dialog.setCanceledOnTouchOutside(false);
            dialog.setCancelable(false);
            viewDialog.findViewById(R.id.confirm).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    BaseApplication.getmTDWebSocket().sendReqConfirmSettlement();
                    dialog.dismiss();
                }
            });
            if (!dialog.isShowing()) dialog.show();
        }
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
                    case TD_MESSAGE_SETTLEMENT:
                        showSettlement();
                        break;
                    case TD_MESSAGE_BROKER_INFO:
                        //条件单服务器重启弹一次通知
                        mIsShowConditionHint = true;
                        break;
                    default:
                        break;
                }
            }
        };
        LocalBroadcastManager.getInstance(this).registerReceiver(mReceiverTrade, new IntentFilter(TD_BROADCAST_ACTION));

        mReceiverCondition = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String mDataString = intent.getStringExtra("msg");
                switch (mDataString) {
                    case CO_MESSAGE:
                        refreshCO();
                        break;
                    default:
                        break;
                }
            }
        };
        LocalBroadcastManager.getInstance(this).registerReceiver(mReceiverCondition, new IntentFilter(CO_BROADCAST_ACTION));
    }

    /**
     * date: 2019/8/19
     * author: chenli
     * description: 刷新条件单信息，更新图标
     */
    public void refreshCO() {
        int count = 0;
        int suspendCount = 0;
        ConditionUserEntity userEntity = sDataManager.getConditionOrderBean().getUsers()
                .get(sDataManager.USER_ID);
        if (userEntity != null){
            Map<String, ConditionOrderEntity> condition_orders = userEntity.getCondition_orders();
            for (ConditionOrderEntity conditionOrderEntity :
                    condition_orders.values()) {
                String status = conditionOrderEntity.getStatus();
                if (CONDITION_STATUS_SUSPEND.equals(status)) suspendCount++;
                if (CONDITION_STATUS_LIVE.equals(status)) count++;
            }
        }
        int allCount = count + suspendCount;
        Menu menu = mToolbar.getMenu();
        if (menu != null && menu.size() != 0){
            MenuItem menuItem = menu.findItem(R.id.right_navigation);
            String menuTitle = menuItem.getTitle().toString();
            if (MENU_TITLE_NAVIGATION.equals(menuTitle)) {
                if (count == 0)count = -1;
            }else count = 0;
        }
        mBadgeView.setBadgeNumber(count);
        mMainActivityPresenter.getmNavigationRightAdapter().refreshBadgeNum(count);

        if (allCount != 0 && suspendCount != 0 && allCount == suspendCount && mIsShowConditionHint){
            SpannableString ss = new SpannableString("您当前条件单皆为暂停状态，如非您人工操作结果，请关注快期官网通知");
            ClickableSpan clickableSpan = new ClickableSpan() {
                @Override
                public void onClick(View textView) {
                    startActivity(new Intent(MainActivity.this, ShinnyTechActivity.class));
                }
                @Override
                public void updateDrawState(TextPaint ds) {
                    super.updateDrawState(ds);
                    ds.setUnderlineText(true);
                }
            };
            ss.setSpan(clickableSpan, 26, 30, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            Snackbar sb = Snackbar.make(mBinding.main, ss, Snackbar.LENGTH_INDEFINITE);
            View view = sb.getView();
            TextView textView = view.findViewById(com.google.android.material.R.id.snackbar_text);
            textView.setMovementMethod(LinkMovementMethod.getInstance());
            sb.setAction("确定", new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    sb.dismiss();
                    mIsShowConditionHint = false;
                }
            });
            sb.show();
        }
    }

    /**
     * date: 2019/7/12
     * author: chenli
     * description: 刷新合约行情
     */
    public void refreshMD() {
        if (!mMainActivityPresenter.ismIsUpdate()) return;
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
        if (!mMainActivityPresenter.ismIsUpdate()) return;
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
                    if (OPTIONAL.equals(mToolbarTitle.getText().toString())) quoteFragment.show();
                }
            }

            //二级页、银期转帐页刷新账户信息
            if (requestCode == MAIN_ACTIVITY_TO_TRANSFER_ACTIVITY) {
                AccountFragment accountFragment = (AccountFragment) mMainActivityPresenter.
                        getmViewPagerFragmentAdapter().getItem(1);
                if (accountFragment != null) accountFragment.refreshTD();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public MainActivityPresenter getmMainActivityPresenter() {
        return mMainActivityPresenter;
    }

    public void setTitle(String title) {
        mTitle = title;
    }

    public Menu getMenu() {
        return mToolbar.getMenu();
    }

    public Badge getmBadgeView() {
        return mBadgeView;
    }
}
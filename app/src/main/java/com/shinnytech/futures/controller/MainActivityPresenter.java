package com.shinnytech.futures.controller;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.PointF;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.ViewDragHelper;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.LinearSmoothScroller;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Layout;
import android.text.SpannableString;
import android.text.style.AlignmentSpan;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.shinnytech.futures.R;
import com.shinnytech.futures.controller.activity.AboutActivity;
import com.shinnytech.futures.databinding.ActivityMainDrawerBinding;
import com.shinnytech.futures.model.bean.eventbusbean.PositionEvent;
import com.shinnytech.futures.model.bean.eventbusbean.UpdateEvent;
import com.shinnytech.futures.model.engine.DataManager;
import com.shinnytech.futures.model.engine.LatestFileManager;
import com.shinnytech.futures.utils.LogUtils;
import com.shinnytech.futures.utils.NetworkUtils;
import com.shinnytech.futures.utils.SPUtils;
import com.shinnytech.futures.controller.activity.AccountActivity;
import com.shinnytech.futures.controller.activity.BankTransferActivity;
import com.shinnytech.futures.controller.activity.FeedBackActivity;
import com.shinnytech.futures.controller.activity.FutureInfoActivity;
import com.shinnytech.futures.controller.activity.MainActivity;
import com.shinnytech.futures.controller.activity.TradeActivity;
import com.shinnytech.futures.model.adapter.QuoteNavAdapter;
import com.shinnytech.futures.model.adapter.ViewPagerFragmentAdapter;
import com.shinnytech.futures.controller.fragment.QuoteFragment;
import com.shinnytech.futures.model.listener.SimpleRecyclerViewItemClickListener;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static com.shinnytech.futures.constants.CommonConstants.DALIAN;
import static com.shinnytech.futures.constants.CommonConstants.DALIANZUHE;
import static com.shinnytech.futures.constants.CommonConstants.DOMINANT;
import static com.shinnytech.futures.constants.CommonConstants.JUMP_TO_FUTURE_INFO_ACTIVITY;
import static com.shinnytech.futures.constants.CommonConstants.NENGYUAN;
import static com.shinnytech.futures.constants.CommonConstants.OPTIONAL;
import static com.shinnytech.futures.constants.CommonConstants.SHANGHAI;
import static com.shinnytech.futures.constants.CommonConstants.ZHENGZHOU;
import static com.shinnytech.futures.constants.CommonConstants.ZHENGZHOUZUHE;
import static com.shinnytech.futures.constants.CommonConstants.ZHONGJIN;
import static com.shinnytech.futures.model.receiver.NetworkReceiver.NETWORK_STATE;

/**
 * Created on 1/17/18.
 * Created by chenli.
 * Description: 将主页的合约导航栏操作封装起来
 */

public class MainActivityPresenter implements NavigationView.OnNavigationItemSelectedListener, ViewPager.OnPageChangeListener, View.OnClickListener, DrawerLayout.DrawerListener {
    private final Handler mDrawerActionHandler = new Handler();
    private final ViewPagerFragmentAdapter mViewPagerFragmentAdapter;
    private ActivityMainDrawerBinding mBinding;
    private MainActivity mMainActivity;
    private Context sContext;
    private Toolbar mToolbar;
    private TextView mToolbarTitle;
    private QuoteNavAdapter mNavAdapter;
    private String[] mMenuTitle = new String[]{"自选", "主力", "上海", "上期能源", "大连", "郑州", "中金", "大连组合", "郑州组合", "账户", "持仓", "成交", "转账", "反馈", "关于"};
    private Map<String, String> mInsListNameNav = new TreeMap<>();
    private String mIns;
    private BroadcastReceiver mReceiver;
    private int mCurItemId;
    private String mTitle = "";

    public MainActivityPresenter(MainActivity mainActivity, Context context, ActivityMainDrawerBinding binding, Toolbar toolbar, TextView toolbarTitle) {
        this.mBinding = binding;
        this.mMainActivity = mainActivity;
        this.mToolbar = toolbar;
        this.mToolbarTitle = toolbarTitle;
        this.sContext = context;

        //设置Drawer的开关
        ActionBarDrawerToggle mToggle = new ActionBarDrawerToggle(
                mMainActivity, mBinding.drawerLayout, mToolbar, R.string.main_activity_openDrawer, R.string.main_activity_closeDrawer);
        mBinding.drawerLayout.addDrawerListener(mToggle);
        mToggle.syncState();

        //初始化合约列表导航
        mBinding.rvQuoteNavigation.setLayoutManager(new LinearLayoutManagerWithSmoothScroller(mMainActivity));
        mNavAdapter = new QuoteNavAdapter(mMainActivity, mInsListNameNav);
        mBinding.rvQuoteNavigation.setAdapter(mNavAdapter);

        //添加合约页，设置首页是自选合约列表页
        List<Fragment> fragmentList = new ArrayList<>();
        fragmentList.add(QuoteFragment.newInstance(OPTIONAL));
        fragmentList.add(QuoteFragment.newInstance(DOMINANT));
        fragmentList.add(QuoteFragment.newInstance(SHANGHAI));
        fragmentList.add(QuoteFragment.newInstance(NENGYUAN));
        fragmentList.add(QuoteFragment.newInstance(DALIAN));
        fragmentList.add(QuoteFragment.newInstance(ZHENGZHOU));
        fragmentList.add(QuoteFragment.newInstance(ZHONGJIN));
        fragmentList.add(QuoteFragment.newInstance(DALIANZUHE));
        fragmentList.add(QuoteFragment.newInstance(ZHENGZHOUZUHE));
        //初始化适配器类
        mViewPagerFragmentAdapter = new ViewPagerFragmentAdapter(mMainActivity.getSupportFragmentManager(), fragmentList);
        mBinding.vpContent.setAdapter(mViewPagerFragmentAdapter);
        mBinding.vpContent.setCurrentItem(1);
        //保证lazyLoad的效用,每次加载一个fragment
        mBinding.vpContent.setOffscreenPageLimit(7);

        //使侧滑菜单上的文字居中
        for (int i = 0; i < mMenuTitle.length; i++) {
            SpannableString s = new SpannableString(mMenuTitle[i]);
            s.setSpan(new AlignmentSpan.Standard(Layout.Alignment.ALIGN_CENTER), 0, s.length(), 0);
            mBinding.nvMenu.getMenu().getItem(i).setTitle(s);
        }

        // 设置导航菜单宽度
        ViewGroup.LayoutParams params = mBinding.nvMenu.getLayoutParams();
        params.width = mMainActivity.getResources().getDisplayMetrics().widthPixels * 1 / 2;
        mBinding.nvMenu.setLayoutParams(params);
    }

    //使导航栏和viewPager页面匹配,重新初始化mCurItemId
    public void resetNavigationItem() {
        mCurItemId = mBinding.vpContent.getCurrentItem();
        mBinding.nvMenu.getMenu().getItem(mCurItemId).setChecked(true);
    }

    /**
     * date: 1/16/18
     * author: chenli
     * description: 检查是否第一次启动APP,弹出免责条款框
     */
    public void checkResponsibility() {
        final int nowVersionCode = DataManager.getInstance().APP_CODE;
        int versionCode = (int) SPUtils.get(sContext, "versionCode", 0);
        if (nowVersionCode > versionCode) {
            final Dialog dialog = new Dialog(mMainActivity, R.style.responsibilityDialog);
            View view = View.inflate(mMainActivity, R.layout.view_dialog_responsibility, null);
            dialog.setContentView(view);
            dialog.setCanceledOnTouchOutside(false);
            dialog.setCancelable(false);
            dialog.show();
            view.findViewById(R.id.agree).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    SPUtils.putAndApply(mMainActivity, "versionCode", nowVersionCode);
                    dialog.dismiss();
                }
            });
            view.findViewById(R.id.disagree).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mMainActivity.finish();
                }
            });
        }
    }

    /**
     * date: 7/7/17
     * author: chenli
     * description: 检查网络的状态
     */
    public void checkNetwork() {
        if (!NetworkUtils.isNetworkConnected(sContext)) {
            AlertDialog.Builder dialog = new AlertDialog.Builder(mMainActivity);
            dialog.setTitle("登录结果");
            dialog.setMessage("网络故障，无法连接到服务器");
            dialog.setCancelable(false);
            dialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    mMainActivity.finish();
                }
            });
            dialog.show();
        }
    }

    /**
     * date: 1/18/18
     * author: chenli
     * description: 注册各类监听事件
     */
    public void registerListeners() {
        mBinding.nvMenu.setNavigationItemSelectedListener(this);
        mBinding.drawerLayout.addDrawerListener(this);
        mBinding.vpContent.addOnPageChangeListener(this);
        //合约导航左右移动
        mBinding.quoteNavLeft.setOnClickListener(this);
        mBinding.quoteNavRight.setOnClickListener(this);
        //为底部合约导航栏添加监听事件
        mBinding.rvQuoteNavigation.addOnItemTouchListener(new SimpleRecyclerViewItemClickListener(mBinding.rvQuoteNavigation, new SimpleRecyclerViewItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                String instrumentId = (String) view.getTag();
                scrollQuotes(mToolbarTitle.getText().toString(), position, instrumentId);
            }

            @Override
            public void onItemLongClick(View view, int position) {
            }
        }));
    }

    /**
     * date: 7/7/17
     * author: chenli
     * description: 监听网络状态的广播
     */
    public void registerBroaderCast() {
        mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                int networkStatus = intent.getIntExtra("networkStatus", 0);
                switch (networkStatus) {
                    case 0:
                        if (!"交易、行情网络未连接！".equals(mToolbarTitle.getText().toString()))
                            mTitle = mToolbarTitle.getText().toString();
                        mToolbar.setBackgroundColor(ContextCompat.getColor(context, R.color.off_line));
                        mToolbarTitle.setTextColor(Color.BLACK);
                        mToolbarTitle.setText("交易、行情网络未连接！");
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
        mMainActivity.registerReceiver(mReceiver, new IntentFilter(NETWORK_STATE));
    }

    /**
     * date: 7/7/17
     * author: chenli
     * description: 注销广播
     */
    public void unRegisterBroaderCast() {
        if (mReceiver != null) mMainActivity.unregisterReceiver(mReceiver);
    }

    public String getPreSubscribedQuotes() {
        return mIns;
    }

    /**
     * date: 7/7/17
     * author: chenli
     * description: 切换合约导航栏和标题栏
     */
    private void switchNavigationAndTitle(int state) {
        //获取当前页
        int currentItem = mBinding.vpContent.getCurrentItem();
        switch (currentItem) {
            case 0:
                //更新页面标题
                if (state == ViewPager.SCROLL_STATE_SETTLING && NetworkUtils.isNetworkConnected(sContext))
                    mToolbarTitle.setText(OPTIONAL);
                //匹配对应合约导航栏
                if (state == ViewPager.SCROLL_STATE_IDLE)
                    refreshQuotesNavigation(OPTIONAL);
                break;
            case 1:
                if (state == ViewPager.SCROLL_STATE_SETTLING && NetworkUtils.isNetworkConnected(sContext))
                    mToolbarTitle.setText(DOMINANT);

                if (state == ViewPager.SCROLL_STATE_IDLE)
                    refreshQuotesNavigation(DOMINANT);
                break;
            case 2:
                if (state == ViewPager.SCROLL_STATE_SETTLING && NetworkUtils.isNetworkConnected(sContext))
                    mToolbarTitle.setText(SHANGHAI);

                if (state == ViewPager.SCROLL_STATE_IDLE)
                    refreshQuotesNavigation(SHANGHAI);
                break;
            case 3:
                if (state == ViewPager.SCROLL_STATE_SETTLING && NetworkUtils.isNetworkConnected(sContext))
                    mToolbarTitle.setText(NENGYUAN);

                if (state == ViewPager.SCROLL_STATE_IDLE)
                    refreshQuotesNavigation(NENGYUAN);
                break;
            case 4:
                if (state == ViewPager.SCROLL_STATE_SETTLING && NetworkUtils.isNetworkConnected(sContext))
                    mToolbarTitle.setText(DALIAN);

                if (state == ViewPager.SCROLL_STATE_IDLE)
                    refreshQuotesNavigation(DALIAN);
                break;
            case 5:
                if (state == ViewPager.SCROLL_STATE_SETTLING && NetworkUtils.isNetworkConnected(sContext))
                    mToolbarTitle.setText(ZHENGZHOU);

                if (state == ViewPager.SCROLL_STATE_IDLE)
                    refreshQuotesNavigation(ZHENGZHOU);
                break;
            case 6:
                if (state == ViewPager.SCROLL_STATE_SETTLING && NetworkUtils.isNetworkConnected(sContext))
                    mToolbarTitle.setText(ZHONGJIN);

                if (state == ViewPager.SCROLL_STATE_IDLE)
                    refreshQuotesNavigation(ZHONGJIN);
                break;
            case 7:
                if (state == ViewPager.SCROLL_STATE_SETTLING && NetworkUtils.isNetworkConnected(sContext))
                    mToolbarTitle.setText(DALIANZUHE);

                if (state == ViewPager.SCROLL_STATE_IDLE)
                    refreshQuotesNavigation(DALIANZUHE);
                break;
            case 8:
                if (state == ViewPager.SCROLL_STATE_SETTLING && NetworkUtils.isNetworkConnected(sContext))
                    mToolbarTitle.setText(ZHENGZHOUZUHE);

                if (state == ViewPager.SCROLL_STATE_IDLE)
                    refreshQuotesNavigation(ZHENGZHOUZUHE);
                break;
            default:
                break;
        }
    }

    /**
     * date: 1/17/18
     * author: chenli
     * description: 点击侧滑栏事件
     */
    private void switchPages(int mCurItemId) {
        switch (mCurItemId) {
            case R.id.nav_optional:
                //如果没有侧滑栏上的itemId没有发生变化,就不切换页面
                //更新页面标题
                if (NetworkUtils.isNetworkConnected(sContext))
                    mToolbarTitle.setText(OPTIONAL);
                mBinding.vpContent.setCurrentItem(0, false);
                //匹配对应合约导航栏
                refreshQuotesNavigation(OPTIONAL);
                break;
            case R.id.nav_dominant:
                if (NetworkUtils.isNetworkConnected(sContext))
                    mToolbarTitle.setText(DOMINANT);
                mBinding.vpContent.setCurrentItem(1, false);
                refreshQuotesNavigation(DOMINANT);
                break;
            case R.id.nav_shanghai:
                if (NetworkUtils.isNetworkConnected(sContext))
                    mToolbarTitle.setText(SHANGHAI);
                mBinding.vpContent.setCurrentItem(2, false);
                refreshQuotesNavigation(SHANGHAI);
                break;
            case R.id.nav_nengyuan:
                if (NetworkUtils.isNetworkConnected(sContext))
                    mToolbarTitle.setText(NENGYUAN);
                mBinding.vpContent.setCurrentItem(3, false);
                refreshQuotesNavigation(NENGYUAN);
                break;
            case R.id.nav_dalian:
                if (NetworkUtils.isNetworkConnected(sContext))
                    mToolbarTitle.setText(DALIAN);
                mBinding.vpContent.setCurrentItem(4, false);
                refreshQuotesNavigation(DALIAN);
                break;
            case R.id.nav_zhengzhou:
                if (NetworkUtils.isNetworkConnected(sContext))
                    mToolbarTitle.setText(ZHENGZHOU);
                mBinding.vpContent.setCurrentItem(5, false);
                refreshQuotesNavigation(ZHENGZHOU);
                break;
            case R.id.nav_zhongjin:
                if (NetworkUtils.isNetworkConnected(sContext))
                    mToolbarTitle.setText(ZHONGJIN);
                mBinding.vpContent.setCurrentItem(6, false);
                refreshQuotesNavigation(ZHONGJIN);
                break;
            case R.id.nav_dalian_zuhe:
                if (NetworkUtils.isNetworkConnected(sContext))
                    mToolbarTitle.setText(DALIANZUHE);
                mBinding.vpContent.setCurrentItem(7, false);
                refreshQuotesNavigation(DALIANZUHE);
                break;
            case R.id.nav_zhengzhou_zuhe:
                if (NetworkUtils.isNetworkConnected(sContext))
                    mToolbarTitle.setText(ZHENGZHOUZUHE);
                mBinding.vpContent.setCurrentItem(8, false);
                refreshQuotesNavigation(ZHENGZHOUZUHE);
                break;
            case R.id.nav_account:
                Intent intentAcc = new Intent(mMainActivity, AccountActivity.class);
                mMainActivity.startActivity(intentAcc);
                break;
            case R.id.nav_position:
                try {
                    mIns = DataManager.getInstance().getRtnData().getIns_list();
                    Intent intentPos = new Intent(mMainActivity, FutureInfoActivity.class);
                    intentPos.putExtra("nav_position", 1);
                    String instrument_id = new ArrayList<>(LatestFileManager.getMainInsList().keySet()).get(0);
                    intentPos.putExtra("instrument_id", instrument_id);
                    mMainActivity.startActivityForResult(intentPos, JUMP_TO_FUTURE_INFO_ACTIVITY);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case R.id.nav_deal:
                Intent intentDeal = new Intent(mMainActivity, TradeActivity.class);
                mMainActivity.startActivity(intentDeal);
                break;
            case R.id.nav_bank:
                Intent intentBank = new Intent(mMainActivity, BankTransferActivity.class);
                mMainActivity.startActivity(intentBank);
                break;
            case R.id.nav_feedback:
                Intent intentFeed = new Intent(mMainActivity, FeedBackActivity.class);
                mMainActivity.startActivity(intentFeed);
                break;
            case R.id.nav_about:
                Intent intentAbout = new Intent(mMainActivity, AboutActivity.class);
                mMainActivity.startActivity(intentAbout);
                break;
            default:
                break;

        }
    }

    /**
     * date: 7/7/17
     * author: chenli
     * description: 点击合约导航滑动行情列表
     */
    private void scrollQuotes(String title, int position, String instrumentId) {
        if (instrumentId.contains("&")) {
            instrumentId = instrumentId.split("&")[0];
        }
        instrumentId = instrumentId.replaceAll("\\d", "");
        List<String> insListName = new ArrayList<>();
        switch (title) {
            case DOMINANT:
                insListName = new ArrayList<>(LatestFileManager.getMainInsList().keySet());
                break;
            case SHANGHAI:
                insListName = new ArrayList<>(LatestFileManager.getShangqiInsList().keySet());
                break;
            case NENGYUAN:
                insListName = new ArrayList<>(LatestFileManager.getNengyuanInsList().keySet());
                break;
            case DALIAN:
                insListName = new ArrayList<>(LatestFileManager.getDalianInsList().keySet());
                break;
            case ZHENGZHOU:
                insListName = new ArrayList<>(LatestFileManager.getZhengzhouInsList().keySet());
                break;
            case ZHONGJIN:
                insListName = new ArrayList<>(LatestFileManager.getZhongjinInsList().keySet());
                break;
            case DALIANZUHE:
                insListName = new ArrayList<>(LatestFileManager.getDalianzuheInsList().keySet());
                break;
            case ZHENGZHOUZUHE:
                insListName = new ArrayList<>(LatestFileManager.getZhengzhouzuheInsList().keySet());
                break;
            default:
                break;
        }

        for (int i = 0; i < insListName.size(); i++) {
            if (insListName.get(i).contains(instrumentId)) {
                //出现重复的合约中文名，则导航到第一个出现的位置
                position = i;
                break;
            }
        }
        //滑动行情列表
        PositionEvent positionEvent = new PositionEvent();
        positionEvent.setPosition(position);
        EventBus.getDefault().post(positionEvent);
    }

    /**
     * date: 7/7/17
     * author: chenli
     * description: 根据页面标题匹配对应的导航栏，自选合约页不显示导航栏
     */
    public void refreshQuotesNavigation(String mTitle) {
        switch (mTitle) {
            case OPTIONAL:
                setNavGone();
                break;
            case DOMINANT:
                setNavVisiable();
                mInsListNameNav = LatestFileManager.getMainInsListNameNav();
                mNavAdapter.updateList(mInsListNameNav);
                break;
            case SHANGHAI:
                setNavVisiable();
                mInsListNameNav = LatestFileManager.getShangqiInsListNameNav();
                mNavAdapter.updateList(mInsListNameNav);
                break;
            case NENGYUAN:
                setNavVisiable();
                mInsListNameNav = LatestFileManager.getNengyuanInsListNameNav();
                mNavAdapter.updateList(mInsListNameNav);
                break;
            case DALIAN:
                setNavVisiable();
                mInsListNameNav = LatestFileManager.getDalianInsListNameNav();
                mNavAdapter.updateList(mInsListNameNav);
                break;
            case ZHENGZHOU:
                setNavVisiable();
                mInsListNameNav = LatestFileManager.getZhengzhouInsListNameNav();
                mNavAdapter.updateList(mInsListNameNav);
                break;
            case ZHONGJIN:
                setNavVisiable();
                mInsListNameNav = LatestFileManager.getZhongjinInsListNameNav();
                mNavAdapter.updateList(mInsListNameNav);
                break;
            case DALIANZUHE:
                setNavVisiable();
                mInsListNameNav = LatestFileManager.getDalianzuheInsListNameNav();
                mNavAdapter.updateList(mInsListNameNav);
                break;
            case ZHENGZHOUZUHE:
                setNavVisiable();
                mInsListNameNav = LatestFileManager.getZhengzhouzuheInsListNameNav();
                mNavAdapter.updateList(mInsListNameNav);
                break;
            default:
                break;
        }
    }

    private void setNavGone() {
        mBinding.rvQuoteNavigation.setVisibility(View.GONE);
        mBinding.quoteNavLeft.setVisibility(View.GONE);
        mBinding.quoteNavRight.setVisibility(View.GONE);
    }

    private void setNavVisiable() {
        mBinding.rvQuoteNavigation.setVisibility(View.VISIBLE);
        mBinding.quoteNavLeft.setVisibility(View.VISIBLE);
        mBinding.quoteNavRight.setVisibility(View.VISIBLE);
    }

    /**
     * date: 7/7/17
     * author: chenli
     * description: 侧滑导航栏的点击事件
     */
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        final int id = item.getItemId();
        if (id != mCurItemId) {
            mDrawerActionHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    switchPages(id);
                }
            }, 350);
            mCurItemId = id;
            item.setChecked(true);
        }
        mBinding.drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    /**
     * date: 7/7/17
     * author: chenli
     * description: 切换合约导航栏和标题栏
     */
    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
    }

    @Override
    public void onPageSelected(int position) {
        int currentItem = mBinding.vpContent.getCurrentItem();
        MenuItem menuItem = mBinding.nvMenu.getMenu().getItem(currentItem);
        menuItem.setChecked(true);
        mCurItemId = menuItem.getItemId();
    }

    @Override
    public void onPageScrollStateChanged(int state) {
        switchNavigationAndTitle(state);
    }

    /**
     * date: 7/26/17
     * author: chenli
     * description: 切换页面
     */
    @Override
    public void onDrawerSlide(View drawerView, float slideOffset) {

    }

    @Override
    public void onDrawerOpened(View drawerView) {

    }

    @Override
    public void onDrawerClosed(View drawerView) {
        UpdateEvent updateEvent = new UpdateEvent();
        updateEvent.setUpdate(true);
        EventBus.getDefault().post(updateEvent);
    }

    @Override
    public void onDrawerStateChanged(int newState) {
        if (newState == ViewDragHelper.STATE_SETTLING) {
            UpdateEvent updateEvent = new UpdateEvent();
            updateEvent.setUpdate(false);
            EventBus.getDefault().post(updateEvent);
        }
    }


    /**
     * date: 7/11/17
     * author: chenli
     * description: 合约导航点击左右移动
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.quote_nav_left:
                mBinding.rvQuoteNavigation.smoothScrollBy(-mBinding.nvMenu.getMeasuredWidth(), 0);
                break;
            case R.id.quote_nav_right:
                mBinding.rvQuoteNavigation.smoothScrollBy(mBinding.nvMenu.getMeasuredWidth(), 0);
                break;
        }
    }

    /**
     * date: 7/9/17
     * author: chenli
     * description: recyclerView滑动控制类，点击底部导航，缓慢滑动到对应合约，置于列表顶部
     */
    private class LinearLayoutManagerWithSmoothScroller extends LinearLayoutManager {

        private LinearLayoutManagerWithSmoothScroller(Context context) {
            super(context, HORIZONTAL, false);
        }

        @Override
        public void smoothScrollToPosition(RecyclerView recyclerView, RecyclerView.State state,
                                           int position) {
            RecyclerView.SmoothScroller smoothScroller = new TopSnappedSmoothScroller(recyclerView.getContext());
            smoothScroller.setTargetPosition(position);
            startSmoothScroll(smoothScroller);
        }

        private class TopSnappedSmoothScroller extends LinearSmoothScroller {
            private TopSnappedSmoothScroller(Context context) {
                super(context);

            }

            @Override
            public PointF computeScrollVectorForPosition(int targetPosition) {
                return super.computeScrollVectorForPosition(targetPosition);
            }

            @Override
            protected int getVerticalSnapPreference() {
                return SNAP_TO_START;
            }
        }
    }
}

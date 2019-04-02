package com.shinnytech.futures.controller;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.PointF;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.LinearSmoothScroller;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.shinnytech.futures.R;
import com.shinnytech.futures.application.BaseApplication;
import com.shinnytech.futures.constants.CommonConstants;
import com.shinnytech.futures.controller.activity.AboutActivity;
import com.shinnytech.futures.controller.activity.AccountActivity;
import com.shinnytech.futures.controller.activity.BankTransferActivity;
import com.shinnytech.futures.controller.activity.ChangePasswordActivity;
import com.shinnytech.futures.controller.activity.FeedBackActivity;
import com.shinnytech.futures.controller.activity.FutureInfoActivity;
import com.shinnytech.futures.controller.activity.MainActivity;
import com.shinnytech.futures.controller.activity.SettingActivity;
import com.shinnytech.futures.controller.activity.TradeActivity;
import com.shinnytech.futures.controller.fragment.AccountFragment;
import com.shinnytech.futures.controller.fragment.QuoteFragment;
import com.shinnytech.futures.databinding.ActivityMainDrawerBinding;
import com.shinnytech.futures.model.adapter.DialogAdapter;
import com.shinnytech.futures.model.adapter.NavigationRightAdapter;
import com.shinnytech.futures.model.adapter.QuoteNavAdapter;
import com.shinnytech.futures.model.adapter.ViewPagerFragmentAdapter;
import com.shinnytech.futures.model.bean.eventbusbean.PositionEvent;
import com.shinnytech.futures.model.bean.settingbean.NavigationRightEntity;
import com.shinnytech.futures.model.engine.DataManager;
import com.shinnytech.futures.model.engine.LatestFileManager;
import com.shinnytech.futures.model.listener.SimpleRecyclerViewItemClickListener;
import com.shinnytech.futures.utils.DividerGridItemDecorationUtils;
import com.shinnytech.futures.utils.LogUtils;
import com.shinnytech.futures.utils.NetworkUtils;
import com.shinnytech.futures.utils.SPUtils;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static com.shinnytech.futures.constants.CommonConstants.DALIAN;
import static com.shinnytech.futures.constants.CommonConstants.DALIANZUHE;
import static com.shinnytech.futures.constants.CommonConstants.DOMINANT;
import static com.shinnytech.futures.constants.CommonConstants.NENGYUAN;
import static com.shinnytech.futures.constants.CommonConstants.OPTIONAL;
import static com.shinnytech.futures.constants.CommonConstants.POSITION_JUMP_TO_FUTURE_INFO_ACTIVITY;
import static com.shinnytech.futures.constants.CommonConstants.SHANGHAI;
import static com.shinnytech.futures.constants.CommonConstants.ZHENGZHOU;
import static com.shinnytech.futures.constants.CommonConstants.ZHENGZHOUZUHE;
import static com.shinnytech.futures.constants.CommonConstants.ZHONGJIN;

/**
 * Created on 1/17/18.
 * Created by chenli.
 * Description: 将主页的合约导航栏操作封装起来
 */

public class MainActivityPresenter {
    public final NavigationRightAdapter mNavigationRightAdapter;
    private final ViewPagerFragmentAdapter mViewPagerFragmentAdapter;
    private ActivityMainDrawerBinding mBinding;
    private MainActivity mMainActivity;
    private Context sContext;
    private TextView mToolbarTitle;
    private QuoteNavAdapter mNavAdapter;
    private Map<String, String> mInsListNameNav = new TreeMap<>();
    private String mIns;
    private Dialog mTitleDialog;
    private DialogAdapter mTitleDialogAdapter;
    private RecyclerView mTitleRecyclerView;
    private List<String> mTitleList;

    public MainActivityPresenter(final MainActivity mainActivity, Context context,
                                 ActivityMainDrawerBinding binding, String title, TextView toolbarTitle) {
        this.mBinding = binding;
        this.mMainActivity = mainActivity;
        this.mToolbarTitle = toolbarTitle;
        this.sContext = context;

        mTitleList = new ArrayList<>();
        mTitleList.add(OPTIONAL);
        mTitleList.add(DOMINANT);
        mTitleList.add(SHANGHAI);
        mTitleList.add(NENGYUAN);
        mTitleList.add(DALIAN);
        mTitleList.add(ZHENGZHOU);
        mTitleList.add(ZHONGJIN);
        mTitleList.add(DALIANZUHE);
        mTitleList.add(ZHENGZHOUZUHE);

        //取消返回键
        mainActivity.getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        mainActivity.getSupportActionBar().setHomeButtonEnabled(false);

        //初始化合约列表导航
        mBinding.rvQuoteNavigation.setLayoutManager(new LinearLayoutManagerWithSmoothScroller(mMainActivity));
        mNavAdapter = new QuoteNavAdapter(mMainActivity, mInsListNameNav);
        mBinding.rvQuoteNavigation.setAdapter(mNavAdapter);

        //添加合约页，设置首页是自选合约列表页
        List<Fragment> fragmentList = new ArrayList<>();
        fragmentList.add(QuoteFragment.newInstance(title));
        fragmentList.add(AccountFragment.newInstance());
        //初始化适配器类
        mViewPagerFragmentAdapter = new ViewPagerFragmentAdapter(mMainActivity.getSupportFragmentManager(), fragmentList);
        mBinding.vpContent.setAdapter(mViewPagerFragmentAdapter);
        mBinding.vpContent.setCurrentItem(0);
        switchQuotesNavigation(title);

        //设置右导航宽度
        ViewGroup.LayoutParams paramsR = mBinding.nvMenuRight.getLayoutParams();
        paramsR.width = mMainActivity.getResources().getDisplayMetrics().widthPixels / 2;
        mBinding.nvMenuRight.setLayoutParams(paramsR);

        //加载右导航图标
        mBinding.navigationRightRv.setLayoutManager(new LinearLayoutManager(mMainActivity));
        mBinding.navigationRightRv.setItemAnimator(new DefaultItemAnimator());
        NavigationRightEntity menu0 = new NavigationRightEntity();
        menu0.setIcon(R.mipmap.ic_account_circle_white_18dp);
        menu0.setContent(CommonConstants.LOGOUT);
        NavigationRightEntity menu1 = new NavigationRightEntity();
        menu1.setIcon(R.mipmap.ic_settings_white_18dp);
        menu1.setContent(CommonConstants.SETTING);
        NavigationRightEntity menu2 = new NavigationRightEntity();
        menu2.setIcon(R.mipmap.ic_assessment_white_18dp);
        menu2.setContent(CommonConstants.ACCOUNT);
        NavigationRightEntity menu3 = new NavigationRightEntity();
        menu3.setIcon(R.mipmap.ic_assignment_turned_in_white_18dp);
        menu3.setContent(CommonConstants.PASSWORD);
        NavigationRightEntity menu4 = new NavigationRightEntity();
        menu4.setIcon(R.mipmap.ic_donut_large_white_18dp);
        menu4.setContent(CommonConstants.POSITION);
        NavigationRightEntity menu5 = new NavigationRightEntity();
        menu5.setIcon(R.mipmap.ic_description_white_18dp);
        menu5.setContent(CommonConstants.TRADE);
        NavigationRightEntity menu6 = new NavigationRightEntity();
        menu6.setIcon(R.mipmap.ic_account_balance_white_18dp);
        menu6.setContent(CommonConstants.BANK);
        NavigationRightEntity menu7 = new NavigationRightEntity();
        menu7.setIcon(R.mipmap.ic_supervisor_account_white_18dp);
        menu7.setContent(CommonConstants.OPEN_ACCOUNT);
        NavigationRightEntity menu8 = new NavigationRightEntity();
        menu8.setIcon(R.mipmap.ic_find_in_page_white_18dp);
        menu8.setContent(CommonConstants.FEEDBACK);
        NavigationRightEntity menu9 = new NavigationRightEntity();
        menu9.setIcon(R.mipmap.ic_info_white_18dp);
        menu9.setContent(CommonConstants.ABOUT);
        List<NavigationRightEntity> list = new ArrayList<>();
        list.add(menu0);
        list.add(menu1);
        list.add(menu2);
        list.add(menu3);
        list.add(menu4);
        list.add(menu5);
        list.add(menu6);
        list.add(menu7);
        list.add(menu8);
        list.add(menu9);
        mNavigationRightAdapter = new NavigationRightAdapter(mainActivity, list);
        mBinding.navigationRightRv.setAdapter(mNavigationRightAdapter);

    }

    /**
     * date: 1/18/18
     * author: chenli
     * description: 注册各类监听事件
     */
    public void registerListeners() {
        //合约导航左右移动
        mBinding.quoteNavLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mBinding.rvQuoteNavigation.smoothScrollBy(-mBinding.rvQuoteNavigation.getMeasuredWidth(), 0);

            }
        });
        mBinding.quoteNavRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mBinding.rvQuoteNavigation.smoothScrollBy(mBinding.rvQuoteNavigation.getMeasuredWidth(), 0);
            }
        });

        //为底部合约导航栏添加监听事件
        mBinding.rvQuoteNavigation.addOnItemTouchListener(new SimpleRecyclerViewItemClickListener(
                mBinding.rvQuoteNavigation, new SimpleRecyclerViewItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                String instrumentId = (String) view.getTag();
                scrollQuotes(mToolbarTitle.getText().toString(), position, instrumentId);
            }

            @Override
            public void onItemLongClick(View view, int position) {
            }
        }));

        mBinding.navigationRightRv.addOnItemTouchListener(new SimpleRecyclerViewItemClickListener(
                mBinding.navigationRightRv, new SimpleRecyclerViewItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                String title = (String) view.getTag();
                switch (title) {
                    case CommonConstants.LOGOUT:
                        SPUtils.putAndApply(sContext, CommonConstants.CONFIG_LOGIN_DATE, "");
                        BaseApplication.getWebSocketService().reConnectTD();
                        mMainActivity.finish();
                        break;
                    case CommonConstants.SETTING:
                        Intent intentSetting = new Intent(mMainActivity, SettingActivity.class);
                        mMainActivity.startActivity(intentSetting);
                        break;
                    case CommonConstants.ACCOUNT:
                        Intent intentAcc = new Intent(mMainActivity, AccountActivity.class);
                        mMainActivity.startActivity(intentAcc);
                        break;
                    case CommonConstants.PASSWORD:
                        Intent intentChange = new Intent(mMainActivity, ChangePasswordActivity.class);
                        mMainActivity.startActivity(intentChange);
                        break;
                    case CommonConstants.POSITION:
                        try {
                            mIns = DataManager.getInstance().getRtnData().getIns_list();
                            Intent intentPos = new Intent(mMainActivity, FutureInfoActivity.class);
                            intentPos.putExtra("nav_position", 1);
                            String instrument_id = new ArrayList<>(LatestFileManager.getMainInsList().keySet()).get(0);
                            intentPos.putExtra("instrument_id", instrument_id);
                            mMainActivity.startActivityForResult(intentPos, POSITION_JUMP_TO_FUTURE_INFO_ACTIVITY);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        break;
                    case CommonConstants.TRADE:
                        Intent intentDeal = new Intent(mMainActivity, TradeActivity.class);
                        mMainActivity.startActivity(intentDeal);
                        break;
                    case CommonConstants.BANK:
                        Intent intentBank = new Intent(mMainActivity, BankTransferActivity.class);
                        mMainActivity.startActivity(intentBank);
                        break;
                    case CommonConstants.OPEN_ACCOUNT:
//                        Intent intentOpenAccount = new Intent(mMainActivity, OpenAccountActivity.class);
                        Intent intentOpenAccount = mMainActivity.getPackageManager().getLaunchIntentForPackage("com.cfmmc.app.sjkh");
                        if (intentOpenAccount != null)
                            mMainActivity.startActivity(intentOpenAccount);
                        else {
                            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://appficaos.cfmmc.com/apps/download.html"));
                            mMainActivity.startActivity(browserIntent);
                        }
                        break;
                    case CommonConstants.FEEDBACK:
                        Intent intentFeed = new Intent(mMainActivity, FeedBackActivity.class);
                        mMainActivity.startActivity(intentFeed);
                        break;
                    case CommonConstants.ABOUT:
                        Intent intentAbout = new Intent(mMainActivity, AboutActivity.class);
                        mMainActivity.startActivity(intentAbout);
                        break;
                    default:
                        break;

                }
            }

            @Override
            public void onItemLongClick(View view, int position) {

            }
        }
        ));

        //为toolbar设置一个弹出框，用于显示自选合约列表，点击切换合约信息
        mToolbarTitle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mTitleDialog == null) {
                    //初始化自选合约弹出框
                    mTitleDialog = new Dialog(mMainActivity, R.style.Theme_Light_Dialog);
                    View viewDialog = View.inflate(mMainActivity, R.layout.view_dialog_optional_quote, null);
                    Window dialogWindow = mTitleDialog.getWindow();
                    if (dialogWindow != null) {
                        dialogWindow.getDecorView().setPadding(0, 0, 0, 0);
                        WindowManager.LayoutParams lp = dialogWindow.getAttributes();
                        dialogWindow.setGravity(Gravity.BOTTOM);
                        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
                        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
                        dialogWindow.setAttributes(lp);
                    }
                    mTitleDialog.setContentView(viewDialog);
                    mTitleDialogAdapter = new DialogAdapter(mMainActivity, mTitleList);
                    TextView textView = viewDialog.findViewById(R.id.dialog_hint);
                    textView.setText("选择期货交易所");
                    mTitleRecyclerView = viewDialog.findViewById(R.id.dialog_rv);
                    mTitleRecyclerView.setLayoutManager(
                            new GridLayoutManager(mMainActivity, 3));
                    mTitleRecyclerView.addItemDecoration(
                            new DividerGridItemDecorationUtils(mMainActivity, R.drawable.divider_optional_dialog));
                    mTitleRecyclerView.setAdapter(mTitleDialogAdapter);

                    mTitleRecyclerView.addOnItemTouchListener(
                            new SimpleRecyclerViewItemClickListener(mTitleRecyclerView,
                                    new SimpleRecyclerViewItemClickListener.OnItemClickListener() {
                                        @Override
                                        public void onItemClick(View view, int position) {
                                            String title = mTitleList.get(position);
                                            switchQuotesNavigation(title);
                                            ((QuoteFragment) mViewPagerFragmentAdapter.getItem(0))
                                                    .switchQuotePage(title);
                                            mTitleDialog.dismiss();
                                        }

                                        @Override
                                        public void onItemLongClick(View view, int position) {

                                        }
                                    }));

                }
                if (!mTitleDialog.isShowing()) mTitleDialog.show();
            }
        });

        mBinding.bottomNavigation.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.market:
                        mBinding.vpContent.setCurrentItem(0, false);
                        String title = ((QuoteFragment) mViewPagerFragmentAdapter.getItem(0)).getTitle();
                        if (OPTIONAL.equals(title)) {
                            mBinding.rvQuoteNavigation.setVisibility(View.GONE);
                            mBinding.quoteNavLeft.setVisibility(View.GONE);
                            mBinding.quoteNavRight.setVisibility(View.GONE);
                        } else {
                            mBinding.rvQuoteNavigation.setVisibility(View.VISIBLE);
                            mBinding.quoteNavLeft.setVisibility(View.VISIBLE);
                            mBinding.quoteNavRight.setVisibility(View.VISIBLE);
                        }
                        mToolbarTitle.setText(title);
                        break;
                    case R.id.trade:
                        mBinding.rvQuoteNavigation.setVisibility(View.GONE);
                        mBinding.quoteNavLeft.setVisibility(View.GONE);
                        mBinding.quoteNavRight.setVisibility(View.GONE);
                        mBinding.vpContent.setCurrentItem(1, false);
                        mToolbarTitle.setText("账户详情");
                        break;
                    default:
                        break;
                }
                return true;
            }
        });

        //重复点击不切换页面
        mBinding.bottomNavigation.setOnNavigationItemReselectedListener(new BottomNavigationView.OnNavigationItemReselectedListener() {
            @Override
            public void onNavigationItemReselected(@NonNull MenuItem item) {

            }
        });

        mBinding.vpContent.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                mBinding.vpContent.setCurrentItem(position, false);
                if (position == 1){
                    mBinding.bottomNavigation.setSelectedItemId(R.id.trade);
                    mBinding.rvQuoteNavigation.setVisibility(View.GONE);
                    mBinding.quoteNavLeft.setVisibility(View.GONE);
                    mBinding.quoteNavRight.setVisibility(View.GONE);
                    mToolbarTitle.setText("账户详情");
                }else {
                    mBinding.bottomNavigation.setSelectedItemId(R.id.market);
                    String title = ((QuoteFragment) mViewPagerFragmentAdapter.getItem(0)).getTitle();
                    if (OPTIONAL.equals(title)) {
                        mBinding.rvQuoteNavigation.setVisibility(View.GONE);
                        mBinding.quoteNavLeft.setVisibility(View.GONE);
                        mBinding.quoteNavRight.setVisibility(View.GONE);
                    } else {
                        mBinding.rvQuoteNavigation.setVisibility(View.VISIBLE);
                        mBinding.quoteNavLeft.setVisibility(View.VISIBLE);
                        mBinding.quoteNavRight.setVisibility(View.VISIBLE);
                    }
                    mToolbarTitle.setText(title);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });

        //右导航监听器
        mBinding.drawerLayout.addDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {

            }

            @Override
            public void onDrawerOpened(View drawerView) {

            }

            @Override
            public void onDrawerClosed(View drawerView) {
                ((QuoteFragment) mViewPagerFragmentAdapter.getItem(0)).setmIsUpdate(true);
            }

            @Override
            public void onDrawerStateChanged(int newState) {
                ((QuoteFragment) mViewPagerFragmentAdapter.getItem(0)).setmIsUpdate(false);
            }
        });
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
    public void switchQuotesNavigation(String mTitle) {
        if (NetworkUtils.isNetworkConnected(sContext))
            mToolbarTitle.setText(mTitle);
        if (OPTIONAL.equals(mTitle)) {
            mBinding.rvQuoteNavigation.setVisibility(View.GONE);
            mBinding.quoteNavLeft.setVisibility(View.GONE);
            mBinding.quoteNavRight.setVisibility(View.GONE);
        } else {
            mBinding.rvQuoteNavigation.setVisibility(View.VISIBLE);
            mBinding.quoteNavLeft.setVisibility(View.VISIBLE);
            mBinding.quoteNavRight.setVisibility(View.VISIBLE);
        }
        switch (mTitle) {
            case DOMINANT:
                mInsListNameNav = LatestFileManager.getMainInsListNameNav();
                break;
            case SHANGHAI:
                mInsListNameNav = LatestFileManager.getShangqiInsListNameNav();
                break;
            case NENGYUAN:
                mInsListNameNav = LatestFileManager.getNengyuanInsListNameNav();
                break;
            case DALIAN:
                mInsListNameNav = LatestFileManager.getDalianInsListNameNav();
                break;
            case ZHENGZHOU:
                mInsListNameNav = LatestFileManager.getZhengzhouInsListNameNav();
                break;
            case ZHONGJIN:
                mInsListNameNav = LatestFileManager.getZhongjinInsListNameNav();
                break;
            case DALIANZUHE:
                mInsListNameNav = LatestFileManager.getDalianzuheInsListNameNav();
                break;
            case ZHENGZHOUZUHE:
                mInsListNameNav = LatestFileManager.getZhengzhouzuheInsListNameNav();
                break;
            default:
                break;
        }
        if (!OPTIONAL.equals(mTitle)) mNavAdapter.updateList(mInsListNameNav);

    }

    public String getPreSubscribedQuotes() {
        return mIns;
    }

    public void setPreSubscribedQuotes(String mIns) {
        this.mIns = mIns;
    }

    public ViewPagerFragmentAdapter getmViewPagerFragmentAdapter() {
        return mViewPagerFragmentAdapter;
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

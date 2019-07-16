package com.shinnytech.futures.controller.activity;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Rect;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.LinearSmoothScroller;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.shinnytech.futures.R;
import com.shinnytech.futures.amplitude.api.Amplitude;
import com.shinnytech.futures.application.BaseApplication;
import com.shinnytech.futures.constants.CommonConstants;
import com.shinnytech.futures.controller.fragment.AccountFragment;
import com.shinnytech.futures.controller.fragment.FutureInfoFragment;
import com.shinnytech.futures.controller.fragment.QuotePagerFragment;
import com.shinnytech.futures.databinding.ActivityMainDrawerBinding;
import com.shinnytech.futures.model.adapter.DialogAdapter;
import com.shinnytech.futures.model.adapter.NavigationRightAdapter;
import com.shinnytech.futures.model.adapter.QuoteNavAdapter;
import com.shinnytech.futures.model.adapter.ViewPagerFragmentAdapter;
import com.shinnytech.futures.model.bean.accountinfobean.PositionEntity;
import com.shinnytech.futures.model.bean.accountinfobean.UserEntity;
import com.shinnytech.futures.model.bean.eventbusbean.IdEvent;
import com.shinnytech.futures.model.bean.eventbusbean.PositionEvent;
import com.shinnytech.futures.model.bean.searchinfobean.SearchEntity;
import com.shinnytech.futures.model.bean.settingbean.NavigationRightEntity;
import com.shinnytech.futures.model.engine.DataManager;
import com.shinnytech.futures.model.engine.LatestFileManager;
import com.shinnytech.futures.model.listener.SimpleRecyclerViewItemClickListener;
import com.shinnytech.futures.utils.DividerGridItemDecorationUtils;
import com.shinnytech.futures.utils.SPUtils;
import com.shinnytech.futures.utils.ScreenUtils;
import com.shinnytech.futures.utils.ToastUtils;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static com.shinnytech.futures.constants.CommonConstants.ABOUT;
import static com.shinnytech.futures.constants.CommonConstants.ACCOUNT;
import static com.shinnytech.futures.constants.CommonConstants.ACCOUNT_DETAIL;
import static com.shinnytech.futures.constants.CommonConstants.AMP_EVENT_LOGIN_TYPE;
import static com.shinnytech.futures.constants.CommonConstants.AMP_EVENT_MENU;
import static com.shinnytech.futures.constants.CommonConstants.AMP_EVENT_PAGE_ID;
import static com.shinnytech.futures.constants.CommonConstants.AMP_EVENT_PAGE_ID_VALUE_MAIN;
import static com.shinnytech.futures.constants.CommonConstants.AMP_EVENT_SWITCH_FROM;
import static com.shinnytech.futures.constants.CommonConstants.AMP_EVENT_SWITCH_TO;
import static com.shinnytech.futures.constants.CommonConstants.AMP_EVENT_TAB_ACCOUNT;
import static com.shinnytech.futures.constants.CommonConstants.AMP_EVENT_TAB_MARKET;
import static com.shinnytech.futures.constants.CommonConstants.AMP_LOGOUT;
import static com.shinnytech.futures.constants.CommonConstants.AMP_MENU;
import static com.shinnytech.futures.constants.CommonConstants.AMP_SWITCH_TAB;
import static com.shinnytech.futures.constants.CommonConstants.CONDITIONAL_ORDER;
import static com.shinnytech.futures.constants.CommonConstants.DALIAN;
import static com.shinnytech.futures.constants.CommonConstants.DALIANZUHE;
import static com.shinnytech.futures.constants.CommonConstants.DOMINANT;
import static com.shinnytech.futures.constants.CommonConstants.FEEDBACK;
import static com.shinnytech.futures.constants.CommonConstants.LOGOUT;
import static com.shinnytech.futures.constants.CommonConstants.MAIN_ACTIVITY_TO_OPTIONAL_SETTING_ACTIVITY;
import static com.shinnytech.futures.constants.CommonConstants.MAIN_ACTIVITY_TO_SETTING_ACTIVITY;
import static com.shinnytech.futures.constants.CommonConstants.MENU_TITLE_COLLECT;
import static com.shinnytech.futures.constants.CommonConstants.MENU_TITLE_NAVIGATION;
import static com.shinnytech.futures.constants.CommonConstants.NENGYUAN;
import static com.shinnytech.futures.constants.CommonConstants.OPEN_ACCOUNT;
import static com.shinnytech.futures.constants.CommonConstants.OPTIONAL;
import static com.shinnytech.futures.constants.CommonConstants.OPTIONAL_SETTING;
import static com.shinnytech.futures.constants.CommonConstants.PASSWORD;
import static com.shinnytech.futures.constants.CommonConstants.SETTING;
import static com.shinnytech.futures.constants.CommonConstants.SHANGHAI;
import static com.shinnytech.futures.constants.CommonConstants.TRANSFER_DIRECTION;
import static com.shinnytech.futures.constants.CommonConstants.TRANSFER_IN;
import static com.shinnytech.futures.constants.CommonConstants.TRANSFER_OUT;
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
    private String mInstrumentId;
    private DataManager sDataManager;
    private boolean mIsUpdate;

    public MainActivityPresenter(final MainActivity mainActivity, Context context,
                                 ActivityMainDrawerBinding binding, TextView toolbarTitle) {
        this.mBinding = binding;
        this.mMainActivity = mainActivity;
        this.mToolbarTitle = toolbarTitle;
        this.sContext = context;
        this.sDataManager = DataManager.getInstance();
        this.mIsUpdate = true;

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
        fragmentList.add(new QuotePagerFragment());
        fragmentList.add(new AccountFragment());
        fragmentList.add(new FutureInfoFragment());
        //初始化适配器类
        mViewPagerFragmentAdapter = new ViewPagerFragmentAdapter(mMainActivity.getSupportFragmentManager(), fragmentList);
        mBinding.vpContent.setAdapter(mViewPagerFragmentAdapter);
        mBinding.vpContent.setPagingEnabled(false);
        //保证lazyLoad的效用,每次加载一个fragment
        mBinding.vpContent.setOffscreenPageLimit(3);

        //设置右导航宽度
        ViewGroup.LayoutParams paramsR = mBinding.nvMenuRight.getLayoutParams();
        paramsR.width = mMainActivity.getResources().getDisplayMetrics().widthPixels / 2;
        mBinding.nvMenuRight.setLayoutParams(paramsR);

        //加载右导航图标
        mBinding.navigationRightRv.setLayoutManager(new LinearLayoutManager(mMainActivity));
        mBinding.navigationRightRv.setItemAnimator(new DefaultItemAnimator());
        NavigationRightEntity menu0 = new NavigationRightEntity();
        menu0.setIcon(R.mipmap.ic_perm_identity_white_18dp);
        menu0.setContent(LOGOUT);
        NavigationRightEntity menu1 = new NavigationRightEntity();
        menu1.setIcon(R.mipmap.ic_settings_white_18dp);
        menu1.setContent(CommonConstants.SETTING);
        NavigationRightEntity menu9 = new NavigationRightEntity();
        menu9.setIcon(R.mipmap.ic_build_white_18dp);
        menu9.setContent(OPTIONAL_SETTING);
        NavigationRightEntity menu2 = new NavigationRightEntity();
        menu2.setIcon(R.mipmap.ic_account_balance_white_18dp);
        menu2.setContent(ACCOUNT);
        NavigationRightEntity menu3 = new NavigationRightEntity();
        menu3.setIcon(R.mipmap.ic_fingerprint_white_18dp);
        menu3.setContent(PASSWORD);
        NavigationRightEntity menu4 = new NavigationRightEntity();
        menu4.setIcon(R.mipmap.ic_flight_land_white_18dp);
        menu4.setContent(CommonConstants.TRANSFER_IN);
        NavigationRightEntity menu5 = new NavigationRightEntity();
        menu5.setIcon(R.mipmap.ic_flight_takeoff_white_18dp);
        menu5.setContent(CommonConstants.TRANSFER_OUT);
        NavigationRightEntity menu10 = new NavigationRightEntity();
        menu10.setIcon(R.mipmap.ic_settings_remote_white_18dp);
        menu10.setContent(CONDITIONAL_ORDER);
        NavigationRightEntity menu6 = new NavigationRightEntity();
        menu6.setIcon(R.mipmap.ic_supervisor_account_white_18dp);
        menu6.setContent(OPEN_ACCOUNT);
        NavigationRightEntity menu7 = new NavigationRightEntity();
        menu7.setIcon(R.mipmap.ic_visibility_white_18dp);
        menu7.setContent(FEEDBACK);
        NavigationRightEntity menu8 = new NavigationRightEntity();
        menu8.setIcon(R.mipmap.ic_info_white_18dp);
        menu8.setContent(ABOUT);
        List<NavigationRightEntity> list = new ArrayList<>();
        list.add(menu0);
        list.add(menu1);
        list.add(menu9);
        list.add(menu2);
        list.add(menu3);
        list.add(menu4);
        list.add(menu5);
        list.add(menu10);
        list.add(menu6);
        list.add(menu7);
        list.add(menu8);
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
                JSONObject jsonObject = new JSONObject();
                switch (title) {
                    case LOGOUT:
                        SPUtils.putAndApply(sContext, CommonConstants.CONFIG_PASSWORD, "");
                        try {
                            jsonObject.put(AMP_EVENT_LOGIN_TYPE, sDataManager.LOGIN_TYPE);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        Amplitude.getInstance().logEventWrap(AMP_LOGOUT, jsonObject);
                        mMainActivity.startActivity(new Intent(mMainActivity, LoginActivity.class));
                        mMainActivity.finish();
                        BaseApplication.getmTDWebSocket().reConnect();
                        break;
                    case SETTING:
                        try {
                            jsonObject.put(AMP_EVENT_MENU, SETTING);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        Amplitude.getInstance().logEventWrap(AMP_MENU, jsonObject);
                        Intent intentSetting = new Intent(mMainActivity, SettingActivity.class);
                        mMainActivity.startActivityForResult(intentSetting, MAIN_ACTIVITY_TO_SETTING_ACTIVITY);
                        break;
                    case OPTIONAL_SETTING:
                        try {
                            jsonObject.put(AMP_EVENT_MENU, OPTIONAL_SETTING);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        Amplitude.getInstance().logEventWrap(AMP_MENU, jsonObject);
                        Intent intentSettingOptional = new Intent(mMainActivity, OptionalActivity.class);
                        mMainActivity.startActivityForResult(intentSettingOptional, MAIN_ACTIVITY_TO_OPTIONAL_SETTING_ACTIVITY);
                        break;
                    case ACCOUNT:
                        try {
                            jsonObject.put(AMP_EVENT_MENU, ACCOUNT);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        Amplitude.getInstance().logEventWrap(AMP_MENU, jsonObject);
                        Intent intentAcc = new Intent(mMainActivity, AccountActivity.class);
                        mMainActivity.startActivity(intentAcc);
                        break;
                    case PASSWORD:
                        try {
                            jsonObject.put(AMP_EVENT_MENU, PASSWORD);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        Amplitude.getInstance().logEventWrap(AMP_MENU, jsonObject);
                        Intent intentChange = new Intent(mMainActivity, ChangePasswordActivity.class);
                        mMainActivity.startActivity(intentChange);
                        break;
                    case TRANSFER_IN:
                        try {
                            jsonObject.put(AMP_EVENT_MENU, TRANSFER_IN);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        Amplitude.getInstance().logEventWrap(AMP_MENU, jsonObject);
                        Intent intentBank = new Intent(mMainActivity, BankTransferActivity.class);
                        intentBank.putExtra(TRANSFER_DIRECTION, TRANSFER_IN);
                        mMainActivity.startActivity(intentBank);
                        break;
                    case TRANSFER_OUT:
                        try {
                            jsonObject.put(AMP_EVENT_MENU, TRANSFER_OUT);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        Amplitude.getInstance().logEventWrap(AMP_MENU, jsonObject);
                        Intent intentBankOut = new Intent(mMainActivity, BankTransferActivity.class);
                        intentBankOut.putExtra(TRANSFER_DIRECTION, TRANSFER_OUT);
                        mMainActivity.startActivity(intentBankOut);
                        break;
                    case CONDITIONAL_ORDER:
                        ToastUtils.showToast(sContext, "敬请期待！");
                        break;
                    case OPEN_ACCOUNT:
                        try {
                            jsonObject.put(AMP_EVENT_MENU, OPEN_ACCOUNT);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        Amplitude.getInstance().logEventWrap(AMP_MENU, jsonObject);
                        Intent intentOpenAccount = mMainActivity.getPackageManager().getLaunchIntentForPackage("com.cfmmc.app.sjkh");
                        if (intentOpenAccount != null)
                            mMainActivity.startActivity(intentOpenAccount);
                        else {
                            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://appficaos.cfmmc.com/apps/download.html"));
                            mMainActivity.startActivity(browserIntent);
                        }
                        break;
                    case FEEDBACK:
                        try {
                            jsonObject.put(AMP_EVENT_MENU, FEEDBACK);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        Amplitude.getInstance().logEventWrap(AMP_MENU, jsonObject);
                        Intent intentFeed = new Intent(mMainActivity, FeedBackActivity.class);
                        mMainActivity.startActivity(intentFeed);
                        break;
                    case ABOUT:
                        try {
                            jsonObject.put(AMP_EVENT_MENU, ABOUT);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        Amplitude.getInstance().logEventWrap(AMP_MENU, jsonObject);
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
                        dialogWindow.getDecorView().setPadding(0, getToolBarHeight(), 0, 0);
                        WindowManager.LayoutParams lp = dialogWindow.getAttributes();
                        dialogWindow.setGravity(Gravity.TOP);
                        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
                        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
                        dialogWindow.setAttributes(lp);
                    }
                    mTitleDialog.setContentView(viewDialog);
                    mTitleDialogAdapter = new DialogAdapter(mMainActivity, new ArrayList<String>(), "");
                    mTitleRecyclerView = viewDialog.findViewById(R.id.dialog_rv);
                    mTitleRecyclerView.setLayoutManager(
                            new GridLayoutManager(mMainActivity, 3));
                    mTitleRecyclerView.addItemDecoration(
                            new DividerGridItemDecorationUtils(mMainActivity, R.drawable.activity_optional_quote_dialog));
                    mTitleRecyclerView.setAdapter(mTitleDialogAdapter);

                    mTitleRecyclerView.addOnItemTouchListener(
                            new SimpleRecyclerViewItemClickListener(mTitleRecyclerView,
                                    new SimpleRecyclerViewItemClickListener.OnItemClickListener() {
                                        @Override
                                        public void onItemClick(View view, int position) {
                                            if (mBinding.vpContent.getCurrentItem() == 0) {
                                                ((QuotePagerFragment) mViewPagerFragmentAdapter.
                                                        getItem(0)).setCurrentItem(position);
                                            } else if (mBinding.vpContent.getCurrentItem() == 2) {
                                                String instrumentId = (String) view.getTag();
                                                if (instrumentId != null && !instrumentId.equals(mInstrumentId)) {
                                                    changeToolbar(instrumentId);
                                                    IdEvent idEvent = new IdEvent();
                                                    idEvent.setInstrument_id(instrumentId);
                                                    EventBus.getDefault().post(idEvent);
                                                }
                                            }
                                            mTitleDialog.dismiss();
                                        }

                                        @Override
                                        public void onItemLongClick(View view, int position) {

                                        }
                                    }));

                }
                if (mBinding.vpContent.getCurrentItem() == 0) {
                    mTitleDialogAdapter.updateList(mTitleList, mToolbarTitle.getText().toString());
                    if (!mTitleDialog.isShowing()) mTitleDialog.show();
                } else if (mBinding.vpContent.getCurrentItem() == 2) {
                    List<String> list = LatestFileManager.readInsListFromFile();
                    UserEntity userEntity = sDataManager.getTradeBean().getUsers().get(sDataManager.USER_ID);
                    if (userEntity != null) {
                        for (PositionEntity positionEntity : userEntity.getPositions().values()) {
                            try {
                                int volume_long = Integer.parseInt(positionEntity.getVolume_long());
                                int volume_short = Integer.parseInt(positionEntity.getVolume_short());
                                String ins = positionEntity.getExchange_id() + "." + positionEntity.getInstrument_id();
                                if ((volume_long != 0 || volume_short != 0) && !list.contains(ins))
                                    list.add(ins);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    mTitleDialogAdapter.updateList(list, mInstrumentId);
                    if (!mTitleDialog.isShowing()) mTitleDialog.show();
                }
            }
        });

        mBinding.bottomNavigation.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.market:
                        switchToMarket();
                        JSONObject jsonObject = new JSONObject();
                        try {
                            jsonObject.put(AMP_EVENT_PAGE_ID, AMP_EVENT_PAGE_ID_VALUE_MAIN);
                            jsonObject.put(AMP_EVENT_SWITCH_FROM, AMP_EVENT_TAB_ACCOUNT);
                            jsonObject.put(AMP_EVENT_SWITCH_TO, AMP_EVENT_TAB_MARKET);
                            Amplitude.getInstance().logEventWrap(AMP_SWITCH_TAB, jsonObject);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        break;
                    case R.id.trade:
                        switchToAccount();
                        JSONObject jsonObject1 = new JSONObject();
                        try {
                            jsonObject1.put(AMP_EVENT_PAGE_ID, AMP_EVENT_PAGE_ID_VALUE_MAIN);
                            jsonObject1.put(AMP_EVENT_SWITCH_FROM, AMP_EVENT_TAB_MARKET);
                            jsonObject1.put(AMP_EVENT_SWITCH_TO, AMP_EVENT_TAB_ACCOUNT);
                            Amplitude.getInstance().logEventWrap(AMP_SWITCH_TAB, jsonObject1);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
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
                //从交易页链接而来
                if (mBinding.vpContent.getCurrentItem() == 2) {
                    mBinding.vpContent.setCurrentItem(1, false);
                }
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
                mIsUpdate = true;
            }

            @Override
            public void onDrawerStateChanged(int newState) {
                if (newState == 2) mIsUpdate = false;
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
        //保存最后一次切换的交易所，用户二级详情页上下滑动切换合约
        sDataManager.EXCHANGE_ID = mTitle;
        mToolbarTitle.setText(mTitle);
        Rect bounds = new Rect();
        Paint textPaint = new Paint();
        textPaint.setTextSize(ScreenUtils.sp2px(sContext, 25));
        textPaint.getTextBounds(mTitle, 0, mTitle.length(), bounds);
        int width = bounds.width() + ScreenUtils.dp2px(sContext, 25);
        ViewGroup.LayoutParams layoutParams = mToolbarTitle.getLayoutParams();
        layoutParams.height = sContext.getResources().getDimensionPixelSize(R.dimen.text_view_height);
        layoutParams.width = width;
        mToolbarTitle.setLayoutParams(layoutParams);
        if (OPTIONAL.equals(mTitle)) mBinding.llNavigation.setVisibility(View.GONE);
        else mBinding.llNavigation.setVisibility(View.VISIBLE);

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

    /**
     * date: 2019/7/2
     * author: chenli
     * description: 点击某个具体合约时改变标题
     */
    public void changeToolbar(String instrumentId) {
        mInstrumentId = instrumentId;
        MenuItem menuItem = mMainActivity.menu.getItem(1);
        menuItem.setTitle(MENU_TITLE_COLLECT);
        if (LatestFileManager.getOptionalInsList().containsKey(mInstrumentId)) {
            menuItem.setIcon(R.mipmap.ic_favorite_white_24dp);
        } else {
            menuItem.setIcon(R.mipmap.ic_favorite_border_white_24dp);
        }
        mMainActivity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mMainActivity.getSupportActionBar().setHomeButtonEnabled(true);
        SearchEntity searchEntity = LatestFileManager.getSearchEntities().get(mInstrumentId);
        if (searchEntity != null) mToolbarTitle.setText(searchEntity.getInstrumentName());
        else mToolbarTitle.setText(mInstrumentId);
        mToolbarTitle.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.mipmap.ic_exchange_down, 0);
    }

    /**
     * date: 2019/7/3
     * author: chenli
     * description: 切换到行情页
     */
    public void switchToMarket() {
        QuotePagerFragment quotePagerFragment = ((QuotePagerFragment) mViewPagerFragmentAdapter.getItem(0));
        String title = quotePagerFragment.getmTitle();
        mMainActivity.setTitle(title);
        if (OPTIONAL.equals(title))
            mBinding.llNavigation.setVisibility(View.GONE);
        else mBinding.llNavigation.setVisibility(View.VISIBLE);
        mBinding.bottomNavigation.setVisibility(View.VISIBLE);
        mBinding.vpContent.setCurrentItem(0, false);
        mToolbarTitle.setText(title);
        mToolbarTitle.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.mipmap.ic_exchange_down, 0);
    }

    /**
     * date: 2019/7/3
     * author: chenli
     * description: 切换到账户页
     */
    public void switchToAccount() {
        mMainActivity.setTitle(ACCOUNT_DETAIL);
        mBinding.llNavigation.setVisibility(View.GONE);
        mBinding.bottomNavigation.setVisibility(View.VISIBLE);
        mBinding.vpContent.setCurrentItem(1, false);
        mToolbarTitle.setText(ACCOUNT_DETAIL);
        mToolbarTitle.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
    }

    /**
     * date: 2019/7/3
     * author: chenli
     * description: 从交易页链接而来
     */
    public void linkToAccount() {
        mBinding.bottomNavigation.setVisibility(View.VISIBLE);
        MenuItem menuItem = mMainActivity.menu.getItem(1);
        menuItem.setIcon(R.mipmap.ic_menu);
        menuItem.setTitle(MENU_TITLE_NAVIGATION);
        mMainActivity.getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        mMainActivity.getSupportActionBar().setHomeButtonEnabled(false);
        AccountFragment accountFragment = (AccountFragment) mViewPagerFragmentAdapter.getItem(1);
        accountFragment.getmBinding().accountFab.show();
        mBinding.bottomNavigation.setSelectedItemId(R.id.trade);
        mToolbarTitle.setText(ACCOUNT_DETAIL);
        mToolbarTitle.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
    }

    /**
     * date: 2019/7/3
     * author: chenli
     * description: 切换到合约详情页
     */
    public void switchToFutureInfo(String instrument_id) {
        setPreSubscribedQuotes(sDataManager.getRtnData().getIns_list());
        changeToolbar(instrument_id);
        mBinding.llNavigation.setVisibility(View.GONE);
        mBinding.bottomNavigation.setVisibility(View.GONE);
        mBinding.vpContent.setCurrentItem(2, false);
    }

    /**
     * date: 2019/7/3
     * author: chenli
     * description: 账户页链接到合约详情页
     */
    public void linkToFutureInfo() {
        MenuItem menuItem = mMainActivity.menu.getItem(1);
        menuItem.setTitle(MENU_TITLE_COLLECT);
        if (LatestFileManager.getOptionalInsList().containsKey(mInstrumentId)) {
            menuItem.setIcon(R.mipmap.ic_favorite_white_24dp);
        } else {
            menuItem.setIcon(R.mipmap.ic_favorite_border_white_24dp);
        }
        mMainActivity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mMainActivity.getSupportActionBar().setHomeButtonEnabled(true);
        SearchEntity searchEntity = LatestFileManager.getSearchEntities().get(mInstrumentId);
        if (searchEntity != null) mToolbarTitle.setText(searchEntity.getInstrumentName());
        else mToolbarTitle.setText(mInstrumentId);
        mToolbarTitle.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.mipmap.ic_exchange_down, 0);

        mBinding.llNavigation.setVisibility(View.GONE);
        mBinding.bottomNavigation.setVisibility(View.GONE);
        mBinding.vpContent.setCurrentItem(2, false);
    }

    /**
     * date: 2019/4/17
     * author: chenli
     * description: 获取toolbar高度px
     */
    private int getToolBarHeight() {
        TypedValue tv = new TypedValue();
        if (mMainActivity.getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true)) {
            return TypedValue.complexToDimensionPixelSize(tv.data, mMainActivity.getResources().getDisplayMetrics());
        }
        return ScreenUtils.dp2px(sContext, 56);
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

    public TextView getmToolbarTitle() {
        return mToolbarTitle;
    }

    public String getmInstrumentId() {
        return mInstrumentId;
    }

    public ActivityMainDrawerBinding getmBinding() {
        return mBinding;
    }

    public boolean ismIsUpdate() {
        return mIsUpdate;
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

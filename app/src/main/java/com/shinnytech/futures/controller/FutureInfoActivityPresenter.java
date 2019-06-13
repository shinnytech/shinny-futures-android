package com.shinnytech.futures.controller;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.shinnytech.futures.R;
import com.shinnytech.futures.amplitude.api.Amplitude;
import com.shinnytech.futures.application.BaseApplication;
import com.shinnytech.futures.constants.CommonConstants;
import com.shinnytech.futures.controller.activity.FutureInfoActivity;
import com.shinnytech.futures.controller.activity.SubSettingActivity;
import com.shinnytech.futures.controller.fragment.BaseChartFragment;
import com.shinnytech.futures.controller.fragment.CurrentDayFragment;
import com.shinnytech.futures.controller.fragment.HandicapFragment;
import com.shinnytech.futures.controller.fragment.KlineFragment;
import com.shinnytech.futures.controller.fragment.OrderFragment;
import com.shinnytech.futures.controller.fragment.PositionFragment;
import com.shinnytech.futures.controller.fragment.TransactionFragment;
import com.shinnytech.futures.databinding.ActivityFutureInfoBinding;
import com.shinnytech.futures.model.adapter.DialogAdapter;
import com.shinnytech.futures.model.adapter.KlineDurationTitleAdapter;
import com.shinnytech.futures.model.adapter.ViewPagerFragmentAdapter;
import com.shinnytech.futures.model.bean.accountinfobean.PositionEntity;
import com.shinnytech.futures.model.bean.accountinfobean.UserEntity;
import com.shinnytech.futures.model.bean.eventbusbean.IdEvent;
import com.shinnytech.futures.model.bean.eventbusbean.VisibilityEvent;
import com.shinnytech.futures.model.bean.futureinfobean.QuoteEntity;
import com.shinnytech.futures.model.bean.searchinfobean.SearchEntity;
import com.shinnytech.futures.model.engine.DataManager;
import com.shinnytech.futures.model.engine.LatestFileManager;
import com.shinnytech.futures.model.listener.SimpleRecyclerViewItemClickListener;
import com.shinnytech.futures.utils.DividerGridItemDecorationUtils;
import com.shinnytech.futures.utils.SPUtils;
import com.shinnytech.futures.utils.ScreenUtils;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import static com.shinnytech.futures.constants.CommonConstants.AMP_EVENT_CURRENT_PAGE;
import static com.shinnytech.futures.constants.CommonConstants.AMP_EVENT_PAGE_VALUE_CHART_SETTING;
import static com.shinnytech.futures.constants.CommonConstants.AMP_EVENT_PAGE_VALUE_FUTURE_INFO;
import static com.shinnytech.futures.constants.CommonConstants.AMP_EVENT_TARGET_PAGE;
import static com.shinnytech.futures.constants.CommonConstants.AMP_SWITCH_PAGE;
import static com.shinnytech.futures.constants.CommonConstants.CHART_SETTING;
import static com.shinnytech.futures.constants.CommonConstants.CONFIG_MD5;
import static com.shinnytech.futures.constants.CommonConstants.CURRENT_DAY_FRAGMENT;
import static com.shinnytech.futures.constants.CommonConstants.DAY_FRAGMENT;
import static com.shinnytech.futures.constants.CommonConstants.FUTURE_INFO_ACTIVITY_TO_CHART_SETTING_ACTIVITY;
import static com.shinnytech.futures.constants.CommonConstants.HOUR_FRAGMENT;
import static com.shinnytech.futures.constants.CommonConstants.INS_BETWEEN_ACTIVITY;
import static com.shinnytech.futures.constants.CommonConstants.KLINE_10_MINUTE;
import static com.shinnytech.futures.constants.CommonConstants.KLINE_10_SECOND;
import static com.shinnytech.futures.constants.CommonConstants.KLINE_15_MINUTE;
import static com.shinnytech.futures.constants.CommonConstants.KLINE_15_SECOND;
import static com.shinnytech.futures.constants.CommonConstants.KLINE_1_DAY;
import static com.shinnytech.futures.constants.CommonConstants.KLINE_1_HOUR;
import static com.shinnytech.futures.constants.CommonConstants.KLINE_1_MINUTE;
import static com.shinnytech.futures.constants.CommonConstants.KLINE_20_SECOND;
import static com.shinnytech.futures.constants.CommonConstants.KLINE_28_DAY;
import static com.shinnytech.futures.constants.CommonConstants.KLINE_2_HOUR;
import static com.shinnytech.futures.constants.CommonConstants.KLINE_2_MINUTE;
import static com.shinnytech.futures.constants.CommonConstants.KLINE_30_MINUTE;
import static com.shinnytech.futures.constants.CommonConstants.KLINE_30_SECOND;
import static com.shinnytech.futures.constants.CommonConstants.KLINE_3_MINUTE;
import static com.shinnytech.futures.constants.CommonConstants.KLINE_3_SECOND;
import static com.shinnytech.futures.constants.CommonConstants.KLINE_4_HOUR;
import static com.shinnytech.futures.constants.CommonConstants.KLINE_5_MINUTE;
import static com.shinnytech.futures.constants.CommonConstants.KLINE_5_SECOND;
import static com.shinnytech.futures.constants.CommonConstants.KLINE_7_DAY;
import static com.shinnytech.futures.constants.CommonConstants.MINUTE_FRAGMENT;
import static com.shinnytech.futures.constants.CommonConstants.SECOND_FRAGMENT;
import static com.shinnytech.futures.constants.CommonConstants.SUB_SETTING_TYPE;

/**
 * Created on 1/17/18.
 * Created by chenli.
 * Description: .
 */

public class FutureInfoActivityPresenter {
    private final ViewPagerFragmentAdapter mInfoPagerAdapter;
    private final KlineDurationTitleAdapter mKlineDurationTitleAdapter;
    /**
     * date: 7/7/17
     * description: 合约代码
     */
    private String mInstrumentId;
    /**
     * date: 2019/2/27
     * author: chenli
     * description: 五档行情开关状态
     */
    private boolean mIsMD5;
    private ActivityFutureInfoBinding mBinding;
    private FutureInfoActivity mFutureInfoActivity;
    private Context sContext;
    private TextView mToolbarTitle;
    private FragmentManager mFragmentManager;
    private Dialog mDialogOptional;
    private RecyclerView mRecyclerViewOptional;
    private DialogAdapter mDialogAdapterOptional;
    private String[] mKlineTitle = CommonConstants.KLINE_DURATION_ALL.split(",");
    private String[] mKlineDuration = new String[]{KLINE_3_SECOND, KLINE_5_SECOND, KLINE_10_SECOND,
            KLINE_15_SECOND, KLINE_20_SECOND, KLINE_30_SECOND, KLINE_1_MINUTE, KLINE_2_MINUTE, KLINE_3_MINUTE,
            KLINE_5_MINUTE, KLINE_10_MINUTE, KLINE_15_MINUTE, KLINE_30_MINUTE, KLINE_1_HOUR, KLINE_2_HOUR,
            KLINE_4_HOUR, KLINE_1_DAY, KLINE_7_DAY, KLINE_28_DAY};

    public FutureInfoActivityPresenter(FutureInfoActivity futureInfoActivity, Context context, ActivityFutureInfoBinding binding, TextView toolbarTitle) {
        this.mBinding = binding;
        this.mFutureInfoActivity = futureInfoActivity;
        this.mToolbarTitle = toolbarTitle;
        this.sContext = context;

        mFragmentManager = mFutureInfoActivity.getSupportFragmentManager();
        BaseChartFragment currentDayFragment = new CurrentDayFragment();
        mFragmentManager.beginTransaction().
                setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN).
                add(R.id.fl_content_up, currentDayFragment, CommonConstants.CURRENT_DAY_FRAGMENT).commit();

        Intent intent = mFutureInfoActivity.getIntent();
        mInstrumentId = intent.getStringExtra(INS_BETWEEN_ACTIVITY);

        mToolbarTitle.setTextSize(20);
        //初始化五档行情控制
        initMD5ViewVisibility();

        //控制图表显示
        if (DataManager.getInstance().IS_SHOW_VP_CONTENT) {
            mBinding.vpInfoContent.setVisibility(View.VISIBLE);
            mBinding.rbHandicapInfo.setText(R.string.future_info_activity_handicap_down);
            mBinding.rbPositionInfo.setText(R.string.future_info_activity_position_down);
            mBinding.rbOrderInfoAlive.setText(R.string.future_info_activity_order_down_alive);
            mBinding.rbTransactionInfo.setText(R.string.future_info_activity_transaction_down);
        } else {
            mBinding.vpInfoContent.setVisibility(View.GONE);
            mBinding.rbHandicapInfo.setText(R.string.future_info_activity_handicap_up);
            mBinding.rbPositionInfo.setText(R.string.future_info_activity_position_up);
            mBinding.rbOrderInfoAlive.setText(R.string.future_info_activity_order_up_alive);
            mBinding.rbTransactionInfo.setText(R.string.future_info_activity_transaction_up);
        }

        //初始化K线类型
        String durationPre = (String) SPUtils.get(BaseApplication.getContext(), CommonConstants.CONFIG_KLINE_DURATION_DEFAULT, "");
        String[] durations = durationPre.split(",");
        List<String> list = new ArrayList<>();
        list.add(CommonConstants.KLINE_DURATION_DAY);
        for (String data : durations) {
            list.add(data);
        }
        mKlineDurationTitleAdapter = new KlineDurationTitleAdapter(sContext, list);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(sContext, LinearLayoutManager.HORIZONTAL, false);
        mBinding.rvDurationTitle.setLayoutManager(linearLayoutManager);
        mBinding.rvDurationTitle.setAdapter(mKlineDurationTitleAdapter);

        //初始化盘口、持仓、挂单、交易切换容器，fragment实例保存，有生命周期的变化，默认情况下屏幕外初始化两个fragment
        List<Fragment> fragmentList = new ArrayList<>();
        PositionFragment positionFragment = new PositionFragment();
        OrderFragment orderFragmentAlive = OrderFragment.newInstance(true);
        HandicapFragment handicapFragment = new HandicapFragment();
        TransactionFragment transactionFragment = new TransactionFragment();
        fragmentList.add(handicapFragment);
        fragmentList.add(positionFragment);
        fragmentList.add(orderFragmentAlive);
        fragmentList.add(transactionFragment);
        mInfoPagerAdapter = new ViewPagerFragmentAdapter(mFutureInfoActivity.getSupportFragmentManager(), fragmentList);
        mBinding.vpInfoContent.setAdapter(mInfoPagerAdapter);
        //设置初始化页为交易页，去除滑动效果
        mBinding.vpInfoContent.setCurrentItem(3, false);
        mBinding.rbTransactionInfo.setChecked(true);
        //由于盘口页和交易页需要通过eventBus实时监听合约代码的改变，当通过toolbar改变合约时，由于默认viewPager保存屏幕外一个页面，
        //盘口页和交易页相差两个页面，所以当显示其中一个的时候，另一个一定会消亡，所以打开时会初始化得到活动的最新合约代码。但是当通过
        //持仓页改变合约代码时会直接跳转到交易页，这个过程和活动更新合约代码同时发生，所以交易页通过初始化可能得不到最新合约代码， 必须
        //通过eventBus实时监控才行，所以要保持页面实例从而可以调用onEvent()方法
        mBinding.vpInfoContent.setOffscreenPageLimit(4);
    }

    /**
     * date: 2019/3/14
     * author: chenli
     * description: 初始化五档行情
     */
    public void initMD5ViewVisibility() {
        //显示五档行情
        if (!mInstrumentId.contains("SHFE") && !mInstrumentId.contains("INE"))
            mBinding.md.setVisibility(View.GONE);
        else {
            //判断有无五档行情
            QuoteEntity quoteEntity = DataManager.getInstance().getRtnData().getQuotes().get(mInstrumentId);
            if (quoteEntity != null && quoteEntity.getAsk_price5() == null) {
                SPUtils.putAndApply(sContext, CONFIG_MD5, false);
            }
            mIsMD5 = (boolean) SPUtils.get(sContext, CONFIG_MD5, true);
            if (mIsMD5) mBinding.md.setVisibility(View.VISIBLE);
            else mBinding.md.setVisibility(View.GONE);
        }
    }

    /**
     * date: 2019/3/14
     * author: chenli
     * description: 切换合约更新五档行情
     */
    public void updateMD5ViewVisibility() {
        //显示五档行情
        if (!mInstrumentId.contains("SHFE") && !mInstrumentId.contains("INE"))
            mBinding.md.setVisibility(View.GONE);
        else {
            mIsMD5 = (boolean) SPUtils.get(sContext, CONFIG_MD5, true);
            if (mIsMD5) mBinding.md.setVisibility(View.VISIBLE);
            else mBinding.md.setVisibility(View.GONE);
        }
    }

    public void registerListeners() {
        //为toolbar设置一个弹出框，用于显示自选合约列表，点击切换合约信息
        mToolbarTitle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mDialogOptional == null) {
                    //初始化自选合约弹出框
                    mDialogOptional = new Dialog(mFutureInfoActivity, R.style.Theme_Light_Dialog);
                    View viewDialog = View.inflate(mFutureInfoActivity, R.layout.view_dialog_optional_quote, null);
                    Window dialogWindow = mDialogOptional.getWindow();
                    if (dialogWindow != null) {
                        dialogWindow.getDecorView().setPadding(0, getToolBarHeight(), 0, 0);
                        WindowManager.LayoutParams lp = dialogWindow.getAttributes();
                        dialogWindow.setGravity(Gravity.TOP);
                        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
                        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
                        dialogWindow.setAttributes(lp);
                    }
                    mDialogOptional.setContentView(viewDialog);
                    List<String> list = new ArrayList<>(LatestFileManager.getOptionalInsList().keySet());
                    DataManager dataManager = DataManager.getInstance();
                    UserEntity userEntity = dataManager.getTradeBean().getUsers().get(dataManager.LOGIN_USER_ID);
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
                    mDialogAdapterOptional = new DialogAdapter(mFutureInfoActivity, list, mInstrumentId);
                    mRecyclerViewOptional = viewDialog.findViewById(R.id.dialog_rv);
                    mRecyclerViewOptional.setLayoutManager(
                            new GridLayoutManager(mFutureInfoActivity, 3));
                    mRecyclerViewOptional.addItemDecoration(
                            new DividerGridItemDecorationUtils(mFutureInfoActivity, R.drawable.activity_optional_quote_dialog));
                    mRecyclerViewOptional.setAdapter(mDialogAdapterOptional);

                    mRecyclerViewOptional.addOnItemTouchListener(
                            new SimpleRecyclerViewItemClickListener(mRecyclerViewOptional,
                                    new SimpleRecyclerViewItemClickListener.OnItemClickListener() {
                                        @Override
                                        public void onItemClick(View view, int position) {
                                            String instrumentId = (String) view.getTag();
                                            //添加判断，防止自选合约列表为空时产生无效的点击事件
                                            if (instrumentId != null) {
                                                if (!instrumentId.isEmpty()) {
                                                    mInstrumentId = instrumentId;
                                                    IdEvent idEvent = new IdEvent();
                                                    idEvent.setInstrument_id(instrumentId);
                                                    EventBus.getDefault().post(idEvent);
                                                }
                                            }
                                            mDialogOptional.dismiss();
                                        }

                                        @Override
                                        public void onItemLongClick(View view, int position) {

                                        }
                                    }));

                } else {
                    List<String> list = new ArrayList<>(LatestFileManager.getOptionalInsList().keySet());
                    DataManager dataManager = DataManager.getInstance();
                    UserEntity userEntity = dataManager.getTradeBean().getUsers().get(dataManager.LOGIN_USER_ID);
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
                    mDialogAdapterOptional.updateList(list, mInstrumentId);
                }
                if (!mDialogOptional.isShowing()) mDialogOptional.show();
            }
        });

        mBinding.rvDurationTitle.addOnItemTouchListener(new SimpleRecyclerViewItemClickListener(mBinding.rvDurationTitle, new SimpleRecyclerViewItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                mKlineDurationTitleAdapter.update(position);
                TextView title = view.findViewById(R.id.duration_title);
                String durationTitle = title.getText().toString();
                switchDuration(durationTitle);
            }

            @Override
            public void onItemLongClick(View view, int position) {

            }
        }));

        mBinding.rbKlineMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = mKlineDurationTitleAdapter.next();
                mBinding.rvDurationTitle.scrollToPosition(position);
                String durationTitle = mKlineDurationTitleAdapter.getDurationTitle();
                if (durationTitle != null) switchDuration(durationTitle);
            }
        });

        //监听“设置”按钮，弹出一个popup对话框
        mBinding.rbSetUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mFutureInfoActivity, SubSettingActivity.class);
                intent.putExtra(SUB_SETTING_TYPE, CHART_SETTING);
                mFutureInfoActivity.startActivityForResult(intent, FUTURE_INFO_ACTIVITY_TO_CHART_SETTING_ACTIVITY);
                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put(AMP_EVENT_CURRENT_PAGE, AMP_EVENT_PAGE_VALUE_FUTURE_INFO);
                    jsonObject.put(AMP_EVENT_TARGET_PAGE, AMP_EVENT_PAGE_VALUE_CHART_SETTING);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                Amplitude.getInstance().logEvent(AMP_SWITCH_PAGE, jsonObject);
            }
        });

        //盘口、持仓、挂单、交易切换容器监听器，滑动改变页面内容时联动导航状态
        mBinding.vpInfoContent.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                DataManager.getInstance().IS_POSITIVE = true;
                //滑动完成
                switch (position) {
                    case 0:
                        mBinding.rbHandicapInfo.setChecked(true);
                        break;
                    case 1:
                        mBinding.rbPositionInfo.setChecked(true);
                        break;
                    case 2:
                        mBinding.rbOrderInfoAlive.setChecked(true);
                        break;
                    case 3:
                        mBinding.rbTransactionInfo.setChecked(true);
                        break;
                    default:
                        break;
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });

        mBinding.rbHandicapInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                controlTransactionPageVisibility(0);
            }
        });

        mBinding.rbPositionInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                controlTransactionPageVisibility(1);
            }
        });

        mBinding.rbOrderInfoAlive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                controlTransactionPageVisibility(2);
            }
        });

        mBinding.rbTransactionInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                controlTransactionPageVisibility(3);
            }
        });

    }

    /**
     * date: 2019/2/20
     * author: chenli
     * description: 控制交易相关页的显示
     */
    public void controlTransactionPageVisibility(int index) {
        DataManager.getInstance().IS_POSITIVE = true;
        mFutureInfoActivity.setmIsVpLoaded(true);
        VisibilityEvent data = new VisibilityEvent();
        if (mBinding.vpInfoContent.getCurrentItem() == index) {
            if (mBinding.vpInfoContent.getVisibility() == View.GONE) {
                mBinding.vpInfoContent.setVisibility(View.VISIBLE);
                mBinding.rbHandicapInfo.setText(R.string.future_info_activity_handicap_down);
                mBinding.rbPositionInfo.setText(R.string.future_info_activity_position_down);
                mBinding.rbOrderInfoAlive.setText(R.string.future_info_activity_order_down_alive);
                mBinding.rbTransactionInfo.setText(R.string.future_info_activity_transaction_down);
                data.setVisible(false);
                EventBus.getDefault().post(data);
                DataManager.getInstance().IS_SHOW_VP_CONTENT = true;
            } else {
                mBinding.vpInfoContent.setVisibility(View.GONE);
                mBinding.rbHandicapInfo.setText(R.string.future_info_activity_handicap_up);
                mBinding.rbPositionInfo.setText(R.string.future_info_activity_position_up);
                mBinding.rbOrderInfoAlive.setText(R.string.future_info_activity_order_up_alive);
                mBinding.rbTransactionInfo.setText(R.string.future_info_activity_transaction_up);
                data.setVisible(true);
                EventBus.getDefault().post(data);
                DataManager.getInstance().IS_SHOW_VP_CONTENT = false;
            }
        } else {
            if (mBinding.vpInfoContent.getVisibility() == View.GONE) {
                mBinding.vpInfoContent.setVisibility(View.VISIBLE);
                mBinding.rbHandicapInfo.setText(R.string.future_info_activity_handicap_down);
                mBinding.rbPositionInfo.setText(R.string.future_info_activity_position_down);
                mBinding.rbOrderInfoAlive.setText(R.string.future_info_activity_order_down_alive);
                mBinding.rbTransactionInfo.setText(R.string.future_info_activity_transaction_down);
                data.setVisible(false);
                EventBus.getDefault().post(data);
                DataManager.getInstance().IS_SHOW_VP_CONTENT = true;
            }
            mBinding.vpInfoContent.setCurrentItem(index, false);
        }
    }

    /**
     * date: 2019/4/20
     * author: chenli
     * description: 设置合约名称
     */
    public void setToolbarTitle() {
        SearchEntity searchEntity = LatestFileManager.getSearchEntities().get(mInstrumentId);
        if (searchEntity != null) {
            if (mInstrumentId.contains("KQ")) {
                String underlying_symbol = searchEntity.getUnderlying_symbol();
                SearchEntity searchEntity1 = LatestFileManager.getSearchEntities().get(underlying_symbol);

                if (searchEntity1 != null) mToolbarTitle.setText(searchEntity1.getInstrumentName());
                else mToolbarTitle.setText(underlying_symbol);
            } else {
                String instrument_name = searchEntity.getInstrumentName();
                mToolbarTitle.setText(instrument_name);
            }

        } else mToolbarTitle.setText(mInstrumentId);

        //设置标题右图标
        String mTitle = mToolbarTitle.getText().toString();
        Rect bounds = new Rect();
        Paint textPaint = new Paint();
        textPaint.setTextSize(ScreenUtils.sp2px(sContext, 20));
        textPaint.getTextBounds(mTitle, 0, mTitle.length(), bounds);
        int width = bounds.width() + ScreenUtils.dp2px(sContext, 40);
        ViewGroup.LayoutParams layoutParams = mToolbarTitle.getLayoutParams();
        layoutParams.height = sContext.getResources().getDimensionPixelSize(R.dimen.text_view_height);
        layoutParams.width = width;
        mToolbarTitle.setLayoutParams(layoutParams);
    }

    public String getInstrumentId() {
        return mInstrumentId;
    }

    public void setInstrumentId(String instrumentId) {
        mInstrumentId = instrumentId;
    }

    public ViewPagerFragmentAdapter getmInfoPagerAdapter() {
        return mInfoPagerAdapter;
    }

    public KlineDurationTitleAdapter getmKlineDurationTitleAdapter() {
        return mKlineDurationTitleAdapter;
    }

    private void switchDuration(String durationTitle) {
        if (CommonConstants.KLINE_DURATION_DAY.equals(durationTitle))
            switchUpFragment(CURRENT_DAY_FRAGMENT, "");
        else {
            String duration = getDuration(durationTitle);
            if (durationTitle.contains("秒")) {
                switchUpFragment(SECOND_FRAGMENT, duration);
            } else if (durationTitle.contains("分")) {
                switchUpFragment(MINUTE_FRAGMENT, duration);
            } else if (durationTitle.contains("时")) {
                switchUpFragment(HOUR_FRAGMENT, duration);
            } else {
                switchUpFragment(DAY_FRAGMENT, duration);
            }
        }
    }

    /**
     * date: 7/7/17
     * author: chenli
     * description: 用于切换图表页，保存单例
     */
    private void switchUpFragment(String title, String klineType) {
        FragmentTransaction transaction = mFragmentManager.beginTransaction();
        BaseChartFragment fragment = createFragmentByTitle(title, klineType);
        transaction.replace(R.id.fl_content_up, fragment, title);
        transaction.setTransition(FragmentTransaction.TRANSIT_NONE).
                commit();
    }

    /**
     * date: 7/7/17
     * author: chenli
     * description: 根据title返回对应的fragment
     */
    private BaseChartFragment createFragmentByTitle(String title, String klineType) {
        switch (title) {
            case CURRENT_DAY_FRAGMENT:
                return new CurrentDayFragment();
            case DAY_FRAGMENT:
                return KlineFragment.newInstance("yy/MM/dd", klineType, DAY_FRAGMENT);
            case HOUR_FRAGMENT:
                return KlineFragment.newInstance("dd/HH:mm", klineType, HOUR_FRAGMENT);
            case MINUTE_FRAGMENT:
                return KlineFragment.newInstance("dd/HH:mm", klineType, MINUTE_FRAGMENT);
            case SECOND_FRAGMENT:
                return KlineFragment.newInstance("HH:mm:ss", klineType, SECOND_FRAGMENT);
            default:
                return null;
        }
    }

    private String getDurationTitle(String data) {
        for (int i = 0; i < mKlineDuration.length; i++) {
            if (mKlineDuration[i].equals(data)) return mKlineTitle[i];
        }
        return "";
    }

    private String getDuration(String data) {
        for (int i = 0; i < mKlineTitle.length; i++) {
            if (mKlineTitle[i].equals(data)) return mKlineDuration[i];
        }
        return "";
    }

    /**
     * date: 2019/4/17
     * author: chenli
     * description: 获取toolbar高度px
     */
    private int getToolBarHeight() {
        TypedValue tv = new TypedValue();
        if (mFutureInfoActivity.getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true)) {
            return TypedValue.complexToDimensionPixelSize(tv.data, mFutureInfoActivity.getResources().getDisplayMetrics());
        }
        return ScreenUtils.dp2px(sContext, 56);
    }

}

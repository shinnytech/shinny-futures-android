package com.shinnytech.futures.controller;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.PopupWindow;
import android.widget.Switch;
import android.widget.TextView;

import com.shinnytech.futures.R;
import com.shinnytech.futures.application.BaseApplication;
import com.shinnytech.futures.constants.CommonConstants;
import com.shinnytech.futures.controller.activity.FutureInfoActivity;
import com.shinnytech.futures.controller.activity.LoginActivity;
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
import com.shinnytech.futures.model.bean.eventbusbean.IdEvent;
import com.shinnytech.futures.model.bean.eventbusbean.SetUpEvent;
import com.shinnytech.futures.model.bean.eventbusbean.VisibilityEvent;
import com.shinnytech.futures.model.bean.futureinfobean.QuoteEntity;
import com.shinnytech.futures.model.bean.searchinfobean.SearchEntity;
import com.shinnytech.futures.model.engine.DataManager;
import com.shinnytech.futures.model.engine.LatestFileManager;
import com.shinnytech.futures.model.listener.SimpleRecyclerViewItemClickListener;
import com.shinnytech.futures.utils.DividerGridItemDecorationUtils;
import com.shinnytech.futures.utils.KeyboardUtils;
import com.shinnytech.futures.utils.SPUtils;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

import static com.shinnytech.futures.constants.CommonConstants.ACTIVITY_TYPE;
import static com.shinnytech.futures.constants.CommonConstants.CONFIG_AVERAGE_LINE;
import static com.shinnytech.futures.constants.CommonConstants.CONFIG_MD5;
import static com.shinnytech.futures.constants.CommonConstants.CONFIG_ORDER_LINE;
import static com.shinnytech.futures.constants.CommonConstants.CONFIG_POSITION_LINE;
import static com.shinnytech.futures.constants.CommonConstants.CURRENT_DAY_FRAGMENT;
import static com.shinnytech.futures.constants.CommonConstants.DAY_FRAGMENT;
import static com.shinnytech.futures.constants.CommonConstants.HOUR_FRAGMENT;
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
import static com.shinnytech.futures.constants.CommonConstants.ORDER_JUMP_TO_LOG_IN_ACTIVITY;
import static com.shinnytech.futures.constants.CommonConstants.POSITION_JUMP_TO_LOG_IN_ACTIVITY;
import static com.shinnytech.futures.constants.CommonConstants.SECOND_FRAGMENT;
import static com.shinnytech.futures.constants.CommonConstants.TRANSACTION_JUMP_TO_LOG_IN_ACTIVITY;

/**
 * Created on 1/17/18.
 * Created by chenli.
 * Description: .
 */

public class FutureInfoActivityPresenter {
    private final ViewPagerFragmentAdapter mInfoPagerAdapter;
    private final KlineDurationTitleAdapter mKlineDurationTitleAdapter;
    public Drawable mRightDrawable;
    /**
     * date: 7/7/17
     * description: 合约代码
     */
    private String mInstrumentId;
    /**
     * date: 7/7/17
     * description: “设置”弹出框
     */
    private PopupWindow mPopupWindow;
    /**
     * date: 7/7/17
     * description: 持仓开关
     */
    private Switch mPosition;
    /**
     * date: 7/7/17
     * description: 挂单开关
     */
    private Switch mPending;
    /**
     * date: 7/7/17
     * description: 均线开关
     */
    private Switch mAverage;
    /**
     * date: 2019/2/27
     * author: chenli
     * description: 五档行情开关
     */
    private Switch mMD5;
    /**
     * date: 7/3/17
     * description: 持仓开关状态
     */
    private boolean mIsPosition;
    /**
     * date: 7/7/17
     * description: 挂单开关状态
     */
    private boolean mIsPending;
    /**
     * date: 7/7/17
     * description: 均线开关状态
     */
    private boolean mIsAverage;
    /**
     * date: 2019/2/27
     * author: chenli
     * description: 五档行情开关状态
     */
    private boolean mIsMD5;
    private ActivityFutureInfoBinding mBinding;
    private FutureInfoActivity mFutureInfoActivity;
    private Context sContext;
    private Toolbar mToolbar;
    private TextView mToolbarTitle;
    private FragmentManager mFragmentManager;
    private int mNav_position;
    private Dialog mDialogOptional;
    private RecyclerView mRecyclerViewOptional;
    private DialogAdapter mDialogAdapterOptional;
    private String[] mKlineTitle = CommonConstants.KLINE_DURATION_ALL.split(",");
    private String[] mKlineDuration = new String[]{KLINE_3_SECOND, KLINE_5_SECOND, KLINE_10_SECOND,
            KLINE_15_SECOND, KLINE_20_SECOND, KLINE_30_SECOND, KLINE_1_MINUTE, KLINE_2_MINUTE, KLINE_3_MINUTE,
            KLINE_5_MINUTE, KLINE_10_MINUTE, KLINE_15_MINUTE, KLINE_30_MINUTE, KLINE_1_HOUR, KLINE_2_HOUR,
            KLINE_4_HOUR, KLINE_1_DAY, KLINE_7_DAY, KLINE_28_DAY};

    public FutureInfoActivityPresenter(FutureInfoActivity futureInfoActivity, Context context, ActivityFutureInfoBinding binding, Toolbar toolbar, TextView toolbarTitle) {
        this.mBinding = binding;
        this.mFutureInfoActivity = futureInfoActivity;
        this.mToolbar = toolbar;
        this.mToolbarTitle = toolbarTitle;
        this.sContext = context;

        mFragmentManager = mFutureInfoActivity.getSupportFragmentManager();
        BaseChartFragment currentDayFragment = new CurrentDayFragment();
        mFragmentManager.beginTransaction().
                setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN).
                add(R.id.fl_content_up, currentDayFragment, CommonConstants.CURRENT_DAY_FRAGMENT).commit();

        Intent intent = mFutureInfoActivity.getIntent();
        mInstrumentId = intent.getStringExtra("instrument_id");
        mNav_position = intent.getIntExtra("nav_position", 0);

        mToolbar.setTitle("");
        mToolbar.setTitleTextAppearance(mFutureInfoActivity, R.style.toolBarTitle);
        mToolbar.setTitleMarginStart(120);
        mToolbarTitle.setPadding(20, 0, 0, 0);
        mToolbarTitle.setTextSize(20);
        ViewGroup.LayoutParams layoutParams = mToolbarTitle.getLayoutParams();
        layoutParams.height = sContext.getResources().getDimensionPixelSize(R.dimen.text_view_height);
        layoutParams.width = sContext.getResources().getDimensionPixelSize(R.dimen.text_view_width);
        mToolbarTitle.setLayoutParams(layoutParams);
        mRightDrawable = ContextCompat.getDrawable(mFutureInfoActivity, R.mipmap.ic_expand_more_white_36dp);
        if (mRightDrawable != null)
            mRightDrawable.setBounds(0, 0, mRightDrawable.getMinimumWidth(), mRightDrawable.getMinimumHeight());


        //初始化开关状态
        mIsPosition = (boolean) SPUtils.get(sContext, CONFIG_POSITION_LINE, true);
        mIsPending = (boolean) SPUtils.get(sContext, CONFIG_ORDER_LINE, true);
        mIsAverage = (boolean) SPUtils.get(sContext, CONFIG_AVERAGE_LINE, true);

        //初始化五档行情控制
        initMD5ViewVisibility();

        //控制图表显示
        if (DataManager.getInstance().IS_SHOW_VP_CONTENT) {
            mBinding.vpInfoContent.setVisibility(View.VISIBLE);
            mBinding.rbHandicapInfo.setText(R.string.future_info_activity_handicap_down);
            mBinding.rbPositionInfo.setText(R.string.future_info_activity_position_down);
            mBinding.rbOrderInfo.setText(R.string.future_info_activity_order_down);
            mBinding.rbTransactionInfo.setText(R.string.future_info_activity_transaction_down);
        } else {
            mBinding.vpInfoContent.setVisibility(View.GONE);
            mBinding.rbHandicapInfo.setText(R.string.future_info_activity_handicap_up);
            mBinding.rbPositionInfo.setText(R.string.future_info_activity_position_up);
            mBinding.rbOrderInfo.setText(R.string.future_info_activity_order_up);
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
        mBinding.rvDurationTitle.setLayoutManager(new LinearLayoutManager(sContext, LinearLayoutManager.HORIZONTAL, false));
        mBinding.rvDurationTitle.setAdapter(mKlineDurationTitleAdapter);

        //初始化盘口、持仓、挂单、交易切换容器，fragment实例保存，有生命周期的变化，默认情况下屏幕外初始化两个fragment
        List<Fragment> fragmentList = new ArrayList<>();
        PositionFragment positionFragment = new PositionFragment();
        OrderFragment orderFragment = new OrderFragment();
        HandicapFragment handicapFragment = new HandicapFragment();
        TransactionFragment transactionFragment = new TransactionFragment();
        fragmentList.add(handicapFragment);
        fragmentList.add(positionFragment);
        fragmentList.add(orderFragment);
        fragmentList.add(transactionFragment);
        mInfoPagerAdapter = new ViewPagerFragmentAdapter(mFutureInfoActivity.getSupportFragmentManager(), fragmentList);
        mBinding.vpInfoContent.setAdapter(mInfoPagerAdapter);
        //设置初始化页为盘口页，去除滑动效果
        if (mNav_position == 1) {
            mBinding.vpInfoContent.setCurrentItem(1, false);
            mBinding.rbPositionInfo.setChecked(true);
            DataManager.getInstance().IS_SHOW_VP_CONTENT = true;
            mBinding.vpInfoContent.setVisibility(View.VISIBLE);
            mBinding.rbHandicapInfo.setText(R.string.future_info_activity_handicap_down);
            mBinding.rbPositionInfo.setText(R.string.future_info_activity_position_down);
            mBinding.rbOrderInfo.setText(R.string.future_info_activity_order_down);
            mBinding.rbTransactionInfo.setText(R.string.future_info_activity_transaction_down);
        } else {
            mBinding.vpInfoContent.setCurrentItem(0, false);
            mBinding.rbHandicapInfo.setChecked(true);
        }
        //由于盘口页和交易页需要通过eventBus实时监听合约代码的改变，当通过toolbar改变合约时，由于默认viewPager保存屏幕外一个页面，
        //盘口页和交易页相差两个页面，所以当显示其中一个的时候，另一个一定会消亡，所以打开时会初始化得到活动的最新合约代码。但是当通过
        //持仓页改变合约代码时会直接跳转到交易页，这个过程和活动更新合约代码同时发生，所以交易页通过初始化可能得不到最新合约代码， 必须
        //通过eventBus实时监控才行，所以要保持页面实例从而可以调用onEvent()方法
        mBinding.vpInfoContent.setOffscreenPageLimit(3);
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
                        dialogWindow.getDecorView().setPadding(0, 0, 0, 0);
                        WindowManager.LayoutParams lp = dialogWindow.getAttributes();
                        dialogWindow.setGravity(Gravity.BOTTOM);
                        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
                        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
                        dialogWindow.setAttributes(lp);
                    }
                    mDialogOptional.setContentView(viewDialog);
                    mDialogAdapterOptional = new DialogAdapter(mFutureInfoActivity,
                            new ArrayList<>(LatestFileManager.getOptionalInsList().keySet()));
                    mRecyclerViewOptional = viewDialog.findViewById(R.id.dialog_rv);
                    mRecyclerViewOptional.setLayoutManager(
                            new GridLayoutManager(mFutureInfoActivity, 3));
                    mRecyclerViewOptional.addItemDecoration(
                            new DividerGridItemDecorationUtils(mFutureInfoActivity, R.drawable.divider_optional_dialog));
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

                } else
                    mDialogAdapterOptional.updateList(new ArrayList<>(LatestFileManager.getOptionalInsList().keySet()));

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
                switchDuration(durationTitle);
            }
        });

        //监听“设置”按钮，弹出一个popup对话框
        mBinding.rbSetUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mPopupWindow == null) {
                    //构造一个“设置”按钮的PopupWindow
                    View view = View.inflate(sContext, R.layout.popup_set_up, null);
                    mPosition = view.findViewById(R.id.position);
                    mPending = view.findViewById(R.id.pending);
                    mAverage = view.findViewById(R.id.average_line);
                    mMD5 = view.findViewById(R.id.md);
                    mPopupWindow = new PopupWindow(view,
                            ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
                    //点击空白处popupWindow消失
                    mPopupWindow.setBackgroundDrawable(new ColorDrawable(0x00000000));
                    //设置动画，左进右出
                    mPopupWindow.setAnimationStyle(R.style.anim_menu_set_up);

                    //初始化开关状态
                    mPosition.setChecked(mIsPosition);
                    mPending.setChecked(mIsPending);
                    mAverage.setChecked(mIsAverage);
                    mMD5.setChecked(mIsMD5);

                    mPosition.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                            SPUtils.putAndApply(sContext, CONFIG_POSITION_LINE, isChecked);
                            mIsPosition = isChecked;
                            SetUpEvent setUpEvent = new SetUpEvent();
                            setUpEvent.setPosition(mIsPosition);
                            setUpEvent.setPending(mIsPending);
                            setUpEvent.setAverage(mIsAverage);
                            EventBus.getDefault().post(setUpEvent);
                        }
                    });

                    mPending.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                            SPUtils.putAndApply(sContext, CONFIG_ORDER_LINE, isChecked);
                            mIsPending = isChecked;
                            SetUpEvent setUpEvent = new SetUpEvent();
                            setUpEvent.setPending(mIsPending);
                            setUpEvent.setPosition(mIsPosition);
                            setUpEvent.setAverage(mIsAverage);
                            EventBus.getDefault().post(setUpEvent);
                        }
                    });

                    mAverage.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                            SPUtils.putAndApply(sContext, CONFIG_AVERAGE_LINE, isChecked);
                            mIsAverage = isChecked;
                            SetUpEvent setUpEvent = new SetUpEvent();
                            setUpEvent.setAverage(mIsAverage);
                            setUpEvent.setPending(mIsPending);
                            setUpEvent.setPosition(mIsPosition);
                            EventBus.getDefault().post(setUpEvent);
                        }
                    });

                    mMD5.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                            SPUtils.putAndApply(sContext, CONFIG_MD5, isChecked);
                            mIsMD5 = isChecked;
                            updateMD5ViewVisibility();
                        }
                    });
                }
                //设置popupWindow显示的位置，参数依次是参照View，x轴的偏移量，y轴的偏移量
                mPopupWindow.showAsDropDown(v, -20, 0);
            }
        });

        //盘口、持仓、挂单、交易切换容器监听器，滑动改变页面内容时联动导航状态
        mBinding.vpInfoContent.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                //滑动完成
                switch (position) {
                    case 0:
                        mBinding.rbHandicapInfo.setChecked(true);
                        closeKeyboard();
                        break;
                    case 1:
                        mBinding.rbPositionInfo.setChecked(true);
                        closeKeyboard();
                        break;
                    case 2:
                        mBinding.rbOrderInfo.setChecked(true);
                        closeKeyboard();
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
                if (!DataManager.getInstance().IS_LOGIN) {
                    Intent intent = new Intent(mFutureInfoActivity, LoginActivity.class);
                    //判断从哪个页面跳到登录页，登录页的销毁方式不一样
                    intent.putExtra(ACTIVITY_TYPE, "FutureInfoActivity");
                    mFutureInfoActivity.startActivityForResult(intent, POSITION_JUMP_TO_LOG_IN_ACTIVITY);
                    return;
                }
                controlTransactionPageVisibility(1);
            }
        });

        mBinding.rbOrderInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!DataManager.getInstance().IS_LOGIN) {
                    Intent intent = new Intent(mFutureInfoActivity, LoginActivity.class);
                    intent.putExtra(ACTIVITY_TYPE, "FutureInfoActivity");
                    mFutureInfoActivity.startActivityForResult(intent, ORDER_JUMP_TO_LOG_IN_ACTIVITY);
                    return;
                }
                controlTransactionPageVisibility(2);
            }
        });

        mBinding.rbTransactionInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!DataManager.getInstance().IS_LOGIN) {
                    Intent intent = new Intent(mFutureInfoActivity, LoginActivity.class);
                    intent.putExtra(ACTIVITY_TYPE, "FutureInfoActivity");
                    mFutureInfoActivity.startActivityForResult(intent, TRANSACTION_JUMP_TO_LOG_IN_ACTIVITY);
                    return;
                }
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
        VisibilityEvent data = new VisibilityEvent();
        if (mBinding.vpInfoContent.getCurrentItem() == index) {
            if (mBinding.vpInfoContent.getVisibility() == View.GONE) {
                mBinding.vpInfoContent.setVisibility(View.VISIBLE);
                mBinding.rbHandicapInfo.setText(R.string.future_info_activity_handicap_down);
                mBinding.rbPositionInfo.setText(R.string.future_info_activity_position_down);
                mBinding.rbOrderInfo.setText(R.string.future_info_activity_order_down);
                mBinding.rbTransactionInfo.setText(R.string.future_info_activity_transaction_down);
                data.setVisible(false);
                EventBus.getDefault().post(data);
                DataManager.getInstance().IS_SHOW_VP_CONTENT = true;
            } else {
                mBinding.vpInfoContent.setVisibility(View.GONE);
                mBinding.rbHandicapInfo.setText(R.string.future_info_activity_handicap_up);
                mBinding.rbPositionInfo.setText(R.string.future_info_activity_position_up);
                mBinding.rbOrderInfo.setText(R.string.future_info_activity_order_up);
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
                mBinding.rbOrderInfo.setText(R.string.future_info_activity_order_down);
                mBinding.rbTransactionInfo.setText(R.string.future_info_activity_transaction_down);
                data.setVisible(false);
                EventBus.getDefault().post(data);
                DataManager.getInstance().IS_SHOW_VP_CONTENT = true;
            }
            mBinding.vpInfoContent.setCurrentItem(index, false);
        }
    }

    /**
     * date: 1/18/18
     * author: chenli
     * description: 当从持仓菜单进入本页时判断登录状态
     */
    public void checkLoginState() {
        if (mNav_position == 1 && !DataManager.getInstance().IS_LOGIN) {
            Intent intent = new Intent(mFutureInfoActivity, LoginActivity.class);
            //判断从哪个页面跳到登录页，登录页的销毁方式不一样
            intent.putExtra(ACTIVITY_TYPE, "FutureInfoActivity");
            mFutureInfoActivity.startActivity(intent);
            mNav_position = 2;
        } else if (mNav_position == 2 && !DataManager.getInstance().IS_LOGIN)
            mFutureInfoActivity.finish();
    }

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
    }

    public boolean closeKeyboard() {
        TransactionFragment transactionFragment = ((TransactionFragment) mInfoPagerAdapter.getItem(3));
        KeyboardUtils keyboardUtilsPrice = transactionFragment.getKeyboardUtilsPrice();
        KeyboardUtils keyboardUtilsVolume = transactionFragment.getKeyboardUtilsVolume();
        if (keyboardUtilsPrice != null) {
            if (keyboardUtilsPrice.isVisible()) {
                keyboardUtilsPrice.hideKeyboard();
                return true;
            }
        }
        if (keyboardUtilsVolume != null) {
            if (keyboardUtilsVolume.isVisible()) {
                keyboardUtilsVolume.hideKeyboard();
                return true;
            }
        }
        return false;
    }

    public boolean isPosition() {
        return mIsPosition;
    }

    public boolean isPending() {
        return mIsPending;
    }

    public boolean isAverage() {
        return mIsAverage;
    }

    public String getInstrumentId() {
        return mInstrumentId;
    }

    public void setInstrumentId(String instrumentId) {
        mInstrumentId = instrumentId;
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
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN).
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
}

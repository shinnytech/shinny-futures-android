package com.shinnytech.futures.controller.fragment;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.shinnytech.futures.R;
import com.shinnytech.futures.amplitude.api.Amplitude;
import com.shinnytech.futures.application.BaseApplication;
import com.shinnytech.futures.constants.CommonConstants;
import com.shinnytech.futures.controller.activity.MainActivity;
import com.shinnytech.futures.controller.activity.SubSettingActivity;
import com.shinnytech.futures.databinding.FragmentFutureInfoBinding;
import com.shinnytech.futures.model.adapter.KlineDurationTitleAdapter;
import com.shinnytech.futures.model.adapter.ViewPagerFragmentAdapter;
import com.shinnytech.futures.model.bean.accountinfobean.UserEntity;
import com.shinnytech.futures.model.bean.eventbusbean.IdEvent;
import com.shinnytech.futures.model.bean.eventbusbean.KlineEvent;
import com.shinnytech.futures.model.bean.eventbusbean.VisibilityEvent;
import com.shinnytech.futures.model.bean.futureinfobean.QuoteEntity;
import com.shinnytech.futures.model.engine.DataManager;
import com.shinnytech.futures.model.engine.LatestFileManager;
import com.shinnytech.futures.model.listener.SimpleRecyclerViewItemClickListener;
import com.shinnytech.futures.utils.CloneUtils;
import com.shinnytech.futures.utils.LogUtils;
import com.shinnytech.futures.utils.SPUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import static com.shinnytech.futures.constants.CommonConstants.AMP_EVENT_IS_INS_IN_OPTIONAL;
import static com.shinnytech.futures.constants.CommonConstants.AMP_EVENT_IS_INS_IN_POSITION;
import static com.shinnytech.futures.constants.CommonConstants.AMP_EVENT_PAGE_ID;
import static com.shinnytech.futures.constants.CommonConstants.AMP_EVENT_PAGE_ID_VALUE_FUTURE_INFO;
import static com.shinnytech.futures.constants.CommonConstants.AMP_EVENT_SOURCE;
import static com.shinnytech.futures.constants.CommonConstants.AMP_EVENT_SWITCH_FROM;
import static com.shinnytech.futures.constants.CommonConstants.AMP_EVENT_SWITCH_TO;
import static com.shinnytech.futures.constants.CommonConstants.AMP_EVENT_SYMBOL;
import static com.shinnytech.futures.constants.CommonConstants.AMP_EVENT_TAB_HANDICAP;
import static com.shinnytech.futures.constants.CommonConstants.AMP_EVENT_TAB_ORDER_ALIVE;
import static com.shinnytech.futures.constants.CommonConstants.AMP_EVENT_TAB_POSITION;
import static com.shinnytech.futures.constants.CommonConstants.AMP_EVENT_TAB_TRANSACTION;
import static com.shinnytech.futures.constants.CommonConstants.AMP_SHOW_PAGE;
import static com.shinnytech.futures.constants.CommonConstants.AMP_SWITCH_TAB;
import static com.shinnytech.futures.constants.CommonConstants.CHART_SETTING;
import static com.shinnytech.futures.constants.CommonConstants.CONFIG_MD5;
import static com.shinnytech.futures.constants.CommonConstants.DAY_FRAGMENT;
import static com.shinnytech.futures.constants.CommonConstants.FUTURE_INFO_FRAGMENT_TO_CHART_SETTING_ACTIVITY;
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
import static com.shinnytech.futures.constants.CommonConstants.SECOND_FRAGMENT;
import static com.shinnytech.futures.constants.CommonConstants.SUB_SETTING_TYPE;

public class FutureInfoFragment extends LazyLoadFragment {
    private FragmentFutureInfoBinding mBinding;
    private MainActivity mMainActivity;
    private Context sContext;
    private DataManager sDataManager;
    private ViewPagerFragmentAdapter mKlinePagerAdapter;
    private ViewPagerFragmentAdapter mInfoPagerAdapter;
    private KlineDurationTitleAdapter mKlineDurationTitleAdapter;
    private String mInstrumentId;
    private boolean mIsMD5;
    private String[] mKlineTitle = CommonConstants.KLINE_DURATION_ALL.split(",");
    private String[] mKlineDuration = new String[]{KLINE_3_SECOND, KLINE_5_SECOND, KLINE_10_SECOND,
            KLINE_15_SECOND, KLINE_20_SECOND, KLINE_30_SECOND, KLINE_1_MINUTE, KLINE_2_MINUTE, KLINE_3_MINUTE,
            KLINE_5_MINUTE, KLINE_10_MINUTE, KLINE_15_MINUTE, KLINE_30_MINUTE, KLINE_1_HOUR, KLINE_2_HOUR,
            KLINE_4_HOUR, KLINE_1_DAY, KLINE_7_DAY, KLINE_28_DAY};

    @Override
    public void show() {
        LogUtils.e("futureInfoShow", true);

        //每次点击进入合约详情页，设置合约代码
        mInstrumentId = mMainActivity.getmMainActivityPresenter().getmInstrumentId();
        IdEvent idEvent = new IdEvent();
        idEvent.setInstrument_id(mInstrumentId);
        EventBus.getDefault().post(idEvent);

        QuoteEntity quoteEntity = sDataManager.getRtnData().getQuotes().get(mInstrumentId);
        if (quoteEntity == null) return;
        if (mInstrumentId.contains("&") && mInstrumentId.contains(" ")) {
            quoteEntity = CloneUtils.clone(quoteEntity);
            quoteEntity = LatestFileManager.calculateCombineQuoteFull(quoteEntity);
        }
        mBinding.setQuote(quoteEntity);
        int indexKline = mBinding.vpKlineContent.getCurrentItem();
        ((LazyLoadFragment) mKlinePagerAdapter.getItem(indexKline)).show();
        if (mBinding.vpInfoContent.getVisibility() == View.VISIBLE) {
            int indexInfo = mBinding.vpInfoContent.getCurrentItem();
            ((LazyLoadFragment) mInfoPagerAdapter.getItem(indexInfo)).show();
        }else {
            //防止首次加载为空
            ((HandicapFragment) mInfoPagerAdapter.getItem(0)).setInstrument_id(mInstrumentId);
            ((PositionFragment) mInfoPagerAdapter.getItem(1)).setInstrument_id(mInstrumentId);
            ((OrderFragment) mInfoPagerAdapter.getItem(2)).setInstrument_id(mInstrumentId);
            ((TransactionFragment) mInfoPagerAdapter.getItem(3)).setInstrument_id(mInstrumentId);

        }
        updateMD5ViewVisibility();

        //控制图表显示
        if (sDataManager.IS_SHOW_VP_CONTENT) {
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

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put(AMP_EVENT_PAGE_ID, AMP_EVENT_PAGE_ID_VALUE_FUTURE_INFO);
            jsonObject.put(AMP_EVENT_SOURCE, sDataManager.SOURCE);
            jsonObject.put(AMP_EVENT_SYMBOL, mInstrumentId);
            sDataManager.SOURCE = AMP_EVENT_PAGE_ID_VALUE_FUTURE_INFO;
            boolean isInsInOptional = LatestFileManager.getOptionalInsList().containsKey(mInstrumentId);
            jsonObject.put(AMP_EVENT_IS_INS_IN_OPTIONAL, isInsInOptional);
            UserEntity userEntity = sDataManager.getTradeBean().getUsers().get(sDataManager.USER_ID);
            boolean isInsInPosition = userEntity.getPositions().containsKey(mInstrumentId);
            jsonObject.put(AMP_EVENT_IS_INS_IN_POSITION, isInsInPosition);
            Amplitude.getInstance().logEventWrap(AMP_SHOW_PAGE, jsonObject);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void leave() {
        //为了记录用户调整的柱子宽度
        ((LazyLoadFragment) mKlinePagerAdapter.getItem(1)).leave();
    }

    @Override
    public void refreshMD() {
        QuoteEntity quoteEntity = sDataManager.getRtnData().getQuotes().get(mInstrumentId);
        if (quoteEntity == null) return;
        if (mInstrumentId.contains("&") && mInstrumentId.contains(" ")) {
            quoteEntity = CloneUtils.clone(quoteEntity);
            quoteEntity = LatestFileManager.calculateCombineQuoteFull(quoteEntity);
        }
        mBinding.setQuote(quoteEntity);

        int indexKline = mBinding.vpKlineContent.getCurrentItem();
        LazyLoadFragment lazyLoadFragmentKline = (LazyLoadFragment) mKlinePagerAdapter.getItem(indexKline);
        lazyLoadFragmentKline.refreshMD();

        if (mBinding.vpInfoContent.getVisibility() == View.VISIBLE) {
            int indexInfo = mBinding.vpInfoContent.getCurrentItem();
            LazyLoadFragment lazyLoadFragmentInfo = (LazyLoadFragment) mInfoPagerAdapter.getItem(indexInfo);
            lazyLoadFragmentInfo.refreshMD();
        }
    }

    @Override
    public void refreshTD() {
        int indexKline = mBinding.vpKlineContent.getCurrentItem();
        LazyLoadFragment lazyLoadFragmentKline = (LazyLoadFragment) mKlinePagerAdapter.getItem(indexKline);
        lazyLoadFragmentKline.refreshTD();

        if (mBinding.vpInfoContent.getVisibility() == View.VISIBLE) {
            int indexInfo = mBinding.vpInfoContent.getCurrentItem();
            LazyLoadFragment lazyLoadFragmentInfo = (LazyLoadFragment) mInfoPagerAdapter.getItem(indexInfo);
            lazyLoadFragmentInfo.refreshTD();
        }

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_future_info, container, false);
        initData();
        initEvent();
        return mBinding.getRoot();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    private void initData() {
        EventBus.getDefault().register(this);

        mMainActivity = (MainActivity) getActivity();
        sDataManager = DataManager.getInstance();
        sContext = BaseApplication.getContext();

        //初始化K线类型
        String durationPre = (String) SPUtils.get(sContext, CommonConstants.CONFIG_KLINE_DURATION_DEFAULT, "");
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

        List<Fragment> fragments = new ArrayList<>();
        CurrentDayFragment currentDayFragment = new CurrentDayFragment();
        KlineFragment klineFragment = generateKlineFragment(list.get(1));
        fragments.add(currentDayFragment);
        fragments.add(klineFragment);
        mKlinePagerAdapter = new ViewPagerFragmentAdapter(mMainActivity.getSupportFragmentManager(), fragments);
        mBinding.vpKlineContent.setAdapter(mKlinePagerAdapter);
        mBinding.vpKlineContent.setOffscreenPageLimit(2);
        mBinding.vpKlineContent.setPagingEnabled(false);

        //初始化盘口、持仓、挂单、交易切换容器，fragment实例保存，有生命周期的变化，默认情况下屏幕外初始化两个fragment
        List<Fragment> fragmentList = new ArrayList<>();
        PositionFragment positionFragment = PositionFragment.newInstance(false);
        OrderFragment orderFragmentAlive = OrderFragment.newInstance(true, false);
        HandicapFragment handicapFragment = new HandicapFragment();
        TransactionFragment transactionFragment = new TransactionFragment();
        fragmentList.add(handicapFragment);
        fragmentList.add(positionFragment);
        fragmentList.add(orderFragmentAlive);
        fragmentList.add(transactionFragment);
        mInfoPagerAdapter = new ViewPagerFragmentAdapter(mMainActivity.getSupportFragmentManager(), fragmentList);
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

    private void initEvent() {
        mBinding.rvDurationTitle.addOnItemTouchListener(new SimpleRecyclerViewItemClickListener(mBinding.rvDurationTitle, new SimpleRecyclerViewItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                if (mKlineDurationTitleAdapter.isCurrentIndex(position)) return;
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
                Intent intent = new Intent(mMainActivity, SubSettingActivity.class);
                intent.putExtra(SUB_SETTING_TYPE, CHART_SETTING);
                mMainActivity.startActivityForResult(intent, FUTURE_INFO_FRAGMENT_TO_CHART_SETTING_ACTIVITY);
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
     * date: 2019/3/14
     * author: chenli
     * description: 切换合约更新五档行情
     */
    public void updateMD5ViewVisibility() {
        //显示五档行情
        if (!mInstrumentId.contains("SHFE") && !mInstrumentId.contains("INE"))
            mBinding.md.setVisibility(View.GONE);
        else {
            //判断有无五档行情
            QuoteEntity quoteEntity = sDataManager.getRtnData().getQuotes().get(mInstrumentId);
            if (quoteEntity != null && quoteEntity.getAsk_price5() == null) {
                SPUtils.putAndApply(sContext, CONFIG_MD5, false);
            }
            mIsMD5 = (boolean) SPUtils.get(sContext, CONFIG_MD5, true);
            if (mIsMD5) mBinding.md.setVisibility(View.VISIBLE);
            else mBinding.md.setVisibility(View.GONE);
        }
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
                mBinding.rbOrderInfoAlive.setText(R.string.future_info_activity_order_down_alive);
                mBinding.rbTransactionInfo.setText(R.string.future_info_activity_transaction_down);
                data.setVisible(false);
                EventBus.getDefault().post(data);
                sDataManager.IS_SHOW_VP_CONTENT = true;
            } else {
                mBinding.vpInfoContent.setVisibility(View.GONE);
                mBinding.rbHandicapInfo.setText(R.string.future_info_activity_handicap_up);
                mBinding.rbPositionInfo.setText(R.string.future_info_activity_position_up);
                mBinding.rbOrderInfoAlive.setText(R.string.future_info_activity_order_up_alive);
                mBinding.rbTransactionInfo.setText(R.string.future_info_activity_transaction_up);
                data.setVisible(true);
                EventBus.getDefault().post(data);
                sDataManager.IS_SHOW_VP_CONTENT = false;
            }
        } else {
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put(AMP_EVENT_PAGE_ID, AMP_EVENT_PAGE_ID_VALUE_FUTURE_INFO);
                jsonObject.put(AMP_EVENT_SWITCH_FROM, getTabTitle(mBinding.vpInfoContent.getCurrentItem()));
                jsonObject.put(AMP_EVENT_SWITCH_TO, getTabTitle(index));
                Amplitude.getInstance().logEventWrap(AMP_SWITCH_TAB, jsonObject);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            if (mBinding.vpInfoContent.getVisibility() == View.GONE) {
                mBinding.vpInfoContent.setVisibility(View.VISIBLE);
                mBinding.rbHandicapInfo.setText(R.string.future_info_activity_handicap_down);
                mBinding.rbPositionInfo.setText(R.string.future_info_activity_position_down);
                mBinding.rbOrderInfoAlive.setText(R.string.future_info_activity_order_down_alive);
                mBinding.rbTransactionInfo.setText(R.string.future_info_activity_transaction_down);
                data.setVisible(false);
                EventBus.getDefault().post(data);
                sDataManager.IS_SHOW_VP_CONTENT = true;
            }
            mBinding.vpInfoContent.setCurrentItem(index, false);
        }
    }

    /**
     * date: 2019/7/3
     * author: chenli
     * description: 切换周期
     */
    public void switchDuration(String durationTitle) {

        if (CommonConstants.KLINE_DURATION_DAY.equals(durationTitle)) {
            mBinding.vpKlineContent.setCurrentItem(0, false);
            BaseApplication.getmMDWebSocket().sendSetChart(mInstrumentId);
        } else {
            String duration = getDuration(durationTitle);
            if (mBinding.vpKlineContent.getCurrentItem() == 0)
                mBinding.vpKlineContent.setCurrentItem(1, false);
            EventBus.getDefault().post(generateKlineEvent(duration, durationTitle));
        }
    }

    /**
     * date: 2019/7/3
     * author: chenli
     * description: 产生周期参数
     */
    private KlineEvent generateKlineEvent(String duration, String durationTitle) {
        KlineEvent klineEvent = new KlineEvent();
        klineEvent.setKlineType(duration);
        if (durationTitle.contains("秒")) {
            klineEvent.setFragmentType(SECOND_FRAGMENT);
            klineEvent.setxValuesType("HH:mm:ss");
        } else if (durationTitle.contains("分")) {
            klineEvent.setFragmentType(MINUTE_FRAGMENT);
            klineEvent.setxValuesType("dd/HH:mm");
        } else if (durationTitle.contains("时")) {
            klineEvent.setFragmentType(HOUR_FRAGMENT);
            klineEvent.setxValuesType("dd/HH:mm");
        } else {
            klineEvent.setFragmentType(DAY_FRAGMENT);
            klineEvent.setxValuesType("yy/MM/dd");
        }
        return klineEvent;
    }

    /**
     * date: 2019/7/2
     * author: chenli
     * description: 产生对应周期的k线图
     */
    private KlineFragment generateKlineFragment(String durationTitle) {
        String duration = getDuration(durationTitle);
        if (durationTitle.contains("秒")) {
            return createFragmentByTitle(SECOND_FRAGMENT, duration);
        } else if (durationTitle.contains("分")) {
            return createFragmentByTitle(MINUTE_FRAGMENT, duration);
        } else if (durationTitle.contains("时")) {
            return createFragmentByTitle(HOUR_FRAGMENT, duration);
        } else {
            return createFragmentByTitle(DAY_FRAGMENT, duration);
        }
    }

    /**
     * date: 7/7/17
     * author: chenli
     * description: 根据title返回对应的fragment
     */
    private KlineFragment createFragmentByTitle(String title, String klineType) {
        switch (title) {
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

    private String getDuration(String data) {
        for (int i = 0; i < mKlineTitle.length; i++) {
            if (mKlineTitle[i].equals(data)) return mKlineDuration[i];
        }
        return "";
    }

    public String getTabTitle(int index){
        switch (index){
            case 0:
                return AMP_EVENT_TAB_HANDICAP;
            case 1:
                return AMP_EVENT_TAB_POSITION;
            case 2:
                return AMP_EVENT_TAB_ORDER_ALIVE;
            case 3:
                return AMP_EVENT_TAB_TRANSACTION;
            default:
                return "";
        }
    }

    /**
     * date: 7/7/17
     * author: chenli
     * description: fragmentPosition/currentDayFragment/KlineFragment页发过来的合约代码
     */
    @Subscribe
    public void onEvent(IdEvent data) {
        //持仓列表中可能出现合约一致,方向不同的情形
        String instrument_id_new = data.getInstrument_id();
        if (!instrument_id_new.equals(mInstrumentId)) {
            mInstrumentId = instrument_id_new;
            //切换合约更新盘口
            refreshMD();
            //切换合约判断是否有五档行情
            updateMD5ViewVisibility();
            //更新标题
            mMainActivity.getmMainActivityPresenter().changeToolbar(mInstrumentId);
        }
    }

    public FragmentFutureInfoBinding getmBinding() {
        return mBinding;
    }

    public KlineDurationTitleAdapter getmKlineDurationTitleAdapter() {
        return mKlineDurationTitleAdapter;
    }
}

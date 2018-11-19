package com.shinnytech.futures.controller.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.mikephil.charting.charts.CombinedChart;
import com.github.mikephil.charting.components.LimitLine;
import com.shinnytech.futures.R;
import com.shinnytech.futures.application.BaseApplication;
import com.shinnytech.futures.model.bean.accountinfobean.OrderEntity;
import com.shinnytech.futures.model.bean.accountinfobean.PositionEntity;
import com.shinnytech.futures.model.bean.accountinfobean.UserEntity;
import com.shinnytech.futures.model.bean.futureinfobean.KlineEntity;
import com.shinnytech.futures.model.bean.searchinfobean.SearchEntity;
import com.shinnytech.futures.model.engine.DataManager;
import com.shinnytech.futures.model.engine.LatestFileManager;
import com.shinnytech.futures.controller.activity.FutureInfoActivity;
import com.shinnytech.futures.utils.LogUtils;

import org.greenrobot.eventbus.EventBus;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import static com.shinnytech.futures.constants.CommonConstants.CURRENT_DAY;
import static com.shinnytech.futures.constants.CommonConstants.KLINE_DAY;
import static com.shinnytech.futures.constants.CommonConstants.KLINE_HOUR;
import static com.shinnytech.futures.constants.CommonConstants.KLINE_MINUTE;
import static com.shinnytech.futures.constants.CommonConstants.MD_MESSAGE;
import static com.shinnytech.futures.constants.CommonConstants.TD_MESSAGE;
import static com.shinnytech.futures.constants.CommonConstants.VIEW_WIDTH;
import static com.shinnytech.futures.model.service.WebSocketService.MD_BROADCAST_ACTION;
import static com.shinnytech.futures.model.service.WebSocketService.TD_BROADCAST_ACTION;

/**
 * date: 9/20/17
 * author: chenli
 * description:
 * version:
 * state:
 */
public class BaseChartFragment extends LazyLoadFragment {

    /**
     * date: 7/9/17
     * description: 组合图
     */
    protected CombinedChart mChart;

    /**
     * date: 7/9/17
     * description: 图表背景色
     */
    protected int mColorHomeBg;

    /**
     * date: 7/9/17
     * description: 轴线颜色
     */
    protected int mColorAxis;

    /**
     * date: 7/9/17
     * description: 文字颜色
     */
    protected int mColorText;

    /**
     * date: 7/9/17
     * description: 网格线颜色
     */
    protected int mColorGrid;

    /**
     * date: 7/9/17
     * description: 持仓线颜色
     */
    protected int mColorBuy;

    /**
     * date: 7/9/17
     * description: 挂单线颜色
     */
    protected int mColorSell;

    /**
     * date: 7/9/17
     * description: 判断是否显示持仓线、挂单线、均线
     */
    protected boolean mIsPosition;
    protected boolean mIsPending;
    protected boolean mIsAverage;

    /**
     * date: 7/9/17
     * description: 持仓线数据
     */
    protected Map<String, LimitLine> mPositionLimitLines;
    /**
     * date: 7/9/17
     * description: 挂单线
     */
    protected Map<String, LimitLine> mOrderLimitLines;

    protected DataManager sDataManager;
    protected BroadcastReceiver mReceiver;
    protected BroadcastReceiver mReceiver1;
    protected String instrument_id;
    protected String instrument_id_transaction;
    protected Calendar mCalendar;
    protected SimpleDateFormat mSimpleDateFormat;
    protected SparseArray<String> xVals;
    protected Map<String, KlineEntity.DataEntity> mDataEntities;
    protected int mLayoutId;
    protected String mKlineType;
    protected int mButtonId;
    private ViewDataBinding mViewDataBinding;
    protected boolean mIsUpdate;

    /**
     * date: 7/9/17
     * author: chenli
     * description: 刷新K线图
     */
    private void refreshChart(String mDataString) {
        try {
            switch (mDataString) {
                case MD_MESSAGE:
                    if (mIsUpdate) refreshMarketing();
                    break;
                case TD_MESSAGE:
                    refreshTrade();
                    break;
                default:
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mViewDataBinding = DataBindingUtil.inflate(inflater, mLayoutId, container, false);
        mChart = mViewDataBinding.getRoot().findViewById(R.id.chart);
        //注册EventBus
        EventBus.getDefault().register(this);
        initData();
        initChart();
        return mViewDataBinding.getRoot();
    }

    /**
     * date: 7/9/17
     * author: chenli
     * description: 获取传进futureInfoActivity页的合约代码，以及初始化持仓线、挂单线、均线
     */
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        instrument_id = ((FutureInfoActivity) getActivity()).getInstrument_id();
        SearchEntity searchEntity = LatestFileManager.getSearchEntities().get(instrument_id);
        if (instrument_id.contains("KQ") && searchEntity != null)
            instrument_id_transaction = searchEntity.getUnderlying_symbol();
        else instrument_id_transaction = instrument_id;

        mIsAverage = ((FutureInfoActivity) getActivity()).isAverage();
        if (!mIsAverage) mChart.getLegend().setEnabled(false);
        mIsPosition = ((FutureInfoActivity) getActivity()).isPosition();
        mIsPending = ((FutureInfoActivity) getActivity()).isPending();
        if (sDataManager.IS_LOGIN) {
            if (mIsPosition) addPositionLimitLines();
            if (mIsPending) addOrderLimitLines();
        }
    }

    protected void initData() {
        mColorHomeBg = ContextCompat.getColor(getActivity(), R.color.kline_background);
        mColorAxis = ContextCompat.getColor(getActivity(), R.color.kline_axis);
        mColorGrid = ContextCompat.getColor(getActivity(), R.color.kline_grid);
        mColorText = ContextCompat.getColor(getActivity(), R.color.kline_text);
        mColorBuy = ContextCompat.getColor(getActivity(), R.color.kline_position);
        mColorSell = ContextCompat.getColor(getActivity(), R.color.kline_order);

        mPositionLimitLines = new HashMap<>();
        mOrderLimitLines = new HashMap<>();
        mCalendar = Calendar.getInstance();
        sDataManager = DataManager.getInstance();
        xVals = new SparseArray<>();
        mIsUpdate = true;
    }

    protected void initChart() {
        mChart.getDescription().setEnabled(false);
        mChart.setDrawGridBackground(true);
        mChart.setBackgroundColor(mColorHomeBg);
        mChart.setGridBackgroundColor(mColorHomeBg);
        mChart.setDrawValueAboveBar(false);
        mChart.setNoDataText(" ");
        mChart.setAutoScaleMinMaxEnabled(true);
        mChart.setDragEnabled(true);
        mChart.setViewPortOffsets(0, 40, 0, 40);
        mChart.setDoubleTapToZoomEnabled(false);
        mChart.setHighlightPerTapEnabled(false);

    }

    @Override
    public void update() {
        refreshMarketing();
    }

    /**
     * date: 6/28/18
     * author: chenli
     * description: 刷新行情信息
     */
    protected void refreshMarketing() {
    }

    /**
     * date: 6/28/18
     * author: chenli
     * description: 刷新账户信息
     */
    private void refreshTrade() {
        UserEntity userEntity = sDataManager.getTradeBean().getUsers().get(sDataManager.USER_ID);
        if (userEntity == null) return;
        if (sDataManager.IS_LOGIN && mIsPending) {
            for (OrderEntity orderEntity :
                    userEntity.getOrders().values()) {
                if (orderEntity != null &&
                        (orderEntity.getExchange_id() + "." + orderEntity.getInstrument_id())
                                .equals(instrument_id_transaction)) {
                    String key = orderEntity.getKey();
                    if (!mOrderLimitLines.containsKey(key)) {
                        if ("ALIVE".equals(orderEntity.getStatus())) {
                            addOneOrderLimitLine(orderEntity);
                        }
                    } else {
                        if ("FINISHED".equals(orderEntity.getStatus())) {
                            removeOneOrderLimitLine(key);
                        }
                    }
                }
            }
        }

        if (sDataManager.IS_LOGIN && mIsPosition) {
            String key = instrument_id_transaction;
            if (!mPositionLimitLines.containsKey(key + "0")) {
                //添加多头持仓线
                addLongPositionLimitLine();
            } else {
                //刷新空头持仓线
                refreshLongPositionLimitLine();
            }

            if (!mPositionLimitLines.containsKey(key + "1")) {
                //添加多头持仓线
                addShortPositionLimitLine();
            } else {
                //刷新空头持仓线
                refreshShortPositionLimitLine();
            }
        }
    }

    /**
     * date: 6/1/18
     * author: chenli
     * description: 增加持仓线
     */
    protected void addPositionLimitLines() {
        addLongPositionLimitLine();
        addShortPositionLimitLine();
    }

    /**
     * date: 6/6/18
     * author: chenli
     * description: 添加多头持仓线
     */
    private void addLongPositionLimitLine() {
        try {
            String key = instrument_id_transaction;
            UserEntity userEntity = sDataManager.getTradeBean().getUsers().get(sDataManager.USER_ID);
            if (userEntity == null) return;
            PositionEntity positionEntity = userEntity.getPositions().get(key);

            if (positionEntity == null) return;
            int volume_long = Integer.parseInt(positionEntity.getVolume_long());
            if (volume_long != 0) {
                String limit_long = LatestFileManager.saveScaleByPtick(positionEntity.getOpen_price_long(), key);
                String label_long = positionEntity.getInstrument_id() + "@" + limit_long + "/" + volume_long + "手";
                generateLimitLine(Float.valueOf(limit_long), label_long, mColorBuy, key + "0");
            }
        } catch (NumberFormatException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * date: 6/6/18
     * author: chenli
     * description: 添加空头持仓线
     */
    private void addShortPositionLimitLine() {
        try {
            String key = instrument_id_transaction;
            UserEntity userEntity = sDataManager.getTradeBean().getUsers().get(sDataManager.USER_ID);
            if (userEntity == null) return;
            PositionEntity positionEntity = userEntity.getPositions().get(key);
            if (positionEntity == null) return;

            int volume_short = Integer.parseInt(positionEntity.getVolume_short());
            if (volume_short != 0) {
                String limit_short = LatestFileManager.saveScaleByPtick(positionEntity.getOpen_price_short(), key);
                String label_short = positionEntity.getInstrument_id() + "@" + limit_short + "/" + volume_short + "手";
                generateLimitLine(Float.valueOf(limit_short), label_short, mColorSell, key + "1");
            }
        } catch (NumberFormatException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * date: 6/6/18
     * author: chenli
     * description: 添加limitLine
     */
    private void generateLimitLine(float limit, String label, int color, String limitKey) {
        LimitLine limitLine = new LimitLine(limit, label);
        limitLine.setLineWidth(1f);
        limitLine.enableDashedLine(10f, 10f, 0f);
        limitLine.setLineColor(color);
        limitLine.setLabelPosition(LimitLine.LimitLabelPosition.LEFT_BOTTOM);
        limitLine.setTextSize(10f);
        limitLine.setTextColor(mColorText);
        mPositionLimitLines.put(limitKey, limitLine);
        mChart.getAxisLeft().addLimitLine(limitLine);
    }

    /**
     * date: 6/1/18
     * author: chenli
     * description: 刷新多头持仓线
     */
    private void refreshLongPositionLimitLine() {
        try {
            String key = instrument_id_transaction;
            UserEntity userEntity = sDataManager.getTradeBean().getUsers().get(sDataManager.USER_ID);
            if (userEntity == null) return;
            PositionEntity positionEntity = userEntity.getPositions().get(key);
            String limitKey = key + "0";
            if (positionEntity == null) return;

            int volume_long = Integer.parseInt(positionEntity.getVolume_long());
            if (volume_long != 0) {
                String limit_long_S = LatestFileManager.saveScaleByPtick(positionEntity.getOpen_price_long(), key);
                float limit_long = Float.parseFloat(limit_long_S);
                LimitLine limitLine = mPositionLimitLines.get(limitKey);
                if (limitLine.getLimit() != limit_long) {
                    String label_long = positionEntity.getInstrument_id() + "@" + limit_long_S + "/" + volume_long + "手";
                    mChart.getAxisLeft().removeLimitLine(mPositionLimitLines.get(limitKey));
                    mPositionLimitLines.remove(limitKey);
                    generateLimitLine(limit_long, label_long, mColorBuy, limitKey);
                }
            } else {
                mChart.getAxisLeft().removeLimitLine(mPositionLimitLines.get(limitKey));
                mPositionLimitLines.remove(limitKey);
            }
        } catch (NumberFormatException ex) {
            ex.printStackTrace();
        }

    }

    /**
     * date: 6/1/18
     * author: chenli
     * description: 刷新空头持仓线
     */
    private void refreshShortPositionLimitLine() {
        try {
            String key = instrument_id_transaction;
            UserEntity userEntity = sDataManager.getTradeBean().getUsers().get(sDataManager.USER_ID);
            if (userEntity == null) return;
            PositionEntity positionEntity = userEntity.getPositions().get(key);
            String limitKey = key + "1";
            if (positionEntity == null) return;

            int volume_short = Integer.parseInt(positionEntity.getVolume_short());
            if (volume_short != 0) {
                String limit_short_S = LatestFileManager.saveScaleByPtick(positionEntity.getOpen_price_short(), key);
                float limit_short = Float.parseFloat(limit_short_S);
                LimitLine limitLine = mPositionLimitLines.get(limitKey);
                if (limitLine.getLimit() != limit_short) {
                    String label_short = positionEntity.getInstrument_id() + "@" + limit_short_S + "/" + volume_short + "手";
                    mChart.getAxisLeft().removeLimitLine(mPositionLimitLines.get(limitKey));
                    mPositionLimitLines.remove(limitKey);
                    generateLimitLine(limit_short, label_short, mColorSell, limitKey);
                }
            } else {
                mChart.getAxisLeft().removeLimitLine(mPositionLimitLines.get(limitKey));
                mPositionLimitLines.remove(limitKey);
            }
        } catch (NumberFormatException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * date: 7/9/17
     * author: chenli
     * description: 移除持仓线
     */
    protected void removePositionLimitLines() {
        if (!mPositionLimitLines.isEmpty()) {
            for (LimitLine limitLine :
                    mPositionLimitLines.values()) {
                mChart.getAxisLeft().removeLimitLine(limitLine);
            }
        }
        mPositionLimitLines.clear();
    }

    /**
     * date: 6/1/18
     * author: chenli
     * description: 增加一条挂单线
     */
    private void addOneOrderLimitLine(OrderEntity orderEntity) {
        try {
            String limit_volume = orderEntity.getVolume_orign();
            String limit_price = LatestFileManager.saveScaleByPtick(orderEntity.getLimit_price(), instrument_id_transaction);
            LimitLine limitLine = new LimitLine(Float.parseFloat(limit_price),
                    orderEntity.getInstrument_id() + "@" + limit_price + "/" + limit_volume + "手");
            mOrderLimitLines.put(orderEntity.getKey(), limitLine);
            limitLine.setLineWidth(1f);
            limitLine.disableDashedLine();
            if ("BUY".equals(orderEntity.getDirection()))
                limitLine.setLineColor(mColorBuy);
            else limitLine.setLineColor(mColorSell);
            limitLine.setLabelPosition(LimitLine.LimitLabelPosition.LEFT_BOTTOM);
            limitLine.setTextSize(10f);
            limitLine.setTextColor(mColorText);
            mChart.getAxisLeft().addLimitLine(limitLine);
        } catch (NumberFormatException ex) {
            ex.printStackTrace();
        }

    }

    /**
     * date: 6/1/18
     * author: chenli
     * description: 移除一条挂单线
     */
    private void removeOneOrderLimitLine(String key) {
        mChart.getAxisLeft().removeLimitLine(mOrderLimitLines.get(key));
        mOrderLimitLines.remove(key);
    }

    /**
     * date: 7/9/17
     * author: chenli
     * description: 增加挂单线
     */
    protected void addOrderLimitLines() {
        UserEntity userEntity = sDataManager.getTradeBean().getUsers().get(sDataManager.USER_ID);
        if (userEntity == null) return;
        for (OrderEntity orderEntity :
                userEntity.getOrders().values()) {
            if (orderEntity != null && (orderEntity.getExchange_id() + "." + orderEntity.getInstrument_id())
                    .equals(instrument_id_transaction) && "ALIVE".equals(orderEntity.getStatus())) {
                addOneOrderLimitLine(orderEntity);
            }
        }
    }

    /**
     * date: 7/9/17
     * author: chenli
     * description: 移除挂单线
     */
    protected void removeOrderLimitLines() {
        if (!mOrderLimitLines.isEmpty()) {
            for (LimitLine limitLine :
                    mOrderLimitLines.values()) {
                mChart.getAxisLeft().removeLimitLine(limitLine);
            }
        }
        mOrderLimitLines.clear();
    }

    /**
     * date: 6/1/18
     * author: chenli
     * description: 注册行情交易广播
     */
    private void registerBroaderCast() {
        mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String mDataString = intent.getStringExtra("msg");
                refreshChart(mDataString);
            }
        };
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mReceiver, new IntentFilter(MD_BROADCAST_ACTION));

        mReceiver1 = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String mDataString = intent.getStringExtra("msg");
                refreshChart(mDataString);
            }
        };
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mReceiver1, new IntentFilter(TD_BROADCAST_ACTION));
    }

    /**
     * date: 6/1/18
     * author: chenli
     * description: k线数据订阅
     */
    @Override
    public void onResume() {
        super.onResume();
        registerBroaderCast();
        if (BaseApplication.getWebSocketService() != null)
            switch (mKlineType) {
                case CURRENT_DAY:
                    BaseApplication.getWebSocketService().sendSetChart(instrument_id);
                    break;
                case KLINE_DAY:
                    BaseApplication.getWebSocketService().sendSetChartDay(instrument_id, VIEW_WIDTH);
                    break;
                case KLINE_HOUR:
                    BaseApplication.getWebSocketService().sendSetChartHour(instrument_id, VIEW_WIDTH);
                    break;
                case KLINE_MINUTE:
                    BaseApplication.getWebSocketService().sendSetChartMin(instrument_id, VIEW_WIDTH);
                    break;
                default:
                    break;
            }
    }

    @Override
    public void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mReceiver);
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mReceiver1);
    }

    /**
     * date: 6/1/18
     * author: chenli
     * description: k线数据取消订阅
     */
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        EventBus.getDefault().unregister(this);
        if (BaseApplication.getWebSocketService() != null)
            switch (mKlineType) {
                case CURRENT_DAY:
                    BaseApplication.getWebSocketService().sendSetChart("");
                    break;
                case KLINE_DAY:
                    BaseApplication.getWebSocketService().sendSetChartDay("", VIEW_WIDTH);
                    break;
                case KLINE_HOUR:
                    BaseApplication.getWebSocketService().sendSetChartHour("", VIEW_WIDTH);
                    break;
                case KLINE_MINUTE:
                    BaseApplication.getWebSocketService().sendSetChartMin("", VIEW_WIDTH);
                    break;
                default:
                    break;
            }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

}

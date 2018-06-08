package com.xinyi.shinnyfutures.view.fragment;

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
import com.xinyi.shinnyfutures.R;
import com.xinyi.shinnyfutures.application.BaseApplicationLike;
import com.xinyi.shinnyfutures.model.bean.accountinfobean.OrderEntity;
import com.xinyi.shinnyfutures.model.bean.accountinfobean.PositionEntity;
import com.xinyi.shinnyfutures.model.bean.futureinfobean.KlineEntity;
import com.xinyi.shinnyfutures.model.bean.futureinfobean.QuoteEntity;
import com.xinyi.shinnyfutures.model.bean.searchinfobean.SearchEntity;
import com.xinyi.shinnyfutures.model.engine.DataManager;
import com.xinyi.shinnyfutures.utils.LatestFileUtils;
import com.xinyi.shinnyfutures.utils.LogUtils;
import com.xinyi.shinnyfutures.utils.MathUtils;
import com.xinyi.shinnyfutures.view.activity.FutureInfoActivity;
import com.xinyi.shinnyfutures.view.activity.LoginActivity;

import org.greenrobot.eventbus.EventBus;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import static com.xinyi.shinnyfutures.constants.CommonConstants.CLOSE;
import static com.xinyi.shinnyfutures.constants.CommonConstants.CURRENT_DAY;
import static com.xinyi.shinnyfutures.constants.CommonConstants.ERROR;
import static com.xinyi.shinnyfutures.constants.CommonConstants.KLINE_DAY;
import static com.xinyi.shinnyfutures.constants.CommonConstants.KLINE_HOUR;
import static com.xinyi.shinnyfutures.constants.CommonConstants.KLINE_MINUTE;
import static com.xinyi.shinnyfutures.constants.CommonConstants.MESSAGE;
import static com.xinyi.shinnyfutures.constants.CommonConstants.MESSAGE_ORDER;
import static com.xinyi.shinnyfutures.constants.CommonConstants.MESSAGE_POSITION;
import static com.xinyi.shinnyfutures.constants.CommonConstants.OPEN;
import static com.xinyi.shinnyfutures.constants.CommonConstants.VIEW_WIDTH;
import static com.xinyi.shinnyfutures.model.service.WebSocketService.BROADCAST_ACTION;
import static com.xinyi.shinnyfutures.model.service.WebSocketService.BROADCAST_ACTION_TRANSACTION;

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
    protected Map<String, LimitLine> positionLimitLines = new HashMap<>();
    /**
     * date: 7/9/17
     * description: 挂单线
     */
    protected Map<String, LimitLine> orderLimitLines = new HashMap<>();

    protected DataManager dataManager = DataManager.getInstance();
    protected BroadcastReceiver mReceiver;
    protected BroadcastReceiver mReceiver1;
    protected String instrument_id;
    protected String exchange_id;
    protected float preSettlement;
    protected Calendar calendar = Calendar.getInstance();
    protected SimpleDateFormat simpleDateFormat;
    protected SparseArray<String> xVals = new SparseArray<>();
    protected Map<String, KlineEntity.DataEntity> dataEntities;
    protected int mLayoutId;
    protected String mKlineType;
    protected int buttonId;
    private ViewDataBinding mViewDataBinding;

    /**
     * date: 7/9/17
     * author: chenli
     * description: 刷新K线图
     */
    private void refreshChart(String mDataString) {
        switch (mDataString) {
            case OPEN:
                break;
            case CLOSE:
                break;
            case ERROR:
                break;
            case MESSAGE:
                loadChartData();
                break;
            case MESSAGE_ORDER:
                if (LoginActivity.isIsLogin() && mIsPending) {
                    for (OrderEntity orderEntity :
                            dataManager.getAccountBean().getOrder().values()) {
                        if (orderEntity != null && orderEntity.getInstrument_id().equals(instrument_id)) {
                            String key = orderEntity.getKey();
                            if (!orderLimitLines.containsKey(key)) {
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
                break;
            case MESSAGE_POSITION:
                if (LoginActivity.isIsLogin() && mIsPosition) {
                    String key = exchange_id + "." + instrument_id ;
                    if (!positionLimitLines.containsKey(key+"0")) {
                        //添加多头持仓线
                        addLongPositionLimitLine();
                    } else {
                        //刷新空头持仓线
                        refreshLongPositionLimitLine();
                    }

                    if (!positionLimitLines.containsKey(key+"1")) {
                        //添加多头持仓线
                        addShortPositionLimitLine();
                    } else {
                        //刷新空头持仓线
                        refreshShortPositionLimitLine();
                    }
                }
                break;
            default:
                break;
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
        exchange_id = LatestFileUtils.getSearchEntities().get(instrument_id).getExchangeId();
        mIsAverage = ((FutureInfoActivity) getActivity()).isAverage();
        if (!mIsAverage) mChart.getLegend().setEnabled(false);
        mIsPosition = ((FutureInfoActivity) getActivity()).isPosition();
        mIsPending = ((FutureInfoActivity) getActivity()).isPending();
        if (LoginActivity.isIsLogin()) {
            if (mIsPosition) addPositionLimitLines();
            if (mIsPending) addOrderLimitLines();
        }

        QuoteEntity quoteEntity = dataManager.getRtnData().getQuotes().get(instrument_id);
        if (quoteEntity != null) {
            try {
                preSettlement = Float.parseFloat(quoteEntity.getPre_settlement());
            } catch (NumberFormatException ex) {
                ex.printStackTrace();
            }
        } else preSettlement = 1;

    }

    @Override
    public void update() {
        loadChartData();
    }

    protected void loadChartData() {
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

    protected void initData() {
        mColorHomeBg = ContextCompat.getColor(getActivity(), R.color.kline_background);
        mColorAxis = ContextCompat.getColor(getActivity(), R.color.kline_axis);
        mColorGrid = ContextCompat.getColor(getActivity(), R.color.kline_grid);
        mColorText = ContextCompat.getColor(getActivity(), R.color.kline_text);
        mColorBuy = ContextCompat.getColor(getActivity(), R.color.kline_position);
        mColorSell = ContextCompat.getColor(getActivity(), R.color.kline_order);
    }

    /**
     * date: 6/6/18
     * author: chenli
     * description: 获取开仓均价
     */
    private float getPrice(String open_cost, String open_price, String vm, int volume){
        try {
            float openCost = Float.parseFloat(open_cost);
            float openPrice = Float.parseFloat(open_price);
            int vmI = Integer.parseInt(vm);
            if (openPrice != 0)return openPrice;
            else if (openCost != 0){
                return openCost / (volume * vmI);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return 0.0f;
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
            String key = exchange_id + "." + instrument_id ;
            SearchEntity searchEntity = LatestFileUtils.getSearchEntities().get(instrument_id);
            String vm = searchEntity != null ? searchEntity.getVm() : "1";
            PositionEntity positionEntity = dataManager.getAccountBean().getPosition().get(key);
            if (positionEntity == null) return;

            String available_long = MathUtils.add(positionEntity.getVolume_long_his(), positionEntity.getVolume_long_today());
            int volume_long = Integer.parseInt(MathUtils.add(available_long, positionEntity.getVolume_long_frozen()));
            if (volume_long != 0){
                float limit_long = getPrice(positionEntity.getOpen_cost_long(), positionEntity.getOpen_price_long(), vm, volume_long);
                String label_long = positionEntity.getInstrument_id() + " " + limit_long;
                generateLimitLine(limit_long, label_long, mColorBuy, key+"0");
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
            String key = exchange_id + "." + instrument_id ;
            SearchEntity searchEntity = LatestFileUtils.getSearchEntities().get(instrument_id);
            String vm = searchEntity != null ? searchEntity.getVm() : "1";
            PositionEntity positionEntity = dataManager.getAccountBean().getPosition().get(key);
            if (positionEntity == null) return;
            String available_short = MathUtils.add(positionEntity.getVolume_short_his(), positionEntity.getVolume_short_today());
            int volume_short = Integer.parseInt(MathUtils.add(available_short, positionEntity.getVolume_short_frozen()));
            if (volume_short != 0){
                float limit_short = getPrice(positionEntity.getOpen_cost_short(), positionEntity.getOpen_price_short(), vm, volume_short);
                String label_short = positionEntity.getInstrument_id() + " " + limit_short;
                generateLimitLine(limit_short, label_short, mColorSell, key+"1");
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
    private void generateLimitLine(float limit, String label, int color, String limitKey){
        LimitLine limitLine = new LimitLine(limit, label);
        limitLine.setLineWidth(2f);
        limitLine.enableDashedLine(10f, 10f, 0f);
        limitLine.setLineColor(color);
        limitLine.setLabelPosition(LimitLine.LimitLabelPosition.LEFT_BOTTOM);
        limitLine.setTextSize(10f);
        limitLine.setTextColor(mColorText);
        positionLimitLines.put(limitKey, limitLine);
        mChart.getAxisLeft().addLimitLine(limitLine);
    }

    /**
     * date: 6/1/18
     * author: chenli
     * description: 刷新多头持仓线
     */
    private void refreshLongPositionLimitLine() {
        try {
            String key = exchange_id + "." + instrument_id ;
            SearchEntity searchEntity = LatestFileUtils.getSearchEntities().get(instrument_id);
            String vm = searchEntity != null ? searchEntity.getVm() : "1";
            PositionEntity positionEntity = dataManager.getAccountBean().getPosition().get(key);
            String limitKey = key + "0";
            if (positionEntity == null) return;
            String available_long = MathUtils.add(positionEntity.getVolume_long_his(), positionEntity.getVolume_long_today());
            int volume_long = Integer.parseInt(MathUtils.add(available_long, positionEntity.getVolume_long_frozen()));
            if (volume_long != 0){
                float limit_long = getPrice(positionEntity.getOpen_cost_long(), positionEntity.getOpen_price_long(), vm, volume_long);
                LimitLine limitLine = positionLimitLines.get(limitKey);
                if (limitLine.getLimit() != limit_long){
                    String label_long = positionEntity.getInstrument_id() + " " + limit_long;
                    mChart.getAxisLeft().removeLimitLine(positionLimitLines.get(limitKey));
                    positionLimitLines.remove(limitKey);
                    generateLimitLine(limit_long, label_long, mColorBuy, limitKey);
                }
            }else {
                mChart.getAxisLeft().removeLimitLine(positionLimitLines.get(limitKey));
                positionLimitLines.remove(limitKey);
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
            String key = exchange_id + "." + instrument_id ;
            SearchEntity searchEntity = LatestFileUtils.getSearchEntities().get(instrument_id);
            String vm = searchEntity != null ? searchEntity.getVm() : "1";
            PositionEntity positionEntity = dataManager.getAccountBean().getPosition().get(key);
            String limitKey = key + "1";
            if (positionEntity == null) return;
            String available_short = MathUtils.add(positionEntity.getVolume_short_his(), positionEntity.getVolume_short_today());
            int volume_short = Integer.parseInt(MathUtils.add(available_short, positionEntity.getVolume_short_frozen()));
            if (volume_short != 0){
                float limit_short = getPrice(positionEntity.getOpen_cost_short(), positionEntity.getOpen_price_short(), vm, volume_short);
                LimitLine limitLine = positionLimitLines.get(limitKey);
                if (limitLine.getLimit() != limit_short){
                    String label_short = positionEntity.getInstrument_id() + " " + limit_short;
                    mChart.getAxisLeft().removeLimitLine(positionLimitLines.get(limitKey));
                    positionLimitLines.remove(limitKey);
                    generateLimitLine(limit_short, label_short, mColorSell, limitKey);
                }
            }else {
                mChart.getAxisLeft().removeLimitLine(positionLimitLines.get(limitKey));
                positionLimitLines.remove(limitKey);
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
        if (!positionLimitLines.isEmpty()) {
            for (LimitLine limitLine :
                    positionLimitLines.values()) {
                mChart.getAxisLeft().removeLimitLine(limitLine);
            }
        }
        positionLimitLines.clear();
    }

    /**
     * date: 6/1/18
     * author: chenli
     * description: 增加一条挂单线
     */
    private void addOneOrderLimitLine(OrderEntity orderEntity) {
        try {
            LimitLine limitLine = new LimitLine(Float.parseFloat(orderEntity.getLimit_price()), orderEntity.getOrder_id() + "@" + orderEntity.getLimit_price());
            orderLimitLines.put(orderEntity.getKey(), limitLine);
            limitLine.setLineWidth(2f);
            limitLine.enableDashedLine(10f, 10f, 0f);
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
        mChart.getAxisLeft().removeLimitLine(orderLimitLines.get(key));
        orderLimitLines.remove(key);
    }

    /**
     * date: 7/9/17
     * author: chenli
     * description: 增加挂单线
     */
    protected void addOrderLimitLines() {
        for (OrderEntity orderEntity :
                dataManager.getAccountBean().getOrder().values()) {
            if (orderEntity != null && orderEntity.getInstrument_id().equals(instrument_id) && "ALIVE".equals(orderEntity.getStatus())) {
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
        if (!orderLimitLines.isEmpty()) {
            for (LimitLine limitLine :
                    orderLimitLines.values()) {
                mChart.getAxisLeft().removeLimitLine(limitLine);
            }
        }
        orderLimitLines.clear();
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
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mReceiver, new IntentFilter(BROADCAST_ACTION));

        mReceiver1 = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String mDataString = intent.getStringExtra("msg");
                refreshChart(mDataString);
            }
        };
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mReceiver1, new IntentFilter(BROADCAST_ACTION_TRANSACTION));
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
        if (BaseApplicationLike.getWebSocketService() != null)
            switch (mKlineType) {
                case CURRENT_DAY:
                    BaseApplicationLike.getWebSocketService().sendSetChart(instrument_id);
                    break;
                case KLINE_DAY:
                    BaseApplicationLike.getWebSocketService().sendSetChartDay(instrument_id, VIEW_WIDTH);
                    break;
                case KLINE_HOUR:
                    BaseApplicationLike.getWebSocketService().sendSetChartHour(instrument_id, VIEW_WIDTH);
                    break;
                case KLINE_MINUTE:
                    BaseApplicationLike.getWebSocketService().sendSetChartMin(instrument_id, VIEW_WIDTH);
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
        if (BaseApplicationLike.getWebSocketService() != null)
            switch (mKlineType) {
                case CURRENT_DAY:
                    BaseApplicationLike.getWebSocketService().sendSetChart("");
                    break;
                case KLINE_DAY:
                    BaseApplicationLike.getWebSocketService().sendSetChartDay("", VIEW_WIDTH);
                    break;
                case KLINE_HOUR:
                    BaseApplicationLike.getWebSocketService().sendSetChartHour("", VIEW_WIDTH);
                    break;
                case KLINE_MINUTE:
                    BaseApplicationLike.getWebSocketService().sendSetChartMin("", VIEW_WIDTH);
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

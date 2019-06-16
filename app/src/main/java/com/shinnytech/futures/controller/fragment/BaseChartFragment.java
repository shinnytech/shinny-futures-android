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
import com.shinnytech.futures.controller.activity.FutureInfoActivity;
import com.shinnytech.futures.model.bean.accountinfobean.OrderEntity;
import com.shinnytech.futures.model.bean.accountinfobean.PositionEntity;
import com.shinnytech.futures.model.bean.accountinfobean.UserEntity;
import com.shinnytech.futures.model.bean.eventbusbean.RedrawEvent;
import com.shinnytech.futures.model.bean.eventbusbean.VisibilityEvent;
import com.shinnytech.futures.model.bean.futureinfobean.QuoteEntity;
import com.shinnytech.futures.model.bean.searchinfobean.SearchEntity;
import com.shinnytech.futures.model.engine.DataManager;
import com.shinnytech.futures.model.engine.LatestFileManager;
import com.shinnytech.futures.utils.SPUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.shinnytech.futures.application.BaseApplication.MD_BROADCAST_ACTION;
import static com.shinnytech.futures.application.BaseApplication.TD_BROADCAST_ACTION;
import static com.shinnytech.futures.constants.CommonConstants.CONFIG_ORDER_LINE;
import static com.shinnytech.futures.constants.CommonConstants.CONFIG_POSITION_LINE;
import static com.shinnytech.futures.constants.CommonConstants.CURRENT_DAY_FRAGMENT;
import static com.shinnytech.futures.constants.CommonConstants.DALIAN;
import static com.shinnytech.futures.constants.CommonConstants.DALIANZUHE;
import static com.shinnytech.futures.constants.CommonConstants.DIRECTION_BUY;
import static com.shinnytech.futures.constants.CommonConstants.DOMINANT;
import static com.shinnytech.futures.constants.CommonConstants.MD_MESSAGE;
import static com.shinnytech.futures.constants.CommonConstants.NENGYUAN;
import static com.shinnytech.futures.constants.CommonConstants.OPTIONAL;
import static com.shinnytech.futures.constants.CommonConstants.SHANGHAI;
import static com.shinnytech.futures.constants.CommonConstants.STATUS_ALIVE;
import static com.shinnytech.futures.constants.CommonConstants.STATUS_FINISHED;
import static com.shinnytech.futures.constants.CommonConstants.TD_MESSAGE;
import static com.shinnytech.futures.constants.CommonConstants.VIEW_WIDTH;
import static com.shinnytech.futures.constants.CommonConstants.ZHENGZHOU;
import static com.shinnytech.futures.constants.CommonConstants.ZHENGZHOUZUHE;
import static com.shinnytech.futures.constants.CommonConstants.ZHONGJIN;

/**
 * date: 9/20/17
 * author: chenli
 * description:
 * version:
 * state:
 */
public class BaseChartFragment extends LazyLoadFragment {

    protected static final int FLING_MAX_DISTANCE_X = 300;// X轴移动最大距离
    protected static final int FLING_MIN_DISTANCE_Y = 200;// Y轴移动最小距离
    protected static final int FLING_MIN_VELOCITY = 200;// 移动最大速度

    public String mFragmentType;
    public String mKlineType;
    /**
     * date: 7/9/17
     * description: 组合图
     */
    protected CombinedChart mTopChartViewBase;
    protected CombinedChart mMiddleChartViewBase;
    protected CombinedChart mBottomChartViewBase;
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
     * description: 持仓线
     */
    protected Map<String, LimitLine> mPositionLimitLines;
    /**
     * date: 2018/11/20
     * description: 持仓线合约手数
     */
    protected Map<String, Integer> mPositionVolumes;
    /**
     * date: 7/9/17
     * description: 挂单线
     */
    protected Map<String, LimitLine> mOrderLimitLines;
    /**
     * date: 2018/11/20
     * description: 挂单合约手数
     */
    protected Map<String, Integer> mOrderVolumes;
    /**
     * date: 2019/5/22
     * description: 目前合约所在的列表
     */
    protected List<String> mInsList;
    protected DataManager sDataManager;
    protected BroadcastReceiver mReceiver;
    protected BroadcastReceiver mReceiver1;
    protected String instrument_id;
    protected String instrument_id_transaction;
    protected Calendar mCalendar;
    protected SimpleDateFormat mSimpleDateFormat;
    protected SparseArray<String> xVals;
    protected int mLayoutId;
    private ViewDataBinding mViewDataBinding;

    /**
     * date: 7/9/17
     * author: chenli
     * description: 刷新K线图
     */
    private void refreshChart(String mDataString) {
        try {
            switch (mDataString) {
                case MD_MESSAGE:
                    drawKline();
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
        mTopChartViewBase = mViewDataBinding.getRoot().findViewById(R.id.chart);
        mMiddleChartViewBase = mViewDataBinding.getRoot().findViewById(R.id.middleChart);
        mBottomChartViewBase = mViewDataBinding.getRoot().findViewById(R.id.bottomChart);
        //注册EventBus
        EventBus.getDefault().register(this);
        initData();
        initChart();
        initInsList();
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

        mIsPosition = (boolean) SPUtils.get(BaseApplication.getContext(), CONFIG_POSITION_LINE, true);
        mIsPending = (boolean) SPUtils.get(BaseApplication.getContext(), CONFIG_ORDER_LINE, true);

    }

    /**
     * date: 2019/5/22
     * author: chenli
     * description: 初始化本合约所在列表
     */
    private void initInsList() {
        String title = DataManager.getInstance().EXCHANGE_ID;
        Map<String, QuoteEntity> map = new HashMap<>();
        switch (title) {
            case OPTIONAL:
                map = LatestFileManager.getOptionalInsList();
                break;
            case DOMINANT:
                map = LatestFileManager.getMainInsList();
                break;
            case SHANGHAI:
                map = LatestFileManager.getShangqiInsList();
                break;
            case NENGYUAN:
                map = LatestFileManager.getNengyuanInsList();
                break;
            case DALIAN:
                map = LatestFileManager.getDalianInsList();
                break;
            case ZHENGZHOU:
                map = LatestFileManager.getZhengzhouInsList();
                break;
            case ZHONGJIN:
                map = LatestFileManager.getZhongjinInsList();
                break;
            case DALIANZUHE:
                map = LatestFileManager.getDalianzuheInsList();
                break;
            case ZHENGZHOUZUHE:
                map = LatestFileManager.getZhengzhouzuheInsList();
                break;
            default:
                break;
        }
        mInsList = new ArrayList<>(map.keySet());
        if (OPTIONAL.equals(title)) {
            DataManager dataManager = DataManager.getInstance();
            UserEntity userEntity = dataManager.getTradeBean().getUsers().get(dataManager.LOGIN_USER_ID);
            if (userEntity != null) {
                for (PositionEntity positionEntity : userEntity.getPositions().values()) {
                    try {
                        int volume_long = Integer.parseInt(positionEntity.getVolume_long());
                        int volume_short = Integer.parseInt(positionEntity.getVolume_short());
                        String ins = positionEntity.getExchange_id() + "." + positionEntity.getInstrument_id();
                        if ((volume_long != 0 || volume_short != 0) && !mInsList.contains(ins))
                            mInsList.add(ins);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
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
        mPositionVolumes = new HashMap<>();
        mOrderLimitLines = new HashMap<>();
        mOrderVolumes = new HashMap<>();
        mCalendar = Calendar.getInstance();
        sDataManager = DataManager.getInstance();
        xVals = new SparseArray<>();
    }

    protected void initChart() {
        mTopChartViewBase.getDescription().setEnabled(false);
        mTopChartViewBase.setDrawGridBackground(true);
        mTopChartViewBase.setBackgroundColor(mColorHomeBg);
        mTopChartViewBase.setGridBackgroundColor(mColorHomeBg);
        mTopChartViewBase.setDrawValueAboveBar(false);
        mTopChartViewBase.setBorderWidth(1f);
        mTopChartViewBase.setDrawBorders(true);
        mTopChartViewBase.setNoDataText("数据申请中");
        mTopChartViewBase.setAutoScaleMinMaxEnabled(true);
        mTopChartViewBase.setDragEnabled(true);
        mTopChartViewBase.setViewPortOffsets(0, 40, 0, 1);
        mTopChartViewBase.setDoubleTapToZoomEnabled(false);
        mTopChartViewBase.setHighlightPerTapEnabled(false);
        mTopChartViewBase.setHighlightPerDragEnabled(false);

        mMiddleChartViewBase.getDescription().setEnabled(false);
        mMiddleChartViewBase.setDrawGridBackground(true);
        mMiddleChartViewBase.setBackgroundColor(mColorHomeBg);
        mMiddleChartViewBase.setGridBackgroundColor(mColorHomeBg);
        mMiddleChartViewBase.setDrawValueAboveBar(false);
        mMiddleChartViewBase.setNoDataText("数据申请中");
        mMiddleChartViewBase.setAutoScaleMinMaxEnabled(true);
        mMiddleChartViewBase.setDragEnabled(true);
        mMiddleChartViewBase.setDrawBorders(false);
        mMiddleChartViewBase.setViewPortOffsets(0, 0, 0, 40);
        mMiddleChartViewBase.setDoubleTapToZoomEnabled(false);
        mMiddleChartViewBase.setHighlightPerTapEnabled(false);
        mMiddleChartViewBase.setHighlightPerDragEnabled(false);

        mBottomChartViewBase.getDescription().setEnabled(false);
        mBottomChartViewBase.setDrawGridBackground(true);
        mBottomChartViewBase.setBackgroundColor(mColorHomeBg);
        mBottomChartViewBase.setGridBackgroundColor(mColorHomeBg);
        mBottomChartViewBase.setDrawValueAboveBar(false);
        mBottomChartViewBase.setNoDataText("数据申请中");
        mBottomChartViewBase.setAutoScaleMinMaxEnabled(true);
        mBottomChartViewBase.setDragEnabled(true);
        mBottomChartViewBase.setDrawBorders(false);
        mBottomChartViewBase.setViewPortOffsets(0, 0, 0, 1);
        mBottomChartViewBase.setDoubleTapToZoomEnabled(false);
        mBottomChartViewBase.setHighlightPerTapEnabled(false);
        mBottomChartViewBase.setHighlightPerDragEnabled(false);

        //切换周期时控制图表显示
        if (sDataManager.IS_SHOW_VP_CONTENT) {
            mMiddleChartViewBase.setVisibility(View.GONE);
//            mBottomChartViewBase.setVisibility(View.GONE);
        } else {
            mMiddleChartViewBase.setVisibility(View.VISIBLE);
//            mBottomChartViewBase.setVisibility(View.VISIBLE);
        }

    }

    @Override
    public void show() {
    }


    @Override
    public void leave() {
    }

    /**
     * date: 6/28/18
     * author: chenli
     * description: 刷新行情信息
     */
    protected void drawKline() {
    }

    /**
     * date: 2019/5/12
     * author: chenli
     * description: 清空行情图
     */
    protected void clearKline() {
    }

    /**
     * date: 6/28/18
     * author: chenli
     * description: 刷新账户信息
     */
    private void refreshTrade() {
        UserEntity userEntity = sDataManager.getTradeBean().getUsers().get(sDataManager.LOGIN_USER_ID);
        if (userEntity == null) return;
        if (mIsPending) {
            for (OrderEntity orderEntity :
                    userEntity.getOrders().values()) {
                if (orderEntity != null &&
                        (orderEntity.getExchange_id() + "." + orderEntity.getInstrument_id())
                                .equals(instrument_id_transaction)) {
                    String key = orderEntity.getKey();

                    if (!mOrderLimitLines.containsKey(key)) {
                        if (STATUS_ALIVE.equals(orderEntity.getStatus())) {
                            addOneOrderLimitLine(orderEntity);
                        }
                    } else {
                        if (STATUS_FINISHED.equals(orderEntity.getStatus())) {
                            removeOneOrderLimitLine(key);
                        }
                    }
                }
            }
        }

        if (mIsPosition) {
            String key = instrument_id_transaction;
            if (!mPositionLimitLines.containsKey(key + "0")) {
                //添加多头持仓线
                addLongPositionLimitLine();
            } else {
                //刷新多头持仓线
                refreshLongPositionLimitLine();
            }

            if (!mPositionLimitLines.containsKey(key + "1")) {
                //添加空持仓线
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
            UserEntity userEntity = sDataManager.getTradeBean().getUsers().get(sDataManager.LOGIN_USER_ID);
            if (userEntity == null) return;
            PositionEntity positionEntity = userEntity.getPositions().get(key);

            if (positionEntity == null) return;
            int volume_long = Integer.parseInt(positionEntity.getVolume_long());
            if (volume_long != 0) {
                String limit_long = LatestFileManager.saveScaleByPtick(positionEntity.getOpen_price_long(), key);
                String label_long = limit_long + "/" + volume_long + "手";
                generateLimitLine(Float.valueOf(limit_long), label_long, mColorBuy, key + "0", volume_long, LimitLine.LimitLabelPosition.LEFT_TOP);
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
            UserEntity userEntity = sDataManager.getTradeBean().getUsers().get(sDataManager.LOGIN_USER_ID);
            if (userEntity == null) return;
            PositionEntity positionEntity = userEntity.getPositions().get(key);
            if (positionEntity == null) return;

            int volume_short = Integer.parseInt(positionEntity.getVolume_short());
            if (volume_short != 0) {
                String limit_short = LatestFileManager.saveScaleByPtick(positionEntity.getOpen_price_short(), key);
                String label_short = limit_short + "/" + volume_short + "手";
                generateLimitLine(Float.valueOf(limit_short), label_short, mColorSell, key + "1", volume_short, LimitLine.LimitLabelPosition.LEFT_BOTTOM);
            }
        } catch (NumberFormatException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * date: 6/6/18
     * author: chenli
     * description: 添加一条持仓线
     */
    private void generateLimitLine(float limit, String label, int color, String limitKey, Integer volume, LimitLine.LimitLabelPosition position) {
        LimitLine limitLine = new LimitLine(limit, label);
        limitLine.setLineWidth(0.7f);
        limitLine.enableDashedLine(10f, 10f, 0f);
        limitLine.setLineColor(color);
        limitLine.setLabelPosition(position);
        limitLine.setTextSize(10f);
        limitLine.setTextColor(mColorText);
        mPositionLimitLines.put(limitKey, limitLine);
        mPositionVolumes.put(limitKey, volume);
        mTopChartViewBase.getAxisLeft().addLimitLine(limitLine);
        mTopChartViewBase.invalidate();
    }

    /**
     * date: 6/1/18
     * author: chenli
     * description: 刷新多头持仓线
     */
    private void refreshLongPositionLimitLine() {
        try {
            String key = instrument_id_transaction;
            UserEntity userEntity = sDataManager.getTradeBean().getUsers().get(sDataManager.LOGIN_USER_ID);
            if (userEntity == null) return;
            PositionEntity positionEntity = userEntity.getPositions().get(key);
            String limitKey = key + "0";
            if (positionEntity == null) return;

            int volume_long = Integer.parseInt(positionEntity.getVolume_long());
            if (volume_long != 0) {
                String limit_long_S = LatestFileManager.saveScaleByPtick(positionEntity.getOpen_price_long(), key);
                float limit_long = Float.parseFloat(limit_long_S);
                LimitLine limitLine = mPositionLimitLines.get(limitKey);
                int volume_long_l = mPositionVolumes.get(limitKey);
                if (limitLine.getLimit() != limit_long || volume_long_l != volume_long) {
                    String label_long = limit_long_S + "/" + volume_long + "手";
                    mTopChartViewBase.getAxisLeft().removeLimitLine(mPositionLimitLines.get(limitKey));
                    mPositionLimitLines.remove(limitKey);
                    mPositionVolumes.remove(limitKey);
                    generateLimitLine(limit_long, label_long, mColorBuy, limitKey, volume_long, LimitLine.LimitLabelPosition.LEFT_TOP);
                }
            } else {
                mTopChartViewBase.getAxisLeft().removeLimitLine(mPositionLimitLines.get(limitKey));
                mTopChartViewBase.invalidate();
                mPositionLimitLines.remove(limitKey);
                mPositionVolumes.remove(limitKey);
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
            UserEntity userEntity = sDataManager.getTradeBean().getUsers().get(sDataManager.LOGIN_USER_ID);
            if (userEntity == null) return;
            PositionEntity positionEntity = userEntity.getPositions().get(key);
            String limitKey = key + "1";
            if (positionEntity == null) return;

            int volume_short = Integer.parseInt(positionEntity.getVolume_short());
            if (volume_short != 0) {
                String limit_short_S = LatestFileManager.saveScaleByPtick(positionEntity.getOpen_price_short(), key);
                float limit_short = Float.parseFloat(limit_short_S);
                LimitLine limitLine = mPositionLimitLines.get(limitKey);
                int volume_short_l = mPositionVolumes.get(limitKey);
                if (limitLine.getLimit() != limit_short || volume_short_l != volume_short) {
                    String label_short = limit_short_S + "/" + volume_short + "手";
                    mTopChartViewBase.getAxisLeft().removeLimitLine(mPositionLimitLines.get(limitKey));
                    mPositionLimitLines.remove(limitKey);
                    mPositionVolumes.remove(limitKey);
                    generateLimitLine(limit_short, label_short, mColorSell, limitKey, volume_short, LimitLine.LimitLabelPosition.LEFT_BOTTOM);
                }
            } else {
                mTopChartViewBase.getAxisLeft().removeLimitLine(mPositionLimitLines.get(limitKey));
                mTopChartViewBase.invalidate();
                mPositionLimitLines.remove(limitKey);
                mPositionVolumes.remove(limitKey);
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
                mTopChartViewBase.getAxisLeft().removeLimitLine(limitLine);
            }
        }
        mPositionLimitLines.clear();
        mPositionVolumes.clear();
    }

    /**
     * date: 6/1/18
     * author: chenli
     * description: 增加一条挂单线
     */
    private void addOneOrderLimitLine(OrderEntity orderEntity) {
        try {
            int limit_volume = Integer.parseInt(orderEntity.getVolume_orign());
            String key = orderEntity.getKey();
            String limit_price = LatestFileManager.saveScaleByPtick(orderEntity.getLimit_price(), instrument_id_transaction);
            LimitLine limitLine = new LimitLine(Float.parseFloat(limit_price),
                    limit_price + "/" + limit_volume + "手");
            mOrderLimitLines.put(key, limitLine);
            mOrderVolumes.put(key, limit_volume);
            limitLine.setLineWidth(0.7f);
            limitLine.disableDashedLine();
            if (DIRECTION_BUY.equals(orderEntity.getDirection())) {
                limitLine.setLabelPosition(LimitLine.LimitLabelPosition.RIGHT_TOP);
                limitLine.setLineColor(mColorBuy);
            } else {
                limitLine.setLabelPosition(LimitLine.LimitLabelPosition.RIGHT_BOTTOM);
                limitLine.setLineColor(mColorSell);
            }
            limitLine.setTextSize(10f);
            limitLine.setTextColor(mColorText);
            mTopChartViewBase.getAxisLeft().addLimitLine(limitLine);
            mTopChartViewBase.invalidate();
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
        mTopChartViewBase.getAxisLeft().removeLimitLine(mOrderLimitLines.get(key));
        mTopChartViewBase.invalidate();
        mOrderLimitLines.remove(key);
        mOrderVolumes.remove(key);
    }

    /**
     * date: 7/9/17
     * author: chenli
     * description: 增加挂单线
     */
    protected void addOrderLimitLines() {
        UserEntity userEntity = sDataManager.getTradeBean().getUsers().get(sDataManager.LOGIN_USER_ID);
        if (userEntity == null) return;
        for (OrderEntity orderEntity :
                userEntity.getOrders().values()) {
            if (orderEntity != null && (orderEntity.getExchange_id() + "." + orderEntity.getInstrument_id())
                    .equals(instrument_id_transaction) && STATUS_ALIVE.equals(orderEntity.getStatus())) {
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
                mTopChartViewBase.getAxisLeft().removeLimitLine(limitLine);
            }
        }
        mOrderLimitLines.clear();
        mOrderVolumes.clear();
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
        if (mIsPosition) addPositionLimitLines();
        if (mIsPending) addOrderLimitLines();
        drawKline();

        registerBroaderCast();
        if (CURRENT_DAY_FRAGMENT.equals(mFragmentType)) {
            BaseApplication.getmMDWebSocket().sendSetChart(instrument_id);
        } else {
            BaseApplication.getmMDWebSocket().sendSetChartKline(instrument_id, VIEW_WIDTH, mKlineType);
        }
    }

    /**
     * date: 2019/1/10
     * author: chenli
     * description: 获取组合两腿
     */
    private String getIns(String ins) {
        if (ins.contains("&") && ins.contains(" ")) {
            SearchEntity searchEntity = LatestFileManager.getSearchEntities().get(ins);
            if (searchEntity != null) {
                String leg1_symbol = searchEntity.getLeg1_symbol();
                String leg2_symbol = searchEntity.getLeg2_symbol();
                ins = leg1_symbol + "," + leg2_symbol;
            }
        }
        return ins;
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
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }


    /**
     * date: 2019/2/20
     * author: chenli
     * description: 控制中间和底部的图表显示
     */
    @Subscribe
    public void onEventBase(VisibilityEvent data) {
        //原本在图上的十字光标消失
        mTopChartViewBase.highlightValue(null);
        if (data.isVisible()) {
            mMiddleChartViewBase.setVisibility(View.VISIBLE);
//            mBottomChartViewBase.setVisibility(View.VISIBLE);
        } else {
            mMiddleChartViewBase.setVisibility(View.GONE);
//            mBottomChartViewBase.setVisibility(View.GONE);
        }
    }

    /**
     * date: 2019/6/4
     * author: chenli
     * description: 切前台重绘界面
     */
    @Subscribe
    public void onEventRedraw(RedrawEvent data) {
        clearKline();
        drawKline();
    }


    /**
     * date: 2019/4/19
     * author: chenli
     * description: 上下滑动获取下个合约代码
     */
    protected String getNextInstrumentId(boolean isNext) {
        int index = mInsList.indexOf(instrument_id);
        if (index == -1) return "";
        if (isNext) index += 1;
        else index -= 1;
        if (index < 0) index = mInsList.size() - 1;
        if (index >= mInsList.size()) index = 0;
        return mInsList.get(index);
    }

}

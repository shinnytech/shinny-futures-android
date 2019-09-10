package com.shinnytech.futures.controller.fragment;

import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;
import android.os.Bundle;
import androidx.core.content.ContextCompat;
import android.util.SparseArray;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.github.mikephil.charting.charts.CombinedChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.LegendEntry;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.Transformer;
import com.shinnytech.futures.R;
import com.shinnytech.futures.application.BaseApplication;
import com.shinnytech.futures.controller.activity.MainActivity;
import com.shinnytech.futures.controller.activity.MainActivityPresenter;
import com.shinnytech.futures.model.bean.accountinfobean.OrderEntity;
import com.shinnytech.futures.model.bean.accountinfobean.PositionEntity;
import com.shinnytech.futures.model.bean.accountinfobean.UserEntity;
import com.shinnytech.futures.model.bean.eventbusbean.SwitchInsEvent;
import com.shinnytech.futures.model.bean.eventbusbean.VisibilityEvent;
import com.shinnytech.futures.model.bean.futureinfobean.KlineEntity;
import com.shinnytech.futures.model.bean.futureinfobean.QuoteEntity;
import com.shinnytech.futures.model.bean.searchinfobean.SearchEntity;
import com.shinnytech.futures.model.engine.DataManager;
import com.shinnytech.futures.model.engine.LatestFileManager;
import com.shinnytech.futures.utils.MathUtils;
import com.shinnytech.futures.utils.SPUtils;
import com.shinnytech.futures.view.custommpchart.mycomponent.MyValueFormatter;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.shinnytech.futures.constants.SettingConstants.CONFIG_ORDER_LINE;
import static com.shinnytech.futures.constants.SettingConstants.CONFIG_POSITION_LINE;
import static com.shinnytech.futures.constants.MarketConstants.CURRENT_DAY_FRAGMENT;
import static com.shinnytech.futures.constants.CommonConstants.DALIAN;
import static com.shinnytech.futures.constants.CommonConstants.DALIANZUHE;
import static com.shinnytech.futures.constants.TradeConstants.DIRECTION_BUY;
import static com.shinnytech.futures.constants.CommonConstants.DOMINANT;
import static com.shinnytech.futures.constants.CommonConstants.NENGYUAN;
import static com.shinnytech.futures.constants.CommonConstants.OPTIONAL;
import static com.shinnytech.futures.constants.CommonConstants.SHANGHAI;
import static com.shinnytech.futures.constants.TradeConstants.STATUS_ALIVE;
import static com.shinnytech.futures.constants.TradeConstants.STATUS_FINISHED;
import static com.shinnytech.futures.constants.MarketConstants.VIEW_WIDTH;
import static com.shinnytech.futures.constants.CommonConstants.ZHENGZHOU;
import static com.shinnytech.futures.constants.CommonConstants.ZHENGZHOUZUHE;
import static com.shinnytech.futures.constants.CommonConstants.ZHONGJIN;
import static java.lang.Float.NaN;

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
     * date: 2019/7/28
     * description: k线看涨色
     */
    protected int mIncreasingColor;
    /**
     * date: 2019/7/28
     * description: k线看跌色
     */
    protected int mDecreasingColor;
    /**
     * date: 2019/7/28
     * description: 十字光标颜色
     */
    protected int mHighlightColor;
    /**
     * date: 2019/7/28
     * description: 持仓线颜色
     */
    protected int mOIColor;
    /**
     * date: 2019/7/28
     * description:
     */
    /**
     * date: 2019/7/28
     * description: diff
     */
    protected int mDiffColor;
    /**
     * date: 2019/7/28
     * description: dea
     */
    protected int mDeaColor;
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
    protected String instrument_id;
    protected String instrument_id_transaction;
    protected Calendar mCalendar;
    protected SparseArray<String> xVals;
    protected String mFragmentType;
    protected String mKlineType;
    protected SimpleDateFormat mSimpleDateFormat;
    protected int mLayoutId;
    protected FutureInfoFragment mFutureInfoFragment;
    private ViewDataBinding mViewDataBinding;
    private View mView;
    protected int mS;
    protected int mL;
    protected int mN;
    protected Map<Integer, Float> mEMAs;
    protected Map<Integer, Float> mEMAl;
    protected Map<Integer, Float> mDEA;
    protected String yValue;
    protected boolean mIsLongPress;
    protected GestureDetector mDetectorTop;
    protected GestureDetector mDetectorMiddle;
    protected GestureDetector mDetectorBottom;
    protected View.OnTouchListener touchListenerTop;
    protected View.OnTouchListener touchListenerMiddle;
    protected View.OnTouchListener touchListenerBottom;
    protected KlineEntity mKlineEntity;
    protected int mViewWidth;
    protected int mLeftIndex;
    protected int mBaseIndex;
    protected int mLastIndex;
    private float startX;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mViewDataBinding = DataBindingUtil.inflate(inflater, mLayoutId, container, false);
        mTopChartViewBase = mViewDataBinding.getRoot().findViewById(R.id.chart);
        mMiddleChartViewBase = mViewDataBinding.getRoot().findViewById(R.id.middleChart);
        mBottomChartViewBase = mViewDataBinding.getRoot().findViewById(R.id.bottomChart);
        initData();
        initChart();
        mView = mViewDataBinding.getRoot();
        return mView;
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
            UserEntity userEntity = dataManager.getTradeBean().getUsers().get(dataManager.USER_ID);
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
        EventBus.getDefault().register(this);
        MainActivity mainActivity = (MainActivity) getActivity();
        MainActivityPresenter mainActivityPresenter = mainActivity.getmMainActivityPresenter();
        mFutureInfoFragment = (FutureInfoFragment) mainActivityPresenter.getmViewPagerFragmentAdapter().getItem(2);

        mOIColor = ContextCompat.getColor(getActivity(), R.color.text_white);
        mDiffColor = ContextCompat.getColor(getActivity(), R.color.text_white);
        mDeaColor = ContextCompat.getColor(getActivity(), R.color.kline_ma2);
        mHighlightColor = ContextCompat.getColor(getActivity(), R.color.text_white);
        mIncreasingColor = ContextCompat.getColor(getActivity(), R.color.kline_red);
        mDecreasingColor = ContextCompat.getColor(getActivity(), R.color.kline_green);
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
        yValue = "";
        mIsLongPress = false;
        mViewWidth = VIEW_WIDTH;
        startX = 0.0f;

        mS = 12;
        mL = 26;
        mN = 9;
        mEMAs = new HashMap<>();
        mEMAl = new HashMap<>();
        mDEA = new HashMap<>();

        mIsPosition = (boolean) SPUtils.get(BaseApplication.getContext(), CONFIG_POSITION_LINE, true);
        mIsPending = (boolean) SPUtils.get(BaseApplication.getContext(), CONFIG_ORDER_LINE, true);
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
        mTopChartViewBase.setViewPortOffsets(0, 1, 0, 1);
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
        mMiddleChartViewBase.setViewPortOffsets(0, 0, 0, 1);
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
        mBottomChartViewBase.setViewPortOffsets(0, 0, 0, 40);
        mBottomChartViewBase.setDoubleTapToZoomEnabled(false);
        mBottomChartViewBase.setHighlightPerTapEnabled(false);
        mBottomChartViewBase.setHighlightPerDragEnabled(false);

        mDetectorTop = new GestureDetector(this.getContext(), new GestureDetector.SimpleOnGestureListener() {
            @Override
            public void onLongPress(MotionEvent e) {
                super.onLongPress(e);
                resizeMarker();
                mIsLongPress = true;
                Highlight h = mTopChartViewBase.getHighlightByTouchPoint(e.getX(), e.getY());
                if (h != null) {
                    h.setDraw(e.getX(), e.getY());
                    mTopChartViewBase.highlightValue(h, true);
                    mTopChartViewBase.disableScroll();
                }
            }

            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                mIsLongPress = false;
                mTopChartViewBase.highlightValue(null, true);
                mTopChartViewBase.enableScroll();
                return super.onSingleTapUp(e);
            }

            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                // TODO Auto-generated method stub
                // e1：第1个ACTION_DOWN MotionEvent
                // e2：最后一个ACTION_MOVE MotionEvent
                // velocityX：X轴上的移动速度（像素/秒）
                // velocityY：Y轴上的移动速度（像素/秒）
                // Y轴的坐标位移大于FLING_MIN_DISTANCE，且移动速度大于FLING_MIN_VELOCITY个像素/秒

                if (mIsLongPress)return false;

                String ins = "";
                if (Math.abs(e1.getX() - e2.getX()) > FLING_MAX_DISTANCE_X) return false;

                //向上
                if (e1.getY() - e2.getY() > FLING_MIN_DISTANCE_Y && Math.abs(velocityY) > FLING_MIN_VELOCITY) {
                    ins = getNextInstrumentId(true);
                }
                //向下
                if (e2.getY() - e1.getY() > FLING_MIN_DISTANCE_Y && Math.abs(velocityY) > FLING_MIN_VELOCITY) {
                    ins = getNextInstrumentId(false);
                }

                if (!ins.isEmpty()) {
                    SwitchInsEvent switchInsEvent = new SwitchInsEvent();
                    switchInsEvent.setInstrument_id(ins);
                    EventBus.getDefault().post(switchInsEvent);
                }
                return false;
            }

        });

        mDetectorMiddle = new GestureDetector(this.getContext(), new GestureDetector.SimpleOnGestureListener() {
            @Override
            public void onLongPress(MotionEvent e) {
                super.onLongPress(e);
                resizeMarker();
                mIsLongPress = true;
                Highlight h = mMiddleChartViewBase.getHighlightByTouchPoint(e.getX(), e.getY());
                if (h != null) {
                    h.setDraw(e.getX(), e.getY());
                    mMiddleChartViewBase.highlightValue(h, true);
                    mMiddleChartViewBase.disableScroll();
                }
            }

            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                mIsLongPress = false;
                mMiddleChartViewBase.highlightValue(null, true);
                mMiddleChartViewBase.enableScroll();
                return super.onSingleTapUp(e);
            }
        });

        mDetectorBottom = new GestureDetector(this.getContext(), new GestureDetector.SimpleOnGestureListener() {
            @Override
            public void onLongPress(MotionEvent e) {
                super.onLongPress(e);
                resizeMarker();
                mIsLongPress = true;
                Highlight h = mBottomChartViewBase.getHighlightByTouchPoint(e.getX(), e.getY());
                if (h != null) {
                    h.setDraw(e.getX(), e.getY());
                    mBottomChartViewBase.highlightValue(h, true);
                    mBottomChartViewBase.disableScroll();
                }
            }

            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                mIsLongPress = false;
                mBottomChartViewBase.highlightValue(null, true);
                mBottomChartViewBase.enableScroll();
                return super.onSingleTapUp(e);
            }
        });

        touchListenerTop = new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                mDetectorTop.onTouchEvent(event);
                if (!CURRENT_DAY_FRAGMENT.equals(mKlineType)) {
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            startX = event.getX();
                            break;
                        case MotionEvent.ACTION_UP:
                            if (!mIsLongPress && (event.getX() - startX) > mTopChartViewBase.getViewPortHandler().contentRight() / 7) {
                                float startIndex = mLeftIndex - mBaseIndex;
                                if (Math.abs(mTopChartViewBase.getLowestVisibleX() - startIndex) < 50) {
                                    if (xVals.size() >= mViewWidth) {
                                        mViewWidth = mViewWidth + 100;
                                        BaseApplication.getmMDWebSocket().sendSetChartKline(instrument_id, mViewWidth, mKlineType);
                                    }
                                }
                            }
                            break;
                        default:
                            break;
                    }
                }
                if (mIsLongPress && event.getAction() == MotionEvent.ACTION_MOVE) {
                    float topHeight = mTopChartViewBase.getViewPortHandler().contentHeight();
                    float y = event.getY();
                    if (y > topHeight) {
                        //滑到中部图
                        event.setLocation(event.getX(), y - topHeight);
                        touchListenerMiddle.onTouch(v, event);
                    } else {
                        //超过上部图顶部
                        if (y <= 0) y = y + topHeight;
                        Highlight h = mTopChartViewBase.getHighlightByTouchPoint(event.getX(), y);
                        if (h != null) {
                            h.setDraw(event.getX(), y);
                            mTopChartViewBase.highlightValue(h, true);
                            mTopChartViewBase.disableScroll();
                        }
                    }
                    return true;
                }
                return false;
            }
        };
        mTopChartViewBase.setOnTouchListener(touchListenerTop);

        touchListenerMiddle = new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                mDetectorMiddle.onTouchEvent(event);
                if (!CURRENT_DAY_FRAGMENT.equals(mKlineType)) {
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            startX = event.getX();
                            break;
                        case MotionEvent.ACTION_UP:
                            if (!mIsLongPress && (event.getX() - startX) > mMiddleChartViewBase.getViewPortHandler().contentRight() / 7) {
                                float startIndex = mLeftIndex - mBaseIndex;
                                if (Math.abs(mMiddleChartViewBase.getLowestVisibleX() - startIndex) < 50) {
                                    if (xVals.size() >= mViewWidth) {
                                        mViewWidth = mViewWidth + 100;
                                        BaseApplication.getmMDWebSocket().sendSetChartKline(instrument_id, mViewWidth, mKlineType);
                                    }
                                }
                            }
                            break;
                        default:
                            break;
                    }
                }
                if (mIsLongPress && event.getAction() == MotionEvent.ACTION_MOVE) {
                    float topHeight = mTopChartViewBase.getViewPortHandler().contentHeight();
                    float middleHeight = mMiddleChartViewBase.getViewPortHandler().contentHeight();
                    float y = event.getY();
                    if (y > middleHeight) {
                        //滑到下部图
                        event.setLocation(event.getX(), y - middleHeight);
                        touchListenerBottom.onTouch(v, event);
                    } else if (y <= 0) {
                        //超过中部图顶部
                        event.setLocation(event.getX(), y + topHeight);
                        touchListenerTop.onTouch(v, event);
                    } else {
                        Highlight h = mMiddleChartViewBase.getHighlightByTouchPoint(event.getX(), y);
                        if (h != null) {
                            h.setDraw(event.getX(), y);
                            mMiddleChartViewBase.highlightValue(h, true);
                            mMiddleChartViewBase.disableScroll();
                        }
                    }
                    return true;
                }
                return false;
            }
        };
        mMiddleChartViewBase.setOnTouchListener(touchListenerMiddle);

        touchListenerBottom = new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                mDetectorBottom.onTouchEvent(event);
                if (!CURRENT_DAY_FRAGMENT.equals(mKlineType)) {
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            startX = event.getX();
                            break;
                        case MotionEvent.ACTION_UP:
                            if (!mIsLongPress && (event.getX() - startX) > mBottomChartViewBase.getViewPortHandler().contentRight() / 7) {
                                float startIndex = mLeftIndex - mBaseIndex;
                                if (Math.abs(mBottomChartViewBase.getLowestVisibleX() - startIndex) < 50) {
                                    if (xVals.size() >= mViewWidth) {
                                        mViewWidth = mViewWidth + 100;
                                        BaseApplication.getmMDWebSocket().sendSetChartKline(instrument_id, mViewWidth, mKlineType);
                                    }
                                }
                            }
                            break;
                        default:
                            break;
                    }
                }
                if (mIsLongPress && event.getAction() == MotionEvent.ACTION_MOVE) {
                    float bottomHeight = mBottomChartViewBase.getViewPortHandler().contentHeight();
                    float middleHeight = mMiddleChartViewBase.getViewPortHandler().contentHeight();
                    float y = event.getY();
                    if (y <= 0) {
                        //超过下部图顶部
                        event.setLocation(event.getX(), y + middleHeight);
                        touchListenerMiddle.onTouch(v, event);
                    } else {
                        if (y > bottomHeight) y = y - bottomHeight;
                        Highlight h = mBottomChartViewBase.getHighlightByTouchPoint(event.getX(), y);
                        if (h != null) {
                            h.setDraw(event.getX(), y);
                            mBottomChartViewBase.highlightValue(h, true);
                            mBottomChartViewBase.disableScroll();
                        }
                    }
                    return true;
                }
                return false;
            }
        };
        mBottomChartViewBase.setOnTouchListener(touchListenerBottom);

        mTopChartViewBase.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, Highlight h) {
                Transformer transformer = mTopChartViewBase.getTransformer(YAxis.AxisDependency.LEFT);
                float yMaxValue = mTopChartViewBase.getYChartMax();
                float yMinValue = mTopChartViewBase.getYChartMin();
                float xValue = h.getX();
                float yMin = (float) transformer.getPixelForValues(xValue, yMaxValue).y;
                float yMax = (float) transformer.getPixelForValues(xValue, yMinValue).y;
                float touchY = h.getDrawY();//手指接触点在srcChart上的Y坐标，即手势监听器中保存数据
                float yData = (yMax - touchY) / (yMax - yMin) * (yMaxValue - yMinValue) + yMinValue;
                yValue = LatestFileManager.saveScaleByPtick(yData + "", instrument_id);
                float y = touchY - mTopChartViewBase.getHeight();
                Highlight hl = mMiddleChartViewBase.getHighlightByTouchPoint(h.getXPx(), h.getYPx());
                if (hl != null) hl.setDraw(h.getX(), y);
                mMiddleChartViewBase.highlightValue(hl);
                Highlight hlBottom = mBottomChartViewBase.getHighlightByTouchPoint(h.getXPx(), h.getYPx());
                if (hlBottom != null) hlBottom.setDraw(h.getX(), y);
                mBottomChartViewBase.highlightValue(hlBottom);
            }

            @Override
            public void onNothingSelected() {
                refreshChartLegend(mLastIndex - mBaseIndex);
                mMiddleChartViewBase.highlightValue(null);
                mBottomChartViewBase.highlightValue(null);
            }
        });

        mMiddleChartViewBase.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, Highlight h) {
                Transformer transformer = mMiddleChartViewBase.getTransformer(YAxis.AxisDependency.LEFT);
                float yMaxValue = mMiddleChartViewBase.getYChartMax();
                float xValue = h.getX();
                float yMin = (float) transformer.getPixelForValues(xValue, yMaxValue).y;
                float yMax = (float) transformer.getPixelForValues(xValue, 0).y;
                float touchY = h.getDrawY();//手指接触点在srcChart上的Y坐标，即手势监听器中保存数据
                int yData = (int) ((yMax - touchY) / (yMax - yMin) * yMaxValue);
                yValue = yData + "";
                float y = touchY + mTopChartViewBase.getHeight();
                Highlight hl = mTopChartViewBase.getHighlightByTouchPoint(h.getXPx(), h.getYPx());
                if (hl != null) hl.setDraw(h.getX(), y);
                mTopChartViewBase.highlightValue(hl);
                float yBottom = touchY - mMiddleChartViewBase.getHeight();
                Highlight hlBottom = mBottomChartViewBase.getHighlightByTouchPoint(h.getXPx(), h.getYPx());
                if (hlBottom != null) hlBottom.setDraw(h.getX(), yBottom);
                mBottomChartViewBase.highlightValue(hlBottom);
            }

            @Override
            public void onNothingSelected() {
                refreshChartLegend(mLastIndex - mBaseIndex);
                mTopChartViewBase.highlightValue(null);
                mBottomChartViewBase.highlightValue(null);
            }
        });

        mBottomChartViewBase.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, Highlight h) {
                Transformer transformer = mBottomChartViewBase.getTransformer(YAxis.AxisDependency.LEFT);
                float yMaxValue = mBottomChartViewBase.getYChartMax();
                float yMinValue = mBottomChartViewBase.getYChartMin();
                float xValue = h.getX();
                float yMin = (float) transformer.getPixelForValues(xValue, yMaxValue).y;
                float yMax = (float) transformer.getPixelForValues(xValue, yMinValue).y;
                float touchY = h.getDrawY();//手指接触点在srcChart上的Y坐标，即手势监听器中保存数据
                float yData = (yMax - touchY) / (yMax - yMin) * (yMaxValue - yMinValue) + yMinValue;
                yValue = MathUtils.round(yData + "", 2);
                float y = touchY + mTopChartViewBase.getHeight() + mMiddleChartViewBase.getHeight();
                Highlight hl = mTopChartViewBase.getHighlightByTouchPoint(h.getXPx(), h.getYPx());
                if (hl != null) hl.setDraw(h.getX(), y);
                mTopChartViewBase.highlightValue(hl);
                Highlight hlMiddle = mMiddleChartViewBase.getHighlightByTouchPoint(h.getXPx(), h.getYPx());
                if (hlMiddle != null) hlMiddle.setDraw(h.getX(), y);
                mMiddleChartViewBase.highlightValue(hlMiddle);
            }

            @Override
            public void onNothingSelected() {
                refreshChartLegend(mLastIndex - mBaseIndex);
                mTopChartViewBase.highlightValue(null);
                mMiddleChartViewBase.highlightValue(null);
            }
        });

    }

    /**
     * date: 2019/7/30
     * description: 调整十字光标尺寸
     */
    private void resizeMarker() {
        int topHeight = (int) (mTopChartViewBase.getViewPortHandler().contentHeight());
        if (CURRENT_DAY_FRAGMENT.equals(mFragmentType))
            ((CurrentDayFragment.CurrentDayMarkerView) mTopChartViewBase.getMarker()).resize(topHeight);
        else ((KlineFragment.KlineMarkerView) mTopChartViewBase.getMarker()).resize(topHeight);
    }

    /**
     * date: 2019/7/3
     * author: chenli
     * description: 设置合约id
     */
    public void setInstrument_id(String ins) {
        instrument_id = ins;
        SearchEntity searchEntity = LatestFileManager.getSearchEntities().get(instrument_id);
        if (instrument_id.contains("KQ") && searchEntity != null)
            instrument_id_transaction = searchEntity.getUnderlying_symbol();
        else instrument_id_transaction = instrument_id;
    }

    @Override
    public void show() {
        if (mView == null) return;
        //控制图表显示
        if (sDataManager.IS_SHOW_VP_CONTENT) {
            mMiddleChartViewBase.setVisibility(View.GONE);
            mBottomChartViewBase.setVisibility(View.GONE);
        } else {
            mMiddleChartViewBase.setVisibility(View.VISIBLE);
            mBottomChartViewBase.setVisibility(View.VISIBLE);
        }
        initInsList();
    }


    @Override
    public void leave() {
    }

    @Override
    public void refreshMD() {
        if (mView == null) return;
        drawKline();
    }


    @Override
    public void refreshTD() {
        if (mView == null) return;
        UserEntity userEntity = sDataManager.getTradeBean().getUsers().get(sDataManager.USER_ID);
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
            UserEntity userEntity = sDataManager.getTradeBean().getUsers().get(sDataManager.USER_ID);
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
        UserEntity userEntity = sDataManager.getTradeBean().getUsers().get(sDataManager.USER_ID);
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
            mBottomChartViewBase.setVisibility(View.VISIBLE);
        } else {
            mMiddleChartViewBase.setVisibility(View.GONE);
            mBottomChartViewBase.setVisibility(View.GONE);
        }
    }

    /**
     * date: 2019/7/31
     * author: chenli
     * description: 刷新中部图标值
     */
    protected void refreshChartLegend(int index){
        Map<String, KlineEntity.DataEntity> dataEntities = mKlineEntity.getData();
        int middleIndex = index;
        if (!CURRENT_DAY_FRAGMENT.equals(mFragmentType)) middleIndex = middleIndex + mBaseIndex;
        KlineEntity.DataEntity dataEntity = dataEntities.get(String.valueOf(middleIndex));
        if (dataEntity != null){
            List<LegendEntry> legendEntriesMiddle = new ArrayList<>();
            legendEntriesMiddle.add(new LegendEntry("OI:"+dataEntity.getClose_oi(), Legend.LegendForm.NONE,
                    NaN, NaN, null, mOIColor));
            legendEntriesMiddle.add(new LegendEntry("VOL:"+dataEntity.getVolume(), Legend.LegendForm.NONE,
                    NaN, NaN, null, mIncreasingColor));
            Legend legendMiddle = mMiddleChartViewBase.getLegend();
            legendMiddle.setCustom(legendEntriesMiddle);
            legendMiddle.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
            legendMiddle.setHorizontalAlignment(Legend.LegendHorizontalAlignment.RIGHT);
        }

        Float emasF = mEMAs.get(index);
        Float emalF = mEMAl.get(index);
        if (emasF != null && emalF != null){
            float emas = mEMAs.get(index);
            float emal = mEMAl.get(index);
            String dif = MathUtils.round((emas - emal) + "", 2);
            String dea = MathUtils.round(mDEA.get(index) + "", 2);
            List<LegendEntry> legendEntriesBottom = new ArrayList<>();
            legendEntriesBottom.add(new LegendEntry("MACD(12,26,9)" , Legend.LegendForm.NONE,
                    NaN, NaN, null, mDeaColor));
            legendEntriesBottom.add(new LegendEntry("DIFF:"+dif, Legend.LegendForm.NONE,
                    NaN, NaN, null, mDiffColor));
            legendEntriesBottom.add(new LegendEntry("DEA:"+dea, Legend.LegendForm.NONE,
                    NaN, NaN, null, mDeaColor));
            Legend legendBottom = mBottomChartViewBase.getLegend();
            legendBottom.setCustom(legendEntriesBottom);
            legendBottom.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
            legendBottom.setHorizontalAlignment(Legend.LegendHorizontalAlignment.RIGHT);
        }

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

    /**
     * date: 7/9/17
     * author: chenli
     * description: 格式化左Y轴数据
     * version:
     * state: done
     */
    public class TopChartLeftYAxisValueFormatter extends MyValueFormatter {

        @Override
        public String getFormattedValue(float value) {
            return LatestFileManager.saveScaleByPtick(value + "", instrument_id);
        }
    }

    public class BottomChartLeftYAxisValueFormatter extends MyValueFormatter{
        @Override
        public String getFormattedValue(float value) {
            return MathUtils.round(value + "", 2);
        }
    }

}

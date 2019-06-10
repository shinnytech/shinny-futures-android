package com.shinnytech.futures.controller.fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.SparseArray;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.LegendEntry;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.CombinedData;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.Transformer;
import com.shinnytech.futures.R;
import com.shinnytech.futures.model.bean.eventbusbean.IdEvent;
import com.shinnytech.futures.model.bean.eventbusbean.SetUpEvent;
import com.shinnytech.futures.model.bean.futureinfobean.KlineEntity;
import com.shinnytech.futures.model.bean.futureinfobean.QuoteEntity;
import com.shinnytech.futures.model.bean.searchinfobean.SearchEntity;
import com.shinnytech.futures.model.engine.LatestFileManager;
import com.shinnytech.futures.service.WebSocketService;
import com.shinnytech.futures.utils.LogUtils;
import com.shinnytech.futures.utils.MathUtils;
import com.shinnytech.futures.view.custommpchart.mycomponent.MyMarkerView;
import com.shinnytech.futures.view.custommpchart.mycomponent.MyXAxis;
import com.shinnytech.futures.view.custommpchart.mycomponent.MyYAxis;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static com.shinnytech.futures.constants.CommonConstants.CURRENT_DAY;
import static com.shinnytech.futures.constants.CommonConstants.CURRENT_DAY_FRAGMENT;
import static java.lang.Float.NaN;

/**
 * date: 7/9/17
 * author: chenli
 * description: 分时图页
 * version:
 * state: basically done
 */
public class CurrentDayFragment extends BaseChartFragment {

    /**
     * date: 7/9/17
     * description: 分时图颜色
     */
    private int mColorOneMinuteChart;

    /**
     * date: 7/9/17
     * description: 均线颜色
     */
    private int mColorAverageChart;

    private SparseArray<String> mStringSparseArray = new SparseArray<>();
    private float mSumVolume = 0.0f;
    private Map<Integer, Float> mSumVolumeMap = new HashMap<>();
    private float mSumCV = 0.0f;
    private Map<Integer, Float> mSumCVMap = new HashMap<>();
    private int mTradingDayStartIndex = 0;
    private int mTradingDayEndIndex = 0;
    private int mLastIndex = 0;
    private float preSettlement = 1.0f;
    private KlineEntity mKlineEntity;
    private String yValue = "";
    private boolean mIsLongPress = false;
    private GestureDetector mDetectorTop;
    private GestureDetector mDetectorMiddle;
    private View.OnTouchListener touchListenerTop;
    private View.OnTouchListener touchListenerMiddle;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mLayoutId = R.layout.fragment_current_day;
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        QuoteEntity quoteEntity = sDataManager.getRtnData().getQuotes().get(instrument_id);
        try {
            if (quoteEntity != null)
                preSettlement = Float.parseFloat(quoteEntity.getPre_settlement());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    protected void initData() {
        super.initData();
        mColorOneMinuteChart = ContextCompat.getColor(getActivity(), R.color.text_white);
        mColorAverageChart = ContextCompat.getColor(getActivity(), R.color.kline_ma2);
        mSimpleDateFormat = new SimpleDateFormat("HH:mm", Locale.CHINA);
        mFragmentType = CURRENT_DAY_FRAGMENT;
        mKlineType = CURRENT_DAY;
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void initChart() {
        super.initChart();

        mTopChartViewBase.setScaleEnabled(false);
        CurrentDayMarkerView marker = new CurrentDayMarkerView(getActivity());
        marker.setChartView(mTopChartViewBase);
        mTopChartViewBase.setMarker(marker);

        MyXAxis xAxis = (MyXAxis) mTopChartViewBase.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawLabels(false);
        xAxis.setDrawGridLines(true);
        xAxis.setDrawAxisLine(false);
        xAxis.setGridColor(mColorGrid);

        MyYAxis leftAxis = (MyYAxis) mTopChartViewBase.getAxisLeft();
        leftAxis.setPosition(YAxis.YAxisLabelPosition.INSIDE_CHART);
        leftAxis.setDrawGridLines(true);
        leftAxis.setDrawAxisLine(false);
        leftAxis.setDrawLabels(true);
        leftAxis.setLabelCount(3, true);
        leftAxis.enableGridDashedLine(3, 6, 0);
        leftAxis.setGridColor(mColorGrid);
        leftAxis.setTextColor(mColorText);
        leftAxis.setValueFormatter(new MyLeftYAxisValueFormatter());

        MyYAxis rightAxis = (MyYAxis) mTopChartViewBase.getAxisRight();
        rightAxis.setPosition(YAxis.YAxisLabelPosition.INSIDE_CHART);
        rightAxis.setDrawGridLines(false);
        rightAxis.setDrawAxisLine(false);
        rightAxis.setLabelCount(3, true);
        rightAxis.setDrawLabels(true);
        rightAxis.setTextColor(mColorText);
        rightAxis.setValueFormatter(new MyYAxisValueFormatter());

        LegendEntry oneMinuteChartLegend = new LegendEntry("分时图", Legend.LegendForm.SQUARE, NaN, NaN, null, mColorOneMinuteChart);
        LegendEntry averageChartLegend = new LegendEntry("均线", Legend.LegendForm.SQUARE, NaN, NaN, null, mColorAverageChart);
        List<LegendEntry> legendEntries = new ArrayList<>();
        legendEntries.add(oneMinuteChartLegend);
        legendEntries.add(averageChartLegend);
        Legend legend = mTopChartViewBase.getLegend();
        legend.setCustom(legendEntries);
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.RIGHT);
        legend.setTextColor(Color.WHITE);

        mMiddleChartViewBase.setScaleEnabled(false);

        MyXAxis middleXAxis = (MyXAxis) mMiddleChartViewBase.getXAxis();
        middleXAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        middleXAxis.setDrawLabels(true);
        middleXAxis.setDrawGridLines(true);
        middleXAxis.setDrawAxisLine(true);
        middleXAxis.setGridColor(mColorGrid);
        middleXAxis.setAxisLineColor(mColorGrid);
        middleXAxis.setTextColor(mColorText);

        MyYAxis middleLeftAxis = (MyYAxis) mMiddleChartViewBase.getAxisLeft();
        middleLeftAxis.setPosition(YAxis.YAxisLabelPosition.INSIDE_CHART);
        middleLeftAxis.setDrawGridLines(true);
        middleLeftAxis.setDrawAxisLine(false);
        middleLeftAxis.setDrawLabels(true);
        middleLeftAxis.setLabelCount(4, true);
        middleLeftAxis.enableGridDashedLine(3, 6, 0);
        middleLeftAxis.setGridColor(mColorGrid);
        middleLeftAxis.setTextColor(mColorText);
        middleLeftAxis.setAxisMinimum(0);
        middleLeftAxis.setSpaceBottom(0);

        MyYAxis middleRightAxis = (MyYAxis) mMiddleChartViewBase.getAxisRight();
        middleRightAxis.setDrawLabels(false);
        middleRightAxis.setDrawAxisLine(false);
        middleRightAxis.setDrawGridLines(false);

        Legend middleLegend = mMiddleChartViewBase.getLegend();
        middleLegend.setEnabled(false);

        mDetectorTop = new GestureDetector(this.getContext(), new GestureDetector.SimpleOnGestureListener() {
            @Override
            public void onLongPress(MotionEvent e) {
                super.onLongPress(e);
                int height = (int) (mTopChartViewBase.getViewPortHandler().contentHeight());
                ((CurrentDayMarkerView) mTopChartViewBase.getMarker()).resize(height);
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
                    IdEvent idEvent = new IdEvent();
                    idEvent.setInstrument_id(ins);
                    EventBus.getDefault().post(idEvent);
                }
                return false;
            }
        });

        mDetectorMiddle = new GestureDetector(this.getContext(), new GestureDetector.SimpleOnGestureListener() {
            @Override
            public void onLongPress(MotionEvent e) {
                super.onLongPress(e);
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

        touchListenerTop = new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                mDetectorTop.onTouchEvent(event);
                if (mIsLongPress && event.getAction() == MotionEvent.ACTION_MOVE) {
                    if (event.getY() > mTopChartViewBase.getViewPortHandler().contentHeight()) {
                        touchListenerMiddle.onTouch(v, event);
                    } else {
                        float y = event.getY();
                        float offset = mTopChartViewBase.getViewPortHandler().contentHeight();
                        if (y < 0) y = y + offset;
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
                if (mIsLongPress && event.getAction() == MotionEvent.ACTION_MOVE) {
                    if (event.getY() < 0) {
                        touchListenerTop.onTouch(v, event);
                    } else {
                        float y = event.getY();
                        float offset = mTopChartViewBase.getViewPortHandler().contentHeight();
                        if (y > offset) y = y - offset;
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
            }

            @Override
            public void onNothingSelected() {
                mMiddleChartViewBase.highlightValue(null);
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
            }

            @Override
            public void onNothingSelected() {
                mTopChartViewBase.highlightValue(null);
            }
        });

    }

    /**
     * date: 7/9/17
     * author: chenli
     * description: 载入数据
     */
    @Override
    protected void drawKline() {
        try {
            //开始加载数据
            if (mTopChartViewBase.getData() != null && mTopChartViewBase.getData().getDataSetCount() > 0) {
                CombinedData topCombinedData = mTopChartViewBase.getCombinedData();
                LineData topLineData = topCombinedData.getLineData();

                CombinedData middleCombinedData = mMiddleChartViewBase.getCombinedData();
                LineData middleLineData = middleCombinedData.getLineData();
                BarData middleBarData = middleCombinedData.getBarData();

                String last_id = mKlineEntity.getLast_id();
                int last_index_t = Integer.parseInt(last_id);
                Map<String, KlineEntity.DataEntity> dataEntities = mKlineEntity.getData();

                if (last_index_t == mLastIndex) {
                    LogUtils.e("分时图刷新", false);
                    KlineEntity.DataEntity dataEntity = dataEntities.get(last_id);
                    if (dataEntity == null) return;
                    topLineData.removeEntry(last_index_t, 0);
                    topLineData.removeEntry(last_index_t, 1);
                    middleLineData.removeEntry(last_index_t, 0);
                    middleBarData.removeEntry(last_index_t, 0);
                    mSumVolume -= mSumVolumeMap.get(last_index_t);
                    mSumCV -= mSumCVMap.get(last_index_t);
                    List<Entry> entries = generateMultiDataEntry(last_index_t, dataEntity);
                    topLineData.addEntry(entries.get(0), 0);
                    topLineData.addEntry(entries.get(1), 1);
                    middleLineData.addEntry(entries.get(2), 0);
                    middleBarData.addEntry(entries.get(3), 0);
                } else if (last_index_t > mLastIndex) {
                    LogUtils.e("分时图加载多个数据", false);
                    for (int i = mLastIndex + 1; i <= last_index_t; i++) {
                        KlineEntity.DataEntity dataEntity = dataEntities.get(i + "");
                        if (dataEntity == null) continue;
                        generateXAxisLabel(i, dataEntity);
                        List<Entry> entries = generateMultiDataEntry(last_index_t, dataEntity);
                        topLineData.addEntry(entries.get(0), 0);
                        topLineData.addEntry(entries.get(1), 1);
                        middleLineData.addEntry(entries.get(2), 0);
                        middleBarData.addEntry(entries.get(3), 0);
                    }
                    mLastIndex = last_index_t;
                }
                refreshYAxisRange(topLineData.getDataSetByIndex(0));

                topCombinedData.notifyDataChanged();
                mTopChartViewBase.notifyDataSetChanged();
                mTopChartViewBase.setVisibleXRangeMinimum(mTradingDayEndIndex - mTradingDayStartIndex);
                mTopChartViewBase.getXAxis().setAxisMaximum(topCombinedData.getXMax() + 0.35f);
                mTopChartViewBase.getXAxis().setAxisMinimum(topCombinedData.getXMin() - 0.35f);
                ((MyXAxis) mTopChartViewBase.getXAxis()).setXLabels(mStringSparseArray);
                mTopChartViewBase.invalidate();

                middleCombinedData.notifyDataChanged();
                mMiddleChartViewBase.notifyDataSetChanged();
                mMiddleChartViewBase.setVisibleXRangeMinimum(mTradingDayEndIndex - mTradingDayStartIndex);
                mMiddleChartViewBase.getXAxis().setAxisMaximum(middleCombinedData.getXMax() + 0.35f);
                mMiddleChartViewBase.getXAxis().setAxisMinimum(middleCombinedData.getXMin() - 0.35f);
                ((MyXAxis) mMiddleChartViewBase.getXAxis()).setXLabels(mStringSparseArray);
                mMiddleChartViewBase.invalidate();
            } else {
                LogUtils.e("分时图初始化", true);
                Map<String, KlineEntity> klineEntities = sDataManager.getRtnData().getKlines().get(instrument_id);
                QuoteEntity quoteEntity = sDataManager.getRtnData().getQuotes().get(instrument_id);

                if (klineEntities == null) return;
                mKlineEntity = klineEntities.get(mKlineType);
                if (mKlineEntity == null) return;
                String last_id = mKlineEntity.getLast_id();
                Map<String, KlineEntity.DataEntity> dataEntities = mKlineEntity.getData();

                if (last_id == null || "-1".equals(last_id) || dataEntities.isEmpty()) return;
                int last_index_t = Integer.parseInt(last_id);
                if (!"-".equals(quoteEntity.getPre_settlement()))
                    preSettlement = Float.parseFloat(quoteEntity.getPre_settlement());
                String trading_day_start_id = mKlineEntity.getTrading_day_start_id();
                String trading_day_end_id = mKlineEntity.getTrading_day_end_id();
                if (trading_day_start_id == null || trading_day_end_id == null
                        || "-1".equals(trading_day_start_id) || "-1".equals(trading_day_end_id))
                    return;
                mTradingDayStartIndex = Integer.parseInt(trading_day_start_id);
                mTradingDayEndIndex = Integer.parseInt(trading_day_end_id);
                mLastIndex = last_index_t;

                ((MyYAxis) mTopChartViewBase.getAxisLeft()).setBaseValue(preSettlement);
                ((MyYAxis) mTopChartViewBase.getAxisRight()).setBaseValue(preSettlement);
                List<Entry> oneMinuteEntries = new ArrayList<>();
                List<Entry> averageEntries = new ArrayList<>();
                CombinedData topCombinedData = new CombinedData();
                List<Entry> oiEntries = new ArrayList<>();
                List<BarEntry> volumeEntries = new ArrayList<>();
                CombinedData middleCombinedData = new CombinedData();
                for (int index = mTradingDayStartIndex; index <= mLastIndex; index++) {
                    KlineEntity.DataEntity dataEntity = dataEntities.get(String.valueOf(index));
                    if (dataEntity == null) continue;
                    generateXAxisLabel(index, dataEntity);
                    List<Entry> entries = generateMultiDataEntry(index, dataEntity);
                    oneMinuteEntries.add(entries.get(0));
                    averageEntries.add(entries.get(1));
                    oiEntries.add(entries.get(2));
                    volumeEntries.add((BarEntry) entries.get(3));
                }

                LineDataSet oneMinuteDataSet = generateLineDataSet(oneMinuteEntries, mColorOneMinuteChart,
                        "oneMinuteChart", true, YAxis.AxisDependency.LEFT);
                LineDataSet averageDataSet = generateLineDataSet(averageEntries, mColorAverageChart,
                        "averageChart", false, YAxis.AxisDependency.LEFT);
                LineData lineData = new LineData(oneMinuteDataSet, averageDataSet);
                topCombinedData.setData(lineData);

                LineDataSet oiDataSet = generateLineDataSet(oiEntries, mColorOneMinuteChart,
                        "OI", false, YAxis.AxisDependency.RIGHT);
                BarDataSet volumeDataSet = generateBarDataSet(volumeEntries, mColorAverageChart,
                        "Volume", true);
                LineData middleLineData = new LineData(oiDataSet);
                BarData middleBarData = new BarData(volumeDataSet);
                middleBarData.setBarWidth(0.01f);
                middleCombinedData.setData(middleBarData);
                middleCombinedData.setData(middleLineData);

                mTopChartViewBase.setData(topCombinedData);//当前屏幕会显示所有的数据
                mTopChartViewBase.setVisibleXRangeMinimum(mTradingDayEndIndex - mTradingDayStartIndex);
                mTopChartViewBase.getXAxis().setAxisMaximum(topCombinedData.getXMax() + 0.35f);
                mTopChartViewBase.getXAxis().setAxisMinimum(topCombinedData.getXMin() - 0.35f);
                ((MyXAxis) mTopChartViewBase.getXAxis()).setXLabels(mStringSparseArray);
                mTopChartViewBase.invalidate();

                mMiddleChartViewBase.setData(middleCombinedData);//当前屏幕会显示所有的数据
                mMiddleChartViewBase.setVisibleXRangeMinimum(mTradingDayEndIndex - mTradingDayStartIndex);
                mMiddleChartViewBase.getXAxis().setAxisMaximum(middleCombinedData.getXMax() + 0.35f);
                mMiddleChartViewBase.getXAxis().setAxisMinimum(middleCombinedData.getXMin() - 0.35f);
                ((MyXAxis) mMiddleChartViewBase.getXAxis()).setXLabels(mStringSparseArray);
                mMiddleChartViewBase.invalidate();

            }
        } catch (Exception ex) {
            ByteArrayOutputStream error = new ByteArrayOutputStream();
            ex.printStackTrace(new PrintStream(error));
            String exception = error.toString();
            LogUtils.e(exception, true);
        }
    }

    @Override
    protected void clearKline() {
        preSettlement = 1;
        xVals.clear();
        mStringSparseArray.clear();
        removeOrderLimitLines();
        removePositionLimitLines();
        mTopChartViewBase.clear();
        mMiddleChartViewBase.clear();
        mSumVolume = 0.0f;
        mSumCV = 0.0f;
    }

    /**
     * date: 6/1/18
     * author: chenli
     * description: 产生x轴标志
     */
    private void generateXAxisLabel(int index, KlineEntity.DataEntity dataEntity) throws ParseException {
        long dateTime = Long.valueOf(dataEntity.getDatetime()) / 1000000;
        mCalendar.setTimeInMillis(dateTime);
        String time = mSimpleDateFormat.format(mCalendar.getTime());
        xVals.put(index, time);
        if (index == mTradingDayStartIndex) {
            mStringSparseArray.put(index, time);
        } else if (index == mTradingDayEndIndex) {
            mCalendar.setTimeInMillis(dateTime + 60000L);
            String timeLast = mSimpleDateFormat.format(mCalendar.getTime());
            mStringSparseArray.put(index, timeLast);
        } else {
            String timePreS = xVals.get(index - 1);
            long timeCur = mSimpleDateFormat.parse(time).getTime();
            long timePre = mSimpleDateFormat.parse(timePreS).getTime();
            if ((timeCur - timePre) != 60000L) mStringSparseArray.put(index, time);
        }
    }

    /**
     * date: 6/1/18
     * author: chenli
     * description: 分时图初始化时添加数据
     */
    private List<Entry> generateMultiDataEntry(int index, KlineEntity.DataEntity dataEntity) {
        List<Entry> entries = new ArrayList<>();
        String close = dataEntity.getClose();
        Float closeF = Float.valueOf(close);
        String volume = dataEntity.getVolume();
        Float volumeF = Float.valueOf(volume);
        String oi = dataEntity.getClose_oi();
        Float oiF = Float.valueOf(oi);
        Float cv = volumeF * closeF;
        mSumVolume += volumeF;
        mSumVolumeMap.put(index, volumeF);
        mSumCV += cv;
        mSumCVMap.put(index, cv);
        float average = mSumVolume != 0 ? mSumCV / mSumVolume : 0;
        entries.add(new Entry(index, closeF));
        entries.add(new Entry(index, average));
        entries.add(new Entry(index, oiF));
        entries.add(new BarEntry(index, volumeF));
        return entries;
    }

    /**
     * date: 6/1/18
     * author: chenli
     * description: 初始化时产生分时图数据集
     */
    private LineDataSet generateLineDataSet(List<Entry> entries, int color, String label, boolean isHighlight, YAxis.AxisDependency axisDependency) {
        LineDataSet set = new LineDataSet(entries, label);
        set.setColor(color);
        set.setLineWidth(0.7f);
        set.setDrawCircles(false);
        set.setDrawCircleHole(false);
        set.setDrawValues(false);
        set.setAxisDependency(axisDependency);
        if (isHighlight) {
            refreshYAxisRange(set);
            set.setHighlightLineWidth(0.7f);
            set.setHighLightColor(ContextCompat.getColor(getActivity(), R.color.text_white));
        } else {
            set.setHighlightEnabled(false);
        }
        return set;
    }

    /**
     * date: 6/1/18
     * author: chenli
     * description: 刷新y轴的显示范围，以preSettlement作为基准
     */
    private void refreshYAxisRange(ILineDataSet lineDataSet) {
        float lowDel = Math.abs(lineDataSet.getYMin() - preSettlement);
        float highDel = Math.abs(lineDataSet.getYMax() - preSettlement);
        if (lowDel > highDel) {
            mTopChartViewBase.getAxisLeft().setAxisMinimum(preSettlement - lowDel);
            mTopChartViewBase.getAxisRight().setAxisMinimum(preSettlement - lowDel);
            mTopChartViewBase.getAxisLeft().setAxisMaximum(preSettlement + lowDel);
            mTopChartViewBase.getAxisRight().setAxisMaximum(preSettlement + lowDel);
        } else {
            mTopChartViewBase.getAxisLeft().setAxisMinimum(preSettlement - highDel);
            mTopChartViewBase.getAxisRight().setAxisMinimum(preSettlement - highDel);
            mTopChartViewBase.getAxisLeft().setAxisMaximum(preSettlement + highDel);
            mTopChartViewBase.getAxisRight().setAxisMaximum(preSettlement + highDel);
        }
    }

    /**
     * date: 2019/2/21
     * author: chenli
     * description: 生成成交量数据集
     */
    private BarDataSet generateBarDataSet(List<BarEntry> entries, int color, String label, boolean isHighlight) {
        BarDataSet set = new BarDataSet(entries, label);
        set.setColor(color);
        set.setBarBorderColor(color);
        set.setBarBorderWidth(0);
        set.setDrawValues(false);
        set.setAxisDependency(YAxis.AxisDependency.LEFT);
        if (isHighlight) {
            set.setHighLightColor(Color.WHITE);
        } else {
            set.setHighlightEnabled(false);
        }
        return set;
    }

    /**
     * date: 7/9/17
     * author: chenli
     * description: 接收自选合约列表弹出框以及持仓页传过来的合约代码，以便更新K线图
     */
    @Subscribe
    public void onEvent(IdEvent data) {
        String instrument_id_new = data.getInstrument_id();
        SearchEntity searchEntity = LatestFileManager.getSearchEntities().get(instrument_id_new);
        if (instrument_id.equals(instrument_id_new)) return;
        instrument_id = instrument_id_new;

        clearKline();

        if (instrument_id.contains("KQ") && searchEntity != null)
            instrument_id_transaction = searchEntity.getUnderlying_symbol();
        else instrument_id_transaction = instrument_id;

        drawKline();

        if (mIsPosition) addPositionLimitLines();
        if (mIsPending) addOrderLimitLines();

        WebSocketService.sendSetChart(instrument_id);
    }

    /**
     * date: 7/9/17
     * author: chenli
     * description: 接收“设置”按钮最新的设置信息，以便更具用户要求显示持仓、挂单、均线
     */
    @Subscribe
    public void onEventMainThread(SetUpEvent data) {
        if (mIsPending != data.isPending()) {
            mIsPending = data.isPending();
            if (mIsPending) addOrderLimitLines();
            else removeOrderLimitLines();
        }

        if (mIsPosition != data.isPosition()) {
            mIsPosition = data.isPosition();
            if (mIsPosition) addPositionLimitLines();
            else removePositionLimitLines();
        }

        mTopChartViewBase.invalidate();
    }

    /**
     * date: 7/9/17
     * author: chenli
     * description: 格式化右Y轴数据
     * version:
     * state: done
     */
    public class MyYAxisValueFormatter implements IAxisValueFormatter {

        @Override
        public String getFormattedValue(float value, AxisBase axis) {
            // "value" represents the position of the label on the axis (x or y)
            DecimalFormat mFormat = new DecimalFormat("#0.00%");
            return mFormat.format((value - preSettlement) / preSettlement);
        }
    }

    /**
     * date: 7/9/17
     * author: chenli
     * description: 格式化左Y轴数据
     * version:
     * state: done
     */
    public class MyLeftYAxisValueFormatter implements IAxisValueFormatter {

        @Override
        public String getFormattedValue(float value, AxisBase axis) {
            // "value" represents the position of the label on the axis (x or y)
            return LatestFileManager.saveScaleByPtick(value + "", instrument_id);
        }
    }

    /**
     * date: 7/9/17
     * author: chenli
     * description: 点击分时图弹出实时信息
     * version:
     * state: done
     */
    public class CurrentDayMarkerView extends MyMarkerView {

        private TextView yValue;
        private TextView xValue;
        private TextView price;
        private TextView average;
        private TextView change;
        private TextView changePercent;
        private TextView volume;
        private TextView volumeDelta;
        private TextView closeOi;
        private TextView deltaOi;
        private String markViewState;

        /**
         * Constructor. Sets up the MarkerView with a custom layout resource.
         *
         * @param context
         */
        public CurrentDayMarkerView(Context context) {
            super(context, R.layout.view_marker_current_day);
            yValue = findViewById(R.id.y_value);
            xValue = findViewById(R.id.x_value);
            price = findViewById(R.id.price);
            average = findViewById(R.id.average);
            change = findViewById(R.id.change);
            changePercent = findViewById(R.id.change_percent);
            volume = findViewById(R.id.volume);
            volumeDelta = findViewById(R.id.volume_delta);
            closeOi = findViewById(R.id.close_oi);
            deltaOi = findViewById(R.id.delta_oi);
            markViewState = "right";
        }

        @Override
        public void refreshContent(Entry e, Highlight highlight) {
            int x = (int) e.getX();
            int index = x - 1;
            Map<String, KlineEntity.DataEntity> dataEntities = mKlineEntity.getData();
            KlineEntity.DataEntity dataEntity = dataEntities.get(String.valueOf(x));
            KlineEntity.DataEntity dataEntityPre = dataEntities.get(String.valueOf(index >= mTradingDayStartIndex ? index : mTradingDayStartIndex));
            if (dataEntity != null && dataEntityPre != null) {
                String xValue = xVals.get(x);
                String price = LatestFileManager.saveScaleByPtick(dataEntity.getClose(), instrument_id);
                Entry averageEntry = mTopChartViewBase.getLineData().getDataSets().get(1).getEntryForXValue(e.getX(), e.getY());
                String average;
                if (averageEntry != null)
                    average = LatestFileManager.saveScaleByPtick(String.valueOf(averageEntry.getY()), instrument_id);
                else
                    average = LatestFileManager.saveScaleByPtick(String.valueOf(e.getY()), instrument_id);
                String change = LatestFileManager.saveScaleByPtick(MathUtils.subtract(dataEntity.getClose(), dataEntityPre.getClose()), instrument_id);
                String changePercent = MathUtils.round(MathUtils.multiply(MathUtils.divide(change, dataEntityPre.getClose()), "100"), 2) + "%";
                String volume = dataEntity.getVolume();
                String volumeDelta = MathUtils.subtract(volume, (dataEntityPre.getVolume()));
                String closeOi = dataEntity.getClose_oi();
                String deltaOi = MathUtils.subtract(closeOi, (dataEntityPre.getClose_oi()));
                this.yValue.setText(CurrentDayFragment.this.yValue);
                this.xValue.setText(xValue);
                this.price.setText(price);
                this.average.setText(average);
                this.change.setText(change);
                this.changePercent.setText(changePercent);
                this.volume.setText(volume);
                this.volumeDelta.setText(volumeDelta);
                this.closeOi.setText(closeOi);
                this.deltaOi.setText(deltaOi);
                try {
                    int volume_delta = Integer.parseInt(volumeDelta);
                    if (volume_delta < 0)
                        this.volumeDelta.setTextColor(ContextCompat.getColor(getActivity(), R.color.text_green));
                    else
                        this.volumeDelta.setTextColor(ContextCompat.getColor(getActivity(), R.color.text_red));

                    int oi_delta = Integer.parseInt(deltaOi);
                    if (oi_delta < 0)
                        this.deltaOi.setTextColor(ContextCompat.getColor(getActivity(), R.color.text_green));
                    else
                        this.deltaOi.setTextColor(ContextCompat.getColor(getActivity(), R.color.text_red));
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
            super.refreshContent(e, highlight);
        }

        @Override
        public void draw(Canvas canvas, float posX, float posY) {
            // translate to the correct position and draw
            float deadlineRight = mTopChartViewBase.getViewPortHandler().contentRight() - getWidth();
            float deadlineLeft = mTopChartViewBase.getViewPortHandler().contentLeft() + getWidth();
            float dy = mTopChartViewBase.getViewPortHandler().contentTop();
            float dx = mTopChartViewBase.getViewPortHandler().contentLeft();
            if (posX <= deadlineLeft) {
                canvas.translate(deadlineRight, dy);
                markViewState = "right";
            } else if (posX >= deadlineRight) {
                canvas.translate(dx, dy);
                markViewState = "left";
            } else {
                if (markViewState.equals("right"))
                    canvas.translate(deadlineRight, dy);
                if (markViewState.equals("left"))
                    canvas.translate(dx, dy);
            }
            draw(canvas);
        }
    }

}

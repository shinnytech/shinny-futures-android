package com.shinnytech.futures.view.fragment;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.SparseArray;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.mikephil.charting.charts.CombinedChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.LegendEntry;
import com.github.mikephil.charting.components.MarkerView;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.CandleData;
import com.github.mikephil.charting.data.CandleDataSet;
import com.github.mikephil.charting.data.CandleEntry;
import com.github.mikephil.charting.data.CombinedData;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.formatter.IValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.jobs.MoveViewJob;
import com.github.mikephil.charting.listener.ChartTouchListener;
import com.github.mikephil.charting.listener.OnChartGestureListener;
import com.github.mikephil.charting.listener.OnDrawLineChartTouchListener;
import com.github.mikephil.charting.utils.ViewPortHandler;
import com.shinnytech.futures.R;
import com.shinnytech.futures.application.BaseApplicationLike;
import com.shinnytech.futures.model.bean.eventbusbean.IdEvent;
import com.shinnytech.futures.model.bean.eventbusbean.SetUpEvent;
import com.shinnytech.futures.model.bean.futureinfobean.ChartEntity;
import com.shinnytech.futures.model.bean.futureinfobean.KlineEntity;
import com.shinnytech.futures.model.bean.futureinfobean.QuoteEntity;
import com.shinnytech.futures.model.engine.LatestFileManager;
import com.shinnytech.futures.utils.LogUtils;
import com.shinnytech.futures.utils.MathUtils;
import com.shinnytech.futures.utils.SPUtils;
import com.shinnytech.futures.view.activity.FutureInfoActivity;
import com.shinnytech.futures.view.custommpchart.mycomponent.MyXAxis;
import com.shinnytech.futures.view.custommpchart.mycomponent.MyYAxis;

import org.greenrobot.eventbus.Subscribe;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static com.shinnytech.futures.constants.CommonConstants.KLINE_DAY;
import static com.shinnytech.futures.constants.CommonConstants.KLINE_HOUR;
import static com.shinnytech.futures.constants.CommonConstants.KLINE_MINUTE;
import static com.shinnytech.futures.constants.CommonConstants.VIEW_WIDTH;
import static java.lang.Float.NaN;

/**
 * date: 7/9/17
 * author: chenli
 * description: K线图页面，利用MPAndroidChart库生成日线、小时线、5分钟线
 * version:
 * state: basically done
 */
public class KlineFragment extends BaseChartFragment {
    /**
     * date: 7/9/17
     * description: X轴的显示格式，“年/月”--“2017/07”、“月/日”--“07/09”
     */
    private static final String FRAGMENT_XVALS_FORMAT = "format";

    /**
     * date: 7/9/17
     * description: K线图类型--日线、小时线、5分钟线
     */
    private static final String FRAGMENT_KLINE_TYPE = "type";
    private static float mScaleX = 0.0f;
    /**
     * date: 7/9/17
     * description: 均线数据
     */
    private LineData mLineData;
    /**
     * date: 7/9/17
     * description: 均线颜色
     */
    private int mColorMa5;
    private int mColorMa10;
    private int mColorMa20;

    private int mViewWidth;
    private int mLeftIndex;
    private int mRightIndex;
    private boolean mIsDrag;
    private Highlight mLastHighlighted;
    private int mLastIndex;


    /**
     * date: 7/9/17
     * author: chenli
     * description: 创建页面实例
     */
    public static KlineFragment newInstance(String xValsFormat, String klineType) {
        KlineFragment fragment = new KlineFragment();
        Bundle bundle = new Bundle();
        bundle.putString(FRAGMENT_XVALS_FORMAT, xValsFormat);
        bundle.putString(FRAGMENT_KLINE_TYPE, klineType);
        fragment.setArguments(bundle);
        return fragment;
    }

    /**
     * date: 7/9/17
     * author: chenli
     * description: fragment根据实例创建时的参数显示不同的K线类型
     */
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String mXValsFormat = getArguments().getString(FRAGMENT_XVALS_FORMAT);
        mKlineType = getArguments().getString(FRAGMENT_KLINE_TYPE);
        switch (mKlineType) {
            case KLINE_DAY:
                mButtonId = R.id.rb_day_up;
                break;
            case KLINE_HOUR:
                mButtonId = R.id.rb_hour_up;
                break;
            case KLINE_MINUTE:
                mButtonId = R.id.rb_minute_up;
                break;
            default:
                break;
        }
        if (mXValsFormat != null)
            mSimpleDateFormat = new SimpleDateFormat(mXValsFormat, Locale.CHINA);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mLayoutId = R.layout.fragment_kline;
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    /**
     * date: 7/9/17
     * author: chenli
     * description: 获取传进futureInfoActivity页的合约代码，以及初始化持仓线、挂单线、均线
     */
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    protected void initData() {
        super.initData();
        mColorMa5 = ContextCompat.getColor(getActivity(), R.color.kline_ma5);
        mColorMa10 = ContextCompat.getColor(getActivity(), R.color.kline_ma10);
        mColorMa20 = ContextCompat.getColor(getActivity(), R.color.kline_ma20);
        mViewWidth = VIEW_WIDTH;
        if (mScaleX == 0.0f)
            mScaleX = (float) SPUtils.get(BaseApplicationLike.getContext(), "mScaleX", 1.0f);
        mIsDrag = true;
    }

    @Override
    protected void initChart() {
        super.initChart();
        mChart.setScaleYEnabled(false);
        mChart.setDrawOrder(
                new CombinedChart.DrawOrder[]{CombinedChart.DrawOrder.CANDLE, CombinedChart.DrawOrder.LINE});
        final MyMarkerView marker = new MyMarkerView(getActivity());
        marker.setChartView(mChart);
        mChart.setMarker(marker);
        mChart.setDrawBorders(true);
        mChart.setBorderColor(mColorGrid);
        mChart.setHighlightPerDragEnabled(false);

        MyXAxis bottomAxis = (MyXAxis) mChart.getXAxis();
        bottomAxis.setValueFormatter(new KlineFragment.MyXAxisValueFormatter(xVals));
        bottomAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        bottomAxis.setDrawGridLines(true);
        bottomAxis.setDrawAxisLine(true);
        bottomAxis.enableGridDashedLine(3, 6, 0);
        bottomAxis.setAxisLineColor(mColorGrid);
        bottomAxis.setGridColor(mColorGrid);
        bottomAxis.setTextColor(mColorText);
        bottomAxis.setGranularityEnabled(true);
        bottomAxis.setGranularity(1);

        MyYAxis leftAxis = (MyYAxis) mChart.getAxisLeft();
        leftAxis.setPosition(YAxis.YAxisLabelPosition.INSIDE_CHART);
        leftAxis.setDrawGridLines(true);
        leftAxis.setDrawAxisLine(false);
        leftAxis.enableGridDashedLine(3, 6, 0);
        leftAxis.setGridColor(mColorGrid);
        leftAxis.setTextColor(mColorText);
        leftAxis.setLabelCount(6, true);
        leftAxis.setValueFormatter(new MyYAxisValueFormatter());

        MyYAxis rightAxis = (MyYAxis) mChart.getAxisRight();
        rightAxis.setEnabled(false);

        LegendEntry MA5 = new LegendEntry("MA5", Legend.LegendForm.SQUARE, NaN, NaN, null, mColorMa5);
        LegendEntry MA10 = new LegendEntry("MA10", Legend.LegendForm.SQUARE, NaN, NaN, null, mColorMa10);
        LegendEntry MA20 = new LegendEntry("MA20", Legend.LegendForm.SQUARE, NaN, NaN, null, mColorMa20);
        List<LegendEntry> legendEntries = new ArrayList<>();
        legendEntries.add(MA5);
        legendEntries.add(MA10);
        legendEntries.add(MA20);
        Legend legend = mChart.getLegend();
        legend.setCustom(legendEntries);
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.RIGHT);
        legend.setTextColor(Color.WHITE);

        mChart.setOnTouchListener(new OnDrawLineChartTouchListener() {
            private float startX = 0.0f;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        startX = event.getX();
                        break;
                    case MotionEvent.ACTION_MOVE:
                        if (!mIsDrag) {
                            performHighlightDrag(event);
                        }
                        break;
                    case MotionEvent.ACTION_UP:
                        if ((event.getX() - startX) > mChart.getViewPortHandler().contentRight() / 7) {
                            //因为mChart.getXAxis().setAxisMinimum(combinedData.getXMin() - 0.5f);
                            if ((int) (mChart.getLowestVisibleX() + 0.5f) == mLeftIndex) {
                                if (xVals.size() >= mViewWidth) {
                                    mViewWidth = mViewWidth + 100;
                                    if (BaseApplicationLike.getWebSocketService() != null)
                                        switch (mKlineType) {
                                            case KLINE_DAY:
                                                BaseApplicationLike.getWebSocketService().sendSetChartDay(instrument_id, mViewWidth);
                                                break;
                                            case KLINE_HOUR:
                                                BaseApplicationLike.getWebSocketService().sendSetChartHour(instrument_id, mViewWidth);
                                                break;
                                            case KLINE_MINUTE:
                                                BaseApplicationLike.getWebSocketService().sendSetChartMin(instrument_id, mViewWidth);
                                                break;
                                            default:
                                                break;
                                        }
                                }
                            }
                        }
                        break;
                    default:
                        break;
                }

                return super.onTouch(v, event);
            }
        });


        mChart.setOnChartGestureListener(new OnChartGestureListener() {

            @Override
            public void onChartGestureStart(MotionEvent me, ChartTouchListener.ChartGesture lastPerformedGesture) {
                mIsUpdate = false;
            }

            @Override
            public void onChartGestureEnd(MotionEvent me, ChartTouchListener.ChartGesture lastPerformedGesture) {
                mIsUpdate = true;
            }

            @Override
            public void onChartLongPressed(MotionEvent me) {

            }

            @Override
            public void onChartDoubleTapped(MotionEvent me) {
                mChart.dispatchTouchEvent(me);
                mChart.setDragEnabled(false);
                mIsDrag = false;
                float tappedX = me.getX();
                float tappedY = me.getY();
                Highlight highlight = mChart.getHighlightByTouchPoint(tappedX, tappedY);
                performHighlight(highlight);
            }

            @Override
            public void onChartSingleTapped(MotionEvent me) {
                mChart.highlightValues(null);
                mChart.setDragEnabled(true);
                mIsDrag = true;
            }

            @Override
            public void onChartFling(MotionEvent me1, MotionEvent me2, float velocityX, float velocityY) {
            }

            @Override
            public void onChartScale(MotionEvent me, float scaleX, float scaleY) {
                mScaleX = mChart.getViewPortHandler().getScaleX();
            }

            @Override
            public void onChartTranslate(MotionEvent me, float dX, float dY) {
            }
        });

    }

    /**
     * Perform a highlight operation.
     */
    protected void performHighlight(Highlight h) {

        if (h == null || h.equalTo(mLastHighlighted)) {
            mChart.highlightValue(null, true);
            mLastHighlighted = null;
        } else {
            mChart.highlightValue(h, true);
            mLastHighlighted = h;
        }
    }


    /**
     * Highlights upon dragging, generates callbacks for the selection-listener.
     */
    private void performHighlightDrag(MotionEvent e) {
        float x = e.getX();

        if (x > mChart.getViewPortHandler().contentLeft() && x < mChart.getViewPortHandler().contentRight()) {

            Highlight h = mChart.getHighlightByTouchPoint(x, e.getY());

            if (h != null && !h.equalTo(mLastHighlighted)) {
                mLastHighlighted = h;
                mChart.highlightValue(h, true);
            }
        }
    }

    /**
     * date: 7/9/17
     * author: chenli
     * description: 载入K线数据
     */
    @Override
    protected void refreshMarketing() {
        if (((FutureInfoActivity) getActivity()).getTabsUp().getCheckedRadioButtonId() == mButtonId) {
            try {
                Map<String, KlineEntity> klineEntities = sDataManager.getRtnData().getKlines().get(instrument_id);
                ChartEntity chartEntity = sDataManager.getRtnData().getCharts().get(mKlineType);
                QuoteEntity quoteEntity = sDataManager.getRtnData().getQuotes().get(instrument_id);
                if (klineEntities == null || chartEntity == null) return;
                KlineEntity klineEntity = klineEntities.get(mKlineType);
                String ins_list = chartEntity.getState().get("ins_list");
                if (klineEntity == null || ins_list == null || quoteEntity == null) return;
                preSettlement = Float.parseFloat(quoteEntity.getPre_settlement());
                String last_id = klineEntity.getLast_id();
                mDataEntities = klineEntity.getData();
                if (last_id == null || "-1".equals(last_id) ||
                        mDataEntities.isEmpty() || !instrument_id.equals(ins_list) || preSettlement == 1)
                    return;
                mLastIndex = Integer.parseInt(last_id);
                //开始加载数据
                if (mChart.getData() != null && mChart.getData().getDataSetCount() > 0) {
                    CandleData candleData = mChart.getCandleData();
                    CombinedData combinedData = mChart.getCombinedData();
                    int itemCount = mDataEntities.size();
                    int entryCount = candleData.getDataSetByIndex(0).getEntryCount();

                    if (entryCount == itemCount) {
                        KlineEntity.DataEntity dataEntity = mDataEntities.get(last_id);
                        if (dataEntity == null) return;
                        LogUtils.e("单个柱子刷新", false);
                        candleData.removeEntry(mLastIndex, 0);
                        mLineData.removeEntry(mLastIndex, 0);
                        mLineData.removeEntry(mLastIndex, 1);
                        mLineData.removeEntry(mLastIndex, 2);
                        generateCandleAndLineDataEntry(candleData, mLeftIndex, mLastIndex);
                    } else {
                        String left_id = chartEntity.getLeft_id();
                        String right_id = chartEntity.getRight_id();
                        if (left_id == null && right_id == null) return;
                        int left_index = Integer.parseInt(left_id);
                        if (left_index < 0) left_index = 0;
                        int right_index = Integer.parseInt(right_id);
                        if (left_index < mLeftIndex) {
                            LogUtils.e("向前添加柱子", false);
                            for (int i = this.mLeftIndex - 1; i >= left_index; i--) {
                                generateCandleAndLineDataEntry(candleData, left_index, i);
                            }
                            this.mLeftIndex = left_index;
                        } else if (right_index > mRightIndex) {
                            LogUtils.e("向后添加柱子", false);
                            for (int i = this.mRightIndex + 1; i <= right_index; i++) {
                                generateCandleAndLineDataEntry(candleData, mLeftIndex, i);
                            }
                            this.mRightIndex = right_index;
                        }

                    }
                    combinedData.notifyDataChanged();
                    mChart.notifyDataSetChanged();
                    mChart.getXAxis().setAxisMaximum(combinedData.getXMax() + 0.5f);
                    mChart.getXAxis().setAxisMinimum(combinedData.getXMin() - 0.5f);
                    mChart.invalidate();
                } else {
                    LogUtils.e("K线图初始化", true);
                    String left_id = chartEntity.getLeft_id();
                    String right_id = chartEntity.getRight_id();
                    if (left_id == null || right_id == null) return;
                    mLeftIndex = Integer.parseInt(left_id);
                    if (mLeftIndex < 0) mLeftIndex = 0;
                    mRightIndex = Integer.parseInt(right_id);
                    List<Entry> ma5Entries = new ArrayList<>();
                    List<Entry> ma10Entries = new ArrayList<>();
                    List<Entry> ma20Entries = new ArrayList<>();
                    List<CandleEntry> candleEntries = new ArrayList<>();
                    for (int i = mLeftIndex; i <= mLastIndex; i++) {
                        generateCandleAndLineDataEntry(ma5Entries, ma10Entries, ma20Entries, candleEntries, i);
                    }

                    CombinedData combinedData = new CombinedData();
                    CandleData candleData = generateCandleData(candleEntries);
                    combinedData.setData(candleData);

                    if (ma5Entries.isEmpty()) {
                        mLineData = new LineData();
                    } else if (ma10Entries.isEmpty()) {
                        mLineData = generateMultiLineData(
                                generateLineDataSet(ma5Entries, mColorMa5, "ma5"));
                    } else if (ma20Entries.isEmpty()) {
                        mLineData = generateMultiLineData(
                                generateLineDataSet(ma5Entries, mColorMa5, "ma5"),
                                generateLineDataSet(ma10Entries, mColorMa10, "ma10"));
                    } else {
                        mLineData = generateMultiLineData(
                                generateLineDataSet(ma5Entries, mColorMa5, "ma5"),
                                generateLineDataSet(ma10Entries, mColorMa10, "ma10"),
                                generateLineDataSet(ma20Entries, mColorMa20, "ma20"));
                    }

                    if (mIsAverage) combinedData.setData(mLineData);
                    else combinedData.setData(new LineData());
                    mChart.setData(combinedData);//当前屏幕会显示所有的数据
                    mChart.getXAxis().setAxisMaximum(combinedData.getXMax() + 0.5f);
                    mChart.getXAxis().setAxisMinimum(combinedData.getXMin() - 0.5f);
                    mChart.setVisibleXRangeMinimum(7);
                    mChart.setVisibleXRangeMaximum(200);
                    mChart.zoom(mScaleX, 1.0f, mLastIndex, 0, YAxis.AxisDependency.LEFT);
                    LogUtils.e("xVals.size()" + xVals.size(), true);
                }
            } catch (Exception ex) {
                ByteArrayOutputStream error = new ByteArrayOutputStream();
                ex.printStackTrace(new PrintStream(error));
                String exception = error.toString();
                LogUtils.e(exception, true);
            }
        }
    }

    @Override
    public void update() {
        super.update();
        if (mChart.getViewPortHandler().getScaleX() != mScaleX) {
            mChart.fitScreen();
            mChart.zoom(mScaleX, 1.0f, mLastIndex, 0, YAxis.AxisDependency.LEFT);
        }
    }

    /**
     * date: 6/1/18
     * author: chenli
     * description: K线图刷新时生成单个数据
     */
    private void generateCandleAndLineDataEntry(CandleData candleData, int left_index, int index) {
        KlineEntity.DataEntity dataEntity = mDataEntities.get(String.valueOf(index));
        if (dataEntity == null) return;
        mCalendar.setTimeInMillis(Long.valueOf(dataEntity.getDatetime()) / 1000000);
        xVals.put(index, mSimpleDateFormat.format(mCalendar.getTime()));

        CandleEntry candleEntry = new CandleEntry(index, Float.valueOf(dataEntity.getHigh()),
                Float.valueOf(dataEntity.getLow()), Float.valueOf(dataEntity.getOpen()),
                Float.valueOf(dataEntity.getClose()));
        candleData.getDataSetByIndex(0).addEntryOrdered(candleEntry);

        if (index >= left_index + 4) {
            mLineData.getDataSetByIndex(0).addEntryOrdered(new Entry(index, getSum(index - 4, index) / 5));
        }
        if (index >= left_index + 9) {
            mLineData.getDataSetByIndex(1).addEntryOrdered(new Entry(index, getSum(index - 9, index) / 10));
        }
        if (index >= left_index + 19) {
            mLineData.getDataSetByIndex(2).addEntryOrdered(new Entry(index, getSum(index - 19, index) / 20));
        }
    }

    /**
     * date: 6/1/18
     * author: chenli
     * description: K线图初始化时生成单个数据
     */
    private void generateCandleAndLineDataEntry(List<Entry> ma5Entries, List<Entry> ma10Entries, List<Entry> ma20Entries, List<CandleEntry> candleEntries, int i) {
        KlineEntity.DataEntity dataEntity = mDataEntities.get(String.valueOf(i));
        if (dataEntity == null) return;
        mCalendar.setTimeInMillis(Long.valueOf(dataEntity.getDatetime()) / 1000000);
        xVals.put(i, mSimpleDateFormat.format(mCalendar.getTime()));
        CandleEntry candleEntry = new CandleEntry(i,
                Float.valueOf(dataEntity.getHigh()),
                Float.valueOf(dataEntity.getLow()),
                Float.valueOf(dataEntity.getOpen()),
                Float.valueOf(dataEntity.getClose()));
        candleEntries.add(candleEntry);
        if (i >= mLeftIndex + 4) {
            ma5Entries.add(new Entry(i, getSum(i - 4, i) / 5));
        }
        if (i >= mLeftIndex + 9) {
            ma10Entries.add(new Entry(i, getSum(i - 9, i) / 10));
        }
        if (i >= mLeftIndex + 19) {
            ma20Entries.add(new Entry(i, getSum(i - 19, i) / 20));
        }
    }

    /**
     * date: 7/9/17
     * author: chenli
     * description: 用于计算均线
     */
    private float getSum(int a, int b) {
        float sum = 0f;
        for (int i = a; i <= b; i++) {
            KlineEntity.DataEntity dataEntity = mDataEntities.get(String.valueOf(i));
            if (dataEntity != null) {
                try {
                    sum += Float.parseFloat(dataEntity.getClose());
                } catch (NumberFormatException ex) {
                    ex.printStackTrace();
                }
            }
        }
        return sum;
    }

    /**
     * date: 7/9/17
     * author: chenli
     * description: 生成蜡烛图数据
     */
    private CandleData generateCandleData(List<CandleEntry> candleEntries) {
        CandleDataSet set = new CandleDataSet(candleEntries, "");
        set.setAxisDependency(YAxis.AxisDependency.LEFT);
        set.setShadowWidth(0.7f);
        set.setDecreasingColor(ContextCompat.getColor(getActivity(), R.color.kline_green));
        set.setDecreasingPaintStyle(Paint.Style.FILL);
        set.setIncreasingColor(ContextCompat.getColor(getActivity(), R.color.kline_red));
        set.setIncreasingPaintStyle(Paint.Style.STROKE);
        set.setNeutralColor(ContextCompat.getColor(getActivity(), R.color.white));
        set.setShadowColorSameAsCandle(true);
        set.setHighlightLineWidth(1f);
        set.setHighLightColor(ContextCompat.getColor(getActivity(), R.color.white));
        set.setDrawValues(true);
        set.setValueTextColor(Color.RED);
        set.setValueTextSize(9f);
        set.setDrawIcons(false);
        set.setValueFormatter(new MyValueFormatter());
        CandleData candleData = new CandleData();
        candleData.addDataSet(set);
        return candleData;
    }

    /**
     * date: 7/9/17
     * author: chenli
     * description: 生成均线数据
     */
    private LineDataSet generateLineDataSet(List<Entry> entries, int color, String label) {
        LineDataSet set = new LineDataSet(entries, label);
        set.setColor(color);
        set.setLineWidth(1f);
        set.setDrawCircles(false);
        set.setDrawCircleHole(false);
        set.setDrawValues(false);
        set.setHighlightEnabled(false);
        set.setAxisDependency(YAxis.AxisDependency.LEFT);
        return set;
    }

    private LineData generateMultiLineData(LineDataSet... lineDataSets) {
        List<ILineDataSet> dataSets = new ArrayList<>();
        for (LineDataSet lineDataSet :
                lineDataSets) {
            dataSets.add(lineDataSet);

        }
        return new LineData(dataSets);
    }

    /**
     * date: 7/9/17
     * author: chenli
     * description: 接收自选合约列表弹出框以及持仓页传过来的合约代码，以便更新K线图
     */
    @Subscribe
    public void onEvent(IdEvent data) {
        String instrument_id_new = data.getInstrument_id();
        if (!instrument_id.equals(instrument_id_new)) {
            instrument_id = instrument_id_new;
            preSettlement = 1;
            if (BaseApplicationLike.getWebSocketService() != null)
                switch (mKlineType) {
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
            removeOrderLimitLines();
            removePositionLimitLines();
            xVals.clear();
            mChart.clear();
            mChart.fitScreen();

            if (instrument_id.contains("KQ"))
                instrument_id_transaction = LatestFileManager.getSearchEntities().get(instrument_id).getUnderlying_symbol();
            else instrument_id_transaction = instrument_id;
            if (sDataManager.IS_LOGIN) {
                if (mIsPosition) addPositionLimitLines();
                if (mIsPending) addOrderLimitLines();
            }
        }
    }

    /**
     * date: 7/9/17
     * author: chenli
     * description: 接收“设置”按钮最新的设置信息，以便根据用户要求显示持仓、挂单、均线
     */
    @Subscribe
    public void onEventMainThread(SetUpEvent data) {
        if (mIsPending != data.isPending()) {
            mIsPending = data.isPending();
            if (sDataManager.IS_LOGIN) {
                if (mIsPending) addOrderLimitLines();
                else removeOrderLimitLines();
            }
        }

        if (mIsPosition != data.isPosition()) {
            mIsPosition = data.isPosition();
            if (sDataManager.IS_LOGIN) {
                if (mIsPosition) addPositionLimitLines();
                else removePositionLimitLines();
            }
        }

        if (mIsAverage != data.isAverage()) {
            mIsAverage = data.isAverage();
            if (mIsAverage) {
                mChart.getCombinedData().setData(mLineData);
                mChart.getLegend().setEnabled(true);
            } else {
                mChart.getCombinedData().setData(new LineData());
                mChart.getLegend().setEnabled(false);
            }
        }

        mChart.getCombinedData().notifyDataChanged();
        mChart.invalidate();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        //moveTo方法的bug
        MoveViewJob.getInstance(null, 0, 0, null, null);
        super.onPause();
    }

    @Override
    public void onDestroyView() {
        SPUtils.putAndApply(BaseApplicationLike.getContext(), "mScaleX", mScaleX);
        super.onDestroyView();
    }

    /**
     * date: 7/9/17
     * author: chenli
     * description: 点击K线图弹出实时信息
     * version:
     * state: done
     */
    public class MyMarkerView extends MarkerView {
        private TextView yValue;
        private TextView xValue;
        private TextView open;
        private TextView high;
        private TextView low;
        private TextView close;
        private TextView closeChange;
        private TextView closeChangePercent;
        private TextView volume;
        private TextView closeOi;
        private TextView closeOiDelta;
        private String markViewState;
        private SimpleDateFormat simpleDateFormat;
        private SimpleDateFormat simpleDateFormat1;
        private Calendar calendar;

        /**
         * Constructor. Sets up the MarkerView with a custom layout resource.
         */
        public MyMarkerView(Context context) {
            super(context, R.layout.view_marker_kline);
            yValue = findViewById(R.id.y_value);
            xValue = findViewById(R.id.x_value);
            open = findViewById(R.id.open);
            high = findViewById(R.id.high);
            low = findViewById(R.id.low);
            close = findViewById(R.id.close);
            closeChange = findViewById(R.id.close_change);
            closeChangePercent = findViewById(R.id.close_change_percent);
            volume = findViewById(R.id.volume);
            closeOi = findViewById(R.id.close_oi);
            closeOiDelta = findViewById(R.id.close_oi_delta);
            markViewState = "right";
            simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA);
            simpleDateFormat1 = new SimpleDateFormat("HH:mm", Locale.CHINA);
            calendar = Calendar.getInstance();
        }

        @Override
        public void refreshContent(Entry e, Highlight highlight) {
            if (e instanceof CandleEntry) {
                CandleEntry candleEntry = (CandleEntry) e;
                String xValue = MathUtils.round(String.valueOf(candleEntry.getX()), 0);
                KlineEntity.DataEntity dataEntity = mDataEntities.get(xValue);
                String xValuePre = MathUtils.subtract(xValue, "1");
                KlineEntity.DataEntity dataEntityPre = mDataEntities.get(xValuePre);
                if (dataEntity != null && dataEntityPre != null) {
                    calendar.setTimeInMillis(Long.valueOf(dataEntity.getDatetime()) / 1000000);
                    String time = simpleDateFormat.format(calendar.getTime());
                    String date = simpleDateFormat1.format(calendar.getTime());
                    String open = LatestFileManager.saveScaleByPtick(dataEntity.getOpen(), instrument_id);
                    String high = LatestFileManager.saveScaleByPtick(dataEntity.getHigh(), instrument_id);
                    String low = LatestFileManager.saveScaleByPtick(dataEntity.getLow(), instrument_id);
                    String close = LatestFileManager.saveScaleByPtick(dataEntity.getClose(), instrument_id);
                    String change = LatestFileManager.saveScaleByPtick(MathUtils.subtract(dataEntity.getClose(), dataEntityPre.getClose()), instrument_id);
                    String changePercent = MathUtils.round(MathUtils.multiply(MathUtils.divide(change, dataEntityPre.getClose()), "100"), 2) + "%";
                    String volume = dataEntity.getVolume();
                    String closeOi = dataEntity.getClose_oi();
                    String closeOiDelta = MathUtils.subtract(closeOi, dataEntityPre.getClose_oi());
                    this.yValue.setText(time);
                    this.xValue.setText(date);
                    this.open.setText(open);
                    this.high.setText(high);
                    this.low.setText(low);
                    this.close.setText(close);
                    this.closeChange.setText(change);
                    this.closeChangePercent.setText(changePercent);
                    this.volume.setText(volume);
                    this.closeOi.setText(closeOi);
                    this.closeOiDelta.setText(closeOiDelta);
                }
            }
            super.refreshContent(e, highlight);
        }

        @Override
        public void draw(Canvas canvas, float posX, float posY) {
            // translate to the correct position and draw
            float deadlineRight = mChart.getViewPortHandler().contentRight() - getWidth();
            float deadlineLeft = mChart.getViewPortHandler().contentLeft() + getWidth();
            if (posX <= deadlineLeft) {
                canvas.translate(deadlineRight, mChart.getViewPortHandler().contentTop());
                markViewState = "right";
            } else if (posX >= deadlineRight) {
                canvas.translate(mChart.getViewPortHandler().contentLeft(), mChart.getViewPortHandler().contentTop());
                markViewState = "left";
            } else {
                if (markViewState.equals("right"))
                    canvas.translate(deadlineRight, mChart.getViewPortHandler().contentTop());
                if (markViewState.equals("left"))
                    canvas.translate(mChart.getViewPortHandler().contentLeft(), mChart.getViewPortHandler().contentTop());
            }
            draw(canvas);
        }
    }

    /**
     * date: 7/9/17
     * author: chenli
     * description: 格式化X轴数据
     * version:
     * state: done
     */
    public class MyXAxisValueFormatter implements IAxisValueFormatter {

        private SparseArray<String> mValues;

        private MyXAxisValueFormatter(SparseArray<String> values) {
            this.mValues = values;
        }

        @Override
        public String getFormattedValue(float value, AxisBase axis) {
            // "value" represents the position of the label on the axis (x or y)
            return mValues.get((int) value);
        }
    }

    /**
     * date: 7/9/17
     * author: chenli
     * description: 格式化左Y轴数据
     * version:
     * state: done
     */
    public class MyYAxisValueFormatter implements IAxisValueFormatter {

        @Override
        public String getFormattedValue(float value, AxisBase axis) {
            return LatestFileManager.saveScaleByPtick(String.valueOf(value), instrument_id);
        }
    }

    /**
     * date: 7/9/17
     * author: chenli
     * description: 格式化最高最低价标识
     * version:
     * state: done
     */
    public class MyValueFormatter implements IValueFormatter {

        @Override
        public String getFormattedValue(float value, Entry entry, int dataSetIndex, ViewPortHandler viewPortHandler) {
            return LatestFileManager.saveScaleByPtick(String.valueOf(value), instrument_id);
        }
    }
}

package com.shinnytech.futures.controller.fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.mikephil.charting.charts.CombinedChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.LegendEntry;
import com.github.mikephil.charting.components.LimitLine;
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
import com.shinnytech.futures.application.BaseApplication;
import com.shinnytech.futures.constants.CommonConstants;
import com.shinnytech.futures.model.bean.eventbusbean.IdEvent;
import com.shinnytech.futures.model.bean.eventbusbean.KlineEvent;
import com.shinnytech.futures.model.bean.eventbusbean.SetUpEvent;
import com.shinnytech.futures.model.bean.futureinfobean.ChartEntity;
import com.shinnytech.futures.model.bean.futureinfobean.KlineEntity;
import com.shinnytech.futures.model.bean.searchinfobean.SearchEntity;
import com.shinnytech.futures.model.engine.LatestFileManager;
import com.shinnytech.futures.utils.LogUtils;
import com.shinnytech.futures.utils.MathUtils;
import com.shinnytech.futures.utils.SPUtils;
import com.shinnytech.futures.view.custommpchart.mycomponent.MyMarkerView;
import com.shinnytech.futures.view.custommpchart.mycomponent.MyXAxis;
import com.shinnytech.futures.view.custommpchart.mycomponent.MyYAxis;

import org.greenrobot.eventbus.Subscribe;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static com.shinnytech.futures.constants.CommonConstants.CHART_ID;
import static com.shinnytech.futures.constants.CommonConstants.DAY_FRAGMENT;
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
    private static final String FRAGMENT_XVALS_FORMAT = "fragment_format";

    /**
     * date: 2018/12/17
     * description: 页面类型
     */
    private static final String FRAGMENT_TYPE = "fragment_type";

    /**
     * date: 7/9/17
     * description: K线图类型--日线、小时线、5分钟线
     */
    private static final String FRAGMENT_KLINE_TYPE = "kline_type";
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
    private int[] mColorMas;

    private int mViewWidth;
    private int mLeftIndex;
    private int mRightIndex;
    private int mLastIndex;
    private int mBaseIndex;
    private boolean mIsDrag;
    private Highlight mLastHighlighted;

    /**
     * date: 2018/11/19
     * description: 最新价线
     */
    protected Map<String, LimitLine> mLatestLimitLines;
    private ChartEntity mChartEntity;
    private KlineEntity mKlineEntity;
    private List<Integer> mas;


    /**
     * date: 7/9/17
     * author: chenli
     * description: 创建页面实例
     */
    public static KlineFragment newInstance(String xValsFormat, String klineType, String fragmentType) {
        KlineFragment fragment = new KlineFragment();
        Bundle bundle = new Bundle();
        bundle.putString(FRAGMENT_XVALS_FORMAT, xValsFormat);
        bundle.putString(FRAGMENT_KLINE_TYPE, klineType);
        bundle.putString(FRAGMENT_TYPE, fragmentType);
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
        mFragmentType = getArguments().getString(FRAGMENT_TYPE);
        mKlineType = getArguments().getString(FRAGMENT_KLINE_TYPE);
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
        int ma1 = ContextCompat.getColor(getActivity(), R.color.kline_ma1);
        int ma2 = ContextCompat.getColor(getActivity(), R.color.kline_ma2);
        int ma3 = ContextCompat.getColor(getActivity(), R.color.kline_ma3);
        int ma4 = ContextCompat.getColor(getActivity(), R.color.kline_ma4);
        int ma5 = ContextCompat.getColor(getActivity(), R.color.kline_ma5);
        int ma6 = ContextCompat.getColor(getActivity(), R.color.kline_ma6);
        mColorMas = new int[]{ma1, ma2, ma3, ma4, ma5, ma6};

        mViewWidth = VIEW_WIDTH;
        if (mScaleX == 0.0f)
            mScaleX = (float) SPUtils.get(BaseApplication.getContext(), "mScaleX", 1.0f);
        mIsDrag = true;
        mLatestLimitLines = new HashMap<>();
        mas = new ArrayList<>();
        String data = (String) SPUtils.get(BaseApplication.getContext(), CommonConstants.CONFIG_PARA_MA, CommonConstants.PARA_MA);
        for (String para :
                data.split(",")) {
            try {
                int ma = Integer.parseInt(para);
                if (ma != 0) {
                    mas.add(ma);
                }
            } catch (Exception e) {
                continue;
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void initChart() {
        super.initChart();
        mChart.setScaleYEnabled(false);
        mChart.setDrawOrder(
                new CombinedChart.DrawOrder[]{CombinedChart.DrawOrder.CANDLE, CombinedChart.DrawOrder.LINE});
        final KlineMarkerView marker = new KlineMarkerView(getActivity());
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

        List<LegendEntry> legendEntries = new ArrayList<>();
        for (int i = 0; i < mas.size(); i++) {
            int para = mas.get(i);
            LegendEntry ma = new LegendEntry("MA" + para, Legend.LegendForm.SQUARE,
                    NaN, NaN, null, mColorMas[i]);
            legendEntries.add(ma);
        }
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
                            float startIndex = mLeftIndex - mBaseIndex;
                            if (Math.abs(mChart.getLowestVisibleX() - startIndex) < 50) {
                                if (xVals.size() >= mViewWidth) {
                                    mViewWidth = mViewWidth + 100;
                                    if (BaseApplication.getWebSocketService() != null)
                                        BaseApplication.getWebSocketService().sendSetChartKline(instrument_id, mViewWidth, mKlineType);
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
    protected void refreshKline() {
        try {
            //开始加载数据
            if (mChart.getData() != null && mChart.getData().getDataSetCount() > 0) {
                CandleData candleData = mChart.getCandleData();
                CombinedData combinedData = mChart.getCombinedData();
                String left_id_t = mChartEntity.getLeft_id();
                String right_id_t = mChartEntity.getRight_id();
                String last_id_t = mKlineEntity.getLast_id();
                int last_index_t = Integer.parseInt(last_id_t);
                int left_index_t = Integer.parseInt(left_id_t);
                int right_index_t = Integer.parseInt(right_id_t);
                Map<String, KlineEntity.DataEntity> dataEntities = mKlineEntity.getData();

                if (right_index_t == mRightIndex && left_index_t == mLeftIndex) {
                    KlineEntity.DataEntity dataEntity = dataEntities.get(last_id_t);
                    if (dataEntity == null) return;
                    LogUtils.e("单个柱子刷新", false);
                    candleData.removeEntry(mLastIndex - mBaseIndex, 0);
                    for (int i = 0; i < mLineData.getDataSetCount(); i++)
                        mLineData.removeEntry(mLastIndex - mBaseIndex, i);
                    generateCandleAndLineDataEntry(candleData, mLeftIndex, mLastIndex);
                    refreshLatestLine(dataEntity);
                } else if (right_index_t > mRightIndex && left_index_t > mLeftIndex) {
                    LogUtils.e("向后添加柱子", false);
                    for (int i = this.mRightIndex + 1; i <= right_index_t; i++) {
                        generateCandleAndLineDataEntry(candleData, mLeftIndex, i);
                    }
                    refreshLatestLine(dataEntities.get(right_id_t));
                } else if (left_index_t < mLeftIndex) {
                    LogUtils.e("向前添加柱子", false);
                    for (int i = this.mLeftIndex - 1; i >= left_index_t; i--) {
                        generateCandleAndLineDataEntry(candleData, left_index_t, i);
                    }
                }
                this.mLastIndex = last_index_t;
                this.mRightIndex = right_index_t;
                this.mLeftIndex = left_index_t;
                combinedData.notifyDataChanged();
                mChart.notifyDataSetChanged();
                mChart.getXAxis().setAxisMaximum(combinedData.getXMax() + 2.5f);
                mChart.getXAxis().setAxisMinimum(combinedData.getXMin() - 0.5f);
                mChart.setVisibleXRangeMinimum(10);
                mChart.setVisibleXRangeMaximum(200);
                mChart.invalidate();
            } else {
                LogUtils.e("K线图初始化", true);
                Map<String, KlineEntity> klineEntities = sDataManager.getRtnData().getKlines().get(instrument_id);
                mChartEntity = sDataManager.getRtnData().getCharts().get(CHART_ID);
                if (klineEntities == null || mChartEntity == null) return;
                String left_id_t = mChartEntity.getLeft_id();
                String right_id_t = mChartEntity.getRight_id();
                if (left_id_t == null || right_id_t == null) return;
                boolean mdhis_more_data = sDataManager.getRtnData().getMdhis_more_data();
                if ((left_id_t.equals("-1") && right_id_t.equals("-1")) || mdhis_more_data) return;
                String ins_list = mChartEntity.getState().get("ins_list");
                String duration = mChartEntity.getState().get("duration");
                if (ins_list == null || duration == null) return;
                if (!ins_list.equals(instrument_id) || !duration.equals(mKlineType)) return;
                mKlineEntity = klineEntities.get(mKlineType);
                if (mKlineEntity == null) return;
                String last_id_t = mKlineEntity.getLast_id();
                Map<String, KlineEntity.DataEntity> dataEntities = mKlineEntity.getData();
                if (last_id_t == null || "-1".equals(last_id_t) || dataEntities.isEmpty()) return;
                mBaseIndex = Integer.parseInt(left_id_t);
                mLeftIndex = Integer.parseInt(left_id_t);
                mRightIndex = Integer.parseInt(right_id_t);
                mLastIndex = Integer.parseInt(last_id_t);

                CombinedData combinedData = new CombinedData();
                CandleData candleData = generateCandleData();
                combinedData.setData(candleData);

                mLineData = generateLineData();
                if (mIsAverage) combinedData.setData(mLineData);
                else combinedData.setData(new LineData());

                mChart.setData(combinedData);//当前屏幕会显示所有的数据
                mChart.getXAxis().setAxisMaximum(combinedData.getXMax() + 2.5f);
                mChart.getXAxis().setAxisMinimum(combinedData.getXMin() - 0.5f);
                mChart.setVisibleXRangeMinimum(10);
                mChart.setVisibleXRangeMaximum(200);
                generateLatestLine(dataEntities.get(right_id_t));
                mChart.zoom(mScaleX, 1.0f, mLastIndex - mBaseIndex, 0, YAxis.AxisDependency.LEFT);
                mChart.moveViewToX(mLastIndex - mBaseIndex);
                int height = (int) mChart.getViewPortHandler().contentHeight();
                int width = (int) (mChart.getViewPortHandler().contentWidth() / 6);
                ((KlineMarkerView) mChart.getMarker()).resize(width, height);
            }
        } catch (Exception ex) {
            ByteArrayOutputStream error = new ByteArrayOutputStream();
            ex.printStackTrace(new PrintStream(error));
            String exception = error.toString();
            LogUtils.e(exception, true);
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
        Map<String, KlineEntity.DataEntity> dataEntities = mKlineEntity.getData();
        KlineEntity.DataEntity dataEntity = dataEntities.get(String.valueOf(index));
        if (dataEntity == null) return;
        mCalendar.setTimeInMillis(Long.valueOf(dataEntity.getDatetime()) / 1000000);
        xVals.put(index - mBaseIndex, mSimpleDateFormat.format(mCalendar.getTime()));

        CandleEntry candleEntry = new CandleEntry(index - mBaseIndex, Float.valueOf(dataEntity.getHigh()),
                Float.valueOf(dataEntity.getLow()), Float.valueOf(dataEntity.getOpen()),
                Float.valueOf(dataEntity.getClose()));

        candleData.getDataSetByIndex(0).addEntryOrdered(candleEntry);

        for (int i = 0; i < mas.size(); i++) {
            int para = mas.get(i);
            if (index >= left_index + para - 1) {
                mLineData.getDataSetByIndex(i).addEntryOrdered(
                        new Entry(index - mBaseIndex, getSum(index - para + 1, index) / para));

            }
        }
    }

    /**
     * date: 6/1/18
     * author: chenli
     * description: K线图初始化时生成单个数据
     */
    private CandleEntry generateCandleEntry(int i) {
        Map<String, KlineEntity.DataEntity> dataEntities = mKlineEntity.getData();
        KlineEntity.DataEntity dataEntity = dataEntities.get(String.valueOf(i));
        if (dataEntity == null) return new CandleEntry(i - mBaseIndex, 0.0f, 0.0f, 0.0f, 0.0f);
        mCalendar.setTimeInMillis(Long.valueOf(dataEntity.getDatetime()) / 1000000);
        xVals.put(i - mBaseIndex, mSimpleDateFormat.format(mCalendar.getTime()));
        CandleEntry candleEntry = new CandleEntry(i - mBaseIndex,
                Float.valueOf(dataEntity.getHigh()),
                Float.valueOf(dataEntity.getLow()),
                Float.valueOf(dataEntity.getOpen()),
                Float.valueOf(dataEntity.getClose()));
        return candleEntry;
    }

    /**
     * date: 2019/1/20
     * author: chenli
     * description: 均线初始化时生成单个数据
     */
    private Entry generateLineDataEntry(int i, int lineIndex) {
        return new Entry(i - mBaseIndex, getSum(i - lineIndex, i) / (lineIndex + 1));
    }


    /**
     * date: 7/9/17
     * author: chenli
     * description: 用于计算均线
     */
    private float getSum(int a, int b) {
        float sum = 0f;
        Map<String, KlineEntity.DataEntity> dataEntities = mKlineEntity.getData();
        for (int i = a; i <= b; i++) {
            KlineEntity.DataEntity dataEntity = dataEntities.get(String.valueOf(i));
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
     * date: 2018/11/19
     * author: chenli
     * description: 生成最新价线
     */
    private void generateLatestLine(KlineEntity.DataEntity dataEntity) {
        try {
            String limit = dataEntity.getClose();
            LimitLine limitLine = new LimitLine(Float.valueOf(limit), LatestFileManager.saveScaleByPtick(limit, instrument_id));
            limitLine.setLineWidth(0.7f);
            limitLine.enableDashedLine(2f, 2f, 0f);
            limitLine.setLineColor(ContextCompat.getColor(getActivity(), R.color.black_light_more));
            limitLine.setLabelPosition(LimitLine.LimitLabelPosition.RIGHT_TOP);
            limitLine.setTextSize(10f);
            limitLine.setTextColor(ContextCompat.getColor(getActivity(), R.color.black_light_more));
            mChart.getAxisLeft().addLimitLine(limitLine);
            mLatestLimitLines.put("latest", limitLine);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * date: 2018/11/19
     * author: chenli
     * description: 刷新最新价线
     */
    private void refreshLatestLine(KlineEntity.DataEntity dataEntity) {
        try {
            float limit = Float.valueOf(dataEntity.getClose());
            LimitLine limitLine = mLatestLimitLines.get("latest");
            if (limitLine.getLimit() != limit) {
                mChart.getAxisLeft().removeLimitLine(limitLine);
                mLatestLimitLines.remove("latest");
                generateLatestLine(dataEntity);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void removeLatestLine() {
        if (!mLatestLimitLines.isEmpty()) {
            LimitLine limitLine = mLatestLimitLines.get("latest");
            mChart.getAxisLeft().removeLimitLine(limitLine);
            mLatestLimitLines.remove("latest");
        }
    }

    /**
     * date: 7/9/17
     * author: chenli
     * description: 生成蜡烛图数据
     */
    private CandleData generateCandleData() {
        List<CandleEntry> candleEntries = new ArrayList<>();
        for (int i = mLeftIndex; i <= mLastIndex; i++) {
            candleEntries.add(generateCandleEntry(i));
        }
        CandleDataSet set = new CandleDataSet(candleEntries, "");
        set.setAxisDependency(YAxis.AxisDependency.LEFT);
        set.setShadowWidth(0.7f);
        set.setDecreasingColor(ContextCompat.getColor(getActivity(), R.color.kline_green));
        set.setDecreasingPaintStyle(Paint.Style.FILL);
        set.setIncreasingColor(ContextCompat.getColor(getActivity(), R.color.kline_red));
        set.setIncreasingPaintStyle(Paint.Style.STROKE);
        set.setNeutralColor(ContextCompat.getColor(getActivity(), R.color.white));
        set.setShadowColorSameAsCandle(true);
        set.setHighlightLineWidth(0.7f);
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
    private LineDataSet generateLineDataSet(int para, int color, String label) {
        List<Entry> entries = new ArrayList<>();
        for (int i = mLeftIndex; i <= mLastIndex; i++) {
            if (i >= mLeftIndex + para - 1)
                entries.add(generateLineDataEntry(i, para - 1));
        }
        LineDataSet set = new LineDataSet(entries, label);
        set.setColor(color);
        set.setLineWidth(0.7f);
        set.setDrawCircles(false);
        set.setDrawCircleHole(false);
        set.setDrawValues(false);
        set.setHighlightEnabled(false);
        set.setAxisDependency(YAxis.AxisDependency.LEFT);
        return set;
    }

    private LineData generateLineData() {
        List<ILineDataSet> dataSets = new ArrayList<>();
        for (int i = 0; i < mas.size(); i++) {
            int ma = mas.get(i);
            int color = mColorMas[i];
            dataSets.add(generateLineDataSet(ma, color, "MA" + ma));
        }
        return new LineData(dataSets);
    }

    /**
     * date: 2018/12/18
     * author: chenli
     * description: 不改页情况下k线周期更新
     */
    @Subscribe
    public void onEvent(KlineEvent klineEvent) {
        String fragmentType = klineEvent.getFragmentType();
        String klineType = klineEvent.getKlineType();
        if (mFragmentType.equals(fragmentType) && !mKlineType.equals(klineType)) {
            mKlineType = klineType;

            removeLatestLine();
            removeOrderLimitLines();
            removePositionLimitLines();
            xVals.clear();
            mChart.clear();
            mChart.fitScreen();

            if (BaseApplication.getWebSocketService() != null)
                BaseApplication.getWebSocketService().sendSetChartKline(instrument_id, VIEW_WIDTH, mKlineType);


            if (sDataManager.IS_LOGIN) {
                if (mIsPosition) addPositionLimitLines();
                if (mIsPending) addOrderLimitLines();
            }

        }
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

        removeLatestLine();
        removeOrderLimitLines();
        removePositionLimitLines();
        xVals.clear();
        mChart.clear();
        mChart.fitScreen();

        if (BaseApplication.getWebSocketService() != null)
            BaseApplication.getWebSocketService().sendSetChartKline(instrument_id, VIEW_WIDTH, mKlineType);

        if (instrument_id.contains("KQ") && searchEntity != null)
            instrument_id_transaction = searchEntity.getUnderlying_symbol();
        else instrument_id_transaction = instrument_id;

        if (sDataManager.IS_LOGIN) {
            if (mIsPosition) addPositionLimitLines();
            if (mIsPending) addOrderLimitLines();
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
        SPUtils.putAndApply(BaseApplication.getContext(), "mScaleX", mScaleX);
        super.onDestroyView();
    }

    /**
     * date: 7/9/17
     * author: chenli
     * description: 点击K线图弹出实时信息
     * version:
     * state: done
     */
    public class KlineMarkerView extends MyMarkerView {
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
        public KlineMarkerView(Context context) {
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
            simpleDateFormat = new SimpleDateFormat("yyyyMMdd", Locale.CHINA);
            if (DAY_FRAGMENT.equals(mFragmentType)) {
                xValue.setVisibility(GONE);
            } else {
                xValue.setVisibility(VISIBLE);
                simpleDateFormat1 = new SimpleDateFormat("HH:mm:ss", Locale.CHINA);
            }
            calendar = Calendar.getInstance();
        }

        @Override
        public void refreshContent(Entry e, Highlight highlight) {
            if (e instanceof CandleEntry) {
                Map<String, KlineEntity.DataEntity> dataEntities = mKlineEntity.getData();
                CandleEntry candleEntry = (CandleEntry) e;
                String xValue = MathUtils.round(String.valueOf(candleEntry.getX() + mBaseIndex), 0);
                KlineEntity.DataEntity dataEntity = dataEntities.get(xValue);
                String xValuePre = MathUtils.subtract(xValue, "1");
                KlineEntity.DataEntity dataEntityPre = dataEntities.get(xValuePre);
                if (dataEntity != null && dataEntityPre != null) {
                    calendar.setTimeInMillis(Long.valueOf(dataEntity.getDatetime()) / 1000000);
                    String time = simpleDateFormat.format(calendar.getTime());
                    String open = LatestFileManager.saveScaleByPtick(dataEntity.getOpen(), instrument_id);
                    String high = LatestFileManager.saveScaleByPtick(dataEntity.getHigh(), instrument_id);
                    String low = LatestFileManager.saveScaleByPtick(dataEntity.getLow(), instrument_id);
                    String close = LatestFileManager.saveScaleByPtick(dataEntity.getClose(), instrument_id);
                    String closePre = LatestFileManager.saveScaleByPtick(dataEntityPre.getClose(), instrument_id);
                    String change = LatestFileManager.saveScaleByPtick(MathUtils.subtract(dataEntity.getClose(), dataEntityPre.getClose()), instrument_id);
                    String changePercent = MathUtils.round(MathUtils.multiply(MathUtils.divide(change, dataEntityPre.getClose()), "100"), 2) + "%";
                    String volume = dataEntity.getVolume();
                    String closeOi = dataEntity.getClose_oi();
                    String closeOiDelta = MathUtils.subtract(closeOi, dataEntityPre.getClose_oi());
                    this.yValue.setText(time);
                    if (this.xValue.getVisibility() == VISIBLE) {
                        String date = simpleDateFormat1.format(calendar.getTime());
                        this.xValue.setText(date);
                    }
                    this.open.setText(open);
                    this.high.setText(high);
                    this.low.setText(low);
                    this.close.setText(close);
                    this.closeChange.setText(change);
                    this.closeChangePercent.setText(changePercent);
                    this.volume.setText(volume);
                    this.closeOi.setText(closeOi);
                    this.closeOiDelta.setText(closeOiDelta);

                    try {
                        float closePre_float = Float.parseFloat(closePre);
                        float open_float = Float.parseFloat(open);
                        float high_float = Float.parseFloat(high);
                        float low_float = Float.parseFloat(low);
                        float close_float = Float.parseFloat(close);

                        if (open_float < closePre_float)this.open.setTextColor(ContextCompat.getColor(getActivity(), R.color.marker_green));
                        else this.open.setTextColor(ContextCompat.getColor(getActivity(), R.color.marker_red));

                        if (high_float < closePre_float)this.high.setTextColor(ContextCompat.getColor(getActivity(), R.color.marker_green));
                        else this.high.setTextColor(ContextCompat.getColor(getActivity(), R.color.marker_red));

                        if (low_float < closePre_float)this.low.setTextColor(ContextCompat.getColor(getActivity(), R.color.marker_green));
                        else this.low.setTextColor(ContextCompat.getColor(getActivity(), R.color.marker_red));

                        if (close_float < closePre_float)this.close.setTextColor(ContextCompat.getColor(getActivity(), R.color.marker_green));
                        else this.close.setTextColor(ContextCompat.getColor(getActivity(), R.color.marker_red));

                        float close_change_float = Float.parseFloat(change);
                        if (close_change_float < 0){
                            this.closeChange.setTextColor(ContextCompat.getColor(getActivity(), R.color.marker_green));
                            this.closeChangePercent.setTextColor(ContextCompat.getColor(getActivity(), R.color.marker_green));
                        }
                        else {
                            this.closeChange.setTextColor(ContextCompat.getColor(getActivity(), R.color.marker_red));
                            this.closeChangePercent.setTextColor(ContextCompat.getColor(getActivity(), R.color.marker_red));
                        }

                        int close_oi_delta = Integer.parseInt(closeOiDelta);
                        if (close_oi_delta < 0)this.closeOiDelta.setTextColor(ContextCompat.getColor(getActivity(), R.color.marker_green));
                        else this.closeOiDelta.setTextColor(ContextCompat.getColor(getActivity(), R.color.marker_red));

                    }catch (Exception ex){
                        ex.printStackTrace();
                    }
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

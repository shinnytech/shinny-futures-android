package com.shinnytech.futures.controller.fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import android.text.TextUtils;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.mikephil.charting.charts.Chart;
import com.github.mikephil.charting.charts.CombinedChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.LegendEntry;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.CandleData;
import com.github.mikephil.charting.data.CandleDataSet;
import com.github.mikephil.charting.data.CandleEntry;
import com.github.mikephil.charting.data.CombinedData;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.jobs.MoveViewJob;
import com.shinnytech.futures.R;
import com.shinnytech.futures.application.BaseApplication;
import com.shinnytech.futures.constants.MarketConstants;
import com.shinnytech.futures.constants.SettingConstants;
import com.shinnytech.futures.controller.activity.MainActivity;
import com.shinnytech.futures.controller.activity.MainActivityPresenter;
import com.shinnytech.futures.model.bean.eventbusbean.CommonSwitchEvent;
import com.shinnytech.futures.model.bean.eventbusbean.ParaChangeEvent;
import com.shinnytech.futures.model.bean.eventbusbean.SwitchDurationEvent;
import com.shinnytech.futures.model.bean.eventbusbean.SwitchInsEvent;
import com.shinnytech.futures.model.bean.futureinfobean.ChartEntity;
import com.shinnytech.futures.model.bean.futureinfobean.KlineEntity;
import com.shinnytech.futures.model.engine.LatestFileManager;
import com.shinnytech.futures.utils.LogUtils;
import com.shinnytech.futures.utils.MathUtils;
import com.shinnytech.futures.utils.SPUtils;
import com.shinnytech.futures.view.custommpchart.mychartlistener.CoupleChartGestureListener;
import com.shinnytech.futures.view.custommpchart.mycomponent.MyMarkerView;
import com.shinnytech.futures.view.custommpchart.mycomponent.MyValueFormatter;
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

import static com.shinnytech.futures.constants.MarketConstants.CHART_ID;
import static com.shinnytech.futures.constants.SettingConstants.CONFIG_AVERAGE_LINE;
import static com.shinnytech.futures.constants.MarketConstants.DAY_FRAGMENT;
import static com.shinnytech.futures.constants.MarketConstants.VIEW_WIDTH;
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
    public boolean mIsDrag;
    /**
     * date: 2018/11/19
     * description: 最新价线
     */
    protected Map<String, LimitLine> mLatestLimitLines;
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
    private int mRightIndex;
    private ChartEntity mChartEntity;
    private List<Integer> mas;
    private String mXValsFormat;
    private KlineMarkerView mKlineMarkerView;

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
        mXValsFormat = getArguments().getString(FRAGMENT_XVALS_FORMAT);
        mFragmentType = getArguments().getString(FRAGMENT_TYPE);
        mKlineType = getArguments().getString(FRAGMENT_KLINE_TYPE);
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
        //k线图控制legend的显示与否
        mIsAverage = (boolean) SPUtils.get(BaseApplication.getContext(), CONFIG_AVERAGE_LINE, true);
        if (!mIsAverage) mTopChartViewBase.getLegend().setEnabled(false);
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
        mIsDrag = true;
        mLatestLimitLines = new HashMap<>();
        mas = new ArrayList<>();
        String data = (String) SPUtils.get(BaseApplication.getContext(), SettingConstants.CONFIG_PARA_MA, SettingConstants.PARA_MA);
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

        mTopChartViewBase.setScaleYEnabled(false);
        mTopChartViewBase.setDrawOrder(
                new CombinedChart.DrawOrder[]{CombinedChart.DrawOrder.CANDLE, CombinedChart.DrawOrder.LINE});
        mKlineMarkerView = new KlineMarkerView(getActivity());
        mKlineMarkerView.setChartView(mTopChartViewBase);
        mTopChartViewBase.setMarker(mKlineMarkerView);

        mTopChartViewBase.setDrawBorders(true);
        mTopChartViewBase.setBorderColor(mColorGrid);

        MyXAxis bottomAxis = (MyXAxis) mTopChartViewBase.getXAxis();
        bottomAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        bottomAxis.setDrawGridLines(true);
        bottomAxis.setDrawAxisLine(true);
        bottomAxis.setDrawLabels(false);
        bottomAxis.enableGridDashedLine(3, 6, 0);
        bottomAxis.setAxisLineColor(mColorGrid);
        bottomAxis.setGridColor(mColorGrid);

        MyYAxis leftAxis = (MyYAxis) mTopChartViewBase.getAxisLeft();
        leftAxis.setPosition(YAxis.YAxisLabelPosition.INSIDE_CHART);
        leftAxis.setDrawGridLines(true);
        leftAxis.setDrawAxisLine(false);
        leftAxis.setSpaceBottom(3f);
        leftAxis.setSpaceTop(3f);
        leftAxis.enableGridDashedLine(3, 6, 0);
        leftAxis.setGridColor(mColorGrid);
        leftAxis.setTextColor(mColorText);
        leftAxis.setLabelCount(6, true);
        leftAxis.setValueFormatter(new TopChartLeftYAxisValueFormatter());

        MyYAxis rightAxis = (MyYAxis) mTopChartViewBase.getAxisRight();
        rightAxis.setEnabled(false);

        mMiddleChartViewBase.setScaleYEnabled(false);
        mMiddleChartViewBase.setDrawBorders(false);

        MyXAxis middleBottomAxis = (MyXAxis) mMiddleChartViewBase.getXAxis();
        middleBottomAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        middleBottomAxis.setDrawGridLines(true);
        middleBottomAxis.setDrawAxisLine(true);
        middleBottomAxis.setAxisLineWidth(0.7f);
        middleBottomAxis.setDrawLabels(false);
        middleBottomAxis.enableGridDashedLine(3, 6, 0);
        middleBottomAxis.setGridColor(mColorGrid);
        middleBottomAxis.setAxisLineColor(mColorGrid);
        middleBottomAxis.setTextColor(mColorText);

        MyYAxis middleLeftAxis = (MyYAxis) mMiddleChartViewBase.getAxisLeft();
        middleLeftAxis.setPosition(YAxis.YAxisLabelPosition.INSIDE_CHART);
        middleLeftAxis.setDrawGridLines(true);
        middleLeftAxis.setDrawAxisLine(false);
        middleLeftAxis.enableGridDashedLine(3, 6, 0);
        middleLeftAxis.setGridColor(mColorGrid);
        middleLeftAxis.setTextColor(mColorText);
        middleLeftAxis.setLabelCount(2, true);
        middleLeftAxis.setAxisMinimum(0);
        middleLeftAxis.setSpaceBottom(0);

        MyYAxis middleRightAxis = (MyYAxis) mMiddleChartViewBase.getAxisRight();
        middleRightAxis.setDrawLabels(false);
        middleRightAxis.setDrawAxisLine(false);
        middleRightAxis.setDrawGridLines(false);

        mBottomChartViewBase.setScaleYEnabled(false);
        mBottomChartViewBase.setDrawBorders(false);

        MyXAxis bottomBottomAxis = (MyXAxis) mBottomChartViewBase.getXAxis();
        bottomBottomAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        bottomBottomAxis.setDrawGridLines(true);
        bottomBottomAxis.setDrawAxisLine(true);
        bottomBottomAxis.setAxisLineWidth(0.7f);
        bottomBottomAxis.setDrawLabels(true);
        bottomBottomAxis.enableGridDashedLine(3, 6, 0);
        bottomBottomAxis.setGridColor(mColorGrid);
        bottomBottomAxis.setAxisLineColor(mColorGrid);
        bottomBottomAxis.setTextColor(mColorText);
        bottomBottomAxis.setValueFormatter(new KlineFragment.MyXAxisValueFormatter(xVals));

        MyYAxis bottomLeftAxis = (MyYAxis) mBottomChartViewBase.getAxisLeft();
        bottomLeftAxis.setPosition(YAxis.YAxisLabelPosition.INSIDE_CHART);
        bottomLeftAxis.setDrawGridLines(true);
        bottomLeftAxis.setDrawAxisLine(false);
        bottomLeftAxis.enableGridDashedLine(3, 6, 0);
        bottomLeftAxis.setGridColor(mColorGrid);
        bottomLeftAxis.setTextColor(mColorText);
        bottomLeftAxis.setLabelCount(4, true);
        bottomLeftAxis.setValueFormatter(new BottomChartLeftYAxisValueFormatter());

        MyYAxis bottomRightAxis = (MyYAxis) mBottomChartViewBase.getAxisRight();
        bottomRightAxis.setDrawLabels(false);
        bottomRightAxis.setDrawAxisLine(false);
        bottomRightAxis.setDrawGridLines(false);

        // 将K线控的滑动事件传递给交易量控件
        mTopChartViewBase.setOnChartGestureListener(
                new CoupleChartGestureListener(mTopChartViewBase, new Chart[]{mMiddleChartViewBase, mBottomChartViewBase}));
        // 将交易量控件的滑动事件传递给K线控件
        mMiddleChartViewBase.setOnChartGestureListener(
                new CoupleChartGestureListener(mMiddleChartViewBase, new Chart[]{mTopChartViewBase, mBottomChartViewBase}));
        mBottomChartViewBase.setOnChartGestureListener(
                new CoupleChartGestureListener(mBottomChartViewBase, new Chart[]{mTopChartViewBase, mMiddleChartViewBase}));

        //初始化柱子宽度
        float scaleX = (float) SPUtils.get(BaseApplication.getContext(), MarketConstants.SCALE_X, 2.4f);
        float[] topVals = new float[9];
        float[] middleVals = new float[9];
        float[] bottomVals = new float[9];
        Matrix topMatrix = mTopChartViewBase.getViewPortHandler().getMatrixTouch();
        Matrix middleMatrix = mMiddleChartViewBase.getViewPortHandler().getMatrixTouch();
        Matrix bottomMatrix = mBottomChartViewBase.getViewPortHandler().getMatrixTouch();
        topMatrix.getValues(topVals);
        middleMatrix.getValues(middleVals);
        bottomMatrix.getValues(bottomVals);
        topVals[Matrix.MSCALE_X] = scaleX;
        middleVals[Matrix.MSCALE_X] = scaleX;
        bottomVals[Matrix.MSCALE_X] = scaleX;
        topMatrix.setValues(topVals);
        middleMatrix.setValues(middleVals);
        bottomMatrix.setValues(bottomVals);
        mTopChartViewBase.getViewPortHandler().refresh(topMatrix, mTopChartViewBase, false);
        mMiddleChartViewBase.getViewPortHandler().refresh(middleMatrix, mMiddleChartViewBase, false);
        mBottomChartViewBase.getViewPortHandler().refresh(bottomMatrix, mBottomChartViewBase, false);
    }

    /**
     * date: 7/9/17
     * author: chenli
     * description: 载入K线数据
     */
    @Override
    protected void drawKline() {
        try {
            //开始加载数据
            if (mTopChartViewBase.getData() != null && mTopChartViewBase.getData().getDataSetCount() > 0) {
                CombinedData topCombinedData = mTopChartViewBase.getCombinedData();
                CandleData candleData = topCombinedData.getCandleData();
                CombinedData middleCombinedData = mMiddleChartViewBase.getCombinedData();
                LineData middleLineData = middleCombinedData.getLineData();
                BarData middleBarData = middleCombinedData.getBarData();
                CombinedData bottomCombinedData = mBottomChartViewBase.getCombinedData();
                LineData bottomLineData = bottomCombinedData.getLineData();
                BarData bottomBarData = bottomCombinedData.getBarData();

                String left_id_t = mChartEntity.getLeft_id();
                String right_id_t = mChartEntity.getRight_id();
                String last_id_t = mKlineEntity.getLast_id();
                int last_index_t = Integer.parseInt(last_id_t);
                if (last_index_t < 0) last_index_t = 0;
                int left_index_t = Integer.parseInt(left_id_t);
                if (left_index_t < 0) left_index_t = 0;
                int right_index_t = Integer.parseInt(right_id_t);
                if (right_index_t < 0) right_index_t = 0;
                Map<String, KlineEntity.DataEntity> dataEntities = mKlineEntity.getData();

                if (right_index_t == mRightIndex && left_index_t == mLeftIndex) {
                    KlineEntity.DataEntity dataEntity = dataEntities.get(last_id_t);
                    if (dataEntity == null) return;
                    LogUtils.e("单个柱子刷新", false);
                    candleData.removeEntry(mLastIndex - mBaseIndex, 0);
                    for (int i = 0; i < mLineData.getDataSetCount(); i++)
                        mLineData.removeEntry(mLastIndex - mBaseIndex, i);
                    middleLineData.removeEntry(mLastIndex - mBaseIndex, 0);
                    middleBarData.removeEntry(mLastIndex - mBaseIndex, 0);
                    bottomLineData.removeEntry(mLastIndex - mBaseIndex, 0);
                    bottomLineData.removeEntry(mLastIndex - mBaseIndex, 1);
                    bottomBarData.removeEntry(mLastIndex - mBaseIndex, 0);
                    generateCandleAndLineDataEntry(mLeftIndex, mLastIndex);
                    refreshLatestLine(dataEntity);
                } else if (right_index_t > mRightIndex && left_index_t > mLeftIndex) {
                    LogUtils.e("向后添加柱子", false);
                    for (int i = this.mRightIndex + 1; i <= right_index_t; i++) {
                        generateCandleAndLineDataEntry(mLeftIndex, i);
                    }
                    refreshLatestLine(dataEntities.get(right_id_t));
                } else if (left_index_t < mLeftIndex) {
                    LogUtils.e("向前添加柱子", false);
                    for (int i = this.mLeftIndex - 1; i >= left_index_t; i--) {
                        generateCandleAndLineDataEntry(left_index_t, i);
                    }
                    reGenerateMACDEntries(left_index_t, last_index_t);
                }
                this.mLastIndex = last_index_t;
                this.mRightIndex = right_index_t;
                this.mLeftIndex = left_index_t;

                if (!mIsLongPress) refreshChartLegend(mLastIndex - mBaseIndex);

                topCombinedData.notifyDataChanged();
                mTopChartViewBase.notifyDataSetChanged();
                mTopChartViewBase.getXAxis().setAxisMaximum(topCombinedData.getXMax() + 2.5f);
                mTopChartViewBase.getXAxis().setAxisMinimum(topCombinedData.getXMin() - 0.5f);
                mTopChartViewBase.invalidate();

                middleCombinedData.notifyDataChanged();
                mMiddleChartViewBase.notifyDataSetChanged();
                mMiddleChartViewBase.getXAxis().setAxisMaximum(middleCombinedData.getXMax() + 2.5f);
                mMiddleChartViewBase.getXAxis().setAxisMinimum(middleCombinedData.getXMin() - 0.5f);
                mMiddleChartViewBase.invalidate();

                bottomCombinedData.notifyDataChanged();
                mBottomChartViewBase.notifyDataSetChanged();
                mBottomChartViewBase.getXAxis().setAxisMaximum(bottomCombinedData.getXMax() + 2.5f);
                mBottomChartViewBase.getXAxis().setAxisMinimum(bottomCombinedData.getXMin() - 0.5f);
                mBottomChartViewBase.invalidate();
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
                if (mBaseIndex < 0) mBaseIndex = 0;
                mLeftIndex = Integer.parseInt(left_id_t);
                if (mLeftIndex < 0) mLeftIndex = 0;
                mRightIndex = Integer.parseInt(right_id_t);
                if (mRightIndex < 0) mRightIndex = 0;
                mLastIndex = Integer.parseInt(last_id_t);
                if (mLastIndex < 0) mLastIndex = 0;

                CombinedData topCombinedData = new CombinedData();
                List<CandleEntry> candleEntries = new ArrayList<>();
                CombinedData middleCombinedData = new CombinedData();
                List<Entry> oiEntries = new ArrayList<>();
                List<BarEntry> volumeEntries = new ArrayList<>();
                CombinedData bottomCombinedData = new CombinedData();
                List<Entry> difEntries = new ArrayList<>();
                List<Entry> deaEntries = new ArrayList<>();
                List<BarEntry> macdEntries = new ArrayList<>();
                for (int i = mLeftIndex; i <= mLastIndex; i++) {
                    KlineEntity.DataEntity dataEntity = dataEntities.get(String.valueOf(i));
                    if (dataEntity == null) continue;
                    List<Entry> entries = generateMultiDataEntry(i, dataEntity, mLeftIndex);
                    candleEntries.add((CandleEntry) entries.get(0));
                    oiEntries.add(entries.get(1));
                    volumeEntries.add((BarEntry) entries.get(2));
                    difEntries.add(entries.get(3));
                    deaEntries.add(entries.get(4));
                    macdEntries.add((BarEntry) entries.get(5));
                }

                CandleData candleData = generateCandleData(candleEntries);
                topCombinedData.setData(candleData);
                mLineData = generateMALineData();
                if (mIsAverage) topCombinedData.setData(mLineData);
                else topCombinedData.setData(new LineData());

                LineDataSet oiDataSet = generateLineDataSet(oiEntries, mOIColor,
                        "OI", false, YAxis.AxisDependency.RIGHT);
                BarDataSet volumeDataSet = generateBarDataSet(volumeEntries, "Volume", true,
                        mDecreasingColor, mIncreasingColor);
                LineData oiData = new LineData(oiDataSet);
                BarData volumeData = new BarData(volumeDataSet);
                middleCombinedData.setData(oiData);
                middleCombinedData.setData(volumeData);

                LineDataSet difDataSet = generateLineDataSet(difEntries, mDiffColor,
                        "difChart", false, YAxis.AxisDependency.LEFT);
                LineDataSet deaDataSet = generateLineDataSet(deaEntries, mDeaColor,
                        "deaChart", false, YAxis.AxisDependency.LEFT);
                LineData bottomLineData = new LineData(difDataSet, deaDataSet);
                BarDataSet macdDataSet = generateBarDataSet(macdEntries,
                        "macdChart", true, mIncreasingColor, mDecreasingColor);
                BarData bottomBarData = new BarData(macdDataSet);
                bottomBarData.setBarWidth(0.01f);
                bottomCombinedData.setData(bottomLineData);
                bottomCombinedData.setData(bottomBarData);

                refreshChartLegend(mLastIndex - mBaseIndex);

                mTopChartViewBase.setData(topCombinedData);//当前屏幕会显示所有的数据
                mTopChartViewBase.getXAxis().setAxisMaximum(topCombinedData.getXMax() + 2.5f);
                mTopChartViewBase.getXAxis().setAxisMinimum(topCombinedData.getXMin() - 0.5f);
                mTopChartViewBase.setVisibleXRangeMinimum(4);
                generateLatestLine(dataEntities.get(right_id_t));
                mTopChartViewBase.moveViewToX(mLastIndex - mBaseIndex);

                mMiddleChartViewBase.setData(middleCombinedData);//当前屏幕会显示所有的数据
                mMiddleChartViewBase.getXAxis().setAxisMaximum(middleCombinedData.getXMax() + 2.5f);
                mMiddleChartViewBase.getXAxis().setAxisMinimum(middleCombinedData.getXMin() - 0.5f);
                mMiddleChartViewBase.setVisibleXRangeMinimum(4);
                mMiddleChartViewBase.moveViewToX(mLastIndex - mBaseIndex);

                mBottomChartViewBase.setData(bottomCombinedData);//当前屏幕会显示所有的数据
                mBottomChartViewBase.getXAxis().setAxisMaximum(bottomCombinedData.getXMax() + 2.5f);
                mBottomChartViewBase.getXAxis().setAxisMinimum(bottomCombinedData.getXMin() - 0.5f);
                mBottomChartViewBase.setVisibleXRangeMinimum(4);
                mBottomChartViewBase.moveViewToX(mLastIndex - mBaseIndex);

                if (mIsPosition) addPositionLimitLines();
                if (mIsPending) addOrderLimitLines();

                LogUtils.e("初始化完成", true);
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
        removeLatestLine();
        removeOrderLimitLines();
        removePositionLimitLines();
        xVals.clear();
        mTopChartViewBase.clear();
        mMiddleChartViewBase.clear();
        mBottomChartViewBase.clear();
        mViewWidth = MarketConstants.VIEW_WIDTH;
    }

    /**
     * date: 2019/7/30
     * author: chenli
     * description: 重新计算MACD
     */
    private void reGenerateMACDEntries(int left_index, int last_index) {
        mBottomChartViewBase.getLineData().getDataSetByIndex(0).clear();
        mBottomChartViewBase.getLineData().getDataSetByIndex(1).clear();
        mBottomChartViewBase.getBarData().getDataSetByIndex(0).clear();

        for (int i = left_index; i <= last_index; i++) {
            Map<String, KlineEntity.DataEntity> dataEntities = mKlineEntity.getData();
            KlineEntity.DataEntity dataEntity = dataEntities.get(String.valueOf(i));
            if (dataEntity == null) continue;
            float close = Float.valueOf(dataEntity.getClose());
            float emas = close;
            float emal = close;
            float dif = 0;
            float dea = 0;
            float macd = 0;
            if (i != left_index && mEMAs.get(i - mBaseIndex - 1) != null) {
                emas = mEMAs.get(i - mBaseIndex - 1) * (mS - 1) / (mS + 1) + close * 2 / (mS + 1);
                emal = mEMAl.get(i - mBaseIndex - 1) * (mL - 1) / (mL + 1) + close * 2 / (mL + 1);
                dif = emas - emal;
                dea = mDEA.get(i - mBaseIndex - 1) * (mN - 1) / (mN + 1) + dif * 2 / (mN + 1);
                macd = (dif - dea) * 2;
            }
            mEMAs.put(i - mBaseIndex, emas);
            mEMAl.put(i - mBaseIndex, emal);
            mDEA.put(i - mBaseIndex, dea);
            mBottomChartViewBase.getLineData().getDataSetByIndex(0).addEntry(new Entry(i - mBaseIndex, dif));
            mBottomChartViewBase.getLineData().getDataSetByIndex(1).addEntry(new Entry(i - mBaseIndex, dea));
            mBottomChartViewBase.getBarData().getDataSetByIndex(0).addEntry(new BarEntry(i - mBaseIndex, macd, macd));
        }

    }

    /**
     * date: 6/1/18
     * author: chenli
     * description: K线图刷新时生成单个数据
     */
    private void generateCandleAndLineDataEntry(int left_index, int index) {
        Map<String, KlineEntity.DataEntity> dataEntities = mKlineEntity.getData();
        KlineEntity.DataEntity dataEntity = dataEntities.get(String.valueOf(index));
        if (dataEntity == null) return;
        mCalendar.setTimeInMillis(Long.valueOf(dataEntity.getDatetime()) / 1000000);
        xVals.put(index - mBaseIndex, mSimpleDateFormat.format(mCalendar.getTime()));

        List<Entry> entries = generateMultiDataEntry(index, dataEntity, left_index);
        mTopChartViewBase.getCandleData().getDataSetByIndex(0).addEntryOrdered((CandleEntry) entries.get(0));
        mMiddleChartViewBase.getLineData().getDataSetByIndex(0).addEntryOrdered(entries.get(1));
        mMiddleChartViewBase.getBarData().getDataSetByIndex(0).addEntryOrdered((BarEntry) entries.get(2));
        mBottomChartViewBase.getLineData().getDataSetByIndex(0).addEntryOrdered(entries.get(3));
        mBottomChartViewBase.getLineData().getDataSetByIndex(1).addEntryOrdered(entries.get(4));
        mBottomChartViewBase.getBarData().getDataSetByIndex(0).addEntryOrdered((BarEntry) entries.get(5));

        for (int i = 0; i < mas.size(); i++) {
            int para = mas.get(i);
            if (index >= left_index + para - 1) {
                Entry entry = generateMALineDataEntry(index, para - 1);
                mLineData.getDataSetByIndex(i).addEntryOrdered(entry);
            }
        }
    }

    /**
     * date: 6/1/18
     * author: chenli
     * description: K线图初始化时生成单个数据
     */
    private List<Entry> generateMultiDataEntry(int i, KlineEntity.DataEntity dataEntity, int left_index) {
        List<Entry> entries = new ArrayList<>();
        mCalendar.setTimeInMillis(Long.valueOf(dataEntity.getDatetime()) / 1000000);
        xVals.put(i - mBaseIndex, mSimpleDateFormat.format(mCalendar.getTime()));
        float high = Float.valueOf(dataEntity.getHigh());
        float low = Float.valueOf(dataEntity.getLow());
        float open = Float.valueOf(dataEntity.getOpen());
        float close = Float.valueOf(dataEntity.getClose());
        float volume = Float.valueOf(dataEntity.getVolume());
        float oi = Float.valueOf(dataEntity.getClose_oi());
        float sub = open - close;
        float emas = close;
        float emal = close;
        float dif = 0;
        float dea = 0;
        float macd = 0;
        if (i != left_index && mEMAs.get(i - mBaseIndex - 1) != null) {
            emas = mEMAs.get(i - mBaseIndex - 1) * (mS - 1) / (mS + 1) + close * 2 / (mS + 1);
            emal = mEMAl.get(i - mBaseIndex - 1) * (mL - 1) / (mL + 1) + close * 2 / (mL + 1);
            dif = emas - emal;
            dea = mDEA.get(i - mBaseIndex - 1) * (mN - 1) / (mN + 1) + dif * 2 / (mN + 1);
            macd = (dif - dea) * 2;
        }
        mEMAs.put(i - mBaseIndex, emas);
        mEMAl.put(i - mBaseIndex, emal);
        mDEA.put(i - mBaseIndex, dea);
        entries.add(new CandleEntry(i - mBaseIndex, high, low, open, close));
        entries.add(new Entry(i - mBaseIndex, oi));
        entries.add(new BarEntry(i - mBaseIndex, volume, sub));
        entries.add(new Entry(i - mBaseIndex, dif));
        entries.add(new Entry(i - mBaseIndex, dea));
        entries.add(new BarEntry(i - mBaseIndex, macd, macd));
        return entries;
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
        set.setDecreasingColor(mDecreasingColor);
        set.setDecreasingPaintStyle(Paint.Style.FILL);
        set.setIncreasingColor(mIncreasingColor);
        set.setIncreasingPaintStyle(Paint.Style.STROKE);
        set.setNeutralColor(mHighlightColor);
        set.setShadowColorSameAsCandle(true);
        set.setHighlightLineWidth(0.7f);
        set.setHighLightColor(mHighlightColor);
        set.setDrawValues(true);
        set.setValueTextColor(Color.RED);
        set.setValueTextSize(9f);
        set.setDrawIcons(false);
        set.setValueFormatter(new TopChartLeftYAxisValueFormatter());
        CandleData candleData = new CandleData();
        candleData.addDataSet(set);
        return candleData;
    }

    /**
     * date: 7/9/17
     * author: chenli
     * description: 生成均线数据
     */
    private LineData generateMALineData() {
        List<ILineDataSet> dataSets = new ArrayList<>();
        for (int i = 0; i < mas.size(); i++) {
            int ma = mas.get(i);
            int color = mColorMas[i];
            LineDataSet set = generateMALineDataSet(ma, color, "MA" + ma);
            dataSets.add(set);
        }
        return new LineData(dataSets);
    }

    private LineDataSet generateMALineDataSet(int para, int color, String label) {
        List<Entry> entries = new ArrayList<>();
        for (int i = mLeftIndex + para - 1; i <= mLastIndex; i++) {
            Entry entry = generateMALineDataEntry(i, para - 1);
            entries.add(entry);
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

    /**
     * date: 2019/1/20
     * author: chenli
     * description: 均线初始化时生成单个数据
     */
    private Entry generateMALineDataEntry(int i, int lineIndex) {
        float sum = getSum(i - lineIndex, i) / (lineIndex + 1);
        return new Entry(i - mBaseIndex, sum);
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
            if (dataEntity == null) continue;
            try {
                sum += Float.parseFloat(dataEntity.getClose());
            } catch (NumberFormatException ex) {
                ex.printStackTrace();
            }
        }
        return sum;
    }

    /**
     * date: 2019/2/22
     * author: chenli
     * description: 产生持仓量数据集
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
            set.setHighlightLineWidth(0.7f);
            set.setHighLightColor(color);
        } else {
            set.setHighlightEnabled(false);
        }
        return set;
    }


    /**
     * date: 2019/2/22
     * author: chenli
     * description: 生成成交量数据集
     */
    private BarDataSet generateBarDataSet(List<BarEntry> entries, String label, boolean isHighlight, int... colors) {
        BarDataSet set = new BarDataSet(entries, label);
        set.setColors(colors);
        set.setBarBorderWidth(0);
        set.setDrawValues(false);
        set.setAxisDependency(YAxis.AxisDependency.LEFT);
        if (isHighlight) {
            set.setHighLightColor(mHighlightColor);
        } else {
            set.setHighlightEnabled(false);
        }
        return set;
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
            limitLine.setLineColor(ContextCompat.getColor(getActivity(), R.color.kline_last));
            limitLine.setLabelPosition(LimitLine.LimitLabelPosition.RIGHT_BOTTOM);
            limitLine.setTextSize(10f);
            limitLine.setTextColor(ContextCompat.getColor(getActivity(), R.color.kline_last));
            mTopChartViewBase.getAxisLeft().addLimitLine(limitLine);
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
                mTopChartViewBase.getAxisLeft().removeLimitLine(limitLine);
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
            mTopChartViewBase.getAxisLeft().removeLimitLine(limitLine);
            mLatestLimitLines.remove("latest");
        }
    }

    /**
     * date: 2019/7/31
     * author: chenli
     * description: 刷新图标值
     */
    @Override
    protected void refreshChartLegend(int index) {
        refreshTopChartLegend(index);
        super.refreshChartLegend(index);
    }

    /**
     * date: 2019/7/31
     * author: chenli
     * description: 刷新均线图标值
     */
    private void refreshTopChartLegend(int index) {
        List<LegendEntry> legendEntries = new ArrayList<>();
        for (int i = 0; i < mas.size(); i++) {
            int para = mas.get(i);
            Entry entry = mLineData.getDataSetByIndex(i).getEntryForXValue(index, 0.0f);
            String data = ":";
            if (entry != null) data = data + MathUtils.round(entry.getY()+"", 2);
            LegendEntry ma = new LegendEntry("MA" + para + data, Legend.LegendForm.NONE,
                    NaN, NaN, null, mColorMas[i]);
            legendEntries.add(ma);
        }
        Legend legend = mTopChartViewBase.getLegend();
        legend.setCustom(legendEntries);
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.RIGHT);
    }

    /**
     * date: 2018/12/18
     * author: chenli
     * description: 不改页情况下k线周期更新
     */
    @Subscribe
    public void onEvent(SwitchDurationEvent switchDurationEvent) {
        String klineType = switchDurationEvent.getKlineType();
        mKlineType = klineType;
        mXValsFormat = switchDurationEvent.getxValuesType();
        mFragmentType = switchDurationEvent.getFragmentType();
        mSimpleDateFormat = new SimpleDateFormat(mXValsFormat, Locale.CHINA);
        clearKline();
        BaseApplication.getmMDWebSocket().sendSetChartKline(instrument_id, VIEW_WIDTH, mKlineType);
        mKlineMarkerView.refreshFragmentType();

    }

    /**
     * date: 7/9/17
     * author: chenli
     * description: 接收自选合约列表弹出框、持仓页、上下滑动传过来的合约代码，以便更新K线图
     */
    @Subscribe
    public void onEvent(SwitchInsEvent data) {
        String instrument_id_new = data.getInstrument_id();
        if (instrument_id_new.equals(instrument_id)) return;
        setInstrument_id(instrument_id_new);
        clearKline();
        MainActivity mainActivity = (MainActivity) getActivity();
        MainActivityPresenter mainActivityPresenter = mainActivity.getmMainActivityPresenter();
        FutureInfoFragment futureInfoFragment = (FutureInfoFragment) mainActivityPresenter.getmViewPagerFragmentAdapter().getItem(2);
        int currentIndex = futureInfoFragment.getmBinding().vpKlineContent.getCurrentItem();
        if (currentIndex == 1) {
            sendSubscribeQuote(instrument_id);
            BaseApplication.getmMDWebSocket().sendSetChartKline(instrument_id, VIEW_WIDTH, mKlineType);
        }

    }

    /**
     * date: 7/9/17
     * author: chenli
     * description: 接收“设置”按钮最新的设置信息，以便根据用户要求显示持仓、挂单、均线
     */
    @Subscribe
    public void onEventMainThread(CommonSwitchEvent data) {
        //k线还没初始化
        if (mTopChartViewBase.getCombinedData() == null) return;
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

        if (mIsAverage != data.isAverage()) {
            mIsAverage = data.isAverage();
            if (mIsAverage) {
                mTopChartViewBase.getCombinedData().setData(mLineData);
                mTopChartViewBase.getLegend().setEnabled(true);
            } else {
                mTopChartViewBase.getCombinedData().setData(new LineData());
                mTopChartViewBase.getLegend().setEnabled(false);
            }
        }

        mTopChartViewBase.getCombinedData().notifyDataChanged();
        mTopChartViewBase.invalidate();
    }

    /**
     * date: 2019/5/12
     * author: chenli
     * description: 重绘均线
     */
    @Subscribe
    public void onEvent(ParaChangeEvent paraChangeEvent) {
        String average = (String) SPUtils.get(BaseApplication.getContext(),
                SettingConstants.CONFIG_PARA_MA, SettingConstants.PARA_MA);
        String[] averageArray = average.split(",");
        List<String> list = new ArrayList<>();
        for (String para : averageArray) {
            if (!"0".equals(para)) list.add(para);
        }
        String averageF = TextUtils.join(",", list);
        String averagePre = TextUtils.join(",", mas);
        //顺序不对也重画
        if (!averageF.equals(averagePre)) {
            mas.clear();
            for (String para : list) {
                try {
                    int ma = Integer.parseInt(para);
                    mas.add(ma);
                } catch (Exception e) {
                    continue;
                }
            }

            refreshChartLegend(mLastIndex - mBaseIndex);
            clearKline();
            drawKline();
        }
    }

    @Override
    public void onPause() {
        //moveTo方法的bug
        MoveViewJob.getInstance(null, 0, 0, null, null);
        super.onPause();
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
        private TextView dateTime;
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
            dateTime = findViewById(R.id.datetime);
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
            simpleDateFormat1 = new SimpleDateFormat("yyyyMMdd", Locale.CHINA);
            calendar = Calendar.getInstance();
            refreshFragmentType();
        }

        public void refreshFragmentType() {
            if (DAY_FRAGMENT.equals(mFragmentType)) {
                dateTime.setVisibility(GONE);
                simpleDateFormat = new SimpleDateFormat("yyyyMMdd", Locale.CHINA);
            } else {
                dateTime.setVisibility(VISIBLE);
                simpleDateFormat = new SimpleDateFormat("HH:mm:ss", Locale.CHINA);
            }
        }

        @Override
        public void refreshContent(Entry e, Highlight highlight) {
            if (e instanceof CandleEntry) {
                Map<String, KlineEntity.DataEntity> dataEntities = mKlineEntity.getData();
                CandleEntry candleEntry = (CandleEntry) e;
                refreshChartLegend((int) e.getX());
                String xValue = MathUtils.round(String.valueOf(candleEntry.getX() + mBaseIndex), 0);
                KlineEntity.DataEntity dataEntity = dataEntities.get(xValue);
                String xValuePre = MathUtils.subtract(xValue, "1");
                KlineEntity.DataEntity dataEntityPre = dataEntities.get(xValuePre);
                if (dataEntity != null) {
                    calendar.setTimeInMillis(Long.valueOf(dataEntity.getDatetime()) / 1000000);
                    String time = simpleDateFormat.format(calendar.getTime());
                    String open = LatestFileManager.saveScaleByPtick(dataEntity.getOpen(), instrument_id);
                    String high = LatestFileManager.saveScaleByPtick(dataEntity.getHigh(), instrument_id);
                    String low = LatestFileManager.saveScaleByPtick(dataEntity.getLow(), instrument_id);
                    String close = LatestFileManager.saveScaleByPtick(dataEntity.getClose(), instrument_id);
                    String volume = dataEntity.getVolume();
                    String closeOi = dataEntity.getClose_oi();
                    String closeOiDelta = closeOi;
                    String closePre = "0";
                    String changePercent = "-";
                    if (dataEntityPre != null) {
                        closePre = LatestFileManager.saveScaleByPtick(dataEntityPre.getClose(), instrument_id);
                        closeOiDelta = MathUtils.subtract(closeOi, dataEntityPre.getClose_oi());
                    }
                    String change = MathUtils.subtract(close, closePre);
                    if (!"0".equals(closePre)) {
                        changePercent = MathUtils.round(MathUtils.multiply(
                                MathUtils.divide(change, closePre), "100"), 2) + "%";
                    }

                    this.yValue.setText(KlineFragment.this.yValue);
                    if (this.dateTime.getVisibility() == VISIBLE) {
                        String date = simpleDateFormat1.format(calendar.getTime());
                        this.dateTime.setText(date);
                    }
                    this.xValue.setText(time);
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

                        if (open_float < closePre_float)
                            this.open.setTextColor(ContextCompat.getColor(getActivity(), R.color.text_green));
                        else
                            this.open.setTextColor(ContextCompat.getColor(getActivity(), R.color.text_red));

                        if (high_float < closePre_float)
                            this.high.setTextColor(ContextCompat.getColor(getActivity(), R.color.text_green));
                        else
                            this.high.setTextColor(ContextCompat.getColor(getActivity(), R.color.text_red));

                        if (low_float < closePre_float)
                            this.low.setTextColor(ContextCompat.getColor(getActivity(), R.color.text_green));
                        else
                            this.low.setTextColor(ContextCompat.getColor(getActivity(), R.color.text_red));

                        if (close_float < closePre_float)
                            this.close.setTextColor(ContextCompat.getColor(getActivity(), R.color.text_green));
                        else
                            this.close.setTextColor(ContextCompat.getColor(getActivity(), R.color.text_red));

                        float close_change_float = Float.parseFloat(change);
                        if (close_change_float < 0) {
                            this.closeChange.setTextColor(ContextCompat.getColor(getActivity(), R.color.text_green));
                            this.closeChangePercent.setTextColor(ContextCompat.getColor(getActivity(), R.color.text_green));
                        } else {
                            this.closeChange.setTextColor(ContextCompat.getColor(getActivity(), R.color.text_red));
                            this.closeChangePercent.setTextColor(ContextCompat.getColor(getActivity(), R.color.text_red));
                        }

                        int close_oi_delta = Integer.parseInt(closeOiDelta);
                        if (close_oi_delta < 0)
                            this.closeOiDelta.setTextColor(ContextCompat.getColor(getActivity(), R.color.text_green));
                        else
                            this.closeOiDelta.setTextColor(ContextCompat.getColor(getActivity(), R.color.text_red));

                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }
            super.refreshContent(e, highlight);
        }

        @Override
        public void draw(Canvas canvas, float posX, float posY) {
            // translate to the correct position and draw
            float deadlineRight = mTopChartViewBase.getViewPortHandler().contentRight() - getWidth();
            float deadlineLeft = mTopChartViewBase.getViewPortHandler().contentLeft() + getWidth();
            if (posX <= deadlineLeft) {
                canvas.translate(deadlineRight, mTopChartViewBase.getViewPortHandler().contentTop());
                markViewState = "right";
            } else if (posX >= deadlineRight) {
                canvas.translate(mTopChartViewBase.getViewPortHandler().contentLeft(), mTopChartViewBase.getViewPortHandler().contentTop());
                markViewState = "left";
            } else {
                if (markViewState.equals("right"))
                    canvas.translate(deadlineRight, mTopChartViewBase.getViewPortHandler().contentTop());
                if (markViewState.equals("left"))
                    canvas.translate(mTopChartViewBase.getViewPortHandler().contentLeft(), mTopChartViewBase.getViewPortHandler().contentTop());
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
    public class MyXAxisValueFormatter extends MyValueFormatter {

        private SparseArray<String> mValues;

        private MyXAxisValueFormatter(SparseArray<String> values) {
            this.mValues = values;
        }

        @Override
        public String getFormattedValue(float value) {
            return mValues.get((int) value);
        }
    }
}

package com.shinnytech.futures.controller.fragment;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.SparseArray;
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
import com.github.mikephil.charting.data.CombinedData;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.listener.ChartTouchListener;
import com.github.mikephil.charting.listener.OnChartGestureListener;
import com.shinnytech.futures.R;
import com.shinnytech.futures.application.BaseApplication;
import com.shinnytech.futures.controller.activity.FutureInfoActivity;
import com.shinnytech.futures.model.bean.eventbusbean.IdEvent;
import com.shinnytech.futures.model.bean.eventbusbean.SetUpEvent;
import com.shinnytech.futures.model.bean.futureinfobean.ChartEntity;
import com.shinnytech.futures.model.bean.futureinfobean.KlineEntity;
import com.shinnytech.futures.model.bean.futureinfobean.QuoteEntity;
import com.shinnytech.futures.model.bean.searchinfobean.SearchEntity;
import com.shinnytech.futures.model.engine.LatestFileManager;
import com.shinnytech.futures.utils.LogUtils;
import com.shinnytech.futures.utils.MathUtils;
import com.shinnytech.futures.view.custommpchart.mycomponent.MyMarkerView;
import com.shinnytech.futures.view.custommpchart.mycomponent.MyXAxis;
import com.shinnytech.futures.view.custommpchart.mycomponent.MyYAxis;

import org.greenrobot.eventbus.Subscribe;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static com.shinnytech.futures.constants.CommonConstants.CHART_ID;
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
            preSettlement = Float.parseFloat(quoteEntity.getPre_settlement());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    protected void initData() {
        super.initData();
        mColorOneMinuteChart = ContextCompat.getColor(getActivity(), R.color.kline_one_minute);
        mColorAverageChart = ContextCompat.getColor(getActivity(), R.color.kline_average);
        mSimpleDateFormat = new SimpleDateFormat("HH:mm", Locale.CHINA);
        mFragmentType = CURRENT_DAY_FRAGMENT;
        mKlineType = CURRENT_DAY;
    }

    @Override
    protected void initChart() {
        super.initChart();

        mChart.setScaleEnabled(false);
        CurrentDayMarkerView marker = new CurrentDayMarkerView(getActivity());
        marker.setChartView(mChart);
        mChart.setMarker(marker);

        MyXAxis xAxis = (MyXAxis) mChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawLabels(true);
        xAxis.setDrawGridLines(true);
        xAxis.setDrawAxisLine(false);
        xAxis.setGridColor(mColorGrid);
        xAxis.setTextColor(mColorText);
        xAxis.setGranularityEnabled(true);
        xAxis.setGranularity(1);

        MyYAxis leftAxis = (MyYAxis) mChart.getAxisLeft();
        leftAxis.setPosition(YAxis.YAxisLabelPosition.INSIDE_CHART);
        leftAxis.setDrawGridLines(true);
        leftAxis.setDrawAxisLine(false);
        leftAxis.setDrawLabels(true);
        leftAxis.setLabelCount(3, true);
        leftAxis.enableGridDashedLine(3, 6, 0);
        leftAxis.setGridColor(mColorGrid);
        leftAxis.setTextColor(mColorText);
        leftAxis.setValueFormatter(new MyLeftYAxisValueFormatter());

        MyYAxis rightAxis = (MyYAxis) mChart.getAxisRight();
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
        Legend legend = mChart.getLegend();
        legend.setCustom(legendEntries);
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.RIGHT);
        legend.setTextColor(Color.WHITE);

        mChart.setOnChartGestureListener(new OnChartGestureListener() {
            @Override
            public void onChartGestureStart(MotionEvent me, ChartTouchListener.ChartGesture lastPerformedGesture) {
            }

            @Override
            public void onChartGestureEnd(MotionEvent me, ChartTouchListener.ChartGesture lastPerformedGesture) {
            }

            @Override
            public void onChartLongPressed(MotionEvent me) {
            }

            @Override
            public void onChartDoubleTapped(MotionEvent me) {
                float tappedX = me.getX();
                float tappedY = me.getY();
                Highlight highlight = mChart.getHighlightByTouchPoint(tappedX, tappedY);
                mChart.highlightValue(highlight, false);
            }

            @Override
            public void onChartSingleTapped(MotionEvent me) {
                mChart.highlightValues(null);
            }

            @Override
            public void onChartFling(MotionEvent me1, MotionEvent me2, float velocityX, float velocityY) {
            }

            @Override
            public void onChartScale(MotionEvent me, float scaleX, float scaleY) {
            }

            @Override
            public void onChartTranslate(MotionEvent me, float dX, float dY) {
            }
        });
    }

    /**
     * date: 7/9/17
     * author: chenli
     * description: 载入数据
     */
    @Override
    protected void refreshKline() {
        try {
            Map<String, KlineEntity> klineEntities = sDataManager.getRtnData().getKlines().get(instrument_id);
            QuoteEntity quoteEntity = sDataManager.getRtnData().getQuotes().get(instrument_id);
            if (klineEntities == null || quoteEntity == null) return;
            KlineEntity klineEntity = klineEntities.get(mKlineType);
            if (klineEntity == null) return;
            String last_id = klineEntity.getLast_id();
            mDataEntities = klineEntity.getData();
            preSettlement = Float.parseFloat(quoteEntity.getPre_settlement());
            if (last_id == null || "-1".equals(last_id) || mDataEntities.isEmpty()) return;

            int last_index_t = Integer.parseInt(last_id);
            //开始加载数据
            if (mChart.getData() != null && mChart.getData().getDataSetCount() > 0) {
                LineData lineData = mChart.getLineData();
                CombinedData combinedData = mChart.getCombinedData();

                if (last_index_t == mLastIndex) {
                    LogUtils.e("分时图刷新", false);
                    KlineEntity.DataEntity dataEntity = mDataEntities.get(last_id);
                    if (dataEntity == null) return;
                    lineData.removeEntry(last_index_t, 0);
                    lineData.removeEntry(last_index_t, 1);
                    mSumVolume -= mSumVolumeMap.get(last_index_t);
                    mSumCV -= mSumCVMap.get(last_index_t);
                    generateLineDataEntry(last_index_t, lineData, dataEntity);
                } else {
                    LogUtils.e("分时图加载多个数据", false);
                    for (int i = mLastIndex + 1; i <= last_index_t; i++) {
                        KlineEntity.DataEntity dataEntity = mDataEntities.get(i + "");
                        if (dataEntity == null) continue;
                        generateXAxisLabel(i, dataEntity);
                        generateLineDataEntry(last_index_t, lineData, dataEntity);
                    }
                    mLastIndex = last_index_t;
                }
                refreshYAxisRange(lineData.getDataSetByIndex(0));
                combinedData.notifyDataChanged();
                mChart.notifyDataSetChanged();
                ((MyXAxis) mChart.getXAxis()).setXLabels(mStringSparseArray);
                mChart.invalidate();
            } else {
                LogUtils.e("分时图初始化", true);
                String trading_day_start_id = klineEntity.getTrading_day_start_id();
                String trading_day_end_id = klineEntity.getTrading_day_end_id();
                if (trading_day_start_id == null || trading_day_end_id == null
                        || "-1".equals(trading_day_start_id) || "-1".equals(trading_day_end_id))
                    return;
                mTradingDayStartIndex = Integer.parseInt(trading_day_start_id);
                mTradingDayEndIndex = Integer.parseInt(trading_day_end_id);
                mLastIndex = last_index_t;
                ((MyYAxis) mChart.getAxisLeft()).setBaseValue(preSettlement);
                ((MyYAxis) mChart.getAxisRight()).setBaseValue(preSettlement);
                List<Entry> oneMinuteChart = new ArrayList<>();
                List<Entry> averageChart = new ArrayList<>();
                CombinedData combinedData = new CombinedData();
                for (int index = mTradingDayStartIndex; index <= mLastIndex; index++) {
                    KlineEntity.DataEntity dataEntity = mDataEntities.get(String.valueOf(index));
                    if (dataEntity == null) continue;
                    generateXAxisLabel(index, dataEntity);
                    generateLineDataEntry(oneMinuteChart, averageChart, index, dataEntity);
                }
                LineData lineData = generateMultiLineData(
                        generateLineDataSet(oneMinuteChart, mColorOneMinuteChart, "oneMinuteChart"),
                        generateLineDataSet(averageChart, mColorAverageChart, "averageChart"));

                combinedData.setData(lineData);
                mChart.setData(combinedData);//当前屏幕会显示所有的数据
                mChart.setVisibleXRangeMinimum(mTradingDayEndIndex - mTradingDayStartIndex);
                ((MyXAxis) mChart.getXAxis()).setXLabels(mStringSparseArray);
                mChart.invalidate();
                int height = (int) mChart.getViewPortHandler().contentHeight();
                int width = (int) (mChart.getViewPortHandler().contentWidth() / 5);
                ((CurrentDayMarkerView)mChart.getMarker()).resize(width, height);
            }
        } catch (Exception ex) {
            ByteArrayOutputStream error = new ByteArrayOutputStream();
            ex.printStackTrace(new PrintStream(error));
            String exception = error.toString();
            LogUtils.e(exception, true);
        }
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
     * description: 分时图刷新时添加数据
     */
    private void generateLineDataEntry(int last_index, LineData lineData, KlineEntity.DataEntity dataEntity) {
        String close = dataEntity.getClose();
        Float closeF = Float.valueOf(close);
        String volume = dataEntity.getVolume();
        Float volumeF = Float.valueOf(volume);
        Float cv = volumeF * closeF;
        mSumVolume += volumeF;
        mSumVolumeMap.put(last_index, volumeF);
        mSumCV += cv;
        mSumCVMap.put(last_index, cv);
        float average = mSumVolume != 0 ? mSumCV / mSumVolume : 0;
        lineData.addEntry(new Entry(last_index, closeF), 0);
        lineData.addEntry(new Entry(last_index, average), 1);
    }

    /**
     * date: 6/1/18
     * author: chenli
     * description: 分时图初始化时添加数据
     */
    private void generateLineDataEntry(List<Entry> oneMinuteChart, List<Entry> averageChart, int index, KlineEntity.DataEntity dataEntity) {
        String close = dataEntity.getClose();
        Float closeF = Float.valueOf(close);
        String volume = dataEntity.getVolume();
        Float volumeF = Float.valueOf(volume);
        Float cv = volumeF * closeF;
        mSumVolume += volumeF;
        mSumVolumeMap.put(index, volumeF);
        mSumCV += cv;
        mSumCVMap.put(index, cv);
        float average = mSumVolume != 0 ? mSumCV / mSumVolume : 0;
        averageChart.add(new Entry(index, average));
        oneMinuteChart.add(new Entry(index, closeF));
    }

    /**
     * date: 6/1/18
     * author: chenli
     * description: 初始化时产生分时图数据集
     */
    private LineDataSet generateLineDataSet(List<Entry> entries, int color, String label) {
        LineDataSet set = new LineDataSet(entries, label);
        set.setColor(color);
        set.setLineWidth(0.7f);
        set.setDrawCircles(false);
        set.setDrawCircleHole(false);
        set.setDrawValues(false);
        set.setAxisDependency(YAxis.AxisDependency.LEFT);
        return set;
    }

    /**
     * date: 6/1/18
     * author: chenli
     * description: 初始化时产生分时图数据
     */
    private LineData generateMultiLineData(LineDataSet... lineDataSets) {
        List<ILineDataSet> dataSets = new ArrayList<>();
        for (int i = 0; i < lineDataSets.length; i++) {
            dataSets.add(lineDataSets[i]);
            if (i == 0) {
                refreshYAxisRange(lineDataSets[i]);
                lineDataSets[i].setHighlightLineWidth(0.7f);
                lineDataSets[i].setHighLightColor(ContextCompat.getColor(getActivity(), R.color.white));
            } else {
                lineDataSets[i].setHighlightEnabled(false);
            }
        }
        LineData lineData = new LineData(dataSets);
        return lineData;
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
            mChart.getAxisLeft().setAxisMinimum(preSettlement - lowDel);
            mChart.getAxisRight().setAxisMinimum(preSettlement - lowDel);
            mChart.getAxisLeft().setAxisMaximum(preSettlement + lowDel);
            mChart.getAxisRight().setAxisMaximum(preSettlement + lowDel);
        } else {
            mChart.getAxisLeft().setAxisMinimum(preSettlement - highDel);
            mChart.getAxisRight().setAxisMinimum(preSettlement - highDel);
            mChart.getAxisLeft().setAxisMaximum(preSettlement + highDel);
            mChart.getAxisRight().setAxisMaximum(preSettlement + highDel);
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
        preSettlement = 1;
        xVals.clear();
        mStringSparseArray.clear();
        removeOrderLimitLines();
        removePositionLimitLines();
        mChart.clear();
        mSumVolume = 0.0f;
        mSumCV = 0.0f;
        if (BaseApplication.getWebSocketService() != null)
            BaseApplication.getWebSocketService().sendSetChart(instrument_id);

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
     * description: 接收“设置”按钮最新的设置信息，以便更具用户要求显示持仓、挂单、均线
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

        mChart.invalidate();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
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
        private SimpleDateFormat simpleDateFormat;
        private Calendar calendar;

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
            simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA);
            calendar = Calendar.getInstance();
        }

        @Override
        public void refreshContent(Entry e, Highlight highlight) {
            int x = (int) e.getX();
            int index = x - 1;
            KlineEntity.DataEntity dataEntity = mDataEntities.get(String.valueOf(x));
            KlineEntity.DataEntity dataEntityPre = mDataEntities.get(String.valueOf(index >= mTradingDayStartIndex ? index : mTradingDayStartIndex));
            if (dataEntity != null && dataEntityPre != null) {
                calendar.setTimeInMillis(Long.valueOf(dataEntity.getDatetime()) / 1000000);
                String time = simpleDateFormat.format(calendar.getTime());
                String xValue = xVals.get(x);
                String price = LatestFileManager.saveScaleByPtick(dataEntity.getClose(), instrument_id);
                Entry averageEntry = mChart.getLineData().getDataSets().get(1).getEntryForXValue(e.getX(), e.getY());
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
                this.yValue.setText(time);
                this.xValue.setText(xValue);
                this.price.setText(price);
                this.average.setText(average);
                this.change.setText(change);
                this.changePercent.setText(changePercent);
                this.volume.setText(volume);
                this.volumeDelta.setText(volumeDelta);
                if (volumeDelta.contains("-"))
                    this.volumeDelta.setTextColor(ContextCompat.getColor(getActivity(), R.color.text_green));
                else
                    this.volumeDelta.setTextColor(ContextCompat.getColor(getActivity(), R.color.text_red));
                this.closeOi.setText(closeOi);
                this.deltaOi.setText(deltaOi);
                if (deltaOi.contains("-"))
                    this.deltaOi.setTextColor(ContextCompat.getColor(getActivity(), R.color.text_green));
                else
                    this.deltaOi.setTextColor(ContextCompat.getColor(getActivity(), R.color.text_red));
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

}

package com.shinnytech.futures.view.custommpchart.mychart;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;

import com.github.mikephil.charting.charts.CombinedChart;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarLineScatterCandleBubbleData;
import com.github.mikephil.charting.data.ChartData;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.datasets.IDataSet;
import com.shinnytech.futures.view.custommpchart.mycomponent.MyLegend;
import com.shinnytech.futures.view.custommpchart.mycomponent.MyTransformer;
import com.shinnytech.futures.view.custommpchart.mycomponent.MyXAxis;
import com.shinnytech.futures.view.custommpchart.mycomponent.MyYAxis;
import com.shinnytech.futures.view.custommpchart.myrenderer.CombinedChartKlineRenderer;
import com.shinnytech.futures.view.custommpchart.myrenderer.MyLegendRenderer;
import com.shinnytech.futures.view.custommpchart.myrenderer.XAxisRendererKline;
import com.shinnytech.futures.view.custommpchart.myrenderer.YAxisRendererKline;

import java.util.List;

/**
 * date: 7/20/17
 * author: chenli
 * description:
 * version:
 * state:
 */

public class CombinedChartKline extends CombinedChart {

    public CombinedChartKline(Context context) {
        super(context);
    }

    public CombinedChartKline(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CombinedChartKline(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void init() {
        super.init();

        mLeftAxisTransformer = new MyTransformer(mViewPortHandler);
        mRightAxisTransformer = new MyTransformer(mViewPortHandler);

        mXAxis = new MyXAxis();
        mXAxisRenderer = new XAxisRendererKline(mViewPortHandler, (MyXAxis) mXAxis, mLeftAxisTransformer, this);

        mAxisLeft = new MyYAxis(YAxis.AxisDependency.LEFT);
        mAxisRendererLeft = new YAxisRendererKline(mViewPortHandler, (MyYAxis) mAxisLeft, mLeftAxisTransformer);

        mAxisRight = new MyYAxis(YAxis.AxisDependency.RIGHT);
        mAxisRendererRight = new YAxisRendererKline(mViewPortHandler, (MyYAxis) mAxisRight, mRightAxisTransformer);

        mLegend = new MyLegend();
        mLegendRenderer = new MyLegendRenderer(mViewPortHandler, (MyLegend) mLegend);

        mRenderer = new CombinedChartKlineRenderer(this, mAnimator, mViewPortHandler);
    }

    /*返回转型后的左右轴*/
    @Override
    public MyXAxis getXAxis() {
        return (MyXAxis) super.getXAxis();
    }

    @Override
    public MyYAxis getAxisLeft() {
        return (MyYAxis) super.getAxisLeft();
    }

    @Override
    public MyYAxis getAxisRight() {
        return (MyYAxis) super.getAxisRight();
    }

    @Override
    public MyLegend getLegend() {
        return (MyLegend) super.getLegend();
    }

    @Override
    protected void drawGridBackground(Canvas c) {

        if (mDrawGridBackground) {

            // draw the grid background
            c.drawRect(mViewPortHandler.getContentRect(), mGridBackgroundPaint);
        }

        if (mDrawBorders) {
            c.drawLine(0, mViewPortHandler.offsetTop(), mViewPortHandler.getChartWidth(), mViewPortHandler.offsetTop(), mBorderPaint);
        }
    }

    @Override
    public MyTransformer getTransformer(YAxis.AxisDependency which) {
        return (MyTransformer) super.getTransformer(which);
    }

    @Override
    protected void drawMarkers(Canvas canvas) {

        // if there is no marker view or drawing marker is disabled
        if (mMarker == null || !isDrawMarkersEnabled() || !valuesToHighlight())
            return;

        for (Highlight highlight :
                mIndicesToHighlight) {

            IDataSet set = mData.getDataSetByIndex(highlight.getDataSetIndex());

            Entry e = getEntryForHighlight(highlight);
            int entryIndex = set.getEntryIndex(e);

            // make sure entry not null
            if (e == null || entryIndex > set.getEntryCount() * mAnimator.getPhaseX())
                continue;

            float[] pos = getMarkerPosition(highlight);

            // check bounds
//            if (!mViewPortHandler.isInBounds(pos[0], pos[1]))
//                continue;

            // callbacks to switchQuoteInsList the content
            mMarker.refreshContent(e, highlight);

            // draw the marker
            mMarker.draw(canvas, pos[0], pos[1]);
        }
    }

    /**
     * Get the Entry for a corresponding highlight object
     *
     * @param highlight
     * @return the entry that is highlighted
     */
    public Entry getEntryForHighlight(Highlight highlight) {

        List<BarLineScatterCandleBubbleData> dataObjects = mData.getAllData();

        if (highlight.getDataIndex() >= dataObjects.size())
            return null;

        ChartData data = dataObjects.get(highlight.getDataIndex());

        if (highlight.getDataSetIndex() >= data.getDataSetCount())
            return null;
        else {
            // The value of the highlighted entry could be NaN -
            //   if we are not interested in highlighting a specific value.

            List<Entry> entries = data.getDataSetByIndex(highlight.getDataSetIndex())
                    .getEntriesForXValue(highlight.getX());

            return entries.get(0);
        }
    }
}
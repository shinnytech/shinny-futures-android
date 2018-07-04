package com.shinnytech.futures.view.custommpchart.myrenderer;

import android.graphics.Canvas;

import com.github.mikephil.charting.charts.BarLineChartBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.renderer.XAxisRenderer;
import com.github.mikephil.charting.utils.MPPointF;
import com.github.mikephil.charting.utils.Transformer;
import com.github.mikephil.charting.utils.Utils;
import com.github.mikephil.charting.utils.ViewPortHandler;
import com.shinnytech.futures.view.custommpchart.mycomponent.MyXAxis;

/**
 * Created on 7/20/17.
 * Created by chenli.
 * Description: .
 */

public class XAxisRendererKline extends XAxisRenderer {
    private final BarLineChartBase mChart;

    private MyXAxis mXAxis;

    public XAxisRendererKline(ViewPortHandler viewPortHandler, MyXAxis xAxis, Transformer trans, BarLineChartBase chart) {
        super(viewPortHandler, xAxis, trans);
        mXAxis = xAxis;
        mChart = chart;
    }


    @Override
    public void renderAxisLabels(Canvas c) {

        if (!mXAxis.isEnabled() || !mXAxis.isDrawLabelsEnabled())
            return;

        float yoffset = mXAxis.getYOffset();

        mAxisLabelPaint.setTypeface(mXAxis.getTypeface());
        mAxisLabelPaint.setTextSize(mXAxis.getTextSize());
        mAxisLabelPaint.setColor(mXAxis.getTextColor());

        MPPointF pointF = MPPointF.getInstance(0, 0);
        if (mXAxis.getPosition() == XAxis.XAxisPosition.TOP) {
            pointF.x = 0.5f;
            pointF.y = 1.0f;
            drawLabels(c, mViewPortHandler.contentTop() - yoffset, pointF);

        } else if (mXAxis.getPosition() == XAxis.XAxisPosition.TOP_INSIDE) {
            pointF.x = 0.5f;
            pointF.y = 1.0f;
            drawLabels(c, mViewPortHandler.contentTop() + yoffset + mXAxis.mLabelRotatedHeight, pointF);

        } else if (mXAxis.getPosition() == XAxis.XAxisPosition.BOTTOM) {
            pointF.x = 0.5f;
            pointF.y = 0.0f;
            drawLabels(c, mViewPortHandler.contentBottom(), pointF);

        } else if (mXAxis.getPosition() == XAxis.XAxisPosition.BOTTOM_INSIDE) {
            pointF.x = 0.5f;
            pointF.y = 0.0f;
            drawLabels(c, mViewPortHandler.contentBottom() - yoffset - mXAxis.mLabelRotatedHeight, pointF);

        } else { // BOTH SIDED
            pointF.x = 0.5f;
            pointF.y = 1.0f;
            drawLabels(c, mViewPortHandler.contentTop() - yoffset, pointF);
            pointF.x = 0.5f;
            pointF.y = 0.0f;
            drawLabels(c, mViewPortHandler.contentBottom() + yoffset, pointF);
        }
        MPPointF.recycleInstance(pointF);
    }


    @Override
    protected void drawLabels(Canvas c, float pos, MPPointF anchor) {
        float[] positions = new float[mXAxis.mEntryCount * 2];
        int labelHeight = 0;
        int labelWidth;

        for (int i = 0; i < positions.length; i += 2) {
            positions[i] = mXAxis.mEntries[i / 2];
        }

        mTrans.pointValuesToPixel(positions);
        int count = positions.length;
        for (int i = 0; i < count; i += 2) {
            float x = positions[i];
            if (mViewPortHandler.isInBoundsX(x)) {
                String label = mXAxis.getValueFormatter().getFormattedValue(mXAxis.mEntries[i / 2], mXAxis);

                if (label != null) {

                    if (i != 0) {
                        int index = label.indexOf("/");
                        label = label.substring(index + 1);
                    }

                    if (labelHeight == 0)
                        labelHeight = Utils.calcTextHeight(mAxisLabelPaint, label);
                    labelWidth = Utils.calcTextWidth(mAxisLabelPaint, label);

                    //右出界
                    if ((labelWidth / 2 + x) > mChart.getViewPortHandler().contentRight()) {
                        x = mViewPortHandler.contentRight() - labelWidth / 2;
                    } else if ((x - labelWidth / 2) < mChart.getViewPortHandler().contentLeft()) {
                        //左出界
                        x = mViewPortHandler.offsetLeft() + labelWidth / 2;
                    }

                    c.drawText(label, x,
                            pos + mChart.getViewPortHandler().offsetBottom() / 2 + labelHeight / 2,
                            mAxisLabelPaint);
                }
            }

        }
    }
}

package com.shinnytech.futures.view.custommpchart.myrenderer;

import android.graphics.Canvas;
import android.graphics.Path;

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

public class XAxisRendererCurrentDay extends XAxisRenderer {
    private final BarLineChartBase mChart;

    private MyXAxis mXAxis;
    private Path mRenderGridLinesPath = new Path();

    public XAxisRendererCurrentDay(ViewPortHandler viewPortHandler, MyXAxis xAxis, Transformer trans, BarLineChartBase chart) {
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
        float[] position = new float[]{
                0f, 0f
        };
        int labelWidth = 0;
        int labelHeight = 0;
        int count = mXAxis.getXLabels().size();

        for (int i = 0; i < count; i++) {
            /*获取label对应key值，也就是x轴坐标0,60,121,182,242*/
            int ix = mXAxis.getXLabels().keyAt(i);

            position[0] = ix;

            /*在图表中的x轴转为像素，方便绘制text*/
            mTrans.pointValuesToPixel(position);

            /*x轴越界*/
            if (mViewPortHandler.isInBoundsX(position[0])) {

                String label = mXAxis.getXLabels().valueAt(i);

                if (label != null) {

                    if (labelWidth == 0) labelWidth = Utils.calcTextWidth(mAxisLabelPaint, label);
                    if (labelHeight == 0)
                        labelHeight = Utils.calcTextHeight(mAxisLabelPaint, label);

                    //右出界
                    if ((labelWidth / 2 + position[0]) > mChart.getViewPortHandler().contentRight()) {
                        position[0] = mViewPortHandler.contentRight() - labelWidth / 2;
                    } else if ((position[0] - labelWidth / 2) < mChart.getViewPortHandler().contentLeft()) {
                        //左出界
                        position[0] = mViewPortHandler.offsetLeft() + labelWidth / 2;
                    }

                    c.drawText(label, position[0],
                            pos + mChart.getViewPortHandler().offsetBottom() / 2 + labelHeight / 2,
                            mAxisLabelPaint);
                }

            }

        }
    }

    /*x轴垂直线*/
    @Override
    public void renderGridLines(Canvas c) {
        if (!mXAxis.isDrawGridLinesEnabled() || !mXAxis.isEnabled())
            return;
        float[] position = new float[]{
                0f, 0f
        };

        mGridPaint.setColor(mXAxis.getGridColor());
        mGridPaint.setStrokeWidth(mXAxis.getGridLineWidth());
        mGridPaint.setPathEffect(mXAxis.getGridDashPathEffect());
        Path gridLinePath = mRenderGridLinesPath;
        gridLinePath.reset();
        int count = mXAxis.getXLabels().size();
        if (!mChart.isScaleXEnabled()) {
            count -= 1;
        }

        //首尾标签不画
        for (int i = 1; i <= count; i++) {

            int ix = mXAxis.getXLabels().keyAt(i);

            position[0] = ix;

            mTrans.pointValuesToPixel(position);

            gridLinePath.moveTo(position[0], mViewPortHandler.contentBottom());
            gridLinePath.lineTo(position[0], mViewPortHandler.contentTop());

            // draw a path because lines don't support dashing on lower android versions
            c.drawPath(gridLinePath, mGridPaint);

            gridLinePath.reset();

        }

    }
}

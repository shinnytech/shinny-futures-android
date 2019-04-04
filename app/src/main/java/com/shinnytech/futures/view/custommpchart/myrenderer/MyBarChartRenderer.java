package com.shinnytech.futures.view.custommpchart.myrenderer;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;

import com.github.mikephil.charting.animation.ChartAnimator;
import com.github.mikephil.charting.buffer.BarBuffer;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.dataprovider.BarDataProvider;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.github.mikephil.charting.renderer.BarChartRenderer;
import com.github.mikephil.charting.utils.MPPointD;
import com.github.mikephil.charting.utils.Transformer;
import com.github.mikephil.charting.utils.Utils;
import com.github.mikephil.charting.utils.ViewPortHandler;

public class MyBarChartRenderer extends BarChartRenderer {

    private RectF mBarShadowRectBuffer = new RectF();
    //画线型成交量图
    private Paint mLinePaint;

    public MyBarChartRenderer(BarDataProvider chart, ChartAnimator animator, ViewPortHandler viewPortHandler) {
        super(chart, animator, viewPortHandler);
        mLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mLinePaint.setStyle(Paint.Style.STROKE);
    }

    protected void drawDataSet(Canvas c, IBarDataSet dataSet, int index) {

        Transformer trans = mChart.getTransformer(dataSet.getAxisDependency());

        mBarBorderPaint.setColor(dataSet.getBarBorderColor());
        mBarBorderPaint.setStrokeWidth(Utils.convertDpToPixel(dataSet.getBarBorderWidth()));

        mLinePaint.setStrokeWidth(Utils.convertDpToPixel(0.7f));
        mRenderPaint.setStrokeWidth(Utils.convertDpToPixel(0.7f));

        mHighlightPaint.setStrokeWidth(Utils.convertDpToPixel(0.7f));

        final boolean drawBorder = dataSet.getBarBorderWidth() > 0.f;

        float phaseX = mAnimator.getPhaseX();
        float phaseY = mAnimator.getPhaseY();

        BarData barData = mChart.getBarData();
        final float barWidth = barData.getBarWidth();

        // draw the bar shadow before the values
        if (mChart.isDrawBarShadowEnabled()) {
            mShadowPaint.setColor(dataSet.getBarShadowColor());

            final float barWidthHalf = barWidth / 2.0f;
            float x;

            for (int i = 0, count = Math.min((int) (Math.ceil((float) (dataSet.getEntryCount()) * phaseX)), dataSet.getEntryCount());
                 i < count;
                 i++) {

                BarEntry e = dataSet.getEntryForIndex(i);

                x = e.getX();

                mBarShadowRectBuffer.left = x - barWidthHalf;
                mBarShadowRectBuffer.right = x + barWidthHalf;

                trans.rectValueToPixel(mBarShadowRectBuffer);

                if (!mViewPortHandler.isInBoundsLeft(mBarShadowRectBuffer.right))
                    continue;

                if (!mViewPortHandler.isInBoundsRight(mBarShadowRectBuffer.left))
                    break;

                mBarShadowRectBuffer.top = mViewPortHandler.contentTop();
                mBarShadowRectBuffer.bottom = mViewPortHandler.contentBottom();

                c.drawRect(mBarShadowRectBuffer, mShadowPaint);
            }
        }

        // initialize the buffer
        BarBuffer buffer = mBarBuffers[index];
        buffer.setPhases(phaseX, phaseY);
        buffer.setDataSet(index);
        buffer.setInverted(mChart.isInverted(dataSet.getAxisDependency()));
        buffer.setBarWidth(mChart.getBarData().getBarWidth());

        buffer.feed(dataSet);

        trans.pointValuesToPixel(buffer.buffer);

        final boolean isSingleColor = dataSet.getColors().size() == 1;

        if (isSingleColor) {
            mRenderPaint.setColor(dataSet.getColor());
            mLinePaint.setColor(dataSet.getColor());
        }

        for (int j = 0; j < buffer.size(); j += 4) {

            if (!mViewPortHandler.isInBoundsLeft(buffer.buffer[j + 2]))
                continue;

            if (!mViewPortHandler.isInBoundsRight(buffer.buffer[j]))
                break;

            if (!isSingleColor) {
                Entry e = dataSet.getEntryForIndex(j / 4);
                float sub = e != null ? (float) e.getData() : 0.0f;

                //线和柱子的颜色
                if (barWidth == 0.01f) {
                    if (sub > 0) {
                        mLinePaint.setColor(dataSet.getColor(0));
                    } else {
                        mLinePaint.setColor(dataSet.getColor(1));
                    }
                } else {
                    if (sub > 0) {
                        mRenderPaint.setColor(dataSet.getColor(0));
                        mRenderPaint.setStyle(Paint.Style.FILL);
                    } else {
                        mRenderPaint.setColor(dataSet.getColor(1));
                        mRenderPaint.setStyle(Paint.Style.STROKE);
                    }
                }
            }

            //成交量是画线还是画柱子
            if (barWidth == 0.01f) {
                float x = (buffer.buffer[j] + buffer.buffer[j + 2]) / 2;
                float startY = buffer.buffer[j + 3];
                float stopY = buffer.buffer[j + 1];
                c.drawLine(x, startY, x, stopY, mLinePaint);
            } else {
                float height = buffer.buffer[j + 1] - buffer.buffer[j + 3];
                if (height == 0) {
                    c.drawLine(buffer.buffer[j], buffer.buffer[j + 1], buffer.buffer[j + 2],
                            buffer.buffer[j + 3], mRenderPaint);
                } else {
                    c.drawRect(buffer.buffer[j], buffer.buffer[j + 1], buffer.buffer[j + 2],
                            buffer.buffer[j + 3], mRenderPaint);
                }

            }

            if (drawBorder) {
                c.drawRect(buffer.buffer[j], buffer.buffer[j + 1], buffer.buffer[j + 2],
                        buffer.buffer[j + 3], mBarBorderPaint);
            }
        }
    }

    @Override
    public void drawHighlighted(Canvas c, Highlight[] indices) {
        BarData barData = mChart.getBarData();

        for (Highlight high : indices) {

            IBarDataSet set = barData.getDataSetByIndex(high.getDataSetIndex());

            if (set == null || !set.isHighlightEnabled())
                continue;

            BarEntry e = set.getEntryForXValue(high.getX(), high.getY());

            if (!isInBoundsX(e, set))
                continue;

            mHighlightPaint.setColor(set.getHighLightColor());
            //保持和顶层图的十字光标一致
            //mHighlightPaint.setAlpha(set.getHighLightAlpha());

            float barWidth = barData.getBarWidth();
            Transformer trans = mChart.getTransformer(set.getAxisDependency());
            prepareBarHighlight(e.getX(), 0, 0, barWidth / 2, trans);

            //画竖线
            float xp = mBarRect.centerX();
            c.drawLine(xp, mViewPortHandler.getContentRect().bottom, xp, 0, mHighlightPaint);

            //判断是否画横线
            float y = high.getDrawY();
            float yMax = getYPixelForValues(xp, 0);
            float xMax = mChart.getWidth();
            if (y >= 0 && y <= yMax) {//在区域内即绘制横线
                //绘制横线
                c.drawLine(0, y, xMax, y, mHighlightPaint);
            }
        }
    }

    protected float getYPixelForValues(float x, float y) {
        MPPointD pixels = mChart.getTransformer(YAxis.AxisDependency.LEFT).getPixelForValues(x, y);
        return (float) pixels.y;
    }
}

package com.shinnytech.futures.view.custommpchart.myrenderer;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.support.v4.content.ContextCompat;

import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.renderer.YAxisRenderer;
import com.github.mikephil.charting.utils.Transformer;
import com.github.mikephil.charting.utils.Utils;
import com.github.mikephil.charting.utils.ViewPortHandler;
import com.shinnytech.futures.R;
import com.shinnytech.futures.application.BaseApplication;
import com.shinnytech.futures.utils.LogUtils;
import com.shinnytech.futures.view.custommpchart.mycomponent.MyYAxis;

import java.util.List;

public class YAxisRendererCurrentDay extends YAxisRenderer {

    protected MyYAxis mYAxis;

    public YAxisRendererCurrentDay(ViewPortHandler viewPortHandler, MyYAxis yAxis, Transformer trans) {
        super(viewPortHandler, yAxis, trans);
        mYAxis = yAxis;
    }

    @Override
    protected void computeAxisValues(float min, float max) {
        if (Float.isNaN(mYAxis.getBaseValue())) {
            int labelCount = mYAxis.getLabelCount();
            float interval = (max - min) / labelCount;
            mYAxis.mEntryCount = 1;
            mYAxis.mEntries = new float[1];
            mYAxis.mEntries[0] = max - interval;
            return;
        }
        float base = mYAxis.getBaseValue();
        int labelCount = mYAxis.getLabelCount();
        float interval = (base - min) / labelCount;
        int n = labelCount * 2 + 1;
        mYAxis.mEntryCount = n;
        mYAxis.mEntries = new float[n];
        int i;
        float f;
        for (f = min, i = 0; i < n; f += interval, i++) {
            mYAxis.mEntries[i] = f;
        }
    }

    @Override
    public void renderAxisLabels(Canvas c) {
        if (!mYAxis.isEnabled() || !mYAxis.isDrawLabelsEnabled())
            return;

        float[] positions = getTransformedPositions();

        mAxisLabelPaint.setTypeface(mYAxis.getTypeface());
        mAxisLabelPaint.setTextSize(mYAxis.getTextSize());
        mAxisLabelPaint.setColor(mYAxis.getTextColor());

        float yoffset = Utils.calcTextHeight(mAxisLabelPaint, "A") / 2.5f + mYAxis.getYOffset();

        YAxis.AxisDependency dependency = mYAxis.getAxisDependency();
        YAxis.YAxisLabelPosition labelPosition = mYAxis.getLabelPosition();

        float xPos;

        if (dependency == YAxis.AxisDependency.LEFT) {

            if (labelPosition == YAxis.YAxisLabelPosition.OUTSIDE_CHART) {
                mAxisLabelPaint.setTextAlign(Paint.Align.RIGHT);
            } else {
                mAxisLabelPaint.setTextAlign(Paint.Align.LEFT);
            }
            xPos = mViewPortHandler.offsetLeft();


        } else {

            if (labelPosition == YAxis.YAxisLabelPosition.OUTSIDE_CHART) {
                mAxisLabelPaint.setTextAlign(Paint.Align.LEFT);
            } else {
                mAxisLabelPaint.setTextAlign(Paint.Align.RIGHT);
            }
            xPos = mViewPortHandler.contentRight();
        }

        drawYLabels(c, xPos, positions, yoffset);
    }

    @Override
    protected void drawYLabels(Canvas c, float fixedPosition, float[] positions, float offset) {
        for (int i = 0; i < mYAxis.mEntryCount; i++) {
            String text = mYAxis.getFormattedLabel(i);
            if (!mYAxis.isDrawTopYLabelEntryEnabled() && i >= mYAxis.mEntryCount - 1) return;
            int labelHeight = Utils.calcTextHeight(mAxisLabelPaint, text);

            float pos = positions[i * 2 + 1] + offset;
            if ((pos - labelHeight) < mViewPortHandler.contentTop()) {
                pos = mViewPortHandler.contentTop() + offset * 2.5f + 3;
            } else if ((pos + labelHeight / 2) > mViewPortHandler.contentBottom()) {
                pos = mViewPortHandler.contentBottom() - 3;
            }

            try {
                if (text.contains("%")) {
                    if (text.contains("-")) {
                        if (Float.parseFloat(text.replaceAll("[^\\d.]", "")) == 0) {
                            text = text.replace("-", "");
                            mAxisLabelPaint.setColor(ContextCompat.getColor(BaseApplication.getContext(), R.color.kline_text));
                        } else {
                            mAxisLabelPaint.setColor(ContextCompat.getColor(BaseApplication.getContext(), R.color.text_green));
                        }
                    } else {
                        if (Float.parseFloat(text.replaceAll("[^\\d.]", "")) > 0) {
                            text = "+" + text;
                            mAxisLabelPaint.setColor(ContextCompat.getColor(BaseApplication.getContext(), R.color.text_red));
                        }
                    }
                } else {
                    if (Float.parseFloat(text.replaceAll("[^\\d.]", "")) > mYAxis.getBaseValue()) {
                        mAxisLabelPaint.setColor(ContextCompat.getColor(BaseApplication.getContext(), R.color.text_red));
                    } else if (Float.parseFloat(text.replaceAll("[^\\d.]", "")) < mYAxis.getBaseValue()) {
                        mAxisLabelPaint.setColor(ContextCompat.getColor(BaseApplication.getContext(), R.color.text_green));
                    } else {
                        mAxisLabelPaint.setColor(ContextCompat.getColor(BaseApplication.getContext(), R.color.kline_text));
                    }
                }
            } catch (NumberFormatException ex) {
                ex.printStackTrace();
            }

            c.drawText(text, fixedPosition, pos, mAxisLabelPaint);
        }
    }

    @Override
    public void renderGridLines(Canvas c) {

        if (!mYAxis.isEnabled())
            return;

        if (mYAxis.isDrawGridLinesEnabled()) {

            int clipRestoreCount = c.save();
            c.clipRect(getGridClippingRect());

            float[] positions = getTransformedPositions();

            mGridPaint.setColor(mYAxis.getGridColor());
            mGridPaint.setStrokeWidth(mYAxis.getGridLineWidth());

            Path gridLinePath = mRenderGridLinesPath;
            gridLinePath.reset();

            // draw the grid
            for (int i = 0; i < positions.length; i += 2) {

                // draw a path because lines don't support dashing on lower android versions
                if (positions.length != 2 && (i == 0 || i == positions.length - 2 || i == (positions.length - 1) / 2))
                    mGridPaint.setPathEffect(null);
                else mGridPaint.setPathEffect(mYAxis.getGridDashPathEffect());
                c.drawPath(linePath(gridLinePath, i, positions), mGridPaint);
                gridLinePath.reset();
            }

            c.restoreToCount(clipRestoreCount);
        }

        if (mYAxis.isDrawZeroLineEnabled()) {
            drawZeroLine(c);
        }
    }

    /**
     * Draws the LimitLines associated with this axis to the screen.
     *
     * @param c
     */
    @Override
    public void renderLimitLines(Canvas c) {

        List<LimitLine> limitLines = mYAxis.getLimitLines();

        if (limitLines == null || limitLines.size() <= 0)
            return;

        float[] pts = mRenderLimitLinesBuffer;
        pts[0] = 0;
        pts[1] = 0;
        Path limitLinePath = mRenderLimitLines;
        limitLinePath.reset();

        for (int i = 0; i < limitLines.size(); i++) {

            LimitLine l = limitLines.get(i);

            if (!l.isEnabled())
                continue;

            int clipRestoreCount = c.save();
            mLimitLineClippingRect.set(mViewPortHandler.getContentRect());
            mLimitLineClippingRect.inset(0.f, -l.getLineWidth());
            c.clipRect(mLimitLineClippingRect);

            mLimitLinePaint.setStyle(Paint.Style.STROKE);
            mLimitLinePaint.setColor(l.getLineColor());
            mLimitLinePaint.setStrokeWidth(l.getLineWidth());
            mLimitLinePaint.setPathEffect(l.getDashPathEffect());

            pts[1] = l.getLimit();

            mTrans.pointValuesToPixel(pts);

            limitLinePath.moveTo(mViewPortHandler.contentLeft(), pts[1]);
            limitLinePath.lineTo(mViewPortHandler.contentRight(), pts[1]);

            c.drawPath(limitLinePath, mLimitLinePaint);
            limitLinePath.reset();
            // a.json.drawLines(pts, mLimitLinePaint);

            String label = l.getLabel();

            // if drawing the limit-value label is enabled
            if (label != null && !label.equals("")) {

                mLimitLinePaint.setStyle(l.getTextStyle());
                mLimitLinePaint.setPathEffect(null);
                mLimitLinePaint.setColor(l.getTextColor());
                mLimitLinePaint.setTypeface(l.getTypeface());
                mLimitLinePaint.setStrokeWidth(0.5f);
                mLimitLinePaint.setTextSize(l.getTextSize());

                final float labelLineHeight = Utils.calcTextHeight(mLimitLinePaint, label);
                float xOffset = Utils.convertDpToPixel(4f) + l.getXOffset();
                float yOffset = l.getLineWidth() + labelLineHeight + l.getYOffset();

                final LimitLine.LimitLabelPosition position = l.getLabelPosition();

                if (position == LimitLine.LimitLabelPosition.RIGHT_TOP) {

                    mLimitLinePaint.setTextAlign(Paint.Align.RIGHT);
                    c.drawText(label,
                            mViewPortHandler.contentRight() - xOffset,
                            pts[1] - yOffset + labelLineHeight, mLimitLinePaint);

                } else if (position == LimitLine.LimitLabelPosition.RIGHT_BOTTOM) {

                    mLimitLinePaint.setTextAlign(Paint.Align.RIGHT);
                    c.drawText(label,
                            mViewPortHandler.contentRight() - xOffset,
                            pts[1] + yOffset, mLimitLinePaint);

                } else if (position == LimitLine.LimitLabelPosition.LEFT_TOP) {

                    mLimitLinePaint.setTextAlign(Paint.Align.LEFT);
                    c.drawText(label,
                            mViewPortHandler.contentLeft() + xOffset,
                            pts[1] - yOffset + labelLineHeight, mLimitLinePaint);

                } else {

                    mLimitLinePaint.setTextAlign(Paint.Align.LEFT);
                    c.drawText(label,
                            mViewPortHandler.offsetLeft() + xOffset,
                            pts[1], mLimitLinePaint);
                }
            }

            c.restoreToCount(clipRestoreCount);
        }
    }
}

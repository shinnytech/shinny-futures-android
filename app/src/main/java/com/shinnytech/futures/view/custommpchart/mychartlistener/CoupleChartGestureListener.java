package com.shinnytech.futures.view.custommpchart.mychartlistener;

import android.graphics.Matrix;
import android.view.MotionEvent;

import com.github.mikephil.charting.charts.BarLineChartBase;
import com.github.mikephil.charting.charts.Chart;
import com.github.mikephil.charting.listener.ChartTouchListener;
import com.github.mikephil.charting.listener.OnChartGestureListener;
import com.shinnytech.futures.application.BaseApplication;
import com.shinnytech.futures.constants.CommonConstants;
import com.shinnytech.futures.utils.LogUtils;
import com.shinnytech.futures.utils.SPUtils;

/**
 * 图表联动交互监听
 */
public class CoupleChartGestureListener implements OnChartGestureListener {

    private BarLineChartBase srcChart;
    private Chart[] dstCharts;

    public CoupleChartGestureListener(BarLineChartBase srcChart, Chart... dstCharts) {
        this.srcChart = srcChart;
        this.dstCharts = dstCharts;
    }

    @Override
    public void onChartGestureStart(MotionEvent me, ChartTouchListener.ChartGesture lastPerformedGesture) {
        syncCharts();
        chartGestureStart(me, lastPerformedGesture);
    }

    @Override
    public void onChartGestureEnd(MotionEvent me, ChartTouchListener.ChartGesture lastPerformedGesture) {
        syncCharts();
        chartGestureEnd(me, lastPerformedGesture);
    }

    @Override
    public void onChartLongPressed(MotionEvent me) {
        syncCharts();
        chartLongPressed(me);
    }

    @Override
    public void onChartDoubleTapped(MotionEvent me) {
        syncCharts();
        chartDoubleTapped(me);
    }

    @Override
    public void onChartSingleTapped(MotionEvent me) {
        syncCharts();
        chartSingleTapped(me);
    }

    @Override
    public void onChartFling(MotionEvent me1, MotionEvent me2, float velocityX, float velocityY) {
        syncCharts();
    }

    @Override
    public void onChartScale(MotionEvent me, float scaleX, float scaleY) {
        float mScalex = (float) SPUtils.get(BaseApplication.getContext(), CommonConstants.SCALE_X, 1.0f);
        if ((scaleX > 1 && scaleX > mScalex) || (scaleX < 1 && scaleX < mScalex )){
            mScalex = scaleX;
        }
        SPUtils.putAndApply(BaseApplication.getContext(), CommonConstants.SCALE_X, mScalex);
        syncCharts();
    }

    @Override
    public void onChartTranslate(MotionEvent me, float dX, float dY) {
        syncCharts();
        chartTranslate(me, dX, dY);
    }

    //以下5个方法仅为了：方便在外部根据需要自行重写
    public void chartGestureStart(MotionEvent me, ChartTouchListener.ChartGesture lastPerformedGesture) {}
    public void chartGestureEnd(MotionEvent me, ChartTouchListener.ChartGesture lastPerformedGesture) {}
    public void chartLongPressed(MotionEvent me) {}
    public void chartDoubleTapped(MotionEvent me) {}
    public void chartSingleTapped(MotionEvent me) {}
    public void chartTranslate(MotionEvent me, float dX, float dY) {}

    private void syncCharts() {
        Matrix srcMatrix;
        float[] srcVals = new float[9];
        Matrix dstMatrix;
        float[] dstVals = new float[9];
        // get src chart translation matrix:
        srcMatrix = srcChart.getViewPortHandler().getMatrixTouch();
        srcMatrix.getValues(srcVals);
        // apply X axis scaling and position to dst charts:
        for (Chart dstChart : dstCharts) {
            dstMatrix = dstChart.getViewPortHandler().getMatrixTouch();
            dstMatrix.getValues(dstVals);

            dstVals[Matrix.MSCALE_X] = srcVals[Matrix.MSCALE_X];
            dstVals[Matrix.MSKEW_X] = srcVals[Matrix.MSKEW_X];
            dstVals[Matrix.MTRANS_X] = srcVals[Matrix.MTRANS_X];
            dstVals[Matrix.MSKEW_Y] = srcVals[Matrix.MSKEW_Y];
            dstVals[Matrix.MSCALE_Y] = srcVals[Matrix.MSCALE_Y];
            dstVals[Matrix.MTRANS_Y] = srcVals[Matrix.MTRANS_Y];
            dstVals[Matrix.MPERSP_0] = srcVals[Matrix.MPERSP_0];
            dstVals[Matrix.MPERSP_1] = srcVals[Matrix.MPERSP_1];
            dstVals[Matrix.MPERSP_2] = srcVals[Matrix.MPERSP_2];

            dstMatrix.setValues(dstVals);
            dstChart.getViewPortHandler().refresh(dstMatrix, dstChart, true);
        }
    }
}
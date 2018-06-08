package com.xinyi.shinnyfutures.view.custommpchart.myrenderer;

import com.github.mikephil.charting.animation.ChartAnimator;
import com.github.mikephil.charting.renderer.BarChartRenderer;
import com.github.mikephil.charting.renderer.BubbleChartRenderer;
import com.github.mikephil.charting.renderer.CombinedChartRenderer;
import com.github.mikephil.charting.renderer.LineChartRenderer;
import com.github.mikephil.charting.renderer.ScatterChartRenderer;
import com.github.mikephil.charting.utils.ViewPortHandler;
import com.xinyi.shinnyfutures.view.custommpchart.mychart.CombinedChartKline;

/**
 * Created on 8/23/17.
 * Created by chenli.
 * Description: .
 */

public class CombinedChartKlineRenderer extends CombinedChartRenderer {

    public MyCandleStickChartRenderer mCandleStickChartRenderer;

    public CombinedChartKlineRenderer(CombinedChartKline chart, ChartAnimator animator, ViewPortHandler viewPortHandler) {
        super(chart, animator, viewPortHandler);
    }


    /**
     * Creates the renderers needed for this combined-renderer in the required order. Also takes the DrawOrder into
     * consideration.
     */
    public void createRenderers() {

        mRenderers.clear();

        CombinedChartKline chart = (CombinedChartKline) mChart.get();
        if (chart == null)
            return;

        CombinedChartKline.DrawOrder[] orders = chart.getDrawOrder();

        for (CombinedChartKline.DrawOrder order : orders) {

            switch (order) {
                case BAR:
                    if (chart.getBarData() != null)
                        mRenderers.add(new BarChartRenderer(chart, mAnimator, mViewPortHandler));
                    break;
                case BUBBLE:
                    if (chart.getBubbleData() != null)
                        mRenderers.add(new BubbleChartRenderer(chart, mAnimator, mViewPortHandler));
                    break;
                case LINE:
                    if (chart.getLineData() != null)
                        mRenderers.add(new LineChartRenderer(chart, mAnimator, mViewPortHandler));
                    break;
                case CANDLE:
                    if (chart.getCandleData() != null) {
                        mCandleStickChartRenderer = new MyCandleStickChartRenderer(chart, mAnimator, mViewPortHandler);
                        mRenderers.add(mCandleStickChartRenderer);
                    }
                    break;
                case SCATTER:
                    if (chart.getScatterData() != null)
                        mRenderers.add(new ScatterChartRenderer(chart, mAnimator, mViewPortHandler));
                    break;
            }
        }
    }
}

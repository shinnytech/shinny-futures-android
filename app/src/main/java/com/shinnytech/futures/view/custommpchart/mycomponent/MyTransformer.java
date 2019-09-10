package com.shinnytech.futures.view.custommpchart.mycomponent;

import com.github.mikephil.charting.data.CandleEntry;
import com.github.mikephil.charting.interfaces.datasets.ICandleDataSet;
import com.github.mikephil.charting.utils.Transformer;
import com.github.mikephil.charting.utils.ViewPortHandler;

/**
 * Created on 8/23/17.
 * Created by chenli.
 * Description: .
 */

public class MyTransformer extends Transformer {

    private float[] valuePointsForGenerateTransformedValuesCandleLow = new float[1];

    public MyTransformer(ViewPortHandler viewPortHandler) {
        super(viewPortHandler);
    }

    public float[] generateTransformedValuesCandleLow(ICandleDataSet data,
                                                      float phaseX, float phaseY, int from, int to) {

        final int count = (int) ((to - from) * phaseX + 1) * 2;

        if (valuePointsForGenerateTransformedValuesCandleLow.length != count) {
            valuePointsForGenerateTransformedValuesCandleLow = new float[count];
        }
        float[] valuePoints = valuePointsForGenerateTransformedValuesCandleLow;

        for (int j = 0; j < count; j += 2) {

            CandleEntry e = data.getEntryForIndex(j / 2 + from);

            if (e != null) {
                valuePoints[j] = e.getX();
                valuePoints[j + 1] = e.getLow() * phaseY;
            } else {
                valuePoints[j] = 0;
                valuePoints[j + 1] = 0;
            }
        }

        getValueToPixelMatrix().mapPoints(valuePoints);

        return valuePoints;
    }
}

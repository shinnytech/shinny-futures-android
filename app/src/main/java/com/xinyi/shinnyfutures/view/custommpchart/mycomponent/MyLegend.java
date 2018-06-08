package com.xinyi.shinnyfutures.view.custommpchart.mycomponent;

import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.utils.Utils;

/**
 * Created on 8/16/17.
 * Created by chenli.
 * Description: .
 */

public class MyLegend extends Legend {

    public MyLegend() {
        this.mTextSize = Utils.convertDpToPixel(10f);
        this.mXOffset = Utils.convertDpToPixel(5f);
        this.mYOffset = Utils.convertDpToPixel(-1f);
    }
}

package com.shinnytech.futures.view.custommpchart.mycomponent;

import com.github.mikephil.charting.data.CandleEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;

public class MyValueFormatter extends ValueFormatter {

    /**
     * Used to draw low labels, calls {@link #getFormattedValue(float)} by default.
     *
     * @param candleEntry candlestick being labeled
     * @return formatted string label
     */
    public String getCandleLabelLow(CandleEntry candleEntry) {
        return getFormattedValue(candleEntry.getLow());
    }
}

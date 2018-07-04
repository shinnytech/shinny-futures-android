package com.shinnytech.futures.view.custommpchart.mycomponent;

import android.util.SparseArray;

import com.github.mikephil.charting.components.XAxis;

/**
 * Created on 7/20/17.
 * Created by chenli.
 * Description: .
 */

public class MyXAxis extends XAxis {
    private SparseArray<String> mLabels = new SparseArray<>();

    public SparseArray<String> getXLabels() {
        return mLabels;
    }

    public void setXLabels(SparseArray<String> labels) {
        this.mLabels = labels;
    }
}
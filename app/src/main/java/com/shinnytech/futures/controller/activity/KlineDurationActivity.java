package com.shinnytech.futures.controller.activity;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;

import com.shinnytech.futures.R;
import com.shinnytech.futures.constants.CommonConstants;
import com.shinnytech.futures.databinding.ActivityKlineDurationBinding;
import com.shinnytech.futures.model.adapter.DragDialogAdapter;

import java.util.ArrayList;
import java.util.List;

public class KlineDurationActivity extends BaseActivity {

    private ActivityKlineDurationBinding mBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mLayoutID = R.layout.activity_kline_duration;
        mTitle = CommonConstants.KLINE_DURATION;
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void initData() {
        mBinding = (ActivityKlineDurationBinding) mViewDataBinding;
        List<String> data = new ArrayList<>();
        data.add("5秒");
        data.add("10秒");
        data.add("20秒");
        DragDialogAdapter dragDialogAdapter = new DragDialogAdapter(this, data);
        mBinding.durationRv.setLayoutManager(new LinearLayoutManager(this));
        mBinding.durationRv.setAdapter(dragDialogAdapter);
    }

    @Override
    protected void initEvent() {

    }

    @Override
    protected void refreshUI() {

    }
}

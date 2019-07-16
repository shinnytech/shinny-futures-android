package com.shinnytech.futures.controller.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.view.KeyEvent;
import android.view.MenuItem;

import com.shinnytech.futures.R;
import com.shinnytech.futures.application.BaseApplication;
import com.shinnytech.futures.constants.CommonConstants;
import com.shinnytech.futures.databinding.ActivityAddKlineDurationBinding;
import com.shinnytech.futures.model.adapter.AddDurationAdapter;
import com.shinnytech.futures.utils.DividerGridItemDecorationUtils;
import com.shinnytech.futures.utils.SPUtils;

import java.util.ArrayList;
import java.util.List;

public class AddKlineDurationActivity extends BaseActivity {

    private ActivityAddKlineDurationBinding mBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mLayoutID = R.layout.activity_add_kline_duration;
        mTitle = CommonConstants.KLINE_DURATION_ADD;
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void initData() {
        mBinding = (ActivityAddKlineDurationBinding) mViewDataBinding;
        String duration = CommonConstants.KLINE_DURATION_ALL;
        String[] durations = duration.split(",");
        List<String> list = new ArrayList<>();
        for (String data : durations) {
            list.add(data);
        }
        String durationPre = (String) SPUtils.get(BaseApplication.getContext(), CommonConstants.CONFIG_KLINE_DURATION_DEFAULT, "");
        String[] durationsPre = durationPre.split(",");
        List<String> listPre = new ArrayList<>();
        for (String data : durationsPre) {
            listPre.add(data);
        }
        final AddDurationAdapter addDurationAdapter = new AddDurationAdapter(this, list, listPre);
        mBinding.addDurationRv.setLayoutManager(
                new GridLayoutManager(this, 4));
        mBinding.addDurationRv.addItemDecoration(
                new DividerGridItemDecorationUtils(this, R.drawable.activity_add_kline_duration_divider));
        mBinding.addDurationRv.setAdapter(addDurationAdapter);
    }

    @Override
    protected void initEvent() {

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent = new Intent();
        setResult(RESULT_OK, intent);
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        Intent intent = new Intent();
        setResult(RESULT_OK, intent);
        return super.onKeyDown(keyCode, event);
    }
}

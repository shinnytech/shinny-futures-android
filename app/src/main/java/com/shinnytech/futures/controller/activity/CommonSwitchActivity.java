package com.shinnytech.futures.controller.activity;

import android.os.Bundle;
import android.widget.CompoundButton;

import com.shinnytech.futures.R;
import com.shinnytech.futures.databinding.ActivityCommonSwitchBinding;
import com.shinnytech.futures.utils.SPUtils;

import static com.shinnytech.futures.constants.CommonConstants.COMMON_SWITCH;
import static com.shinnytech.futures.constants.CommonConstants.CONFIG_AVERAGE_LINE;
import static com.shinnytech.futures.constants.CommonConstants.CONFIG_MD5;
import static com.shinnytech.futures.constants.CommonConstants.CONFIG_ORDER_LINE;
import static com.shinnytech.futures.constants.CommonConstants.CONFIG_POSITION_LINE;

public class CommonSwitchActivity extends BaseActivity {

    private ActivityCommonSwitchBinding mBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mLayoutID = R.layout.activity_common_switch;
        mTitle = COMMON_SWITCH;
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void initData() {
        mBinding = (ActivityCommonSwitchBinding) mViewDataBinding;
        //初始化开关状态
        boolean mIsPosition = (boolean) SPUtils.get(sContext, CONFIG_POSITION_LINE, true);
        boolean mIsPending = (boolean) SPUtils.get(sContext, CONFIG_ORDER_LINE, true);
        boolean mIsAverage = (boolean) SPUtils.get(sContext, CONFIG_AVERAGE_LINE, true);
        boolean mIsMD5 = (boolean) SPUtils.get(sContext, CONFIG_MD5, true);
        //初始化开关状态
        mBinding.position.setChecked(mIsPosition);
        mBinding.pending.setChecked(mIsPending);
        mBinding.averageLine.setChecked(mIsAverage);
        mBinding.md.setChecked(mIsMD5);
    }

    @Override
    protected void initEvent() {
        mBinding.position.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SPUtils.putAndApply(sContext, CONFIG_POSITION_LINE, isChecked);
            }
        });

        mBinding.pending.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SPUtils.putAndApply(sContext, CONFIG_ORDER_LINE, isChecked);
            }
        });

        mBinding.averageLine.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SPUtils.putAndApply(sContext, CONFIG_AVERAGE_LINE, isChecked);
            }
        });

        mBinding.md.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SPUtils.putAndApply(sContext, CONFIG_MD5, isChecked);
            }
        });
    }

    @Override
    protected void refreshUI() {

    }

}

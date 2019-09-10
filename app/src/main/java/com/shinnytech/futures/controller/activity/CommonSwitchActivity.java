package com.shinnytech.futures.controller.activity;

import android.os.Bundle;
import android.widget.CompoundButton;

import com.shinnytech.futures.R;
import com.shinnytech.futures.databinding.ActivityCommonSwitchBinding;
import com.shinnytech.futures.model.bean.eventbusbean.CommonSwitchEvent;
import com.shinnytech.futures.utils.SPUtils;

import org.greenrobot.eventbus.EventBus;

import static com.shinnytech.futures.constants.SettingConstants.COMMON_SWITCH;
import static com.shinnytech.futures.constants.SettingConstants.CONFIG_AVERAGE_LINE;
import static com.shinnytech.futures.constants.SettingConstants.CONFIG_MD5;
import static com.shinnytech.futures.constants.SettingConstants.CONFIG_ORDER_LINE;
import static com.shinnytech.futures.constants.SettingConstants.CONFIG_POSITION_LINE;

public class CommonSwitchActivity extends BaseActivity {

    private ActivityCommonSwitchBinding mBinding;
    private CommonSwitchEvent mCommonSwitchEvent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mLayoutID = R.layout.activity_common_switch;
        mTitle = COMMON_SWITCH;
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void initData() {
        mBinding = (ActivityCommonSwitchBinding) mViewDataBinding;
        mCommonSwitchEvent = new CommonSwitchEvent();
        //初始化开关状态
        boolean position = (boolean) SPUtils.get(sContext, CONFIG_POSITION_LINE, true);
        boolean pending = (boolean) SPUtils.get(sContext, CONFIG_ORDER_LINE, true);
        boolean average = (boolean) SPUtils.get(sContext, CONFIG_AVERAGE_LINE, true);
        boolean md5 = (boolean) SPUtils.get(sContext, CONFIG_MD5, true);
        //初始化开关状态
        mBinding.position.setChecked(position);
        mBinding.pending.setChecked(pending);
        mBinding.averageLine.setChecked(average);
        mBinding.md.setChecked(md5);
        mCommonSwitchEvent.setAverage(average);
        mCommonSwitchEvent.setMD5(md5);
        mCommonSwitchEvent.setPending(pending);
        mCommonSwitchEvent.setPosition(position);
    }

    @Override
    protected void initEvent() {
        mBinding.position.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SPUtils.putAndApply(sContext, CONFIG_POSITION_LINE, isChecked);
                mCommonSwitchEvent.setPosition(isChecked);
                EventBus.getDefault().post(mCommonSwitchEvent);
            }
        });

        mBinding.pending.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SPUtils.putAndApply(sContext, CONFIG_ORDER_LINE, isChecked);
                mCommonSwitchEvent.setPending(isChecked);
                EventBus.getDefault().post(mCommonSwitchEvent);
            }
        });

        mBinding.averageLine.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SPUtils.putAndApply(sContext, CONFIG_AVERAGE_LINE, isChecked);
                mCommonSwitchEvent.setAverage(isChecked);
                EventBus.getDefault().post(mCommonSwitchEvent);
            }
        });

        mBinding.md.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SPUtils.putAndApply(sContext, CONFIG_MD5, isChecked);
                mCommonSwitchEvent.setMD5(isChecked);
                EventBus.getDefault().post(mCommonSwitchEvent);
            }
        });
    }
}

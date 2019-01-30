package com.shinnytech.futures.controller.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;

import com.shinnytech.futures.R;
import com.shinnytech.futures.constants.CommonConstants;
import com.shinnytech.futures.databinding.ActivitySettingBinding;
import com.shinnytech.futures.model.adapter.SettingAdapter;
import com.shinnytech.futures.model.bean.settingbean.SettingEntity;
import com.shinnytech.futures.utils.DividerItemDecorationUtils;

import java.util.ArrayList;
import java.util.List;

import static com.shinnytech.futures.constants.CommonConstants.SETTING;

public class SettingActivity extends BaseActivity {

    private ActivitySettingBinding mBinding;
    private SettingAdapter mSettingAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mLayoutID = R.layout.activity_setting;
        mTitle = SETTING;
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void initData() {
        mBinding = (ActivitySettingBinding) mViewDataBinding;
        mBinding.settingRv.setLayoutManager(new LinearLayoutManager(this));
        mBinding.settingRv.setItemAnimator(new DefaultItemAnimator());
        List<SettingEntity> settingEntities = new ArrayList<>();
        SettingEntity settingEntity = new SettingEntity();
        settingEntity.setTitle(CommonConstants.CHART_SETTING);
        settingEntity.setIcon(R.mipmap.ic_timeline_white_24dp);
        settingEntity.setContent(CommonConstants.PARA_CHANGE);
        settingEntity.setJump(true);

        SettingEntity settingEntity1 = new SettingEntity();
        settingEntity1.setTitle("");
        settingEntity1.setIcon(R.mipmap.ic_watch_later_white_24dp);
        settingEntity1.setContent(CommonConstants.KLINE_DURATION);
        settingEntity1.setJump(true);

        SettingEntity settingEntity2 = new SettingEntity();
        settingEntity2.setTitle(CommonConstants.TRANSACTION_SETTING);
        settingEntity2.setIcon(R.mipmap.ic_speaker_notes_white_24dp);
        settingEntity2.setContent(CommonConstants.ORDER_CONFIRM);
        settingEntity2.setJump(false);

        settingEntities.add(settingEntity);
        settingEntities.add(settingEntity1);
        settingEntities.add(settingEntity2);
        mSettingAdapter = new SettingAdapter(this, settingEntities);
        mBinding.settingRv.setAdapter(mSettingAdapter);
    }

    @Override
    protected void initEvent() {
        mSettingAdapter.setSettingItemClickListener(new SettingAdapter.SettingItemClickListener() {
            @Override
            public void onJump(String content) {
                switch (content){
                    case CommonConstants.PARA_CHANGE:
                        Intent paraIntent = new Intent(SettingActivity.this, ParaChangeActivity.class);
                        startActivity(paraIntent);
                        break;
                    case CommonConstants.KLINE_DURATION:
                        Intent klineIntent = new Intent(SettingActivity.this, KlineDurationActivity.class);
                        startActivity(klineIntent);
                        break;
                    default:
                        break;
                }

            }
        });
    }

    @Override
    protected void refreshUI() {

    }

}

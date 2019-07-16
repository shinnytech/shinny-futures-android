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

import java.util.ArrayList;
import java.util.List;

import static com.shinnytech.futures.constants.CommonConstants.CHART_SETTING;
import static com.shinnytech.futures.constants.CommonConstants.OPTIONAL_SETTING;
import static com.shinnytech.futures.constants.CommonConstants.SETTING;
import static com.shinnytech.futures.constants.CommonConstants.SUB_SETTING_TYPE;
import static com.shinnytech.futures.constants.CommonConstants.SYSTEM_SETTING;
import static com.shinnytech.futures.constants.CommonConstants.TRANSACTION_SETTING;

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
        List<String> titles = new ArrayList<>();
        titles.add(CommonConstants.CHART_SETTING);
        titles.add(TRANSACTION_SETTING);
        titles.add(OPTIONAL_SETTING);
        titles.add(SYSTEM_SETTING);

        List<SettingEntity> settingEntities = new ArrayList<>();
        SettingEntity settingEntity = new SettingEntity();
        settingEntity.setContent(CommonConstants.CHART_SETTING);
        settingEntity.setIcon(R.mipmap.ic_timeline_white_24dp);
        settingEntity.setJump(true);

        SettingEntity settingEntity1 = new SettingEntity();
        settingEntity1.setContent(TRANSACTION_SETTING);
        settingEntity1.setIcon(R.mipmap.ic_speaker_notes_white_24dp);
        settingEntity1.setJump(true);

        SettingEntity settingEntity2 = new SettingEntity();
        settingEntity2.setContent(SYSTEM_SETTING);
        settingEntity2.setIcon(R.mipmap.ic_backup_white_24dp);
        settingEntity2.setJump(true);

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
                Intent subIntent = new Intent(SettingActivity.this, SubSettingActivity.class);
                switch (content) {
                    case CHART_SETTING:
                        subIntent.putExtra(SUB_SETTING_TYPE, CHART_SETTING);
                        break;
                    case TRANSACTION_SETTING:
                        subIntent.putExtra(SUB_SETTING_TYPE, TRANSACTION_SETTING);
                        break;
                    case SYSTEM_SETTING:
                        subIntent.putExtra(SUB_SETTING_TYPE, SYSTEM_SETTING);
                        break;
                    default:
                        break;
                }
                startActivity(subIntent);
            }
        });
    }
}

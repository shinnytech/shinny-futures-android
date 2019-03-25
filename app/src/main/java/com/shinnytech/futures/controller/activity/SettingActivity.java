package com.shinnytech.futures.controller.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.text.TextUtils;

import com.aliyun.sls.android.sdk.LOGClient;
import com.aliyun.sls.android.sdk.LogEntity;
import com.aliyun.sls.android.sdk.LogException;
import com.aliyun.sls.android.sdk.SLSDatabaseManager;
import com.aliyun.sls.android.sdk.SLSLog;
import com.aliyun.sls.android.sdk.core.callback.CompletedCallback;
import com.aliyun.sls.android.sdk.model.Log;
import com.aliyun.sls.android.sdk.model.LogGroup;
import com.aliyun.sls.android.sdk.request.PostLogRequest;
import com.aliyun.sls.android.sdk.result.PostLogResult;
import com.shinnytech.futures.R;
import com.shinnytech.futures.application.BaseApplication;
import com.shinnytech.futures.constants.CommonConstants;
import com.shinnytech.futures.databinding.ActivitySettingBinding;
import com.shinnytech.futures.model.adapter.SettingAdapter;
import com.shinnytech.futures.model.bean.settingbean.SettingEntity;
import com.shinnytech.futures.utils.LogUtils;
import com.shinnytech.futures.utils.ToastNotificationUtils;

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
        settingEntity1.setContent(CommonConstants.KLINE_DURATION_SETTING);
        settingEntity1.setJump(true);

        SettingEntity settingEntity2 = new SettingEntity();
        settingEntity2.setTitle(CommonConstants.TRANSACTION_SETTING);
        settingEntity2.setIcon(R.mipmap.ic_speaker_notes_white_24dp);
        settingEntity2.setContent(CommonConstants.ORDER_CONFIRM);
        settingEntity2.setJump(false);

        SettingEntity settingEntity3 = new SettingEntity();
        settingEntity3.setTitle(CommonConstants.SYSTEM_SETTING);
        settingEntity3.setIcon(R.mipmap.ic_backup_white_24dp);
        settingEntity3.setContent(CommonConstants.UPLOAD_LOG);
        settingEntity3.setJump(true);

        settingEntities.add(settingEntity);
        settingEntities.add(settingEntity1);
        settingEntities.add(settingEntity2);
        settingEntities.add(settingEntity3);
        mSettingAdapter = new SettingAdapter(this, settingEntities);
        mBinding.settingRv.setAdapter(mSettingAdapter);
    }

    @Override
    protected void initEvent() {
        mSettingAdapter.setSettingItemClickListener(new SettingAdapter.SettingItemClickListener() {
            @Override
            public void onJump(String content) {
                switch (content) {
                    case CommonConstants.PARA_CHANGE:
                        Intent paraIntent = new Intent(SettingActivity.this, ParaChangeActivity.class);
                        startActivity(paraIntent);
                        break;
                    case CommonConstants.KLINE_DURATION_SETTING:
                        Intent klineIntent = new Intent(SettingActivity.this, KlineDurationActivity.class);
                        startActivity(klineIntent);
                        break;
                    case CommonConstants.UPLOAD_LOG:
                        upload();
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

    private void upload(){
        /* 创建logGroup */
        final LogGroup logGroup = new LogGroup("user log", "V"+sDataManager.APP_VERSION + " User Id:" + sDataManager.USER_ID);

        List<LogEntity> list = SLSDatabaseManager.getInstance().queryRecordFromDB();
        for (LogEntity logEntity: list) {
            /* 存入一条log */
            Log log = new Log();
            log.PutContent("timeStamp", "" + logEntity.getTimestamp() / 1000);
            log.PutContent("content", logEntity.getJsonString());
            LogUtils.e(logEntity.getJsonString(), true);
            logGroup.PutLog(log);
        }

        try {
            PostLogRequest request = new PostLogRequest("kq-xq", "kq-xq", logGroup);
            LOGClient logClient = BaseApplication.getLOGClient();
            if (logClient == null)return;
            logClient.asyncPostLog(request, new CompletedCallback<PostLogRequest, PostLogResult>() {
                @Override
                public void onSuccess(PostLogRequest request, PostLogResult result) {
                    ToastNotificationUtils.showToast(sContext, "日志上传成功");
                    LogUtils.e("111", true);
                }

                @Override
                public void onFailure(PostLogRequest request, LogException exception) {
                    ToastNotificationUtils.showToast(sContext, "日志上传失败");
                    LogUtils.e("222", true);
                }

            });
        } catch (LogException e) {
            e.printStackTrace();
        }
    }

}

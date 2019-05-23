package com.shinnytech.futures.controller.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.text.format.DateFormat;

import com.aliyun.sls.android.sdk.LOGClient;
import com.aliyun.sls.android.sdk.LogEntity;
import com.aliyun.sls.android.sdk.LogException;
import com.aliyun.sls.android.sdk.SLSDatabaseManager;
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
import com.shinnytech.futures.utils.ToastUtils;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import static com.shinnytech.futures.constants.CommonConstants.CHART_SETTING;
import static com.shinnytech.futures.constants.CommonConstants.SUB_SETTING_TYPE;
import static com.shinnytech.futures.constants.CommonConstants.SYSTEM_SETTING;
import static com.shinnytech.futures.constants.CommonConstants.TRANSACTION_SETTING;

public class SubSettingActivity extends BaseActivity {

    public final static int HANDLER_MESSAGE_UPLOAD_FAILED = 00011;
    public final static int HANDLER_MESSAGE_UPLOAD_SUCCESS = 00012;
    private ActivitySettingBinding mBinding;
    private SettingAdapter mSettingAdapter;
    private MyHandler myHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mLayoutID = R.layout.activity_setting;
        String title = getIntent().getStringExtra(SUB_SETTING_TYPE);
        mTitle = title;
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void initData() {
        mBinding = (ActivitySettingBinding) mViewDataBinding;
        mBinding.settingRv.setLayoutManager(new LinearLayoutManager(this));
        mBinding.settingRv.setItemAnimator(new DefaultItemAnimator());
        List<SettingEntity> settingEntities = new ArrayList<>();

        switch (mTitle) {
            case CHART_SETTING:
                SettingEntity settingEntity = new SettingEntity();
                settingEntity.setIcon(R.mipmap.ic_timeline_white_24dp);
                settingEntity.setContent(CommonConstants.PARA_CHANGE);
                settingEntity.setJump(true);
                SettingEntity settingEntity1 = new SettingEntity();
                settingEntity1.setIcon(R.mipmap.ic_watch_later_white_24dp);
                settingEntity1.setContent(CommonConstants.KLINE_DURATION_SETTING);
                settingEntity1.setJump(true);
                SettingEntity settingEntity4 = new SettingEntity();
                settingEntity4.setIcon(R.mipmap.ic_watch_later_white_24dp);
                settingEntity4.setContent(CommonConstants.COMMON_SWITCH_SETTING);
                settingEntity4.setJump(true);
                settingEntities.add(settingEntity);
                settingEntities.add(settingEntity1);
                settingEntities.add(settingEntity4);
                break;
            case TRANSACTION_SETTING:
                SettingEntity settingEntity2 = new SettingEntity();
                settingEntity2.setIcon(R.mipmap.ic_speaker_notes_white_24dp);
                settingEntity2.setContent(CommonConstants.INSERT_ORDER_CONFIRM);
                settingEntity2.setJump(false);
                settingEntities.add(settingEntity2);
                SettingEntity settingEntity5 = new SettingEntity();
                settingEntity5.setIcon(R.mipmap.ic_speaker_notes_white_24dp);
                settingEntity5.setContent(CommonConstants.CANCEL_ORDER_CONFIRM);
                settingEntity5.setJump(false);
                settingEntities.add(settingEntity5);
                break;
            case SYSTEM_SETTING:
                SettingEntity settingEntity3 = new SettingEntity();
                settingEntity3.setIcon(R.mipmap.ic_backup_white_24dp);
                settingEntity3.setContent(CommonConstants.UPLOAD_LOG);
                settingEntity3.setJump(true);
                settingEntities.add(settingEntity3);
                break;
            default:
                break;
        }

        mSettingAdapter = new SettingAdapter(this, settingEntities);
        mBinding.settingRv.setAdapter(mSettingAdapter);

        myHandler = new MyHandler(this);
    }

    @Override
    protected void initEvent() {
        mSettingAdapter.setSettingItemClickListener(new SettingAdapter.SettingItemClickListener() {
            @Override
            public void onJump(String content) {
                switch (content) {
                    case CommonConstants.PARA_CHANGE:
                        Intent paraIntent = new Intent(SubSettingActivity.this, ParaChangeActivity.class);
                        startActivity(paraIntent);
                        break;
                    case CommonConstants.KLINE_DURATION_SETTING:
                        Intent klineIntent = new Intent(SubSettingActivity.this, KlineDurationActivity.class);
                        startActivity(klineIntent);
                        break;
                    case CommonConstants.COMMON_SWITCH_SETTING:
                        Intent switchIntent = new Intent(SubSettingActivity.this, CommonSwitchActivity.class);
                        startActivity(switchIntent);
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

    private void upload() {
        /* 创建logGroup */
        final LogGroup logGroup = new LogGroup("user log", "V" + sDataManager.APP_VERSION + " User Id: " + sDataManager.USER_ID);

        List<LogEntity> list = SLSDatabaseManager.getInstance().queryRecordFromDB();
        for (LogEntity logEntity : list) {
            Log log = new Log();
            log.PutContent("timeStamp", getDate(logEntity.getTimestamp()));
            log.PutContent("content", logEntity.getJsonString());
            logGroup.PutLog(log);
        }

        try {
            PostLogRequest request = new PostLogRequest("kq-xq", "kq-xq", logGroup);
            LOGClient logClient = BaseApplication.getLOGClient();
            if (logClient == null) return;
            logClient.asyncPostLog(request, new CompletedCallback<PostLogRequest, PostLogResult>() {
                @Override
                public void onSuccess(PostLogRequest request, PostLogResult result) {
                    Message message = new Message();
                    message.what = HANDLER_MESSAGE_UPLOAD_SUCCESS;
                    myHandler.sendMessage(message);
//                    List<LogEntity> list = SLSDatabaseManager.getInstance().queryRecordFromDB();
//                    if (list != null && list.size() > 1000){
//                        for (int i = 0; i < 500; i++){
//                            LogEntity logEntity = list.get(i);
//                            SLSDatabaseManager.getInstance().deleteRecordFromDB(logEntity);
//                        }
//                    }
                }

                @Override
                public void onFailure(PostLogRequest request, LogException exception) {
                    Message message = new Message();
                    message.what = HANDLER_MESSAGE_UPLOAD_FAILED;
                    myHandler.sendMessage(message);
                }

            });
        } catch (LogException e) {
            e.printStackTrace();
        }
    }

    private String getDate(long time) {
        Calendar cal = Calendar.getInstance(Locale.CHINA);
        cal.setTimeInMillis(time);
        String date = DateFormat.format("yyyy-MM-dd HH:mm:ss", cal).toString();
        return date;
    }

    static class MyHandler extends Handler {
        WeakReference<SubSettingActivity> mActivityReference;

        MyHandler(SubSettingActivity activity) {
            mActivityReference = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            final SubSettingActivity activity = mActivityReference.get();
            if (activity != null) {
                switch (msg.what) {
                    case HANDLER_MESSAGE_UPLOAD_SUCCESS:
                        ToastUtils.showToast(BaseApplication.getContext(), "日志上传成功");
                        break;
                    case HANDLER_MESSAGE_UPLOAD_FAILED:
                        ToastUtils.showToast(BaseApplication.getContext(), "日志上传失败");
                    default:
                        break;
                }
            }
        }
    }

}

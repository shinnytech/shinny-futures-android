package com.shinnytech.futures.controller.activity;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.DatePicker;

import com.shinnytech.futures.R;
import com.shinnytech.futures.amplitude.api.Amplitude;
import com.shinnytech.futures.application.BaseApplication;
import com.shinnytech.futures.databinding.ActivityHistoryConditionBinding;
import com.shinnytech.futures.model.adapter.ConditionOrderAdapter;
import com.shinnytech.futures.model.bean.conditionorderbean.ConditionOrderEntity;
import com.shinnytech.futures.model.bean.conditionorderbean.ConditionUserEntity;
import com.shinnytech.futures.model.listener.ConditionOrderDiffCallback;
import com.shinnytech.futures.utils.CloneUtils;
import com.shinnytech.futures.utils.DividerItemDecorationUtils;
import com.shinnytech.futures.utils.TimeUtils;
import com.shinnytech.futures.utils.ToastUtils;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import static com.shinnytech.futures.application.BaseApplication.CO_BROADCAST_ACTION;
import static com.shinnytech.futures.constants.AmpConstants.AMP_CONDITION_QUERY;
import static com.shinnytech.futures.constants.BroadcastConstants.CO_HIS_MESSAGE;
import static com.shinnytech.futures.constants.CommonConstants.HISTORY_CONDITIONAL_ORDER;
import static com.shinnytech.futures.utils.TimeUtils.YMD_FORMAT_4;

public class HistoryConditionActivity extends BaseActivity {
    private ActivityHistoryConditionBinding mBinding;
    private Context sContext;
    private ConditionOrderAdapter mAdapter;
    private List<ConditionOrderEntity> mOldData = new ArrayList<>();
    private List<ConditionOrderEntity> mNewData = new ArrayList<>();
    private BroadcastReceiver mReceiverCondition;
    private Dialog mDialog;
    private Timer mTimer;
    private DatePickerDialog mDateDialog;
    private Dialog mResponsibilityDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mLayoutID = R.layout.activity_history_condition;
        mTitle = HISTORY_CONDITIONAL_ORDER;
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void initData() {
        mBinding = (ActivityHistoryConditionBinding) mViewDataBinding;
        sContext = BaseApplication.getContext();
        mBinding.rv.setLayoutManager(new LinearLayoutManager(this));
        mBinding.rv.addItemDecoration(
                new DividerItemDecorationUtils(this, DividerItemDecorationUtils.VERTICAL_LIST));
        mBinding.rv.setItemAnimator(new DefaultItemAnimator());
        mAdapter = new ConditionOrderAdapter(this, mOldData);
        mBinding.rv.setAdapter(mAdapter);

        mBinding.textViewActionDay.setText(
                TimeUtils.date2String(Calendar.getInstance().getTime(), YMD_FORMAT_4));
    }

    @Override
    protected void initEvent() {
        registerBroaderCast();

        mBinding.buttonQuery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    int time = Integer.parseInt(mBinding.textViewActionDay.getText().toString());
                    BaseApplication.getmTDWebSocket().sendReqQueryConditionOrder(time);

                    Amplitude.getInstance().logEventWrap(AMP_CONDITION_QUERY, new JSONObject());

                    if (mDialog == null){
                        mDialog = new Dialog(HistoryConditionActivity.this, R.style.Theme_Light_Dialog);
                        View view = View.inflate(HistoryConditionActivity.this, R.layout.view_dialog_query_his_condition, null);
                        Window dialogWindow = mDialog.getWindow();
                        if (dialogWindow != null) {
                            dialogWindow.getDecorView().setPadding(0, 0, 0, 0);
                            WindowManager.LayoutParams lp = dialogWindow.getAttributes();
                            dialogWindow.setGravity(Gravity.CENTER);
                            lp.width = (int) HistoryConditionActivity.this.getResources().getDimension(R.dimen.optional_dialog_width);
                            lp.height = (int) HistoryConditionActivity.this.getResources().getDimension(R.dimen.optional_dialog_height);
                            dialogWindow.setAttributes(lp);
                        }
                        mDialog.setContentView(view);
                        mDialog.setCancelable(false);
                    }
                    if (!mDialog.isShowing())mDialog.show();
                    mTimer = new Timer();
                    mTimer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            new Handler(Looper.getMainLooper()).post(new Runnable() {
                                @Override
                                public void run() {
                                    if (mTimer != null) {
                                        mTimer.cancel();
                                        mTimer = null;
                                    }
                                    ToastUtils.showToast(sContext, "查询超时");
                                    mDialog.dismiss();
                                }
                            });
                        }
                    }, 20000);
                }catch (NumberFormatException e){
                    ToastUtils.showToast(sContext, "日期输入不合法");
                }
            }
        });

        mBinding.textViewActionDay.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                final Calendar calendar = Calendar.getInstance();
                if (mDateDialog == null)
                    mDateDialog = new DatePickerDialog(HistoryConditionActivity.this,
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                                calendar.set(Calendar.YEAR, year);
                                calendar.set(Calendar.MONTH, month);
                                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                                String time = TimeUtils.date2String(calendar.getTime(), YMD_FORMAT_4);
                                mBinding.textViewActionDay.setText(time);
                            }
                        },
                        calendar.get(Calendar.YEAR),
                        calendar.get(Calendar.MONTH),
                        calendar.get(Calendar.DAY_OF_MONTH));
                if (!mDateDialog.isShowing())mDateDialog.show();
            }
        });

        mBinding.textViewResponsibility.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mResponsibilityDialog == null){
                    mResponsibilityDialog = new Dialog(HistoryConditionActivity.this, R.style.AppTheme);
                    View view = View.inflate(HistoryConditionActivity.this, R.layout.view_dialog_condition_order_responsibility, null);
                    mResponsibilityDialog.setContentView(view);
                    mResponsibilityDialog.setCanceledOnTouchOutside(false);
                    mResponsibilityDialog.setCancelable(false);
                    view.findViewById(R.id.agree).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            mResponsibilityDialog.dismiss();
                        }
                    });
                }
                if (!mResponsibilityDialog.isShowing())mResponsibilityDialog.show();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mReceiverCondition != null)LocalBroadcastManager.getInstance(this).unregisterReceiver(mReceiverCondition);
    }

    /**
     * date: 2019/8/10
     * author: chenli
     * description: 历史条件单只能查前面一个交易日的：
     */
    public void refreshCO() {
        try {
            if (mTimer != null) {
                mTimer.cancel();
                mTimer = null;
            }
            mDialog.dismiss();
            ConditionUserEntity userEntity = sDataManager.getHisConditionOrderBean().getUsers().get(sDataManager.USER_ID);
            if (userEntity == null)return;
            Map<String, ConditionOrderEntity> condition_orders = userEntity.getCondition_orders();
            mNewData.clear();
            for (ConditionOrderEntity conditionOrderEntity :
                    condition_orders.values()) {
                long dateTime = Long.parseLong(conditionOrderEntity.getInsert_date_time());
                String date = TimeUtils.date2String(new Date(dateTime * 1000), YMD_FORMAT_4);
                if (!mBinding.textViewActionDay.getText().toString().equals(date))continue;
                ConditionOrderEntity t = CloneUtils.clone(conditionOrderEntity);
                mNewData.add(t);
            }
            if (mNewData.isEmpty())ToastUtils.showToast(sContext, "暂无记录");
            Collections.sort(mNewData);
            DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new ConditionOrderDiffCallback(mOldData, mNewData), false);
            mAdapter.setData(mNewData);
            diffResult.dispatchUpdatesTo(mAdapter);
            mOldData.clear();
            mOldData.addAll(mNewData);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * date: 2019/8/10
     * author: chenli
     * description: 注册条件单广播
     */
    private void registerBroaderCast() {
        mReceiverCondition = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String mDataString = intent.getStringExtra("msg");
                switch (mDataString) {
                    case CO_HIS_MESSAGE:
                        refreshCO();
                        break;
                    default:
                        break;
                }
            }
        };
        LocalBroadcastManager.getInstance(this).registerReceiver(mReceiverCondition, new IntentFilter(CO_BROADCAST_ACTION));
    }
}

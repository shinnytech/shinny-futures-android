package com.shinnytech.futures.controller.activity;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.shinnytech.futures.R;
import com.shinnytech.futures.amplitude.api.Amplitude;
import com.shinnytech.futures.application.BaseApplication;
import com.shinnytech.futures.databinding.ActivityManagerConditionOrderBinding;
import com.shinnytech.futures.model.adapter.ConditionOrderAdapter;
import com.shinnytech.futures.model.bean.conditionorderbean.ConditionOrderEntity;
import com.shinnytech.futures.model.bean.conditionorderbean.ConditionUserEntity;
import com.shinnytech.futures.model.engine.LatestFileManager;
import com.shinnytech.futures.model.listener.ConditionOrderDiffCallback;
import com.shinnytech.futures.model.listener.SimpleRecyclerViewItemClickListener;
import com.shinnytech.futures.utils.CloneUtils;
import com.shinnytech.futures.utils.DividerItemDecorationUtils;
import com.shinnytech.futures.utils.ScreenUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.shinnytech.futures.application.BaseApplication.CO_BROADCAST_ACTION;
import static com.shinnytech.futures.constants.AmpConstants.AMP_CONDITION_ADD;
import static com.shinnytech.futures.constants.AmpConstants.AMP_CONDITION_EDIT;
import static com.shinnytech.futures.constants.AmpConstants.AMP_CONDITION_HISTORY;
import static com.shinnytech.futures.constants.AmpConstants.AMP_EVENT_CONDITION_EDIT_ACTION_TYPE;
import static com.shinnytech.futures.constants.AmpConstants.AMP_EVENT_CONDITION_EDIT_ACTION_TYPE_DELETE;
import static com.shinnytech.futures.constants.AmpConstants.AMP_EVENT_CONDITION_EDIT_ACTION_TYPE_PAUSE;
import static com.shinnytech.futures.constants.AmpConstants.AMP_EVENT_CONDITION_EDIT_ACTION_TYPE_START;
import static com.shinnytech.futures.constants.ConditionConstants.CONDITION_STATUS_LIVE;
import static com.shinnytech.futures.constants.ConditionConstants.CONDITION_STATUS_SUSPEND;
import static com.shinnytech.futures.constants.BroadcastConstants.CO_MESSAGE;
import static com.shinnytech.futures.constants.CommonConstants.INS_BETWEEN_ACTIVITY;
import static com.shinnytech.futures.constants.CommonConstants.MANAGER_CONDITIONAL_ORDER;
import static com.shinnytech.futures.constants.ServerConstants.REQ_CANCEL_CONDITION_ORDER;
import static com.shinnytech.futures.constants.ServerConstants.REQ_PAUSE_CONDITION_ORDER;
import static com.shinnytech.futures.constants.ServerConstants.REQ_RESUME_CONDITION_ORDER;

public class ManagerConditionOrderActivity extends BaseActivity {
    private ActivityManagerConditionOrderBinding mBinding;
    private Context sContext;
    private ConditionOrderAdapter mAdapter;
    private List<ConditionOrderEntity> mOldData = new ArrayList<>();
    private List<ConditionOrderEntity> mNewData = new ArrayList<>();
    private BroadcastReceiver mReceiverCondition;
    private Dialog mResponsibilityDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mLayoutID = R.layout.activity_manager_condition_order;
        mTitle = MANAGER_CONDITIONAL_ORDER;
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void initData() {
        mBinding = (ActivityManagerConditionOrderBinding) mViewDataBinding;
        sContext = BaseApplication.getContext();
        mBinding.rv.setLayoutManager(new LinearLayoutManager(this));
        mBinding.rv.addItemDecoration(
                new DividerItemDecorationUtils(this, DividerItemDecorationUtils.VERTICAL_LIST));
        mBinding.rv.setItemAnimator(new DefaultItemAnimator());
        mAdapter = new ConditionOrderAdapter(this, mOldData);
        mBinding.rv.setAdapter(mAdapter);
    }

    @Override
    protected void initEvent() {
        mBinding.historyCondition.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Amplitude.getInstance().logEventWrap(AMP_CONDITION_HISTORY, new JSONObject());
                Intent intent = new Intent(ManagerConditionOrderActivity.this, HistoryConditionActivity.class);
                ManagerConditionOrderActivity.this.startActivity(intent);
            }
        });

        mBinding.newCondition.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Amplitude.getInstance().logEventWrap(AMP_CONDITION_ADD, new JSONObject());
                Intent intent = new Intent(ManagerConditionOrderActivity.this, ConditionOrderActivity.class);
                String ins;
                List<String> optional = LatestFileManager.readInsListFromFile();
                if (optional.isEmpty()) ins = (new ArrayList<>(LatestFileManager.getMainInsList().keySet())).get(0);
                else ins = optional.get(0);
                intent.putExtra(INS_BETWEEN_ACTIVITY, ins);
                ManagerConditionOrderActivity.this.startActivity(intent);
            }
        });

        mBinding.textViewResponsibility.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mResponsibilityDialog == null){
                    mResponsibilityDialog = new Dialog(ManagerConditionOrderActivity.this, R.style.AppTheme);
                    View view = View.inflate(ManagerConditionOrderActivity.this, R.layout.view_dialog_condition_order_responsibility, null);
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

        mBinding.rv.addOnItemTouchListener(new SimpleRecyclerViewItemClickListener(mBinding.rv, new SimpleRecyclerViewItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                if (position >= 0 && position < mAdapter.getItemCount()) {
                    ConditionOrderEntity conditionOrderEntity = mAdapter.getData().get(position);
                    initPopUp(view, conditionOrderEntity);
                }
            }

            @Override
            public void onItemLongClick(View view, int position) {
            }
        }));

        registerBroaderCast();
    }

    /**
     * date: 2019/8/10
     * author: chenli
     * description:
     */
    public void refreshCO() {
        try {
            ConditionUserEntity userEntity = sDataManager.getConditionOrderBean().getUsers().get(sDataManager.USER_ID);
            if (userEntity == null)return;
            Map<String, ConditionOrderEntity> condition_orders = userEntity.getCondition_orders();
            mNewData.clear();
            for (ConditionOrderEntity conditionOrderEntity :
                    condition_orders.values()) {
                ConditionOrderEntity t = CloneUtils.clone(conditionOrderEntity);
                mNewData.add(t);
            }
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

    @Override
    protected void onResume() {
        super.onResume();
        refreshCO();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mReceiverCondition != null)LocalBroadcastManager.getInstance(this).unregisterReceiver(mReceiverCondition);
    }

    /**
     * date: 2019/8/10
     * author: chenli
     * description: 删除/暂停/启动
     */
    private void initPopUp(final View view, final ConditionOrderEntity conditionOrderEntity) {
        final View popUpView = View.inflate(this, R.layout.popup_activity_condition_manager, null);
        final PopupWindow popWindow = new PopupWindow(popUpView,
                ViewGroup.LayoutParams.MATCH_PARENT, ScreenUtils.dp2px(this, 42), true);
        //设置动画，淡入淡出
        popWindow.setAnimationStyle(R.style.anim_menu_quote);
        //点击空白处popupWindow消失
        popWindow.setBackgroundDrawable(new ColorDrawable(0x00000000));
        TextView cancel = popUpView.findViewById(R.id.cancel_condition_order);
        final TextView pause = popUpView.findViewById(R.id.pause_condition_order);
        //设置popupWindow显示的位置，参数依次是参照View，x轴的偏移量，y轴的偏移量
        DisplayMetrics outMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(outMetrics);
        popWindow.showAsDropDown(view, outMetrics.widthPixels / 4 * 3, 0);
        final String order_id = conditionOrderEntity.getOrder_id();
        final String status = conditionOrderEntity.getStatus();
        switch (status){
            case CONDITION_STATUS_LIVE:
                pause.setText("暂停");
                break;
            case CONDITION_STATUS_SUSPEND:
                pause.setText("启动");
                break;
            default:
                break;
        }

        JSONObject jsonObject = new JSONObject();

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    jsonObject.put(AMP_EVENT_CONDITION_EDIT_ACTION_TYPE, AMP_EVENT_CONDITION_EDIT_ACTION_TYPE_DELETE);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                Amplitude.getInstance().logEventWrap(AMP_CONDITION_EDIT, jsonObject);
                BaseApplication.getmTDWebSocket().sendReqControlConditionOrder(
                        REQ_CANCEL_CONDITION_ORDER, order_id);
                popWindow.dismiss();
            }
        });

        pause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (status){
                    case CONDITION_STATUS_LIVE:
                        try {
                            jsonObject.put(AMP_EVENT_CONDITION_EDIT_ACTION_TYPE, AMP_EVENT_CONDITION_EDIT_ACTION_TYPE_PAUSE);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        Amplitude.getInstance().logEventWrap(AMP_CONDITION_EDIT, jsonObject);
                        BaseApplication.getmTDWebSocket().sendReqControlConditionOrder(
                                REQ_PAUSE_CONDITION_ORDER, order_id);
                        break;
                    case CONDITION_STATUS_SUSPEND:
                        try {
                            jsonObject.put(AMP_EVENT_CONDITION_EDIT_ACTION_TYPE, AMP_EVENT_CONDITION_EDIT_ACTION_TYPE_START);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        Amplitude.getInstance().logEventWrap(AMP_CONDITION_EDIT, jsonObject);
                        BaseApplication.getmTDWebSocket().sendReqControlConditionOrder(
                                REQ_RESUME_CONDITION_ORDER, order_id);
                        break;
                    default:
                        break;
                }
                popWindow.dismiss();
            }
        });
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
                    case CO_MESSAGE:
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

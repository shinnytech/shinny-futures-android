package com.shinnytech.futures.controller.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.shinnytech.futures.R;
import com.shinnytech.futures.application.BaseApplication;
import com.shinnytech.futures.controller.activity.FutureInfoActivity;
import com.shinnytech.futures.databinding.FragmentHandicapBinding;
import com.shinnytech.futures.amplitude.api.Amplitude;
import com.shinnytech.futures.model.bean.accountinfobean.AccountEntity;
import com.shinnytech.futures.model.bean.accountinfobean.OrderEntity;
import com.shinnytech.futures.model.bean.accountinfobean.PositionEntity;
import com.shinnytech.futures.model.bean.accountinfobean.UserEntity;
import com.shinnytech.futures.model.bean.eventbusbean.IdEvent;
import com.shinnytech.futures.model.bean.futureinfobean.QuoteEntity;
import com.shinnytech.futures.model.engine.DataManager;
import com.shinnytech.futures.model.engine.LatestFileManager;
import com.shinnytech.futures.utils.CloneUtils;
import com.shinnytech.futures.utils.LogUtils;
import com.shinnytech.futures.utils.MathUtils;
import com.shinnytech.futures.utils.SPUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.json.JSONException;
import org.json.JSONObject;

import static com.shinnytech.futures.constants.CommonConstants.AMP_EVENT_BALANCE;
import static com.shinnytech.futures.constants.CommonConstants.AMP_EVENT_BROKER_ID;
import static com.shinnytech.futures.constants.CommonConstants.AMP_EVENT_IS_INS_IN_OPTIONAL;
import static com.shinnytech.futures.constants.CommonConstants.AMP_EVENT_IS_INS_IN_POSITION;
import static com.shinnytech.futures.constants.CommonConstants.AMP_EVENT_IS_POSITIVE;
import static com.shinnytech.futures.constants.CommonConstants.AMP_EVENT_ORDER_COUNT;
import static com.shinnytech.futures.constants.CommonConstants.AMP_EVENT_PAGE_ID;
import static com.shinnytech.futures.constants.CommonConstants.AMP_EVENT_PAGE_ID_VALUE_FUTURE_INFO;
import static com.shinnytech.futures.constants.CommonConstants.AMP_EVENT_PAGE_VISIBLE_TIME;
import static com.shinnytech.futures.constants.CommonConstants.AMP_EVENT_POSITION_COUNT;
import static com.shinnytech.futures.constants.CommonConstants.AMP_EVENT_SUB_PAGE_ID;
import static com.shinnytech.futures.constants.CommonConstants.AMP_EVENT_SUB_PAGE_ID_VALUE_HANDICAP;
import static com.shinnytech.futures.constants.CommonConstants.AMP_LEAVE_PAGE;
import static com.shinnytech.futures.constants.CommonConstants.AMP_SHOW_PAGE;
import static com.shinnytech.futures.constants.CommonConstants.CONFIG_BROKER;
import static com.shinnytech.futures.constants.CommonConstants.MD_MESSAGE;
import static com.shinnytech.futures.constants.CommonConstants.STATUS_ALIVE;
import static com.shinnytech.futures.service.WebSocketService.MD_BROADCAST_ACTION;

/**
 * date: 7/9/17
 * author: chenli
 * description: 盘口信息页
 * version:
 * state: done
 */
public class HandicapFragment extends LazyLoadFragment {
    private DataManager sDataManager = DataManager.getInstance();
    private BroadcastReceiver mReceiver;
    private String mInstrumentId;
    private FragmentHandicapBinding mBinding;
    private long mShowTime;

    /**
     * date: 7/9/17
     * author: chenli
     * description: 实时刷新盘口信息
     */
    private void refreshUI() {
        QuoteEntity quoteEntity = sDataManager.getRtnData().getQuotes().get(mInstrumentId);
        if (quoteEntity == null) return;
        if (mInstrumentId.contains("&") && mInstrumentId.contains(" ")) {
            quoteEntity = CloneUtils.clone(quoteEntity);
            quoteEntity = LatestFileManager.calculateCombineQuoteFull(quoteEntity);
        }
        mBinding.setHandicap(quoteEntity);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_handicap, container, false);
        EventBus.getDefault().register(this);
        return mBinding.getRoot();
    }

    private void registerBroaderCast() {
        mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String mDataString = intent.getStringExtra("msg");
                switch (mDataString) {
                    case MD_MESSAGE:
                        if (((FutureInfoActivity) getActivity()).getTabsInfo().getCheckedRadioButtonId()
                                == R.id.rb_handicap_info) refreshUI();
                        break;
                    default:
                        break;
                }
            }
        };
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mReceiver, new IntentFilter(MD_BROADCAST_ACTION));
    }

    /**
     * date: 7/9/17
     * author: chenli
     * description: 初始化时获取合约代码
     */
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mInstrumentId = ((FutureInfoActivity) getActivity()).getInstrument_id();
    }

    @Override
    public void show() {
        try {
            refreshUI();
            showEvent();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void showEvent() {
        try {
            LogUtils.e("showHandicap", true);
            mShowTime = System.currentTimeMillis();
            String broker_id = (String) SPUtils.get(BaseApplication.getContext(), CONFIG_BROKER, "");
            JSONObject jsonObject = new JSONObject();
            String ins = mInstrumentId;
            boolean isInsInOptional = LatestFileManager.getOptionalInsList().keySet().contains(ins);
            jsonObject.put(AMP_EVENT_IS_INS_IN_OPTIONAL, isInsInOptional);
            jsonObject.put(AMP_EVENT_PAGE_ID, AMP_EVENT_PAGE_ID_VALUE_FUTURE_INFO);
            jsonObject.put(AMP_EVENT_SUB_PAGE_ID, AMP_EVENT_SUB_PAGE_ID_VALUE_HANDICAP);
            jsonObject.put(AMP_EVENT_BROKER_ID, broker_id);
            jsonObject.put(AMP_EVENT_IS_POSITIVE, DataManager.getInstance().IS_POSITIVE);
            UserEntity userEntity = DataManager.getInstance().getTradeBean().getUsers().get(DataManager.getInstance().LOGIN_USER_ID);
            if (userEntity != null) {
                AccountEntity accountEntity = userEntity.getAccounts().get("CNY");
                if (accountEntity != null) {
                    String static_balance = MathUtils.round(accountEntity.getStatic_balance(), 2);
                    jsonObject.put(AMP_EVENT_BALANCE, static_balance);
                    int positionCount = 0;
                    int orderCount = 0;
                    for (PositionEntity positionEntity :
                            userEntity.getPositions().values()) {
                        int volume_long = Integer.parseInt(positionEntity.getVolume_long());
                        int volume_short = Integer.parseInt(positionEntity.getVolume_short());
                        if (volume_long != 0 || volume_short != 0) positionCount += 1;
                    }

                    for (OrderEntity orderEntity :
                            userEntity.getOrders().values()) {
                        if (STATUS_ALIVE.equals(orderEntity.getStatus())) orderCount += 1;
                    }
                    jsonObject.put(AMP_EVENT_POSITION_COUNT, positionCount);
                    jsonObject.put(AMP_EVENT_ORDER_COUNT, orderCount);

                    boolean isInsInPosition = userEntity.getPositions().keySet().contains(ins);
                    jsonObject.put(AMP_EVENT_IS_INS_IN_POSITION, isInsInPosition);
                }
            }
            Amplitude.getInstance().logEvent(AMP_SHOW_PAGE, jsonObject);
            DataManager.getInstance().IS_POSITIVE = false;
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void leave() {
        leaveEvent();
    }

    public void leaveEvent() {
        try {
            LogUtils.e("leaveHandicap", true);
            long pageVisibleTime = System.currentTimeMillis() - mShowTime;
            String broker_id = (String) SPUtils.get(BaseApplication.getContext(), CONFIG_BROKER, "");
            JSONObject jsonObject = new JSONObject();
            String ins = mInstrumentId;
            boolean isInsInOptional = LatestFileManager.getOptionalInsList().keySet().contains(ins);
            jsonObject.put(AMP_EVENT_IS_INS_IN_OPTIONAL, isInsInOptional);
            jsonObject.put(AMP_EVENT_PAGE_ID, AMP_EVENT_PAGE_ID_VALUE_FUTURE_INFO);
            jsonObject.put(AMP_EVENT_SUB_PAGE_ID, AMP_EVENT_SUB_PAGE_ID_VALUE_HANDICAP);
            jsonObject.put(AMP_EVENT_BROKER_ID, broker_id);
            jsonObject.put(AMP_EVENT_IS_POSITIVE, DataManager.getInstance().IS_POSITIVE);
            jsonObject.put(AMP_EVENT_PAGE_VISIBLE_TIME, pageVisibleTime);
            UserEntity userEntity = DataManager.getInstance().getTradeBean().getUsers().get(DataManager.getInstance().LOGIN_USER_ID);
            if (userEntity != null) {
                AccountEntity accountEntity = userEntity.getAccounts().get("CNY");
                if (accountEntity != null) {
                    String static_balance = MathUtils.round(accountEntity.getStatic_balance(), 2);
                    jsonObject.put(AMP_EVENT_BALANCE, static_balance);
                    int positionCount = 0;
                    int orderCount = 0;
                    for (PositionEntity positionEntity :
                            userEntity.getPositions().values()) {
                        int volume_long = Integer.parseInt(positionEntity.getVolume_long());
                        int volume_short = Integer.parseInt(positionEntity.getVolume_short());
                        if (volume_long != 0 || volume_short != 0) positionCount += 1;
                    }

                    for (OrderEntity orderEntity :
                            userEntity.getOrders().values()) {
                        if (STATUS_ALIVE.equals(orderEntity.getStatus())) orderCount += 1;
                    }
                    jsonObject.put(AMP_EVENT_POSITION_COUNT, positionCount);
                    jsonObject.put(AMP_EVENT_ORDER_COUNT, orderCount);

                    boolean isInsInPosition = userEntity.getPositions().keySet().contains(ins);
                    jsonObject.put(AMP_EVENT_IS_INS_IN_POSITION, isInsInPosition);
                }
            }
            Amplitude.getInstance().logEvent(AMP_LEAVE_PAGE, jsonObject);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    /**
     * date: 7/9/17
     * author: chenli
     * description: 接收子线合约列表弹出框以及持仓页传过来的合约代码，以便更新盘口信息
     */
    @Subscribe
    public void onEvent(IdEvent data) {
        mInstrumentId = data.getInstrument_id();
        //此处由上层活动页向服务器请求数据
    }

    @Override
    public void onResume() {
        super.onResume();
        registerBroaderCast();
    }

    @Override
    public void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mReceiver);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        EventBus.getDefault().unregister(this);
    }

}


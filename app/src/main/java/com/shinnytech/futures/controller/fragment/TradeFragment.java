package com.shinnytech.futures.controller.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.shinnytech.futures.R;
import com.shinnytech.futures.amplitude.api.Amplitude;
import com.shinnytech.futures.application.BaseApplication;
import com.shinnytech.futures.controller.activity.FutureInfoActivity;
import com.shinnytech.futures.controller.activity.MainActivity;
import com.shinnytech.futures.databinding.FragmentTradeBinding;
import com.shinnytech.futures.model.adapter.TradeAdapter;
import com.shinnytech.futures.model.bean.accountinfobean.AccountEntity;
import com.shinnytech.futures.model.bean.accountinfobean.OrderEntity;
import com.shinnytech.futures.model.bean.accountinfobean.PositionEntity;
import com.shinnytech.futures.model.bean.accountinfobean.TradeEntity;
import com.shinnytech.futures.model.bean.accountinfobean.UserEntity;
import com.shinnytech.futures.model.engine.DataManager;
import com.shinnytech.futures.model.listener.SimpleRecyclerViewItemClickListener;
import com.shinnytech.futures.model.listener.TradeDiffCallback;
import com.shinnytech.futures.utils.CloneUtils;
import com.shinnytech.futures.utils.DividerItemDecorationUtils;
import com.shinnytech.futures.utils.LogUtils;
import com.shinnytech.futures.utils.MathUtils;
import com.shinnytech.futures.utils.SPUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.shinnytech.futures.constants.CommonConstants.AMP_EVENT_BALANCE;
import static com.shinnytech.futures.constants.CommonConstants.AMP_EVENT_BROKER_ID;
import static com.shinnytech.futures.constants.CommonConstants.AMP_EVENT_CURRENT_PAGE;
import static com.shinnytech.futures.constants.CommonConstants.AMP_EVENT_IS_POSITIVE;
import static com.shinnytech.futures.constants.CommonConstants.AMP_EVENT_ORDER_COUNT;
import static com.shinnytech.futures.constants.CommonConstants.AMP_EVENT_PAGE_ID;
import static com.shinnytech.futures.constants.CommonConstants.AMP_EVENT_PAGE_ID_VALUE_ACCOUNT;
import static com.shinnytech.futures.constants.CommonConstants.AMP_EVENT_PAGE_VALUE_FUTURE_INFO;
import static com.shinnytech.futures.constants.CommonConstants.AMP_EVENT_PAGE_VALUE_MAIN;
import static com.shinnytech.futures.constants.CommonConstants.AMP_EVENT_PAGE_VISIBLE_TIME;
import static com.shinnytech.futures.constants.CommonConstants.AMP_EVENT_POSITION_COUNT;
import static com.shinnytech.futures.constants.CommonConstants.AMP_EVENT_SUB_PAGE_ID;
import static com.shinnytech.futures.constants.CommonConstants.AMP_EVENT_SUB_PAGE_ID_VALUE_TRADE;
import static com.shinnytech.futures.constants.CommonConstants.AMP_EVENT_TARGET_PAGE;
import static com.shinnytech.futures.constants.CommonConstants.AMP_LEAVE_PAGE;
import static com.shinnytech.futures.constants.CommonConstants.AMP_SHOW_PAGE;
import static com.shinnytech.futures.constants.CommonConstants.AMP_SWITCH_PAGE;
import static com.shinnytech.futures.constants.CommonConstants.CONFIG_BROKER;
import static com.shinnytech.futures.constants.CommonConstants.INS_BETWEEN_ACTIVITY;
import static com.shinnytech.futures.constants.CommonConstants.MAIN_ACTIVITY_TO_FUTURE_INFO_ACTIVITY;
import static com.shinnytech.futures.constants.CommonConstants.STATUS_ALIVE;
import static com.shinnytech.futures.constants.CommonConstants.TD_MESSAGE;
import static com.shinnytech.futures.service.WebSocketService.TD_BROADCAST_ACTION;

public class TradeFragment extends LazyLoadFragment {

    private DataManager sDataManager = DataManager.getInstance();
    private BroadcastReceiver mReceiverAccount;
    private FragmentTradeBinding mBinding;
    private TradeAdapter mAdapter;
    private boolean mIsUpdate = true;
    private List<TradeEntity> mOldData = new ArrayList<>();
    private List<TradeEntity> mNewData = new ArrayList<>();
    private long mShowTime;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void show() {
        try {
            refreshAccount();
            showEvent();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void showEvent() {
        try {
            LogUtils.e("showTrade", true);
            mShowTime = System.currentTimeMillis();
            String broker_id = (String) SPUtils.get(BaseApplication.getContext(), CONFIG_BROKER, "");
            JSONObject jsonObject = new JSONObject();
            jsonObject.put(AMP_EVENT_PAGE_ID, AMP_EVENT_PAGE_ID_VALUE_ACCOUNT);
            jsonObject.put(AMP_EVENT_SUB_PAGE_ID, AMP_EVENT_SUB_PAGE_ID_VALUE_TRADE);
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
            LogUtils.e("leaveTrade", true);
            long pageVisibleTime = System.currentTimeMillis() - mShowTime;
            String broker_id = (String) SPUtils.get(BaseApplication.getContext(), CONFIG_BROKER, "");
            JSONObject jsonObject = new JSONObject();
            jsonObject.put(AMP_EVENT_PAGE_ID, AMP_EVENT_PAGE_ID_VALUE_ACCOUNT);
            jsonObject.put(AMP_EVENT_SUB_PAGE_ID, AMP_EVENT_SUB_PAGE_ID_VALUE_TRADE);
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
                }
            }
            Amplitude.getInstance().logEvent(AMP_LEAVE_PAGE, jsonObject);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_trade, container, false);
        initData();
        initEvent();
        return mBinding.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();
        registerBroaderCast();
    }

    @Override
    public void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mReceiverAccount);
    }

    private void registerBroaderCast() {
        mReceiverAccount = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String mDataString = intent.getStringExtra("msg");
                switch (mDataString) {
                    case TD_MESSAGE:
                        refreshAccount();
                        break;
                    default:
                        break;
                }
            }
        };

        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mReceiverAccount, new IntentFilter(TD_BROADCAST_ACTION));
    }

    private void refreshAccount() {
        if (!mIsUpdate) return;
        try {
            UserEntity userEntity = sDataManager.getTradeBean().getUsers().get(sDataManager.LOGIN_USER_ID);
            if (userEntity == null) return;
            mNewData.clear();
            for (TradeEntity tradeEntity :
                    userEntity.getTrades().values()) {
                TradeEntity t = CloneUtils.clone(tradeEntity);
                mNewData.add(t);
            }
            Collections.sort(mNewData);
            DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new TradeDiffCallback(mOldData, mNewData), false);
            mAdapter.setData(mNewData);
            diffResult.dispatchUpdatesTo(mAdapter);
            mOldData.clear();
            mOldData.addAll(mNewData);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initData() {
        mBinding.rv.setLayoutManager(new LinearLayoutManager(getActivity()));
        mBinding.rv.addItemDecoration(
                new DividerItemDecorationUtils(getActivity(), DividerItemDecorationUtils.VERTICAL_LIST));
        mBinding.rv.setItemAnimator(new DefaultItemAnimator());
        mAdapter = new TradeAdapter(getActivity(), mOldData);
        mBinding.rv.setAdapter(mAdapter);
    }

    private void initEvent() {
        mBinding.rv.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                switch (newState) {
                    case RecyclerView.SCROLL_STATE_IDLE:
                        mIsUpdate = true;
                        break;
                    case RecyclerView.SCROLL_STATE_DRAGGING:
                        mIsUpdate = false;
                        break;
                    case RecyclerView.SCROLL_STATE_SETTLING:
                        mIsUpdate = false;
                        break;
                }
            }
        });

        mBinding.rv.addOnItemTouchListener(new SimpleRecyclerViewItemClickListener(mBinding.rv, new SimpleRecyclerViewItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                if (position >= 0 && position < mAdapter.getItemCount()) {
                    TradeEntity tradeEntity = mAdapter.getData().get(position);
                    if (tradeEntity == null) return;
                    if (getActivity() instanceof MainActivity) {
                        ((MainActivity) getActivity()).getmMainActivityPresenter()
                                .setPreSubscribedQuotes(sDataManager.getRtnData().getIns_list());
                    }
                    sDataManager.IS_SHOW_VP_CONTENT = true;
                    Intent intent = new Intent(getActivity(), FutureInfoActivity.class);
                    intent.putExtra(INS_BETWEEN_ACTIVITY, tradeEntity.getExchange_id() + "." + tradeEntity.getInstrument_id());
                    getActivity().startActivityForResult(intent, MAIN_ACTIVITY_TO_FUTURE_INFO_ACTIVITY);
                    JSONObject jsonObject = new JSONObject();
                    try {
                        jsonObject.put(AMP_EVENT_CURRENT_PAGE, AMP_EVENT_PAGE_VALUE_MAIN);
                        jsonObject.put(AMP_EVENT_TARGET_PAGE, AMP_EVENT_PAGE_VALUE_FUTURE_INFO);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    Amplitude.getInstance().logEvent(AMP_SWITCH_PAGE, jsonObject);
                }
            }

            @Override
            public void onItemLongClick(View view, int position) {

            }
        }));
    }
}

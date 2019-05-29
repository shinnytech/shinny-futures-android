package com.shinnytech.futures.controller.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.shinnytech.futures.R;
import com.shinnytech.futures.application.BaseApplication;
import com.shinnytech.futures.controller.activity.FutureInfoActivity;
import com.shinnytech.futures.controller.activity.MainActivity;
import com.shinnytech.futures.databinding.FragmentPositionBinding;
import com.shinnytech.futures.model.adapter.PositionAdapter;
import com.shinnytech.futures.model.amplitude.api.Amplitude;
import com.shinnytech.futures.model.bean.accountinfobean.AccountEntity;
import com.shinnytech.futures.model.bean.accountinfobean.OrderEntity;
import com.shinnytech.futures.model.bean.accountinfobean.PositionEntity;
import com.shinnytech.futures.model.bean.accountinfobean.UserEntity;
import com.shinnytech.futures.model.bean.eventbusbean.IdEvent;
import com.shinnytech.futures.model.engine.DataManager;
import com.shinnytech.futures.model.engine.LatestFileManager;
import com.shinnytech.futures.model.listener.PositionDiffCallback;
import com.shinnytech.futures.model.listener.SimpleRecyclerViewItemClickListener;
import com.shinnytech.futures.utils.CloneUtils;
import com.shinnytech.futures.utils.DividerItemDecorationUtils;
import com.shinnytech.futures.utils.LogUtils;
import com.shinnytech.futures.utils.MathUtils;
import com.shinnytech.futures.utils.SPUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.shinnytech.futures.constants.CommonConstants.AMP_EVENT_BALANCE;
import static com.shinnytech.futures.constants.CommonConstants.AMP_EVENT_BROKER_ID;
import static com.shinnytech.futures.constants.CommonConstants.AMP_EVENT_CURRENT_PAGE;
import static com.shinnytech.futures.constants.CommonConstants.AMP_EVENT_IS_INS_IN_OPTIONAL;
import static com.shinnytech.futures.constants.CommonConstants.AMP_EVENT_IS_INS_IN_POSITION;
import static com.shinnytech.futures.constants.CommonConstants.AMP_EVENT_IS_POSITIVE;
import static com.shinnytech.futures.constants.CommonConstants.AMP_EVENT_ORDER_COUNT;
import static com.shinnytech.futures.constants.CommonConstants.AMP_EVENT_PAGE_ID;
import static com.shinnytech.futures.constants.CommonConstants.AMP_EVENT_PAGE_ID_VALUE_ACCOUNT;
import static com.shinnytech.futures.constants.CommonConstants.AMP_EVENT_PAGE_ID_VALUE_FUTURE_INFO;
import static com.shinnytech.futures.constants.CommonConstants.AMP_EVENT_PAGE_VALUE_FUTURE_INFO;
import static com.shinnytech.futures.constants.CommonConstants.AMP_EVENT_PAGE_VALUE_MAIN;
import static com.shinnytech.futures.constants.CommonConstants.AMP_EVENT_PAGE_VISIBLE_TIME;
import static com.shinnytech.futures.constants.CommonConstants.AMP_EVENT_POSITION_COUNT;
import static com.shinnytech.futures.constants.CommonConstants.AMP_EVENT_SUB_PAGE_ID;
import static com.shinnytech.futures.constants.CommonConstants.AMP_EVENT_SUB_PAGE_ID_VALUE_POSITION;
import static com.shinnytech.futures.constants.CommonConstants.AMP_EVENT_TARGET_PAGE;
import static com.shinnytech.futures.constants.CommonConstants.AMP_LEAVE_PAGE;
import static com.shinnytech.futures.constants.CommonConstants.AMP_SHOW_PAGE;
import static com.shinnytech.futures.constants.CommonConstants.AMP_SWITCH_PAGE;
import static com.shinnytech.futures.constants.CommonConstants.CONFIG_BROKER;
import static com.shinnytech.futures.constants.CommonConstants.INS_BETWEEN_ACTIVITY;
import static com.shinnytech.futures.constants.CommonConstants.MAIN_ACTIVITY_TO_FUTURE_INFO_ACTIVITY;
import static com.shinnytech.futures.constants.CommonConstants.STATUS_ALIVE;
import static com.shinnytech.futures.constants.CommonConstants.TD_MESSAGE;
import static com.shinnytech.futures.model.service.WebSocketService.TD_BROADCAST_ACTION;

/**
 * date: 5/10/17
 * author: chenli
 * description: 持仓页
 * version:
 * state: done
 */
public class PositionFragment extends LazyLoadFragment {
    protected BroadcastReceiver mReceiver;
    protected DataManager sDataManager = DataManager.getInstance();
    private PositionAdapter mAdapter;
    private List<PositionEntity> mOldData = new ArrayList<>();
    private List<PositionEntity> mNewData = new ArrayList<>();
    private FragmentPositionBinding mBinding;
    private boolean mIsUpdate;
    private long mShowTime;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_position, container, false);
        initData();
        initEvent();
        return mBinding.getRoot();
    }

    protected void initData() {
        mIsUpdate = true;
        mBinding.rv.setLayoutManager(
                new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
        mBinding.rv.addItemDecoration(
                new DividerItemDecorationUtils(getActivity(), DividerItemDecorationUtils.VERTICAL_LIST));
        mAdapter = new PositionAdapter(getActivity(), mOldData);
        mBinding.rv.setAdapter(mAdapter);
        if (getActivity() instanceof FutureInfoActivity) {
            mAdapter.setHighlightIns(((FutureInfoActivity) getActivity()).getInstrument_id());
            //注册EventBus
            EventBus.getDefault().register(this);
        }
    }

    protected void initEvent() {
        //recyclerView点击事件监听器，点击改变合约代码，并跳转到交易页
        mBinding.rv.addOnItemTouchListener(new SimpleRecyclerViewItemClickListener(mBinding.rv,
                new SimpleRecyclerViewItemClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        PositionEntity positionEntity = mAdapter.getData().get(position);
                        if (positionEntity == null) return;
                        sDataManager.POSITION_DIRECTION = ((TextView) view.findViewById(R.id.position_direction))
                                .getText().toString();
                        String instrument_id = positionEntity.getExchange_id() + "." + positionEntity.getInstrument_id();
                        //添加判断，防止自选合约列表为空时产生无效的点击事件
                        if (instrument_id != null) {
                            if (getActivity() instanceof MainActivity) {
                                ((MainActivity) getActivity()).getmMainActivityPresenter()
                                        .setPreSubscribedQuotes(sDataManager.getRtnData().getIns_list());
                                sDataManager.IS_SHOW_VP_CONTENT = true;
                                Intent intentPos = new Intent(getActivity(), FutureInfoActivity.class);
                                intentPos.putExtra(INS_BETWEEN_ACTIVITY, instrument_id);
                                getActivity().startActivityForResult(intentPos, MAIN_ACTIVITY_TO_FUTURE_INFO_ACTIVITY);
                                JSONObject jsonObject = new JSONObject();
                                try {
                                    jsonObject.put(AMP_EVENT_CURRENT_PAGE, AMP_EVENT_PAGE_VALUE_MAIN);
                                    jsonObject.put(AMP_EVENT_TARGET_PAGE, AMP_EVENT_PAGE_VALUE_FUTURE_INFO);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                Amplitude.getInstance().logEvent(AMP_SWITCH_PAGE, jsonObject);
                            } else {
                                mAdapter.updateHighlightIns(instrument_id);
                                IdEvent idEvent = new IdEvent();
                                idEvent.setInstrument_id(instrument_id);
                                EventBus.getDefault().post(idEvent);
                                ((FutureInfoActivity) getActivity()).getViewPager().setCurrentItem(4, false);

                            }
                        }
                    }

                    @Override
                    public void onItemLongClick(View view, int position) {
                    }
                }));


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

        mBinding.seeMd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getActivity() instanceof MainActivity) {
                    MainActivity mainActivity = ((MainActivity) getActivity());
                    mainActivity.getmBinding().bottomNavigation.setSelectedItemId(R.id.market);
                    QuotePagerFragment quotePagerFragment = (QuotePagerFragment) mainActivity.getmMainActivityPresenter().getmViewPagerFragmentAdapter().getItem(0);
                    quotePagerFragment.setCurrentItem(0);
                }
            }
        });
    }

    protected void refreshPosition() {
        try {
            if (!mIsUpdate) return;
            UserEntity userEntity = sDataManager.getTradeBean().getUsers().get(sDataManager.USER_ID);
            if (userEntity == null) return;
            mNewData.clear();

            for (PositionEntity positionEntity :
                    userEntity.getPositions().values()) {

                int volume_long = Integer.parseInt(positionEntity.getVolume_long());
                int volume_short = Integer.parseInt(positionEntity.getVolume_short());
                if (volume_long != 0 && volume_short != 0) {
                    PositionEntity positionEntityLong = positionEntity.cloneLong();
                    PositionEntity positionEntityShort = positionEntity.cloneShort();
                    mNewData.add(positionEntityLong);
                    mNewData.add(positionEntityShort);
                } else if (!(volume_long == 0 && volume_short == 0)) {
                    mNewData.add(CloneUtils.clone(positionEntity));
                }
            }

            if (getActivity() instanceof MainActivity && mNewData.isEmpty())
                mBinding.seeMd.setVisibility(View.VISIBLE);
            else mBinding.seeMd.setVisibility(View.GONE);

            Collections.sort(mNewData);
            DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new PositionDiffCallback(mOldData, mNewData), false);
            mAdapter.setData(mNewData);
            diffResult.dispatchUpdatesTo(mAdapter);
            mOldData.clear();
            mOldData.addAll(mNewData);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    protected void registerBroaderCast() {
        mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String mDataString = intent.getStringExtra("msg");
                switch (mDataString) {
                    case TD_MESSAGE:
                        refreshPosition();
                        break;
                    default:
                        break;
                }
            }
        };
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mReceiver, new IntentFilter(TD_BROADCAST_ACTION));
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
        if (getActivity() instanceof FutureInfoActivity) EventBus.getDefault().unregister(this);
    }

    @Override
    public void show() {
        refreshPosition();
        mBinding.rv.scrollToPosition(0);
        showEvent();
    }

    /**
     * date: 2019/5/25
     * author: chenli
     * description: 显示时上报
     */
    public void showEvent() {
        try {
            LogUtils.e("PositionShow", true);
            mShowTime = System.currentTimeMillis();
            String broker_id = (String) SPUtils.get(BaseApplication.getContext(), CONFIG_BROKER, "");
            JSONObject jsonObject = new JSONObject();
            if (getActivity() instanceof MainActivity) {
                jsonObject.put(AMP_EVENT_PAGE_ID, AMP_EVENT_PAGE_ID_VALUE_ACCOUNT);
            } else {
                String ins = ((FutureInfoActivity) getActivity()).getInstrument_id();
                boolean isInsInOptional = LatestFileManager.getOptionalInsList().keySet().contains(ins);
                jsonObject.put(AMP_EVENT_IS_INS_IN_OPTIONAL, isInsInOptional);
                jsonObject.put(AMP_EVENT_PAGE_ID, AMP_EVENT_PAGE_ID_VALUE_FUTURE_INFO);
            }
            jsonObject.put(AMP_EVENT_SUB_PAGE_ID, AMP_EVENT_SUB_PAGE_ID_VALUE_POSITION);
            jsonObject.put(AMP_EVENT_BROKER_ID, broker_id);
            jsonObject.put(AMP_EVENT_IS_POSITIVE, DataManager.getInstance().IS_POSITIVE);
            UserEntity userEntity = DataManager.getInstance().getTradeBean().getUsers().get(DataManager.getInstance().USER_ID);
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

                    if (getActivity() instanceof FutureInfoActivity) {
                        String ins = ((FutureInfoActivity) getActivity()).getInstrument_id();
                        boolean isInsInPosition = userEntity.getPositions().keySet().contains(ins);
                        jsonObject.put(AMP_EVENT_IS_INS_IN_POSITION, isInsInPosition);
                    }
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

    /**
     * date: 2019/5/25
     * author: chenli
     * description: 离开时上报
     */
    public void leaveEvent() {
        try {
            LogUtils.e("PositionLeave", true);
            long pageVisibleTime = System.currentTimeMillis() - mShowTime;
            String broker_id = (String) SPUtils.get(BaseApplication.getContext(), CONFIG_BROKER, "");
            JSONObject jsonObject = new JSONObject();
            if (getActivity() instanceof MainActivity) {
                jsonObject.put(AMP_EVENT_PAGE_ID, AMP_EVENT_PAGE_ID_VALUE_ACCOUNT);
            } else {
                String ins = ((FutureInfoActivity) getActivity()).getInstrument_id();
                boolean isInsInOptional = LatestFileManager.getOptionalInsList().keySet().contains(ins);
                jsonObject.put(AMP_EVENT_IS_INS_IN_OPTIONAL, isInsInOptional);
                jsonObject.put(AMP_EVENT_PAGE_ID, AMP_EVENT_PAGE_ID_VALUE_FUTURE_INFO);
            }
            jsonObject.put(AMP_EVENT_SUB_PAGE_ID, AMP_EVENT_SUB_PAGE_ID_VALUE_POSITION);
            jsonObject.put(AMP_EVENT_BROKER_ID, broker_id);
            jsonObject.put(AMP_EVENT_IS_POSITIVE, DataManager.getInstance().IS_POSITIVE);
            jsonObject.put(AMP_EVENT_PAGE_VISIBLE_TIME, pageVisibleTime);
            UserEntity userEntity = DataManager.getInstance().getTradeBean().getUsers().get(DataManager.getInstance().USER_ID);
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

                    if (getActivity() instanceof FutureInfoActivity) {
                        String ins = ((FutureInfoActivity) getActivity()).getInstrument_id();
                        boolean isInsInPosition = userEntity.getPositions().keySet().contains(ins);
                        jsonObject.put(AMP_EVENT_IS_INS_IN_POSITION, isInsInPosition);
                    }

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
     * description: 接收持仓点击、自选点击、搜索页点击发来的合约，用于更新高亮合约
     */
    @Subscribe
    public void onEvent(IdEvent data) {
        mAdapter.updateHighlightIns(data.getInstrument_id());
    }

}

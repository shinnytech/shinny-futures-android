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
import com.shinnytech.futures.controller.activity.FutureInfoActivity;
import com.shinnytech.futures.controller.activity.MainActivity;
import com.shinnytech.futures.databinding.FragmentPositionBinding;
import com.shinnytech.futures.model.adapter.PositionAdapter;
import com.shinnytech.futures.model.bean.accountinfobean.PositionEntity;
import com.shinnytech.futures.model.bean.accountinfobean.UserEntity;
import com.shinnytech.futures.model.bean.eventbusbean.IdEvent;
import com.shinnytech.futures.model.engine.DataManager;
import com.shinnytech.futures.model.listener.PositionDiffCallback;
import com.shinnytech.futures.model.listener.SimpleRecyclerViewItemClickListener;
import com.shinnytech.futures.utils.CloneUtils;
import com.shinnytech.futures.utils.DividerItemDecorationUtils;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.shinnytech.futures.constants.CommonConstants.JUMP_TO_FUTURE_INFO_ACTIVITY;
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
    private boolean mIsPositionsAll;

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
        if (getActivity() instanceof MainActivity) mIsPositionsAll = true;
        else mIsPositionsAll = false;
        mIsUpdate = true;
        mBinding.rv.setLayoutManager(
                new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
        mBinding.rv.addItemDecoration(
                new DividerItemDecorationUtils(getActivity(), DividerItemDecorationUtils.VERTICAL_LIST));
        mAdapter = new PositionAdapter(getActivity(), mOldData);
        mBinding.rv.setAdapter(mAdapter);
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
                            if (mIsPositionsAll) {
                                try {
                                    if (getActivity() instanceof MainActivity){
                                        ((MainActivity)getActivity()).getmMainActivityPresenter()
                                                .setPreSubscribedQuotes(sDataManager.getRtnData().getIns_list());
                                    }
                                    sDataManager.IS_SHOW_VP_CONTENT = true;
                                    Intent intentPos = new Intent(getActivity(), FutureInfoActivity.class);
                                    intentPos.putExtra("instrument_id", instrument_id);
                                    startActivityForResult(intentPos, JUMP_TO_FUTURE_INFO_ACTIVITY);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }else {
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
    }

    protected void refreshPosition() {
        try {
            if (!mIsUpdate)return;
            UserEntity userEntity = sDataManager.getTradeBean().getUsers().get(sDataManager.USER_ID);
            if (userEntity == null) return;
            mNewData.clear();

            for (PositionEntity positionEntity :
                    userEntity.getPositions().values()) {
                if (!mIsPositionsAll){
                    String ins = ((FutureInfoActivity)getActivity()).getInstrument_id();
                    String ins_ = positionEntity.getExchange_id() + "." + positionEntity.getInstrument_id();
                    if (!ins_.equals(ins))continue;
                }
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
    public void update() {
        refreshPosition();
        mBinding.rv.scrollToPosition(0);
    }

}

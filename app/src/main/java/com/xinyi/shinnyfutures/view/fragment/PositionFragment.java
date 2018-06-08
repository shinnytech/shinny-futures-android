package com.xinyi.shinnyfutures.view.fragment;

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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.xinyi.shinnyfutures.R;
import com.xinyi.shinnyfutures.databinding.FragmentPositionBinding;
import com.xinyi.shinnyfutures.model.adapter.PositionAdapter;
import com.xinyi.shinnyfutures.model.bean.accountinfobean.PositionEntity;
import com.xinyi.shinnyfutures.model.bean.eventbusbean.IdEvent;
import com.xinyi.shinnyfutures.model.engine.DataManager;
import com.xinyi.shinnyfutures.model.listener.PositionDiffCallback;
import com.xinyi.shinnyfutures.model.listener.SimpleRecyclerViewItemClickListener;
import com.xinyi.shinnyfutures.utils.DividerItemDecorationUtils;
import com.xinyi.shinnyfutures.utils.MathUtils;
import com.xinyi.shinnyfutures.view.activity.FutureInfoActivity;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.xinyi.shinnyfutures.constants.CommonConstants.CLOSE;
import static com.xinyi.shinnyfutures.constants.CommonConstants.ERROR;
import static com.xinyi.shinnyfutures.constants.CommonConstants.MESSAGE_POSITION;
import static com.xinyi.shinnyfutures.constants.CommonConstants.OPEN;
import static com.xinyi.shinnyfutures.model.service.WebSocketService.BROADCAST_ACTION_TRANSACTION;

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
        mBinding.rv.setLayoutManager(
                new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
        mBinding.rv.addItemDecoration(
                new DividerItemDecorationUtils(getActivity(), DividerItemDecorationUtils.VERTICAL_LIST));
        mAdapter = new PositionAdapter(getActivity(), mOldData);
        mBinding.rv.setAdapter(mAdapter);
    }

    protected void initEvent() {
        if (mBinding.rv != null) {
            //recyclerView点击事件监听器，点击改变合约代码，并跳转到交易页
            SimpleRecyclerViewItemClickListener mTouchListener = new SimpleRecyclerViewItemClickListener(mBinding.rv, new SimpleRecyclerViewItemClickListener.OnItemClickListener() {
                @Override
                public void onItemClick(View view, int position) {
                    if (mAdapter.getData().get(position) != null) {
                        String instrument_id = mAdapter.getData().get(position).getInstrument_id();
                        String exchange_id = mAdapter.getData().get(position).getExchange_id();
                        //添加判断，防止自选合约列表为空时产生无效的点击事件
                        if (instrument_id != null) {
                            IdEvent idEvent = new IdEvent();
                            idEvent.setPosition_id(exchange_id);
                            idEvent.setInstrument_id(instrument_id);
                            EventBus.getDefault().post(idEvent);
                            ((FutureInfoActivity) getActivity()).getViewPager().setCurrentItem(3, false);
                        }
                    }
                }

                @Override
                public void onItemLongClick(View view, int position) {
                }
            });
            mBinding.rv.addOnItemTouchListener(mTouchListener);
        }
    }

    protected void refreshPosition() {
        mNewData.clear();
        for (Map.Entry<String, PositionEntity> entry :
                sDataManager.getAccountBean().getPosition().entrySet()) {
            PositionEntity positionEntity = entry.getValue();
            String available_long = MathUtils.add(positionEntity.getVolume_long_his(), positionEntity.getVolume_long_today());
            int volume_long = Integer.parseInt(MathUtils.add(available_long, positionEntity.getVolume_long_frozen()));
            String available_short = MathUtils.add(positionEntity.getVolume_short_his(), positionEntity.getVolume_short_today());
            int volume_short = Integer.parseInt(MathUtils.add(available_short, positionEntity.getVolume_short_frozen()));
            if (volume_long != 0 || volume_short != 0) {
                mNewData.add(entry.getValue());
            }
        }
        Collections.sort(mNewData);
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new PositionDiffCallback(mOldData, mNewData), false);
        mAdapter.setData(mNewData);
        diffResult.dispatchUpdatesTo(mAdapter);
        mOldData.clear();
        mOldData.addAll(mNewData);
    }

    protected void registerBroaderCast() {
        mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String mDataString = intent.getStringExtra("msg");
                switch (mDataString) {
                    case OPEN:
                        break;
                    case CLOSE:
                        break;
                    case ERROR:
                        break;
                    case MESSAGE_POSITION:
                        if ((R.id.rb_position_info == ((FutureInfoActivity) getActivity()).getTabsInfo().getCheckedRadioButtonId()))
                            refreshPosition();
                        break;
                    default:
                        break;
                }
            }
        };
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mReceiver, new IntentFilter(BROADCAST_ACTION_TRANSACTION));
    }


    @Override
    public void onResume() {
        super.onResume();
        refreshPosition();
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
    }
}

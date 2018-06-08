package com.xinyi.shinnyfutures.view.fragment;

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

import com.xinyi.shinnyfutures.R;
import com.xinyi.shinnyfutures.databinding.FragmentHandicapBinding;
import com.xinyi.shinnyfutures.model.bean.eventbusbean.IdEvent;
import com.xinyi.shinnyfutures.model.engine.DataManager;
import com.xinyi.shinnyfutures.view.activity.FutureInfoActivity;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import static com.xinyi.shinnyfutures.constants.CommonConstants.CLOSE;
import static com.xinyi.shinnyfutures.constants.CommonConstants.ERROR;
import static com.xinyi.shinnyfutures.constants.CommonConstants.MESSAGE;
import static com.xinyi.shinnyfutures.constants.CommonConstants.OPEN;
import static com.xinyi.shinnyfutures.model.service.WebSocketService.BROADCAST_ACTION;

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
    private String mInstrumenId;
    private FragmentHandicapBinding mBinding;

    /**
     * date: 7/9/17
     * author: chenli
     * description: 实时刷新盘口信息
     */
    private void refreshUI() {
        mBinding.setHandicap(sDataManager.getRtnData().getQuotes().get(mInstrumenId));
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
                    case OPEN:
                        break;
                    case CLOSE:
                        break;
                    case ERROR:
                        break;
                    case MESSAGE:
                        if (((FutureInfoActivity) getActivity()).getTabsInfo().getCheckedRadioButtonId()
                                == R.id.rb_handicap_info) refreshUI();
                        break;
                    default:
                        break;
                }
            }
        };
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mReceiver, new IntentFilter(BROADCAST_ACTION));
    }

    /**
     * date: 7/9/17
     * author: chenli
     * description: 初始化时获取合约代码
     */
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mInstrumenId = ((FutureInfoActivity) getActivity()).getInstrument_id();
    }

    @Override
    public void update() {
        refreshUI();
    }

    /**
     * date: 7/9/17
     * author: chenli
     * description: 接收子线合约列表弹出框以及持仓页传过来的合约代码，以便更新盘口信息
     */
    @Subscribe
    public void onEvent(IdEvent data) {
        mInstrumenId = data.getInstrument_id();
        //此处由上层活动页向服务器请求数据
    }

    @Override
    public void onResume() {
        super.onResume();
        registerBroaderCast();
        //防止重复发相同的合约代码,服务器什么都不回
        refreshUI();
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


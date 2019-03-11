package com.shinnytech.futures.controller.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.shinnytech.futures.R;
import com.shinnytech.futures.application.BaseApplication;
import com.shinnytech.futures.databinding.FragmentHandicapBinding;
import com.shinnytech.futures.model.bean.eventbusbean.IdEvent;
import com.shinnytech.futures.model.bean.futureinfobean.QuoteEntity;
import com.shinnytech.futures.model.engine.DataManager;
import com.shinnytech.futures.controller.activity.FutureInfoActivity;
import com.shinnytech.futures.model.engine.LatestFileManager;
import com.shinnytech.futures.utils.CloneUtils;
import com.shinnytech.futures.utils.LogUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import static com.shinnytech.futures.constants.CommonConstants.MD_MESSAGE;
import static com.shinnytech.futures.model.service.WebSocketService.MD_BROADCAST_ACTION;

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
    private Context sContext = BaseApplication.getContext();

    /**
     * date: 7/9/17
     * author: chenli
     * description: 实时刷新盘口信息
     */
    private void refreshUI() {
        QuoteEntity quoteEntity = sDataManager.getRtnData().getQuotes().get(mInstrumentId);
        if (quoteEntity == null)return;
        if (mInstrumentId.contains("&") && mInstrumentId.contains(" ")){
            quoteEntity = CloneUtils.clone(quoteEntity);
            quoteEntity = LatestFileManager.calculateCombineQuoteFull(quoteEntity);
        }
        mBinding.setHandicap(quoteEntity);
        setPriceColor(quoteEntity);
    }

    /**
     * date: 2019/1/11
     * author: chenli
     * description: 设置价格颜色
     */
    private void setPriceColor(QuoteEntity quoteEntity){
        String pre_settlement = LatestFileManager.saveScaleByPtick(quoteEntity.getPre_settlement(),
                quoteEntity.getInstrument_id());
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
        mInstrumentId = data.getInstrument_id();
        //此处由上层活动页向服务器请求数据
    }

    @Override
    public void onResume() {
        super.onResume();
        registerBroaderCast();
        //防止重复发相同的合约代码,服务器什么都不回
        update();
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


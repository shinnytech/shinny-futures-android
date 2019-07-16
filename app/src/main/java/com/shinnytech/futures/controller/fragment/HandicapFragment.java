package com.shinnytech.futures.controller.fragment;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.shinnytech.futures.R;
import com.shinnytech.futures.databinding.FragmentHandicapBinding;
import com.shinnytech.futures.model.bean.eventbusbean.IdEvent;
import com.shinnytech.futures.model.bean.futureinfobean.QuoteEntity;
import com.shinnytech.futures.model.engine.DataManager;
import com.shinnytech.futures.model.engine.LatestFileManager;
import com.shinnytech.futures.utils.CloneUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

/**
 * date: 7/9/17
 * author: chenli
 * description: 盘口信息页
 * version:
 * state: done
 */
public class HandicapFragment extends LazyLoadFragment {
    private DataManager sDataManager = DataManager.getInstance();
    private String mInstrumentId;
    private FragmentHandicapBinding mBinding;

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

    @Override
    public void show() {
        refreshMD();
    }

    @Override
    public void leave() {
    }

    @Override
    public void refreshMD() {
        QuoteEntity quoteEntity = sDataManager.getRtnData().getQuotes().get(mInstrumentId);
        if (quoteEntity == null) return;
        if (mInstrumentId.contains("&") && mInstrumentId.contains(" ")) {
            quoteEntity = CloneUtils.clone(quoteEntity);
            quoteEntity = LatestFileManager.calculateCombineQuoteFull(quoteEntity);
        }
        mBinding.setHandicap(quoteEntity);
    }

    @Override
    public void refreshTD() {

    }

    /**
     * date: 2019/7/3
     * author: chenli
     * description: 设置合约id
     */
    public void setInstrument_id(String ins) {
        mInstrumentId = ins;
    }

    /**
     * date: 7/9/17
     * author: chenli
     * description: 接收子线合约列表弹出框以及持仓页传过来的合约代码，以便更新盘口信息
     */
    @Subscribe
    public void onEvent(IdEvent data) {
        setInstrument_id(data.getInstrument_id());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        EventBus.getDefault().unregister(this);
    }

}


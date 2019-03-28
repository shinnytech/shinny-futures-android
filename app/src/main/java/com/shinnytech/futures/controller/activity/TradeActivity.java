package com.shinnytech.futures.controller.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.shinnytech.futures.R;
import com.shinnytech.futures.application.BaseApplication;
import com.shinnytech.futures.databinding.ActivityTradeBinding;
import com.shinnytech.futures.model.adapter.TradeAdapter;
import com.shinnytech.futures.model.bean.accountinfobean.TradeEntity;
import com.shinnytech.futures.model.bean.accountinfobean.UserEntity;
import com.shinnytech.futures.model.listener.TradeDiffCallback;
import com.shinnytech.futures.utils.CloneUtils;
import com.shinnytech.futures.utils.DividerItemDecorationUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.shinnytech.futures.constants.CommonConstants.ACTIVITY_TYPE;
import static com.shinnytech.futures.constants.CommonConstants.TRADE;

/**
 * date: 7/7/17
 * author: chenli
 * description: 成交记录页，用于显示用户一天的成交记录
 * version:
 * state: done
 */
public class TradeActivity extends BaseActivity {
    private TradeAdapter mAdapter;
    private boolean mIsUpdate = true;
    private List<TradeEntity> mOldData = new ArrayList<>();
    private List<TradeEntity> mNewData = new ArrayList<>();
    private ActivityTradeBinding mBinding;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mLayoutID = R.layout.activity_trade;
        mTitle = TRADE;
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void initData() {
        mBinding = (ActivityTradeBinding) mViewDataBinding;
        sContext = BaseApplication.getContext();
        mBinding.rv.setLayoutManager(new LinearLayoutManager(this));
        mBinding.rv.addItemDecoration(
                new DividerItemDecorationUtils(this, DividerItemDecorationUtils.VERTICAL_LIST));
        mBinding.rv.setItemAnimator(new DefaultItemAnimator());
        mAdapter = new TradeAdapter(this, mOldData);
        mBinding.rv.setAdapter(mAdapter);
    }

    @Override
    protected void initEvent() {
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

    @Override
    protected void refreshUI() {
        if (!mIsUpdate) return;
        try {
            UserEntity userEntity = sDataManager.getTradeBean().getUsers().get(sDataManager.USER_ID);
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


    @Override
    public void onResume() {
        super.onResume();
    }

    /**
     * date: 2019/3/15
     * author: chenli
     * description: 进入登录页如果不登陆返回，则退出本页
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        finish();
        super.onActivityResult(requestCode, resultCode, data);
    }

}

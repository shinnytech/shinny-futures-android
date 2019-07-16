package com.shinnytech.futures.controller.fragment;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.shinnytech.futures.R;
import com.shinnytech.futures.controller.activity.MainActivity;
import com.shinnytech.futures.controller.activity.MainActivityPresenter;
import com.shinnytech.futures.databinding.FragmentTradeBinding;
import com.shinnytech.futures.model.adapter.TradeAdapter;
import com.shinnytech.futures.model.bean.accountinfobean.TradeEntity;
import com.shinnytech.futures.model.bean.accountinfobean.UserEntity;
import com.shinnytech.futures.model.engine.DataManager;
import com.shinnytech.futures.model.listener.SimpleRecyclerViewItemClickListener;
import com.shinnytech.futures.model.listener.TradeDiffCallback;
import com.shinnytech.futures.utils.CloneUtils;
import com.shinnytech.futures.utils.DividerItemDecorationUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TradeFragment extends LazyLoadFragment {

    private DataManager sDataManager = DataManager.getInstance();
    private FragmentTradeBinding mBinding;
    private TradeAdapter mAdapter;
    private boolean mIsUpdate = true;
    private List<TradeEntity> mOldData = new ArrayList<>();
    private List<TradeEntity> mNewData = new ArrayList<>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void show() {
        refreshTD();
    }

    @Override
    public void leave() {
    }

    @Override
    public void refreshMD() {

    }

    @Override
    public void refreshTD() {
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_trade, container, false);
        initData();
        initEvent();
        return mBinding.getRoot();
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
                    String instrument_id = tradeEntity.getExchange_id() + "." + tradeEntity.getInstrument_id();
                    sDataManager.IS_SHOW_VP_CONTENT = true;
                    MainActivity mainActivity = (MainActivity) getActivity();
                    MainActivityPresenter mainActivityPresenter = mainActivity.getmMainActivityPresenter();
                    mainActivityPresenter.switchToFutureInfo(instrument_id);
                }
            }

            @Override
            public void onItemLongClick(View view, int position) {

            }
        }));
    }
}

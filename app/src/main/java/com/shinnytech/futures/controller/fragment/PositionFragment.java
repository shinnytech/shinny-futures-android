package com.shinnytech.futures.controller.fragment;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.shinnytech.futures.R;
import com.shinnytech.futures.controller.activity.MainActivity;
import com.shinnytech.futures.controller.activity.MainActivityPresenter;
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
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * date: 5/10/17
 * author: chenli
 * description: 持仓页
 * version:
 * state: done
 */
public class PositionFragment extends LazyLoadFragment {
    private static final String KEY_FRAGMENT_TYPE = "isInAccountFragment";
    protected DataManager sDataManager = DataManager.getInstance();
    private PositionAdapter mAdapter;
    private List<PositionEntity> mOldData = new ArrayList<>();
    private List<PositionEntity> mNewData = new ArrayList<>();
    private FragmentPositionBinding mBinding;
    private boolean mIsUpdate;
    private String mInstrumentId;
    private boolean mIsInAccountFragment;

    public static PositionFragment newInstance(boolean isInAccountFragment) {
        PositionFragment fragment = new PositionFragment();
        Bundle bundle = new Bundle();
        bundle.putBoolean(KEY_FRAGMENT_TYPE, isInAccountFragment);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mIsInAccountFragment = getArguments().getBoolean(KEY_FRAGMENT_TYPE);
    }


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
        mAdapter.setHighlightIns(mInstrumentId);
        mBinding.rv.setAdapter(mAdapter);
        if (!mIsInAccountFragment) EventBus.getDefault().register(this);
    }

    protected void initEvent() {
        //recyclerView点击事件监听器，点击改变合约代码，并跳转到交易页
        mBinding.rv.addOnItemTouchListener(new SimpleRecyclerViewItemClickListener(mBinding.rv,
                new SimpleRecyclerViewItemClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        if (position >= 0 && position < mAdapter.getItemCount()) {
                            PositionEntity positionEntity = mAdapter.getData().get(position);
                            if (positionEntity == null) return;
                            sDataManager.POSITION_DIRECTION = ((TextView) view.findViewById(R.id.position_direction))
                                    .getText().toString();
                            String instrument_id = positionEntity.getExchange_id() + "." + positionEntity.getInstrument_id();
                            MainActivity mainActivity = (MainActivity) getActivity();
                            MainActivityPresenter mainActivityPresenter = mainActivity.getmMainActivityPresenter();
                            if (mIsInAccountFragment) {
                                sDataManager.IS_SHOW_VP_CONTENT = true;
                                mainActivityPresenter.switchToFutureInfo(instrument_id);
                            } else {
                                IdEvent idEvent = new IdEvent();
                                idEvent.setInstrument_id(instrument_id);
                                EventBus.getDefault().post(idEvent);
                                mAdapter.updateHighlightIns(instrument_id);
                                FutureInfoFragment futureInfoFragment = (FutureInfoFragment) mainActivityPresenter.getmViewPagerFragmentAdapter().getItem(2);
                                futureInfoFragment.getmBinding().vpInfoContent.setCurrentItem(3, false);
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
                if (mIsInAccountFragment) {
                    MainActivity mainActivity = ((MainActivity) getActivity());
                    mainActivity.getmMainActivityPresenter().getmBinding().bottomNavigation.setSelectedItemId(R.id.market);
                    QuotePagerFragment quotePagerFragment = (QuotePagerFragment) mainActivity.getmMainActivityPresenter().getmViewPagerFragmentAdapter().getItem(0);
                    quotePagerFragment.setCurrentItem(0);
                }
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (!mIsInAccountFragment) EventBus.getDefault().unregister(this);
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

            if (mIsInAccountFragment && mNewData.isEmpty())
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
     * description: 接收持仓点击、自选点击、搜索页点击发来的合约，用于更新高亮合约
     */
    @Subscribe
    public void onEvent(IdEvent data) {
        mInstrumentId = data.getInstrument_id();
        mAdapter.updateHighlightIns(mInstrumentId);
    }

}

package com.shinnytech.futures.controller.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;

import com.shinnytech.futures.R;
import com.shinnytech.futures.application.BaseApplicationLike;
import com.shinnytech.futures.databinding.ActivityTradeBinding;
import com.shinnytech.futures.model.bean.accountinfobean.TradeEntity;
import com.shinnytech.futures.model.bean.accountinfobean.UserEntity;
import com.shinnytech.futures.model.engine.DataManager;
import com.shinnytech.futures.utils.CloneUtils;
import com.shinnytech.futures.utils.DividerItemDecorationUtils;
import com.shinnytech.futures.model.adapter.TradeAdapter;
import com.shinnytech.futures.model.listener.TradeDiffCallback;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.shinnytech.futures.constants.CommonConstants.ACTIVITY_TYPE;
import static com.shinnytech.futures.constants.CommonConstants.CLOSE;
import static com.shinnytech.futures.constants.CommonConstants.DEAL;
import static com.shinnytech.futures.constants.CommonConstants.ERROR;
import static com.shinnytech.futures.constants.CommonConstants.MESSAGE_TRADE;
import static com.shinnytech.futures.constants.CommonConstants.OPEN;
import static com.shinnytech.futures.model.receiver.NetworkReceiver.NETWORK_STATE;
import static com.shinnytech.futures.model.service.WebSocketService.BROADCAST_ACTION_TRANSACTION;

/**
 * date: 7/7/17
 * author: chenli
 * description: 成交记录页，用于显示用户一天的成交记录
 * version:
 * state: done
 */
public class TradeActivity extends BaseActivity {
    private TradeAdapter mAdapter;
    private BroadcastReceiver mReceiver;
    private BroadcastReceiver mReceiver1;
    private DataManager sDataManager = DataManager.getInstance();
    private Context sContext;
    private boolean mIsUpdate = true;
    private List<TradeEntity> mOldData = new ArrayList<>();
    private List<TradeEntity> mNewData = new ArrayList<>();
    private ActivityTradeBinding mBinding;

    private void registerBroaderCast() {
        mReceiver1 = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                int networkStatus = intent.getIntExtra("networkStatus", 0);
                switch (networkStatus) {
                    case 0:
                        mToolbar.setBackgroundColor(ContextCompat.getColor(context, R.color.off_line));
                        mToolbarTitle.setTextColor(Color.BLACK);
                        mToolbarTitle.setText("交易、行情网络未连接！");
                        mToolbarTitle.setTextSize(20);
                        break;
                    case 1:
                        mToolbar.setBackgroundColor(ContextCompat.getColor(context, R.color.black_dark));
                        mToolbarTitle.setTextColor(Color.WHITE);
                        mToolbarTitle.setText("成交记录");
                        mToolbarTitle.setTextSize(25);
                        break;
                    default:
                        break;
                }
            }
        };
        registerReceiver(mReceiver1, new IntentFilter(NETWORK_STATE));

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
                    case MESSAGE_TRADE:
                        if (mIsUpdate) refreshUI();
                        break;
                    default:
                        break;
                }
            }
        };
        LocalBroadcastManager.getInstance(this).registerReceiver(mReceiver, new IntentFilter(BROADCAST_ACTION_TRANSACTION));
    }

    public void refreshUI() {
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
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mLayoutID = R.layout.activity_trade;
        mTitle = DEAL;
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void initData() {
        mBinding = (ActivityTradeBinding) mViewDataBinding;
        sContext = BaseApplicationLike.getContext();
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
    public void onResume() {
        super.onResume();
        updateToolbarFromNetwork(sContext, "成交记录");
        refreshUI();
        if (!sDataManager.IS_LOGIN) {
            Intent intent = new Intent(this, LoginActivity.class);
            //判断从哪个页面跳到登录页，登录页的销毁方式不一样
            intent.putExtra(ACTIVITY_TYPE, "MainActivity");
            startActivity(intent);
        }
        registerBroaderCast();
    }

    @Override
    public void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mReceiver);
        unregisterReceiver(mReceiver1);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case android.R.id.home:
                finish();
        }
        return super.onOptionsItemSelected(item);
    }
}

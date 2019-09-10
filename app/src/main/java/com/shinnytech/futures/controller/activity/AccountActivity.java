package com.shinnytech.futures.controller.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.shinnytech.futures.R;
import com.shinnytech.futures.databinding.ActivityAccountBinding;
import com.shinnytech.futures.model.bean.accountinfobean.AccountEntity;
import com.shinnytech.futures.model.bean.accountinfobean.UserEntity;

import static com.shinnytech.futures.application.BaseApplication.TD_BROADCAST_ACTION;
import static com.shinnytech.futures.constants.CommonConstants.ACCOUNT;
import static com.shinnytech.futures.constants.BroadcastConstants.TD_MESSAGE;

/**
 * date: 7/7/17
 * author: chenli
 * description: 账户信息页，实时刷新用户的账户信息，用户不登录的情况下看不到本页
 * version:
 * state: done
 */
public class AccountActivity extends BaseActivity {

    private ActivityAccountBinding mBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mLayoutID = R.layout.activity_account;
        mTitle = ACCOUNT;
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void initData() {
        mBinding = (ActivityAccountBinding) mViewDataBinding;
    }

    @Override
    protected void initEvent() {
    }

    private void refreshUI() {
        UserEntity userEntity = sDataManager.getTradeBean().getUsers().get(sDataManager.USER_ID);
        if (userEntity == null) return;
        AccountEntity accountEntity = userEntity.getAccounts().get("CNY");
        ((ActivityAccountBinding) mViewDataBinding).setAccount(accountEntity);
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshUI();
        registerBroaderCast();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mReceiverLocal != null)
            LocalBroadcastManager.getInstance(this).unregisterReceiver(mReceiverLocal);
    }

    /**
     * date: 7/7/17
     * author: chenli
     * description: 注册账户广播，监听账户实时信息
     */
    protected void registerBroaderCast() {
        mReceiverLocal = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String msg = intent.getStringExtra("msg");
                if (TD_MESSAGE.equals(msg)) refreshUI();
            }
        };
        LocalBroadcastManager.getInstance(this).registerReceiver(mReceiverLocal, new IntentFilter(TD_BROADCAST_ACTION));
    }
}

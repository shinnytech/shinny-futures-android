package com.shinnytech.futures.controller.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.view.MenuItem;
import android.view.View;

import com.shinnytech.futures.R;
import com.shinnytech.futures.application.BaseApplication;
import com.shinnytech.futures.databinding.ActivityAccountBinding;
import com.shinnytech.futures.model.bean.accountinfobean.AccountEntity;
import com.shinnytech.futures.model.bean.accountinfobean.UserEntity;
import com.shinnytech.futures.model.engine.DataManager;

import static com.shinnytech.futures.constants.CommonConstants.ACCOUNT;
import static com.shinnytech.futures.constants.CommonConstants.ACTIVITY_TYPE;
import static com.shinnytech.futures.constants.CommonConstants.OFFLINE;
import static com.shinnytech.futures.constants.CommonConstants.TD_MESSAGE;
import static com.shinnytech.futures.model.receiver.NetworkReceiver.NETWORK_STATE;
import static com.shinnytech.futures.model.service.WebSocketService.TD_BROADCAST_ACTION;

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

    protected void refreshUI() {
        UserEntity userEntity = sDataManager.getTradeBean().getUsers().get(sDataManager.USER_ID);
        if (userEntity == null) return;
        AccountEntity accountEntity = userEntity.getAccounts().get("CNY");
        ((ActivityAccountBinding) mViewDataBinding).setAccount(accountEntity);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!sDataManager.IS_LOGIN) {
            Intent intent = new Intent(this, LoginActivity.class);
            //判断从哪个页面跳到登录页，登录页的销毁方式不一样
            intent.putExtra(ACTIVITY_TYPE, "MainActivity");
            startActivity(intent);
        }
    }
}

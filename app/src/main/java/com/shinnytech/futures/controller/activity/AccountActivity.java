package com.shinnytech.futures.controller.activity;

import android.content.Intent;
import android.os.Bundle;

import com.shinnytech.futures.R;
import com.shinnytech.futures.databinding.ActivityAccountBinding;
import com.shinnytech.futures.model.bean.accountinfobean.AccountEntity;
import com.shinnytech.futures.model.bean.accountinfobean.UserEntity;

import static com.shinnytech.futures.constants.CommonConstants.ACCOUNT;

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

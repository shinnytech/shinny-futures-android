package com.shinnytech.futures.controller.activity;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.shinnytech.futures.R;
import com.shinnytech.futures.application.BaseApplication;
import com.shinnytech.futures.databinding.ActivityConfirmBinding;
import com.shinnytech.futures.model.engine.DataManager;

/**
 * date: 6/1/18
 * author: chenli
 * description: 结算单处理界面
 * version:
 * state:
 */
public class ConfirmActivity extends AppCompatActivity implements View.OnClickListener {

    private DataManager mDataManager = DataManager.getInstance();
    private ActivityConfirmBinding mBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_confirm);
        initData();
        initEvent();
    }


    private void initData() {
        mBinding.settlementInfo.setText(mDataManager.getBroker().getSettlement());
    }

    private void initEvent() {
        mBinding.notNow.setOnClickListener(this);
        mBinding.confirm.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (BaseApplication.getWebSocketService() != null)
            switch (v.getId()) {
                case R.id.not_now:
                    finish();
                    break;
                case R.id.confirm:
                    if (BaseApplication.getWebSocketService() != null)
                        BaseApplication.getWebSocketService().sendReqConfirmSettlement();
                    finish();
                    break;
                default:
                    break;
            }
    }
}

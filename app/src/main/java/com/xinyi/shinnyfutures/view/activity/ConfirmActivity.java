package com.xinyi.shinnyfutures.view.activity;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.xinyi.shinnyfutures.R;
import com.xinyi.shinnyfutures.application.BaseApplicationLike;
import com.xinyi.shinnyfutures.databinding.ActivityConfirmBinding;
import com.xinyi.shinnyfutures.model.engine.DataManager;

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
        mBinding.settlementInfo.setText(mDataManager.getLogin().getMsg_settlement());
    }

    private void initEvent() {
        mBinding.notNow.setOnClickListener(this);
        mBinding.confirm.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (BaseApplicationLike.getWebSocketService() != null)
            switch (v.getId()) {
                case R.id.not_now:
                    BaseApplicationLike.getWebSocketService().sendReqConfirmSettlement("6", "no");
                    finish();
                    break;
                case R.id.confirm:
                    BaseApplicationLike.getWebSocketService().sendReqConfirmSettlement("5", "yes");
                    finish();
                    break;
                default:
                    break;
            }
    }
}

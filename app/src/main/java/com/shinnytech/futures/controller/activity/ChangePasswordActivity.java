package com.shinnytech.futures.controller.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.LocalBroadcastManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;

import com.shinnytech.futures.R;
import com.shinnytech.futures.application.BaseApplication;
import com.shinnytech.futures.databinding.ActivityChangePasswordBinding;
import com.shinnytech.futures.utils.ToastNotificationUtils;

import java.lang.ref.WeakReference;

import static com.shinnytech.futures.constants.CommonConstants.PASSWORD;
import static com.shinnytech.futures.constants.CommonConstants.TD_MESSAGE_CHANGE_SUCCESS;
import static com.shinnytech.futures.model.service.WebSocketService.TD_BROADCAST_ACTION;

public class ChangePasswordActivity extends BaseActivity {

    private ActivityChangePasswordBinding mBinding;
    private BroadcastReceiver mReceiverChange;
    private Handler mHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mLayoutID = R.layout.activity_change_password;
        mTitle = PASSWORD;
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mReceiverChange != null)
            LocalBroadcastManager.getInstance(this).unregisterReceiver(mReceiverChange);
    }

    @Override
    protected void initData() {
        mBinding = (ActivityChangePasswordBinding) mViewDataBinding;
        mHandler = new MyHandler(this);
    }

    @Override
    protected void initEvent() {

        mBinding.deleteOldPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mBinding.etOldPassword.getEditableText().clear();
            }
        });

        mBinding.deleteNewPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mBinding.etNewPassword.getEditableText().clear();
            }
        });

        mBinding.deleteConfirmPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mBinding.etConfirmNewPassword.getEditableText().clear();
            }
        });

        mBinding.etOldPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() == 0) {
                    mBinding.deleteOldPassword.setVisibility(View.INVISIBLE);
                } else {
                    mBinding.deleteOldPassword.setVisibility(View.VISIBLE);
                }

            }
        });

        mBinding.etNewPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() == 0) {
                    mBinding.deleteNewPassword.setVisibility(View.INVISIBLE);
                } else {
                    mBinding.deleteNewPassword.setVisibility(View.VISIBLE);
                }

            }
        });

        mBinding.etConfirmNewPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() == 0) {
                    mBinding.deleteConfirmPassword.setVisibility(View.INVISIBLE);
                } else {
                    mBinding.deleteConfirmPassword.setVisibility(View.VISIBLE);
                }

            }
        });

        mBinding.etOldPassword.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    mBinding.llOldPassword.setBackgroundResource(R.drawable.login_rectangle_border_focused);
                } else {
                    mBinding.llOldPassword.setBackgroundResource(R.drawable.rectangle_border);
                }
            }
        });

        mBinding.etNewPassword.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    mBinding.llNewPassword.setBackgroundResource(R.drawable.login_rectangle_border_focused);
                } else {
                    mBinding.llNewPassword.setBackgroundResource(R.drawable.rectangle_border);
                }
            }
        });

        mBinding.etConfirmNewPassword.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    mBinding.llConfirmNewPassword.setBackgroundResource(R.drawable.login_rectangle_border_focused);
                } else {
                    mBinding.llConfirmNewPassword.setBackgroundResource(R.drawable.rectangle_border);
                }
            }
        });

        mBinding.change.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (BaseApplication.getWebSocketService() == null) {
                    ToastNotificationUtils.showToast(sContext, "连接断开，请重启");
                    return;
                }

                String old_password = mBinding.etOldPassword.getText().toString();
                if (old_password == null || old_password.isEmpty()) {
                    ToastNotificationUtils.showToast(sContext, "旧密码不能为空");
                    return;
                }

                String new_password = mBinding.etNewPassword.getText().toString();
                if (new_password == null || new_password.isEmpty()) {
                    ToastNotificationUtils.showToast(sContext, "新密码不能为空");
                    return;
                }
                String confirm_new_password = mBinding.etConfirmNewPassword.getText().toString();
                if (confirm_new_password == null || confirm_new_password.isEmpty()) {
                    ToastNotificationUtils.showToast(sContext, "请确认新密码");
                    return;
                }

                if (!confirm_new_password.equals(new_password)) {
                    ToastNotificationUtils.showToast(sContext, "新密码确认不一致");
                    return;
                }

                BaseApplication.getWebSocketService().sendReqPassword(new_password, old_password);

            }
        });
    }

    @Override
    protected void refreshUI() {

    }

    /**
     * date: 2019/3/21
     * author: chenli
     * description: 修改密码成功检测
     */
    protected void registerBroaderCast() {

        super.registerBroaderCast();

        mReceiverChange = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String msg = intent.getStringExtra("msg");
                switch (msg) {
                    case TD_MESSAGE_CHANGE_SUCCESS:
                        mHandler.sendEmptyMessageDelayed(0, 2000);
                        break;
                    default:
                        break;
                }
            }
        };
        LocalBroadcastManager.getInstance(this).registerReceiver(mReceiverChange, new IntentFilter(TD_BROADCAST_ACTION));
    }


    /**
     * date: 6/1/18
     * author: chenli
     * description: 点击登录后服务器返回处理
     * version:
     * state:
     */
    static class MyHandler extends Handler {
        WeakReference<ChangePasswordActivity> mActivityReference;

        MyHandler(ChangePasswordActivity activity) {
            mActivityReference = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            final ChangePasswordActivity activity = mActivityReference.get();
            if (activity != null) {
                switch (msg.what) {
                    case 0:
                        Intent intent = new Intent();
                        activity.setResult(RESULT_OK, intent);
                        activity.finish();
                        break;
                    default:
                        break;
                }
            }
        }
    }


}

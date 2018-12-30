package com.shinnytech.futures.controller.activity;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;

import com.shinnytech.futures.R;
import com.shinnytech.futures.application.BaseApplication;
import com.shinnytech.futures.databinding.ActivityChangePasswordBinding;
import com.shinnytech.futures.utils.ToastNotificationUtils;

import static com.shinnytech.futures.constants.CommonConstants.PASSWORD;

public class ChangePasswordActivity extends BaseActivity {

    private ActivityChangePasswordBinding mBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mLayoutID = R.layout.activity_change_password;
        mTitle = PASSWORD;
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void initData() {
        mBinding = (ActivityChangePasswordBinding) mViewDataBinding;
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
                if (s.length() == 0){
                    mBinding.deleteOldPassword.setVisibility(View.INVISIBLE);
                }else {
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
                if (s.length() == 0){
                    mBinding.deleteNewPassword.setVisibility(View.INVISIBLE);
                }else {
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
                if (s.length() == 0){
                    mBinding.deleteConfirmPassword.setVisibility(View.INVISIBLE);
                }else {
                    mBinding.deleteConfirmPassword.setVisibility(View.VISIBLE);
                }

            }
        });

        mBinding.etOldPassword.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus){
                    mBinding.llOldPassword.setBackgroundResource(R.drawable.rectangle_border_focused);
                }else {
                    mBinding.llOldPassword.setBackgroundResource(R.drawable.rectangle_border);
                }
            }
        });

        mBinding.etNewPassword.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus){
                    mBinding.llNewPassword.setBackgroundResource(R.drawable.rectangle_border_focused);
                }else {
                    mBinding.llNewPassword.setBackgroundResource(R.drawable.rectangle_border);
                }
            }
        });

        mBinding.etConfirmNewPassword.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus){
                    mBinding.llConfirmNewPassword.setBackgroundResource(R.drawable.rectangle_border_focused);
                }else {
                    mBinding.llConfirmNewPassword.setBackgroundResource(R.drawable.rectangle_border);
                }
            }
        });

        mBinding.change.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (BaseApplication.getWebSocketService() == null){
                    ToastNotificationUtils.showToast(sContext, "连接断开，请重启");
                    return;
                }

                String old_password = mBinding.etOldPassword.getText().toString();
                if (old_password == null || old_password.isEmpty()){
                    ToastNotificationUtils.showToast(sContext, "旧密码不能为空");
                    return;
                }

                String new_password = mBinding.etNewPassword.getText().toString();
                if (new_password == null || new_password.isEmpty()){
                    ToastNotificationUtils.showToast(sContext, "新密码不能为空");
                    return;
                }
                String confirm_new_password = mBinding.etConfirmNewPassword.getText().toString();
                if (confirm_new_password == null || confirm_new_password.isEmpty()){
                    ToastNotificationUtils.showToast(sContext, "请确认新密码");
                    return;
                }

                if (!confirm_new_password.equals(new_password)){
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

}

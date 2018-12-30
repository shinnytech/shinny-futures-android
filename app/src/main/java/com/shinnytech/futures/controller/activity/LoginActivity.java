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
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.CompoundButton;

import com.shinnytech.futures.R;
import com.shinnytech.futures.application.BaseApplication;
import com.shinnytech.futures.databinding.ActivityLoginBinding;
import com.shinnytech.futures.utils.LogUtils;
import com.shinnytech.futures.utils.SPUtils;

import java.lang.ref.WeakReference;

import static android.view.inputmethod.InputMethodManager.HIDE_NOT_ALWAYS;
import static com.shinnytech.futures.constants.CommonConstants.ACTIVITY_TYPE;
import static com.shinnytech.futures.constants.CommonConstants.CONFIG_BROKER;
import static com.shinnytech.futures.constants.CommonConstants.CONFIG_LOCK_ACCOUNT;
import static com.shinnytech.futures.constants.CommonConstants.CONFIG_LOCK_PASSWORD;
import static com.shinnytech.futures.constants.CommonConstants.CONFIG_PASSWORD;
import static com.shinnytech.futures.constants.CommonConstants.CONFIG_ACCOUNT;
import static com.shinnytech.futures.constants.CommonConstants.LOGIN;
import static com.shinnytech.futures.constants.CommonConstants.LOGIN_BROKER_JUMP_TO_BROKER_LIST_ACTIVITY;
import static com.shinnytech.futures.constants.CommonConstants.TD_MESSAGE_BROKER_INFO;
import static com.shinnytech.futures.constants.CommonConstants.TD_MESSAGE_LOGIN;
import static com.shinnytech.futures.model.service.WebSocketService.TD_BROADCAST_ACTION;

/**
 * date: 6/1/17
 * author: chenli
 * description: 待优化：在用户名框和密码框两边加上图片,还可以添加一键删除功能
 * version:
 * state: basically done
 */

public class LoginActivity extends BaseActivity {
    /**
     * date: 7/7/17
     * description: 用户登录监听广播
     */
    private BroadcastReceiver mReceiverLogin;
    private String mActivityType;
    private String mBrokerName;
    private String mPhoneNumber;
    private Handler mHandler;
    private ActivityLoginBinding mBinding;
    private String mPassword;
    private boolean mRememberPassword;
    private boolean mRememberAccount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mLayoutID = R.layout.activity_login;
        mTitle = LOGIN;
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void initData() {
        mHandler = new MyHandler(this);
        mBinding = (ActivityLoginBinding) mViewDataBinding;
        mActivityType = getIntent().getStringExtra(ACTIVITY_TYPE);
        String[] brokerList = sDataManager.getBroker().getBrokers();

        //获取用户登录成功后保存在sharedPreference里的期货公司
        if (SPUtils.contains(sContext, CONFIG_BROKER)) {
            String brokerName = (String) SPUtils.get(sContext, CONFIG_BROKER, "");
            mBinding.broker.setText(brokerName);
        }else if (brokerList.length != 0){
            mBinding.broker.setText(brokerList[0]);
        }

        //获取用户登录成功后保存在sharedPreference里的用户名
        if (SPUtils.contains(sContext, CONFIG_ACCOUNT)) {
            String account = (String) SPUtils.get(sContext, CONFIG_ACCOUNT, "");
            mBinding.account.setText(account);
            mBinding.account.setSelection(account.length());
            if (!account.isEmpty()) mBinding.deleteAccount.setVisibility(View.VISIBLE);
        }

        if (SPUtils.contains(sContext, CONFIG_PASSWORD)) {
            String password = (String) SPUtils.get(sContext, CONFIG_PASSWORD, "");
            mBinding.password.setText(password);
            mBinding.password.setSelection(password.length());
            if (!password.isEmpty())mBinding.deletePassword.setVisibility(View.VISIBLE);
        }

        if (SPUtils.contains(sContext, CONFIG_LOCK_ACCOUNT)){
            mRememberAccount = (boolean) SPUtils.get(sContext, CONFIG_LOCK_ACCOUNT, false);
            if (mRememberAccount){
                mBinding.cbRememberAccount.setChecked(true);
            }
        }

        if (SPUtils.contains(sContext, CONFIG_LOCK_PASSWORD)){
            mRememberPassword = (boolean) SPUtils.get(sContext, CONFIG_LOCK_PASSWORD, false);
            if (mRememberPassword){
                mBinding.cbRememberPassword.setChecked(true);
            }
        }
    }

    @Override
    protected void initEvent() {

        mBinding.broker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intentBroker = new Intent(LoginActivity.this, BrokerListActivity.class);
                startActivityForResult(intentBroker, LOGIN_BROKER_JUMP_TO_BROKER_LIST_ACTIVITY);
            }
        });

        mBinding.deleteAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mBinding.account.getEditableText().clear();
            }
        });

        mBinding.deletePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mBinding.password.getEditableText().clear();
            }
        });

        mBinding.account.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() == 0){
                    mBinding.deleteAccount.setVisibility(View.INVISIBLE);
                }else {
                    mBinding.deleteAccount.setVisibility(View.VISIBLE);
                }

            }
        });

        mBinding.password.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() == 0){
                    mBinding.deletePassword.setVisibility(View.INVISIBLE);
                }else {
                    mBinding.deletePassword.setVisibility(View.VISIBLE);
                }

            }
        });

        mBinding.account.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus){
                    mBinding.llAccount.setBackgroundResource(R.drawable.rectangle_border_focused);
                }else {
                    mBinding.llAccount.setBackgroundResource(R.drawable.rectangle_border);
                }
            }
        });

        mBinding.password.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus){
                    mBinding.llPassword.setBackgroundResource(R.drawable.rectangle_border_focused);
                }else {
                    mBinding.llPassword.setBackgroundResource(R.drawable.rectangle_border);
                }
            }
        });

        mBinding.cbRememberAccount.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked){
                    mRememberAccount = true;
                    SPUtils.putAndApply(sContext, CONFIG_LOCK_ACCOUNT, true);
                }else {
                    mRememberAccount = false;
                    SPUtils.putAndApply(sContext, CONFIG_LOCK_ACCOUNT, false);
                }
            }
        });

        mBinding.cbRememberPassword.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked){
                    mRememberPassword = true;
                    SPUtils.putAndApply(sContext, CONFIG_LOCK_PASSWORD, true);
                }else {
                    mRememberPassword = false;
                    SPUtils.putAndApply(sContext, CONFIG_LOCK_PASSWORD, false);
                }
            }
        });

        //点击登录
        mBinding.buttonIdLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

    }

    @Override
    protected void refreshUI() {

    }

    /**
     * date: 6/1/18
     * author: chenli
     * description: 页面返回回调处理
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case android.R.id.home:
                //关闭键盘后销毁
                View view = getCurrentFocus();
                if (view != null) {
                    InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    if (inputMethodManager != null)
                        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), HIDE_NOT_ALWAYS);
                }
                switch (mActivityType) {
                    case "MainActivity":
                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        startActivity(intent);
                        break;
                    case "FutureInfoActivity":
                        //返回未登录信息给合约详情页，使盘口页显示，涉及登录页的fragment隐藏
                        Intent intent1 = new Intent();
                        setResult(RESULT_CANCELED, intent1);
                        finish();
                        break;
                    default:
                        break;
                }
                break;
            default:
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * date: 7/7/17
     * author: chenli
     * description: 不登录退出时，由于MainActivity的launchMode是SingleInstance，所以调到主页面时，登录页会被弹出，也就finish掉了
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
            switch (mActivityType) {
                case "MainActivity":
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    startActivity(intent);
                    break;
                case "FutureInfoActivity":
                    //返回未登录信息给合约详情页，使盘口页显示，涉及登录页的fragment隐藏
                    Intent intent1 = new Intent();
                    setResult(RESULT_CANCELED, intent1);
                    finish();
                    break;
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mReceiverLogin != null)LocalBroadcastManager.getInstance(this).unregisterReceiver(mReceiverLogin);
    }

    /**
     * Attempts to sign in or register the activity_account specified by the fragment_home form.
     * If there are form errors (invalid phone, missing fields, etc.), the
     * errors are presented and no actual fragment_home attempt is made.
     */
    private void attemptLogin() {
        // Reset errors.
        mBinding.password.setError(null);
        mBinding.account.setError(null);

        // Store values at the time of the fragment_home attempt.
        mBrokerName = mBinding.broker.getText().toString();
        mPhoneNumber = mBinding.account.getText().toString();
        mPassword = mBinding.password.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (TextUtils.isEmpty(mPassword)) {
            mBinding.password.setError(getString(R.string.login_activity_error_invalid_password));
            focusView = mBinding.password;
            cancel = true;
        }

        // Check for a valid phone number.
        if (TextUtils.isEmpty(mPhoneNumber)) {
            mBinding.account.setError(getString(R.string.login_activity_error_field_required));
            focusView = mBinding.account;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt fragment_home and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user fragment_home attempt.
            if (BaseApplication.getWebSocketService() != null)
                BaseApplication.getWebSocketService().sendReqLogin(mBrokerName, mPhoneNumber, mPassword);

            //关闭键盘
            View view = getWindow().getCurrentFocus();
            if (view != null) {
                InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                if (inputMethodManager != null)
                    inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), HIDE_NOT_ALWAYS);
            }
        }

    }

    /**
     * date: 7/7/17
     * author: chenli
     * description: 监控网络状态与登录状态
     */
    protected void registerBroaderCast() {

        super.registerBroaderCast();

        mReceiverLogin = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String msg = intent.getStringExtra("msg");
                switch (msg) {
                    case TD_MESSAGE_LOGIN:
                        mHandler.sendEmptyMessageDelayed(0, 2000);
                        break;
                    case TD_MESSAGE_BROKER_INFO:
                        //如果客户端打开后期货公司列表信息还没有解析完毕，服务器发送brokerId后更新期货公司列表
                        mHandler.sendEmptyMessage(1);
                        break;
                    default:
                        break;
                }
            }
        };
        LocalBroadcastManager.getInstance(this).registerReceiver(mReceiverLogin, new IntentFilter(TD_BROADCAST_ACTION));
    }

    /**
     * date: 6/1/18
     * author: chenli
     * description: 根据不同软件版本获取期货公司列表
     */
//    private List<String> getBrokerIdFromBuildConfig(String[] brokerIdOrigin) {
//        List<String> brokerList = new ArrayList<>();
//        if (brokerIdOrigin != null) {
//            if (KUAI_QI_XIAO_Q.equals(BuildConfig.BROKER_ID)) {
//                brokerList.addAll(Arrays.asList(brokerIdOrigin));
//            } else {
//                for (int i = 0; i < brokerIdOrigin.length; i++) {
//                    if (brokerIdOrigin[i] != null && brokerIdOrigin[i].contains(BuildConfig.BROKER_ID)) {
//                        brokerList.add(brokerIdOrigin[i]);
//                    }
//                }
//            }
//        } else if (BaseApplication.getWebSocketService() != null)
//            BaseApplication.getWebSocketService().reConnectTD();
//
//        return brokerList;
//    }

    /**
     * date: 6/1/18
     * author: chenli
     * description: 点击登录后服务器返回处理
     * version:
     * state:
     */
    static class MyHandler extends Handler {
        WeakReference<LoginActivity> mActivityReference;

        MyHandler(LoginActivity activity) {
            mActivityReference = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            final LoginActivity activity = mActivityReference.get();
            if (activity != null) {
                switch (msg.what) {
                    case 0:
                        activity.sDataManager.IS_LOGIN = true;
                        if (activity.mRememberAccount) SPUtils.putAndApply(activity.sContext, CONFIG_ACCOUNT, activity.mPhoneNumber);
                        else SPUtils.putAndApply(activity.sContext, CONFIG_ACCOUNT, "");
                        if (activity.mRememberPassword)SPUtils.putAndApply(activity.sContext, CONFIG_PASSWORD, activity.mPassword);
                        else SPUtils.putAndApply(activity.sContext, CONFIG_PASSWORD, "");
                        SPUtils.putAndApply(activity.sContext, CONFIG_BROKER, activity.mBinding.broker.getText().toString());
                        //关闭键盘
                        View view = activity.getWindow().getCurrentFocus();
                        if (view != null) {
                            InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
                            if (inputMethodManager != null)
                                inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), HIDE_NOT_ALWAYS);
                        }

                        //返回登录信息给合约详情页，使相应页面显示
                        if (activity.mActivityType.equals("FutureInfoActivity")) {
                            Intent intent = new Intent();
                            activity.setResult(RESULT_OK, intent);
                        }
                        activity.finish();
                        break;
                    case 1:
                        String[] brokerList = activity.sDataManager.getBroker().getBrokers();
                        if (activity.mBinding.broker.getText().toString().isEmpty() && brokerList.length != 0){
                            activity.mBinding.broker.setText(brokerList[0]);
                        }
                        break;
                    default:
                        break;
                }
            }
        }
    }

    /**
     * date: 6/21/17
     * author: chenli
     * description: 合约详情页返回,发送原来订阅合约
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK){
            switch (requestCode){
                case LOGIN_BROKER_JUMP_TO_BROKER_LIST_ACTIVITY:
                    String broker = data.getStringExtra("broker");
                    LogUtils.e("broker"+broker, true);
                    mBinding.broker.setText(broker);
                    break;
                default:
                    break;
            }
        }
    }
}


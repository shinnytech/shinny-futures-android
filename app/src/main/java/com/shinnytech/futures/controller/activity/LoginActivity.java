package com.shinnytech.futures.controller.activity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.CompoundButton;

import com.shinnytech.futures.BuildConfig;
import com.shinnytech.futures.R;
import com.shinnytech.futures.application.BaseApplication;
import com.shinnytech.futures.databinding.ActivityLoginBinding;
import com.shinnytech.futures.model.amplitude.api.Amplitude;
import com.shinnytech.futures.model.amplitude.api.Identify;
import com.shinnytech.futures.model.engine.DataManager;
import com.shinnytech.futures.model.engine.LatestFileManager;
import com.shinnytech.futures.utils.NetworkUtils;
import com.shinnytech.futures.utils.SPUtils;
import com.shinnytech.futures.utils.TimeUtils;
import com.shinnytech.futures.utils.ToastNotificationUtils;

import java.lang.ref.WeakReference;
import java.util.List;

import static android.view.inputmethod.InputMethodManager.HIDE_NOT_ALWAYS;
import static com.shinnytech.futures.constants.CommonConstants.AMP_LOGGED;
import static com.shinnytech.futures.constants.CommonConstants.AMP_USER_BROKER_ID;
import static com.shinnytech.futures.constants.CommonConstants.AMP_USER_PACKAGE_ID;
import static com.shinnytech.futures.constants.CommonConstants.AMP_USER_SCREEN_SIZE;
import static com.shinnytech.futures.constants.CommonConstants.CONFIG_ACCOUNT;
import static com.shinnytech.futures.constants.CommonConstants.CONFIG_BROKER;
import static com.shinnytech.futures.constants.CommonConstants.CONFIG_LOCK_ACCOUNT;
import static com.shinnytech.futures.constants.CommonConstants.CONFIG_LOCK_PASSWORD;
import static com.shinnytech.futures.constants.CommonConstants.CONFIG_LOGIN_DATE;
import static com.shinnytech.futures.constants.CommonConstants.CONFIG_PASSWORD;
import static com.shinnytech.futures.constants.CommonConstants.LOGIN_BROKER_JUMP_TO_BROKER_LIST_ACTIVITY;
import static com.shinnytech.futures.constants.CommonConstants.LOGIN_JUMP_TO_CHANGE_PASSWORD_ACTIVITY;
import static com.shinnytech.futures.constants.CommonConstants.LOGIN_JUMP_TO_MAIN_ACTIVITY;
import static com.shinnytech.futures.constants.CommonConstants.TD_MESSAGE_BROKER_INFO;
import static com.shinnytech.futures.constants.CommonConstants.TD_MESSAGE_LOGIN;
import static com.shinnytech.futures.constants.CommonConstants.TD_MESSAGE_WEAK_PASSWORD;
import static com.shinnytech.futures.model.service.WebSocketService.TD_BROADCAST_ACTION;

/**
 * date: 6/1/17
 * author: chenli
 * description: 待优化：在用户名框和密码框两边加上图片,还可以添加一键删除功能
 * version:
 * state: basically done
 */

public class LoginActivity extends AppCompatActivity {
    protected Context sContext;
    protected DataManager sDataManager;
    /**
     * date: 7/7/17
     * description: 用户登录监听广播
     */
    private BroadcastReceiver mReceiverLogin;
    private String mBrokerName;
    private String mPhoneNumber;
    private Handler mHandler;
    private ActivityLoginBinding mBinding;
    private String mPassword;
    private long mExitTime = 0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_login);
        initData();
        initEvent();
        checkResponsibility();
        checkNetwork();
        initAmpUserProperties();
    }

    private void initData() {

        sContext = BaseApplication.getContext();
        sDataManager = DataManager.getInstance();
        mHandler = new MyHandler(this);

        //获取用户登录成功后保存在sharedPreference里的期货公司
        if (SPUtils.contains(sContext, CONFIG_BROKER)) {
            String brokerName = (String) SPUtils.get(sContext, CONFIG_BROKER, "");
            mBinding.broker.setText(brokerName);
        }

        boolean mRememberPassword = false;
        boolean mRememberAccount = false;

        if (SPUtils.contains(sContext, CONFIG_LOCK_ACCOUNT)) {
            mRememberAccount = (boolean) SPUtils.get(sContext, CONFIG_LOCK_ACCOUNT, false);
            if (mRememberAccount) {
                mBinding.cbRememberAccount.setChecked(true);
            }
        }

        if (SPUtils.contains(sContext, CONFIG_LOCK_PASSWORD)) {
            mRememberPassword = (boolean) SPUtils.get(sContext, CONFIG_LOCK_PASSWORD, false);
            if (mRememberPassword) {
                mBinding.cbRememberPassword.setChecked(true);
            }
        }

        //获取用户登录成功后保存在sharedPreference里的用户名
        if (mRememberAccount) {
            if (SPUtils.contains(sContext, CONFIG_ACCOUNT)) {
                String account = (String) SPUtils.get(sContext, CONFIG_ACCOUNT, "");
                mBinding.account.setText(account);
                mBinding.account.setSelection(account.length());
                if (!account.isEmpty()) mBinding.deleteAccount.setVisibility(View.VISIBLE);
            }
        } else {
            mBinding.account.setText("");
        }


        if (mRememberPassword) {
            if (SPUtils.contains(sContext, CONFIG_PASSWORD)) {
                String password = (String) SPUtils.get(sContext, CONFIG_PASSWORD, "");
                mBinding.password.setText(password);
                mBinding.password.setSelection(password.length());
                if (!password.isEmpty()) mBinding.deletePassword.setVisibility(View.VISIBLE);
            }
        } else {
            mBinding.password.setText("");
        }


    }

    private void initEvent() {

        mBinding.broker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intentBroker = new Intent(LoginActivity.this, BrokerListActivity.class);
                startActivityForResult(intentBroker, LOGIN_BROKER_JUMP_TO_BROKER_LIST_ACTIVITY);
            }
        });

        mBinding.selectBroker.setOnClickListener(new View.OnClickListener() {
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
                if (s.length() == 0) {
                    mBinding.deleteAccount.setVisibility(View.INVISIBLE);
                } else {
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
                if (s.length() == 0) {
                    mBinding.deletePassword.setVisibility(View.INVISIBLE);
                } else {
                    mBinding.deletePassword.setVisibility(View.VISIBLE);
                }

            }
        });

        mBinding.account.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    mBinding.llAccount.setBackgroundResource(R.drawable.rectangle_border_focused);
                } else {
                    mBinding.llAccount.setBackgroundResource(R.drawable.rectangle_border);
                }
            }
        });

        mBinding.password.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    mBinding.llPassword.setBackgroundResource(R.drawable.rectangle_border_focused);
                } else {
                    mBinding.llPassword.setBackgroundResource(R.drawable.rectangle_border);
                }
            }
        });

        mBinding.cbRememberAccount.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    SPUtils.putAndApply(sContext, CONFIG_LOCK_ACCOUNT, true);
                } else {
                    SPUtils.putAndApply(sContext, CONFIG_LOCK_ACCOUNT, false);
                }
            }
        });

        mBinding.cbRememberPassword.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    SPUtils.putAndApply(sContext, CONFIG_LOCK_PASSWORD, true);
                } else {
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

    /**
     * date: 7/7/17
     * author: chenli
     * description: 不登录退出时，由于MainActivity的launchMode是SingleInstance，所以调到主页面时，登录页会被弹出，也就finish掉了
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
            if ((System.currentTimeMillis() - mExitTime) > 2000) {
                ToastNotificationUtils.showToast(BaseApplication.getContext(), getString(R.string.main_activity_exit));
                mExitTime = System.currentTimeMillis();
            } else {
                LoginActivity.this.finish();
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerBroaderCast();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mReceiverLogin != null)
            LocalBroadcastManager.getInstance(this).unregisterReceiver(mReceiverLogin);
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
    private void registerBroaderCast() {

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
                    case TD_MESSAGE_WEAK_PASSWORD:
                        //弱密码
                        mHandler.sendEmptyMessageDelayed(2, 2000);
                        break;
                    default:
                        break;
                }
            }
        };
        LocalBroadcastManager.getInstance(this).registerReceiver(mReceiverLogin, new IntentFilter(TD_BROADCAST_ACTION));

    }

    /**
     * date: 6/21/17
     * author: chenli
     * description: 合约详情页返回,发送原来订阅合约
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case LOGIN_BROKER_JUMP_TO_BROKER_LIST_ACTIVITY:
                    String broker = data.getStringExtra("broker");
                    mBinding.broker.setText(broker);
                    break;
                case LOGIN_JUMP_TO_CHANGE_PASSWORD_ACTIVITY:
                    mBinding.password.setText("");
                    break;
                case LOGIN_JUMP_TO_MAIN_ACTIVITY:
                    finish();
                    break;
                default:
                    break;
            }
        }
    }

    /**
     * date: 1/16/18
     * author: chenli
     * description: 检查是否第一次启动APP,弹出免责条款框
     */
    public void checkResponsibility() {
        try {
            final float nowVersionCode = DataManager.getInstance().APP_CODE;
            float versionCode = (float) SPUtils.get(sContext, "versionCode", 0.0f);
            if (nowVersionCode > versionCode) {
                final Dialog dialog = new Dialog(this, R.style.responsibilityDialog);
                View view = View.inflate(this, R.layout.view_dialog_responsibility, null);
                dialog.setContentView(view);
                dialog.setCanceledOnTouchOutside(false);
                dialog.setCancelable(false);
                dialog.show();
                view.findViewById(R.id.agree).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        SPUtils.putAndApply(LoginActivity.this, "versionCode", nowVersionCode);
                        dialog.dismiss();
                    }
                });
                view.findViewById(R.id.disagree).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        LoginActivity.this.finish();
                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * date: 7/7/17
     * author: chenli
     * description: 检查网络的状态
     */
    public void checkNetwork() {
        if (!NetworkUtils.isNetworkConnected(sContext)) {
            AlertDialog.Builder dialog = new AlertDialog.Builder(this);
            dialog.setTitle("登录结果");
            dialog.setMessage("网络故障，无法连接到服务器");
            dialog.setCancelable(false);
            dialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    LoginActivity.this.finish();
                }
            });
            dialog.show();
        }
    }

    /**
     * date: 2019/3/22
     * author: chenli
     * description: 初始化amp用户属性
     */
    private void initAmpUserProperties() {
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        int height = metrics.heightPixels;
        int width = metrics.widthPixels;
        Identify identify = new Identify().set(AMP_USER_BROKER_ID, "unknown")
                .set(AMP_USER_PACKAGE_ID, BuildConfig.FLAVOR)
                .set(AMP_USER_SCREEN_SIZE, width + "*" + height);
        Amplitude.getInstance().identify(identify);
    }

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
                        String broker_id = activity.mBinding.broker.getText().toString();
                        Identify identify = new Identify().set(AMP_USER_BROKER_ID, broker_id);
                        Amplitude.getInstance().identify(identify);
                        Amplitude.getInstance().logEvent(AMP_LOGGED);
                        SPUtils.putAndApply(activity.sContext, CONFIG_LOGIN_DATE, TimeUtils.getNowTime());
                        SPUtils.putAndApply(activity.sContext, CONFIG_ACCOUNT, activity.mPhoneNumber);
                        SPUtils.putAndApply(activity.sContext, CONFIG_PASSWORD, activity.mPassword);
                        SPUtils.putAndApply(activity.sContext, CONFIG_BROKER, broker_id);
                        //关闭键盘
                        View view = activity.getWindow().getCurrentFocus();
                        if (view != null) {
                            InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
                            if (inputMethodManager != null)
                                inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), HIDE_NOT_ALWAYS);
                        }
                        Intent intent1 = new Intent(activity, MainActivity.class);
                        activity.startActivityForResult(intent1, LOGIN_JUMP_TO_MAIN_ACTIVITY);
                        break;
                    case 1:
                        List<String> brokerList = LatestFileManager
                                .getBrokerIdFromBuildConfig(activity.sDataManager.getBroker().getBrokers());
                        if (activity.mBinding.broker.getText().toString().isEmpty()
                                && brokerList != null && brokerList.size() != 0) {
                            activity.mBinding.broker.setText(brokerList.get(0));
                        }
                        break;
                    case 2:
                        Intent intent = new Intent(activity, ChangePasswordActivity.class);
                        activity.startActivityForResult(intent, LOGIN_JUMP_TO_CHANGE_PASSWORD_ACTIVITY);
                        break;
                    default:
                        break;
                }
            }
        }
    }

}


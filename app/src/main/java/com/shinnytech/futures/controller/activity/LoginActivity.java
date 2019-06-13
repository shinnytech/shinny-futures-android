package com.shinnytech.futures.controller.activity;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.databinding.DataBindingUtil;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.FrameLayout;

import com.sfit.ctp.info.DeviceInfoManager;
import com.shinnytech.futures.R;
import com.shinnytech.futures.amplitude.api.Amplitude;
import com.shinnytech.futures.amplitude.api.Identify;
import com.shinnytech.futures.application.BaseApplication;
import com.shinnytech.futures.databinding.ActivityLoginBinding;
import com.shinnytech.futures.model.engine.DataManager;
import com.shinnytech.futures.model.engine.LatestFileManager;
import com.shinnytech.futures.service.WebSocketService;
import com.shinnytech.futures.utils.Base64;
import com.shinnytech.futures.utils.NetworkUtils;
import com.shinnytech.futures.utils.SPUtils;
import com.shinnytech.futures.utils.TimeUtils;
import com.shinnytech.futures.utils.ToastUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Random;

import static android.view.inputmethod.InputMethodManager.HIDE_NOT_ALWAYS;
import static com.shinnytech.futures.constants.CommonConstants.AMP_EVENT_CURRENT_PAGE;
import static com.shinnytech.futures.constants.CommonConstants.AMP_EVENT_LOGIN_BROKER_ID;
import static com.shinnytech.futures.constants.CommonConstants.AMP_EVENT_LOGIN_TIME;
import static com.shinnytech.futures.constants.CommonConstants.AMP_EVENT_LOGIN_TYPE;
import static com.shinnytech.futures.constants.CommonConstants.AMP_EVENT_LOGIN_TYPE_VALUE_AUTO;
import static com.shinnytech.futures.constants.CommonConstants.AMP_EVENT_LOGIN_TYPE_VALUE_LOGIN;
import static com.shinnytech.futures.constants.CommonConstants.AMP_EVENT_LOGIN_TYPE_VALUE_VISIT;
import static com.shinnytech.futures.constants.CommonConstants.AMP_EVENT_LOGIN_USER_ID;
import static com.shinnytech.futures.constants.CommonConstants.AMP_EVENT_PAGE_VALUE_LOGIN;
import static com.shinnytech.futures.constants.CommonConstants.AMP_EVENT_PAGE_VALUE_MAIN;
import static com.shinnytech.futures.constants.CommonConstants.AMP_EVENT_TARGET_PAGE;
import static com.shinnytech.futures.constants.CommonConstants.AMP_LOGIN;
import static com.shinnytech.futures.constants.CommonConstants.AMP_LOGIN_TIME_OUT;
import static com.shinnytech.futures.constants.CommonConstants.AMP_SWITCH_PAGE;
import static com.shinnytech.futures.constants.CommonConstants.AMP_USER_LOGIN_TIME_FIRST;
import static com.shinnytech.futures.constants.CommonConstants.BROKER_ID_SIMNOW;
import static com.shinnytech.futures.constants.CommonConstants.BROKER_ID_SIMULATION;
import static com.shinnytech.futures.constants.CommonConstants.BROKER_ID_VISITOR;
import static com.shinnytech.futures.constants.CommonConstants.CONFIG_ACCOUNT;
import static com.shinnytech.futures.constants.CommonConstants.CONFIG_BROKER;
import static com.shinnytech.futures.constants.CommonConstants.CONFIG_INIT_TIME;
import static com.shinnytech.futures.constants.CommonConstants.CONFIG_IS_FIRM;
import static com.shinnytech.futures.constants.CommonConstants.CONFIG_LOGIN_DATE;
import static com.shinnytech.futures.constants.CommonConstants.CONFIG_PASSWORD;
import static com.shinnytech.futures.constants.CommonConstants.CONFIG_SYSTEM_INFO;
import static com.shinnytech.futures.constants.CommonConstants.CONFIG_VERSION_CODE;
import static com.shinnytech.futures.constants.CommonConstants.LOGIN_ACTIVITY_TO_BROKER_LIST_ACTIVITY;
import static com.shinnytech.futures.constants.CommonConstants.LOGIN_ACTIVITY_TO_CHANGE_PASSWORD_ACTIVITY;
import static com.shinnytech.futures.constants.CommonConstants.TD_MESSAGE_BROKER_INFO;
import static com.shinnytech.futures.constants.CommonConstants.TD_MESSAGE_LOGIN_FAIL;
import static com.shinnytech.futures.constants.CommonConstants.TD_MESSAGE_LOGIN_SUCCEED;
import static com.shinnytech.futures.constants.CommonConstants.TD_MESSAGE_WEAK_PASSWORD;
import static com.shinnytech.futures.service.WebSocketService.TD_BROADCAST_ACTION;
import static com.shinnytech.futures.utils.ScreenUtils.getStatusBarHeight;

/**
 * date: 6/1/17
 * author: chenli
 * description: 待优化：在用户名框和密码框两边加上图片,还可以添加一键删除功能
 * version:
 * state: basically done
 */

public class LoginActivity extends AppCompatActivity {
    private static final int MY_PERMISSIONS_REQUEST = 1;
    private static final int LOGIN_SUCCESS = 2;
    private static final int LOGIN_FAIL = 3;
    private static final int LOGIN_TO_CHANGE_PASSWORD = 4;
    private static final int LOGIN_TIME_OUT = 5;
    private static final int EXIT_APP = 6;
    private static final int MY_PERMISSIONS_REQUEST_DENIED = 7;
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
    private boolean mIsFirm;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_login);
        initData();
        initBrokerAccount();
        initEvent();
        checkResponsibility();
        checkPermissions();
    }

    private void initData() {
        sContext = BaseApplication.getContext();
        sDataManager = DataManager.getInstance();
        mHandler = new MyHandler(this);
        //控制是否显示登录成功弹出框
        sDataManager.IS_SHOW_LOGIN_SUCCESS = false;
        //登录入口
        sDataManager.LOGIN_TYPE = AMP_EVENT_LOGIN_TYPE_VALUE_AUTO;
    }

    /**
     * date: 2019/5/30
     * author: chenli
     * description: 初始化期货公司、账户
     */
    private void initBrokerAccount() {
        mIsFirm = (boolean) SPUtils.get(sContext, CONFIG_IS_FIRM, true);
        if (mIsFirm) switchFirm();
        else switchSimulator();
    }

    private void initEvent() {

        mBinding.llFirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switchFirm();
            }
        });

        mBinding.llSimulation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switchSimulator();
            }
        });

        mBinding.visitor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mBinding.visitor.setEnabled(false);
                //随机生成8位字符串
                String data = "";
                Random random = new Random();
                for (int i = 0; i < 8; i++) {
                    data += random.nextInt(10);
                }
                String generatedString = BROKER_ID_VISITOR + "_" + data;
                mBrokerName = BROKER_ID_SIMULATION;
                mPhoneNumber = generatedString;
                mPassword = generatedString;
                sDataManager.LOGIN_TYPE = AMP_EVENT_LOGIN_TYPE_VALUE_VISIT;
                sDataManager.LOGIN_BROKER_ID = mBrokerName;
                sDataManager.LOGIN_USER_ID = mPhoneNumber;
                changeStatusBarColor(false);
                mIsFirm = false;
                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put(AMP_EVENT_LOGIN_BROKER_ID, mBrokerName);
                    jsonObject.put(AMP_EVENT_LOGIN_USER_ID, mPhoneNumber);
                    jsonObject.put(AMP_EVENT_LOGIN_TIME, TimeUtils.getAmpTime());
                    jsonObject.put(AMP_EVENT_LOGIN_TYPE, AMP_EVENT_LOGIN_TYPE_VALUE_VISIT);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                Amplitude.getInstance().logEvent(AMP_LOGIN, jsonObject);
                WebSocketService.sendReqLogin(mBrokerName, mPhoneNumber, mPassword);
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        ToastUtils.showToast(sContext, "游客模式账户信息和持仓隔日会重置");
                    }
                });

            }
        });

        mBinding.broker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    String broker = mBinding.broker.getText().toString();
                    Intent intentBroker = new Intent(LoginActivity.this, BrokerListActivity.class);
                    intentBroker.putExtra("broker", broker);
                    startActivityForResult(intentBroker, LOGIN_ACTIVITY_TO_BROKER_LIST_ACTIVITY);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        mBinding.selectBroker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    String broker = mBinding.broker.getText().toString();
                    Intent intentBroker = new Intent(LoginActivity.this, BrokerListActivity.class);
                    intentBroker.putExtra("broker", broker);
                    startActivityForResult(intentBroker, LOGIN_ACTIVITY_TO_BROKER_LIST_ACTIVITY);
                } catch (Exception e) {
                    e.printStackTrace();
                }
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
                    mBinding.llAccount.setBackgroundResource(R.drawable.activity_login_rectangle_border_focused);
                } else {
                    mBinding.llAccount.setBackgroundResource(R.drawable.activity_login_rectangle_border);
                }
            }
        });

        mBinding.password.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    mBinding.llPassword.setBackgroundResource(R.drawable.activity_login_rectangle_border_focused);
                } else {
                    mBinding.llPassword.setBackgroundResource(R.drawable.activity_login_rectangle_border);
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
     * description:
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
            if ((System.currentTimeMillis() - mExitTime) > 2000) {
                ToastUtils.showToast(BaseApplication.getContext(), getString(R.string.main_activity_exit));
                mExitTime = System.currentTimeMillis();
            } else {
                mHandler.sendEmptyMessage(EXIT_APP);
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkNetwork();
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
        if (mIsFirm) mBrokerName = mBinding.broker.getText().toString();
        else mBrokerName = BROKER_ID_SIMULATION;
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
            if (mBrokerName != null && !(mBrokerName.equals(BROKER_ID_SIMULATION) || mBrokerName.equals(BROKER_ID_SIMNOW))) {
                Identify identify = new Identify();
                long currentTime = System.currentTimeMillis();
                long initTime = (long) SPUtils.get(sContext, CONFIG_INIT_TIME, currentTime);
                long loginTime = currentTime - initTime;
                identify.setOnce(AMP_USER_LOGIN_TIME_FIRST, loginTime);
                Amplitude.getInstance().identify(identify);
            }
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put(AMP_EVENT_LOGIN_BROKER_ID, mBrokerName);
                jsonObject.put(AMP_EVENT_LOGIN_USER_ID, mPhoneNumber);
                jsonObject.put(AMP_EVENT_LOGIN_TIME, TimeUtils.getAmpTime());
                jsonObject.put(AMP_EVENT_LOGIN_TYPE, AMP_EVENT_LOGIN_TYPE_VALUE_LOGIN);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            Amplitude.getInstance().logEvent(AMP_LOGIN, jsonObject);
            sDataManager.LOGIN_BROKER_ID = mBrokerName;
            sDataManager.LOGIN_USER_ID = mPhoneNumber;
            sDataManager.LOGIN_TYPE = AMP_EVENT_LOGIN_TYPE_VALUE_LOGIN;
            sDataManager.IS_SHOW_LOGIN_SUCCESS = true;
            mBinding.buttonIdLogin.setEnabled(false);

            // Show a progress spinner, and kick off a background task to
            // perform the user fragment_home attempt.
            WebSocketService.sendReqLogin(mBrokerName, mPhoneNumber, mPassword);

            //超时检测
            mHandler.sendEmptyMessageDelayed(LOGIN_TIME_OUT, 5000);

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
                    case TD_MESSAGE_LOGIN_SUCCEED:
                        //登录成功
                        mHandler.sendEmptyMessageDelayed(LOGIN_SUCCESS, 2000);
                        break;
                    case TD_MESSAGE_WEAK_PASSWORD:
                        //弱密码
                        mHandler.sendEmptyMessageDelayed(LOGIN_TO_CHANGE_PASSWORD, 2000);
                        break;
                    case TD_MESSAGE_BROKER_INFO:
                        if (mBinding.broker.getText().toString().isEmpty()
                                && mBinding.account.getText().toString().isEmpty())
                            initBrokerAccount();
                        break;
                    case TD_MESSAGE_LOGIN_FAIL:
                        //登录失败
                        mHandler.sendEmptyMessage(LOGIN_FAIL);
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
                case LOGIN_ACTIVITY_TO_BROKER_LIST_ACTIVITY:
                    String broker = data.getStringExtra("broker");
                    mBinding.broker.setText(broker);
                    break;
                case LOGIN_ACTIVITY_TO_CHANGE_PASSWORD_ACTIVITY:
                    mBinding.password.setText("");
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
            float versionCode = (float) SPUtils.get(sContext, CONFIG_VERSION_CODE, 0.0f);
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
                        SPUtils.putAndApply(LoginActivity.this, CONFIG_VERSION_CODE, nowVersionCode);
                        dialog.dismiss();
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
                    mHandler.sendEmptyMessageDelayed(EXIT_APP, 500);
                }
            });
            dialog.show();
        }
    }

    /**
     * date: 2019/4/2
     * author: chenli
     * description: 穿透视监管动态权限检查
     */
    private void checkPermissions() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_PHONE_STATE)
                != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_PHONE_STATE,
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION},
                    MY_PERMISSIONS_REQUEST);
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST:
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 2
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED
                        && grantResults[1] == PackageManager.PERMISSION_GRANTED
                        && grantResults[2] == PackageManager.PERMISSION_GRANTED) {

                    getSystemInfo();

                } else {
                    mHandler.sendEmptyMessageDelayed(MY_PERMISSIONS_REQUEST_DENIED, 1000);
                }
                break;
            default:
                break;

        }
    }

    /**
     * date: 2019/5/30
     * author: chenli
     * description: 穿透式监管信息
     */
    private void getSystemInfo() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                byte[] info = DeviceInfoManager.getCollectInfo(LoginActivity.this);
                String encodeInfo = Base64.encode(info);
                SPUtils.putAndApply(sContext, CONFIG_SYSTEM_INFO, encodeInfo);
            }
        }).start();
    }

    /**
     * date: 2019/6/4
     * author: chenli
     * description: 切换模拟
     */
    private void switchSimulator() {
        mBinding.simulation.setTextColor(getResources().getColor(R.color.white));
        mBinding.simulationUnderline.setVisibility(View.VISIBLE);
        mBinding.firm.setTextColor(getResources().getColor(R.color.login_gray));
        mBinding.firmUnderline.setVisibility(View.INVISIBLE);

        mBinding.tvBroker.setVisibility(View.GONE);
        mBinding.llBroker.setVisibility(View.GONE);
        mBinding.tvAccount.setText("手机号码");
        mBinding.simulationHint.setVisibility(View.VISIBLE);

        changeStatusBarColor(false);
        mIsFirm = false;

        boolean isFirm = (boolean) SPUtils.get(sContext, CONFIG_IS_FIRM, true);
        //获取用户登录成功后保存在sharedPreference里的期货公司
        if (SPUtils.contains(sContext, CONFIG_ACCOUNT) && !isFirm) {
            String account = (String) SPUtils.get(sContext, CONFIG_ACCOUNT, "");
            if (account.contains(BROKER_ID_VISITOR)) return;
            mBinding.account.setText(account);
            mBinding.account.setSelection(account.length());
            if (!account.isEmpty()) mBinding.deleteAccount.setVisibility(View.VISIBLE);
        } else {
            mBinding.account.getEditableText().clear();
        }
    }

    /**
     * date: 2019/6/4
     * author: chenli
     * description: 切换实盘
     */
    private void switchFirm() {
        mBinding.firm.setTextColor(getResources().getColor(R.color.white));
        mBinding.firmUnderline.setVisibility(View.VISIBLE);
        mBinding.simulation.setTextColor(getResources().getColor(R.color.login_gray));
        mBinding.simulationUnderline.setVisibility(View.INVISIBLE);

        mBinding.tvBroker.setVisibility(View.VISIBLE);
        mBinding.llBroker.setVisibility(View.VISIBLE);
        mBinding.tvAccount.setText("资金账号");
        mBinding.simulationHint.setVisibility(View.GONE);

        changeStatusBarColor(true);
        mIsFirm = true;

        boolean isFirm = (boolean) SPUtils.get(sContext, CONFIG_IS_FIRM, true);
        List<String> brokers = LatestFileManager.getBrokerIdFromBuildConfig(sDataManager.getBroker().getBrokers());
        //获取用户登录成功后保存在sharedPreference里的期货公司
        if (SPUtils.contains(sContext, CONFIG_BROKER) && isFirm) {
            String brokerName = (String) SPUtils.get(sContext, CONFIG_BROKER, "");
            String account = (String) SPUtils.get(sContext, CONFIG_ACCOUNT, "");
            if (brokers.contains(brokerName)) mBinding.broker.setText(brokerName);
            mBinding.account.setText(account);
            mBinding.account.setSelection(account.length());
            if (!account.isEmpty()) mBinding.deleteAccount.setVisibility(View.VISIBLE);
        } else if (!brokers.isEmpty()) {
            mBinding.broker.setText(brokers.get(0));
            mBinding.account.getEditableText().clear();
        }
    }

    private void changeStatusBarColor(boolean isFirm) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            Window w = getWindow();
            w.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            int statusBarHeight = getStatusBarHeight(sContext);

            View view = new View(this);
            view.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            view.getLayoutParams().height = statusBarHeight;
            ((ViewGroup) w.getDecorView()).addView(view);
            if (isFirm) view.setBackground(getResources().getDrawable(R.color.colorPrimaryDark));
            else view.setBackground(getResources().getDrawable(R.color.login_simulation_hint));

        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();

            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);

            if (isFirm)
                window.setStatusBarColor(ContextCompat.getColor(this, R.color.colorPrimaryDark));
            else
                window.setStatusBarColor(ContextCompat.getColor(this, R.color.login_simulation_hint));
        }
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
                    case LOGIN_SUCCESS:
                        SPUtils.putAndApply(activity.sContext, CONFIG_LOGIN_DATE, TimeUtils.getNowTime());
                        SPUtils.putAndApply(activity.sContext, CONFIG_ACCOUNT, activity.mPhoneNumber);
                        SPUtils.putAndApply(activity.sContext, CONFIG_PASSWORD, activity.mPassword);
                        SPUtils.putAndApply(activity.sContext, CONFIG_BROKER, activity.mBrokerName);
                        SPUtils.putAndApply(activity.sContext, CONFIG_IS_FIRM, activity.mIsFirm);
                        //关闭键盘
                        View view = activity.getWindow().getCurrentFocus();
                        if (view != null) {
                            InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
                            if (inputMethodManager != null)
                                inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), HIDE_NOT_ALWAYS);
                        }
                        JSONObject jsonObjectSwitch = new JSONObject();
                        try {
                            jsonObjectSwitch.put(AMP_EVENT_CURRENT_PAGE, AMP_EVENT_PAGE_VALUE_LOGIN);
                            jsonObjectSwitch.put(AMP_EVENT_TARGET_PAGE, AMP_EVENT_PAGE_VALUE_MAIN);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        Amplitude.getInstance().logEvent(AMP_SWITCH_PAGE, jsonObjectSwitch);
                        Intent intent1 = new Intent(activity, MainActivity.class);
                        activity.startActivity(intent1);
                        activity.finish();
                        break;
                    case LOGIN_FAIL:
                        activity.mBinding.visitor.setEnabled(true);
                        activity.mBinding.buttonIdLogin.setEnabled(true);
                        break;
                    case LOGIN_TO_CHANGE_PASSWORD:
                        Intent intent = new Intent(activity, ChangePasswordActivity.class);
                        activity.startActivityForResult(intent, LOGIN_ACTIVITY_TO_CHANGE_PASSWORD_ACTIVITY);
                        break;
                    case LOGIN_TIME_OUT:
                        if (activity.sDataManager.LOGIN_USER_ID.isEmpty()) {
                            JSONObject jsonObject = new JSONObject();
                            try {
                                jsonObject.put(AMP_EVENT_LOGIN_BROKER_ID, activity.mBrokerName);
                                jsonObject.put(AMP_EVENT_LOGIN_USER_ID, activity.mPhoneNumber);
                                jsonObject.put(AMP_EVENT_LOGIN_TIME, TimeUtils.getAmpTime());
                                jsonObject.put(AMP_EVENT_LOGIN_TYPE, activity.sDataManager.LOGIN_TYPE);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            Amplitude.getInstance().logEvent(AMP_LOGIN_TIME_OUT, jsonObject);
                        }
                        break;
                    case EXIT_APP:
                        Intent startMain = new Intent(Intent.ACTION_MAIN);
                        startMain.addCategory(Intent.CATEGORY_HOME);
                        startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        activity.startActivity(startMain);
                        break;
                    case MY_PERMISSIONS_REQUEST_DENIED:
                        activity.checkPermissions();
                        break;
                    default:
                        break;
                }
            }
        }
    }

}


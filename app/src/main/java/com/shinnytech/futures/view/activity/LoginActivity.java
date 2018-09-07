package com.shinnytech.futures.view.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;

import com.shinnytech.futures.BuildConfig;
import com.shinnytech.futures.R;
import com.shinnytech.futures.application.BaseApplicationLike;
import com.shinnytech.futures.databinding.ActivityLoginBinding;
import com.shinnytech.futures.model.engine.DataManager;
import com.shinnytech.futures.utils.AndroidBug5497Workaround;
import com.shinnytech.futures.utils.SPUtils;
import com.shinnytech.futures.utils.ToastNotificationUtils;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static android.view.inputmethod.InputMethodManager.HIDE_NOT_ALWAYS;
import static com.shinnytech.futures.constants.CommonConstants.ACTIVITY_TYPE;
import static com.shinnytech.futures.constants.CommonConstants.CLOSE;
import static com.shinnytech.futures.constants.CommonConstants.ERROR;
import static com.shinnytech.futures.constants.CommonConstants.KUAI_QI_XIAO_Q;
import static com.shinnytech.futures.constants.CommonConstants.MESSAGE_BROKER_INFO;
import static com.shinnytech.futures.constants.CommonConstants.MESSAGE_LOGIN;
import static com.shinnytech.futures.constants.CommonConstants.OPEN;
import static com.shinnytech.futures.model.receiver.NetworkReceiver.NETWORK_STATE;
import static com.shinnytech.futures.model.service.WebSocketService.BROADCAST_ACTION_TRANSACTION;

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
     * description: 用户名输入框的下拉列表
     */
    private String[] mData = new String[]{""};
    /**
     * date: 7/7/17
     * description: 网络监听广播
     */
    private BroadcastReceiver mReceiver;
    /**
     * date: 7/7/17
     * description: 用户登录监听广播
     */
    private BroadcastReceiver mReceiverLogin;
    private String mActivityType;
    private DataManager sDataManager;
    private Context sContext;
    private String mBrokerId;
    private String mPhoneNumber;
    private ArrayAdapter<String> mSpinnerAdapter;
    private Handler mHandler;
    private ActivityLoginBinding mBinding;
    private String mPassword;

    /**
     * date: 6/1/18
     * author: chenli
     * description: 根据不同软件版本获取期货公司列表
     */
    private List<String> getBrokerIdFromBuildConfig(String[] brokerIdOrigin) {
        List<String> brokerList = new ArrayList<>();
        if (brokerIdOrigin != null) {
            if (KUAI_QI_XIAO_Q.equals(BuildConfig.BROKER_ID)) {
                brokerList.addAll(Arrays.asList(brokerIdOrigin));
            } else {
                for (int i = 0; i < brokerIdOrigin.length; i++) {
                    if (brokerIdOrigin[i] != null && brokerIdOrigin[i].contains(BuildConfig.BROKER_ID)) {
                        brokerList.add(brokerIdOrigin[i]);
                    }
                }
            }
        } else brokerList.add(" ");
        return brokerList;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mLayoutID = R.layout.activity_login;
        mTitle = "登录";
        super.onCreate(savedInstanceState);
        AndroidBug5497Workaround.assistActivity(this);
    }

    @Override
    protected void initData() {
        sContext = BaseApplicationLike.getContext();
        sDataManager = DataManager.getInstance();
        mHandler = new MyHandler(this);
        mBinding = (ActivityLoginBinding) mViewDataBinding;
        mActivityType = getIntent().getStringExtra(ACTIVITY_TYPE);
        List<String> brokerList = getBrokerIdFromBuildConfig(sDataManager.getLogin().getBrokers());
        mSpinnerAdapter = new ArrayAdapter<>(this, R.layout.spinner_display_style, R.id.tv_Spinner, brokerList);
        mSpinnerAdapter.setDropDownViewResource(R.layout.spinner_dropdown_style);
        mBinding.spinner.setAdapter(mSpinnerAdapter);

        //获取用户登录成功后保存在sharedPreference里的期货公司
        if (SPUtils.contains(sContext, "brokerId") && brokerList.size() > 1) {
            int brokerId = (int) SPUtils.get(sContext, "brokerId", 0);
            if (brokerId < brokerList.size()) mBinding.spinner.setSelection(brokerId, true);
        }

        //获取用户登录成功后保存在sharedPreference里的用户名
        if (SPUtils.contains(sContext, "phone")) {
            mData[0] = (String) SPUtils.get(sContext, "phone", "");
        }
        //初始化用户名输入框，避免重复输入
        mBinding.tvIdPhone.requestFocus();
        mBinding.tvIdPhone.setText(mData[0]);
        mBinding.tvIdPhone.setSelection(mData[0].length());
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, mData);
        mBinding.tvIdPhone.setAdapter(adapter);

        if (SPUtils.contains(sContext, "password")) {
            String password = (String) SPUtils.get(sContext, "password", "");
            mBinding.etIdPassword.setText(password);
        }
    }

    @Override
    protected void initEvent() {

        //点击登录
        mBinding.buttonIdLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

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
    protected void onResume() {
        super.onResume();
        registerBroaderCast();
        updateToolbarFromNetwork(sContext, "登录");
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mReceiver);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mReceiverLogin);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    /**
     * Attempts to sign in or register the activity_account specified by the fragment_home form.
     * If there are form errors (invalid phone, missing fields, etc.), the
     * errors are presented and no actual fragment_home attempt is made.
     */
    private void attemptLogin() {
        // Reset errors.
        mBinding.tvIdPhone.setError(null);
        mBinding.etIdPassword.setError(null);

        // Store values at the time of the fragment_home attempt.
        mBrokerId = (String) mBinding.spinner.getSelectedItem();
        mPhoneNumber = mBinding.tvIdPhone.getText().toString();
        mPassword = mBinding.etIdPassword.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (TextUtils.isEmpty(mPassword)) {
            mBinding.etIdPassword.setError(getString(R.string.login_activity_error_invalid_password));
            focusView = mBinding.etIdPassword;
            cancel = true;
        }

        // Check for a valid phone number.
        if (TextUtils.isEmpty(mPhoneNumber)) {
            mBinding.tvIdPhone.setError(getString(R.string.login_activity_error_field_required));
            focusView = mBinding.tvIdPhone;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt fragment_home and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user fragment_home attempt.
            if (BaseApplicationLike.getWebSocketService() != null)
                BaseApplicationLike.getWebSocketService().sendReqLogin(mBrokerId, mPhoneNumber, mPassword);

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
        mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                int networkStatus = intent.getIntExtra("networkStatus", 0);
                switch (networkStatus) {
                    case 0:
                        mToolbar.setBackgroundColor(ContextCompat.getColor(context, R.color.off_line));
                        mToolbarTitle.setTextColor(Color.BLACK);
                        mToolbarTitle.setText("交易、行情网络未连接！");
                        mToolbarTitle.setTextSize(20);
                        break;
                    case 1:
                        mToolbar.setBackgroundColor(ContextCompat.getColor(context, R.color.black_dark));
                        mToolbarTitle.setTextColor(Color.WHITE);
                        mToolbarTitle.setText("登录");
                        mToolbarTitle.setTextSize(25);
                        break;
                    default:
                        break;
                }
            }
        };

        mReceiverLogin = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String msg = intent.getStringExtra("msg");
                switch (msg) {
                    case OPEN:
                        break;
                    case CLOSE:
                        break;
                    case ERROR:
                        break;
                    case MESSAGE_LOGIN:
                        mHandler.sendEmptyMessageDelayed(0, 2000);
                        break;
                    case MESSAGE_BROKER_INFO:
                        //如果客户端打开后期货公司列表信息还没有解析完毕，服务器发送brokerId后更新期货公司列表
                        mHandler.sendEmptyMessage(1);
                        break;
                    default:
                        break;
                }
            }
        };
        registerReceiver(mReceiver, new IntentFilter(NETWORK_STATE));
        LocalBroadcastManager.getInstance(this).registerReceiver(mReceiverLogin, new IntentFilter(BROADCAST_ACTION_TRANSACTION));
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
                        activity.sDataManager.IS_LOGIN = true;
                        SPUtils.putAndApply(activity.sContext, "phone", activity.mPhoneNumber);
                        SPUtils.putAndApply(activity.sContext, "password", activity.mPassword);
                        SPUtils.putAndApply(activity.sContext, "brokerId", activity.mBinding.spinner.getSelectedItemPosition());
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
                        activity.mSpinnerAdapter.clear();
                        List<String> brokerList = activity.getBrokerIdFromBuildConfig(activity.sDataManager.getLogin().getBrokers());
                        activity.mSpinnerAdapter.addAll(brokerList);
                        //获取用户登录成功后保存在sharedPreference里的期货公司
                        if (SPUtils.contains(activity.sContext, "brokerId") && brokerList.size() > 1) {
                            int brokerId = (int) SPUtils.get(activity.sContext, "brokerId", 0);
                            if (brokerId < brokerList.size())
                                activity.mBinding.spinner.setSelection(brokerId, true);
                        }
                        break;
                    default:
                        break;
                }
            }
        }
    }

}


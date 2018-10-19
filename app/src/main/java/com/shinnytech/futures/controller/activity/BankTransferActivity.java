package com.shinnytech.futures.controller.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;

import com.shinnytech.futures.R;
import com.shinnytech.futures.application.BaseApplication;
import com.shinnytech.futures.databinding.ActivityBankTransferBinding;
import com.shinnytech.futures.model.bean.accountinfobean.BankEntity;
import com.shinnytech.futures.model.bean.accountinfobean.TransferEntity;
import com.shinnytech.futures.model.bean.accountinfobean.UserEntity;
import com.shinnytech.futures.model.engine.DataManager;
import com.shinnytech.futures.utils.CloneUtils;
import com.shinnytech.futures.utils.DividerItemDecorationUtils;
import com.shinnytech.futures.utils.ToastNotificationUtils;
import com.shinnytech.futures.model.adapter.BankTransferAdapter;
import com.shinnytech.futures.model.listener.TransferDiffCallback;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.shinnytech.futures.constants.CommonConstants.ACTIVITY_TYPE;
import static com.shinnytech.futures.constants.CommonConstants.BANK;
import static com.shinnytech.futures.constants.CommonConstants.TD_MESSAGE;
import static com.shinnytech.futures.model.receiver.NetworkReceiver.NETWORK_STATE;
import static com.shinnytech.futures.model.service.WebSocketService.TD_BROADCAST_ACTION;
import static java.lang.Math.abs;

public class BankTransferActivity extends BaseActivity {

    private BroadcastReceiver mReceiver;
    private BroadcastReceiver mReceiver1;
    private DataManager sDataManager;
    private Context sContext;
    private ArrayAdapter<String> mBankSpinnerAdapter;
    private ArrayAdapter<String> mCurrencySpinnerAdapter;
    private ActivityBankTransferBinding mBinding;
    private List<TransferEntity> mOldData;
    private List<TransferEntity> mNewData;
    private BankTransferAdapter mAdapter;
    private boolean mIsUpdate;
    private Map<String, String> mBankId;

    /**
     * date: 7/7/17
     * author: chenli
     * description: 注册账户广播，监听账户实时信息
     */
    private void registerBroaderCast() {
        mReceiver1 = new BroadcastReceiver() {
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
                        mToolbarTitle.setText(mTitle);
                        mToolbarTitle.setTextSize(25);
                        break;
                    default:
                        break;
                }
            }
        };
        registerReceiver(mReceiver1, new IntentFilter(NETWORK_STATE));

        mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String mDataString = intent.getStringExtra("msg");
                switch (mDataString) {
                    case TD_MESSAGE:
                        if (mBankSpinnerAdapter.isEmpty())refreshBank();
                        if (mCurrencySpinnerAdapter.isEmpty()) refreshCurrency();
                        if (mIsUpdate) refreshTransfer();
                        break;
                    default:
                        break;
                }
            }
        };
        LocalBroadcastManager.getInstance(this).registerReceiver(mReceiver, new IntentFilter(TD_BROADCAST_ACTION));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mLayoutID = R.layout.activity_bank_transfer;
        mTitle = BANK;
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void initData() {
        mBinding = (ActivityBankTransferBinding) mViewDataBinding;
        sContext = BaseApplication.getContext();
        sDataManager = DataManager.getInstance();
        mIsUpdate = true;
        mOldData = new ArrayList<>();
        mNewData = new ArrayList<>();
        mBinding.rv.setLayoutManager(new LinearLayoutManager(this));
        mBinding.rv.addItemDecoration(
                new DividerItemDecorationUtils(this, DividerItemDecorationUtils.VERTICAL_LIST));
        mBinding.rv.setItemAnimator(new DefaultItemAnimator());
        mAdapter = new BankTransferAdapter(this, mOldData);
        mBinding.rv.setAdapter(mAdapter);

        mBankId = new HashMap<>();
        List<String> bankList = new ArrayList<>();
        List<String> currencyList = new ArrayList<>();

        mBankSpinnerAdapter = new ArrayAdapter<>(this, R.layout.spinner_display_style, R.id.tv_Spinner, bankList);
        mBankSpinnerAdapter.setDropDownViewResource(R.layout.spinner_dropdown_style);
        mBinding.spinnerBank.setAdapter(mBankSpinnerAdapter);

        mCurrencySpinnerAdapter = new ArrayAdapter<>(this, R.layout.spinner_display_style, R.id.tv_Spinner, currencyList);
        mCurrencySpinnerAdapter.setDropDownViewResource(R.layout.spinner_dropdown_style);
        mBinding.spinnerCurrency.setAdapter(mCurrencySpinnerAdapter);
    }

    @Override
    protected void initEvent() {
        mBinding.futureBank.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String futureAccount = sDataManager.USER_ID;
                String bank = (String) mBinding.spinnerBank.getSelectedItem();
                String bankId = mBankId.get(bank);
                String accountPassword = mBinding.etAccountPassword.getText().toString();
                String bankPassword = mBinding.etBankPassword.getText().toString();
                String amount = mBinding.etTransferMoney.getText().toString();
                String currency = (String) mBinding.spinnerCurrency.getSelectedItem();
                try {
                    float amountF = -abs(Float.parseFloat(amount));
                    BaseApplication.getWebSocketService().sendReqTransfer(futureAccount, accountPassword, bankId, bankPassword, currency, amountF);
                } catch (Exception e) {
                    ToastNotificationUtils.showToast(sContext, "输入金额错误！");
                }
            }
        });

        mBinding.bankFuture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String futureAccount = sDataManager.USER_ID;
                String bank = (String) mBinding.spinnerBank.getSelectedItem();
                String bankId = mBankId.get(bank);
                String accountPassword = mBinding.etAccountPassword.getText().toString();
                String bankPassword = mBinding.etBankPassword.getText().toString();
                String amount = mBinding.etTransferMoney.getText().toString();
                String currency = (String) mBinding.spinnerCurrency.getSelectedItem();
                try {
                    float amountF = abs(Float.parseFloat(amount)) ;
                    BaseApplication.getWebSocketService().sendReqTransfer(futureAccount, accountPassword, bankId, bankPassword, currency, amountF);
                } catch (Exception e) {
                    ToastNotificationUtils.showToast(sContext, "输入金额错误！");
                }
            }
        });

        mBinding.rv.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                switch (newState) {
                    case RecyclerView.SCROLL_STATE_IDLE:
                        mIsUpdate = true;
                        break;
                    case RecyclerView.SCROLL_STATE_DRAGGING:
                        mIsUpdate = false;
                        break;
                    case RecyclerView.SCROLL_STATE_SETTLING:
                        mIsUpdate = false;
                        break;
                }
            }
        });

    }

    @Override
    public void onResume() {
        super.onResume();
        updateToolbarFromNetwork(sContext, mTitle);
        if (!sDataManager.IS_LOGIN) {
            Intent intent = new Intent(this, LoginActivity.class);
            //判断从哪个页面跳到登录页，登录页的销毁方式不一样
            intent.putExtra(ACTIVITY_TYPE, "MainActivity");
            startActivity(intent);
        }
        registerBroaderCast();
        refreshBank();
        refreshCurrency();
        refreshTransfer();
    }

    @Override
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mReceiver);
        unregisterReceiver(mReceiver1);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case android.R.id.home:
                finish();
        }
        return super.onOptionsItemSelected(item);
    }

    private void refreshBank() {
        UserEntity userEntity = sDataManager.getTradeBean().getUsers().get(sDataManager.USER_ID);
        if (userEntity == null) return;
        mBankId.clear();
        mBankSpinnerAdapter.clear();
        List<String> bankList = new ArrayList<>();
        for (BankEntity bankEntity :
                userEntity.getBanks().values()) {
            bankList.add(bankEntity.getName());
            mBankId.put(bankEntity.getName(), bankEntity.getId());
        }
        mBankSpinnerAdapter.addAll(bankList);
        mBankSpinnerAdapter.notifyDataSetChanged();

    }

    private void refreshCurrency(){
        UserEntity userEntity = sDataManager.getTradeBean().getUsers().get(sDataManager.USER_ID);
        if (userEntity == null) return;
        mCurrencySpinnerAdapter.clear();
        List<String> currencyList = new ArrayList<>();
        for (String currency :
                userEntity.getAccounts().keySet()) {
            currencyList.add(currency);
        }
        mCurrencySpinnerAdapter.addAll(currencyList);
        mCurrencySpinnerAdapter.notifyDataSetChanged();
    }

    private void refreshTransfer() {
        UserEntity userEntity = sDataManager.getTradeBean().getUsers().get(sDataManager.USER_ID);
        if (userEntity == null) return;
        mNewData.clear();
        for (TransferEntity transferEntity :
                userEntity.getTransfers().values()) {
            TransferEntity t = CloneUtils.clone(transferEntity);
            mNewData.add(t);
        }
        Collections.sort(mNewData);
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new TransferDiffCallback(mOldData, mNewData), false);
        mAdapter.setData(mNewData);
        diffResult.dispatchUpdatesTo(mAdapter);
        mOldData.clear();
        mOldData.addAll(mNewData);
    }

}

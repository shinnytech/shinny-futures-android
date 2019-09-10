package com.shinnytech.futures.controller.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.View;
import android.widget.ArrayAdapter;

import com.shinnytech.futures.R;
import com.shinnytech.futures.amplitude.api.Amplitude;
import com.shinnytech.futures.amplitude.api.Identify;
import com.shinnytech.futures.application.BaseApplication;
import com.shinnytech.futures.databinding.ActivityBankTransferBinding;
import com.shinnytech.futures.model.adapter.BankTransferAdapter;
import com.shinnytech.futures.model.bean.accountinfobean.BankEntity;
import com.shinnytech.futures.model.bean.accountinfobean.TransferEntity;
import com.shinnytech.futures.model.bean.accountinfobean.UserEntity;
import com.shinnytech.futures.model.listener.TransferDiffCallback;
import com.shinnytech.futures.utils.CloneUtils;
import com.shinnytech.futures.utils.DividerItemDecorationUtils;
import com.shinnytech.futures.utils.ToastUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.shinnytech.futures.application.BaseApplication.TD_BROADCAST_ACTION;
import static com.shinnytech.futures.constants.AmpConstants.AMP_USER_BANK_FIRST;
import static com.shinnytech.futures.constants.AmpConstants.AMP_USER_BANK_LAST;
import static com.shinnytech.futures.constants.BroadcastConstants.TD_MESSAGE;
import static com.shinnytech.futures.constants.CommonConstants.TRANSFER_DIRECTION;
import static com.shinnytech.futures.constants.CommonConstants.TRANSFER_IN;
import static com.shinnytech.futures.constants.CommonConstants.TRANSFER_OUT;
import static java.lang.Math.abs;

public class BankTransferActivity extends BaseActivity {

    private ArrayAdapter<String> mBankSpinnerAdapter;
    private ArrayAdapter<String> mCurrencySpinnerAdapter;
    private ActivityBankTransferBinding mBinding;
    private List<TransferEntity> mOldData;
    private List<TransferEntity> mNewData;
    private BankTransferAdapter mAdapter;
    private boolean mIsUpdate;
    private Map<String, String> mBankId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mLayoutID = R.layout.activity_bank_transfer;
        Intent intent = getIntent();
        if (intent != null) {
            mTitle = intent.getStringExtra(TRANSFER_DIRECTION);
        }
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void initData() {
        mBinding = (ActivityBankTransferBinding) mViewDataBinding;
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

        if (TRANSFER_IN.equals(mTitle)) mBinding.futureBank.setVisibility(View.GONE);

        if (TRANSFER_OUT.equals(mTitle)) mBinding.bankFuture.setVisibility(View.GONE);
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
                    BaseApplication.getmTDWebSocket().sendReqTransfer(futureAccount, accountPassword, bankId, bankPassword, currency, amountF);
                    Identify identify = new Identify()
                            .setOnce(AMP_USER_BANK_FIRST, bank)
                            .set(AMP_USER_BANK_LAST, bank);
                    Amplitude.getInstance().identify(identify);
                } catch (Exception e) {
                    ToastUtils.showToast(sContext, "输入金额错误！");
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
                    float amountF = abs(Float.parseFloat(amount));
                    BaseApplication.getmTDWebSocket().sendReqTransfer(futureAccount, accountPassword, bankId, bankPassword, currency, amountF);
                    Identify identify = new Identify()
                            .setOnce(AMP_USER_BANK_FIRST, bank)
                            .set(AMP_USER_BANK_LAST, bank);
                    Amplitude.getInstance().identify(identify);
                } catch (Exception e) {
                    ToastUtils.showToast(sContext, "输入金额错误！");
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

    private void refreshUI() {
        if (mBankSpinnerAdapter.isEmpty()) refreshBank();
        if (mCurrencySpinnerAdapter.isEmpty()) refreshCurrency();
        if (mIsUpdate) refreshTransfer();
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshUI();
        registerBroaderCast();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mReceiverLocal != null)
            LocalBroadcastManager.getInstance(this).unregisterReceiver(mReceiverLocal);
    }

    /**
     * date: 7/7/17
     * author: chenli
     * description: 注册账户广播，监听账户实时信息
     */
    protected void registerBroaderCast() {
        mReceiverLocal = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String msg = intent.getStringExtra("msg");
                if (TD_MESSAGE.equals(msg)) refreshUI();
            }
        };
        LocalBroadcastManager.getInstance(this).registerReceiver(mReceiverLocal, new IntentFilter(TD_BROADCAST_ACTION));
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

    private void refreshCurrency() {
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
        try {
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
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}

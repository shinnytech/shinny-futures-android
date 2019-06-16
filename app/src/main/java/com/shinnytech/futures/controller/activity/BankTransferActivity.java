package com.shinnytech.futures.controller.activity;

import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewTreeObserver;
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

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.shinnytech.futures.constants.CommonConstants.AMP_EVENT_AMOUNT;
import static com.shinnytech.futures.constants.CommonConstants.AMP_EVENT_BANK;
import static com.shinnytech.futures.constants.CommonConstants.AMP_EVENT_CURRENCY;
import static com.shinnytech.futures.constants.CommonConstants.AMP_EVENT_LOGIN_BROKER_ID;
import static com.shinnytech.futures.constants.CommonConstants.AMP_EVENT_LOGIN_USER_ID;
import static com.shinnytech.futures.constants.CommonConstants.AMP_TRANSFER_IN;
import static com.shinnytech.futures.constants.CommonConstants.AMP_TRANSFER_OUT;
import static com.shinnytech.futures.constants.CommonConstants.AMP_USER_BANK_FIRST;
import static com.shinnytech.futures.constants.CommonConstants.AMP_USER_BANK_LAST;
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
    private View mRootView;  //activity的根视图
    private boolean mIsKeyboardShowing;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mLayoutID = R.layout.activity_bank_transfer;
        Intent intent = getIntent();
        if (intent != null) {
            mTitle = intent.getStringExtra(TRANSFER_DIRECTION);
        }
        mRootView = this.getWindow().getDecorView();
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
                String futureAccount = sDataManager.LOGIN_USER_ID;
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
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put(AMP_EVENT_LOGIN_BROKER_ID, sDataManager.LOGIN_BROKER_ID);
                    jsonObject.put(AMP_EVENT_LOGIN_USER_ID, sDataManager.LOGIN_USER_ID);
                    jsonObject.put(AMP_EVENT_BANK, bank);
                    jsonObject.put(AMP_EVENT_AMOUNT, amountF);
                    jsonObject.put(AMP_EVENT_CURRENCY, currency);
                    Amplitude.getInstance().logEvent(AMP_TRANSFER_OUT, jsonObject);
                } catch (Exception e) {
                    ToastUtils.showToast(sContext, "输入金额错误！");
                }
            }
        });

        mBinding.bankFuture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String futureAccount = sDataManager.LOGIN_USER_ID;
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
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put(AMP_EVENT_LOGIN_BROKER_ID, sDataManager.LOGIN_BROKER_ID);
                    jsonObject.put(AMP_EVENT_LOGIN_USER_ID, sDataManager.LOGIN_USER_ID);
                    jsonObject.put(AMP_EVENT_BANK, bank);
                    jsonObject.put(AMP_EVENT_AMOUNT, amountF);
                    jsonObject.put(AMP_EVENT_CURRENCY, currency);
                    Amplitude.getInstance().logEvent(AMP_TRANSFER_IN, jsonObject);
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

        mRootView.getViewTreeObserver().addOnGlobalLayoutListener(
                new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {

                        Rect r = new Rect();
                        mRootView.getWindowVisibleDisplayFrame(r);
                        int screenHeight = mRootView.getRootView().getHeight();

                        int keypadHeight = screenHeight - r.bottom;

                        if (keypadHeight > screenHeight * 0.15) { // 0.15 ratio is perhaps enough to determine keypad height.
                            // keyboard is opened
                            if (!mIsKeyboardShowing) {
                                mIsKeyboardShowing = true;
                            }
                        } else {
                            // keyboard is closed
                            if (mIsKeyboardShowing) {
                                mIsKeyboardShowing = false;
                            }
                        }
                    }
                });

    }

    @Override
    protected void refreshUI() {
        if (mBankSpinnerAdapter.isEmpty()) refreshBank();
        if (mCurrencySpinnerAdapter.isEmpty()) refreshCurrency();
        if (mIsUpdate) refreshTransfer();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    private void refreshBank() {
        UserEntity userEntity = sDataManager.getTradeBean().getUsers().get(sDataManager.LOGIN_USER_ID);
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
        UserEntity userEntity = sDataManager.getTradeBean().getUsers().get(sDataManager.LOGIN_USER_ID);
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
            UserEntity userEntity = sDataManager.getTradeBean().getUsers().get(sDataManager.LOGIN_USER_ID);
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

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (mIsKeyboardShowing) return true;
        else return super.onKeyDown(keyCode, event);
    }
}

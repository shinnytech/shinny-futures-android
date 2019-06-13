package com.shinnytech.futures.controller.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.shinnytech.futures.R;
import com.shinnytech.futures.amplitude.api.Amplitude;
import com.shinnytech.futures.controller.activity.BankTransferActivity;
import com.shinnytech.futures.controller.activity.FutureInfoActivity;
import com.shinnytech.futures.controller.activity.MainActivity;
import com.shinnytech.futures.databinding.FragmentAccountBinding;
import com.shinnytech.futures.model.adapter.ViewPagerFragmentAdapter;
import com.shinnytech.futures.model.bean.accountinfobean.AccountEntity;
import com.shinnytech.futures.model.bean.accountinfobean.UserEntity;
import com.shinnytech.futures.model.engine.DataManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import static com.shinnytech.futures.constants.CommonConstants.AMP_ACCOUNT_TRANSFER_IN;
import static com.shinnytech.futures.constants.CommonConstants.AMP_ACCOUNT_TRANSFER_OUT;
import static com.shinnytech.futures.constants.CommonConstants.AMP_EVENT_CURRENT_PAGE;
import static com.shinnytech.futures.constants.CommonConstants.AMP_EVENT_LOGIN_BROKER_ID;
import static com.shinnytech.futures.constants.CommonConstants.AMP_EVENT_LOGIN_USER_ID;
import static com.shinnytech.futures.constants.CommonConstants.AMP_EVENT_PAGE_VALUE_MAIN;
import static com.shinnytech.futures.constants.CommonConstants.AMP_EVENT_PAGE_VALUE_TRANSFER;
import static com.shinnytech.futures.constants.CommonConstants.AMP_EVENT_TARGET_PAGE;
import static com.shinnytech.futures.constants.CommonConstants.AMP_SWITCH_PAGE;
import static com.shinnytech.futures.constants.CommonConstants.INS_BETWEEN_ACTIVITY;
import static com.shinnytech.futures.constants.CommonConstants.MAIN_ACTIVITY_TO_FUTURE_INFO_ACTIVITY;
import static com.shinnytech.futures.constants.CommonConstants.MAIN_ACTIVITY_TO_TRANSFER_ACTIVITY;
import static com.shinnytech.futures.constants.CommonConstants.TD_MESSAGE;
import static com.shinnytech.futures.constants.CommonConstants.TRANSFER_DIRECTION;
import static com.shinnytech.futures.constants.CommonConstants.TRANSFER_IN;
import static com.shinnytech.futures.constants.CommonConstants.TRANSFER_OUT;
import static com.shinnytech.futures.service.WebSocketService.TD_BROADCAST_ACTION;

public class AccountFragment extends LazyLoadFragment {

    private DataManager sDataManager = DataManager.getInstance();
    private BroadcastReceiver mReceiverAccount;
    private FragmentAccountBinding mBinding;
    private ViewPagerFragmentAdapter mViewPagerFragmentAdapter;
    private String ins;
    private boolean mIsInit = true;

    public static AccountFragment newInstance() {
        AccountFragment fragment = new AccountFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void show() {
        try {
            refreshAccount();
            if (!mIsInit) {
                ((LazyLoadFragment) mViewPagerFragmentAdapter.getItem(mBinding.vp.getCurrentItem())).show();
            }
            mIsInit = false;
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public void leave() {
        try {
            if (!mIsInit) {
                ((LazyLoadFragment) mViewPagerFragmentAdapter.getItem(mBinding.vp.getCurrentItem())).leave();
            }
            mIsInit = false;
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_account, container, false);
        initData();
        initEvent();
        return mBinding.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();
        registerBroaderCast();
    }

    @Override
    public void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mReceiverAccount);
    }

    private void registerBroaderCast() {
        mReceiverAccount = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String mDataString = intent.getStringExtra("msg");
                switch (mDataString) {
                    case TD_MESSAGE:
                        refreshAccount();
                        break;
                    default:
                        break;
                }
            }
        };

        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mReceiverAccount, new IntentFilter(TD_BROADCAST_ACTION));
    }

    /**
     * date: 2019/5/17
     * author: chenli
     * description: 刷新账户信息
     */
    public void refreshAccount() {
        UserEntity userEntity = sDataManager.getTradeBean().getUsers().get(sDataManager.LOGIN_USER_ID);
        if (userEntity == null) return;
        AccountEntity accountEntity = userEntity.getAccounts().get("CNY");
        if (accountEntity == null) return;
        mBinding.setAccount(accountEntity);
        mBinding.broker.setText(sDataManager.LOGIN_BROKER_ID);
    }

    private void initData() {
        mBinding.tabLayout.setTabTextColors(getResources().getColor(R.color.text_white),
                getResources().getColor(R.color.text_yellow));

        //初始化盘口、持仓、挂单、交易切换容器，fragment实例保存，有生命周期的变化，默认情况下屏幕外初始化两个fragment
        List<Fragment> fragmentList = new ArrayList<>();
        PositionFragment positionFragment = new PositionFragment();
        OrderFragment orderFragmentAlive = OrderFragment.newInstance(true);
        OrderFragment orderFragment = OrderFragment.newInstance(false);
        TradeFragment tradeFragment = new TradeFragment();
        fragmentList.add(positionFragment);
        fragmentList.add(orderFragmentAlive);
        fragmentList.add(orderFragment);
        fragmentList.add(tradeFragment);
        mViewPagerFragmentAdapter = new ViewPagerFragmentAdapter(getActivity().getSupportFragmentManager(), fragmentList);
        mBinding.vp.setAdapter(mViewPagerFragmentAdapter);
        mBinding.vp.setOffscreenPageLimit(4);
    }

    private void initEvent() {
        mBinding.vp.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                DataManager.getInstance().IS_POSITIVE = true;
                mBinding.tabLayout.getTabAt(position).select();
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        mBinding.tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                DataManager.getInstance().IS_POSITIVE = true;
                mBinding.vp.setCurrentItem(tab.getPosition(), false);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        mBinding.accountFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity) getActivity()).getmMainActivityPresenter()
                        .setPreSubscribedQuotes(sDataManager.getRtnData().getIns_list());
                sDataManager.IS_SHOW_VP_CONTENT = true;
                Intent intentPos = new Intent(getActivity(), FutureInfoActivity.class);
                intentPos.putExtra(INS_BETWEEN_ACTIVITY, ins);
                startActivityForResult(intentPos, MAIN_ACTIVITY_TO_FUTURE_INFO_ACTIVITY);
                mBinding.accountFab.hide();
            }
        });

        mBinding.transferIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                JSONObject jsonObject = new JSONObject();
                JSONObject jsonObject1 = new JSONObject();
                try {
                    jsonObject1.put(AMP_EVENT_LOGIN_BROKER_ID, sDataManager.LOGIN_BROKER_ID);
                    jsonObject1.put(AMP_EVENT_LOGIN_USER_ID, sDataManager.LOGIN_USER_ID);
                    jsonObject.put(AMP_EVENT_CURRENT_PAGE, AMP_EVENT_PAGE_VALUE_MAIN);
                    jsonObject.put(AMP_EVENT_TARGET_PAGE, AMP_EVENT_PAGE_VALUE_TRANSFER);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                Amplitude.getInstance().logEvent(AMP_SWITCH_PAGE, jsonObject);
                Amplitude.getInstance().logEvent(AMP_ACCOUNT_TRANSFER_IN, jsonObject1);
                Intent intentBank = new Intent(getActivity(), BankTransferActivity.class);
                intentBank.putExtra(TRANSFER_DIRECTION, TRANSFER_IN);
                getActivity().startActivityForResult(intentBank, MAIN_ACTIVITY_TO_TRANSFER_ACTIVITY);
            }
        });

        mBinding.transferOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                JSONObject jsonObject = new JSONObject();
                JSONObject jsonObject1 = new JSONObject();
                try {
                    jsonObject1.put(AMP_EVENT_LOGIN_BROKER_ID, sDataManager.LOGIN_BROKER_ID);
                    jsonObject1.put(AMP_EVENT_LOGIN_USER_ID, sDataManager.LOGIN_USER_ID);
                    jsonObject.put(AMP_EVENT_CURRENT_PAGE, AMP_EVENT_PAGE_VALUE_MAIN);
                    jsonObject.put(AMP_EVENT_TARGET_PAGE, AMP_EVENT_PAGE_VALUE_TRANSFER);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                Amplitude.getInstance().logEvent(AMP_SWITCH_PAGE, jsonObject);
                Amplitude.getInstance().logEvent(AMP_ACCOUNT_TRANSFER_OUT, jsonObject1);
                Intent intentBank = new Intent(getActivity(), BankTransferActivity.class);
                intentBank.putExtra(TRANSFER_DIRECTION, TRANSFER_OUT);
                getActivity().startActivityForResult(intentBank, MAIN_ACTIVITY_TO_TRANSFER_ACTIVITY);
            }
        });
    }

    public FragmentAccountBinding getmBinding() {
        return mBinding;
    }

    public void setIns(String ins) {
        this.ins = ins;
    }

    public void setmIsInit(boolean mIsInit) {
        this.mIsInit = mIsInit;
    }
}

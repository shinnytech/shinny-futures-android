package com.shinnytech.futures.controller.fragment;

import android.content.Intent;
import androidx.databinding.DataBindingUtil;
import android.os.Bundle;
import com.google.android.material.tabs.TabLayout;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.shinnytech.futures.R;
import com.shinnytech.futures.amplitude.api.Amplitude;
import com.shinnytech.futures.controller.activity.BankTransferActivity;
import com.shinnytech.futures.controller.activity.MainActivity;
import com.shinnytech.futures.databinding.FragmentAccountBinding;
import com.shinnytech.futures.model.adapter.ViewPagerFragmentAdapter;
import com.shinnytech.futures.model.bean.accountinfobean.AccountEntity;
import com.shinnytech.futures.model.bean.accountinfobean.UserEntity;
import com.shinnytech.futures.model.engine.DataManager;
import com.shinnytech.futures.utils.LogUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import static com.shinnytech.futures.constants.AmpConstants.AMP_EVENT_PAGE_ID;
import static com.shinnytech.futures.constants.AmpConstants.AMP_EVENT_PAGE_ID_VALUE_ACCOUNT;
import static com.shinnytech.futures.constants.AmpConstants.AMP_EVENT_SOURCE;
import static com.shinnytech.futures.constants.AmpConstants.AMP_EVENT_SWITCH_FROM;
import static com.shinnytech.futures.constants.AmpConstants.AMP_EVENT_SWITCH_TO;
import static com.shinnytech.futures.constants.AmpConstants.AMP_EVENT_TAB_ORDER;
import static com.shinnytech.futures.constants.AmpConstants.AMP_EVENT_TAB_ORDER_ALIVE;
import static com.shinnytech.futures.constants.AmpConstants.AMP_EVENT_TAB_POSITION;
import static com.shinnytech.futures.constants.AmpConstants.AMP_EVENT_TAB_TRADE;
import static com.shinnytech.futures.constants.AmpConstants.AMP_SHOW_PAGE;
import static com.shinnytech.futures.constants.AmpConstants.AMP_SWITCH_TAB;
import static com.shinnytech.futures.constants.CommonConstants.MAIN_ACTIVITY_TO_TRANSFER_ACTIVITY;
import static com.shinnytech.futures.constants.CommonConstants.TRANSFER_DIRECTION;
import static com.shinnytech.futures.constants.CommonConstants.TRANSFER_IN;
import static com.shinnytech.futures.constants.CommonConstants.TRANSFER_OUT;

public class AccountFragment extends LazyLoadFragment {

    private DataManager sDataManager = DataManager.getInstance();
    private FragmentAccountBinding mBinding;
    private ViewPagerFragmentAdapter mViewPagerFragmentAdapter;
    private View mView;

    @Override
    public void show() {
        if (mView == null)return;
        LogUtils.e("accountShow", true);

        UserEntity userEntity = sDataManager.getTradeBean().getUsers().get(sDataManager.USER_ID);
        if (userEntity == null) return;
        AccountEntity accountEntity = userEntity.getAccounts().get("CNY");
        if (accountEntity == null) return;
        mBinding.setAccount(accountEntity);
        mBinding.broker.setText(sDataManager.BROKER_ID);
        ((LazyLoadFragment) mViewPagerFragmentAdapter.getItem(mBinding.vp.getCurrentItem())).show();

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put(AMP_EVENT_PAGE_ID, AMP_EVENT_PAGE_ID_VALUE_ACCOUNT);
            jsonObject.put(AMP_EVENT_SOURCE, sDataManager.SOURCE);
            sDataManager.SOURCE = AMP_EVENT_PAGE_ID_VALUE_ACCOUNT;
            Amplitude.getInstance().logEventWrap(AMP_SHOW_PAGE, jsonObject);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void leave() {
    }

    @Override
    public void refreshMD() {

    }

    @Override
    public void refreshTD() {
        //fragment还没有加载完成
        if (mView == null)return;
        UserEntity userEntity = sDataManager.getTradeBean().getUsers().get(sDataManager.USER_ID);
        if (userEntity == null) return;
        AccountEntity accountEntity = userEntity.getAccounts().get("CNY");
        if (accountEntity == null) return;
        mBinding.setAccount(accountEntity);
        mBinding.broker.setText(sDataManager.BROKER_ID);

        int index = mBinding.vp.getCurrentItem();
        LazyLoadFragment lazyLoadFragment = (LazyLoadFragment) mViewPagerFragmentAdapter.getItem(index);
        lazyLoadFragment.refreshTD();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_account, container, false);
        initData();
        initEvent();
        mView = mBinding.getRoot();
        return mView;
    }

    private void initData() {
        mBinding.tabLayout.setTabTextColors(getResources().getColor(R.color.text_white),
                getResources().getColor(R.color.text_yellow));

        //初始化盘口、持仓、挂单、交易切换容器，fragment实例保存，有生命周期的变化，默认情况下屏幕外初始化两个fragment
        List<Fragment> fragmentList = new ArrayList<>();
        PositionFragment positionFragment = PositionFragment.newInstance(true);
        OrderFragment orderFragmentAlive = OrderFragment.newInstance(true, true);
        OrderFragment orderFragment = OrderFragment.newInstance(false, true);
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
                mBinding.tabLayout.getTabAt(position).select();
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        mBinding.tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                JSONObject jsonObject1 = new JSONObject();
                try {
                    jsonObject1.put(AMP_EVENT_PAGE_ID, AMP_EVENT_PAGE_ID_VALUE_ACCOUNT);
                    jsonObject1.put(AMP_EVENT_SWITCH_FROM, getTabTitle(mBinding.vp.getCurrentItem()));
                    jsonObject1.put(AMP_EVENT_SWITCH_TO, getTabTitle(tab.getPosition()));
                    Amplitude.getInstance().logEventWrap(AMP_SWITCH_TAB, jsonObject1);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
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
                MainActivity mainActivity = (MainActivity) getActivity();
                mainActivity.getmMainActivityPresenter().linkToFutureInfo();
                mBinding.accountFab.hide();
            }
        });

        mBinding.transferIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intentBank = new Intent(getActivity(), BankTransferActivity.class);
                intentBank.putExtra(TRANSFER_DIRECTION, TRANSFER_IN);
                getActivity().startActivityForResult(intentBank, MAIN_ACTIVITY_TO_TRANSFER_ACTIVITY);
            }
        });

        mBinding.transferOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intentBank = new Intent(getActivity(), BankTransferActivity.class);
                intentBank.putExtra(TRANSFER_DIRECTION, TRANSFER_OUT);
                getActivity().startActivityForResult(intentBank, MAIN_ACTIVITY_TO_TRANSFER_ACTIVITY);
            }
        });
    }

    public String getTabTitle(int index){
        switch (index){
            case 0:
                return AMP_EVENT_TAB_POSITION;
            case 1:
                return AMP_EVENT_TAB_ORDER_ALIVE;
            case 2:
                return AMP_EVENT_TAB_ORDER;
            case 3:
                return AMP_EVENT_TAB_TRADE;
            default:
                return "";
        }
    }

    public FragmentAccountBinding getmBinding() {
        return mBinding;
    }

}

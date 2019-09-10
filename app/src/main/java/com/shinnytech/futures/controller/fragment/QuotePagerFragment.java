package com.shinnytech.futures.controller.fragment;

import androidx.databinding.DataBindingUtil;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.shinnytech.futures.R;
import com.shinnytech.futures.amplitude.api.Amplitude;
import com.shinnytech.futures.application.BaseApplication;
import com.shinnytech.futures.controller.activity.MainActivity;
import com.shinnytech.futures.databinding.FragmentQuotePagerBinding;
import com.shinnytech.futures.model.adapter.ViewPagerFragmentAdapter;
import com.shinnytech.futures.model.engine.DataManager;
import com.shinnytech.futures.model.engine.LatestFileManager;
import com.shinnytech.futures.utils.LogUtils;
import com.shinnytech.futures.utils.SPUtils;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import static com.shinnytech.futures.constants.AmpConstants.AMP_EVENT_PAGE_ID;
import static com.shinnytech.futures.constants.AmpConstants.AMP_EVENT_PAGE_ID_VALUE_MAIN;
import static com.shinnytech.futures.constants.AmpConstants.AMP_EVENT_SOURCE;
import static com.shinnytech.futures.constants.AmpConstants.AMP_SHOW_PAGE;
import static com.shinnytech.futures.constants.SettingConstants.CONFIG_RECOMMEND_OPTIONAL;
import static com.shinnytech.futures.constants.CommonConstants.DALIAN;
import static com.shinnytech.futures.constants.CommonConstants.DALIANZUHE;
import static com.shinnytech.futures.constants.CommonConstants.DOMINANT;
import static com.shinnytech.futures.constants.CommonConstants.NENGYUAN;
import static com.shinnytech.futures.constants.CommonConstants.OPTIONAL;
import static com.shinnytech.futures.constants.CommonConstants.SHANGHAI;
import static com.shinnytech.futures.constants.CommonConstants.ZHENGZHOU;
import static com.shinnytech.futures.constants.CommonConstants.ZHENGZHOUZUHE;
import static com.shinnytech.futures.constants.CommonConstants.ZHONGJIN;


public class QuotePagerFragment extends LazyLoadFragment {

    private FragmentQuotePagerBinding mBinding;
    private ViewPagerFragmentAdapter mViewPagerFragmentAdapter;
    private List<String> mTitleList;
    private MainActivity mMainActivity;
    private String mTitle;
    private boolean mIsInit = true;
    private View mView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void show() {
        if (mView == null)return;
        LogUtils.e("mainShow", true);
        try {
            if (!mIsInit) {
                ((QuoteFragment) mViewPagerFragmentAdapter.getItem(mBinding.quotePager.getCurrentItem())).show();
            }
            mIsInit = false;

            JSONObject jsonObject = new JSONObject();
            jsonObject.put(AMP_EVENT_PAGE_ID, AMP_EVENT_PAGE_ID_VALUE_MAIN);
            jsonObject.put(AMP_EVENT_SOURCE, DataManager.getInstance().SOURCE);
            DataManager.getInstance().SOURCE = AMP_EVENT_PAGE_ID_VALUE_MAIN;
            Amplitude.getInstance().logEventWrap(AMP_SHOW_PAGE, jsonObject);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public void leave() {
    }

    @Override
    public void refreshMD() {
        if (mView == null)return;
        int index = mBinding.quotePager.getCurrentItem();
        LazyLoadFragment lazyLoadFragment = (LazyLoadFragment) mViewPagerFragmentAdapter.getItem(index);
        lazyLoadFragment.refreshMD();
    }

    @Override
    public void refreshTD() {
        if (mView == null)return;
        int index = mBinding.quotePager.getCurrentItem();
        LazyLoadFragment lazyLoadFragment = (LazyLoadFragment) mViewPagerFragmentAdapter.getItem(index);
        lazyLoadFragment.refreshTD();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_quote_pager, container, false);
        initData();
        initEvent();
        mView = mBinding.getRoot();
        return mView;
    }

    private void initData() {
        mMainActivity = (MainActivity) getActivity();
        mTitleList = new ArrayList<>();
        mTitleList.add(OPTIONAL);
        mTitleList.add(DOMINANT);
        mTitleList.add(SHANGHAI);
        mTitleList.add(NENGYUAN);
        mTitleList.add(DALIAN);
        mTitleList.add(ZHENGZHOU);
        mTitleList.add(ZHONGJIN);
        mTitleList.add(DALIANZUHE);
        mTitleList.add(ZHENGZHOUZUHE);

        //添加合约页，设置首页是自选合约列表页
        List<Fragment> fragmentList = new ArrayList<>();
        fragmentList.add(QuoteFragment.newInstance(OPTIONAL));
        fragmentList.add(QuoteFragment.newInstance(DOMINANT));
        fragmentList.add(QuoteFragment.newInstance(SHANGHAI));
        fragmentList.add(QuoteFragment.newInstance(NENGYUAN));
        fragmentList.add(QuoteFragment.newInstance(DALIAN));
        fragmentList.add(QuoteFragment.newInstance(ZHENGZHOU));
        fragmentList.add(QuoteFragment.newInstance(ZHONGJIN));
        fragmentList.add(QuoteFragment.newInstance(DALIANZUHE));
        fragmentList.add(QuoteFragment.newInstance(ZHENGZHOUZUHE));
        //初始化适配器类
        mViewPagerFragmentAdapter = new ViewPagerFragmentAdapter(
                getActivity().getSupportFragmentManager(), fragmentList);
        mBinding.quotePager.setAdapter(mViewPagerFragmentAdapter);
        if (!SPUtils.contains(BaseApplication.getContext(), CONFIG_RECOMMEND_OPTIONAL)) {
            mTitle = OPTIONAL;
            mBinding.quotePager.setCurrentItem(0);
        } else if (LatestFileManager.getOptionalInsList().isEmpty()) {
            mTitle = DOMINANT;
            mBinding.quotePager.setCurrentItem(1);
        } else {
            mTitle = OPTIONAL;
            mBinding.quotePager.setCurrentItem(0);
        }
        mMainActivity.getmMainActivityPresenter().getmToolbarTitle().setText(mTitle);
        mMainActivity.getmMainActivityPresenter().switchQuotesNavigation(mTitle);

        mBinding.quotePager.setOffscreenPageLimit(8);
    }

    private void initEvent() {
        mBinding.quotePager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                mTitle = mTitleList.get(position);
                mMainActivity.getmMainActivityPresenter().getmToolbarTitle().setText(mTitle);
                mMainActivity.getmMainActivityPresenter().switchQuotesNavigation(mTitle);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
    }

    public String getmTitle() {
        return mTitle;
    }

    public QuoteFragment getCurrentItem() {
        try {
            if (mBinding.quotePager != null) {
                int index = mBinding.quotePager.getCurrentItem();
                return (QuoteFragment) mViewPagerFragmentAdapter.getItem(index);
            } else return null;
        } catch (Exception e) {
            return null;
        }
    }

    public void setCurrentItem(int index) {
        try {
            if (mBinding.quotePager != null) mBinding.quotePager.setCurrentItem(index, false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}

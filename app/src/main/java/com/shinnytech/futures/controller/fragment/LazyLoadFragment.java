package com.shinnytech.futures.controller.fragment;


import android.os.Bundle;
import androidx.fragment.app.Fragment;

import com.shinnytech.futures.application.BaseApplication;
import com.shinnytech.futures.model.bean.searchinfobean.SearchEntity;
import com.shinnytech.futures.model.engine.LatestFileManager;

/**
 * date: 6/1/18
 * author: chenli
 * description: 懒加载基类
 * version:
 * state:
 */
public abstract class LazyLoadFragment extends Fragment {
    protected boolean mIsViewInitiated;
    protected boolean mIsVisibleToUser;
    protected boolean mIsDataInitiated;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mIsViewInitiated = true;
        prepareFetchData();
    }

    /**
     * 如果是与ViewPager一起使用，调用的是setUserVisibleHint
     *
     * @param isVisibleToUser 是否显示出来了
     */
    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        this.mIsVisibleToUser = isVisibleToUser;
        prepareFetchData();
    }

    /**
     * 如果是通过FragmentTransaction的show和hide的方法来控制显示，调用的是onHiddenChanged.
     * 若是初始就show的Fragment 为了触发该事件 需要先hide再show
     *
     * @param hidden hidden True if the fragment is now hidden, false if it is not
     *               visible.
     */
    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        this.mIsVisibleToUser = !hidden;
        prepareFetchData();
    }

    public abstract void show();

    public abstract void leave();

    public void prepareFetchData() {
        prepareFetchData(true);
    }

    public void prepareFetchData(boolean forceUpdate) {
        if (mIsVisibleToUser && mIsViewInitiated && (!mIsDataInitiated || forceUpdate)) {
            show();
            mIsDataInitiated = true;
        }

        if (!mIsVisibleToUser && mIsViewInitiated && mIsDataInitiated) {
            leave();
        }
    }

    /**
     * date: 2019/1/10
     * author: chenli
     * description: 订阅合约行情
     */
    protected void sendSubscribeQuote(String ins) {
        if (ins.contains("&") && ins.contains(" ")) {
            SearchEntity searchEntity = LatestFileManager.getSearchEntities().get(ins);
            if (searchEntity != null) {
                String leg1_symbol = searchEntity.getLeg1_symbol();
                String leg2_symbol = searchEntity.getLeg2_symbol();
                ins = ins + "," + leg1_symbol + "," + leg2_symbol;
            }
        }
        BaseApplication.getmMDWebSocket().sendSubscribeQuote(ins);
    }

    public abstract void refreshMD();
    public abstract void refreshTD();

}
package com.shinnytech.futures.view.fragment;


import android.os.Bundle;
import android.support.v4.app.Fragment;

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

    public abstract void update();

    public boolean prepareFetchData() {
        return prepareFetchData(true);
    }

    public boolean prepareFetchData(boolean forceUpdate) {
        if (mIsVisibleToUser && mIsViewInitiated && (!mIsDataInitiated || forceUpdate)) {
            update();
            mIsDataInitiated = true;
            return true;
        }
        return false;
    }
}
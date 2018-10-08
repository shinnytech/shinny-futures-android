package com.shinnytech.futures.model.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

/**
 * date: 7/19/17
 * author: chenli
 * description:
 * version:
 * state:
 */
public class ViewPagerFragmentAdapter extends FragmentStatePagerAdapter {

    private List<Fragment> mFragmentList = new ArrayList<>();

    public ViewPagerFragmentAdapter(FragmentManager fm, List<Fragment> fragmentList) {
        super(fm);
        this.mFragmentList.addAll(fragmentList);
    }

    @Override
    public Fragment getItem(int position) {//返回子View对象
        return mFragmentList.get(position);
    }

    @Override
    public int getCount() {//返回子View的个数
        return mFragmentList.size();
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {//初始子View方法
        return super.instantiateItem(container, position);
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {//销毁子View
        super.destroyItem(container, position, object);
    }
}
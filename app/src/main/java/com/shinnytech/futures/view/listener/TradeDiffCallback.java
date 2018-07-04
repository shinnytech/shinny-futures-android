package com.shinnytech.futures.view.listener;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.util.DiffUtil;

import com.shinnytech.futures.model.bean.accountinfobean.TradeEntity;

import java.util.List;

/**
 * Created on 7/27/17.
 * Created by chenli.
 * Description: .
 */

public class TradeDiffCallback extends DiffUtil.Callback {

    private List<TradeEntity> mOldData;
    private List<TradeEntity> mNewData;

    public TradeDiffCallback(List<TradeEntity> oldData, List<TradeEntity> newData) {
        this.mOldData = oldData;
        this.mNewData = newData;
    }

    @Override
    public int getOldListSize() {
        return mOldData == null ? 0 : mOldData.size();
    }

    @Override
    public int getNewListSize() {
        return mNewData == null ? 0 : mNewData.size();
    }

    @Override
    public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
        TradeEntity oldData = mOldData.get(oldItemPosition);
        TradeEntity newData = mNewData.get(newItemPosition);
        if (oldData != null && newData != null)
            return oldData.getKey().equals(newData.getKey());
        return false;
    }

    @Override
    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
        return mOldData.get(oldItemPosition).equals(mNewData.get(newItemPosition));
    }

}

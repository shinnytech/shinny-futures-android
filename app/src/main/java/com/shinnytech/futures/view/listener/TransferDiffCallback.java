package com.shinnytech.futures.view.listener;

import android.support.v7.util.DiffUtil;

import com.shinnytech.futures.model.bean.accountinfobean.TransferEntity;

import java.util.List;

/**
 * Created on 7/27/17.
 * Created by chenli.
 * Description: .
 */

public class TransferDiffCallback extends DiffUtil.Callback {

    private List<TransferEntity> mOldData;
    private List<TransferEntity> mNewData;

    public TransferDiffCallback(List<TransferEntity> oldData, List<TransferEntity> newData) {
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
        TransferEntity oldData = mOldData.get(oldItemPosition);
        TransferEntity newData = mNewData.get(newItemPosition);
        if (oldData != null && newData != null)
            return oldData.getKey().equals(newData.getKey());
        return false;
    }

    @Override
    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
        return mOldData.get(oldItemPosition).equals(mNewData.get(newItemPosition));
    }

}

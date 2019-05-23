package com.shinnytech.futures.model.listener;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.util.DiffUtil;

import com.shinnytech.futures.model.bean.accountinfobean.OrderEntity;
import com.shinnytech.futures.utils.MathUtils;

import java.util.List;

import static com.shinnytech.futures.constants.CommonConstants.STATUS_ALIVE;
import static com.shinnytech.futures.constants.CommonConstants.STATUS_ALIVE_ZN;
import static com.shinnytech.futures.constants.CommonConstants.STATUS_CANCELED_ZN;
import static com.shinnytech.futures.constants.CommonConstants.STATUS_FINISHED_ZN;

/**
 * Created on 7/27/17.
 * Created by chenli.
 * Description: .
 */

public class OrderDiffCallback extends DiffUtil.Callback {

    private List<OrderEntity> mOldData;
    private List<OrderEntity> mNewData;

    public OrderDiffCallback(List<OrderEntity> oldData, List<OrderEntity> newData) {
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
        OrderEntity oldData = mOldData.get(oldItemPosition);
        OrderEntity newData = mNewData.get(newItemPosition);
        if (oldData != null && newData != null)
            return oldData.getKey().equals(newData.getKey());
        return false;
    }

    @Override
    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
        return mOldData.get(oldItemPosition).equals(mNewData.get(newItemPosition));
    }

    @Nullable
    @Override
    public Object getChangePayload(int oldItemPosition, int newItemPosition) {
        Bundle bundle = new Bundle();

        try {
            String volume_origin_old = mOldData.get(oldItemPosition).getVolume_orign();
            String volume_left_old = mOldData.get(oldItemPosition).getVolume_left();
            String status_old;
            if (STATUS_ALIVE.equals(mOldData.get(oldItemPosition).getStatus()))
                status_old = STATUS_ALIVE_ZN;
            else {
                if (Integer.parseInt(volume_left_old) == 0) status_old = STATUS_FINISHED_ZN;
                else status_old = STATUS_CANCELED_ZN;
            }

            String volume_origin_new = mNewData.get(newItemPosition).getVolume_orign();
            String volume_left_new = mNewData.get(newItemPosition).getVolume_left();
            String status_new;
            if (STATUS_ALIVE.equals(mNewData.get(newItemPosition).getStatus()))
                status_new = STATUS_ALIVE_ZN;
            else {
                if (Integer.parseInt(volume_left_new) == 0) status_new = STATUS_FINISHED_ZN;
                else status_new = STATUS_CANCELED_ZN;
            }

            if (!status_old.equals(status_new))
                bundle.putString("status", status_new);

            String volume_trade_old = MathUtils.subtract(volume_origin_old, volume_left_old) + "/" + volume_origin_old;
            String volume_trade_new = MathUtils.subtract(volume_origin_new, volume_left_new) + "/" + volume_origin_new;
            if (!volume_trade_old.equals(volume_trade_new))
                bundle.putString("volume_trade", volume_trade_new);

            if (bundle.size() == 0) {
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return bundle;
    }

}

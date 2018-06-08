package com.xinyi.shinnyfutures.model.listener;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.util.DiffUtil;

import com.xinyi.shinnyfutures.model.bean.futureinfobean.QuoteEntity;

import java.util.List;

/**
 * Created on 7/27/17.
 * Created by chenli.
 * Description: .
 */

public class QuoteDiffCallback extends DiffUtil.Callback {

    private List<QuoteEntity> mOldData;
    private List<QuoteEntity> mNewData;

    public QuoteDiffCallback(List<QuoteEntity> oldData, List<QuoteEntity> newData) {
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
        //newData里的值不会为空
        return mOldData.get(oldItemPosition) != null && mOldData.get(oldItemPosition).getInstrument_id().equals(mNewData.get(newItemPosition).getInstrument_id());
    }

    @Override
    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
        return mOldData.get(oldItemPosition).equals(mNewData.get(newItemPosition));
    }

    @Nullable
    @Override
    public Object getChangePayload(int oldItemPosition, int newItemPosition) {
        Bundle bundle = new Bundle();

        String latest_old = mOldData.get(oldItemPosition).getLast_price();
        String change_old = mOldData.get(oldItemPosition).getChange();
        String change_percent_old = mOldData.get(oldItemPosition).getChange_percent();
        String volume_old = mOldData.get(oldItemPosition).getVolume();
        String open_interest_old = mOldData.get(oldItemPosition).getOpen_interest();

        String latest_new = mNewData.get(newItemPosition).getLast_price();
        String change_new = mNewData.get(newItemPosition).getChange();
        String change_percent_new = mNewData.get(newItemPosition).getChange_percent();
        String volume_new = mNewData.get(newItemPosition).getVolume();
        String open_interest_new = mNewData.get(newItemPosition).getOpen_interest();

        if (latest_old != null && latest_new != null) {
            if (!latest_old.equals(latest_new)) bundle.putString("latest", latest_new);
        } else if (latest_old == null && latest_new != null) {
            bundle.putString("latest", latest_new);
        }

        if (change_old != null && change_new != null) {
            if (!change_old.equals(change_new)) bundle.putString("change", change_new);
        } else if (change_old == null && change_new != null) {
            bundle.putString("change", change_new);
        }

        if (change_percent_old != null && change_percent_new != null) {
            if (!change_percent_old.equals(change_percent_new))
                bundle.putString("change_percent", change_percent_new);
        } else if (change_percent_old == null && change_percent_new != null) {
            bundle.putString("change_percent", change_percent_new);
        }

        if (volume_old != null && volume_new != null) {
            if (!volume_old.equals(volume_new)) bundle.putString("volume", volume_new);
        } else if (volume_old == null && volume_new != null) {
            bundle.putString("volume", volume_new);
        }

        if (open_interest_old != null && open_interest_new != null) {
            if (!open_interest_old.equals(open_interest_new))
                bundle.putString("open_interest", open_interest_new);
        } else if (open_interest_old == null && open_interest_new != null) {
            bundle.putString("open_interest", open_interest_new);
        }


        if (bundle.size() == 0) {
            return null;
        }
        return bundle;
    }
}

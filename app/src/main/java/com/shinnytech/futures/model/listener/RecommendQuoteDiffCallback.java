package com.shinnytech.futures.model.listener;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.util.DiffUtil;

import com.shinnytech.futures.model.bean.futureinfobean.QuoteEntity;
import com.shinnytech.futures.model.engine.LatestFileManager;
import com.shinnytech.futures.utils.MathUtils;

import java.util.List;

import static com.shinnytech.futures.model.engine.LatestFileManager.getUpDownRate;

/**
 * Created on 7/27/17.
 * Created by chenli.
 * Description: .
 */

public class RecommendQuoteDiffCallback extends DiffUtil.Callback {

    private List<QuoteEntity> mOldData;
    private List<QuoteEntity> mNewData;

    public RecommendQuoteDiffCallback(List<QuoteEntity> oldData, List<QuoteEntity> newData) {
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
        return mOldData.get(oldItemPosition) != null
                && mNewData.get(newItemPosition) != null
                && mOldData.get(oldItemPosition).getInstrument_id()
                .equals(mNewData.get(newItemPosition).getInstrument_id());
    }

    @Override
    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
        return mOldData.get(oldItemPosition).equals(mNewData.get(newItemPosition));
    }

    @Nullable
    @Override
    public Object getChangePayload(int oldItemPosition, int newItemPosition) {
        Bundle bundle = new Bundle();
        String instrumentId = mOldData.get(oldItemPosition).getInstrument_id();

        String latest_old = LatestFileManager.saveScaleByPtick(
                mOldData.get(oldItemPosition).getLast_price(), instrumentId);
        String change_percent_old = MathUtils.round(
                getUpDownRate(latest_old, mOldData.get(oldItemPosition).getPre_settlement()), 2);

        String latest_new = LatestFileManager.saveScaleByPtick(
                mNewData.get(newItemPosition).getLast_price(), instrumentId);
        String change_percent_new = MathUtils.round(
                getUpDownRate(latest_new, mNewData.get(newItemPosition).getPre_settlement()), 2);

        String pre_settlement_new = LatestFileManager.saveScaleByPtick(mNewData.get(newItemPosition).getPre_settlement(), instrumentId);
        bundle.putString("pre_settlement", pre_settlement_new);

        if (latest_old != null && latest_new != null) {
            if (!latest_old.equals(latest_new)) bundle.putString("latest", latest_new);
        } else if (latest_old == null && latest_new != null) {
            bundle.putString("latest", latest_new);
        }

        if (change_percent_old != null && change_percent_new != null) {
            if (!change_percent_old.equals(change_percent_new))
                bundle.putString("change_percent", change_percent_new);
        } else if (change_percent_old == null && change_percent_new != null) {
            bundle.putString("change_percent", change_percent_new);
        }

        if (bundle.size() == 0) {
            return null;
        }
        return bundle;
    }
}

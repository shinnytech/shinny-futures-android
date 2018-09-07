package com.shinnytech.futures.view.listener;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.util.DiffUtil;

import com.shinnytech.futures.model.bean.futureinfobean.QuoteEntity;
import com.shinnytech.futures.model.engine.LatestFileManager;
import com.shinnytech.futures.utils.MathUtils;

import java.util.List;

import static com.shinnytech.futures.model.engine.LatestFileManager.getUpDown;
import static com.shinnytech.futures.model.engine.LatestFileManager.getUpDownRate;

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
        String change_old = LatestFileManager.saveScaleByPtick(
                getUpDown(latest_old, mOldData.get(oldItemPosition).getPre_settlement()), instrumentId);
        String change_percent_old = MathUtils.round(
                getUpDownRate(latest_old, mOldData.get(oldItemPosition).getPre_settlement()), 2);
        String volume_old = mOldData.get(oldItemPosition).getVolume();
        String open_interest_old = mOldData.get(oldItemPosition).getOpen_interest();

        String upper_limit_old = LatestFileManager.saveScaleByPtick(
                mOldData.get(oldItemPosition).getUpper_limit(), instrumentId);
        String lower_limit_old = LatestFileManager.saveScaleByPtick(
                mOldData.get(oldItemPosition).getLower_limit(), instrumentId);
        String ask_price1_old = LatestFileManager.saveScaleByPtick(
                mOldData.get(oldItemPosition).getAsk_price1(), instrumentId);
        String ask_volume1_old = mOldData.get(oldItemPosition).getAsk_volume1();
        String bid_price1_old = LatestFileManager.saveScaleByPtick(
                mOldData.get(oldItemPosition).getBid_price1(), instrumentId);
        String bid_volume1_old = mOldData.get(oldItemPosition).getBid_volume1();

        String latest_new = LatestFileManager.saveScaleByPtick(
                mNewData.get(newItemPosition).getLast_price(), instrumentId);
        String change_new = LatestFileManager.saveScaleByPtick(
                getUpDown(latest_new, mNewData.get(newItemPosition).getPre_settlement()), instrumentId);
        String change_percent_new = MathUtils.round(
                getUpDownRate(latest_new, mNewData.get(newItemPosition).getPre_settlement()), 2);
        String volume_new = mNewData.get(newItemPosition).getVolume();
        String open_interest_new = mNewData.get(newItemPosition).getOpen_interest();

        String upper_limit_new = LatestFileManager.saveScaleByPtick(
                mNewData.get(newItemPosition).getUpper_limit(), instrumentId);
        String lower_limit_new = LatestFileManager.saveScaleByPtick(
                mNewData.get(newItemPosition).getLower_limit(), instrumentId);
        String ask_price1_new = LatestFileManager.saveScaleByPtick(
                mNewData.get(newItemPosition).getAsk_price1(), instrumentId);
        String ask_volume1_new = mNewData.get(newItemPosition).getAsk_volume1();
        String bid_price1_new = LatestFileManager.saveScaleByPtick(
                mNewData.get(newItemPosition).getBid_price1(), instrumentId);
        String bid_volume1_new = mNewData.get(newItemPosition).getBid_volume1();

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

        if (upper_limit_old != null && upper_limit_new != null) {
            if (!upper_limit_old.equals(upper_limit_new))
                bundle.putString("upper_limit", upper_limit_new);
        } else if (upper_limit_old == null && upper_limit_new != null) {
            bundle.putString("upper_limit", upper_limit_new);
        }

        if (lower_limit_old != null && lower_limit_new != null) {
            if (!lower_limit_old.equals(lower_limit_new))
                bundle.putString("lower_limit", lower_limit_new);
        } else if (lower_limit_old == null && lower_limit_new != null) {
            bundle.putString("lower_limit", lower_limit_new);
        }

        if (ask_price1_old != null && ask_price1_new != null) {
            if (!ask_price1_old.equals(ask_price1_new))
                bundle.putString("ask_price1", ask_price1_new);
        } else if (ask_price1_old == null && ask_price1_new != null) {
            bundle.putString("ask_price1", ask_price1_new);
        }

        if (ask_volume1_old != null && ask_volume1_new != null) {
            if (!ask_volume1_old.equals(ask_volume1_new))
                bundle.putString("ask_volume1", ask_volume1_new);
        } else if (ask_volume1_old == null && ask_volume1_new != null) {
            bundle.putString("ask_volume1", ask_volume1_new);
        }

        if (bid_price1_old != null && bid_price1_new != null) {
            if (!bid_price1_old.equals(bid_price1_new))
                bundle.putString("bid_price1", bid_price1_new);
        } else if (bid_price1_old == null && bid_price1_new != null) {
            bundle.putString("bid_price1", bid_price1_new);
        }

        if (bid_volume1_old != null && bid_volume1_new != null) {
            if (!bid_volume1_old.equals(bid_volume1_new))
                bundle.putString("bid_volume1", bid_volume1_new);
        } else if (bid_volume1_old == null && bid_volume1_new != null) {
            bundle.putString("bid_volume1", bid_volume1_new);
        }


        if (bundle.size() == 0) {
            return null;
        }
        return bundle;
    }
}

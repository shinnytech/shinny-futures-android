package com.xinyi.shinnyfutures.model.listener;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.util.DiffUtil;

import com.xinyi.shinnyfutures.model.bean.accountinfobean.TradeEntity;

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

    @Nullable
    @Override
    public Object getChangePayload(int oldItemPosition, int newItemPosition) {
        Bundle bundle = new Bundle();

        String instrument_id_old = mOldData.get(oldItemPosition).getInstrument_id();
        String direction_old = mOldData.get(oldItemPosition).getDirection();
        String offset_flag_old = mOldData.get(oldItemPosition).getOffset();
        String price_old = mOldData.get(oldItemPosition).getPrice();
        String volume_old = mOldData.get(oldItemPosition).getVolume();
        String trade_time_old = mOldData.get(oldItemPosition).getTrade_date_time();

        String instrument_id_new = mNewData.get(newItemPosition).getInstrument_id();
        String direction_new = mNewData.get(newItemPosition).getDirection();
        String offset_flag_new = mNewData.get(newItemPosition).getOffset();
        String price_new = mNewData.get(newItemPosition).getPrice();
        String volume_new = mNewData.get(newItemPosition).getVolume();
        String trade_time_new = mNewData.get(newItemPosition).getTrade_date_time();

        if (!instrument_id_old.equals(instrument_id_new))
            bundle.putString("instrument_id", instrument_id_new);

        if (!direction_old.equals(direction_new))
            bundle.putString("direction", direction_new);

        if (!offset_flag_old.equals(offset_flag_new))
            bundle.putString("offset_flag", offset_flag_new);

        if (!price_old.equals(price_new))
            bundle.putString("price", price_new);

        if (!volume_old.equals(volume_new))
            bundle.putString("volume", volume_new);

        if (!trade_time_old.equals(trade_time_new))
            bundle.putString("trade_time", trade_time_new);

        if (bundle.size() == 0) {
            return null;
        }
        return bundle;
    }

}

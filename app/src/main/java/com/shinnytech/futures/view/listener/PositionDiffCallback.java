package com.shinnytech.futures.view.listener;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.util.DiffUtil;

import com.shinnytech.futures.model.bean.accountinfobean.PositionEntity;
import com.shinnytech.futures.model.bean.searchinfobean.SearchEntity;
import com.shinnytech.futures.model.engine.LatestFileManager;
import com.shinnytech.futures.utils.MathUtils;

import java.util.List;

/**
 * Created on 7/27/17.
 * Created by chenli.
 * Description: .
 */

public class PositionDiffCallback extends DiffUtil.Callback {

    private List<PositionEntity> mOldData;
    private List<PositionEntity> mNewData;

    public PositionDiffCallback(List<PositionEntity> oldData,
                                List<PositionEntity> newData) {
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
        PositionEntity oldData = mOldData.get(oldItemPosition);
        PositionEntity newData = mNewData.get(newItemPosition);
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
        String instrument_id = mOldData.get(oldItemPosition).getInstrument_id();
        SearchEntity searchEntity = LatestFileManager.getSearchEntities().get(instrument_id);
        String vm = searchEntity != null ? searchEntity.getVm() : "1";
        int available_long = 0;
        int volume_long = 0;
        String open_price_long = "0.0";
        String float_profit_long = "0.0";
        int available_short = 0;
        int volume_short = 0;
        String open_price_short = "0.0";
        String float_profit_short = "0.0";

        String volume_long_his_old = mOldData.get(oldItemPosition).getVolume_long_his();
        String volume_long_today_old = mOldData.get(oldItemPosition).getVolume_long_today();
        String volume_long_frozen_old = mOldData.get(oldItemPosition).getVolume_long_frozen();
        String open_cost_long_old = mOldData.get(oldItemPosition).getOpen_cost_long();
        String open_price_long_old = mOldData.get(oldItemPosition).getOpen_price_long();
        String float_profit_long_old = mOldData.get(oldItemPosition).getFloat_profit_long();

        String volume_short_his_old = mOldData.get(oldItemPosition).getVolume_short_his();
        String volume_short_today_old = mOldData.get(oldItemPosition).getVolume_short_today();
        String volume_short_frozen_old = mOldData.get(oldItemPosition).getVolume_short_frozen();
        String open_cost_short_old = mOldData.get(oldItemPosition).getOpen_cost_short();
        String open_price_short_old = mOldData.get(oldItemPosition).getOpen_price_short();
        String float_profit_short_old = mOldData.get(oldItemPosition).getFloat_profit_short();

        String volume_long_his_new = mNewData.get(newItemPosition).getVolume_long_his();
        String volume_long_today_new = mNewData.get(newItemPosition).getVolume_long_today();
        String volume_long_frozen_new = mNewData.get(newItemPosition).getVolume_long_frozen();
        String open_cost_long_new = mNewData.get(newItemPosition).getOpen_cost_long();
        String open_price_long_new = mNewData.get(newItemPosition).getOpen_price_long();
        String float_profit_long_new = mNewData.get(newItemPosition).getFloat_profit_long();

        String volume_short_his_new = mNewData.get(newItemPosition).getVolume_short_his();
        String volume_short_today_new = mNewData.get(newItemPosition).getVolume_short_today();
        String volume_short_frozen_new = mNewData.get(newItemPosition).getVolume_short_frozen();
        String open_cost_short_new = mNewData.get(newItemPosition).getOpen_cost_short();
        String open_price_short_new = mNewData.get(newItemPosition).getOpen_price_short();
        String float_profit_short_new = mNewData.get(newItemPosition).getFloat_profit_short();

        try {
            String available_long_old = MathUtils.add(volume_long_his_old, volume_long_today_old);
            String available_long_new = MathUtils.add(volume_long_his_new, volume_long_today_new);
            String available_short_old = MathUtils.add(volume_short_his_old, volume_short_today_old);
            String available_short_new = MathUtils.add(volume_short_his_new, volume_short_today_new);
            if (!available_long_old.equals(available_long_new) || !available_short_old.equals(available_short_new)) {
                available_long = Integer.parseInt(available_long_new);
                available_short = Integer.parseInt(available_short_new);
            } else {
                available_long = Integer.parseInt(available_long_old);
                available_short = Integer.parseInt(available_short_old);
            }

            String volume_long_old = MathUtils.add(available_long_old, volume_long_frozen_old);
            String volume_long_new = MathUtils.add(available_long_new, volume_long_frozen_new);
            String volume_short_old = MathUtils.add(available_short_old, volume_short_frozen_old);
            String volume_short_new = MathUtils.add(available_short_new, volume_short_frozen_new);
            if (!volume_long_old.equals(volume_long_new) || !volume_short_old.equals(volume_short_new)) {
                volume_long = Integer.parseInt(volume_long_new);
                volume_short = Integer.parseInt(volume_short_new);
            } else {
                volume_long = Integer.parseInt(volume_long_old);
                volume_short = Integer.parseInt(volume_short_old);
            }

            if (!open_cost_long_old.equals(open_cost_long_new) || !open_cost_short_old.equals(open_cost_short_new)
                    || !open_price_long_old.equals(open_price_long_new) || !open_price_short_old.equals(open_price_short_new)) {
                open_price_long = LatestFileManager.saveScaleByPtickA(getPrice(open_cost_long_new, open_price_long_new, vm, volume_long), instrument_id);
                open_price_short = LatestFileManager.saveScaleByPtickA(getPrice(open_cost_short_new, open_price_short_new, vm, volume_short), instrument_id);
            } else {
                open_price_long = LatestFileManager.saveScaleByPtickA(getPrice(open_cost_long_old, open_price_long_old, vm, volume_long), instrument_id);
                open_price_short = LatestFileManager.saveScaleByPtickA(getPrice(open_cost_short_old, open_price_short_old, vm, volume_short), instrument_id);
            }

            if (!float_profit_long_old.equals(float_profit_long_new) || !float_profit_short_old.equals(float_profit_short_new)) {
                float_profit_long = MathUtils.round(float_profit_long_new, 0);
                float_profit_short = MathUtils.round(float_profit_short_new, 0);
            } else {
                float_profit_long = MathUtils.round(float_profit_long_old, 0);
                float_profit_short = MathUtils.round(float_profit_short_old, 0);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        if (volume_long != 0 && volume_short != 0) {
            bundle.putString("direction", "双向");
            bundle.putString("volume", volume_long + "/" + volume_short);
            bundle.putString("available", available_long + "/" + available_short);
            bundle.putString("open_price", open_price_long + "/" + open_price_short);
            bundle.putString("float_profit", float_profit_long + "/" + float_profit_short);
        } else if (volume_long == 0 && volume_short != 0) {
            bundle.putString("direction", "空");
            bundle.putString("volume", volume_short + "");
            bundle.putString("available", available_short + "");
            bundle.putString("open_price", open_price_short + "");
            bundle.putString("float_profit", float_profit_short + "");
        } else if (volume_long != 0 && volume_short == 0) {
            bundle.putString("direction", "多");
            bundle.putString("volume", volume_long + "");
            bundle.putString("available", available_long + "");
            bundle.putString("open_price", open_price_long + "");
            bundle.putString("float_profit", float_profit_long + "");
        }

        if (bundle.size() == 0) {
            return null;
        }
        return bundle;
    }

    private String getPrice(String open_cost, String open_price, String vm, int volume) {
        try {
            int vmI = Integer.parseInt(vm);
            float openCost = Float.parseFloat(open_cost);
            float openPrice = Float.parseFloat(open_price);
            if (openPrice != 0) return openPrice + "";
            else if (openCost != 0 && volume != 0) {
                return openCost / (volume * vmI) + "";
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "0.0";
    }
}

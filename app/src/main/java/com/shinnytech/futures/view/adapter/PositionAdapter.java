package com.shinnytech.futures.view.adapter;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.shinnytech.futures.R;
import com.shinnytech.futures.databinding.ItemFragmentPositionBinding;
import com.shinnytech.futures.model.bean.accountinfobean.PositionEntity;
import com.shinnytech.futures.model.bean.searchinfobean.SearchEntity;
import com.shinnytech.futures.model.engine.LatestFileManager;
import com.shinnytech.futures.utils.LogUtils;
import com.shinnytech.futures.utils.MathUtils;

import java.util.List;

/**
 * date: 7/9/17
 * author: chenli
 * description: 持仓页适配器
 * version:
 * state: done
 */
public class PositionAdapter extends RecyclerView.Adapter<PositionAdapter.ItemViewHolder> {

    private Context sContext;
    private List<com.shinnytech.futures.model.bean.accountinfobean.PositionEntity> mPositionData;

    public PositionAdapter(Context context, List<com.shinnytech.futures.model.bean.accountinfobean.PositionEntity> positionData) {
        this.sContext = context;
        this.mPositionData = positionData;
    }

    public List<com.shinnytech.futures.model.bean.accountinfobean.PositionEntity> getData() {
        return mPositionData;
    }

    public void setData(List<com.shinnytech.futures.model.bean.accountinfobean.PositionEntity> data) {
        this.mPositionData = data;
    }

    @Override
    public ItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        ItemFragmentPositionBinding binding = DataBindingUtil.inflate(LayoutInflater
                .from(sContext), R.layout.item_fragment_position, parent, false);
        ItemViewHolder holder = new ItemViewHolder(binding.getRoot());
        holder.setBinding(binding);
        return holder;
    }

    @Override
    public void onBindViewHolder(ItemViewHolder itemViewHolder, int position) {
        itemViewHolder.update();
    }

    @Override
    public void onBindViewHolder(ItemViewHolder itemViewHolder, int position, List<Object> payloads) {
        if (payloads.isEmpty()) {
            onBindViewHolder(itemViewHolder, position);
        } else {
            Bundle bundle = (Bundle) payloads.get(0);
            itemViewHolder.updatePart(bundle);
        }
    }

    @Override
    public int getItemCount() {

        return mPositionData == null ? 0 : mPositionData.size();
    }

    class ItemViewHolder extends RecyclerView.ViewHolder {

        private ItemFragmentPositionBinding mBinding;

        public ItemViewHolder(View itemView) {
            super(itemView);
        }

        public ItemFragmentPositionBinding getBinding() {
            return this.mBinding;
        }

        public void setBinding(ItemFragmentPositionBinding binding) {
            this.mBinding = binding;
        }

        public void update() {
            if (mPositionData != null && mPositionData.size() != 0) {
                PositionEntity positionEntity = mPositionData.get(getLayoutPosition());
                if (positionEntity == null) return;
                try {
                    String instrument_id = positionEntity.getExchange_id() + "." + positionEntity.getInstrument_id();
                    LogUtils.e(instrument_id, true);
                    SearchEntity searchEntity = LatestFileManager.getSearchEntities().get(instrument_id);
                    mBinding.positionName.setText(searchEntity == null ? instrument_id : searchEntity.getInstrumentName());

                    String available_long = MathUtils.subtract(positionEntity.getVolume_long(),
                            MathUtils.add(positionEntity.getVolume_long_frozen_his(), positionEntity.getVolume_long_frozen_today()));
                    int volume_long = Integer.parseInt(positionEntity.getVolume_long());
                    String available_short = MathUtils.subtract(positionEntity.getVolume_short(),
                            MathUtils.add(positionEntity.getVolume_short_frozen_his(), positionEntity.getVolume_short_frozen_today()));
                    int volume_short = Integer.parseInt(positionEntity.getVolume_short());
                    float profit = 0;
                    if (volume_long != 0 && volume_short == 0) {
                        mBinding.positionDirection.setText("多");
                        mBinding.positionDirection.setTextColor(ContextCompat.getColor(sContext, R.color.text_red));
                        mBinding.positionAvailable.setText(available_long);
                        mBinding.positionVolume.setText(volume_long + "");
                        mBinding.positionOpenPrice.setText(LatestFileManager.saveScaleByPtickA(positionEntity.getOpen_price_long(), instrument_id));
                        mBinding.positionProfit.setText(MathUtils.round(positionEntity.getFloat_profit_long(), 2));
                        profit = Float.valueOf(positionEntity.getFloat_profit_long());
                    } else if (volume_long == 0 && volume_short != 0) {
                        mBinding.positionDirection.setText("空");
                        mBinding.positionDirection.setTextColor(ContextCompat.getColor(sContext, R.color.text_green));
                        mBinding.positionAvailable.setText(available_short);
                        mBinding.positionVolume.setText(volume_short + "");
                        mBinding.positionOpenPrice.setText(LatestFileManager.saveScaleByPtickA(positionEntity.getOpen_price_short(), instrument_id));
                        mBinding.positionProfit.setText(MathUtils.round(positionEntity.getFloat_profit_short(), 2));
                        profit = Float.valueOf(positionEntity.getFloat_profit_short());
                    } else if (volume_long != 0 && volume_short != 0) {
                        mBinding.positionDirection.setText("双向");
                        mBinding.positionDirection.setTextColor(ContextCompat.getColor(sContext, R.color.white));
                        mBinding.positionAvailable.setText(available_long + "/" + available_short);
                        mBinding.positionVolume.setText(available_long + "/" + volume_short);
                        String price_long = LatestFileManager.saveScaleByPtickA(positionEntity.getOpen_price_long(), instrument_id);
                        String price_short = LatestFileManager.saveScaleByPtickA(positionEntity.getOpen_price_short(), instrument_id);
                        mBinding.positionOpenPrice.setText(price_long + "/" + price_short);
                        mBinding.positionProfit.setText(MathUtils.round(positionEntity.getFloat_profit_long(), 2)
                                + "/" + MathUtils.round(positionEntity.getFloat_profit_short(), 2));
                        profit = Float.valueOf(positionEntity.getFloat_profit_long());
                    }
                    if (profit < 0)
                        mBinding.positionProfit.setTextColor(ContextCompat.getColor(sContext, R.color.text_green));
                    else if (profit > 0)
                        mBinding.positionProfit.setTextColor(ContextCompat.getColor(sContext, R.color.text_red));
                    else
                        mBinding.positionProfit.setTextColor(ContextCompat.getColor(sContext, R.color.white));

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        private void updatePart(Bundle bundle) {
            for (String key :
                    bundle.keySet()) {
                String value = bundle.getString(key);
                switch (key) {
                    case "direction":
                        if ("多".equals(value)) {
                            mBinding.positionDirection.setTextColor(ContextCompat.getColor(sContext, R.color.text_red));
                        } else if ("空".equals(value)) {
                            mBinding.positionDirection.setTextColor(ContextCompat.getColor(sContext, R.color.text_green));
                        } else if ("双向".equals(value)) {
                            mBinding.positionDirection.setTextColor(ContextCompat.getColor(sContext, R.color.white));
                        }
                        mBinding.positionDirection.setText(value);
                        break;
                    case "volume":
                        mBinding.positionVolume.setText(value);
                        break;
                    case "available":
                        mBinding.positionAvailable.setText(value);
                        break;
                    case "open_price":
                        mBinding.positionOpenPrice.setText(value);
                        break;
                    case "float_profit":
                        mBinding.positionProfit.setText(value);
                        if (value.contains("/")) {
                            mBinding.positionProfit.setTextColor(ContextCompat.getColor(sContext, R.color.white));
                        } else {
                            try {
                                float profit = Float.valueOf(mBinding.positionProfit.getText().toString());
                                if (profit < 0)
                                    mBinding.positionProfit.setTextColor(ContextCompat.getColor(sContext, R.color.text_green));
                                else if (profit > 0)
                                    mBinding.positionProfit.setTextColor(ContextCompat.getColor(sContext, R.color.text_red));
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        break;
                    default:
                        break;


                }
            }
        }
    }
}

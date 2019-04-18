package com.shinnytech.futures.model.adapter;


import android.content.Context;
import android.databinding.DataBindingUtil;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.shinnytech.futures.R;
import com.shinnytech.futures.databinding.ItemActivityTradeBinding;
import com.shinnytech.futures.model.bean.accountinfobean.TradeEntity;
import com.shinnytech.futures.model.bean.searchinfobean.SearchEntity;
import com.shinnytech.futures.model.engine.DataManager;
import com.shinnytech.futures.model.engine.LatestFileManager;

import java.util.Date;
import java.util.List;

import static com.shinnytech.futures.constants.CommonConstants.DIRECTION_BUY;
import static com.shinnytech.futures.constants.CommonConstants.OFFSET_CLOSE_ZN;
import static com.shinnytech.futures.constants.CommonConstants.OFFSET_CLOSE_FORCE_ZN;
import static com.shinnytech.futures.constants.CommonConstants.OFFSET_CLOSE_HISTORY_ZN;
import static com.shinnytech.futures.constants.CommonConstants.OFFSET_CLOSE_TODAY_ZN;
import static com.shinnytech.futures.constants.CommonConstants.OFFSET_OPEN_ZN;
import static com.shinnytech.futures.constants.CommonConstants.DIRECTION_SELL;
import static com.shinnytech.futures.constants.CommonConstants.OFFSET_CLOSE;
import static com.shinnytech.futures.constants.CommonConstants.OFFSET_CLOSE_FORCE;
import static com.shinnytech.futures.constants.CommonConstants.OFFSET_CLOSE_HISTORY;
import static com.shinnytech.futures.constants.CommonConstants.OFFSET_CLOSE_TODAY;
import static com.shinnytech.futures.constants.CommonConstants.OFFSET_OPEN;

/**
 * date: 7/9/17
 * author: chenli
 * description: 成交记录适配器
 * version:
 * state: done
 */
public class TradeAdapter extends RecyclerView.Adapter<TradeAdapter.ItemViewHolder> {

    private Context sContext;
    private List<TradeEntity> mData;

    public TradeAdapter(Context context, List<TradeEntity> data) {
        this.sContext = context;
        this.mData = data;
    }

    public List<TradeEntity> getData() {
        return mData;
    }

    public void setData(List<TradeEntity> data) {
        this.mData = data;
    }

    @Override
    public ItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        ItemActivityTradeBinding binding = DataBindingUtil.inflate(LayoutInflater
                .from(sContext), R.layout.item_activity_trade, parent, false);
        ItemViewHolder holder = new ItemViewHolder(binding.getRoot());
        holder.setBinding(binding);
        return holder;
    }

    @Override
    public void onBindViewHolder(final ItemViewHolder itemViewHolder, int position) {
        itemViewHolder.update();
    }

    @Override
    public int getItemCount() {
        return mData == null ? 0 : mData.size();
    }

    public class ItemViewHolder extends RecyclerView.ViewHolder {

        TradeEntity tradeEntity;

        private ItemActivityTradeBinding mBinding;

        public ItemViewHolder(View itemView) {
            super(itemView);
        }

        public ItemActivityTradeBinding getBinding() {
            return this.mBinding;
        }

        public void setBinding(ItemActivityTradeBinding binding) {
            this.mBinding = binding;
        }

        public void update() {
            if (mData != null && mData.size() != 0) {
                tradeEntity = mData.get(getLayoutPosition());
                if (tradeEntity != null) {
                    String instrument_id = tradeEntity.getExchange_id() + "." + tradeEntity.getInstrument_id();
                    SearchEntity insName = LatestFileManager.getSearchEntities().get(instrument_id);
                    mBinding.tvTradeName.setText(insName == null ? instrument_id : insName.getInstrumentName());
                    switch (tradeEntity.getOffset()) {
                        case OFFSET_OPEN:
                            mBinding.tvTradeOffset.setText(OFFSET_OPEN_ZN);
                            break;
                        case OFFSET_CLOSE_TODAY:
                            mBinding.tvTradeOffset.setText(OFFSET_CLOSE_TODAY_ZN);
                            break;
                        case OFFSET_CLOSE_HISTORY:
                            mBinding.tvTradeOffset.setText(OFFSET_CLOSE_HISTORY_ZN);
                            break;
                        case OFFSET_CLOSE:
                            mBinding.tvTradeOffset.setText(OFFSET_CLOSE_ZN);
                            break;
                        case OFFSET_CLOSE_FORCE:
                            mBinding.tvTradeOffset.setText(OFFSET_CLOSE_FORCE_ZN);
                            break;
                        default:
                            mBinding.tvTradeOffset.setText("");
                            break;
                    }
                    switch (tradeEntity.getDirection()) {
                        case DIRECTION_BUY:
                            mBinding.tvTradeOffset.setTextColor(ContextCompat.getColor(sContext, R.color.text_red));
                            break;
                        case DIRECTION_SELL:
                            mBinding.tvTradeOffset.setTextColor(ContextCompat.getColor(sContext, R.color.text_green));
                            break;
                        default:
                            mBinding.tvTradeOffset.setTextColor(ContextCompat.getColor(sContext, R.color.text_red));
                            break;
                    }
                    mBinding.tvTradePrice.setText(LatestFileManager.saveScaleByPtick(tradeEntity.getPrice(), instrument_id));
                    mBinding.tvTradeVolume.setText(tradeEntity.getVolume());
                    String date = DataManager.getInstance().getSimpleDateFormat().format(new Date(Long.valueOf(tradeEntity.getTrade_date_time()) / 1000000));
                    mBinding.tvTradeTime.setText(date);
                }
            }
        }
    }
}

package com.xinyi.shinnyfutures.view.adapter;


import android.content.Context;
import android.databinding.DataBindingUtil;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.xinyi.shinnyfutures.R;
import com.xinyi.shinnyfutures.databinding.ItemActivityTradeBinding;
import com.xinyi.shinnyfutures.model.bean.accountinfobean.TradeEntity;
import com.xinyi.shinnyfutures.model.bean.searchinfobean.SearchEntity;
import com.xinyi.shinnyfutures.model.engine.LatestFileManager;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

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
    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm:ss", Locale.CHINA);

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
                    String instrument_id = tradeEntity.getInstrument_id();
                    SearchEntity insName = LatestFileManager.getSearchEntities().get(instrument_id);
                    mBinding.tvTradeName.setText(insName == null ? instrument_id : insName.getInstrumentName());
                    switch (tradeEntity.getOffset()) {
                        case "OPEN":
                            mBinding.tvTradeOffset.setText("开仓");
                            break;
                        case "CLOSETODAY":
                            mBinding.tvTradeOffset.setText("平今");
                            break;
                        case "CLOSEHISTORY":
                            mBinding.tvTradeOffset.setText("平昨");
                            break;
                        case "CLOSE":
                            mBinding.tvTradeOffset.setText("平仓");
                            break;
                        case "FORCECLOSE":
                            mBinding.tvTradeOffset.setText("强平");
                            break;
                        default:
                            mBinding.tvTradeOffset.setText("");
                            break;
                    }
                    switch (tradeEntity.getDirection()) {
                        case "BUY":
                            mBinding.tvTradeOffset.setTextColor(ContextCompat.getColor(sContext, R.color.text_red));
                            break;
                        case "SELL":
                            mBinding.tvTradeOffset.setTextColor(ContextCompat.getColor(sContext, R.color.text_green));
                            break;
                        default:
                            mBinding.tvTradeOffset.setTextColor(ContextCompat.getColor(sContext, R.color.text_red));
                            break;
                    }
                    mBinding.tvTradePrice.setText(tradeEntity.getPrice());
                    mBinding.tvTradeVolume.setText(tradeEntity.getVolume());
                    String date = simpleDateFormat.format(new Date(Long.valueOf(tradeEntity.getTrade_date_time()) / 1000000));
                    mBinding.tvTradeTime.setText(date);
                }
            }
        }
    }
}

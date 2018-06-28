package com.xinyi.shinnyfutures.view.adapter;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.xinyi.shinnyfutures.R;
import com.xinyi.shinnyfutures.databinding.ItemActivityNavQuoteBinding;
import com.xinyi.shinnyfutures.model.bean.searchinfobean.SearchEntity;
import com.xinyi.shinnyfutures.model.engine.LatestFileManager;

import java.util.List;

/**
 * date: 7/9/17
 * author: chenli
 * description: 行情页底部合约导航栏适配器
 * version:
 * state: done
 */
public class QuoteNavAdapter extends RecyclerView.Adapter<QuoteNavAdapter.ItemViewHolder> {

    private Context sContext;
    private List<String> mData;

    public QuoteNavAdapter(Context context, List<String> data) {
        this.sContext = context;
        this.mData = data;
    }

    /**
     * 用于更新数据
     *
     * @param data 更新的数据
     */
    public void updateList(List<String> data) {
        this.mData = data;
        notifyDataSetChanged();
    }

    @Override
    public ItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        ItemActivityNavQuoteBinding binding = DataBindingUtil.inflate(LayoutInflater
                .from(sContext), R.layout.item_activity_nav_quote, parent, false);
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

    class ItemViewHolder extends RecyclerView.ViewHolder {

        private ItemActivityNavQuoteBinding mBinding;

        public ItemViewHolder(View itemView) {
            super(itemView);
        }

        public ItemActivityNavQuoteBinding getBinding() {
            return this.mBinding;
        }

        public void setBinding(ItemActivityNavQuoteBinding binding) {
            this.mBinding = binding;
        }

        public void update() {
            if (mData != null && mData.size() != 0) {
                String instrumentId = mData.get(getLayoutPosition());
                if (instrumentId != null && instrumentId.contains("&")) {
                    String instrument_id = instrumentId.split("&")[0].split(" ")[1];
                    SearchEntity searchEntity = LatestFileManager.getSearchEntities().get(instrument_id);
                    if (searchEntity != null)
                        mBinding.quoteNav.setText(searchEntity.getInstrumentName().replaceAll("\\d+", ""));
                    else mBinding.quoteNav.setText(instrument_id.replaceAll("\\d+", ""));
                } else {
                    mBinding.quoteNav.setText(instrumentId);
                }
                itemView.setTag(instrumentId);
            }
        }
    }
}

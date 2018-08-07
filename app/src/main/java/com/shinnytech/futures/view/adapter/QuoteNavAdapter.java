package com.shinnytech.futures.view.adapter;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.shinnytech.futures.R;
import com.shinnytech.futures.databinding.ItemActivityNavQuoteBinding;
import com.shinnytech.futures.model.bean.searchinfobean.SearchEntity;
import com.shinnytech.futures.model.engine.LatestFileManager;
import com.shinnytech.futures.utils.LogUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * date: 7/9/17
 * author: chenli
 * description: 行情页底部合约导航栏适配器
 * version:
 * state: done
 */
public class QuoteNavAdapter extends RecyclerView.Adapter<QuoteNavAdapter.ItemViewHolder> {

    private Context sContext;
    private Map<String, String> mData;

    public QuoteNavAdapter(Context context, Map<String, String> data) {
        this.sContext = context;
        this.mData = data;
    }

    /**
     * 用于更新数据
     *
     * @param data 更新的数据
     */
    public void updateList(Map<String, String> data) {
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
                List<String> keys = new ArrayList<>(mData.keySet());
                List<String> values = new ArrayList<>(mData.values());
                String instrumentName = values.get(getLayoutPosition());
                String instrumentId = keys.get(getLayoutPosition());
                mBinding.quoteNav.setText(instrumentName);
                itemView.setTag(instrumentId);
            }
        }
    }
}

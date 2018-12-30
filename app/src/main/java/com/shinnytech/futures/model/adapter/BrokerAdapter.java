package com.shinnytech.futures.model.adapter;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.shinnytech.futures.R;
import com.shinnytech.futures.databinding.ItemActivityBrokerBinding;
import com.shinnytech.futures.databinding.ItemActivitySearchQuoteBinding;
import com.shinnytech.futures.model.bean.searchinfobean.SearchEntity;
import com.shinnytech.futures.model.engine.LatestFileManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 * date: 7/9/17
 * author: chenli
 * description: 搜索页列表适配器
 * version:
 * state: done
 */
public class BrokerAdapter extends RecyclerView.Adapter<BrokerAdapter.ItemViewHolder> {
    private Context sContext;
    private String[] mData;

    public BrokerAdapter(Context context, String[] data) {
        this.sContext = context;
        this.mData = data;
    }

    public String[] getData() {
        return mData;
    }

    @Override
    public ItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        ItemActivityBrokerBinding binding = DataBindingUtil.inflate(LayoutInflater
                .from(sContext), R.layout.item_activity_broker, parent, false);
        ItemViewHolder holder = new ItemViewHolder(binding.getRoot());
        holder.setBinding(binding);
        return holder;
    }

    @Override
    public void onBindViewHolder(ItemViewHolder holder, int position) {
        holder.update();
    }

    @Override
    public int getItemCount() {
        return mData == null ? 0 : mData.length;
    }

    public class ItemViewHolder extends RecyclerView.ViewHolder {

        private ItemActivityBrokerBinding mBinding;

        public ItemViewHolder(View itemView) {
            super(itemView);
        }

        public ItemActivityBrokerBinding getBinding() {
            return this.mBinding;
        }

        public void setBinding(ItemActivityBrokerBinding binding) {
            this.mBinding = binding;
        }

        public void update() {
            if (mData == null || mData.length == 0) return;
            String data = mData[getLayoutPosition()];
            mBinding.broker.setText(data);
        }

    }
}

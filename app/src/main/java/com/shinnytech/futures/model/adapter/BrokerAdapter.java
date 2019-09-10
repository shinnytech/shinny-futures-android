package com.shinnytech.futures.model.adapter;

import android.content.Context;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.shinnytech.futures.R;
import com.shinnytech.futures.databinding.ItemActivityBrokerBinding;

import java.util.List;


/**
 * date: 7/9/17
 * author: chenli
 * description: 选择期货公司列表适配器
 * version:
 * state: done
 */
public class BrokerAdapter extends RecyclerView.Adapter<BrokerAdapter.ItemViewHolder> {
    private Context sContext;
    private List<String> mData;

    public BrokerAdapter(Context context, List<String> data) {
        this.sContext = context;
        this.mData = data;
    }

    public List<String> getData() {
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
        return mData == null ? 0 : mData.size();
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
            if (mData == null || mData.size() == 0) return;
            String data = mData.get(getLayoutPosition());
            mBinding.broker.setText(data);
        }

    }
}

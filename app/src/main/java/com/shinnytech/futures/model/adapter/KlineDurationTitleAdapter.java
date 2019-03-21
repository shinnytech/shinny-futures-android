package com.shinnytech.futures.model.adapter;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.shinnytech.futures.R;
import com.shinnytech.futures.databinding.ItemDurationTitleBinding;

import java.util.ArrayList;
import java.util.List;


/**
 * date: 7/9/17
 * author: chenli
 * description: futureInfoActivity页上toolbar的标题点击弹出框适配器，用于显示自选合约列表
 * version:
 * state: done
 */
public class KlineDurationTitleAdapter extends RecyclerView.Adapter<KlineDurationTitleAdapter.ItemViewHolder> {
    private Context sContext;
    private List<String> mData = new ArrayList<>();
    private int index = 0;

    public KlineDurationTitleAdapter(Context context, List<String> data) {
        this.sContext = context;
        this.mData.addAll(data);
    }

    public void update(int position) {
        index = position;
        notifyDataSetChanged();
    }

    public int next() {
        index = index + 1;
        if (index == mData.size()) index = 0;
        notifyDataSetChanged();
        return index;
    }

    public String getDurationTitle() {
        String duration_title = mData.get(index);
        return duration_title;
    }

    @Override
    public ItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        ItemDurationTitleBinding binding = DataBindingUtil.inflate(LayoutInflater
                .from(sContext), R.layout.item_duration_title, parent, false);
        ItemViewHolder holder = new ItemViewHolder(binding.getRoot());
        holder.setBinding(binding);
        return holder;
    }

    @Override
    public void onBindViewHolder(final ItemViewHolder holder, int position) {
        holder.update();
    }

    @Override
    public int getItemCount() {
        return mData == null ? 0 : mData.size();
    }

    public class ItemViewHolder extends RecyclerView.ViewHolder {

        private ItemDurationTitleBinding mBinding;

        public ItemViewHolder(View itemView) {
            super(itemView);
        }

        public ItemDurationTitleBinding getBinding() {
            return this.mBinding;
        }

        public void setBinding(ItemDurationTitleBinding binding) {
            this.mBinding = binding;
        }

        public void update() {
            if (mData == null || mData.size() == 0) return;
            String duration_title = mData.get(getLayoutPosition());
            mBinding.durationTitle.setText(duration_title);
            if (index == getLayoutPosition()) {
                mBinding.durationTitleUnderline.setBackground(ContextCompat.getDrawable(sContext, R.color.marker_yellow));
                mBinding.durationTitle.setTextColor(ContextCompat.getColor(sContext, R.color.marker_yellow));
            } else {
                mBinding.durationTitleUnderline.setBackground(ContextCompat.getDrawable(sContext, R.color.black_light));
                mBinding.durationTitle.setTextColor(ContextCompat.getColor(sContext, R.color.white));
            }
        }
    }
}

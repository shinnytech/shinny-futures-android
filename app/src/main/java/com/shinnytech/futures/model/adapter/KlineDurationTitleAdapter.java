package com.shinnytech.futures.model.adapter;

import android.content.Context;
import androidx.databinding.DataBindingUtil;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.shinnytech.futures.R;
import com.shinnytech.futures.application.BaseApplication;
import com.shinnytech.futures.constants.SettingConstants;
import com.shinnytech.futures.databinding.ItemDurationTitleBinding;
import com.shinnytech.futures.utils.SPUtils;

import java.util.ArrayList;
import java.util.List;


/**
 * date: 7/9/17
 * author: chenli
 * description: 添加常用适配器
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

    public void update() {
        String durationPre = TextUtils.join(",", mData);
        String duration = (String) SPUtils.get(BaseApplication.getContext(),
                SettingConstants.CONFIG_KLINE_DURATION_DEFAULT, "");
        if (!duration.equals(durationPre)) {
            mData.clear();
            String[] durations = duration.split(",");
            mData.add(SettingConstants.KLINE_DURATION_DAY);
            for (String data : durations) {
                mData.add(data);
            }
            notifyDataSetChanged();
        }
    }

    public boolean isCurrentIndex(int position){
        return position == index;
    }

    public void update(int position) {
        if (position >= 0 && position < getItemCount()) {
            index = position;
            notifyDataSetChanged();
        }
    }

    public int next() {
        index = index + 1;
        if (index == getItemCount()) index = 0;
        notifyDataSetChanged();
        return index;
    }

    public String getDurationTitle() {
        if (index >= 0 && index < getItemCount()) return mData.get(index);
        else return null;
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
                mBinding.durationTitleUnderline.setBackground(ContextCompat.getDrawable(sContext, R.color.text_yellow));
                mBinding.durationTitle.setTextColor(ContextCompat.getColor(sContext, R.color.text_yellow));
            } else {
                mBinding.durationTitleUnderline.setBackground(ContextCompat.getDrawable(sContext, R.color.future_info_toolbar));
                mBinding.durationTitle.setTextColor(ContextCompat.getColor(sContext, R.color.text_white));
            }
        }
    }
}

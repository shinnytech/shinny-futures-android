package com.shinnytech.futures.model.adapter;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.shinnytech.futures.R;
import com.shinnytech.futures.databinding.ItemDialogKlineBinding;
import com.shinnytech.futures.databinding.ItemDialogOptionalBinding;
import com.shinnytech.futures.model.bean.searchinfobean.SearchEntity;
import com.shinnytech.futures.model.engine.LatestFileManager;

import java.util.ArrayList;
import java.util.List;


/**
 * date: 7/9/17
 * author: chenli
 * description: futureInfoActivity页上toolbar的标题点击弹出框适配器，用于显示自选合约列表
 * version:
 * state: done
 */
public class DialogKlineAdapter extends RecyclerView.Adapter<DialogKlineAdapter.ItemViewHolder> {
    private Context sContext;
    private String[] mData;

    public DialogKlineAdapter(Context context, String[] data) {
        this.sContext = context;
        this.mData = data;
    }

    @Override
    public ItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        ItemDialogKlineBinding binding = DataBindingUtil.inflate(LayoutInflater
                .from(sContext), R.layout.item_dialog_kline, parent, false);
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
        return mData == null ? 0 : mData.length;
    }

    public class ItemViewHolder extends RecyclerView.ViewHolder {
        private ItemDialogKlineBinding mBinding;

        public ItemViewHolder(View itemView) {
            super(itemView);
        }

        public ItemDialogKlineBinding getBinding() {
            return this.mBinding;
        }

        public void setBinding(ItemDialogKlineBinding binding) {
            this.mBinding = binding;
        }

        public void update() {
            if (mData == null || mData.length == 0) return;
            int index = getLayoutPosition();
            String duration = mData[index];
            mBinding.tvIdDialog.setText(duration);
            itemView.setTag(index);
        }

    }
}

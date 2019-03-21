package com.shinnytech.futures.model.adapter;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.shinnytech.futures.R;
import com.shinnytech.futures.databinding.ItemActivityParaContentBinding;


/**
 * date: 7/9/17
 * author: chenli
 * description: 搜索页列表适配器
 * version:
 * state: done
 */
public class ParaContentAdapter extends RecyclerView.Adapter<ParaContentAdapter.ItemViewHolder> {
    private Context sContext;
    private String[] mData;

    public ParaContentAdapter(Context context, String[] data) {
        this.sContext = context;
        this.mData = data;
    }

    public String[] getData() {
        return mData;
    }

    public void setData(String[] data) {
        mData = data;
        notifyDataSetChanged();
    }

    @Override
    public ItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final ItemActivityParaContentBinding binding = DataBindingUtil.inflate(LayoutInflater
                .from(sContext), R.layout.item_activity_para_content, parent, false);
        final ItemViewHolder holder = new ItemViewHolder(binding.getRoot());
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

        public ItemActivityParaContentBinding mBinding;

        public ItemViewHolder(View itemView) {
            super(itemView);
        }

        public ItemActivityParaContentBinding getBinding() {
            return this.mBinding;
        }

        public void setBinding(ItemActivityParaContentBinding binding) {
            this.mBinding = binding;
        }

        public void update() {
            if (mData == null || mData.length == 0) return;
            String data = mData[getLayoutPosition()];
            mBinding.tvKey.setText("参数N" + (getLayoutPosition() + 1));
            mBinding.edValue.setText(data);
        }

    }
}

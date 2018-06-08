package com.xinyi.shinnyfutures.view.adapter;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.xinyi.shinnyfutures.R;
import com.xinyi.shinnyfutures.databinding.ItemDialogOptionalBinding;
import com.xinyi.shinnyfutures.model.bean.searchinfobean.SearchEntity;
import com.xinyi.shinnyfutures.utils.LatestFileUtils;

import java.util.ArrayList;
import java.util.List;


/**
 * date: 7/9/17
 * author: chenli
 * description: futureInfoActivity页上toolbar的标题点击弹出框适配器，用于显示自选合约列表
 * version:
 * state: done
 */
public class DialogAdapter extends RecyclerView.Adapter<DialogAdapter.ItemViewHolder> {
    private Context sContext;
    private List<String> mData = new ArrayList<>();

    public DialogAdapter(Context context, List<String> data) {
        this.sContext = context;
        this.mData.addAll(data);
    }

    public void updateList(List<String> data) {
        this.mData.clear();
        this.mData.addAll(data);
        notifyDataSetChanged();
    }

    @Override
    public ItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        ItemDialogOptionalBinding binding = DataBindingUtil.inflate(LayoutInflater
                .from(sContext), R.layout.item_dialog_optional, parent, false);
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
        String instrumentId;

        private ItemDialogOptionalBinding mBinding;

        public ItemViewHolder(View itemView) {
            super(itemView);
        }

        public ItemDialogOptionalBinding getBinding() {
            return this.mBinding;
        }

        public void setBinding(ItemDialogOptionalBinding binding) {
            this.mBinding = binding;
        }

        public void update() {
            if (mData != null && mData.size() != 0) {
                instrumentId = mData.get(getLayoutPosition());
                if (!instrumentId.isEmpty()) {
                    SearchEntity insName = LatestFileUtils.getSearchEntities().get(instrumentId);
                    if (insName != null) mBinding.tvIdDialog.setText(insName.getInstrumentName());
                    else mBinding.tvIdDialog.setText(instrumentId);
                    itemView.setTag(instrumentId);
                }
            }
        }

    }
}

package com.shinnytech.futures.view.adapter;


import android.content.Context;
import android.databinding.DataBindingUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.shinnytech.futures.R;
import com.shinnytech.futures.databinding.ItemActivityBankTransferBinding;
import com.shinnytech.futures.model.bean.accountinfobean.TransferEntity;
import com.shinnytech.futures.model.engine.DataManager;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

/**
 * date: 7/9/17
 * author: chenli
 * description: 成交记录适配器
 * version:
 * state: done
 */
public class BankTransferAdapter extends RecyclerView.Adapter<BankTransferAdapter.ItemViewHolder> {

    private Context sContext;
    private List<TransferEntity> mData;

    public BankTransferAdapter(Context context, List<TransferEntity> data) {
        this.sContext = context;
        this.mData = data;
    }

    public List<TransferEntity> getData() {
        return mData;
    }

    public void setData(List<TransferEntity> data) {
        this.mData = data;
    }

    @Override
    public ItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        ItemActivityBankTransferBinding binding = DataBindingUtil.inflate(LayoutInflater
                .from(sContext), R.layout.item_activity_bank_transfer, parent, false);
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

        TransferEntity transferEntity;

        private ItemActivityBankTransferBinding mBinding;

        public ItemViewHolder(View itemView) {
            super(itemView);
        }

        public ItemActivityBankTransferBinding getBinding() {
            return this.mBinding;
        }

        public void setBinding(ItemActivityBankTransferBinding binding) {
            this.mBinding = binding;
        }

        public void update() {
            if (mData != null && mData.size() != 0) {
                transferEntity = mData.get(getLayoutPosition());
                if (transferEntity == null) return;
                String date = DataManager.getInstance().getSimpleDateFormat().format(new Date(Long.valueOf(transferEntity.getDatetime()) / 1000000));
                mBinding.datetime.setText(date);
                mBinding.amount.setText(transferEntity.getAmount());
                mBinding.currency.setText(transferEntity.getCurrency());
                mBinding.tradeResult.setText(transferEntity.getError_msg());
            }
        }
    }
}

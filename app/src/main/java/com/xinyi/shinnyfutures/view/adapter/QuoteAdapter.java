package com.xinyi.shinnyfutures.view.adapter;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.xinyi.shinnyfutures.R;
import com.xinyi.shinnyfutures.databinding.ItemFragmentQuoteBinding;
import com.xinyi.shinnyfutures.model.bean.futureinfobean.QuoteEntity;

import java.util.List;


/**
 * date: 7/9/17
 * author: chenli
 * description: 行情页适配器
 * version:
 * state: done
 */
public class QuoteAdapter extends RecyclerView.Adapter<QuoteAdapter.ItemViewHolder> {

    private Context sContext;
    private List<QuoteEntity> mData;
    private boolean mSwitchChange = false;
    private boolean mSwitchVolume = false;

    public QuoteAdapter(Context context, List<QuoteEntity> data) {
        this.sContext = context;
        this.mData = data;
    }

    public List<QuoteEntity> getData() {
        return mData;
    }

    public void setData(List<QuoteEntity> data) {
        this.mData = data;
    }

    /**
     * date: 7/9/17
     * author: chenli
     * description: 涨跌/涨跌幅切换
     */
    public void switchChangeView() {
        mSwitchChange = !mSwitchChange;
        notifyDataSetChanged();
    }

    /**
     * date: 7/9/17
     * author: chenli
     * description: 持仓量/成交量切换
     */
    public void switchVolView() {
        mSwitchVolume = !mSwitchVolume;
        notifyDataSetChanged();
    }

    @Override
    public ItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        ItemFragmentQuoteBinding binding = DataBindingUtil.inflate(LayoutInflater
                .from(sContext), R.layout.item_fragment_quote, parent, false);
        ItemViewHolder holder = new ItemViewHolder(binding.getRoot());
        holder.setBinding(binding);
        return holder;
    }

    @Override
    public void onBindViewHolder(final ItemViewHolder itemViewHolder, int position) {
        itemViewHolder.update();
    }

    @Override
    public void onBindViewHolder(ItemViewHolder itemViewHolder, int position, List<Object> payloads) {
        if (payloads.isEmpty()) {
            onBindViewHolder(itemViewHolder, position);
        } else {
            Bundle bundle = (Bundle) payloads.get(0);
            itemViewHolder.updatePart(bundle);
        }
    }

    @Override
    public int getItemCount() {
        return mData == null ? 0 : mData.size();
    }

    class ItemViewHolder extends RecyclerView.ViewHolder {

        private ItemFragmentQuoteBinding mBinding;

        public ItemViewHolder(View itemView) {
            super(itemView);
        }

        public ItemFragmentQuoteBinding getBinding() {
            return this.mBinding;
        }

        public void setBinding(ItemFragmentQuoteBinding binding) {
            this.mBinding = binding;
        }

        public void update() {
            if (mData != null && mData.size() != 0) {
                QuoteEntity quoteEntity = mData.get(getLayoutPosition());
                if (quoteEntity != null) {
                    String name = quoteEntity.getInstrument_name();
                    if (name != null && name.contains("&")) mBinding.quoteName.setTextSize(13);
                    else mBinding.quoteName.setTextSize(15);
                    mBinding.quoteName.setText(name);
                    setTextColor(mBinding.quoteLatest, quoteEntity.getLast_price());
                    if (mSwitchChange) {
                        setTextColor(mBinding.quoteChangePercent, quoteEntity.getChange());
                    } else {
                        setTextColor(mBinding.quoteChangePercent, quoteEntity.getChange_percent());
                    }
                    if (mSwitchVolume) {
                        mBinding.quoteOpenInterest.setText(quoteEntity.getVolume());
                    } else {
                        mBinding.quoteOpenInterest.setText(quoteEntity.getOpen_interest());
                    }
                }
            }
        }

        private void updatePart(Bundle bundle) {
            for (String key :
                    bundle.keySet()) {
                String value = bundle.getString(key);
                switch (key) {
                    case "latest":
                        setTextColor(mBinding.quoteLatest, value);
                        break;
                    case "change":
                        if (mSwitchChange) {
                            setTextColor(mBinding.quoteChangePercent, value);
                        }
                        break;
                    case "change_percent":
                        if (!mSwitchChange) {
                            setTextColor(mBinding.quoteChangePercent, value);
                        }
                        break;
                    case "volume":
                        if (mSwitchVolume) {
                            mBinding.quoteOpenInterest.setText(value);
                        }
                        break;
                    case "open_interest":
                        if (!mSwitchVolume) {
                            mBinding.quoteOpenInterest.setText(value);
                        }
                        break;
                }
            }
        }

        /**
         * date: 7/9/17
         * author: chenli
         * description: 设置价格颜色
         */
        public void setTextColor(TextView textView, String data) {
            textView.setText(data);
            if (data != null) {
                if (data.contains("-")) {
                    if (data.length() > 1) {
                        textView.setTextColor(ContextCompat.getColor(sContext, R.color.text_green));
                    } else {
                        textView.setTextColor(ContextCompat.getColor(sContext, R.color.white));
                    }
                } else {
                    textView.setTextColor(ContextCompat.getColor(sContext, R.color.text_red));
                }
            }
        }
    }
}

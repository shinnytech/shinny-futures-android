package com.xinyi.shinnyfutures.view.adapter;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.xinyi.shinnyfutures.R;
import com.xinyi.shinnyfutures.databinding.ItemActivitySearchQuoteBinding;
import com.xinyi.shinnyfutures.model.bean.searchinfobean.SearchEntity;
import com.xinyi.shinnyfutures.utils.LatestFileUtils;

import java.util.ArrayList;
import java.util.List;


/**
 * date: 7/9/17
 * author: chenli
 * description: 搜索页列表适配器
 * version:
 * state: done
 */
public class SearchAdapter extends RecyclerView.Adapter<SearchAdapter.ItemViewHolder> {
    private Context sContext;
    private List<SearchEntity> mData;
    private List<SearchEntity> mDataCopy;
    private List<SearchEntity> mDataOriginal;
    private OnItemClickListener mOnItemClickListener;

    public SearchAdapter(Context context) {
        this.sContext = context;
        this.mData = new ArrayList<>(LatestFileUtils.getSearchEntitiesHistory().values());
        this.mDataCopy = new ArrayList<>(LatestFileUtils.getSearchEntitiesHistory().values());
        this.mDataOriginal = new ArrayList<>(LatestFileUtils.getSearchEntities().values());
    }

    public void setOnItemClickListener(OnItemClickListener mOnItemClickListener) {
        this.mOnItemClickListener = mOnItemClickListener;
    }

    @Override
    public ItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        ItemActivitySearchQuoteBinding binding = DataBindingUtil.inflate(LayoutInflater
                .from(sContext), R.layout.item_activity_search_quote, parent, false);
        ItemViewHolder holder = new ItemViewHolder(binding.getRoot());
        holder.setBinding(binding);
        holder.initEvent();
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

    public void filter(String text) {
        mData.clear();
        if (text.isEmpty()) {
            mData.addAll(mDataCopy);
        } else {
            text = text.toLowerCase();
            for (SearchEntity searchEntity : mDataOriginal) {
                if (searchEntity.getPy().toLowerCase().contains(text) || searchEntity.getInstrumentName().toLowerCase().contains(text) || searchEntity.getInstrumentId().toLowerCase().contains(text) || searchEntity.getExchangeName().toLowerCase().contains(text)) {
                    mData.add(searchEntity);
                }
            }
        }
        notifyDataSetChanged();
    }

    public interface OnItemClickListener {
        void OnItemJump(SearchEntity searchEntity, String instrument_id);

        void OnItemCollect(View view, String instrument_id);
    }

    public class ItemViewHolder extends RecyclerView.ViewHolder {

        SearchEntity searchEntity;
        String instrument_id;

        private ItemActivitySearchQuoteBinding mBinding;

        public ItemViewHolder(View itemView) {
            super(itemView);
        }

        public ItemActivitySearchQuoteBinding getBinding() {
            return this.mBinding;
        }

        public void setBinding(ItemActivitySearchQuoteBinding binding) {
            this.mBinding = binding;
        }

        private void initEvent() {
            mBinding.ivSearchCollect.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mOnItemClickListener != null) {
                        mOnItemClickListener.OnItemCollect(v, instrument_id);
                    }
                }
            });
            mBinding.rlSearchJump.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mOnItemClickListener != null) {
                        mOnItemClickListener.OnItemJump(searchEntity, instrument_id);
                    }
                }
            });
        }

        public void update() {
            if (mData != null && mData.size() != 0) {
                searchEntity = mData.get(getLayoutPosition());
                if (searchEntity != null) {
                    instrument_id = searchEntity.getInstrumentId();
                    mBinding.tvSearchInstrumentName.setText(searchEntity.getInstrumentName());
                    if (instrument_id != null && instrument_id.contains("&"))
                        mBinding.tvSearchInstrumentId.setText("");
                    else {
                        String name = "(" + instrument_id + ")";
                        mBinding.tvSearchInstrumentId.setText(name);
                    }
                    mBinding.tvSearchExchangeName.setText(searchEntity.getExchangeName());
                    if (LatestFileUtils.getOptionalInsList().containsKey(instrument_id)) {
                        mBinding.ivSearchCollect.setImageResource(R.mipmap.ic_favorite_white_24dp);
                    } else {
                        mBinding.ivSearchCollect.setImageResource(R.mipmap.ic_favorite_border_white_24dp);
                    }
                }
            }
        }

    }
}

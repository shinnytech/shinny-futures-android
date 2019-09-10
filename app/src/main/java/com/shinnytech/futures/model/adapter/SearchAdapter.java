package com.shinnytech.futures.model.adapter;

import android.content.Context;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.shinnytech.futures.R;
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
public class SearchAdapter extends RecyclerView.Adapter<SearchAdapter.ItemViewHolder> {
    private Context sContext;
    private List<SearchEntity> mData;
    private List<SearchEntity> mDataCopy;
    private List<SearchEntity> mDataOriginal;
    private OnItemClickListener mOnItemClickListener;

    public SearchAdapter(Context context) {
        this.sContext = context;
        this.mData = new ArrayList<>(LatestFileManager.getSearchEntitiesHistory().values());
        this.mDataCopy = new ArrayList<>(LatestFileManager.getSearchEntitiesHistory().values());
        this.mDataOriginal = new ArrayList(LatestFileManager.getSearchEntities().values());
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
            List<SearchEntity> data0 = new ArrayList<>();
            List<SearchEntity> data1 = new ArrayList<>();
            List<SearchEntity> data2 = new ArrayList<>();
            List<SearchEntity> data3 = new ArrayList<>();
            List<SearchEntity> data4 = new ArrayList<>();
            for (SearchEntity searchEntity : mDataOriginal) {
                if (searchEntity.getIns_id().toLowerCase().contains(text)) {
                    data0.add(searchEntity);
                    continue;
                }
                if (searchEntity.getPy().toLowerCase().contains(text)) {
                    data1.add(searchEntity);
                    continue;
                }
                if (searchEntity.getInstrumentName().toLowerCase().contains(text)) {
                    data2.add(searchEntity);
                    continue;
                }
                if (searchEntity.getExchangeId().toLowerCase().contains(text)) {
                    data3.add(searchEntity);
                    continue;
                }
                if (searchEntity.getExchangeName().toLowerCase().contains(text)) {
                    data4.add(searchEntity);
                    continue;
                }
            }
            Collections.sort(data0);
            Collections.sort(data1);
            Collections.sort(data2);
            Collections.sort(data3);
            Collections.sort(data4);
            mData.addAll(data0);
            mData.addAll(data1);
            mData.addAll(data2);
            mData.addAll(data3);
            mData.addAll(data4);
        }
        notifyDataSetChanged();
    }

    public interface OnItemClickListener {
        void OnItemJump(SearchEntity searchEntity, String instrument_id);

        void OnItemCollect(View view, String instrument_id);
    }

    public class ItemViewHolder extends RecyclerView.ViewHolder {

        private SearchEntity searchEntity;
        private String instrument_id;

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
            if (mData == null || mData.size() == 0) return;
            searchEntity = mData.get(getLayoutPosition());
            if (searchEntity == null) return;
            instrument_id = searchEntity.getInstrumentId();
            String ins_id = searchEntity.getIns_id();
            mBinding.tvSearchInstrumentName.setText(searchEntity.getInstrumentName());
            if (ins_id.contains("&"))
                mBinding.tvSearchInstrumentId.setText("");
            else {
                String name = "(" + ins_id + ")";
                mBinding.tvSearchInstrumentId.setText(name);
            }
            mBinding.tvSearchExchangeName.setText(searchEntity.getExchangeName());
            if (LatestFileManager.getOptionalInsList().containsKey(instrument_id)) {
                mBinding.ivSearchCollect.setImageResource(R.mipmap.ic_favorite_white_24dp);
            } else {
                mBinding.ivSearchCollect.setImageResource(R.mipmap.ic_favorite_border_white_24dp);
            }

        }

    }
}

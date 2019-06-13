package com.shinnytech.futures.model.adapter;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.shinnytech.futures.R;
import com.shinnytech.futures.databinding.ItemFragmentQuoteRecommendBinding;

import java.util.ArrayList;
import java.util.List;

/**
 * date: 7/9/17
 * author: chenli
 * description: 推荐合约列表适配器
 * version:
 * state: done
 */
public class QuoteAdapterRecommend extends RecyclerView.Adapter<QuoteAdapterRecommend.ItemViewHolder> {
    private Context sContext;
    private List<String> mData = new ArrayList<>();
    private List<String> mDataPre = new ArrayList<>();

    public QuoteAdapterRecommend(Context context, List<String> data) {
        this.sContext = context;
        this.mData.addAll(data);
    }

    public List<String> getmDataPre(){
        return mDataPre;
    }

    @Override
    public ItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        ItemFragmentQuoteRecommendBinding binding = DataBindingUtil.inflate(LayoutInflater
                .from(sContext), R.layout.item_fragment_quote_recommend, parent, false);
        ItemViewHolder holder = new ItemViewHolder(binding.getRoot());
        holder.setBinding(binding);
        holder.initEvent();
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


    /**
     * date: 2019/4/22
     * author: chenli
     * description: 正常合约布局
     * version:
     * state:
     */
    class ItemViewHolder extends RecyclerView.ViewHolder {

        private ItemFragmentQuoteRecommendBinding mBinding;

        public ItemViewHolder(View itemView) {
            super(itemView);
        }

        public ItemFragmentQuoteRecommendBinding getBinding() {
            return this.mBinding;
        }

        public void setBinding(ItemFragmentQuoteRecommendBinding binding) {
            this.mBinding = binding;
        }

        public void update() {
            if (mData == null || mData.size() == 0) return;
            int index = getLayoutPosition();
            String data = mData.get(index);
            mBinding.tvIns.setText(data);
        }

        public void initEvent() {
            mBinding.tvIns.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String tag = (String) mBinding.tvIns.getTag();
                    if ("0".equals(tag)) {
                        mBinding.tvIns.setBackground(ContextCompat.getDrawable(sContext, R.drawable.fragment_quote_recommend_rectangle));
                        mBinding.tvIns.setTag("1");
                        mDataPre.add(mBinding.tvIns.getText().toString());
                    } else if ("1".equals(tag)) {
                        mBinding.tvIns.setBackground(ContextCompat.getDrawable(sContext, R.drawable.fragment_quote_recommend_rectangle_border));
                        mBinding.tvIns.setTag("0");
                        mDataPre.remove(mBinding.tvIns.getText().toString());
                    }
                }
            });
        }
    }
}

package com.shinnytech.futures.model.adapter;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.shinnytech.futures.R;
import com.shinnytech.futures.databinding.ItemFragmentQuoteRecommendBinding;
import com.shinnytech.futures.model.bean.futureinfobean.QuoteEntity;
import com.shinnytech.futures.model.bean.searchinfobean.SearchEntity;
import com.shinnytech.futures.model.engine.LatestFileManager;
import com.shinnytech.futures.utils.MathUtils;

import java.util.List;

import static com.shinnytech.futures.model.engine.LatestFileManager.getUpDownRate;


/**
 * date: 7/9/17
 * author: chenli
 * description: 行情页适配器
 * version:
 * state: done
 */
public class QuoteAdapterRecommend extends RecyclerView.Adapter<QuoteAdapterRecommend.ItemViewHolder> {
    private Context sContext;
    private List<QuoteEntity> mData;
    private OnItemClickListener mOnItemClickListener;

    public QuoteAdapterRecommend(Context context, List<QuoteEntity> data) {
        this.sContext = context;
        this.mData = data;
    }

    public List<QuoteEntity> getData() {
        return mData;
    }

    public void setData(List<QuoteEntity> data) {
        this.mData = data;
    }

    public void setOnItemClickListener(OnItemClickListener mOnItemClickListener) {
        this.mOnItemClickListener = mOnItemClickListener;
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

    public interface OnItemClickListener {
        void OnItemCollect(View view, String instrument_id, int position);
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
        private String instrumentId;
        private int position;

        public ItemViewHolder(View itemView) {
            super(itemView);
        }

        public ItemFragmentQuoteRecommendBinding getBinding() {
            return this.mBinding;
        }

        public void setBinding(ItemFragmentQuoteRecommendBinding binding) {
            this.mBinding = binding;
        }

        private void initEvent() {
            mBinding.addRecommend.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mOnItemClickListener != null) {
                        mOnItemClickListener.OnItemCollect(v, instrumentId, position);
                    }
                }
            });
        }

        public void update() {
            if (mData == null || mData.size() == 0) return;
            position = getLayoutPosition();
            QuoteEntity quoteEntity = mData.get(position);
            if (quoteEntity == null) return;
            try {
                instrumentId = quoteEntity.getInstrument_id();
                if (instrumentId == null) return;
                String instrumentName = instrumentId;
                SearchEntity searchEntity = LatestFileManager.getSearchEntities().get(instrumentId);
                if (searchEntity != null) instrumentName = searchEntity.getInstrumentName();
                if (instrumentName.contains("&")) mBinding.quoteName.setTextSize(10);
                else mBinding.quoteName.setTextSize(15);
                mBinding.quoteName.setText(instrumentName);

                String pre_settlement = LatestFileManager.saveScaleByPtick(quoteEntity.getPre_settlement(), instrumentId);
                String latest = LatestFileManager.saveScaleByPtick(quoteEntity.getLast_price(), instrumentId);
                String changePercent = MathUtils.round(
                        getUpDownRate(quoteEntity.getLast_price(), quoteEntity.getPre_settlement()), 2);

                setTextColor(mBinding.quoteLatest, latest, pre_settlement);
                setChangeTextColor(mBinding.quoteChangePercent, changePercent);

            } catch (Exception e) {
                e.printStackTrace();
            }


        }

        private void updatePart(Bundle bundle) {
            for (String key :
                    bundle.keySet()) {
                String value = bundle.getString(key);
                switch (key) {
                    case "latest":
                        setTextColor(mBinding.quoteLatest, value, bundle.getString("pre_settlement"));
                        break;
                    case "change_percent":
                        setChangeTextColor(mBinding.quoteChangePercent, value);
                        break;
                    default:
                        break;

                }
            }
        }

        /**
         * date: 7/9/17
         * author: chenli
         * description: 设置涨跌幅文字颜色
         */
        public void setChangeTextColor(TextView textView, String data) {
            textView.setText(data);
            if (data == null || data.equals("-")) {
                textView.setTextColor(ContextCompat.getColor(sContext, R.color.white));
                return;
            }
            try {
                float value = new Float(data);
                if (value < 0)
                    textView.setTextColor(ContextCompat.getColor(sContext, R.color.text_green));
                else if (value > 0)
                    textView.setTextColor(ContextCompat.getColor(sContext, R.color.text_red));
                else textView.setTextColor(ContextCompat.getColor(sContext, R.color.white));
            } catch (Exception e) {
                e.printStackTrace();

            }
        }

        /**
         * date: 2018/12/4
         * author: chenli
         * description: 设置最新价文字颜色
         */
        public void setTextColor(TextView textView, String latest, String pre_settlement) {
            textView.setText(latest);
            if (latest == null || latest.equals("-") ||
                    pre_settlement == null || pre_settlement.equals("-")) {
                textView.setTextColor(ContextCompat.getColor(sContext, R.color.white));
                return;
            }
            try {
                float value = Float.parseFloat(latest) - Float.parseFloat(pre_settlement);
                if (value < 0)
                    textView.setTextColor(ContextCompat.getColor(sContext, R.color.text_green));
                else if (value > 0)
                    textView.setTextColor(ContextCompat.getColor(sContext, R.color.text_red));
                else textView.setTextColor(ContextCompat.getColor(sContext, R.color.white));
            } catch (Exception e) {
                e.printStackTrace();

            }
        }
    }
}

package com.shinnytech.futures.view.adapter;

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
import com.shinnytech.futures.databinding.ItemFragmentQuoteBinding;
import com.shinnytech.futures.model.bean.futureinfobean.QuoteEntity;
import com.shinnytech.futures.model.bean.searchinfobean.SearchEntity;
import com.shinnytech.futures.model.engine.LatestFileManager;
import com.shinnytech.futures.utils.MathUtils;

import java.util.List;

import static com.shinnytech.futures.constants.CommonConstants.DALIANZUHE;
import static com.shinnytech.futures.constants.CommonConstants.ZHENGZHOUZUHE;
import static com.shinnytech.futures.model.engine.LatestFileManager.getUpDown;
import static com.shinnytech.futures.model.engine.LatestFileManager.getUpDownRate;


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
    private String mTitle;
    private boolean mSwitchChange = false;
    private boolean mSwitchVolume = false;
    private boolean mSwitchLowerLimit = false;

    public QuoteAdapter(Context context, List<QuoteEntity> data, String title) {
        this.sContext = context;
        this.mData = data;
        this.mTitle = title;
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

    /**
     * date: 7/9/17
     * author: chenli
     * description: 涨停价/跌停价切换
     */
    public void switchLastView() {
        mSwitchLowerLimit = !mSwitchLowerLimit;
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
                if (quoteEntity == null) {
                    return;
                }

                String instrumentId = quoteEntity.getInstrument_id();
                SearchEntity searchEntity = LatestFileManager.getSearchEntities().get(instrumentId);
                String instrumentName = instrumentId;
                if (searchEntity != null) instrumentName = searchEntity.getInstrumentName();
                if (instrumentName.contains("&")) mBinding.quoteName.setTextSize(10);
                else mBinding.quoteName.setTextSize(15);
                mBinding.quoteName.setText(instrumentName);

                String latest = LatestFileManager.saveScaleByPtick(quoteEntity.getLast_price(), instrumentId);
                String changePercent = MathUtils.round(
                        getUpDownRate(quoteEntity.getLast_price(), quoteEntity.getPre_settlement()), 2);
                String change = LatestFileManager.saveScaleByPtick(
                        getUpDown(quoteEntity.getLast_price(), quoteEntity.getPre_settlement()), instrumentId);
                String lowerLimit = LatestFileManager.saveScaleByPtick(quoteEntity.getLower_limit(), instrumentId);
                String upperLimit = LatestFileManager.saveScaleByPtick(quoteEntity.getUpper_limit(), instrumentId);
                String askPrice1 = LatestFileManager.saveScaleByPtick(quoteEntity.getAsk_price1(), instrumentId);
                String bidPrice1 = LatestFileManager.saveScaleByPtick(quoteEntity.getBid_price1(), instrumentId);

                if (DALIANZUHE.equals(mTitle) || ZHENGZHOUZUHE.equals(mTitle)) {
                    if (mSwitchLowerLimit) {
                        setTextColor(mBinding.quoteLatest, lowerLimit);
                    } else {
                        setTextColor(mBinding.quoteLatest, upperLimit);
                    }
                    if (mSwitchChange) {
                        setTextColor(mBinding.quoteChangePercent, bidPrice1);
                    } else {
                        setTextColor(mBinding.quoteChangePercent, askPrice1);
                    }
                    if (mSwitchVolume) {
                        mBinding.quoteOpenInterest.setText(quoteEntity.getBid_volume1());
                    } else {
                        mBinding.quoteOpenInterest.setText(quoteEntity.getAsk_volume1());
                    }
                } else {
                    setTextColor(mBinding.quoteLatest, latest);
                    if (mSwitchChange) {
                        setTextColor(mBinding.quoteChangePercent, change);
                    } else {
                        setTextColor(mBinding.quoteChangePercent, changePercent);
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
                        if (!(DALIANZUHE.equals(mTitle) || ZHENGZHOUZUHE.equals(mTitle)))
                            setTextColor(mBinding.quoteLatest, value);
                        break;
                    case "change":
                        if (!(DALIANZUHE.equals(mTitle) || ZHENGZHOUZUHE.equals(mTitle)) && mSwitchChange) {
                            setTextColor(mBinding.quoteChangePercent, value);
                        }
                        break;
                    case "change_percent":
                        if (!(DALIANZUHE.equals(mTitle) || ZHENGZHOUZUHE.equals(mTitle)) && !mSwitchChange) {
                            setTextColor(mBinding.quoteChangePercent, value);
                        }
                        break;
                    case "volume":
                        if (!(DALIANZUHE.equals(mTitle) || ZHENGZHOUZUHE.equals(mTitle)) && mSwitchVolume) {
                            mBinding.quoteOpenInterest.setText(value);
                        }
                        break;
                    case "open_interest":
                        if (!(DALIANZUHE.equals(mTitle) || ZHENGZHOUZUHE.equals(mTitle)) && !mSwitchVolume) {
                            mBinding.quoteOpenInterest.setText(value);
                        }
                        break;
                    case "upper_limit":
                        if (DALIANZUHE.equals(mTitle) || ZHENGZHOUZUHE.equals(mTitle) && !mSwitchLowerLimit)
                            setTextColor(mBinding.quoteLatest, value);
                        break;
                    case "lower_limit":
                        if ((DALIANZUHE.equals(mTitle) || ZHENGZHOUZUHE.equals(mTitle)) && mSwitchLowerLimit) {
                            setTextColor(mBinding.quoteLatest, value);
                        }
                        break;
                    case "ask_price1":
                        if ((DALIANZUHE.equals(mTitle) || ZHENGZHOUZUHE.equals(mTitle)) && mSwitchChange) {
                            setTextColor(mBinding.quoteChangePercent, value);
                        }
                        break;
                    case "ask_volume1":
                        if ((DALIANZUHE.equals(mTitle) || ZHENGZHOUZUHE.equals(mTitle)) && mSwitchVolume) {
                            mBinding.quoteOpenInterest.setText(value);
                        }
                        break;
                    case "bid_price1":
                        if ((DALIANZUHE.equals(mTitle) || ZHENGZHOUZUHE.equals(mTitle)) && !mSwitchChange) {
                            setTextColor(mBinding.quoteChangePercent, value);
                        }
                        break;
                    case "bid_volume1":
                        if ((DALIANZUHE.equals(mTitle) || ZHENGZHOUZUHE.equals(mTitle)) && !mSwitchVolume) {
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

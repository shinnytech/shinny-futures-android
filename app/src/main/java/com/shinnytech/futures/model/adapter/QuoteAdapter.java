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
import com.shinnytech.futures.databinding.ItemFragmentQuoteBinding;
import com.shinnytech.futures.model.bean.accountinfobean.PositionEntity;
import com.shinnytech.futures.model.bean.accountinfobean.UserEntity;
import com.shinnytech.futures.model.bean.futureinfobean.QuoteEntity;
import com.shinnytech.futures.model.bean.searchinfobean.SearchEntity;
import com.shinnytech.futures.model.engine.DataManager;
import com.shinnytech.futures.model.engine.LatestFileManager;
import com.shinnytech.futures.utils.MathUtils;

import java.util.List;
import java.util.Map;

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

    /**
     * date: 2019/4/22
     * author: chenli
     * description: 正常合约布局
     * version:
     * state:
     */
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
            if (mData == null || mData.size() == 0) return;
            QuoteEntity quoteEntity = mData.get(getLayoutPosition());
            if (quoteEntity == null) return;
            try {

                String instrumentId = quoteEntity.getInstrument_id();
                if (instrumentId == null) return;

                //合约高亮
                DataManager manager = DataManager.getInstance();
                UserEntity userEntity = manager.getTradeBean().getUsers().get(manager.USER_ID);
                if (userEntity != null){
                    Map<String, PositionEntity> positionEntityMap = userEntity.getPositions();
                    if (positionEntityMap.containsKey(instrumentId))
                        mBinding.llQuote.setBackground(sContext.getResources().getDrawable(R.drawable.item_touch_bg_highlight));
                    else mBinding.llQuote.setBackground(sContext.getResources().getDrawable(R.drawable.item_touch_bg));
                }

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
                String change = LatestFileManager.saveScaleByPtick(
                        getUpDown(quoteEntity.getLast_price(), quoteEntity.getPre_settlement()), instrumentId);
                String askPrice1 = LatestFileManager.saveScaleByPtick(quoteEntity.getAsk_price1(), instrumentId);
                String bidPrice1 = LatestFileManager.saveScaleByPtick(quoteEntity.getBid_price1(), instrumentId);

                setTextColor(mBinding.quoteLatest, latest, pre_settlement);
                if (DALIANZUHE.equals(mTitle) || ZHENGZHOUZUHE.equals(mTitle)) {
                    if (mSwitchChange) {
                        setTextColor(mBinding.quoteChangePercent, askPrice1, pre_settlement);
                    } else {
                        setTextColor(mBinding.quoteChangePercent, bidPrice1, pre_settlement);
                    }
                    if (mSwitchVolume) {
                        mBinding.quoteOpenInterest.setText(quoteEntity.getAsk_volume1());
                    } else {
                        mBinding.quoteOpenInterest.setText(quoteEntity.getBid_volume1());
                    }
                } else {
                    if (mSwitchChange) {
                        setChangeTextColor(mBinding.quoteChangePercent, change);
                    } else {
                        setChangeTextColor(mBinding.quoteChangePercent, changePercent);
                    }
                    if (mSwitchVolume) {
                        mBinding.quoteOpenInterest.setText(quoteEntity.getVolume());
                    } else {
                        mBinding.quoteOpenInterest.setText(quoteEntity.getOpen_interest());
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }


        }

        private void updatePart(Bundle bundle) {
            String instrumentId = bundle.getString("instrument_id");
            //合约高亮
            DataManager manager = DataManager.getInstance();
            UserEntity userEntity = manager.getTradeBean().getUsers().get(manager.USER_ID);
            if (userEntity != null){
                Map<String, PositionEntity> positionEntityMap = userEntity.getPositions();
                if (positionEntityMap.containsKey(instrumentId))
                    mBinding.llQuote.setBackground(sContext.getResources().getDrawable(R.drawable.item_touch_bg_highlight));
                else mBinding.llQuote.setBackground(sContext.getResources().getDrawable(R.drawable.item_touch_bg));
            }

            for (String key :
                    bundle.keySet()) {
                String value = bundle.getString(key);
                switch (key) {
                    case "latest":
                        setTextColor(mBinding.quoteLatest, value, bundle.getString("pre_settlement"));
                        break;
                    case "change":
                        if (!(DALIANZUHE.equals(mTitle) || ZHENGZHOUZUHE.equals(mTitle)) && mSwitchChange) {
                            setChangeTextColor(mBinding.quoteChangePercent, value);
                        }
                        break;
                    case "change_percent":
                        if (!(DALIANZUHE.equals(mTitle) || ZHENGZHOUZUHE.equals(mTitle)) && !mSwitchChange) {
                            setChangeTextColor(mBinding.quoteChangePercent, value);
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
                    case "ask_price1":
                        if ((DALIANZUHE.equals(mTitle) || ZHENGZHOUZUHE.equals(mTitle)) && mSwitchChange) {
                            setTextColor(mBinding.quoteChangePercent, value, bundle.getString("pre_settlement"));
                        }
                        break;
                    case "ask_volume1":
                        if ((DALIANZUHE.equals(mTitle) || ZHENGZHOUZUHE.equals(mTitle)) && mSwitchVolume) {
                            mBinding.quoteOpenInterest.setText(value);
                        }
                        break;
                    case "bid_price1":
                        if ((DALIANZUHE.equals(mTitle) || ZHENGZHOUZUHE.equals(mTitle)) && !mSwitchChange) {
                            setTextColor(mBinding.quoteChangePercent, value, bundle.getString("pre_settlement"));
                        }
                        break;
                    case "bid_volume1":
                        if ((DALIANZUHE.equals(mTitle) || ZHENGZHOUZUHE.equals(mTitle)) && !mSwitchVolume) {
                            mBinding.quoteOpenInterest.setText(value);
                        }
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

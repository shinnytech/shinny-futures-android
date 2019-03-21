package com.shinnytech.futures.model.adapter;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.shinnytech.futures.R;
import com.shinnytech.futures.databinding.ItemFragmentOrderBinding;
import com.shinnytech.futures.model.bean.accountinfobean.OrderEntity;
import com.shinnytech.futures.model.bean.searchinfobean.SearchEntity;
import com.shinnytech.futures.model.engine.DataManager;
import com.shinnytech.futures.model.engine.LatestFileManager;
import com.shinnytech.futures.utils.MathUtils;

import java.util.Date;
import java.util.List;

/**
 * date: 7/9/17
 * author: chenli
 * description: 挂单页适配器
 * version:
 * state: done
 */
public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.ItemViewHolder> {

    private Context sContext;
    private List<OrderEntity> mOrderData;

    public OrderAdapter(Context context, List<OrderEntity> orderData) {
        this.sContext = context;
        this.mOrderData = orderData;
    }

    public List<OrderEntity> getData() {
        return mOrderData;
    }

    public void setData(List<OrderEntity> data) {
        this.mOrderData = data;
    }

    @Override
    public ItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        ItemFragmentOrderBinding binding = DataBindingUtil.inflate(LayoutInflater
                .from(sContext), R.layout.item_fragment_order, parent, false);
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
        return mOrderData == null ? 0 : mOrderData.size();
    }

    class ItemViewHolder extends RecyclerView.ViewHolder {

        private OrderEntity orderEntity;

        private ItemFragmentOrderBinding mBinding;

        public ItemViewHolder(View itemView) {
            super(itemView);
        }

        public ItemFragmentOrderBinding getBinding() {
            return this.mBinding;
        }

        public void setBinding(ItemFragmentOrderBinding binding) {
            this.mBinding = binding;
        }

        public void update() {
            if (mOrderData == null || mOrderData.size() == 0) return;
            orderEntity = mOrderData.get(getLayoutPosition());
            if (orderEntity == null) return;

            try {
                String instrument_id = orderEntity.getExchange_id() + "." + orderEntity.getInstrument_id();
                SearchEntity insName = LatestFileManager.getSearchEntities().get(instrument_id);
                mBinding.orderName.setText(insName == null ? orderEntity.getInstrument_id() : insName.getInstrumentName());
                mBinding.orderStatus.setText(orderEntity.getLast_msg());
                switch (orderEntity.getOffset()) {
                    case "OPEN":
                        mBinding.orderOffset.setText("开仓");
                        break;
                    case "CLOSETODAY":
                        mBinding.orderOffset.setText("平今");
                        break;
                    case "CLOSEHISTORY":
                        mBinding.orderOffset.setText("平昨");
                        break;
                    case "CLOSE":
                        mBinding.orderOffset.setText("平仓");
                        break;
                    case "FORCECLOSE":
                        mBinding.orderOffset.setText("强平");
                        break;
                    default:
                        mBinding.orderOffset.setText("");
                        break;
                }
                switch (orderEntity.getDirection()) {
                    case "BUY":
                        mBinding.orderOffset.setTextColor(ContextCompat.getColor(sContext, R.color.text_red));
                        break;
                    case "SELL":
                        mBinding.orderOffset.setTextColor(ContextCompat.getColor(sContext, R.color.text_green));
                        break;
                    default:
                        mBinding.orderOffset.setTextColor(ContextCompat.getColor(sContext, R.color.text_red));
                        break;
                }
                mBinding.orderPrice.setText(LatestFileManager.saveScaleByPtick(orderEntity.getLimit_price(), instrument_id));
                String volume = MathUtils.subtract(orderEntity.getVolume_orign(), orderEntity.getVolume_left()) + "/" + orderEntity.getVolume_orign();
                mBinding.orderVolume.setText(volume);
                long dateTime = Long.valueOf(orderEntity.getInsert_date_time()) / 1000000;
                //错单时间为0
                if (dateTime == 0) mBinding.orderTime.setText("--");
                else {
                    String date = DataManager.getInstance().getSimpleDateFormat().format(new Date(dateTime));
                    mBinding.orderTime.setText(date);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

        private void updatePart(Bundle bundle) {
            //撤销委托单时会局部刷新
            for (String key :
                    bundle.keySet()) {
                String value = bundle.getString(key);
                switch (key) {
                    case "status":
                        mBinding.orderStatus.setText(value);
                        break;
                    case "volume_trade":
                        mBinding.orderVolume.setText(value);
                        break;
                    default:
                        break;
                }
            }
        }
    }
}

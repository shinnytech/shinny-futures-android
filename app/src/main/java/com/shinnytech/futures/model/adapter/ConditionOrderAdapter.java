package com.shinnytech.futures.model.adapter;

import android.content.Context;
import androidx.databinding.DataBindingUtil;
import android.os.Bundle;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.shinnytech.futures.R;
import com.shinnytech.futures.databinding.ItemActivityConditionNanagerOrderBinding;
import com.shinnytech.futures.model.bean.conditionorderbean.COrderEntity;
import com.shinnytech.futures.model.bean.conditionorderbean.ConditionEntity;
import com.shinnytech.futures.model.bean.conditionorderbean.ConditionOrderEntity;
import com.shinnytech.futures.model.bean.searchinfobean.SearchEntity;
import com.shinnytech.futures.model.engine.LatestFileManager;
import com.shinnytech.futures.utils.TimeUtils;

import java.util.Date;
import java.util.List;

import static com.shinnytech.futures.constants.ConditionConstants.CONDITION_CONTINGENT_PRICE_RELATION_G;
import static com.shinnytech.futures.constants.ConditionConstants.CONDITION_CONTINGENT_PRICE_RELATION_GE;
import static com.shinnytech.futures.constants.ConditionConstants.CONDITION_CONTINGENT_PRICE_RELATION_L;
import static com.shinnytech.futures.constants.ConditionConstants.CONDITION_CONTINGENT_PRICE_RELATION_LE;
import static com.shinnytech.futures.constants.ConditionConstants.CONDITION_CONTINGENT_TYPE_MARKET_OPEN;
import static com.shinnytech.futures.constants.ConditionConstants.CONDITION_CONTINGENT_TYPE_MARKET_OPEN_TITLE;
import static com.shinnytech.futures.constants.ConditionConstants.CONDITION_CONTINGENT_TYPE_PRICE;
import static com.shinnytech.futures.constants.ConditionConstants.CONDITION_CONTINGENT_TYPE_PRICE_RANGE;
import static com.shinnytech.futures.constants.ConditionConstants.CONDITION_CONTINGENT_TYPE_PRICE_RANGE_TITLE;
import static com.shinnytech.futures.constants.ConditionConstants.CONDITION_CONTINGENT_TYPE_PRICE_TITLE;
import static com.shinnytech.futures.constants.ConditionConstants.CONDITION_CONTINGENT_TYPE_TIME;
import static com.shinnytech.futures.constants.ConditionConstants.CONDITION_CONTINGENT_TYPE_TIME_TITLE;
import static com.shinnytech.futures.constants.ConditionConstants.CONDITION_DIRECTION_BUY;
import static com.shinnytech.futures.constants.ConditionConstants.CONDITION_DIRECTION_BUY_TITLE;
import static com.shinnytech.futures.constants.ConditionConstants.CONDITION_DIRECTION_SELL;
import static com.shinnytech.futures.constants.ConditionConstants.CONDITION_DIRECTION_SELL_TITLE;
import static com.shinnytech.futures.constants.ConditionConstants.CONDITION_OFFSET_CLOSE;
import static com.shinnytech.futures.constants.ConditionConstants.CONDITION_OFFSET_CLOSE_TITLE;
import static com.shinnytech.futures.constants.ConditionConstants.CONDITION_OFFSET_OPEN;
import static com.shinnytech.futures.constants.ConditionConstants.CONDITION_OFFSET_OPEN_TITLE;
import static com.shinnytech.futures.constants.ConditionConstants.CONDITION_OFFSET_REVERSE;
import static com.shinnytech.futures.constants.ConditionConstants.CONDITION_OFFSET_REVERSE_TITLE;
import static com.shinnytech.futures.constants.ConditionConstants.CONDITION_PRICE_TYPE_CONSIDERATION;
import static com.shinnytech.futures.constants.ConditionConstants.CONDITION_PRICE_TYPE_CONSIDERATION_TITLE;
import static com.shinnytech.futures.constants.ConditionConstants.CONDITION_PRICE_TYPE_CONTINGENT;
import static com.shinnytech.futures.constants.ConditionConstants.CONDITION_PRICE_TYPE_CONTINGENT_TITLE;
import static com.shinnytech.futures.constants.ConditionConstants.CONDITION_PRICE_TYPE_LIMIT;
import static com.shinnytech.futures.constants.ConditionConstants.CONDITION_PRICE_TYPE_MARKET;
import static com.shinnytech.futures.constants.ConditionConstants.CONDITION_PRICE_TYPE_MARKET_TITLE;
import static com.shinnytech.futures.constants.ConditionConstants.CONDITION_PRICE_TYPE_OVER;
import static com.shinnytech.futures.constants.ConditionConstants.CONDITION_PRICE_TYPE_OVER_TITLE;
import static com.shinnytech.futures.constants.ConditionConstants.CONDITION_STATUS_CANCEL;
import static com.shinnytech.futures.constants.ConditionConstants.CONDITION_STATUS_CANCEL_TITLE;
import static com.shinnytech.futures.constants.ConditionConstants.CONDITION_STATUS_DISCARD;
import static com.shinnytech.futures.constants.ConditionConstants.CONDITION_STATUS_DISCARD_TITLE;
import static com.shinnytech.futures.constants.ConditionConstants.CONDITION_STATUS_LIVE;
import static com.shinnytech.futures.constants.ConditionConstants.CONDITION_STATUS_LIVE_TITLE;
import static com.shinnytech.futures.constants.ConditionConstants.CONDITION_STATUS_SUSPEND;
import static com.shinnytech.futures.constants.ConditionConstants.CONDITION_STATUS_SUSPEND_TITLE;
import static com.shinnytech.futures.constants.ConditionConstants.CONDITION_STATUS_TOUCHED;
import static com.shinnytech.futures.constants.ConditionConstants.CONDITION_STATUS_TOUCHED_TITLE;
import static com.shinnytech.futures.constants.ConditionConstants.CONDITION_TIME_TYPE_GFD;
import static com.shinnytech.futures.constants.ConditionConstants.CONDITION_TIME_TYPE_GFD_TITLE_EXHIBIT;
import static com.shinnytech.futures.constants.ConditionConstants.CONDITION_TIME_TYPE_GTC;
import static com.shinnytech.futures.constants.ConditionConstants.CONDITION_TIME_TYPE_GTC_TITLE_EXHIBIT;
import static com.shinnytech.futures.constants.ConditionConstants.CONDITION_VOLUME_TYPE_ALL;
import static com.shinnytech.futures.constants.ConditionConstants.CONDITION_VOLUME_TYPE_CLOSE_ALL;
import static com.shinnytech.futures.utils.TimeUtils.YMD_HMS_FORMAT_4;

/**
 * date: 7/9/17
 * author: chenli
 * description: 行情页适配器
 * version:
 * state: done
 */
public class ConditionOrderAdapter extends RecyclerView.Adapter<ConditionOrderAdapter.ItemViewHolder> {
    private Context sContext;
    private List<ConditionOrderEntity> mData;

    public ConditionOrderAdapter(Context context, List<ConditionOrderEntity> data) {
        this.sContext = context;
        this.mData = data;
    }

    public List<ConditionOrderEntity> getData() {
        return mData;
    }

    public void setData(List<ConditionOrderEntity> data) {
        this.mData = data;
    }

    @Override
    public ItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        ItemActivityConditionNanagerOrderBinding binding = DataBindingUtil.inflate(LayoutInflater
                .from(sContext), R.layout.item_activity_condition_nanager_order, parent, false);
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
            itemViewHolder.update();
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

        private ItemActivityConditionNanagerOrderBinding mBinding;

        public ItemViewHolder(View itemView) {
            super(itemView);
        }

        public ItemActivityConditionNanagerOrderBinding getBinding() {
            return this.mBinding;
        }

        public void setBinding(ItemActivityConditionNanagerOrderBinding binding) {
            this.mBinding = binding;
        }

        public void update() {
            if (mData == null || mData.size() == 0) return;
            ConditionOrderEntity conditionOrderEntity = mData.get(getLayoutPosition());
            if (conditionOrderEntity == null) return;
            List<COrderEntity> orderList = conditionOrderEntity.getOrder_list();
            COrderEntity orderEntity = null;
            if (orderList != null && !orderList.isEmpty())orderEntity = orderList.get(0);
            if (orderEntity == null)return;
            String ins = orderEntity.getInstrument_id();
            String exchange_id = orderEntity.getExchange_id();
            String instrument_id = exchange_id+ "." + ins;
            String name = ins;
            SearchEntity searchEntity = LatestFileManager.getSearchEntities().get(instrument_id);
            if (searchEntity != null) name = searchEntity.getInstrumentName();
            mBinding.conditionOrderName.setText(name);
            String status = conditionOrderEntity.getStatus();
            String touched_time = "";
            try {
                touched_time = TimeUtils.date2String(new Date(Long.parseLong(conditionOrderEntity.
                        getTouched_time()) * 1000), YMD_HMS_FORMAT_4);
            }catch (NumberFormatException e){
                e.printStackTrace();
            }
            switch (status){
                case CONDITION_STATUS_LIVE:
                    touched_time = CONDITION_STATUS_LIVE_TITLE + " " + touched_time;
                    break;
                case CONDITION_STATUS_SUSPEND:
                    touched_time = CONDITION_STATUS_SUSPEND_TITLE + " " + touched_time;
                    break;
                case CONDITION_STATUS_CANCEL:
                    touched_time = CONDITION_STATUS_CANCEL_TITLE + " " + touched_time;
                    break;
                case CONDITION_STATUS_DISCARD:
                    touched_time = CONDITION_STATUS_DISCARD_TITLE + " " + touched_time;
                    break;
                case CONDITION_STATUS_TOUCHED:
                    touched_time = CONDITION_STATUS_TOUCHED_TITLE + " " + touched_time;
                    break;
                default:
                    break;
            }
            mBinding.conditionOrderStatus.setText(touched_time);

            List<ConditionEntity> conditionList = conditionOrderEntity.getCondition_list();
            ConditionEntity conditionEntity = null;
            if (conditionList != null && !conditionList.isEmpty())conditionEntity = conditionList.get(0);
            if (conditionEntity == null)return;
            switch (conditionEntity.getContingent_type()){
                case CONDITION_CONTINGENT_TYPE_PRICE:
                    mBinding.conditionOrderType.setText(CONDITION_CONTINGENT_TYPE_PRICE_TITLE);
                    String priceRelation = conditionEntity.getPrice_relation();
                    String relation = "";
                    String price = LatestFileManager.saveScaleByPtick(
                            conditionEntity.getContingent_price(), instrument_id);
                    switch (priceRelation){
                        case CONDITION_CONTINGENT_PRICE_RELATION_GE:
                            relation = sContext.getResources().getString(R.string.trigger_larger_equal);
                            break;
                        case CONDITION_CONTINGENT_PRICE_RELATION_G:
                            relation = sContext.getResources().getString(R.string.trigger_larger);
                            break;
                        case CONDITION_CONTINGENT_PRICE_RELATION_LE:
                            relation = sContext.getResources().getString(R.string.trigger_lower_equal);
                            break;
                        case CONDITION_CONTINGENT_PRICE_RELATION_L:
                            relation = sContext.getResources().getString(R.string.trigger_lower);
                            break;
                        default:
                            break;
                    }
                    String condition = relation + " " + price;
                    //止盈止损
                    if (conditionList.size() == 2){
                        ConditionEntity conditionEntity1 = conditionList.get(1);
                        if (conditionEntity1 != null && CONDITION_CONTINGENT_TYPE_PRICE.
                                equals(conditionEntity1.getContingent_type())){
                            String priceRelation1 = conditionEntity1.getPrice_relation();
                            String relation1 = "";
                            String price1 = LatestFileManager.saveScaleByPtick(
                                    conditionEntity1.getContingent_price(), instrument_id);
                            switch (priceRelation1){
                                case CONDITION_CONTINGENT_PRICE_RELATION_GE:
                                    relation1 = sContext.getResources().getString(R.string.trigger_larger_equal);
                                    break;
                                case CONDITION_CONTINGENT_PRICE_RELATION_G:
                                    relation1 = sContext.getResources().getString(R.string.trigger_larger);
                                    break;
                                case CONDITION_CONTINGENT_PRICE_RELATION_LE:
                                    relation1 = sContext.getResources().getString(R.string.trigger_lower_equal);
                                    break;
                                case CONDITION_CONTINGENT_PRICE_RELATION_L:
                                    relation1 = sContext.getResources().getString(R.string.trigger_lower);
                                    break;
                                default:
                                    break;
                            }
                            condition = condition + " or " + relation1 + " " + price1;
                        }
                    }
                    mBinding.conditionOrderCondition.setText(condition);
                    break;
                case CONDITION_CONTINGENT_TYPE_PRICE_RANGE:
                    mBinding.conditionOrderType.setText(CONDITION_CONTINGENT_TYPE_PRICE_RANGE_TITLE);
                    String priceLeft = LatestFileManager.saveScaleByPtick(
                            conditionEntity.getContingent_price_range_left(), instrument_id);
                    String priceRight = LatestFileManager.saveScaleByPtick(
                            conditionEntity.getContingent_price_range_right(), instrument_id);
                    String symbol = sContext.getResources().getString(R.string.trigger_lower_equal);
                    String data = priceLeft + " " + symbol + " " + "最新" + " " + symbol + " " + priceRight;
                    mBinding.conditionOrderCondition.setText(data);
                    break;
                case CONDITION_CONTINGENT_TYPE_MARKET_OPEN:
                    mBinding.conditionOrderType.setText(CONDITION_CONTINGENT_TYPE_MARKET_OPEN_TITLE);
                    mBinding.conditionOrderCondition.setText("再开盘发出");
                    break;
                case CONDITION_CONTINGENT_TYPE_TIME:
                    mBinding.conditionOrderType.setText(CONDITION_CONTINGENT_TYPE_TIME_TITLE);
                    try {
                        long time = Long.parseLong(conditionEntity.getContingent_time());
                        String date = TimeUtils.date2String(new Date(time * 1000), YMD_HMS_FORMAT_4);
                        mBinding.conditionOrderCondition.setText(date);
                    }catch (NumberFormatException e){
                        mBinding.conditionOrderCondition.setText("时间错误");
                    }
                    break;
                default:
                    break;
            }
            String priceType = orderEntity.getPrice_type();
            String priceTypeTitle = "";
            switch (priceType){
                case CONDITION_PRICE_TYPE_MARKET:
                    priceTypeTitle = CONDITION_PRICE_TYPE_MARKET_TITLE;
                    break;
                case CONDITION_PRICE_TYPE_CONSIDERATION:
                    priceTypeTitle = CONDITION_PRICE_TYPE_CONSIDERATION_TITLE;
                    break;
                case CONDITION_PRICE_TYPE_CONTINGENT:
                    priceTypeTitle = CONDITION_PRICE_TYPE_CONTINGENT_TITLE;
                    break;
                case CONDITION_PRICE_TYPE_LIMIT:
                    priceTypeTitle = LatestFileManager.saveScaleByPtick(
                            orderEntity.getLimit_price(), instrument_id);
                    break;
                case CONDITION_PRICE_TYPE_OVER:
                    priceTypeTitle = CONDITION_PRICE_TYPE_OVER_TITLE;
                    break;
                default:
                    break;
            }

            String direction = orderEntity.getDirection();
            String directionTitle = "";
            switch (direction){
                case CONDITION_DIRECTION_BUY:
                    directionTitle = CONDITION_DIRECTION_BUY_TITLE;
                    break;
                case CONDITION_DIRECTION_SELL:
                    directionTitle = CONDITION_DIRECTION_SELL_TITLE;
                    break;
                default:
                    break;
            }

            String offset = orderEntity.getOffset();
            String offsetTitle = "";
            switch (offset){
                case CONDITION_OFFSET_OPEN:
                    offsetTitle = CONDITION_OFFSET_OPEN_TITLE;
                    break;
                case CONDITION_OFFSET_CLOSE:
                    offsetTitle = CONDITION_OFFSET_CLOSE_TITLE;
                    break;
                case CONDITION_OFFSET_REVERSE:
                    offsetTitle = CONDITION_OFFSET_REVERSE_TITLE;
                    break;
                default:
                    break;
            }

            String volume = orderEntity.getVolume();
            if (CONDITION_VOLUME_TYPE_CLOSE_ALL.equals(orderEntity.getVolume_type()))volume = CONDITION_VOLUME_TYPE_ALL;
            String order = priceTypeTitle + " " + directionTitle + " " + offsetTitle + " " + volume + "手";
            mBinding.conditionOrderInfo.setText(order);

            String timeConditionType = conditionOrderEntity.getTime_condition_type();
            String timeConditionTypeTitle = "";
            switch (timeConditionType){
                case CONDITION_TIME_TYPE_GFD:
                    timeConditionTypeTitle = CONDITION_TIME_TYPE_GFD_TITLE_EXHIBIT;
                    break;
                case CONDITION_TIME_TYPE_GTC:
                    timeConditionTypeTitle = CONDITION_TIME_TYPE_GTC_TITLE_EXHIBIT;
                    break;
                default:
                    break;
            }
            mBinding.conditionOrderExpiry.setText(timeConditionTypeTitle);

            try {
                long insert_date_time = Long.parseLong(conditionOrderEntity.getInsert_date_time());
                String time = TimeUtils.date2String(new Date(insert_date_time * 1000), YMD_HMS_FORMAT_4);
                mBinding.conditionOrderTime.setText(time);
            }catch (NumberFormatException e){
                e.printStackTrace();
            }

        }

        private void updatePart(Bundle bundle) {
            for (String key :
                    bundle.keySet()) {
                String value = bundle.getString(key);
                switch (key) {
                    case "status":
                        mBinding.conditionOrderStatus.setText(value);
                        break;
                    default:
                        break;

                }
            }
        }

    }
}

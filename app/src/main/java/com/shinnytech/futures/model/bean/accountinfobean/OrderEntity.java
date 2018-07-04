package com.shinnytech.futures.model.bean.accountinfobean;

import android.support.annotation.NonNull;

import com.shinnytech.futures.utils.TimeUtils;

/**
 * Created on 6/16/17.
 * Created by chenli.
 * Description: .
 */

public class OrderEntity implements Comparable<OrderEntity> {
    private String key = "";
    private String order_id = "";
    private String exchange_id = "";
    private String instrument_id = "";
    private String session_id = "";
    private String front_id = "";
    private String direction = "";
    private String offset = "";
    private String volume_orign = "";
    private String volume_left = "";
    private String price_type = "";
    private String limit_price = "";
    private String status = "";
    private String time_condition = "";
    private String volume_condition = "";
    private String min_volume = "";
    private String force_close = "";
    private String hedge_flag = "";
    private String exchange_order_id = "";
    private String order_type = "";
    private String trade_type = "";
    private String last_msg = "";
    private String is_rtn = "";
    private String insert_date_time = "";

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getOrder_id() {
        return order_id;
    }

    public void setOrder_id(String order_id) {
        this.order_id = order_id;
    }

    public String getExchange_id() {
        return exchange_id;
    }

    public void setExchange_id(String exchange_id) {
        this.exchange_id = exchange_id;
    }

    public String getInstrument_id() {
        return instrument_id;
    }

    public void setInstrument_id(String instrument_id) {
        this.instrument_id = instrument_id;
    }

    public String getDirection() {
        return direction;
    }

    public void setDirection(String direction) {
        this.direction = direction;
    }

    public String getOffset() {
        return offset;
    }

    public void setOffset(String offset) {
        this.offset = offset;
    }

    public String getVolume_orign() {
        return volume_orign;
    }

    public void setVolume_orign(String volume_orign) {
        this.volume_orign = volume_orign;
    }

    public String getVolume_left() {
        return volume_left;
    }

    public void setVolume_left(String volume_left) {
        this.volume_left = volume_left;
    }

    public String getLimit_price() {
        return limit_price;
    }

    public void setLimit_price(String limit_price) {
        this.limit_price = limit_price;
    }

    public String getPrice_type() {
        return price_type;
    }

    public void setPrice_type(String price_type) {
        this.price_type = price_type;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getInsert_date_time() {
        return insert_date_time;
    }

    public void setInsert_date_time(String insert_date_time) {
        this.insert_date_time = insert_date_time;
    }

    public String getExchange_order_id() {
        return exchange_order_id;
    }

    public void setExchange_order_id(String exchange_order_id) {
        this.exchange_order_id = exchange_order_id;
    }

    public String getSession_id() {
        return session_id;
    }

    public void setSession_id(String session_id) {
        this.session_id = session_id;
    }

    public String getFront_id() {
        return front_id;
    }

    public void setFront_id(String front_id) {
        this.front_id = front_id;
    }

    public String getTime_condition() {
        return time_condition;
    }

    public void setTime_condition(String time_condition) {
        this.time_condition = time_condition;
    }

    public String getMin_volume() {
        return min_volume;
    }

    public void setMin_volume(String min_volume) {
        this.min_volume = min_volume;
    }

    public String getForce_close() {
        return force_close;
    }

    public void setForce_close(String force_close) {
        this.force_close = force_close;
    }

    public String getHedge_flag() {
        return hedge_flag;
    }

    public void setHedge_flag(String hedge_flag) {
        this.hedge_flag = hedge_flag;
    }

    public String getOrder_type() {
        return order_type;
    }

    public void setOrder_type(String order_type) {
        this.order_type = order_type;
    }

    public String getTrade_type() {
        return trade_type;
    }

    public void setTrade_type(String trade_type) {
        this.trade_type = trade_type;
    }

    public String getLast_msg() {
        return last_msg;
    }

    public void setLast_msg(String last_msg) {
        this.last_msg = last_msg;
    }

    public String getIs_rtn() {
        return is_rtn;
    }

    public void setIs_rtn(String is_rtn) {
        this.is_rtn = is_rtn;
    }

    public String getVolume_condition() {
        return volume_condition;
    }

    public void setVolume_condition(String volume_condition) {
        this.volume_condition = volume_condition;
    }

    @Override
    public int compareTo(@NonNull OrderEntity o) {
        long date1 = Long.parseLong(this.getInsert_date_time()) / 1000000;
        long date2 = Long.parseLong(o.getInsert_date_time()) / 1000000;
        if (!TimeUtils.isBetw2124(this.getInsert_date_time())) date1 += 24 * 3600 * 1000;
        if (!TimeUtils.isBetw2124(o.getInsert_date_time())) date2 += 24 * 3600 * 1000;
        return (int) -(date1 - date2);
    }

    @Override
    public boolean equals(Object obj) {

        if (obj instanceof OrderEntity) {
            OrderEntity orderEntity = (OrderEntity) obj;
            if (this.key.equals(orderEntity.key)
                    && this.last_msg.equals(orderEntity.last_msg)
                    && this.status.equals(orderEntity.status)
                    && this.volume_left.equals(orderEntity.volume_left)) {
                return true;
            }
        }
        return false;
    }
}

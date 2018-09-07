package com.shinnytech.futures.model.bean.accountinfobean;

import android.support.annotation.NonNull;

import com.shinnytech.futures.utils.TimeUtils;

import java.io.Serializable;

/**
 * Created on 6/16/17.
 * Created by chenli.
 * Description: .
 */

public class OrderEntity implements Comparable<OrderEntity>, Serializable {
    private static final long serialVersionUID = 2631590509760908282L;
    private String key = "";
    private String user_id = "";
    private String order_id = "";
    private String exchange_id = "";
    private String instrument_id = "";
    private String direction = "";
    private String offset = "";
    private String volume_orign = "";
    private String price_type = "";
    private String limit_price = "";
    private String time_condition = "";
    private String volume_condition = "";

    private String exchange_order_id = "";
    private String insert_date_time = "";

    private String status = "";
    private String last_msg = "";
    private String volume_left = "";

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
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

    public String getPrice_type() {
        return price_type;
    }

    public void setPrice_type(String price_type) {
        this.price_type = price_type;
    }

    public String getLimit_price() {
        return limit_price;
    }

    public void setLimit_price(String limit_price) {
        this.limit_price = limit_price;
    }

    public String getTime_condition() {
        return time_condition;
    }

    public void setTime_condition(String time_condition) {
        this.time_condition = time_condition;
    }

    public String getVolume_condition() {
        return volume_condition;
    }

    public void setVolume_condition(String volume_condition) {
        this.volume_condition = volume_condition;
    }

    public String getExchange_order_id() {
        return exchange_order_id;
    }

    public void setExchange_order_id(String exchange_order_id) {
        this.exchange_order_id = exchange_order_id;
    }

    public String getInsert_date_time() {
        return insert_date_time;
    }

    public void setInsert_date_time(String insert_date_time) {
        this.insert_date_time = insert_date_time;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getVolume_left() {
        return volume_left;
    }

    public void setVolume_left(String volume_left) {
        this.volume_left = volume_left;
    }

    public String getLast_msg() {
        return last_msg;
    }

    public void setLast_msg(String last_msg) {
        this.last_msg = last_msg;
    }

    @Override
    public int compareTo(@NonNull OrderEntity o) {
        long date1 = Long.parseLong(this.insert_date_time) / 1000000;
        long date2 = Long.parseLong(o.insert_date_time) / 1000000;
        if (!TimeUtils.isBetw2124(this.insert_date_time)) date1 += 24 * 3600 * 1000;
        if (!TimeUtils.isBetw2124(o.insert_date_time)) date2 += 24 * 3600 * 1000;
        return (int) -(date1 - date2);
    }

    @Override
    public boolean equals(Object obj) {

        if (obj instanceof OrderEntity) {
            OrderEntity orderEntity = (OrderEntity) obj;
            if (this.key.equals(orderEntity.key)
                    && this.status.equals(orderEntity.status)
                    && this.volume_left.equals(orderEntity.volume_left)) {
                return true;
            }
        }
        return false;
    }
}

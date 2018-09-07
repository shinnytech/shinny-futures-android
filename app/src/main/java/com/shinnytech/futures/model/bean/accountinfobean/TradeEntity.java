package com.shinnytech.futures.model.bean.accountinfobean;

import android.support.annotation.NonNull;

import com.shinnytech.futures.utils.TimeUtils;

import java.io.Serializable;

/**
 * Created on 6/16/17.
 * Created by chenli.
 * Description: .
 */

public class TradeEntity implements Comparable<TradeEntity>, Serializable {
    private static final long serialVersionUID = 2631590509760908284L;
    private String key = "";
    private String user_id = "";
    private String trade_id = "";
    private String exchange_id = "";
    private String instrument_id = "";
    private String order_id = "";
    private String exchange_trade_id = "";

    private String direction = "";
    private String offset = "";
    private String volume = "";
    private String price = "";
    private String trade_date_time = "";
    private String commission = "";

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getTrade_id() {
        return trade_id;
    }

    public void setTrade_id(String trade_id) {
        this.trade_id = trade_id;
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

    public String getExchange_trade_id() {
        return exchange_trade_id;
    }

    public void setExchange_trade_id(String exchange_trade_id) {
        this.exchange_trade_id = exchange_trade_id;
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

    public String getVolume() {
        return volume;
    }

    public void setVolume(String volume) {
        this.volume = volume;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getTrade_date_time() {
        return trade_date_time;
    }

    public void setTrade_date_time(String trade_date_time) {
        this.trade_date_time = trade_date_time;
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

    public String getCommission() {
        return commission;
    }

    public void setCommission(String commission) {
        this.commission = commission;
    }

    @Override
    public int compareTo(@NonNull TradeEntity o) {
        long date1 = Long.parseLong(this.trade_date_time) / 1000000;
        long date2 = Long.parseLong(o.trade_date_time) / 1000000;
        if (!TimeUtils.isBetw2124(this.trade_date_time)) date1 += 24 * 3600 * 1000;
        if (!TimeUtils.isBetw2124(o.trade_date_time)) date2 += 24 * 3600 * 1000;
        return (int) -(date1 - date2);
    }

    @Override
    public boolean equals(Object obj) {

        if (obj instanceof TradeEntity) {
            TradeEntity tradeEntity = (TradeEntity) obj;
            if (this.key.equals(tradeEntity.key)) {
                return true;
            }
        }
        return false;
    }

}

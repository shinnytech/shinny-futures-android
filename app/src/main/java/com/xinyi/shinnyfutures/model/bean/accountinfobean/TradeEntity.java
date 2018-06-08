package com.xinyi.shinnyfutures.model.bean.accountinfobean;

import android.support.annotation.NonNull;

import com.xinyi.shinnyfutures.utils.TimeUtils;

/**
 * Created on 6/16/17.
 * Created by chenli.
 * Description: .
 */

public class TradeEntity implements Comparable<TradeEntity> {
    private String key = "";
    private String order_id = "";
    private String exchange_id = "";
    private String instrument_id = "";
    private String exchange_trade_id = "";
    private String direction = "";
    private String offset = "";
    private String volume = "";
    private String price = "";
    private String trade_date_time = "";

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

    @Override
    public int compareTo(@NonNull TradeEntity o) {
        long date1 = Long.parseLong(this.getTrade_date_time()) / 1000000;
        long date2 = Long.parseLong(o.getTrade_date_time()) / 1000000;
        if (!TimeUtils.isBetw2124(this.getTrade_date_time())) date1 += 24 * 3600 * 1000;
        if (!TimeUtils.isBetw2124(o.getTrade_date_time())) date2 += 24 * 3600 * 1000;
        return (int) -(date1 - date2);
    }

    @Override
    public boolean equals(Object obj) {

        if (obj instanceof TradeEntity) {
            TradeEntity tradeEntity = (TradeEntity) obj;
            if (this.instrument_id.equals(tradeEntity.instrument_id)
                    && this.direction.equals(tradeEntity.direction)
                    && this.price.equals(tradeEntity.price)
                    && this.volume.equals(tradeEntity.volume)
                    && this.trade_date_time.equals(tradeEntity.trade_date_time)) {
                return true;
            }
        }
        return false;
    }

}

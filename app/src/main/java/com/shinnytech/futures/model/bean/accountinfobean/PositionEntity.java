package com.shinnytech.futures.model.bean.accountinfobean;


import android.support.annotation.NonNull;

/**
 * Created on 6/16/17.
 * Created by chenli.
 * Description: .
 */

public class PositionEntity implements Comparable<PositionEntity> {
    private String key = "";
    private String exchange_id = "";
    private String instrument_id = "";
    private String open_price_long = "";
    private String open_price_short = "";
    private String open_cost_long = "";
    private String open_cost_short = "";
    private String float_profit_long = "";
    private String float_profit_short = "";
    private String position_cost_long = "";
    private String position_cost_short = "";
    private String position_profit_long = "";
    private String position_profit_short = "";
    private String volume_long_today = "";
    private String volume_long_his = "";
    private String volume_long_frozen = "";
    private String volume_long_frozen_today = "";
    private String volume_short_today = "";
    private String volume_short_his = "";
    private String volume_short_frozen = "";
    private String volume_short_frozen_today = "";
    private String order_volume_buy_open = "";
    private String order_volume_buy_close = "";
    private String order_volume_sell_open = "";
    private String order_volume_sell_close = "";
    private String margin_long = "";
    private String margin_short = "";

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
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

    public String getOpen_price_long() {
        return open_price_long;
    }

    public void setOpen_price_long(String open_price_long) {
        this.open_price_long = open_price_long;
    }

    public String getOpen_price_short() {
        return open_price_short;
    }

    public void setOpen_price_short(String open_price_short) {
        this.open_price_short = open_price_short;
    }

    public String getOpen_cost_long() {
        return open_cost_long;
    }

    public void setOpen_cost_long(String open_cost_long) {
        this.open_cost_long = open_cost_long;
    }

    public String getOpen_cost_short() {
        return open_cost_short;
    }

    public void setOpen_cost_short(String open_cost_short) {
        this.open_cost_short = open_cost_short;
    }

    public String getFloat_profit_long() {
        return float_profit_long;
    }

    public void setFloat_profit_long(String float_profit_long) {
        this.float_profit_long = float_profit_long;
    }

    public String getFloat_profit_short() {
        return float_profit_short;
    }

    public void setFloat_profit_short(String float_profit_short) {
        this.float_profit_short = float_profit_short;
    }

    public String getPosition_cost_long() {
        return position_cost_long;
    }

    public void setPosition_cost_long(String position_cost_long) {
        this.position_cost_long = position_cost_long;
    }

    public String getPosition_cost_short() {
        return position_cost_short;
    }

    public void setPosition_cost_short(String position_cost_short) {
        this.position_cost_short = position_cost_short;
    }

    public String getPosition_profit_long() {
        return position_profit_long;
    }

    public void setPosition_profit_long(String position_profit_long) {
        this.position_profit_long = position_profit_long;
    }

    public String getPosition_profit_short() {
        return position_profit_short;
    }

    public void setPosition_profit_short(String position_profit_short) {
        this.position_profit_short = position_profit_short;
    }

    public String getVolume_long_today() {
        return volume_long_today;
    }

    public void setVolume_long_today(String volume_long_today) {
        this.volume_long_today = volume_long_today;
    }

    public String getVolume_long_his() {
        return volume_long_his;
    }

    public void setVolume_long_his(String volume_long_his) {
        this.volume_long_his = volume_long_his;
    }

    public String getVolume_long_frozen() {
        return volume_long_frozen;
    }

    public void setVolume_long_frozen(String volume_long_frozen) {
        this.volume_long_frozen = volume_long_frozen;
    }

    public String getVolume_long_frozen_today() {
        return volume_long_frozen_today;
    }

    public void setVolume_long_frozen_today(String volume_long_frozen_today) {
        this.volume_long_frozen_today = volume_long_frozen_today;
    }

    public String getVolume_short_today() {
        return volume_short_today;
    }

    public void setVolume_short_today(String volume_short_today) {
        this.volume_short_today = volume_short_today;
    }

    public String getVolume_short_his() {
        return volume_short_his;
    }

    public void setVolume_short_his(String volume_short_his) {
        this.volume_short_his = volume_short_his;
    }

    public String getVolume_short_frozen() {
        return volume_short_frozen;
    }

    public void setVolume_short_frozen(String volume_short_frozen) {
        this.volume_short_frozen = volume_short_frozen;
    }

    public String getVolume_short_frozen_today() {
        return volume_short_frozen_today;
    }

    public void setVolume_short_frozen_today(String volume_short_frozen_today) {
        this.volume_short_frozen_today = volume_short_frozen_today;
    }

    public String getOrder_volume_buy_open() {
        return order_volume_buy_open;
    }

    public void setOrder_volume_buy_open(String order_volume_buy_open) {
        this.order_volume_buy_open = order_volume_buy_open;
    }

    public String getOrder_volume_buy_close() {
        return order_volume_buy_close;
    }

    public void setOrder_volume_buy_close(String order_volume_buy_close) {
        this.order_volume_buy_close = order_volume_buy_close;
    }

    public String getOrder_volume_sell_open() {
        return order_volume_sell_open;
    }

    public void setOrder_volume_sell_open(String order_volume_sell_open) {
        this.order_volume_sell_open = order_volume_sell_open;
    }

    public String getOrder_volume_sell_close() {
        return order_volume_sell_close;
    }

    public void setOrder_volume_sell_close(String order_volume_sell_close) {
        this.order_volume_sell_close = order_volume_sell_close;
    }

    public String getMargin_long() {
        return margin_long;
    }

    public void setMargin_long(String margin_long) {
        this.margin_long = margin_long;
    }

    public String getMargin_short() {
        return margin_short;
    }

    public void setMargin_short(String margin_short) {
        this.margin_short = margin_short;
    }

    @Override
    public int compareTo(@NonNull PositionEntity o) {
        return this.getInstrument_id().compareToIgnoreCase(o.getInstrument_id());
    }

    @Override
    public boolean equals(Object obj) {

        if (obj instanceof PositionEntity) {
            PositionEntity positionEntity = (PositionEntity) obj;
            if (this.key.equals(positionEntity.key)
                    && this.volume_long_frozen.equals(positionEntity.volume_long_frozen)
                    && this.volume_long_his.equals(positionEntity.volume_long_his)
                    && this.volume_long_today.equals(positionEntity.volume_long_today)
                    && this.open_price_long.equals(positionEntity.open_price_long)
                    && this.open_cost_long.equals(positionEntity.open_cost_long)
                    && this.float_profit_long.equals(positionEntity.float_profit_long)
                    && this.volume_short_frozen.equals(positionEntity.volume_short_frozen)
                    && this.volume_short_his.equals(positionEntity.volume_short_his)
                    && this.volume_short_today.equals(positionEntity.volume_short_today)
                    && this.open_price_short.equals(positionEntity.open_price_short)
                    && this.open_cost_short.equals(positionEntity.open_cost_short)
                    && this.float_profit_short.equals(positionEntity.float_profit_short))
                return true;
        }
        return false;
    }
}

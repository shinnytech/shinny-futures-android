package com.shinnytech.futures.model.bean.conditionorderbean;

import java.io.Serializable;

public class COrderEntity implements Serializable {
    private static final long serialVersionUID = 2631590509760908291L;
    private String exchange_id;
    private String instrument_id;
    private String direction;
    private String offset;
    private String volume_type;
    private String volume;
    private String price_type;
    private String limit_price;

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

    public String getVolume_type() {
        return volume_type;
    }

    public void setVolume_type(String volume_type) {
        this.volume_type = volume_type;
    }

    public String getVolume() {
        return volume;
    }

    public void setVolume(String volume) {
        this.volume = volume;
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
}

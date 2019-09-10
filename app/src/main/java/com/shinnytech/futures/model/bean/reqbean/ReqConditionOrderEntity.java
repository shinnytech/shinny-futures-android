package com.shinnytech.futures.model.bean.reqbean;

public class ReqConditionOrderEntity {
    private String exchange_id;
    private String instrument_id;
    private String direction;
    private String offset;
    private boolean close_today_prior;
    private String volume_type;
    private int volume;
    private String price_type;
    private float limit_price;

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

    public boolean getClose_today_prior() {
        return close_today_prior;
    }

    public void setClose_today_prior(boolean close_today_prior) {
        this.close_today_prior = close_today_prior;
    }

    public String getVolume_type() {
        return volume_type;
    }

    public void setVolume_type(String volume_type) {
        this.volume_type = volume_type;
    }

    public int getVolume() {
        return volume;
    }

    public void setVolume(int volume) {
        this.volume = volume;
    }

    public String getPrice_type() {
        return price_type;
    }

    public void setPrice_type(String price_type) {
        this.price_type = price_type;
    }

    public float getLimit_price() {
        return limit_price;
    }

    public void setLimit_price(float limit_price) {
        this.limit_price = limit_price;
    }
}

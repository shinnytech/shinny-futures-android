package com.shinnytech.futures.model.bean.reqbean;

public class ReqInsertOrderEntity {
    private String aid;
    private String user_id;
    private String order_id;
    private String exchange_id;
    private String instrument_id;
    private String direction;
    private String offset;
    private int volume;
    private String price_type;
    private double limit_price;
    private String volume_condition;
    private String time_condition;

    public String getAid() {
        return aid;
    }

    public void setAid(String aid) {
        this.aid = aid;
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

    public double getLimit_price() {
        return limit_price;
    }

    public void setLimit_price(double limit_price) {
        this.limit_price = limit_price;
    }

    public String getVolume_condition() {
        return volume_condition;
    }

    public void setVolume_condition(String volume_condition) {
        this.volume_condition = volume_condition;
    }

    public String getTime_condition() {
        return time_condition;
    }

    public void setTime_condition(String time_condition) {
        this.time_condition = time_condition;
    }
}

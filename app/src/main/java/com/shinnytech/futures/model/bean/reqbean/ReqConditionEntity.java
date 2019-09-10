package com.shinnytech.futures.model.bean.reqbean;

public class ReqConditionEntity {
    private String contingent_type;
    private String exchange_id;
    private String instrument_id;
    private long contingent_time;
    private float contingent_price;
    private String price_relation;
    private float contingent_price_range_left;
    private float contingent_price_range_right;
    private float break_even_price;
    private String break_even_direction;

    public String getContingent_type() {
        return contingent_type;
    }

    public void setContingent_type(String contingent_type) {
        this.contingent_type = contingent_type;
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

    public long getContingent_time() {
        return contingent_time;
    }

    public void setContingent_time(long contingent_time) {
        this.contingent_time = contingent_time;
    }

    public float getContingent_price() {
        return contingent_price;
    }

    public void setContingent_price(float contingent_price) {
        this.contingent_price = contingent_price;
    }

    public String getPrice_relation() {
        return price_relation;
    }

    public void setPrice_relation(String price_relation) {
        this.price_relation = price_relation;
    }

    public float getContingent_price_range_left() {
        return contingent_price_range_left;
    }

    public void setContingent_price_range_left(float contingent_price_range_left) {
        this.contingent_price_range_left = contingent_price_range_left;
    }

    public float getContingent_price_range_right() {
        return contingent_price_range_right;
    }

    public void setContingent_price_range_right(float contingent_price_range_right) {
        this.contingent_price_range_right = contingent_price_range_right;
    }

    public float getBreak_even_price() {
        return break_even_price;
    }

    public void setBreak_even_price(float break_even_price) {
        this.break_even_price = break_even_price;
    }

    public String getBreak_even_direction() {
        return break_even_direction;
    }

    public void setBreak_even_direction(String break_even_direction) {
        this.break_even_direction = break_even_direction;
    }

}

package com.shinnytech.futures.model.bean.conditionorderbean;

import java.io.Serializable;

public class ConditionEntity implements Serializable {
    private static final long serialVersionUID = 2631590509760908290L;
    private String contingent_type;
    private String exchange_id;
    private String instrument_id;
    private String is_touched;
    private String contingent_price;
    private String price_relation;
    private String contingent_time;
    private String contingent_price_range_left;
    private String contingent_price_range_right;
    private String break_even_price;
    private String m_has_break_event;
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

    public String getIs_touched() {
        return is_touched;
    }

    public void setIs_touched(String is_touched) {
        this.is_touched = is_touched;
    }

    public String getContingent_price() {
        return contingent_price;
    }

    public void setContingent_price(String contingent_price) {
        this.contingent_price = contingent_price;
    }

    public String getPrice_relation() {
        return price_relation;
    }

    public void setPrice_relation(String price_relation) {
        this.price_relation = price_relation;
    }

    public String getContingent_time() {
        return contingent_time;
    }

    public void setContingent_time(String contingent_time) {
        this.contingent_time = contingent_time;
    }

    public String getContingent_price_range_left() {
        return contingent_price_range_left;
    }

    public void setContingent_price_range_left(String contingent_price_range_left) {
        this.contingent_price_range_left = contingent_price_range_left;
    }

    public String getContingent_price_range_right() {
        return contingent_price_range_right;
    }

    public void setContingent_price_range_right(String contingent_price_range_right) {
        this.contingent_price_range_right = contingent_price_range_right;
    }

    public String getBreak_even_price() {
        return break_even_price;
    }

    public void setBreak_even_price(String break_even_price) {
        this.break_even_price = break_even_price;
    }

    public String getM_has_break_event() {
        return m_has_break_event;
    }

    public void setM_has_break_event(String m_has_break_event) {
        this.m_has_break_event = m_has_break_event;
    }

    public String getBreak_even_direction() {
        return break_even_direction;
    }

    public void setBreak_even_direction(String break_even_direction) {
        this.break_even_direction = break_even_direction;
    }
}

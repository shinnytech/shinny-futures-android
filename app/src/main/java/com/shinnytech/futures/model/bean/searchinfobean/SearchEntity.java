package com.shinnytech.futures.model.bean.searchinfobean;

import android.support.annotation.NonNull;

import com.shinnytech.futures.utils.LogUtils;

/**
 * Created on 6/20/17.
 * Created by chenli.
 * Description: .
 */

public class SearchEntity implements Comparable<SearchEntity>{
    private String instrumentName = "";
    private String instrumentId = "";
    private String exchangeName = "";
    private String exchangeId = "";
    private String py = "";
    private String pTick = "";
    private String pTick_decs = "";
    private String vm = "";
    private String sort_key = "";
    private String margin = "";
    private String underlying_symbol = "";
    private boolean expired = false;
    private int pre_volume = 0;
    private String leg1_symbol;
    private String leg2_symbol;
    private String product_id;
    private String ins_id;

    public String getLeg1_symbol() {
        return leg1_symbol;
    }

    public void setLeg1_symbol(String leg1_symbol) {
        this.leg1_symbol = leg1_symbol;
    }

    public String getLeg2_symbol() {
        return leg2_symbol;
    }

    public void setLeg2_symbol(String leg2_symbol) {
        this.leg2_symbol = leg2_symbol;
    }

    public String getpTick_decs() {
        return pTick_decs;
    }

    public void setpTick_decs(String pTick_decs) {
        this.pTick_decs = pTick_decs;
    }

    public String getUnderlying_symbol() {
        return underlying_symbol;
    }

    public void setUnderlying_symbol(String underlying_symbol) {
        this.underlying_symbol = underlying_symbol;
    }

    public String getMargin() {
        return margin;
    }

    public void setMargin(String margin) {
        this.margin = margin;
    }

    public String getSort_key() {
        return sort_key;
    }

    public void setSort_key(String sort_key) {
        this.sort_key = sort_key;
    }

    public String getVm() {
        return vm;
    }

    public void setVm(String vm) {
        this.vm = vm;
    }

    public String getpTick() {
        return pTick;
    }

    public void setpTick(String pTick) {
        this.pTick = pTick;
    }

    public String getPy() {
        return py;
    }

    public void setPy(String py) {
        this.py = py;
    }

    public String getInstrumentName() {
        return instrumentName;
    }

    public void setInstrumentName(String instrumentName) {
        this.instrumentName = instrumentName;
    }

    public String getInstrumentId() {
        return instrumentId;
    }

    public void setInstrumentId(String instrumentId) {
        this.instrumentId = instrumentId;
    }

    public String getExchangeName() {
        return exchangeName;
    }

    public void setExchangeName(String exchangeName) {
        this.exchangeName = exchangeName;
    }

    public String getExchangeId() {
        return exchangeId;
    }

    public void setExchangeId(String exchangeId) {
        this.exchangeId = exchangeId;
    }

    public boolean isExpired() {
        return expired;
    }

    public void setExpired(boolean expired) {
        this.expired = expired;
    }

    public int getPre_volume() {
        return pre_volume;
    }

    public void setPre_volume(int pre_volume) {
        this.pre_volume = pre_volume;
    }

    public String getProduct_id() {
        return product_id;
    }

    public void setProduct_id(String product_id) {
        this.product_id = product_id;
    }

    public String getIns_id() {
        return ins_id;
    }

    public void setIns_id(String ins_id) {
        this.ins_id = ins_id;
    }

    @Override
    public int compareTo(@NonNull SearchEntity o) {
        String product_id1 = this.product_id;
        String product_id2 = o.product_id;
        if (product_id1.length() != product_id2.length()){
            if (product_id1.isEmpty())return 1;
            else if (product_id2.isEmpty())return -1;
            return product_id1.length() - product_id2.length();
        }else {
            int key1 = product_id1.compareTo(product_id2);
            if (key1 == 0){
                int pre_volume1 = this.pre_volume;
                int pre_volume2 = o.pre_volume;
                int key2 = pre_volume2 - pre_volume1;
                if (key2 == 0){
                    String ins_id1 = this.ins_id;
                    String ins_id2 = o.ins_id;
                    return -ins_id1.compareTo(ins_id2);
                }
                return key2;
            }
            return key1;
        }
    }
}

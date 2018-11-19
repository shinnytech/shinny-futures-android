package com.shinnytech.futures.model.bean.searchinfobean;

import android.support.annotation.NonNull;

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

    @Override
    public int compareTo(@NonNull SearchEntity o) {
        String instrument_id1 = this.instrumentId;
        String instrument_id2 = o.instrumentId;
        int pre_volume1 = this.pre_volume;
        int pre_volume2 = o.pre_volume;
        if (pre_volume1 != pre_volume2) return pre_volume2 - pre_volume1;
        else return - instrument_id1.compareTo(instrument_id2);
    }
}

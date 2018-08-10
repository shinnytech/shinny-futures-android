package com.shinnytech.futures.model.bean.searchinfobean;

/**
 * Created on 6/20/17.
 * Created by chenli.
 * Description: .
 */

public class SearchEntity {
    private String instrumentName = "";
    private String instrumentId = "";
    private String exchangeName = "";
    private String exchangeId = "";
    private String py = "";
    private String pTick = "";
    private String vm = "";
    private String sort_key = "";
    private String margin = "";

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
}

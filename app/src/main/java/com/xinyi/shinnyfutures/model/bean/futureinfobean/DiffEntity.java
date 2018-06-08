package com.xinyi.shinnyfutures.model.bean.futureinfobean;


import java.util.HashMap;
import java.util.Map;

/**
 * Created by chenli on 12/27/16.
 */

public class DiffEntity {
    private String mdhis_more_data;
    private String ins_list;
    private Map<String, QuoteEntity> quotes = new HashMap<>(1600);
    private Map<String, Map<String, KlineEntity>> klines = new HashMap<>(160);
    private Map<String, TickEntity> ticks = new HashMap<>();
    private Map<String, ChartEntity> charts = new HashMap<>();

    public String getMdhis_more_data() {
        return mdhis_more_data;
    }

    public void setMdhis_more_data(String mdhis_more_data) {
        this.mdhis_more_data = mdhis_more_data;
    }

    public String getIns_list() {
        return ins_list;
    }

    public void setIns_list(String ins_list) {
        this.ins_list = ins_list;
    }

    public Map<String, QuoteEntity> getQuotes() {
        return quotes;
    }

    public void setQuotes(Map<String, QuoteEntity> quotes) {
        this.quotes = quotes;
    }

    public Map<String, Map<String, KlineEntity>> getKlines() {
        return klines;
    }

    public void setKlines(Map<String, Map<String, KlineEntity>> klines) {
        this.klines = klines;
    }

    public Map<String, TickEntity> getTicks() {
        return ticks;
    }

    public void setTicks(Map<String, TickEntity> ticks) {
        this.ticks = ticks;
    }

    public Map<String, ChartEntity> getCharts() {
        return charts;
    }

    public void setCharts(Map<String, ChartEntity> charts) {
        this.charts = charts;
    }

}



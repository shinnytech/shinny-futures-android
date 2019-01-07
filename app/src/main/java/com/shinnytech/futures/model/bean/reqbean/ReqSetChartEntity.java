package com.shinnytech.futures.model.bean.reqbean;

public class ReqSetChartEntity {
    private String aid;
    private String chart_id;
    private String ins_list;
    private long duration;
    private int trading_day_start;
    private long trading_day_count;

    public String getAid() {
        return aid;
    }

    public void setAid(String aid) {
        this.aid = aid;
    }

    public String getChart_id() {
        return chart_id;
    }

    public void setChart_id(String chart_id) {
        this.chart_id = chart_id;
    }

    public String getIns_list() {
        return ins_list;
    }

    public void setIns_list(String ins_list) {
        this.ins_list = ins_list;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public int getTrading_day_start() {
        return trading_day_start;
    }

    public void setTrading_day_start(int trading_day_start) {
        this.trading_day_start = trading_day_start;
    }

    public long getTrading_day_count() {
        return trading_day_count;
    }

    public void setTrading_day_count(long trading_day_count) {
        this.trading_day_count = trading_day_count;
    }
}

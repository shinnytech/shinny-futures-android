package com.shinnytech.futures.model.bean.reqbean;

public class ReqSetChartKlineEntity {
    private String aid;
    private String chart_id;
    private String ins_list;
    private long duration;
    private int view_width;

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

    public int getView_width() {
        return view_width;
    }

    public void setView_width(int view_width) {
        this.view_width = view_width;
    }
}

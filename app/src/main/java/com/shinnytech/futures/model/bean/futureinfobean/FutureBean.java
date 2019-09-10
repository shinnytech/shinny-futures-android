package com.shinnytech.futures.model.bean.futureinfobean;


/**
 * Created by chenli on 12/21/16.
 */

public class FutureBean {
    private String aid;
    private DiffEntity data = new DiffEntity();

    public DiffEntity getData() {
        return data;
    }

    public void setData(DiffEntity data) {
        this.data = data;
    }

    public String getAid() {
        return aid;
    }

    public void setAid(String aid) {
        this.aid = aid;
    }

}

package com.shinnytech.futures.model.bean.accountinfobean;

/**
 * Created on 7/13/17.
 * Created by chenli.
 * Description: .
 */

public class BrokerEntity {
    private String aid;
    private String[] brokers;
    private String settlement;

    public String getSettlement() {
        return settlement;
    }

    public void setSettlement(String settlement) {
        this.settlement = settlement;
    }

    public String getAid() {
        return aid;
    }

    public void setAid(String aid) {
        this.aid = aid;
    }

    public String[] getBrokers() {
        return brokers;
    }

    public void setBrokers(String[] brokers) {
        this.brokers = brokers;
    }


}

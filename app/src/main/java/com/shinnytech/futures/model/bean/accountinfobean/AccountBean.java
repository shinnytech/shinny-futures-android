package com.shinnytech.futures.model.bean.accountinfobean;


import java.util.HashMap;
import java.util.Map;

/**
 * Created by chenli on 5/11/17.
 */

public class AccountBean {
    private String aid;
    private Map<String, AccountEntity> account = new HashMap<>();
    private Map<String, OrderEntity> order = new HashMap<>();
    private Map<String, PositionEntity> position = new HashMap<>();
    private Map<String, TradeEntity> trade = new HashMap<>();

    public String getAid() {
        return aid;
    }

    public void setAid(String aid) {
        this.aid = aid;
    }

    public Map<String, AccountEntity> getAccount() {
        return account;
    }

    public void setAccount(Map<String, AccountEntity> account) {
        this.account = account;
    }

    public Map<String, TradeEntity> getTrade() {
        return trade;
    }

    public void setTrade(Map<String, TradeEntity> trade) {
        this.trade = trade;
    }

    public Map<String, OrderEntity> getOrder() {
        return order;
    }

    public void setOrder(Map<String, OrderEntity> order) {
        this.order = order;
    }

    public Map<String, PositionEntity> getPosition() {
        return position;
    }

    public void setPosition(Map<String, PositionEntity> position) {
        this.position = position;
    }
}

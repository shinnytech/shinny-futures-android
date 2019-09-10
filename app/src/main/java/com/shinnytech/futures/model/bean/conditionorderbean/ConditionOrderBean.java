package com.shinnytech.futures.model.bean.conditionorderbean;

import java.util.HashMap;
import java.util.Map;

public class ConditionOrderBean {
    private String aid;
    private String user_id;
    private String trading_day;
    private Map<String, ConditionUserEntity> users = new HashMap<>();

    public String getAid() {
        return aid;
    }

    public void setAid(String aid) {
        this.aid = aid;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getTrading_day() {
        return trading_day;
    }

    public void setTrading_day(String trading_day) {
        this.trading_day = trading_day;
    }

    public Map<String, ConditionUserEntity> getUsers() {
        return users;
    }

    public void setUsers(Map<String, ConditionUserEntity> users) {
        this.users = users;
    }
}

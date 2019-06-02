package com.shinnytech.futures.model.bean.reqbean;

public class ReqLoginEntity {
    private String aid;
    private String bid;
    private String user_name;
    private String password;
    private String client_system_info;
    private String client_app_id;

    public String getAid() {
        return aid;
    }

    public void setAid(String aid) {
        this.aid = aid;
    }

    public String getBid() {
        return bid;
    }

    public void setBid(String bid) {
        this.bid = bid;
    }

    public String getUser_name() {
        return user_name;
    }

    public void setUser_name(String user_name) {
        this.user_name = user_name;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getClient_system_info() {
        return client_system_info;
    }

    public void setClient_system_info(String client_system_info) {
        this.client_system_info = client_system_info;
    }

    public String getClient_app_id() {
        return client_app_id;
    }

    public void setClient_app_id(String client_app_id) {
        this.client_app_id = client_app_id;
    }
}

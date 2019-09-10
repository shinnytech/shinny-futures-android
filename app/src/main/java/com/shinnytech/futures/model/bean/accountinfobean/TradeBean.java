package com.shinnytech.futures.model.bean.accountinfobean;


import java.util.HashMap;

/**
 * Created by chenli on 5/11/17.
 */

public class TradeBean {
    private HashMap<String, UserEntity> users = new HashMap<>();

    public HashMap<String, UserEntity> getUsers() {
        return users;
    }

    public void setUsers(HashMap<String, UserEntity> users) {
        this.users = users;
    }
}

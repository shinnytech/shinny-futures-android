package com.shinnytech.futures.model.bean.accountinfobean;

import java.util.HashMap;
import java.util.Map;

public class UserEntity {

    private String user_id;
    private Map<String, AccountEntity> accounts = new HashMap<>();
    private Map<String, OrderEntity> orders = new HashMap<>();
    private Map<String, PositionEntity> positions = new HashMap<>();
    private Map<String, TradeEntity> trades = new HashMap<>();
    private Map<String, BankEntity> banks = new HashMap<>();
    private Map<String, TransferEntity> transfers = new HashMap<>();

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public Map<String, AccountEntity> getAccounts() {
        return accounts;
    }

    public void setAccounts(Map<String, AccountEntity> accounts) {
        this.accounts = accounts;
    }

    public Map<String, OrderEntity> getOrders() {
        return orders;
    }

    public void setOrders(Map<String, OrderEntity> orders) {
        this.orders = orders;
    }

    public Map<String, PositionEntity> getPositions() {
        return positions;
    }

    public void setPositions(Map<String, PositionEntity> positions) {
        this.positions = positions;
    }

    public Map<String, TradeEntity> getTrades() {
        return trades;
    }

    public void setTrades(Map<String, TradeEntity> trades) {
        this.trades = trades;
    }

    public Map<String, BankEntity> getBanks() {
        return banks;
    }

    public void setBanks(Map<String, BankEntity> banks) {
        this.banks = banks;
    }

    public Map<String, TransferEntity> getTransfers() {
        return transfers;
    }

    public void setTransfers(Map<String, TransferEntity> transfers) {
        this.transfers = transfers;
    }
}

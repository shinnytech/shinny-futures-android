package com.shinnytech.futures.model.bean.reqbean;

public class ReqTransferEntity {
    private String aid;
    private String future_account;
    private String future_password;
    private String bank_id;
    private String bank_password;
    private String currency;
    private float amount;

    public String getAid() {
        return aid;
    }

    public void setAid(String aid) {
        this.aid = aid;
    }

    public String getFuture_account() {
        return future_account;
    }

    public void setFuture_account(String future_account) {
        this.future_account = future_account;
    }

    public String getFuture_password() {
        return future_password;
    }

    public void setFuture_password(String future_password) {
        this.future_password = future_password;
    }

    public String getBank_id() {
        return bank_id;
    }

    public void setBank_id(String bank_id) {
        this.bank_id = bank_id;
    }

    public String getBank_password() {
        return bank_password;
    }

    public void setBank_password(String bank_password) {
        this.bank_password = bank_password;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public float getAmount() {
        return amount;
    }

    public void setAmount(float amount) {
        this.amount = amount;
    }
}

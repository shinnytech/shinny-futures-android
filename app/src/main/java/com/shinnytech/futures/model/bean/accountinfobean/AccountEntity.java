package com.shinnytech.futures.model.bean.accountinfobean;

/**
 * Created on 6/16/17.
 * Created by chenli.
 * Description: .
 */

public class AccountEntity{
    private String key = "";
    private String user_id = "";
    private String currency = "";

    private String pre_balance = "";

    private String deposit = "";
    private String withdraw = "";
    private String close_profit = "";
    private String commission = "";
    private String premium = "";
    private String static_balance = "";

    private String position_profit = "";
    private String float_profit = "";

    private String balance = "";

    private String margin = "";
    private String frozen_margin = "";
    private String frozen_commission = "";
    private String frozen_premium = "";
    private String available = "";
    private String risk_ratio = "";

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getBalance() {
        return balance;
    }

    public void setBalance(String balance) {
        this.balance = balance;
    }

    public String getAvailable() {
        return available;
    }

    public void setAvailable(String available) {
        this.available = available;
    }

    public String getPre_balance() {
        return pre_balance;
    }

    public void setPre_balance(String pre_balance) {
        this.pre_balance = pre_balance;
    }

    public String getDeposit() {
        return deposit;
    }

    public void setDeposit(String deposit) {
        this.deposit = deposit;
    }

    public String getWithdraw() {
        return withdraw;
    }

    public void setWithdraw(String withdraw) {
        this.withdraw = withdraw;
    }

    public String getCommission() {
        return commission;
    }

    public void setCommission(String commission) {
        this.commission = commission;
    }

    public String getPremium() {
        return premium;
    }

    public void setPremium(String premium) {
        this.premium = premium;
    }

    public String getStatic_balance() {
        return static_balance;
    }

    public void setStatic_balance(String static_balance) {
        this.static_balance = static_balance;
    }

    public String getPosition_profit() {
        return position_profit;
    }

    public void setPosition_profit(String position_profit) {
        this.position_profit = position_profit;
    }

    public String getFloat_profit() {
        return float_profit;
    }

    public void setFloat_profit(String float_profit) {
        this.float_profit = float_profit;
    }

    public String getRisk_ratio() {
        return risk_ratio;
    }

    public void setRisk_ratio(String risk_ratio) {
        this.risk_ratio = risk_ratio;
    }

    public String getMargin() {
        return margin;
    }

    public void setMargin(String margin) {
        this.margin = margin;
    }

    public String getFrozen_margin() {
        return frozen_margin;
    }

    public void setFrozen_margin(String frozen_margin) {
        this.frozen_margin = frozen_margin;
    }

    public String getFrozen_commission() {
        return frozen_commission;
    }

    public void setFrozen_commission(String frozen_commission) {
        this.frozen_commission = frozen_commission;
    }

    public String getFrozen_premium() {
        return frozen_premium;
    }

    public void setFrozen_premium(String frozen_premium) {
        this.frozen_premium = frozen_premium;
    }

    public String getClose_profit() {
        return close_profit;
    }

    public void setClose_profit(String close_profit) {
        this.close_profit = close_profit;
    }
}

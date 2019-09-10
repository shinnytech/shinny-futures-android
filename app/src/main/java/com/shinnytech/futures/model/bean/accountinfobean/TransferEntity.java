package com.shinnytech.futures.model.bean.accountinfobean;

import androidx.annotation.NonNull;

import com.shinnytech.futures.utils.TimeUtils;

import java.io.Serializable;

public class TransferEntity implements Comparable<TransferEntity>, Serializable {
    private static final long serialVersionUID = 2631590509760908285L;
    private String key;
    private String datetime;
    private String currency;
    private String amount;
    private String error_id;
    private String error_msg;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getDatetime() {
        return datetime;
    }

    public void setDatetime(String datetime) {
        this.datetime = datetime;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getError_id() {
        return error_id;
    }

    public void setError_id(String error_id) {
        this.error_id = error_id;
    }

    public String getError_msg() {
        return error_msg;
    }

    public void setError_msg(String error_msg) {
        this.error_msg = error_msg;
    }

    @Override
    public int compareTo(@NonNull TransferEntity transferEntity) {
        long date1 = Long.parseLong(this.datetime) / 1000000;
        long date2 = Long.parseLong(transferEntity.datetime) / 1000000;
        if (!TimeUtils.isBetw2124(this.datetime)) date1 += 24 * 3600 * 1000;
        if (!TimeUtils.isBetw2124(transferEntity.datetime)) date2 += 24 * 3600 * 1000;
        return (int) -(date1 - date2);
    }

    @Override
    public boolean equals(Object obj) {

        if (obj instanceof TransferEntity) {
            TransferEntity transferEntity = (TransferEntity) obj;
            if (this.key.equals(transferEntity.key)) {
                return true;
            }
        }
        return false;
    }

}

package com.shinnytech.futures.model.bean.futureinfobean;


import java.io.Serializable;

/**
 * Created by chenli on 12/26/16.
 */
public class QuoteEntity implements Serializable {
    private static final long serialVersionUID = 2631590509760908280L;

    private String instrument_id;//
    private String datetime;//

    private String ask_price1;//
    private String ask_volume1;//
    private String bid_price1;//
    private String bid_volume1;//

    private String last_price;//

    private String highest;//
    private String lowest;//

    private String amount;//
    private String volume;//
    private String open_interest;//
    private String pre_open_interest;//

    private String pre_close;//
    private String open;//
    private String close;//

    private String lower_limit;//
    private String upper_limit;//
    private String average;//

    private String pre_settlement;//
    private String settlement;//

    private String status;//

    public String getInstrument_id() {
        return instrument_id;
    }

    public void setInstrument_id(String instrument_id) {
        this.instrument_id = instrument_id;
    }

    public String getDatetime() {
        return datetime;
    }

    public void setDatetime(String datetime) {
        this.datetime = datetime;
    }

    public String getAsk_price1() {
        return ask_price1;
    }

    public void setAsk_price1(String ask_price1) {
        this.ask_price1 = ask_price1;
    }

    public String getAsk_volume1() {
        return ask_volume1;
    }

    public void setAsk_volume1(String ask_volume1) {
        this.ask_volume1 = ask_volume1;
    }

    public String getBid_price1() {
        return bid_price1;
    }

    public void setBid_price1(String bid_price1) {
        this.bid_price1 = bid_price1;
    }

    public String getBid_volume1() {
        return bid_volume1;
    }

    public void setBid_volume1(String bid_volume1) {
        this.bid_volume1 = bid_volume1;
    }

    public String getLast_price() {
        return last_price;
    }

    public void setLast_price(String last_price) {
        this.last_price = last_price;
    }

    public String getHighest() {
        return highest;
    }

    public void setHighest(String highest) {
        this.highest = highest;
    }

    public String getLowest() {
        return lowest;
    }

    public void setLowest(String lowest) {
        this.lowest = lowest;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getVolume() {
        return volume;
    }

    public void setVolume(String volume) {
        this.volume = volume;
    }

    public String getOpen_interest() {
        return open_interest;
    }

    public void setOpen_interest(String open_interest) {
        this.open_interest = open_interest;
    }

    public String getPre_open_interest() {
        return pre_open_interest;
    }

    public void setPre_open_interest(String pre_open_interest) {
        this.pre_open_interest = pre_open_interest;
    }

    public String getPre_close() {
        return pre_close;
    }

    public void setPre_close(String pre_close) {
        this.pre_close = pre_close;
    }

    public String getOpen() {
        return open;
    }

    public void setOpen(String open) {
        this.open = open;
    }

    public String getClose() {
        return close;
    }

    public void setClose(String close) {
        this.close = close;
    }

    public String getLower_limit() {
        return lower_limit;
    }

    public void setLower_limit(String lower_limit) {
        this.lower_limit = lower_limit;
    }

    public String getUpper_limit() {
        return upper_limit;
    }

    public void setUpper_limit(String upper_limit) {
        this.upper_limit = upper_limit;
    }

    public String getAverage() {
        return average;
    }

    public void setAverage(String average) {
        this.average = average;
    }

    public String getPre_settlement() {
        return pre_settlement;
    }

    public void setPre_settlement(String pre_settlement) {
        this.pre_settlement = pre_settlement;
    }

    public String getSettlement() {
        return settlement;
    }

    public void setSettlement(String settlement) {
        this.settlement = settlement;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public boolean equals(Object obj) {

        if (obj instanceof QuoteEntity) {
            QuoteEntity quoteEntity = (QuoteEntity) obj;
            if (this.last_price == null && quoteEntity.last_price == null
                    && this.pre_settlement == null && quoteEntity.pre_settlement == null
                    && this.volume == null && quoteEntity.volume == null
                    && this.upper_limit == null && quoteEntity.upper_limit == null
                    && this.lower_limit == null && quoteEntity.lower_limit == null
                    && this.ask_price1 == null && quoteEntity.ask_price1 == null
                    && this.bid_price1 == null && quoteEntity.bid_price1 == null
                    && this.ask_volume1 == null && quoteEntity.ask_volume1 == null
                    && this.bid_volume1 == null && quoteEntity.bid_volume1 == null
                    && this.open_interest == null && quoteEntity.open_interest == null) return true;
            else if (this.last_price != null && quoteEntity.last_price != null
                    && this.pre_settlement != null && quoteEntity.pre_settlement != null
                    && this.volume != null && quoteEntity.volume != null
                    && this.upper_limit != null && quoteEntity.upper_limit != null
                    && this.lower_limit != null && quoteEntity.lower_limit != null
                    && this.ask_price1 != null && quoteEntity.ask_price1 != null
                    && this.bid_price1 != null && quoteEntity.bid_price1 != null
                    && this.ask_volume1 != null && quoteEntity.ask_volume1 != null
                    && this.bid_volume1 != null && quoteEntity.bid_volume1 != null
                    && this.open_interest != null && quoteEntity.open_interest != null) {
                if (this.last_price.equals(quoteEntity.last_price)
                        && this.pre_settlement.equals(quoteEntity.pre_settlement)
                        && this.volume.equals(quoteEntity.volume)
                        && this.upper_limit.equals(quoteEntity.upper_limit)
                        && this.lower_limit.equals(quoteEntity.lower_limit)
                        && this.ask_volume1.equals(quoteEntity.ask_volume1)
                        && this.bid_volume1.equals(quoteEntity.bid_volume1)
                        && this.ask_price1.equals(quoteEntity.ask_price1)
                        && this.bid_price1.equals(quoteEntity.bid_price1)
                        && this.open_interest.equals(quoteEntity.open_interest)) return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        String msg = "instrument_id--->" + this.instrument_id + "last--->" + this.last_price + "pre_settlement--->" + this.pre_settlement + "open_interest--->" + this.open_interest;
        return msg;
    }
}

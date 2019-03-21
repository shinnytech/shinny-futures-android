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

    private String ask_price2;//
    private String ask_volume2;//
    private String bid_price2;//
    private String bid_volume2;//

    private String ask_price3;//
    private String ask_volume3;//
    private String bid_price3;//
    private String bid_volume3;//

    private String ask_price4;//
    private String ask_volume4;//
    private String bid_price4;//
    private String bid_volume4;//

    private String ask_price5;//
    private String ask_volume5;//
    private String bid_price5;//
    private String bid_volume5;//

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

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

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

    public String getAsk_price2() {
        return ask_price2;
    }

    public void setAsk_price2(String ask_price2) {
        this.ask_price2 = ask_price2;
    }

    public String getAsk_volume2() {
        return ask_volume2;
    }

    public void setAsk_volume2(String ask_volume2) {
        this.ask_volume2 = ask_volume2;
    }

    public String getBid_price2() {
        return bid_price2;
    }

    public void setBid_price2(String bid_price2) {
        this.bid_price2 = bid_price2;
    }

    public String getBid_volume2() {
        return bid_volume2;
    }

    public void setBid_volume2(String bid_volume2) {
        this.bid_volume2 = bid_volume2;
    }

    public String getAsk_price3() {
        return ask_price3;
    }

    public void setAsk_price3(String ask_price3) {
        this.ask_price3 = ask_price3;
    }

    public String getAsk_volume3() {
        return ask_volume3;
    }

    public void setAsk_volume3(String ask_volume3) {
        this.ask_volume3 = ask_volume3;
    }

    public String getBid_price3() {
        return bid_price3;
    }

    public void setBid_price3(String bid_price3) {
        this.bid_price3 = bid_price3;
    }

    public String getBid_volume3() {
        return bid_volume3;
    }

    public void setBid_volume3(String bid_volume3) {
        this.bid_volume3 = bid_volume3;
    }

    public String getAsk_price4() {
        return ask_price4;
    }

    public void setAsk_price4(String ask_price4) {
        this.ask_price4 = ask_price4;
    }

    public String getAsk_volume4() {
        return ask_volume4;
    }

    public void setAsk_volume4(String ask_volume4) {
        this.ask_volume4 = ask_volume4;
    }

    public String getBid_price4() {
        return bid_price4;
    }

    public void setBid_price4(String bid_price4) {
        this.bid_price4 = bid_price4;
    }

    public String getBid_volume4() {
        return bid_volume4;
    }

    public void setBid_volume4(String bid_volume4) {
        this.bid_volume4 = bid_volume4;
    }

    public String getAsk_price5() {
        return ask_price5;
    }

    public void setAsk_price5(String ask_price5) {
        this.ask_price5 = ask_price5;
    }

    public String getAsk_volume5() {
        return ask_volume5;
    }

    public void setAsk_volume5(String ask_volume5) {
        this.ask_volume5 = ask_volume5;
    }

    public String getBid_price5() {
        return bid_price5;
    }

    public void setBid_price5(String bid_price5) {
        this.bid_price5 = bid_price5;
    }

    public String getBid_volume5() {
        return bid_volume5;
    }

    public void setBid_volume5(String bid_volume5) {
        this.bid_volume5 = bid_volume5;
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
                    && this.ask_price2 == null && quoteEntity.ask_price2 == null
                    && this.bid_price2 == null && quoteEntity.bid_price2 == null
                    && this.ask_volume2 == null && quoteEntity.ask_volume2 == null
                    && this.bid_volume2 == null && quoteEntity.bid_volume2 == null
                    && this.ask_price3 == null && quoteEntity.ask_price3 == null
                    && this.bid_price3 == null && quoteEntity.bid_price3 == null
                    && this.ask_volume3 == null && quoteEntity.ask_volume3 == null
                    && this.bid_volume3 == null && quoteEntity.bid_volume3 == null
                    && this.ask_price4 == null && quoteEntity.ask_price4 == null
                    && this.bid_price4 == null && quoteEntity.bid_price4 == null
                    && this.ask_volume4 == null && quoteEntity.ask_volume4 == null
                    && this.bid_volume4 == null && quoteEntity.bid_volume4 == null
                    && this.ask_price5 == null && quoteEntity.ask_price5 == null
                    && this.bid_price5 == null && quoteEntity.bid_price5 == null
                    && this.ask_volume5 == null && quoteEntity.ask_volume5 == null
                    && this.bid_volume5 == null && quoteEntity.bid_volume5 == null
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
                    && this.ask_price2 != null && quoteEntity.ask_price2 != null
                    && this.bid_price2 != null && quoteEntity.bid_price2 != null
                    && this.ask_volume2 != null && quoteEntity.ask_volume2 != null
                    && this.bid_volume2 != null && quoteEntity.bid_volume2 != null
                    && this.ask_price3 != null && quoteEntity.ask_price3 != null
                    && this.bid_price3 != null && quoteEntity.bid_price3 != null
                    && this.ask_volume3 != null && quoteEntity.ask_volume3 != null
                    && this.bid_volume3 != null && quoteEntity.bid_volume3 != null
                    && this.ask_price4 != null && quoteEntity.ask_price4 != null
                    && this.bid_price4 != null && quoteEntity.bid_price4 != null
                    && this.ask_volume4 != null && quoteEntity.ask_volume4 != null
                    && this.bid_volume4 != null && quoteEntity.bid_volume4 != null
                    && this.ask_price5 != null && quoteEntity.ask_price5 != null
                    && this.bid_price5 != null && quoteEntity.bid_price5 != null
                    && this.ask_volume5 != null && quoteEntity.ask_volume5 != null
                    && this.bid_volume5 != null && quoteEntity.bid_volume5 != null
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
                        && this.ask_volume2.equals(quoteEntity.ask_volume2)
                        && this.bid_volume2.equals(quoteEntity.bid_volume2)
                        && this.ask_price2.equals(quoteEntity.ask_price2)
                        && this.bid_price2.equals(quoteEntity.bid_price2)
                        && this.ask_volume3.equals(quoteEntity.ask_volume3)
                        && this.bid_volume3.equals(quoteEntity.bid_volume3)
                        && this.ask_price3.equals(quoteEntity.ask_price3)
                        && this.bid_price3.equals(quoteEntity.bid_price3)
                        && this.ask_volume4.equals(quoteEntity.ask_volume4)
                        && this.bid_volume4.equals(quoteEntity.bid_volume4)
                        && this.ask_price4.equals(quoteEntity.ask_price4)
                        && this.bid_price4.equals(quoteEntity.bid_price4)
                        && this.ask_volume5.equals(quoteEntity.ask_volume5)
                        && this.bid_volume5.equals(quoteEntity.bid_volume5)
                        && this.ask_price5.equals(quoteEntity.ask_price5)
                        && this.bid_price5.equals(quoteEntity.bid_price5)
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

package com.xinyi.shinnyfutures.model.bean.futureinfobean;


import java.util.HashMap;
import java.util.Map;

/**
 * Created by chenli on 12/26/16.
 */
public class TickEntity {
    private String last_id;
    private Map<String, Map<String, DataEntity>> data = new HashMap<>();

    public Map<String, Map<String, DataEntity>> getData() {
        return data;
    }

    public void setData(Map<String, Map<String, DataEntity>> data) {
        this.data = data;
    }

    public String getLast_id() {
        return last_id;
    }

    public void setLast_id(String last_id) {
        this.last_id = last_id;
    }

    public static class DataEntity {
        private String datetime;
        private String trading_day;
        private String last_price;
        private String highest;
        private String lowest;
        private String bid_price1;
        private String ask_price1;
        private String bid_volume1;
        private String ask_volume1;
        private String volume;
        private String open_interest;

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

        public String getDatetime() {
            return datetime;
        }

        public void setDatetime(String datetime) {
            this.datetime = datetime;
        }

        public String getHighest() {
            return highest;
        }

        public void setHighest(String highest) {
            this.highest = highest;
        }

        public String getLast_price() {
            return last_price;
        }

        public void setLast_price(String last_price) {
            this.last_price = last_price;
        }

        public String getLowest() {
            return lowest;
        }

        public void setLowest(String lowest) {
            this.lowest = lowest;
        }

        public String getOpen_interest() {
            return open_interest;
        }

        public void setOpen_interest(String open_interest) {
            this.open_interest = open_interest;
        }

        public String getTrading_day() {
            return trading_day;
        }

        public void setTrading_day(String trading_day) {
            this.trading_day = trading_day;
        }

        public String getVolume() {
            return volume;
        }

        public void setVolume(String volume) {
            this.volume = volume;
        }
    }
}

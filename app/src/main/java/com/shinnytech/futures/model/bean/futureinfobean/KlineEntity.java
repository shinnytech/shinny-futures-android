package com.shinnytech.futures.model.bean.futureinfobean;


import android.support.annotation.NonNull;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by chenli on 12/26/16.
 */
public class KlineEntity {
    private String last_id;
    private String trading_day_start_id;
    private String trading_day_end_id;
    private Map<String, DataEntity> data = new HashMap<>();
    private Map<String, BindingEntity> binding = new HashMap<>();

    public Map<String, DataEntity> getData() {
        return data;
    }

    public void setData(Map<String, DataEntity> data) {
        this.data = data;
    }

    public String getLast_id() {
        return last_id;
    }

    public void setLast_id(String last_id) {
        this.last_id = last_id;
    }

    public String getTrading_day_start_id() {
        return trading_day_start_id;
    }

    public void setTrading_day_start_id(String trading_day_start_id) {
        this.trading_day_start_id = trading_day_start_id;
    }

    public String getTrading_day_end_id() {
        return trading_day_end_id;
    }

    public void setTrading_day_end_id(String trading_day_end_id) {
        this.trading_day_end_id = trading_day_end_id;
    }

    public Map<String, BindingEntity> getBinding() {
        return binding;
    }

    public void setBinding(Map<String, BindingEntity> binding) {
        this.binding = binding;
    }

    public static class DataEntity implements Comparable<DataEntity> {
        private String datetime;
        private String open;
        private String high;
        private String low;
        private String close;
        private String volume;
        private String open_oi;
        private String close_oi;

        public String getClose() {
            return close;
        }

        public void setClose(String close) {
            this.close = close;
        }

        public String getClose_oi() {
            return close_oi;
        }

        public void setClose_oi(String close_oi) {
            this.close_oi = close_oi;
        }

        public String getDatetime() {
            return datetime;
        }

        public void setDatetime(String datetime) {
            this.datetime = datetime;
        }

        public String getHigh() {
            return high;
        }

        public void setHigh(String high) {
            this.high = high;
        }

        public String getLow() {
            return low;
        }

        public void setLow(String low) {
            this.low = low;
        }

        public String getOpen() {
            return open;
        }

        public void setOpen(String open) {
            this.open = open;
        }

        public String getOpen_oi() {
            return open_oi;
        }

        public void setOpen_oi(String open_oi) {
            this.open_oi = open_oi;
        }

        public String getVolume() {
            return volume;
        }

        public void setVolume(String volume) {
            this.volume = volume;
        }

        @Override
        public int compareTo(@NonNull DataEntity o) {
            return this.getDatetime().compareTo(o.getDatetime());
        }
    }

    public static class BindingEntity {
        private Map<String, String> bindingData = new HashMap<>();

        public Map<String, String> getBindingData() {
            return bindingData;
        }

        public void setBindingData(Map<String, String> bindingData) {
            this.bindingData = bindingData;
        }
    }

    @Override
    public String toString() {
        return "last_id"+last_id+"\n"+"trading_day_start_id"+trading_day_start_id+"\n"+
                "trading_day_end_id"+trading_day_end_id;
    }
}

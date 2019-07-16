package com.shinnytech.futures.model.bean.eventbusbean;

/**
 * Created on 7/5/17.
 * Created by chenli.
 * Description: .
 */

public class KlineEvent {
    private String klineType;

    private String fragmentType;

    private String xValuesType;

    public String getxValuesType() {
        return xValuesType;
    }

    public void setxValuesType(String xValuesType) {
        this.xValuesType = xValuesType;
    }

    public String getKlineType() {
        return klineType;
    }

    public void setKlineType(String klineType) {
        this.klineType = klineType;
    }

    public String getFragmentType() {
        return fragmentType;
    }

    public void setFragmentType(String fragmentType) {
        this.fragmentType = fragmentType;
    }
}

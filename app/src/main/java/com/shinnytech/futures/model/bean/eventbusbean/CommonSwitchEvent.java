package com.shinnytech.futures.model.bean.eventbusbean;

/**
 * Created on 7/6/17.
 * Created by chenli.
 * Description: .
 */

public class CommonSwitchEvent {
    private boolean isPosition;
    private boolean isPending;
    private boolean isAverage;
    private boolean isMD5;

    public boolean isPosition() {
        return isPosition;
    }

    public void setPosition(boolean position) {
        isPosition = position;
    }

    public boolean isPending() {
        return isPending;
    }

    public void setPending(boolean pending) {
        isPending = pending;
    }

    public boolean isAverage() {
        return isAverage;
    }

    public void setAverage(boolean average) {
        isAverage = average;
    }

    public boolean isMD5() {
        return isMD5;
    }

    public void setMD5(boolean MD5) {
        isMD5 = MD5;
    }
}

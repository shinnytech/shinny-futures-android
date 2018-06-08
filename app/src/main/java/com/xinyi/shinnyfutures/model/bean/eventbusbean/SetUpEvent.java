package com.xinyi.shinnyfutures.model.bean.eventbusbean;

/**
 * Created on 7/6/17.
 * Created by chenli.
 * Description: .
 */

public class SetUpEvent {
    private boolean isPosition;
    private boolean isPending;
    private boolean isAverage;

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
}

package com.shinnytech.futures.model.bean.eventbusbean;

/**
 * Created on 7/5/17.
 * Created by chenli.
 * Description: .
 */

public class OrderSettingEvent {
    private boolean isPopup;

    public boolean isPopup() {
        return isPopup;
    }

    public void setPopup(boolean popup) {
        isPopup = popup;
    }
}

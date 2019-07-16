package com.shinnytech.futures.model.bean.eventbusbean;

/**
 * Created on 7/5/17.
 * Created by chenli.
 * Description: .
 */

public class OrderSettingEvent {
    private boolean isCancelPopup;
    private boolean isInsertPopup;

    public boolean isCancelPopup() {
        return isCancelPopup;
    }

    public void setCancelPopup(boolean cancelPopup) {
        isCancelPopup = cancelPopup;
    }

    public boolean isInsertPopup() {
        return isInsertPopup;
    }

    public void setInsertPopup(boolean insertPopup) {
        isInsertPopup = insertPopup;
    }
}

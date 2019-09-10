package com.shinnytech.futures.model.bean.eventbusbean;

/**
 * Created on 7/5/17.
 * Created by chenli.
 * Description: .
 */

public class CancelOrderEvent {
    private boolean isCancelPopup;

    public boolean isCancelPopup() {
        return isCancelPopup;
    }

    public void setCancelPopup(boolean cancelPopup) {
        isCancelPopup = cancelPopup;
    }

}

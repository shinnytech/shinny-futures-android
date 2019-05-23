package com.shinnytech.futures.model.bean.eventbusbean;

/**
 * Created on 7/5/17.
 * Created by chenli.
 * Description: .
 */

public class IdEvent {
    private String position_direction;
    private String instrument_id;

    public String getInstrument_id() {
        return instrument_id;
    }

    public void setInstrument_id(String instrument_id) {
        this.instrument_id = instrument_id;
    }

    public String getPosition_direction() {
        return position_direction;
    }

    public void setPosition_direction(String position_direction) {
        this.position_direction = position_direction;
    }
}

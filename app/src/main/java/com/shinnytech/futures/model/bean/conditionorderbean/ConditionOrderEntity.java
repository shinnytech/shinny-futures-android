package com.shinnytech.futures.model.bean.conditionorderbean;

import java.io.Serializable;
import java.util.List;

public class ConditionOrderEntity implements Comparable<ConditionOrderEntity>, Serializable {
    private static final long serialVersionUID = 2631590509760908292L;
    private String order_id;
    private String trading_day;
    private String insert_date_time;
    private List<ConditionEntity> condition_list;
    private String conditions_logic_oper;
    private List<COrderEntity> order_list;
    private String time_condition_type;
    private String GTD_date;
    private String is_cancel_ori_close_order;
    private String status;
    private String touched_time;

    public String getOrder_id() {
        return order_id;
    }

    public void setOrder_id(String order_id) {
        this.order_id = order_id;
    }

    public String getTrading_day() {
        return trading_day;
    }

    public void setTrading_day(String trading_day) {
        this.trading_day = trading_day;
    }

    public String getInsert_date_time() {
        return insert_date_time;
    }

    public void setInsert_date_time(String insert_date_time) {
        this.insert_date_time = insert_date_time;
    }

    public List<ConditionEntity> getCondition_list() {
        return condition_list;
    }

    public void setCondition_list(List<ConditionEntity> condition_list) {
        this.condition_list = condition_list;
    }

    public String getConditions_logic_oper() {
        return conditions_logic_oper;
    }

    public void setConditions_logic_oper(String conditions_logic_oper) {
        this.conditions_logic_oper = conditions_logic_oper;
    }

    public List<COrderEntity> getOrder_list() {
        return order_list;
    }

    public void setOrder_list(List<COrderEntity> order_list) {
        this.order_list = order_list;
    }

    public String getTime_condition_type() {
        return time_condition_type;
    }

    public void setTime_condition_type(String time_condition_type) {
        this.time_condition_type = time_condition_type;
    }

    public String getGTD_date() {
        return GTD_date;
    }

    public void setGTD_date(String GTD_date) {
        this.GTD_date = GTD_date;
    }

    public String getIs_cancel_ori_close_order() {
        return is_cancel_ori_close_order;
    }

    public void setIs_cancel_ori_close_order(String is_cancel_ori_close_order) {
        this.is_cancel_ori_close_order = is_cancel_ori_close_order;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getTouched_time() {
        return touched_time;
    }

    public void setTouched_time(String touched_time) {
        this.touched_time = touched_time;
    }

    @Override
    public boolean equals(Object obj) {

        if (obj instanceof ConditionOrderEntity) {
            ConditionOrderEntity conditionOrderEntity = (ConditionOrderEntity) obj;
            if (this.order_id.equals(conditionOrderEntity.order_id) &&
                    this.status.equals(conditionOrderEntity.status)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int compareTo(ConditionOrderEntity o) {
        long date1 = Long.parseLong(this.insert_date_time);
        long date2 = Long.parseLong(o.insert_date_time);
        return (int) -(date1 - date2);
    }
}

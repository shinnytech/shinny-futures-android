package com.shinnytech.futures.model.bean.reqbean;

import com.alibaba.fastjson.annotation.JSONField;

public class ReqInsertConditionOrderEntity {
    private String aid;
    private String user_id;
    private String order_id;
    private ReqConditionEntity[] condition_list;
    private String conditions_logic_oper;
    private ReqConditionOrderEntity[] order_list;
    private String time_condition_type;
    @JSONField(name = "GTD_date")
    private int gtd_date;
    private boolean is_cancel_ori_close_order;

    public String getAid() {
        return aid;
    }

    public void setAid(String aid) {
        this.aid = aid;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getOrder_id() {
        return order_id;
    }

    public void setOrder_id(String order_id) {
        this.order_id = order_id;
    }

    public ReqConditionEntity[] getCondition_list() {
        return condition_list;
    }

    public void setCondition_list(ReqConditionEntity[] condition_list) {
        this.condition_list = condition_list;
    }

    public String getConditions_logic_oper() {
        return conditions_logic_oper;
    }

    public void setConditions_logic_oper(String conditions_logic_oper) {
        this.conditions_logic_oper = conditions_logic_oper;
    }

    public ReqConditionOrderEntity[] getOrder_list() {
        return order_list;
    }

    public void setOrder_list(ReqConditionOrderEntity[] order_list) {
        this.order_list = order_list;
    }

    public String getTime_condition_type() {
        return time_condition_type;
    }

    public void setTime_condition_type(String time_condition_type) {
        this.time_condition_type = time_condition_type;
    }

    public int getGtd_date() {
        return gtd_date;
    }

    public void setGtd_date(int gtd_date) {
        this.gtd_date = gtd_date;
    }

    public boolean isIs_cancel_ori_close_order() {
        return is_cancel_ori_close_order;
    }

    public void setIs_cancel_ori_close_order(boolean is_cancel_ori_close_order) {
        this.is_cancel_ori_close_order = is_cancel_ori_close_order;
    }
}

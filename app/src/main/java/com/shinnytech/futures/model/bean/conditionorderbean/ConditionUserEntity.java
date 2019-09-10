package com.shinnytech.futures.model.bean.conditionorderbean;

import java.util.HashMap;
import java.util.Map;

public class ConditionUserEntity {
    private Map<String, ConditionOrderEntity> condition_orders = new HashMap<>();

    public Map<String, ConditionOrderEntity> getCondition_orders() {
        return condition_orders;
    }

    public void setCondition_orders(Map<String, ConditionOrderEntity> condition_orders) {
        this.condition_orders = condition_orders;
    }
}

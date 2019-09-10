package com.shinnytech.futures.model.listener;

import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DiffUtil;

import com.shinnytech.futures.model.bean.conditionorderbean.ConditionOrderEntity;
import com.shinnytech.futures.utils.TimeUtils;

import java.util.Date;
import java.util.List;

import static com.shinnytech.futures.constants.ConditionConstants.CONDITION_STATUS_CANCEL;
import static com.shinnytech.futures.constants.ConditionConstants.CONDITION_STATUS_CANCEL_TITLE;
import static com.shinnytech.futures.constants.ConditionConstants.CONDITION_STATUS_DISCARD;
import static com.shinnytech.futures.constants.ConditionConstants.CONDITION_STATUS_DISCARD_TITLE;
import static com.shinnytech.futures.constants.ConditionConstants.CONDITION_STATUS_LIVE;
import static com.shinnytech.futures.constants.ConditionConstants.CONDITION_STATUS_LIVE_TITLE;
import static com.shinnytech.futures.constants.ConditionConstants.CONDITION_STATUS_SUSPEND;
import static com.shinnytech.futures.constants.ConditionConstants.CONDITION_STATUS_SUSPEND_TITLE;
import static com.shinnytech.futures.constants.ConditionConstants.CONDITION_STATUS_TOUCHED;
import static com.shinnytech.futures.constants.ConditionConstants.CONDITION_STATUS_TOUCHED_TITLE;
import static com.shinnytech.futures.utils.TimeUtils.YMD_HMS_FORMAT_4;

/**
 * Created on 7/27/17.
 * Created by chenli.
 * Description: .
 */

public class ConditionOrderDiffCallback extends DiffUtil.Callback {

    private List<ConditionOrderEntity> mOldData;
    private List<ConditionOrderEntity> mNewData;

    public ConditionOrderDiffCallback(List<ConditionOrderEntity> oldData, List<ConditionOrderEntity> newData) {
        this.mOldData = oldData;
        this.mNewData = newData;
    }

    @Override
    public int getOldListSize() {
        return mOldData == null ? 0 : mOldData.size();
    }

    @Override
    public int getNewListSize() {
        return mNewData == null ? 0 : mNewData.size();
    }

    @Override
    public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
        ConditionOrderEntity oldData = mOldData.get(oldItemPosition);
        ConditionOrderEntity newData = mNewData.get(newItemPosition);
        if (oldData != null && newData != null)
            return oldData.getOrder_id().equals(newData.getOrder_id());
        return false;
    }

    @Override
    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
        return mOldData.get(oldItemPosition).equals(mNewData.get(newItemPosition));
    }


    @Nullable
    @Override
    public Object getChangePayload(int oldItemPosition, int newItemPosition) {
        Bundle bundle = new Bundle();

        try {
            String status_old = "";
            switch (mOldData.get(oldItemPosition).getStatus()){
                case CONDITION_STATUS_LIVE:
                    status_old = CONDITION_STATUS_LIVE_TITLE;
                    break;
                case CONDITION_STATUS_SUSPEND:
                    status_old = CONDITION_STATUS_SUSPEND_TITLE;
                    break;
                case CONDITION_STATUS_CANCEL:
                    status_old = CONDITION_STATUS_CANCEL_TITLE;
                    break;
                case CONDITION_STATUS_DISCARD:
                    status_old = CONDITION_STATUS_DISCARD_TITLE;
                    break;
                case CONDITION_STATUS_TOUCHED:
                    status_old = CONDITION_STATUS_TOUCHED_TITLE;
                    break;
                default:
                    break;
            }

            String status_new = "";
            switch (mNewData.get(newItemPosition).getStatus()){
                case CONDITION_STATUS_LIVE:
                    status_new = CONDITION_STATUS_LIVE_TITLE;
                    break;
                case CONDITION_STATUS_SUSPEND:
                    status_new = CONDITION_STATUS_SUSPEND_TITLE;
                    break;
                case CONDITION_STATUS_CANCEL:
                    status_new = CONDITION_STATUS_CANCEL_TITLE;
                    break;
                case CONDITION_STATUS_DISCARD:
                    status_new = CONDITION_STATUS_DISCARD_TITLE;
                    break;
                case CONDITION_STATUS_TOUCHED:
                    status_new = CONDITION_STATUS_TOUCHED_TITLE;
                    break;
                default:
                    break;
            }

            if (!status_old.equals(status_new)){
                String touched_time = "";
                try {
                    touched_time = TimeUtils.date2String(new Date(Long.parseLong(mNewData.
                            get(newItemPosition).getTouched_time()) * 1000), YMD_HMS_FORMAT_4);
                }catch (NumberFormatException e){
                    e.printStackTrace();
                }
                bundle.putString("status", status_new + " " + touched_time);

            }

            if (bundle.size() == 0) {
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return bundle;
    }


}

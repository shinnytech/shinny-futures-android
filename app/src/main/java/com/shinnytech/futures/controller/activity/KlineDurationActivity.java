package com.shinnytech.futures.controller.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Vibrator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.ItemTouchHelper;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;

import com.shinnytech.futures.R;
import com.shinnytech.futures.application.BaseApplication;
import com.shinnytech.futures.constants.CommonConstants;
import com.shinnytech.futures.constants.SettingConstants;
import com.shinnytech.futures.databinding.ActivityKlineDurationBinding;
import com.shinnytech.futures.model.adapter.KlineDurationAdapter;
import com.shinnytech.futures.model.bean.eventbusbean.UpdateDurationsEvent;
import com.shinnytech.futures.utils.SPUtils;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

public class KlineDurationActivity extends BaseActivity {

    private ActivityKlineDurationBinding mBinding;
    private KlineDurationAdapter mKlineDurationAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mLayoutID = R.layout.activity_kline_duration;
        mTitle = SettingConstants.KLINE_DURATION_SETTING;
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void initData() {
        mBinding = (ActivityKlineDurationBinding) mViewDataBinding;
        String duration = (String) SPUtils.get(BaseApplication.getContext(), SettingConstants.CONFIG_KLINE_DURATION_DEFAULT, "");
        String[] durations = duration.split(",");
        List<String> list = new ArrayList<>();
        for (String data : durations) {
            list.add(data);
        }
        mKlineDurationAdapter = new KlineDurationAdapter(this, list);
        mBinding.durationRv.setLayoutManager(new LinearLayoutManager(this));
        mBinding.durationRv.setAdapter(mKlineDurationAdapter);

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new ItemTouchHelper.Callback() {
            @Override
            public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
                int swipeFlag = 0;
                int dragFlag = ItemTouchHelper.UP | ItemTouchHelper.DOWN;
                return makeMovementFlags(dragFlag, swipeFlag);
            }


            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                mKlineDurationAdapter.onItemMove(viewHolder.getAdapterPosition(), target.getAdapterPosition());
                return true;
            }

            @Override
            public void onSelectedChanged(RecyclerView.ViewHolder viewHolder, int actionState) {
                super.onSelectedChanged(viewHolder, actionState);
                if (actionState == ItemTouchHelper.ACTION_STATE_DRAG) {
                    Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                    vibrator.vibrate(70);
                }
                if (actionState == ItemTouchHelper.ACTION_STATE_IDLE)
                    mKlineDurationAdapter.saveDurationList();
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                //暂不处理
            }

            @Override
            public boolean canDropOver(RecyclerView recyclerView, RecyclerView.ViewHolder current, RecyclerView.ViewHolder target) {
                return true;
            }

            @Override
            public boolean isLongPressDragEnabled() {
                //return true后，可以实现长按拖动排序和拖动动画了
                return true;
            }
        });
        itemTouchHelper.attachToRecyclerView(mBinding.durationRv);
        mKlineDurationAdapter.setItemTouchHelper(itemTouchHelper);
    }

    @Override
    protected void initEvent() {
        mBinding.addDuration.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(KlineDurationActivity.this, AddKlineDurationActivity.class);
                KlineDurationActivity.this.startActivityForResult(intent, CommonConstants.KLINE_DURATION_ACTIVITY_TO_ADD_DURATION_ACTIVITY);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CommonConstants.KLINE_DURATION_ACTIVITY_TO_ADD_DURATION_ACTIVITY
                && resultCode == RESULT_OK) {
            String duration = (String) SPUtils.get(BaseApplication.getContext(), SettingConstants.CONFIG_KLINE_DURATION_DEFAULT, "");
            String[] durations = duration.split(",");
            List<String> list = new ArrayList<>();
            for (String d : durations) {
                list.add(d);
            }
            mKlineDurationAdapter.updateList(list);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        UpdateDurationsEvent updateDurationsEvent = new UpdateDurationsEvent();
        EventBus.getDefault().post(updateDurationsEvent);
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        UpdateDurationsEvent updateDurationsEvent = new UpdateDurationsEvent();
        EventBus.getDefault().post(updateDurationsEvent);
        return super.onOptionsItemSelected(item);
    }
}

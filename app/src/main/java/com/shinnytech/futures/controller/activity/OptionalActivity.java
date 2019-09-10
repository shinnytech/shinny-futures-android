package com.shinnytech.futures.controller.activity;

import android.content.Context;
import android.os.Bundle;
import android.os.Vibrator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.ItemTouchHelper;

import com.shinnytech.futures.R;
import com.shinnytech.futures.constants.CommonConstants;
import com.shinnytech.futures.databinding.ActivityOptionalBinding;
import com.shinnytech.futures.model.adapter.OptionalAdapter;
import com.shinnytech.futures.model.engine.LatestFileManager;

import java.util.List;

public class OptionalActivity extends BaseActivity {

    private ActivityOptionalBinding mBinding;
    private OptionalAdapter mOptionalAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mLayoutID = R.layout.activity_optional;
        mTitle = CommonConstants.OPTIONAL_SETTING;
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void initData() {
        mBinding = (ActivityOptionalBinding) mViewDataBinding;
        List<String> list = LatestFileManager.readInsListFromFile();
        mOptionalAdapter = new OptionalAdapter(this, list);
        mBinding.optionalRv.setLayoutManager(new LinearLayoutManager(this));
        mBinding.optionalRv.setAdapter(mOptionalAdapter);

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new ItemTouchHelper.Callback() {
            @Override
            public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
                int swipeFlag = 0;
                int dragFlag = ItemTouchHelper.UP | ItemTouchHelper.DOWN;
                return makeMovementFlags(dragFlag, swipeFlag);
            }


            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                mOptionalAdapter.onItemMove(viewHolder.getAdapterPosition(), target.getAdapterPosition());
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
                    mOptionalAdapter.saveOptionalList();
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
        itemTouchHelper.attachToRecyclerView(mBinding.optionalRv);
        mOptionalAdapter.setItemTouchHelper(itemTouchHelper);
    }

    @Override
    protected void initEvent() {
    }

}

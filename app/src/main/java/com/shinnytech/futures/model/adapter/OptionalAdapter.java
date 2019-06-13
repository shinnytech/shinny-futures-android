package com.shinnytech.futures.model.adapter;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.shinnytech.futures.R;
import com.shinnytech.futures.databinding.ItemKlineDurationBinding;
import com.shinnytech.futures.model.bean.searchinfobean.SearchEntity;
import com.shinnytech.futures.model.engine.LatestFileManager;
import com.shinnytech.futures.model.listener.ItemTouchHelperListener;

import java.util.ArrayList;
import java.util.List;


/**
 * date: 7/9/17
 * author: chenli
 * description: 自选管理页列表适配器
 * version:
 * state: done
 */
public class OptionalAdapter extends RecyclerView.Adapter<OptionalAdapter.ItemViewHolder> implements ItemTouchHelperListener {
    private Context sContext;
    private List<String> mData = new ArrayList<>();
    private ItemTouchHelper itemTouchHelper;

    public OptionalAdapter(Context context, List<String> data) {
        this.sContext = context;
        this.mData.addAll(data);
    }

    public void setItemTouchHelper(ItemTouchHelper itemTouchHelper) {
        this.itemTouchHelper = itemTouchHelper;
    }

    public void saveOptionalList() {
        LatestFileManager.saveInsListToFile(mData);
    }

    @Override
    public void onItemMove(int fromPosition, int toPosition) {
        String ins = mData.remove(fromPosition);
        mData.add(toPosition, ins);
        notifyItemMoved(fromPosition, toPosition);
    }

    @Override
    public ItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        ItemKlineDurationBinding binding = DataBindingUtil.inflate(LayoutInflater
                .from(sContext), R.layout.item_kline_duration, parent, false);
        ItemViewHolder holder = new ItemViewHolder(binding.getRoot());
        holder.setBinding(binding);
        return holder;
    }

    @Override
    public void onBindViewHolder(final ItemViewHolder holder, int position) {
        holder.update();
    }

    @Override
    public int getItemCount() {
        return mData == null ? 0 : mData.size();
    }

    public class ItemViewHolder extends RecyclerView.ViewHolder {

        private ItemKlineDurationBinding mBinding;

        public ItemViewHolder(View itemView) {
            super(itemView);
        }

        public ItemKlineDurationBinding getBinding() {
            return this.mBinding;
        }

        public void setBinding(ItemKlineDurationBinding binding) {
            this.mBinding = binding;
        }

        public void update() {
            if (mData == null || mData.size() == 0) return;
            final String ins = mData.get(getLayoutPosition());
            SearchEntity searchEntity = LatestFileManager.getSearchEntities().get(ins);
            if (searchEntity == null) mBinding.tvIdDialog.setText(ins);
            else mBinding.tvIdDialog.setText(searchEntity.getInstrumentName());


            mBinding.ivDrag.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    try {
                        if (event.getAction() == MotionEvent.ACTION_DOWN) {
                            if (itemTouchHelper != null)
                                itemTouchHelper.startDrag(ItemViewHolder.this);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return false;
                }
            });

            mBinding.ivTop.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        int index = getLayoutPosition();
                        if (index >= 0 && index < getItemCount()) {
                            onItemMove(index, 0);
                            saveOptionalList();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }
            });

            mBinding.ivCut.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        int index = getLayoutPosition();
                        if (index >= 0 && index < getItemCount()) {
                            mData.remove(index);
                            notifyItemRemoved(index);
                            saveOptionalList();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }
            });
        }
    }
}

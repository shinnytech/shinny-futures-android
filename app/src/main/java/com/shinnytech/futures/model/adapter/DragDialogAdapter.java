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
import com.shinnytech.futures.databinding.ItemDialogOptionalDragBinding;
import com.shinnytech.futures.model.bean.searchinfobean.SearchEntity;
import com.shinnytech.futures.model.engine.LatestFileManager;
import com.shinnytech.futures.model.listener.ItemTouchHelperListener;

import java.util.ArrayList;
import java.util.List;


/**
 * date: 7/9/17
 * author: chenli
 * description: futureInfoActivity页上toolbar的标题点击弹出框适配器，用于显示自选合约列表
 * version:
 * state: done
 */
public class DragDialogAdapter extends RecyclerView.Adapter<DragDialogAdapter.ItemViewHolder> implements ItemTouchHelperListener {
    private Context sContext;
    private List<String> mData = new ArrayList<>();
    private ItemTouchHelper itemTouchHelper;

    public DragDialogAdapter(Context context, List<String> data) {
        this.sContext = context;
        this.mData.addAll(data);
    }

    public void updateList(List<String> data) {
        this.mData.clear();
        this.mData.addAll(data);
        notifyDataSetChanged();
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
        ItemDialogOptionalDragBinding binding = DataBindingUtil.inflate(LayoutInflater
                .from(sContext), R.layout.item_dialog_optional_drag, parent, false);
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
        String instrumentId;

        private ItemDialogOptionalDragBinding mBinding;

        public ItemViewHolder(View itemView) {
            super(itemView);
        }

        public ItemDialogOptionalDragBinding getBinding() {
            return this.mBinding;
        }

        public void setBinding(ItemDialogOptionalDragBinding binding) {
            this.mBinding = binding;
        }

        public void update() {
            if (mData == null || mData.size() == 0) return;
            instrumentId = mData.get(getLayoutPosition());
            if (instrumentId.isEmpty()) return;
            SearchEntity insName = LatestFileManager.getSearchEntities().get(instrumentId);
            if (insName != null) mBinding.tvIdDialog.setText(insName.getInstrumentName());
            else mBinding.tvIdDialog.setText(instrumentId);
            itemView.setTag(instrumentId);

            mBinding.tvDragDialog.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        if (itemTouchHelper != null) itemTouchHelper.startDrag(ItemViewHolder.this);
                    }
                    return false;
                }
            });

            mBinding.tvTopDialog.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onItemMove(getLayoutPosition(), 0);
                    saveOptionalList();
                }
            });

            mBinding.tvCutDialog.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int index = getLayoutPosition();
                    if (index >= 0 && index <= mData.size() - 1) {
                        mData.remove(getLayoutPosition());
                        notifyItemRemoved(getLayoutPosition());
                        saveOptionalList();
                    }
                }
            });
        }
    }
}

package com.shinnytech.futures.model.adapter;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.shinnytech.futures.R;
import com.shinnytech.futures.application.BaseApplication;
import com.shinnytech.futures.constants.CommonConstants;
import com.shinnytech.futures.databinding.ItemKlineDurationBinding;
import com.shinnytech.futures.model.listener.ItemTouchHelperListener;
import com.shinnytech.futures.utils.SPUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 * date: 7/9/17
 * author: chenli
 * description: futureInfoActivity页上toolbar的标题点击弹出框适配器，用于显示自选合约列表
 * version:
 * state: done
 */
public class KlineDurationAdapter extends RecyclerView.Adapter<KlineDurationAdapter.ItemViewHolder> implements ItemTouchHelperListener {
    private Context sContext;
    private List<String> mData = new ArrayList<>();
    private ItemTouchHelper itemTouchHelper;

    public KlineDurationAdapter(Context context, List<String> data) {
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

    public void saveDurationList() {
        String data = TextUtils.join(",", mData);
        SPUtils.putAndApply(BaseApplication.getContext(), CommonConstants.CONFIG_KLINE_DURATION_DEFAULT, data);
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
            String duration = mData.get(getLayoutPosition());
            if (duration.isEmpty()) return;
            mBinding.tvIdDialog.setText(duration);

            mBinding.ivDrag.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if (event.getAction()== MotionEvent.ACTION_DOWN) {
                        if (itemTouchHelper != null)itemTouchHelper.startDrag(ItemViewHolder.this);
                    }
                    return false;
                }
            });

            mBinding.ivTop.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onItemMove(getLayoutPosition(), 0);
                    saveDurationList();
                }
            });

            mBinding.ivCut.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mData.remove(getLayoutPosition());
                    notifyItemRemoved(getLayoutPosition());
                    saveDurationList();
                }
            });
        }
    }
}

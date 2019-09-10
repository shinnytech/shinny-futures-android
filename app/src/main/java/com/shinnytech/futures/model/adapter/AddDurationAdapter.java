package com.shinnytech.futures.model.adapter;

import android.content.Context;
import androidx.databinding.DataBindingUtil;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.shinnytech.futures.R;
import com.shinnytech.futures.application.BaseApplication;
import com.shinnytech.futures.constants.SettingConstants;
import com.shinnytech.futures.databinding.ItemAddDurationBinding;
import com.shinnytech.futures.utils.SPUtils;
import com.shinnytech.futures.utils.ToastUtils;

import java.util.ArrayList;
import java.util.List;


/**
 * date: 7/9/17
 * author: chenli
 * description: 设置页添加常用周期适配器
 * version:
 * state: done
 */
public class AddDurationAdapter extends RecyclerView.Adapter<AddDurationAdapter.ItemViewHolder> {
    private Context sContext;
    private List<String> mData = new ArrayList<>();
    private List<String> mDataPre = new ArrayList<>();

    public AddDurationAdapter(Context context, List<String> data, List<String> dataPre) {
        this.sContext = context;
        this.mData.addAll(data);
        this.mDataPre.addAll(dataPre);
    }

    @Override
    public ItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        ItemAddDurationBinding binding = DataBindingUtil.inflate(LayoutInflater
                .from(sContext), R.layout.item_add_duration, parent, false);
        ItemViewHolder holder = new ItemViewHolder(binding.getRoot());
        holder.setBinding(binding);
        holder.initEvent();
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

        private ItemAddDurationBinding mBinding;

        public ItemViewHolder(View itemView) {
            super(itemView);
        }

        public ItemAddDurationBinding getBinding() {
            return this.mBinding;
        }

        public void setBinding(ItemAddDurationBinding binding) {
            this.mBinding = binding;
        }

        public void update() {
            if (mData == null || mData.size() == 0) return;
            int index = getLayoutPosition();
            String data = mData.get(index);
            mBinding.tvDuration.setText(data);
            if (mDataPre.contains(data)) {
                mBinding.tvDuration.setBackgroundColor(ContextCompat.getColor(sContext, R.color.launch_light));
                mBinding.tvDuration.setTag("1");
            }
        }

        public void initEvent() {
            mBinding.tvDuration.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String tag = (String) mBinding.tvDuration.getTag();
                    if ("0".equals(tag)) {
                        mBinding.tvDuration.setBackgroundColor(ContextCompat.getColor(sContext, R.color.launch_light));
                        mBinding.tvDuration.setTag("1");
                        mDataPre.add(mBinding.tvDuration.getText().toString());
                    } else if ("1".equals(tag)) {
                        if (mDataPre.size() == 1){
                            ToastUtils.showToast(sContext, "至少保留一个周期");
                            return;
                        }
                        mBinding.tvDuration.setBackgroundColor(ContextCompat.getColor(sContext, R.color.black_light));
                        mBinding.tvDuration.setTag("0");
                        mDataPre.remove(mBinding.tvDuration.getText().toString());
                    }
                    SPUtils.putAndApply(BaseApplication.getContext(), SettingConstants.CONFIG_KLINE_DURATION_DEFAULT, TextUtils.join(",", mDataPre));
                }
            });
        }
    }
}

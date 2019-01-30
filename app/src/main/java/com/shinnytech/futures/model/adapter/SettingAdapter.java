package com.shinnytech.futures.model.adapter;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.shinnytech.futures.R;
import com.shinnytech.futures.constants.CommonConstants;
import com.shinnytech.futures.databinding.ItemActivitySettingBinding;
import com.shinnytech.futures.model.bean.settingbean.SettingEntity;
import com.shinnytech.futures.utils.SPUtils;

import java.util.List;


/**
 * date: 7/9/17
 * author: chenli
 * description: 行情页适配器
 * version:
 * state: done
 */
public class SettingAdapter extends RecyclerView.Adapter<SettingAdapter.ItemViewHolder> {

    private Context sContext;
    private List<SettingEntity> mData;
    private SettingItemClickListener mSettingItemClickListener;

    public SettingAdapter(Context context, List<SettingEntity> data) {
        this.sContext = context;
        this.mData = data;
    }

    public void setSettingItemClickListener(SettingItemClickListener settingItemClickListener){
        this.mSettingItemClickListener = settingItemClickListener;
    }

    @Override
    public ItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final ItemActivitySettingBinding binding = DataBindingUtil.inflate(LayoutInflater
                .from(sContext), R.layout.item_activity_setting, parent, false);
        ItemViewHolder holder = new ItemViewHolder(binding.getRoot());
        holder.setBinding(binding);
        holder.initEvent();
        return holder;
    }

    @Override
    public void onBindViewHolder(final ItemViewHolder itemViewHolder, int position) {
        itemViewHolder.update();
    }

    @Override
    public int getItemCount() {
        return mData == null ? 0 : mData.size();
    }

    public interface SettingItemClickListener {
        void onJump(String content);
    }

    class ItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private ItemActivitySettingBinding mBinding;

        public ItemViewHolder(View itemView) {
            super(itemView);
        }

        public ItemActivitySettingBinding getBinding() {
            return this.mBinding;
        }

        public void setBinding(ItemActivitySettingBinding binding) {
            this.mBinding = binding;
        }

        private void initEvent() {
            mBinding.settingToggle.setOnClickListener(this);
            mBinding.settingJump.setOnClickListener(this);
            mBinding.settingItem.setOnClickListener(this);
        }

        public void update() {
            SettingEntity settingEntity = mData.get(getLayoutPosition());
            if (settingEntity == null) return;
            String title = settingEntity.getTitle();
            int icon = settingEntity.getIcon();
            final String content = settingEntity.getContent();
            boolean jump = settingEntity.isJump();

            if ("".equals(title)) mBinding.title.setVisibility(View.GONE);
            else mBinding.title.setText(title);

            if (jump) mBinding.settingToggle.setVisibility(View.GONE);
            else {
                switch (content) {
                    case CommonConstants.ORDER_CONFIRM:
                        boolean check = (boolean) SPUtils.get(sContext, CommonConstants.CONFIG_ORDER_CONFIRM, false);
                        mBinding.settingToggle.setChecked(check);
                        break;
                    default:
                        break;
                }
                mBinding.settingJump.setVisibility(View.GONE);
            }

            mBinding.icon.setImageResource(icon);

            mBinding.content.setText(content);

            itemView.setTag(content);

        }

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.setting_item:
                    if (mBinding.settingToggle.getVisibility() == View.VISIBLE){
                        if (mBinding.settingToggle.isChecked()) mBinding.settingToggle.setChecked(false);
                        else mBinding.settingToggle.setChecked(true);
                        saveSettingConfig();
                    }else {
                        if (mSettingItemClickListener != null){
                            mSettingItemClickListener.onJump(mBinding.content.getText().toString());
                        }
                    }
                    break;
                case R.id.setting_jump:
                    if (mSettingItemClickListener != null){
                        mSettingItemClickListener.onJump(mBinding.content.getText().toString());
                    }
                    break;
                case R.id.setting_toggle:
                    saveSettingConfig();
                    break;
                default:
                    break;
            }
        }

        public void saveSettingConfig(){
            switch (mBinding.content.getText().toString()){
                case CommonConstants.ORDER_CONFIRM:
                    SPUtils.putAndApply(sContext, CommonConstants.CONFIG_ORDER_CONFIRM, mBinding.settingToggle.isChecked());
                    break;
                default:
                    break;
            }
        }

    }
}

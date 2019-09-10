package com.shinnytech.futures.model.adapter;

import android.content.Context;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;
import q.rorbin.badgeview.Badge;
import q.rorbin.badgeview.QBadgeView;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.shinnytech.futures.R;
import com.shinnytech.futures.constants.CommonConstants;
import com.shinnytech.futures.databinding.ItemRightNavigationBinding;
import com.shinnytech.futures.model.bean.settingbean.NavigationRightEntity;
import com.shinnytech.futures.utils.ScreenUtils;

import java.util.List;

import static com.shinnytech.futures.constants.CommonConstants.CONDITIONAL_ORDER;


/**
 * date: 7/9/17
 * author: chenli
 * description: 右导航适配器
 * version:
 * state: done
 */
public class NavigationRightAdapter extends RecyclerView.Adapter<NavigationRightAdapter.ItemViewHolder> {

    private Context sContext;
    private List<NavigationRightEntity> mData;
    private Badge mBadgeView;

    public NavigationRightAdapter(Context sContext, List<NavigationRightEntity> mData) {
        this.sContext = sContext;
        this.mData = mData;
        mBadgeView = new QBadgeView(sContext)
                .setBadgeNumber(-1)
                .setBadgePadding(4, true)
                .setGravityOffset(4, 8, true)
                .setBadgeBackgroundColor(sContext.getResources().getColor(R.color.launch))
                .setBadgeGravity(Gravity.CENTER|Gravity.TOP)
                .setBadgeTextColor(sContext.getResources().getColor(R.color.white));

    }

    public void refreshBadgeNum(int count){
        if (count != -1)
            mBadgeView.setBadgeNumber(count)
                .setBadgeGravity(Gravity.CENTER|Gravity.END);
        else mBadgeView.setBadgeNumber(count)
                .setBadgeGravity(Gravity.CENTER|Gravity.TOP);
    }

    @Override
    public ItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final ItemRightNavigationBinding binding = DataBindingUtil.inflate(LayoutInflater
                .from(sContext), R.layout.item_right_navigation, parent, false);
        ItemViewHolder holder = new ItemViewHolder(binding.getRoot());
        holder.setBinding(binding);
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

    class ItemViewHolder extends RecyclerView.ViewHolder {

        private ItemRightNavigationBinding mBinding;

        public ItemViewHolder(View itemView) {
            super(itemView);
        }

        public ItemRightNavigationBinding getBinding() {
            return this.mBinding;
        }

        public void setBinding(ItemRightNavigationBinding binding) {
            this.mBinding = binding;
        }

        public void update() {
            NavigationRightEntity navigationRightEntity = mData.get(getLayoutPosition());
            if (navigationRightEntity == null) return;
            int icon = navigationRightEntity.getIcon();
            final String content = navigationRightEntity.getContent();

            mBinding.icon.setImageResource(icon);
            mBinding.content.setText(content);

            if (CONDITIONAL_ORDER.equals(content)){
                mBadgeView.bindTarget(mBinding.content);
            }

            //分割线加粗
            if (CommonConstants.LOGIN.equals(content) || CommonConstants.LOGOUT.equals(content)
                    || CommonConstants.SETTING.equals(content)) {
                ViewGroup.LayoutParams layoutParams = mBinding.divider.getLayoutParams();
                layoutParams.height = ScreenUtils.dp2px(sContext, 3);
                mBinding.divider.setLayoutParams(layoutParams);
            }

            itemView.setTag(content);
        }

    }
}

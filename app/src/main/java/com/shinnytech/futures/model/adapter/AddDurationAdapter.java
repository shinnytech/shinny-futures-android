package com.shinnytech.futures.model.adapter;

import android.app.Dialog;
import android.content.Context;
import android.databinding.DataBindingUtil;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;

import com.shinnytech.futures.R;
import com.shinnytech.futures.application.BaseApplication;
import com.shinnytech.futures.constants.CommonConstants;
import com.shinnytech.futures.databinding.ItemAddDurationBinding;
import com.shinnytech.futures.utils.SPUtils;

import java.util.ArrayList;
import java.util.List;


/**
 * date: 7/9/17
 * author: chenli
 * description: futureInfoActivity页上toolbar的标题点击弹出框适配器，用于显示自选合约列表
 * version:
 * state: done
 */
public class AddDurationAdapter extends RecyclerView.Adapter<AddDurationAdapter.ItemViewHolder> {
    private Context sContext;
    private List<String> mData = new ArrayList<>();
    private List<String> mDataPre = new ArrayList<>();
    private Dialog mDialogDuration;
    private String[] durations = new String[]{"秒", "分钟", "小时", "日", "月", "年"};
    private int i = 0;

    public AddDurationAdapter(Context context, List<String> data, List<String> dataPre) {
        this.sContext = context;
        this.mData.addAll(data);
        this.mDataPre.addAll(dataPre);
    }

    public void updateList(List<String> data) {
        this.mData.clear();
        this.mData.addAll(data);
        notifyDataSetChanged();
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
            if (index < mData.size()) {
                String data = mData.get(index);
                mBinding.tvDuration.setText(data);
                if (mDataPre.contains(data)) {
                    mBinding.tvDuration.setBackgroundColor(ContextCompat.getColor(sContext, R.color.launch_light));
                    mBinding.tvDuration.setTag("1");
                }
            } else if (index == mData.size()) {
                mBinding.tvDuration.setText("✚");
            }
        }

        public void initEvent() {
            mBinding.tvDuration.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int index = getLayoutPosition();
                    if (index < mData.size()) {
                        String tag = (String) mBinding.tvDuration.getTag();
                        if ("0".equals(tag)) {
                            mBinding.tvDuration.setBackgroundColor(ContextCompat.getColor(sContext, R.color.launch_light));
                            mBinding.tvDuration.setTag("1");
                            mDataPre.add(mBinding.tvDuration.getText().toString());
                        } else if ("1".equals(tag)) {
                            mBinding.tvDuration.setBackgroundColor(ContextCompat.getColor(sContext, R.color.black_light));
                            mBinding.tvDuration.setTag("0");
                            mDataPre.remove(mBinding.tvDuration.getText().toString());
                        }
                        SPUtils.putAndApply(BaseApplication.getContext(), CommonConstants.CONFIG_KLINE_DURATION_DEFAULT, TextUtils.join(",", mDataPre));
                    } else if (index == mData.size()) {
                        if (mDialogDuration == null) {
                            mDialogDuration = new Dialog(sContext, R.style.Theme_Light_Dialog);
                            View viewDialog = View.inflate(sContext, R.layout.view_add_kline_duration, null);
                            final EditText duration = viewDialog.findViewById(R.id.input_duration);
                            final Button duration_button = viewDialog.findViewById(R.id.select_duration);
                            duration_button.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    i = i + 1;
                                    if (i == 6) i = 0;
                                    String duration = durations[i] + "⇲";
                                    duration_button.setText(duration);
                                }
                            });
                            viewDialog.findViewById(R.id.add_duration).setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    String data = duration.getText().toString();
                                    String data1 = duration_button.getText().toString();
                                    data1 = data1.replace("⇲", "");
                                    String data2 = data + data1;
                                }
                            });
                            Window dialogWindow = mDialogDuration.getWindow();
                            if (dialogWindow != null) {
                                dialogWindow.getDecorView().setPadding(0, 0, 0, 0);
                                WindowManager.LayoutParams lp = dialogWindow.getAttributes();
                                dialogWindow.setGravity(Gravity.BOTTOM);
                                lp.width = WindowManager.LayoutParams.MATCH_PARENT;
                                lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
                                dialogWindow.setAttributes(lp);
                            }
                            mDialogDuration.setContentView(viewDialog);
                        }

                        if (!mDialogDuration.isShowing()) mDialogDuration.show();
                    }
                }
            });
        }
    }
}

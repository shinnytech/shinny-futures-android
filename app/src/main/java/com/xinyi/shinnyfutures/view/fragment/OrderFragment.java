package com.xinyi.shinnyfutures.view.fragment;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.LinearLayoutManager;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.xinyi.shinnyfutures.R;
import com.xinyi.shinnyfutures.application.BaseApplicationLike;
import com.xinyi.shinnyfutures.databinding.FragmentOrderBinding;
import com.xinyi.shinnyfutures.view.adapter.OrderAdapter;
import com.xinyi.shinnyfutures.model.bean.accountinfobean.OrderEntity;
import com.xinyi.shinnyfutures.model.engine.DataManager;
import com.xinyi.shinnyfutures.view.listener.OrderDiffCallback;
import com.xinyi.shinnyfutures.view.listener.SimpleRecyclerViewItemClickListener;
import com.xinyi.shinnyfutures.utils.DividerItemDecorationUtils;
import com.xinyi.shinnyfutures.view.activity.FutureInfoActivity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.xinyi.shinnyfutures.constants.CommonConstants.CLOSE;
import static com.xinyi.shinnyfutures.constants.CommonConstants.ERROR;
import static com.xinyi.shinnyfutures.constants.CommonConstants.MESSAGE_ORDER;
import static com.xinyi.shinnyfutures.constants.CommonConstants.OPEN;
import static com.xinyi.shinnyfutures.model.service.WebSocketService.BROADCAST_ACTION_TRANSACTION;

/**
 * date: 5/10/17
 * author: chenli
 * description: 挂单页
 * version:
 * state: done
 */
public class OrderFragment extends LazyLoadFragment implements RadioGroup.OnCheckedChangeListener {

    protected BroadcastReceiver mReceiver;
    protected DataManager sDataManager = DataManager.getInstance();

    private OrderAdapter mAdapter;
    private List<OrderEntity> mOldData = new ArrayList<>();
    private List<OrderEntity> mNewData = new ArrayList<>();
    private FragmentOrderBinding mBinding;

    /**
     * date: 7/14/17
     * author: chenli
     * description: 撤单弹出框，根据固定宽高值自定义dialog，注意宽高值从dimens.xml文件中得到
     */
    private void initDialog(final String order_id, String instrument_id, String direction_title, String volume, String price) {
        final Dialog dialog = new Dialog(getActivity(), R.style.Theme_Light_Dialog);
        View view = View.inflate(getActivity(), R.layout.view_dialog_cancel_order, null);
        Window dialogWindow = dialog.getWindow();
        if (dialogWindow != null) {
            dialogWindow.getDecorView().setPadding(0, 0, 0, 0);
            WindowManager.LayoutParams lp = dialogWindow.getAttributes();
            dialogWindow.setGravity(Gravity.CENTER);
            lp.width = (int) getActivity().getResources().getDimension(R.dimen.order_dialog_width);
            lp.height = (int) getActivity().getResources().getDimension(R.dimen.order_dialog_height);
            dialogWindow.setAttributes(lp);
        }
        dialog.setContentView(view);
        dialog.setCancelable(false);
        TextView info = view.findViewById(R.id.order_instrument_id);
        TextView ok = view.findViewById(R.id.order_ok);
        TextView cancel = view.findViewById(R.id.order_cancel);
        String information = instrument_id + ", " + price + ", " + direction_title + ", " + volume + "手";
        info.setText(information);
        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if (BaseApplicationLike.getWebSocketService() != null)
                        BaseApplicationLike.getWebSocketService().sendReqCancelOrder(order_id);
                } catch (NumberFormatException ex) {
                    ex.printStackTrace();
                }
                dialog.dismiss();
            }
        });
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_order, container, false);
        initData();
        initEvent();
        return mBinding.getRoot();
    }

    protected void initData() {
        mBinding.rv.setLayoutManager(
                new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
        mBinding.rv.addItemDecoration(
                new DividerItemDecorationUtils(getActivity(), DividerItemDecorationUtils.VERTICAL_LIST));
        mAdapter = new OrderAdapter(getActivity(), mOldData);
        mBinding.rv.setAdapter(mAdapter);
    }

    protected void initEvent() {
        if (mBinding.rv != null) {
            //recyclerView点击事件监听器，对于未成交单进行撤单
            SimpleRecyclerViewItemClickListener mTouchListener = new SimpleRecyclerViewItemClickListener(mBinding.rv, new SimpleRecyclerViewItemClickListener.OnItemClickListener() {
                @Override
                public void onItemClick(View view, int position) {
                    OrderEntity orderEntity = mAdapter.getData().get(position);
                    if (orderEntity != null) {
                        if (("ALIVE").equals(orderEntity.getStatus())) {
                            String order_id = orderEntity.getOrder_id();
                            String instrument_id = orderEntity.getInstrument_id();
                            String direction_title = ((TextView) view.findViewById(R.id.order_offset)).getText().toString();
                            String volume = orderEntity.getVolume_left();
                            String price = ((TextView) view.findViewById(R.id.order_price)).getText().toString();
                            initDialog(order_id, instrument_id, direction_title, volume, price);
                        }
                    }
                }

                @Override
                public void onItemLongClick(View view, int position) {
                }
            });
            mBinding.rv.addOnItemTouchListener(mTouchListener);
        }
        mBinding.rgOrder.setOnCheckedChangeListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshOrder();
        registerBroaderCast();
    }

    @Override
    public void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mReceiver);
    }

    protected void registerBroaderCast() {
        mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String mDataString = intent.getStringExtra("msg");
                switch (mDataString) {
                    case OPEN:
                        break;
                    case CLOSE:
                        break;
                    case ERROR:
                        break;
                    case MESSAGE_ORDER:
                        if ((R.id.rb_order_info == ((FutureInfoActivity) getActivity()).getTabsInfo().getCheckedRadioButtonId()))
                            refreshOrder();
                        break;
                    default:
                        break;
                }
            }
        };
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mReceiver, new IntentFilter(BROADCAST_ACTION_TRANSACTION));
    }

    /**
     * date: 7/9/17
     * author: chenli
     * description: 根据用户选择显示全部挂单或未成交单
     */
    protected void refreshOrder() {
        mNewData.clear();
        if (mBinding.rbAllOrder.isChecked()) {
            mNewData.addAll(sDataManager.getAccountBean().getOrder().values());
        } else {
            for (Map.Entry<String, OrderEntity> entry :
                    sDataManager.getAccountBean().getOrder().entrySet()) {
                if (("ALIVE").equals(entry.getValue().getStatus())) {
                    mNewData.add(entry.getValue());
                }
            }
        }
        Collections.sort(mNewData);
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new OrderDiffCallback(mOldData, mNewData), false);
        mAdapter.setData(mNewData);
        diffResult.dispatchUpdatesTo(mAdapter);
        mBinding.rv.scrollToPosition(0);
        mOldData.clear();
        mOldData.addAll(mNewData);
    }

    @Override
    public void onCheckedChanged(RadioGroup group, @IdRes int checkedId) {
        switch (checkedId) {
            case R.id.rb_all_order:
                mBinding.rbAllOrder.setChecked(true);
                refreshOrder();
                break;
            case R.id.rb_undone_order:
                mBinding.rbUndoneOrder.setChecked(true);
                refreshOrder();
                break;
            default:
                break;
        }
    }


    @Override
    public void update() {
        refreshOrder();
    }

}

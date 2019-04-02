package com.shinnytech.futures.controller.fragment;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.shinnytech.futures.R;
import com.shinnytech.futures.application.BaseApplication;
import com.shinnytech.futures.constants.CommonConstants;
import com.shinnytech.futures.controller.activity.FutureInfoActivity;
import com.shinnytech.futures.databinding.FragmentOrderBinding;
import com.shinnytech.futures.model.adapter.OrderAdapter;
import com.shinnytech.futures.model.bean.accountinfobean.OrderEntity;
import com.shinnytech.futures.model.bean.accountinfobean.UserEntity;
import com.shinnytech.futures.model.engine.DataManager;
import com.shinnytech.futures.model.listener.OrderDiffCallback;
import com.shinnytech.futures.model.listener.SimpleRecyclerViewItemClickListener;
import com.shinnytech.futures.utils.CloneUtils;
import com.shinnytech.futures.utils.DividerItemDecorationUtils;
import com.shinnytech.futures.utils.SPUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.shinnytech.futures.constants.CommonConstants.TD_MESSAGE;
import static com.shinnytech.futures.model.service.WebSocketService.TD_BROADCAST_ACTION;

/**
 * date: 5/10/17
 * author: chenli
 * description: 挂单页
 * version:
 * state: done
 */
public class OrderFragment extends LazyLoadFragment {

    private static final String KEY_FRAGMENT_TYPE = "tradeType";

    protected BroadcastReceiver mReceiver;
    protected DataManager sDataManager = DataManager.getInstance();

    private OrderAdapter mAdapter;
    private List<OrderEntity> mOldData = new ArrayList<>();
    private List<OrderEntity> mNewData = new ArrayList<>();
    private FragmentOrderBinding mBinding;
    private boolean mIsUpdate;
    private boolean mIsShowDialog;
    private boolean mIsAllOrders;

    public static OrderFragment newInstance(boolean mIsAllOrders) {
        OrderFragment fragment = new OrderFragment();
        Bundle bundle = new Bundle();
        bundle.putBoolean(KEY_FRAGMENT_TYPE, mIsAllOrders);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mIsAllOrders = getArguments().getBoolean(KEY_FRAGMENT_TYPE);
        setHasOptionsMenu(true);
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
        mIsUpdate = true;
        mBinding.rv.setLayoutManager(
                new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
        mBinding.rv.addItemDecoration(
                new DividerItemDecorationUtils(getActivity(), DividerItemDecorationUtils.VERTICAL_LIST));
        mAdapter = new OrderAdapter(getActivity(), mOldData);
        mBinding.rv.setAdapter(mAdapter);
        mIsShowDialog = (boolean) SPUtils.get(BaseApplication.getContext(), CommonConstants.CONFIG_ORDER_CONFIRM, true);
    }

    protected void initEvent() {

        mBinding.rv.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                switch (newState) {
                    case RecyclerView.SCROLL_STATE_IDLE:
                        mIsUpdate = true;
                        break;
                    case RecyclerView.SCROLL_STATE_DRAGGING:
                        mIsUpdate = false;
                        break;
                    case RecyclerView.SCROLL_STATE_SETTLING:
                        mIsUpdate = false;
                        break;
                }
            }
        });


        mBinding.rv.addOnItemTouchListener(new SimpleRecyclerViewItemClickListener(mBinding.rv, new SimpleRecyclerViewItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                OrderEntity orderEntity = mAdapter.getData().get(position);
                if (orderEntity != null) {
                    if (("ALIVE").equals(orderEntity.getStatus())) {
                        String order_id = orderEntity.getOrder_id();
                        if (mIsShowDialog) {
                            String instrument_id = orderEntity.getInstrument_id();
                            String direction_title = ((TextView) view.findViewById(R.id.order_offset)).getText().toString();
                            String volume = orderEntity.getVolume_left();
                            String price = ((TextView) view.findViewById(R.id.order_price)).getText().toString();
                            initDialog(order_id, instrument_id, direction_title, volume, price);
                        } else {
                            BaseApplication.getWebSocketService().sendReqCancelOrder(order_id);
                        }
                    }
                }
            }

            @Override
            public void onItemLongClick(View view, int position) {
            }
        }));

    }

    @Override
    public void onResume() {
        super.onResume();
        update();
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
                    case TD_MESSAGE:
                        if (getActivity() instanceof FutureInfoActivity)
                            if ((R.id.rb_order_info == ((FutureInfoActivity) getActivity()).getTabsInfo().getCheckedRadioButtonId())
                                    && mIsUpdate)
                                refreshOrder();
                        else refreshOrder();
                        break;
                    default:
                        break;
                }
            }
        };
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mReceiver, new IntentFilter(TD_BROADCAST_ACTION));
    }

    /**
     * date: 7/9/17
     * author: chenli
     * description: 根据用户选择显示全部挂单或未成交单
     */
    protected void refreshOrder() {
        try {
            UserEntity userEntity = sDataManager.getTradeBean().getUsers().get(sDataManager.USER_ID);
            if (userEntity == null) return;
            mNewData.clear();
            for (OrderEntity orderEntity :
                    userEntity.getOrders().values()) {
                OrderEntity o = CloneUtils.clone(orderEntity);
                if (mIsAllOrders) {
                    mNewData.add(o);
                } else if (("ALIVE").equals(orderEntity.getStatus())) {
                    mNewData.add(o);
                }
            }
            Collections.sort(mNewData);
            DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new OrderDiffCallback(mOldData, mNewData), false);
            mAdapter.setData(mNewData);
            diffResult.dispatchUpdatesTo(mAdapter);
            mOldData.clear();
            mOldData.addAll(mNewData);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void update() {
        refreshOrder();
        mBinding.rv.scrollToPosition(0);
    }

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
                    if (BaseApplication.getWebSocketService() != null)
                        BaseApplication.getWebSocketService().sendReqCancelOrder(order_id);
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

}

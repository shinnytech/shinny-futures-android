package com.shinnytech.futures.controller.fragment;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.databinding.DataBindingUtil;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.shinnytech.futures.R;
import com.shinnytech.futures.application.BaseApplication;
import com.shinnytech.futures.constants.CommonConstants;
import com.shinnytech.futures.controller.activity.FutureInfoActivity;
import com.shinnytech.futures.controller.activity.MainActivity;
import com.shinnytech.futures.databinding.FragmentOrderBinding;
import com.shinnytech.futures.model.adapter.OrderAdapter;
import com.shinnytech.futures.model.bean.accountinfobean.OrderEntity;
import com.shinnytech.futures.model.bean.accountinfobean.UserEntity;
import com.shinnytech.futures.model.engine.DataManager;
import com.shinnytech.futures.model.listener.OrderDiffCallback;
import com.shinnytech.futures.model.listener.SimpleRecyclerViewItemClickListener;
import com.shinnytech.futures.utils.CloneUtils;
import com.shinnytech.futures.utils.DensityUtils;
import com.shinnytech.futures.utils.DividerItemDecorationUtils;
import com.shinnytech.futures.utils.LogUtils;
import com.shinnytech.futures.utils.SPUtils;
import com.shinnytech.futures.utils.TimeUtils;
import com.shinnytech.futures.utils.ToastNotificationUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.shinnytech.futures.constants.CommonConstants.CONFIG_LOGIN_DATE;
import static com.shinnytech.futures.constants.CommonConstants.JUMP_TO_FUTURE_INFO_ACTIVITY;
import static com.shinnytech.futures.constants.CommonConstants.STATUS_ALIVE;
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

    private static final String KEY_FRAGMENT_TYPE = "mIsOrdersAlive";

    protected BroadcastReceiver mReceiver;
    protected DataManager sDataManager = DataManager.getInstance();

    private OrderAdapter mAdapter;
    private List<OrderEntity> mOldData = new ArrayList<>();
    private List<OrderEntity> mNewData = new ArrayList<>();
    private FragmentOrderBinding mBinding;
    private boolean mIsUpdate;
    private boolean mIsShowCancelPop;
    private boolean mIsOrdersAlive;
    private boolean mIsOrdersAll;

    public static OrderFragment newInstance(boolean mIsOrdersAlive) {
        OrderFragment fragment = new OrderFragment();
        Bundle bundle = new Bundle();
        bundle.putBoolean(KEY_FRAGMENT_TYPE, mIsOrdersAlive);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mIsOrdersAlive = getArguments().getBoolean(KEY_FRAGMENT_TYPE);
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
        if (getActivity() instanceof MainActivity) mIsOrdersAll = true;
        else mIsOrdersAll = false;

        mIsUpdate = true;
        mBinding.rv.setLayoutManager(
                new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
        mBinding.rv.addItemDecoration(
                new DividerItemDecorationUtils(getActivity(), DividerItemDecorationUtils.VERTICAL_LIST));
        mAdapter = new OrderAdapter(getActivity(), mOldData);
        mBinding.rv.setAdapter(mAdapter);
        mIsShowCancelPop = (boolean) SPUtils.get(BaseApplication.getContext(), CommonConstants.CONFIG_CANCEL_ORDER_CONFIRM, true);
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
                if (orderEntity == null) return;

                if (mIsOrdersAlive) {
                    checkPassword(view, orderEntity);
                }else {
                    if (mIsOrdersAll){
                        ((MainActivity)getActivity()).getmMainActivityPresenter()
                                .setPreSubscribedQuotes(sDataManager.getRtnData().getIns_list());

                        sDataManager.IS_SHOW_VP_CONTENT = true;
                        Intent intent = new Intent(getActivity(), FutureInfoActivity.class);
                        intent.putExtra("instrument_id", orderEntity.getExchange_id()
                                + "." + orderEntity.getInstrument_id());
                        startActivityForResult(intent, JUMP_TO_FUTURE_INFO_ACTIVITY);
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
                        refreshOrder();
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
            if (!mIsUpdate)return;
            UserEntity userEntity = sDataManager.getTradeBean().getUsers().get(sDataManager.USER_ID);
            if (userEntity == null) return;
            mNewData.clear();

            for (OrderEntity orderEntity :
                    userEntity.getOrders().values()) {
                OrderEntity o = CloneUtils.clone(orderEntity);
                if (!mIsOrdersAll){
                    String ins = ((FutureInfoActivity)getActivity()).getInstrument_id();
                    String ins_ = orderEntity.getExchange_id() + "." + orderEntity.getInstrument_id();
                    if (!ins_.equals(ins))continue;
                }

                if (!mIsOrdersAlive) {
                    mNewData.add(o);
                } else if (STATUS_ALIVE.equals(orderEntity.getStatus())) {
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
     * date: 2019/4/9
     * author: chenli
     * description: 撤单确认密码
     */
    private void checkPassword(final View view, final OrderEntity orderEntity){
        final Context context = BaseApplication.getContext();
        String date = (String) SPUtils.get(context, CommonConstants.CONFIG_LOGIN_DATE, "");
        if (TimeUtils.getNowTime().equals(date)) {
            String order_id = orderEntity.getOrder_id();
            if (mIsShowCancelPop) {
                initPopUp(view, order_id);
            } else {
                BaseApplication.getWebSocketService().sendReqCancelOrder(order_id);
            }
        }else {
            final Dialog dialog = new Dialog(getActivity(), R.style.Theme_Light_Dialog);
            View viewDialog = View.inflate(getActivity(), R.layout.view_dialog_check_password, null);
            Window dialogWindow = dialog.getWindow();
            if (dialogWindow != null) {
                dialogWindow.getDecorView().setPadding(0, 0, 0, 0);
                WindowManager.LayoutParams lp = dialogWindow.getAttributes();
                dialogWindow.setGravity(Gravity.CENTER);
                lp.width = WindowManager.LayoutParams.MATCH_PARENT;
                lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
                dialogWindow.setAttributes(lp);
            }
            final EditText editText = viewDialog.findViewById(R.id.password);
            viewDialog.findViewById(R.id.tv_password_check).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String password = editText.getText().toString();
                    String passwordLocal = (String) SPUtils.get(context, CommonConstants.CONFIG_PASSWORD, "");
                    if (passwordLocal.equals(password)){
                        dialog.dismiss();
                        SPUtils.putAndApply(context, CONFIG_LOGIN_DATE, TimeUtils.getNowTime());

                        String order_id = orderEntity.getOrder_id();
                        if (mIsShowCancelPop) {
                            initPopUp(view, order_id);
                        } else {
                            BaseApplication.getWebSocketService().sendReqCancelOrder(order_id);
                        }
                    } else ToastNotificationUtils.showToast(context, "密码输入错误");
                }
            });
            dialog.setContentView(viewDialog);
            dialog.show();

        }
    }


    /**
     * date: 2019/4/17
     * author: chenli
     * description: 构造一个撤单的PopupWindow
     */
    private void initPopUp(View view, final String order_id) {
        final View popUpView = View.inflate(getActivity(), R.layout.popup_fragment_order, null);
        final PopupWindow popWindow = new PopupWindow(popUpView,
                ViewGroup.LayoutParams.MATCH_PARENT, DensityUtils.dp2px(getActivity(), 42), true);
        //设置动画，淡入淡出
        popWindow.setAnimationStyle(R.style.anim_menu_quote);
        //点击空白处popupWindow消失
        popWindow.setBackgroundDrawable(new ColorDrawable(0x00000000));
        TextView cancel = popUpView.findViewById(R.id.cancel_order);
        //设置popupWindow显示的位置，参数依次是参照View，x轴的偏移量，y轴的偏移量
        DisplayMetrics outMetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(outMetrics);
        popWindow.showAsDropDown(view, outMetrics.widthPixels / 4 * 3, 0);

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BaseApplication.getWebSocketService().sendReqCancelOrder(order_id);
                popWindow.dismiss();
            }
        });
    }

}

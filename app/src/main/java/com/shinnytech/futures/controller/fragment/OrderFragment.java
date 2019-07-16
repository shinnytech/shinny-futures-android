package com.shinnytech.futures.controller.fragment;

import android.app.Dialog;
import android.content.Context;
import android.databinding.DataBindingUtil;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
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
import com.shinnytech.futures.controller.activity.MainActivity;
import com.shinnytech.futures.controller.activity.MainActivityPresenter;
import com.shinnytech.futures.databinding.FragmentOrderBinding;
import com.shinnytech.futures.model.adapter.OrderAdapter;
import com.shinnytech.futures.model.bean.accountinfobean.OrderEntity;
import com.shinnytech.futures.model.bean.accountinfobean.UserEntity;
import com.shinnytech.futures.model.bean.eventbusbean.IdEvent;
import com.shinnytech.futures.model.bean.eventbusbean.OrderSettingEvent;
import com.shinnytech.futures.model.bean.searchinfobean.SearchEntity;
import com.shinnytech.futures.model.engine.DataManager;
import com.shinnytech.futures.model.engine.LatestFileManager;
import com.shinnytech.futures.model.listener.OrderDiffCallback;
import com.shinnytech.futures.model.listener.SimpleRecyclerViewItemClickListener;
import com.shinnytech.futures.utils.CloneUtils;
import com.shinnytech.futures.utils.DividerItemDecorationUtils;
import com.shinnytech.futures.utils.SPUtils;
import com.shinnytech.futures.utils.ScreenUtils;
import com.shinnytech.futures.utils.TimeUtils;
import com.shinnytech.futures.utils.ToastUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.shinnytech.futures.constants.CommonConstants.CONFIG_LOGIN_DATE;
import static com.shinnytech.futures.constants.CommonConstants.STATUS_ALIVE;

/**
 * date: 5/10/17
 * author: chenli
 * description: 挂单页
 * version:
 * state: done
 */
public class OrderFragment extends LazyLoadFragment {

    private static final String KEY_FRAGMENT_TYPE = "mIsOrdersAlive";

    private static final String KEY_FRAGMENT_TYPE_1 = "isInAccountFragment";

    protected DataManager sDataManager = DataManager.getInstance();

    private OrderAdapter mAdapter;
    private List<OrderEntity> mOldData = new ArrayList<>();
    private List<OrderEntity> mNewData = new ArrayList<>();
    private FragmentOrderBinding mBinding;
    private boolean mIsUpdate;
    private boolean mIsShowCancelPop;
    private boolean mIsOrdersAlive;
    private String mInstrumentId;
    private boolean mIsInAccountFragment;

    public static OrderFragment newInstance(boolean mIsOrdersAlive, boolean isInAccountFragment) {
        OrderFragment fragment = new OrderFragment();
        Bundle bundle = new Bundle();
        bundle.putBoolean(KEY_FRAGMENT_TYPE, mIsOrdersAlive);
        bundle.putBoolean(KEY_FRAGMENT_TYPE_1, isInAccountFragment);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mIsOrdersAlive = getArguments().getBoolean(KEY_FRAGMENT_TYPE);
        mIsInAccountFragment = getArguments().getBoolean(KEY_FRAGMENT_TYPE_1);
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
        mAdapter.setHighlightIns(mInstrumentId);
        mBinding.rv.setAdapter(mAdapter);
        mIsShowCancelPop = (boolean) SPUtils.get(BaseApplication.getContext(), CommonConstants.CONFIG_CANCEL_ORDER_CONFIRM, true);
        EventBus.getDefault().register(this);
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
                if (position >= 0 && position < mAdapter.getItemCount()) {
                    OrderEntity orderEntity = mAdapter.getData().get(position);
                    if (orderEntity == null) return;
                    if (mIsOrdersAlive) {
                        checkPassword(view, orderEntity);
                    } else {
                        String instrument_id = orderEntity.getExchange_id() + "." + orderEntity.getInstrument_id();
                        sDataManager.IS_SHOW_VP_CONTENT = true;
                        MainActivity mainActivity = (MainActivity) getActivity();
                        MainActivityPresenter mainActivityPresenter = mainActivity.getmMainActivityPresenter();
                        mainActivityPresenter.switchToFutureInfo(instrument_id);
                    }
                }
            }

            @Override
            public void onItemLongClick(View view, int position) {
            }
        }));

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void show() {
        refreshTD();
    }

    @Override
    public void leave() {
    }

    @Override
    public void refreshMD() {

    }

    @Override
    public void refreshTD() {
        try {
            if (!mIsUpdate) return;
            UserEntity userEntity = sDataManager.getTradeBean().getUsers().get(sDataManager.USER_ID);
            if (userEntity == null) return;
            mNewData.clear();

            for (OrderEntity orderEntity :
                    userEntity.getOrders().values()) {

                OrderEntity o = CloneUtils.clone(orderEntity);

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

    /**
     * date: 2019/4/9
     * author: chenli
     * description: 撤单确认密码
     */
    private void checkPassword(final View view, final OrderEntity orderEntity) {
        final Context context = BaseApplication.getContext();
        String date = (String) SPUtils.get(context, CommonConstants.CONFIG_LOGIN_DATE, "");
        if (TimeUtils.getNowTime().equals(date)) {
            initPopUp(view, orderEntity);
        } else {
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
                    if (passwordLocal.equals(password)) {
                        dialog.dismiss();
                        SPUtils.putAndApply(context, CONFIG_LOGIN_DATE, TimeUtils.getNowTime());
                        initPopUp(view, orderEntity);
                    } else ToastUtils.showToast(context, "密码输入错误");
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
    private void initPopUp(final View view, final OrderEntity orderEntity) {
        final View popUpView = View.inflate(getActivity(), R.layout.popup_fragment_order, null);
        final PopupWindow popWindow = new PopupWindow(popUpView,
                ViewGroup.LayoutParams.MATCH_PARENT, ScreenUtils.dp2px(getActivity(), 42), true);
        //设置动画，淡入淡出
        popWindow.setAnimationStyle(R.style.anim_menu_quote);
        //点击空白处popupWindow消失
        popWindow.setBackgroundDrawable(new ColorDrawable(0x00000000));
        TextView cancel = popUpView.findViewById(R.id.cancel_order);
        //设置popupWindow显示的位置，参数依次是参照View，x轴的偏移量，y轴的偏移量
        DisplayMetrics outMetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(outMetrics);
        popWindow.showAsDropDown(view, outMetrics.widthPixels / 4 * 3, 0);
        final String order_id = orderEntity.getOrder_id();
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mIsShowCancelPop) {
                    String instrument_id = orderEntity.getExchange_id() + "." + orderEntity.getInstrument_id();
                    String ins_name = instrument_id;
                    SearchEntity searchEntity = LatestFileManager.getSearchEntities().get(instrument_id);
                    if (searchEntity != null) ins_name = searchEntity.getInstrumentName();
                    String direction_title = ((TextView) view.findViewById(R.id.order_offset)).getText().toString();
                    String volume = orderEntity.getVolume_left();
                    String price = ((TextView) view.findViewById(R.id.order_price)).getText().toString();
                    initDialog(order_id, ins_name, direction_title, volume, price);
                    popWindow.dismiss();
                } else {
                    BaseApplication.getmTDWebSocket().sendReqCancelOrder(order_id);
                    popWindow.dismiss();
                }
            }
        });
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
        TextView tv_ins = view.findViewById(R.id.order_instrument_id);
        TextView tv_price = view.findViewById(R.id.order_price);
        TextView tv_direction = view.findViewById(R.id.order_direction);
        TextView tv_volume = view.findViewById(R.id.order_volume);
        TextView ok = view.findViewById(R.id.order_ok);
        TextView cancel = view.findViewById(R.id.order_cancel);
        tv_ins.setText(instrument_id);
        tv_price.setText(price);
        tv_direction.setText(direction_title);
        tv_volume.setText(volume);
        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    BaseApplication.getmTDWebSocket().sendReqCancelOrder(order_id);
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

    /**
     * date: 2019/7/3
     * author: chenli
     * description: 设置合约id
     */
    public void setInstrument_id(String ins) {
        mInstrumentId = ins;
    }

    /**
     * date: 7/9/17
     * author: chenli
     * description: 接收持仓点击、自选点击、搜索页点击发来的合约，用于更新高亮合约
     */
    @Subscribe
    public void onEvent(IdEvent data) {
        if (!mIsInAccountFragment) {
            mInstrumentId = data.getInstrument_id();
            mAdapter.updateHighlightIns(mInstrumentId);
        }
    }

    /**
     * date: 2019/5/28
     * author: chenli
     * description: 撤单开关切换
     */
    @Subscribe
    public void onEventSetting(OrderSettingEvent data) {
        mIsShowCancelPop = data.isCancelPopup();
    }


}

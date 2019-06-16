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
import com.shinnytech.futures.amplitude.api.Amplitude;
import com.shinnytech.futures.application.BaseApplication;
import com.shinnytech.futures.constants.CommonConstants;
import com.shinnytech.futures.controller.activity.FutureInfoActivity;
import com.shinnytech.futures.controller.activity.MainActivity;
import com.shinnytech.futures.databinding.FragmentOrderBinding;
import com.shinnytech.futures.model.adapter.OrderAdapter;
import com.shinnytech.futures.model.bean.accountinfobean.AccountEntity;
import com.shinnytech.futures.model.bean.accountinfobean.OrderEntity;
import com.shinnytech.futures.model.bean.accountinfobean.PositionEntity;
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
import com.shinnytech.futures.utils.LogUtils;
import com.shinnytech.futures.utils.MathUtils;
import com.shinnytech.futures.utils.SPUtils;
import com.shinnytech.futures.utils.ScreenUtils;
import com.shinnytech.futures.utils.TimeUtils;
import com.shinnytech.futures.utils.ToastUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.shinnytech.futures.application.BaseApplication.TD_BROADCAST_ACTION;
import static com.shinnytech.futures.constants.CommonConstants.AMP_EVENT_BALANCE;
import static com.shinnytech.futures.constants.CommonConstants.AMP_EVENT_BROKER_ID;
import static com.shinnytech.futures.constants.CommonConstants.AMP_EVENT_CURRENT_PAGE;
import static com.shinnytech.futures.constants.CommonConstants.AMP_EVENT_IS_INS_IN_OPTIONAL;
import static com.shinnytech.futures.constants.CommonConstants.AMP_EVENT_IS_INS_IN_POSITION;
import static com.shinnytech.futures.constants.CommonConstants.AMP_EVENT_IS_POSITIVE;
import static com.shinnytech.futures.constants.CommonConstants.AMP_EVENT_ORDER_COUNT;
import static com.shinnytech.futures.constants.CommonConstants.AMP_EVENT_PAGE_ID;
import static com.shinnytech.futures.constants.CommonConstants.AMP_EVENT_PAGE_ID_VALUE_ACCOUNT;
import static com.shinnytech.futures.constants.CommonConstants.AMP_EVENT_PAGE_ID_VALUE_FUTURE_INFO;
import static com.shinnytech.futures.constants.CommonConstants.AMP_EVENT_PAGE_VALUE_FUTURE_INFO;
import static com.shinnytech.futures.constants.CommonConstants.AMP_EVENT_PAGE_VALUE_MAIN;
import static com.shinnytech.futures.constants.CommonConstants.AMP_EVENT_PAGE_VISIBLE_TIME;
import static com.shinnytech.futures.constants.CommonConstants.AMP_EVENT_POSITION_COUNT;
import static com.shinnytech.futures.constants.CommonConstants.AMP_EVENT_SUB_PAGE_ID;
import static com.shinnytech.futures.constants.CommonConstants.AMP_EVENT_SUB_PAGE_ID_VALUE_ALIVE;
import static com.shinnytech.futures.constants.CommonConstants.AMP_EVENT_SUB_PAGE_ID_VALUE_ORDER;
import static com.shinnytech.futures.constants.CommonConstants.AMP_EVENT_TARGET_PAGE;
import static com.shinnytech.futures.constants.CommonConstants.AMP_LEAVE_PAGE;
import static com.shinnytech.futures.constants.CommonConstants.AMP_SHOW_PAGE;
import static com.shinnytech.futures.constants.CommonConstants.AMP_SWITCH_PAGE;
import static com.shinnytech.futures.constants.CommonConstants.CONFIG_BROKER;
import static com.shinnytech.futures.constants.CommonConstants.CONFIG_LOGIN_DATE;
import static com.shinnytech.futures.constants.CommonConstants.INS_BETWEEN_ACTIVITY;
import static com.shinnytech.futures.constants.CommonConstants.MAIN_ACTIVITY_TO_FUTURE_INFO_ACTIVITY;
import static com.shinnytech.futures.constants.CommonConstants.STATUS_ALIVE;
import static com.shinnytech.futures.constants.CommonConstants.TD_MESSAGE;

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
    private long mShowTime;
    private String mInstrumentId;

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
        mIsUpdate = true;
        mBinding.rv.setLayoutManager(
                new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
        mBinding.rv.addItemDecoration(
                new DividerItemDecorationUtils(getActivity(), DividerItemDecorationUtils.VERTICAL_LIST));
        mAdapter = new OrderAdapter(getActivity(), mOldData);
        mBinding.rv.setAdapter(mAdapter);
        mIsShowCancelPop = (boolean) SPUtils.get(BaseApplication.getContext(), CommonConstants.CONFIG_CANCEL_ORDER_CONFIRM, true);
        mInstrumentId = "";
        if (getActivity() instanceof FutureInfoActivity) {
            mInstrumentId = ((FutureInfoActivity) getActivity()).getInstrument_id();
            mAdapter.setHighlightIns(mInstrumentId);
        }
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
                        ((MainActivity) getActivity()).getmMainActivityPresenter()
                                .setPreSubscribedQuotes(sDataManager.getRtnData().getIns_list());

                        sDataManager.IS_SHOW_VP_CONTENT = true;
                        Intent intent = new Intent(getActivity(), FutureInfoActivity.class);
                        intent.putExtra(INS_BETWEEN_ACTIVITY, orderEntity.getExchange_id()
                                + "." + orderEntity.getInstrument_id());
                        getActivity().startActivityForResult(intent, MAIN_ACTIVITY_TO_FUTURE_INFO_ACTIVITY);
                        JSONObject jsonObject = new JSONObject();
                        try {
                            jsonObject.put(AMP_EVENT_CURRENT_PAGE, AMP_EVENT_PAGE_VALUE_MAIN);
                            jsonObject.put(AMP_EVENT_TARGET_PAGE, AMP_EVENT_PAGE_VALUE_FUTURE_INFO);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        Amplitude.getInstance().logEvent(AMP_SWITCH_PAGE, jsonObject);
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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        EventBus.getDefault().unregister(this);
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
            if (!mIsUpdate) return;
            UserEntity userEntity = sDataManager.getTradeBean().getUsers().get(sDataManager.LOGIN_USER_ID);
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

    @Override
    public void show() {
        try {
            refreshOrder();
            showEvent();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void showEvent() {
        try {
            LogUtils.e("showOrder", true);
            mShowTime = System.currentTimeMillis();
            String broker_id = (String) SPUtils.get(BaseApplication.getContext(), CONFIG_BROKER, "");
            JSONObject jsonObject = new JSONObject();
            if (getActivity() instanceof MainActivity) {
                jsonObject.put(AMP_EVENT_PAGE_ID, AMP_EVENT_PAGE_ID_VALUE_ACCOUNT);
            } else {
                String ins = mInstrumentId;
                boolean isInsInOptional = LatestFileManager.getOptionalInsList().containsKey(ins);
                jsonObject.put(AMP_EVENT_IS_INS_IN_OPTIONAL, isInsInOptional);
                jsonObject.put(AMP_EVENT_PAGE_ID, AMP_EVENT_PAGE_ID_VALUE_FUTURE_INFO);
            }
            if (mIsOrdersAlive)
                jsonObject.put(AMP_EVENT_SUB_PAGE_ID, AMP_EVENT_SUB_PAGE_ID_VALUE_ALIVE);
            else jsonObject.put(AMP_EVENT_SUB_PAGE_ID, AMP_EVENT_SUB_PAGE_ID_VALUE_ORDER);
            jsonObject.put(AMP_EVENT_BROKER_ID, broker_id);
            jsonObject.put(AMP_EVENT_IS_POSITIVE, DataManager.getInstance().IS_POSITIVE);
            UserEntity userEntity = DataManager.getInstance().getTradeBean().getUsers().get(DataManager.getInstance().LOGIN_USER_ID);
            if (userEntity != null) {
                AccountEntity accountEntity = userEntity.getAccounts().get("CNY");
                if (accountEntity != null) {
                    String static_balance = MathUtils.round(accountEntity.getStatic_balance(), 2);
                    jsonObject.put(AMP_EVENT_BALANCE, static_balance);
                    int positionCount = 0;
                    int orderCount = 0;
                    for (PositionEntity positionEntity :
                            userEntity.getPositions().values()) {
                        int volume_long = Integer.parseInt(positionEntity.getVolume_long());
                        int volume_short = Integer.parseInt(positionEntity.getVolume_short());
                        if (volume_long != 0 || volume_short != 0) positionCount += 1;
                    }

                    for (OrderEntity orderEntity :
                            userEntity.getOrders().values()) {
                        if (STATUS_ALIVE.equals(orderEntity.getStatus())) orderCount += 1;
                    }
                    jsonObject.put(AMP_EVENT_POSITION_COUNT, positionCount);
                    jsonObject.put(AMP_EVENT_ORDER_COUNT, orderCount);

                    if (getActivity() instanceof FutureInfoActivity) {
                        String ins = ((FutureInfoActivity) getActivity()).getInstrument_id();
                        boolean isInsInPosition = userEntity.getPositions().keySet().contains(ins);
                        jsonObject.put(AMP_EVENT_IS_INS_IN_POSITION, isInsInPosition);
                    }
                }
            }
            Amplitude.getInstance().logEvent(AMP_SHOW_PAGE, jsonObject);
            DataManager.getInstance().IS_POSITIVE = false;
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    @Override
    public void leave() {
        leaveEvent();
    }

    public void leaveEvent() {
        try {
            LogUtils.e("leaveOrder", true);
            long pageVisibleTime = System.currentTimeMillis() - mShowTime;
            String broker_id = (String) SPUtils.get(BaseApplication.getContext(), CONFIG_BROKER, "");
            JSONObject jsonObject = new JSONObject();
            if (getActivity() instanceof MainActivity) {
                jsonObject.put(AMP_EVENT_PAGE_ID, AMP_EVENT_PAGE_ID_VALUE_ACCOUNT);
            } else {
                String ins = mInstrumentId;
                boolean isInsInOptional = LatestFileManager.getOptionalInsList().containsKey(ins);
                jsonObject.put(AMP_EVENT_IS_INS_IN_OPTIONAL, isInsInOptional);
                jsonObject.put(AMP_EVENT_PAGE_ID, AMP_EVENT_PAGE_ID_VALUE_FUTURE_INFO);
            }
            if (mIsOrdersAlive)
                jsonObject.put(AMP_EVENT_SUB_PAGE_ID, AMP_EVENT_SUB_PAGE_ID_VALUE_ALIVE);
            else jsonObject.put(AMP_EVENT_SUB_PAGE_ID, AMP_EVENT_SUB_PAGE_ID_VALUE_ORDER);
            jsonObject.put(AMP_EVENT_BROKER_ID, broker_id);
            jsonObject.put(AMP_EVENT_IS_POSITIVE, DataManager.getInstance().IS_POSITIVE);
            jsonObject.put(AMP_EVENT_PAGE_VISIBLE_TIME, pageVisibleTime);
            UserEntity userEntity = DataManager.getInstance().getTradeBean().getUsers().get(DataManager.getInstance().LOGIN_USER_ID);
            if (userEntity != null) {
                AccountEntity accountEntity = userEntity.getAccounts().get("CNY");
                if (accountEntity != null) {
                    String static_balance = MathUtils.round(accountEntity.getStatic_balance(), 2);
                    jsonObject.put(AMP_EVENT_BALANCE, static_balance);
                    int positionCount = 0;
                    int orderCount = 0;
                    for (PositionEntity positionEntity :
                            userEntity.getPositions().values()) {
                        int volume_long = Integer.parseInt(positionEntity.getVolume_long());
                        int volume_short = Integer.parseInt(positionEntity.getVolume_short());
                        if (volume_long != 0 || volume_short != 0) positionCount += 1;
                    }

                    for (OrderEntity orderEntity :
                            userEntity.getOrders().values()) {
                        if (STATUS_ALIVE.equals(orderEntity.getStatus())) orderCount += 1;
                    }
                    jsonObject.put(AMP_EVENT_POSITION_COUNT, positionCount);
                    jsonObject.put(AMP_EVENT_ORDER_COUNT, orderCount);

                    if (getActivity() instanceof FutureInfoActivity) {
                        String ins = ((FutureInfoActivity) getActivity()).getInstrument_id();
                        boolean isInsInPosition = userEntity.getPositions().keySet().contains(ins);
                        jsonObject.put(AMP_EVENT_IS_INS_IN_POSITION, isInsInPosition);
                    }

                }
            }
            Amplitude.getInstance().logEvent(AMP_LEAVE_PAGE, jsonObject);
        } catch (JSONException e) {
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
     * date: 7/9/17
     * author: chenli
     * description: 接收持仓点击、自选点击、搜索页点击发来的合约，用于更新高亮合约
     */
    @Subscribe
    public void onEvent(IdEvent data) {
        if (getActivity() instanceof FutureInfoActivity)
            mAdapter.updateHighlightIns(data.getInstrument_id());
    }

    /**
     * date: 2019/5/28
     * author: chenli
     * description: 撤单开关切换
     */
    @Subscribe
    public void onEventSetting(OrderSettingEvent data) {
        mIsShowCancelPop = data.isPopup();
    }


}

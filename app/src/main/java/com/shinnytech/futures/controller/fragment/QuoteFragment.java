package com.shinnytech.futures.controller.fragment;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.databinding.DataBindingUtil;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.shinnytech.futures.R;
import com.shinnytech.futures.application.BaseApplication;
import com.shinnytech.futures.controller.activity.FutureInfoActivity;
import com.shinnytech.futures.controller.activity.MainActivity;
import com.shinnytech.futures.databinding.FragmentQuoteBinding;
import com.shinnytech.futures.model.adapter.DragDialogAdapter;
import com.shinnytech.futures.model.adapter.QuoteAdapter;
import com.shinnytech.futures.amplitude.api.Amplitude;
import com.shinnytech.futures.model.bean.accountinfobean.AccountEntity;
import com.shinnytech.futures.model.bean.accountinfobean.OrderEntity;
import com.shinnytech.futures.model.bean.accountinfobean.PositionEntity;
import com.shinnytech.futures.model.bean.accountinfobean.UserEntity;
import com.shinnytech.futures.model.bean.eventbusbean.PositionEvent;
import com.shinnytech.futures.model.bean.eventbusbean.UpdateEvent;
import com.shinnytech.futures.model.bean.futureinfobean.QuoteEntity;
import com.shinnytech.futures.model.engine.DataManager;
import com.shinnytech.futures.model.engine.LatestFileManager;
import com.shinnytech.futures.model.listener.QuoteDiffCallback;
import com.shinnytech.futures.model.listener.SimpleRecyclerViewItemClickListener;
import com.shinnytech.futures.service.WebSocketService;
import com.shinnytech.futures.utils.CloneUtils;
import com.shinnytech.futures.utils.ScreenUtils;
import com.shinnytech.futures.utils.DividerItemDecorationUtils;
import com.shinnytech.futures.utils.LogUtils;
import com.shinnytech.futures.utils.MathUtils;
import com.shinnytech.futures.utils.SPUtils;
import com.shinnytech.futures.utils.ToastUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.shinnytech.futures.constants.CommonConstants.AMP_EVENT_BALANCE;
import static com.shinnytech.futures.constants.CommonConstants.AMP_EVENT_BROKER_ID;
import static com.shinnytech.futures.constants.CommonConstants.AMP_EVENT_CURRENT_PAGE;
import static com.shinnytech.futures.constants.CommonConstants.AMP_EVENT_IS_POSITIVE;
import static com.shinnytech.futures.constants.CommonConstants.AMP_EVENT_OPTIONAL_DIRECTION;
import static com.shinnytech.futures.constants.CommonConstants.AMP_EVENT_OPTIONAL_DIRECTION_VALUE_ADD;
import static com.shinnytech.futures.constants.CommonConstants.AMP_EVENT_OPTIONAL_DIRECTION_VALUE_DELETE;
import static com.shinnytech.futures.constants.CommonConstants.AMP_EVENT_OPTIONAL_INSTRUMENT_ID;
import static com.shinnytech.futures.constants.CommonConstants.AMP_EVENT_ORDER_COUNT;
import static com.shinnytech.futures.constants.CommonConstants.AMP_EVENT_PAGE_ID;
import static com.shinnytech.futures.constants.CommonConstants.AMP_EVENT_PAGE_ID_VALUE_MAIN;
import static com.shinnytech.futures.constants.CommonConstants.AMP_EVENT_PAGE_VALUE_FUTURE_INFO;
import static com.shinnytech.futures.constants.CommonConstants.AMP_EVENT_PAGE_VALUE_MAIN;
import static com.shinnytech.futures.constants.CommonConstants.AMP_EVENT_PAGE_VISIBLE_TIME;
import static com.shinnytech.futures.constants.CommonConstants.AMP_EVENT_POSITION_COUNT;
import static com.shinnytech.futures.constants.CommonConstants.AMP_EVENT_SUB_PAGE_ID;
import static com.shinnytech.futures.constants.CommonConstants.AMP_EVENT_SUB_PAGE_ID_VALUE_QUOTE;
import static com.shinnytech.futures.constants.CommonConstants.AMP_EVENT_TARGET_PAGE;
import static com.shinnytech.futures.constants.CommonConstants.AMP_LEAVE_PAGE;
import static com.shinnytech.futures.constants.CommonConstants.AMP_OPTIONAL_QUOTE;
import static com.shinnytech.futures.constants.CommonConstants.AMP_SHOW_PAGE;
import static com.shinnytech.futures.constants.CommonConstants.AMP_SWITCH_PAGE;
import static com.shinnytech.futures.constants.CommonConstants.CONFIG_BROKER;
import static com.shinnytech.futures.constants.CommonConstants.CONFIG_RECOMMEND_OPTIONAL;
import static com.shinnytech.futures.constants.CommonConstants.DALIAN;
import static com.shinnytech.futures.constants.CommonConstants.DALIANZUHE;
import static com.shinnytech.futures.constants.CommonConstants.DOMINANT;
import static com.shinnytech.futures.constants.CommonConstants.INS_BETWEEN_ACTIVITY;
import static com.shinnytech.futures.constants.CommonConstants.LOAD_QUOTE_NUM;
import static com.shinnytech.futures.constants.CommonConstants.MAIN_ACTIVITY_TO_FUTURE_INFO_ACTIVITY;
import static com.shinnytech.futures.constants.CommonConstants.MD_MESSAGE;
import static com.shinnytech.futures.constants.CommonConstants.NENGYUAN;
import static com.shinnytech.futures.constants.CommonConstants.OPTIONAL;
import static com.shinnytech.futures.constants.CommonConstants.SHANGHAI;
import static com.shinnytech.futures.constants.CommonConstants.STATUS_ALIVE;
import static com.shinnytech.futures.constants.CommonConstants.TD_MESSAGE;
import static com.shinnytech.futures.constants.CommonConstants.ZHENGZHOU;
import static com.shinnytech.futures.constants.CommonConstants.ZHENGZHOUZUHE;
import static com.shinnytech.futures.constants.CommonConstants.ZHONGJIN;
import static com.shinnytech.futures.service.WebSocketService.MD_BROADCAST_ACTION;
import static com.shinnytech.futures.service.WebSocketService.TD_BROADCAST_ACTION;

/**
 * date: 7/9/17
 * author: chenli
 * description: 行情页
 * version:
 * state: done
 */
public class QuoteFragment extends LazyLoadFragment {

    /**
     * date: 7/9/17
     * description: 页面标题
     */
    private static final String KEY_FRAGMENT_TITLE = "title";
    /**
     * date: 7/9/17
     * description: 数据刷新控制标志
     */
    private boolean mIsUpdate = true;
    private QuoteAdapter mAdapter;
    private DataManager mDataManager = DataManager.getInstance();
    private BroadcastReceiver mReceiver;
    private BroadcastReceiver mReceiverTrade;
    private String mTitle = DOMINANT;
    private TextView mToolbarTitle;
    private List<String> mInsList = new ArrayList<>();
    private List<QuoteEntity> mOldData = new ArrayList<>();
    private Map<String, QuoteEntity> mNewData = new LinkedHashMap<>();
    private FragmentQuoteBinding mBinding;
    private Dialog mDialog;
    private RecyclerView mRecyclerView;
    private DragDialogAdapter mDragDialogAdapter;
    private long mShowTime;

    /**
     * date: 7/9/17
     * author: chenli
     * description: 创建行情页实例
     */
    public static QuoteFragment newInstance(String title) {
        QuoteFragment fragment = new QuoteFragment();
        Bundle bundle = new Bundle();
        bundle.putString(KEY_FRAGMENT_TITLE, title);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mTitle = getArguments().getString(KEY_FRAGMENT_TITLE);
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_quote, container, false);
        initData();
        initEvent();
        return mBinding.getRoot();
    }

    private void initData() {
        mToolbarTitle = getActivity().findViewById(R.id.title_toolbar);
        mBinding.rvQuote.setLayoutManager(new LinearLayoutManager(getActivity()));
        mBinding.rvQuote.addItemDecoration(
                new DividerItemDecorationUtils(getActivity(), DividerItemDecorationUtils.VERTICAL_LIST));
        mBinding.rvQuote.setItemAnimator(new DefaultItemAnimator());
        mAdapter = new QuoteAdapter(getActivity(), mOldData, mTitle);
        mBinding.rvQuote.setAdapter(mAdapter);

        switch (mTitle) {
            case OPTIONAL:
                mNewData = LatestFileManager.getOptionalInsList();
                break;
            case DOMINANT:
                mNewData.putAll(LatestFileManager.getMainInsList());
                break;
            case SHANGHAI:
                //添加一下，不要一直排序
                mNewData.putAll(LatestFileManager.getShangqiInsList());
                break;
            case NENGYUAN:
                mNewData.putAll(LatestFileManager.getNengyuanInsList());
                break;
            case DALIAN:
                mNewData.putAll(LatestFileManager.getDalianInsList());
                break;
            case ZHENGZHOU:
                mNewData.putAll(LatestFileManager.getZhengzhouInsList());
                break;
            case ZHONGJIN:
                mNewData.putAll(LatestFileManager.getZhongjinInsList());
                break;
            case DALIANZUHE:
                mNewData.putAll(LatestFileManager.getDalianzuheInsList());
                break;
            case ZHENGZHOUZUHE:
                mNewData.putAll(LatestFileManager.getZhengzhouzuheInsList());
                break;
            default:
                break;
        }
        mInsList = new ArrayList<>(mNewData.keySet());
    }

    @Override
    public void show() {
        try {
            showEvent();

            if (DALIANZUHE.equals(mTitle) || ZHENGZHOUZUHE.equals(mTitle)) {
                mBinding.tvChangePercent.setText(R.string.quote_fragment_bid_price1);
                mBinding.tvOpenInterest.setText(R.string.quote_fragment_ask_price1);
            } else {
                mBinding.tvChangePercent.setText(R.string.quote_fragment_up_down_rate);
                mBinding.tvOpenInterest.setText(R.string.quote_fragment_open_interest);
            }

            refreshMD(mToolbarTitle.getText().toString());
            refreshTD();

            mBinding.rvQuote.scrollToPosition(0);
            if (OPTIONAL.equals(mTitle)) {
                List<String> ins;
                if (mInsList.size() <= LOAD_QUOTE_NUM) {
                    ins = mInsList;
                } else {
                    ins = mInsList.subList(0, LOAD_QUOTE_NUM);
                }
                sendSubscribeQuotes(ins);
            } else {
                if (mInsList.size() <= LOAD_QUOTE_NUM) {
                    sendSubscribeQuotes(mInsList);
                } else {
                    List<String> insList = mInsList.subList(0, LOAD_QUOTE_NUM);
                    sendSubscribeQuotes(insList);
                }
            }

        } catch (IndexOutOfBoundsException e) {
            e.printStackTrace();
        }
    }

    /**
     * date: 2019/5/25
     * author: chenli
     * description: 进入页面上报
     */
    public void showEvent() {
        try {
            LogUtils.e("quoteShow", true);
            mShowTime = System.currentTimeMillis();
            String broker_id = (String) SPUtils.get(BaseApplication.getContext(), CONFIG_BROKER, "");
            JSONObject jsonObject = new JSONObject();
            jsonObject.put(AMP_EVENT_PAGE_ID, AMP_EVENT_PAGE_ID_VALUE_MAIN);
            jsonObject.put(AMP_EVENT_SUB_PAGE_ID, AMP_EVENT_SUB_PAGE_ID_VALUE_QUOTE + "_" + mTitle);
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

    /**
     * date: 2019/5/25
     * author: chenli
     * description: 离开页面上报
     */
    public void leaveEvent() {
        try {
            LogUtils.e("quoteLeave", true);
            long pageVisibleTime = System.currentTimeMillis() - mShowTime;
            String broker_id = (String) SPUtils.get(BaseApplication.getContext(), CONFIG_BROKER, "");
            JSONObject jsonObject = new JSONObject();
            jsonObject.put(AMP_EVENT_PAGE_ID, AMP_EVENT_PAGE_ID_VALUE_MAIN);
            jsonObject.put(AMP_EVENT_SUB_PAGE_ID, AMP_EVENT_SUB_PAGE_ID_VALUE_QUOTE + "_" + mTitle);
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
                }
            }
            Amplitude.getInstance().logEvent(AMP_LEAVE_PAGE, jsonObject);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * date: 2019/4/14
     * author: chenli
     * description: 订阅行情
     */
    private void sendSubscribeQuotes(List<String> insList) {

        if (DALIANZUHE.equals(mTitle) || ZHENGZHOUZUHE.equals(mTitle) || OPTIONAL.equals(mTitle)) {
            WebSocketService.
                    sendSubscribeQuote(TextUtils.join(",", LatestFileManager.getCombineInsList(insList)));
        } else {
            WebSocketService.
                    sendSubscribeQuote(TextUtils.join(",", insList));
        }
    }


    /**
     * date: 2019/4/14
     * author: chenli
     * description: 初始化监听器
     */
    private void initEvent() {

        mBinding.tvChangePercent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (DALIANZUHE.equals(mTitle) || ZHENGZHOUZUHE.equals(mTitle)) {
                    switch (mBinding.tvChangePercent.getText().toString()) {
                        case "买价 ⇲":
                            mBinding.tvChangePercent.setText("买量 ⇲");
                            mAdapter.switchChangeView();
                            break;
                        case "买量 ⇲":
                            mBinding.tvChangePercent.setText("买价 ⇲");
                            mAdapter.switchChangeView();
                            break;
                    }
                } else {
                    switch (mBinding.tvChangePercent.getText().toString()) {
                        case "涨跌幅% ⇲":
                            mBinding.tvChangePercent.setText("涨跌 ⇲");
                            mAdapter.switchChangeView();
                            break;
                        case "涨跌 ⇲":
                            mBinding.tvChangePercent.setText("涨跌幅% ⇲");
                            mAdapter.switchChangeView();
                            break;
                    }
                }
            }
        });

        mBinding.tvOpenInterest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (DALIANZUHE.equals(mTitle) || ZHENGZHOUZUHE.equals(mTitle)) {
                    switch (mBinding.tvOpenInterest.getText().toString()) {
                        case "卖价 ⇲":
                            mBinding.tvOpenInterest.setText("卖量 ⇲");
                            mAdapter.switchVolView();
                            break;
                        case "卖量 ⇲":
                            mBinding.tvOpenInterest.setText("卖价 ⇲");
                            mAdapter.switchVolView();
                            break;
                    }
                } else {
                    switch (mBinding.tvOpenInterest.getText().toString()) {
                        case "持仓量 ⇲":
                            mBinding.tvOpenInterest.setText("成交量 ⇲");
                            mAdapter.switchVolView();
                            break;
                        case "成交量 ⇲":
                            mBinding.tvOpenInterest.setText("持仓量 ⇲");
                            mAdapter.switchVolView();
                            break;
                    }
                }

            }
        });

        mBinding.rvQuote.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                switch (newState) {
                    case RecyclerView.SCROLL_STATE_IDLE:
                        mIsUpdate = true;
                        try {
                            LinearLayoutManager lm = (LinearLayoutManager) recyclerView.getLayoutManager();
                            int firstVisibleItemPosition = lm.findFirstVisibleItemPosition();
                            int lastVisibleItemPosition = lm.findLastVisibleItemPosition();
                            List<String> insList = mInsList.subList(firstVisibleItemPosition, lastVisibleItemPosition + 1);
                            sendSubscribeQuotes(insList);
                        } catch (IndexOutOfBoundsException e) {
                            e.printStackTrace();
                        }
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

        //recyclerView的单击与长按事件，单击跳转到合约详情页，长按添加或删除合约到自选合约列表, 把recyclerView传入
        //防止viewpager滑动后
        mBinding.rvQuote.addOnItemTouchListener(new SimpleRecyclerViewItemClickListener(mBinding.rvQuote,
                new SimpleRecyclerViewItemClickListener.OnItemClickListener() {

                    @Override
                    public void onItemClick(View view, int position) {
                        if (position >= 0 && position < mAdapter.getItemCount()) {
                            QuoteEntity quoteEntity = mAdapter.getData().get(position);
                            if (quoteEntity == null) return;
                            String instrument_id = quoteEntity.getInstrument_id();
                            //添加判断，防止自选合约列表为空时产生无效的点击事件
                            if (instrument_id != null && !instrument_id.isEmpty()) {
                                if (getActivity() instanceof MainActivity) {
                                    ((MainActivity) getActivity()).getmMainActivityPresenter()
                                            .setPreSubscribedQuotes(mDataManager.getRtnData().getIns_list());
                                }
                                Intent intent = new Intent(getActivity(), FutureInfoActivity.class);
                                intent.putExtra(INS_BETWEEN_ACTIVITY, instrument_id);
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
                        Vibrator vibrator = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);
                        vibrator.vibrate(70);
                        if (mAdapter.getData().get(position) != null) {
                            String instrument_id = mAdapter.getData().get(position).getInstrument_id();
                            if (instrument_id == null || instrument_id.isEmpty()) return;
                            Map<String, QuoteEntity> insList = LatestFileManager.getOptionalInsList();
                            if (insList.containsKey(instrument_id)) {
                                initPopUp(view, instrument_id, insList, false);
                            } else {
                                initPopUp(view, instrument_id, insList, true);
                            }

                        }
                    }

                    private void initPopUp(View view, final String instrument_id, final Map<String, QuoteEntity> insList, final boolean isAdd) {
                        //构造一个添加自选合约合约的PopupWindow
                        final View popUpView = View.inflate(getActivity(), R.layout.popup_fragment_quote, null);
                        final PopupWindow popWindow = new PopupWindow(popUpView,
                                ViewGroup.LayoutParams.MATCH_PARENT, ScreenUtils.dp2px(getActivity(), 42), true);
                        //设置动画，淡入淡出
                        popWindow.setAnimationStyle(R.style.anim_menu_quote);
                        //点击空白处popupWindow消失
                        popWindow.setBackgroundDrawable(new ColorDrawable(0x00000000));
                        TextView add = popUpView.findViewById(R.id.add_remove_quote);
                        TextView trade = popUpView.findViewById(R.id.trade_quote);
                        TextView drag = popUpView.findViewById(R.id.drag_quote);
                        if (OPTIONAL.equals(mTitle)) drag.setVisibility(View.VISIBLE);
                        else drag.setVisibility(View.INVISIBLE);
                        if (isAdd) add.setText("添加");
                        else add.setText("删除");

                        //设置popupWindow显示的位置，参数依次是参照View，x轴的偏移量，y轴的偏移量
                        DisplayMetrics outMetrics = new DisplayMetrics();
                        getActivity().getWindowManager().getDefaultDisplay().getMetrics(outMetrics);
                        popWindow.showAsDropDown(view, outMetrics.widthPixels / 4 * 3, 0);

                        drag.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                popWindow.dismiss();
                                if (mDragDialogAdapter != null)
                                    mDragDialogAdapter.updateList(new ArrayList<>(LatestFileManager.getOptionalInsList().keySet()));

                                if (mDialog == null) {
                                    //初始化自选合约弹出框
                                    mDialog = new Dialog(getActivity(), R.style.Theme_Light_Dialog);
                                    View viewDialog = View.inflate(getActivity(), R.layout.view_dialog_optional_drag_quote, null);
                                    Window dialogWindow = mDialog.getWindow();
                                    if (dialogWindow != null) {
                                        dialogWindow.getDecorView().setPadding(0, 0, 0, 0);
                                        WindowManager.LayoutParams lp = dialogWindow.getAttributes();
                                        dialogWindow.setGravity(Gravity.CENTER);
                                        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
                                        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
                                        dialogWindow.setAttributes(lp);
                                    }
                                    mDialog.setContentView(viewDialog);
                                    mDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                                        @Override
                                        public void onDismiss(DialogInterface dialog) {
                                            refreshOptional();
                                        }
                                    });
                                    mDragDialogAdapter = new DragDialogAdapter(getActivity(), new ArrayList<>(insList.keySet()));
                                    mRecyclerView = viewDialog.findViewById(R.id.dialog_rv);
                                    viewDialog.findViewById(R.id.iv_close).setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            mDialog.dismiss();
                                        }
                                    });

                                    mRecyclerView.setLayoutManager(
                                            new LinearLayoutManager(getActivity()));
                                    mRecyclerView.setAdapter(mDragDialogAdapter);
                                    final ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new ItemTouchHelper.Callback() {
                                        @Override
                                        public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
                                            int swipeFlag = 0;
                                            int dragFlag = ItemTouchHelper.UP | ItemTouchHelper.DOWN;
                                            return makeMovementFlags(dragFlag, swipeFlag);
                                        }


                                        @Override
                                        public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                                            mDragDialogAdapter.onItemMove(viewHolder.getAdapterPosition(), target.getAdapterPosition());
                                            return true;
                                        }

                                        @Override
                                        public void onSelectedChanged(RecyclerView.ViewHolder viewHolder, int actionState) {
                                            super.onSelectedChanged(viewHolder, actionState);
                                            if (actionState == ItemTouchHelper.ACTION_STATE_DRAG) {
                                                Vibrator vibrator = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);
                                                vibrator.vibrate(70);
                                            }
                                            if (actionState == ItemTouchHelper.ACTION_STATE_IDLE)
                                                mDragDialogAdapter.saveOptionalList();
                                        }

                                        @Override
                                        public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                                            //暂不处理
                                        }

                                        @Override
                                        public boolean canDropOver(RecyclerView recyclerView, RecyclerView.ViewHolder current, RecyclerView.ViewHolder target) {
                                            return true;
                                        }

                                        @Override
                                        public boolean isLongPressDragEnabled() {
                                            //return true后，可以实现长按拖动排序和拖动动画了
                                            return true;
                                        }
                                    });
                                    itemTouchHelper.attachToRecyclerView(mRecyclerView);
                                    mDragDialogAdapter.setItemTouchHelper(itemTouchHelper);
                                }

                                if (!mDialog.isShowing()) mDialog.show();
                            }
                        });

                        add.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                JSONObject jsonObject = new JSONObject();
                                try {
                                    jsonObject.put(AMP_EVENT_OPTIONAL_INSTRUMENT_ID, instrument_id);
                                    if (isAdd) {
                                        jsonObject.put(AMP_EVENT_OPTIONAL_DIRECTION, AMP_EVENT_OPTIONAL_DIRECTION_VALUE_ADD);
                                        QuoteEntity quoteEntity = new QuoteEntity();
                                        quoteEntity.setInstrument_id(instrument_id);
                                        insList.put(instrument_id, quoteEntity);
                                        LatestFileManager.saveInsListToFile(new ArrayList<>(insList.keySet()));
                                        getActivity().runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                popWindow.dismiss();
                                                ToastUtils.showToast(BaseApplication.getContext(),
                                                        "该合约已添加到自选列表");
                                            }
                                        });
                                    } else {
                                        jsonObject.put(AMP_EVENT_OPTIONAL_DIRECTION, AMP_EVENT_OPTIONAL_DIRECTION_VALUE_DELETE);
                                        insList.remove(instrument_id);
                                        LatestFileManager.saveInsListToFile(new ArrayList<>(insList.keySet()));
                                        refreshOptional();
                                        getActivity().runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                popWindow.dismiss();
                                                ToastUtils.showToast(BaseApplication.getContext(),
                                                        "该合约已被移除自选列表");
                                            }
                                        });
                                    }
                                    Amplitude.getInstance().logEvent(AMP_OPTIONAL_QUOTE, jsonObject);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        });

                        trade.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if (getActivity() instanceof MainActivity) {
                                    ((MainActivity) getActivity()).getmMainActivityPresenter()
                                            .setPreSubscribedQuotes(mDataManager.getRtnData().getIns_list());
                                }
                                DataManager.getInstance().IS_SHOW_VP_CONTENT = true;
                                Intent intentPos = new Intent(getActivity(), FutureInfoActivity.class);
                                intentPos.putExtra(INS_BETWEEN_ACTIVITY, instrument_id);
                                getActivity().startActivityForResult(intentPos, MAIN_ACTIVITY_TO_FUTURE_INFO_ACTIVITY);
                                popWindow.dismiss();
                            }
                        });
                    }
                }));

    }

    /**
     * date: 2019/4/14
     * author: chenli
     * description: 注册广播
     */
    private void registerBroaderCast() {
        mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String mDataString = intent.getStringExtra("msg");
                switch (mDataString) {
                    case MD_MESSAGE:
                        if (mIsUpdate) refreshMD(mToolbarTitle.getText().toString());
                        break;
                    default:
                        break;
                }
            }
        };
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mReceiver, new IntentFilter(MD_BROADCAST_ACTION));

        mReceiverTrade = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String mDataString = intent.getStringExtra("msg");
                switch (mDataString) {
                    case TD_MESSAGE:
                        refreshTD();
                        break;
                    default:
                        break;
                }
            }
        };
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mReceiverTrade, new IntentFilter(TD_BROADCAST_ACTION));

    }

    /**
     * date: 7/9/17
     * author: chenli
     * description: 根据主页标题和mTitle判断刷新不同行情页, 不显示的页面不刷
     */
    public void refreshMD(String title) {
        //防止相邻合约列表页面刷新
        if (!title.equals(mTitle)) return;
        try {
            String[] insList = mDataManager.getRtnData().getIns_list().split(",");
            for (String ins : insList) {
                try {
                    //防止合约页切换时,前一页的数据加载
                    if (mNewData.containsKey(ins)) {
                        QuoteEntity quoteEntity = CloneUtils.clone(mDataManager.getRtnData().getQuotes().get(ins));
                        if (DALIANZUHE.equals(mTitle) || ZHENGZHOUZUHE.equals(mTitle) || OPTIONAL.equals(mTitle)) {
                            if (ins.contains("&") && ins.contains(" "))
                                quoteEntity = LatestFileManager.calculateCombineQuotePart(quoteEntity);
                        }
                        mNewData.put(ins, quoteEntity);
                    }
                } catch (Exception e) {
                    continue;
                }
            }

            List<QuoteEntity> newData = new ArrayList<>(mNewData.values());
            DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new QuoteDiffCallback(mOldData, newData), false);
            mAdapter.setData(newData);
            diffResult.dispatchUpdatesTo(mAdapter);
            mOldData.clear();
            mOldData.addAll(newData);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * date: 2019/5/19
     * author: chenli
     * description: 刷新持仓挂单信息
     */
    public void refreshTD() {
        DataManager dataManager = DataManager.getInstance();
        UserEntity userEntity = dataManager.getTradeBean().getUsers().get(dataManager.LOGIN_USER_ID);
        if (userEntity == null) return;
        List<String> list = new ArrayList<>();

        for (PositionEntity positionEntity : userEntity.getPositions().values()) {
            String ins = positionEntity.getExchange_id() + "." + positionEntity.getInstrument_id();
            int volume_long = Integer.parseInt(positionEntity.getVolume_long());
            int volume_short = Integer.parseInt(positionEntity.getVolume_short());
            if (volume_long != 0 || volume_short != 0) list.add(ins);
        }

        for (OrderEntity orderEntity : userEntity.getOrders().values()) {
            if (STATUS_ALIVE.equals(orderEntity.getStatus())) {
                String ins = orderEntity.getExchange_id() + "." + orderEntity.getInstrument_id();
                if (!list.contains(ins)) list.add(ins);
            }
        }

        mAdapter.updateHighlightList(list);
    }

    @Override
    public void onResume() {
        super.onResume();
        EventBus.getDefault().register(this);
        registerBroaderCast();
    }

    @Override
    public void onPause() {
        super.onPause();
        EventBus.getDefault().unregister(this);
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mReceiver);
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mReceiverTrade);
    }

    //根据合约导航滑动行情列表
    @Subscribe
    public void onEvent(PositionEvent positionEvent) {
        if (mTitle.equals(mToolbarTitle.getText().toString())) {
            try {
                int position = positionEvent.getPosition();
                ((LinearLayoutManager) mBinding.rvQuote.getLayoutManager()).scrollToPositionWithOffset(position, 0);
                int visibleItemCount1 = mBinding.rvQuote.getChildCount();
                int lastPosition1 = (position + visibleItemCount1) > mInsList.size() ? mInsList.size() : (position + visibleItemCount1);
                int firstPosition1 = (lastPosition1 - position) != visibleItemCount1 ? (lastPosition1 - visibleItemCount1) : position;
                List<String> insList = mInsList.subList(firstPosition1, lastPosition1);
                sendSubscribeQuotes(insList);
            } catch (IndexOutOfBoundsException e) {
                e.printStackTrace();
            }
        }
    }

    //控制行情是否刷新
    @Subscribe
    public void onEvent(UpdateEvent updateEvent) {
        if (mTitle.equals(mToolbarTitle.getText().toString())) {
            switch (updateEvent.getState()) {
                case 1:
                    mIsUpdate = true;
                    break;
                case 2:
                    mIsUpdate = false;
                    break;
                default:
                    break;
            }
        }
    }

    /**
     * date: 2019/4/2
     * author: chenli
     * description: 配置新用户自选合约
     */
    public void initConfigOptional() {
        if (!SPUtils.contains(BaseApplication.getContext(), CONFIG_RECOMMEND_OPTIONAL)
                && LatestFileManager.getOptionalInsList().isEmpty()) {
            final Dialog dialog = new Dialog(getActivity(), R.style.Theme_Light_Dialog);
            View view = View.inflate(getActivity(), R.layout.view_dialog_init_optional, null);
            Window dialogWindow = dialog.getWindow();
            if (dialogWindow != null) {
                dialogWindow.getDecorView().setPadding(0, 0, 0, 0);
                WindowManager.LayoutParams lp = dialogWindow.getAttributes();
                dialogWindow.setGravity(Gravity.CENTER);
                lp.width = (int) getActivity().getResources().getDimension(R.dimen.optional_dialog_width);
                lp.height = (int) getActivity().getResources().getDimension(R.dimen.optional_dialog_height);
                dialogWindow.setAttributes(lp);
            }
            dialog.setContentView(view);
            dialog.setCancelable(false);
            dialog.show();

            dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    QuoteFragment.this.refreshOptional();
                }
            });

            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    UserEntity userEntity = mDataManager.getTradeBean().getUsers().get(mDataManager.LOGIN_USER_ID);
                    if (userEntity != null) {
                        Map<String, PositionEntity> positions = userEntity.getPositions();
                        //持仓合约
                        if (!positions.isEmpty()) {
                            List<String> list = new ArrayList<>();
                            for (PositionEntity positionEntity : positions.values()) {
                                int volume_long = Integer.parseInt(positionEntity.getVolume_long());
                                int volume_short = Integer.parseInt(positionEntity.getVolume_short());
                                if (!(volume_long == 0 && volume_short == 0)) {
                                    list.add(positionEntity.getExchange_id() + "." + positionEntity.getInstrument_id());
                                }
                            }
                            LatestFileManager.saveInsListToFile(list);
                        }
                    }
                    SPUtils.putAndApply(BaseApplication.getContext(), CONFIG_RECOMMEND_OPTIONAL, true);
                    dialog.dismiss();
                }
            }, 3000);
        }

    }

    public String getTitle() {
        return mTitle;
    }

    /**
     * date: 2019/3/29
     * author: chenli
     * description: 自选合约管理菜单返回，刷新自选合约
     */
    public void refreshOptional() {
        if (!OPTIONAL.equals(mTitle)) return;
        //先刷新列表再更新行情
        mNewData = LatestFileManager.getOptionalInsList();
        mInsList = new ArrayList<>(mNewData.keySet());
        List<QuoteEntity> newData = new ArrayList<>(mNewData.values());
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new QuoteDiffCallback(mOldData, newData), false);
        mAdapter.setData(newData);
        diffResult.dispatchUpdatesTo(mAdapter);
        mOldData.clear();
        mOldData.addAll(newData);
        try {
            List<String> insList;
            if (mInsList.size() < LOAD_QUOTE_NUM) insList = mInsList;
            else {
                LinearLayoutManager lm = (LinearLayoutManager) mBinding.rvQuote.getLayoutManager();
                int firstVisibleItemPosition = lm.findFirstVisibleItemPosition();
                int lastVisibleItemPosition = lm.findLastVisibleItemPosition();
                insList = mInsList.subList(firstVisibleItemPosition, lastVisibleItemPosition + 1);
            }
            sendSubscribeQuotes(insList);
        } catch (IndexOutOfBoundsException e) {
            e.printStackTrace();
        }
    }

}


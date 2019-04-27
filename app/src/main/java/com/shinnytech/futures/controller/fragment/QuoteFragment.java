package com.shinnytech.futures.controller.fragment;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.databinding.DataBindingUtil;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
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
import com.shinnytech.futures.model.adapter.QuoteAdapterRecommend;
import com.shinnytech.futures.model.bean.accountinfobean.PositionEntity;
import com.shinnytech.futures.model.bean.accountinfobean.UserEntity;
import com.shinnytech.futures.model.bean.eventbusbean.PositionEvent;
import com.shinnytech.futures.model.bean.eventbusbean.UpdateEvent;
import com.shinnytech.futures.model.bean.futureinfobean.QuoteEntity;
import com.shinnytech.futures.model.engine.DataManager;
import com.shinnytech.futures.model.engine.LatestFileManager;
import com.shinnytech.futures.model.listener.QuoteDiffCallback;
import com.shinnytech.futures.model.listener.RecommendQuoteDiffCallback;
import com.shinnytech.futures.model.listener.SimpleRecyclerViewItemClickListener;
import com.shinnytech.futures.utils.CloneUtils;
import com.shinnytech.futures.utils.DensityUtils;
import com.shinnytech.futures.utils.DividerItemDecorationUtils;
import com.shinnytech.futures.utils.LogUtils;
import com.shinnytech.futures.utils.MathUtils;
import com.shinnytech.futures.utils.SPUtils;
import com.shinnytech.futures.utils.ToastNotificationUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static com.shinnytech.futures.constants.CommonConstants.CONFIG_RECOMMEND_OPTIONAL;
import static com.shinnytech.futures.constants.CommonConstants.DALIAN;
import static com.shinnytech.futures.constants.CommonConstants.DALIANZUHE;
import static com.shinnytech.futures.constants.CommonConstants.DOMINANT;
import static com.shinnytech.futures.constants.CommonConstants.INS_BETWEEN_ACTIVITY;
import static com.shinnytech.futures.constants.CommonConstants.JUMP_TO_FUTURE_INFO_ACTIVITY;
import static com.shinnytech.futures.constants.CommonConstants.LOAD_QUOTE_NUM;
import static com.shinnytech.futures.constants.CommonConstants.LOAD_QUOTE_RECOMMEND_NUM;
import static com.shinnytech.futures.constants.CommonConstants.MD_MESSAGE;
import static com.shinnytech.futures.constants.CommonConstants.NENGYUAN;
import static com.shinnytech.futures.constants.CommonConstants.OPTIONAL;
import static com.shinnytech.futures.constants.CommonConstants.SHANGHAI;
import static com.shinnytech.futures.constants.CommonConstants.ZHENGZHOU;
import static com.shinnytech.futures.constants.CommonConstants.ZHENGZHOUZUHE;
import static com.shinnytech.futures.constants.CommonConstants.ZHONGJIN;
import static com.shinnytech.futures.model.service.WebSocketService.MD_BROADCAST_ACTION;

/**
 * date: 7/9/17
 * author: chenli
 * description: 行情页
 * version:
 * state: done
 */
public class QuoteFragment extends LazyLoadFragment {

    private static Comparator<String> comparator = new Comparator<String>() {
        @Override
        public int compare(String instrumentId1, String instrumentId2) {
            try {
                QuoteEntity quoteEntity1 = DataManager.getInstance().getRtnData().getQuotes().get(instrumentId1);
                QuoteEntity quoteEntity2 = DataManager.getInstance().getRtnData().getQuotes().get(instrumentId2);
                String change1 = MathUtils.divide(MathUtils.subtract(quoteEntity1.getLast_price(), quoteEntity1.getPre_settlement()), quoteEntity1.getPre_settlement());
                String change2 = MathUtils.divide(MathUtils.subtract(quoteEntity2.getLast_price(), quoteEntity2.getPre_settlement()), quoteEntity2.getPre_settlement());
                if (MathUtils.upper(change1, change2)) {
                    return -1;
                } else if (MathUtils.lower(change1, change2)){
                    return 1;
                }else return 1;
            } catch (Exception e) {
                e.printStackTrace();
                return 1;
            }
        }
    };

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
    private QuoteAdapterRecommend mAdapterRecommend;
    private DataManager mDataManager = DataManager.getInstance();
    private BroadcastReceiver mReceiver;
    private String mTitle = DOMINANT;
    private TextView mToolbarTitle;
    private List<String> mInsList = new ArrayList<>();
    private List<String> mInsListRecommend = new ArrayList<>();
    private List<QuoteEntity> mOldData = new ArrayList<>();
    private List<QuoteEntity> mOldDataRecommend = new ArrayList<>();
    private Map<String, QuoteEntity> mNewData = new LinkedHashMap<>();
    private Map<String, QuoteEntity> mNewDataRecommend = new LinkedHashMap<>();
    private Map<String, QuoteEntity> sortedRecommend = new TreeMap<>(comparator);
    private FragmentQuoteBinding mBinding;
    private Dialog mDialog;
    private RecyclerView mRecyclerView;
    private DragDialogAdapter mDragDialogAdapter;

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
        EventBus.getDefault().register(this);
        registerBroaderCast();
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

        mBinding.rvQuoteRecommend.setLayoutManager(new LinearLayoutManager(getActivity()));
        mBinding.rvQuoteRecommend.addItemDecoration(
                new DividerItemDecorationUtils(getActivity(), DividerItemDecorationUtils.VERTICAL_LIST));
        mBinding.rvQuoteRecommend.setItemAnimator(new DefaultItemAnimator());
        mAdapterRecommend = new QuoteAdapterRecommend(getActivity(), mOldDataRecommend);
        mBinding.rvQuoteRecommend.setAdapter(mAdapterRecommend);

        if (OPTIONAL.equals(mTitle)) {
            //新用户设置自选合约
            initConfigOptional();

            //配置推荐合约列表
            for (String ins : LatestFileManager.getMainInsList().keySet()) {
                QuoteEntity quoteEntity = CloneUtils.clone(DataManager.getInstance().getRtnData().getQuotes().get(ins));
                if (!LatestFileManager.getOptionalInsList().containsKey(ins))sortedRecommend.put(ins, quoteEntity);
            }

            mNewDataRecommend.putAll(sortedRecommend);
            mInsListRecommend = new ArrayList<>(mNewDataRecommend.keySet());
        }
    }

    @Override
    public void update() {
        try {
            mBinding.rvQuote.scrollToPosition(0);
            mBinding.rvQuoteRecommend.scrollToPosition(0);
            getQuoteInsList();
            if (OPTIONAL.equals(mTitle)) {
                List<String> ins;
                if (mInsList.size() <= LOAD_QUOTE_NUM) {
                    ins = mInsList;
                } else {
                    ins = mInsList.subList(0, LOAD_QUOTE_RECOMMEND_NUM);
                }
                List<String> insRecommend;
                if (mInsListRecommend.size() <= LOAD_QUOTE_NUM) {
                    insRecommend = mInsListRecommend;
                } else {
                    insRecommend = mInsListRecommend.subList(0, LOAD_QUOTE_RECOMMEND_NUM);
                }
                ins.addAll(insRecommend);
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
     * date: 2019/4/14
     * author: chenli
     * description: 根据标题获取不同合约列表
     */
    private void getQuoteInsList() {
        if (DALIANZUHE.equals(mTitle) || ZHENGZHOUZUHE.equals(mTitle)) {
            mBinding.tvChangePercent.setText(R.string.quote_fragment_bid_price1);
            mBinding.tvOpenInterest.setText(R.string.quote_fragment_bid_volume1);
        } else {
            mBinding.tvChangePercent.setText(R.string.quote_fragment_up_down_rate);
            mBinding.tvOpenInterest.setText(R.string.quote_fragment_open_interest);
        }

        //控制合约推荐显示与否
        if (OPTIONAL.equals(mTitle)) {
            mBinding.rvQuoteRecommend.setVisibility(View.VISIBLE);
            mBinding.tvRecommend.setVisibility(View.VISIBLE);
        } else {
            mBinding.rvQuoteRecommend.setVisibility(View.GONE);
            mBinding.tvRecommend.setVisibility(View.GONE);
        }

        switch (mTitle) {
            case OPTIONAL:
                mNewData = LatestFileManager.getOptionalInsList();
                break;
            case DOMINANT:
                mNewData.putAll(LatestFileManager.getMainInsList());
                break;
            case SHANGHAI:
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

    /**
     * date: 2019/4/14
     * author: chenli
     * description: 订阅行情
     */
    private void sendSubscribeQuotes(List<String> insList) {
        if (BaseApplication.getWebSocketService() == null) return;

        if (DALIANZUHE.equals(mTitle) || ZHENGZHOUZUHE.equals(mTitle) || OPTIONAL.equals(mTitle)) {
            BaseApplication.getWebSocketService().
                    sendSubscribeQuote(TextUtils.join(",", LatestFileManager.getCombineInsList(insList)));
        } else {
            BaseApplication.getWebSocketService().
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
                        case "买价":
                            mBinding.tvChangePercent.setText("卖价");
                            mAdapter.switchChangeView();
                            break;
                        case "卖价":
                            mBinding.tvChangePercent.setText("买价");
                            mAdapter.switchChangeView();
                            break;
                    }
                } else {
                    switch (mBinding.tvChangePercent.getText().toString()) {
                        case "涨跌幅%":
                            mBinding.tvChangePercent.setText("涨跌");
                            mAdapter.switchChangeView();
                            break;
                        case "涨跌":
                            mBinding.tvChangePercent.setText("涨跌幅%");
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
                        case "买量":
                            mBinding.tvOpenInterest.setText("卖量");
                            mAdapter.switchVolView();
                            break;
                        case "卖量":
                            mBinding.tvOpenInterest.setText("买量");
                            mAdapter.switchVolView();
                            break;
                    }
                } else {
                    switch (mBinding.tvOpenInterest.getText().toString()) {
                        case "持仓量":
                            mBinding.tvOpenInterest.setText("成交量");
                            mAdapter.switchVolView();
                            break;
                        case "成交量":
                            mBinding.tvOpenInterest.setText("持仓量");
                            mAdapter.switchVolView();
                            break;
                    }
                }

            }
        });

        mAdapterRecommend.setOnItemClickListener(new QuoteAdapterRecommend.OnItemClickListener() {
            @Override
            public void OnItemCollect(View view, String instrument_id, int position) {
                if (instrument_id == null || "".equals(instrument_id)) return;
                Map<String, QuoteEntity> insListOptional = LatestFileManager.getOptionalInsList();
                if (!insListOptional.containsKey(instrument_id)) {
                    QuoteEntity quoteEntity = new QuoteEntity();
                    quoteEntity.setInstrument_id(instrument_id);
                    insListOptional.put(instrument_id, quoteEntity);
                    LatestFileManager.saveInsListToFile(new ArrayList<>(insListOptional.keySet()));
                    mInsListRecommend.remove(position);
                    refreshOptional();
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ToastNotificationUtils.showToast(BaseApplication.getContext(),
                                    "该合约已添加到自选列表");
                        }
                    });
                }
            }
        });

        mBinding.rvQuoteRecommend.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                switch (newState) {
                    case RecyclerView.SCROLL_STATE_IDLE:
                        mIsUpdate = true;
                        try {
                            LinearLayoutManager lmRecommend = (LinearLayoutManager) recyclerView.getLayoutManager();
                            int firstVisibleItemPositionRecommend = lmRecommend.findFirstVisibleItemPosition();
                            int lastVisibleItemPositionRecommend = lmRecommend.findLastVisibleItemPosition();
                            List<String> insListRecommend = mInsListRecommend.subList(firstVisibleItemPositionRecommend, lastVisibleItemPositionRecommend + 1);
                            LinearLayoutManager lm = (LinearLayoutManager) mBinding.rvQuote.getLayoutManager();
                            int firstVisibleItemPosition = lm.findFirstVisibleItemPosition();
                            int lastVisibleItemPosition = lm.findLastVisibleItemPosition();
                            List<String> insList = mInsList.subList(firstVisibleItemPosition, lastVisibleItemPosition + 1);
                            insList.addAll(insListRecommend);
                            sendSubscribeQuotes(insList);
                        }catch (Exception e){
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
                            if (OPTIONAL.equals(mTitle)){
                                LinearLayoutManager lmRecommend = (LinearLayoutManager) mBinding.rvQuoteRecommend.getLayoutManager();
                                int firstVisibleItemPositionRecommend = lmRecommend.findFirstVisibleItemPosition();
                                int lastVisibleItemPositionRecommend = lmRecommend.findLastVisibleItemPosition();
                                List<String> insListRecommend = mInsListRecommend.subList(firstVisibleItemPositionRecommend, lastVisibleItemPositionRecommend + 1);
                                insList.addAll(insListRecommend);
                            }
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
                        if (mAdapter.getData().get(position) != null) {
                            String instrument_id = mAdapter.getData().get(position).getInstrument_id();
                            //添加判断，防止自选合约列表为空时产生无效的点击事件
                            if (instrument_id != null && !instrument_id.isEmpty()) {
                                if (getActivity() instanceof MainActivity) {
                                    ((MainActivity) getActivity()).getmMainActivityPresenter()
                                            .setPreSubscribedQuotes(mDataManager.getRtnData().getIns_list());
                                }
                                Intent intent = new Intent(getActivity(), FutureInfoActivity.class);
                                intent.putExtra(INS_BETWEEN_ACTIVITY, instrument_id);
                                startActivityForResult(intent, JUMP_TO_FUTURE_INFO_ACTIVITY);
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
                                ViewGroup.LayoutParams.MATCH_PARENT, DensityUtils.dp2px(getActivity(), 42), true);
                        //设置动画，淡入淡出
                        popWindow.setAnimationStyle(R.style.anim_menu_quote);
                        //点击空白处popupWindow消失
                        popWindow.setBackgroundDrawable(new ColorDrawable(0x00000000));
                        TextView add = popUpView.findViewById(R.id.add_remove_quote);
                        TextView trade = popUpView.findViewById(R.id.trade_quote);
                        TextView drag = popUpView.findViewById(R.id.drag_quote);
                        if (OPTIONAL.equals(mTitle)) drag.setVisibility(View.VISIBLE);
                        else drag.setVisibility(View.INVISIBLE);
                        if (isAdd) {
                            Drawable leftDrawable = ContextCompat.getDrawable(getActivity(), R.mipmap.ic_favorite_border_white_18dp);
                            if (leftDrawable != null)
                                leftDrawable.setBounds(0, 0, leftDrawable.getMinimumWidth(), leftDrawable.getMinimumHeight());
                            add.setCompoundDrawables(leftDrawable, null, null, null);
                            add.setText("添加自选");
                        } else {
                            Drawable leftDrawable = ContextCompat.getDrawable(getActivity(), R.mipmap.ic_favorite_white_18dp);
                            if (leftDrawable != null)
                                leftDrawable.setBounds(0, 0, leftDrawable.getMinimumWidth(), leftDrawable.getMinimumHeight());
                            add.setCompoundDrawables(leftDrawable, null, null, null);
                            add.setText("删除自选");
                        }
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
                                if (isAdd) {
                                    QuoteEntity quoteEntity = new QuoteEntity();
                                    quoteEntity.setInstrument_id(instrument_id);
                                    insList.put(instrument_id, quoteEntity);
                                    LatestFileManager.saveInsListToFile(new ArrayList<>(insList.keySet()));
                                    getActivity().runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            popWindow.dismiss();
                                            ToastNotificationUtils.showToast(BaseApplication.getContext(),
                                                    "该合约已添加到自选列表");
                                        }
                                    });
                                } else {
                                    insList.remove(instrument_id);
                                    LatestFileManager.saveInsListToFile(new ArrayList<>(insList.keySet()));
                                    refreshOptional();
                                    getActivity().runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            popWindow.dismiss();
                                            ToastNotificationUtils.showToast(BaseApplication.getContext(),
                                                    "该合约已被移除自选列表");
                                        }
                                    });
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
                                startActivityForResult(intentPos, JUMP_TO_FUTURE_INFO_ACTIVITY);
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
                        if (mIsUpdate) refreshUI(mToolbarTitle.getText().toString());
                        break;
                    default:
                        break;
                }
            }
        };
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mReceiver, new IntentFilter(MD_BROADCAST_ACTION));
    }

    /**
     * date: 7/9/17
     * author: chenli
     * description: 根据主页标题和mTitle判断刷新不同行情页, 不显示的页面不刷
     */
    public void refreshUI(String title) {
        //防止相邻合约列表页面刷新
        if (!title.equals(mTitle)) return;
        try {
            String[] insList = mDataManager.getRtnData().getIns_list().split(",");
            for (String ins : insList) {
                //防止合约页切换时,前一页的数据加载
                if (mNewData.containsKey(ins)) {
                    QuoteEntity quoteEntity = CloneUtils.clone(mDataManager.getRtnData().getQuotes().get(ins));
                    if (DALIANZUHE.equals(mTitle) || ZHENGZHOUZUHE.equals(mTitle) || OPTIONAL.equals(mTitle)) {
                        if (ins.contains("&") && ins.contains(" "))
                            quoteEntity = LatestFileManager.calculateCombineQuotePart(quoteEntity);
                    }
                    mNewData.put(ins, quoteEntity);
                }

                if (OPTIONAL.equals(title) && mNewDataRecommend.containsKey(ins)) {
                    QuoteEntity quoteEntity = CloneUtils.clone(mDataManager.getRtnData().getQuotes().get(ins));
                    if (ins.contains("&") && ins.contains(" "))
                        quoteEntity = LatestFileManager.calculateCombineQuotePart(quoteEntity);
                    mNewDataRecommend.put(ins, quoteEntity);
                }
            }

            List<QuoteEntity> newData = new ArrayList<>(mNewData.values());
            DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new QuoteDiffCallback(mOldData, newData), false);
            mAdapter.setData(newData);
            diffResult.dispatchUpdatesTo(mAdapter);
            mOldData.clear();
            mOldData.addAll(newData);

            if (OPTIONAL.equals(title)) {
                List<QuoteEntity> newDataRecommend = new ArrayList<>(mNewDataRecommend.values());
                DiffUtil.DiffResult diffResultRecommend = DiffUtil.calculateDiff(new RecommendQuoteDiffCallback(mOldDataRecommend, newDataRecommend), false);
                mAdapterRecommend.setData(newDataRecommend);
                diffResultRecommend.dispatchUpdatesTo(mAdapterRecommend);
                mOldDataRecommend.clear();
                mOldDataRecommend.addAll(newDataRecommend);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mReceiver);
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
        if (!SPUtils.contains(BaseApplication.getContext(), CONFIG_RECOMMEND_OPTIONAL) && LatestFileManager.getOptionalInsList().isEmpty()) {
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
                    QuoteFragment.this.update();
                }
            });

            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    UserEntity userEntity = mDataManager.getTradeBean().getUsers().get(mDataManager.USER_ID);
                    if (userEntity == null) return;
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
        //先刷新列表再更新行情
        getQuoteInsList();
        mAdapter.setData(new ArrayList<>(mNewData.values()));
        List<QuoteEntity> newData = new ArrayList<>(mNewData.values());
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new QuoteDiffCallback(mOldData, newData), false);
        mAdapter.setData(newData);
        diffResult.dispatchUpdatesTo(mAdapter);
        mOldData.clear();
        mOldData.addAll(newData);

        try {
            List<String> insList;
            if (mInsList.size() < LOAD_QUOTE_NUM)insList = mInsList;
            else {
                LinearLayoutManager lm = (LinearLayoutManager) mBinding.rvQuote.getLayoutManager();
                int firstVisibleItemPosition = lm.findFirstVisibleItemPosition();
                int lastVisibleItemPosition = lm.findLastVisibleItemPosition();
                insList = mInsList.subList(firstVisibleItemPosition, lastVisibleItemPosition + 1);
            }
            LinearLayoutManager lmRecommend = (LinearLayoutManager) mBinding.rvQuoteRecommend.getLayoutManager();
            int firstVisibleItemPositionRecommend = lmRecommend.findFirstVisibleItemPosition();
            int lastVisibleItemPositionRecommend = lmRecommend.findLastVisibleItemPosition();
            List<String> insListRecommend = mInsListRecommend.subList(firstVisibleItemPositionRecommend, lastVisibleItemPositionRecommend + 1);
            insList.addAll(insListRecommend);
            sendSubscribeQuotes(insList);
        } catch (IndexOutOfBoundsException e) {
            e.printStackTrace();
        }
    }

}


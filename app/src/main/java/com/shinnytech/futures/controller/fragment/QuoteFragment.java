package com.shinnytech.futures.controller.fragment;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import androidx.databinding.DataBindingUtil;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.ItemTouchHelper;
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
import com.shinnytech.futures.amplitude.api.Amplitude;
import com.shinnytech.futures.application.BaseApplication;
import com.shinnytech.futures.controller.activity.MainActivity;
import com.shinnytech.futures.controller.activity.MainActivityPresenter;
import com.shinnytech.futures.databinding.FragmentQuoteBinding;
import com.shinnytech.futures.model.adapter.DragDialogAdapter;
import com.shinnytech.futures.model.adapter.QuoteAdapter;
import com.shinnytech.futures.model.adapter.QuoteAdapterRecommend;
import com.shinnytech.futures.model.bean.accountinfobean.OrderEntity;
import com.shinnytech.futures.model.bean.accountinfobean.PositionEntity;
import com.shinnytech.futures.model.bean.accountinfobean.UserEntity;
import com.shinnytech.futures.model.bean.eventbusbean.ScrollQuotesEvent;
import com.shinnytech.futures.model.bean.futureinfobean.QuoteEntity;
import com.shinnytech.futures.model.bean.searchinfobean.SearchEntity;
import com.shinnytech.futures.model.engine.DataManager;
import com.shinnytech.futures.model.engine.LatestFileManager;
import com.shinnytech.futures.model.listener.QuoteDiffCallback;
import com.shinnytech.futures.model.listener.SimpleRecyclerViewItemClickListener;
import com.shinnytech.futures.utils.CloneUtils;
import com.shinnytech.futures.utils.DividerItemDecorationUtils;
import com.shinnytech.futures.utils.LogUtils;
import com.shinnytech.futures.utils.SPUtils;
import com.shinnytech.futures.utils.ScreenUtils;
import com.shinnytech.futures.utils.ToastUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.shinnytech.futures.constants.AmpConstants.AMP_EVENT_OPTIONAL_DIRECTION;
import static com.shinnytech.futures.constants.AmpConstants.AMP_EVENT_OPTIONAL_DIRECTION_VALUE_ADD;
import static com.shinnytech.futures.constants.AmpConstants.AMP_EVENT_OPTIONAL_DIRECTION_VALUE_DELETE;
import static com.shinnytech.futures.constants.AmpConstants.AMP_EVENT_OPTIONAL_INSTRUMENT_ID;
import static com.shinnytech.futures.constants.AmpConstants.AMP_OPTIONAL_QUOTE;
import static com.shinnytech.futures.constants.SettingConstants.CONFIG_RECOMMEND_OPTIONAL;
import static com.shinnytech.futures.constants.CommonConstants.DALIAN;
import static com.shinnytech.futures.constants.CommonConstants.DALIANZUHE;
import static com.shinnytech.futures.constants.CommonConstants.DOMINANT;
import static com.shinnytech.futures.constants.MarketConstants.LOAD_QUOTE_NUM;
import static com.shinnytech.futures.constants.CommonConstants.NENGYUAN;
import static com.shinnytech.futures.constants.CommonConstants.OPTIONAL;
import static com.shinnytech.futures.constants.CommonConstants.RECOMMEND_INS;
import static com.shinnytech.futures.constants.CommonConstants.SHANGHAI;
import static com.shinnytech.futures.constants.TradeConstants.STATUS_ALIVE;
import static com.shinnytech.futures.constants.CommonConstants.ZHENGZHOU;
import static com.shinnytech.futures.constants.CommonConstants.ZHENGZHOUZUHE;
import static com.shinnytech.futures.constants.CommonConstants.ZHONGJIN;

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
    private DataManager sDataManager = DataManager.getInstance();
    private String mTitle = DOMINANT;
    private TextView mToolbarTitle;
    private List<String> mInsList = new ArrayList<>();
    private List<QuoteEntity> mOldData = new ArrayList<>();
    private Map<String, QuoteEntity> mNewData = new LinkedHashMap<>();
    private FragmentQuoteBinding mBinding;
    private Dialog mDialog;
    private RecyclerView mRecyclerView;
    private DragDialogAdapter mDragDialogAdapter;
    private View mView;

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
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_quote, container, false);
        initData();
        initEvent();
        mView = mBinding.getRoot();
        if (OPTIONAL.equals(mTitle)) initConfigOptional();
        return mView;
    }

    private void initData() {
        mToolbarTitle = getActivity().findViewById(R.id.title_toolbar);
        mBinding.rvQuote.setLayoutManager(new LinearLayoutManager(getActivity()));
        mBinding.rvQuote.addItemDecoration(
                new DividerItemDecorationUtils(getActivity(), DividerItemDecorationUtils.VERTICAL_LIST));
        mBinding.rvQuote.setItemAnimator(new DefaultItemAnimator());
        mAdapter = new QuoteAdapter(getActivity(), mOldData, mTitle);
        mBinding.rvQuote.setAdapter(mAdapter);
    }

    /**
     * date: 2019/6/11
     * author: chenli
     * description: 初始化合约列表
     */
    private void initInsList() {
        if (OPTIONAL.equals(mTitle)) {
            mNewData = LatestFileManager.getOptionalInsList();
            mInsList = new ArrayList<>(mNewData.keySet());
        } else {
            if (!mNewData.isEmpty()) return;
            switch (mTitle) {
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
    }

    @Override
    public void show() {
        if (mView == null)return;
        try {
            LogUtils.e(mTitle+"show", true);
            initInsList();
            refreshMD();
            refreshTD();

            if (DALIANZUHE.equals(mTitle) || ZHENGZHOUZUHE.equals(mTitle)) {
                mBinding.tvChangePercent.setText(R.string.quote_fragment_bid_price1);
                mBinding.tvOpenInterest.setText(R.string.quote_fragment_ask_price1);
            } else {
                mBinding.tvChangePercent.setText(R.string.quote_fragment_up_down_rate);
                mBinding.tvOpenInterest.setText(R.string.quote_fragment_open_interest);
            }

            //返回订阅之前的合约
            MainActivityPresenter mainActivityPresenter = ((MainActivity) getActivity()).getmMainActivityPresenter();
            String ins_list = mainActivityPresenter.getPreSubscribedQuotes();
            if (ins_list != null && !ins_list.equals(DataManager.getInstance().getRtnData().getIns_list())) {
                //自选发生变化后，首次显示全订阅
                if (OPTIONAL.equals(mTitle) && mOldData.size() != mNewData.size()) {
                    ins_list = TextUtils.join(",", mInsList);
                }
                BaseApplication.getmMDWebSocket().sendSubscribeQuote(ins_list);
                mainActivityPresenter.setPreSubscribedQuotes(null);
            } else if (mInsList.size() <= LOAD_QUOTE_NUM) {
                sendSubscribeQuotes(mInsList);
            } else {
                List<String> insList = mInsList.subList(0, LOAD_QUOTE_NUM);
                sendSubscribeQuotes(insList);
            }

        } catch (IndexOutOfBoundsException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void leave() {
    }

    /**
     * date: 2019/4/14
     * author: chenli
     * description: 订阅行情
     */
    private void sendSubscribeQuotes(List<String> insList) {

        if (DALIANZUHE.equals(mTitle) || ZHENGZHOUZUHE.equals(mTitle) || OPTIONAL.equals(mTitle)) {
            BaseApplication.getmMDWebSocket().
                    sendSubscribeQuote(TextUtils.join(",", LatestFileManager.getCombineInsList(insList)));
        } else {
            BaseApplication.getmMDWebSocket().
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
                                MainActivity mainActivity = (MainActivity) getActivity();
                                MainActivityPresenter mainActivityPresenter = mainActivity.getmMainActivityPresenter();
                                mainActivityPresenter.switchToFutureInfo(instrument_id);
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
                                    mDragDialogAdapter.updateList(LatestFileManager.readInsListFromFile());

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
                                            if (OPTIONAL.equals(mTitle)) show();
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
                                                String name = instrument_id;
                                                SearchEntity searchEntity = LatestFileManager.getSearchEntities().get(instrument_id);
                                                if (searchEntity != null)
                                                    name = searchEntity.getInstrumentName();
                                                ToastUtils.showToast(BaseApplication.getContext(),
                                                        name + "合约已添加");
                                            }
                                        });
                                    } else {
                                        jsonObject.put(AMP_EVENT_OPTIONAL_DIRECTION, AMP_EVENT_OPTIONAL_DIRECTION_VALUE_DELETE);
                                        insList.remove(instrument_id);
                                        LatestFileManager.saveInsListToFile(new ArrayList<>(insList.keySet()));
                                        if (OPTIONAL.equals(mTitle)) show();
                                        getActivity().runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                popWindow.dismiss();
                                                String name = instrument_id;
                                                SearchEntity searchEntity = LatestFileManager.getSearchEntities().get(instrument_id);
                                                if (searchEntity != null)
                                                    name = searchEntity.getInstrumentName();
                                                ToastUtils.showToast(BaseApplication.getContext(),
                                                        name + "合约已移除");
                                            }
                                        });
                                    }
                                    Amplitude.getInstance().logEventWrap(AMP_OPTIONAL_QUOTE, jsonObject);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        });

                        trade.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                DataManager.getInstance().IS_SHOW_VP_CONTENT = true;
                                MainActivity mainActivity = (MainActivity) getActivity();
                                MainActivityPresenter mainActivityPresenter = mainActivity.getmMainActivityPresenter();
                                mainActivityPresenter.switchToFutureInfo(instrument_id);
                                popWindow.dismiss();
                            }
                        });
                    }
                }));

    }

    /**
     * date: 7/9/17
     * author: chenli
     * description: 根据主页标题和mTitle判断刷新不同行情页, 不显示的页面不刷
     */
    @Override
    public void refreshMD() {
        if (mView == null)return;
        //防止相邻合约列表页面刷新
        if (!mToolbarTitle.getText().toString().equals(mTitle) || !mIsUpdate) return;
        try {
            String[] insList = sDataManager.getRtnData().getIns_list().split(",");
            for (String ins : insList) {
                try {
                    //防止合约页切换时,前一页的数据加载
                    if (mNewData.containsKey(ins)) {
                        QuoteEntity quoteEntity = CloneUtils.clone(sDataManager.getRtnData().getQuotes().get(ins));
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
    @Override
    public void refreshTD() {
        if (mView == null)return;
        if (!mIsUpdate)return;

        UserEntity userEntity = sDataManager.getTradeBean().getUsers().get(sDataManager.USER_ID);
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
    }

    @Override
    public void onPause() {
        super.onPause();
        EventBus.getDefault().unregister(this);
    }

    //根据合约导航滑动行情列表
    @Subscribe
    public void onEvent(ScrollQuotesEvent scrollQuotesEvent) {
        if (mTitle.equals(mToolbarTitle.getText().toString())) {
            try {
                int position = scrollQuotesEvent.getPosition();
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
                    final Dialog dialog1 = new Dialog(getActivity(), R.style.Theme_Light_Dialog);
                    View viewDialog = View.inflate(getActivity(), R.layout.view_dialog_recommend_quote, null);
                    Window dialogWindow = dialog1.getWindow();
                    if (dialogWindow != null) {
                        dialogWindow.getDecorView().setPadding(0, 0, 0, 0);
                        WindowManager.LayoutParams lp = dialogWindow.getAttributes();
                        dialogWindow.setGravity(Gravity.CENTER);
                        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
                        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
                        dialogWindow.setAttributes(lp);
                    }
                    dialog1.setContentView(viewDialog);
                    final List<String> insList = new ArrayList<>();
                    for (String ins : RECOMMEND_INS.split(",")) {
                        insList.add(ins);
                    }
                    final QuoteAdapterRecommend quoteAdapterRecommend = new QuoteAdapterRecommend(BaseApplication.getContext(), insList);
                    RecyclerView recyclerView = viewDialog.findViewById(R.id.dialog_rv);
                    viewDialog.findViewById(R.id.iv_close).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            dialog1.dismiss();
                        }
                    });
                    viewDialog.findViewById(R.id.enter).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            dialog1.dismiss();
                        }
                    });
                    recyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 3));
                    recyclerView.setAdapter(quoteAdapterRecommend);
                    dialog1.show();
                    dialog1.setCancelable(false);
                    dialog1.setOnDismissListener(new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialog) {
                            List<String> list = quoteAdapterRecommend.getmDataPre();
                            List<String> insList = LatestFileManager.readInsListFromFile();
                            if (list != null && !list.isEmpty()) {
                                List<String> values = new ArrayList<>(LatestFileManager.getMainInsListNameNav().values());
                                List<String> keys = new ArrayList<>(LatestFileManager.getMainInsListNameNav().keySet());
                                for (String name : list) {
                                    int index = values.indexOf(name);
                                    if (index != -1 && index < keys.size()) {
                                        String ins = keys.get(index);
                                        if (ins != null && !ins.isEmpty()) insList.add(ins);
                                    }
                                }
                                LatestFileManager.saveInsListToFile(insList);
                            }
                            if (OPTIONAL.equals(mTitle)) show();
                        }
                    });
                }
            });

            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    UserEntity userEntity = sDataManager.getTradeBean().getUsers().get(sDataManager.USER_ID);
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
            }, 2000);
        }

    }

    public String getTitle() {
        return mTitle;
    }

}


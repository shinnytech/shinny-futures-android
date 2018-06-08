package com.xinyi.shinnyfutures.view.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.databinding.DataBindingUtil;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.xinyi.shinnyfutures.R;
import com.xinyi.shinnyfutures.application.BaseApplicationLike;
import com.xinyi.shinnyfutures.databinding.FragmentQuoteBinding;
import com.xinyi.shinnyfutures.view.adapter.QuoteAdapter;
import com.xinyi.shinnyfutures.model.bean.eventbusbean.PositionEvent;
import com.xinyi.shinnyfutures.model.bean.eventbusbean.UpdateEvent;
import com.xinyi.shinnyfutures.model.bean.futureinfobean.QuoteEntity;
import com.xinyi.shinnyfutures.model.bean.searchinfobean.SearchEntity;
import com.xinyi.shinnyfutures.model.engine.DataManager;
import com.xinyi.shinnyfutures.model.listener.QuoteDiffCallback;
import com.xinyi.shinnyfutures.model.listener.SimpleRecyclerViewItemClickListener;
import com.xinyi.shinnyfutures.utils.DensityUtils;
import com.xinyi.shinnyfutures.utils.DividerItemDecorationUtils;
import com.xinyi.shinnyfutures.utils.LatestFileUtils;
import com.xinyi.shinnyfutures.utils.ToastNotificationUtils;
import com.xinyi.shinnyfutures.view.activity.FutureInfoActivity;
import com.xinyi.shinnyfutures.view.activity.SearchActivity;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.xinyi.shinnyfutures.constants.CommonConstants.CLOSE;
import static com.xinyi.shinnyfutures.constants.CommonConstants.DALIAN;
import static com.xinyi.shinnyfutures.constants.CommonConstants.DALIANZUHE;
import static com.xinyi.shinnyfutures.constants.CommonConstants.DOMINANT;
import static com.xinyi.shinnyfutures.constants.CommonConstants.ERROR;
import static com.xinyi.shinnyfutures.constants.CommonConstants.JUMP_TO_FUTURE_INFO_ACTIVITY;
import static com.xinyi.shinnyfutures.constants.CommonConstants.JUMP_TO_SEARCH_ACTIVITY;
import static com.xinyi.shinnyfutures.constants.CommonConstants.LOAD_QUOTE_NUM;
import static com.xinyi.shinnyfutures.constants.CommonConstants.MESSAGE;
import static com.xinyi.shinnyfutures.constants.CommonConstants.NENGYUAN;
import static com.xinyi.shinnyfutures.constants.CommonConstants.OPEN;
import static com.xinyi.shinnyfutures.constants.CommonConstants.OPTIONAL;
import static com.xinyi.shinnyfutures.constants.CommonConstants.SHANGHAI;
import static com.xinyi.shinnyfutures.constants.CommonConstants.ZHENGZHOU;
import static com.xinyi.shinnyfutures.constants.CommonConstants.ZHENGZHOUZUHE;
import static com.xinyi.shinnyfutures.constants.CommonConstants.ZHONGJIN;
import static com.xinyi.shinnyfutures.model.service.WebSocketService.BROADCAST_ACTION;

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
    private String mTitle = DOMINANT;
    private TextView mToolbarTitle;
    private String mIns = "";
    private Map<String, String> mInsMap = new LinkedHashMap<>();
    private List<String> mInsList = new ArrayList<>();
    private Map<String, QuoteEntity> mOldData = new LinkedHashMap<>();
    private Map<String, QuoteEntity> mNewData = new LinkedHashMap<>();
    private FragmentQuoteBinding mBinding;

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
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_quote, container, false);
        initData();
        initEvent();
        return mBinding.getRoot();
    }

    //开机合约列表解析完毕刷新主力行情
    @Subscribe
    public void onEvent(String msg) {
        if (DOMINANT.equals(mTitle))
            update();
    }

    //根据合约导航滑动行情列表
    @Subscribe
    public void onEvent(PositionEvent positionEvent) {
        if (mTitle.equals(mToolbarTitle.getText().toString())) {
            int position = positionEvent.getPosition();
            ((LinearLayoutManager) mBinding.rvQuote.getLayoutManager()).scrollToPositionWithOffset(position, 0);
            int visibleItemCount1 = mBinding.rvQuote.getChildCount();
            int lastPosition1 = (position + visibleItemCount1) > mInsList.size() ?
                    mInsList.size() : (position + visibleItemCount1);
            int firstPosition1 = (lastPosition1 - position) != visibleItemCount1 ? (lastPosition1 - visibleItemCount1) : position;
            try {
                if (mInsList.size() > LOAD_QUOTE_NUM && BaseApplicationLike.getWebSocketService() != null)
                    BaseApplicationLike.getWebSocketService().sendSubscribeQuote(TextUtils.join(",",
                            mInsList.subList(firstPosition1, lastPosition1)));
            } catch (IndexOutOfBoundsException e) {
                e.printStackTrace();
            }
        }
    }

    //控制行情是否刷新
    @Subscribe
    public void onEvent(UpdateEvent updateEvent) {
        if (mTitle.equals(mToolbarTitle.getText().toString()))
            this.mIsUpdate = updateEvent.isUpdate();
    }

    private void initData() {
        mToolbarTitle = getActivity().findViewById(R.id.title_toolbar);
        mBinding.rvQuote.setLayoutManager(new LinearLayoutManager(getActivity()));
        mBinding.rvQuote.addItemDecoration(
                new DividerItemDecorationUtils(getActivity(), DividerItemDecorationUtils.VERTICAL_LIST));
        mBinding.rvQuote.setItemAnimator(new DefaultItemAnimator());
        mAdapter = new QuoteAdapter(getActivity(), new ArrayList<>(mOldData.values()));
        mBinding.rvQuote.setAdapter(mAdapter);
    }

    /**
     * date: 7/9/17
     * author: chenli
     * description: 根据标题获取不同的适配器数据
     */
    @Override
    public void update() {
        switch (mTitle) {
            case OPTIONAL:
                mInsMap = LatestFileUtils.getOptionalInsList();
                break;
            case DOMINANT:
                mInsMap = LatestFileUtils.getMainInsList();
                break;
            case SHANGHAI:
                mInsMap = LatestFileUtils.getShangqiInsList();
                break;
            case NENGYUAN:
                mInsMap = LatestFileUtils.getNengyuanInsList();
                break;
            case DALIAN:
                mInsMap = LatestFileUtils.getDalianInsList();
                break;
            case ZHENGZHOU:
                mInsMap = LatestFileUtils.getZhengzhouInsList();
                break;
            case ZHONGJIN:
                mInsMap = LatestFileUtils.getZhongjinInsList();
                break;
            case DALIANZUHE:
                mInsMap = LatestFileUtils.getDalianzuheInsList();
                break;
            case ZHENGZHOUZUHE:
                mInsMap = LatestFileUtils.getZhengzhouzuheInsList();
                break;
            default:
                break;
        }
        //获取服务器数据
        mInsList = new ArrayList<>(mInsMap.keySet());
        if (BaseApplicationLike.getWebSocketService() != null) {
            try {
                if (mInsList.size() <= LOAD_QUOTE_NUM)
                    BaseApplicationLike.getWebSocketService().
                            sendSubscribeQuote(TextUtils.join(",", mInsList));
                else
                    BaseApplicationLike.getWebSocketService().
                            sendSubscribeQuote(TextUtils.join(",", mInsList.subList(0, LOAD_QUOTE_NUM)));
            } catch (IndexOutOfBoundsException e) {
                e.printStackTrace();
            }
        }

        mNewData.clear();
        for (Map.Entry<String, String> entry :
                mInsMap.entrySet()) {
            String ins = entry.getKey();
            String insName = entry.getValue();
            QuoteEntity quoteEntity = mDataManager.getRtnData().getQuotes().get(ins);
            if (quoteEntity == null) {
                QuoteEntity quoteEntity1 = new QuoteEntity();
                quoteEntity1.setInstrument_id(ins);
                quoteEntity1.setInstrument_name(insName);
                mNewData.put(ins, quoteEntity1);
            } else mNewData.put(ins, quoteEntity);
        }
    }

    protected void initEvent() {

        mBinding.tvChangePercent.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
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
                return false;
            }
        });

        mBinding.tvOpenInterest.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
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
                return false;
            }
        });

        mBinding.rvQuote.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                switch (newState) {
                    case RecyclerView.SCROLL_STATE_IDLE:
                        mIsUpdate = true;
                        LinearLayoutManager lm = (LinearLayoutManager) recyclerView.getLayoutManager();
                        int firstVisibleItemPosition = lm.findFirstVisibleItemPosition();
                        int lastVisibleItemPosition = lm.findLastVisibleItemPosition();
                        try {
                            if (mInsMap.size() > LOAD_QUOTE_NUM && BaseApplicationLike.getWebSocketService() != null)
                                BaseApplicationLike.getWebSocketService().sendSubscribeQuote(TextUtils.join(",",
                                        mInsList.subList(firstVisibleItemPosition, lastVisibleItemPosition + 1)));
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
                                mIns = mDataManager.getRtnData().getIns_list();
                                Intent intent = new Intent(getActivity(), FutureInfoActivity.class);
                                intent.putExtra("instrument_id", instrument_id);
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
                            if (instrument_id != null) {
                                if (!instrument_id.isEmpty()) {
                                    if (LatestFileUtils.getOptionalInsList().containsKey(instrument_id)) {
                                        initPopUp(view, instrument_id, false);
                                    } else {
                                        initPopUp(view, instrument_id, true);
                                    }
                                }
                            }
                        }
                    }

                    private void initPopUp(View view, final String instrument_id, final boolean isAdd) {
                        //构造一个添加自选合约合约的PopupWindow
                        final View popUpView = View.inflate(getActivity(), R.layout.popup_fragment_quote, null);
                        final PopupWindow popWindow = new PopupWindow(popUpView,
                                ViewGroup.LayoutParams.MATCH_PARENT, DensityUtils.dp2px(getActivity(), 42), true);
                        //设置动画，淡入淡出
                        popWindow.setAnimationStyle(R.style.anim_menu_quote);
                        //点击空白处popupWindow消失
                        popWindow.setBackgroundDrawable(new ColorDrawable(0x00000000));
                        TextView add = popUpView.findViewById(R.id.add_remove_quote);
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

                        add.setOnTouchListener(new View.OnTouchListener() {
                            @Override
                            public boolean onTouch(View v, MotionEvent event) {
                                if (isAdd) {
                                    SearchEntity searchEntity = LatestFileUtils.getSearchEntities().get(instrument_id);
                                    Map<String, String> insList = LatestFileUtils.getOptionalInsList();
                                    if (searchEntity != null)
                                        insList.put(instrument_id, searchEntity.getInstrumentName());
                                    else insList.put(instrument_id, instrument_id);
                                    LatestFileUtils.saveInsListToFile(insList);
                                    getActivity().runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            popWindow.dismiss();
                                            ToastNotificationUtils.showToast(BaseApplicationLike.getContext(), "该合约已添加到自选列表");
                                        }
                                    });
                                } else {
                                    Map<String, String> insList = LatestFileUtils.getOptionalInsList();
                                    insList.remove(instrument_id);
                                    LatestFileUtils.saveInsListToFile(insList);
                                    if (mTitle.equals(OPTIONAL)) {
                                        update();
                                    }
                                    getActivity().runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            popWindow.dismiss();
                                            ToastNotificationUtils.showToast(BaseApplicationLike.getContext(), "该合约已被移除自选列表");
                                        }
                                    });
                                }
                                return true;
                            }
                        });
                    }
                }));

    }

    private void registerBroaderCast() {
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
                    case MESSAGE:
                        refreshUI(mToolbarTitle.getText().toString());
                        break;
                    default:
                        break;
                }
            }
        };
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mReceiver, new IntentFilter(BROADCAST_ACTION));
    }

    /**
     * date: 7/9/17
     * author: chenli
     * description: 根据主页标题和mtitle判断刷新不同行情页, 不显示的页面不刷
     */
    public void refreshUI(String toolbarTitle) {
        //防止相邻合约列表页面刷新
        if (mIsUpdate && toolbarTitle.equals(mTitle)) {
            try {
                for (String ins : mDataManager.getRtnData().getIns_list().split(",")) {
                    //防止合约页切换时,前一页的数据加载
                    if (mNewData.containsKey(ins)) {
                        QuoteEntity quoteEntity = mDataManager.getRtnData().getQuotes().get(ins);
                        if (quoteEntity != null) mNewData.put(ins, quoteEntity);
                    }
                }
                DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new QuoteDiffCallback(new ArrayList<>(mOldData.values()), new ArrayList<>(mNewData.values())), false);
                mAdapter.setData(new ArrayList<>(mNewData.values()));
                diffResult.dispatchUpdatesTo(mAdapter);
                mOldData.clear();
                mOldData.putAll(mNewData);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
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
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater menuInflater) {
        menuInflater.inflate(R.menu.fragment_quote, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.fragment_quote) {
            mIns = mDataManager.getRtnData().getIns_list();
            Intent intent = new Intent(getActivity(), SearchActivity.class);
            startActivityForResult(intent, JUMP_TO_SEARCH_ACTIVITY);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * date: 6/21/17
     * author: chenli
     * description: 从搜索页添加合约到自选后，返回自选合约列表页时刷新合约列表，当然，在下达收藏指令时就已经向服务器发送合约列表了
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (OPTIONAL.equals(mToolbarTitle.getText().toString()) && mInsMap.size() != LatestFileUtils.getOptionalInsList().size())
            update();
        //三种情况:搜索页返回,合约详情页返回,搜索页点击进入合约详情页再返回
        if (BaseApplicationLike.getWebSocketService() != null)
            switch (requestCode) {
                case JUMP_TO_FUTURE_INFO_ACTIVITY:
                    BaseApplicationLike.getWebSocketService().sendSubscribeQuote(mIns);
                    break;
                case JUMP_TO_SEARCH_ACTIVITY:
                    if (mIns != null && !mIns.equals(mDataManager.getRtnData().getIns_list()))
                        BaseApplicationLike.getWebSocketService().sendSubscribeQuote(mIns);
                    break;
                default:
                    break;
            }
    }

}


package com.shinnytech.futures.controller.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.RadioGroup;

import com.shinnytech.futures.R;
import com.shinnytech.futures.application.BaseApplication;
import com.shinnytech.futures.controller.FutureInfoActivityPresenter;
import com.shinnytech.futures.databinding.ActivityFutureInfoBinding;
import com.shinnytech.futures.model.amplitude.api.Amplitude;
import com.shinnytech.futures.model.bean.eventbusbean.AverageEvent;
import com.shinnytech.futures.model.bean.eventbusbean.IdEvent;
import com.shinnytech.futures.model.bean.eventbusbean.SetUpEvent;
import com.shinnytech.futures.model.bean.futureinfobean.QuoteEntity;
import com.shinnytech.futures.model.bean.searchinfobean.SearchEntity;
import com.shinnytech.futures.model.engine.LatestFileManager;
import com.shinnytech.futures.utils.CloneUtils;
import com.shinnytech.futures.utils.NetworkUtils;
import com.shinnytech.futures.utils.SPUtils;
import com.shinnytech.futures.utils.ToastUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Map;

import static com.shinnytech.futures.constants.CommonConstants.AMP_EVENT_CURRENT_PAGE;
import static com.shinnytech.futures.constants.CommonConstants.AMP_EVENT_OPTIONAL_DIRECTION;
import static com.shinnytech.futures.constants.CommonConstants.AMP_EVENT_OPTIONAL_DIRECTION_VALUE_ADD;
import static com.shinnytech.futures.constants.CommonConstants.AMP_EVENT_OPTIONAL_DIRECTION_VALUE_DELETE;
import static com.shinnytech.futures.constants.CommonConstants.AMP_EVENT_OPTIONAL_INSTRUMENT_ID;
import static com.shinnytech.futures.constants.CommonConstants.AMP_EVENT_PAGE_VALUE_CHART_SETTING;
import static com.shinnytech.futures.constants.CommonConstants.AMP_EVENT_PAGE_VALUE_FUTURE_INFO;
import static com.shinnytech.futures.constants.CommonConstants.AMP_EVENT_PAGE_VALUE_SEARCH;
import static com.shinnytech.futures.constants.CommonConstants.AMP_EVENT_TARGET_PAGE;
import static com.shinnytech.futures.constants.CommonConstants.AMP_OPTIONAL_FUTURE_INFO;
import static com.shinnytech.futures.constants.CommonConstants.AMP_SWITCH_PAGE;
import static com.shinnytech.futures.constants.CommonConstants.CONFIG_AVERAGE_LINE;
import static com.shinnytech.futures.constants.CommonConstants.CONFIG_ORDER_LINE;
import static com.shinnytech.futures.constants.CommonConstants.CONFIG_POSITION_LINE;
import static com.shinnytech.futures.constants.CommonConstants.FUTURE_INFO_ACTIVITY_TO_CHART_SETTING_ACTIVITY;
import static com.shinnytech.futures.constants.CommonConstants.MD_MESSAGE;
import static com.shinnytech.futures.constants.CommonConstants.OFFLINE;
import static com.shinnytech.futures.model.receiver.NetworkReceiver.NETWORK_STATE;
import static com.shinnytech.futures.model.service.WebSocketService.MD_BROADCAST_ACTION;

/**
 * date: 7/7/17
 * author: chenli
 * description: 合约详情页，主要用来显示合约的分时图、K线图、盘口信息、持仓信息、委托单信息、下单板
 * version:
 * state: basically done
 */
public class FutureInfoActivity extends BaseActivity {
    private ActivityFutureInfoBinding mBinding;
    private MenuItem mMenuItem;
    private FutureInfoActivityPresenter mFutureInfoActivityPresenter;
    private String mInstrumentId;
    private BroadcastReceiver mReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mLayoutID = R.layout.activity_future_info;
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
    }

    @Override
    protected void initData() {
        mBinding = (ActivityFutureInfoBinding) mViewDataBinding;
        mFutureInfoActivityPresenter = new FutureInfoActivityPresenter(this, sContext, mBinding, mToolbarTitle);
        mInstrumentId = mFutureInfoActivityPresenter.getInstrumentId();
    }

    @Override
    protected void initEvent() {
        mFutureInfoActivityPresenter.registerListeners();
    }

    @Override
    protected void refreshUI() {

    }

    private void refreshMD() {
        QuoteEntity quoteEntity = sDataManager.getRtnData().getQuotes().get(mInstrumentId);
        if (quoteEntity == null) return;
        if (mInstrumentId.contains("&") && mInstrumentId.contains(" ")) {
            quoteEntity = CloneUtils.clone(quoteEntity);
            quoteEntity = LatestFileManager.calculateCombineQuoteFull(quoteEntity);
        }
        mBinding.setQuote(quoteEntity);
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshMD();
        sendSubscribeQuote(mInstrumentId);
    }

    @Override
    protected void updateToolbarFromNetwork(Context context, String title) {
        if (NetworkUtils.isNetworkConnected(sContext)) {
            mToolbar.setBackgroundColor(ContextCompat.getColor(sContext, R.color.toolbar));
            mToolbarTitle.setTextColor(Color.WHITE);
            mFutureInfoActivityPresenter.setToolbarTitle();
            mToolbarTitle.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.mipmap.ic_exchange_down, 0);
        } else {
            mToolbar.setBackgroundColor(ContextCompat.getColor(sContext, R.color.off_line));
            mToolbarTitle.setTextColor(Color.BLACK);
            mToolbarTitle.setText(OFFLINE);
            mToolbarTitle.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
        }
    }

    @Override
    protected void registerBroaderCast() {
        mReceiverNetwork = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                int networkStatus = intent.getIntExtra("networkStatus", 0);
                switch (networkStatus) {
                    case 0:
                        mToolbar.setBackgroundColor(ContextCompat.getColor(context, R.color.off_line));
                        mToolbarTitle.setTextColor(Color.BLACK);
                        mToolbarTitle.setText(OFFLINE);
                        mToolbarTitle.setTextSize(20);
                        mToolbarTitle.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                        break;
                    case 1:
                        mToolbar.setBackgroundColor(ContextCompat.getColor(context, R.color.toolbar));
                        mToolbarTitle.setTextColor(Color.WHITE);
                        mFutureInfoActivityPresenter.setToolbarTitle();
                        mToolbarTitle.setTextSize(25);
                        mToolbarTitle.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.mipmap.ic_exchange_down, 0);
                        break;
                }
            }
        };
        registerReceiver(mReceiverNetwork, new IntentFilter(NETWORK_STATE));

        mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String mDataString = intent.getStringExtra("msg");
                switch (mDataString) {
                    case MD_MESSAGE:
                        refreshMD();
                        break;
                    default:
                        break;
                }
            }
        };
        LocalBroadcastManager.getInstance(this).registerReceiver(mReceiver, new IntentFilter(MD_BROADCAST_ACTION));

    }

    @Override
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mReceiver);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    /**
     * date: 6/1/18
     * author: chenli
     * description: 初始化收藏图标
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_futureinfo, menu);
        mMenuItem = menu.findItem(R.id.action_collect);
        if (LatestFileManager.getOptionalInsList().containsKey(mInstrumentId)) {
            mMenuItem.setIcon(R.mipmap.ic_favorite_white_24dp);
        } else {
            mMenuItem.setIcon(R.mipmap.ic_favorite_border_white_24dp);
        }
        return true;
    }

    /**
     * date: 7/7/17
     * author: chenli
     * description: 点击“收藏”菜单，添加或删除合约到自选列表
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        JSONObject jsonObject = new JSONObject();
        switch (id) {
            case R.id.action_collect:
                if (mInstrumentId == null || mInstrumentId.equals(""))
                    return super.onOptionsItemSelected(item);
                try {
                    jsonObject.put(AMP_EVENT_OPTIONAL_INSTRUMENT_ID, mInstrumentId);
                    Map<String, QuoteEntity> insList = LatestFileManager.getOptionalInsList();
                    if (insList.containsKey(mInstrumentId)) {
                        jsonObject.put(AMP_EVENT_OPTIONAL_DIRECTION, AMP_EVENT_OPTIONAL_DIRECTION_VALUE_DELETE);
                        insList.remove(mInstrumentId);
                        LatestFileManager.saveInsListToFile(new ArrayList<>(insList.keySet()));
                        ToastUtils.showToast(BaseApplication.getContext(), "该合约已被移除自选列表");
                        mMenuItem.setIcon(R.mipmap.ic_favorite_border_white_24dp);
                    } else {
                        jsonObject.put(AMP_EVENT_OPTIONAL_DIRECTION, AMP_EVENT_OPTIONAL_DIRECTION_VALUE_ADD);
                        QuoteEntity quoteEntity = new QuoteEntity();
                        quoteEntity.setInstrument_id(mInstrumentId);
                        insList.put(mInstrumentId, quoteEntity);
                        LatestFileManager.saveInsListToFile(new ArrayList<>(insList.keySet()));
                        ToastUtils.showToast(BaseApplication.getContext(), "该合约已添加到自选列表");
                        mMenuItem.setIcon(R.mipmap.ic_favorite_white_24dp);
                    }
                    Amplitude.getInstance().logEvent(AMP_OPTIONAL_FUTURE_INFO, jsonObject);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                break;
            case android.R.id.home:
                Intent intent = new Intent();
                setResult(RESULT_OK, intent);
                finish();
                break;
            case R.id.search_quote:
                try {
                    jsonObject.put(AMP_EVENT_CURRENT_PAGE, AMP_EVENT_PAGE_VALUE_FUTURE_INFO);
                    jsonObject.put(AMP_EVENT_TARGET_PAGE, AMP_EVENT_PAGE_VALUE_SEARCH);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                Amplitude.getInstance().logEvent(AMP_SWITCH_PAGE, jsonObject);
                Intent intentS = new Intent(this, SearchActivity.class);
                intentS.putExtra("fromFutureInfoActivity", true);
                startActivity(intentS);
                return true;
            default:
                break;
        }
        return true;
    }


    /**
     * 处理返回键逻辑或者使用onBackPressed()
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        Intent intent = new Intent();
        setResult(RESULT_OK, intent);
        finish();
        return super.onKeyDown(keyCode, event);
    }

    /**
     * date: 7/7/17
     * author: chenli
     * description: 通过EventBus框架实现fragment与activity之间的数据传递，
     * 这里接受本页和fragmentPosition/currentDayFragment/KlineFragment页发过来的合约代码数据；
     * 在接受到数据后向服务器发送合约代码指令
     */
    @Subscribe
    public void onEvent(IdEvent data) {
        //持仓列表中可能出现合约一致,方向不同的情形
        String instrument_id_new = data.getInstrument_id();
        if (!mInstrumentId.equals(instrument_id_new)) {
            mInstrumentId = instrument_id_new;
            sendSubscribeQuote(instrument_id_new);
            mFutureInfoActivityPresenter.setInstrumentId(mInstrumentId);
            mFutureInfoActivityPresenter.setToolbarTitle();
            if (LatestFileManager.getOptionalInsList().containsKey(mInstrumentId)) {
                mMenuItem.setIcon(R.mipmap.ic_favorite_white_24dp);
            } else {
                mMenuItem.setIcon(R.mipmap.ic_favorite_border_white_24dp);
            }
            //切换合约更新盘口
            refreshMD();
            //切换合约判断是否有五档行情
            mFutureInfoActivityPresenter.updateMD5ViewVisibility();
        }
    }

    /**
     * date: 2019/1/10
     * author: chenli
     * description: 订阅合约行情
     */
    private void sendSubscribeQuote(String ins) {
        if (ins.contains("&") && ins.contains(" ")) {
            SearchEntity searchEntity = LatestFileManager.getSearchEntities().get(ins);
            if (searchEntity != null) {
                String leg1_symbol = searchEntity.getLeg1_symbol();
                String leg2_symbol = searchEntity.getLeg2_symbol();
                ins = ins + "," + leg1_symbol + "," + leg2_symbol;
            }
        }
        if (BaseApplication.getWebSocketService() != null)
            BaseApplication.getWebSocketService().sendSubscribeQuote(ins);
    }

    public ViewPager getViewPager() {
        return mBinding.vpInfoContent;
    }

    public String getInstrument_id() {
        return mInstrumentId;
    }

    public RadioGroup getTabsInfo() {
        return mBinding.rgTabInfo;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == FUTURE_INFO_ACTIVITY_TO_CHART_SETTING_ACTIVITY) {
            //刷新开关
            boolean mIsPosition = (boolean) SPUtils.get(sContext, CONFIG_POSITION_LINE, true);
            boolean mIsPending = (boolean) SPUtils.get(sContext, CONFIG_ORDER_LINE, true);
            boolean mIsAverage = (boolean) SPUtils.get(sContext, CONFIG_AVERAGE_LINE, true);
            SetUpEvent setUpEvent = new SetUpEvent();
            setUpEvent.setAverage(mIsAverage);
            setUpEvent.setPending(mIsPending);
            setUpEvent.setPosition(mIsPosition);
            EventBus.getDefault().post(setUpEvent);
            //重绘均线
            EventBus.getDefault().post(new AverageEvent());
            //刷新五档行情
            mFutureInfoActivityPresenter.updateMD5ViewVisibility();
            //刷新k线周期
            mFutureInfoActivityPresenter.getmKlineDurationTitleAdapter().update();
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put(AMP_EVENT_CURRENT_PAGE, AMP_EVENT_PAGE_VALUE_CHART_SETTING);
                jsonObject.put(AMP_EVENT_TARGET_PAGE, AMP_EVENT_PAGE_VALUE_FUTURE_INFO);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            Amplitude.getInstance().logEvent(AMP_SWITCH_PAGE, jsonObject);
        }
    }
}
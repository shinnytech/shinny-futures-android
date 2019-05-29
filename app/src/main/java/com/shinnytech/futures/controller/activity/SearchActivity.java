package com.shinnytech.futures.controller.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.databinding.DataBindingUtil;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.SearchView;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;

import com.shinnytech.futures.R;
import com.shinnytech.futures.application.BaseApplication;
import com.shinnytech.futures.databinding.ActivitySearchBinding;
import com.shinnytech.futures.model.adapter.SearchAdapter;
import com.shinnytech.futures.model.amplitude.api.Amplitude;
import com.shinnytech.futures.model.bean.eventbusbean.IdEvent;
import com.shinnytech.futures.model.bean.futureinfobean.QuoteEntity;
import com.shinnytech.futures.model.bean.searchinfobean.SearchEntity;
import com.shinnytech.futures.model.engine.LatestFileManager;
import com.shinnytech.futures.utils.DividerItemDecorationUtils;
import com.shinnytech.futures.utils.NetworkUtils;
import com.shinnytech.futures.utils.ToastUtils;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Map;

import static android.view.inputmethod.InputMethodManager.HIDE_NOT_ALWAYS;
import static com.shinnytech.futures.constants.CommonConstants.AMP_EVENT_CURRENT_PAGE;
import static com.shinnytech.futures.constants.CommonConstants.AMP_EVENT_OPTIONAL_DIRECTION;
import static com.shinnytech.futures.constants.CommonConstants.AMP_EVENT_OPTIONAL_DIRECTION_VALUE_ADD;
import static com.shinnytech.futures.constants.CommonConstants.AMP_EVENT_OPTIONAL_DIRECTION_VALUE_DELETE;
import static com.shinnytech.futures.constants.CommonConstants.AMP_EVENT_OPTIONAL_INSTRUMENT_ID;
import static com.shinnytech.futures.constants.CommonConstants.AMP_EVENT_PAGE_VALUE_FUTURE_INFO;
import static com.shinnytech.futures.constants.CommonConstants.AMP_EVENT_PAGE_VALUE_SEARCH;
import static com.shinnytech.futures.constants.CommonConstants.AMP_EVENT_TARGET_PAGE;
import static com.shinnytech.futures.constants.CommonConstants.AMP_OPTIONAL_SEARCH;
import static com.shinnytech.futures.constants.CommonConstants.AMP_SWITCH_PAGE;
import static com.shinnytech.futures.constants.CommonConstants.INS_BETWEEN_ACTIVITY;
import static com.shinnytech.futures.constants.CommonConstants.OFFLINE;
import static com.shinnytech.futures.model.receiver.NetworkReceiver.NETWORK_STATE;

/**
 * date: 6/21/17
 * author: chenli
 * description: 搜索合约页。问题：在finish本活动时略显混乱的感觉，能不能像文华一样加个向左的转场动画
 * version:
 * state: basically done
 */
public class SearchActivity extends AppCompatActivity implements SearchView.OnQueryTextListener {

    private SearchAdapter mSearchAdapter;
    private BroadcastReceiver mReceiver;
    private Context sContext;
    private ActivitySearchBinding mBinding;
    private boolean mIsFromFutureInfoActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_search);
        initData();
        initEvent();
    }

    private void initData() {
        mIsFromFutureInfoActivity = getIntent().getBooleanExtra("fromFutureInfoActivity", false);
        sContext = BaseApplication.getContext();
        mBinding.toolbarSearch.setTitle("");
        setSupportActionBar(mBinding.toolbarSearch);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }

        mBinding.rvSearchQuoteList.setLayoutManager(new LinearLayoutManager(this));
        mBinding.rvSearchQuoteList.addItemDecoration(
                new DividerItemDecorationUtils(this, DividerItemDecorationUtils.VERTICAL_LIST));
        mBinding.rvSearchQuoteList.setItemAnimator(new DefaultItemAnimator());
        mSearchAdapter = new SearchAdapter(this);
        mBinding.rvSearchQuoteList.setAdapter(mSearchAdapter);
    }

    private void initEvent() {
        mSearchAdapter.setOnItemClickListener(new SearchAdapter.OnItemClickListener() {
            //跳转到合约详情页
            @Override
            public void OnItemJump(SearchEntity searchEntity, String instrument_id) {
                //如果用户点击了搜索到的合约信息，就把此条合约保存到搜索历史中
                if (instrument_id != null && !instrument_id.isEmpty()) {
                    LatestFileManager.getSearchEntitiesHistory().put(instrument_id, searchEntity);
                    if (mIsFromFutureInfoActivity) {
                        IdEvent idEvent = new IdEvent();
                        idEvent.setInstrument_id(instrument_id);
                        EventBus.getDefault().post(idEvent);
                    } else {
                        Intent intent = new Intent(SearchActivity.this, FutureInfoActivity.class);
                        intent.putExtra(INS_BETWEEN_ACTIVITY, instrument_id);
                        startActivity(intent);
                    }
                    JSONObject jsonObject = new JSONObject();
                    try {
                        jsonObject.put(AMP_EVENT_CURRENT_PAGE, AMP_EVENT_PAGE_VALUE_SEARCH);
                        jsonObject.put(AMP_EVENT_TARGET_PAGE, AMP_EVENT_PAGE_VALUE_FUTURE_INFO);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    Amplitude.getInstance().logEvent(AMP_SWITCH_PAGE, jsonObject);
                    //关闭键盘后销毁
                    View view = getCurrentFocus();
                    if (view != null) {
                        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        if (inputMethodManager != null)
                            inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), HIDE_NOT_ALWAYS);
                    }
                    finish();
                }
            }

            //收藏或移除该合约到自选合约列表
            @Override
            public void OnItemCollect(View view, String instrument_id) {
                if (instrument_id == null || "".equals(instrument_id)) return;
                Map<String, QuoteEntity> insList = LatestFileManager.getOptionalInsList();
                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put(AMP_EVENT_OPTIONAL_INSTRUMENT_ID, instrument_id);
                    if (insList.containsKey(instrument_id)) {
                        jsonObject.put(AMP_EVENT_OPTIONAL_DIRECTION, AMP_EVENT_OPTIONAL_DIRECTION_VALUE_DELETE);
                        insList.remove(instrument_id);
                        LatestFileManager.saveInsListToFile(new ArrayList<>(insList.keySet()));
                        ToastUtils.showToast(BaseApplication.getContext(), "该合约已被移除自选列表");
                        ((ImageView) view).setImageResource(R.mipmap.ic_favorite_border_white_24dp);
                    } else {
                        jsonObject.put(AMP_EVENT_OPTIONAL_DIRECTION, AMP_EVENT_OPTIONAL_DIRECTION_VALUE_ADD);
                        QuoteEntity quoteEntity = new QuoteEntity();
                        quoteEntity.setInstrument_id(instrument_id);
                        insList.put(instrument_id, quoteEntity);
                        LatestFileManager.saveInsListToFile(new ArrayList<>(insList.keySet()));
                        ToastUtils.showToast(BaseApplication.getContext(), "该合约已添加到自选列表");
                        ((ImageView) view).setImageResource(R.mipmap.ic_favorite_white_24dp);
                    }
                    Amplitude.getInstance().logEvent(AMP_OPTIONAL_SEARCH, jsonObject);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
        //添加搜索监听
        mBinding.searchEditFrame.setOnQueryTextListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerBroaderCast();
        updateToolbarFromNetwork();
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mReceiver);
    }

    /**
     * date: 7/7/17
     * author: chenli
     * description: 返回行情页时，如果是自选合约详情页则刷新之
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case android.R.id.home:
                //关闭键盘后销毁
                back();
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * 处理返回键逻辑或者使用onBackPressed()
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        back();
        return super.onKeyDown(keyCode, event);
    }

    /**
     * date: 2019/3/30
     * author: chenli
     * description: 返回上一页
     */
    private void back() {
        //关闭键盘后销毁
        View view = getCurrentFocus();
        if (view != null) {
            InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (inputMethodManager != null)
                inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), HIDE_NOT_ALWAYS);
        }
        Intent intent = new Intent();
        setResult(RESULT_OK, intent);
        finish();
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    /**
     * date: 7/7/17
     * author: chenli
     * description: 添加搜索过滤
     */
    @Override
    public boolean onQueryTextChange(String newText) {
        mSearchAdapter.filter(newText);
        return true;
    }


    /**
     * date: 7/9/17
     * author: chenli
     * description: 检查网络状态，更新toolbar的显示
     */
    private void updateToolbarFromNetwork() {
        if (NetworkUtils.isNetworkConnected(sContext)) {
            mBinding.toolbarSearch.setBackgroundColor(ContextCompat.getColor(sContext, R.color.toolbar));
            mBinding.titleToolbar.setVisibility(View.GONE);
        } else {
            mBinding.toolbarSearch.setBackgroundColor(ContextCompat.getColor(sContext, R.color.off_line));
            mBinding.titleToolbar.setVisibility(View.VISIBLE);
            mBinding.titleToolbar.setTextColor(Color.BLACK);
            mBinding.titleToolbar.setText(OFFLINE);
        }
    }

    /**
     * date: 7/7/17
     * author: chenli
     * description: 监听网络状态
     */
    private void registerBroaderCast() {
        mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                int networkStatus = intent.getIntExtra("networkStatus", 0);
                switch (networkStatus) {
                    case 0:
                        mBinding.toolbarSearch.setBackgroundColor(ContextCompat.getColor(context, R.color.off_line));
                        mBinding.titleToolbar.setVisibility(View.VISIBLE);
                        mBinding.titleToolbar.setTextColor(Color.BLACK);
                        mBinding.titleToolbar.setText(OFFLINE);
                        break;
                    case 1:
                        mBinding.toolbarSearch.setBackgroundColor(ContextCompat.getColor(context, R.color.toolbar));
                        mBinding.titleToolbar.setVisibility(View.GONE);
                        break;
                }
            }
        };
        registerReceiver(mReceiver, new IntentFilter(NETWORK_STATE));
    }

}

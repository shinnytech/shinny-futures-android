package com.xinyi.shinnyfutures.view.activity;

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
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;

import com.xinyi.shinnyfutures.R;
import com.xinyi.shinnyfutures.application.BaseApplicationLike;
import com.xinyi.shinnyfutures.databinding.ActivitySearchBinding;
import com.xinyi.shinnyfutures.view.adapter.SearchAdapter;
import com.xinyi.shinnyfutures.model.bean.searchinfobean.SearchEntity;
import com.xinyi.shinnyfutures.utils.DividerItemDecorationUtils;
import com.xinyi.shinnyfutures.utils.LatestFileUtils;
import com.xinyi.shinnyfutures.utils.NetworkUtils;
import com.xinyi.shinnyfutures.utils.ToastNotificationUtils;

import java.util.Map;

import static android.view.inputmethod.InputMethodManager.HIDE_NOT_ALWAYS;
import static com.xinyi.shinnyfutures.model.receiver.NetworkReceiver.NETWORK_STATE;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_search);
        initData();
        initEvent();
    }

    private void initData() {
        sContext = BaseApplicationLike.getContext();
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
                LatestFileUtils.getSearchEntitiesHistory().put(instrument_id, searchEntity);
                Intent intent = new Intent(SearchActivity.this, FutureInfoActivity.class);
                intent.putExtra("instrument_id", instrument_id);
                startActivity(intent);
                finish();
            }

            //收藏或移除该合约到自选合约列表
            @Override
            public void OnItemCollect(View view, String instrument_id) {
                if (LatestFileUtils.getOptionalInsList().containsKey(instrument_id)) {
                    if (instrument_id != null) {
                        if (!instrument_id.equals("")) {
                            Map<String, String> insList = LatestFileUtils.getOptionalInsList();
                            insList.remove(instrument_id);
                            LatestFileUtils.saveInsListToFile(insList);
                            ToastNotificationUtils.showToast(BaseApplicationLike.getContext(), "该合约已被移除自选列表");
                            ((ImageView) view).setImageResource(R.mipmap.ic_favorite_border_white_24dp);
                        }
                    }
                } else {
                    if (instrument_id != null) {
                        if (!instrument_id.equals("")) {
                            SearchEntity searchEntity = LatestFileUtils.getSearchEntities().get(instrument_id);
                            Map<String, String> insList = LatestFileUtils.getOptionalInsList();
                            if (searchEntity != null)
                                insList.put(instrument_id, searchEntity.getInstrumentName());
                            else insList.put(instrument_id, instrument_id);
                            LatestFileUtils.saveInsListToFile(insList);
                            ToastNotificationUtils.showToast(BaseApplicationLike.getContext(), "该合约已添加到自选列表");
                            ((ImageView) view).setImageResource(R.mipmap.ic_favorite_white_24dp);
                        }
                    }
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
                View view = getCurrentFocus();
                if (view != null) {
                    InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    if (inputMethodManager != null)
                        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), HIDE_NOT_ALWAYS);
                }
                Intent intent = new Intent();
                setResult(RESULT_OK, intent);
                finish();
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
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
            mBinding.toolbarSearch.setBackgroundColor(ContextCompat.getColor(sContext, R.color.black_dark));
            mBinding.titleToolbar.setVisibility(View.GONE);
        } else {
            mBinding.toolbarSearch.setBackgroundColor(ContextCompat.getColor(sContext, R.color.off_line));
            mBinding.titleToolbar.setVisibility(View.VISIBLE);
            mBinding.titleToolbar.setTextColor(Color.BLACK);
            mBinding.titleToolbar.setText("交易、行情网络未连接！");
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
                        mBinding.titleToolbar.setText("交易、行情网络未连接！");
                        break;
                    case 1:
                        mBinding.toolbarSearch.setBackgroundColor(ContextCompat.getColor(context, R.color.black_dark));
                        mBinding.titleToolbar.setVisibility(View.GONE);
                        break;
                }
            }
        };
        registerReceiver(mReceiver, new IntentFilter(NETWORK_STATE));
    }

}

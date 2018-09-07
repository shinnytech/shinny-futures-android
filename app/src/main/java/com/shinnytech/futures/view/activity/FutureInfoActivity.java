package com.shinnytech.futures.view.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.RadioGroup;

import com.shinnytech.futures.R;
import com.shinnytech.futures.application.BaseApplicationLike;
import com.shinnytech.futures.databinding.ActivityFutureInfoBinding;
import com.shinnytech.futures.model.bean.eventbusbean.IdEvent;
import com.shinnytech.futures.model.bean.futureinfobean.QuoteEntity;
import com.shinnytech.futures.model.bean.searchinfobean.SearchEntity;
import com.shinnytech.futures.model.engine.LatestFileManager;
import com.shinnytech.futures.presenter.FutureInfoActivityPresenter;
import com.shinnytech.futures.utils.ToastNotificationUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.Map;

import static com.shinnytech.futures.constants.CommonConstants.LOG_OUT;
import static com.shinnytech.futures.constants.CommonConstants.ORDER_JUMP_TO_LOG_IN_ACTIVITY;
import static com.shinnytech.futures.constants.CommonConstants.POSITION_JUMP_TO_LOG_IN_ACTIVITY;
import static com.shinnytech.futures.constants.CommonConstants.TRANSACTION_JUMP_TO_LOG_IN_ACTIVITY;

/**
 * date: 7/7/17
 * author: chenli
 * description: 合约详情页，主要用来显示合约的分时图、K线图、盘口信息、持仓信息、委托单信息、下单板
 * version:
 * state: basically done
 */
public class FutureInfoActivity extends BaseActivity {
    private ActivityFutureInfoBinding mBinding;
    private Context sContext;
    private MenuItem mMenuItem;
    private FutureInfoActivityPresenter mFutureInfoActivityPresenter;
    private String mInstrumentId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mLayoutID = R.layout.activity_future_info;
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
    }

    @Override
    protected void initData() {
        mBinding = (ActivityFutureInfoBinding) mViewDataBinding;
        sContext = BaseApplicationLike.getContext();
        mFutureInfoActivityPresenter = new FutureInfoActivityPresenter(this, sContext, mBinding, mToolbar, mToolbarTitle);
        mInstrumentId = mFutureInfoActivityPresenter.getInstrumentId();
    }

    @Override
    protected void initEvent() {
        mFutureInfoActivityPresenter.registerListeners();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mFutureInfoActivityPresenter.checkLoginState();
        if (BaseApplicationLike.getWebSocketService() != null)
            BaseApplicationLike.getWebSocketService().sendSubscribeQuote(mInstrumentId);
        mFutureInfoActivityPresenter.updateToolbarFromNetwork();
        mFutureInfoActivityPresenter.registerBroaderCast();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mFutureInfoActivityPresenter.unRegisterBroaderCast();
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
        switch (id) {
            case R.id.action_collect:
                if (mInstrumentId == null || mInstrumentId.equals(""))
                    return super.onOptionsItemSelected(item);
                Map<String, QuoteEntity> insList = LatestFileManager.getOptionalInsList();
                if (insList.containsKey(mInstrumentId)) {
                    insList.remove(mInstrumentId);
                    LatestFileManager.saveInsListToFile(insList.keySet());
                    ToastNotificationUtils.showToast(BaseApplicationLike.getContext(), "该合约已被移除自选列表");
                    mMenuItem.setIcon(R.mipmap.ic_favorite_border_white_24dp);
                    mFutureInfoActivityPresenter.refreshOptionalQuotesPopup(new ArrayList<>(insList.keySet()));
                } else {
                    QuoteEntity quoteEntity = new QuoteEntity();
                    quoteEntity.setInstrument_id(mInstrumentId);
                    insList.put(mInstrumentId, quoteEntity);
                    LatestFileManager.saveInsListToFile(insList.keySet());
                    ToastNotificationUtils.showToast(BaseApplicationLike.getContext(), "该合约已添加到自选列表");
                    mMenuItem.setIcon(R.mipmap.ic_favorite_white_24dp);
                    mFutureInfoActivityPresenter.refreshOptionalQuotesPopup(new ArrayList<>(insList.keySet()));
                }
                break;
            case android.R.id.home:
                if (mFutureInfoActivityPresenter.closeKeyboard()) break;
                Intent intent = new Intent();
                setResult(RESULT_OK, intent);
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * date: 7/12/17
     * author: chenli
     * description: 登录页销毁返回判断逻辑，处理盘口、持仓、挂单、交易页的显示状态
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (resultCode) {
            //登录页返回
            case RESULT_CANCELED:
                //未登录返回后，跳转到盘口页，重新点击登录，不使持仓页、挂单页、交易页暴露
                mBinding.vpInfoContent.setCurrentItem(0, false);
                mBinding.rbHandicapInfo.setChecked(true);
                break;
            case RESULT_OK:
                switch (requestCode) {
                    //登陆成功后跳转到相应页
                    case POSITION_JUMP_TO_LOG_IN_ACTIVITY:
                        mBinding.vpInfoContent.setCurrentItem(1, false);
                        mBinding.rbPositionInfo.setChecked(true);
                        break;
                    case ORDER_JUMP_TO_LOG_IN_ACTIVITY:
                        mBinding.vpInfoContent.setCurrentItem(2, false);
                        mBinding.rbOrderInfo.setChecked(true);
                        break;
                    case TRANSACTION_JUMP_TO_LOG_IN_ACTIVITY:
                        mBinding.vpInfoContent.setCurrentItem(3, false);
                        mBinding.rbTransactionInfo.setChecked(true);
                        break;
                    default:
                        break;
                }
                break;
            default:
                break;
        }
    }

    /**
     * date: 7/7/17
     * author: chenli
     * description: 通过EventBus框架实现fragment与activity之间的数据传递，这里接受本页和fragmentPosition/currentDayFragment/KlineFragment页发过来的合约代码数据；
     * 在接受到数据后向服务器发送合约代码指令
     */
    @Subscribe
    public void onEvent(IdEvent data) {
        //持仓列表中可能出现合约一致,方向不同的情形
        String instrument_id_new = data.getInstrument_id();
        if (!mInstrumentId.equals(instrument_id_new)) {
            mInstrumentId = instrument_id_new;
            if (BaseApplicationLike.getWebSocketService() != null)
                BaseApplicationLike.getWebSocketService().sendSubscribeQuote(mInstrumentId);
            SearchEntity searchEntity = LatestFileManager.getSearchEntities().get(mInstrumentId);
            if (searchEntity != null) mToolbarTitle.setText(searchEntity.getInstrumentName());
            else mToolbarTitle.setText(mInstrumentId);
            if (LatestFileManager.getOptionalInsList().containsKey(mInstrumentId)) {
                mMenuItem.setIcon(R.mipmap.ic_favorite_white_24dp);
            } else {
                mMenuItem.setIcon(R.mipmap.ic_favorite_border_white_24dp);
            }
        }
    }

    //交易服务器断开连接后发送一条信息，使信息页显示
    @Subscribe
    public void onEvent(String msg) {
        if (LOG_OUT.equals(msg)) {
            mBinding.vpInfoContent.setCurrentItem(0, false);
            mBinding.rbHandicapInfo.setChecked(true);
        }
    }

    /**
     * date: 7/7/17
     * author: chenli
     * description: 获取持仓、挂单、均线开关状态值
     */
    public boolean isPosition() {
        return mFutureInfoActivityPresenter.isPosition();
    }

    public boolean isPending() {
        return mFutureInfoActivityPresenter.isPending();
    }

    public boolean isAverage() {
        return mFutureInfoActivityPresenter.isAverage();
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

    public RadioGroup getTabsUp() {
        return mBinding.rgTabUp;
    }
}
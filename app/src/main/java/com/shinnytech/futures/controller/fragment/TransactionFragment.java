package com.shinnytech.futures.controller.fragment;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.PopupMenu;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.shinnytech.futures.R;
import com.shinnytech.futures.application.BaseApplication;
import com.shinnytech.futures.databinding.FragmentTransactionBinding;
import com.shinnytech.futures.model.bean.accountinfobean.AccountEntity;
import com.shinnytech.futures.model.bean.accountinfobean.PositionEntity;
import com.shinnytech.futures.model.bean.accountinfobean.UserEntity;
import com.shinnytech.futures.model.bean.eventbusbean.IdEvent;
import com.shinnytech.futures.model.bean.futureinfobean.QuoteEntity;
import com.shinnytech.futures.model.bean.searchinfobean.SearchEntity;
import com.shinnytech.futures.model.engine.DataManager;
import com.shinnytech.futures.model.engine.LatestFileManager;
import com.shinnytech.futures.utils.KeyboardUtils;
import com.shinnytech.futures.utils.MathUtils;
import com.shinnytech.futures.utils.ToastNotificationUtils;
import com.shinnytech.futures.controller.activity.FutureInfoActivity;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import static com.shinnytech.futures.constants.CommonConstants.MD_MESSAGE;
import static com.shinnytech.futures.constants.CommonConstants.TD_MESSAGE;
import static com.shinnytech.futures.model.service.WebSocketService.MD_BROADCAST_ACTION;
import static com.shinnytech.futures.model.service.WebSocketService.TD_BROADCAST_ACTION;

/**
 * date: 6/8/17
 * author: chenli
 * description: 交易页
 * version:
 * state: done
 */
public class TransactionFragment extends LazyLoadFragment implements View.OnClickListener {

    private String mInstrumentIdTransaction;
    private String mInstrumentId;
    private String mExchangeId;
    private String mDirection;

    private DataManager sDataManager = DataManager.getInstance();
    private BroadcastReceiver mReceiverAccount;
    private BroadcastReceiver mReceiverPrice;
    /**
     * date: 7/9/17
     * description: 价格键盘
     */
    private KeyboardUtils mKeyboardUtilsPrice;
    /**
     * date: 7/9/17
     * description: 手数键盘
     */
    private KeyboardUtils mKeyboardUtilsVolume;
    private String mPriceType = "最新价";
    /**
     * date: 7/9/17
     * description: 平仓状态是否为实时数字标志
     */
    private boolean mIsClosePriceShow = false;
    private FragmentTransactionBinding mBinding;

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mInstrumentId = ((FutureInfoActivity) getActivity()).getInstrument_id();
        SearchEntity searchEntity = LatestFileManager.getSearchEntities().get(mInstrumentId);
        if (mInstrumentId.contains("KQ") && searchEntity != null)
            mInstrumentIdTransaction = searchEntity.getUnderlying_symbol();
        else mInstrumentIdTransaction = mInstrumentId;
        mExchangeId = mInstrumentIdTransaction.split("\\.")[0];
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_transaction, container, false);
        initEvent();
        return mBinding.getRoot();
    }

    private void initEvent() {
        EventBus.getDefault().register(this);
        mBinding.bidOpenPosition.setOnClickListener(this);
        mBinding.askOpenPosition.setOnClickListener(this);
        mBinding.closePosition.setOnClickListener(this);

        //弹出价格键盘，不可以用单击监控器，会弹出系统自带键盘
        mBinding.price.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                mKeyboardUtilsPrice = new KeyboardUtils(getActivity(),
                        R.xml.future_price, mInstrumentId);
                mKeyboardUtilsPrice.attachTo(mBinding.price);
                if (!mKeyboardUtilsPrice.isVisible()) {
                    mKeyboardUtilsPrice.showKeyboard();
                }
                return true;
            }

        });

        //弹出手数键盘
        mBinding.volume.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                mKeyboardUtilsVolume = new KeyboardUtils(getActivity(),
                        R.xml.future_volume, mInstrumentId);
                mKeyboardUtilsVolume.attachTo(mBinding.volume);
                if (!mKeyboardUtilsVolume.isVisible()) {
                    mKeyboardUtilsVolume.showKeyboard();
                }
                return true;
            }

        });

        //价格输入框监听器，实现下单板上价格联动
        mBinding.price.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                QuoteEntity quoteEntity = sDataManager.getRtnData().getQuotes().get(mInstrumentId);
                if (quoteEntity != null) {

                    switch (mBinding.price.getText().toString()) {
                        case "排队价":
                            String ask_price1 = LatestFileManager.saveScaleByPtick(quoteEntity.getAsk_price1(),
                                    quoteEntity.getInstrument_id());
                            String bid_price1 = LatestFileManager.saveScaleByPtick(quoteEntity.getBid_price1(),
                                    quoteEntity.getInstrument_id());
                            mPriceType = "排队价";
                            mBinding.bidPrice11.setText(bid_price1);
                            mBinding.askPrice11.setText(ask_price1);
                            if (mIsClosePriceShow) {
                                if ("多".equals(mDirection))
                                    mBinding.closePrice.setText(ask_price1);
                                else if ("空".equals(mDirection))
                                    mBinding.closePrice.setText(bid_price1);
                            }
                            break;
                        case "对手价":
                            String ask_price1_1 = LatestFileManager.saveScaleByPtick(quoteEntity.getAsk_price1(),
                                    quoteEntity.getInstrument_id());
                            String bid_price1_1 = LatestFileManager.saveScaleByPtick(quoteEntity.getBid_price1(),
                                    quoteEntity.getInstrument_id());
                            mPriceType = "对手价";
                            mBinding.bidPrice11.setText(ask_price1_1);
                            mBinding.askPrice11.setText(bid_price1_1);
                            if (mIsClosePriceShow) {
                                if ("多".equals(mDirection))
                                    mBinding.closePrice.setText(bid_price1_1);
                                else if ("空".equals(mDirection))
                                    mBinding.closePrice.setText(ask_price1_1);
                            }
                            break;
                        case "市价":
                            String lower_limit = LatestFileManager.saveScaleByPtick(quoteEntity.getLower_limit(),
                                    quoteEntity.getInstrument_id());
                            String upper_limit = LatestFileManager.saveScaleByPtick(quoteEntity.getUpper_limit(),
                                    quoteEntity.getInstrument_id());
                            mPriceType = "市价";
                            mBinding.bidPrice11.setText(upper_limit);
                            mBinding.askPrice11.setText(lower_limit);
                            if (mIsClosePriceShow)
                                mBinding.closePrice.setText(upper_limit);
                            break;
                        case "最新价":
                            String last_price = LatestFileManager.saveScaleByPtick(quoteEntity.getLast_price(),
                                    quoteEntity.getInstrument_id());
                            mPriceType = "最新价";
                            mBinding.bidPrice11.setText(last_price);
                            mBinding.askPrice11.setText(last_price);
                            if (mIsClosePriceShow)
                                mBinding.closePrice.setText(last_price);
                            break;
                        default:
                            mPriceType = "用户设置价";
                            mBinding.bidPrice11.setText(mBinding.price.getText());
                            mBinding.askPrice11.setText(mBinding.price.getText());
                            if (mIsClosePriceShow)
                                mBinding.closePrice.setText(mBinding.price.getText());
                            break;
                    }
                }
            }
        });

    }

    @Override
    public void onResume() {
        super.onResume();
        registerBroaderCast();
        update();
    }

    @Override
    public void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mReceiverAccount);
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mReceiverPrice);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void update() {
        refreshPrice();
        refreshAccount();
        refreshPosition();
    }

    /**
     * date: 7/9/17
     * author: chenli
     * description: 初始化持仓信息，据此设置下单版的显示状态
     */
    private void refreshPosition() {
        try {
            mBinding.minPrice.setText(LatestFileManager.getSearchEntities().get(mInstrumentIdTransaction).getpTick());
            mBinding.price.setText("最新价");
            mPriceType = "最新价";
            String key = mInstrumentIdTransaction;
            UserEntity userEntity = sDataManager.getTradeBean().getUsers().get(sDataManager.USER_ID);
            if (userEntity == null) return;
            PositionEntity positionEntity = userEntity.getPositions().get(key);
            if (positionEntity == null) {
                this.mDirection = "";
                mBinding.volume.setText("1");
                mIsClosePriceShow = false;
                mBinding.bidPrice1Direction.setText("买多");
                mBinding.askPrice1Direction.setText("卖空");
                mBinding.closePrice.setText("先开先平");
            } else {
                String volume_available_long = MathUtils.add(positionEntity.getVolume_long_his(), positionEntity.getVolume_long_today());
                int volume_long = Integer.parseInt(MathUtils.add(volume_available_long, positionEntity.getVolume_long_frozen_his()));
                String volume_available_short = MathUtils.add(positionEntity.getVolume_short_his(), positionEntity.getVolume_short_today());
                int volume_short = Integer.parseInt(MathUtils.add(volume_available_short, positionEntity.getVolume_short_frozen_his()));
                if (volume_long != 0 && volume_short == 0) {
                    this.mDirection = "多";
                    mBinding.volume.setText(volume_available_long);
                    mIsClosePriceShow = true;
                    mBinding.bidPrice1Direction.setText("加多");
                    mBinding.askPrice1Direction.setText("锁仓");
                    mBinding.closePrice.setText(mBinding.bidPrice11.getText().toString());
                } else if (volume_long == 0 && volume_short != 0) {
                    this.mDirection = "空";
                    mBinding.volume.setText(volume_available_short);
                    mIsClosePriceShow = true;
                    mBinding.bidPrice1Direction.setText("锁仓");
                    mBinding.askPrice1Direction.setText("加空");
                    mBinding.closePrice.setText(mBinding.bidPrice11.getText().toString());
                } else if (volume_long != 0 && volume_short != 0) {
                    this.mDirection = "双向";
                    mBinding.volume.setText("1");
                    mIsClosePriceShow = false;
                    mBinding.bidPrice1Direction.setText("买多");
                    mBinding.askPrice1Direction.setText("卖空");
                    mBinding.closePrice.setText("锁仓状态");
                } else {
                    this.mDirection = "";
                    mBinding.volume.setText("1");
                    mIsClosePriceShow = false;
                    mBinding.bidPrice1Direction.setText("买多");
                    mBinding.askPrice1Direction.setText("卖空");
                    mBinding.closePrice.setText("先开先平");
                }
            }
            mBinding.closeDirection.setText("平仓");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updatePosition(){
        try {
            String key = mInstrumentIdTransaction;
            UserEntity userEntity = sDataManager.getTradeBean().getUsers().get(sDataManager.USER_ID);
            if (userEntity == null) return;
            PositionEntity positionEntity = userEntity.getPositions().get(key);
            if (positionEntity == null) {
                this.mDirection = "";
                mIsClosePriceShow = false;
                mBinding.bidPrice1Direction.setText("买多");
                mBinding.askPrice1Direction.setText("卖空");
                mBinding.closePrice.setText("先开先平");
            } else {
                String volume_available_long = MathUtils.add(positionEntity.getVolume_long_his(), positionEntity.getVolume_long_today());
                int volume_long = Integer.parseInt(MathUtils.add(volume_available_long, positionEntity.getVolume_long_frozen_his()));
                String volume_available_short = MathUtils.add(positionEntity.getVolume_short_his(), positionEntity.getVolume_short_today());
                int volume_short = Integer.parseInt(MathUtils.add(volume_available_short, positionEntity.getVolume_short_frozen_his()));
                if (volume_long != 0 && volume_short == 0) {
                    this.mDirection = "多";
                    mIsClosePriceShow = true;
                    mBinding.bidPrice1Direction.setText("加多");
                    mBinding.askPrice1Direction.setText("锁仓");
                    mBinding.closePrice.setText(mBinding.bidPrice11.getText().toString());
                } else if (volume_long == 0 && volume_short != 0) {
                    this.mDirection = "空";
                    mIsClosePriceShow = true;
                    mBinding.bidPrice1Direction.setText("锁仓");
                    mBinding.askPrice1Direction.setText("加空");
                    mBinding.closePrice.setText(mBinding.bidPrice11.getText().toString());
                } else if (volume_long != 0 && volume_short != 0) {
                    this.mDirection = "双向";
                    mIsClosePriceShow = false;
                    mBinding.bidPrice1Direction.setText("买多");
                    mBinding.askPrice1Direction.setText("卖空");
                    mBinding.closePrice.setText("锁仓状态");
                } else {
                    this.mDirection = "";
                    mIsClosePriceShow = false;
                    mBinding.bidPrice1Direction.setText("买多");
                    mBinding.askPrice1Direction.setText("卖空");
                    mBinding.closePrice.setText("先开先平");
                }
            }
            mBinding.closeDirection.setText("平仓");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * date: 7/9/17
     * author: chenli
     * description: 刷新价格信息
     */
    private void refreshPrice() {
        QuoteEntity quoteEntity = sDataManager.getRtnData().getQuotes().get(mInstrumentId);
        if (quoteEntity != null) {
            mBinding.setQuote(quoteEntity);
            //控制下单板的显示模式
            switch (mPriceType) {
                case "排队价":
                    String ask_price1 = LatestFileManager.saveScaleByPtick(quoteEntity.getAsk_price1(),
                            quoteEntity.getInstrument_id());
                    String bid_price1 = LatestFileManager.saveScaleByPtick(quoteEntity.getBid_price1(),
                            quoteEntity.getInstrument_id());
                    mBinding.bidPrice11.setText(bid_price1);
                    mBinding.askPrice11.setText(ask_price1);
                    if (mIsClosePriceShow) {
                        if ("多".equals(mDirection))
                            mBinding.closePrice.setText(ask_price1);
                        else if ("空".equals(mDirection))
                            mBinding.closePrice.setText(bid_price1);
                    }
                    break;
                case "对手价":
                    String ask_price1_1 = LatestFileManager.saveScaleByPtick(quoteEntity.getAsk_price1(),
                            quoteEntity.getInstrument_id());
                    String bid_price1_1 = LatestFileManager.saveScaleByPtick(quoteEntity.getBid_price1(),
                            quoteEntity.getInstrument_id());
                    mBinding.bidPrice11.setText(ask_price1_1);
                    mBinding.askPrice11.setText(bid_price1_1);
                    if (mIsClosePriceShow) {
                        if ("多".equals(mDirection))
                            mBinding.closePrice.setText(bid_price1_1);
                        else if ("空".equals(mDirection))
                            mBinding.closePrice.setText(ask_price1_1);
                    }
                    break;
                case "市价":
                    String lower_limit = LatestFileManager.saveScaleByPtick(quoteEntity.getLower_limit(),
                            quoteEntity.getInstrument_id());
                    String upper_limit = LatestFileManager.saveScaleByPtick(quoteEntity.getUpper_limit(),
                            quoteEntity.getInstrument_id());
                    mBinding.bidPrice11.setText(upper_limit);
                    mBinding.askPrice11.setText(lower_limit);
                    if (mIsClosePriceShow)
                        mBinding.closePrice.setText(upper_limit);
                    break;
                case "最新价":
                    String last_price = LatestFileManager.saveScaleByPtick(quoteEntity.getLast_price(),
                            quoteEntity.getInstrument_id());
                    mBinding.bidPrice11.setText(last_price);
                    mBinding.askPrice11.setText(last_price);
                    if (mIsClosePriceShow) mBinding.closePrice.setText(last_price);
                    break;
                default:
                    break;
            }
        }
    }

    /**
     * date: 7/9/17
     * author: chenli
     * description: 刷新账户信息
     */
    private void refreshAccount() {
        UserEntity userEntity = sDataManager.getTradeBean().getUsers().get(sDataManager.USER_ID);
        if (userEntity == null) return;
        AccountEntity accountEntity = userEntity.getAccounts().get("CNY");
        mBinding.setAccount(accountEntity);
        if (accountEntity == null) return;
        String margin = LatestFileManager.getSearchEntities().get(mInstrumentIdTransaction).getMargin();
        if (margin.isEmpty()) mBinding.maxVolume.setText("0");
        else
            mBinding.maxVolume.setText(MathUtils.round(MathUtils.divide(accountEntity.getAvailable(), margin), 0));

    }

    private void registerBroaderCast() {
        mReceiverAccount = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String mDataString = intent.getStringExtra("msg");
                switch (mDataString) {
                    case TD_MESSAGE:
                        if (((FutureInfoActivity) getActivity()).getTabsInfo().getCheckedRadioButtonId() == R.id.rb_transaction_info)
                            refreshAccount();
                        break;
                    default:
                        break;
                }
            }
        };

        mReceiverPrice = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String mDataString = intent.getStringExtra("msg");
                switch (mDataString) {
                    case MD_MESSAGE:
                        if (((FutureInfoActivity) getActivity()).getTabsInfo().getCheckedRadioButtonId() == R.id.rb_transaction_info)
                            refreshPrice();
                    default:
                        break;
                }
            }
        };
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mReceiverAccount, new IntentFilter(TD_BROADCAST_ACTION));
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mReceiverPrice, new IntentFilter(MD_BROADCAST_ACTION));
    }

    /**
     * date: 7/9/17
     * author: chenli
     * description: 下单动作
     */
    @Override
    public void onClick(final View v) {
        switch (v.getId()) {
            case R.id.bid_open_position:
                buyOpenPosition();
                break;
            case R.id.ask_open_position:
                sellOpenPosition();
                break;
            case R.id.close_position:
                switch (mBinding.closePrice.getText().toString()) {
                    case "锁仓状态":
                        lockClosePosition(v);
                        break;
                    case "先开先平":
                        firstClosePosition(v);
                        break;
                    default:
                        defaultClosePosition(v);
                        break;
                }
                break;
            default:
                break;
        }
    }

    /**
     * date: 20/9/18
     * author: chenli
     * description: 买开仓处理逻辑
     */
    private void buyOpenPosition(){
        if (mBinding.bidPrice11.getText() != null && mBinding.volume.getText() != null) {
            String price = mBinding.bidPrice11.getText().toString();
            String volume = mBinding.volume.getText().toString();
            if (price.length() == 0)
                ToastNotificationUtils.showToast(BaseApplication.getContext(), "价格不能为空");
            if (".".equals(price))
                ToastNotificationUtils.showToast(BaseApplication.getContext(), "价格输入不合法");
            if (volume.length() == 0)
                ToastNotificationUtils.showToast(BaseApplication.getContext(), "手数不能为空");
            if (volume.length() > 10)
                ToastNotificationUtils.showToast(BaseApplication.getContext(), "手数太大");
            if ("0".equals(volume))
                ToastNotificationUtils.showToast(BaseApplication.getContext(), "手数不能为零");
            if (price.length() != 0 && !".".equals(price) && volume.length() != 0 && volume.length() <= 10 && !"0".equals(volume))
                initDialog(mExchangeId, mInstrumentIdTransaction.split("\\.")[1], "买开", "BUY", "OPEN", volume, "LIMIT", price);

        }
    }

    /**
     * date: 20/9/18
     * author: chenli
     * description: 卖开仓处理逻辑
     */
    private void sellOpenPosition(){
        if (mBinding.askPrice11.getText() != null && mBinding.volume.getText() != null) {
            String price = mBinding.askPrice11.getText().toString();
            String volume = mBinding.volume.getText().toString();
            if (price.length() == 0)
                ToastNotificationUtils.showToast(BaseApplication.getContext(), "价格不能为空");
            if (".".equals(price))
                ToastNotificationUtils.showToast(BaseApplication.getContext(), "价格输入不合法");
            if (volume.length() == 0)
                ToastNotificationUtils.showToast(BaseApplication.getContext(), "手数不能为空");
            if (volume.length() > 10)
                ToastNotificationUtils.showToast(BaseApplication.getContext(), "手数太大");
            if ("0".equals(volume))
                ToastNotificationUtils.showToast(BaseApplication.getContext(), "手数不能为零");
            if (price.length() != 0 && !".".equals(price) && volume.length() != 0 && volume.length() <= 10 && !"0".equals(volume)) {
                initDialog(mExchangeId, mInstrumentIdTransaction.split("\\.")[1], "卖开", "SELL", "OPEN", volume, "LIMIT", price);

            }
        }
    }

    /**
     * date: 20/9/18
     * author: chenli
     * description: 锁仓平仓处理逻辑
     */
    private void lockClosePosition(final View v) {
        PopupMenu popup = new PopupMenu(getActivity(), v);
        popup.getMenuInflater().inflate(R.menu.fragment_transaction, popup.getMenu());
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.bid_position:
                        mDirection = "多";
                        mIsClosePriceShow = true;
                        mBinding.closeDirection.setText("平多");
                        mBinding.bidPrice1Direction.setText("加多");
                        mBinding.askPrice1Direction.setText("锁仓");
                        if ("排队价".equals(mPriceType))
                            mBinding.closePrice.setText(mBinding.askPrice11.getText().toString());
                        else
                            mBinding.closePrice.setText(mBinding.bidPrice11.getText().toString());
                        defaultClosePosition(v);
                        break;
                    case R.id.ask_position:
                        mDirection = "空";
                        mIsClosePriceShow = true;
                        mBinding.closeDirection.setText("平空");
                        mBinding.bidPrice1Direction.setText("锁仓");
                        mBinding.askPrice1Direction.setText("加空");
                        if ("对手价".equals(mPriceType))
                            mBinding.closePrice.setText(mBinding.askPrice11.getText().toString());
                        else
                            mBinding.closePrice.setText(mBinding.bidPrice11.getText().toString());
                        defaultClosePosition(v);
                        break;
                    default:
                        break;
                }
                return true;
            }
        });
        popup.show();
    }


    /**
     * date: 20/9/18
     * author: chenli
     * description: 先开先平平仓处理逻辑
     */
    private void firstClosePosition(final View v) {
        if (mInstrumentIdTransaction != null && mInstrumentIdTransaction.contains("&")) {
            PopupMenu popupCombine = new PopupMenu(getActivity(), v);
            popupCombine.getMenuInflater().inflate(R.menu.fragment_transaction, popupCombine.getMenu());
            popupCombine.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    switch (item.getItemId()) {
                        case R.id.bid_position:
                            mDirection = "多";
                            mIsClosePriceShow = true;
                            mBinding.closeDirection.setText("平多");
                            if ("排队价".equals(mPriceType))
                                mBinding.closePrice.setText(mBinding.askPrice11.getText().toString());
                            else
                                mBinding.closePrice.setText(mBinding.bidPrice11.getText().toString());
                            defaultClosePosition(v);
                            break;
                        case R.id.ask_position:
                            mDirection = "空";
                            mIsClosePriceShow = true;
                            mBinding.closeDirection.setText("平空");
                            if ("对手价".equals(mPriceType))
                                mBinding.closePrice.setText(mBinding.askPrice11.getText().toString());
                            else
                                mBinding.closePrice.setText(mBinding.bidPrice11.getText().toString());
                            defaultClosePosition(v);
                            break;
                        default:
                            break;
                    }
                    return true;
                }
            });
            popupCombine.show();
        }else {
            ToastNotificationUtils.showToast(BaseApplication.getContext(), "您还没有此合约持仓～");
        }
    }


    /**
     * date: 6/1/18
     * author: chenli
     * description: 默认平仓处理逻辑
     */
    private void defaultClosePosition(View v) {
        if (mBinding.closePrice.getText() != null && mBinding.volume.getText() != null && !"".equals(mDirection)) {
            final String price = mBinding.closePrice.getText().toString();
            final String volume = mBinding.volume.getText().toString();
            final String direction = "多".equals(mDirection) ? "SELL" : "BUY";
            if (price.length() == 0) {
                ToastNotificationUtils.showToast(BaseApplication.getContext(), "价格不能为空");
                return;
            }
            if (".".equals(price)) {
                ToastNotificationUtils.showToast(BaseApplication.getContext(), "价格输入不合法");
                return;
            }
            if (volume.length() == 0) {
                ToastNotificationUtils.showToast(BaseApplication.getContext(), "手数不能为空");
                return;
            }
            if (volume.length() > 10) {
                ToastNotificationUtils.showToast(BaseApplication.getContext(), "手数太大");
                return;
            }
            if ("0".equals(volume)) {
                ToastNotificationUtils.showToast(BaseApplication.getContext(), "手数不能为零");
                return;
            }
            try {
                final int volumeN = Integer.parseInt(volume);
                final String instrumentId = mInstrumentIdTransaction.split("\\.")[1];
                SearchEntity searchEntity = LatestFileManager.getSearchEntities().get(mInstrumentIdTransaction);
                if (searchEntity != null && ("上海国际能源交易中心".equals(searchEntity.getExchangeName())
                        || "上海期货交易所".equals(searchEntity.getExchangeName()))) {
                    UserEntity userEntity = sDataManager.getTradeBean().getUsers().get(sDataManager.USER_ID);
                    if (userEntity == null) return;
                    PositionEntity positionEntity = userEntity.getPositions().get(mInstrumentIdTransaction);
                    if (positionEntity == null) return;
                    int volume_today = 0;
                    int volume_history = 0;
                    if ("SELL".equals(direction)) {
                        volume_today = Integer.parseInt(positionEntity.getVolume_long_today());
                        volume_history = Integer.parseInt(positionEntity.getVolume_long_his());
                    } else if ("BUY".equals(direction)) {
                        volume_today = Integer.parseInt(positionEntity.getVolume_short_today());
                        volume_history = Integer.parseInt(positionEntity.getVolume_short_his());
                    }

                    if (volume_today > 0 && volume_history > 0) {
                        if (volumeN <= volume_today || volumeN <= volume_history) {
                            PopupMenu popup = new PopupMenu(getActivity(), v);
                            popup.getMenuInflater().inflate(R.menu.fragment_transaction_position, popup.getMenu());
                            popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                                @Override
                                public boolean onMenuItemClick(MenuItem item) {
                                    switch (item.getItemId()) {
                                        case R.id.close_today:
                                            initDialog(mExchangeId, instrumentId, "平今", direction, "CLOSETODAY", volume, "LIMIT", price);
                                            break;
                                        case R.id.close_history:
                                            initDialog(mExchangeId, instrumentId, "平昨", direction, "CLOSE", volume, "LIMIT", price);
                                            break;
                                        default:
                                            break;
                                    }
                                    return true;
                                }
                            });
                            popup.show();
                        } else if (volumeN > volume_today || volumeN > volume_history) {
                            int volume_sub = volumeN - volume_today;
                            initDialog(mExchangeId, instrumentId, "平今", "平昨", direction, "CLOSETODAY", "CLOSE", volume_today, volume_sub, "LIMIT", price);
                        }
                    } else if (volume_today == 0 && volume_history > 0) {
                        initDialog(mExchangeId, instrumentId, "平昨", direction, "CLOSE", volume, "LIMIT", price);
                    } else if (volume_today > 0 && volume_history == 0) {
                        initDialog(mExchangeId, instrumentId, "平今", direction, "CLOSETODAY", volume, "LIMIT", price);
                    }

                } else
                    initDialog(mExchangeId, instrumentId, "平仓", direction, "CLOSE", volume, "LIMIT", price);
            } catch (NumberFormatException ex) {
                ex.printStackTrace();
            }
        }
    }

    /**
     * date: 7/9/17
     * author: chenli
     * description: 更新合约代码以及持仓信息--来自自选择约选择以及持仓列表点击切换
     */
    @Subscribe
    public void onEvent(IdEvent data) {
        if (mInstrumentId.equals(data.getInstrument_id())) return;
        mInstrumentId = data.getInstrument_id();
        SearchEntity searchEntity = LatestFileManager.getSearchEntities().get(mInstrumentId);
        if (mInstrumentId.contains("KQ") && searchEntity != null)
            mInstrumentIdTransaction = searchEntity.getUnderlying_symbol();
        else mInstrumentIdTransaction = mInstrumentId;
        mExchangeId = mInstrumentIdTransaction.split("\\.")[0];
        update();
    }

    /**
     * date: 7/14/17
     * author: chenli
     * description: 下单弹出框，根据固定宽高值自定义dialog，注意宽高值从dimens.xml文件中得到；用于同时平今昨仓
     */
    private void initDialog(final String exchange_id, final String instrument_id,
                            String direction_title, String direction_title1, final String direction,
                            final String offset, final String offset1, final int volume, final int volume1,
                            final String price_type, final String price) {
        final Dialog dialog = new Dialog(getActivity(), R.style.Theme_Light_Dialog);
        View view = View.inflate(getActivity(), R.layout.view_dialog_insert_order, null);
        Window dialogWindow = dialog.getWindow();
        if (dialogWindow != null) {
            dialogWindow.getDecorView().setPadding(0, 0, 0, 0);
            WindowManager.LayoutParams lp = dialogWindow.getAttributes();
            dialogWindow.setGravity(Gravity.CENTER);
            lp.width = (int) getActivity().getResources().getDimension(R.dimen.order_dialog_width1);
            lp.height = (int) getActivity().getResources().getDimension(R.dimen.order_dialog_height1);
            dialogWindow.setAttributes(lp);
        }
        dialog.setContentView(view);
        dialog.setCancelable(false);
        TextView tv_instrument_id = view.findViewById(R.id.order_instrument_id);
        TextView tv_price = view.findViewById(R.id.order_price);
        TextView tv_direction = view.findViewById(R.id.order_direction);
        TextView tv_volume = view.findViewById(R.id.order_volume);
        TextView ok = view.findViewById(R.id.order_ok);
        TextView cancel = view.findViewById(R.id.order_cancel);
        TextView tv_comma1 = view.findViewById(R.id.order_comma1);
        tv_comma1.setVisibility(View.VISIBLE);
        TextView tv_comma2 = view.findViewById(R.id.order_comma2);
        tv_comma2.setVisibility(View.VISIBLE);
        TextView tv_direction1 = view.findViewById(R.id.order_direction1);
        tv_direction1.setVisibility(View.VISIBLE);
        TextView tv_volume1 = view.findViewById(R.id.order_volume1);
        tv_volume1.setVisibility(View.VISIBLE);
        TextView tv_unit = view.findViewById(R.id.order_unit);
        tv_unit.setVisibility(View.VISIBLE);
        tv_instrument_id.setText(instrument_id);
        tv_price.setText(price);
        tv_direction.setText(direction_title);
        tv_direction1.setText(direction_title1);
        tv_volume.setText(volume);
        tv_volume1.setText(volume1);
        try {
            final double priceN = Double.parseDouble(price);
            ok.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (BaseApplication.getWebSocketService() != null) {
                        BaseApplication.getWebSocketService().sendReqInsertOrder(exchange_id, instrument_id, direction, offset, volume, price_type, priceN);
                        BaseApplication.getWebSocketService().sendReqInsertOrder(exchange_id, instrument_id, direction, offset1, volume1, price_type, priceN);
                    }
                    updatePosition();
                    dialog.dismiss();
                }
            });
        } catch (NumberFormatException ex) {
            ex.printStackTrace();
        }
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    /**
     * date: 7/14/17
     * author: chenli
     * description: 下单弹出框，根据固定宽高值自定义dialog，注意宽高值从dimens.xml文件中得到
     */
    private void initDialog(final String exchange_id, final String instrument_id,
                            String direction_title, final String direction, final String offset, final String volume,
                            final String price_type, final String price) {
        final Dialog dialog = new Dialog(getActivity(), R.style.Theme_Light_Dialog);
        View view = View.inflate(getActivity(), R.layout.view_dialog_insert_order, null);
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
        TextView tv_instrument_id = view.findViewById(R.id.order_instrument_id);
        TextView tv_price = view.findViewById(R.id.order_price);
        TextView tv_direction = view.findViewById(R.id.order_direction);
        TextView tv_volume = view.findViewById(R.id.order_volume);
        TextView ok = view.findViewById(R.id.order_ok);
        TextView cancel = view.findViewById(R.id.order_cancel);
        tv_instrument_id.setText(instrument_id);
        tv_price.setText(price);
        tv_direction.setText(direction_title);
        tv_volume.setText(volume);
        try {
            final int volumeN = Integer.parseInt(volume);
            final double priceN = Double.parseDouble(price);
            ok.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (BaseApplication.getWebSocketService() != null)
                        BaseApplication.getWebSocketService().sendReqInsertOrder(exchange_id, instrument_id, direction, offset, volumeN, price_type, priceN);
                    updatePosition();
                    dialog.dismiss();
                }
            });
        } catch (NumberFormatException ex) {
            ex.printStackTrace();
        }
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    public KeyboardUtils getKeyboardUtilsPrice() {
        return mKeyboardUtilsPrice;
    }

    public KeyboardUtils getKeyboardUtilsVolume() {
        return mKeyboardUtilsVolume;
    }
}

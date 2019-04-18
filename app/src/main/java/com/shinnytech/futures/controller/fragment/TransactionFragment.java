package com.shinnytech.futures.controller.fragment;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.databinding.DataBindingUtil;
import android.graphics.Rect;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.PopupMenu;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import com.shinnytech.futures.R;
import com.shinnytech.futures.application.BaseApplication;
import com.shinnytech.futures.constants.CommonConstants;
import com.shinnytech.futures.controller.activity.FutureInfoActivity;
import com.shinnytech.futures.databinding.FragmentTransactionBinding;
import com.shinnytech.futures.databinding.ViewDialogKeyboardBinding;
import com.shinnytech.futures.model.bean.accountinfobean.AccountEntity;
import com.shinnytech.futures.model.bean.accountinfobean.OrderEntity;
import com.shinnytech.futures.model.bean.accountinfobean.PositionEntity;
import com.shinnytech.futures.model.bean.accountinfobean.UserEntity;
import com.shinnytech.futures.model.bean.eventbusbean.IdEvent;
import com.shinnytech.futures.model.bean.futureinfobean.QuoteEntity;
import com.shinnytech.futures.model.bean.searchinfobean.SearchEntity;
import com.shinnytech.futures.model.engine.DataManager;
import com.shinnytech.futures.model.engine.LatestFileManager;
import com.shinnytech.futures.utils.CloneUtils;
import com.shinnytech.futures.utils.DensityUtils;
import com.shinnytech.futures.utils.MathUtils;
import com.shinnytech.futures.utils.SPUtils;
import com.shinnytech.futures.utils.TimeUtils;
import com.shinnytech.futures.utils.ToastNotificationUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.List;

import static com.shinnytech.futures.constants.CommonConstants.CONFIG_LOGIN_DATE;
import static com.shinnytech.futures.constants.CommonConstants.COUNTERPARTY_PRICE;
import static com.shinnytech.futures.constants.CommonConstants.ACTION_ADD_SELL;
import static com.shinnytech.futures.constants.CommonConstants.ACTION_ADD_BUY;
import static com.shinnytech.futures.constants.CommonConstants.DIRECTION_BOTH_ZN;
import static com.shinnytech.futures.constants.CommonConstants.DIRECTION_BUY;
import static com.shinnytech.futures.constants.CommonConstants.ACTION_OPEN_BUY;
import static com.shinnytech.futures.constants.CommonConstants.DIRECTION_BUY_ZN;
import static com.shinnytech.futures.constants.CommonConstants.ACTION_CLOSE_SELL;
import static com.shinnytech.futures.constants.CommonConstants.ACTION_CLOSE_BUY;
import static com.shinnytech.futures.constants.CommonConstants.OFFSET_CLOSE_HISTORY;
import static com.shinnytech.futures.constants.CommonConstants.OFFSET_CLOSE_TODAY_ZN;
import static com.shinnytech.futures.constants.CommonConstants.OFFSET_CLOSE_HISTORY_ZN;
import static com.shinnytech.futures.constants.CommonConstants.OFFSET_CLOSE_ZN;
import static com.shinnytech.futures.constants.CommonConstants.STATUS_ALIVE;
import static com.shinnytech.futures.constants.CommonConstants.STATUS_FIRST_OPEN_FIRST_CLOSE;
import static com.shinnytech.futures.constants.CommonConstants.ACTION_LOCK;
import static com.shinnytech.futures.constants.CommonConstants.STATUS_LOCK;
import static com.shinnytech.futures.constants.CommonConstants.DIRECTION_SELL;
import static com.shinnytech.futures.constants.CommonConstants.ACTION_OPEN_SELL;
import static com.shinnytech.futures.constants.CommonConstants.DIRECTION_SELL_ZN;
import static com.shinnytech.futures.constants.CommonConstants.INE_ZN;
import static com.shinnytech.futures.constants.CommonConstants.LATEST_PRICE;
import static com.shinnytech.futures.constants.CommonConstants.MARKET_PRICE;
import static com.shinnytech.futures.constants.CommonConstants.MD_MESSAGE;
import static com.shinnytech.futures.constants.CommonConstants.OFFSET_CLOSE;
import static com.shinnytech.futures.constants.CommonConstants.OFFSET_CLOSE_TODAY;
import static com.shinnytech.futures.constants.CommonConstants.OFFSET_OPEN;
import static com.shinnytech.futures.constants.CommonConstants.PRICE_TYPE_LIMIT;
import static com.shinnytech.futures.constants.CommonConstants.QUEUED_PRICE;
import static com.shinnytech.futures.constants.CommonConstants.SHFE_ZN;
import static com.shinnytech.futures.constants.CommonConstants.TD_MESSAGE;
import static com.shinnytech.futures.constants.CommonConstants.USER_PRICE;
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
    private Context sContext = BaseApplication.getContext();
    private BroadcastReceiver mReceiverAccount;
    private BroadcastReceiver mReceiverPrice;
    /**
     * date: 7/9/17
     * description: 平仓状态是否为实时数字标志
     */
    private boolean mIsClosePriceShow = false;
    private boolean mIsRefreshPosition = true;
    /**
     * date: 2019/4/18
     * description: 是否重新平仓
     */
    private boolean mIsReClose = false;
    private FragmentTransactionBinding mBinding;
    private boolean mIsShowDialog;

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mInstrumentId = ((FutureInfoActivity) getActivity()).getInstrument_id();
        SearchEntity searchEntity = LatestFileManager.getSearchEntities().get(mInstrumentId);
        if (mInstrumentId.contains("KQ") && searchEntity != null)
            mInstrumentIdTransaction = searchEntity.getUnderlying_symbol();
        else mInstrumentIdTransaction = mInstrumentId;
        mExchangeId = mInstrumentIdTransaction.split("\\.")[0];
        mIsShowDialog = (boolean) SPUtils.get(sContext, CommonConstants.CONFIG_INSERT_ORDER_CONFIRM, true);
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
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    mBinding.price.requestFocus();
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        mBinding.price.setShowSoftInputOnFocus(false);
                    } else {
                        View view1 = getActivity().findViewById(android.R.id.content);
                        if (view1 != null) {
                            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                            imm.hideSoftInputFromWindow(view1.getWindowToken(), 0);
                        }
                    }
                    mBinding.price.setSelection(0, mBinding.price.getText().length());
                    popupKeyboardDialog(mBinding.price, R.xml.future_price);
                }
                return true;
            }

        });

        //弹出手数键盘
        mBinding.volume.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    mBinding.volume.requestFocus();
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        mBinding.volume.setShowSoftInputOnFocus(false);
                    } else {
                        View view1 = getActivity().findViewById(android.R.id.content);
                        if (view1 != null) {
                            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                            imm.hideSoftInputFromWindow(view1.getWindowToken(), 0);
                        }
                    }
                    mBinding.volume.setSelection(0, mBinding.volume.getText().length());
                    popupKeyboardDialog(mBinding.volume, R.xml.future_volume);
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
                        case QUEUED_PRICE:
                            String ask_price1 = LatestFileManager.saveScaleByPtick(quoteEntity.getAsk_price1(),
                                    quoteEntity.getInstrument_id());
                            String bid_price1 = LatestFileManager.saveScaleByPtick(quoteEntity.getBid_price1(),
                                    quoteEntity.getInstrument_id());
                            sDataManager.PRICE_TYPE = QUEUED_PRICE;
                            mBinding.bidPrice11.setText(bid_price1);
                            mBinding.askPrice11.setText(ask_price1);
                            if (mIsClosePriceShow) {
                                if (DIRECTION_BUY_ZN.equals(mDirection))
                                    mBinding.closePrice.setText(ask_price1);
                                else if (DIRECTION_SELL_ZN.equals(mDirection))
                                    mBinding.closePrice.setText(bid_price1);
                            }
                            break;
                        case COUNTERPARTY_PRICE:
                            String ask_price1_1 = LatestFileManager.saveScaleByPtick(quoteEntity.getAsk_price1(),
                                    quoteEntity.getInstrument_id());
                            String bid_price1_1 = LatestFileManager.saveScaleByPtick(quoteEntity.getBid_price1(),
                                    quoteEntity.getInstrument_id());
                            sDataManager.PRICE_TYPE = COUNTERPARTY_PRICE;
                            mBinding.bidPrice11.setText(ask_price1_1);
                            mBinding.askPrice11.setText(bid_price1_1);
                            if (mIsClosePriceShow) {
                                if (DIRECTION_BUY_ZN.equals(mDirection))
                                    mBinding.closePrice.setText(bid_price1_1);
                                else if (DIRECTION_SELL_ZN.equals(mDirection))
                                    mBinding.closePrice.setText(ask_price1_1);
                            }
                            break;
                        case MARKET_PRICE:
                            String lower_limit = LatestFileManager.saveScaleByPtick(quoteEntity.getLower_limit(),
                                    quoteEntity.getInstrument_id());
                            String upper_limit = LatestFileManager.saveScaleByPtick(quoteEntity.getUpper_limit(),
                                    quoteEntity.getInstrument_id());
                            sDataManager.PRICE_TYPE = MARKET_PRICE;
                            mBinding.bidPrice11.setText(upper_limit);
                            mBinding.askPrice11.setText(lower_limit);
                            if (mIsClosePriceShow) {
                                if (DIRECTION_BUY_ZN.equals(mDirection))
                                    mBinding.closePrice.setText(lower_limit);
                                else if (DIRECTION_SELL_ZN.equals(mDirection))
                                    mBinding.closePrice.setText(upper_limit);
                            }
                            break;
                        case LATEST_PRICE:
                            String last_price = LatestFileManager.saveScaleByPtick(quoteEntity.getLast_price(),
                                    quoteEntity.getInstrument_id());
                            sDataManager.PRICE_TYPE = LATEST_PRICE;
                            mBinding.bidPrice11.setText(last_price);
                            mBinding.askPrice11.setText(last_price);
                            if (mIsClosePriceShow)
                                mBinding.closePrice.setText(last_price);
                            break;
                        default:
                            sDataManager.PRICE_TYPE = USER_PRICE;
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
        initPosition();
    }

    /**
     * date: 7/9/17
     * author: chenli
     * description: 根据持仓状态，初始价格手数信息，据此设置下单版的显示状态
     */
    private void initPosition() {
        try {
            if (USER_PRICE.equals(sDataManager.PRICE_TYPE))
                mBinding.price.setText(COUNTERPARTY_PRICE);
            else mBinding.price.setText(sDataManager.PRICE_TYPE);
            String key = mInstrumentIdTransaction;
            UserEntity userEntity = sDataManager.getTradeBean().getUsers().get(sDataManager.USER_ID);
            if (userEntity == null) return;
            PositionEntity positionEntity = userEntity.getPositions().get(key);
            if (positionEntity == null) {
                this.mDirection = "";
                mBinding.volume.setText("1");
                mIsClosePriceShow = false;
                mBinding.bidPrice1Direction.setText(ACTION_OPEN_BUY);
                mBinding.askPrice1Direction.setText(ACTION_OPEN_SELL);
                mBinding.closePrice.setText(STATUS_FIRST_OPEN_FIRST_CLOSE);
            } else {
                String volume_available_long = MathUtils.add(positionEntity.getVolume_long_his(), positionEntity.getVolume_long_today());
                int volume_long = Integer.parseInt(MathUtils.add(volume_available_long, positionEntity.getVolume_long_frozen_his()));
                String volume_available_short = MathUtils.add(positionEntity.getVolume_short_his(), positionEntity.getVolume_short_today());
                int volume_short = Integer.parseInt(MathUtils.add(volume_available_short, positionEntity.getVolume_short_frozen_his()));
                if (volume_long != 0 && volume_short == 0) {
                    this.mDirection = DIRECTION_BUY_ZN;
                    mBinding.volume.setText(volume_available_long);
                    mIsClosePriceShow = true;
                    mBinding.bidPrice1Direction.setText(ACTION_ADD_BUY);
                    mBinding.askPrice1Direction.setText(ACTION_LOCK);
                    mBinding.closePrice.setText(mBinding.askPrice11.getText().toString());
                } else if (volume_long == 0 && volume_short != 0) {
                    this.mDirection = DIRECTION_SELL_ZN;
                    mBinding.volume.setText(volume_available_short);
                    mIsClosePriceShow = true;
                    mBinding.bidPrice1Direction.setText(ACTION_LOCK);
                    mBinding.askPrice1Direction.setText(ACTION_ADD_SELL);
                    mBinding.closePrice.setText(mBinding.bidPrice11.getText().toString());
                } else if (volume_long != 0 && volume_short != 0) {
                    if (sDataManager.POSITION_DIRECTION.isEmpty()) {
                        this.mDirection = DIRECTION_BOTH_ZN;
                        mBinding.volume.setText("1");
                        mIsClosePriceShow = false;
                        mBinding.bidPrice1Direction.setText(ACTION_OPEN_BUY);
                        mBinding.askPrice1Direction.setText(ACTION_OPEN_SELL);
                        mBinding.closePrice.setText(STATUS_LOCK);
                    } else {
                        switch (sDataManager.POSITION_DIRECTION) {
                            case DIRECTION_BUY_ZN:
                                this.mDirection = DIRECTION_BUY_ZN;
                                mBinding.volume.setText(volume_available_long);
                                mIsClosePriceShow = true;
                                mBinding.bidPrice1Direction.setText(ACTION_ADD_BUY);
                                mBinding.askPrice1Direction.setText(ACTION_LOCK);
                                mBinding.closePrice.setText(mBinding.askPrice11.getText().toString());
                                break;
                            case DIRECTION_SELL_ZN:
                                this.mDirection = DIRECTION_SELL_ZN;
                                mBinding.volume.setText(volume_available_short);
                                mIsClosePriceShow = true;
                                mBinding.bidPrice1Direction.setText(ACTION_LOCK);
                                mBinding.askPrice1Direction.setText(ACTION_ADD_SELL);
                                mBinding.closePrice.setText(mBinding.bidPrice11.getText().toString());
                                break;
                            default:
                                break;
                        }
                        mIsRefreshPosition = false;
                        sDataManager.POSITION_DIRECTION = "";
                    }
                } else {
                    this.mDirection = "";
                    mBinding.volume.setText("1");
                    mIsClosePriceShow = false;
                    mBinding.bidPrice1Direction.setText(ACTION_OPEN_BUY);
                    mBinding.askPrice1Direction.setText(ACTION_OPEN_SELL);
                    mBinding.closePrice.setText(STATUS_FIRST_OPEN_FIRST_CLOSE);
                }
            }
            mBinding.closeDirection.setText(OFFSET_CLOSE_ZN);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * date: 2018/11/9
     * author: chenli
     * description: 下单后刷新下单版状态
     */
    private void refreshPosition() {
        try {
            String key = mInstrumentIdTransaction;
            UserEntity userEntity = sDataManager.getTradeBean().getUsers().get(sDataManager.USER_ID);
            if (userEntity == null) return;
            PositionEntity positionEntity = userEntity.getPositions().get(key);
            if (positionEntity == null) {
                this.mDirection = "";
                mIsClosePriceShow = false;
                mBinding.bidPrice1Direction.setText(ACTION_OPEN_BUY);
                mBinding.askPrice1Direction.setText(ACTION_OPEN_SELL);
                mBinding.closePrice.setText(STATUS_FIRST_OPEN_FIRST_CLOSE);
            } else {
                String volume_available_long = MathUtils.add(positionEntity.getVolume_long_his(), positionEntity.getVolume_long_today());
                int volume_long = Integer.parseInt(MathUtils.add(volume_available_long, positionEntity.getVolume_long_frozen_his()));
                String volume_available_short = MathUtils.add(positionEntity.getVolume_short_his(), positionEntity.getVolume_short_today());
                int volume_short = Integer.parseInt(MathUtils.add(volume_available_short, positionEntity.getVolume_short_frozen_his()));
                if (volume_long != 0 && volume_short == 0) {
                    this.mDirection = DIRECTION_BUY_ZN;
                    mIsClosePriceShow = true;
                    mBinding.bidPrice1Direction.setText(ACTION_ADD_BUY);
                    mBinding.askPrice1Direction.setText(ACTION_LOCK);
                    mBinding.closePrice.setText(mBinding.askPrice11.getText().toString());
                } else if (volume_long == 0 && volume_short != 0) {
                    this.mDirection = DIRECTION_SELL_ZN;
                    mIsClosePriceShow = true;
                    mBinding.bidPrice1Direction.setText(ACTION_LOCK);
                    mBinding.askPrice1Direction.setText(ACTION_ADD_SELL);
                    mBinding.closePrice.setText(mBinding.bidPrice11.getText().toString());
                } else if (volume_long != 0 && volume_short != 0) {
                    this.mDirection = DIRECTION_BOTH_ZN;
                    mIsClosePriceShow = false;
                    mBinding.bidPrice1Direction.setText(ACTION_OPEN_BUY);
                    mBinding.askPrice1Direction.setText(ACTION_OPEN_SELL);
                    mBinding.closePrice.setText(STATUS_LOCK);
                } else {
                    this.mDirection = "";
                    mIsClosePriceShow = false;
                    mBinding.bidPrice1Direction.setText(ACTION_OPEN_BUY);
                    mBinding.askPrice1Direction.setText(ACTION_OPEN_SELL);
                    mBinding.closePrice.setText(STATUS_FIRST_OPEN_FIRST_CLOSE);
                }
            }
            mBinding.closeDirection.setText(OFFSET_CLOSE_ZN);
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
        if (quoteEntity == null) return;
        if (mInstrumentId.contains("&") && mInstrumentId.contains(" ")) {
            quoteEntity = CloneUtils.clone(quoteEntity);
            quoteEntity = LatestFileManager.calculateCombineQuotePart(quoteEntity);
        }
        mBinding.setQuote(quoteEntity);
        //控制下单板的显示模式
        switch (sDataManager.PRICE_TYPE) {
            case QUEUED_PRICE:
                String ask_price1 = LatestFileManager.saveScaleByPtick(quoteEntity.getAsk_price1(),
                        quoteEntity.getInstrument_id());
                String bid_price1 = LatestFileManager.saveScaleByPtick(quoteEntity.getBid_price1(),
                        quoteEntity.getInstrument_id());
                mBinding.bidPrice11.setText(bid_price1);
                mBinding.askPrice11.setText(ask_price1);
                if (mIsClosePriceShow) {
                    if (DIRECTION_BUY_ZN.equals(mDirection))
                        mBinding.closePrice.setText(ask_price1);
                    else if (DIRECTION_SELL_ZN.equals(mDirection))
                        mBinding.closePrice.setText(bid_price1);
                }
                break;
            case COUNTERPARTY_PRICE:
                String ask_price1_1 = LatestFileManager.saveScaleByPtick(quoteEntity.getAsk_price1(),
                        quoteEntity.getInstrument_id());
                String bid_price1_1 = LatestFileManager.saveScaleByPtick(quoteEntity.getBid_price1(),
                        quoteEntity.getInstrument_id());
                mBinding.bidPrice11.setText(ask_price1_1);
                mBinding.askPrice11.setText(bid_price1_1);
                if (mIsClosePriceShow) {
                    if (DIRECTION_BUY_ZN.equals(mDirection))
                        mBinding.closePrice.setText(bid_price1_1);
                    else if (DIRECTION_SELL_ZN.equals(mDirection))
                        mBinding.closePrice.setText(ask_price1_1);
                }
                break;
            case MARKET_PRICE:
                String lower_limit = LatestFileManager.saveScaleByPtick(quoteEntity.getLower_limit(),
                        quoteEntity.getInstrument_id());
                String upper_limit = LatestFileManager.saveScaleByPtick(quoteEntity.getUpper_limit(),
                        quoteEntity.getInstrument_id());
                mBinding.bidPrice11.setText(upper_limit);
                mBinding.askPrice11.setText(lower_limit);
                if (mIsClosePriceShow) {
                    if (DIRECTION_BUY_ZN.equals(mDirection))
                        mBinding.closePrice.setText(lower_limit);
                    else if (DIRECTION_SELL_ZN.equals(mDirection))
                        mBinding.closePrice.setText(upper_limit);
                }
                break;
            case LATEST_PRICE:
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

    /**
     * date: 7/9/17
     * author: chenli
     * description: 刷新账户信息
     */
    private void refreshAccount() {
        UserEntity userEntity = sDataManager.getTradeBean().getUsers().get(sDataManager.USER_ID);
        if (userEntity == null) return;
        AccountEntity accountEntity = userEntity.getAccounts().get("CNY");
        if (accountEntity == null) return;
        mBinding.setAccount(accountEntity);
    }

    private void registerBroaderCast() {
        mReceiverAccount = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String mDataString = intent.getStringExtra("msg");
                switch (mDataString) {
                    case TD_MESSAGE:
                        refreshAccount();
                        if (mIsRefreshPosition) refreshPosition();
                        //撤单平仓
                        if (mIsReClose){
                            if (detectCloseVolumeEnough()){
                                defaultClosePosition(mBinding.closePosition);
                                mIsReClose = false;
                            }
                        }
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
        checkPassword(v);
    }

    /**
     * date: 20/9/18
     * author: chenli
     * description: 买开仓处理逻辑
     */
    private void buyOpenPosition() {
        if (mBinding.bidPrice11.getText() != null && mBinding.volume.getText() != null) {
            String price = mBinding.bidPrice11.getText().toString();
            String volume = mBinding.volume.getText().toString();
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
                final double priceN = Double.parseDouble(price);
                initDialog(mExchangeId, mInstrumentIdTransaction.split("\\.")[1],
                        ACTION_OPEN_BUY, DIRECTION_BUY, OFFSET_OPEN, volumeN, PRICE_TYPE_LIMIT, priceN);
            } catch (NumberFormatException ex) {
                ToastNotificationUtils.showToast(BaseApplication.getContext(), "价格或手数输入不合法");
            }


        }
    }

    /**
     * date: 20/9/18
     * author: chenli
     * description: 卖开仓处理逻辑
     */
    private void sellOpenPosition() {
        if (mBinding.askPrice11.getText() != null && mBinding.volume.getText() != null) {
            String price = mBinding.askPrice11.getText().toString();
            String volume = mBinding.volume.getText().toString();
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
                final double priceN = Double.parseDouble(price);
                initDialog(mExchangeId, mInstrumentIdTransaction.split("\\.")[1],
                        ACTION_OPEN_SELL, DIRECTION_SELL, OFFSET_OPEN, volumeN, PRICE_TYPE_LIMIT, priceN);
            } catch (NumberFormatException ex) {
                ToastNotificationUtils.showToast(BaseApplication.getContext(), "价格或手数输入不合法");
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
                        mDirection = DIRECTION_BUY_ZN;
                        mIsClosePriceShow = true;
                        mBinding.closeDirection.setText(ACTION_CLOSE_BUY);
                        mBinding.bidPrice1Direction.setText(ACTION_ADD_BUY);
                        mBinding.askPrice1Direction.setText(ACTION_LOCK);
                        refreshCloseBidPrice();
                        defaultClosePosition(v);
                        break;
                    case R.id.ask_position:
                        mDirection = DIRECTION_SELL_ZN;
                        mIsClosePriceShow = true;
                        mBinding.closeDirection.setText(ACTION_CLOSE_SELL);
                        mBinding.bidPrice1Direction.setText(ACTION_LOCK);
                        mBinding.askPrice1Direction.setText(ACTION_ADD_SELL);
                        refreshCloseAskPrice();
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
                            mDirection = DIRECTION_BUY_ZN;
                            mIsClosePriceShow = true;
                            mBinding.closeDirection.setText(ACTION_CLOSE_BUY);
                            refreshCloseBidPrice();
                            defaultClosePosition(v);
                            break;
                        case R.id.ask_position:
                            mDirection = DIRECTION_SELL_ZN;
                            mIsClosePriceShow = true;
                            mBinding.closeDirection.setText(ACTION_CLOSE_SELL);
                            refreshCloseAskPrice();
                            defaultClosePosition(v);
                            break;
                        default:
                            break;
                    }
                    return true;
                }
            });
            popupCombine.show();
        } else {
            ToastNotificationUtils.showToast(BaseApplication.getContext(), "您还没有此合约持仓～");
        }
    }

    //刷新平多仓价
    private void refreshCloseBidPrice() {
        QuoteEntity quoteEntity = sDataManager.getRtnData().getQuotes().get(mInstrumentId);
        if (quoteEntity == null) return;
        switch (sDataManager.PRICE_TYPE) {
            case QUEUED_PRICE:
                String ask_price1 = LatestFileManager.saveScaleByPtick(quoteEntity.getAsk_price1(),
                        quoteEntity.getInstrument_id());
                mBinding.closePrice.setText(ask_price1);
                break;
            case COUNTERPARTY_PRICE:
                String bid_price1_1 = LatestFileManager.saveScaleByPtick(quoteEntity.getBid_price1(),
                        quoteEntity.getInstrument_id());
                mBinding.closePrice.setText(bid_price1_1);
                break;
            case MARKET_PRICE:
                String lower_limit = LatestFileManager.saveScaleByPtick(quoteEntity.getLower_limit(),
                        quoteEntity.getInstrument_id());
                mBinding.closePrice.setText(lower_limit);
                break;
            case LATEST_PRICE:
                String last_price = LatestFileManager.saveScaleByPtick(quoteEntity.getLast_price(),
                        quoteEntity.getInstrument_id());
                mBinding.closePrice.setText(last_price);
                break;
            default:
                break;
        }

    }

    //刷新平空仓价
    private void refreshCloseAskPrice() {
        QuoteEntity quoteEntity = sDataManager.getRtnData().getQuotes().get(mInstrumentId);
        if (quoteEntity == null) return;
        switch (sDataManager.PRICE_TYPE) {
            case QUEUED_PRICE:
                String bid_price1 = LatestFileManager.saveScaleByPtick(quoteEntity.getBid_price1(),
                        quoteEntity.getInstrument_id());
                mBinding.closePrice.setText(bid_price1);
                break;
            case COUNTERPARTY_PRICE:
                String ask_price1_1 = LatestFileManager.saveScaleByPtick(quoteEntity.getAsk_price1(),
                        quoteEntity.getInstrument_id());
                mBinding.closePrice.setText(ask_price1_1);
                break;
            case MARKET_PRICE:
                String upper_limit = LatestFileManager.saveScaleByPtick(quoteEntity.getUpper_limit(),
                        quoteEntity.getInstrument_id());
                mBinding.closePrice.setText(upper_limit);
                break;
            case LATEST_PRICE:
                String last_price = LatestFileManager.saveScaleByPtick(quoteEntity.getLast_price(),
                        quoteEntity.getInstrument_id());
                mBinding.closePrice.setText(last_price);
                break;
            default:
                break;
        }

    }

    /**
     * date: 6/1/18
     * author: chenli
     * description: 默认平仓处理逻辑
     */
    private void defaultClosePosition(View v) {
        if (mBinding.closePrice.getText() != null && mBinding.volume.getText() != null && !"".equals(mDirection)) {
            String price = mBinding.closePrice.getText().toString();
            String volume = mBinding.volume.getText().toString();
            final String direction = DIRECTION_BUY_ZN.equals(mDirection) ? DIRECTION_SELL : DIRECTION_BUY;
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
                final double priceN = Double.parseDouble(price);
                final String instrumentId = mInstrumentIdTransaction.split("\\.")[1];
                SearchEntity searchEntity = LatestFileManager.getSearchEntities().get(mInstrumentIdTransaction);
                if (searchEntity != null && (INE_ZN.equals(searchEntity.getExchangeName())
                        || SHFE_ZN.equals(searchEntity.getExchangeName()))) {
                    UserEntity userEntity = sDataManager.getTradeBean().getUsers().get(sDataManager.USER_ID);
                    if (userEntity == null) return;
                    PositionEntity positionEntity = userEntity.getPositions().get(mInstrumentIdTransaction);
                    if (positionEntity == null) return;

                    int available_long = Integer.parseInt(MathUtils.subtract(positionEntity.getVolume_long(),
                            MathUtils.add(positionEntity.getVolume_long_frozen_his(), positionEntity.getVolume_long_frozen_today())));
                    int available_short = Integer.parseInt(MathUtils.subtract(positionEntity.getVolume_short(),
                            MathUtils.add(positionEntity.getVolume_short_frozen_his(), positionEntity.getVolume_short_frozen_today())));
                    int volume_today = 0;
                    int volume_history = 0;
                    if (DIRECTION_SELL.equals(direction)) {
                        if (volumeN > available_long){
                            initDialog(userEntity, mExchangeId, instrumentId);
                            return;
                        }
                        volume_today = Integer.parseInt(positionEntity.getVolume_long_today());
                        volume_history = Integer.parseInt(positionEntity.getVolume_long_his());
                    } else if (DIRECTION_BUY.equals(direction)) {
                        if (volumeN > available_short){
                            initDialog(userEntity, mExchangeId, instrumentId);
                            return;
                        }
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
                                            initDialog(mExchangeId, instrumentId, OFFSET_CLOSE_TODAY_ZN,
                                                    direction, OFFSET_CLOSE_TODAY, volumeN, PRICE_TYPE_LIMIT, priceN);
                                            break;
                                        case R.id.close_history:
                                            initDialog(mExchangeId, instrumentId, OFFSET_CLOSE_HISTORY_ZN,
                                                    direction, OFFSET_CLOSE, volumeN, PRICE_TYPE_LIMIT, priceN);
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
                            initDialog(mExchangeId, instrumentId, OFFSET_CLOSE_TODAY_ZN, OFFSET_CLOSE_HISTORY_ZN,
                                    direction, OFFSET_CLOSE_TODAY, OFFSET_CLOSE, volume_today, volume_sub,
                                    PRICE_TYPE_LIMIT, priceN);
                        }
                    } else if (volume_today == 0 && volume_history > 0) {
                        initDialog(mExchangeId, instrumentId, OFFSET_CLOSE_HISTORY_ZN, direction,
                                OFFSET_CLOSE, volumeN, PRICE_TYPE_LIMIT, priceN);
                    } else if (volume_today > 0 && volume_history == 0) {
                        initDialog(mExchangeId, instrumentId, OFFSET_CLOSE_TODAY_ZN, direction,
                                OFFSET_CLOSE_TODAY, volumeN, PRICE_TYPE_LIMIT, priceN);
                    }

                } else
                    initDialog(mExchangeId, instrumentId, OFFSET_CLOSE_ZN, direction,
                            OFFSET_CLOSE, volumeN, PRICE_TYPE_LIMIT, priceN);
            } catch (NumberFormatException ex) {
                ToastNotificationUtils.showToast(BaseApplication.getContext(), "价格或手数输入不合法");
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
        if (sDataManager.POSITION_DIRECTION.isEmpty()) update();
    }

    /**
     * date: 7/14/17
     * author: chenli
     * description: 下单弹出框，根据固定宽高值自定义dialog，注意宽高值从dimens.xml文件中得到；用于同时平今昨仓
     */
    private void initDialog(final String exchange_id, final String instrument_id,
                            String direction_title, String direction_title1, final String direction,
                            final String offset, final String offset1, final int volume, final int volume1,
                            final String price_type, final double price) {
        if (mIsShowDialog) {
            mIsRefreshPosition = false;
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
            tv_price.setText(price + "");
            tv_direction.setText(direction_title);
            tv_direction1.setText(direction_title1);
            tv_volume.setText(volume + "");
            tv_volume1.setText(volume1 + "");
            ok.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (BaseApplication.getWebSocketService() != null) {
                        BaseApplication.getWebSocketService().sendReqInsertOrder(exchange_id, instrument_id, direction, offset, volume, price_type, price);
                        BaseApplication.getWebSocketService().sendReqInsertOrder(exchange_id, instrument_id, direction, offset1, volume1, price_type, price);
                    }
                    refreshPosition();
                    dialog.dismiss();
                    mIsRefreshPosition = true;
                }
            });
            cancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    refreshPosition();
                    dialog.dismiss();
                    mIsRefreshPosition = true;
                }
            });
            dialog.show();
        } else {
            if (BaseApplication.getWebSocketService() != null) {
                BaseApplication.getWebSocketService().sendReqInsertOrder(exchange_id, instrument_id, direction, offset, volume, price_type, price);
                BaseApplication.getWebSocketService().sendReqInsertOrder(exchange_id, instrument_id, direction, offset1, volume1, price_type, price);
            }
            refreshPosition();
            mIsRefreshPosition = true;
        }

    }

    /**
     * date: 7/14/17
     * author: chenli
     * description: 下单弹出框，根据固定宽高值自定义dialog，注意宽高值从dimens.xml文件中得到
     */
    private void initDialog(final String exchange_id, final String instrument_id,
                            String direction_title, final String direction, final String offset, final int volume,
                            final String price_type, final double price) {
        if (mIsShowDialog) {
            mIsRefreshPosition = false;
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
            tv_price.setText(price + "");
            tv_direction.setText(direction_title);
            tv_volume.setText(volume + "");
            ok.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (BaseApplication.getWebSocketService() != null)
                        BaseApplication.getWebSocketService().sendReqInsertOrder(exchange_id, instrument_id, direction, offset, volume, price_type, price);
                    refreshPosition();
                    dialog.dismiss();
                    mIsRefreshPosition = true;
                }
            });
            cancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    refreshPosition();
                    dialog.dismiss();
                    mIsRefreshPosition = true;
                }
            });
            dialog.show();
        } else {
            if (BaseApplication.getWebSocketService() != null)
                BaseApplication.getWebSocketService().sendReqInsertOrder(exchange_id, instrument_id, direction, offset, volume, price_type, price);
            refreshPosition();
            mIsRefreshPosition = true;
        }

    }

    /**
     * date: 2019/4/18
     * author: chenli
     * description: 仓位手数不足：撤单下单
     */
    private void initDialog(UserEntity userEntity, final String exchange_id, final String instrument_id) {
        try {
            mIsReClose = true;
            final List<String> orderIds = new ArrayList<>();
            for (OrderEntity orderEntity :
                    userEntity.getOrders().values()) {
                String order_id = orderEntity.getOrder_id();
                String exId = orderEntity.getExchange_id();
                String insId = orderEntity.getInstrument_id();
                String offset = orderEntity.getOffset();
                String status = orderEntity.getStatus();
                if (STATUS_ALIVE.equals(status) && exchange_id.equals(exId) && instrument_id.equals(insId)
                        && (OFFSET_CLOSE.equals(offset) || OFFSET_CLOSE_HISTORY.equals(offset) || OFFSET_CLOSE_TODAY.equals(offset))){
                    orderIds.add(order_id);
                }
            }
            final Dialog dialog = new Dialog(getActivity(), R.style.Theme_Light_Dialog);
            View view = View.inflate(getActivity(), R.layout.view_dialog_cancel_insert_order, null);
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
            TextView ok = view.findViewById(R.id.order_ok);
            TextView cancel = view.findViewById(R.id.order_cancel);
            ok.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    for (String order_id: orderIds) {
                        BaseApplication.getWebSocketService().sendReqCancelOrder(order_id);
                    }
                }
            });
            cancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                }
            });
            if (!dialog.isShowing())dialog.show();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * date: 2019/4/18
     * author: chenli
     * description: 检测平仓手数是否满足
     */
    private boolean detectCloseVolumeEnough(){
        String volume = mBinding.volume.getText().toString();
        final String direction = DIRECTION_BUY_ZN.equals(mDirection) ? DIRECTION_SELL : DIRECTION_BUY;
        final int volumeN = Integer.parseInt(volume);
        UserEntity userEntity = sDataManager.getTradeBean().getUsers().get(sDataManager.USER_ID);
        if (userEntity == null) return false;
        PositionEntity positionEntity = userEntity.getPositions().get(mInstrumentIdTransaction);
        if (positionEntity == null) return false;

        int available_long = Integer.parseInt(MathUtils.subtract(positionEntity.getVolume_long(),
                MathUtils.add(positionEntity.getVolume_long_frozen_his(), positionEntity.getVolume_long_frozen_today())));
        int available_short = Integer.parseInt(MathUtils.subtract(positionEntity.getVolume_short(),
                MathUtils.add(positionEntity.getVolume_short_frozen_his(), positionEntity.getVolume_short_frozen_today())));

        if (DIRECTION_SELL.equals(direction)) {
            if (volumeN <= available_long){
                return true;
            }
        } else if (DIRECTION_BUY.equals(direction)) {
            if (volumeN <= available_short){
                return true;
            }
        }
        return false;
    }

    /**
     * date: 2019/4/9
     * author: chenli
     * description: 下单确认密码
     */
    private void checkPassword(final View view) {
        String date = (String) SPUtils.get(sContext, CommonConstants.CONFIG_LOGIN_DATE, "");
        if (TimeUtils.getNowTime().equals(date)) {
            switch (view.getId()) {
                case R.id.bid_open_position:
                    buyOpenPosition();
                    break;
                case R.id.ask_open_position:
                    sellOpenPosition();
                    break;
                case R.id.close_position:
                    switch (mBinding.closePrice.getText().toString()) {
                        case STATUS_LOCK:
                            lockClosePosition(view);
                            break;
                        case STATUS_FIRST_OPEN_FIRST_CLOSE:
                            firstClosePosition(view);
                            break;
                        default:
                            defaultClosePosition(view);
                            break;
                    }
                    break;
                default:
                    break;
            }
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
                    String passwordLocal = (String) SPUtils.get(sContext, CommonConstants.CONFIG_PASSWORD, "");
                    if (passwordLocal.equals(password)) {
                        dialog.dismiss();
                        SPUtils.putAndApply(sContext, CONFIG_LOGIN_DATE, TimeUtils.getNowTime());

                        switch (view.getId()) {
                            case R.id.bid_open_position:
                                buyOpenPosition();
                                break;
                            case R.id.ask_open_position:
                                sellOpenPosition();
                                break;
                            case R.id.close_position:
                                switch (mBinding.closePrice.getText().toString()) {
                                    case STATUS_LOCK:
                                        lockClosePosition(view);
                                        break;
                                    case STATUS_FIRST_OPEN_FIRST_CLOSE:
                                        firstClosePosition(view);
                                        break;
                                    default:
                                        defaultClosePosition(view);
                                        break;
                                }
                                break;
                            default:
                                break;
                        }
                    } else ToastNotificationUtils.showToast(sContext, "密码输入错误");
                }
            });
            dialog.setContentView(viewDialog);
            dialog.show();
        }
    }

    /**
     * date: 2019/4/15
     * author: chenli
     * description: 软键盘弹出框
     */
    private void popupKeyboardDialog(final EditText mEditText, final int idKeyboard) {
        final Dialog dialog = new Dialog(getActivity(), R.style.Theme_Light_Dialog);
        ViewDialogKeyboardBinding binding = DataBindingUtil.inflate(LayoutInflater.from(getContext()), R.layout. view_dialog_keyboard, null, false);
        Window dialogWindow = dialog.getWindow();
        if (dialogWindow != null) {
            dialogWindow.getDecorView().setPadding(0, 0, 0, 0);
            WindowManager.LayoutParams lp = dialogWindow.getAttributes();
            dialogWindow.setGravity(Gravity.TOP);
            lp.width = WindowManager.LayoutParams.MATCH_PARENT;
            int height = getRootViewHeight() - getToolBarHeight() - DensityUtils.dp2px(sContext, 130);
            int klineViewHeight = (int) (height / 2.5 * 1.5);
            lp.height = getToolBarHeight() + DensityUtils.dp2px(sContext, 90) + klineViewHeight;
            dialogWindow.setAttributes(lp);
        }
        Keyboard mKeyboard = new Keyboard(getActivity(), idKeyboard);
        KeyboardView keyboardView = binding.getRoot().findViewById(R.id.keyboard);
        keyboardView.setKeyboard(mKeyboard);
        keyboardView.setEnabled(true);
        keyboardView.setPreviewEnabled(false);
        keyboardView.setOnKeyboardActionListener(new KeyboardView.OnKeyboardActionListener() {
            boolean mIsInit = true;

            @Override
            public void onPress(int primaryCode) {

            }

            @Override
            public void onRelease(int primaryCode) {

            }

            @Override
            public void onKey(int primaryCode, int[] keyCodes) {
                if (mEditText != null) {
                    Editable editable = mEditText.getText();
                    String text = mEditText.getText().toString();
                    int start = mEditText.length();
                    if (primaryCode == Keyboard.KEYCODE_DELETE) {
                        if (editable.length() > 0) {
                            if (!QUEUED_PRICE.equals(text) && !COUNTERPARTY_PRICE.equals(text) && !MARKET_PRICE.equals(text) && !LATEST_PRICE.equals(text)) {
                                editable.delete(start - 1, start);
                            } else {
                                editable.clear();
                            }
                        }
                    } else if (primaryCode == mEditText.getContext().getResources().getInteger(R.integer.keycode_empty_text)) {
                        editable.clear();
                    } else if (primaryCode == Keyboard.KEYCODE_DONE) {
                        dialog.dismiss();
                    } else if (primaryCode == mEditText.getContext().getResources().getInteger(R.integer.keycode_line_up_price)) {
                        editable.clear();
                        editable.insert(0, QUEUED_PRICE);
                    } else if (primaryCode == mEditText.getContext().getResources().getInteger(R.integer.keycode_opponent_price)) {
                        editable.clear();
                        editable.insert(0, COUNTERPARTY_PRICE);
                    } else if (primaryCode == mEditText.getContext().getResources().getInteger(R.integer.keycode_last_price)) {
                        editable.clear();
                        editable.insert(0, LATEST_PRICE);
                    } else if (primaryCode == mEditText.getContext().getResources().getInteger(R.integer.keycode_market_price)) {
                        editable.clear();
                        editable.insert(0, MARKET_PRICE);
                    } else if (primaryCode == mEditText.getContext().getResources().getInteger(R.integer.keycode_add)) {
                        QuoteEntity quoteEntity = DataManager.getInstance().getRtnData().getQuotes().get(mInstrumentId);
                        switch (text) {
                            case QUEUED_PRICE:
                                if (quoteEntity != null) {
                                    String ask_price1 = LatestFileManager.saveScaleByPtick(quoteEntity.getAsk_price1(), mInstrumentId);
                                    String bid_price1 = LatestFileManager.saveScaleByPtick(quoteEntity.getBid_price1(), mInstrumentId);
                                    editable.clear();
                                    editable.insert(0, MathUtils.subtract(bid_price1, ask_price1).contains("-") ? bid_price1 : ask_price1);
                                }
                                break;
                            case COUNTERPARTY_PRICE:
                                if (quoteEntity != null) {
                                    String ask_price1 = LatestFileManager.saveScaleByPtick(quoteEntity.getAsk_price1(), mInstrumentId);
                                    String bid_price1 = LatestFileManager.saveScaleByPtick(quoteEntity.getBid_price1(), mInstrumentId);
                                    editable.clear();
                                    editable.insert(0, MathUtils.subtract(bid_price1, ask_price1).contains("-") ? ask_price1 : bid_price1);
                                }
                                break;
                            case LATEST_PRICE:
                                if (quoteEntity != null) {
                                    String last_price = LatestFileManager.saveScaleByPtick(quoteEntity.getLast_price(), mInstrumentId);
                                    editable.clear();
                                    editable.insert(0, last_price);
                                }
                                break;
                            case MARKET_PRICE:
                                if (quoteEntity != null) {
                                    String last_price = LatestFileManager.saveScaleByPtick(quoteEntity.getLast_price(), mInstrumentId);
                                    editable.clear();
                                    editable.insert(0, last_price);
                                }
                                break;
                            default:
                                String data;
                                if (idKeyboard == R.xml.future_price) {
                                    SearchEntity searchEntity = LatestFileManager.getSearchEntities().get(mInstrumentId);
                                    String price_tick = searchEntity == null ? "0" : searchEntity.getpTick();
                                    data = MathUtils.add(editable.toString(), price_tick);
                                } else {
                                    data = MathUtils.add(editable.toString(), "1");
                                }
                                editable.clear();
                                editable.insert(0, data);
                                break;
                        }
                    } else if (primaryCode == mEditText.getContext().getResources().getInteger(R.integer.keycode_sub)) {
                        QuoteEntity quoteEntity = DataManager.getInstance().getRtnData().getQuotes().get(mInstrumentId);
                        switch (text) {
                            case QUEUED_PRICE:
                                if (quoteEntity != null) {
                                    String ask_price1 = LatestFileManager.saveScaleByPtick(quoteEntity.getAsk_price1(), mInstrumentId);
                                    String bid_price1 = LatestFileManager.saveScaleByPtick(quoteEntity.getBid_price1(), mInstrumentId);
                                    editable.clear();
                                    editable.insert(0, MathUtils.subtract(bid_price1, ask_price1).contains("-") ? bid_price1 : ask_price1);
                                }
                                break;
                            case COUNTERPARTY_PRICE:
                                if (quoteEntity != null) {
                                    String ask_price1 = LatestFileManager.saveScaleByPtick(quoteEntity.getAsk_price1(), mInstrumentId);
                                    String bid_price1 = LatestFileManager.saveScaleByPtick(quoteEntity.getBid_price1(), mInstrumentId);
                                    editable.clear();
                                    editable.insert(0, MathUtils.subtract(bid_price1, ask_price1).contains("-") ? ask_price1 : bid_price1);
                                }
                                break;
                            case LATEST_PRICE:
                                if (quoteEntity != null) {
                                    String last_price = LatestFileManager.saveScaleByPtick(quoteEntity.getLast_price(), mInstrumentId);
                                    editable.clear();
                                    editable.insert(0, last_price);
                                }
                                break;
                            case MARKET_PRICE:
                                if (quoteEntity != null) {
                                    String last_price = LatestFileManager.saveScaleByPtick(quoteEntity.getLast_price(), mInstrumentId);
                                    editable.clear();
                                    editable.insert(0, last_price);
                                }
                                break;
                            default:
                                String data;
                                if (idKeyboard == R.xml.future_price) {
                                    //添加负号功能
                                    if (mEditText.getSelectionStart() == 0 && mEditText.getSelectionEnd() == 0) {
                                        editable.insert(0, "-");
                                        break;
                                    } else {
                                        SearchEntity searchEntity = LatestFileManager.getSearchEntities().get(mInstrumentId);
                                        String price_tick = searchEntity == null ? "0" : searchEntity.getpTick();
                                        data = MathUtils.subtract(editable.toString(), price_tick);
                                    }
                                } else {
                                    data = MathUtils.subtract(editable.toString(), "1");
                                }
                                editable.clear();
                                editable.insert(0, data);
                                break;
                        }
                    } else {
                        String insertStr = Character.toString((char) primaryCode);
                        String str = editable.toString();
                        if (QUEUED_PRICE.equals(text) || COUNTERPARTY_PRICE.equals(text) || MARKET_PRICE.equals(text) || LATEST_PRICE.equals(text) || mIsInit) {
                            //添加负号功能
                            if ("-".equals(str)) {
                                if ((!".".equals(insertStr)) || (".".equals(insertStr) && !text.contains(".")))
                                    editable.insert(start, insertStr);
                            } else {
                                editable.clear();
                                editable.insert(0, insertStr);
                            }
                            mIsInit = false;
                        } else if ((!".".equals(insertStr)) || (".".equals(insertStr) && !text.contains(".")))
                            editable.insert(start, insertStr);
                    }
                }
            }

            @Override
            public void onText(CharSequence text) {

            }

            @Override
            public void swipeLeft() {

            }

            @Override
            public void swipeRight() {

            }

            @Override
            public void swipeDown() {

            }

            @Override
            public void swipeUp() {

            }
        });
        dialog.setContentView(binding.getRoot());
        QuoteEntity quoteEntity = sDataManager.getRtnData().getQuotes().get(mInstrumentId);
        if (quoteEntity != null){
            if (mInstrumentId.contains("&") && mInstrumentId.contains(" ")){
                quoteEntity = CloneUtils.clone(quoteEntity);
                quoteEntity = LatestFileManager.calculateCombineQuotePart(quoteEntity);
            }
            binding.setQuote(quoteEntity);
        }
        SearchEntity searchEntity = LatestFileManager.getSearchEntities().get(mInstrumentIdTransaction);
        if (searchEntity != null) binding.minPrice.setText(searchEntity.getpTick());

        if (!dialog.isShowing()) dialog.show();
    }

    /**
     * date: 2019/4/17
     * author: chenli
     * description: 获取根视图高度px
     */
    private int getRootViewHeight() {
        Display display = getActivity().getWindowManager().getDefaultDisplay();
        DisplayMetrics outMetrics = new DisplayMetrics();
        display.getMetrics(outMetrics);

        float dpHeight = outMetrics.heightPixels - getStatusBarHeight();
        return (int) dpHeight;
    }

    /**
     * date: 2019/4/17
     * author: chenli
     * description: 获取statusBar高度px
     */
    private int getStatusBarHeight() {
        Rect rectangle = new Rect();
        Window window = getActivity().getWindow();
        window.getDecorView().getWindowVisibleDisplayFrame(rectangle);
        int statusBarHeight = rectangle.top;
        return statusBarHeight;
    }

    /**
     * date: 2019/4/17
     * author: chenli
     * description: 获取toolbar高度px
     */
    private int getToolBarHeight() {
        TypedValue tv = new TypedValue();
        if (getActivity().getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true)) {
            return TypedValue.complexToDimensionPixelSize(tv.data, getResources().getDisplayMetrics());
        }
        return DensityUtils.dp2px(sContext, 56);
    }


}

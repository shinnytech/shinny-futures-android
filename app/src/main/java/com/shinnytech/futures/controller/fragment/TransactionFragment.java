package com.shinnytech.futures.controller.fragment;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import androidx.databinding.DataBindingUtil;

import android.content.Intent;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.appcompat.widget.PopupMenu;
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
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import com.shinnytech.futures.R;
import com.shinnytech.futures.amplitude.api.Amplitude;
import com.shinnytech.futures.application.BaseApplication;
import com.shinnytech.futures.constants.CommonConstants;
import com.shinnytech.futures.constants.SettingConstants;
import com.shinnytech.futures.controller.activity.ConditionOrderActivity;
import com.shinnytech.futures.controller.activity.MainActivity;
import com.shinnytech.futures.databinding.FragmentTransactionBinding;
import com.shinnytech.futures.model.bean.accountinfobean.AccountEntity;
import com.shinnytech.futures.model.bean.accountinfobean.OrderEntity;
import com.shinnytech.futures.model.bean.accountinfobean.PositionEntity;
import com.shinnytech.futures.model.bean.accountinfobean.UserEntity;
import com.shinnytech.futures.model.bean.eventbusbean.SwitchInsEvent;
import com.shinnytech.futures.model.bean.eventbusbean.InsertOrderEvent;
import com.shinnytech.futures.model.bean.futureinfobean.QuoteEntity;
import com.shinnytech.futures.model.bean.searchinfobean.SearchEntity;
import com.shinnytech.futures.model.engine.DataManager;
import com.shinnytech.futures.model.engine.LatestFileManager;
import com.shinnytech.futures.utils.CloneUtils;
import com.shinnytech.futures.utils.LogUtils;
import com.shinnytech.futures.utils.MathUtils;
import com.shinnytech.futures.utils.SPUtils;
import com.shinnytech.futures.utils.ScreenUtils;
import com.shinnytech.futures.utils.TDUtils;
import com.shinnytech.futures.utils.TimeUtils;
import com.shinnytech.futures.utils.ToastUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import static com.shinnytech.futures.constants.TradeConstants.ACTION_ADD_BUY;
import static com.shinnytech.futures.constants.TradeConstants.ACTION_ADD_SELL;
import static com.shinnytech.futures.constants.TradeConstants.ACTION_CLOSE_BUY;
import static com.shinnytech.futures.constants.TradeConstants.ACTION_CLOSE_SELL;
import static com.shinnytech.futures.constants.TradeConstants.ACTION_LOCK;
import static com.shinnytech.futures.constants.TradeConstants.ACTION_OPEN_BUY;
import static com.shinnytech.futures.constants.TradeConstants.ACTION_OPEN_SELL;
import static com.shinnytech.futures.constants.AmpConstants.AMP_CONDITION_BUTTON;
import static com.shinnytech.futures.constants.AmpConstants.AMP_EVENT_PRICE_KEY;
import static com.shinnytech.futures.constants.AmpConstants.AMP_EVENT_PRICE_KEY_VALUE_0;
import static com.shinnytech.futures.constants.AmpConstants.AMP_EVENT_PRICE_KEY_VALUE_1;
import static com.shinnytech.futures.constants.AmpConstants.AMP_EVENT_PRICE_KEY_VALUE_2;
import static com.shinnytech.futures.constants.AmpConstants.AMP_EVENT_PRICE_KEY_VALUE_3;
import static com.shinnytech.futures.constants.AmpConstants.AMP_EVENT_PRICE_KEY_VALUE_4;
import static com.shinnytech.futures.constants.AmpConstants.AMP_EVENT_PRICE_KEY_VALUE_5;
import static com.shinnytech.futures.constants.AmpConstants.AMP_EVENT_PRICE_KEY_VALUE_6;
import static com.shinnytech.futures.constants.AmpConstants.AMP_EVENT_PRICE_KEY_VALUE_7;
import static com.shinnytech.futures.constants.AmpConstants.AMP_EVENT_PRICE_KEY_VALUE_8;
import static com.shinnytech.futures.constants.AmpConstants.AMP_EVENT_PRICE_KEY_VALUE_9;
import static com.shinnytech.futures.constants.AmpConstants.AMP_EVENT_PRICE_KEY_VALUE_DEL;
import static com.shinnytech.futures.constants.AmpConstants.AMP_EVENT_PRICE_KEY_VALUE_LAST;
import static com.shinnytech.futures.constants.AmpConstants.AMP_EVENT_PRICE_KEY_VALUE_MARKET;
import static com.shinnytech.futures.constants.AmpConstants.AMP_EVENT_PRICE_KEY_VALUE_MINUS;
import static com.shinnytech.futures.constants.AmpConstants.AMP_EVENT_PRICE_KEY_VALUE_OPPONENT;
import static com.shinnytech.futures.constants.AmpConstants.AMP_EVENT_PRICE_KEY_VALUE_PLUS;
import static com.shinnytech.futures.constants.AmpConstants.AMP_EVENT_PRICE_KEY_VALUE_POINT;
import static com.shinnytech.futures.constants.AmpConstants.AMP_EVENT_PRICE_KEY_VALUE_QUEUED;
import static com.shinnytech.futures.constants.AmpConstants.AMP_EVENT_PRICE_TYPE_VALUE_LAST;
import static com.shinnytech.futures.constants.AmpConstants.AMP_EVENT_PRICE_TYPE_VALUE_MARKET;
import static com.shinnytech.futures.constants.AmpConstants.AMP_EVENT_PRICE_TYPE_VALUE_NUMBER;
import static com.shinnytech.futures.constants.AmpConstants.AMP_EVENT_PRICE_TYPE_VALUE_OPPONENT;
import static com.shinnytech.futures.constants.AmpConstants.AMP_EVENT_PRICE_TYPE_VALUE_QUEUED;
import static com.shinnytech.futures.constants.AmpConstants.AMP_EVENT_VOLUME_KEY;
import static com.shinnytech.futures.constants.AmpConstants.AMP_EVENT_VOLUME_KEY_VALUE_0;
import static com.shinnytech.futures.constants.AmpConstants.AMP_EVENT_VOLUME_KEY_VALUE_1;
import static com.shinnytech.futures.constants.AmpConstants.AMP_EVENT_VOLUME_KEY_VALUE_2;
import static com.shinnytech.futures.constants.AmpConstants.AMP_EVENT_VOLUME_KEY_VALUE_3;
import static com.shinnytech.futures.constants.AmpConstants.AMP_EVENT_VOLUME_KEY_VALUE_4;
import static com.shinnytech.futures.constants.AmpConstants.AMP_EVENT_VOLUME_KEY_VALUE_5;
import static com.shinnytech.futures.constants.AmpConstants.AMP_EVENT_VOLUME_KEY_VALUE_6;
import static com.shinnytech.futures.constants.AmpConstants.AMP_EVENT_VOLUME_KEY_VALUE_7;
import static com.shinnytech.futures.constants.AmpConstants.AMP_EVENT_VOLUME_KEY_VALUE_8;
import static com.shinnytech.futures.constants.AmpConstants.AMP_EVENT_VOLUME_KEY_VALUE_9;
import static com.shinnytech.futures.constants.AmpConstants.AMP_EVENT_VOLUME_KEY_VALUE_CLEAR;
import static com.shinnytech.futures.constants.AmpConstants.AMP_EVENT_VOLUME_KEY_VALUE_DEL;
import static com.shinnytech.futures.constants.AmpConstants.AMP_EVENT_VOLUME_KEY_VALUE_MINUS;
import static com.shinnytech.futures.constants.AmpConstants.AMP_EVENT_VOLUME_KEY_VALUE_PLUS;
import static com.shinnytech.futures.constants.AmpConstants.AMP_PRICE_KEY;
import static com.shinnytech.futures.constants.AmpConstants.AMP_VOLUME_KEY;
import static com.shinnytech.futures.constants.SettingConstants.CONFIG_LOGIN_DATE;
import static com.shinnytech.futures.constants.SettingConstants.CONFIG_PASSWORD;
import static com.shinnytech.futures.constants.TradeConstants.DIRECTION_BOTH_ZN;
import static com.shinnytech.futures.constants.TradeConstants.DIRECTION_BUY;
import static com.shinnytech.futures.constants.TradeConstants.DIRECTION_BUY_ZN;
import static com.shinnytech.futures.constants.TradeConstants.DIRECTION_SELL;
import static com.shinnytech.futures.constants.TradeConstants.DIRECTION_SELL_ZN;
import static com.shinnytech.futures.constants.MarketConstants.INE_ZN;
import static com.shinnytech.futures.constants.CommonConstants.INS_BETWEEN_ACTIVITY;
import static com.shinnytech.futures.constants.TradeConstants.LATEST_PRICE;
import static com.shinnytech.futures.constants.TradeConstants.MARKET_PRICE;
import static com.shinnytech.futures.constants.TradeConstants.OFFSET_CLOSE;
import static com.shinnytech.futures.constants.TradeConstants.OFFSET_CLOSE_HISTORY;
import static com.shinnytech.futures.constants.TradeConstants.OFFSET_CLOSE_HISTORY_ZN;
import static com.shinnytech.futures.constants.TradeConstants.OFFSET_CLOSE_TODAY;
import static com.shinnytech.futures.constants.TradeConstants.OFFSET_CLOSE_TODAY_ZN;
import static com.shinnytech.futures.constants.TradeConstants.OFFSET_CLOSE_ZN;
import static com.shinnytech.futures.constants.TradeConstants.OFFSET_OPEN;
import static com.shinnytech.futures.constants.TradeConstants.OPPONENT_PRICE;
import static com.shinnytech.futures.constants.TradeConstants.PRICE_TYPE_LIMIT;
import static com.shinnytech.futures.constants.TradeConstants.QUEUED_PRICE;
import static com.shinnytech.futures.constants.MarketConstants.SHFE_ZN;
import static com.shinnytech.futures.constants.TradeConstants.STATUS_ALIVE;
import static com.shinnytech.futures.constants.TradeConstants.STATUS_FIRST_OPEN_FIRST_CLOSE;
import static com.shinnytech.futures.constants.TradeConstants.STATUS_LOCK;
import static com.shinnytech.futures.constants.TradeConstants.USER_PRICE;

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
    private Dialog mPriceDialog;
    private Dialog mVolumeDialog;
    /**
     * date: 2019/6/17
     * description: 软键盘是否刚隐藏
     */
    private boolean mIsKeyBoardJustHide = false;
    private View mView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_transaction, container, false);
        initData();
        initEvent();
        mView = mBinding.getRoot();
        return mView;
    }

    private void initData() {
        mIsShowDialog = (boolean) SPUtils.get(sContext, SettingConstants.CONFIG_INSERT_ORDER_CONFIRM, true);
        mPriceDialog = initKeyboardDialog(mBinding.price, R.xml.future_price);
        mVolumeDialog = initKeyboardDialog(mBinding.volume, R.xml.future_volume);
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
                            InputMethodManager imm = (InputMethodManager) getActivity().
                                    getSystemService(Context.INPUT_METHOD_SERVICE);
                            imm.hideSoftInputFromWindow(view1.getWindowToken(), 0);
                        }
                    }
                    mBinding.price.setSelection(0, mBinding.price.getText().length());
                    if (!mPriceDialog.isShowing()) mPriceDialog.show();
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
                            InputMethodManager imm = (InputMethodManager) getActivity().
                                    getSystemService(Context.INPUT_METHOD_SERVICE);
                            imm.hideSoftInputFromWindow(view1.getWindowToken(), 0);
                        }
                    }
                    mBinding.volume.setSelection(0, mBinding.volume.getText().length());
                    if (!mVolumeDialog.isShowing()) mVolumeDialog.show();
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
                        case OPPONENT_PRICE:
                            String ask_price1_1 = LatestFileManager.saveScaleByPtick(quoteEntity.getAsk_price1(),
                                    quoteEntity.getInstrument_id());
                            String bid_price1_1 = LatestFileManager.saveScaleByPtick(quoteEntity.getBid_price1(),
                                    quoteEntity.getInstrument_id());
                            sDataManager.PRICE_TYPE = OPPONENT_PRICE;
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

        mBinding.backAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mIsKeyBoardJustHide) {
                    mIsKeyBoardJustHide = false;
                    return;
                }
                MainActivity mainActivity = (MainActivity) getActivity();
                mainActivity.getmMainActivityPresenter().linkToAccount();
            }
        });

        mBinding.toConditionOrder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity mainActivity = (MainActivity) getActivity();
                if (!mainActivity.checkConditionResponsibility())return;

                Context sContext = BaseApplication.getContext();
                String name = sDataManager.USER_ID;
                String password = (String) SPUtils.get(sContext, CONFIG_PASSWORD, "");
                boolean isVisitor = TDUtils.isVisitor(name, password);
                if (isVisitor){
                    ToastUtils.showToast(sContext, "游客模式暂不支持条件单/止盈止损");
                    return;
                }

                Amplitude.getInstance().logEventWrap(AMP_CONDITION_BUTTON, new JSONObject());
                Intent intent = new Intent(getActivity(), ConditionOrderActivity.class);
                intent.putExtra(INS_BETWEEN_ACTIVITY, mInstrumentId);
                startActivity(intent);
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        //不用show()防止重复上报
        refreshMD();
        refreshTD();
        initPosition();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void show() {
        refreshMD();
        refreshTD();
        initPosition();
    }

    @Override
    public void leave() {
    }

    @Override
    public void refreshMD() {
        if (mView == null)return;
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
            case OPPONENT_PRICE:
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

    @Override
    public void refreshTD() {
        if (mView == null)return;
        UserEntity userEntity = sDataManager.getTradeBean().getUsers().get(sDataManager.USER_ID);
        if (userEntity == null) return;
        AccountEntity accountEntity = userEntity.getAccounts().get("CNY");
        if (accountEntity == null) return;
        mBinding.setAccount(accountEntity);
        QuoteEntity quoteEntity = sDataManager.getRtnData().getQuotes().get(mInstrumentId);
        if (quoteEntity != null) {
            if (mInstrumentId.contains("&") && mInstrumentId.contains(" ")) {
                quoteEntity = CloneUtils.clone(quoteEntity);
                quoteEntity = LatestFileManager.calculateCombineQuotePart(quoteEntity);
            }
            mBinding.setQuote(quoteEntity);
        }

        if (mIsRefreshPosition) refreshPosition();

        //撤单平仓
        if (mIsReClose && detectCloseVolumeEnough()) {
            mIsShowDialog = false;
            mIsReClose = false;
            defaultClosePosition(mBinding.closePosition);
            mIsShowDialog = (boolean) SPUtils.get(sContext, SettingConstants.CONFIG_INSERT_ORDER_CONFIRM, true);
        }
    }

    /**
     * date: 7/9/17
     * author: chenli
     * description: 根据持仓状态，初始价格手数信息，据此设置下单版的显示状态
     */
    private void initPosition() {
        try {
            if (USER_PRICE.equals(sDataManager.PRICE_TYPE))
                mBinding.price.setText(OPPONENT_PRICE);
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
                mBinding.closeDirection.setText(OFFSET_CLOSE_ZN);
            } else {
                int volume_long = Integer.parseInt(positionEntity.getVolume_long());
                int volume_short = Integer.parseInt(positionEntity.getVolume_short());
                if (volume_long != 0 && volume_short == 0) {
                    this.mDirection = DIRECTION_BUY_ZN;
                    mBinding.volume.setText(volume_long + "");
                    mIsClosePriceShow = true;
                    mBinding.bidPrice1Direction.setText(ACTION_ADD_BUY);
                    mBinding.askPrice1Direction.setText(ACTION_LOCK);
                    mBinding.closePrice.setText(mBinding.askPrice11.getText().toString());
                    mBinding.closeDirection.setText(ACTION_CLOSE_BUY);
                } else if (volume_long == 0 && volume_short != 0) {
                    this.mDirection = DIRECTION_SELL_ZN;
                    mBinding.volume.setText(volume_short + "");
                    mIsClosePriceShow = true;
                    mBinding.bidPrice1Direction.setText(ACTION_LOCK);
                    mBinding.askPrice1Direction.setText(ACTION_ADD_SELL);
                    mBinding.closePrice.setText(mBinding.bidPrice11.getText().toString());
                    mBinding.closeDirection.setText(ACTION_CLOSE_SELL);
                } else if (volume_long != 0 && volume_short != 0) {
                    if (sDataManager.POSITION_DIRECTION.isEmpty()) {
                        this.mDirection = DIRECTION_BOTH_ZN;
                        mBinding.volume.setText("1");
                        mIsClosePriceShow = false;
                        mBinding.bidPrice1Direction.setText(ACTION_OPEN_BUY);
                        mBinding.askPrice1Direction.setText(ACTION_OPEN_SELL);
                        mBinding.closePrice.setText(STATUS_LOCK);
                        mBinding.closeDirection.setText(OFFSET_CLOSE_ZN);
                    } else {
                        switch (sDataManager.POSITION_DIRECTION) {
                            case DIRECTION_BUY_ZN:
                                this.mDirection = DIRECTION_BUY_ZN;
                                mBinding.volume.setText(volume_long + "");
                                mIsClosePriceShow = true;
                                mBinding.bidPrice1Direction.setText(ACTION_ADD_BUY);
                                mBinding.askPrice1Direction.setText(ACTION_LOCK);
                                mBinding.closePrice.setText(mBinding.askPrice11.getText().toString());
                                mBinding.closeDirection.setText(ACTION_CLOSE_BUY);
                                break;
                            case DIRECTION_SELL_ZN:
                                this.mDirection = DIRECTION_SELL_ZN;
                                mBinding.volume.setText(volume_short + "");
                                mIsClosePriceShow = true;
                                mBinding.bidPrice1Direction.setText(ACTION_LOCK);
                                mBinding.askPrice1Direction.setText(ACTION_ADD_SELL);
                                mBinding.closePrice.setText(mBinding.bidPrice11.getText().toString());
                                mBinding.closeDirection.setText(ACTION_CLOSE_SELL);
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
                    mBinding.closeDirection.setText(OFFSET_CLOSE_ZN);
                }
            }
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
                mBinding.closeDirection.setText(OFFSET_CLOSE_ZN);
            } else {
                int volume_long = Integer.parseInt(positionEntity.getVolume_long());
                int volume_short = Integer.parseInt(positionEntity.getVolume_short());
                if (volume_long != 0 && volume_short == 0) {
                    this.mDirection = DIRECTION_BUY_ZN;
                    mIsClosePriceShow = true;
                    mBinding.bidPrice1Direction.setText(ACTION_ADD_BUY);
                    mBinding.askPrice1Direction.setText(ACTION_LOCK);
                    mBinding.closePrice.setText(mBinding.askPrice11.getText().toString());
                    mBinding.closeDirection.setText(ACTION_CLOSE_BUY);
                } else if (volume_long == 0 && volume_short != 0) {
                    this.mDirection = DIRECTION_SELL_ZN;
                    mIsClosePriceShow = true;
                    mBinding.bidPrice1Direction.setText(ACTION_LOCK);
                    mBinding.askPrice1Direction.setText(ACTION_ADD_SELL);
                    mBinding.closePrice.setText(mBinding.bidPrice11.getText().toString());
                    mBinding.closeDirection.setText(ACTION_CLOSE_SELL);
                } else if (volume_long != 0 && volume_short != 0) {
                    this.mDirection = DIRECTION_BOTH_ZN;
                    mIsClosePriceShow = false;
                    mBinding.bidPrice1Direction.setText(ACTION_OPEN_BUY);
                    mBinding.askPrice1Direction.setText(ACTION_OPEN_SELL);
                    mBinding.closePrice.setText(STATUS_LOCK);
                    mBinding.closeDirection.setText(OFFSET_CLOSE_ZN);
                } else {
                    this.mDirection = "";
                    mIsClosePriceShow = false;
                    mBinding.bidPrice1Direction.setText(ACTION_OPEN_BUY);
                    mBinding.askPrice1Direction.setText(ACTION_OPEN_SELL);
                    mBinding.closePrice.setText(STATUS_FIRST_OPEN_FIRST_CLOSE);
                    mBinding.closeDirection.setText(OFFSET_CLOSE_ZN);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
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
                ToastUtils.showToast(BaseApplication.getContext(), "价格不能为空");
                return;
            }
            if (".".equals(price)) {
                ToastUtils.showToast(BaseApplication.getContext(), "价格输入不合法");
                return;
            }
            if (volume.length() == 0) {
                ToastUtils.showToast(BaseApplication.getContext(), "手数不能为空");
                return;
            }
            if (volume.length() > 10) {
                ToastUtils.showToast(BaseApplication.getContext(), "手数太大");
                return;
            }
            if ("0".equals(volume)) {
                ToastUtils.showToast(BaseApplication.getContext(), "手数不能为零");
                return;
            }
            try {
                final int volumeN = Integer.parseInt(volume);
                final double priceN = Double.parseDouble(price);
                initDialog(mExchangeId, mInstrumentIdTransaction.split("\\.")[1],
                        ACTION_OPEN_BUY, DIRECTION_BUY, OFFSET_OPEN, volumeN, PRICE_TYPE_LIMIT, priceN, price);
            } catch (NumberFormatException ex) {
                ToastUtils.showToast(BaseApplication.getContext(), "价格或手数输入不合法");
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
                ToastUtils.showToast(BaseApplication.getContext(), "价格不能为空");
                return;
            }
            if (".".equals(price)) {
                ToastUtils.showToast(BaseApplication.getContext(), "价格输入不合法");
                return;
            }
            if (volume.length() == 0) {
                ToastUtils.showToast(BaseApplication.getContext(), "手数不能为空");
                return;
            }
            if (volume.length() > 10) {
                ToastUtils.showToast(BaseApplication.getContext(), "手数太大");
                return;
            }
            if ("0".equals(volume)) {
                ToastUtils.showToast(BaseApplication.getContext(), "手数不能为零");
                return;
            }
            try {
                final int volumeN = Integer.parseInt(volume);
                final double priceN = Double.parseDouble(price);
                initDialog(mExchangeId, mInstrumentIdTransaction.split("\\.")[1],
                        ACTION_OPEN_SELL, DIRECTION_SELL, OFFSET_OPEN, volumeN, PRICE_TYPE_LIMIT, priceN, price);
            } catch (NumberFormatException ex) {
                ToastUtils.showToast(BaseApplication.getContext(), "价格或手数输入不合法");
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
            ToastUtils.showToast(BaseApplication.getContext(), "您还没有此合约持仓～");
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
            case OPPONENT_PRICE:
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
            case USER_PRICE:
                mBinding.closePrice.setText(mBinding.price.getText().toString());
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
            case OPPONENT_PRICE:
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
            case USER_PRICE:
                mBinding.closePrice.setText(mBinding.price.getText().toString());
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
            final String price = mBinding.closePrice.getText().toString();
            LogUtils.e(price, true);
            String volume = mBinding.volume.getText().toString();
            final String direction = DIRECTION_BUY_ZN.equals(mDirection) ? DIRECTION_SELL : DIRECTION_BUY;
            String directionTitle = DIRECTION_BUY_ZN.equals(mDirection) ? ACTION_CLOSE_BUY : ACTION_CLOSE_SELL;
            if (price.length() == 0) {
                ToastUtils.showToast(BaseApplication.getContext(), "价格不能为空");
                return;
            }
            if (".".equals(price)) {
                ToastUtils.showToast(BaseApplication.getContext(), "价格输入不合法");
                return;
            }
            if (volume.length() == 0) {
                ToastUtils.showToast(BaseApplication.getContext(), "手数不能为空");
                return;
            }
            if (volume.length() > 10) {
                ToastUtils.showToast(BaseApplication.getContext(), "手数太大");
                return;
            }
            if ("0".equals(volume)) {
                ToastUtils.showToast(BaseApplication.getContext(), "手数不能为零");
                return;
            }
            try {
                final int volumeN = Integer.parseInt(volume);
                final double priceN = Double.parseDouble(price);
                final String instrumentId = mInstrumentIdTransaction.split("\\.")[1];
                UserEntity userEntity = sDataManager.getTradeBean().getUsers().get(sDataManager.USER_ID);
                if (userEntity == null) return;
                PositionEntity positionEntity = userEntity.getPositions().get(mInstrumentIdTransaction);
                if (positionEntity == null) return;

                SearchEntity searchEntity = LatestFileManager.getSearchEntities().get(mInstrumentIdTransaction);
                if (searchEntity != null && (INE_ZN.equals(searchEntity.getExchangeName())
                        || SHFE_ZN.equals(searchEntity.getExchangeName()))) {

                    int volume_today = 0;
                    int volume_history = 0;

                    if (DIRECTION_SELL.equals(direction)) {
                        volume_today = Integer.parseInt(positionEntity.getVolume_long_today());
                        volume_history = Integer.parseInt(positionEntity.getVolume_long_his());
                    } else if (DIRECTION_BUY.equals(direction)) {
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
                                                    direction, OFFSET_CLOSE_TODAY, volumeN, PRICE_TYPE_LIMIT, priceN, price);
                                            break;
                                        case R.id.close_history:
                                            initDialog(mExchangeId, instrumentId, OFFSET_CLOSE_HISTORY_ZN,
                                                    direction, OFFSET_CLOSE, volumeN, PRICE_TYPE_LIMIT, priceN, price);
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
                                    PRICE_TYPE_LIMIT, priceN, price);
                        }
                    } else if (volume_today == 0 && volume_history > 0) {
                        initDialog(mExchangeId, instrumentId, OFFSET_CLOSE_HISTORY_ZN, direction,
                                OFFSET_CLOSE, volumeN, PRICE_TYPE_LIMIT, priceN, price);
                    } else if (volume_today > 0 && volume_history == 0) {
                        initDialog(mExchangeId, instrumentId, OFFSET_CLOSE_TODAY_ZN, direction,
                                OFFSET_CLOSE_TODAY, volumeN, PRICE_TYPE_LIMIT, priceN, price);
                    }

                } else
                    initDialog(mExchangeId, instrumentId, directionTitle, direction,
                            OFFSET_CLOSE, volumeN, PRICE_TYPE_LIMIT, priceN, price);
            } catch (NumberFormatException ex) {
                ex.printStackTrace();
                LogUtils.e("价格或手数输入不合法", true);
                ToastUtils.showToast(BaseApplication.getContext(), "价格或手数输入不合法");
            }
        }
    }

    /**
     * date: 7/14/17
     * author: chenli
     * description: 下单弹出框，根据固定宽高值自定义dialog，注意宽高值从dimens.xml文件中得到；用于同时平今昨仓
     */
    private void initDialog(final String exchange_id, final String instrument_id,
                            String direction_title, String direction_title1, final String direction,
                            final String offset, final String offset1, final int volume, final int volume1,
                            final String price_type, final double price, String priceS) {
        boolean isVolumeEnough = true;
        List<String> orders = new ArrayList<>();
        if (!OFFSET_OPEN.equals(offset))isVolumeEnough = detectCloseVolumeEnough();
        if (!isVolumeEnough){
            UserEntity userEntity = sDataManager.getTradeBean().getUsers().get(sDataManager.USER_ID);
            orders = getInsOrders(userEntity, exchange_id, instrument_id);
            if (orders.isEmpty())isVolumeEnough = true;
        }
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
                if (isVolumeEnough) lp.height = (int) getActivity().getResources().getDimension(R.dimen.order_dialog_height1);
                else lp.height = (int) getActivity().getResources().getDimension(R.dimen.order_dialog_height1_close);
                dialogWindow.setAttributes(lp);
            }
            dialog.setContentView(view);
            dialog.setCancelable(false);
            TextView tv_cancel_insert = view.findViewById(R.id.cancel_insert);
            if (!isVolumeEnough)tv_cancel_insert.setVisibility(View.VISIBLE);
            TextView tv_instrument_id = view.findViewById(R.id.order_instrument_id);
            TextView tv_price = view.findViewById(R.id.order_price);
            TextView tv_direction = view.findViewById(R.id.order_direction);
            TextView tv_volume = view.findViewById(R.id.order_volume);
            TextView ok = view.findViewById(R.id.order_ok);
            TextView cancel = view.findViewById(R.id.order_cancel);
            TextView tv_comma0 = view.findViewById(R.id.order_comma0);
            TextView tv_comma1 = view.findViewById(R.id.order_comma1);
            tv_comma1.setVisibility(View.VISIBLE);
            TextView tv_comma2 = view.findViewById(R.id.order_comma2);
            tv_comma2.setVisibility(View.VISIBLE);
            TextView tv_direction1 = view.findViewById(R.id.order_direction1);
            tv_direction1.setVisibility(View.VISIBLE);
            TextView tv_volume1 = view.findViewById(R.id.order_volume1);
            tv_volume1.setVisibility(View.VISIBLE);
            String ins = exchange_id + "." + instrument_id;
            String ins_name = ins;
            SearchEntity searchEntity = LatestFileManager.getSearchEntities().get(ins);
            if (searchEntity != null) ins_name = searchEntity.getInstrumentName();
            tv_instrument_id.setText(ins_name);
            tv_price.setText(priceS);
            tv_direction.setText(direction_title);
            tv_direction1.setText(direction_title1);
            tv_volume.setText(volume + "手");
            tv_volume1.setText(volume1 + "手");
            if (DIRECTION_BUY.equals(direction)){
                tv_direction.setTextColor(getResources().getColor(R.color.text_red));
                tv_direction1.setTextColor(getResources().getColor(R.color.text_red));
                tv_comma0.setTextColor(getResources().getColor(R.color.text_red));
                tv_comma2.setTextColor(getResources().getColor(R.color.text_red));
                tv_volume.setTextColor(getResources().getColor(R.color.text_red));
                tv_volume1.setTextColor(getResources().getColor(R.color.text_red));
            }
            if (DIRECTION_SELL.equals(direction)){
                tv_direction.setTextColor(getResources().getColor(R.color.text_green));
                tv_direction1.setTextColor(getResources().getColor(R.color.text_green));
                tv_comma0.setTextColor(getResources().getColor(R.color.text_green));
                tv_comma2.setTextColor(getResources().getColor(R.color.text_green));
                tv_volume.setTextColor(getResources().getColor(R.color.text_green));
                tv_volume1.setTextColor(getResources().getColor(R.color.text_green));
            }
            final boolean finalIsVolumeEnough = isVolumeEnough;
            final List<String> finalOrders = orders;
            ok.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!finalIsVolumeEnough){
                        mIsReClose = true;
                        for (String order_id : finalOrders) {
                            BaseApplication.getmTDWebSocket().sendReqCancelOrder(order_id);
                        }
                    }else {
                        BaseApplication.getmTDWebSocket().sendReqInsertOrder(exchange_id, instrument_id,
                                direction, offset, volume, price_type, price, getAmpPriceType());
                        BaseApplication.getmTDWebSocket().sendReqInsertOrder(exchange_id, instrument_id,
                                direction, offset1, volume1, price_type, price, getAmpPriceType());
                        refreshPosition();
                        mIsRefreshPosition = true;
                    }
                    dialog.dismiss();
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
            if (!isVolumeEnough){
                mIsReClose = true;
                for (String order_id : orders) {
                    BaseApplication.getmTDWebSocket().sendReqCancelOrder(order_id);
                }
            }else {
                BaseApplication.getmTDWebSocket().sendReqInsertOrder(exchange_id, instrument_id,
                        direction, offset, volume, price_type, price, getAmpPriceType());
                BaseApplication.getmTDWebSocket().sendReqInsertOrder(exchange_id, instrument_id,
                        direction, offset1, volume1, price_type, price, getAmpPriceType());
                refreshPosition();
                mIsRefreshPosition = true;
            }
        }

    }

    /**
     * date: 7/14/17
     * author: chenli
     * description: 下单弹出框，根据固定宽高值自定义dialog，注意宽高值从dimens.xml文件中得到
     */
    private void initDialog(final String exchange_id, final String instrument_id,
                            String direction_title, final String direction, final String offset, final int volume,
                            final String price_type, final double price, String priceS) {
        boolean isVolumeEnough = true;
        List<String> orders = new ArrayList<>();
        if (!OFFSET_OPEN.equals(offset))isVolumeEnough = detectCloseVolumeEnough();
        if (!isVolumeEnough){
            UserEntity userEntity = sDataManager.getTradeBean().getUsers().get(sDataManager.USER_ID);
            orders = getInsOrders(userEntity, exchange_id, instrument_id);
            if (orders.isEmpty())isVolumeEnough = true;
        }
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
                if (isVolumeEnough) lp.height = (int) getActivity().getResources().getDimension(R.dimen.order_dialog_height);
                else lp.height = (int) getActivity().getResources().getDimension(R.dimen.order_dialog_height_close);
                dialogWindow.setAttributes(lp);
            }
            dialog.setContentView(view);
            dialog.setCancelable(false);
            TextView tv_cancel_insert = view.findViewById(R.id.cancel_insert);
            if (!isVolumeEnough)tv_cancel_insert.setVisibility(View.VISIBLE);
            TextView tv_instrument_id = view.findViewById(R.id.order_instrument_id);
            TextView tv_price = view.findViewById(R.id.order_price);
            TextView tv_direction = view.findViewById(R.id.order_direction);
            TextView tv_comma0 = view.findViewById(R.id.order_comma0);
            TextView tv_volume = view.findViewById(R.id.order_volume);
            TextView ok = view.findViewById(R.id.order_ok);
            TextView cancel = view.findViewById(R.id.order_cancel);
            String ins = exchange_id + "." + instrument_id;
            String ins_name = ins;
            SearchEntity searchEntity = LatestFileManager.getSearchEntities().get(ins);
            if (searchEntity != null) ins_name = searchEntity.getInstrumentName();
            tv_instrument_id.setText(ins_name);
            tv_price.setText(priceS);
            tv_direction.setText(direction_title);
            tv_volume.setText(volume + "手");
            if (DIRECTION_BUY.equals(direction)){
                tv_direction.setTextColor(getResources().getColor(R.color.text_red));
                tv_comma0.setTextColor(getResources().getColor(R.color.text_red));
                tv_volume.setTextColor(getResources().getColor(R.color.text_red));
            }
            if (DIRECTION_SELL.equals(direction)){
                tv_direction.setTextColor(getResources().getColor(R.color.text_green));
                tv_comma0.setTextColor(getResources().getColor(R.color.text_green));
                tv_volume.setTextColor(getResources().getColor(R.color.text_green));
            }
            final boolean finalIsVolumeEnough = isVolumeEnough;
            final List<String> finalOrders = orders;
            ok.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!finalIsVolumeEnough){
                        mIsReClose = true;
                        for (String order_id : finalOrders) {
                            BaseApplication.getmTDWebSocket().sendReqCancelOrder(order_id);
                        }
                    }else {
                        BaseApplication.getmTDWebSocket().sendReqInsertOrder(exchange_id, instrument_id,
                                direction, offset, volume, price_type, price, getAmpPriceType());
                        refreshPosition();
                        mIsRefreshPosition = true;
                    }
                    dialog.dismiss();
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
            if (!isVolumeEnough){
                mIsReClose = true;
                for (String order_id : orders) {
                    BaseApplication.getmTDWebSocket().sendReqCancelOrder(order_id);
                }
            }else {
                BaseApplication.getmTDWebSocket().sendReqInsertOrder(exchange_id, instrument_id,
                        direction, offset, volume, price_type, price, getAmpPriceType());
                refreshPosition();
                mIsRefreshPosition = true;
            }
        }

    }

    /**
     * date: 2019/6/17
     * author: chenli
     * description: 获取合约对应挂单
     */
    private List<String> getInsOrders(UserEntity userEntity, String exchange_id, String instrument_id) {
        List<String> orderIds = new ArrayList<>();
        if (userEntity == null)return orderIds;
        for (OrderEntity orderEntity :
                userEntity.getOrders().values()) {
            String order_id = orderEntity.getOrder_id();
            String exId = orderEntity.getExchange_id();
            String insId = orderEntity.getInstrument_id();
            String offset = orderEntity.getOffset();
            String status = orderEntity.getStatus();
            if (STATUS_ALIVE.equals(status) && exchange_id.equals(exId) && instrument_id.equals(insId)
                    && (OFFSET_CLOSE.equals(offset) || OFFSET_CLOSE_HISTORY.equals(offset) ||
                    OFFSET_CLOSE_TODAY.equals(offset))) {
                orderIds.add(order_id);
            }
        }
        return orderIds;
    }

    /**
     * date: 2019/4/18
     * author: chenli
     * description: 检测平仓手数是否满足
     */
    private boolean detectCloseVolumeEnough() {
        String volume = mBinding.volume.getText().toString();
        final String direction = DIRECTION_BUY_ZN.equals(mDirection) ? DIRECTION_SELL : DIRECTION_BUY;
        final int volumeN = Integer.parseInt(volume);
        UserEntity userEntity = sDataManager.getTradeBean().getUsers().get(sDataManager.USER_ID);
        if (userEntity == null) return false;
        PositionEntity positionEntity = userEntity.getPositions().get(mInstrumentIdTransaction);
        if (positionEntity == null) return false;

        try {
            int available_long = Integer.parseInt(MathUtils.subtract(positionEntity.getVolume_long(),
                    positionEntity.getVolume_long_frozen()));
            int available_short = Integer.parseInt(MathUtils.subtract(positionEntity.getVolume_short(),
                    positionEntity.getVolume_short_frozen()));

            if (DIRECTION_SELL.equals(direction)) {
                if (volumeN <= available_long) {
                    return true;
                }
            } else if (DIRECTION_BUY.equals(direction)) {
                if (volumeN <= available_short) {
                    return true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * date: 2019/4/9
     * author: chenli
     * description: 下单确认密码
     */
    private void checkPassword(final View view) {
        String date = (String) SPUtils.get(sContext, SettingConstants.CONFIG_LOGIN_DATE, "");
        String account = (String) SPUtils.get(sContext, SettingConstants.CONFIG_ACCOUNT, "");
        if (TimeUtils.getNowTime().equals(date) || account.contains(CommonConstants.BROKER_ID_VISITOR)) {
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
                    String passwordLocal = (String) SPUtils.get(sContext, SettingConstants.CONFIG_PASSWORD, "");
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
                    } else ToastUtils.showToast(sContext, "密码输入错误");
                }
            });
            dialog.setContentView(viewDialog);
            dialog.show();
        }
    }

    /**
     * date: 2019/4/15
     * author: chenli
     * description: 初始化软键盘
     */
    private Dialog initKeyboardDialog(final EditText mEditText, final int idKeyboard) {
        final Dialog dialog = new Dialog(getActivity(), R.style.Theme_Light_No_Dim_Dialog);
        View viewDialog = View.inflate(getActivity(), R.layout.view_dialog_keyboard, null);
        Window dialogWindow = dialog.getWindow();
        if (dialogWindow != null) {
            dialogWindow.getDecorView().setPadding(0, 0, 0, 0);
            dialogWindow.setGravity(Gravity.BOTTOM);
            WindowManager.LayoutParams lp = dialogWindow.getAttributes();
            lp.width = WindowManager.LayoutParams.WRAP_CONTENT;
            lp.height = ScreenUtils.dp2px(sContext, 200);
            lp.x = ScreenUtils.dp2px(sContext, 100);
            lp.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH;
            dialogWindow.setAttributes(lp);
            dialogWindow.getDecorView().setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    if (motionEvent.getAction() == MotionEvent.ACTION_OUTSIDE) {
                        dialog.dismiss();
                    }
                    return false;
                }
            });
        }

        Keyboard mKeyboard = new Keyboard(getActivity(), idKeyboard);
        KeyboardView keyboardView = viewDialog.findViewById(R.id.keyboard);
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
                try {
                    JSONObject jsonObject = new JSONObject();
                    Editable editable = mEditText.getText();
                    String text = mEditText.getText().toString();
                    int start = mEditText.length();
                    if (primaryCode == Keyboard.KEYCODE_DELETE) {
                        if (idKeyboard == R.xml.future_price) {
                            jsonObject.put(AMP_EVENT_PRICE_KEY, AMP_EVENT_PRICE_KEY_VALUE_DEL);
                        } else {
                            jsonObject.put(AMP_EVENT_VOLUME_KEY, AMP_EVENT_VOLUME_KEY_VALUE_DEL);
                        }
                        if (editable.length() > 0) {
                            if (!QUEUED_PRICE.equals(text) && !OPPONENT_PRICE.equals(text)
                                    && !MARKET_PRICE.equals(text) && !LATEST_PRICE.equals(text)) {
                                editable.delete(start - 1, start);
                            } else {
                                editable.clear();
                            }
                        }
                    } else if (primaryCode == mEditText.getContext().getResources().getInteger(R.integer.keycode_empty_text)) {
                        jsonObject.put(AMP_EVENT_VOLUME_KEY, AMP_EVENT_VOLUME_KEY_VALUE_CLEAR);
                        editable.clear();
                    } else if (primaryCode == Keyboard.KEYCODE_DONE) {
                        mPriceDialog.dismiss();
                    } else if (primaryCode == mEditText.getContext().getResources().getInteger(R.integer.keycode_line_up_price)) {
                        jsonObject.put(AMP_EVENT_PRICE_KEY, AMP_EVENT_PRICE_KEY_VALUE_QUEUED);
                        editable.clear();
                        editable.insert(0, QUEUED_PRICE);
                        mPriceDialog.dismiss();
                    } else if (primaryCode == mEditText.getContext().getResources().getInteger(R.integer.keycode_opponent_price)) {
                        jsonObject.put(AMP_EVENT_PRICE_KEY, AMP_EVENT_PRICE_KEY_VALUE_OPPONENT);
                        editable.clear();
                        editable.insert(0, OPPONENT_PRICE);
                        mPriceDialog.dismiss();
                    } else if (primaryCode == mEditText.getContext().getResources().getInteger(R.integer.keycode_last_price)) {
                        jsonObject.put(AMP_EVENT_PRICE_KEY, AMP_EVENT_PRICE_KEY_VALUE_LAST);
                        editable.clear();
                        editable.insert(0, LATEST_PRICE);
                        mPriceDialog.dismiss();
                    } else if (primaryCode == mEditText.getContext().getResources().getInteger(R.integer.keycode_market_price)) {
                        jsonObject.put(AMP_EVENT_PRICE_KEY, AMP_EVENT_PRICE_KEY_VALUE_MARKET);
                        editable.clear();
                        editable.insert(0, MARKET_PRICE);
                        mPriceDialog.dismiss();
                    } else if (primaryCode == mEditText.getContext().getResources().getInteger(R.integer.keycode_add)) {
                        if (idKeyboard == R.xml.future_price) {
                            jsonObject.put(AMP_EVENT_PRICE_KEY, AMP_EVENT_PRICE_KEY_VALUE_PLUS);
                        } else {
                            jsonObject.put(AMP_EVENT_VOLUME_KEY, AMP_EVENT_VOLUME_KEY_VALUE_PLUS);
                        }
                        QuoteEntity quoteEntity = DataManager.getInstance().getRtnData().getQuotes()
                                .get(mInstrumentId);
                        switch (text) {
                            case QUEUED_PRICE:
                                if (quoteEntity != null) {
                                    String ask_price1 = LatestFileManager.
                                            saveScaleByPtick(quoteEntity.getAsk_price1(), mInstrumentId);
                                    String bid_price1 = LatestFileManager.
                                            saveScaleByPtick(quoteEntity.getBid_price1(), mInstrumentId);
                                    editable.clear();
                                    editable.insert(0, MathUtils.subtract(bid_price1, ask_price1).
                                            contains("-") ? bid_price1 : ask_price1);
                                }
                                break;
                            case OPPONENT_PRICE:
                                if (quoteEntity != null) {
                                    String ask_price1 = LatestFileManager.
                                            saveScaleByPtick(quoteEntity.getAsk_price1(), mInstrumentId);
                                    String bid_price1 = LatestFileManager.
                                            saveScaleByPtick(quoteEntity.getBid_price1(), mInstrumentId);
                                    editable.clear();
                                    editable.insert(0, MathUtils.
                                            subtract(bid_price1, ask_price1).contains("-") ? ask_price1 : bid_price1);
                                }
                                break;
                            case LATEST_PRICE:
                                if (quoteEntity != null) {
                                    String last_price = LatestFileManager.
                                            saveScaleByPtick(quoteEntity.getLast_price(), mInstrumentId);
                                    editable.clear();
                                    editable.insert(0, last_price);
                                }
                                break;
                            case MARKET_PRICE:
                                if (quoteEntity != null) {
                                    String last_price = LatestFileManager.
                                            saveScaleByPtick(quoteEntity.getLast_price(), mInstrumentId);
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
                        if (idKeyboard == R.xml.future_price) {
                            jsonObject.put(AMP_EVENT_PRICE_KEY, AMP_EVENT_PRICE_KEY_VALUE_MINUS);
                        } else {
                            jsonObject.put(AMP_EVENT_VOLUME_KEY, AMP_EVENT_VOLUME_KEY_VALUE_MINUS);
                        }
                        QuoteEntity quoteEntity = DataManager.getInstance().getRtnData().getQuotes().
                                get(mInstrumentId);
                        switch (text) {
                            case QUEUED_PRICE:
                                if (quoteEntity != null) {
                                    String ask_price1 = LatestFileManager.
                                            saveScaleByPtick(quoteEntity.getAsk_price1(), mInstrumentId);
                                    String bid_price1 = LatestFileManager.
                                            saveScaleByPtick(quoteEntity.getBid_price1(), mInstrumentId);
                                    editable.clear();
                                    editable.insert(0, MathUtils.
                                            subtract(bid_price1, ask_price1).contains("-") ? bid_price1 : ask_price1);
                                }
                                break;
                            case OPPONENT_PRICE:
                                if (quoteEntity != null) {
                                    String ask_price1 = LatestFileManager.
                                            saveScaleByPtick(quoteEntity.getAsk_price1(), mInstrumentId);
                                    String bid_price1 = LatestFileManager.
                                            saveScaleByPtick(quoteEntity.getBid_price1(), mInstrumentId);
                                    editable.clear();
                                    editable.insert(0, MathUtils.
                                            subtract(bid_price1, ask_price1).contains("-") ? ask_price1 : bid_price1);
                                }
                                break;
                            case LATEST_PRICE:
                                if (quoteEntity != null) {
                                    String last_price = LatestFileManager.
                                            saveScaleByPtick(quoteEntity.getLast_price(), mInstrumentId);
                                    editable.clear();
                                    editable.insert(0, last_price);
                                }
                                break;
                            case MARKET_PRICE:
                                if (quoteEntity != null) {
                                    String last_price = LatestFileManager.
                                            saveScaleByPtick(quoteEntity.getLast_price(), mInstrumentId);
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
                                        SearchEntity searchEntity = LatestFileManager.getSearchEntities().
                                                get(mInstrumentId);
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
                        switch (insertStr) {
                            case ".":
                                jsonObject.put(AMP_EVENT_PRICE_KEY, AMP_EVENT_PRICE_KEY_VALUE_POINT);
                                break;
                            case "0":
                                if (idKeyboard == R.xml.future_price) {
                                    jsonObject.put(AMP_EVENT_PRICE_KEY, AMP_EVENT_PRICE_KEY_VALUE_0);
                                } else {
                                    jsonObject.put(AMP_EVENT_VOLUME_KEY, AMP_EVENT_VOLUME_KEY_VALUE_0);
                                }
                                break;
                            case "1":
                                if (idKeyboard == R.xml.future_price) {
                                    jsonObject.put(AMP_EVENT_PRICE_KEY, AMP_EVENT_PRICE_KEY_VALUE_1);
                                } else {
                                    jsonObject.put(AMP_EVENT_VOLUME_KEY, AMP_EVENT_VOLUME_KEY_VALUE_1);
                                }
                                break;
                            case "2":
                                if (idKeyboard == R.xml.future_price) {
                                    jsonObject.put(AMP_EVENT_PRICE_KEY, AMP_EVENT_PRICE_KEY_VALUE_2);
                                } else {
                                    jsonObject.put(AMP_EVENT_VOLUME_KEY, AMP_EVENT_VOLUME_KEY_VALUE_2);
                                }
                                break;
                            case "3":
                                if (idKeyboard == R.xml.future_price) {
                                    jsonObject.put(AMP_EVENT_PRICE_KEY, AMP_EVENT_PRICE_KEY_VALUE_3);
                                } else {
                                    jsonObject.put(AMP_EVENT_VOLUME_KEY, AMP_EVENT_VOLUME_KEY_VALUE_3);
                                }
                                break;
                            case "4":
                                if (idKeyboard == R.xml.future_price) {
                                    jsonObject.put(AMP_EVENT_PRICE_KEY, AMP_EVENT_PRICE_KEY_VALUE_4);
                                } else {
                                    jsonObject.put(AMP_EVENT_VOLUME_KEY, AMP_EVENT_VOLUME_KEY_VALUE_4);
                                }
                                break;
                            case "5":
                                if (idKeyboard == R.xml.future_price) {
                                    jsonObject.put(AMP_EVENT_PRICE_KEY, AMP_EVENT_PRICE_KEY_VALUE_5);
                                } else {
                                    jsonObject.put(AMP_EVENT_VOLUME_KEY, AMP_EVENT_VOLUME_KEY_VALUE_5);
                                }
                                break;
                            case "6":
                                if (idKeyboard == R.xml.future_price) {
                                    jsonObject.put(AMP_EVENT_PRICE_KEY, AMP_EVENT_PRICE_KEY_VALUE_6);
                                } else {
                                    jsonObject.put(AMP_EVENT_VOLUME_KEY, AMP_EVENT_VOLUME_KEY_VALUE_6);
                                }
                                break;
                            case "7":
                                if (idKeyboard == R.xml.future_price) {
                                    jsonObject.put(AMP_EVENT_PRICE_KEY, AMP_EVENT_PRICE_KEY_VALUE_7);
                                } else {
                                    jsonObject.put(AMP_EVENT_VOLUME_KEY, AMP_EVENT_VOLUME_KEY_VALUE_7);
                                }
                                break;
                            case "8":
                                if (idKeyboard == R.xml.future_price) {
                                    jsonObject.put(AMP_EVENT_PRICE_KEY, AMP_EVENT_PRICE_KEY_VALUE_8);
                                } else {
                                    jsonObject.put(AMP_EVENT_VOLUME_KEY, AMP_EVENT_VOLUME_KEY_VALUE_8);
                                }
                                break;
                            case "9":
                                if (idKeyboard == R.xml.future_price) {
                                    jsonObject.put(AMP_EVENT_PRICE_KEY, AMP_EVENT_PRICE_KEY_VALUE_9);
                                } else {
                                    jsonObject.put(AMP_EVENT_VOLUME_KEY, AMP_EVENT_VOLUME_KEY_VALUE_9);
                                }
                                break;
                            default:
                                break;
                        }
                        String str = editable.toString();
                        if (QUEUED_PRICE.equals(text) || OPPONENT_PRICE.equals(text) ||
                                MARKET_PRICE.equals(text) || LATEST_PRICE.equals(text) || mIsInit) {
                            //添加负号功能
                            if ("-".equals(str)) {
                                if ((!".".equals(insertStr)) || (".".equals(insertStr) && !text.contains(".")))
                                    editable.insert(start, insertStr);
                            } else {
                                editable.clear();
                                editable.insert(0, insertStr);
                            }
                            mIsInit = false;
                        } else if ((!".".equals(insertStr)) || (".".equals(insertStr) && !text.contains("."))) {
                            if ((mEditText.getSelectionEnd() - mEditText.getSelectionStart() == editable.length())) {
                                editable.clear();
                                editable.insert(0, insertStr);
                            } else editable.insert(start, insertStr);
                        }
                    }
                    if (idKeyboard == R.xml.future_price) {
                        Amplitude.getInstance().logEventWrap(AMP_PRICE_KEY, jsonObject);
                    } else {
                        Amplitude.getInstance().logEventWrap(AMP_VOLUME_KEY, jsonObject);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
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
        dialog.setContentView(viewDialog);
        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                mBinding.backAccount.setImageDrawable(
                        ContextCompat.getDrawable(sContext, R.mipmap.ic_account_circle_white_36dp));
                mIsKeyBoardJustHide = true;
            }
        });

        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                mBinding.backAccount.setImageDrawable(
                        ContextCompat.getDrawable(sContext, R.mipmap.ic_keyboard_hide_white_36dp));
            }
        });

        return dialog;
    }

    /**
     * date: 2019/7/3
     * author: chenli
     * description: 设置合约id
     */
    public void setInstrument_id(String ins) {
        mInstrumentId = ins;
        SearchEntity searchEntity = LatestFileManager.getSearchEntities().get(mInstrumentId);
        if (mInstrumentId.contains("KQ") && searchEntity != null)
            mInstrumentIdTransaction = searchEntity.getUnderlying_symbol();
        else mInstrumentIdTransaction = mInstrumentId;
        mExchangeId = mInstrumentIdTransaction.split("\\.")[0];
    }

    /**
     * date: 2019/7/5
     * author: chenli
     * description: 获取上报价格类型
     */
    private String getAmpPriceType(){
        String ampPriceType;
        switch (mBinding.price.getText().toString()) {
            case QUEUED_PRICE:
                ampPriceType = AMP_EVENT_PRICE_TYPE_VALUE_QUEUED;
                break;
            case OPPONENT_PRICE:
                ampPriceType = AMP_EVENT_PRICE_TYPE_VALUE_OPPONENT;
                break;
            case MARKET_PRICE:
                ampPriceType = AMP_EVENT_PRICE_TYPE_VALUE_MARKET;
                break;
            case LATEST_PRICE:
                ampPriceType = AMP_EVENT_PRICE_TYPE_VALUE_LAST;
                break;
            default:
                ampPriceType = AMP_EVENT_PRICE_TYPE_VALUE_NUMBER;
                break;
        }
        return ampPriceType;
    }


    /**
     * date: 2019/5/28
     * author: chenli
     * description: 下单开关切换
     */
    @Subscribe
    public void onEventSetting(InsertOrderEvent data) {
        mIsShowDialog = data.isInsertPopup();
    }

    /**
     * date: 7/9/17
     * author: chenli
     * description: 更新合约代码以及持仓信息--来自自选择约选择以及持仓列表点击切换
     */
    @Subscribe
    public void onEvent(SwitchInsEvent data) {
        if (data.getInstrument_id().equals(mInstrumentId)) return;
        setInstrument_id(data.getInstrument_id());
        //若如果不是从持仓点击而来，则刷新页面
        if (sDataManager.POSITION_DIRECTION.isEmpty()) show();
    }

}

package com.shinnytech.futures.controller.activity;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import q.rorbin.badgeview.Badge;
import q.rorbin.badgeview.QBadgeView;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RadioGroup;

import com.shinnytech.futures.R;
import com.shinnytech.futures.amplitude.api.Amplitude;
import com.shinnytech.futures.application.BaseApplication;
import com.shinnytech.futures.databinding.ActivityStopLossTakeProfitBinding;
import com.shinnytech.futures.model.bean.accountinfobean.PositionEntity;
import com.shinnytech.futures.model.bean.accountinfobean.UserEntity;
import com.shinnytech.futures.model.bean.conditionorderbean.ConditionOrderEntity;
import com.shinnytech.futures.model.bean.conditionorderbean.ConditionUserEntity;
import com.shinnytech.futures.model.bean.futureinfobean.QuoteEntity;
import com.shinnytech.futures.model.bean.reqbean.ReqConditionEntity;
import com.shinnytech.futures.model.bean.reqbean.ReqConditionOrderEntity;
import com.shinnytech.futures.model.bean.searchinfobean.SearchEntity;
import com.shinnytech.futures.model.engine.LatestFileManager;
import com.shinnytech.futures.utils.CloneUtils;
import com.shinnytech.futures.utils.MathUtils;
import com.shinnytech.futures.utils.ScreenUtils;
import com.shinnytech.futures.utils.TimeUtils;
import com.shinnytech.futures.utils.ToastUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static com.shinnytech.futures.application.BaseApplication.CO_BROADCAST_ACTION;
import static com.shinnytech.futures.application.BaseApplication.MD_BROADCAST_ACTION;
import static com.shinnytech.futures.application.BaseApplication.TD_BROADCAST_ACTION;
import static com.shinnytech.futures.constants.AmpConstants.AMP_CONDITION_SAVE;
import static com.shinnytech.futures.constants.AmpConstants.AMP_EVENT_CONDITION_DIRECTION;
import static com.shinnytech.futures.constants.AmpConstants.AMP_EVENT_CONDITION_EXPIRY;
import static com.shinnytech.futures.constants.AmpConstants.AMP_EVENT_CONDITION_INSTRUMENT_ID;
import static com.shinnytech.futures.constants.AmpConstants.AMP_EVENT_CONDITION_OFFSET;
import static com.shinnytech.futures.constants.AmpConstants.AMP_EVENT_CONDITION_TRIGGER_INSERT_PRICE;
import static com.shinnytech.futures.constants.AmpConstants.AMP_EVENT_CONDITION_TRIGGER_PRICE;
import static com.shinnytech.futures.constants.AmpConstants.AMP_EVENT_CONDITION_TRIGGER_TIME;
import static com.shinnytech.futures.constants.AmpConstants.AMP_EVENT_CONDITION_TRIGGER_VOLUME;
import static com.shinnytech.futures.constants.AmpConstants.AMP_EVENT_CONDITION_TYPE;
import static com.shinnytech.futures.constants.AmpConstants.AMP_EVENT_CONDITION_TYPE_VALUE_STOP_LOSS_PRICE_TRIGGER;
import static com.shinnytech.futures.constants.AmpConstants.AMP_EVENT_CONDITION_TYPE_VALUE_STOP_LOSS_TIME_TRIGGER;
import static com.shinnytech.futures.constants.BroadcastConstants.CO_MESSAGE;
import static com.shinnytech.futures.constants.CommonConstants.BREAK_EVEN_CONDITIONAL_ORDER;
import static com.shinnytech.futures.constants.ConditionConstants.CONDITION_CONTINGENT_PRICE_RELATION_GE;
import static com.shinnytech.futures.constants.ConditionConstants.CONDITION_CONTINGENT_PRICE_RELATION_LE;
import static com.shinnytech.futures.constants.ConditionConstants.CONDITION_CONTINGENT_TYPE_PRICE;
import static com.shinnytech.futures.constants.ConditionConstants.CONDITION_CONTINGENT_TYPE_TIME;
import static com.shinnytech.futures.constants.ConditionConstants.CONDITION_DIRECTION_BUY;
import static com.shinnytech.futures.constants.ConditionConstants.CONDITION_DIRECTION_BUY_TITLE;
import static com.shinnytech.futures.constants.ConditionConstants.CONDITION_DIRECTION_SELL;
import static com.shinnytech.futures.constants.ConditionConstants.CONDITION_DIRECTION_SELL_TITLE;
import static com.shinnytech.futures.constants.ConditionConstants.CONDITION_LOGIC_OR;
import static com.shinnytech.futures.constants.ConditionConstants.CONDITION_OFFSET_CLOSE;
import static com.shinnytech.futures.constants.ConditionConstants.CONDITION_OFFSET_CLOSE_TITLE;
import static com.shinnytech.futures.constants.ConditionConstants.CONDITION_OFFSET_OPEN;
import static com.shinnytech.futures.constants.ConditionConstants.CONDITION_OFFSET_OPEN_TITLE;
import static com.shinnytech.futures.constants.ConditionConstants.CONDITION_OFFSET_REVERSE;
import static com.shinnytech.futures.constants.ConditionConstants.CONDITION_OFFSET_REVERSE_TITLE;
import static com.shinnytech.futures.constants.ConditionConstants.CONDITION_PRICE_TYPE_CONSIDERATION;
import static com.shinnytech.futures.constants.ConditionConstants.CONDITION_PRICE_TYPE_CONSIDERATION_TITLE;
import static com.shinnytech.futures.constants.ConditionConstants.CONDITION_PRICE_TYPE_CONTINGENT;
import static com.shinnytech.futures.constants.ConditionConstants.CONDITION_PRICE_TYPE_CONTINGENT_TITLE;
import static com.shinnytech.futures.constants.ConditionConstants.CONDITION_PRICE_TYPE_LIMIT;
import static com.shinnytech.futures.constants.ConditionConstants.CONDITION_PRICE_TYPE_LIMIT_TITLE;
import static com.shinnytech.futures.constants.ConditionConstants.CONDITION_PRICE_TYPE_MARKET;
import static com.shinnytech.futures.constants.ConditionConstants.CONDITION_PRICE_TYPE_MARKET_TITLE;
import static com.shinnytech.futures.constants.ConditionConstants.CONDITION_PRICE_TYPE_OVER;
import static com.shinnytech.futures.constants.ConditionConstants.CONDITION_PRICE_TYPE_OVER_TITLE;
import static com.shinnytech.futures.constants.ConditionConstants.CONDITION_STATUS_LIVE;
import static com.shinnytech.futures.constants.ConditionConstants.CONDITION_STATUS_SUSPEND;
import static com.shinnytech.futures.constants.ConditionConstants.CONDITION_TIME_TYPE_GFD;
import static com.shinnytech.futures.constants.ConditionConstants.CONDITION_TIME_TYPE_GFD_TITLE;
import static com.shinnytech.futures.constants.ConditionConstants.CONDITION_TIME_TYPE_GFD_TITLE_EXHIBIT;
import static com.shinnytech.futures.constants.ConditionConstants.CONDITION_TIME_TYPE_GTC;
import static com.shinnytech.futures.constants.ConditionConstants.CONDITION_TIME_TYPE_GTC_TITLE;
import static com.shinnytech.futures.constants.ConditionConstants.CONDITION_TIME_TYPE_GTC_TITLE_EXHIBIT;
import static com.shinnytech.futures.constants.ConditionConstants.CONDITION_VOLUME_TYPE_ALL;
import static com.shinnytech.futures.constants.ConditionConstants.CONDITION_VOLUME_TYPE_CLOSE_ALL;
import static com.shinnytech.futures.constants.ConditionConstants.CONDITION_VOLUME_TYPE_CUSTOM;
import static com.shinnytech.futures.constants.ConditionConstants.CONDITION_VOLUME_TYPE_NUM;
import static com.shinnytech.futures.constants.CommonConstants.DIRECTION_BETWEEN_ACTIVITY;
import static com.shinnytech.futures.constants.TradeConstants.DIRECTION_BUY;
import static com.shinnytech.futures.constants.CommonConstants.INS_BETWEEN_ACTIVITY;
import static com.shinnytech.futures.constants.BroadcastConstants.MD_MESSAGE;
import static com.shinnytech.futures.constants.BroadcastConstants.TD_MESSAGE;
import static com.shinnytech.futures.constants.CommonConstants.VOLUME_BETWEEN_ACTIVITY;
import static com.shinnytech.futures.utils.TimeUtils.HMS_FORMAT;
import static com.shinnytech.futures.utils.TimeUtils.YMD_FORMAT_3;
import static com.shinnytech.futures.utils.TimeUtils.YMD_HMS_FORMAT_2;
import static com.shinnytech.futures.utils.TimeUtils.YMD_HMS_FORMAT_4;

public class StopLossTakeProfitActivity extends BaseActivity {
    private ActivityStopLossTakeProfitBinding mBinding;
    private Context sContext;
    private String mInstrumentId;
    private String mIns;
    private String mExchangeId;
    private String mDirection;
    private String mVolume;
    private BroadcastReceiver mReceiverMarket;
    private BroadcastReceiver mReceiverTrade;
    private Dialog mResponsibilityDialog;
    private ArrayAdapter<String> mTriggerOrderPriceSpinnerAdapter;
    private ArrayAdapter<String> mTriggerOrderVolumeSpinnerAdapter;
    private ArrayAdapter<String> mTriggerExpirySpinnerAdapter;
    private com.wdullaer.materialdatetimepicker.date.DatePickerDialog mDateDialog;
    private com.wdullaer.materialdatetimepicker.time.TimePickerDialog mTimeDialog;
    private List<String> mOrderPriceValues;
    private List<String> mOrderPriceValuesOnlyMarket;
    private Badge mBadgeView;
    private BroadcastReceiver mReceiverCondition;
    private String mInitOrderPrice;
    private String mInitOrderVolume;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mLayoutID = R.layout.activity_stop_loss_take_profit;
        mTitle = BREAK_EVEN_CONDITIONAL_ORDER;
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void initData() {
        mBinding = (ActivityStopLossTakeProfitBinding) mViewDataBinding;
        sContext = BaseApplication.getContext();
        String ins = getIntent().getStringExtra(INS_BETWEEN_ACTIVITY);
        setInstrumentName(ins);
        mDirection = getIntent().getStringExtra(DIRECTION_BETWEEN_ACTIVITY);
        mVolume = getIntent().getStringExtra(VOLUME_BETWEEN_ACTIVITY);
        mInitOrderPrice = "";
        mInitOrderVolume = mVolume;
        mOrderPriceValues = new ArrayList<>(Arrays.asList(getResources().getStringArray(R.array.trigger_order_price_without_contingent)));
        mOrderPriceValuesOnlyMarket = new ArrayList<>(Arrays.asList(getResources().getStringArray(R.array.trigger_order_price_only_market)));

        mTriggerOrderPriceSpinnerAdapter = new ArrayAdapter<>(this, R.layout.spinner_display_style_condition,
                R.id.tv_spinner_condition, new ArrayList<>());
        mTriggerOrderPriceSpinnerAdapter.setDropDownViewResource(R.layout.spinner_dropdown_style_condition);
        mBinding.spinnerTriggerOrderPrice.setAdapter(mTriggerOrderPriceSpinnerAdapter);
        mBinding.spinnerTriggerOrderPrice.setDropDownVerticalOffset(ScreenUtils.dp2px(sContext, 35));

        mTriggerOrderVolumeSpinnerAdapter = new ArrayAdapter<>(this, R.layout.spinner_display_style_condition,
                R.id.tv_spinner_condition, getResources().getStringArray(R.array.trigger_order_volume_sltp));
        mTriggerOrderVolumeSpinnerAdapter.setDropDownViewResource(R.layout.spinner_dropdown_style_condition);
        mBinding.spinnerTriggerOrderVolume.setAdapter(mTriggerOrderVolumeSpinnerAdapter);
        mBinding.spinnerTriggerOrderVolume.setDropDownVerticalOffset(ScreenUtils.dp2px(sContext, 35));

        mTriggerExpirySpinnerAdapter = new ArrayAdapter<>(this, R.layout.spinner_display_style_condition,
                R.id.tv_spinner_condition, getResources().getStringArray(R.array.trigger_expiry));
        mTriggerExpirySpinnerAdapter.setDropDownViewResource(R.layout.spinner_dropdown_style_condition);
        mBinding.spinnerTriggerExpiry.setAdapter(mTriggerExpirySpinnerAdapter);
        mBinding.spinnerTriggerExpiry.setDropDownVerticalOffset(ScreenUtils.dp2px(sContext, 35));
        ViewTreeObserver vto = mBinding.spinnerTriggerExpiry.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                mBinding.spinnerTriggerExpiry.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                int width  = mBinding.spinnerTriggerExpiry.getMeasuredWidth() / 2;
                mBinding.spinnerTriggerExpiry.setDropDownHorizontalOffset(width);
                mBinding.spinnerTriggerExpiry.setDropDownWidth(width);

            }
        });

        String time = TimeUtils.date2String(Calendar.getInstance().getTime(), YMD_HMS_FORMAT_2);
        mBinding.textViewTriggerTime.setText(time);

        if (DIRECTION_BUY.equals(mDirection)){
            mBinding.textViewExhibitOrderDirection.setText(CONDITION_DIRECTION_SELL_TITLE);
            mBinding.textViewExhibitOrderDirection.setTextColor(getResources().getColor(R.color.text_green));
            mBinding.textViewExhibitPriceLeft.setText(getResources().getString(R.string.trigger_larger_equal));
            mBinding.textViewExhibitPriceRight.setText(getResources().getString(R.string.trigger_lower_equal));
        } else {
            mBinding.textViewExhibitOrderDirection.setText(CONDITION_DIRECTION_BUY_TITLE);
            mBinding.textViewExhibitOrderDirection.setTextColor(getResources().getColor(R.color.text_red));
            mBinding.textViewExhibitPriceLeft.setText(getResources().getString(R.string.trigger_lower_equal));
            mBinding.textViewExhibitPriceRight.setText(getResources().getString(R.string.trigger_larger_equal));
        }
    }

    @Override
    protected void initEvent() {
        registerBroaderCast();

        mBinding.textViewResponsibility.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mResponsibilityDialog == null){
                    mResponsibilityDialog = new Dialog(StopLossTakeProfitActivity.this, R.style.AppTheme);
                    View view = View.inflate(StopLossTakeProfitActivity.this, R.layout.view_dialog_condition_order_responsibility, null);
                    mResponsibilityDialog.setContentView(view);
                    mResponsibilityDialog.setCanceledOnTouchOutside(false);
                    mResponsibilityDialog.setCancelable(false);
                    view.findViewById(R.id.agree).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            mResponsibilityDialog.dismiss();
                        }
                    });
                }
                if (!mResponsibilityDialog.isShowing())mResponsibilityDialog.show();
            }
        });

        mBinding.radioGroupPriceType.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId){
                    case R.id.radioButton_price:
                        mBinding.textTextTriggerTakeProfit.setText("止盈触发");
                        mBinding.textTextTriggerStopLoss.setVisibility(View.VISIBLE);
                        mBinding.editTextTriggerTakeProfitPrice.setVisibility(View.VISIBLE);
                        mBinding.editTextTriggerStopLossPrice.setVisibility(View.VISIBLE);
                        mBinding.textViewTakeProfitAlmost.setVisibility(View.VISIBLE);
                        mBinding.textTextTriggerTakeProfitValue.setVisibility(View.VISIBLE);
                        mBinding.textViewTakeProfitAlmostUnit.setVisibility(View.VISIBLE);
                        mBinding.textViewStopLossAlmost.setVisibility(View.VISIBLE);
                        mBinding.textTextTriggerStopLossValue.setVisibility(View.VISIBLE);
                        mBinding.textViewStopLossAlmostUnit.setVisibility(View.VISIBLE);
                        mBinding.textViewTriggerTime.setVisibility(View.INVISIBLE);

                        mBinding.textViewExhibitPrice1.setText("最新价");
                        mBinding.textViewExhibitPriceLeft.setVisibility(View.VISIBLE);
                        mBinding.textViewExhibitPriceLargerValue.setVisibility(View.VISIBLE);
                        mBinding.textViewExhibitOr.setVisibility(View.VISIBLE);
                        mBinding.textViewExhibitPrice2.setVisibility(View.VISIBLE);
                        mBinding.textViewExhibitPriceRight.setVisibility(View.VISIBLE);
                        mBinding.textViewExhibitPriceLowerValue.setVisibility(View.VISIBLE);
                        break;
                    case R.id.radioButton_time:
                        mBinding.textTextTriggerTakeProfit.setText("时间触发");
                        mBinding.textTextTriggerStopLoss.setVisibility(View.INVISIBLE);
                        mBinding.editTextTriggerTakeProfitPrice.setVisibility(View.INVISIBLE);
                        mBinding.editTextTriggerStopLossPrice.setVisibility(View.INVISIBLE);
                        mBinding.textViewTakeProfitAlmost.setVisibility(View.INVISIBLE);
                        mBinding.textTextTriggerTakeProfitValue.setVisibility(View.INVISIBLE);
                        mBinding.textViewTakeProfitAlmostUnit.setVisibility(View.INVISIBLE);
                        mBinding.textViewStopLossAlmost.setVisibility(View.INVISIBLE);
                        mBinding.textTextTriggerStopLossValue.setVisibility(View.INVISIBLE);
                        mBinding.textViewStopLossAlmostUnit.setVisibility(View.INVISIBLE);
                        mBinding.textViewTriggerTime.setVisibility(View.VISIBLE);

                        String[] time = mBinding.textViewTriggerTime.getText().toString().split("\n");
                        String data = time[0] + " " + time[1];
                        mBinding.textViewExhibitPrice1.setText(data);
                        mBinding.textViewExhibitPriceLeft.setVisibility(View.GONE);
                        mBinding.textViewExhibitPriceLargerValue.setVisibility(View.GONE);
                        mBinding.textViewExhibitOr.setVisibility(View.GONE);
                        mBinding.textViewExhibitPrice2.setVisibility(View.GONE);
                        mBinding.textViewExhibitPriceRight.setVisibility(View.GONE);
                        mBinding.textViewExhibitPriceLowerValue.setVisibility(View.GONE);
                        break;
                    default:
                        break;
                }
            }
        });

        mBinding.spinnerTriggerOrderPrice.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String price = (String) parent.getSelectedItem();
                if (CONDITION_PRICE_TYPE_LIMIT_TITLE.equals(price)){
                    mBinding.editTextTriggerOrderPrice.setText(mInitOrderPrice);
                    mBinding.editTextTriggerOrderPrice.setSelection(mInitOrderPrice.length());
                    mBinding.textViewExhibitOrderPrice.setText(mInitOrderPrice);
                    if (!mInitOrderPrice.isEmpty())mInitOrderPrice = "";
                }else {
                    mBinding.editTextTriggerOrderPrice.setText(price);
                    mBinding.textViewExhibitOrderPrice.setText(price);
                    //手动输入时需要获取焦点，防止焦点跑调
                    mBinding.editTextTriggerOrderPrice.clearFocus();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        mBinding.spinnerTriggerOrderVolume.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String triggerVolumeType = (String) parent.getSelectedItem();
                switch (triggerVolumeType){
                    case CONDITION_VOLUME_TYPE_CUSTOM:
                        mTriggerOrderPriceSpinnerAdapter.clear();
                        mTriggerOrderPriceSpinnerAdapter.addAll(mOrderPriceValues);
                        mTriggerOrderPriceSpinnerAdapter.notifyDataSetChanged();
                        mBinding.editTextTriggerOrderPrice.setEnabled(true);
                        mBinding.editTextTriggerOrderVolume.setText(mInitOrderVolume);
                        mBinding.editTextTriggerOrderVolume.setSelection(mInitOrderVolume.length());
                        mBinding.textViewExhibitOrderVolume.setText(mInitOrderVolume);
                        mBinding.textViewExhibitOrderOffset.setText(CONDITION_OFFSET_CLOSE_TITLE + "仓");
                        if (!mInitOrderVolume.equals(mVolume))mInitOrderVolume = mVolume;
                        break;
                    case CONDITION_VOLUME_TYPE_ALL:
                        mTriggerOrderPriceSpinnerAdapter.clear();
                        mTriggerOrderPriceSpinnerAdapter.addAll(mOrderPriceValues);
                        mTriggerOrderPriceSpinnerAdapter.notifyDataSetChanged();
                        mBinding.editTextTriggerOrderPrice.setEnabled(true);
                        mBinding.editTextTriggerOrderVolume.setText(CONDITION_VOLUME_TYPE_ALL);
                        mBinding.textViewExhibitOrderVolume.setText(CONDITION_VOLUME_TYPE_ALL);
                        mBinding.textViewExhibitOrderOffset.setText(CONDITION_OFFSET_CLOSE_TITLE + "仓");
                        mBinding.editTextTriggerOrderVolume.clearFocus();
                        break;
                    case CONDITION_OFFSET_REVERSE_TITLE:
                        mTriggerOrderPriceSpinnerAdapter.clear();
                        mTriggerOrderPriceSpinnerAdapter.addAll(mOrderPriceValuesOnlyMarket);
                        mTriggerOrderPriceSpinnerAdapter.notifyDataSetChanged();
                        mBinding.editTextTriggerOrderPrice.setEnabled(false);
                        mBinding.editTextTriggerOrderVolume.setText(CONDITION_VOLUME_TYPE_ALL);
                        mBinding.textViewExhibitOrderVolume.setText(CONDITION_VOLUME_TYPE_ALL);
                        mBinding.textViewExhibitOrderOffset.setText(CONDITION_OFFSET_REVERSE_TITLE);
                        mBinding.editTextTriggerOrderVolume.clearFocus();
                        break;
                    default:
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        mBinding.spinnerTriggerExpiry.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String period = (String) parent.getSelectedItem();
                switch (period){
                    case CONDITION_TIME_TYPE_GTC_TITLE:
                        mBinding.textViewExhibitOrderExpery.setText(CONDITION_TIME_TYPE_GTC_TITLE_EXHIBIT);
                        break;
                    case CONDITION_TIME_TYPE_GFD_TITLE:
                        mBinding.textViewExhibitOrderExpery.setText(CONDITION_TIME_TYPE_GFD_TITLE_EXHIBIT);
                        break;
                    default:
                        break;
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        mBinding.editTextTriggerTakeProfitPrice.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mBinding.textViewExhibitPriceLargerValue.setText(s);
            }

            @Override
            public void afterTextChanged(Editable s) {
                refreshTakeProfitAlmost();
            }
        });

        mBinding.editTextTriggerStopLossPrice.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mBinding.textViewExhibitPriceLowerValue.setText(s);
            }

            @Override
            public void afterTextChanged(Editable s) {
                refreshTakeStopLoss();
            }
        });

        mBinding.editTextTriggerOrderPrice.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mBinding.textViewExhibitOrderPrice.setText(s);
                if (s.toString().matches(".*\\d.*")){
                    int index = mBinding.spinnerTriggerOrderPrice.getSelectedItemPosition();
                    int position = mTriggerOrderPriceSpinnerAdapter.getPosition(getResources().
                            getString(R.string.trigger_order_price_limit));
                    if (index != position) {
                        mInitOrderPrice = s.toString();
                        mBinding.spinnerTriggerOrderPrice.setSelection(position);
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        mBinding.editTextTriggerOrderVolume.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mBinding.textViewExhibitOrderVolume.setText(s);
                if (s.toString().matches(".*\\d.*")){
                    int index = mBinding.spinnerTriggerOrderVolume.getSelectedItemPosition();
                    int position = mTriggerOrderVolumeSpinnerAdapter.getPosition(getResources().
                            getString(R.string.trigger_order_volume_custom));
                    if (index != position){
                        mInitOrderVolume = s.toString();
                        mBinding.spinnerTriggerOrderVolume.setSelection(position);
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                refreshTakeProfitAlmost();
                refreshTakeStopLoss();
            }
        });

        mBinding.editTextTriggerTakeProfitPrice.setSelectAllOnFocus(true);
        mBinding.editTextTriggerStopLossPrice.setSelectAllOnFocus(true);
        mBinding.editTextTriggerOrderPrice.setSelectAllOnFocus(true);
        mBinding.editTextTriggerOrderVolume.setSelectAllOnFocus(true);

        mBinding.textViewTriggerTime.setOnClickListener(new View.OnClickListener() {
            private String mTime = "";

            @Override
            public void onClick(View v) {
                final Calendar calendar = Calendar.getInstance();
                if (mDateDialog == null)
                    mDateDialog = com.wdullaer.materialdatetimepicker.date.DatePickerDialog.newInstance(
                            new com.wdullaer.materialdatetimepicker.date.DatePickerDialog.OnDateSetListener() {

                                @Override
                                public void onDateSet(com.wdullaer.materialdatetimepicker.date.DatePickerDialog view, int year, int monthOfYear, int dayOfMonth) {
                                    calendar.set(Calendar.YEAR, year);
                                    calendar.set(Calendar.MONTH, monthOfYear);
                                    calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                                    mTime = TimeUtils.date2String(calendar.getTime(), YMD_FORMAT_3);

                                    if (mTimeDialog == null){
                                        mTimeDialog = com.wdullaer.materialdatetimepicker.time.TimePickerDialog.newInstance(
                                                        new  com.wdullaer.materialdatetimepicker.time.TimePickerDialog.OnTimeSetListener(){

                                                            @Override
                                                            public void onTimeSet(com.wdullaer.materialdatetimepicker.time.TimePickerDialog view, int hourOfDay, int minute, int second) {
                                                                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                                                                calendar.set(Calendar.MINUTE, minute);
                                                                calendar.set(Calendar.SECOND, second);
                                                                String time = TimeUtils.date2String(calendar.getTime(), HMS_FORMAT);
                                                                mBinding.textViewTriggerTime.setText(mTime + "\n" + time);
                                                                mBinding.textViewExhibitPrice1.setText(mTime + " " + time);
                                                            }
                                                        },
                                                        calendar.get(Calendar.HOUR_OF_DAY),
                                                        calendar.get(Calendar.MINUTE),
                                                        calendar.get(Calendar.SECOND),
                                                        true);
                                        mTimeDialog.enableSeconds(true);
                                    }
                                    mTimeDialog.show(getSupportFragmentManager(), "time");
                                }
                            },
                            calendar.get(Calendar.YEAR),
                            calendar.get(Calendar.MONTH),
                            calendar.get(Calendar.DAY_OF_MONTH));
                mDateDialog.show(getSupportFragmentManager(), "date");
            }
        });

        mBinding.triggerSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                JSONObject jsonObject = new JSONObject();
                boolean isInsertOrder = true;

                //报单列表
                String exchange_id = mExchangeId;
                String instrument_id = mIns;
                String direction = "";
                switch (mDirection){
                    case CONDITION_DIRECTION_SELL:
                        direction = CONDITION_DIRECTION_BUY;
                        break;
                    case CONDITION_DIRECTION_BUY:
                        direction = CONDITION_DIRECTION_SELL;
                        break;
                    default:
                        break;
                }

                String volumeTitle = (String) mBinding.spinnerTriggerOrderVolume.getSelectedItem();
                String offset = CONDITION_OFFSET_CLOSE;
                if (CONDITION_OFFSET_REVERSE_TITLE.equals(volumeTitle))offset = CONDITION_OFFSET_REVERSE;

                String volumeType = "";
                String volumeTypeTitle = (String) mBinding.spinnerTriggerOrderVolume.getSelectedItem();
                switch (volumeTypeTitle){
                    case CONDITION_VOLUME_TYPE_CUSTOM:
                        volumeType = CONDITION_VOLUME_TYPE_NUM;
                        break;
                    case CONDITION_VOLUME_TYPE_ALL:
                        volumeType = CONDITION_VOLUME_TYPE_CLOSE_ALL;
                        break;
                    case CONDITION_OFFSET_REVERSE_TITLE:
                        volumeType = CONDITION_VOLUME_TYPE_CLOSE_ALL;
                        break;
                    default:
                        break;
                }

                String volume = mBinding.editTextTriggerOrderVolume.getText().toString();
                if (volume.isEmpty()){
                    ToastUtils.showToast(sContext, "报单量不能为空");
                    isInsertOrder = false;
                }
                int volumeInt = 0;
                try {
                    if (volumeType.equals(CONDITION_VOLUME_TYPE_NUM))volumeInt = Integer.parseInt(volume);
                }catch (NumberFormatException e){
                    ToastUtils.showToast(sContext, "报单量输入不合法");
                    isInsertOrder = false;
                }

                String priceType = "";
                String priceTypeTitle = (String) mBinding.spinnerTriggerOrderPrice.getSelectedItem();
                switch (priceTypeTitle){
                    case CONDITION_PRICE_TYPE_MARKET_TITLE:
                        priceType = CONDITION_PRICE_TYPE_MARKET;
                        break;
                    case CONDITION_PRICE_TYPE_CONSIDERATION_TITLE:
                        priceType = CONDITION_PRICE_TYPE_CONSIDERATION;
                        break;
                    case CONDITION_PRICE_TYPE_CONTINGENT_TITLE:
                        priceType = CONDITION_PRICE_TYPE_CONTINGENT;
                        break;
                    case CONDITION_PRICE_TYPE_LIMIT_TITLE:
                        priceType = CONDITION_PRICE_TYPE_LIMIT;
                        break;
                    case CONDITION_PRICE_TYPE_OVER_TITLE:
                        priceType = CONDITION_PRICE_TYPE_OVER;
                        break;
                    default:
                        break;
                }

                String limitPrice = mBinding.editTextTriggerOrderPrice.getText().toString();
                if (limitPrice.isEmpty()){
                    ToastUtils.showToast(sContext, "报单价不能为空");
                    isInsertOrder = false;
                }
                float limitPriceFloat = 0;
                try {
                    if (priceType.equals(CONDITION_PRICE_TYPE_LIMIT))limitPriceFloat = Float.parseFloat(limitPrice);
                }catch (NumberFormatException e){
                    ToastUtils.showToast(sContext, "报单价输入不合法");
                    isInsertOrder = false;
                }

                ReqConditionOrderEntity orderEntity = new ReqConditionOrderEntity();
                orderEntity.setDirection(direction);
                orderEntity.setExchange_id(exchange_id);
                orderEntity.setInstrument_id(instrument_id);
                orderEntity.setOffset(offset);
                orderEntity.setPrice_type(priceType);
                orderEntity.setLimit_price(limitPriceFloat);
                orderEntity.setVolume_type(volumeType);
                orderEntity.setVolume(volumeInt);
                orderEntity.setClose_today_prior(true);
                try {
                    jsonObject.put(AMP_EVENT_CONDITION_INSTRUMENT_ID, instrument_id);
                    if (volumeType.equals(CONDITION_VOLUME_TYPE_NUM)) jsonObject.put(AMP_EVENT_CONDITION_TRIGGER_VOLUME, volumeInt);
                    else if (volumeType.equals(CONDITION_VOLUME_TYPE_ALL))jsonObject.put(AMP_EVENT_CONDITION_TRIGGER_VOLUME, volumeType);
                    jsonObject.put(AMP_EVENT_CONDITION_TRIGGER_INSERT_PRICE, priceTypeTitle);
                    String directionTitle = "";
                    switch (mDirection){
                        case CONDITION_DIRECTION_SELL:
                            directionTitle = CONDITION_DIRECTION_BUY_TITLE;
                            break;
                        case CONDITION_DIRECTION_BUY:
                            directionTitle = CONDITION_DIRECTION_SELL_TITLE;
                            break;
                        default:
                            break;
                    }
                    jsonObject.put(AMP_EVENT_CONDITION_DIRECTION, directionTitle);
                    String offsetTitle = "";
                    switch (offset){
                        case CONDITION_OFFSET_OPEN:
                            offsetTitle = CONDITION_OFFSET_OPEN_TITLE;
                            break;
                        case CONDITION_OFFSET_CLOSE:
                            offsetTitle = CONDITION_OFFSET_CLOSE_TITLE;
                            break;
                        case CONDITION_OFFSET_REVERSE:
                            offsetTitle = CONDITION_OFFSET_REVERSE_TITLE;
                            break;
                        default:
                            break;
                    }
                    jsonObject.put(AMP_EVENT_CONDITION_OFFSET, offsetTitle);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                //条件列表
                int GTD_date = 0;
                String time_condition_type = CONDITION_TIME_TYPE_GFD;
                String timeConditionType = (String) mBinding.spinnerTriggerExpiry.getSelectedItem();
                switch (timeConditionType){
                    case CONDITION_TIME_TYPE_GTC_TITLE:
                        time_condition_type = CONDITION_TIME_TYPE_GTC;
                        break;
                    case CONDITION_TIME_TYPE_GFD_TITLE:
                        time_condition_type = CONDITION_TIME_TYPE_GFD;
                        break;
                    default:
                        break;
                }
                try {
                    jsonObject.put(AMP_EVENT_CONDITION_EXPIRY, time_condition_type);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                boolean is_cancel_ori_close_order = true;
                String conditions_logic_oper = CONDITION_LOGIC_OR;
                String contingentType = "";
                List<ReqConditionEntity> conditionList = new ArrayList<>();
                ReqConditionEntity conditionEntity = new ReqConditionEntity();
                conditionEntity.setExchange_id(exchange_id);
                conditionEntity.setInstrument_id(instrument_id);
                int  id = mBinding.radioGroupPriceType.getCheckedRadioButtonId();
                switch (id){
                    case R.id.radioButton_price:
                        contingentType = CONDITION_CONTINGENT_TYPE_PRICE;
                        float contingentPrice = 0;
                        String priceRelation = "";
                        String contingentTakeProfitPrice = mBinding.editTextTriggerTakeProfitPrice.getText().toString();
                        String contingentTakeProfitPriceRelation = mBinding.textViewExhibitPriceLeft.getText().toString();
                        String contingentStopLossPrice = mBinding.editTextTriggerStopLossPrice.getText().toString();
                        String contingentStopLossPriceRelation = mBinding.textViewExhibitPriceRight.getText().toString();
                        if (contingentTakeProfitPrice.isEmpty() && contingentStopLossPrice.isEmpty()){
                            ToastUtils.showToast(sContext, "止盈止损价不可都为空");
                            isInsertOrder = false;
                        }else if (!contingentTakeProfitPrice.isEmpty() && contingentStopLossPrice.isEmpty()){
                            if (DIRECTION_BUY.equals(mDirection)) priceRelation = CONDITION_CONTINGENT_PRICE_RELATION_GE;
                            else priceRelation = CONDITION_CONTINGENT_PRICE_RELATION_LE;

                            try {
                                contingentPrice = Float.parseFloat(contingentTakeProfitPrice);
                            }catch (NumberFormatException e){
                                ToastUtils.showToast(sContext, "止盈价输入不合法");
                                isInsertOrder = false;
                            }

                            try {
                                jsonObject.put(AMP_EVENT_CONDITION_TRIGGER_PRICE,
                                        contingentTakeProfitPriceRelation + " " + contingentTakeProfitPrice);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }else if (contingentTakeProfitPrice.isEmpty() && !contingentStopLossPrice.isEmpty()){
                            if (DIRECTION_BUY.equals(mDirection)) priceRelation = CONDITION_CONTINGENT_PRICE_RELATION_LE;
                            else priceRelation = CONDITION_CONTINGENT_PRICE_RELATION_GE;

                            try {
                                contingentPrice = Float.parseFloat(contingentStopLossPrice);
                            }catch (NumberFormatException e){
                                ToastUtils.showToast(sContext, "止损价输入不合法");
                                isInsertOrder = false;
                            }

                            try {
                                jsonObject.put(AMP_EVENT_CONDITION_TRIGGER_PRICE,
                                        contingentStopLossPriceRelation + " " + contingentStopLossPrice);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }else {
                            String priceRelationLoss;
                            if (DIRECTION_BUY.equals(mDirection)){
                                priceRelation = CONDITION_CONTINGENT_PRICE_RELATION_GE;
                                priceRelationLoss = CONDITION_CONTINGENT_PRICE_RELATION_LE;
                            }
                            else {
                                priceRelationLoss = CONDITION_CONTINGENT_PRICE_RELATION_GE;
                                priceRelation = CONDITION_CONTINGENT_PRICE_RELATION_LE;
                            }

                            try {
                                contingentPrice = Float.parseFloat(contingentTakeProfitPrice);
                            }catch (NumberFormatException e){
                                ToastUtils.showToast(sContext, "止盈价输入不合法");
                                isInsertOrder = false;
                            }

                            float contingentPriceLoss = 0;
                            try {
                                contingentPriceLoss = Float.parseFloat(contingentStopLossPrice);
                            }catch (NumberFormatException e){
                                ToastUtils.showToast(sContext, "止损价输入不合法");
                                isInsertOrder = false;
                            }
                            ReqConditionEntity conditionEntityLoss = new ReqConditionEntity();
                            conditionEntityLoss.setExchange_id(exchange_id);
                            conditionEntityLoss.setInstrument_id(instrument_id);
                            conditionEntityLoss.setContingent_price(contingentPriceLoss);
                            conditionEntityLoss.setPrice_relation(priceRelationLoss);
                            conditionEntityLoss.setContingent_type(contingentType);
                            conditionList.add(conditionEntityLoss);

                            try {
                                jsonObject.put(AMP_EVENT_CONDITION_TRIGGER_PRICE,
                                        contingentTakeProfitPriceRelation + " " + contingentTakeProfitPrice + " or "
                                + contingentStopLossPriceRelation + " " + contingentStopLossPrice);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                        conditionEntity.setContingent_price(contingentPrice);
                        conditionEntity.setPrice_relation(priceRelation);
                        try {
                            jsonObject.put(AMP_EVENT_CONDITION_TYPE, AMP_EVENT_CONDITION_TYPE_VALUE_STOP_LOSS_PRICE_TRIGGER);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        break;
                    case R.id.radioButton_time:
                        contingentType = CONDITION_CONTINGENT_TYPE_TIME;
                        long contingentTime = 0;
                        String contingentTimeS = mBinding.textViewTriggerTime.getText().toString();
                        Date date = TimeUtils.string2Date(contingentTimeS, YMD_HMS_FORMAT_2);
                        if (date != null) contingentTime = date.getTime() / 1000;
                        try {
                            String GTDDate = TimeUtils.date2String(date, YMD_HMS_FORMAT_4);
                            GTD_date = Integer.parseInt(GTDDate.split(" ")[0]);
                        }catch (Exception e){
                            ToastUtils.showToast(sContext, "日期格式错误");
                            isInsertOrder = false;
                        }
                        conditionEntity.setContingent_time(contingentTime);
                        try {
                            jsonObject.put(AMP_EVENT_CONDITION_TYPE, AMP_EVENT_CONDITION_TYPE_VALUE_STOP_LOSS_TIME_TRIGGER);
                            jsonObject.put(AMP_EVENT_CONDITION_TRIGGER_TIME, contingentTimeS);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        break;
                    default:
                        break;
                }

                Amplitude.getInstance().logEventWrap(AMP_CONDITION_SAVE, jsonObject);

                if (!isInsertOrder)return;
                conditionEntity.setContingent_type(contingentType);
                conditionList.add(conditionEntity);
                ReqConditionEntity[] conditionArray = new ReqConditionEntity[conditionList.size()];
                conditionArray = conditionList.toArray(conditionArray);
                ReqConditionOrderEntity orderList[] = {orderEntity};
                BaseApplication.getmTDWebSocket().sendReqInsertConditionOrder(conditionArray, orderList,
                        time_condition_type, GTD_date , is_cancel_ori_close_order, conditions_logic_oper);
            }
        });
    }


    /**
     * date: 2019/8/10
     * author: chenli
     * description: 显示合约标的名和代码、订阅行情
     */
    private void setInstrumentName(String instrumentId){
        if (instrumentId == null)return;
        if (instrumentId.contains("KQ.m@")){
            SearchEntity searchEntity = LatestFileManager.getSearchEntities().get(instrumentId);
            if (searchEntity != null)mInstrumentId = searchEntity.getUnderlying_symbol();
            else {
                ToastUtils.showToast(sContext, "不支持的合约");
                return;
            }
        }else mInstrumentId = instrumentId;
        sendSubscribeQuote(mInstrumentId);
        String[] data = mInstrumentId.split("\\.");
        mExchangeId = data[0];
        mIns = data[1];
        String name = mIns;
        SearchEntity searchEntity = LatestFileManager.getSearchEntities().get(mInstrumentId);
        if ( searchEntity != null)name = searchEntity.getInstrumentName();
        name = name.replaceAll("\\d+", "");
        if (mIns.contains("&") && mIns.contains(" ")){
            String ins = mIns.replaceAll("[^\\d&]", "");
            mBinding.textViewExhibitIns.setText(ins);
            mBinding.textViewExhibitName.setText(name.split(" ")[1]);
        }else {
            mBinding.textViewExhibitIns.setText(mIns);
            mBinding.textViewExhibitName.setText(name);
        }
    }

    /**
     * date: 2019/8/11
     * author: chenli
     * description: 注册行情广播
     */
    private void registerBroaderCast() {
        mReceiverMarket = new BroadcastReceiver() {
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
        LocalBroadcastManager.getInstance(this).registerReceiver(mReceiverMarket, new IntentFilter(MD_BROADCAST_ACTION));

        mReceiverTrade = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String mDataString = intent.getStringExtra("msg");
                switch (mDataString) {
                    case TD_MESSAGE:
                        refreshTakeProfitAlmost();
                        refreshTakeStopLoss();
                        break;
                    default:
                        break;
                }
            }
        };
        LocalBroadcastManager.getInstance(this).registerReceiver(mReceiverTrade, new IntentFilter(TD_BROADCAST_ACTION));
        
        mReceiverCondition = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String mDataString = intent.getStringExtra("msg");
                switch (mDataString) {
                    case CO_MESSAGE:
                        refreshCO();
                        break;
                    default:
                        break;
                }
            }
        };
        LocalBroadcastManager.getInstance(this).registerReceiver(mReceiverCondition, new IntentFilter(CO_BROADCAST_ACTION));

    }

    /**
     * date: 2019/8/11
     * author: chenli
     * description: 刷新合约行情
     */
    public void refreshMD() {
        QuoteEntity quoteEntity = sDataManager.getRtnData().getQuotes().get(mInstrumentId);
        if (quoteEntity == null) return;
        if (mInstrumentId.contains("&") && mInstrumentId.contains(" ")) {
            quoteEntity = CloneUtils.clone(quoteEntity);
            quoteEntity = LatestFileManager.calculateCombineQuoteFull(quoteEntity);
        }
        String last = LatestFileManager.saveScaleByPtick(quoteEntity.getLast_price(), mInstrumentId);
        mBinding.textViewExhibitPriceValue.setText(last);
    }

    /**
     * date: 2019/8/12
     * author: chenli
     * description: 刷新约盈信息
     */
    public void refreshTakeProfitAlmost() {
        UserEntity userEntity = sDataManager.getTradeBean().getUsers().get(sDataManager.USER_ID);
        if (userEntity == null) return;
        PositionEntity positionEntity = userEntity.getPositions().get(mInstrumentId);
        if (positionEntity == null)return;
        String takeProfit = mBinding.editTextTriggerTakeProfitPrice.getText().toString();
        String open_interest;
        if (DIRECTION_BUY.equals(mDirection))open_interest = positionEntity.getOpen_price_long();
        else open_interest = positionEntity.getOpen_price_short();
        SearchEntity searchEntity = LatestFileManager.getSearchEntities().get(mInstrumentId);
        if (searchEntity == null)return;
        String vm = searchEntity.getVm();
        String sub;
        if (DIRECTION_BUY.equals(mDirection))sub = MathUtils.subtract(takeProfit, open_interest);
        else sub = MathUtils.subtract(open_interest, takeProfit);
        if (MathUtils.lower(sub, "0")) sub = "0";
        String takeProfitAlmost = MathUtils.round(MathUtils.multiply(vm, sub), 0);
        mBinding.textTextTriggerTakeProfitValue.setText(takeProfitAlmost);
    }

    /**
     * date: 2019/8/12
     * author: chenli
     * description: 刷新约亏信息
     */
    public void refreshTakeStopLoss() {
        UserEntity userEntity = sDataManager.getTradeBean().getUsers().get(sDataManager.USER_ID);
        if (userEntity == null) return;
        PositionEntity positionEntity = userEntity.getPositions().get(mInstrumentId);
        if (positionEntity == null)return;
        String stopLoss = mBinding.editTextTriggerStopLossPrice.getText().toString();
        String open_interest;
        if (DIRECTION_BUY.equals(mDirection))open_interest = positionEntity.getOpen_price_long();
        else open_interest = positionEntity.getOpen_price_short();
        SearchEntity searchEntity = LatestFileManager.getSearchEntities().get(mInstrumentId);
        if (searchEntity == null)return;
        String vm = searchEntity.getVm();
        String sub;
        if (DIRECTION_BUY.equals(mDirection))sub = MathUtils.subtract(stopLoss, open_interest);
        else sub = MathUtils.subtract(open_interest, stopLoss);
        if (MathUtils.upper(sub, "0")) sub = "0";
        else sub = MathUtils.subtract("0", sub);
        String stopLossAlmost = MathUtils.round(MathUtils.multiply(vm, sub), 0);
        mBinding.textTextTriggerStopLossValue.setText(stopLossAlmost);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mReceiverMarket != null)LocalBroadcastManager.getInstance(this).unregisterReceiver(mReceiverMarket);
        if (mReceiverTrade != null)LocalBroadcastManager.getInstance(this).unregisterReceiver(mReceiverTrade);
        if (mReceiverCondition != null)LocalBroadcastManager.getInstance(this).unregisterReceiver(mReceiverCondition);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_condition_menu, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem menuItem = menu.findItem(R.id.right_navigation);
        FrameLayout rootView = (FrameLayout) menuItem.getActionView();
        ImageView view = rootView.findViewById(R.id.view_menu);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(StopLossTakeProfitActivity.this,
                        ManagerConditionOrderActivity.class);
                StopLossTakeProfitActivity.this.startActivity(intent);
            }
        });
        mBadgeView = new QBadgeView(sContext).bindTarget(view)
                .setBadgeNumber(-1)
                .setBadgeBackgroundColor(sContext.getResources().getColor(R.color.launch))
                .setBadgeGravity( Gravity.END | Gravity.TOP)
                .setBadgePadding(4, true)
                .setGravityOffset(8, 8, true)
                .setBadgeTextColor(sContext.getResources().getColor(R.color.white));
        refreshCO();
        return true;
    }

    /**
     * date: 2019/8/19
     * author: chenli
     * description: 刷新条件单信息，更新图标
     */
    private void refreshCO() {
        ConditionUserEntity userEntity = sDataManager.getConditionOrderBean().getUsers().get(sDataManager.USER_ID);
        if (userEntity == null)return;
        Map<String, ConditionOrderEntity> condition_orders = userEntity.getCondition_orders();
        int count = 0;
        for (ConditionOrderEntity conditionOrderEntity :
                condition_orders.values()) {
            String status = conditionOrderEntity.getStatus();
            if (CONDITION_STATUS_LIVE.equals(status))count++;
        }
        if (count == 0)
            mBadgeView.setBadgeNumber(-1)
                      .setGravityOffset(8, 8, true);
        else mBadgeView.setBadgeNumber(count)
                       .setGravityOffset(4, 4, true);
    }
}

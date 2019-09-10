package com.shinnytech.futures.controller.activity;

import android.app.Activity;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;

import androidx.core.view.GravityCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import q.rorbin.badgeview.Badge;
import q.rorbin.badgeview.QBadgeView;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.shinnytech.futures.R;
import com.shinnytech.futures.amplitude.api.Amplitude;
import com.shinnytech.futures.application.BaseApplication;
import com.shinnytech.futures.databinding.ActivityConditionOrderBinding;
import com.shinnytech.futures.model.adapter.DialogAdapter;
import com.shinnytech.futures.model.bean.conditionorderbean.ConditionOrderEntity;
import com.shinnytech.futures.model.bean.conditionorderbean.ConditionUserEntity;
import com.shinnytech.futures.model.bean.futureinfobean.QuoteEntity;
import com.shinnytech.futures.model.bean.reqbean.ReqConditionEntity;
import com.shinnytech.futures.model.bean.reqbean.ReqConditionOrderEntity;
import com.shinnytech.futures.model.bean.searchinfobean.SearchEntity;
import com.shinnytech.futures.model.engine.LatestFileManager;
import com.shinnytech.futures.model.listener.SimpleRecyclerViewItemClickListener;
import com.shinnytech.futures.utils.CloneUtils;
import com.shinnytech.futures.utils.DividerGridItemDecorationUtils;
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
import static com.shinnytech.futures.constants.AmpConstants.AMP_EVENT_CONDITION_TYPE_VALUE_OPENING_TRIGGER;
import static com.shinnytech.futures.constants.AmpConstants.AMP_EVENT_CONDITION_TYPE_VALUE_PRICE_RANGE_TRIGGER;
import static com.shinnytech.futures.constants.AmpConstants.AMP_EVENT_CONDITION_TYPE_VALUE_PRICE_TRIGGER;
import static com.shinnytech.futures.constants.AmpConstants.AMP_EVENT_CONDITION_TYPE_VALUE_TIME_TRIGGER;
import static com.shinnytech.futures.constants.AmpConstants.AMP_EVENT_OPTIONAL_DIRECTION;
import static com.shinnytech.futures.constants.AmpConstants.AMP_EVENT_OPTIONAL_DIRECTION_VALUE_ADD;
import static com.shinnytech.futures.constants.AmpConstants.AMP_EVENT_OPTIONAL_DIRECTION_VALUE_DELETE;
import static com.shinnytech.futures.constants.AmpConstants.AMP_EVENT_OPTIONAL_INSTRUMENT_ID;
import static com.shinnytech.futures.constants.AmpConstants.AMP_OPTIONAL_FUTURE_INFO;
import static com.shinnytech.futures.constants.BroadcastConstants.CO_MESSAGE;
import static com.shinnytech.futures.constants.BroadcastConstants.TD_MESSAGE;
import static com.shinnytech.futures.constants.BroadcastConstants.TD_MESSAGE_SETTLEMENT;
import static com.shinnytech.futures.constants.CommonConstants.MENU_TITLE_NAVIGATION;
import static com.shinnytech.futures.constants.ConditionConstants.CONDITION_CONTINGENT_PRICE;
import static com.shinnytech.futures.constants.ConditionConstants.CONDITION_CONTINGENT_PRICE_RELATION_G;
import static com.shinnytech.futures.constants.ConditionConstants.CONDITION_CONTINGENT_PRICE_RELATION_GE;
import static com.shinnytech.futures.constants.ConditionConstants.CONDITION_CONTINGENT_PRICE_RELATION_L;
import static com.shinnytech.futures.constants.ConditionConstants.CONDITION_CONTINGENT_PRICE_RELATION_LE;
import static com.shinnytech.futures.constants.ConditionConstants.CONDITION_CONTINGENT_TIME;
import static com.shinnytech.futures.constants.ConditionConstants.CONDITION_CONTINGENT_TYPE_MARKET_OPEN;
import static com.shinnytech.futures.constants.ConditionConstants.CONDITION_CONTINGENT_TYPE_PRICE;
import static com.shinnytech.futures.constants.ConditionConstants.CONDITION_CONTINGENT_TYPE_PRICE_RANGE;
import static com.shinnytech.futures.constants.ConditionConstants.CONDITION_CONTINGENT_TYPE_TIME;
import static com.shinnytech.futures.constants.ConditionConstants.CONDITION_DIRECTION_BUY;
import static com.shinnytech.futures.constants.ConditionConstants.CONDITION_DIRECTION_SELL;
import static com.shinnytech.futures.constants.ConditionConstants.CONDITION_LOGIC_OR;
import static com.shinnytech.futures.constants.ConditionConstants.CONDITION_OFFSET_CLOSE;
import static com.shinnytech.futures.constants.ConditionConstants.CONDITION_OFFSET_OPEN;
import static com.shinnytech.futures.constants.ConditionConstants.CONDITION_OFFSET_REVERSE;
import static com.shinnytech.futures.constants.CommonConstants.CONDITION_ORDER_ACTIVITY_TO_SEARCH_ACTIVITY;
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
import static com.shinnytech.futures.constants.ConditionConstants.CONDITION_TIME_TYPE_GTC;
import static com.shinnytech.futures.constants.ConditionConstants.CONDITION_VOLUME_TYPE_CLOSE_ALL;
import static com.shinnytech.futures.constants.ConditionConstants.CONDITION_VOLUME_TYPE_NUM;
import static com.shinnytech.futures.constants.CommonConstants.INS_BETWEEN_ACTIVITY;
import static com.shinnytech.futures.constants.BroadcastConstants.MD_MESSAGE;
import static com.shinnytech.futures.constants.CommonConstants.NEW_CONDITIONAL_ORDER;
import static com.shinnytech.futures.constants.ConditionConstants.CONDITION_TIME_TYPE_GFD;
import static com.shinnytech.futures.constants.ConditionConstants.CONDITION_DIRECTION_BUY_TITLE;
import static com.shinnytech.futures.constants.ConditionConstants.CONDITION_DIRECTION_SELL_TITLE;
import static com.shinnytech.futures.constants.ConditionConstants.CONDITION_OFFSET_CLOSE_TITLE;
import static com.shinnytech.futures.constants.ConditionConstants.CONDITION_OFFSET_OPEN_TITLE;
import static com.shinnytech.futures.constants.ConditionConstants.CONDITION_OFFSET_REVERSE_TITLE;
import static com.shinnytech.futures.constants.ConditionConstants.CONDITION_TIME_TYPE_GTC_TITLE;
import static com.shinnytech.futures.constants.ConditionConstants.CONDITION_TIME_TYPE_GTC_TITLE_EXHIBIT;
import static com.shinnytech.futures.constants.ConditionConstants.CONDITION_TIME_TYPE_GFD_TITLE;
import static com.shinnytech.futures.constants.ConditionConstants.CONDITION_TIME_TYPE_GFD_TITLE_EXHIBIT;
import static com.shinnytech.futures.constants.ConditionConstants.CONDITION_VOLUME_TYPE_ALL;
import static com.shinnytech.futures.constants.ConditionConstants.CONDITION_VOLUME_TYPE_CUSTOM;
import static com.shinnytech.futures.constants.ConditionConstants.CONDITION_WAY_OPEN;
import static com.shinnytech.futures.constants.ConditionConstants.CONDITION_WAY_PRICE;
import static com.shinnytech.futures.constants.ConditionConstants.CONDITION_WAY_RANGE;
import static com.shinnytech.futures.constants.ConditionConstants.CONDITION_WAY_TIME;
import static com.shinnytech.futures.constants.CommonConstants.SOURCE_ACTIVITY;
import static com.shinnytech.futures.constants.CommonConstants.SOURCE_ACTIVITY_CONDITION_ORDER;
import static com.shinnytech.futures.utils.TimeUtils.HMS_FORMAT;
import static com.shinnytech.futures.utils.TimeUtils.YMD_FORMAT_4;
import static com.shinnytech.futures.utils.TimeUtils.YMD_HMS_FORMAT_4;

public class ConditionOrderActivity extends BaseActivity {

    private ActivityConditionOrderBinding mBinding;
    private Context sContext;
    private ArrayAdapter<String> mTriggerWaySpinnerAdapter;
    private ArrayAdapter<String> mTriggerRangerSpinnerAdapter;
    private ArrayAdapter<String> mTriggerOrderPriceSpinnerAdapter;
    private ArrayAdapter<String> mTriggerOrderVolumeSpinnerAdapter;
    private ArrayAdapter<String> mTriggerOrderOffsetSpinnerAdapter;
    private ArrayAdapter<String> mTriggerOrderDirectionSpinnerAdapter;
    private ArrayAdapter<String> mTriggerExpirySpinnerAdapter;
    private Dialog mOptionalDialog;
    private DialogAdapter mOptionalDialogAdapter;
    private RecyclerView mOptionalRecyclerView;
    private String mInstrumentId;
    private String mIns;
    private String mExchangeId;
    private BroadcastReceiver mReceiverMarket;
    private com.wdullaer.materialdatetimepicker.date.DatePickerDialog mDateDialog;
    private com.wdullaer.materialdatetimepicker.time.TimePickerDialog mTimeDialog;
    private Dialog mResponsibilityDialog;
    private List<String> mOrderPriceValues;
    private List<String> mOrderPriceValuesWithoutContingent;
    private List<String> mOrderPriceValuesOnlyMarket;
    private List<String> mOrderVolumeValues;
    private List<String> mOrderVolumeValuesOnlyCustom;
    private List<String> mOrderVolumeValuesOnlyAll;
    private Badge mBadgeView;
    private BroadcastReceiver mReceiverCondition;
    private String mInitOrderPrice;
    private String mInitOrderVolume;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mLayoutID = R.layout.activity_condition_order;
        mTitle = NEW_CONDITIONAL_ORDER;
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void initData() {
        mBinding = (ActivityConditionOrderBinding) mViewDataBinding;
        sContext = BaseApplication.getContext();
        String ins = getIntent().getStringExtra(INS_BETWEEN_ACTIVITY);
        setInstrumentName(ins);
        mInitOrderPrice = "";
        mInitOrderVolume = "1";
        mOrderPriceValues = new ArrayList<>(Arrays.asList(getResources().getStringArray(R.array.trigger_order_price)));
        mOrderPriceValuesWithoutContingent = new ArrayList<>(Arrays.asList(getResources().getStringArray(R.array.trigger_order_price_without_contingent)));
        mOrderPriceValuesOnlyMarket = new ArrayList<>(Arrays.asList(getResources().getStringArray(R.array.trigger_order_price_only_market)));
        mOrderVolumeValues = new ArrayList<>(Arrays.asList(getResources().getStringArray(R.array.trigger_order_volume)));
        mOrderVolumeValuesOnlyCustom = new ArrayList<>(Arrays.asList(getResources().getStringArray(R.array.trigger_order_volume_only_custom)));
        mOrderVolumeValuesOnlyAll = new ArrayList<>(Arrays.asList(getResources().getStringArray(R.array.trigger_order_volume_only_all)));

        mTriggerWaySpinnerAdapter = new ArrayAdapter<>(this, R.layout.spinner_display_style_condition,
                R.id.tv_spinner_condition, getResources().getStringArray(R.array.trigger_way));
        mTriggerWaySpinnerAdapter.setDropDownViewResource(R.layout.spinner_dropdown_style_condition);
        mBinding.spinnerTriggerWay.setAdapter(mTriggerWaySpinnerAdapter);
        mBinding.spinnerTriggerWay.setDropDownVerticalOffset(ScreenUtils.dp2px(sContext, 35));
        ViewTreeObserver vtoTriggerWay = mBinding.spinnerTriggerWay.getViewTreeObserver();
        vtoTriggerWay.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                mBinding.spinnerTriggerWay.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                int width  = mBinding.spinnerTriggerWay.getMeasuredWidth() / 2;
                mBinding.spinnerTriggerWay.setDropDownHorizontalOffset(width);
                mBinding.spinnerTriggerWay.setDropDownWidth(width);

            }
        });

        mTriggerRangerSpinnerAdapter = new ArrayAdapter<>(this, R.layout.spinner_display_style_condition,
                R.id.tv_spinner_condition, getResources().getStringArray(R.array.trigger_price_range));
        mTriggerRangerSpinnerAdapter.setDropDownViewResource(R.layout.spinner_dropdown_style_condition);
        mBinding.spinnerTriggerPriceRange.setAdapter(mTriggerRangerSpinnerAdapter);
        mBinding.spinnerTriggerPriceRange.setDropDownVerticalOffset(ScreenUtils.dp2px(sContext, 35));

        mTriggerOrderPriceSpinnerAdapter = new ArrayAdapter<>(this, R.layout.spinner_display_style_condition,
                R.id.tv_spinner_condition, new ArrayList<>());
        mTriggerOrderPriceSpinnerAdapter.setDropDownViewResource(R.layout.spinner_dropdown_style_condition);
        mBinding.spinnerTriggerOrderPrice.setAdapter(mTriggerOrderPriceSpinnerAdapter);
        mBinding.spinnerTriggerOrderPrice.setDropDownVerticalOffset(ScreenUtils.dp2px(sContext, 35));

        mTriggerOrderVolumeSpinnerAdapter = new ArrayAdapter<>(this, R.layout.spinner_display_style_condition,
                R.id.tv_spinner_condition, new ArrayList<>());
        mTriggerOrderVolumeSpinnerAdapter.setDropDownViewResource(R.layout.spinner_dropdown_style_condition);
        mBinding.spinnerTriggerOrderVolume.setAdapter(mTriggerOrderVolumeSpinnerAdapter);
        mBinding.spinnerTriggerOrderVolume.setDropDownVerticalOffset(ScreenUtils.dp2px(sContext, 35));

        mTriggerOrderOffsetSpinnerAdapter = new ArrayAdapter<>(this, R.layout.spinner_display_style_condition,
                R.id.tv_spinner_condition, getResources().getStringArray(R.array.trigger_order_offset));
        mTriggerOrderOffsetSpinnerAdapter.setDropDownViewResource(R.layout.spinner_dropdown_style_condition);
        mBinding.spinnerTriggerOrderOffset.setAdapter(mTriggerOrderOffsetSpinnerAdapter);
        mBinding.spinnerTriggerOrderOffset.setDropDownVerticalOffset(ScreenUtils.dp2px(sContext, 35));
        ViewTreeObserver vtoOffset = mBinding.spinnerTriggerOrderOffset.getViewTreeObserver();
        vtoOffset.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                mBinding.spinnerTriggerOrderOffset.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                int width  = mBinding.spinnerTriggerOrderOffset.getMeasuredWidth() / 2;
                mBinding.spinnerTriggerOrderOffset.setDropDownHorizontalOffset(width);
                mBinding.spinnerTriggerOrderOffset.setDropDownWidth(width);

            }
        });

        mTriggerOrderDirectionSpinnerAdapter = new ArrayAdapter<>(this, R.layout.spinner_display_style_condition,
                R.id.tv_spinner_condition, getResources().getStringArray(R.array.trigger_order_direction));
        mTriggerOrderDirectionSpinnerAdapter.setDropDownViewResource(R.layout.spinner_dropdown_style_condition);
        mBinding.spinnerTriggerOrderDirection.setAdapter(mTriggerOrderDirectionSpinnerAdapter);
        mBinding.spinnerTriggerOrderDirection.setDropDownVerticalOffset(ScreenUtils.dp2px(sContext, 35));
        ViewTreeObserver vtoDirection = mBinding.spinnerTriggerOrderDirection.getViewTreeObserver();
        vtoDirection.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                mBinding.spinnerTriggerOrderDirection.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                int width  = mBinding.spinnerTriggerOrderDirection.getMeasuredWidth() / 2;
                mBinding.spinnerTriggerOrderDirection.setDropDownHorizontalOffset(width);
                mBinding.spinnerTriggerOrderDirection.setDropDownWidth(width);

            }
        });

        mTriggerExpirySpinnerAdapter = new ArrayAdapter<>(this, R.layout.spinner_display_style_condition,
                R.id.tv_spinner_condition, getResources().getStringArray(R.array.trigger_expiry));
        mTriggerExpirySpinnerAdapter.setDropDownViewResource(R.layout.spinner_dropdown_style_condition);
        mBinding.spinnerTriggerExpiry.setAdapter(mTriggerExpirySpinnerAdapter);
        mBinding.spinnerTriggerExpiry.setDropDownVerticalOffset(ScreenUtils.dp2px(sContext, 35));
        ViewTreeObserver vtoExpiry = mBinding.spinnerTriggerExpiry.getViewTreeObserver();
        vtoExpiry.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                mBinding.spinnerTriggerExpiry.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                int width  = mBinding.spinnerTriggerExpiry.getMeasuredWidth() / 2;
                mBinding.spinnerTriggerExpiry.setDropDownHorizontalOffset(width);
                mBinding.spinnerTriggerExpiry.setDropDownWidth(width);

            }
        });

    }

    @Override
    protected void initEvent() {
        registerBroaderCast();

        mBinding.spinnerTriggerWay.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                adjustPriceLayout();
                filterPriceTypeValues();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        mBinding.spinnerTriggerPriceRange.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String rangeType = (String) parent.getSelectedItem();
                mBinding.textViewExhibitPriceLower.setText(rangeType);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        mBinding.spinnerTriggerOrderOffset.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String offset = (String) parent.getSelectedItem();
                switch (offset){
                    case CONDITION_OFFSET_OPEN_TITLE:
                        mBinding.textViewExhibitOrderOffset.setText(offset + "仓");
                        break;
                    case CONDITION_OFFSET_CLOSE_TITLE:
                        mBinding.textViewExhibitOrderOffset.setText(offset + "仓");
                        break;
                    case CONDITION_OFFSET_REVERSE_TITLE:
                        mBinding.textViewExhibitOrderOffset.setText(offset);
                        break;
                    default:
                        break;
                }
                filterPriceTypeValues();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

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
                        mBinding.editTextTriggerOrderVolume.setText(mInitOrderVolume);
                        mBinding.editTextTriggerOrderVolume.setSelection(mInitOrderVolume.length());
                        mBinding.textViewExhibitOrderVolume.setText(mInitOrderVolume);
                        if (!mInitOrderVolume.equals("1"))mInitOrderVolume = "1";
                        break;
                    case CONDITION_VOLUME_TYPE_ALL:
                        mBinding.editTextTriggerOrderVolume.setText(CONDITION_VOLUME_TYPE_ALL);
                        mBinding.textViewExhibitOrderVolume.setText(CONDITION_VOLUME_TYPE_ALL);
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

        mBinding.spinnerTriggerOrderDirection.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String direction = (String) parent.getSelectedItem();
                switch (direction){
                    case CONDITION_DIRECTION_BUY_TITLE:
                        mBinding.textViewExhibitDirection.setText(CONDITION_DIRECTION_BUY_TITLE);
                        mBinding.textViewExhibitDirection.setTextColor(getResources().getColor(R.color.text_red));
                        break;
                    case CONDITION_DIRECTION_SELL_TITLE:
                        mBinding.textViewExhibitDirection.setText(CONDITION_DIRECTION_SELL_TITLE);
                        mBinding.textViewExhibitDirection.setTextColor(getResources().getColor(R.color.text_green));
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

        mBinding.editTextTriggerPrice.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mBinding.textViewExhibitPriceLowerValue.setText(s);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        mBinding.editTextTriggerRangeLargerValue.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mBinding.textViewExhibitPriceLargerValue.setText(s);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        mBinding.editTextTriggerRangeLowerValue.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mBinding.textViewExhibitPriceLowerValue.setText(s);
            }

            @Override
            public void afterTextChanged(Editable s) {

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

            }
        });

        mBinding.editTextTriggerPrice.setSelectAllOnFocus(true);
        mBinding.editTextTriggerRangeLargerValue.setSelectAllOnFocus(true);
        mBinding.editTextTriggerRangeLowerValue.setSelectAllOnFocus(true);
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
                                    mTime = TimeUtils.date2String(calendar.getTime(), YMD_FORMAT_4);

                                    if (mTimeDialog == null){
                                        mTimeDialog = com.wdullaer.materialdatetimepicker.time.TimePickerDialog.newInstance(
                                                new  com.wdullaer.materialdatetimepicker.time.TimePickerDialog.OnTimeSetListener(){

                                                    @Override
                                                    public void onTimeSet(com.wdullaer.materialdatetimepicker.time.TimePickerDialog view, int hourOfDay, int minute, int second) {
                                                        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                                                        calendar.set(Calendar.MINUTE, minute);
                                                        calendar.set(Calendar.SECOND, second);
                                                        mTime = mTime + " " + TimeUtils.date2String(calendar.getTime(), HMS_FORMAT);
                                                        mBinding.textViewTriggerTime.setText(mTime);
                                                        mBinding.textViewExhibitPrice.setText(mTime);
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
                String directionTitle = (String) mBinding.spinnerTriggerOrderDirection.getSelectedItem();
                switch (directionTitle){
                    case CONDITION_DIRECTION_BUY_TITLE:
                        direction = CONDITION_DIRECTION_BUY;
                        break;
                    case CONDITION_DIRECTION_SELL_TITLE:
                        direction = CONDITION_DIRECTION_SELL;
                        break;
                    default:
                        break;
                }
                String offset = "";
                String offsetTitle = (String) mBinding.spinnerTriggerOrderOffset.getSelectedItem();
                switch (offsetTitle){
                    case CONDITION_OFFSET_OPEN_TITLE:
                        offset = CONDITION_OFFSET_OPEN;
                        break;
                    case CONDITION_OFFSET_CLOSE_TITLE:
                        offset = CONDITION_OFFSET_CLOSE;
                        break;
                    case CONDITION_OFFSET_REVERSE_TITLE:
                        offset = CONDITION_OFFSET_REVERSE;
                        break;
                    default:
                        break;
                }

                String volumeType = "";
                String volumeTypeTitle = (String) mBinding.spinnerTriggerOrderVolume.getSelectedItem();
                switch (volumeTypeTitle){
                    case CONDITION_VOLUME_TYPE_CUSTOM:
                        volumeType = CONDITION_VOLUME_TYPE_NUM;
                        break;
                    case CONDITION_VOLUME_TYPE_ALL:
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
                    jsonObject.put(AMP_EVENT_CONDITION_DIRECTION, directionTitle);
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
                ReqConditionEntity conditionEntity = new ReqConditionEntity();
                conditionEntity.setExchange_id(exchange_id);
                conditionEntity.setInstrument_id(instrument_id);
                String triggerWay = (String) mBinding.spinnerTriggerWay.getSelectedItem();
                switch (triggerWay){
                    case CONDITION_WAY_PRICE:
                        contingentType = CONDITION_CONTINGENT_TYPE_PRICE;
                        float contingentPrice = 0;
                        String priceRelation = "";
                        String contingentPriceS = mBinding.editTextTriggerPrice.getText().toString();
                        try {
                            contingentPrice = Float.parseFloat(contingentPriceS);
                        }catch (NumberFormatException e){
                            ToastUtils.showToast(sContext, "触发价输入不合法");
                            isInsertOrder = false;
                        }
                        int index = mBinding.spinnerTriggerPriceRange.getSelectedItemPosition();
                        switch (index){
                            case 0:
                                priceRelation = CONDITION_CONTINGENT_PRICE_RELATION_GE;
                                break;
                            case 1:
                                priceRelation = CONDITION_CONTINGENT_PRICE_RELATION_LE;
                                break;
                            case 2:
                                priceRelation = CONDITION_CONTINGENT_PRICE_RELATION_G;
                                break;
                            case 3:
                                priceRelation = CONDITION_CONTINGENT_PRICE_RELATION_L;
                                break;
                            default:
                                break;

                        }
                        conditionEntity.setContingent_price(contingentPrice);
                        conditionEntity.setPrice_relation(priceRelation);
                        try {
                            jsonObject.put(AMP_EVENT_CONDITION_TYPE, AMP_EVENT_CONDITION_TYPE_VALUE_PRICE_TRIGGER);
                            String trigger_price = (String) mBinding.spinnerTriggerPriceRange.getSelectedItem();
                            jsonObject.put(AMP_EVENT_CONDITION_TRIGGER_PRICE, trigger_price + " " + contingentPriceS);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        break;
                    case CONDITION_WAY_TIME:
                        contingentType = CONDITION_CONTINGENT_TYPE_TIME;
                        long contingentTime = 0;
                        String contingentTimeS = mBinding.textViewTriggerTime.getText().toString();
                        Date date = TimeUtils.string2Date(contingentTimeS, YMD_HMS_FORMAT_4);
                        if (date != null) contingentTime = date.getTime() / 1000;
                        try {
                            GTD_date = Integer.parseInt(contingentTimeS.split(" ")[0]);
                        }catch (Exception e){
                            ToastUtils.showToast(sContext, "日期格式错误");
                            isInsertOrder = false;
                        }
                        conditionEntity.setContingent_time(contingentTime);
                        try {
                            jsonObject.put(AMP_EVENT_CONDITION_TYPE, AMP_EVENT_CONDITION_TYPE_VALUE_TIME_TRIGGER);
                            jsonObject.put(AMP_EVENT_CONDITION_TRIGGER_TIME, contingentTimeS);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        break;
                    case CONDITION_WAY_OPEN:
                        try {
                            jsonObject.put(AMP_EVENT_CONDITION_TYPE, AMP_EVENT_CONDITION_TYPE_VALUE_OPENING_TRIGGER);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        contingentType = CONDITION_CONTINGENT_TYPE_MARKET_OPEN;
                        break;
                    case CONDITION_WAY_RANGE:
                        contingentType = CONDITION_CONTINGENT_TYPE_PRICE_RANGE;
                        float contingentPriceRangeLeft = 0;
                        float contingentPriceRangeRight = 0;
                        String contingentPriceRangeLeftS = mBinding.editTextTriggerRangeLargerValue.getText().toString();
                        try {
                            contingentPriceRangeLeft = Float.parseFloat(contingentPriceRangeLeftS);
                        }catch (NumberFormatException e){
                            ToastUtils.showToast(sContext, "触发区间左边界输入不合法");
                            isInsertOrder = false;
                        }
                        String contingentPriceRangeRightS = mBinding.editTextTriggerRangeLowerValue.getText().toString();
                        try {
                            contingentPriceRangeRight = Float.parseFloat(contingentPriceRangeRightS);
                        }catch (NumberFormatException e){
                            ToastUtils.showToast(sContext, "触发区间左边界输入不合法");
                            isInsertOrder = false;
                        }
                        conditionEntity.setContingent_price_range_left(contingentPriceRangeLeft);
                        conditionEntity.setContingent_price_range_right(contingentPriceRangeRight);
                        try {
                            jsonObject.put(AMP_EVENT_CONDITION_TYPE, AMP_EVENT_CONDITION_TYPE_VALUE_PRICE_RANGE_TRIGGER);
                            String price_relation = getResources().getString(R.string.trigger_lower_equal);
                            price_relation = contingentPriceRangeLeftS + " " + price_relation + " 最新价 " + price_relation + " " + contingentPriceRangeRightS;
                            jsonObject.put(AMP_EVENT_CONDITION_TRIGGER_PRICE, price_relation);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        break;
                    default:
                        break;
                }
                conditionEntity.setContingent_type(contingentType);

                Amplitude.getInstance().logEventWrap(AMP_CONDITION_SAVE, jsonObject);

                if (!isInsertOrder)return;
                ReqConditionEntity conditionList[] = {conditionEntity};
                ReqConditionOrderEntity orderList[] = {orderEntity};
                BaseApplication.getmTDWebSocket().sendReqInsertConditionOrder(conditionList, orderList,
                        time_condition_type, GTD_date , is_cancel_ori_close_order, conditions_logic_oper);
            }
        });

        mBinding.textViewResponsibility.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mResponsibilityDialog == null){
                    mResponsibilityDialog = new Dialog(ConditionOrderActivity.this, R.style.AppTheme);
                    View view = View.inflate(ConditionOrderActivity.this, R.layout.view_dialog_condition_order_responsibility, null);
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
                if (!mResponsibilityDialog.isShowing()) mResponsibilityDialog.show();
            }
        });

        mBinding.textViewExhibitIns.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mOptionalDialog == null) {
                    //初始化自选合约弹出框
                    mOptionalDialog = new Dialog(ConditionOrderActivity.this, R.style.Theme_Light_Dialog);
                    View viewDialog = View.inflate(ConditionOrderActivity.this, R.layout.view_dialog_optional_quote, null);
                    Window dialogWindow = mOptionalDialog.getWindow();
                    if (dialogWindow != null) {
                        dialogWindow.getDecorView().setPadding(0, getToolBarHeight(ConditionOrderActivity.this), 0, 0);
                        WindowManager.LayoutParams lp = dialogWindow.getAttributes();
                        dialogWindow.setGravity(Gravity.TOP);
                        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
                        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
                        dialogWindow.setAttributes(lp);
                    }
                    mOptionalDialog.setContentView(viewDialog);
                    mOptionalDialogAdapter = new DialogAdapter(ConditionOrderActivity.this, new ArrayList<String>(), "");
                    mOptionalRecyclerView = viewDialog.findViewById(R.id.dialog_rv);
                    mOptionalRecyclerView.setLayoutManager(
                            new GridLayoutManager(ConditionOrderActivity.this, 3));
                    mOptionalRecyclerView.addItemDecoration(
                            new DividerGridItemDecorationUtils(ConditionOrderActivity.this, R.drawable.activity_optional_quote_dialog));
                    mOptionalRecyclerView.setAdapter(mOptionalDialogAdapter);

                    mOptionalRecyclerView.addOnItemTouchListener(
                            new SimpleRecyclerViewItemClickListener(mOptionalRecyclerView,
                                    new SimpleRecyclerViewItemClickListener.OnItemClickListener() {
                                        @Override
                                        public void onItemClick(View view, int position) {
                                            String ins = (String) view.getTag();
                                            setInstrumentName(ins);
                                            mOptionalDialog.dismiss();
                                        }

                                        @Override
                                        public void onItemLongClick(View view, int position) {

                                        }
                                    }));

                }
                List<String> list = LatestFileManager.readInsListFromFile();
                mOptionalDialogAdapter.updateList(list, mInstrumentId);
                if (!mOptionalDialog.isShowing()) mOptionalDialog.show();
            }
        });

        mBinding.imageViewSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ConditionOrderActivity.this, SearchActivity.class);
                intent.putExtra(SOURCE_ACTIVITY, SOURCE_ACTIVITY_CONDITION_ORDER);
                startActivityForResult(intent, CONDITION_ORDER_ACTIVITY_TO_SEARCH_ACTIVITY);
            }
        });
    }

    /**
     * date: 2019/8/6
     * author: chenli
     * description: 根据触发类型调整布局
     */
    private void adjustPriceLayout(){
        String priceType = (String) mBinding.spinnerTriggerWay.getSelectedItem();
        switch (priceType){
            case CONDITION_WAY_PRICE:
                mBinding.spinnerTriggerPriceRange.setVisibility(View.VISIBLE);
                mBinding.editTextTriggerPrice.setVisibility(View.VISIBLE);
                mBinding.editTextTriggerRangeLargerValue.setVisibility(View.INVISIBLE);
                mBinding.textViewTriggerPriceRange.setVisibility(View.INVISIBLE);
                mBinding.editTextTriggerRangeLowerValue.setVisibility(View.INVISIBLE);
                mBinding.textViewTriggerTime.setVisibility(View.INVISIBLE);
                mBinding.textViewTriggerPriceQuick.setVisibility(View.INVISIBLE);

                mBinding.textViewExhibitPriceLargerValue.setVisibility(View.GONE);
                mBinding.textViewExhibitPriceLarger.setVisibility(View.GONE);
                mBinding.textViewExhibitPriceLower.setVisibility(View.VISIBLE);
                mBinding.textViewExhibitPriceLowerValue.setVisibility(View.VISIBLE);
                mBinding.textViewExhibitPrice.setText(R.string.exhibit_last);
                String rangeType = (String) mBinding.spinnerTriggerPriceRange.getSelectedItem();
                mBinding.textViewExhibitPriceLower.setText(rangeType);
                String price = mBinding.editTextTriggerPrice.getText().toString();
                mBinding.textViewExhibitPriceLowerValue.setText(price);
                mBinding.editTextTriggerPrice.requestFocus();
                break;
            case CONDITION_WAY_TIME:
                mBinding.spinnerTriggerPriceRange.setVisibility(View.INVISIBLE);
                mBinding.editTextTriggerPrice.setVisibility(View.INVISIBLE);
                mBinding.editTextTriggerRangeLargerValue.setVisibility(View.INVISIBLE);
                mBinding.textViewTriggerPriceRange.setVisibility(View.INVISIBLE);
                mBinding.editTextTriggerRangeLowerValue.setVisibility(View.INVISIBLE);
                mBinding.textViewTriggerTime.setVisibility(View.VISIBLE);
                mBinding.textViewTriggerPriceQuick.setVisibility(View.INVISIBLE);

                mBinding.textViewExhibitPriceLargerValue.setVisibility(View.GONE);
                mBinding.textViewExhibitPriceLarger.setVisibility(View.GONE);
                mBinding.textViewExhibitPriceLower.setVisibility(View.GONE);
                mBinding.textViewExhibitPriceLowerValue.setVisibility(View.GONE);
                String time  = TimeUtils.date2String(Calendar.getInstance().getTime(), YMD_HMS_FORMAT_4);
                mBinding.textViewTriggerTime.setText(time);
                mBinding.textViewExhibitPrice.setText(time);
                break;
            case CONDITION_WAY_OPEN:
                mBinding.spinnerTriggerPriceRange.setVisibility(View.INVISIBLE);
                mBinding.editTextTriggerPrice.setVisibility(View.INVISIBLE);
                mBinding.editTextTriggerRangeLargerValue.setVisibility(View.INVISIBLE);
                mBinding.textViewTriggerPriceRange.setVisibility(View.INVISIBLE);
                mBinding.editTextTriggerRangeLowerValue.setVisibility(View.INVISIBLE);
                mBinding.textViewTriggerTime.setVisibility(View.INVISIBLE);
                mBinding.textViewTriggerPriceQuick.setVisibility(View.VISIBLE);

                mBinding.textViewExhibitPriceLargerValue.setVisibility(View.GONE);
                mBinding.textViewExhibitPriceLarger.setVisibility(View.GONE);
                mBinding.textViewExhibitPriceLower.setVisibility(View.GONE);
                mBinding.textViewExhibitPriceLowerValue.setVisibility(View.GONE);
                mBinding.textViewExhibitPrice.setText(R.string.trigger_price_quick);
                break;
            case CONDITION_WAY_RANGE:
                mBinding.spinnerTriggerPriceRange.setVisibility(View.INVISIBLE);
                mBinding.editTextTriggerPrice.setVisibility(View.INVISIBLE);
                mBinding.editTextTriggerRangeLargerValue.setVisibility(View.VISIBLE);
                mBinding.textViewTriggerPriceRange.setVisibility(View.VISIBLE);
                mBinding.editTextTriggerRangeLowerValue.setVisibility(View.VISIBLE);
                mBinding.textViewTriggerTime.setVisibility(View.INVISIBLE);
                mBinding.textViewTriggerPriceQuick.setVisibility(View.INVISIBLE);

                mBinding.textViewExhibitPriceLargerValue.setVisibility(View.VISIBLE);
                mBinding.textViewExhibitPriceLarger.setVisibility(View.VISIBLE);
                mBinding.textViewExhibitPriceLower.setVisibility(View.VISIBLE);
                mBinding.textViewExhibitPriceLowerValue.setVisibility(View.VISIBLE);
                String priceLarger = mBinding.editTextTriggerRangeLargerValue.getText().toString();
                mBinding.textViewExhibitPriceLargerValue.setText(priceLarger);
                mBinding.textViewExhibitPriceLarger.setText(R.string.exhibit_range);
                mBinding.textViewExhibitPrice.setText(R.string.exhibit_last);
                mBinding.textViewExhibitPriceLower.setText(R.string.exhibit_range);
                String priceLower = mBinding.editTextTriggerRangeLowerValue.getText().toString();
                mBinding.textViewExhibitPriceLowerValue.setText(priceLower);
                mBinding.editTextTriggerRangeLowerValue.requestFocus();
                break;
            default:
                break;
        }
    }

    /**
     * date: 2019/8/13
     * author: chenli
     * description: 筛选触发报单价、报单量类型
     */
    private void filterPriceTypeValues(){
        String direction = (String) mBinding.spinnerTriggerOrderOffset.getSelectedItem();
        String priceType = (String) mBinding.spinnerTriggerWay.getSelectedItem();
        switch (priceType){
            case CONDITION_WAY_PRICE:
                mTriggerOrderPriceSpinnerAdapter.clear();
                mTriggerOrderPriceSpinnerAdapter.addAll(mOrderPriceValues);
                mTriggerOrderPriceSpinnerAdapter.notifyDataSetChanged();
                mBinding.textTextTriggerPrice.setText(CONDITION_CONTINGENT_PRICE);
                break;
            case CONDITION_WAY_TIME:
                mTriggerOrderPriceSpinnerAdapter.clear();
                mTriggerOrderPriceSpinnerAdapter.addAll(mOrderPriceValuesWithoutContingent);
                mTriggerOrderPriceSpinnerAdapter.notifyDataSetChanged();
                mBinding.textTextTriggerPrice.setText(CONDITION_CONTINGENT_TIME);
                break;
            case CONDITION_WAY_OPEN:
                mTriggerOrderPriceSpinnerAdapter.clear();
                mTriggerOrderPriceSpinnerAdapter.addAll(mOrderPriceValuesWithoutContingent);
                mTriggerOrderPriceSpinnerAdapter.notifyDataSetChanged();
                mBinding.textTextTriggerPrice.setText(CONDITION_CONTINGENT_TIME);
                break;
            case CONDITION_WAY_RANGE:
                mTriggerOrderPriceSpinnerAdapter.clear();
                mTriggerOrderPriceSpinnerAdapter.addAll(mOrderPriceValuesWithoutContingent);
                mTriggerOrderPriceSpinnerAdapter.notifyDataSetChanged();
                mBinding.textTextTriggerPrice.setText(CONDITION_CONTINGENT_PRICE);
                break;
            default:
                break;
        }
        switch (direction){
            case CONDITION_OFFSET_OPEN_TITLE:
                mTriggerOrderVolumeSpinnerAdapter.clear();
                mTriggerOrderVolumeSpinnerAdapter.addAll(mOrderVolumeValuesOnlyCustom);
                mTriggerOrderVolumeSpinnerAdapter.notifyDataSetChanged();
                mBinding.editTextTriggerOrderVolume.setText(mInitOrderVolume);
                mBinding.editTextTriggerOrderVolume.setEnabled(true);
                mBinding.editTextTriggerOrderPrice.setEnabled(true);
                break;
            case CONDITION_OFFSET_CLOSE_TITLE:
                mTriggerOrderVolumeSpinnerAdapter.clear();
                mTriggerOrderVolumeSpinnerAdapter.addAll(mOrderVolumeValues);
                mTriggerOrderVolumeSpinnerAdapter.notifyDataSetChanged();
                mBinding.editTextTriggerOrderVolume.setText(mInitOrderVolume);
                mBinding.editTextTriggerOrderVolume.setEnabled(true);
                mBinding.editTextTriggerOrderPrice.setEnabled(true);
                break;
            case CONDITION_OFFSET_REVERSE_TITLE:
                mTriggerOrderPriceSpinnerAdapter.clear();
                mTriggerOrderPriceSpinnerAdapter.addAll(mOrderPriceValuesOnlyMarket);
                mTriggerOrderPriceSpinnerAdapter.notifyDataSetChanged();
                mTriggerOrderVolumeSpinnerAdapter.clear();
                mTriggerOrderVolumeSpinnerAdapter.addAll(mOrderVolumeValuesOnlyAll);
                mTriggerOrderVolumeSpinnerAdapter.notifyDataSetChanged();
                mBinding.editTextTriggerOrderVolume.setText(CONDITION_VOLUME_TYPE_ALL);
                mBinding.editTextTriggerOrderVolume.setEnabled(false);
                mBinding.editTextTriggerOrderPrice.setText(CONDITION_PRICE_TYPE_MARKET_TITLE);
                mBinding.editTextTriggerOrderPrice.setEnabled(false);
                break;
            default:
                break;
        }
        mBinding.editTextTriggerOrderPrice.clearFocus();
        mBinding.editTextTriggerOrderVolume.clearFocus();
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
     * date: 2019/8/10
     * author: chenli
     * description: 获取标题栏高度
     */
    private int getToolBarHeight(Activity activity) {
        TypedValue tv = new TypedValue();
        if (activity.getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true)) {
            return TypedValue.complexToDimensionPixelSize(tv.data, activity.getResources().getDisplayMetrics());
        }
        return ScreenUtils.dp2px(sContext, 56);
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CONDITION_ORDER_ACTIVITY_TO_SEARCH_ACTIVITY){
            String ins = data.getStringExtra(INS_BETWEEN_ACTIVITY);
            setInstrumentName(ins);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mReceiverMarket != null)LocalBroadcastManager.getInstance(this).unregisterReceiver(mReceiverMarket);
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
                Intent intent = new Intent(ConditionOrderActivity.this,
                        ManagerConditionOrderActivity.class);
                ConditionOrderActivity.this.startActivity(intent);
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

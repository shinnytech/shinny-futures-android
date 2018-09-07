package com.shinnytech.futures.presenter;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.PopupWindow;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.TextView;

import com.shinnytech.futures.R;
import com.shinnytech.futures.constants.CommonConstants;
import com.shinnytech.futures.databinding.ActivityFutureInfoBinding;
import com.shinnytech.futures.model.bean.eventbusbean.IdEvent;
import com.shinnytech.futures.model.bean.eventbusbean.SetUpEvent;
import com.shinnytech.futures.model.bean.searchinfobean.SearchEntity;
import com.shinnytech.futures.model.engine.DataManager;
import com.shinnytech.futures.model.engine.LatestFileManager;
import com.shinnytech.futures.utils.DividerGridItemDecorationUtils;
import com.shinnytech.futures.utils.KeyboardUtils;
import com.shinnytech.futures.utils.NetworkUtils;
import com.shinnytech.futures.utils.SPUtils;
import com.shinnytech.futures.view.activity.FutureInfoActivity;
import com.shinnytech.futures.view.activity.LoginActivity;
import com.shinnytech.futures.view.adapter.DialogAdapter;
import com.shinnytech.futures.view.adapter.ViewPagerFragmentAdapter;
import com.shinnytech.futures.view.fragment.CurrentDayFragment;
import com.shinnytech.futures.view.fragment.HandicapFragment;
import com.shinnytech.futures.view.fragment.KlineFragment;
import com.shinnytech.futures.view.fragment.OrderFragment;
import com.shinnytech.futures.view.fragment.PositionFragment;
import com.shinnytech.futures.view.fragment.TransactionFragment;
import com.shinnytech.futures.view.listener.SimpleRecyclerViewItemClickListener;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

import static com.shinnytech.futures.constants.CommonConstants.ACTIVITY_TYPE;
import static com.shinnytech.futures.constants.CommonConstants.CURRENT_DAY_FRAGMENT;
import static com.shinnytech.futures.constants.CommonConstants.DAY_FRAGMENT;
import static com.shinnytech.futures.constants.CommonConstants.HOUR_FRAGMENT;
import static com.shinnytech.futures.constants.CommonConstants.KLINE_DAY;
import static com.shinnytech.futures.constants.CommonConstants.KLINE_HOUR;
import static com.shinnytech.futures.constants.CommonConstants.KLINE_MINUTE;
import static com.shinnytech.futures.constants.CommonConstants.MINUTE_FRAGMENT;
import static com.shinnytech.futures.constants.CommonConstants.ORDER_JUMP_TO_LOG_IN_ACTIVITY;
import static com.shinnytech.futures.constants.CommonConstants.POSITION_JUMP_TO_LOG_IN_ACTIVITY;
import static com.shinnytech.futures.constants.CommonConstants.TRANSACTION_JUMP_TO_LOG_IN_ACTIVITY;
import static com.shinnytech.futures.model.receiver.NetworkReceiver.NETWORK_STATE;

/**
 * Created on 1/17/18.
 * Created by chenli.
 * Description: .
 */

public class FutureInfoActivityPresenter {
    private final ViewPagerFragmentAdapter mInfoPagerAdapter;
    /**
     * date: 7/7/17
     * description: 合约代码
     */
    private String mInstrumentId;
    /**
     * date: 7/7/17
     * description: “设置”弹出框
     */
    private PopupWindow mPopupWindow;
    /**
     * date: 7/7/17
     * description: 持仓开关
     */
    private Switch mPosition;
    /**
     * date: 7/7/17
     * description: 挂单开关
     */
    private Switch mPending;
    /**
     * date: 7/7/17
     * description: 均线开关
     */
    private Switch mAverage;
    /**
     * date: 7/3/17
     * description: 持仓开关状态
     */
    private boolean mIsPosition;
    /**
     * date: 7/7/17
     * description: 挂单开关状态
     */
    private boolean mIsPending;
    /**
     * date: 7/7/17
     * description: 均线开关状态
     */
    private boolean mIsAverage;
    private ActivityFutureInfoBinding mBinding;
    private FutureInfoActivity mFutureInfoActivity;
    private Context sContext;
    private Toolbar mToolbar;
    private TextView mToolbarTitle;
    private FragmentManager mFragmentManager;
    private Fragment mCurFragment;
    private Drawable mRightDrawable;
    private int mNav_position = 0;
    private Dialog mDialog;
    private RecyclerView mRecyclerView;
    private DialogAdapter mDialogAdapter;
    private BroadcastReceiver mReceiver;

    public FutureInfoActivityPresenter(FutureInfoActivity futureInfoActivity, Context context, ActivityFutureInfoBinding binding, Toolbar toolbar, TextView toolbarTitle) {
        this.mBinding = binding;
        this.mFutureInfoActivity = futureInfoActivity;
        this.mToolbar = toolbar;
        this.mToolbarTitle = toolbarTitle;
        this.sContext = context;

        mFragmentManager = mFutureInfoActivity.getSupportFragmentManager();
        if (mCurFragment == null) {
            Fragment currentDayFragment = new CurrentDayFragment();
            mFragmentManager.beginTransaction().
                    setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN).
                    add(R.id.fl_content_up, currentDayFragment, CommonConstants.CURRENT_DAY_FRAGMENT).commit();
            mCurFragment = currentDayFragment;
        }

        Intent intent = mFutureInfoActivity.getIntent();
        mInstrumentId = intent.getStringExtra("instrument_id");
        mNav_position = intent.getIntExtra("nav_position", 0);
        SearchEntity searchEntity = LatestFileManager.getSearchEntities().get(mInstrumentId);
        if (searchEntity != null) mToolbarTitle.setText(searchEntity.getInstrumentName());
        else mToolbarTitle.setText(mInstrumentId);
        mToolbar.setTitle("合约:");
        mToolbar.setTitleTextAppearance(mFutureInfoActivity, R.style.toolBarTitle);
        mToolbar.setTitleMarginStart(120);
        mToolbarTitle.setBackgroundColor(ContextCompat.getColor(sContext, R.color.title));
        mToolbarTitle.setPadding(20, 0, 0, 0);
        mToolbarTitle.setTextSize(20);
        mRightDrawable = ContextCompat.getDrawable(mFutureInfoActivity, R.mipmap.ic_expand_more_white_36dp);
        if (mRightDrawable != null)
            mRightDrawable.setBounds(0, 0, mRightDrawable.getMinimumWidth(), mRightDrawable.getMinimumHeight());
        mToolbarTitle.setCompoundDrawables(null, null, mRightDrawable, null);

        //初始化开关状态
        if (SPUtils.contains(sContext, "isPosition")) {
            mIsPosition = (boolean) SPUtils.get(sContext, "isPosition", false);
        }
        if (SPUtils.contains(sContext, "isPending")) {
            mIsPending = (boolean) SPUtils.get(sContext, "isPending", false);
        }
        if (SPUtils.contains(sContext, "isAverage")) {
            mIsAverage = (boolean) SPUtils.get(sContext, "isAverage", false);
        }

        //初始化盘口、持仓、挂单、交易切换容器，fragment实例保存，有生命周期的变化，默认情况下屏幕外初始化两个fragment
        List<Fragment> fragmentList = new ArrayList<>();
        PositionFragment positionFragment = new PositionFragment();
        OrderFragment orderFragment = new OrderFragment();
        HandicapFragment handicapFragment = new HandicapFragment();
        TransactionFragment transactionFragment = new TransactionFragment();
        fragmentList.add(handicapFragment);
        fragmentList.add(positionFragment);
        fragmentList.add(orderFragment);
        fragmentList.add(transactionFragment);
        mInfoPagerAdapter = new ViewPagerFragmentAdapter(mFutureInfoActivity.getSupportFragmentManager(), fragmentList);
        mBinding.vpInfoContent.setAdapter(mInfoPagerAdapter);
        //设置初始化页为盘口页，去除滑动效果
        if (mNav_position == 1) {
            mBinding.vpInfoContent.setCurrentItem(1, false);
            mBinding.rbPositionInfo.setChecked(true);
        } else {
            mBinding.vpInfoContent.setCurrentItem(0, false);
            mBinding.rbHandicapInfo.setChecked(true);
        }
        //由于盘口页和交易页需要通过eventBus实时监听合约代码的改变，当通过toolbar改变合约时，由于默认viewPager保存屏幕外一个页面，
        //盘口页和交易页相差两个页面，所以当显示其中一个的时候，另一个一定会消亡，所以打开时会初始化得到活动的最新合约代码。但是当通过
        //持仓页改变合约代码时会直接跳转到交易页，这个过程和活动更新合约代码同时发生，所以交易页通过初始化可能得不到最新合约代码， 必须
        //通过eventBus实时监控才行，所以要保持页面实例从而可以调用onEvent()方法
        mBinding.vpInfoContent.setOffscreenPageLimit(3);
    }

    public void registerListeners() {
        //为toolbar设置一个弹出框，用于显示自选合约列表，点击切换合约信息
        mToolbarTitle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mDialog == null) {
                    //初始化自选合约弹出框
                    mDialog = new Dialog(mFutureInfoActivity, R.style.Theme_Light_Dialog);
                    View viewDialog = View.inflate(mFutureInfoActivity, R.layout.view_dialog_optional_quote, null);
                    Window dialogWindow = mDialog.getWindow();
                    if (dialogWindow != null) {
                        dialogWindow.getDecorView().setPadding(0, 0, 0, 0);
                        WindowManager.LayoutParams lp = dialogWindow.getAttributes();
                        dialogWindow.setGravity(Gravity.BOTTOM);
                        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
                        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
                        dialogWindow.setAttributes(lp);
                    }
                    mDialog.setContentView(viewDialog);
                    mDialogAdapter = new DialogAdapter(mFutureInfoActivity, new ArrayList<>(LatestFileManager.getOptionalInsList().keySet()));
                    mRecyclerView = viewDialog.findViewById(R.id.dialog_rv);
                    mRecyclerView.setLayoutManager(
                            new GridLayoutManager(mFutureInfoActivity, 3));
                    mRecyclerView.addItemDecoration(
                            new DividerGridItemDecorationUtils(mFutureInfoActivity));
                    mRecyclerView.setAdapter(mDialogAdapter);

                    mRecyclerView.addOnItemTouchListener(new SimpleRecyclerViewItemClickListener(mRecyclerView, new SimpleRecyclerViewItemClickListener.OnItemClickListener() {
                        @Override
                        public void onItemClick(View view, int position) {
                            String instrumentId = (String) view.getTag();
                            //添加判断，防止自选合约列表为空时产生无效的点击事件
                            if (instrumentId != null) {
                                if (!instrumentId.isEmpty()) {
                                    IdEvent idEvent = new IdEvent();
                                    idEvent.setInstrument_id(instrumentId);
                                    EventBus.getDefault().post(idEvent);
                                }
                            }
                            mDialog.dismiss();
                        }

                        @Override
                        public void onItemLongClick(View view, int position) {

                        }
                    }));

                }
                if (!mDialog.isShowing()) mDialog.show();
            }
        });

        //图表切换容器监听器
        mBinding.rgTabUp.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.rb_current_day_up:
                        switchUpFragment(CURRENT_DAY_FRAGMENT);
                        break;
                    case R.id.rb_day_up:
                        switchUpFragment(DAY_FRAGMENT);
                        break;
                    case R.id.rb_hour_up:
                        switchUpFragment(HOUR_FRAGMENT);
                        break;
                    case R.id.rb_minute_up:
                        switchUpFragment(MINUTE_FRAGMENT);
                        break;
                    default:
                        break;
                }
            }
        });

        //监听“设置”按钮，弹出一个popup对话框
        mBinding.rbSetUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mPopupWindow == null) {
                    //构造一个“设置”按钮的PopupWindow
                    View view = View.inflate(sContext, R.layout.popup_set_up, null);
                    mPosition = view.findViewById(R.id.position);
                    mPending = view.findViewById(R.id.pending);
                    mAverage = view.findViewById(R.id.average_line);
                    mPopupWindow = new PopupWindow(view,
                            ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
                    //点击空白处popupWindow消失
                    mPopupWindow.setBackgroundDrawable(new ColorDrawable(0x00000000));
                    //设置动画，左进右出
                    mPopupWindow.setAnimationStyle(R.style.anim_menu_set_up);

                    //初始化开关状态
                    mPosition.setChecked(mIsPosition);
                    mPending.setChecked(mIsPending);
                    mAverage.setChecked(mIsAverage);

                    mPosition.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                            SPUtils.putAndApply(sContext, "isPosition", isChecked);
                            mIsPosition = isChecked;
                            SetUpEvent setUpEvent = new SetUpEvent();
                            setUpEvent.setPosition(mIsPosition);
                            setUpEvent.setPending(mIsPending);
                            setUpEvent.setAverage(mIsAverage);
                            EventBus.getDefault().post(setUpEvent);
                        }
                    });

                    mPending.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                            SPUtils.putAndApply(sContext, "isPending", isChecked);
                            mIsPending = isChecked;
                            SetUpEvent setUpEvent = new SetUpEvent();
                            setUpEvent.setPending(mIsPending);
                            setUpEvent.setPosition(mIsPosition);
                            setUpEvent.setAverage(mIsAverage);
                            EventBus.getDefault().post(setUpEvent);
                        }
                    });

                    mAverage.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                            SPUtils.putAndApply(sContext, "isAverage", isChecked);
                            mIsAverage = isChecked;
                            SetUpEvent setUpEvent = new SetUpEvent();
                            setUpEvent.setAverage(mIsAverage);
                            setUpEvent.setPending(mIsPending);
                            setUpEvent.setPosition(mIsPosition);
                            EventBus.getDefault().post(setUpEvent);
                        }
                    });
                }
                //设置popupWindow显示的位置，参数依次是参照View，x轴的偏移量，y轴的偏移量
                mPopupWindow.showAsDropDown(v, -20, 0);
            }
        });

        //盘口、持仓、挂单、交易切换容器监听器，滑动改变页面内容时联动导航状态
        mBinding.vpInfoContent.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                //滑动完成
                switch (position) {
                    case 0:
                        mBinding.rbHandicapInfo.setChecked(true);
                        closeKeyboard();
                        break;
                    case 1:
                        mBinding.rbPositionInfo.setChecked(true);
                        closeKeyboard();
                        break;
                    case 2:
                        mBinding.rbOrderInfo.setChecked(true);
                        closeKeyboard();
                        break;
                    case 3:
                        mBinding.rbTransactionInfo.setChecked(true);
                        break;
                    default:
                        break;
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });

        //RadioGroup点击的时让viewpager跟着切换
        mBinding.rgTabInfo.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int checkId) {
                switch (checkId) {
                    case R.id.rb_handicap_info:
                        mBinding.vpInfoContent.setCurrentItem(0, false);
                        break;
                    case R.id.rb_position_info:
                        if (!DataManager.getInstance().IS_LOGIN) {
                            Intent intent = new Intent(mFutureInfoActivity, LoginActivity.class);
                            //判断从哪个页面跳到登录页，登录页的销毁方式不一样
                            intent.putExtra(ACTIVITY_TYPE, "FutureInfoActivity");
                            mFutureInfoActivity.startActivityForResult(intent, POSITION_JUMP_TO_LOG_IN_ACTIVITY);
                            break;
                        }
                        mBinding.vpInfoContent.setCurrentItem(1, false);
                        break;
                    case R.id.rb_order_info:
                        if (!DataManager.getInstance().IS_LOGIN) {
                            Intent intent = new Intent(mFutureInfoActivity, LoginActivity.class);
                            intent.putExtra(ACTIVITY_TYPE, "FutureInfoActivity");
                            mFutureInfoActivity.startActivityForResult(intent, ORDER_JUMP_TO_LOG_IN_ACTIVITY);
                            break;
                        }
                        mBinding.vpInfoContent.setCurrentItem(2, false);
                        break;
                    case R.id.rb_transaction_info:
                        if (!DataManager.getInstance().IS_LOGIN) {
                            Intent intent = new Intent(mFutureInfoActivity, LoginActivity.class);
                            intent.putExtra(ACTIVITY_TYPE, "FutureInfoActivity");
                            mFutureInfoActivity.startActivityForResult(intent, TRANSACTION_JUMP_TO_LOG_IN_ACTIVITY);
                            break;
                        }
                        mBinding.vpInfoContent.setCurrentItem(3, false);
                        break;
                    default:
                        break;
                }
            }
        });

    }

    /**
     * date: 1/18/18
     * author: chenli
     * description: 当从持仓菜单进入本页时判断登录状态
     */
    public void checkLoginState() {
        if (mNav_position == 1 && !DataManager.getInstance().IS_LOGIN) {
            Intent intent = new Intent(mFutureInfoActivity, LoginActivity.class);
            //判断从哪个页面跳到登录页，登录页的销毁方式不一样
            intent.putExtra(ACTIVITY_TYPE, "FutureInfoActivity");
            mFutureInfoActivity.startActivity(intent);
            mNav_position = 2;
        } else if (mNav_position == 2 && !DataManager.getInstance().IS_LOGIN)
            mFutureInfoActivity.finish();
    }

    /**
     * date: 7/7/17
     * author: chenli
     * description: 注册网络状态广播监听器，检测手机网络状态，断网状态下在toolbar上提示用户网络已断开
     */
    public void registerBroaderCast() {
        mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                int networkStatus = intent.getIntExtra("networkStatus", 0);
                switch (networkStatus) {
                    case 0:
                        mToolbar.setBackgroundColor(ContextCompat.getColor(context, R.color.off_line));
                        mToolbarTitle.setTextColor(Color.BLACK);
                        mToolbarTitle.setText("交易、行情网络未连接！");
                        mToolbarTitle.setCompoundDrawables(null, null, null, null);
                        mToolbarTitle.setBackgroundColor(ContextCompat.getColor(context, R.color.off_line));
                        break;
                    case 1:
                        mToolbar.setBackgroundColor(ContextCompat.getColor(context, R.color.black_dark));
                        mToolbarTitle.setTextColor(Color.WHITE);
                        SearchEntity searchEntity = LatestFileManager.getSearchEntities().get(mInstrumentId);
                        if (searchEntity != null)
                            mToolbarTitle.setText(searchEntity.getInstrumentName());
                        else mToolbarTitle.setText(mInstrumentId);
                        mToolbarTitle.setCompoundDrawables(null, null, mRightDrawable, null);
                        mToolbarTitle.setBackgroundColor(ContextCompat.getColor(context, R.color.title));
                        break;
                }
            }
        };
        mFutureInfoActivity.registerReceiver(mReceiver, new IntentFilter(NETWORK_STATE));
    }

    /**
     * date: 7/7/17
     * author: chenli
     * description: 注销广播
     */
    public void unRegisterBroaderCast() {
        if (mReceiver != null) mFutureInfoActivity.unregisterReceiver(mReceiver);
    }

    public void refreshOptionalQuotesPopup(ArrayList arrayList) {
        if (mDialogAdapter != null) mDialogAdapter.updateList(arrayList);
    }

    public boolean closeKeyboard() {
        TransactionFragment transactionFragment = ((TransactionFragment) mInfoPagerAdapter.getItem(3));
        KeyboardUtils keyboardUtilsPrice = transactionFragment.getKeyboardUtilsPrice();
        KeyboardUtils keyboardUtilsVolume = transactionFragment.getKeyboardUtilsVolume();
        if (keyboardUtilsPrice != null) {
            if (keyboardUtilsPrice.isVisible()) {
                keyboardUtilsPrice.hideKeyboard();
                return true;
            }
        }
        if (keyboardUtilsVolume != null) {
            if (keyboardUtilsVolume.isVisible()) {
                keyboardUtilsVolume.hideKeyboard();
                return true;
            }
        }
        return false;
    }

    /**
     * date: 7/9/17
     * author: chenli
     * description: 检查网络状态，更新toolbar的显示
     */
    public void updateToolbarFromNetwork() {
        if (NetworkUtils.isNetworkConnected(sContext)) {
            mToolbar.setBackgroundColor(ContextCompat.getColor(sContext, R.color.black_dark));
            mToolbarTitle.setTextColor(Color.WHITE);
            SearchEntity searchEntity = LatestFileManager.getSearchEntities().get(mInstrumentId);
            if (searchEntity != null) mToolbarTitle.setText(searchEntity.getInstrumentName());
            else mToolbarTitle.setText(mInstrumentId);
            mToolbarTitle.setCompoundDrawables(null, null, mRightDrawable, null);
            mToolbarTitle.setBackgroundColor(ContextCompat.getColor(sContext, R.color.title));
        } else {
            mToolbar.setBackgroundColor(ContextCompat.getColor(sContext, R.color.off_line));
            mToolbarTitle.setTextColor(Color.BLACK);
            mToolbarTitle.setText("交易、行情网络未连接！");
            mToolbarTitle.setCompoundDrawables(null, null, null, null);
            mToolbarTitle.setBackgroundColor(ContextCompat.getColor(sContext, R.color.off_line));
        }
    }

    public boolean isPosition() {
        return mIsPosition;
    }

    public boolean isPending() {
        return mIsPending;
    }

    public boolean isAverage() {
        return mIsAverage;
    }

    public String getInstrumentId() {
        return mInstrumentId;
    }

    /**
     * date: 7/7/17
     * author: chenli
     * description: 用于切换图表页，由于性能影响，不保存图表页实例
     */
    private void switchUpFragment(String title) {
        FragmentTransaction transaction = mFragmentManager.beginTransaction();
        Fragment fragment = mFragmentManager.findFragmentByTag(title);
        if (fragment == null) {
            transaction.hide(mCurFragment);
            fragment = createFragmentByTitle(title);
            transaction.add(R.id.fl_content_up, fragment, title);
            mCurFragment = fragment;
        } else if (fragment != mCurFragment) {
            transaction.hide(mCurFragment).show(fragment);
            mCurFragment = fragment;
        }
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN).
                commit();

    }

    /**
     * date: 7/7/17
     * author: chenli
     * description: 根据title返回对应的fragment
     */
    private Fragment createFragmentByTitle(String title) {
        switch (title) {
            case CURRENT_DAY_FRAGMENT:
                return new CurrentDayFragment();
            case DAY_FRAGMENT:
                return KlineFragment.newInstance("yy/MM/dd", KLINE_DAY);
            case HOUR_FRAGMENT:
                return KlineFragment.newInstance("dd/HH:mm", KLINE_HOUR);
            case MINUTE_FRAGMENT:
                return KlineFragment.newInstance("dd/HH:mm", KLINE_MINUTE);
            default:
                return null;
        }
    }
}

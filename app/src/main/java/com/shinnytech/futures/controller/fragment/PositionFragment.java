package com.shinnytech.futures.controller.fragment;

import android.content.Context;
import android.content.Intent;
import androidx.databinding.DataBindingUtil;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.shinnytech.futures.R;
import com.shinnytech.futures.amplitude.api.Amplitude;
import com.shinnytech.futures.application.BaseApplication;
import com.shinnytech.futures.controller.activity.MainActivity;
import com.shinnytech.futures.controller.activity.MainActivityPresenter;
import com.shinnytech.futures.controller.activity.ManagerConditionOrderActivity;
import com.shinnytech.futures.controller.activity.StopLossTakeProfitActivity;
import com.shinnytech.futures.databinding.FragmentPositionBinding;
import com.shinnytech.futures.model.adapter.PositionAdapter;
import com.shinnytech.futures.model.bean.accountinfobean.PositionEntity;
import com.shinnytech.futures.model.bean.accountinfobean.UserEntity;
import com.shinnytech.futures.model.bean.eventbusbean.SwitchInsEvent;
import com.shinnytech.futures.model.engine.DataManager;
import com.shinnytech.futures.model.listener.PositionDiffCallback;
import com.shinnytech.futures.model.listener.SimpleRecyclerViewItemClickListener;
import com.shinnytech.futures.utils.CloneUtils;
import com.shinnytech.futures.utils.DividerItemDecorationUtils;
import com.shinnytech.futures.utils.SPUtils;
import com.shinnytech.futures.utils.ScreenUtils;
import com.shinnytech.futures.utils.TDUtils;
import com.shinnytech.futures.utils.ToastUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.shinnytech.futures.constants.AmpConstants.AMP_CONDITION_POSITION;
import static com.shinnytech.futures.constants.AmpConstants.AMP_CONDITION_STOP_LOSS;
import static com.shinnytech.futures.constants.SettingConstants.CONFIG_PASSWORD;
import static com.shinnytech.futures.constants.CommonConstants.DIRECTION_BETWEEN_ACTIVITY;
import static com.shinnytech.futures.constants.TradeConstants.DIRECTION_BUY;
import static com.shinnytech.futures.constants.TradeConstants.DIRECTION_BUY_ZN;
import static com.shinnytech.futures.constants.TradeConstants.DIRECTION_SELL;
import static com.shinnytech.futures.constants.TradeConstants.DIRECTION_SELL_ZN;
import static com.shinnytech.futures.constants.CommonConstants.INS_BETWEEN_ACTIVITY;
import static com.shinnytech.futures.constants.CommonConstants.VOLUME_BETWEEN_ACTIVITY;

/**
 * date: 5/10/17
 * author: chenli
 * description: 持仓页
 * version:
 * state: done
 */
public class PositionFragment extends LazyLoadFragment {
    private static final String KEY_FRAGMENT_TYPE = "isInAccountFragment";
    protected DataManager sDataManager = DataManager.getInstance();
    private PositionAdapter mAdapter;
    private List<PositionEntity> mOldData = new ArrayList<>();
    private List<PositionEntity> mNewData = new ArrayList<>();
    private FragmentPositionBinding mBinding;
    private boolean mIsUpdate;
    private String mInstrumentId;
    private boolean mIsInAccountFragment;
    private View mView;

    public static PositionFragment newInstance(boolean isInAccountFragment) {
        PositionFragment fragment = new PositionFragment();
        Bundle bundle = new Bundle();
        bundle.putBoolean(KEY_FRAGMENT_TYPE, isInAccountFragment);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mIsInAccountFragment = getArguments().getBoolean(KEY_FRAGMENT_TYPE);
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_position, container, false);
        initData();
        initEvent();
        mView = mBinding.getRoot();
        return mView;
    }

    protected void initData() {
        mIsUpdate = true;
        mBinding.rv.setLayoutManager(
                new LinearLayoutManager(getActivity(), RecyclerView.VERTICAL, false));
        mBinding.rv.addItemDecoration(
                new DividerItemDecorationUtils(getActivity(), DividerItemDecorationUtils.VERTICAL_LIST));
        mAdapter = new PositionAdapter(getActivity(), mOldData);
        mAdapter.setHighlightIns(mInstrumentId);
        mBinding.rv.setAdapter(mAdapter);
        if (!mIsInAccountFragment) EventBus.getDefault().register(this);
    }

    protected void initEvent() {
        //recyclerView点击事件监听器，点击改变合约代码，并跳转到交易页
        mBinding.rv.addOnItemTouchListener(new SimpleRecyclerViewItemClickListener(mBinding.rv,
                new SimpleRecyclerViewItemClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        if (position >= 0 && position < mAdapter.getItemCount()) {
                            PositionEntity positionEntity = mAdapter.getData().get(position);
                            if (positionEntity == null) return;
                            sDataManager.POSITION_DIRECTION = ((TextView) view.findViewById(R.id.position_direction))
                                    .getText().toString();
                            String instrument_id = positionEntity.getExchange_id() + "." + positionEntity.getInstrument_id();
                            MainActivity mainActivity = (MainActivity) getActivity();
                            MainActivityPresenter mainActivityPresenter = mainActivity.getmMainActivityPresenter();
                            if (mIsInAccountFragment) {
                                sDataManager.IS_SHOW_VP_CONTENT = true;
                                mainActivityPresenter.switchToFutureInfo(instrument_id);
                            } else {
                                SwitchInsEvent switchInsEvent = new SwitchInsEvent();
                                switchInsEvent.setInstrument_id(instrument_id);
                                EventBus.getDefault().post(switchInsEvent);
                                mAdapter.updateHighlightIns(instrument_id);
                                FutureInfoFragment futureInfoFragment = (FutureInfoFragment) mainActivityPresenter.getmViewPagerFragmentAdapter().getItem(2);
                                futureInfoFragment.getmBinding().vpInfoContent.setCurrentItem(3, false);
                            }
                        }
                    }

                    @Override
                    public void onItemLongClick(View view, int position) {
                        if (position >= 0 && position < mAdapter.getItemCount()) {
                            PositionEntity positionEntity = mAdapter.getData().get(position);
                            if (positionEntity == null) return;
                            String instrument_id = positionEntity.getExchange_id() + "." + positionEntity.getInstrument_id();
                            TextView direction = view.findViewById(R.id.position_direction);
                            TextView volume = view.findViewById(R.id.position_volume);
                            initPopUp(view, instrument_id, direction.getText().toString(),
                                    volume.getText().toString());
                        }
                    }
                }));


        mBinding.rv.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                switch (newState) {
                    case RecyclerView.SCROLL_STATE_IDLE:
                        mIsUpdate = true;
                        break;
                    case RecyclerView.SCROLL_STATE_DRAGGING:
                        mIsUpdate = false;
                        break;
                    case RecyclerView.SCROLL_STATE_SETTLING:
                        mIsUpdate = false;
                        break;
                }
            }
        });

        mBinding.seeMd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mIsInAccountFragment) {
                    MainActivity mainActivity = ((MainActivity) getActivity());
                    mainActivity.getmMainActivityPresenter().getmBinding().bottomNavigation.setSelectedItemId(R.id.market);
                    QuotePagerFragment quotePagerFragment = (QuotePagerFragment) mainActivity.getmMainActivityPresenter().getmViewPagerFragmentAdapter().getItem(0);
                    quotePagerFragment.setCurrentItem(0);
                }
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (!mIsInAccountFragment) EventBus.getDefault().unregister(this);
    }

    @Override
    public void show() {
        refreshTD();
    }

    @Override
    public void leave() {
    }

    @Override
    public void refreshMD() {

    }

    @Override
    public void refreshTD() {
        try {
            if (mView == null)return;
            if (!mIsUpdate) return;
            UserEntity userEntity = sDataManager.getTradeBean().getUsers().get(sDataManager.USER_ID);
            if (userEntity == null) return;
            mNewData.clear();

            for (PositionEntity positionEntity :
                    userEntity.getPositions().values()) {

                int volume_long = Integer.parseInt(positionEntity.getVolume_long());
                int volume_short = Integer.parseInt(positionEntity.getVolume_short());
                if (volume_long != 0 && volume_short != 0) {
                    PositionEntity positionEntityLong = positionEntity.cloneLong();
                    PositionEntity positionEntityShort = positionEntity.cloneShort();
                    mNewData.add(positionEntityLong);
                    mNewData.add(positionEntityShort);
                } else if (!(volume_long == 0 && volume_short == 0)) {
                    mNewData.add(CloneUtils.clone(positionEntity));
                }
            }

            if (mIsInAccountFragment && mNewData.isEmpty())
                mBinding.seeMd.setVisibility(View.VISIBLE);
            else mBinding.seeMd.setVisibility(View.GONE);

            Collections.sort(mNewData);
            DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new PositionDiffCallback(mOldData, mNewData), false);
            mAdapter.setData(mNewData);
            diffResult.dispatchUpdatesTo(mAdapter);
            mOldData.clear();
            mOldData.addAll(mNewData);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * date: 2019/8/11
     * author: chenli
     * description: 止盈止损单
     */
    private void initPopUp(final View view, final String ins, final String directionTitle, final String volume) {
        final View popUpView = View.inflate(getActivity(), R.layout.popup_fragment_position, null);
        final PopupWindow popWindow = new PopupWindow(popUpView,
                ViewGroup.LayoutParams.MATCH_PARENT, ScreenUtils.dp2px(getActivity(), 42), true);
        //设置动画，淡入淡出
        popWindow.setAnimationStyle(R.style.anim_menu_quote);
        //点击空白处popupWindow消失
        popWindow.setBackgroundDrawable(new ColorDrawable(0x00000000));
        TextView manager = popUpView.findViewById(R.id.condition_order_manager);
        TextView sltp = popUpView.findViewById(R.id.condition_order_sltp);
        //设置popupWindow显示的位置，参数依次是参照View，x轴的偏移量，y轴的偏移量
        DisplayMetrics outMetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(outMetrics);
        popWindow.showAsDropDown(view, outMetrics.widthPixels / 4 * 3, 0);
        final String direction;
        switch (directionTitle){
            case DIRECTION_BUY_ZN:
                direction = DIRECTION_BUY;
                break;
            case DIRECTION_SELL_ZN:
                direction = DIRECTION_SELL;
                break;
            default:
                direction = "";
                break;
        }
        Context sContext = BaseApplication.getContext();
        String name = sDataManager.USER_ID;
        String password = (String) SPUtils.get(sContext, CONFIG_PASSWORD, "");
        boolean isVisitor = TDUtils.isVisitor(name, password);

        sltp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity mainActivity = (MainActivity) getActivity();
                if (!mainActivity.checkConditionResponsibility())return;

                if (isVisitor){
                    ToastUtils.showToast(sContext, "游客模式暂不支持条件单/止盈止损");
                    popWindow.dismiss();
                    return;
                }
                Intent intent = new Intent(getActivity(), StopLossTakeProfitActivity.class);
                intent.putExtra(INS_BETWEEN_ACTIVITY,ins);
                intent.putExtra(DIRECTION_BETWEEN_ACTIVITY, direction);
                intent.putExtra(VOLUME_BETWEEN_ACTIVITY, volume);
                startActivity(intent);
                popWindow.dismiss();
                Amplitude.getInstance().logEventWrap(AMP_CONDITION_STOP_LOSS, new JSONObject());
            }
        });

        manager.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity mainActivity = (MainActivity) getActivity();
                if (!mainActivity.checkConditionResponsibility())return;

                if (isVisitor){
                    ToastUtils.showToast(sContext, "游客模式暂不支持条件单/止盈止损");
                    popWindow.dismiss();
                    return;
                }
                Intent intent = new Intent(getActivity(), ManagerConditionOrderActivity.class);
                startActivity(intent);
                popWindow.dismiss();
                Amplitude.getInstance().logEventWrap(AMP_CONDITION_POSITION, new JSONObject());
            }
        });
    }

    /**
     * date: 2019/7/3
     * author: chenli
     * description: 设置合约id
     */
    public void setInstrument_id(String ins) {
        mInstrumentId = ins;
    }

    /**
     * date: 7/9/17
     * author: chenli
     * description: 接收持仓点击、自选点击、搜索页点击发来的合约，用于更新高亮合约
     */
    @Subscribe
    public void onEvent(SwitchInsEvent data) {
        mInstrumentId = data.getInstrument_id();
        mAdapter.updateHighlightIns(mInstrumentId);
    }

}

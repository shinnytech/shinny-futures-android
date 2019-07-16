package com.shinnytech.futures.controller.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.widget.LinearLayoutManager;
import android.view.View;

import com.shinnytech.futures.R;
import com.shinnytech.futures.databinding.ActivityBrokerListBinding;
import com.shinnytech.futures.model.adapter.BrokerAdapter;
import com.shinnytech.futures.model.engine.LatestFileManager;
import com.shinnytech.futures.model.listener.SimpleRecyclerViewItemClickListener;
import com.shinnytech.futures.utils.DividerItemDecorationUtils;
import com.shinnytech.futures.utils.ToastUtils;

import java.util.List;

import static com.shinnytech.futures.constants.CommonConstants.BROKER_LIST;

public class BrokerListActivity extends BaseActivity {

    private ActivityBrokerListBinding mBinding;
    private BrokerAdapter mBrokerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mLayoutID = R.layout.activity_broker_list;
        mTitle = BROKER_LIST;
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void initData() {
        mBinding = (ActivityBrokerListBinding) mViewDataBinding;
        mBinding.rv.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        mBinding.rv.addItemDecoration(
                new DividerItemDecorationUtils(this, DividerItemDecorationUtils.VERTICAL_LIST));
        List<String> brokers = LatestFileManager
                .getBrokerIdFromBuildConfig(sDataManager.getBroker().getBrokers());
        mBrokerAdapter = new BrokerAdapter(this, brokers);
        mBinding.rv.setAdapter(mBrokerAdapter);
        try {
            String broker = getIntent().getStringExtra("broker");
            int index = brokers.indexOf(broker);
            if (index != -1) mBinding.rv.scrollToPosition(index);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void initEvent() {
        mBinding.rv.addOnItemTouchListener(new SimpleRecyclerViewItemClickListener(mBinding.rv,
                new SimpleRecyclerViewItemClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        String broker = mBrokerAdapter.getData().get(position);
                        if (broker == null) return;

                        if (broker.contains(" ")) {
                            ToastUtils.showToast(sContext, "请联系期货公司申请！");
                            return;
                        }

                        Intent intent = new Intent();
                        intent.putExtra("broker", broker);
                        setResult(RESULT_OK, intent);
                        Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                finish();
                            }
                        }, 300);
                    }

                    @Override
                    public void onItemLongClick(View view, int position) {
                    }
                }));
    }

}

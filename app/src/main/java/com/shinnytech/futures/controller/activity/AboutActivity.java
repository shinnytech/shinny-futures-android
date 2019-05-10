package com.shinnytech.futures.controller.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.shinnytech.futures.R;
import com.shinnytech.futures.model.engine.DataManager;
import com.tencent.bugly.beta.Beta;

import static com.shinnytech.futures.constants.CommonConstants.ABOUT;

public class AboutActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mLayoutID = R.layout.activity_about;
        mTitle = ABOUT;
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void initData() {
        ((TextView) findViewById(R.id.version)).setText(DataManager.getInstance().APP_VERSION);
    }

    @Override
    protected void initEvent() {
        findViewById(R.id.check_version).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Beta.checkUpgrade();
            }
        });
    }

    @Override
    protected void refreshUI() {

    }

}

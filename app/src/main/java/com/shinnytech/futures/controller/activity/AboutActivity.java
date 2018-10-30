package com.shinnytech.futures.controller.activity;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;

import com.shinnytech.futures.R;
import com.shinnytech.futures.databinding.ActivityAboutBinding;

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
        try {
            String versionName = this.getPackageManager().getPackageInfo(this.getPackageName(), 0).versionName;
            ((TextView)findViewById(R.id.version)).setText(versionName);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void initEvent() {

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case android.R.id.home:
                finish();
        }
        return super.onOptionsItemSelected(item);
    }
}

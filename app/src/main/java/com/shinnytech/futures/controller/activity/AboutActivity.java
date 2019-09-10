package com.shinnytech.futures.controller.activity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.shinnytech.futures.R;
import com.shinnytech.futures.model.engine.DataManager;
import com.shinnytech.futures.utils.ImageUtils;
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
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher);
        ImageView imageView = findViewById(R.id.icon);
        imageView.setImageBitmap(ImageUtils.getRoundedCornerBitmap(bitmap, 30));
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

}

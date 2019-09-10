package com.shinnytech.futures.controller.activity;

import android.os.Bundle;
import android.webkit.WebSettings;

import com.shinnytech.futures.R;
import com.shinnytech.futures.databinding.ActivityShinnyTechBinding;

import static com.shinnytech.futures.constants.CommonConstants.SHINNYTECH;

public class ShinnyTechActivity extends BaseActivity {
    private ActivityShinnyTechBinding mBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mLayoutID = R.layout.activity_shinny_tech;
        mTitle = SHINNYTECH;
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void initData() {
        mBinding = (ActivityShinnyTechBinding) mViewDataBinding;
        WebSettings settings = mBinding.webView.getSettings();
        settings.setDomStorageEnabled(true);
        settings.setJavaScriptEnabled(true);  //设置WebView属性,运行执行js脚本
        settings.setUseWideViewPort(true);//将图片调整到适合webView的大小
        settings.setLoadWithOverviewMode(true);   //自适应屏幕
        settings.setBuiltInZoomControls(true);
        settings.setDisplayZoomControls(false);
        settings.setSupportZoom(true);//设定支持缩放
        settings.setDefaultTextEncodingName("utf-8");
        settings.setLoadsImagesAutomatically(true);
        mBinding.webView.loadUrl("https://www.shinnytech.com/q/");//调用loadUrl方法为WebView加入链接
    }

    @Override
    protected void initEvent() {

    }
}

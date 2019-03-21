package com.shinnytech.futures.controller.activity;

import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.shinnytech.futures.R;
import com.shinnytech.futures.databinding.ActivityOpenAccountBinding;
import com.shinnytech.futures.utils.LogUtils;

import java.io.IOException;
import java.io.InputStream;

import static com.shinnytech.futures.constants.CommonConstants.OPEN_ACCOUNT;

public class OpenAccountActivity extends BaseActivity {
    private ActivityOpenAccountBinding mBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mLayoutID = R.layout.activity_open_account;
        mTitle = OPEN_ACCOUNT;
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void initData() {
        mBinding = (ActivityOpenAccountBinding) mViewDataBinding;
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            WebView.setWebContentsDebuggingEnabled(true);
        }
        mBinding.webView.loadUrl("https://appficaos.cfmmc.com/indexnew");//调用loadUrl方法为WebView加入链接
    }

    @Override
    protected void initEvent() {
        mBinding.webView.setWebViewClient(new WebViewClient() {
            //设置在webView点击打开的新网页在当前界面显示,而不跳转到新的浏览器中
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    view.loadUrl(request.getUrl().getPath());
                }
                return true;
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
            }

            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Nullable
            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
                String url = request.getUrl().getPath();
                LogUtils.e(url, true);
                if (url.contains("profileController")
                        || url.contains("loginController")
                        || url.contains("collectController")
                        || url.contains("depositoryController")
                        || url.contains("videoController")
                        || url.contains("common")) {
                    url = url.replace("/template/future/js/", "");
                    return getJSWebResourceResponseFromAsset(view, url);
                }
                return super.shouldInterceptRequest(view, request);
            }
        });
    }

    private WebResourceResponse getJSWebResourceResponseFromAsset(WebView webView, String fileName) {
        try {
            return getUtf8EncodedJSWebResourceResponse(webView.getContext().getAssets().open(fileName));
        } catch (IOException e) {
            return null;
        }
    }

    private WebResourceResponse getUtf8EncodedJSWebResourceResponse(InputStream data) {
        return new WebResourceResponse("text/javascript", "UTF-8", data);
    }

    @Override
    protected void refreshUI() {

    }
}

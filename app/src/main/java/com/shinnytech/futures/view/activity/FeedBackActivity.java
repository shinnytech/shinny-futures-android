package com.shinnytech.futures.view.activity;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.DownloadListener;
import android.webkit.URLUtil;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.shinnytech.futures.R;
import com.shinnytech.futures.application.BaseApplicationLike;
import com.shinnytech.futures.constants.CommonConstants;
import com.shinnytech.futures.databinding.ActivityFeedBackBinding;
import com.shinnytech.futures.utils.ToastNotificationUtils;

import java.io.File;

import static com.shinnytech.futures.constants.CommonConstants.FEEDBACK;
import static com.shinnytech.futures.model.receiver.NetworkReceiver.NETWORK_STATE;

/**
 * date: 7/7/17
 * author: chenli
 * description: 用于记录用户的反馈信息，嵌入web页面
 * version:
 * state: undone
 */
public class FeedBackActivity extends BaseActivity {

    /**
     * date: 7/12/17
     * description: 申请外部存储权限
     */
    private static final int WRITE_EXTERNAL_STORAGE_REQUEST_CODE = 0;
    /**
     * date: 7/12/17
     * description: 文件选择码
     */
    private final static int FILE_CHOOSER_RESULT_CODE = 1;

    private long mExitTime = 0;
    /**
     * date: 7/12/17
     * description: 5.0以下的回调
     */
    private ValueCallback<Uri> mUploadMessage;
    /**
     * date: 7/12/17
     * description: 5.0以上的回调
     */
    private ValueCallback<Uri[]> mUploadMessageAboveL;
    /**
     * date: 7/12/17
     * description: 文件下载地址
     */
    private String mUrl;

    /**
     * date: 7/12/17
     * description: 用于生成下载文件名称
     */
    private String mMimetype;
    private Context sContext;
    private BroadcastReceiver mReceiver;
    private ActivityFeedBackBinding mBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mLayoutID = R.layout.activity_feed_back;
        mTitle = FEEDBACK;
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void initData() {
        mBinding = (ActivityFeedBackBinding) mViewDataBinding;
        sContext = BaseApplicationLike.getContext();
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
        mBinding.webView.loadUrl(CommonConstants.FEED_BACK_URL);//调用loadUrl方法为WebView加入链接
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
        });

        //webView上传文件，不同版本的适配，注意：里面的方法不能混淆
        mBinding.webView.setWebChromeClient(new WebChromeClient() {

            // For Android < 3.0
            public void openFileChooser(ValueCallback<Uri> valueCallback) {
                mUploadMessage = valueCallback;
                openImageChooserActivity();
            }

            // For Android  >= 3.0
            public void openFileChooser(ValueCallback valueCallback, String acceptType) {
                mUploadMessage = valueCallback;
                openImageChooserActivity();
            }

            //For Android  >= 4.1
            public void openFileChooser(ValueCallback<Uri> valueCallback, String acceptType, String capture) {
                mUploadMessage = valueCallback;
                openImageChooserActivity();
            }

            // For Android >= 5.0
            @Override
            public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, WebChromeClient.FileChooserParams fileChooserParams) {
                mUploadMessageAboveL = filePathCallback;
                openImageChooserActivity();
                return true;
            }
        });

        //webView下载文件
        mBinding.webView.setDownloadListener(new DownloadListener() {
            @Override
            public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimetype, long contentLength) {
                mUrl = url;
                mMimetype = mimetype;
                if (ContextCompat.checkSelfPermission(FeedBackActivity.this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    //申请WRITE_EXTERNAL_STORAGE权限
                    ActivityCompat.requestPermissions(FeedBackActivity.this, new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            WRITE_EXTERNAL_STORAGE_REQUEST_CODE);
                } else {
                    downLoadFile(url, contentDisposition, mimetype);
                }
            }
        });
    }

    /**
     * date: 7/12/17
     * author: chenli
     * description: 利用系统的下载服务
     */
    private void downLoadFile(String url, String contentDisposition, String mimetype) {
        //创建下载任务,downloadUrl就是下载链接
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
        //指定下载路径和下载文件名
        request.setDestinationInExternalPublicDir("/download/", URLUtil.guessFileName(url, contentDisposition, mimetype));
        //获取下载管理器
        DownloadManager downloadManager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
        //将下载任务加入下载队列，否则不会进行下载
        if (downloadManager != null) downloadManager.enqueue(request);
        ToastNotificationUtils.showToast(BaseApplicationLike.getContext(),
                "下载完成:" + Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                        .getAbsolutePath() + File.separator + URLUtil.guessFileName(url, contentDisposition, mimetype));
    }

    /**
     * date: 7/12/17
     * author: chenli
     * description: 授权回调，授权成功则下载文件
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case WRITE_EXTERNAL_STORAGE_REQUEST_CODE:
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    downLoadFile(mUrl, mMimetype, mMimetype);
                }
                break;
            default:
                break;
        }
    }

    /**
     * date: 7/12/17
     * author: chenli
     * description: 打开本地文件
     */
    private void openImageChooserActivity() {
        Intent i = new Intent(Intent.ACTION_GET_CONTENT);
        i.addCategory(Intent.CATEGORY_OPENABLE);
        i.setType("image/*");
        startActivityForResult(Intent.createChooser(i, "Image Chooser"), FILE_CHOOSER_RESULT_CODE);
    }

    /**
     * date: 7/12/17
     * author: chenli
     * description: 返回数据给webView处理
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == FILE_CHOOSER_RESULT_CODE) {
            if (null == mUploadMessage && null == mUploadMessageAboveL) return;
            Uri result = data == null || resultCode != RESULT_OK ? null : data.getData();
            if (mUploadMessageAboveL != null) {
                onActivityResultAboveL(requestCode, resultCode, data);
            } else if (mUploadMessage != null) {
                mUploadMessage.onReceiveValue(result);
                mUploadMessage = null;
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void onActivityResultAboveL(int requestCode, int resultCode, Intent intent) {
        if (requestCode != FILE_CHOOSER_RESULT_CODE || mUploadMessageAboveL == null)
            return;
        Uri[] results = null;
        if (resultCode == Activity.RESULT_OK) {
            if (intent != null) {
                String dataString = intent.getDataString();
                ClipData clipData = intent.getClipData();
                if (clipData != null) {
                    results = new Uri[clipData.getItemCount()];
                    for (int i = 0; i < clipData.getItemCount(); i++) {
                        ClipData.Item item = clipData.getItemAt(i);
                        results[i] = item.getUri();
                    }
                }
                if (dataString != null)
                    results = new Uri[]{Uri.parse(dataString)};
            }
        }
        mUploadMessageAboveL.onReceiveValue(results);
        mUploadMessageAboveL = null;
    }

    @Override
    public void onResume() {
        super.onResume();
        updateToolbarFromNetwork(sContext, "反馈");
        registerBroaderCast();
    }

    @Override
    public void onPause() {
        super.onPause();
        unregisterReceiver(mReceiver);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_feedback, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_refresh:
                mBinding.webView.reload();
                break;
            case android.R.id.home:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }


    /**
     * date: 7/12/17
     * author: chenli
     * description: 我们需要重写回退按钮的时间, 当用户点击回退按钮：
     * 1.mWebView.canGoBack()判断网页是否能后退,可以则goback()
     * 2.如果不可以连续点击两次退出App,否则弹出提示Toast
     */
    @Override
    public void onBackPressed() {
        if (mBinding.webView.canGoBack()) {
            mBinding.webView.goBack();
        } else {
            if ((System.currentTimeMillis() - mExitTime) > 2000) {
                ToastNotificationUtils.showToast(BaseApplicationLike.getContext(), getString(R.string.main_activity_exit));
                mExitTime = System.currentTimeMillis();
            } else {
                super.onBackPressed();
            }

        }
    }


    /**
     * date: 7/7/17
     * author: chenli
     * description: 监控网络状态与登录状态
     */
    private void registerBroaderCast() {
        mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                int networkStatus = intent.getIntExtra("networkStatus", 0);
                switch (networkStatus) {
                    case 0:
                        mToolbar.setBackgroundColor(ContextCompat.getColor(context, R.color.off_line));
                        mToolbarTitle.setTextColor(Color.BLACK);
                        mToolbarTitle.setText("交易、行情网络未连接！");
                        mToolbarTitle.setTextSize(20);
                        break;
                    case 1:
                        mToolbar.setBackgroundColor(ContextCompat.getColor(context, R.color.black_dark));
                        mToolbarTitle.setTextColor(Color.WHITE);
                        mToolbarTitle.setText("反馈");
                        mToolbarTitle.setTextSize(25);
                        break;
                    default:
                        break;
                }
            }
        };
        registerReceiver(mReceiver, new IntentFilter(NETWORK_STATE));
    }

}

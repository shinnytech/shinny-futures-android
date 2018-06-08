package com.xinyi.shinnyfutures.application;

import com.tencent.tinker.loader.app.TinkerApplication;
import com.tencent.tinker.loader.shareutil.ShareConstants;

/**
 * date: 7/9/17
 * author: chenli
 * description: 自定义application基类，用于获取合约列表文件，webSocket连接服务器,接入Tinker升级热更新框架
 * version:
 * state:
 */
public class BaseApplication extends TinkerApplication {

    public BaseApplication() {
        super(ShareConstants.TINKER_ENABLE_ALL, "com.xinyi.shinnyfutures.application.BaseApplicationLike",
                "com.tencent.tinker.loader.TinkerLoader", false);
    }
}

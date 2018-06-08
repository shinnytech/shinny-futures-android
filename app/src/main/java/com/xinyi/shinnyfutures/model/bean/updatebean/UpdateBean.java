package com.xinyi.shinnyfutures.model.bean.updatebean;

/**
 * Created on 11/7/17.
 * Created by chenli.
 * Description: .
 */

public class UpdateBean {
    //app名字
    private String appName;
    //versionName
    private String appVersion;
    //versionCode
    private int appCode;
    //app最新版本地址
    private String updateUrl;
    //升级信息
    private String updateInfo;

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getAppVersion() {
        return appVersion;
    }

    public void setAppVersion(String appVersion) {
        this.appVersion = appVersion;
    }

    public int getAppCode() {
        return appCode;
    }

    public void setAppCode(int appCode) {
        this.appCode = appCode;
    }

    public String getUpdateUrl() {
        return updateUrl;
    }

    public void setUpdateUrl(String updateUrl) {
        this.updateUrl = updateUrl;
    }

    public String getUpdateInfo() {
        return updateInfo;
    }

    public void setUpdateInfo(String updateInfo) {
        this.updateInfo = updateInfo;
    }
}

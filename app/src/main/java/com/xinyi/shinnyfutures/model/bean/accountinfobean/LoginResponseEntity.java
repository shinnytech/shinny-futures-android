package com.xinyi.shinnyfutures.model.bean.accountinfobean;

/**
 * Created on 7/21/17.
 * Created by chenli.
 * Description: .
 */

public class LoginResponseEntity {
    private String session;
    private String ip;
    private String session_port;
    private String gateway_web_port;

    public String getSession() {
        return session;
    }

    public void setSession(String session) {
        this.session = session;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getSession_port() {
        return session_port;
    }

    public void setSession_port(String session_port) {
        this.session_port = session_port;
    }

    public String getGateway_web_port() {
        return gateway_web_port;
    }

    public void setGateway_web_port(String gateway_web_port) {
        this.gateway_web_port = gateway_web_port;
    }
}

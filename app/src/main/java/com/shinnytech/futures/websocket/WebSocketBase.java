package com.shinnytech.futures.websocket;

import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketAdapter;
import com.neovisionaries.ws.client.WebSocketException;
import com.neovisionaries.ws.client.WebSocketExtension;
import com.neovisionaries.ws.client.WebSocketFactory;
import com.neovisionaries.ws.client.WebSocketFrame;
import com.shinnytech.futures.amplitude.api.Amplitude;
import com.shinnytech.futures.application.BaseApplication;
import com.shinnytech.futures.model.engine.DataManager;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import static com.shinnytech.futures.constants.CommonConstants.TRANSACTION_URL;

public class WebSocketBase extends WebSocketAdapter {
    protected DataManager sDataManager = DataManager.getInstance();
    protected WebSocket mWebSocketClient;
    protected List<String> mUrls;
    protected int mIndex;
    private int mPongCount;
    private Timer mTimer;
    private TimerTask mTimerTask;
    private long mConnectTime;

    public WebSocketBase(List<String> urls, int index){
        mIndex = index;
        mUrls = urls;
    }

    @Override
    public void onConnected(WebSocket websocket, Map<String, List<String>> headers) throws Exception {
        super.onConnected(websocket, headers);
        mConnectTime = System.currentTimeMillis() / 1000;
        mPongCount = 0;
        mTimer = new Timer();
        mTimerTask = new TimerTask() {
            @Override
            public void run() {
                if (BaseApplication.issBackGround())return;
                if (mPongCount > 3){
                    reconnect();
                    return;
                }
                mWebSocketClient.sendPing();
                mPongCount++;
            }
        };
        mTimer.schedule(mTimerTask, 0, 10000);
    }

    @Override
    public void onDisconnected(WebSocket websocket, WebSocketFrame serverCloseFrame, WebSocketFrame clientCloseFrame, boolean closedByServer) throws Exception {
        super.onDisconnected(websocket, serverCloseFrame, clientCloseFrame, closedByServer);
        long currentTime = System.currentTimeMillis() / 1000;
        if (currentTime - mConnectTime >= 10) reconnect();
        else {
            Timer timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    reconnect();
                }
            }, 10000);
        }

    }

    @Override
    public void onConnectError(WebSocket websocket, WebSocketException exception) throws Exception {
        super.onConnectError(websocket, exception);
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                reconnect();
            }
        }, 10000);
    }

    @Override
    public void onPongFrame(WebSocket websocket, WebSocketFrame frame) throws Exception {
        super.onPongFrame(websocket, frame);
        mPongCount--;
    }

    public void reconnect(){
        if (mTimer != null){
            mTimer.cancel();
            mTimer = null;
        }

        if (mTimerTask != null){
            mTimerTask.cancel();
            mTimerTask = null;
        }
        try {
            if (mWebSocketClient != null) mWebSocketClient.clearListeners();
            mWebSocketClient = new WebSocketFactory()
                    .setVerifyHostname(TRANSACTION_URL.equals(mUrls.get(0)))
                    .setConnectionTimeout(5000)
                    .createSocket(mUrls.get(mIndex))
                    .setMissingCloseFrameAllowed(false)
                    .addListener(this)
                    .addHeader("User-Agent", sDataManager.USER_AGENT + " " + sDataManager.APP_VERSION)
                    .addHeader("SA-Machine", Amplitude.getInstance().getDeviceId())
                    .addHeader("SA-Session", Amplitude.getInstance().getDeviceId())
                    .addExtension(WebSocketExtension.PERMESSAGE_DEFLATE);
        } catch (IOException e) {
            e.printStackTrace();
        }
        mIndex += 1;
        if (mIndex == mUrls.size()) mIndex = 0;
        mWebSocketClient.connectAsynchronously();
    }

    public void resetConnectTime(){
        mConnectTime = System.currentTimeMillis() / 1000;
    }

}

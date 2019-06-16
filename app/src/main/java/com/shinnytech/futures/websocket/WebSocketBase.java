package com.shinnytech.futures.websocket;

import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketAdapter;
import com.neovisionaries.ws.client.WebSocketException;
import com.neovisionaries.ws.client.WebSocketExtension;
import com.neovisionaries.ws.client.WebSocketFactory;
import com.neovisionaries.ws.client.WebSocketFrame;
import com.shinnytech.futures.amplitude.api.Amplitude;
import com.shinnytech.futures.model.engine.DataManager;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class WebSocketBase extends WebSocketAdapter {
    private static final int TIMEOUT = 5000;
    protected DataManager sDataManager = DataManager.getInstance();
    protected WebSocket mWebSocketClient;
    protected List<String> mUrls;
    protected int mIndex;
    private int mPongCount;
    private Timer mTimer;
    private TimerTask mTimerTask;

    public WebSocketBase(List<String> urls, int index){
        mIndex = index;
        mUrls = urls;
        create();
    }

    @Override
    public void onConnected(WebSocket websocket, Map<String, List<String>> headers) throws Exception {
        super.onConnected(websocket, headers);
        mPongCount = 0;
        mTimer = new Timer();
        mTimerTask = new TimerTask() {
            @Override
            public void run() {
                if (mPongCount > 3){
                    mWebSocketClient.disconnect();
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
        if (mTimer != null){
            mTimer.cancel();
            mTimer = null;
        }

        if (mTimerTask != null){
            mTimerTask.cancel();
            mTimerTask = null;
        }

        reconnect();
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

    private void create(){
        try {
            mWebSocketClient = new WebSocketFactory()
                    .setConnectionTimeout(TIMEOUT)
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
    }

    public void reconnect(){
        create();
        connect();
    }

    public void connect(){
        mIndex += 1;
        if (mIndex == mUrls.size()) mIndex = 0;
        mWebSocketClient.connectAsynchronously();
    }
}

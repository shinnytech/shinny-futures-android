package com.shinnytech.futures.model.amplitude.api;

import android.os.Handler;
import android.os.HandlerThread;

public class WorkerThread extends HandlerThread {

    private Handler handler;

    public WorkerThread(String name) {
        super(name);
    }

    Handler getHandler() {
        return handler;
    }

    void post(Runnable r) {
        waitForInitialization();
        handler.post(r);
    }

    void postDelayed(Runnable r, long delayMillis) {
        waitForInitialization();
        handler.postDelayed(r, delayMillis);
    }

    void removeCallbacks(Runnable r) {
        waitForInitialization();
        handler.removeCallbacks(r);
    }

    private synchronized void waitForInitialization() {
        if (handler == null) {
            handler = new Handler(getLooper());
        }
    }
}

package com.winning.mars_generator.core.modules.sm.blockCanary;

import android.os.Handler;
import android.os.HandlerThread;

public final class HandlerThreadFactory {

    /**
     * dump thread
     */
    private static HandlerThreadWrapper sDoDumpThread = new HandlerThreadWrapper("do-dump");
    /**
     * obtain dump data thread
     */
    private static HandlerThreadWrapper sObtainDumpThread = new HandlerThreadWrapper("obtain-dump");

    private HandlerThreadFactory() {
        throw new InstantiationError("can not init this class");
    }

    public static Handler getDoDumpThreadHandler() {
        return sDoDumpThread.getHandler();
    }

    public static Handler getObtainDumpThreadHandler() {
        return sObtainDumpThread.getHandler();
    }

    private static class HandlerThreadWrapper {
        private Handler handler = null;

        public HandlerThreadWrapper(String threadName) {
            HandlerThread handlerThread = new HandlerThread(threadName);
            handlerThread.start();
            handler = new Handler(handlerThread.getLooper());
        }

        public Handler getHandler() {
            return handler;
        }
    }
}

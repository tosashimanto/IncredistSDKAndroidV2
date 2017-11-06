package jp.co.flight.incredist.android.internal.util;

import android.util.Log;

/**
 * SDK内部 Log クラス (for debug)
 */
public class FLog {
    public static int d(String tag, String msg, Throwable tr) {
        return Log.d(tag, msg, tr);
    }

    public static int d(String tag, String msg) {
        return Log.d(tag, msg);
    }

    public static int e(String tag, String msg, Throwable tr) {
        return Log.e(tag, msg, tr);
    }

    public static int e(String tag, String msg) {
        return Log.e(tag, msg);
    }

    public static int i(String tag, String msg, Throwable tr) {
        return Log.i(tag, msg, tr);
    }

    public static int i(String tag, String msg) {
        return Log.i(tag, msg);
    }

    public static int v(String tag, String msg, Throwable tr) {
        return Log.v(tag, msg, tr);
    }

    public static int v(String tag, String msg) {
        return Log.v(tag, msg);
    }

    public static int w(String tag, String msg, Throwable tr) {
        return Log.w(tag, msg, tr);
    }

    public static int w(String tag, String msg) {
        return Log.w(tag, msg);
    }
}

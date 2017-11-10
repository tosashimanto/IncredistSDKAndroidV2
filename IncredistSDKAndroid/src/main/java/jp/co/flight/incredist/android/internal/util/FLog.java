package jp.co.flight.incredist.android.internal.util;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * SDK内部 Log クラス (for debug).
 */
@SuppressWarnings({"WeakerAccess", "unused"})
public class FLog {
    private static List<LogInterface> logInstances = new ArrayList<>();

    static {
        FLog.addLogInstance(new AndroidLog());
    }

    public static int d(String tag, String msg, Throwable tr) {
        int result = 0;
        for (LogInterface log : logInstances) {
            result = log.d(tag, msg, tr);
        }
        return result;
    }

    public static int d(String tag, String msg) {
        int result = 0;
        for (LogInterface log : logInstances) {
            result = log.d(tag, msg);
        }
        return result;
    }

    public static int e(String tag, String msg, Throwable tr) {
        int result = 0;
        for (LogInterface log : logInstances) {
            result = log.e(tag, msg, tr);
        }
        return result;
    }

    public static int e(String tag, String msg) {
        int result = 0;
        for (LogInterface log : logInstances) {
            result = log.e(tag, msg);
        }
        return result;
    }

    public static int i(String tag, String msg, Throwable tr) {
        int result = 0;
        for (LogInterface log : logInstances) {
            result = log.i(tag, msg, tr);
        }
        return result;
    }

    public static int i(String tag, String msg) {
        int result = 0;
        for (LogInterface log : logInstances) {
            result = log.i(tag, msg);
        }
        return result;
    }

    public static int v(String tag, String msg, Throwable tr) {
        int result = 0;
        for (LogInterface log : logInstances) {
            result = log.v(tag, msg, tr);
        }
        return result;
    }

    public static int v(String tag, String msg) {
        int result = 0;
        for (LogInterface log : logInstances) {
            result = log.v(tag, msg);
        }
        return result;
    }

    public static int w(String tag, String msg, Throwable tr) {
        int result = 0;
        for (LogInterface log : logInstances) {
            result = log.w(tag, msg, tr);
        }
        return result;
    }

    public static int w(String tag, String msg) {
        int result = 0;
        for (LogInterface log : logInstances) {
            result = log.w(tag, msg);
        }
        return result;
    }

    public static void addLogInstance(LogInterface log) {
        logInstances.add(log);
    }

    static class AndroidLog implements LogInterface {
        public int d(String tag, String msg, Throwable tr) {
            return Log.d(tag, msg, tr);
        }

        public int d(String tag, String msg) {
            return Log.d(tag, msg);
        }

        public int e(String tag, String msg, Throwable tr) {
            return Log.e(tag, msg, tr);
        }

        public int e(String tag, String msg) {
            return Log.e(tag, msg);
        }

        public int i(String tag, String msg, Throwable tr) {
            return Log.i(tag, msg, tr);
        }

        public int i(String tag, String msg) {
            return Log.i(tag, msg);
        }

        public int v(String tag, String msg, Throwable tr) {
            return Log.v(tag, msg, tr);
        }

        public int v(String tag, String msg) {
            return Log.v(tag, msg);
        }

        public int w(String tag, String msg, Throwable tr) {
            return Log.w(tag, msg, tr);
        }

        public int w(String tag, String msg) {
            return Log.w(tag, msg);
        }
    }
}

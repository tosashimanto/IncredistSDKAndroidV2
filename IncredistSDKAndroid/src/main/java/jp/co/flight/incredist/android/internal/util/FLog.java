package jp.co.flight.incredist.android.internal.util;

import java.util.ArrayList;
import java.util.List;

/**
 * SDK内部 Log クラス (for debug).
 */
@SuppressWarnings({"WeakerAccess", "unused"})
public class FLog {
    private static List<LogInterface> sLogInstances = new ArrayList<>();

    static {
        FLog.addLogInstance(new AndroidLogCat());
    }

    public static int d(String tag, String msg, Throwable tr) {
        int result = 0;
        for (LogInterface log : sLogInstances) {
            result = log.d(tag, msg, tr);
        }
        return result;
    }

    public static int d(String tag, String msg) {
        int result = 0;
        for (LogInterface log : sLogInstances) {
            result = log.d(tag, msg);
        }
        return result;
    }

    public static int e(String tag, String msg, Throwable tr) {
        int result = 0;
        for (LogInterface log : sLogInstances) {
            result = log.e(tag, msg, tr);
        }
        return result;
    }

    public static int e(String tag, String msg) {
        int result = 0;
        for (LogInterface log : sLogInstances) {
            result = log.e(tag, msg);
        }
        return result;
    }

    public static int i(String tag, String msg, Throwable tr) {
        int result = 0;
        for (LogInterface log : sLogInstances) {
            result = log.i(tag, msg, tr);
        }
        return result;
    }

    public static int i(String tag, String msg) {
        int result = 0;
        for (LogInterface log : sLogInstances) {
            result = log.i(tag, msg);
        }
        return result;
    }

    public static int v(String tag, String msg, Throwable tr) {
        int result = 0;
        for (LogInterface log : sLogInstances) {
            result = log.v(tag, msg, tr);
        }
        return result;
    }

    public static int v(String tag, String msg) {
        int result = 0;
        for (LogInterface log : sLogInstances) {
            result = log.v(tag, msg);
        }
        return result;
    }

    public static int w(String tag, String msg, Throwable tr) {
        int result = 0;
        for (LogInterface log : sLogInstances) {
            result = log.w(tag, msg, tr);
        }
        return result;
    }

    public static int w(String tag, String msg) {
        int result = 0;
        for (LogInterface log : sLogInstances) {
            result = log.w(tag, msg);
        }
        return result;
    }

    public static void addLogInstance(LogInterface log) {
        sLogInstances.add(log);
    }

}

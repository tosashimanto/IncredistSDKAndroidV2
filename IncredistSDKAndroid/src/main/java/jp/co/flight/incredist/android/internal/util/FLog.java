package jp.co.flight.incredist.android.internal.util;

import java.util.ArrayList;
import java.util.List;

/**
 * SDK内部 Log クラス (for debug).
 */
@SuppressWarnings({"WeakerAccess", "unused"})
public class FLog {
    private static final List<LogInterface> sLogInstances = new ArrayList<>();
    //ANDROID_TFPS-1169
    private static final String TAG_PREFIX = "SDK internal.util ";
    static {
        FLog.addLogInstance(new AndroidLogCat());
    }

    public static int d(String tag, String msg, Throwable tr) {
        int result = 0;
        for (LogInterface log : sLogInstances) {
            result = log.d(getTag() + " " + tag, msg, tr);
        }
        return result;
    }

    public static int d(String tag, String msg) {
        int result = 0;
        for (LogInterface log : sLogInstances) {
            result = log.d(getTag() + " " + tag, msg);
        }
        return result;
    }

    public static int e(String tag, String msg, Throwable tr) {
        int result = 0;
        for (LogInterface log : sLogInstances) {
            result = log.e(getTag() + " " + tag, msg, tr);
        }
        return result;
    }

    public static int e(String tag, String msg) {
        int result = 0;
        for (LogInterface log : sLogInstances) {
            result = log.e(getTag() + " " + tag, msg);
        }
        return result;
    }

    public static int i(String tag, String msg, Throwable tr) {
        int result = 0;
        for (LogInterface log : sLogInstances) {
            result = log.i(getTag() + " " + tag, msg, tr);
        }
        return result;
    }

    public static int i(String tag, String msg) {
        int result = 0;
        for (LogInterface log : sLogInstances) {
            result = log.i(getTag() + " " + tag, msg);
        }
        return result;
    }

    public static int v(String tag, String msg, Throwable tr) {
        int result = 0;
        for (LogInterface log : sLogInstances) {
            result = log.v(getTag() + " " + tag, msg, tr);
        }
        return result;
    }

    public static int v(String tag, String msg) {
        int result = 0;
        for (LogInterface log : sLogInstances) {
            result = log.v(getTag() + " " + tag, msg);
        }
        return result;
    }

    public static int w(String tag, String msg, Throwable tr) {
        int result = 0;
        for (LogInterface log : sLogInstances) {
            result = log.w(getTag() + " " + tag, msg, tr);
        }
        return result;
    }

    public static int w(String tag, String msg) {
        int result = 0;
        for (LogInterface log : sLogInstances) {
            result = log.w(getTag() + " " + tag, msg);
        }
        return result;
    }

    public static void addLogInstance(LogInterface log) {
        sLogInstances.add(log);
    }

    private static final int STACK_NUM_OF_CALLER = 4;

    private static String getTag() {
        String threadName = Thread.currentThread().getName();
        StackTraceElement element = Thread.currentThread().getStackTrace()[STACK_NUM_OF_CALLER];
        String fqClassName = element.getClassName();
        String[] splitClassNames = fqClassName.split("\\.");
        String simpleClassName = splitClassNames[splitClassNames.length - 1];
        String methodName = element.getMethodName();
        int lineNo = element.getLineNumber();

        return TAG_PREFIX + "[" + threadName + "][" + simpleClassName + "][" + methodName + "]:" + lineNo;
    }
}

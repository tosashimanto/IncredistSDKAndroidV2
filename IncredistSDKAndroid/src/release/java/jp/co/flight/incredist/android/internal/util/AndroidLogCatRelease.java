package jp.co.flight.incredist.android.internal.util;

/**
 * Release ビルドでは LogCat へ出力しない
 */
public class AndroidLogCatRelease implements LogInterface {
    public int d(String tag, String msg, Throwable tr) {
        return 0;
    }

    public int d(String tag, String msg) {
        return 0;
    }

    public int e(String tag, String msg, Throwable tr) {
        return 0;
    }

    public int e(String tag, String msg) {
        return 0;
    }

    public int i(String tag, String msg, Throwable tr) {
        return 0;
    }

    public int i(String tag, String msg) {
        return 0;
    }

    public int v(String tag, String msg, Throwable tr) {
        return 0;
    }

    public int v(String tag, String msg) {
        return 0;
    }

    public int w(String tag, String msg, Throwable tr) {
        return 0;
    }

    public int w(String tag, String msg) {
        return 0;
    }
}

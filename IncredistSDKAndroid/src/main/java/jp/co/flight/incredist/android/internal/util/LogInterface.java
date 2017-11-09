package jp.co.flight.incredist.android.internal.util;

import android.util.Log;

/**
 * ライブラリ外部でログ表示できるようにするためのインタフェース.
 */
public interface LogInterface {
    int d(String tag, String msg, Throwable tr);

    int d(String tag, String msg);

    int e(String tag, String msg, Throwable tr);

    int e(String tag, String msg);

    int i(String tag, String msg, Throwable tr);

    int i(String tag, String msg);

    int v(String tag, String msg, Throwable tr);

    int v(String tag, String msg);

    int w(String tag, String msg, Throwable tr);

    int w(String tag, String msg);
}

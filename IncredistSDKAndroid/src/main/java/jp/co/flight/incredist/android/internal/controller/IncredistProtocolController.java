package jp.co.flight.incredist.android.internal.controller;

import android.support.annotation.Nullable;

import java.util.Calendar;

import jp.co.flight.incredist.android.model.EncryptionMode;
import jp.co.flight.incredist.android.model.LedColor;
import jp.co.flight.incredist.android.model.PinEntry;

/**
 * Incredist MFi 版 / PCSC 版 共通インタフェース.
 */
interface IncredistProtocolController {
    /**
     * シリアル番号を取得します.
     *
     * @param callback コールバック
     */
    void getDeviceInfo(IncredistController.Callback callback);

    /**
     * EMV メッセージを表示します
     *
     * @param type メッセージ番号
     * @param message メッセージ文字列
     * @param callback コールバック
     */
    void emvDisplaymessage(int type, @Nullable String message, IncredistController.Callback callback);

    /**
     * TFP メッセージを表示します
     *
     * @param type メッセージ番号
     * @param message メッセージ文字列
     * @param callback コールバック
     */
    void tfpDisplaymessage(int type, @Nullable String message, IncredistController.Callback callback);

    /**
     * 暗号化モードを設定します
     *
     * @param mode 暗号化モード
     * @param callback コールバック
     */
    void setEncryptionMode(EncryptionMode mode, IncredistController.Callback callback);

    /**
     * PIN入力を行います
     *
     * @param pinType PIN入力タイプ
     * @param pinMode PIN暗号化モード
     * @param mask 表示マスク
     * @param min 最小桁数
     * @param max 最大桁数
     * @param align 表示左右寄せ
     * @param line 表示行
     * @param timeout タイムアウト時間(msec)
     * @param callback コールバック
     */
    void pinEntryD(PinEntry.Type pinType, PinEntry.Mode pinMode, PinEntry.MaskMode mask, int min, int max, PinEntry.Alignment align, int line, long timeout, IncredistController.Callback callback);

    /**
     * 磁気カードを読み取ります
     *
     * @param timeout タイムアウト時間(msec)
     * @param callback コールバック
     */
    void scanMagneticCard(long timeout, IncredistController.Callback callback);

    /**
     * LED色を設定します。
     * @param color LED色
     * @param isOn true: 点灯 false: 消灯
     * @param callback コールバック
     */
    void setLedColor(LedColor color, boolean isOn, IncredistController.Callback callback);

    /**
     * felica モードを開始します。
     *
     * @param withLed LED を点灯するかどうか
     * @param callback コールバック
     */
    void felicaOpen(boolean withLed, IncredistController.Callback callback);

    /**
     * felica コマンドを送信します.
     *
     * @param command FeliCaコマンドデータ
     * @param callback コールバック
     */
    void felicaSendCommand(byte[] command, IncredistController.Callback callback);

    /**
     * felica モード時のLED色を設定します。
     * @param color LED色
     * @param callback コールバック
     */
    void felicaLedColor(LedColor color, IncredistController.Callback callback);

    /**
     * felica モードを終了します。
     *
     * @param callback コールバック
     */
    void felicaClose(IncredistController.Callback callback);

    /**
     * Incredistに設定されている時刻を取得します
     *
     * @param callback コールバック
     */
    void rtcGetTime(IncredistController.Callback callback);

    /**
     * Incredist に時刻を設定します
     *
     * @param cal 設定時刻
     * @param callback コールバック
     */
    void rtcSetTime(Calendar cal, IncredistController.Callback callback);

    /**
     * Incredist に現在時刻を設定します
     *
     * @param callback コールバック
     */
    void rtcSetCurrentTime(IncredistController.Callback callback);

    /**
     * Incredist を停止します。
     *
     * @param callback コールバック
     */
    void stop(IncredistController.Callback callback);

    /**
     * オブジェクトを解放します
     */
    void release();
}

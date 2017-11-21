package jp.co.flight.incredist.android.internal.controller;

import android.support.annotation.Nullable;

/**
 * Incredist MFi 版 / PCSC 版 共通インタフェース.
 */
interface IncredistProtocolController {
    /**
     * シリアル番号を取得します.
     */
    void getDeviceInfo(IncredistController.Callback callback);

    /**
     * EMV メッセージを表示します
     */
    void emvDisplaymessage(int type, @Nullable String message, IncredistController.Callback callback);

    /**
     * TFP メッセージを表示します
     */
    void tfpDisplaymessage(int type, @Nullable String message, IncredistController.Callback callback);

    /**
     * felica モードを開始します。
     * @param withLed LED を点灯するかどうか
     * @param callback コールバック
     */
    void felicaOpen(boolean withLed, IncredistController.Callback callback);

    /**
     * felica コマンドを送信します.
     * @param command コマンド
     * @param callback コールバック
     */
    void felicaSendCommand(byte[] command, IncredistController.Callback callback);

    /**
     * felica モードを終了します。
     * @param callback コールバック
     */
    void felicaClose(IncredistController.Callback callback);
}

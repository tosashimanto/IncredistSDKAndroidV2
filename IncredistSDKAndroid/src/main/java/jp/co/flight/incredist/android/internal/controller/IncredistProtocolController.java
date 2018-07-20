package jp.co.flight.incredist.android.internal.controller;

import android.support.annotation.Nullable;

import java.util.Calendar;
import java.util.EnumSet;

import jp.co.flight.incredist.android.model.CreditCardType;
import jp.co.flight.incredist.android.model.EmvTagType;
import jp.co.flight.incredist.android.model.EmvTransactionType;
import jp.co.flight.incredist.android.model.EncryptionMode;
import jp.co.flight.incredist.android.model.LedColor;
import jp.co.flight.incredist.android.model.PinEntry;

/**
 * Incredist MFi 版 / PCSC 版 共通インタフェース.
 */
interface IncredistProtocolController {

    /**
     * 現在コマンドを実行中かどうかを取得
     *
     * @return 現在コマンドを実行中の場合 true
     */
    boolean isBusy();

    /**
     * シリアル番号を取得します.
     *
     * @param callback コールバック
     */
    void getDeviceInfo(IncredistController.Callback callback);

    /**
     * ブートローダのバージョンを取得します
     *
     * @param callback コールバック
     */
    void getBootloaderVersion(IncredistController.Callback callback);

    /**
     * EMV メッセージを表示します
     *
     * @param type     メッセージ番号
     * @param message  メッセージ文字列
     * @param callback コールバック
     */
    void emvDisplayMessage(int type, @Nullable String message, IncredistController.Callback callback);

    /**
     * TFP メッセージを表示します
     *
     * @param type     メッセージ番号
     * @param message  メッセージ文字列
     * @param callback コールバック
     */
    void tfpDisplayMessage(int type, @Nullable String message, IncredistController.Callback callback);

    /**
     * 暗号化モードを設定します
     *
     * @param mode     暗号化モード
     * @param callback コールバック
     */
    void setEncryptionMode(EncryptionMode mode, IncredistController.Callback callback);

    /**
     * PIN入力を行います
     *
     * @param pinType  PIN入力タイプ
     * @param pinMode  PIN暗号化モード
     * @param mask     表示マスク
     * @param min      最小桁数
     * @param max      最大桁数
     * @param align    表示左右寄せ
     * @param line     表示行
     * @param timeout  タイムアウト時間(msec)
     * @param callback コールバック
     */
    void pinEntryD(PinEntry.Type pinType, PinEntry.Mode pinMode, PinEntry.MaskMode mask, int min, int max, PinEntry.Alignment align, int line, long timeout, IncredistController.Callback callback);

    /**
     * 磁気カードを読み取ります
     *
     * @param timeout  タイムアウト時間(msec)
     * @param callback コールバック
     */
    void scanMagneticCard(long timeout, IncredistController.Callback callback);

    /**
     * 決済用にクレジットカード(EMV 接触・非接触 と磁気カード)を読み取ります
     *
     * @param cardTypeSet     カード種別
     * @param amount          決済金額
     * @param tagType         タグ種別
     * @param aidSetting      AID設定
     * @param transactionType トランザクション種別
     * @param fallback        フォールバック処理を実行するかどうか
     * @param timeout         タイムアウト時間(msec)
     * @param callback        コールバック
     */
    void scanCreditCard(EnumSet<CreditCardType> cardTypeSet, long amount, EmvTagType tagType, int aidSetting, EmvTransactionType transactionType, boolean fallback, long timeout, IncredistController.Callback callback);

    /**
     * LED色を設定します。
     *
     * @param color    LED色
     * @param isOn     true: 点灯 false: 消灯
     * @param callback コールバック
     */
    void setLedColor(LedColor color, boolean isOn, IncredistController.Callback callback);

    /**
     * felica モードを開始します。
     *
     * @param withLed  LED を点灯するかどうか
     * @param callback コールバック
     */
    void felicaOpen(boolean withLed, IncredistController.Callback callback);

    /**
     * felica コマンドを送信します.
     *
     * @param command  FeliCaコマンドデータ
     * @param wait     ウェイト(単位: msec)
     * @param callback コールバック
     */
    void felicaSendCommand(byte[] command, int wait, IncredistController.Callback callback);

    /**
     * felica モード時のLED色を設定します。
     *
     * @param color    LED色
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
     * @param cal      設定時刻
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
     * EMV kernel に ARC データを送信します
     *
     * @param arcData  ARCデータ
     * @param callback コールバック
     */
    void emvSendArc(byte[] arcData, IncredistController.Callback callback);

    /**
     * icカードの挿入状態をチェックします
     *
     * @param callback コールバック
     */
    void emvCheckCardStatus(IncredistController.Callback callback);

    /**
     * 電子マネー向けの画面・LED点滅します
     *
     * @param isBlink  画面点滅開始の場合 true, 点滅停止の場合 false を指定
     * @param color    LEDの点灯時の色
     * @param duration 点灯時間(msec)
     */
    void emoneyBlink(boolean isBlink, LedColor color, int duration, IncredistController.Callback callback);

    /**
     * 現在実行中のコマンドをキャンセルします
     *
     * @param callback コールバック
     */
    void cancel(IncredistController.Callback callback);

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

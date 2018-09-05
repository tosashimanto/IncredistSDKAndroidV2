package jp.co.flight.incredist.android.internal.controller;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.Calendar;
import java.util.EnumSet;
import java.util.List;

import jp.co.flight.android.bluetooth.le.BluetoothGattConnection;
import jp.co.flight.incredist.android.internal.controller.result.IncredistResult;
import jp.co.flight.incredist.android.internal.exception.ParameterException;
import jp.co.flight.incredist.android.internal.transport.mfi.BleMFiTransport;
import jp.co.flight.incredist.android.internal.transport.mfi.MFiBootloaderVersionCommand;
import jp.co.flight.incredist.android.internal.transport.mfi.MFiCommand;
import jp.co.flight.incredist.android.internal.transport.mfi.MFiDeviceInfoCommand;
import jp.co.flight.incredist.android.internal.transport.mfi.MFiEmoneyBlinkCommand;
import jp.co.flight.incredist.android.internal.transport.mfi.MFiEmvCardStatusCommand;
import jp.co.flight.incredist.android.internal.transport.mfi.MFiEmvCheckKernelSettingCommand;
import jp.co.flight.incredist.android.internal.transport.mfi.MFiEmvDisplayMessageCommand;
import jp.co.flight.incredist.android.internal.transport.mfi.MFiEmvKernelSetupCommand;
import jp.co.flight.incredist.android.internal.transport.mfi.MFiEmvSendArcCommand;
import jp.co.flight.incredist.android.internal.transport.mfi.MFiFelicaCloseCommand;
import jp.co.flight.incredist.android.internal.transport.mfi.MFiFelicaLedColorCommand;
import jp.co.flight.incredist.android.internal.transport.mfi.MFiFelicaOpenCommand;
import jp.co.flight.incredist.android.internal.transport.mfi.MFiFelicaOpenWithoutLedCommand;
import jp.co.flight.incredist.android.internal.transport.mfi.MFiFelicaSendCommand;
import jp.co.flight.incredist.android.internal.transport.mfi.MFiGetRealTimeCommand;
import jp.co.flight.incredist.android.internal.transport.mfi.MFiPinEntryDCommand;
import jp.co.flight.incredist.android.internal.transport.mfi.MFiScanCreditCardCommand;
import jp.co.flight.incredist.android.internal.transport.mfi.MFiScanMagneticCard2Command;
import jp.co.flight.incredist.android.internal.transport.mfi.MFiSetEncryptionModeCommand;
import jp.co.flight.incredist.android.internal.transport.mfi.MFiSetLedColorCommand;
import jp.co.flight.incredist.android.internal.transport.mfi.MFiSetRealTimeCommand;
import jp.co.flight.incredist.android.internal.transport.mfi.MFiStopCommand;
import jp.co.flight.incredist.android.internal.transport.mfi.MFiTfpmxDisplayMessageCommand;
import jp.co.flight.incredist.android.internal.transport.mfi.MFiTransport;
import jp.co.flight.incredist.android.model.CreditCardType;
import jp.co.flight.incredist.android.model.EmvSetupDataType;
import jp.co.flight.incredist.android.model.EmvTagType;
import jp.co.flight.incredist.android.model.EmvTransactionType;
import jp.co.flight.incredist.android.model.EncryptionMode;
import jp.co.flight.incredist.android.model.LedColor;
import jp.co.flight.incredist.android.model.PinEntry;

/**
 * BLE - MFi版 Incredist 用 Controller.
 */
public class IncredistMFiController implements IncredistProtocolController {

    @Nullable
    private IncredistController mController;

    private BluetoothGattConnection mConnection;

    @Nullable
    private MFiTransport mMFiTransport;

    /**
     * コンストラクタ
     *
     * @param controller IncredistController オブジェクト
     * @param connection BluetoothGattConnection オブジェクt
     */
    IncredistMFiController(@NonNull IncredistController controller, @NonNull BluetoothGattConnection connection) {
        mController = controller;
        mConnection = connection;
        mMFiTransport = new BleMFiTransport(connection);
        MFiCommand.setPacketLength(connection.getMtu() - 3);
    }

    /**
     * MFi コマンドを送信します
     *
     * @param command  送信コマンド
     * @param callback コールバック
     */
    private void postMFiCommand(final MFiCommand command, final IncredistController.Callback callback) {
        IncredistController controller2 = mController;
        if (controller2 != null) {
            controller2.postCommand(() -> {
                MFiTransport transport = mMFiTransport;
                if (mController != null && transport != null) {
                    final IncredistResult result = transport.sendCommand(command);

                    if (result.status == IncredistResult.STATUS_CANCELED && command.cancelable()) {
                        command.onCancelled(transport);
                    }

                    // sendCommand 処理中に release が呼ばれている場合があるので　controller を再チェックする
                    IncredistController controller = mController;
                    if (controller != null) {
                        controller.postCallback(() -> {
                            callback.onResult(result);
                        });
                    }
                }
            }, callback);
        }
    }

    /**
     * 複数の MFi コマンドを送信します
     *
     * @param commandList 送信コマンドリスト
     * @param callback    コールバック
     */
    private void postMFiCommandList(@NonNull final List<? extends MFiCommand> commandList, final IncredistController.Callback callback) {
        IncredistController controller3 = mController;
        if (controller3 != null) {
            controller3.postCommand(() -> {
                IncredistController controller2 = mController;
                MFiTransport transport = mMFiTransport;
                if (controller2 != null && transport != null) {
                    if (commandList.size() == 0) {
                        controller2.postCallback(() -> {
                            callback.onResult(new IncredistResult(IncredistResult.STATUS_INVALID_COMMAND));
                        });
                        return;
                    }

                    MFiCommand command = commandList.get(0);
                    final IncredistResult result = transport.sendCommand(commandList.toArray(new MFiCommand[0]));

                    if (result.status == IncredistResult.STATUS_CANCELED && command.cancelable()) {
                        command.onCancelled(transport);
                    }

                    // sendCommand 処理中に release が呼ばれている場合があるので　controller を再チェックする
                    IncredistController controller = mController;
                    if (controller != null) {
                        controller.postCallback(() -> {
                            callback.onResult(result);
                        });
                    }
                }
            }, callback);
        }
    }

    /**
     * コマンド処理中かどうかを返します
     *
     * @return 処理中の場合 true
     */
    @Override
    public boolean isBusy() {
        MFiTransport transport = mMFiTransport;
        if (transport != null) {
            return transport.isBusy();
        } else {
            return false;
        }
    }

    /**
     * シリアル番号を取得します.
     *
     * @param callback コールバック
     */
    @Override
    public void getDeviceInfo(final IncredistController.Callback callback) {
        postMFiCommand(new MFiDeviceInfoCommand(), callback);
    }

    /**
     * ブートローダのバージョンを取得します
     *
     * @param callback コールバック
     */
    @Override
    public void getBootloaderVersion(IncredistController.Callback callback) {
        postMFiCommand(new MFiBootloaderVersionCommand(), callback);
    }

    /**
     * EMV メッセージを表示します
     *
     * @param type     メッセージ番号
     * @param message  メッセージ文字列
     * @param callback コールバック
     */
    @Override
    public void emvDisplayMessage(int type, @Nullable String message, IncredistController.Callback callback) {
        postMFiCommand(new MFiEmvDisplayMessageCommand(type, message), callback);
    }

    /**
     * TFP メッセージを表示します
     *
     * @param type     メッセージ番号
     * @param message  メッセージ文字列
     * @param callback コールバック
     */
    @Override
    public void tfpDisplayMessage(int type, @Nullable String message, IncredistController.Callback callback) {
        postMFiCommand(new MFiTfpmxDisplayMessageCommand(type, message), callback);
    }

    /**
     * 暗号化モードを設定します
     *
     * @param mode     暗号化モード
     * @param callback コールバック
     */
    @Override
    public void setEncryptionMode(EncryptionMode mode, IncredistController.Callback callback) {
        postMFiCommand(new MFiSetEncryptionModeCommand(mode), callback);
    }

    /**
     * PIN 入力を行います
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
    @Override
    public void pinEntryD(PinEntry.Type pinType, PinEntry.Mode pinMode, PinEntry.MaskMode mask, int min, int max, PinEntry.Alignment align, int line, long timeout, IncredistController.Callback callback) {
        try {
            postMFiCommand(new MFiPinEntryDCommand(pinType, pinMode, mask, min, max, align, line, timeout), callback);
        } catch (ParameterException ex) {
            IncredistController controller = mController;
            if (controller != null) {
                controller.postCallback(() -> {
                    callback.onResult(new IncredistResult(IncredistResult.STATUS_PARAMETER_ERROR));
                });
            }
        }
    }

    /**
     * 磁気カードを読み取ります
     *
     * @param timeout  タイムアウト時間(msec)
     * @param callback コールバック
     */
    @Override
    public void scanMagneticCard(long timeout, IncredistController.Callback callback) {
        postMFiCommand(new MFiScanMagneticCard2Command(timeout), callback);
    }

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
    @Override
    public void scanCreditCard(EnumSet<CreditCardType> cardTypeSet, long amount, EmvTagType tagType, int aidSetting, EmvTransactionType transactionType, boolean fallback, long timeout, IncredistController.Callback callback) {
        postMFiCommand(new MFiScanCreditCardCommand(cardTypeSet, amount, tagType, aidSetting, transactionType, fallback, timeout), callback);
    }

    /**
     * LED色を設定します。
     *
     * @param color    LED色
     * @param isOn     true: 点灯 false: 消灯
     * @param callback コールバック
     */
    @Override
    public void setLedColor(LedColor color, boolean isOn, IncredistController.Callback callback) {
        postMFiCommand(new MFiSetLedColorCommand(color, isOn), callback);
    }

    /**
     * FeliCa RF モードを開始します
     *
     * @param withLed  LED を点灯するかどうか
     * @param callback コールバック
     */
    @Override
    public void felicaOpen(boolean withLed, final IncredistController.Callback callback) {
        MFiCommand command;
        if (withLed) {
            command = new MFiFelicaOpenCommand();
        } else {
            command = new MFiFelicaOpenWithoutLedCommand();
        }

        postMFiCommand(command, callback);
    }

    /**
     * FeliCa コマンドを送信します
     *
     * @param command  送信するFeliCaコマンド
     * @param wait     ウェイト(単位: msec)
     * @param callback コールバック
     */
    @Override
    public void felicaSendCommand(byte[] command, int wait, IncredistController.Callback callback) {
        postMFiCommand(new MFiFelicaSendCommand(wait, command), callback);
    }

    /**
     * felica モード時のLED色を設定します。
     *
     * @param color    LED色
     * @param callback コールバック
     */
    @Override
    public void felicaLedColor(LedColor color, IncredistController.Callback callback) {
        postMFiCommand(new MFiFelicaLedColorCommand(color), callback);
    }

    /**
     * FeliCa RF モードを終了します
     *
     * @param callback コールバック
     */
    @Override
    public void felicaClose(IncredistController.Callback callback) {
        postMFiCommand(new MFiFelicaCloseCommand(), callback);
    }

    /**
     * Incredistに設定されている時刻を取得します
     *
     * @param callback コールバック
     */
    @Override
    public void rtcGetTime(IncredistController.Callback callback) {
        postMFiCommand(new MFiGetRealTimeCommand(), callback);
    }

    /**
     * Incredist に時刻を設定します
     *
     * @param cal      設定時刻
     * @param callback コールバック
     */
    @Override
    public void rtcSetTime(Calendar cal, IncredistController.Callback callback) {
        postMFiCommand(new MFiSetRealTimeCommand(cal), callback);
    }

    /**
     * Incredist に現在時刻を設定します
     *
     * @param callback コールバック
     */
    @Override
    public void rtcSetCurrentTime(IncredistController.Callback callback) {
        postMFiCommand(new MFiSetRealTimeCommand(), callback);
    }

    /**
     * EMV kernel に setup データを送信します
     *
     * @param type      設定種別
     * @param setupData setupデータ
     * @param callback  コールバック
     */
    @Override
    public void emvKernelSetup(EmvSetupDataType type, byte[] setupData, IncredistController.Callback callback) {
        postMFiCommandList(MFiEmvKernelSetupCommand.createCommandList(type, setupData), callback);
    }

    /**
     * Incredist の EMVカーネル設定情報をチェックします
     *
     * @param type     設定種別
     * @param hashData 設定値のハッシュデータ
     * @param callback コールバック
     */
    @Override
    public void emvCheckKernelSetting(EmvSetupDataType type, byte[] hashData, IncredistController.Callback callback) {
        postMFiCommand(new MFiEmvCheckKernelSettingCommand(type, hashData), callback);
    }

    /**
     * EMV kernel に ARC データを送信します
     *
     * @param arcData  ARCデータ
     * @param callback コールバック
     */
    @Override
    public void emvSendArc(byte[] arcData, IncredistController.Callback callback) {
        postMFiCommandList(MFiEmvSendArcCommand.createCommandList(arcData), callback);
    }

    /**
     * icカードの挿入状態をチェックします
     *
     * @param callback コールバック
     */
    @Override
    public void emvCheckCardStatus(IncredistController.Callback callback) {
        postMFiCommand(new MFiEmvCardStatusCommand(), callback);
    }

    /**
     * 電子マネー向けの画面・LED点滅します
     *
     * @param isBlink  画面on の場合 true, off の場合 false を指定
     * @param color    LEDの点灯時の色
     * @param duration 点灯時間(msec)
     */
    @Override
    public void emoneyBlink(boolean isBlink, LedColor color, int duration, IncredistController.Callback callback) {
        postMFiCommand(new MFiEmoneyBlinkCommand(isBlink, color, duration), callback);
    }

    /**
     * 現在実行中のコマンドをキャンセルします
     *
     * @param callback コールバック
     */
    @Override
    public void cancel(IncredistController.Callback callback) {
        IncredistController controller2 = mController;
        if (controller2 != null) {
            controller2.postCancel(() -> {
                IncredistController controller = mController;
                MFiTransport transport = mMFiTransport;
                if (controller != null && transport != null) {
                    final IncredistResult result = transport.cancel();
                    controller.postCallback(() -> {
                        callback.onResult(result);
                    });
                }
            });
        }
    }

    /**
     * Incredist を停止します。
     *
     * @param callback コールバック
     */
    @Override
    public void stop(IncredistController.Callback callback) {
        postMFiCommand(new MFiStopCommand(), callback);
    }

    @Override
    public void disconnect() {
        mConnection.disconnect();
    }

    /**
     * オブジェクトを解放します
     */
    @Override
    public void release() {
        mConnection.close();
        mConnection = null;

        if (mMFiTransport != null) {
            mMFiTransport.release();
        }
        mController = null;
        mMFiTransport = null;
    }

}

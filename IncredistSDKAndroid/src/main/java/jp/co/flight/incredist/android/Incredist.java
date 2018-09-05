package jp.co.flight.incredist.android;

import android.bluetooth.BluetoothGatt;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbInterface;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.Calendar;
import java.util.EnumSet;

import jp.co.flight.android.bluetooth.le.BluetoothGattConnection;
import jp.co.flight.incredist.android.internal.controller.IncredistController;
import jp.co.flight.incredist.android.internal.controller.result.BlinkResult;
import jp.co.flight.incredist.android.internal.controller.result.BootloaderVersionResult;
import jp.co.flight.incredist.android.internal.controller.result.CardStatusResult;
import jp.co.flight.incredist.android.internal.controller.result.DeviceInfoResult;
import jp.co.flight.incredist.android.internal.controller.result.EmvArcResult;
import jp.co.flight.incredist.android.internal.controller.result.EmvCheckKernelSettingResult;
import jp.co.flight.incredist.android.internal.controller.result.EmvResult;
import jp.co.flight.incredist.android.internal.controller.result.IncredistResult;
import jp.co.flight.incredist.android.internal.controller.result.MagCardResult;
import jp.co.flight.incredist.android.internal.controller.result.PinEntryResult;
import jp.co.flight.incredist.android.internal.controller.result.RtcResult;
import jp.co.flight.incredist.android.model.BootloaderVersion;
import jp.co.flight.incredist.android.model.CreditCardType;
import jp.co.flight.incredist.android.model.DeviceInfo;
import jp.co.flight.incredist.android.model.EmvKernelSettingStatus;
import jp.co.flight.incredist.android.model.EmvPacket;
import jp.co.flight.incredist.android.model.EmvSetupDataType;
import jp.co.flight.incredist.android.model.EmvTagType;
import jp.co.flight.incredist.android.model.EmvTransactionType;
import jp.co.flight.incredist.android.model.EncryptionMode;
import jp.co.flight.incredist.android.model.FelicaCommandResult;
import jp.co.flight.incredist.android.model.ICCardStatus;
import jp.co.flight.incredist.android.model.LedColor;
import jp.co.flight.incredist.android.model.MagCard;
import jp.co.flight.incredist.android.model.PinEntry;
import jp.co.flight.incredist.android.model.ProductInfo;

/**
 * Incredist API クラス.
 */
@SuppressWarnings({"WeakerAccess", "unused"}) // for public API.
public class Incredist {
    private static final String TAG = "Incredist";
    /**
     * 生成元の IncredistManager インスタンス.
     */
    @Nullable
    private IncredistManager mManager;

    /**
     * IncredistController インスタンス.
     */
    @Nullable
    private IncredistController mController;

    /**
     * BLE 用コンストラクタ. IncredistManager によって呼び出されます.
     *
     * @param connection Bluetooth ペリフェラルとの接続オブジェクト
     * @param deviceName
     */
    Incredist(@NonNull IncredistManager manager, BluetoothGattConnection connection, String deviceName) {
        mManager = manager;
        mController = new IncredistController(connection, deviceName);
    }

    /**
     * USB 用コンストラクタ.
     *
     * @param connection   UsbDeviceConnection オブジェクト
     * @param usbInterface UsbInterface オブジェクト
     */
    public Incredist(@NonNull IncredistManager manager, UsbDeviceConnection connection, UsbInterface usbInterface) {
        mManager = manager;
        mController = new IncredistController(connection, usbInterface);
    }

    /**
     * Incredistとの接続を切断します.
     */
    @Deprecated
    public void disconnect(@Nullable OnSuccessFunction<Incredist> success, @Nullable OnFailureFunction failure) {
        if (mManager != null && mController != null) {
            mManager.setupDisconnectV1(this, success, failure);
            mController.disconnect(result -> {
                if (result.status != IncredistResult.STATUS_SUCCESS) {
                    if (failure != null) {
                        failure.onFailure(result.status);
                    }
                }
            });
        }
    }

    /**
     * Incredistとの接続を切断します.
     */
    public void disconnect() {
        if (mController != null) {
            mController.cancel(result -> {
                mController.disconnect(result2 -> {
                    // コールバックでは特に処理不要
                });
            });
        }
    }

    /**
     * 接続中のIncredistデバイス名を取得します.
     *
     * @return Incredistデバイス名
     */
    public String getDeviceName() {
        if (mController != null) {
            return mController.getDeviceName();
        } else {
            return "";
        }
    }

    /**
     * Bluetooth の接続状態を取得します。
     *
     * @return 接続状態(接続中 BluetoothGatt.STATE_CONNECTED, 切断時 BluetoothGatt.STATE_DISCONNECTED)
     */
    public int getConnectionState() {
        if (mController != null) {
            return mController.getConnectionState();
        } else {
            return BluetoothGatt.STATE_DISCONNECTED;
        }
    }

    /**
     * シリアル番号を取得します.
     *
     * @param success 取得成功時の処理
     * @param failure 取得失敗時の処理
     */
    public void getSerialNumber(@Nullable OnSuccessFunction<String> success, @Nullable OnFailureFunction failure) {
        if (mController != null) {
            mController.getDeviceInfo(result -> {
                if (result.status == IncredistResult.STATUS_SUCCESS && result instanceof DeviceInfoResult) {
                    if (success != null) {
                        success.onSuccess(((DeviceInfoResult) result).serialNumber);
                    }
                } else {
                    if (failure != null) {
                        failure.onFailure(result.status);
                    }
                }
            });
        }
    }

    /**
     * デバイス情報を取得します。
     *
     * @param success 取得成功時の処理
     * @param failure 取得失敗時の処理
     */
    public void getDeviceInfo(@Nullable OnSuccessFunction<DeviceInfo> success, @Nullable OnFailureFunction failure) {
        if (mController != null) {
            mController.getDeviceInfo(result -> {
                if (result.status == IncredistResult.STATUS_SUCCESS && result instanceof DeviceInfoResult) {
                    if (success != null) {
                        success.onSuccess(((DeviceInfoResult) result).toDeviceInfo());
                    }
                } else {
                    if (failure != null) {
                        failure.onFailure(result.status);
                    }
                }
            });
        }
    }

    /**
     * プロダクト情報を取得します
     *
     * @param success 取得成功時の処理
     * @param failure 取得失敗時の処理
     */
    public void getProductInfo(@Nullable OnSuccessFunction<ProductInfo> success, @Nullable OnFailureFunction failure) {
        getDeviceInfo((deviceInfo) -> {
            if (success != null) {
                success.onSuccess(new ProductInfo(deviceInfo));
            }
        }, failure);
    }

    /**
     * ブートローダのバージョン情報を取得します
     *
     * @param success 取得成功時の処理
     * @param failure 取得失敗時の処理
     */
    public void getBootloaderVersion(@Nullable OnSuccessFunction<BootloaderVersion> success, @Nullable OnFailureFunction failure) {
        if (mController != null) {
            mController.getBootloaderVersion(result -> {
                if (result.status == IncredistResult.STATUS_SUCCESS && result instanceof BootloaderVersionResult) {
                    if (success != null) {
                        success.onSuccess(((BootloaderVersionResult) result).toBootloaderVersion());
                    }
                } else {
                    if (failure != null) {
                        failure.onFailure(result.status);
                    }
                }
            });
        }
    }

    /**
     * EMV メッセージを表示します
     *
     * @param type    メッセージ番号
     * @param message メッセージ文字列
     * @param success 成功時処理
     * @param failure 失敗時処理
     */
    public void emvDisplayMessage(int type, @Nullable String message, @Nullable OnSuccessVoidFunction success, @Nullable OnFailureFunction failure) {
        if (mController != null) {
            mController.emvDisplayMessage(type, message, result -> {
                if (result.status == IncredistResult.STATUS_SUCCESS) {
                    if (success != null) {
                        success.onSuccess();
                    }
                } else {
                    if (failure != null) {
                        failure.onFailure(result.status);
                    }
                }
            });
        }
    }

    /**
     * EMV メッセージを表示します
     *
     * @param type    メッセージ番号
     * @param success 成功時処理
     * @param failure 失敗時処理
     */
    public void emvDisplayMessage(int type, @Nullable OnSuccessVoidFunction success, @Nullable OnFailureFunction failure) {
        emvDisplayMessage(type, null, success, failure);
    }

    /**
     * TFP メッセージを表示します
     *
     * @param type    メッセージ番号
     * @param message メッセージ文字列
     * @param success 成功時処理
     * @param failure 失敗時処理
     */
    public void tfpDisplayMessage(int type, @Nullable String message, @Nullable OnSuccessVoidFunction success, @Nullable OnFailureFunction failure) {
        if (mController != null) {
            mController.tfpDisplayMessage(type, message, result -> {
                if (result.status == IncredistResult.STATUS_SUCCESS) {
                    if (success != null) {
                        success.onSuccess();
                    }
                } else {
                    if (failure != null) {
                        failure.onFailure(result.status);
                    }
                }
            });
        }
    }

    /**
     * TFP メッセージを表示します
     *
     * @param type    メッセージ番号
     * @param success 成功時処理
     * @param failure 失敗時処理
     */
    public void tfpDisplayMessage(int type, @Nullable OnSuccessVoidFunction success, @Nullable OnFailureFunction failure) {
        tfpDisplayMessage(type, null, success, failure);
    }

    /**
     * 暗号化モードを設定します
     *
     * @param mode    暗号化モード
     * @param success 設定成功時処理
     * @param failure 設定失敗時処理
     */
    public void setEncryptionMode(EncryptionMode mode, @Nullable OnSuccessVoidFunction success, @Nullable OnFailureFunction failure) {
        if (mController != null) {
            mController.setEncryptionMode(mode, result -> {
                if (result.status == IncredistResult.STATUS_SUCCESS) {
                    if (success != null) {
                        success.onSuccess();
                    }
                } else {
                    if (failure != null) {
                        failure.onFailure(result.status);
                    }
                }
            });
        }
    }

    /**
     * PIN入力を行います(D向け)
     *
     * @param pinType PIN入力タイプ
     * @param pinMode PIN暗号化モード
     * @param mask    表示マスク
     * @param min     最小桁数
     * @param max     最大桁数
     * @param align   表示左右寄せ
     * @param line    表示行
     * @param timeout タイムアウト時間(msec)
     * @param success 成功時処理
     * @param failure 失敗時処理
     */
    public void pinEntryD(PinEntry.Type pinType, PinEntry.Mode pinMode, PinEntry.MaskMode mask, int min, int max, PinEntry.Alignment align, int line, long timeout,
                          @Nullable OnSuccessFunction<PinEntry.Result> success, @Nullable OnFailureFunction failure) {
        if (mController != null) {
            mController.pinEntryD(pinType, pinMode, mask, min, max, align, line, timeout, result -> {
                if (result.status == IncredistResult.STATUS_SUCCESS && result instanceof PinEntryResult) {
                    if (success != null) {
                        success.onSuccess(new PinEntry.Result((PinEntryResult) result));
                    }
                } else {
                    if (failure != null) {
                        failure.onFailure(result.status);
                    }
                }
            });
        }
    }

    /**
     * 磁気カード読み取り
     *
     * @param timeout タイムアウト時間(msec)
     * @param success 成功時処理
     * @param failure 失敗時処理
     */
    public void scanMagneticCard(long timeout, @Nullable OnSuccessFunction<MagCard> success, @Nullable OnFailureFunction failure) {
        if (mController != null) {
            mController.scanMagneticCard(timeout, result -> {
                if (result.status == IncredistResult.STATUS_SUCCESS && result instanceof MagCardResult) {
                    if (success != null) {
                        success.onSuccess(((MagCardResult) result).toMagCard());
                    }
                } else {
                    if (failure != null) {
                        failure.onFailure(result.status);
                    }
                }
            });
        }
    }

    /**
     * 決済処理を実行します
     *
     * @param cardTypeSet     カード種別
     * @param amount          決済金額
     * @param tagType         暗号化タグ種別(カード種別が MSR の場合は AllTag を指定する必要がある)
     * @param aidSetting      AID設定
     * @param transactionType トランザクション種別
     * @param fallback        フォールバック処理を実行するかどうか
     * @param timeout         タイムアウト時間(msec)
     * @param emvSuccess      ICカード決済成功時処理
     * @param magSuccess      磁気カード決済成功時処理
     * @param failure         決済失敗時処理　// TODO コールバックインタフェースを専用に作る方がよいかも
     */
    public void scanCreditCard(EnumSet<CreditCardType> cardTypeSet, long amount, EmvTagType tagType,
                               int aidSetting, EmvTransactionType transactionType, boolean fallback, long timeout,
                               @Nullable OnSuccessFunction<EmvPacket> emvSuccess,
                               @Nullable OnSuccessFunction<MagCard> magSuccess,
                               @Nullable OnFailureFunction failure) {
        if (mController != null) {
            mController.scanCreditCard(cardTypeSet, amount, tagType, aidSetting, transactionType, fallback, timeout, result -> {
                if (result.status == IncredistResult.STATUS_SUCCESS) {
                    if (result instanceof EmvResult) {
                        if (emvSuccess != null) {
                            emvSuccess.onSuccess(((EmvResult) result).toEmvPacket());
                        }
                    } else if (result instanceof MagCardResult) {
                        if (magSuccess != null) {
                            magSuccess.onSuccess(((MagCardResult) result).toMagCard());
                        }
                    }
                } else {
                    if (failure != null) {
                        failure.onFailure(result.status);
                    }
                }
            });
        }
    }

    /**
     * LED色を設定します。
     *
     * @param color   LED色
     * @param isOn    true: 点灯 false: 消灯
     * @param success 成功時処理
     * @param failure 失敗時処理
     */
    public void setLedColor(LedColor color, boolean isOn, @Nullable OnSuccessVoidFunction success, @Nullable OnFailureFunction failure) {
        if (mController != null) {
            mController.setLedColor(color, isOn, result -> {
                if (result.status == IncredistResult.STATUS_SUCCESS) {
                    if (success != null) {
                        success.onSuccess();
                    }
                } else {
                    if (failure != null) {
                        failure.onFailure(result.status);
                    }
                }
            });
        }
    }

    /**
     * FeliCa アクセスのため、デバイスを FeliCa RF モードにします.
     *
     * @param withLed LED を点灯するかどうか
     * @param success 設定成功時の処理
     * @param failure 設定失敗時の処理
     */
    public void felicaOpen(boolean withLed, @Nullable OnSuccessVoidFunction success, @Nullable OnFailureFunction failure) {
        if (mController != null) {
            mController.felicaOpen(withLed, result -> {
                if (result.status == IncredistResult.STATUS_SUCCESS) {
                    if (success != null) {
                        success.onSuccess();
                    }
                } else {
                    if (failure != null) {
                        failure.onFailure(result.status);
                    }
                }
            });
        }
    }

    /**
     * FeliCa アクセスのため、デバイスを FeliCa RF モードにし、LEDを点灯します.
     *
     * @param success 設定成功時の処理
     * @param failure 設定失敗時の処理
     */
    public void felicaOpen(@Nullable OnSuccessVoidFunction success, @Nullable OnFailureFunction failure) {
        felicaOpen(true, success, failure);
    }

    /**
     * felica モード時のLED色を設定します。
     *
     * @param color   LED色
     * @param success 成功時処理
     * @param failure 失敗時処理
     */
    public void felicaLedColor(LedColor color, @Nullable OnSuccessVoidFunction success, @Nullable OnFailureFunction failure) {
        if (mController != null) {
            mController.felicaLedColor(color, result -> {
                if (result.status == IncredistResult.STATUS_SUCCESS) {
                    if (success != null) {
                        success.onSuccess();
                    }
                } else {
                    if (failure != null) {
                        failure.onFailure(result.status);
                    }
                }
            });
        }
    }

    /**
     * FeliCa コマンドを送信します.
     *
     * @param felicaCommand FeliCaコマンドのバイト列
     * @param wait          ウェイト(単位: msec)
     * @param success       送信成功時の処理
     * @param failure       送信失敗時の処理
     */
    public void felicaSendCommand(byte[] felicaCommand, int wait, @Nullable OnSuccessFunction<FelicaCommandResult> success, @Nullable OnFailureFunction failure) {
        if (mController != null) {
            mController.felicaSendCommand(felicaCommand, wait, result -> {
                if (result.status == IncredistResult.STATUS_SUCCESS && result instanceof jp.co.flight.incredist.android.internal.controller.result.FelicaCommandResult) {
                    jp.co.flight.incredist.android.internal.controller.result.FelicaCommandResult felicaResult = (jp.co.flight.incredist.android.internal.controller.result.FelicaCommandResult) result;
                    if (success != null) {
                        success.onSuccess(new FelicaCommandResult(felicaResult));
                    }
                } else {
                    if (failure != null) {
                        failure.onFailure(result.status);
                    }
                }
            });
        }
    }

    /**
     * FeliCa RF モードを終了します.
     *
     * @param success 設定成功時の処理
     * @param failure 設定失敗時の処理
     */
    public void felicaClose(@Nullable OnSuccessVoidFunction success, @Nullable OnFailureFunction failure) {
        if (mController != null) {
            mController.felicaClose(result -> {
                if (result.status == IncredistResult.STATUS_SUCCESS) {
                    if (success != null) {
                        success.onSuccess();
                    }
                } else {
                    if (failure != null) {
                        failure.onFailure(result.status);
                    }
                }
            });
        }
    }

    /**
     * Incredistに設定されている時刻を取得します
     *
     * @param success 取得成功時の処理
     * @param failure 取得失敗時の処理
     */
    public void rtcGetTime(@Nullable OnSuccessFunction<Calendar> success, @Nullable OnFailureFunction failure) {
        if (mController != null) {
            mController.rtcGetTime(result -> {
                if (result.status == IncredistResult.STATUS_SUCCESS && result instanceof RtcResult) {
                    if (success != null) {
                        success.onSuccess(((RtcResult) result).calendar);
                    }
                } else {
                    if (failure != null) {
                        failure.onFailure(result.status);
                    }
                }
            });
        }
    }

    /**
     * Incredist に時刻を設定します
     *
     * @param cal     設定時刻
     * @param success 設定成功時処理
     * @param failure 設定失敗時処理
     */
    public void rtcSetTime(Calendar cal, @Nullable OnSuccessVoidFunction success, @Nullable OnFailureFunction failure) {
        if (mController != null) {
            mController.rtcSetTime(cal, result -> {
                if (result.status == IncredistResult.STATUS_SUCCESS) {
                    if (success != null) {
                        success.onSuccess();
                    }
                } else {
                    if (failure != null) {
                        failure.onFailure(result.status);
                    }
                }
            });
        }
    }

    /**
     * Incredist に現在時刻を設定します
     *
     * @param success 設定成功時処理
     * @param failure 設定失敗時処理
     */
    public void rtcSetCurrentTime(@Nullable OnSuccessVoidFunction success, @Nullable OnFailureFunction failure) {
        if (mController != null) {
            mController.rtcSetCurrentTime(result -> {
                if (result.status == IncredistResult.STATUS_SUCCESS) {
                    if (success != null) {
                        success.onSuccess();
                    }
                } else {
                    if (failure != null) {
                        failure.onFailure(result.status);
                    }
                }
            });
        }
    }

    /**
     * Incredist の EMVカーネル設定情報を送信します
     *
     * @param type      設定種別
     * @param setupData 設定データ
     * @param success   成功時処理
     * @param failure   失敗時処理
     */
    public void emvKernelSetup(EmvSetupDataType type, byte[] setupData, @Nullable OnSuccessVoidFunction success, @Nullable OnFailureFunction failure) {
        if (mController != null) {
            mController.emvKernelSetup(type, setupData, result -> {
                if (result.status == IncredistResult.STATUS_SUCCESS) {
                    if (success != null) {
                        success.onSuccess();
                    }
                } else {
                    if (failure != null) {
                        failure.onFailure(result.status);
                    }
                }
            });
        }
    }

    /**
     * Incredist の EMVカーネル設定情報をチェックします
     *
     * @param type     設定種別
     * @param hashData 設定値のハッシュデータ
     * @param success  成功時処理
     * @param failure  失敗時処理
     */
    public void emvCheckKernelSetting(EmvSetupDataType type, byte[] hashData, @Nullable OnSuccessFunction<EmvKernelSettingStatus> success, @Nullable OnFailureFunction failure) {
        if (mController != null) {
            mController.emvCheckKernelSetting(type, hashData, result -> {
                if (result.status == IncredistResult.STATUS_SUCCESS && result instanceof EmvCheckKernelSettingResult) {
                    if (success != null) {
                        success.onSuccess(((EmvCheckKernelSettingResult) result).isMatched ? EmvKernelSettingStatus.MATCHED : EmvKernelSettingStatus.UNMATCHED);
                    }
                } else {
                    if (failure != null) {
                        failure.onFailure(result.status);
                    }
                }
            });
        }
    }

    /**
     * EMV kernel に ARC データを送信します
     *
     * @param arcData カードへの送信データ
     * @param success 送信成功時処理
     * @param failure 失敗時処理
     */
    public void emvSendArc(byte[] arcData, @Nullable OnSuccessFunction<EmvPacket> success, @Nullable OnFailureFunction failure) {
        if (mController != null) {
            mController.emvSendArc(arcData, result -> {
                if (result.status == IncredistResult.STATUS_SUCCESS && result instanceof EmvArcResult) {
                    if (success != null) {
                        success.onSuccess(((EmvArcResult) result).toEmvPacket());
                    }
                } else {
                    if (failure != null) {
                        failure.onFailure(result.status);
                    }
                }
            });
        }
    }

    /**
     * icカードが挿入されているかどうかチェックします。
     * 挿入されている場合 success に指定したメソッドに true が返されます。
     *
     * @param success チェック成功時処理
     * @param failure 失敗時処理
     */
    public void emvCheckCardStatus(@Nullable OnSuccessFunction<ICCardStatus> success, @Nullable OnFailureFunction failure) {
        if (mController != null) {
            mController.emvCheckCardStatus(result -> {
                if (result.status == IncredistResult.STATUS_SUCCESS && result instanceof CardStatusResult) {
                    if (success != null) {
                        success.onSuccess(((CardStatusResult) result).isInserted ? ICCardStatus.INSERTED : ICCardStatus.REMOVED);
                    }
                } else {
                    if (failure != null) {
                        failure.onFailure(result.status);
                    }
                }
            });
        }
    }

    /**
     * 電子マネー向けの画面・LED点滅します
     *
     * @param isBlink  画面点滅開始の場合 true, 点滅停止の場合 false を指定
     * @param color    LEDの点灯時の色
     * @param duration 点灯時間(msec)
     */
    public void emoneyBlink(boolean isBlink, LedColor color, int duration, OnSuccessFunction<Boolean> success, OnFailureFunction failure) {
        if (mController != null) {
            mController.emoneyBlink(isBlink, color, duration, result -> {
                if (result.status == IncredistResult.STATUS_SUCCESS && result instanceof BlinkResult) {
                    if (success != null) {
                        success.onSuccess(((BlinkResult) result).isOn);
                    }
                } else {
                    if (failure != null) {
                        failure.onFailure(result.status);
                    }
                }
            });
        }
    }

    /**
     * Incredist で処理中のコマンドをキャンセルします
     *
     * @param success キャンセル成功時の処理
     * @param failure キャンセル失敗時の処理
     */
    public void cancel(OnSuccessVoidFunction success, OnFailureFunction failure) {
        if (mController != null) {
            mController.cancel(result -> {
                if (result.status == IncredistResult.STATUS_SUCCESS) {
                    if (success != null) {
                        success.onSuccess();
                    }
                } else {
                    if (failure != null) {
                        failure.onFailure(result.status);
                    }
                }
            });
        }
    }

    /**
     * Incredist を停止します。
     *
     * @param success 設定成功時の処理
     * @param failure 設定失敗時の処理
     */
    public void stop(@Nullable OnSuccessVoidFunction success, @Nullable OnFailureFunction failure) {
        if (mController != null) {
            mController.stop(result -> {
                if (result.status == IncredistResult.STATUS_SUCCESS) {
                    if (success != null) {
                        success.onSuccess();
                    }
                } else {
                    if (failure != null) {
                        failure.onFailure(result.status);
                    }
                }
            });
        }
    }

    /**
     * Incredist との接続リソースを解放します
     */
    public void release() {
        if (mController != null) {
            mController.close();
            mController.release();
        }

        mController = null;
        mManager = null;
    }

    /**
     * Incredist との接続リソースを解放します
     */
    public void refreshAndRelease() {
        if (mController != null) {
            mController.refreshAndClose();
            mController.release();
        }

        mController = null;
        mManager = null;
    }

}

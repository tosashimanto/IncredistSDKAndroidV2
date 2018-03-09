package jp.co.flight.incredist.android;

import android.support.annotation.Nullable;

import java.util.Calendar;
import java.util.EnumSet;

import jp.co.flight.android.bluetooth.le.BluetoothGattConnection;
import jp.co.flight.incredist.android.internal.controller.IncredistController;
import jp.co.flight.incredist.android.internal.controller.result.DeviceInfoResult;
import jp.co.flight.incredist.android.internal.controller.result.EmvResult;
import jp.co.flight.incredist.android.internal.controller.result.IncredistResult;
import jp.co.flight.incredist.android.internal.controller.result.MagCardResult;
import jp.co.flight.incredist.android.internal.controller.result.PinEntryResult;
import jp.co.flight.incredist.android.internal.controller.result.RtcResult;
import jp.co.flight.incredist.android.model.CreditCardType;
import jp.co.flight.incredist.android.model.DeviceInfo;
import jp.co.flight.incredist.android.model.EmvPacket;
import jp.co.flight.incredist.android.model.EmvTagType;
import jp.co.flight.incredist.android.model.EncryptionMode;
import jp.co.flight.incredist.android.model.FelicaCommandResult;
import jp.co.flight.incredist.android.model.LedColor;
import jp.co.flight.incredist.android.model.MagCard;
import jp.co.flight.incredist.android.model.PinEntry;

/**
 * Incredist API クラス.
 */
@SuppressWarnings({"WeakerAccess", "unused"}) // for public API.
public class Incredist {
    private static final String TAG = "Incredist";
    /**
     * 生成元の IncredistManager インスタンス.
     */
    private IncredistManager mManager;

    /**
     * IncredistController インスタンス.
     */
    private IncredistController mController;

    /**
     * コンストラクタ. IncredistManager によって呼び出されます.
     *
     * @param connection Bluetooth ペリフェラルとの接続オブジェクト
     */
    Incredist(IncredistManager manager, BluetoothGattConnection connection, String deviceName) {
        mManager = manager;
        mController = new IncredistController(connection, deviceName);
    }

    /**
     * Incredistとの接続を切断します.
     */
    public void disconnect(@Nullable OnSuccessFunction<Incredist> success, @Nullable OnFailureFunction failure) {
        mManager.setupDisconnect(this, success, failure);
        mController.disconnect(result -> {
            if (result.status != IncredistResult.STATUS_SUCCESS) {
                if (failure != null) {
                    failure.onFailure(result.status);
                }
            }
        });
    }

    /**
     * 接続中のIncredistデバイス名を取得します.
     *
     * @return Incredistデバイス名
     */
    public String getDeviceName() {
        return mController.getDeviceName();
    }

    /**
     * Bluetooth の接続状態を取得します.
     *
     * @return 接続状態(BluetoothGatt クラスの定数)
     */
    public int getConnectionState() {
        return mController.getConnectionState();
    }

    /**
     * シリアル番号を取得します.
     *
     * @param success 取得成功時の処理
     * @param failure 取得失敗時の処理
     */
    public void getSerialNumber(@Nullable OnSuccessFunction<String> success, @Nullable OnFailureFunction failure) {
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

    /**
     * デバイス情報を取得します。
     *
     * @param success 取得成功時の処理
     * @param failure 取得失敗時の処理
     */
    public void getDeviceInfo(@Nullable OnSuccessFunction<DeviceInfo> success, @Nullable OnFailureFunction failure) {
        mController.getDeviceInfo(result -> {
            if (result.status == IncredistResult.STATUS_SUCCESS && result instanceof DeviceInfoResult) {
                if (success != null) {
                    success.onSuccess(new DeviceInfo((DeviceInfoResult) result));
                }
            } else {
                if (failure != null) {
                    failure.onFailure(result.status);
                }
            }
        });
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
        mController.emvDisplaymessage(type, message, result -> {
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
        mController.tfpDisplaymessage(type, message, result -> {
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

    /**
     * 磁気カード読み取り
     *
     * @param timeout タイムアウト時間(msec)
     * @param success 成功時処理
     * @param failure 失敗時処理
     */
    public void scanMagneticCard(long timeout, @Nullable OnSuccessFunction<MagCard> success, @Nullable OnFailureFunction failure) {
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

    /**
     * 決済処理を実行します
     *
     * @param cardTypeSet カード種別
     * @param amount      決済金額
     * @param tagType     暗号化タグ種別(カード種別が MSR の場合は AllTag を指定する必要がある)
     * @param timeout     タイムアウト時間(msec)
     * @param emvSuccess  ICカード決済成功時処理
     * @param magSuccess  磁気カード決済成功時処理
     * @param failure     決済失敗時処理　// TODO コールバックインタフェースを専用に作る方がよいかも
     */
    public void scanCreditCard(EnumSet<CreditCardType> cardTypeSet, long amount, EmvTagType tagType, long timeout,
                               @Nullable OnSuccessFunction<EmvPacket> emvSuccess,
                               @Nullable OnSuccessFunction<MagCard> magSuccess,
                               @Nullable OnFailureFunction failure) {
        mController.scanCreditCard(cardTypeSet, amount, tagType, timeout, result -> {
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

    /**
     * LED色を設定します。
     *
     * @param color   LED色
     * @param isOn    true: 点灯 false: 消灯
     * @param success 成功時処理
     * @param failure 失敗時処理
     */
    public void setLedColor(LedColor color, boolean isOn, @Nullable OnSuccessVoidFunction success, @Nullable OnFailureFunction failure) {
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


    /**
     * FeliCa アクセスのため、デバイスを FeliCa RF モードにします.
     *
     * @param withLed LED を点灯するかどうか
     * @param success 設定成功時の処理
     * @param failure 設定失敗時の処理
     */
    public void felicaOpen(boolean withLed, @Nullable OnSuccessVoidFunction success, @Nullable OnFailureFunction failure) {
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
    public void feliaLedColor(LedColor color, @Nullable OnSuccessVoidFunction success, @Nullable OnFailureFunction failure) {
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

    /**
     * FeliCa コマンドを送信します.
     *
     * @param felicaCommand FeliCaコマンドのバイト列
     * @param success       送信成功時の処理
     * @param failure       送信失敗時の処理
     */
    public void felicaSendCommand(byte[] felicaCommand, @Nullable OnSuccessFunction<FelicaCommandResult> success, @Nullable OnFailureFunction failure) {
        mController.felicaSendCommand(felicaCommand, result -> {
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

    /**
     * FeliCa RF モードを終了します.
     *
     * @param success 設定成功時の処理
     * @param failure 設定失敗時の処理
     */
    public void felicaClose(@Nullable OnSuccessVoidFunction success, @Nullable OnFailureFunction failure) {
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

    /**
     * Incredistに設定されている時刻を取得します
     *
     * @param success 取得成功時の処理
     * @param failure 取得失敗時の処理
     */
    public void rtcGetTime(@Nullable OnSuccessFunction<Calendar> success, @Nullable OnFailureFunction failure) {
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

    /**
     * Incredist に時刻を設定します
     *
     * @param cal     設定時刻
     * @param success 設定成功時処理
     * @param failure 設定失敗時処理
     */
    public void rtcSetTime(Calendar cal, @Nullable OnSuccessVoidFunction success, @Nullable OnFailureFunction failure) {
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

    /**
     * Incredist に現在時刻を設定します
     *
     * @param success 設定成功時処理
     * @param failure 設定失敗時処理
     */
    public void rtcSetCurrentTime(@Nullable OnSuccessVoidFunction success, @Nullable OnFailureFunction failure) {
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

    /**
     * EMV kernel に ARC データを送信します
     */
    public void emvSendArc(byte[] arcData, @Nullable OnSuccessVoidFunction success, @Nullable OnFailureFunction failure) {
        mController.emvSendArc(arcData, result -> {
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

    /**
     * Incredist で処理中のコマンドをキャンセルします
     *
     * @param success キャンセル成功時の処理
     * @param failure キャンセル失敗時の処理
     */
    public void cancel(OnSuccessVoidFunction success, OnFailureFunction failure) {
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

    /**
     * Incredist を停止します。
     *
     * @param success 設定成功時の処理
     * @param failure 設定失敗時の処理
     */
    public void stop(@Nullable OnSuccessVoidFunction success, @Nullable OnFailureFunction failure) {
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

    /**
     * Incredist との接続リソースを解放します
     */
    public void release() {
        mController.close();
        mController.release();

        mController = null;
        mManager = null;
    }

    /**
     * Incredist との接続リソースを解放します
     */
    public void refreshAndRelease() {
        mController.refreshAndClose();
        mController.release();

        mController = null;
        mManager = null;
    }

}

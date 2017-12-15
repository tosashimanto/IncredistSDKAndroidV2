package jp.co.flight.incredist.android.internal.controller.command;

import android.support.annotation.NonNull;

import java.util.Arrays;

import jp.co.flight.incredist.android.internal.controller.result.IncredistResult;
import jp.co.flight.incredist.android.internal.controller.result.PinEntryResult;
import jp.co.flight.incredist.android.internal.exception.ParameterException;
import jp.co.flight.incredist.android.internal.transport.mfi.MFiCommand;
import jp.co.flight.incredist.android.internal.transport.mfi.MFiResponse;
import jp.co.flight.incredist.android.model.PinEntry;

/**
 * MFi版 PIN入力(docomo用)送信コマンド
 * なお、本コマンドは IncredistPremium では対応していない
 */
public class MFiPinEntryDCommand extends MFiCommand {
    private static final byte[] PIND_HEADER = {'p', 'i', 'n', 'd'};
    private static final int KSN_LENGTH = 10;

    private final long mTimeout;

    private static byte[] createPayload(PinEntry.Type pinType, PinEntry.Mode pinMode, PinEntry.MaskMode mask, int min, int max, PinEntry.Alignment align, int line, long timeout) throws ParameterException {
        // CHECKSTYLE:OFF MagicNumber
        if (timeout < 20 * 1000L || timeout > 240 * 1000L) {
            throw new ParameterException();
        }

        if (line < 1 || line > 4) {
            throw new ParameterException();
        }

        if (max < min) {
            throw new ParameterException();
        }

        if (pinType == PinEntry.Type.ISO9564 && max > 12) {
            throw new ParameterException();
        }

        if (mask == PinEntry.MaskMode.NoneWithComma && max > 15) {
            throw new ParameterException();
        }

        if (pinMode == PinEntry.Mode.DebitScramble && (min != 4 || max != 4)) {
            throw new ParameterException();
        }

        if (max > 20 || min < 0) {
            throw new ParameterException();
        }

        byte[] payload = new byte[PIND_HEADER.length + 8];
        System.arraycopy(PIND_HEADER, 0, payload, 0, PIND_HEADER.length);
        payload[4] = pinType.getValue();
        payload[5] = pinMode.getValue();
        payload[6] = mask.getValue();
        payload[7] = (byte) (min & 0xff);
        payload[8] = (byte) (max & 0xff);
        payload[9] = align.getValue();
        payload[10] = (byte) (line & 0xff);
        payload[11] = (byte) (timeout / 1000 & 0xff);
        // CHECKSTYLE:ON MagicNumber

        return payload;
    }

    public MFiPinEntryDCommand(PinEntry.Type pinType, PinEntry.Mode pinMode, PinEntry.MaskMode mask, int min, int max, PinEntry.Alignment align, int line, long timeout) throws ParameterException {
        super(createPayload(pinType, pinMode, mask, min, max, align, line, timeout));
        mTimeout = timeout;
    }

    @Override
    public long getResponseTimeout() {
        return mTimeout;
    }

    @Override
    public boolean cancelable() {
        return true;
    }

    @NonNull
    @Override
    protected IncredistResult parseMFiResponse(MFiResponse response) {
        byte[] data = response.getData();

        if (data != null) {
            // CHECKSTYLE:OFF MagicNumber
            int status = data[0];

            if (status == 1 && data.length > 1 + KSN_LENGTH) {
                // 正常
                int pinLength = data[1 + KSN_LENGTH];
                if (data.length == 1 + KSN_LENGTH + 1 + pinLength) {
                    return new PinEntryResult(Arrays.copyOfRange(data, 1, 1 + KSN_LENGTH), Arrays.copyOfRange(data, 1 + KSN_LENGTH + 1, 1 + KSN_LENGTH + 1 + pinLength));
                }
            } else if (status == 0) {
                // タイムアウト
                return new IncredistResult(IncredistResult.STATUS_PIN_TIMEOUT);
            } else if (status == 2) {
                // キャンセルボタン
                return new IncredistResult(IncredistResult.STATUS_PIN_CANCEL);
            } else if (status == 3) {
                // スキップ
                return new IncredistResult(IncredistResult.STATUS_PIN_SKIP);
            }
            // CHECKSTYLE:ON MagicNumber
        }

        return new IncredistResult(IncredistResult.STATUS_INVALID_RESPONSE);
    }
}

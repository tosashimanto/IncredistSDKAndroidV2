package jp.co.flight.incredist.android.internal.transport.mfi;

import android.support.annotation.NonNull;

import java.util.Arrays;

import jp.co.flight.incredist.android.internal.controller.result.IncredistResult;
import jp.co.flight.incredist.android.internal.controller.result.PinEntryResult;
import jp.co.flight.incredist.android.internal.exception.ParameterException;
import jp.co.flight.incredist.android.model.PinEntry;

/**
 * MFi版 PIN入力(iD用)送信コマンド
 * なお、本コマンドは IncredistPremium では対応していない
 */
public class MFiPinEntryICommand extends MFiCommand {
    private static final byte[] PIND_HEADER = {'p', 'i', 'n', 'i'};
    private static final int KSN_LENGTH = 10;

    private static byte[] createPayload(PinEntry.Type pinType) throws ParameterException {
        // CHECKSTYLE:OFF MagicNumber

        if (pinType != PinEntry.Type.PlainText && pinType != PinEntry.Type.ISO9564 && pinType != PinEntry.Type.JMUPS ) {
            throw new ParameterException();
        }

        byte[] payload = new byte[PIND_HEADER.length + 1];
        System.arraycopy(PIND_HEADER, 0, payload, 0, PIND_HEADER.length);
        payload[4] = pinType.getValue();
        // CHECKSTYLE:ON MagicNumber

        return payload;
    }

    public MFiPinEntryICommand(PinEntry.Type pinType) throws ParameterException {
        super(createPayload(pinType));
    }

    @Override
    public long getResponseTimeout() {
        return Long.MAX_VALUE;
    }

    @Override
    public boolean cancelable() {
        return false;
    }

    @NonNull
    @Override
    protected IncredistResult parseMFiResponse(MFiResponse response) {
        byte[] data = response.getData();

        if (data != null) {
            if (data.length == 3) {
                //CANCEL : return "P1x" etc
                return new IncredistResult(IncredistResult.STATUS_PIN_CANCEL);

            } else if( data.length > 1+ KSN_LENGTH ) {
                int pinLength = data[KSN_LENGTH];
                if (data.length == KSN_LENGTH + 1 + pinLength) {
                    return new PinEntryResult(Arrays.copyOfRange(data, 0, KSN_LENGTH), Arrays.copyOfRange(data, KSN_LENGTH + 1, KSN_LENGTH + 1 + pinLength));
                }
            }
        }

        return new IncredistResult(IncredistResult.STATUS_INVALID_RESPONSE);
    }
}

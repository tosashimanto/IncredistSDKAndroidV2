package jp.co.flight.incredist.android.internal.transport.mfi;

import android.support.annotation.NonNull;

import java.util.Calendar;

import jp.co.flight.incredist.android.internal.controller.result.IncredistResult;

/**
 * MFi 用 時刻設定コマンド (stc)
 */
public class MFiSetRealTimeCommand extends MFiCommand {
    private static final byte[] STC_HEADER = new byte[]{'s', 't', 'c'};

    private static void setAsciiNum(byte[] data, int offset, int length, int value) {
        // CHECKSTYLE:OFF MagicNumber
        if (offset >= 0 && offset < data.length && length >= 0 && offset + length <= data.length) {
            int v = value;
            for (int i = length - 1; i >= 0; i--) {
                int c = (v % 10) + '0';
                data[offset + i] = (byte) c;
                v = (v - v % 10) / 10;
            }
        }
        // CHECKSTYLE:ON MagicNumber
    }

    private static byte[] createPayload(Calendar cal) {
        // CHECKSTYLE:OFF MagicNumber
        byte[] payload = new byte[15];

        System.arraycopy(STC_HEADER, 0, payload, 0, STC_HEADER.length);
        setAsciiNum(payload, 3, 2, cal.get(Calendar.YEAR));
        setAsciiNum(payload, 5, 2, cal.get(Calendar.MONTH) + 1);
        setAsciiNum(payload, 7, 2, cal.get(Calendar.DATE));
        setAsciiNum(payload, 9, 2, cal.get(Calendar.HOUR_OF_DAY));
        setAsciiNum(payload, 11, 2, cal.get(Calendar.MINUTE));
        setAsciiNum(payload, 13, 2, cal.get(Calendar.SECOND));
        // CHECKSTYLE:ON MagicNumber

        return payload;
    }

    public MFiSetRealTimeCommand(Calendar cal) {
        super(createPayload(cal));
    }

    public MFiSetRealTimeCommand() {
        this(Calendar.getInstance());
    }

    @NonNull
    @Override
    protected IncredistResult parseMFiResponse(MFiResponse response) {
        // CHECKSTYLE:OFF MagicNumber
        byte[] bytes = response.getData();
        if (bytes != null && bytes.length == 5 && bytes[0] == 's' && bytes[1] == 't' && bytes[2] == 'c'
                && bytes[3] == 'o' && bytes[4] == 'k') {
            return new IncredistResult(IncredistResult.STATUS_SUCCESS);
        }
        // CHECKSTYLE:ON MagicNumber

        return new IncredistResult(IncredistResult.STATUS_INVALID_RESPONSE);
    }
}

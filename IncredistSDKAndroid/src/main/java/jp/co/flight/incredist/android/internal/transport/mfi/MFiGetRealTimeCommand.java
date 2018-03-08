package jp.co.flight.incredist.android.internal.transport.mfi;

import android.support.annotation.NonNull;

import java.util.Calendar;

import jp.co.flight.incredist.android.internal.controller.result.IncredistResult;
import jp.co.flight.incredist.android.internal.controller.result.RtcResult;

/**
 * MFi 用 時刻取得コマンド (gtc)
 */
public class MFiGetRealTimeCommand extends MFiCommand {
    public MFiGetRealTimeCommand() {
        super(new byte[]{'g', 't', 'c'});
    }

    @Override
    public long getResponseTimeout() {
        return 1000;  // SUPPRESS CHECKSTYLE MagicNumber
    }

    private int asciiToNum(byte[] data, int offset, int length, int min, int max) throws NumberFormatException {
        if (offset >= 0 && offset < data.length && length >= 0 && offset + length <= data.length) {
            int result = 0;
            for (int i = 0; i < length; i++) {
                byte c = data[offset + i];
                if (c < '0' || c > '9') {
                    throw new NumberFormatException();
                }
                result *= 10;  // SUPPRESS CHECKSTYLE MagicNumber
                result += (c - '0');
            }

            if (min <= result && result <= max) {
                return result;
            }
        }

        throw new NumberFormatException();
    }

    @NonNull
    @Override
    protected IncredistResult parseMFiResponse(MFiResponse response) {
        // CHECKSTYLE:OFF MagicNumber
        byte[] bytes = response.getData();
        if (bytes != null && bytes.length == 20 && bytes[0] == 'g' && bytes[1] == 't' && bytes[2] == 'c') {
            try {
                int year = asciiToNum(bytes, 3, 2, 0, 99);
                int month = asciiToNum(bytes, 6, 2, 1, 12);
                int day = asciiToNum(bytes, 9, 2, 1, 31);
                int hour = asciiToNum(bytes, 12, 2, 0, 23);
                int minute = asciiToNum(bytes, 15, 2, 0, 59);
                int second = asciiToNum(bytes, 18, 2, 0, 59);

                Calendar cal = Calendar.getInstance();
                cal.set(2000 + year, month - 1, day, hour, minute, second);

                return new RtcResult(cal);
            } catch (NumberFormatException ex) {
                // do nothing
            }
        }
        // CHECKSTYLE:ON MagicNumber

        return new IncredistResult(IncredistResult.STATUS_INVALID_RESPONSE);
    }
}

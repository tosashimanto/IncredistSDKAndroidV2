package jp.co.flight.incredist.android.internal.controller.command;

import android.support.annotation.NonNull;

import java.util.Arrays;

import jp.co.flight.incredist.android.internal.controller.result.IncredistResult;
import jp.co.flight.incredist.android.internal.controller.result.MagCardResult;
import jp.co.flight.incredist.android.internal.transport.mfi.MFiCommand;
import jp.co.flight.incredist.android.internal.transport.mfi.MFiResponse;
import jp.co.flight.incredist.android.internal.transport.mfi.MFiTransport;
import jp.co.flight.incredist.android.internal.util.BytesUtils;
import jp.co.flight.incredist.android.model.MagCard;

/**
 * 磁気カード読み取りコマンド(mj2)
 */
public class MFiScanMagneticCard2Command extends MFiCommand {
    private static final byte[] MJ2_HEADER = new byte[] {'m', 'j', '2'};
    private static final byte[] MJ2_T1X_ERROR = new byte[] {'T', '1', 'x'};
    private static final byte[] MJ2_T2X_ERROR = new byte[] {'T', '2', 'x'};
    private static final byte[] MJ2_T3X_ERROR = new byte[] {'T', '3', 'x'};

    private static final byte MJ2_CARD_TYPE_JIS1 = 0x63;
    private static final byte MJ2_CARD_TYPE_JIS2 = 0x6a;

    private final long mTimeout;

    public MFiScanMagneticCard2Command(long timeout) {
        super(MJ2_HEADER);
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

    @Override
    public void onCancelled(MFiTransport transport) {
        onCommonCancelled(transport);
    }

    @NonNull
    @Override
    protected IncredistResult parseMFiResponse(MFiResponse response) {
        byte[] data = response.getData();

        // CHECKSTYLE:OFF MagicNumber
        if (data != null && data.length > 15) {
            int length = (data[0] & 0xff) | ((data[1] & 0xff) << 8);
            if (length == data.length) {
                int cardType = data[2];

                byte[] ksn = Arrays.copyOfRange(data, 3, 13);
                int track1Length = data[13] & 0xff;
                int track2Length = data[14] & 0xff;
                int track3Length = data[15] & 0xff;

                int start = 16;
                byte[] track1 = Arrays.copyOfRange(data, start, start + track1Length);
                start += track1Length;

                if (BytesUtils.startsWith(track1, MJ2_T1X_ERROR)) {
                    return new IncredistResult(IncredistResult.STATUS_MAG_TRACK_ERROR);
                }

                byte[] track2 = Arrays.copyOfRange(data, start, start + track2Length);
                start += track2Length;

                if (BytesUtils.startsWith(track2, MJ2_T2X_ERROR)) {
                    return new IncredistResult(IncredistResult.STATUS_MAG_TRACK_ERROR);
                }

                byte[] track3 = Arrays.copyOfRange(data, start, start + track3Length);
                start += track3Length;

                if (BytesUtils.startsWith(track3, MJ2_T3X_ERROR)) {
                    return new IncredistResult(IncredistResult.STATUS_MAG_TRACK_ERROR);
                }

                MagCardResult result = new MagCardResult();
                result.cardType = (cardType == MJ2_CARD_TYPE_JIS1) ? MagCard.Type.JIS1 : ((cardType == MJ2_CARD_TYPE_JIS2) ? MagCard.Type.JIS2 : MagCard.Type.Unknown);
                result.ksn = ksn;
                result.track1 = track1;
                result.track2 = track2;

                //TODO cardholdername など

                return result;
            }
        }
        // CHECKSTYLE:ON MagicNumber

        return new IncredistResult(IncredistResult.STATUS_INVALID_RESPONSE);
    }
}

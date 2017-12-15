package jp.co.flight.incredist.model;

import jp.co.flight.android.dukpt.Dukpt;
import jp.co.flight.android.dukpt.domain.Decrypto;
import jp.co.flight.incredist.android.internal.util.FLog;
import jp.co.flight.incredist.android.model.MagCard;

/**
 * 復号済み磁気情報
 */
public class DecodedMagCard extends MagCard {
    private final static String TAG = "DecodedMagCard";
    private Decrypto mDec1;
    private Decrypto mDec2;

    public DecodedMagCard(MagCard magCard) {
        super(magCard);

        byte[] track1_dec = new byte[81];
        byte[] track2_dec = new byte[73];

        Dukpt dukpt = new Dukpt();
        Decrypto dec1 = new Decrypto(0, magCard.getTrack1().length, magCard.getTrack1(), magCard.getTrack2().length, track1_dec);
        if (magCard.getCardType() == Type.JIS1) {
            if (magCard.getTrack1().length > 0) {
                dec1 = dukpt.decrypt2(magCard.getKsn(), magCard.getTrack1().length, magCard.getTrack1(), track1_dec);
                if (dec1.getStatus() != 1) {
                    FLog.e(TAG, "Track-1:DUKPT Decrypt ERROR");
                    return;
                }
            }
        }
        Decrypto dec2 = new Decrypto(0, magCard.getTrack2().length, magCard.getTrack2(), magCard.getTrack2().length, track2_dec);
        if (magCard.getTrack2().length > 0) {
            dec2 = dukpt.decrypt2(magCard.getKsn(), magCard.getTrack2().length, magCard.getTrack2(), track2_dec);
            if (dec2.getStatus() != 1) {
                FLog.e(TAG, "Track-2:DUKPT Decrypt ERROR");
                return;
            }
        }

        mDec1 = dec1;
        mDec2 = dec2;
    }

    public Decrypto getDec1() {
        return mDec1;
    }

    public Decrypto getDec2() {
        return mDec2;
    }
}

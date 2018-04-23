package jp.co.flight.incredist.android.model;

/**
 * 決済時の EMVKernel からの応答データ
 */
@SuppressWarnings("unused")
public class EmvPacket {
    private final byte[] mData;

    public EmvPacket(byte[] data) {
        mData = data;
    }

    public byte[] getData() {
        return mData;
    }
}

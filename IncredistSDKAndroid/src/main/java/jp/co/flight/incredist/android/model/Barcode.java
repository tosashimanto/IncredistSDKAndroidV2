package jp.co.flight.incredist.android.model;

/**
 * バーコード読み取りデータ
 *
 */
@SuppressWarnings("unused")
public class Barcode {

    private byte[] mContent;

    public Barcode(byte[] content) {
        this.mContent = content;
    }

    protected Barcode(Barcode barcode) {
        byte[] content = barcode.mContent;
    }

    public byte[] getContent() {
        return mContent;
    }
}

package jp.co.flight.incredist.android.model;

/**
 * バーコード読み取りデータ
 *
 */
@SuppressWarnings("unused")
public class Barcode {

    String content;

    public Barcode(String content) {
        this.content = content;
    }

    protected Barcode(Barcode barcode) {
        String content = barcode.content;
    }

}

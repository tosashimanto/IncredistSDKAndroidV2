package jp.co.flight.incredist.android.internal.controller.result;

import jp.co.flight.incredist.android.model.Barcode;

/**
 * バーコード読み取り結果
 */
public class BarcodeResult extends IncredistResult {

    String content;

    public BarcodeResult() {
        super(IncredistResult.STATUS_SUCCESS);
    }

    public Barcode toBarcode() {
        return new Barcode(content);
    }
}

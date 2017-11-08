package jp.co.flight.incredist.android.internal.transport.mfi;

/**
 * 不正な応答パケットを示すクラス.
 */
/* package */
class MFiInvalidResponse extends MFiResponse {
    /**
     * コンストラクタ.
     */
    MFiInvalidResponse(int errorCode) {
        super();

        this.errorCode = errorCode;
    }
}

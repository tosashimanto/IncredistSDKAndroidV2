package jp.co.flight.incredist.android.internal.transport.mfi;

import jp.co.flight.incredist.android.internal.controller.result.IncredistResult;

/**
 * 応答を待たない場合のダミーのレスポンス.
 */
/* package */
class MFiNoResponse extends MFiResponse {
    MFiNoResponse() {
        errorCode = IncredistResult.STATUS_SUCCESS;
    }
}

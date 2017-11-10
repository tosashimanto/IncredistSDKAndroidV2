package jp.co.flight.incredist.android.internal.transport.mfi;

import jp.co.flight.incredist.android.internal.controller.result.IncredistResult;

/**
 * 応答を待たない場合のダミーのレスポンス.
 */
class MFiNoResponse extends MFiResponse {
    MFiNoResponse() {
        mErrorCode = IncredistResult.STATUS_SUCCESS;
    }
}

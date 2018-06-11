package jp.co.flight.incredist.android.model;

import jp.co.flight.android.bluetooth.le.BluetoothLeStatusCode;

/**
 * エラーコード類の定義
 */
public interface StatusCode extends BluetoothLeStatusCode {
    /**
     * 処理成功
     */
    int STATUS_SUCCESS = 1;

    /**
     * 処理失敗
     */
    int STATUS_FAILURE = 2;

    /**
     * Incredist へのコマンド送受信自体が失敗したことを示します
     */
    int STATUS_FAILED_EXECUTION = 901;

    /**
     * Incredist へ他のコマンドを送信中のために処理できないことを示します
     */
    int STATUS_BUSY = 902;

    /**
     * 送信コマンドが不正であることを示します
     * 通常発生しない内部エラーです
     */
    int STATUS_INVALID_COMMAND = 990;

    /**
     * Java スレッドがなんらかの理由で中断されたことを示します
     */
    int STATUS_INTERRUPTED = 801;

    /**
     * 受信処理がタイムアウトしたことを示します
     */
    int STATUS_TIMEOUT = 802;

    /**
     * 予期せぬ形式のデータを受信したことを示します
     */
    int STATUS_INVALID_RESPONSE = 701;

    /**
     * 受信データサイズが大きすぎたことを示します
     */
    int STATUS_TOO_LARGE_RESPONSE = 702;

    /**
     * 受信データのヘッダ部分が異常であることを示します
     */
    int STATUS_INVALID_RESPONSE_HEADER = 703;

    /**
     * 与えられたパラメータが異常であることを示します
     */
    int STATUS_PARAMETER_ERROR = 300;

    /**
     * PIN 入力待ちでタイムアウトしたことを示します
     */
    int STATUS_PIN_TIMEOUT = 600;

    /**
     * PIN 入力で Incredist のキャンセルボタンが押されたことを示します
     */
    int STATUS_PIN_CANCEL = 602;

    /**
     * EMV モードの PIN 入力で ENTER ボタンが押されたことを示します
     */
    int STATUS_PIN_SKIP = 603;

    /**
     * 磁気カード情報が異常だったことを示します
     */
    int STATUS_MAG_TRACK_ERROR = 604;

    /**
     * 別スレッドで cancel メソッドが呼び出され、処理が中断されたことを示します
     */
    int STATUS_CANCELED = 101;

    /**
     * cancel しようとして失敗したことを示します。キャンセル対象の処理は続行されます
     */
    int STATUS_CANCEL_FAILED = 102;

    /**
     * 現在キャンセルできる処理が存在しないことを示します
     */
    int STATUS_NOT_CANCELLABLE = 103;

    /**
     * コマンドの処理中に release() が呼び出されたことを示します
     */
    int STATUS_RELEASED = 104;

    /**
     * 決済処理でカードが拒否されたことを示します
     */
    int STATUS_DECLINE = 201;

    /**
     * 決済処理でキャンセルボタンが押されたことを示します
     */
    int STATUS_INPUT_CANCEL_BUTTON = 202;

    /**
     * 決済処理で EMV FALLBACK 状態であることを示します
     */
    int STATUS_EMV_FALLBACK = 203;

    /**
     * 決済処理でエラーが発生したことを示します
     */
    int STATUS_ERROR = 204;

    /**
     * 決済処理で入力タイムアウトになったことを示します
     */
    int STATUS_INPUT_TIMEOUT = 205;

    /**
     * 決済処理で利用できないカードが読み込まれたことを示します
     */
    int STATUS_CARD_BLOCK = 206;

    /**
     * 接続処理で指定したデバイスが見つからなかったことを示します
     */
    int CONNECT_ERROR_NOT_FOUND = 1798;

    /**
     * 接続処理でタイムアウトしたことを示します
     */
    int CONNECT_ERROR_TIMEOUT = 1799;

    int STATUS_CONTINUE_MULTIPLE_RESPONSE = 9; // SDK 内部でのみ使用
}

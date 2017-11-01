package jp.co.flight.incredist.android.internal.transport.mfi;

/**
 * MFi 版 Incredist との MFi パケット通信を行うユーティリティクラス.
 */

public class MFiTransport {
    /**
     * コンストラクタ.
     */
    public MFiTransport() {
        //TODO
    }

    /**
     * コマンドを送信し、レスポンスを受信して返却します.
     * @param command 送信コマンド
     * @return レスポンスの MFiパケット
     */
    public MFiResponse sendCommand(MFiCommand command) {
        //TODO

        return new MFiResponse(new byte[0]);
    }
}

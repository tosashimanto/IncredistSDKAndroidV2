package jp.co.flight.incredist.android.internal.controller.command;

/**
 * Incredist への送信コマンドの抽象基底クラス.
 */
public abstract class IncredistCommand {
    /**
     * コマンド受信待ち時間を取得します.
     * @return コマンド受信待ち時間(単位 msec)
     */
    abstract long getResponseTimeout();

    /**
     * コマンド送受信後のウェイト時間を取得します.
     * @return ウェイト時間(単位 msec)
     */
    abstract long getGuardWait();
}

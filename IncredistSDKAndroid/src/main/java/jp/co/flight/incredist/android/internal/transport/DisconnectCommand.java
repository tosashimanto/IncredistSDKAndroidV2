package jp.co.flight.incredist.android.internal.transport;

import jp.co.flight.incredist.android.internal.controller.command.IncredistCommand;

/**
 * 切断処理用のダミーのコマンドクラス
 * BLEにコマンドは送信しないので返却値は使用されない
 */
public class DisconnectCommand implements IncredistCommand {
    @Override
    public long getResponseTimeout() {
        return 0;
    }

    @Override
    public long getGuardWait() {
        return 0;
    }
}

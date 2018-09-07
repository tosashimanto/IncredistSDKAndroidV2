package jp.co.flight.incredist.android.model;

public enum EmvMessageType {
    /* 英語 */
    Reset(0),                // メッセージ消去
    ENInputPIN(1),                // AMT, Please enter PIN
    ENInputPINLast(2),                // AMT, Please enter PIN, last
    ENIncorrectPIN(3),                // Incorrect PIN
    ENCorrectPIN(4),                // Correct PIN
    ENIncorrectPINAndCall(5),                // Incorrect PIN, Please call issuer
    ENJDebit(6),                // AMT, Please enter PIN
    ENInputPINWithoutAMT(7),                // Please enter PIN
    ENAccept(91),               // Accept?, AMT, YES->Press Green, NO->Press Red
    ENInputPINDCC(8),                // Amount, Please enter PIN
    ENInputPINLastDCC(9),                // Amount(Last try), Please enter PIN
    ENRemoveCard(0),               // Please remove, the card

    /* 日本語 */
    JPInputPIN(11),               // 金額, 暗証番号をどうぞ
    JPInputPINLast(12),               // 金額, 暗証番号をどうぞ, 残り1回
    JPIncorrectPIN(13),               // 暗証番号が間違っています
    JPCorrectPIN(14),               // 暗証番号を確認しました
    JPIncorrectPINAndCall(15),               // 暗証番号が間違っています, カード会社に連絡してください
    JPJDebit(16),               // 金額, 暗証番号をどうぞ
    JPInputPINWithoutAMT(17),               // 暗証番号をどうぞ
    JPAccept(92),               // これで支払いますか?, 金額, はい->緑を押す, いいえ->赤を押す
    JPInputPINDCC(18),               // 金額, 暗証番号をどうぞ
    JPInputPINLastDCC(19),               // 金額(残り1回), 暗証番号をどうぞ
    JPRemoveCard(20);               // カードを, 抜いて下さい

    /* 韓国語 */
    //    KPInputPIN              = 21,               // 金額, 暗証番号をどうぞ
    //    KPInputPINLast          = 22,               // 金額, 暗証番号をどうぞ, 残り1回
    //    KPIncorrectPIN          = 23,               // 暗証番号が間違っています
    //    KPCorrectPIN            = 24,               // 暗証番号を確認しました
    //    KPIncorrectPINAndCall   = 25,               // 暗証番号が間違っています, カード会社に連絡してください
    //    KPJDebit                = 26,               // 金額, 暗証番号をどうぞ
    //    KPInputPINWithoutAMT    = 27,               // 暗証番号をどうぞ
    //    KPAccept                = 93,               // これで支払いますか?, 金額, はい->緑を押す, いいえ->赤を押す
    //    KPInputPINDCC           = 28,               // 金額, 暗証番号をどうぞ
    //    KPInputPINLastDCC       = 29,               // 金額(残り1回), 暗証番号をどうぞ
    //    KPRemoveCard            = 30,               // カードを, 抜いて下さい

    /* 中国語 */
    //    CNInputPIN              = 31,               // 金額, 暗証番号をどうぞ
    //    CNInputPINLast          = 32,               // 金額, 暗証番号をどうぞ, 残り1回
    //    CNIncorrectPIN          = 33,               // 暗証番号が間違っています
    //    CNCorrectPIN            = 34,               // 暗証番号を確認しました
    //    CNIncorrectPINAndCall   = 35,               // 暗証番号が間違っています, カード会社に連絡してください
    //    CNJDebit                = 36,               // 金額, 暗証番号をどうぞ
    //    CNInputPINWithoutAMT    = 37,               // 暗証番号をどうぞ
    //    CNAccept                = 94,               // これで支払いますか?, 金額, はい->緑を押す, いいえ->赤を押す
    //    CNInputPINDCC           = 38,               // 金額, 暗証番号をどうぞ
    //    CNInputPINLastDCC       = 39,               // 金額(残り1回), 暗証番号をどうぞ
    //    CNRemoveCard            = 40,               // カードを, 抜いて下さい

    private final int value;

    EmvMessageType(int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }
}

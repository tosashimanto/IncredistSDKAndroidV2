package jp.co.flight.incredist.android.model;

public enum TfpMessageType {
    Reset(0),            // メッセージ消去
    PlaceCard(1),            // カードをかざしてください
    NanacoBalance(2),            // nanaco 残高
    NanacoBalancePlace1(3),            // nanaco 残高 カードを1枚にしてかざしてください
    NanacoBalancePlaceAgain(4),            // nanaco 残高 もう一度かざしてください
    NanacoPlaceCard(5),            // nanaco 残高 カードをかざしてください
    NanacoPay(6),            // nanaco 支払い ありがとうございました
    NanacoPayShortBalance(7),            // nanaco 支払い 残高不足です
    NanacoPayPlaceAgain(8),            // nanaco 支払い もう一度かざしてください
    NanacoPayPlaceAgainSameCard(9),            // nanaco 支払い もう一度同じnanacoをかざしてください
    NanacoPayPlace1(10),           // nanaco 支払い カードを1枚にしてもう一度かざしてください
    NanacoPayNotAccepted(11),           // nanaco 支払い このカードはお取り扱いできません
    PayNotAccepted(12),           // お取り扱いできません
    PayNotAcceptedNow(13),           // ただいまお取り扱いできません
    TouchCard(14),           // カードをタッチしてください
    EdyBalance(15),           // Edy 残高
    PayNotAcceptedCard(16),           // このカードはお取り扱いできません
    EdyPayTouchCard(17),           // Edy 支払い カードをタッチしてください
    EdyPayHoldCard(18),           // Edy 支払い 決済処理中。カードを離さないでください
    EdyPayCompleteion(19),           // Edy 支払い 取引が完了しました
    EdyPayShortBalance(20),           // Edy 支払い 残高不足です
    EdyPayPlaceAgain(21),           // Edy 支払い もう一度かざしてください
    EdyPayEnd1(22),           // Edy 支払い 取引を終了します
    EdyPayEnd2(23),           // Edy 支払い 取引を終了します
    EdyPayNotAccepted(24),           // Edy 支払い お取り扱いできません
    SuicaBalanceTouchCard(25),       // 交通系 残高照会 カードタッチ待ち
    SuicaBalance(26),       // 交通系 残高照会 正常カードタッチ
    SuicaBalancePlace1(27),       // 交通系 残高照会 複数枚検知
    SuicaBalanceExpired(28),       // 交通系 残高照会 有効期限切れ
    SuicaBalanceNotAccepted(29),       // 交通系 残高照会 不正カードタッチ
    SuicaPayTouchCard(30),       // 交通系 決済処理 カードタッチ待ち
    SuicaPay(32),       // 交通系 決済処理 正常カードタッチ
    SuicaPayShortBalance(33),       // 交通系 決済処理 残高不足
    SuicaPayNotAccepted(34),       // 交通系支払 このカードは ご利用できません（不正カードタッチ）
    SuicaPayPlace1(35),       // 交通系支払 カードを１枚にして もう一度タッチして ください（複数枚検知）
    SuicaPayExpired(36),       // 交通系支払 有効期限切れ カードです（有効期限切れ）
    SuicaPayDualCard(37),           // 交通系支払 複数枚検知
    SuicaPayPlaceAgain(39),       // 交通系支払 もう一度同じカードを タッチしてください（処理未了）
    SuicaPayDifferentCard(40),       // 交通系 決済処理 処理未了で別カードタッチ
    SuicaPayEnd(41),       // 交通系支払 取引を終了します（処理未了で終了）
    SuicaVoidSalesTouchCard(42),       // 交通系取消 カードを タッチしてください（カードタッチ待ち）
    SuicaVoidSalesDualCard(43),         // 交通系取消 複数枚検知
    SuicaVoidSales(44),       // 交通系 支払取消 正常カードタッチ
    SuicaVoidSalesNotAccepted(45),       // 交通系取消 このカードは お取り扱いできません（不正カードタッチ）
    SuicaVoidSalesPlace1(46),       // 交通系取消 カードを１枚にして もう一度タッチして ください（複数枚検知）
    SuicaVoidSalesExpired(47),       // 交通系取消 有効期限切れ カードです（有効期限切れ）
    SuicaVoidSalesDifferentCard1(48),       // 交通系取消 ご利用時のカードと異なります（別カードタッチ）
    SuicaVoidSalesPlaceAgain(49),       // 交通系取消 もう一度同じカードを タッチしてください（処理未了）
    SuicaVoidSalesDifferentCard2(50),       // 交通系 支払取消 処理未了で別カード
    SuicaVoidSalesEnd(51),       // 交通系取消 （処理未了で終了）
    SuicaTerminalCheckError(52),       // 交通系 お取り扱いできません（端末チェックエラー）
    SuicaConnectError(53),       // 交通系 ただいま お取り扱いできません（通信エラー）
    IDPayTouchCard(54),           // iD支払 カードを かざしてください （カードタッチ待ち）
    IDPay(55),           // iD支払 （カード処理中、正常カードタッチ）
    IDPayPlaceAgain(57),           // iD支払 もう一度同じカードを かざしてください（再かざし待ち）
    IDPayInputPIN(58),           // iD 暗証番号を 入力してください（PIN入力）
    IDVoidSalesTouchCard(59),           // iD支払取消 カードを かざしてください（カードタッチ待ち）
    IDVoidSales(60),           // iD支払取消（正常カードタッチ）
    IDVoidSalesPlaceAgain(62),           // iD支払取消 もう一度同じカードを かざしてください（再かざし待ち）
    IDNotAccepted(63),           // iD このカードは ご利用できません（取引拒否）
    IDTimeout(64),           // iD カードが かざされませんでした（タイムアウト）
    IDDifferentCard(65),           // iD 最初のカードを かざしてください（処理未了で別カードタッチ）
    IDEnd(66),           // iD 取引を終了します（処理未了で終了）
    IDTerminalCheckError(67),           // iD お取り扱いできません（端末チェックエラー）
    IDConnectError(68),           // iD ただいま お取り扱いできません（通信エラー）
    IDPlace1(69),           // iD カードを１枚にして もう一度かざして ください（複数枚検知）
    IDConnecting(70),           // iD オンライン通信中です（通信中）
    IDExpired(71),           // iD 有効期限切れ カードです（有効期限切れ）
    IDAppLock(72),           // iD パスワードを入力して 再度かざしてください （アプリロック）
    IDCardError(73),           // iD このカードは ご利用できません（カード設定エラー）
    IDError(74),           // iD お取り扱いできません（その他エラー）
    IDVoidSalesError(75),           // iD 取消返品処理が 失敗しました（取消エラー）
    EdyWaitMessage(76),// Edy しばらくお待ち下さい

    // 77 〜 96 は WAONの為ポーティングしていません
    QUICPayPayTouchCard(97),                // QUICPay 支払 カードタッチ待ち
    QUICPayPayProcessing(98),               // QUICPay 支払 カード処理中
    QUICPayTransactonRefused(99),           // QUICPay 取引拒否
    QUICPayPlaceAgain(100),                 // QUICPay 再かざし待ち
    QUICPayTimeout(101),                    // QUICPay タイムアウト
    QUICPayDifferentCard(102),              // QUICPay 処理未了で別カードタッチ
    QUICPayEnd(103),                        // QUICPay 処理未了で終了
    QUICPayTerminalCheckError(104),         // QUICPay 端末チェックエラー
    QUICPayConnectError(105),               // QUICPay 通信エラー
    QUICPayDualCard(106),                   // QUICPay 複数枚検知
    QUICPayConnecting(107),                 // QUICPay 通信中
    QUICPayExpired(108),                    // QUICPay 有効期限切れ
    QUICPayCardError(109),                  // QUICPay カード設定エラー
    QUICPayAuthoriOtherCardTouch(110),      // QUICPay オーソリ後別カードタッチ
    QUICPayError(111),                      // QUICPay その他エラー
    QUICPayVoidSalesTouchCard(112),         // QUICPay 支払取消 カードタッチ待ち
    QUICPayVoidSalesProcessing(113),        // QUICPay 支払取消 処理中
    QUICPayVoidSalesError(114),             // QUICPay 支払取消 取消エラー
    QUICPayCardHistoryTouchCard(115);       // QUICPay カード履歴照会 カードタッチ待ち

    private final int value;

    TfpMessageType(int value) {
        this.value = value;
    }

    /**
     * 数値から識別子へ
     *
     * @param numeric 数値
     * @return enum識別子
     */
    public static TfpMessageType getTfpMessageType(final int numeric) {
        TfpMessageType[] tfpMessageTypes = TfpMessageType.values();
        for (TfpMessageType tfpMessageType : tfpMessageTypes) {
            if (tfpMessageType.value == numeric) {
                return tfpMessageType;
            }
        }
        return null;
    }

    public int getValue() {
        return this.value;
    }
}

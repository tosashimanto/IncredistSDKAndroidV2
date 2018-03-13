package jp.co.flight.incredist.android.internal.transport.mfi;

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import jp.co.flight.incredist.android.internal.controller.result.EmvArcResult;
import jp.co.flight.incredist.android.internal.controller.result.IncredistResult;
import jp.co.flight.incredist.android.internal.util.LogUtil;

/**
 * EMV kernel への ARC 送信コマンド
 *
 * ARCデータは複数の MFi パケットで構成される場合があるのでこのクラスは1パケット分を扱う
 */
public final class MFiEmvSendArc extends MFiCommand {
    private static final byte[] ICQ_HEADER = new byte[]{'i', 'c', 'q'};

    /**
     * ARC データの最大長(最大長を超える場合複数の MFiEmvSendArc コマンドに分割する)
     */
    private static final int MAX_SEND_ARC_PACKET_LENGTH = 200;

    /**
     * ARC データを複数の MFiEmvSendArc に分割して生成します
     *
     * @param arcData ARC データ
     * @return MFiEmvSendArc のリスト
     */
    public static List<MFiEmvSendArc> createCommandList(byte[] arcData) {
        List<MFiEmvSendArc> list = new ArrayList<>();

        int totalLength = arcData.length;
        int totalCountPackets = totalLength / MAX_SEND_ARC_PACKET_LENGTH + 1;

        for (int i = 0; i < totalCountPackets; i++) {
            list.add(new MFiEmvSendArc(totalCountPackets, i, totalLength,
                    Arrays.copyOfRange(arcData, i * MAX_SEND_ARC_PACKET_LENGTH,
                            (i < totalCountPackets - 1) ? MAX_SEND_ARC_PACKET_LENGTH : totalLength - i * MAX_SEND_ARC_PACKET_LENGTH)));
        }

        return list;
    }

    private static byte[] createPayload(int totalCountPackets, int indexPacket, int totalLength, byte[] arcData) {
        int arcDataLength = arcData.length;
        if (arcDataLength > MAX_SEND_ARC_PACKET_LENGTH) {
            // この場合はデータ不正
            arcDataLength = MAX_SEND_ARC_PACKET_LENGTH;

        }
        // CHECKSTYLE:OFF MagicNumber
        byte[] payload = new byte[ICQ_HEADER.length + 5 + arcData.length];
        System.arraycopy(ICQ_HEADER, 0, payload, 0, ICQ_HEADER.length);
        payload[3] = (byte) (totalCountPackets & 0xff);
        payload[4] = (byte) (indexPacket & 0xff);
        payload[5] = (byte) (totalLength & 0xff);
        payload[6] = (byte) ((totalLength >> 8) & 0xff);
        payload[7] = (byte) (arcDataLength & 0xff);
        System.arraycopy(arcData, 0, payload, 8, arcDataLength);
        // CHECKSTYLE:ON MagicNumber

        return payload;
    }

    /**
     * コンストラクタ
     *
     * @param totalCountPackets 合計パケット数
     * @param indexPacket       パケット番号
     * @param totalLength       合計データ長
     * @param arcData           パケットデータ
     */
    private MFiEmvSendArc(int totalCountPackets, int indexPacket, int totalLength, byte[] arcData) {
        super(createPayload(totalCountPackets, indexPacket, totalLength, arcData));
    }

    @Override
    public long getResponseTimeout() {
        return 3000; // SUPPRESS CHECKSTYLE MagicNumber
    }

    @NonNull
    @Override
    protected IncredistResult parseMFiResponse(MFiResponse response) {
        // CHECKSTYLE:OFF MagicNumber
        byte[] bytes = response.getData();
        if (bytes != null && bytes.length > 1) {
            if (bytes[0] == 0) {
                return new EmvArcResult(Arrays.copyOfRange(bytes, 1, bytes.length));
            } else {
                return new IncredistResult(IncredistResult.STATUS_FAILURE);
            }
        }
        // CHECKSTYLE:ON MagicNumber

        return new IncredistResult(IncredistResult.STATUS_INVALID_RESPONSE, LogUtil.hexString(bytes));
    }
}

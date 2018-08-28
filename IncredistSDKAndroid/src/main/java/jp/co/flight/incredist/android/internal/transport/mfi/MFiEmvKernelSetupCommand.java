package jp.co.flight.incredist.android.internal.transport.mfi;

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import jp.co.flight.incredist.android.internal.controller.result.IncredistResult;
import jp.co.flight.incredist.android.internal.util.LogUtil;
import jp.co.flight.incredist.android.model.EmvSetupDataType;

/**
 * EMV kernel への setup データ送信コマンド
 *
 * setupデータは複数の MFi パケットで構成される場合があるのでこのクラスは1パケット分を扱う
 */
public final class MFiEmvKernelSetupCommand extends MFiCommand {
    private static final byte[] ICK_HEADER = new byte[]{'i', 'c', 'k'};

    /**
     * setup データのパケットあたりの最大長(最大長を超える場合複数の MfiEmvKernelSetup コマンドに分割する)
     */
    private static final int MAX_SEND_SETUP_PACKET_LENGTH = 200;

    /**
     * setup データを複数の MFiEmvSendArcCommand に分割して生成します
     *
     * @param setupData ARC データ
     * @return MFiEmvSendArcCommand のリスト
     */
    public static List<MFiEmvKernelSetupCommand> createCommandList(EmvSetupDataType type, byte[] setupData) {
        List<MFiEmvKernelSetupCommand> list = new ArrayList<>();

        int totalLength = setupData.length;
        int totalCountPackets = totalLength / MAX_SEND_SETUP_PACKET_LENGTH + 1;

        for (int i = 0; i < totalCountPackets; i++) {
            byte[] dataFrag = Arrays.copyOfRange(setupData, i * MAX_SEND_SETUP_PACKET_LENGTH,
                    (i < totalCountPackets - 1) ? MAX_SEND_SETUP_PACKET_LENGTH : totalLength - i * MAX_SEND_SETUP_PACKET_LENGTH);
            if (i == 0) {
                list.add(new MFiEmvKernelSetupCommand(type, totalCountPackets, totalLength, dataFrag));
            } else {
                list.add(new MFiEmvKernelSetupCommand(i + 1, dataFrag));
            }
        }

        return list;
    }

    private static byte[] create1stPayload(EmvSetupDataType type, int totalCountPackets, int totalLength, byte[] setupData) {
        int setupDataLength = setupData.length;
        if (setupDataLength > MAX_SEND_SETUP_PACKET_LENGTH) {
            // この場合はデータ不正
            setupDataLength = MAX_SEND_SETUP_PACKET_LENGTH;
        }
        // CHECKSTYLE:OFF MagicNumber
        byte[] payload = new byte[ICK_HEADER.length + 6 + setupData.length];
        System.arraycopy(ICK_HEADER, 0, payload, 0, ICK_HEADER.length);
        payload[3] = type.getValue();
        payload[4] = (byte) (totalCountPackets & 0xff);
        payload[5] = (byte) 1;
        payload[6] = (byte) (totalLength & 0xff);
        payload[7] = (byte) ((totalLength >> 8) & 0xff);
        payload[8] = (byte) (setupDataLength & 0xff);
        System.arraycopy(setupData, 0, payload, 9, setupDataLength);
        // CHECKSTYLE:ON MagicNumber

        return payload;
    }

    private static byte[] create2ndPayload(int indexPacket, byte[] setupData) {
        int setupDataLength = setupData.length;
        if (setupDataLength > MAX_SEND_SETUP_PACKET_LENGTH) {
            // この場合はデータ不正
            setupDataLength = MAX_SEND_SETUP_PACKET_LENGTH;
        }
        // CHECKSTYLE:OFF MagicNumber
        byte[] payload = new byte[ICK_HEADER.length + 2 + setupData.length];
        System.arraycopy(ICK_HEADER, 0, payload, 0, ICK_HEADER.length);
        payload[3] = (byte) (indexPacket & 0xff);
        payload[4] = (byte) (setupDataLength & 0xff);
        System.arraycopy(setupData, 0, payload, 5, setupDataLength);
        // CHECKSTYLE:ON MagicNumber

        return payload;
    }

    /**
     * コンストラクタ 先頭パケット
     *
     * @param type              設定種別
     * @param totalCountPackets 合計パケット数
     * @param totalLength       合計データ長
     * @param setupData         パケットデータ
     */
    private MFiEmvKernelSetupCommand(EmvSetupDataType type, int totalCountPackets, int totalLength, byte[] setupData) {
        super(create1stPayload(type, totalCountPackets, totalLength, setupData));
    }

    /**
     * コンストラクタ 2パケット目以降
     *
     * @param indexPacket パケット番号
     * @param setupData   パケットデータ
     */
    private MFiEmvKernelSetupCommand(int indexPacket, byte[] setupData) {
        super(create2ndPayload(indexPacket, setupData));
    }

    @Override
    public long getResponseTimeout() {
        return 3000; // SUPPRESS CHECKSTYLE MagicNumber
    }

    @NonNull
    @Override
    protected IncredistResult parseMFiResponse(MFiResponse response) {
        byte[] bytes = response.getData();
        if (bytes != null && bytes.length == 1) {
            if (bytes[0] == 0) {
                return new IncredistResult(IncredistResult.STATUS_SUCCESS);
            } else {
                return new IncredistResult(IncredistResult.STATUS_FAILURE);
            }
        }

        return new IncredistResult(IncredistResult.STATUS_INVALID_RESPONSE, LogUtil.hexString(bytes));
    }
}

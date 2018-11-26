package jp.co.flight.incredist.android.model;

public enum EmvSetupDataType {
    TerminalConfiguration('T'),
    Aid('A'),
    CapkForContactIc('C'),
    CapkForContactlessIc('L'),
    FlexibleConfiguration('O');

    byte mData;

    EmvSetupDataType(char data) {
        mData = (byte) data;
    }

    public byte getValue() {
        return mData;
    }
}

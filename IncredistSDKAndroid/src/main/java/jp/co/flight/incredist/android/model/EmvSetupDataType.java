package jp.co.flight.incredist.android.model;

public enum EmvSetupDataType {
    TerminalConfiguration('T'),
    Aid('A'),
    ContactCapk('C'),
    ContactlessCapk('L');

    byte mData;

    EmvSetupDataType(char data) {
        mData = (byte) data;
    }

    public byte getValue() {
        return mData;
    }
}

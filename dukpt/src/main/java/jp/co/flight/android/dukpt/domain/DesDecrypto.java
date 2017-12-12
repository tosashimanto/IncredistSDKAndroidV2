package jp.co.flight.android.dukpt.domain;

/**
 * Created by ss990011 on 16/03/30.
 */
public class DesDecrypto {

    int status;
    int track1Size;
    byte[] track1;
    int track2Size;
    byte[] track2;
    byte[] sessionKey;

    public DesDecrypto(int status, int track1Size, byte[] track1, int track2Size, byte[] track2, byte[] sessionKey) {
        this.status = status;
        this.track1Size = track1Size;
        this.track1 = track1;
        this.track2Size = track2Size;
        this.track2 = track2;
        this.sessionKey = sessionKey;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getTrack1Size() {
        return track1Size;
    }

    public void setTrack1Size(int track1Size) {
        this.track1Size = track1Size;
    }

    public byte[] getTrack1() {
        return track1;
    }

    public void setTrack1(byte[] track1) {
        this.track1 = track1;
    }

    public int getTrack2Size() {
        return track2Size;
    }

    public void setTrack2Size(int track2Size) {
        this.track2Size = track2Size;
    }

    public byte[] getTrack2() {
        return track2;
    }

    public void setTrack2(byte[] track2) {
        this.track2 = track2;
    }

    public byte[] getSessionKey() {
        return sessionKey;
    }

    public void setSessionKey(byte[] sessionKey) {
        this.sessionKey = sessionKey;
    }

}

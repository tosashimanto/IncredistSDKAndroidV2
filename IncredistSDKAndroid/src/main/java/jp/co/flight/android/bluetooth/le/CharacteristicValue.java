package jp.co.flight.android.bluetooth.le;

import java.util.UUID;

/**
 * Bluetooth Characteristic の受信値.
 */
@SuppressWarnings("WeakerAccess")
public class CharacteristicValue {
    private final UUID mUuid;
    private final byte[] mValue;

    CharacteristicValue(UUID uuid, byte[] value) {
        this.mUuid = uuid;
        this.mValue = value;
    }

    public UUID getUuid() {
        return this.mUuid;
    }

    public byte[] getValue() {
        return this.mValue;
    }
}

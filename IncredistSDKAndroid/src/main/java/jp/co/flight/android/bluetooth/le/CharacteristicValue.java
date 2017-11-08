package jp.co.flight.android.bluetooth.le;

import java.util.UUID;

/**
 * Bluetooth Characteristic の受信値
 */
@SuppressWarnings("WeakerAccess")
public class CharacteristicValue {
    private UUID uuid;
    private byte[] value;

    CharacteristicValue(UUID uuid, byte[] value) {
        this.uuid = uuid;
        this.value = value;
    }

    public UUID getUuid() {
        return this.uuid;
    }

    public byte[] getValue() {
        return this.value;
    }
}

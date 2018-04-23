package jp.co.flight.incredist.android.model;

import android.support.annotation.NonNull;

/**
 * プロダクト情報取得結果
 */
@SuppressWarnings("unused")
public class ProductInfo {
    public enum ProductType {
        Incredist("B"),
        Premium("P"),
        IncredistDocomo("D"),
        Unknown(null);

        private final String mType;

        ProductType(String type) {
            mType = type;
        }

        @NonNull
        static ProductType findProductType(String type) {
            for (ProductType value : values()) {
                if (value.mType.contains(type)) {
                    return value;
                }
            }

            return Unknown;
        }
    }

    public enum ProductSupportOs {
        SupportIos("1"),
        SupportWindows("2"),
        SupportAndroid("3"),
        SupportUnknown("");

        private final String mType;

        ProductSupportOs(String type) {
            mType = type;
        }

        @NonNull
        static ProductSupportOs findProductSupportOs(String type) {
            for (ProductSupportOs value : values()) {
                if (value.mType.contains(type)) {
                    return value;
                }
            }

            return SupportUnknown;
        }
    }

    public enum ProductColor {
        Black("B"),
        White("W"),
        Silver("S"),
        Unknown("");

        private final String mType;

        ProductColor(String type) {
            mType = type;
        }

        @NonNull
        static ProductColor findProductColor(String type) {
            for (ProductColor value : values()) {
                if (value.mType.contains(type)) {
                    return value;
                }
            }

            return Unknown;
        }
    }


    private final String mDeviceName;
    private final String mFirmwareVersion;
    private final ProductType mProductType;
    private final ProductSupportOs mSupportOs;
    private final ProductColor mColor;
    private final String mCountryCode;
    private final String mSerialNumber;
    private final String mHardwareVersion;

    /**
     * コンストラクタ.
     *
     * @param deviceInfo デバイス情報
     */
    public ProductInfo(DeviceInfo deviceInfo) {
        this.mDeviceName = deviceInfo.getDeviceName();
        this.mFirmwareVersion = deviceInfo.getFirmwareVersion();
        this.mSerialNumber = deviceInfo.getSerialNumber();
        this.mHardwareVersion = deviceInfo.getHardwareVersion();

        // CHECKSTYLE:OFF MagicNumber
        String product = mSerialNumber.substring(2, 3);
        String osVersion = mSerialNumber.substring(3, 4);
        String color = mSerialNumber.substring(4, 5);
        String countryCode = mSerialNumber.substring(5, 7);
        // CHECKSTYLE:ON MagicNumber

        mProductType = ProductType.findProductType(product);
        mSupportOs = ProductSupportOs.findProductSupportOs(osVersion);
        mColor = ProductColor.findProductColor(color);
        mCountryCode = countryCode;
    }

    /**
     * デバイス名を取得します
     *
     * @return デバイス名
     */
    public String getDeviceName() {
        return mDeviceName;
    }

    /**
     * ファームウェアのバージョンを取得します
     *
     * @return バージョン名
     */
    public String getFirmwareVersion() {
        return mFirmwareVersion;
    }

    /**
     * シリアル番号を取得します
     *
     * @return シリアル番号
     */
    public String getSerialNumber() {
        return mSerialNumber;
    }

    /**
     * Incredist の製品種別を取得します
     *
     * @return 製品種別
     */
    public ProductType getProductType() {
        return mProductType;
    }

    /**
     * Incredist の対象 OS を取得します
     *
     * @return 対象OS
     */
    public ProductSupportOs getSupportOs() {
        return mSupportOs;
    }

    /**
     * Incredist の本体色を取得します
     *
     * @return 本体色
     */
    public ProductColor getColor() {
        return mColor;
    }

    /**
     * Incredist の国コードを取得します
     *
     * @return 国コード文字列
     */
    public String getCountryCode() {
        return mCountryCode;
    }

    /**
     * Incredist のハードウェアバージョンを取得します
     *
     * @return ハードウェアバージョン
     */
    public String getHardwareVersion() {
        return mHardwareVersion;
    }
}

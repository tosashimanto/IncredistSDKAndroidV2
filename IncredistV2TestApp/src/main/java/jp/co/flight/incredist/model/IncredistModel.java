package jp.co.flight.incredist.model;

import android.content.Context;

import java.util.List;

import jp.co.flight.incredist.android.Incredist;
import jp.co.flight.incredist.android.IncredistManager;
import jp.co.flight.incredist.android.OnFailureFunction;
import jp.co.flight.incredist.android.OnSuccessFunction;

/**
 * Incredist テストプログラム用モデル.
 */
public interface IncredistModel {

    void newIncredistObject();
    void startScan(OnSuccessFunction<List<String>> success, OnFailureFunction<Void> failure);
    void connect(OnSuccessFunction<Incredist> success, OnFailureFunction<Void> failure);
    void release();
    void clearIncredist();

    class Impl implements IncredistModel {
        private final Context mContext;
        private IncredistManager mIncredistManager;
        private String mSelectedDevice = "SamilF40726DF1105";
        private Incredist mIncredist;

        public Impl(Context context) {
            mContext = context;
        }

        @Override
        public void newIncredistObject() {
            mIncredistManager = new IncredistManager(mContext);
        }

        @Override
        public void startScan(OnSuccessFunction<List<String>> success, OnFailureFunction<Void> failure) {
            mIncredistManager.startScan(null, 5000, success, failure);
        }

        @Override
        public void connect(OnSuccessFunction<Incredist> success, OnFailureFunction<Void> failure) {
            mIncredistManager.connect(mSelectedDevice, 3000, (incredist)->{
                mIncredist = incredist;
                success.onSuccess(incredist);
            }, failure);
        }

        @Override
        public void release() {
            mIncredistManager.release();
        }

        @Override
        public void clearIncredist() {
            mIncredistManager = null;
        }
    }
}

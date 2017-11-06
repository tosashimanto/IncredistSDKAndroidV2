package jp.co.flight.incredist;

import android.os.Handler;
import android.os.Looper;

import java.util.List;
import java.util.Locale;

import jp.co.flight.incredist.databinding.ActivityMainBinding;
import jp.co.flight.incredist.model.IncredistModel;

/**
 * Created by flight on 2017/11/02.
 */

public interface MainPresenter {
    void onStartScan();

    class Impl implements MainPresenter {
        private final ActivityMainBinding mBinding;
        private final IncredistModel mIncredist;
        private final Handler mMainThreadHandler;

        Impl(ActivityMainBinding binding, IncredistModel model) {
            mBinding = binding;
            mIncredist = model;
            mMainThreadHandler = new Handler(Looper.getMainLooper());
        }

        @Override
        public void onStartScan() {
            mIncredist.newIncredistObject();
            mIncredist.startScan((List<String> scanResult) ->{
                addLog(String.format(Locale.JAPANESE, "%d", scanResult.size()));
            }, (errorCode, failure)->{
                addLog("failure");
            });
        }

        private void addLog(String message) {
            mMainThreadHandler.post(new Runnable() {
                @Override
                public void run() {
                    mBinding.textLog.append(message + "\n");
                }
            });
        }
    }
}

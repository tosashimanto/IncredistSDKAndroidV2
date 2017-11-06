package jp.co.flight.incredist;

import android.os.Handler;
import android.os.Looper;

import java.util.List;
import java.util.Locale;

import jp.co.flight.incredist.databinding.ActivityMainBinding;
import jp.co.flight.incredist.model.IncredistModel;

/**
 * MainActivity 用 Presenter インタフェース.
 */
public interface MainPresenter {
    void onStartScan();

    /**
     * MainActiivty 用 Presenter 実体クラス.
     */
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
            addLog("onStattScan");
            mIncredist.newIncredistObject();
            mIncredist.startScan((List<String> scanResult) ->{
                addLog(String.format(Locale.JAPANESE, "onStartScan result %d", scanResult.size()));
            }, (errorCode, failure)->{
                addLog("onStatScan failure");
            });
        }

        private void addLog(String message) {
            mMainThreadHandler.post(()->mBinding.textLog.append(message + "\n"));
        }
    }
}

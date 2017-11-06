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
    void onConnect();
    void onDisconnect();

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
                addLog(String.format(Locale.JAPANESE,"onStatScan failure %d", errorCode));
            });
        }

        @Override
        public void onConnect() {
            addLog("onConnect");
            mIncredist.connect((incredist)->{
                addLog(String.format(Locale.JAPANESE, "connected: %s", incredist.getDeviceName()));
            }, (errorCode, failure)->{
                addLog(String.format(Locale.JAPANESE,"onConnect failure %d", errorCode));
            });
        }

        @Override
        public void onDisconnect() {
            addLog("onDisconnect");
            mIncredist.disconnect(incredist->{
                addLog("disconnected");
            }, (errorCode, incredist) -> {
                addLog(String.format(Locale.JAPANESE,"onDisconnect failure %d", errorCode));
            });
        }

        private void addLog(String message) {
            mMainThreadHandler.post(()->mBinding.textLog.append(message + "\n"));
        }
    }
}

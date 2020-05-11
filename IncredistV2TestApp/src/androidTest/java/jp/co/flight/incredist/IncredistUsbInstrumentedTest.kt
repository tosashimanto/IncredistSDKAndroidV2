package jp.co.flight.incredist

import android.content.Context
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.support.test.InstrumentationRegistry
import android.support.test.runner.AndroidJUnit4
import android.util.Log
import com.google.common.truth.Truth.assertThat
import jp.co.flight.incredist.android.Incredist
import jp.co.flight.incredist.android.IncredistManager
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

/**
 * IncredistSDK usb接続のInstrumented test
 */
@RunWith(AndroidJUnit4::class)
class IncredistUsbInstrumentedTest {
    val appContext = InstrumentationRegistry.getTargetContext()
    lateinit var usbDevice: UsbDevice
    lateinit var usbManager: UsbManager

    companion object {
        private val TAG = "IncredistUsbInstrumentedTest"
    }

    @Before
    fun setUp() {
        // 下記２点を満たしていないと失敗する
        // - 事前にテストアプリを一度起動して、usbパーミッションを許可しておく
        // - Incredistとusb接続しておく
        usbManager = appContext.getSystemService(Context.USB_SERVICE) as UsbManager
        val deviceList = usbManager.deviceList
        usbDevice = deviceList.toList()[0].second
    }

    /**
     * ANDROID_SDK_DEV_54
     * disconnectとreleaseを同時に実行してクラッシュしない事を確認するテスト<br>
     * 交通系でアプリサスペンド時に実行されるReaderIncredist#reset() -> ReaderIncredist#disconnect()をシミュレートするため、<br>
     * 接続 -> リセット後の動作（切断 -> リリース） -> 切断 という流れにしています。
     */
    @Test
    fun noCrashWhenDisconnectAndReleaseAtTheSameTime() {
        val repeat = 100
        for (i in 1..repeat) {
            val counter = "$i/$repeat"
            Log.d(TAG, "noCrashWhenDisconnectAndReleaseAtTheSameTime: **** running $counter ****")
            val incredistManager = IncredistManager(appContext)

            var latch = CountDownLatch(1)
            var reader: Incredist? = null
            var errCode: Int? = null
            var connected = false
            incredistManager.connect(
                usbDevice,
                object : IncredistManager.IncredistConnectionListener {
                    override fun onConnectIncredist(incredist: Incredist?) {
                        reader = incredist
                        connected = true
                        latch.countDown()
                    }

                    override fun onConnectFailure(errorCode: Int) {
                        errCode = errorCode
                        connected = false
                        latch.countDown()
                    }

                    override fun onDisconnectIncredist(incredist: Incredist?) {
                        reader = incredist
                        connected = false

                        latch.countDown()
                    }
                })

            // 接続
            latch.await(2000, TimeUnit.MILLISECONDS)
            assertThat(connected).isTrue()
            assertThat(reader).isNotNull()

            Thread.sleep(10)

            // 切断
            latch = CountDownLatch(1)
            reader?.disconnect()
            latch.await(2000, TimeUnit.MILLISECONDS)
            assertThat(connected).isFalse()
            assertThat(errCode).isNull()

            Thread.sleep(10)

            // release、disconnectをほぼ同時に実行
            // コードの順番はrelease -> disconnectだが、実行順はその限りではない
            latch = CountDownLatch(1)
            Thread {
                Log.d(TAG, "$counter release start.")
                reader?.release()
            }.start()
            Thread {
                Log.d(TAG, "$counter disconnect start.")
                reader?.disconnect()
            }.start()

            latch.await(2000, TimeUnit.MILLISECONDS)
            assertThat(connected).isFalse()
            assertThat(errCode).isNull()

            incredistManager.release()

            Thread.sleep(10)
        }
    }
}
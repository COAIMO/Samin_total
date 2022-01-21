package com.coai.samin_total.Service

import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbDeviceConnection
import android.hardware.usb.UsbManager
import android.os.Binder
import android.os.Handler
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import com.coai.samin_total.BuildConfig
import com.coai.samin_total.MainActivity
import com.coai.samin_total.serviceTAG
import com.hoho.android.usbserial.driver.UsbSerialDriver
import com.hoho.android.usbserial.driver.UsbSerialPort
import com.hoho.android.usbserial.driver.UsbSerialProber
import com.hoho.android.usbserial.util.SerialInputOutputManager
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.IOException
import java.lang.Exception
import kotlin.concurrent.thread
import kotlin.concurrent.timer

class SerialService : Service(), SerialInputOutputManager.Listener {
    companion object {
        const val ACTION_USB_PERMISSION_GRANTED = "USB_PERMISSION_GRANTED"
        const val ACTION_USB_PERMISSION_NOT_GRANTED = "ACTION_USB_PERMISSION_NOT_GRANTED"
        const val ACTION_USB_DEVICE_DETACHED = "ACTION_USB_DEVICE_DETACHED"
        val INTENT_ACTION_GRANT_USB = BuildConfig.APPLICATION_ID + ".GRANT_USB"
        private const val BAUD_RATE = 250000
        private const val WRITE_WAIT_MILLIS = 200
        private const val READ_WAIT_MILLIS = 200
        var SERVICE_CONNECTED = false
        val RECEIVED_SERERIAL_DATA = 1
    }


    val binder = SerialServiceBinder()
    private var usbSerialPort: UsbSerialPort? = null
    var serialPortConnected = false
    lateinit var usbManager: UsbManager
    lateinit var usbDriver: UsbSerialDriver
    var usbDrivers: List<UsbSerialDriver>? = null
    var device: UsbDevice? = null
    var usbConnection: UsbDeviceConnection? = null
    private var usbIoManager: SerialInputOutputManager? = null
    var mHandler = Handler()
    lateinit var checkThread: Thread

    val broadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (INTENT_ACTION_GRANT_USB.equals(intent?.action)) {
                val granted: Boolean =
                    intent?.getExtras()!!.getBoolean(UsbManager.EXTRA_PERMISSION_GRANTED)
                if (granted) {
                    serialPortConnect()
                    val grantedIntent = Intent(ACTION_USB_PERMISSION_GRANTED)
                    context?.sendBroadcast(grantedIntent)
                } else {
                    val grantedIntent = Intent(ACTION_USB_PERMISSION_NOT_GRANTED)
                    context?.sendBroadcast(grantedIntent)
                }
            } else if (intent?.action == UsbManager.ACTION_USB_DEVICE_ATTACHED) {
                if (!serialPortConnected) {
                    findUSBSerialDevice()
                }
            } else if (intent?.action == UsbManager.ACTION_USB_DEVICE_DETACHED) {
                val detachedIntent = Intent(ACTION_USB_DEVICE_DETACHED)
                context?.sendBroadcast(detachedIntent)
                if (serialPortConnected) {
                    usbSerialPort?.close()
                    serialPortConnected = false
                }
            }
        }
    }

    inner class SerialServiceBinder : Binder() {
        fun getService(): SerialService {
            return this@SerialService
        }
    }

    override fun onBind(intent: Intent): IBinder {
        return binder
    }

    //SerialInputOutputManager.Listener
    override fun onNewData(data: ByteArray?) {
        Log.d(serviceTAG, "onNewData : ${HexDump.dumpHexString(data)}")
        mHandler.obtainMessage(RECEIVED_SERERIAL_DATA, data).sendToTarget()

    }

    //SerialInputOutputManager.Listener
    override fun onRunError(e: Exception?) {
        mHandler.post(Runnable {
            disconnect()
        })
    }

    override fun onCreate() {
        Log.d(serviceTAG, "SerialService : onCreate")
        GlobalScope.launch {
            delay(1000L)
            findUSBSerialDevice()
            if (serialPortConnected) {
                cancel()
            }
        }
        setFilter()
        super.onCreate()
    }


    override fun onDestroy() {
        Log.d(serviceTAG, "SerialService : onDestroy")
        unregisterReceiver(broadcastReceiver)
        super.onDestroy()
    }

    //activity랑 연결해줄 핸들러 셋 fun
    fun setHandler(mHandler: Handler) {
        this.mHandler = mHandler
    }

    private fun setFilter() {
        val filter = IntentFilter()
        filter.addAction(INTENT_ACTION_GRANT_USB)
        filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED)
        filter.addAction(ACTION_USB_DEVICE_DETACHED)
        filter.addAction(ACTION_USB_PERMISSION_GRANTED)
        filter.addAction(ACTION_USB_PERMISSION_NOT_GRANTED)
        registerReceiver(broadcastReceiver, filter)
    }

    private fun findUSBSerialDevice() {
        usbManager = getSystemService(Context.USB_SERVICE) as UsbManager
        if (usbManager.deviceList.isEmpty()) {
            Log.d(serviceTAG, "connection failed: device not found")
            mHandler.postDelayed({
                Toast.makeText(this, "connection failed: device not found", Toast.LENGTH_SHORT)
                    .show()
            }, 0)
            return
        }
        usbDrivers = UsbSerialProber.getDefaultProber().findAllDrivers(usbManager)
        if (usbDrivers == null) {
            Log.d(serviceTAG, "connection failed: no driver for device")
            mHandler.postDelayed({
                Toast.makeText(this, "connection failed: no driver for device", Toast.LENGTH_SHORT)
                    .show()
            }, 0)
            return
        }
        for (i in usbDrivers!!) {
            usbDriver = i
            device = i.device
            val intent: PendingIntent =
                PendingIntent.getBroadcast(this, 0, Intent(INTENT_ACTION_GRANT_USB), 0)
            usbManager.requestPermission(device, intent)
            Log.d(serviceTAG, "${i.device}")
        }
    }

    private fun serialPortConnect() {
        if (usbManager.hasPermission(device) && usbSerialPort == null) {
            usbConnection = usbManager.openDevice(device)
            usbSerialPort = usbDriver.ports[0]
            usbSerialPort!!.open(usbConnection)
            usbSerialPort!!.setParameters(
                BAUD_RATE,
                UsbSerialPort.DATABITS_8,
                UsbSerialPort.STOPBITS_1,
                UsbSerialPort.PARITY_NONE
            )
            usbIoManager = SerialInputOutputManager(usbSerialPort, this)
            usbIoManager!!.start()
            serialPortConnected = true

        }
    }

    private fun disconnect() {
        serialPortConnected = false
        if (usbIoManager != null) {
            usbIoManager!!.listener = null
            usbIoManager!!.stop()
        }
        usbIoManager = null
        try {
            usbSerialPort!!.close()
        } catch (ignored: IOException) {
        }
        usbSerialPort = null
    }

}
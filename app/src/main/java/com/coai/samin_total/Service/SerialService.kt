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
import android.os.Process
import android.util.Log
import android.widget.Toast
import com.coai.samin_total.BuildConfig
import com.coai.samin_total.Logic.SaminProtocol
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
//    SerialInputOutputManager.Listener
    companion object {
        const val ACTION_USB_PERMISSION_GRANTED = "USB_PERMISSION_GRANTED"
        const val ACTION_USB_PERMISSION_NOT_GRANTED = "ACTION_USB_PERMISSION_NOT_GRANTED"
        const val ACTION_USB_DEVICE_DETACHED = "ACTION_USB_DEVICE_DETACHED"
        val INTENT_ACTION_GRANT_USB = BuildConfig.APPLICATION_ID + ".GRANT_USB"
        private const val BAUD_RATE = 1000000
        private const val WRITE_WAIT_MILLIS = 2000
        private const val READ_WAIT_MILLIS = 2000
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
    private var usbIoManager: CoAISerialInputOutputManager? = null
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
                mHandler.obtainMessage(
                    2,
                )
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
        Log.d("로그", "onNewData : ${HexDump.dumpHexString(data)}")
        if (data != null) {
            parseReceiveData(data)
        }
    }

//    SerialInputOutputManager.Listener
    override fun onRunError(e: Exception?) {
        mHandler.post(Runnable {
            disconnect()
        })
    }

    override fun onCreate() {
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
//        Log.d(serviceTAG, "SerialService : onDestroy")
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
            mHandler.postDelayed({
                Toast.makeText(this, "connection failed: device not found", Toast.LENGTH_SHORT)
                    .show()
            }, 0)
            return
        }
        usbDrivers = UsbSerialProber.getDefaultProber().findAllDrivers(usbManager)
        if (usbDrivers == null) {
//            Log.d(serviceTAG, "connection failed: no driver for device")
            mHandler.postDelayed({
                Toast.makeText(this, "connection failed: no driver for device", Toast.LENGTH_SHORT)
                    .show()
            }, 0)
            return
        }
        if(usbDrivers!!.count() > 0){
            usbDriver = getFirstDevice(usbDrivers!!)
            device = usbDriver.device
            val intent: PendingIntent =
                PendingIntent.getBroadcast(this, 0, Intent(INTENT_ACTION_GRANT_USB), 0)
            usbManager.requestPermission(device, intent)
        }
//        for (i in usbDrivers!!) {
//            usbDriver = i
//            device = i.device
//            val intent: PendingIntent =
//                PendingIntent.getBroadcast(this, 0, Intent(INTENT_ACTION_GRANT_USB), 0)
//            usbManager.requestPermission(device, intent)
//            Log.d(serviceTAG, "${i.device}")
//        }
    }
    private fun getFirstDevice(lst : List<UsbSerialDriver>): UsbSerialDriver {
        val sortList = lst.sortedBy { it.device.deviceName}
        return sortList.get(0)
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
            usbSerialPort!!.dtr = true
            usbSerialPort!!.rts = true
//            Thread {
//
//                val buff = ByteArray(30)
//                while (true){
//                    val size = usbSerialPort!!.read(buff, READ_WAIT_MILLIS)
//                    if (size > 0) {
//                        Log.d("태그", "recieved data: ${HexDump.dumpHexString(buff)}")
//                    }
//                }
//
//            }.start()
//
            usbIoManager = CoAISerialInputOutputManager(usbSerialPort, this)
            usbIoManager!!.readTimeout = 35
//            usbIoManager!!.writeTimeout = 200
//            usbIoManager!!.readBufferSize = 1000
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

    fun sendData(data: ByteArray) {
//        usbSerialPort?.write(data, WRITE_WAIT_MILLIS)
        usbIoManager?.writeAsync(data)

//        Log.d("태그", "usbIoManager is Null : ${usbIoManager == null}, send data :${HexDump.dumpHexString(data)}")
    }

    fun checkModelandID() {
        val protocol = SaminProtocol()
        protocol.feedBack(3, 0)
//        usbSerialPort?.write()
    }


    private val HEADER: ByteArray = byteArrayOf(0xff.toByte(), 0xFE.toByte())
    private var lastRecvTime: Long = System.currentTimeMillis()
    private var bufferIndex: Int = 0
    private var recvBuffer: ByteArray = ByteArray(1024)

    fun parseReceiveData(data: ByteArray) {
        lastRecvTime = System.currentTimeMillis()
        try {
            //1. 버퍼인덱스(이전 부족한 데이터크기) 및 받은 데이터 크기만큼 배열생성
            val tmpdata = ByteArray(bufferIndex + data.size)
            //2.이전 recvBurffer에 남아있는 데이터를 tmpdata로 이동함
            System.arraycopy(recvBuffer, 0, tmpdata, 0, bufferIndex)
            //3. 데이터를 tmpdata의 잔여 데이터 뒤에 데이터 사이즈만큼 넣음
            System.arraycopy(data, 0, tmpdata, bufferIndex, data.size)
            var idx: Int = 0
//            Log.d("태그", "received = ${HexDump.dumpHexString(data)}")

            if (tmpdata.size < 7) {
                //3. 수신받은 데이터 부족 시 리시브버퍼로 데이터 이동
                System.arraycopy(tmpdata, idx, recvBuffer, 0, tmpdata.size)
                //4. 이전 받은 데이터 확인을 위해 버퍼 인덱스 수정
                bufferIndex = tmpdata.size
                return
            }

            while (true) {
                val chkPos = indexOfBytes(tmpdata, idx, tmpdata.size)
                val num = tmpdata[chkPos + 4] + 4 + 1
                num
                if (chkPos != -1) {
                    //해더 유무 체크 및 헤더 몇 번째 있는지 반환
                    val scndpos = indexOfBytes(tmpdata, chkPos + 1, tmpdata.size)
                    //다음 헤더가 없는 경우 -1 변환(헤더 중복 체크)
                    if (scndpos == -1) {
                        // 다음 데이터 없음
                        if (tmpdata[chkPos + 4] + 4 + 1 <= tmpdata.size - chkPos) {
                            // 해당 전문을 다 받았을 경우 ,또는 크거나
                            val focusdata: ByteArray =
                                tmpdata.drop(chkPos).toByteArray()
                            //todo
                            mHandler.obtainMessage(RECEIVED_SERERIAL_DATA, focusdata)
                                .sendToTarget()

                            bufferIndex = 0;

                        } else {
                            //해당 전문보다 데이터가 작을경우
                            System.arraycopy(
                                tmpdata,
                                chkPos,
                                recvBuffer,
                                0,
                                tmpdata.size - chkPos
                            )
                            bufferIndex = tmpdata.size - chkPos
                        }
                        break

                    } else {

                        //첫번째 헤더 앞부분 짤라냄.(drop) //첫번째 헤더부터 두번째 헤더 앞까지 짤라냄.(take)
                        val focusdata: ByteArray =
                            tmpdata.drop(chkPos).take(scndpos - chkPos).toByteArray()

                        mHandler.obtainMessage(RECEIVED_SERERIAL_DATA, focusdata).sendToTarget()
                        // 두번째 헤더 부분을 idx
                        idx = scndpos
                    }
                } else {
                    System.arraycopy(tmpdata, idx, recvBuffer, 0, tmpdata.size)
                    bufferIndex = tmpdata.size
                    break
                }
            }
        } catch (ex: Exception) {
//            ex.message?.let { Log.e(UsbService.TAG, it) }
        }
    }

    private fun indexOfBytes(data: ByteArray, startIdx: Int, count: Int): Int {
        if (data.size == 0 || count == 0 || startIdx >= count)
            return -1
        var i = startIdx
        val endIndex = Math.min(startIdx + count, data.size)
        var fidx: Int = 0
        var lastFidx = 0
        while (i < endIndex) {
            lastFidx = fidx
            fidx = if (data[i] == HEADER[fidx]) fidx + 1 else 0
            if (fidx == 2) {
                return i - fidx + 1
            }
            if (lastFidx > 0 && fidx == 0) {
                i = i - lastFidx
                lastFidx = 0
            }
            i++
        }
        return -1
    }

}
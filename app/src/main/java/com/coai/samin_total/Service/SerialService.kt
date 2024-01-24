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
import android.os.*
import android.util.Log
import com.coai.samin_total.BuildConfig
import com.coai.samin_total.Logic.*
import com.hoho.android.usbserial.driver.UsbSerialDriver
import com.hoho.android.usbserial.driver.UsbSerialPort
import com.hoho.android.usbserial.driver.UsbSerialProber
import com.hoho.android.usbserial.util.SerialInputOutputManager
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.IOException
import java.lang.RuntimeException
import java.lang.ref.WeakReference
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicLong

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
        const val MSG_BIND_CLIENT = 2
        const val MSG_UNBIND_CLIENT = 3
        const val MSG_SERIAL_CONNECT = 4
        const val MSG_SERIAL_SEND = 5
        const val MSG_SERIAL_RECV = 6
        const val MSG_SERIAL_DISCONNECT = 7
        const val MSG_NO_SERIAL = 8
        const val MSG_CHECK_PING = 9
        const val MSG_CHECK_VERSION = 10
        const val MSG_SHARE_SETTING = 11
        const val MSG_GASDOCK = 12
        const val MSG_GASROOM = 13
        const val MSG_WASTE = 14
        const val MSG_OXYGEN = 15
        const val MSG_STEMER = 16
        const val MSG_TEMPHUM = 17
        const val MSG_BAUDRATE_CHANGE = 18
        const val MSG_ERROR = 19
        const val MSG_SERIAL_FEEDBACK_SEND = 20
    }

    private lateinit var messenger: Messenger
    var currentBaudrate: Int = 1000000

    inner class IncomingHandler(
        service: Service,
        private val context: Context = service.applicationContext
    ) :
        Handler(Looper.getMainLooper()) {

        private val clients = mutableListOf<Messenger>()
        private val weakClients: WeakReference<MutableList<Messenger>> = WeakReference(clients)

        override fun handleMessage(msg: Message) {
            when (msg.what) {
                MSG_BIND_CLIENT -> clients.add(msg.replyTo)
                MSG_UNBIND_CLIENT -> {
                    clients.remove(msg.replyTo)
                    disconnect()
                }
                MSG_SERIAL_SEND -> {
                    msg.data.getByteArray("")?.let { sendData(it) }
                }
                MSG_SERIAL_FEEDBACK_SEND-> {
                    msg.data.getByteArray("")?.let { sendFeedbackData(it) }
                }
                MSG_BAUDRATE_CHANGE-> {
                    // 통신속도 변경
                    msg.data.getInt("")?.let {
                        val shared: SaminSVCSharedPreference  = SaminSVCSharedPreference(context)
                        shared.saveBoardSetData(SaminSVCSharedPreference.BAUDRATE, it)
                    }
                }
                else -> super.handleMessage(msg)
            }
        }

        fun sendConnected() {
            val message = Message.obtain(null, MSG_SERIAL_CONNECT, null)
//            clients.forEach {
//                it.send(message)
//            }
            weakClients.get()?.forEach {
                it.send(message)
            }
        }

        fun sendUIDATA(data: ByteArray) {
            val message = Message.obtain(null, MSG_SERIAL_RECV)
            val bundle = Bundle()
            bundle.putByteArray("", data)
            message.data = bundle
//            clients.forEach {
//                it.send(message)
//            }
            weakClients.get()?.forEach {
                it.send(message)
            }
        }

        fun sendSettingDATA(data: ByteArray) {
            val message = Message.obtain(null, MSG_SHARE_SETTING)
            val bundle = Bundle()
            bundle.putByteArray("", data)
            message.data = bundle
//            clients.forEach {
//                it.send(message)
//            }
            weakClients.get()?.forEach {
                it.send(message)
            }
        }

        fun sendMSG_SERIAL_DISCONNECT() {
            val message = Message.obtain(null, MSG_SERIAL_DISCONNECT)
            weakClients.get()?.forEach {
                it.send(message)
            }
        }

        fun sendMSG_SERIAL_CONNECT() {
            val message = Message.obtain(null, MSG_SERIAL_CONNECT)
            weakClients.get()?.forEach {
                it.send(message)
            }
        }

        fun sendMSG_NO_SERIAL() {
            val message = Message.obtain(null, MSG_NO_SERIAL)
            weakClients.get()?.forEach {
                it.send(message)
            }
        }

        fun sendMSG_ERROR() {
            val message = Message.obtain(null, MSG_ERROR)
            weakClients.get()?.forEach {
                it.send(message)
            }
        }
        fun sendMSG(msg: Message) {
//            clients.forEach {
//                it.send(msg)
//            }
            weakClients.get()?.forEach {
                it.send(msg)
            }
        }
    }

    var incomingHandler: IncomingHandler? = null

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
    private var isruncheckThread = AtomicBoolean(true)

    val broadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (INTENT_ACTION_GRANT_USB.equals(intent?.action)) {
                val granted: Boolean =
                    intent?.getExtras()!!.getBoolean(UsbManager.EXTRA_PERMISSION_GRANTED)
                if (granted) {
                    findUSBSerialDevice(true)
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
                incomingHandler?.sendMSG_SERIAL_CONNECT();
                Log.d("로그", "ACTION_USB_DEVICE_ATTACHED ======")
            } else if (intent?.action == UsbManager.ACTION_USB_DEVICE_DETACHED) {
                Log.d("로그", "ACTION_USB_DEVICE_DETACHED ======")
                val detachedIntent = Intent(ACTION_USB_DEVICE_DETACHED)
                context?.sendBroadcast(detachedIntent)
                try {
                    if (serialPortConnected) {
                        usbSerialPort?.close()
                        serialPortConnected = false
                    }
                } catch ( ex: Exception ){

                }
                incomingHandler?.sendMSG_SERIAL_DISCONNECT()
            }
        }
    }

    inner class SerialServiceBinder : Binder() {
        fun getService(): SerialService {
            return this@SerialService
        }
    }

    //    override fun onBind(intent: Intent): IBinder {
//        return binder
//    }
    override fun onBind(intent: Intent): IBinder {
        if (incomingHandler == null)
            incomingHandler = IncomingHandler(this)
        messenger = Messenger(incomingHandler)
        return messenger.binder
    }

    //SerialInputOutputManager.Listener
    override fun onNewData(data: ByteArray?) {
//        Log.d("로그", "onNewData : ${HexDump.dumpHexString(data)}")
//        Log.d("로그", "onNewData recived ======")
        if (data != null) {
            parseReceiveData(data)
        }
    }

    //    SerialInputOutputManager.Listener
    override fun onRunError(e: Exception?) {
        Log.d(
            "svc OnRunError",
            "================= current : ${System.currentTimeMillis()} ${e?.message}"
        )
        mHandler.post(Runnable {
            disconnect()
        })
    }

    override fun onCreate() {
//        Log.d(serviceTAG, "SerialService : onCreate")
        val shared: SaminSVCSharedPreference  = SaminSVCSharedPreference(this)
        currentBaudrate = shared.loadBoardSetData(SaminSVCSharedPreference.BAUDRATE) as Int
        Log.d("Service", "baudrate : ${currentBaudrate}")

        Thread.setDefaultUncaughtExceptionHandler { _, ex ->
            incomingHandler?.sendMSG_ERROR()
            android.os.Process.killProcess(android.os.Process.myPid())
            System.exit(10)
        }

        GlobalScope.launch {
            delay(1000L)
            findUSBSerialDevice()
            if (serialPortConnected) {
                cancel()
            }
        }
        setFilter()

//        val handler = Handler(Looper.getMainLooper())
//        handler.postDelayed({
//            throw RuntimeException("runtimeException입니다.")
//        }, 1000 * 30)

        lastRecvProtocol.set(System.currentTimeMillis())
        checkThread = Thread {
            while (isruncheckThread.get()) {
                try {
                    val lastrecv = lastRecvProtocol.get()
                    if ((lastrecv + 1000L * 30) < System.currentTimeMillis()) {
                        Log.d(
                            "Serial Fail",
                            "================= Time : $lastrecv current : ${System.currentTimeMillis()} ===================="
                        )
                        disconnect()
                        findUSBSerialDevice()

                        lastRecvProtocol.set(System.currentTimeMillis())

                    }
                    Thread.sleep(10)
                } catch (ex: Exception) {

                }
            }
        }
        checkThread.start()

//        val memoutThread = Thread {
//            val largeList = mutableListOf<String>()
//            while (true) {
//                largeList.add("메모리 부족 테스트".repeat(1000))
//                Thread.sleep(10)
//            }
//        }
//        memoutThread.start()
        super.onCreate()
    }


    override fun onDestroy() {
        unregisterReceiver(broadcastReceiver)
        isruncheckThread.set(false)
        checkThread.interrupt()
        checkThread.join()
        super.onDestroy()
        Log.d("Service", "SerialService : onDestroy")
    }

//    //activity랑 연결해줄 핸들러 셋 fun
//    fun setHandler(mHandler: Handler) {
//        this.mHandler = mHandler
//    }


    private fun setFilter() {
        val filter = IntentFilter()
        filter.addAction(INTENT_ACTION_GRANT_USB)
        filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED)
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED)
//        filter.addAction(ACTION_USB_DEVICE_DETACHED)
        filter.addAction(ACTION_USB_PERMISSION_GRANTED)
        filter.addAction(ACTION_USB_PERMISSION_NOT_GRANTED)
        registerReceiver(broadcastReceiver, filter)
    }

    private fun findUSBSerialDevice(hasPermission: Boolean = false) {
        usbManager = getSystemService(Context.USB_SERVICE) as UsbManager
        if (usbManager.deviceList.isEmpty()) {
            incomingHandler?.sendMSG_NO_SERIAL()
            return
        }
        usbDrivers = UsbSerialProber.getDefaultProber().findAllDrivers(usbManager)
        if (usbDrivers == null) {
            incomingHandler?.sendMSG_NO_SERIAL()
            return
        }
        if (usbDrivers!!.count() > 0) {
            usbDriver = getFirstDevice(usbDrivers!!)
            device = usbDriver.device

            if (!hasPermission) {
                val intent: PendingIntent =
                    PendingIntent.getBroadcast(this, 0, Intent(INTENT_ACTION_GRANT_USB), 0)
                usbManager.requestPermission(device, intent)
            } else {
                serialPortConnect()
                incomingHandler?.sendConnected()
            }
        } else {
            incomingHandler?.sendMSG_NO_SERIAL()
            return
        }
    }

    private fun getFirstDevice(lst: List<UsbSerialDriver>): UsbSerialDriver {
        val sortList = lst.sortedBy { it.device.deviceName }
        return sortList.get(0)
    }

    private fun serialPortConnect() {
        if (usbManager.hasPermission(device) && usbSerialPort == null) {
            usbConnection = usbManager.openDevice(device)
            usbSerialPort = usbDriver.ports[0]

            usbSerialPort!!.open(usbConnection)
            usbSerialPort!!.setParameters(
                currentBaudrate,
                UsbSerialPort.DATABITS_8,
                UsbSerialPort.STOPBITS_1,
                UsbSerialPort.PARITY_NONE
            )
            usbSerialPort!!.dtr = true
            usbSerialPort!!.rts = true
//
            usbIoManager = CoAISerialInputOutputManager(usbSerialPort, this)
            usbIoManager!!.readTimeout = 100
//            usbIoManager!!.writeTimeout = 200
//            usbIoManager!!.readBufferSize = 1000
            usbIoManager!!.setBaudrate(currentBaudrate)
            usbIoManager!!.start()
            serialPortConnected = true

        }
    }

    private fun disconnect() {
        serialPortConnected = false
        if (usbIoManager != null) {
            usbIoManager?.clearWriteBuff()
            usbIoManager?.listener = null
            usbIoManager?.stop()
        }

        usbIoManager = null
        try {
            usbSerialPort?.close()
//            usbSerialPort!!.close()
        } catch (ignored: IOException) {
        }
        usbSerialPort = null
    }

    private fun sendData(data: ByteArray) {
        if (usbSerialPort?.isOpen == true)
            usbIoManager?.writeAsync(data)
    }

    private fun sendFeedbackData(data: ByteArray) {
        if (usbSerialPort?.isOpen == true)
            usbIoManager?.writeFeedbackAsync(data)
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

    private val lastRecvProtocol: AtomicLong = AtomicLong()

    private fun littleEndianConversion(bytes: ByteArray): Int {
        var result = 0
        for (i in bytes.indices) {
            result = result or (bytes[i].toUByte().toInt() shl 8 * i)
        }
        return result
    }

    private fun littleEndianConversion(bytes: ByteArray, start: Int, end: Int): Int {
        var result = 0
        for (i in start..end) {
            result = result or (bytes[i].toUByte().toInt() shl 8 * (i - start))
        }
        return result
    }

    val exSensorData = HashMap<Int, Int>()
    val alphavalue = 0.25

    private fun getLPF(x: Int, key: Int): Int {
        var tmpx = x
        if (x > 1023) {
            Log.d("ERROR", "이상한 데이터 : $x")
            tmpx = exSensorData[key] ?: 0
        }
        if (!exSensorData.containsKey(key)) {
            exSensorData.put(key, tmpx)
            return tmpx
        }

        val prev = exSensorData[key]
        val ret = alphavalue * prev!! + (1 - alphavalue) * tmpx
        exSensorData[key] = ret.toInt()
        return ret.toInt()
    }

    private var datas: ArrayList<Int> = ArrayList()
    private fun preprocessParser(arg: ByteArray) {
        try {
            val id = arg[3]
            val model = arg[2]
            val time = System.currentTimeMillis()
//            val datas = ArrayList<Int>()
            datas.clear()

            if (model == 6.toByte()){
                datas.add(littleEndianConversion(arg,7, 10))
                datas.add(littleEndianConversion(arg,11, 14))
            }else{
                datas.add(littleEndianConversion(arg,7, 8))
                datas.add(littleEndianConversion(arg,9, 10))
                datas.add(littleEndianConversion(arg,11, 12))
                datas.add(littleEndianConversion(arg,13, 14))
            }

            if (model == 1.toByte() || model == 2.toByte() || model == 5.toByte()) {
                for (t in 0..3) {
                    datas[t] = getLPF(
                        datas[t], littleEndianConversion(
                            byteArrayOf(
                                model,
                                id.toByte(),
                                (t + 1).toByte()
                            )
                        )
                    )
                }
            } else if (model == 3.toByte()) {
                for (t in 0..3) {
                    1
                    if (datas[t] != 0 && datas[t] != 1) {
                        return
                    }
                }
            }

            val tmp: ParsingData = ParsingData(
                id,
                model,
                time,
                datas
            )
            val bundle = Bundle()
            bundle.putSerializable("", tmp)

            incomingHandler?.let {
                val msgType = when(model) {
                    0x01.toByte() -> MSG_GASDOCK
                    0x02.toByte() -> MSG_GASROOM
                    0x03.toByte() -> MSG_WASTE
                    0x04.toByte() -> MSG_OXYGEN
                    0x05.toByte() -> MSG_STEMER
                    0x06.toByte() -> MSG_TEMPHUM
                    // 다른 모델에 대한 메시지 타입 매핑
                    else -> MSG_GASDOCK
                }
                val message = Message.obtain(null, msgType)
                message.data = bundle
                it.sendMSG(message)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun recvData(data: ByteArray) {
        // 정상 프로토콜 수신 시간
        lastRecvProtocol.set(System.currentTimeMillis())

        val receiveParser = SaminProtocol()
//        Log.d("SerialService", "data : \n${HexDump.dumpHexString(data)}")
        if (!receiveParser.parse(data))
            return

        when (receiveParser.packet) {
            SaminProtocolMode.CheckProductPing.byte -> {

                val message = Message.obtain(
                    null,
                    MSG_CHECK_PING,
                    receiveParser.mProtocol.get(2).toInt(),
                    receiveParser.mProtocol.get(3).toInt()
                )
                incomingHandler?.sendMSG(message)
            }
            SaminProtocolMode.RequestFeedBackPing.byte -> {
                preprocessParser(data)
            }
            SaminProtocolMode.SettingShare.byte -> {
                incomingHandler?.sendSettingDATA(data)
            }
            SaminProtocolMode.CheckVersion.byte -> {
                val message = Message.obtain(
                    null,
                    MSG_CHECK_VERSION,
                    receiveParser.mProtocol.get(7).toInt(),
                    0
                )
                incomingHandler?.sendMSG(message)
            }
        }
    }

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
//                val num = tmpdata[chkPos + 4] + 4 + 1
//                num
                if (chkPos != -1) {
                    //해더 유무 체크 및 헤더 몇 번째 있는지 반환
                    val scndpos = indexOfBytes(tmpdata, chkPos + 1, tmpdata.size)
                    //다음 헤더가 없는 경우 -1 변환(헤더 중복 체크)
                    if (scndpos == -1) {
                        // 다음 데이터 없음
                        if (chkPos + 4 < tmpdata.size && tmpdata[chkPos + 4] + 4 + 1 <= tmpdata.size - chkPos) {
//                        if (tmpdata[chkPos + 4] + 4 + 1 <= tmpdata.size - chkPos) {
                            // 해당 전문을 다 받았을 경우 ,또는 크거나
                            val focusdata: ByteArray =
                                tmpdata.drop(chkPos).toByteArray()
                            //todo
//                            mHandler.obtainMessage(RECEIVED_SERERIAL_DATA, focusdata)
//                                .sendToTarget()
                            recvData(focusdata)
//                            Log.d("태그", "chkPos=${chkPos}, chkPos + 4 = ${chkPos + 4}, tmpdata.size= ${tmpdata.size}, tmpdata[chkPos + 4] + 4 + 1 = ${tmpdata[chkPos + 4] + 4 + 1}, tmpdata.size - chkPos= ${tmpdata.size - chkPos} , focusdata = ${HexDump.dumpHexString(focusdata)}")

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

//                        mHandler.obtainMessage(RECEIVED_SERERIAL_DATA, focusdata).sendToTarget()
                        // 두번째 헤더 부분을 idx
                        recvData(focusdata)
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
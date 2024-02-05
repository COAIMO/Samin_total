package com.coai.samin_total

//import android.app.Activity
import android.app.PendingIntent
import android.app.ProgressDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
//import android.content.Intent
import android.os.Bundle
import android.util.Base64
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
//import android.view.WindowManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.app.ActivityCompat.finishAffinity
import androidx.fragment.app.activityViewModels
import com.coai.samin_total.Logic.*
import com.coai.samin_total.databinding.FragmentControlBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToByteArray
import kotlinx.serialization.protobuf.ProtoBuf
//import java.util.Base64

//import kotlin.system.exitProcess

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [ControlFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ControlFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    var activity: MainActivity? = null
    private lateinit var mBinding: FragmentControlBinding
    private lateinit var onBackPressed: OnBackPressedCallback
    lateinit var shared: SaminSharedPreference
    private val viewmodel by activityViewModels<MainViewModel>()
    var sendThread: Thread? = null
    lateinit var progress_Dialog: ProgressDialog
    val buadrate = arrayListOf<String>(
        "9600",
        "19200",
        "57600",
        "115200",
        "230400",
        "250000",
        "500000",
        "1000000",
    )

    //    val modbusID = arrayListOf<String>(
//        "1",
//        "2",
//        "3",
//        "4",
//        "5",
//        "6",
//        "7"
//    )
    val modbusID = arrayListOf<String>().apply {
        for (i in 0..255) {
            this.add(i.toString())
        }
    }
    var selected_Id = 1
    var selected_buadrate = 9600

    override fun onAttach(context: Context) {
        super.onAttach(context)
        activity = getActivity() as MainActivity
        onBackPressed = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                activity!!.onFragmentChange(MainViewModel.ADMINFRAGMENT)
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(this, onBackPressed)
    }

    override fun onDetach() {
        super.onDetach()
        activity = null
        onBackPressed.remove()
    }

    override fun onDestroyView() {
        super.onDestroyView()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }

    }

    private fun setSaveData() {
        val tmp = ControlData()
        tmp.isMirrorMode = mBinding.swMirror.isChecked
        tmp.modbusBaudrate = ModbusBaudrate.codesMap.get(selected_buadrate)!!
        tmp.modbusRTUID = selected_Id
        tmp.useModbusRTU = mBinding.swConnectModbus.isChecked
//        tmp.useSettingShare = mBinding.swConnectSetting.isChecked
        shared.saveBoardSetData(SaminSharedPreference.CONTROL, tmp)
        Thread.sleep(500)

//        android.os.Process.killProcess(android.os.Process.myPid())
//        getActivity()?.let { finishAffinity(it) }
//        System.exit(0)
        val intent = Intent(context, AppRestartReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(context, 0, intent,
            PendingIntent.FLAG_IMMUTABLE)
        pendingIntent.send()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mBinding = FragmentControlBinding.inflate(inflater, container, false)
        shared = SaminSharedPreference(requireContext())
        setModbusIDSpinner()
        setModbusBuadrateSpinner()

        mBinding.swMirror.isChecked = viewmodel.controlData.isMirrorMode
        val ididx = modbusID.indexOf(viewmodel.controlData.modbusRTUID.toString())
        mBinding.spModbusId.setSelection(ididx)
        val baudidx = buadrate.indexOf(viewmodel.controlData.modbusBaudrate.value.toString())
        mBinding.spModbusBuadrate.setSelection(baudidx)
        mBinding.swConnectModbus.isChecked = viewmodel.controlData.useModbusRTU
//        mBinding.swConnectSetting.isChecked = viewmodel.controlData.useSettingShare

        mBinding.btnSettingSend.setOnClickListener {
            if (!mBinding.swMirror.isChecked)
                sendSettingValues()
        }

        mBinding.saveBtn.setOnClickListener {
            setSaveData()
        }

        mBinding.cancelBtn.setOnClickListener {
            activity?.onFragmentChange(MainViewModel.ADMINFRAGMENT)
        }

        mBinding.btnBack.setOnClickListener {
            activity?.onFragmentChange(MainViewModel.ADMINFRAGMENT)
        }
        return mBinding.root
    }

    private fun setModbusIDSpinner() {
        val arrayAdapter = ArrayAdapter(
            requireContext(),
            R.layout.support_simple_spinner_dropdown_item,
            modbusID
        )
        mBinding.spModbusId.adapter = arrayAdapter
        mBinding.spModbusId.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                selected_Id = modbusID[position].toInt()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                Toast.makeText(context, "아이디를 선택해주세요.", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    private fun setModbusBuadrateSpinner() {
        val arrayAdapter = ArrayAdapter(
            requireContext(),
            R.layout.support_simple_spinner_dropdown_item,
            buadrate
        )
        mBinding.spModbusBuadrate.adapter = arrayAdapter
        mBinding.spModbusBuadrate.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    selected_buadrate = buadrate[position].toInt()
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                    Toast.makeText(context, "통신속도를 선택해주세요.", Toast.LENGTH_SHORT)
                        .show()
                }
            }
    }

    private fun sendProtocolToSerial(data: ByteArray) {
        activity?.sendProtocolToSerial(data)
    }

    private fun sendMultipartSend(model: Byte, data: ByteArray? = null) {
        val protocol = SaminProtocol()

        if (data != null) {
            val chunked = data.asSequence().chunked(40) { t ->
                t.toByteArray()
            }
            var idx = 0
            for (tmp in chunked) {
                protocol.BuildProtocoOld(
                    model,
                    chunked.count().toByte(),
                    SaminProtocolMode.SettingShare.byte,
                    byteArrayOf(idx.toByte()) + tmp
                )
                sendProtocolToSerial(protocol.mProtocol.clone())
                idx++
                Thread.sleep(55)
            }
        } else {
            protocol.buildProtocol(model, 0.toByte(), SaminProtocolMode.SettingShare.byte, null)
            sendProtocolToSerial(protocol.mProtocol.clone())
        }
    }

    private fun getBase64byteArray(arg: ByteArray): ByteArray {
//        return Base64.getEncoder().encodeToString(arg).toByteArray()
        return Base64.encode(arg, 0)
    }

    private fun sendSettingValues() {
        getProgressShow()
        sendThread = Thread {
//            activity?.isAnotherJob = true
            activity?.isAnotherSettingJob?.set(true)
            Thread.sleep(500)
            try {
                var bytes: ByteArray? = null
                var byteRoom: ByteArray? = null
                var byteWaste: ByteArray? = null
                var byteOxyzen: ByteArray? = null
                var byteSteamer: ByteArray? = null
                var byteOxyzenMst: ByteArray? = null
                var byteModelmap: ByteArray? = null
                var byteLabName: ByteArray? = null
                var byteTemphum: ByteArray? = null
                val labname = SaminSharedPreference(requireContext()).loadLabNameData()

                byteModelmap = getBase64byteArray(ProtoBuf.encodeToByteArray(viewmodel.modelMap))

                viewmodel.GasStorageDataLiveList.value?.let {
                    bytes = getBase64byteArray(ProtoBuf.encodeToByteArray(it.toList()))
                }

                viewmodel.GasRoomDataLiveList.value?.let {
                    byteRoom = getBase64byteArray(ProtoBuf.encodeToByteArray(it.toList()))
                }

                viewmodel.WasteLiquorDataLiveList.value?.let {
                    byteWaste = getBase64byteArray(ProtoBuf.encodeToByteArray(it.toList()))
                    //                var ttt = ProtoBuf.decodeFromByteArray<List<SetWasteLiquorViewData>>(byteWaste!!)
                    //                println(ttt)
                }

                viewmodel.OxygenDataLiveList.value?.let {
                    byteOxyzen = getBase64byteArray(ProtoBuf.encodeToByteArray(it.toList()))
                }

                viewmodel.SteamerDataLiveList.value?.let {
                    byteSteamer = getBase64byteArray(ProtoBuf.encodeToByteArray(it.toList()))
                }

//                viewmodel.oxygenMasterData?.let {
//                    byteOxyzenMst = ProtoBuf.encodeToByteArray(it)
//                }
                viewmodel.TempHumDataLiveList.value?.let {
                    byteTemphum = getBase64byteArray(ProtoBuf.encodeToByteArray(it.toList()))
                }
                byteLabName = getBase64byteArray(ProtoBuf.encodeToByteArray(labname))

                progress_Dialog?.incrementProgressBy(40)

                for (i in 0..5) {
                    // 가스 스토리지
                    bytes.let {
                        sendMultipartSend((16 + 1).toByte(), it)
//                        Thread.sleep(40)
                    }

                    // 가스 룸
                    byteRoom.let {
                        sendMultipartSend((16 + 2).toByte(), it)
//                        Thread.sleep(40)
                    }

                    // 폐액통
                    byteWaste.let {
                        sendMultipartSend((16 + 3).toByte(), it)
//                        Thread.sleep(40)
                    }

                    // 산소
                    byteOxyzen.let {
                        sendMultipartSend((16 + 4).toByte(), it)
//                        if (viewmodel.oxygenMasterData != null) {
//                            byteOxyzenMst?.let {
//                                sendMultipartSend((16 + 6).toByte(), it)
////                                Thread.sleep(40)
//                            }
//                        }
                    }

                    // 스팀
                    byteSteamer.let {
                        sendMultipartSend((16 + 5).toByte(), it)
//                        Thread.sleep(40)
                    }

                    byteModelmap.let {
                        sendMultipartSend((16 + 7).toByte(), it)
                  //                        Thread.sleep(40)
                    }

                    byteTemphum.let {
                        sendMultipartSend((16 + 9).toByte(), it)
                    }

                    byteLabName.let {
                        sendMultipartSend((16 + 8).toByte(), it)
                    }
                    Thread.sleep(200)
                    progress_Dialog?.incrementProgressBy(10)
                }
                sendMultipartSend(32.toByte())

                Thread.sleep(100)

            } catch (e: Exception) {
                e.printStackTrace()
            }


//            activity?.isAnotherJob = false
            getProgressHidden()
            activity?.isAnotherSettingJob?.set(false)
        }

        sendThread?.start()
    }

    private fun getProgressShow() {
        try {
            val str_tittle = "Please Wait ..."
            val str_message = "잠시만 기다려주세요 ...\n진행 중입니다 ..."
            val str_buttonOK = "종료"
            val str_buttonNO = "취소"

            progress_Dialog = ProgressDialog(context)
            progress_Dialog.setTitle(str_tittle) //팝업창 타이틀 지정
            progress_Dialog.setIcon(R.mipmap.samin_launcher_ic) //팝업창 아이콘 지정
            progress_Dialog.setMessage(str_message) //팝업창 내용 지정
            progress_Dialog.setCancelable(false) //외부 레이아웃 클릭시도 팝업창이 사라지지않게 설정
            progress_Dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL) //프로그레스 원형 표시 설정
            progress_Dialog.setButton(
                /* whichButton = */ DialogInterface.BUTTON_POSITIVE,
                /* text = */ str_buttonOK,
            ) { _, _ ->
                try {
                    sendThread?.interrupt()
                    getProgressHidden()
                } catch (e: Exception) {
                }
            }
//            progress_Dialog.setButton(
//                DialogInterface.BUTTON_NEGATIVE,
//                str_buttonNO,
//                DialogInterface.OnClickListener { dialog, which ->
//                    getProgressHidden()
//                })
            try {
                progress_Dialog.show()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun getProgressHidden() {
        try {
            progress_Dialog.dismiss()
            progress_Dialog.cancel()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment ControlFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            ControlFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}
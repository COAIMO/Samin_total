package com.coai.samin_total

import android.app.Activity
import android.app.ProgressDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.app.ActivityCompat
import androidx.core.view.children
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.coai.samin_total.Dialog.AlertDialogFragment
import com.coai.samin_total.GasDock.SetGasStorageViewData
import com.coai.samin_total.GasRoom.SetGasRoomViewData
import com.coai.samin_total.Logic.SaminProtocol
import com.coai.samin_total.Logic.SaminSharedPreference
import com.coai.samin_total.Oxygen.SetOxygenViewData
import com.coai.samin_total.Steamer.SetSteamerViewData
import com.coai.samin_total.TempHum.SetTempHumViewData
import com.coai.samin_total.WasteLiquor.SetWasteLiquorViewData
import com.coai.samin_total.databinding.FragmentMainBinding
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [MainFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class MainFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    lateinit var mBinding: FragmentMainBinding
    var activity: MainActivity? = null
    private val viewmodel: MainViewModel by activityViewModels()
    lateinit var progress_Dialog: ProgressDialog
    lateinit var sendThread: Thread
    lateinit var alertdialogFragment: AlertDialogFragment
    lateinit var shared: SaminSharedPreference
    private lateinit var onBackPressed: OnBackPressedCallback
    var backKeyPressedTime: Long = 0
    private var taskRefresh: Thread? = null
    private var btn_Count = 0
    private var isOnTaskRefesh: Boolean = true
    var heartbeatCount: UByte = 0u
    //    lateinit var db: SaminDataBase

    override fun onAttach(context: Context) {
        activity = getActivity() as MainActivity
        super.onAttach(context)
        onBackPressed = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (System.currentTimeMillis() > backKeyPressedTime + 2500) {
                    backKeyPressedTime = System.currentTimeMillis()
                    return
                }
                if (System.currentTimeMillis() <= backKeyPressedTime + 2500) {
                    ActivityCompat.finishAffinity(activity!!)
                    System.exit(0)
                }
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(this, onBackPressed)
    }

    override fun onDetach() {
        activity = null
        super.onDetach()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    lateinit var thUIError: Thread
    var isrunthUIError = true
    override fun onResume() {
        super.onResume()
        uiError()
        isOnTaskRefesh = true
//        taskRefresh = Thread() {
//            try {
//                while (isOnTaskRefesh) {
//                    heartbeatCount++
//                    activity?.runOnUiThread() {
//                        mBinding.gasDockMainStatus.heartBeat(heartbeatCount)
//                        mBinding.gasRoomMainStatus.heartBeat(heartbeatCount)
//                        mBinding.oxygenMainStatus.heartBeat(heartbeatCount)
//                        mBinding.steamerMainStatus.heartBeat(heartbeatCount)
//                        mBinding.wasteLiquorMainStatus.heartBeat(heartbeatCount)
//                    }
//                    Thread.sleep(50)
//                }
//
//            } catch (e: Exception) {
////                e.printStackTrace()
//            }
//        }
//        taskRefresh?.start()

//        isrunthUIError = true
//        viewmodel.steamerAlert.value = false
//        thUIError = Thread {
//            try {
//                while (isrunthUIError) {
//                    // 메인화면 경고 유무 변화
//                    val targets = HashMap<Int, Int>()
//                    for (t in viewmodel.alertMap.values) {
//                        if (t.isAlert && !targets.containsKey(t.model)) {
//                            targets[t.model] = t.model
//                        }
//                    }
//
//
//                    activity?.runOnUiThread {
//                        try {
//                            viewmodel.gasStorageAlert.value = targets.containsKey(1)
//                        } catch (ex: Exception) {
//                        }
//                        try {
//                            viewmodel.gasRoomAlert.value = targets.containsKey(2)
//                        } catch (ex: Exception) {
//                        }
//                        try {
//                            viewmodel.wasteAlert.value = targets.containsKey(3)
//                        } catch (ex: Exception) {
//                        }
//                        try {
//                            viewmodel.oxyenAlert.value = targets.containsKey(4)
//                        } catch (ex: Exception) {
//                        }
//                        try {
//                            viewmodel.steamerAlert.value = targets.containsKey(5)
//                        } catch (ex: Exception) {
//                        }
//                    }
//
//                    Thread.sleep(100)
//                }
//            } catch (e : Exception) {
//
//            }
//        }
//        thUIError?.start()
    }

    override fun onPause() {
        super.onPause()
        isrunthUIError = false
        thUIError?.interrupt()
        thUIError?.join()

        isOnTaskRefesh = false
//        taskRefresh?.interrupt()
//        taskRefresh?.join()
    }

    var isFirst = true
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mBinding = FragmentMainBinding.inflate(inflater, container, false)
        shared = SaminSharedPreference(requireContext())
        setButtonClickEvent()
//            mBinding.gasDockMainStatusLayout.visibility = View.VISIBLE
//            mBinding.gasRoomMainStatusLayout.visibility = View.VISIBLE
//            mBinding.wasteLiquorMainStatusLayout.visibility = View.VISIBLE
//            mBinding.oxygenMainStatusLayout.visibility = View.VISIBLE
//            mBinding.steamerMainStatusLayout.visibility = View.VISIBLE
//            mBinding.tempHumMainStatusLayout.visibility = View.VISIBLE
//            mBinding.tempHumMainStatus.setAlert(true)
        if (isFirst) {
            isFirst = false
            initView()
        }
        mainLayoutIconVisibility()
        updateAlert()

        if (!shared.loadLabNameData().isEmpty()) {
            mBinding.labIDTextView.text = shared.loadLabNameData()
        }

//        mBinding.labIDTextView.setOnClickListener {
//            shared.removeBoardSetData(SaminSharedPreference.GASSTORAGE)
//            shared.removeBoardSetData(SaminSharedPreference.GASROOM)
//            shared.removeBoardSetData(SaminSharedPreference.WASTELIQUOR)
//            shared.removeBoardSetData(SaminSharedPreference.OXYGEN)
//            shared.removeBoardSetData(SaminSharedPreference.STEAMER)
//        }
        if (viewmodel.isSoundAlert) {
            mBinding.btnSound.setImageResource(R.drawable.sound_ic)
        } else {
            mBinding.btnSound.setImageResource(R.drawable.sound_mute_ic)
        }
        viewmodel.date.observe(viewLifecycleOwner){
            mBinding.tvCurruntTime.text = it
        }
        return mBinding.root
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment MainFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            MainFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    private fun setButtonClickEvent() {
        mBinding.gasDockMainStatus.setOnClickListener {
            onClick(mBinding.gasDockMainStatus)
        }
        mBinding.gasRoomMainStatus.setOnClickListener {
            onClick(mBinding.gasRoomMainStatus)
        }
        mBinding.wasteLiquorMainStatus.setOnClickListener {
            onClick(mBinding.wasteLiquorMainStatus)
        }
        mBinding.oxygenMainStatus.setOnClickListener {
            onClick(mBinding.oxygenMainStatus)
        }
        mBinding.steamerMainStatus.setOnClickListener {
            onClick(mBinding.steamerMainStatus)
        }
        mBinding.btnSetting.setOnClickListener {
            onClick(mBinding.btnSetting)
        }
        mBinding.btnAlert.setOnClickListener {
            onClick(mBinding.btnAlert)
        }
        mBinding.btnSound.setOnClickListener {
            onClick(mBinding.btnSound)
//            throw RuntimeException("Test Crash")
        }
        mBinding.tempHumMainStatus.setOnClickListener{
            onClick(it)
        }
//        mBinding.btnHomepage.setOnClickListener {
//            onClick(it)
//        }
    }

    private fun onClick(view: View) {
        when (view) {
            mBinding.gasDockMainStatus -> {
                activity?.onFragmentChange(MainViewModel.GASDOCKMAINFRAGMENT)
            }
            mBinding.gasRoomMainStatus -> {
                activity?.onFragmentChange(MainViewModel.GASROOMMAINFRAGMENT)
            }
            mBinding.wasteLiquorMainStatus -> {
                activity?.onFragmentChange(MainViewModel.WASTELIQUORMAINFRAGMENT)
            }
            mBinding.oxygenMainStatus -> {
                activity?.onFragmentChange(MainViewModel.OXYGENMAINFRAGMENT)
            }
            mBinding.steamerMainStatus -> {
                activity?.onFragmentChange(MainViewModel.STEAMERMAINFRAGMENT)
            }
            mBinding.btnSetting -> {
                activity?.onFragmentChange(MainViewModel.MAINSETTINGFRAGMENT)
            }
            mBinding.btnAlert -> {
                alertdialogFragment = viewmodel.alertDialogFragment
                val bundle = Bundle()
                bundle.putString("model", "Main")
                alertdialogFragment.arguments = bundle
                alertdialogFragment.show(requireActivity().supportFragmentManager, "Main")
                mBinding.btnAlert.setImageResource(R.drawable.nonalert_ic)
            }
            mBinding.btnSound -> {
                if (viewmodel.isSoundAlert) {
                    mBinding.btnSound.setImageResource(R.drawable.sound_mute_ic)
                    viewmodel.isSoundAlert = false
                } else {
                    mBinding.btnSound.setImageResource(R.drawable.sound_ic)
                    viewmodel.isSoundAlert = true
                }
            }
            mBinding.tempHumMainStatus ->{
                activity?.onFragmentChange(MainViewModel.TEMPHUMMAINFRAGMENT)
            }
//            mBinding.btnHomepage ->{
//                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("http://www.saminsci.com/"))
//                startActivity(intent)
//            }
        }
    }

    private fun sendAlertProtocol(data: ByteArray) {
        activity?.sendProtocolToSerial(data)
    }

    private fun scanModel() {
        getProgressShow()
        sendThread = Thread {
            try {
                viewmodel.isScanmode = true
                activity?.deleteExDataSet()
                activity?.feedBackThreadInterrupt()
                for (model in 1..5) {
                    for (id in 0..7) {
                        for (count in 0..2) {
                            val protocol = SaminProtocol()
                            protocol.checkModel(model.toByte(), id.toByte())
//                            activity?.serialService?.sendData(protocol.mProtocol)
                            sendAlertProtocol(protocol.mProtocol)
                            Thread.sleep(40)
                        }
                    }
                }
                Thread.sleep(400)
                viewmodel.isScanmode = false

            } catch (e: Exception) {
            }

            activity?.runOnUiThread {
                if (viewmodel.modelMap.isEmpty()) {
                    Toast.makeText(requireContext(), "연결된 AQ보드가 없습니다.", Toast.LENGTH_SHORT).show()
                }
                initView()
            }
//            createHasKey()
            if (!activity?.isSending!!) {
                activity?.callFeedback()
                activity?.isSending = true
            }
            shared.saveHashMap(viewmodel.modelMap)
            getProgressHidden()

        }
        sendThread.start()

    }


    private fun initView() {
        mBinding.gasDockMainStatusLayout.visibility = View.GONE
        mBinding.gasRoomMainStatusLayout.visibility = View.GONE
        mBinding.wasteLiquorMainStatusLayout.visibility = View.GONE
        mBinding.oxygenMainStatusLayout.visibility = View.GONE
        mBinding.steamerMainStatusLayout.visibility = View.GONE
//        if (viewmodel.isSoundAlert){
//            mBinding.btnSound.setImageResource(R.drawable.sound_ic)
//        }else{
//            mBinding.btnSound.setImageResource(R.drawable.sound_mute_ic)
//        }
        invalidateView()
        if (!shared.loadHashMap().isNullOrEmpty()) {
            shared.loadHashMap().forEach { (key, value) ->
                viewmodel.modelMap[key] = value
                var id = when {
                    key.equals("GasDock") -> 1
                    key.equals("GasRoom") -> 2
                    key.equals("WasteLiquor") -> 3
                    key.equals("Oxygen") -> 4
                    key.equals("Steamer") -> 5
                    key.equals("TempHum") -> 6
                    else -> 1
                }

                viewmodel.modelMapInt[id] = value.clone()
            }

            val storgeDataSet =
                shared.loadBoardSetData(SaminSharedPreference.GASSTORAGE) as MutableList<SetGasStorageViewData>
            viewmodel.GasStorageDataLiveList.clear(true)
            if (storgeDataSet.isNotEmpty()) {
                for (i in storgeDataSet) {
                    viewmodel.GasStorageDataLiveList.add(i)
                }
            }

            viewmodel.GasRoomDataLiveList.clear(true)
            val roomDataSet =
                shared.loadBoardSetData(SaminSharedPreference.GASROOM) as MutableList<SetGasRoomViewData>
            if (roomDataSet.isNotEmpty()) {
                for (i in roomDataSet) {
                    viewmodel.GasRoomDataLiveList.add(i)
                }
            }

            viewmodel.OxygenDataLiveList.clear(true)
            val oxygenDataSet =
                shared.loadBoardSetData(SaminSharedPreference.OXYGEN) as MutableList<SetOxygenViewData>
            if (oxygenDataSet.isNotEmpty()) {
                for (i in oxygenDataSet) {
                    viewmodel.OxygenDataLiveList.add(i)
                }
            }

            viewmodel.SteamerDataLiveList.clear(true)
            val steamerDataSet =
                shared.loadBoardSetData(SaminSharedPreference.STEAMER) as MutableList<SetSteamerViewData>
            if (steamerDataSet.isNotEmpty()) {
                for (i in steamerDataSet) {
                    viewmodel.SteamerDataLiveList.add(i)
                }
            }

            viewmodel.WasteLiquorDataLiveList.clear(true)
            val wasteDataSet =
                shared.loadBoardSetData(SaminSharedPreference.WASTELIQUOR) as MutableList<SetWasteLiquorViewData>
            if (wasteDataSet.isNotEmpty()) {
                for (i in wasteDataSet) {
                    viewmodel.WasteLiquorDataLiveList.add(i)
                }
            }
            viewmodel.TempHumDataLiveList.clear(true)
            val tempHumDataSet =
                 shared.loadBoardSetData(SaminSharedPreference.TEMPHUM) as MutableList<SetTempHumViewData>
            if (tempHumDataSet.isNotEmpty()){
                for (i in tempHumDataSet){
                    viewmodel.TempHumDataLiveList.add(i)
                }
            }

//            viewmodel.oxygenMasterData = null
//            viewmodel.oxygensData.clear()
//            val tmpobj =
//                shared.loadBoardSetData(SaminSharedPreference.MASTEROXYGEN)
//            if (tmpobj is SetOxygenViewData) {
//                viewmodel.oxygenMasterData = tmpobj
//            }
//            val oxygenMasterDataSet =
//                (shared.loadBoardSetData(SaminSharedPreference.MASTEROXYGEN)) as SetOxygenViewData

            activity?.isSending = true
            activity?.tmp?.LoadSetting()
            activity?.callFeedback()
            activity?.callTimemout()

        }

        createHasKey()
        mainLayoutIconVisibility()
    }

    private fun mainLayoutIconVisibility() {
        for ((key, ids) in viewmodel.modelMap) {
            when (key) {
                "GasDock" -> {
                    mBinding.gasDockMainStatusLayout.visibility = View.VISIBLE
                }
                "GasRoom" -> {
                    mBinding.gasRoomMainStatusLayout.visibility = View.VISIBLE
                }
                "WasteLiquor" -> {
                    mBinding.wasteLiquorMainStatusLayout.visibility = View.VISIBLE
                }
                "Oxygen" -> {
                    mBinding.oxygenMainStatusLayout.visibility = View.VISIBLE
                }
                "Steamer" -> {
                    mBinding.steamerMainStatusLayout.visibility = View.VISIBLE
                }
                "TempHum" ->{
                    mBinding.tempHumMainStatusLayout.visibility = View.VISIBLE
                }
            }
        }
        val visibleChlidren = mBinding.mainIconContainerLayout.children.filter {
            it.visibility == View.VISIBLE
        }
        when (visibleChlidren.count()) {
            2 -> {
                for (i in visibleChlidren) {
                    val params = LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    )
                    params.setMargins(170, 0, 170, 0)
                    i.layoutParams = params
                }
            }
            3 -> {
                for (i in visibleChlidren) {
                    val params = LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    )
                    params.setMargins(120, 0, 120, 0)
                    i.layoutParams = params
                }
            }
            4 -> {
                for (i in visibleChlidren) {
                    val params = LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    )
                    params.setMargins(50, 0, 50, 0)
                    i.layoutParams = params
                }
            }
            5 -> {
                for (i in visibleChlidren) {
                    val params = LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    )
                    params.setMargins(15, 0, 15, 0)
                    i.layoutParams = params
                }
            }
            else -> {}
        }
        invalidateView()
    }

    fun invalidateView() {
        Thread.sleep(100)
        mBinding.gasDockMainStatusLayout.invalidate()
        mBinding.gasRoomMainStatusLayout.invalidate()
        mBinding.wasteLiquorMainStatusLayout.invalidate()
        mBinding.oxygenMainStatusLayout.invalidate()
        mBinding.steamerMainStatusLayout.invalidate()
        mBinding.tempHumMainStatusLayout.invalidate()
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
            progress_Dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER) //프로그레스 원형 표시 설정
            progress_Dialog.setButton(
                DialogInterface.BUTTON_POSITIVE,
                str_buttonOK,
                DialogInterface.OnClickListener { dialog, which ->
                    try {
                        sendThread.interrupt()
                        viewmodel.removeModelMap()
                        getProgressHidden()
                    } catch (e: Exception) {
                    }
                })
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

    fun updateAlert() {
        viewmodel.wasteAlert.observe(viewLifecycleOwner) {
            if (it) {
                mBinding.wasteLiquorMainStatus.setAlert(true)
            } else {
                mBinding.wasteLiquorMainStatus.setAlert(false)
            }
        }
        viewmodel.oxyenAlert.observe(viewLifecycleOwner) {
            if (it) {
                mBinding.oxygenMainStatus.setAlert(true)
            } else {
                mBinding.oxygenMainStatus.setAlert(false)

            }
        }
        viewmodel.gasStorageAlert.observe(viewLifecycleOwner) {
            if (it) {
                mBinding.gasDockMainStatus.setAlert(true)
            } else {
                mBinding.gasDockMainStatus.setAlert(false)
            }
        }
        viewmodel.steamerAlert.observe(viewLifecycleOwner) {
            if (it) {
                mBinding.steamerMainStatus.setAlert(true)
            } else {
                mBinding.steamerMainStatus.setAlert(false)
            }
        }
        viewmodel.gasRoomAlert.observe(viewLifecycleOwner) {
            if (it) {
                mBinding.gasRoomMainStatus.setAlert(true)
            } else {
                mBinding.gasRoomMainStatus.setAlert(false)
            }
        }
        viewmodel.tempHumAlert.observe(viewLifecycleOwner){
            if (it) {
                mBinding.tempHumMainStatus.setAlert(true)
            } else {
                mBinding.tempHumMainStatus.setAlert(false)
            }
        }

    }

    private fun uiError() {
        isrunthUIError = true
        thUIError = Thread {
            try {
                while (isrunthUIError) {
                    // 메인화면 경고 유무 변화
                    val targets = HashMap<Int, Int>()
                    for (t in viewmodel.alertMap.values) {
                        if (t.isAlert && !targets.containsKey(t.model)) {
                            targets[t.model] = t.model
                        }
                    }

                    activity?.runOnUiThread {
                        if (targets.isNotEmpty()) {
                            mBinding.btnAlert.setImageResource(R.drawable.onalert_ic)
                        } else {
                            mBinding.btnAlert.setImageResource(R.drawable.nonalert_ic)
                        }
                    }
                    Thread.sleep(100)
                }
            } catch (e: Exception) {

            }
        }
        thUIError?.start()
    }

    private fun createHasKey() {
        for ((key, value) in viewmodel.modelMapInt) {
            val model = key.toByte()
            for (i in value) {
                val id = i
                if (key == 1 || key == 2 || key == 3) {
                    for (port in 1..4) {
                        val createkey =
                            littleEndianConversion(
                                byteArrayOf(
                                    model,
                                    id,
                                    port.toByte()
                                )
                            )
                        viewmodel.hasKey.put(createkey, createkey)
                    }
                } else if (key == 4 || key == 6) {
                    val createkey =
                        littleEndianConversion(
                            byteArrayOf(
                                model,
                                id,
                                1.toByte()
                            )
                        )
                    viewmodel.hasKey.put(createkey, createkey)
                } else if (key == 5) {
                    for (port in 1..2) {
                        val createkey =
                            littleEndianConversion(
                                byteArrayOf(
                                    model,
                                    id,
                                    port.toByte()
                                )
                            )
                        viewmodel.hasKey.put(createkey, createkey)
                    }
                }
            }
        }
    }

    private fun littleEndianConversion(bytes: ByteArray): Int {
        var result = 0
        for (i in bytes.indices) {
            result = result or (bytes[i].toUByte().toInt() shl 8 * i)
        }
        return result
    }
}
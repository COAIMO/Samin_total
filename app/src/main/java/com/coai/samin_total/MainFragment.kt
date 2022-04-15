package com.coai.samin_total

import android.app.ProgressDialog
import android.content.*
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Message
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.activityViewModels
import com.coai.libmodbus.service.SaminModbusService
import com.coai.libsaminmodbus.model.ModelMonitorValues
import com.coai.libsaminmodbus.model.ObserveModelMonitorValues
import com.coai.samin_total.Dialog.AlertDialogFragment
import com.coai.samin_total.GasDock.SetGasStorageViewData
import com.coai.samin_total.GasRoom.SetGasRoomViewData
import com.coai.samin_total.Logic.SaminProtocol
import com.coai.samin_total.Logic.SaminSharedPreference
import com.coai.samin_total.Oxygen.SetOxygenViewData
import com.coai.samin_total.Steamer.SetSteamerViewData
import com.coai.samin_total.WasteLiquor.SetWasteLiquorViewData
import com.coai.samin_total.databinding.FragmentMainBinding
import java.lang.ref.WeakReference
import java.util.*
import kotlin.concurrent.thread

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
    private var btn_Count = 0
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
                    activity?.finishAffinity()
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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mBinding = FragmentMainBinding.inflate(inflater, container, false)
        shared = SaminSharedPreference(requireContext())
        setButtonClickEvent()
        initView()
        udateAlert()

        mBinding.labIDTextView.setOnClickListener {
            shared.removeBoardSetData(SaminSharedPreference.GASSTORAGE)
            shared.removeBoardSetData(SaminSharedPreference.GASROOM)
            shared.removeBoardSetData(SaminSharedPreference.WASTELIQUOR)
            shared.removeBoardSetData(SaminSharedPreference.OXYGEN)
            shared.removeBoardSetData(SaminSharedPreference.STEAMER)
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
        }
        mBinding.btnScan.setOnClickListener {
            onClick(mBinding.btnScan)
        }
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
                alertdialogFragment = AlertDialogFragment()
                val bundle = Bundle()
                bundle.putString("model", "Main")
                alertdialogFragment.arguments = bundle
                alertdialogFragment.show(requireActivity().supportFragmentManager, "Main")
                mBinding.btnAlert.setImageResource(R.drawable.nonalert_ic)

            }
            mBinding.btnSound -> {

                if (btn_Count % 2 == 0) {
                    btn_Count++
                    mBinding.btnSound.setImageResource(R.drawable.sound_mute_ic)
                    viewmodel.isSoundAlert = false
                } else {
                    btn_Count++
                    mBinding.btnSound.setImageResource(R.drawable.sound_ic)
                    viewmodel.isSoundAlert = true
                }
            }
            mBinding.btnScan -> {
                scanModel()
            }
        }
    }

    private fun scanModel() {
        activity?.feedBackThreadInterrupt()
        activity?.deleteExDataSet()
        getProgressShow()
        sendThread = Thread {
            try {
                for (model in 1..5) {
                    for (id in 0..7) {
                        for (count in 0..2) {
                            val protocol = SaminProtocol()
                            protocol.checkModel(model.toByte(), id.toByte())
                            activity?.serialService?.sendData(protocol.mProtocol)
                            Thread.sleep(40)
                        }
                    }
                }
                Thread.sleep(400)

            } catch (e: Exception) {
            }

            activity?.runOnUiThread {
                if (viewmodel.modelMap.isEmpty()) {
                    Toast.makeText(requireContext(), "연결된 AQ보드가 없습니다.", Toast.LENGTH_SHORT).show()
                }
                initView()
            }
            getProgressHidden()
            if (!activity?.isSending!!) {
                activity?.callFeedback()
                activity?.isSending = true
            }
            shared.saveHashMap(viewmodel.modelMap)

        }
        sendThread.start()

    }

    private fun initView() {
        mBinding.labIDTextView.text = viewmodel.labName
        mBinding.gasDockMainStatusLayout.visibility = View.GONE
        mBinding.gasRoomMainStatusLayout.visibility = View.GONE
        mBinding.wasteLiquorMainStatusLayout.visibility = View.GONE
        mBinding.oxygenMainStatusLayout.visibility = View.GONE
        mBinding.steamerMainStatusLayout.visibility = View.GONE
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
            val oxygenDataSet = shared.loadBoardSetData(SaminSharedPreference.OXYGEN) as MutableList<SetOxygenViewData>
            if (oxygenDataSet.isNotEmpty()){
                for (i in oxygenDataSet){
                    viewmodel.OxygenDataLiveList.add(i)
                }
            }

            viewmodel.SteamerDataLiveList.clear(true)
            val steamerDataSet = shared.loadBoardSetData(SaminSharedPreference.STEAMER) as MutableList<SetSteamerViewData>
            if (steamerDataSet.isNotEmpty()){
                for (i in steamerDataSet){
                    viewmodel.SteamerDataLiveList.add(i)
                }
            }

            viewmodel.WasteLiquorDataLiveList.clear(true)
            val wasteDataSet = shared.loadBoardSetData(SaminSharedPreference.WASTELIQUOR) as MutableList<SetWasteLiquorViewData>
            if (wasteDataSet.isNotEmpty()){
                for (i in wasteDataSet){
                    viewmodel.WasteLiquorDataLiveList.add(i)
                }
            }
            activity?.tmp?.LoadSetting()
            activity?.isSending = true
            activity?.callFeedback()
            activity?.callTimemout()

        }

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
            }
        }
        invalidateView()
    }

    fun invalidateView(){
        Thread.sleep(100)
        mBinding.gasDockMainStatusLayout.invalidate()
        mBinding.gasRoomMainStatusLayout.invalidate()
        mBinding.wasteLiquorMainStatusLayout.invalidate()
        mBinding.oxygenMainStatusLayout.invalidate()
        mBinding.steamerMainStatusLayout.invalidate()
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

    private fun udateAlert() {
        viewmodel.wasteAlert.observe(viewLifecycleOwner) {
            if (it) {
                mBinding.wasteLiquorMainStatus.setAlert(true)
                mBinding.btnAlert.setImageResource(R.drawable.onalert_ic)
            } else {
                mBinding.wasteLiquorMainStatus.setAlert(false)
                mBinding.btnAlert.setImageResource(R.drawable.nonalert_ic)

            }
        }
        viewmodel.oxyenAlert.observe(viewLifecycleOwner) {
            if (it) {
                mBinding.oxygenMainStatus.setAlert(true)
                mBinding.btnAlert.setImageResource(R.drawable.onalert_ic)
            } else {
                mBinding.oxygenMainStatus.setAlert(false)
                mBinding.btnAlert.setImageResource(R.drawable.nonalert_ic)

            }
        }
        viewmodel.gasStorageAlert.observe(viewLifecycleOwner) {
            if (it) {
                mBinding.gasDockMainStatus.setAlert(true)
                mBinding.btnAlert.setImageResource(R.drawable.onalert_ic)
            } else {
                mBinding.gasDockMainStatus.setAlert(false)
                mBinding.btnAlert.setImageResource(R.drawable.nonalert_ic)
            }
        }
        viewmodel.steamerAlert.observe(viewLifecycleOwner) {
            if (it) {
                mBinding.steamerMainStatus.setAlert(true)
                mBinding.btnAlert.setImageResource(R.drawable.onalert_ic)
            } else {
                mBinding.steamerMainStatus.setAlert(false)
                mBinding.btnAlert.setImageResource(R.drawable.nonalert_ic)
            }
        }
        viewmodel.gasRoomAlert.observe(viewLifecycleOwner) {
            if (it) {
                mBinding.gasRoomMainStatus.setAlert(true)
                mBinding.btnAlert.setImageResource(R.drawable.onalert_ic)
            } else {
                mBinding.gasRoomMainStatus.setAlert(false)
                mBinding.btnAlert.setImageResource(R.drawable.nonalert_ic)

            }
        }

    }

}
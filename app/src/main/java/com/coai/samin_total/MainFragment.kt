package com.coai.samin_total

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.activity.OnBackPressedCallback
import androidx.core.view.children
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.coai.samin_total.Dialog.AlertDialogFragment
import com.coai.samin_total.GasDock.SetGasStorageViewData
import com.coai.samin_total.GasRoom.SetGasRoomViewData
import com.coai.samin_total.Logic.SaminSharedPreference
import com.coai.samin_total.Oxygen.SetOxygenViewData
import com.coai.samin_total.Steamer.SetSteamerViewData
import com.coai.samin_total.TempHum.SetTempHumViewData
import com.coai.samin_total.WasteLiquor.SetWasteLiquorViewData
import com.coai.samin_total.databinding.FragmentMainBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean


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
//    lateinit var progress_Dialog: ProgressDialog
//    lateinit var sendThread: Thread
    lateinit var alertdialogFragment: AlertDialogFragment
    lateinit var shared: SaminSharedPreference
    private lateinit var onBackPressed: OnBackPressedCallback
    var backKeyPressedTime: Long = 0
//    private var taskRefresh: Thread? = null
    private var btn_Count = 0
//    private var isOnTaskRefesh: Boolean = true
//    private val isOnTaskRefesh = AtomicBoolean(true)
    var heartbeatCount: UByte = 0u
    //    lateinit var db: SaminDataBase

    private var isOnTaskRefesh = AtomicBoolean(true)
    private var updateJob: Job? = null

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
                    activity?.finishAndRemoveTask()
//                    ActivityCompat.finishAffinity(activity!!)
//                    System.exit(0)
                }
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(this, onBackPressed)

        isOnTaskRefesh.set(true)
        startUpdateTask()
    }

    override fun onDetach() {
        activity = null
        super.onDetach()

        isOnTaskRefesh.set(false)
        stopUpdateTask()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onPause() {
        super.onPause()
        Log.d("MainFragment", "onPause ============================")

        activity?.shared?.setFragment(MainViewModel.MAINFRAGMENT)
    }

//    var isFirst = true
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mBinding = FragmentMainBinding.inflate(inflater, container, false)
        shared = SaminSharedPreference(requireContext())
        setButtonClickEvent()

        if (activity?.isFirstRun?.get() == true) {
            activity?.isFirstRun?.set(false)
            initView()
        }
        mainLayoutIconVisibility()
        updateAlert()

        if (!shared.loadLabNameData().isEmpty()) {
            mBinding.labIDTextView.text = shared.loadLabNameData()
        }

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
        }
        mBinding.tempHumMainStatus.setOnClickListener{
            onClick(it)
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
                activity?.shared?.saveAlarmSound(viewmodel.isSoundAlert)
            }
            mBinding.tempHumMainStatus ->{
                activity?.onFragmentChange(MainViewModel.TEMPHUMMAINFRAGMENT)
            }
        }
    }

    private fun initView() {
        mBinding.gasDockMainStatusLayout.visibility = View.GONE
        mBinding.gasRoomMainStatusLayout.visibility = View.GONE
        mBinding.wasteLiquorMainStatusLayout.visibility = View.GONE
        mBinding.oxygenMainStatusLayout.visibility = View.GONE
        mBinding.steamerMainStatusLayout.visibility = View.GONE

        invalidateView()
        val loadhashmap = shared.loadHashMap()
        Log.d("MainFrag", "loadhashmap : ${loadhashmap.size}")
        if (!loadhashmap.isNullOrEmpty()) {
            loadhashmap.forEach { (key, value) ->
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

//            activity?.isSending?.set(true)
            activity?.tmp?.LoadSetting()
            activity?.callFeedback()
            activity?.callTimemout()

        }
        else {
            activity?.callTimemout()
        }

        Log.d("MainFag", "callTimemout ================================>>>>>>>>>>>>>>>>>>>>>>><<<<<<<<<<<<<<<<<<<<<<")


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

    private fun startUpdateTask() {
        updateJob = CoroutineScope(Dispatchers.Main).launch {
            while (isOnTaskRefesh.get()) {
                // 메인화면 경고 유무 변화
                val targets = HashMap<Int, Int>()
                for (t in viewmodel.alertMap.values) {
                    if (t.isAlert && !targets.containsKey(t.model)) {
                        targets[t.model] = t.model
                    }
                }

                if (targets.isNotEmpty()) {
                    mBinding.btnAlert.setImageResource(R.drawable.onalert_ic)
                } else {
                    mBinding.btnAlert.setImageResource(R.drawable.nonalert_ic)
                }
                delay(100)
            }
        }

        Log.d("MainFrag", "updateJob id : ${updateJob}")
    }

    private fun stopUpdateTask() {
        updateJob?.cancel()
    }
}
package com.coai.samin_total

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.core.view.children
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.coai.samin_total.CustomView.*
import com.coai.samin_total.GasDock.SetGasdockViewData
import com.coai.samin_total.GasRoom.SetGasRoomViewData
import com.coai.samin_total.Logic.SaminProtocol
import com.coai.samin_total.Logic.ThreadSynchronied
import com.coai.samin_total.Oxygen.SetOxygenViewData
import com.coai.samin_total.Steamer.SetSteamerViewData
import com.coai.samin_total.WasteLiquor.SetWasteLiquorViewData
import com.coai.samin_total.databinding.FragmentAqSettingBinding

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"


// TODO: 2022-01-28 AQ ID는 앱시작 시 호출되서 저장되어있으며, 저장된 아이디값은 받아와야야됨 (알람켜기, 끄기, led경고, 정상만)UI 작업
class AqSettingFragment : Fragment() {
    private var param1: String? = null
    private var param2: String? = null
    var activity: MainActivity? = null
    private lateinit var onBackPressed: OnBackPressedCallback
    private lateinit var mBinding: FragmentAqSettingBinding
    private val viewmodel by activityViewModels<MainViewModel>()
    private val aqInfoData = mutableListOf<SetAqInfo>()
    private lateinit var recycleAdapter: AqSetting_RecycleAdapter
    val aqSettingViewList = mutableListOf<View>()
    val aqInfo_ViewMap = HashMap<SetAqInfo, View>()
    private lateinit var sendThread: Thread
    private lateinit var progressThread: Thread
    private val progressSync = ThreadSynchronied()
    private val sendSync = ThreadSynchronied()

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
        mBinding = FragmentAqSettingBinding.inflate(inflater, container, false)
        setButtonClickEvent()
        initRecycler()
        initProgressBar()
        initView()

        recycleAdapter.setItemClickListener(object : AqSetting_RecycleAdapter.OnItemClickListener {
            override fun onClick(v: View, position: Int) {
                for (i in mBinding.boardsettingContainer.children) {
                    i.visibility = View.INVISIBLE
                }
                aqSettingViewList[position].visibility = View.VISIBLE
            }
        })

        return mBinding.root
    }

    private fun setButtonClickEvent() {
        mBinding.searchBtn.setOnClickListener {
            onClick(mBinding.searchBtn)
        }
        mBinding.saveBtn.setOnClickListener {
            onClick(mBinding.saveBtn)
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun initView() {
        recycleAdapter.submitList(aqInfoData)
        recycleAdapter.notifyDataSetChanged()

        mBinding.boardsettingContainer.removeAllViews()
//         TODO: 2022-03-23 aq셋팅 view 이전데이터 불러오기 안됨..

    }

    private fun onClick(view: View) {
        when (view) {
            mBinding.searchBtn -> {
                progressThread = Thread {
                    activity?.runOnUiThread {
                        mBinding.aqSettingView.setBackgroundColor(Color.parseColor("#919191"))
                        disableEnableControls(false, mBinding.aqSettingView)
                        showProgrss(true)
                    }
                    sendSync.waitOne()
                    activity?.runOnUiThread {
                        mBinding.aqSettingView.setBackgroundColor(Color.parseColor("#FFFFFF"))
                        disableEnableControls(true, mBinding.aqSettingView)
                        showProgrss(false)
                    }

                }
                progressThread.start()

                sendThread = Thread {
                    try {
                        activity?.runOnUiThread {
                            aqInfoData.removeAll(aqInfoData)
                            recycleAdapter.submitList(aqInfoData)
                            recycleAdapter.notifyDataSetChanged()
                        }
                        for (model in 0..5) {
                            for (id in 0..7) {
                                for (count in 0..2) {
                                    val protocol = SaminProtocol()
                                    protocol.checkModel(model.toByte(), id.toByte())
                                    activity?.serialService?.sendData(protocol.mProtocol)
                                    Thread.sleep(25)
                                }
                            }
                        }
                        setAqInfoView()
                        sendSync.set()
                    } catch (e: Exception) {
                        Log.d("세팅", "e: $e")
                    }
                    sendSync.set()
                }
                sendThread.start()
            }

            mBinding.saveBtn -> {
                //뷰 상태값 저장하고 데이터 aq세팅 데이터 넘기기
                for ((aqInfo, view) in aqInfo_ViewMap) {
                    when (aqInfo.model) {
                        "GasDock" -> {
                            view as GasStorageBoardSettingView
                            val sensorType = view.selected_SensorType
                            val gasName = view.selected_GasType
                            val minCapa: Float = view.mCapaAlert_Et.text.toString().toFloat()
                            val maxCapa: Float = view.mMaxCapa_Et.text.toString().toFloat()
                            val gasIndex: Int? = null
                            val id = aqInfo.id
                            val port = aqInfo.port
                            viewmodel.GasStorageDataLiveList.add(
                                SetGasdockViewData(
                                    id = id,
                                    port = port,
                                    0,
                                    gasName = gasName,
                                    gasColor = Color.parseColor("#6599CD"),
                                    pressure_Min = minCapa,
                                    pressure_Max = maxCapa,
                                    gasIndex = gasIndex
                                )
                            )

                        }
                        "GasRoom" -> {
                            view as GasStorageBoardSettingView
                            //가스 컬러
                            val sensorType = view.selected_SensorType
                            val gasName = view.selected_GasType
                            val pressureMax: Float = view.mMaxCapa_Et.text.toString().toFloat()
                            val id = aqInfo.id
                            val port = aqInfo.port
                            viewmodel.GasRoomDataLiveList.add(
                                SetGasRoomViewData(
                                    id = id,
                                    port = port,
                                    gasName = gasName,
                                    gasColor = Color.parseColor("#6599CD"),
                                    pressure = 0f,
                                    pressureMax = pressureMax,
                                    gasUnit = 0,
                                    gasIndex = 0,
                                    isAlert = false
                                )
                            )


                        }
                        "WasteLiquor" -> {
                            view as WasteLiquorBoardSettingView
                            val id = aqInfo.id
                            val port = aqInfo.port
                            val liquidName: String = view.mWasteName_Et.text.toString()
                            viewmodel.WasteLiquorDataLiveList.add(
                                SetWasteLiquorViewData(
                                    id = id,
                                    port = port,
                                    liquidName = liquidName,
                                    isAlert = false
                                )
                            )
                        }
                        "Oxygen" -> {
                            view as OxygenBoardSettingView
                            val minValue = view.mOxygen_minValue_et.text.toString().toInt()
                            val id = aqInfo.id
                            viewmodel.OxygenDataLiveList.add(
                                SetOxygenViewData(
                                    id = id,
                                    setValue = 0,
                                    setMinValue = minValue
                                )
                            )

                        }
                        "Steamer" -> {
                            view as SteamerBoardSettingView
                            val id = aqInfo.id
                            val port = aqInfo.port
                            val isTempMin = view.mTemp_minValue_et.text.toString().toInt()
                            val isTemp = 0
                            viewmodel.SteamerDataLiveList.add(
                                SetSteamerViewData(
                                    id = id,
                                    port = port,
                                    isAlertLow = false,
                                    isTempMin = isTempMin,
                                    isTemp = isTemp
                                )
                            )
                        }
                    }

                }
                activity?.onFragmentChange(MainViewModel.MAINFRAGMENT)
                activity?.callFeedback()
            }
        }

    }

    private fun setAqInfoView() {
        //기존 데이터 삭제
        for (i in aqInfoData) {
            aqInfo_ViewMap.remove(i)
        }
        aqSettingViewList.removeAll(aqSettingViewList)

        activity?.runOnUiThread {
//            mBinding.boardRecyclerView.apply {
//                aqInfoData
//            }
            recycleAdapter.submitList(aqInfoData)
            recycleAdapter.notifyDataSetChanged()
        }



        for ((key, ids) in viewmodel.modelMap) {
            //indices 배열을 인덱스 범위
            for (i in ids.indices) {
                when (key) {
                    "GasDock" -> {
                        for (port in 1..4) {
                            aqInfoData.add(SetAqInfo(key, ids.get(i).toInt(), port))
                        }
                    }
                    "GasRoom" -> {
                        for (port in 1..4) {
                            aqInfoData.add(SetAqInfo(key, ids.get(i).toInt(), port))
                        }
                    }
                    "WasteLiquor" -> {
                        for (port in 1..4) {
                            aqInfoData.add(SetAqInfo(key, ids.get(i).toInt(), port))
                        }
                    }
                    "Oxygen" -> {
                        aqInfoData.add(SetAqInfo(key, ids.get(i).toInt(), 1))
                    }
                    "Steamer" -> {
                        for (port in 1..2) {
                            aqInfoData.add(SetAqInfo(key, ids.get(i).toInt(), port))
                        }
                    }
                }
            }
        }
        Log.d(
            "세팅",
            "aqInfoData Size: ${aqInfoData.size}  // aqInfoData :${aqInfoData}"
        )

        //여기 막음

        activity?.runOnUiThread {
            recycleAdapter.submitList(aqInfoData)
            recycleAdapter.notifyDataSetChanged()
        }


        for (i in aqInfoData) {
            when (i.model) {
                "GasDock" -> {
//                    val view = GasStorageBoardSettingView(
//                        requireActivity()
//                    )
//                    aqSettingViewList.add(view)
//                    aqInfo_ViewMap.put(i, view)
                }
                "GasRoom" -> {
//                    val view = GasStorageBoardSettingView(
//                        requireActivity()
//                    )
//                    aqSettingViewList.add(view)
//                    aqInfo_ViewMap.put(i, view)
                }
                "WasteLiquor" -> {
                    val view = WasteLiquorBoardSettingView(requireActivity())
                    aqSettingViewList.add(view)
                    aqInfo_ViewMap.put(i, view)

                }
                "Oxygen" -> {
                    val view = OxygenBoardSettingView(requireActivity())
                    aqSettingViewList.add(view)
                    aqInfo_ViewMap.put(i, view)
                }
                "Steamer" -> {
                    val view = SteamerBoardSettingView(requireActivity())
                    aqSettingViewList.add(view)
                    aqInfo_ViewMap.put(i, view)
                }
            }

        }
        Log.d(
            "세팅",
            "aqSettingViewList Size: ${aqSettingViewList.size}} "
        )
        for (i in aqSettingViewList) {
            Log.d(
                "세팅",
                "aqSettingViewList : ${i}} "
            )
            i.visibility = View.INVISIBLE
            mBinding.boardsettingContainer.addView(i)

        }
    }

    private fun initRecycler() {
        mBinding.boardRecyclerView.apply {
            layoutManager =
                LinearLayoutManager(context)

            //아이템 높이 간격 조절
            val decoration = SpaceDecoration(20, 20)
            addItemDecoration(decoration)

            recycleAdapter = AqSetting_RecycleAdapter()
//            recycleAdapter.submitList(singleDockViewData)
            adapter = recycleAdapter
        }

    }

    private fun initProgressBar() {
        showProgrss(false)
    }

    private fun showProgrss(isShow: Boolean) {
        if (isShow) mBinding.progressLayout.visibility = View.VISIBLE
        else mBinding.progressLayout.visibility = View.INVISIBLE
    }

    private fun disableEnableControls(enable: Boolean, vg: ViewGroup) {
        for (i in 0 until vg.childCount) {
            val child = vg.getChildAt(i)
            child.isEnabled = enable
            if (child is ViewGroup) {
                disableEnableControls(enable, child)
            }
        }
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment AqSettingFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            AqSettingFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}
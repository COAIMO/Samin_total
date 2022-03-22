package com.coai.samin_total

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.view.allViews
import androidx.core.view.children
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.coai.samin_total.CustomView.*
import com.coai.samin_total.GasDock.SetGasdockViewData
import com.coai.samin_total.GasRoom.SetGasRoomViewData
import com.coai.samin_total.Logic.SaminProtocol
import com.coai.samin_total.Oxygen.SetOxygenViewData
import com.coai.samin_total.Service.HexDump
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
        recycleAdapter.setItemClickListener(object : AqSetting_RecycleAdapter.OnItemClickListener {
            override fun onClick(v: View, position: Int) {
//                    v.setBackgroundColor(Color.parseColor("#ff9800"))
                //미리 뷰객체 생성해서 컨트롤방법

                for (i in mBinding.boardsettingContainer.children) {
                    i.visibility = View.INVISIBLE
                }

//                when (aqInfoData[position].model) {
//                    "Oxygen" -> {
//                        val views = OxygenBoardSettingView(requireActivity())
////                        val view = getActivity()?.let { OxygenBoardSettingView(it) }
//                        mBinding.boardsettingContainer.addView(views)
//                    }
//                    "GasRoom" -> {
//                        val view = getActivity()?.let { GasBoardSettingView(it) }
//                        mBinding.boardsettingContainer.addView(view)
//                    }
//                    "GasDock" -> {
//                        val view = getActivity()?.let { GasBoardSettingView(it) }
//                        mBinding.boardsettingContainer.addView(view)
//                    }
//                    "Steamer" -> {
//                        val view = getActivity()?.let { SteamerBoardSettingView(it) }
//                        mBinding.boardsettingContainer.addView(view)
//                    }
//                    "WasteLiquor" -> {
//                        val view = getActivity()?.let { WasteLiquorBoardSettingView(it) }
//                        mBinding.boardsettingContainer.addView(view)
//                    }
//                }

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

    private fun onClick(view: View) {
        when (view) {
            mBinding.searchBtn -> {
                sendThread = Thread {
                    try {
                        activity?.runOnUiThread {
                            aqInfoData.removeAll(aqInfoData)
                            recycleAdapter.submitList(aqInfoData)
                            recycleAdapter.notifyDataSetChanged()
                        }
                        for (model in 0..5) {
                            for (id in 0..7) {
//                                for (count in 0..4){
                                val protocol = SaminProtocol()
                                protocol.checkModel(model.toByte(), id.toByte())
                                activity?.serialService?.sendData(protocol.mProtocol)
                                Thread.sleep(100)
//                                }
                            }
                        }
                        setAqInfoView()
//                        for ((key, value) in viewmodel.modelMap) {
////                            Log.d(
////                                "세팅",
////                                "modelMap(key): ${key} // modelMap(value):${HexDump.dumpHexString(viewmodel.modelMap[key])} "
////                            )
//                            when (key) {
//                                "Oxygen" -> {
//                                    Log.d(
//                                        "세팅",
//                                        "modelMap(key): ${key} // Oxygen:${
//                                            HexDump.dumpHexString(
//                                                value
//                                            )
//                                        } "
//                                    )
//                                    for (i in value.indices) {
//                                        activity?.runOnUiThread {
//                                            mBinding.boardRecyclerView.apply {
//                                                aqInfoData.add(
//                                                    SetAqInfo(key, value.get(i).toInt())
//                                                )
//                                            }
//
//                                            recycleAdapter.submitList(aqInfoData)
//                                            recycleAdapter.notifyDataSetChanged()
//                                        }
//                                    }
//
//                                }
//                                "GasRoom" -> {
//                                    for (i in value.indices) {
//                                        activity?.runOnUiThread {
//                                            mBinding.boardRecyclerView.apply {
//                                                aqInfoData.add(
//                                                    SetAqInfo(key, value.get(i).toInt())
//                                                )
//                                            }
//
//                                            recycleAdapter.submitList(aqInfoData)
//                                            recycleAdapter.notifyDataSetChanged()
//                                        }
//                                    }
//                                }
//                                "GasDock" -> {
//                                    for (i in value.indices) {
//                                        activity?.runOnUiThread {
//                                            mBinding.boardRecyclerView.apply {
//                                                aqInfoData.add(
//                                                    SetAqInfo(key, value.get(i).toInt())
//                                                )
//                                            }
//
//                                            recycleAdapter.submitList(aqInfoData)
//                                            recycleAdapter.notifyDataSetChanged()
//                                        }
//                                    }
//                                }
//                                "Steamer" -> {
//                                    for (i in value.indices) {
//                                        activity?.runOnUiThread {
//                                            mBinding.boardRecyclerView.apply {
//                                                aqInfoData.add(
//                                                    SetAqInfo(key, value.get(i).toInt())
//                                                )
//                                            }
//
//                                            recycleAdapter.submitList(aqInfoData)
//                                            recycleAdapter.notifyDataSetChanged()
//                                        }
//                                    }
//                                }
//                                "WasteLiquor" -> {
//                                    for (i in value.indices) {
//                                        activity?.runOnUiThread {
//                                            mBinding.boardRecyclerView.apply {
//                                                aqInfoData.add(
//                                                    SetAqInfo(key, value.get(i).toInt())
//                                                )
//                                            }
//
//                                            recycleAdapter.submitList(aqInfoData)
//                                            recycleAdapter.notifyDataSetChanged()
//                                        }
//                                    }
//                                }
//                            }
//                        }


                    } catch (e: Exception) {
                    }
                }
                sendThread.start()
            }

            mBinding.saveBtn -> {
                //뷰 상태값 저장하고 데이터 aq세팅 데이터 넘기기

                for ((aqInfo, view) in aqInfo_ViewMap) {
                    when (aqInfo.model) {
                        "GasDock" -> {
                            view as GasBoardSettingView
                            val sensorType = view.selected_SensorType
                            val gasName = view.selected_GasType
                            val minCapa: Float = view.mCapaAlert_Et.text.toString().toFloat()
                            val maxCapa: Float = view.mMaxCapa_Et.text.toString().toFloat()
                            val gasIndex: Int? = null
                            viewmodel.GasStorageDataLiveList.add(
                                SetGasdockViewData(
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
                            view as GasBoardSettingView
                            //가스 컬러
                            val sensorType = view.selected_SensorType
                            val gasName = view.selected_GasType
                            val pressureMax: Float = view.mMaxCapa_Et.text.toString().toFloat()


                        }
                        "WasteLiquor" -> {
                            view as WasteLiquorBoardSettingView
                        }
                        "Oxygen" -> {
                            view as OxygenBoardSettingView
                            val minValue = view.mOxygen_minValue_et.text.toString().toInt()
                            val id = aqInfo.id
                            viewmodel.add(
                                SetOxygenViewData(
                                    id = id,
                                    setValue = 15,
                                    setMinValue = minValue
                                )
                            )

                        }
                        "Steamer" -> {
                            view as SteamerBoardSettingView
                        }
                    }

                }
                activity?.onFragmentChange(MainViewModel.MAINFRAGMENT)
            }
        }

    }

    private fun setAqInfoView() {
        for ((key, value) in viewmodel.modelMap) {
            //indices 배열을 인덱스 범위
            for (i in value.indices) {
                aqInfoData.add(SetAqInfo(key, value.get(i).toInt()))
            }
        }


        activity?.runOnUiThread {
//            mBinding.boardRecyclerView.apply {
//                aqInfoData
//            }
            recycleAdapter.submitList(aqInfoData)
            recycleAdapter.notifyDataSetChanged()
        }

        //기존 데이터 삭제
        for (i in aqInfoData) {
            aqInfo_ViewMap.remove(i)
        }
        aqSettingViewList.removeAll(aqSettingViewList)


        for (i in aqInfoData) {
            when (i.model) {
                "GasDock" -> {
                    val view = GasBoardSettingView(requireActivity())
                    aqSettingViewList.add(view)
                    aqInfo_ViewMap.put(i, view)
                }
                "GasRoom" -> {
                    val view = GasBoardSettingView(requireActivity())
                    aqSettingViewList.add(view)
                    aqInfo_ViewMap.put(i, view)
                }
                "WasteLiquor" -> {
                    val view = WasteLiquorBoardSettingView(requireContext())
                    aqSettingViewList.add(view)
                    aqInfo_ViewMap.put(i, view)

                }
                "Oxygen" -> {
                    val view = OxygenBoardSettingView(requireActivity())
//                        val view = getActivity()?.let { OxygenBoardSettingView(it) }

//                    listSetOxygenViewData.add(
//                        SetOxygenViewData(
//                            setMinValue = view.mOxygen_minValue_et.text.toString().toInt()
//                        )
//                    )
                    aqSettingViewList.add(view)
                    aqInfo_ViewMap.put(i, view)
                }
                "Steamer" -> {
                    val view = SteamerBoardSettingView(requireContext())
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
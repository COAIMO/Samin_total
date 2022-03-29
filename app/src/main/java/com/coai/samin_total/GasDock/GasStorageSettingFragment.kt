package com.coai.samin_total.GasDock

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.view.children
import androidx.core.view.get
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.coai.samin_total.*
import com.coai.samin_total.CustomView.AQInfoView
import com.coai.samin_total.CustomView.GasStorageBoardSettingView
import com.coai.samin_total.CustomView.SpaceDecoration
import com.coai.samin_total.databinding.FragmentGasStorageSettingBinding
import java.util.concurrent.ConcurrentHashMap

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [GasStorageSettingFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class GasStorageSettingFragment : Fragment() {
    // TODO: 2022-03-29 키보드 입력시 화면이 가려서 스크롤 가능하게 구현, 저장하여 데이터 뷰로 넘기는 부분만하면됨 
    private var param1: String? = null
    private var param2: String? = null
    private lateinit var mBinding: FragmentGasStorageSettingBinding
    private lateinit var sendThread: Thread
    var sending = false
    private lateinit var onBackPressed: OnBackPressedCallback
    private var activity: MainActivity? = null
    private val viewmodel by activityViewModels<MainViewModel>()

    //    private lateinit var recycleAdapter: AqSetting_RecycleAdapter
    private lateinit var recycleAdapter: GasStorageSetting_RecycleAdapter
    private lateinit var settingView: GasStorageBoardSettingView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        activity = getActivity() as MainActivity
        onBackPressed = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                activity!!.onFragmentChange(MainViewModel.GASDOCKMAINFRAGMENT)
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(this, onBackPressed)
    }

    override fun onDetach() {
        super.onDetach()
        activity = null
        onBackPressed.remove()
    }

    var selectedSensor = SetGasStorageViewData("0", 0, 0)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        mBinding = FragmentGasStorageSettingBinding.inflate(inflater, container, false)
        initRecycler()
        initView()
        setGasTypeSpinner()
        setSensorTypeSpinner()
//        changeView()
//        recycleAdapter.setItemClickListener(object : AqSetting_RecycleAdapter.OnItemClickListener {
//            override fun onClick(v: View, position: Int) {
//                for (i in mBinding.boardsettingContainer.children) {
//                    i.visibility = View.INVISIBLE
//                }
//                aqSettingViewList[position].visibility = View.VISIBLE
//            }
//        })

        recycleAdapter.setItemClickListener(object :
            GasStorageSetting_RecycleAdapter.OnItemClickListener {
            override fun onClick(v: View, position: Int) {
//                val view = (v as AQInfoView)
                selectedSensor = setGasSensorInfo[position]
                mBinding.gasStorageBoardSettingView.mSensorUsable_Sw.isChecked =
                    selectedSensor.usable

                mBinding.gasStorageBoardSettingView.mSensorType_Sp.setSelection(
                    viewmodel.sensorType.indexOf(
                        selectedSensor.sensorType
                    )
                )
                Log.d(
                    "테스트", "index:${
                        viewmodel.sensorType.indexOf(
                            selectedSensor.sensorType
                        )
                    } // 값 :${selectedSensor.sensorType}"
                )

                mBinding.gasStorageBoardSettingView.mGasType_Sp.setSelection(
                    viewmodel.gasType.indexOf(
                        selectedSensor.gasName
                    )
                )
                Log.d(
                    "테스트", "index:${
                        viewmodel.gasType.indexOf(
                            selectedSensor.gasName
                        )
                    } // 값 :${selectedSensor.gasName}"
                )
                mBinding.gasStorageBoardSettingView.setRadioButton(selectedSensor.ViewType)
                mBinding.gasStorageBoardSettingView.mCapaAlert_Et.setText(selectedSensor.pressure_Min.toString())
                mBinding.gasStorageBoardSettingView.mMaxCapa_Et.setText(selectedSensor.pressure_Max.toString())
                mBinding.gasStorageBoardSettingView.mRewardValue_Et.setText(selectedSensor.rewardValue.toString())
                mBinding.gasStorageBoardSettingView.mZeroPoint_Et.setText(selectedSensor.zeroPoint.toString())


            }
        })

        change()
        mBinding.saveBtn.setOnClickListener {
            setSaveData()
        }
        mBinding.gasStorageSettingLayout.setOnClickListener {
            hideKeyboard()
        }



        return mBinding.root
    }

    fun change() {
        mBinding.gasStorageBoardSettingView.mSensorUsable_Sw.setOnClickListener {
            selectedSensor.usable = mBinding.gasStorageBoardSettingView.mSensorUsable_Sw.isChecked
        }
        mBinding.gasStorageBoardSettingView.mViewType_Rg.setOnCheckedChangeListener { group, checkedId ->
//            when (checkedId) {
//                R.id.btn_single -> {
//                    selectedSensor.ViewType = 0
//                }
//                R.id.btn_dual -> {
//                    selectedSensor.ViewType = 1
//                }
//                R.id.btn_autoChanger -> {
//                    selectedSensor.ViewType = 2
//                }
//            }

            when (checkedId) {
                R.id.btn_single -> {
                    if (selectedSensor.port == 1) {
                        selectedSensor.ViewType = 0
                        for (i in setGasSensorInfo) {
                            if (i.port == 2) {
                                i.ViewType = 0
                            }
                        }
                    } else if (selectedSensor.port == 2) {
                        selectedSensor.ViewType = 0
                        for (i in setGasSensorInfo) {
                            if (i.port == 1) {
                                i.ViewType = 0
                            }
                        }
                    } else if (selectedSensor.port == 3) {
                        selectedSensor.ViewType = 0
                        for (i in setGasSensorInfo) {
                            if (i.port == 4) {
                                i.ViewType = 0
                            }
                        }
                    } else if (selectedSensor.port == 4) {
                        selectedSensor.ViewType = 0
                        for (i in setGasSensorInfo) {
                            if (i.port == 3) {
                                i.ViewType = 0
                            }
                        }
                    }
                }
                R.id.btn_dual -> {
                    if (selectedSensor.port == 1) {
                        selectedSensor.ViewType = 1
                        for (i in setGasSensorInfo) {
                            if (i.port == 2) {
                                i.ViewType = 1
                            }
                        }
                    } else if (selectedSensor.port == 2) {
                        selectedSensor.ViewType = 1
                        for (i in setGasSensorInfo) {
                            if (i.port == 1) {
                                i.ViewType = 1
                            }
                        }
                    } else if (selectedSensor.port == 3) {
                        selectedSensor.ViewType = 1
                        for (i in setGasSensorInfo) {
                            if (i.port == 4) {
                                i.ViewType = 1
                            }
                        }
                    } else if (selectedSensor.port == 4) {
                        selectedSensor.ViewType = 1
                        for (i in setGasSensorInfo) {
                            if (i.port == 3) {
                                i.ViewType = 1
                            }
                        }
                    }
                }
                R.id.btn_autoChanger -> {
                    if (selectedSensor.port == 1) {
                        selectedSensor.ViewType = 2
                        for (i in setGasSensorInfo) {
                            if (i.port == 2) {
                                i.ViewType = 2
                            }
                        }
                    } else if (selectedSensor.port == 2) {
                        selectedSensor.ViewType = 2
                        for (i in setGasSensorInfo) {
                            if (i.port == 1) {
                                i.ViewType = 2
                            }
                        }
                    } else if (selectedSensor.port == 3) {
                        selectedSensor.ViewType = 2
                        for (i in setGasSensorInfo) {
                            if (i.port == 4) {
                                i.ViewType = 2
                            }
                        }
                    } else if (selectedSensor.port == 4) {
                        selectedSensor.ViewType = 2
                        for (i in setGasSensorInfo) {
                            if (i.port == 3) {
                                i.ViewType = 2
                            }
                        }
                    }
                }
            }
        }

        mBinding.gasStorageBoardSettingView.mCapaAlert_Et.setOnClickListener {
//            selectedSensor.pressure_Min =
//                mBinding.gasStorageBoardSettingView.mCapaAlert_Et.text.toString().toFloat()

            if (selectedSensor.ViewType == 1 || selectedSensor.ViewType == 2) {

                selectedSensor.pressure_Min =
                    mBinding.gasStorageBoardSettingView.mCapaAlert_Et.text.toString().toFloat()

                if (selectedSensor.port == 1) {
                    for (i in setGasSensorInfo) {
                        if (i.port == 2) {
                            i.pressure_Min = selectedSensor.pressure_Min
                        }
                    }
                } else if (selectedSensor.port == 2) {
                    for (i in setGasSensorInfo) {
                        if (i.port == 1) {
                            i.pressure_Min = selectedSensor.pressure_Min
                        }
                    }
                } else if (selectedSensor.port == 3) {
                    for (i in setGasSensorInfo) {
                        if (i.port == 4) {
                            i.pressure_Min = selectedSensor.pressure_Min
                        }
                    }
                } else if (selectedSensor.port == 4) {
                    for (i in setGasSensorInfo) {
                        if (i.port == 3) {
                            i.pressure_Min = selectedSensor.pressure_Min
                        }
                    }
                }

            } else {
                selectedSensor.pressure_Min =
                    mBinding.gasStorageBoardSettingView.mCapaAlert_Et.text.toString().toFloat()
            }
        }
        mBinding.gasStorageBoardSettingView.mMaxCapa_Et.setOnClickListener {
//            selectedSensor.pressure_Max =
//                mBinding.gasStorageBoardSettingView.mMaxCapa_Et.text.toString().toFloat()

            if (selectedSensor.ViewType == 1 || selectedSensor.ViewType == 2) {

                selectedSensor.pressure_Max =
                    mBinding.gasStorageBoardSettingView.mMaxCapa_Et.text.toString().toFloat()

                if (selectedSensor.port == 1) {
                    for (i in setGasSensorInfo) {
                        if (i.port == 2) {
                            i.pressure_Max = selectedSensor.pressure_Max
                        }
                    }
                } else if (selectedSensor.port == 2) {
                    for (i in setGasSensorInfo) {
                        if (i.port == 1) {
                            i.pressure_Max = selectedSensor.pressure_Max
                        }
                    }
                } else if (selectedSensor.port == 3) {
                    for (i in setGasSensorInfo) {
                        if (i.port == 4) {
                            i.pressure_Max = selectedSensor.pressure_Max
                        }
                    }
                } else if (selectedSensor.port == 4) {
                    for (i in setGasSensorInfo) {
                        if (i.port == 3) {
                            i.pressure_Max = selectedSensor.pressure_Max
                        }
                    }
                }

            } else {
                selectedSensor.pressure_Max =
                    mBinding.gasStorageBoardSettingView.mMaxCapa_Et.text.toString().toFloat()
            }
        }
        mBinding.gasStorageBoardSettingView.mZeroPoint_Et.setOnClickListener {
//            selectedSensor.zeroPoint =
//                mBinding.gasStorageBoardSettingView.mZeroPoint_Et.text.toString().toFloat()

            if (selectedSensor.ViewType == 1 || selectedSensor.ViewType == 2) {

                selectedSensor.zeroPoint =
                    mBinding.gasStorageBoardSettingView.mZeroPoint_Et.text.toString().toFloat()

                if (selectedSensor.port == 1) {
                    for (i in setGasSensorInfo) {
                        if (i.port == 2) {
                            i.zeroPoint = selectedSensor.zeroPoint
                        }
                    }
                } else if (selectedSensor.port == 2) {
                    for (i in setGasSensorInfo) {
                        if (i.port == 1) {
                            i.zeroPoint = selectedSensor.zeroPoint
                        }
                    }
                } else if (selectedSensor.port == 3) {
                    for (i in setGasSensorInfo) {
                        if (i.port == 4) {
                            i.zeroPoint = selectedSensor.zeroPoint
                        }
                    }
                } else if (selectedSensor.port == 4) {
                    for (i in setGasSensorInfo) {
                        if (i.port == 3) {
                            i.zeroPoint = selectedSensor.zeroPoint
                        }
                    }
                }

            } else {
                selectedSensor.zeroPoint =
                    mBinding.gasStorageBoardSettingView.mZeroPoint_Et.text.toString().toFloat()
            }
        }
        mBinding.gasStorageBoardSettingView.mRewardValue_Et.setOnClickListener {
//            selectedSensor.rewardValue =
//                mBinding.gasStorageBoardSettingView.mRewardValue_Et.text.toString().toFloat()

            if (selectedSensor.ViewType == 1 || selectedSensor.ViewType == 2) {

                selectedSensor.rewardValue =
                    mBinding.gasStorageBoardSettingView.mRewardValue_Et.text.toString().toFloat()

                if (selectedSensor.port == 1) {
                    for (i in setGasSensorInfo) {
                        if (i.port == 2) {
                            i.rewardValue = selectedSensor.rewardValue
                        }
                    }
                } else if (selectedSensor.port == 2) {
                    for (i in setGasSensorInfo) {
                        if (i.port == 1) {
                            i.rewardValue = selectedSensor.rewardValue
                        }
                    }
                } else if (selectedSensor.port == 3) {
                    for (i in setGasSensorInfo) {
                        if (i.port == 4) {
                            i.rewardValue = selectedSensor.rewardValue
                        }
                    }
                } else if (selectedSensor.port == 4) {
                    for (i in setGasSensorInfo) {
                        if (i.port == 3) {
                            i.rewardValue = selectedSensor.rewardValue
                        }
                    }
                }

            } else {
                selectedSensor.rewardValue =
                    mBinding.gasStorageBoardSettingView.mRewardValue_Et.text.toString().toFloat()
            }
        }
    }

    private val sensorInfoData = mutableListOf<SetAqInfo>()
    val aqSettingViewList = mutableListOf<View>()
    val aqInfo_ViewMap = ConcurrentHashMap<SetAqInfo, View>()

    private val setGasSensorInfo = mutableListOf<SetGasStorageViewData>()

    private fun initView() {
//        Log.d("테스트", "시작")
//
//        for ((key, ids) in viewmodel.modelMap) {
//            //indices 배열을 인덱스 범위
//            if (key == "GasDock") {
//                for (id in ids.indices) {
//                    for (port in 1..4) {
//                        sensorInfoData.add(SetAqInfo(key, ids.get(id).toInt(), port))
//                    }
//                }
//            }
//        }
//        recycleAdapter.submitList(sensorInfoData)
//
//        for (i in sensorInfoData) {
//            val view = GasStorageBoardSettingView(
//                requireActivity()
//            )
//            aqSettingViewList.add(view)
//            aqInfo_ViewMap.put(i, view)
//        }
//
//        for (i in aqSettingViewList) {
//            i.visibility = View.INVISIBLE
//            mBinding.boardsettingContainer.addView(i)
//        }
//        Log.d("테스트", "끝")

        for ((key, ids) in viewmodel.modelMap) {
            //indices 배열을 인덱스 범위
            if (key == "GasDock") {
                for (id in ids.indices) {
                    for (port in 1..4) {
                        setGasSensorInfo.add(SetGasStorageViewData(key, ids.get(id).toInt(), port))
                    }
                }
            }
        }
        recycleAdapter.submitList(setGasSensorInfo)

//        for (i in setGasSensorInfo) {
//            val view = GasStorageBoardSettingView(
//                requireActivity()
//            )
//            aqSettingViewList.add(view)
//            aqInfo_ViewMap.put(i, view)
//        }
//
//        for (i in aqSettingViewList) {
//            i.visibility = View.INVISIBLE
//            mBinding.boardsettingContainer.addView(i)
//        }
//        Log.d("테스트", "끝")

    }

    private fun initRecycler() {
        mBinding.boardRecyclerView.apply {
            layoutManager =
                LinearLayoutManager(context)

            //아이템 높이 간격 조절
            val decoration = SpaceDecoration(20, 20)
            addItemDecoration(decoration)

//            recycleAdapter = AqSetting_RecycleAdapter()
            recycleAdapter = GasStorageSetting_RecycleAdapter()

            adapter = recycleAdapter
        }

    }

//    private fun changeView() {
//        for (i in mBinding.boardsettingContainer.children) {
//            i as GasStorageBoardSettingView
//            i.mViewType_Rg.setOnCheckedChangeListener { group, checkedId ->
//                Log.d("테스트", "group : $group // checked: $checkedId")
////                val findAqInfo = aqInfo_ViewMap.filterValues {
////                    it === i
////
////                }.keys
//                val key = getKey(aqInfo_ViewMap, i)
//                Log.d("테스트", "key : $key")
//                if (key != null) {
//                    val value1 = aqInfo_ViewMap.get(SetAqInfo(key.model, key.id, 1))
//                    val value2 = aqInfo_ViewMap.get(SetAqInfo(key.model, key.id, 2))
//                    val value3 = aqInfo_ViewMap.get(SetAqInfo(key.model, key.id, 3))
//                    val value4 = aqInfo_ViewMap.get(SetAqInfo(key.model, key.id, 4))
//
//                    when (checkedId) {
//                        R.id.btn_single -> {
//                            Log.d("테스트", "key : $key")
//                            if (key!!.port == 1 || key.port == 2) {
//                                (value1 as GasStorageBoardSettingView).btn_single.isChecked = true
//                                (value1 as GasStorageBoardSettingView).selected_ViewType = 0
//                                (value2 as GasStorageBoardSettingView).btn_single.isChecked = true
//                                (value2 as GasStorageBoardSettingView).selected_ViewType = 0
//                            } else if (key!!.port == 3 || key.port == 4) {
//                                (value3 as GasStorageBoardSettingView).btn_single.isChecked = true
//                                (value3 as GasStorageBoardSettingView).selected_ViewType = 0
//                                (value4 as GasStorageBoardSettingView).btn_single.isChecked = true
//                                (value4 as GasStorageBoardSettingView).selected_ViewType = 0
//
//                            }
//                        }
//                        R.id.btn_dual -> {
//                            Log.d("테스트", "key : $key")
//                            if (key!!.port == 1 || key.port == 2) {
//                                (value1 as GasStorageBoardSettingView).btn_dual.isChecked = true
//                                (value1 as GasStorageBoardSettingView).selected_ViewType = 1
//                                (value2 as GasStorageBoardSettingView).btn_dual.isChecked = true
//                                (value2 as GasStorageBoardSettingView).selected_ViewType = 1
//
//                            } else if (key!!.port == 3 || key.port == 4) {
//                                (value3 as GasStorageBoardSettingView).btn_dual.isChecked = true
//                                (value3 as GasStorageBoardSettingView).selected_ViewType = 1
//                                (value4 as GasStorageBoardSettingView).btn_dual.isChecked = true
//                                (value4 as GasStorageBoardSettingView).selected_ViewType = 1
//
//                            }
//
//                        }
//                        R.id.btn_autoChanger -> {
//                            Log.d("테스트", "key : $key")
//                            if (key!!.port == 1 || key.port == 2) {
//                                (value1 as GasStorageBoardSettingView).btn_autoChanger.isChecked =
//                                    true
//                                (value1 as GasStorageBoardSettingView).selected_ViewType = 2
//
//                                (value2 as GasStorageBoardSettingView).btn_autoChanger.isChecked =
//                                    true
//                                (value2 as GasStorageBoardSettingView).selected_ViewType = 2
//
//                            } else if (key!!.port == 3 || key.port == 4) {
//                                (value3 as GasStorageBoardSettingView).btn_autoChanger.isChecked =
//                                    true
//                                (value3 as GasStorageBoardSettingView).selected_ViewType = 2
//                                (value4 as GasStorageBoardSettingView).btn_autoChanger.isChecked =
//                                    true
//                                (value4 as GasStorageBoardSettingView).selected_ViewType = 2
//
//                            }
//
//                        }
//                        else -> {
//                        }
//                    }
//
//                }
//
//            }
//        }
//    }

    fun <K, V> getKey(map: Map<K, V>, value: V): K? {
        for (key in map.keys) {
            if (value == map[key]) {
                return key
            }
        }
        return null
    }

    fun setSaveData() {
//        for ((aqInfo, view) in aqInfo_ViewMap) {
//            (view as GasStorageBoardSettingView)
//            val sensorType = view.selected_SensorType
//            val gasName = view.selected_GasType
//            val minCapa: Float = view.mCapaAlert_Et.text.toString().toFloat()
//            val maxCapa: Float = view.mMaxCapa_Et.text.toString().toFloat()
//            val gasIndex: Int? = null
//            val id = aqInfo.id
//            val port = aqInfo.port
//            val viewType = view.selected_ViewType
//
//            if (viewType == 1 || viewType == 2) {
//                if (aqInfo.port == 2 || aqInfo.port == 4) {
//                    aqInfo_ViewMap.remove(aqInfo)
//
//                } else {
//                    viewmodel.GasStorageDataLiveList.add(
//                        SetGasStorageViewData(
//                            model = aqInfo.model,
//                            id = id,
//                            port = port,
//                            ViewType = viewType,
//                            gasName = gasName,
//                            gasColor = viewmodel.gasColorMap[gasName]!!,
//                            pressure_Min = minCapa,
//                            pressure_Max = maxCapa,
//                            gasIndex = gasIndex
//                        )
//                    )
//
//                }
//            } else {
//                viewmodel.GasStorageDataLiveList.add(
//                    SetGasStorageViewData(
//                        model = aqInfo.model,
//                        id = id,
//                        port = port,
//                        ViewType = viewType,
//                        gasName = gasName,
//                        gasColor = viewmodel.gasColorMap[gasName]!!,
//                        pressure_Min = minCapa,
//                        pressure_Max = maxCapa,
//                        gasIndex = gasIndex
//                    )
//                )
//
//            }
//
//        }
//        activity?.onFragmentChange(MainViewModel.GASDOCKMAINFRAGMENT)
//        activity?.callFeedback()

        for (i in setGasSensorInfo) {
            if (i.ViewType == 1 || i.ViewType == 2) {

            }
        }
        //위에꺼
        for ((aqInfo, view) in aqInfo_ViewMap) {
            (view as GasStorageBoardSettingView)
            val sensorType = view.selected_SensorType
            val gasName = view.selected_GasType
            val minCapa: Float = view.mCapaAlert_Et.text.toString().toFloat()
            val maxCapa: Float = view.mMaxCapa_Et.text.toString().toFloat()
            val gasIndex: Int? = null
            val id = aqInfo.id
            val port = aqInfo.port
            val viewType = view.selected_ViewType

            if (viewType == 1 || viewType == 2) {
                if (aqInfo.port == 2 || aqInfo.port == 4) {
                    aqInfo_ViewMap.remove(aqInfo)

                } else {
                    viewmodel.GasStorageDataLiveList.add(
                        SetGasStorageViewData(
                            model = aqInfo.model,
                            id = id,
                            port = port,
                            ViewType = viewType,
                            gasName = gasName,
                            gasColor = viewmodel.gasColorMap[gasName]!!,
                            pressure_Min = minCapa,
                            pressure_Max = maxCapa,
                            gasIndex = gasIndex
                        )
                    )

                }
            } else {
                viewmodel.GasStorageDataLiveList.add(
                    SetGasStorageViewData(
                        model = aqInfo.model,
                        id = id,
                        port = port,
                        ViewType = viewType,
                        gasName = gasName,
                        gasColor = viewmodel.gasColorMap[gasName]!!,
                        pressure_Min = minCapa,
                        pressure_Max = maxCapa,
                        gasIndex = gasIndex
                    )
                )

            }

        }
        activity?.onFragmentChange(MainViewModel.GASDOCKMAINFRAGMENT)
        activity?.callFeedback()
    }

    private fun setGasTypeSpinner() {
        val arrayAdapter = ArrayAdapter(
            requireContext(),
            R.layout.support_simple_spinner_dropdown_item,
            viewmodel.gasType
        )
        mBinding.gasStorageBoardSettingView.mGasType_Sp.adapter = arrayAdapter
        mBinding.gasStorageBoardSettingView.mGasType_Sp.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
//                    selectedSensor.gasName = viewmodel.gasType[position]
//                    selectedSensor.gasColor =
//                        viewmodel.gasColorMap[mBinding.gasStorageBoardSettingView.selected_GasType]

                    if (selectedSensor.ViewType == 1 || selectedSensor.ViewType == 2) {

                        selectedSensor.gasName = viewmodel.gasType[position]
                        selectedSensor.gasColor =
                            viewmodel.gasColorMap[mBinding.gasStorageBoardSettingView.selected_GasType]

                        if (selectedSensor.port == 1) {
                            for (i in setGasSensorInfo) {
                                if (i.port == 2) {
                                    i.gasName = selectedSensor.gasName
                                }
                            }
                        } else if (selectedSensor.port == 2) {
                            for (i in setGasSensorInfo) {
                                if (i.port == 1) {
                                    i.gasName = selectedSensor.gasName
                                }
                            }
                        } else if (selectedSensor.port == 3) {
                            for (i in setGasSensorInfo) {
                                if (i.port == 4) {
                                    i.gasName = selectedSensor.gasName
                                }
                            }
                        } else if (selectedSensor.port == 4) {
                            for (i in setGasSensorInfo) {
                                if (i.port == 3) {
                                    i.gasName = selectedSensor.gasName
                                }
                            }
                        }

                    } else {
                        selectedSensor.gasName = viewmodel.gasType[position]
                        selectedSensor.gasColor =
                            viewmodel.gasColorMap[mBinding.gasStorageBoardSettingView.selected_GasType]
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                    Toast.makeText(context, "센서 타입을 선택해주세요.", Toast.LENGTH_SHORT)
                        .show()
                }
            }
    }

    private fun setSensorTypeSpinner() {
        val arrayAdapter = ArrayAdapter(
            requireContext(),
            R.layout.support_simple_spinner_dropdown_item,
            viewmodel.sensorType
        )
        mBinding.gasStorageBoardSettingView.mSensorType_Sp.adapter = arrayAdapter
        mBinding.gasStorageBoardSettingView.mSensorType_Sp.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    selectedSensor.sensorType = viewmodel.sensorType[position]
                    Log.d(
                        "테스트",
                        "index:${viewmodel.gasType[position]} // 값 :${selectedSensor.sensorType}"
                    )

                    if (selectedSensor.ViewType == 1 || selectedSensor.ViewType == 2) {

                        selectedSensor.sensorType = viewmodel.sensorType[position]

                        if (selectedSensor.port == 1) {
                            for (i in setGasSensorInfo) {
                                if (i.port == 2) {
                                    i.sensorType = selectedSensor.sensorType
                                }
                            }
                        } else if (selectedSensor.port == 2) {
                            for (i in setGasSensorInfo) {
                                if (i.port == 1) {
                                    i.sensorType = selectedSensor.sensorType
                                }
                            }
                        } else if (selectedSensor.port == 3) {
                            for (i in setGasSensorInfo) {
                                if (i.port == 4) {
                                    i.sensorType = selectedSensor.sensorType
                                }
                            }
                        } else if (selectedSensor.port == 4) {
                            for (i in setGasSensorInfo) {
                                if (i.port == 3) {
                                    i.sensorType = selectedSensor.sensorType
                                }
                            }
                        }

                        change()
                        
                    } else {
                        selectedSensor.sensorType = viewmodel.sensorType[position]
                    }

                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                    Toast.makeText(context, "센서 타입을 선택해주세요.", Toast.LENGTH_SHORT)
                        .show()
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
         * @return A new instance of fragment GasStorageSettingFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            GasStorageSettingFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    private fun hideKeyboard() {
        if (getActivity() != null && requireActivity().currentFocus != null) {
            // 프래그먼트기 때문에 getActivity() 사용
            val inputManager: InputMethodManager =
                requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            inputManager.hideSoftInputFromWindow(
                requireActivity().currentFocus!!.windowToken,
                InputMethodManager.HIDE_NOT_ALWAYS
            )
        }
    }
}
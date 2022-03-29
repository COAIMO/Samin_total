package com.coai.samin_total.GasDock

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
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private lateinit var mBinding: FragmentGasStorageSettingBinding
    private lateinit var sendThread: Thread
    var sending = false
    private lateinit var onBackPressed: OnBackPressedCallback
    private var activity: MainActivity? = null
    private val viewmodel by activityViewModels<MainViewModel>()
    private lateinit var recycleAdapter: AqSetting_RecycleAdapter

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


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        mBinding = FragmentGasStorageSettingBinding.inflate(inflater, container, false)
        initRecycler()
        initView()
        changeView()
        recycleAdapter.setItemClickListener(object : AqSetting_RecycleAdapter.OnItemClickListener {
            override fun onClick(v: View, position: Int) {
                for (i in mBinding.boardsettingContainer.children) {
                    i.visibility = View.INVISIBLE
                }
                aqSettingViewList[position].visibility = View.VISIBLE
            }
        })

        mBinding.saveBtn.setOnClickListener {
            setSaveData()
        }


        return mBinding.root
    }

    private val sensorInfoData = mutableListOf<SetAqInfo>()
    val aqSettingViewList = mutableListOf<View>()
    val aqInfo_ViewMap = ConcurrentHashMap<SetAqInfo, View>()

    private fun initView() {
        Log.d("테스트", "시작")

        for ((key, ids) in viewmodel.modelMap) {
            //indices 배열을 인덱스 범위
            if (key == "GasDock") {
                for (id in ids.indices) {
                    for (port in 1..4) {
                        sensorInfoData.add(SetAqInfo(key, ids.get(id).toInt(), port))
                    }
                }
            }
        }
        recycleAdapter.submitList(sensorInfoData)

        for (i in sensorInfoData) {
            val view = GasStorageBoardSettingView(
                requireActivity()
            )
            aqSettingViewList.add(view)
            aqInfo_ViewMap.put(i, view)
        }

        for (i in aqSettingViewList) {
            i.visibility = View.INVISIBLE
            mBinding.boardsettingContainer.addView(i)
        }
        Log.d("테스트", "끝")

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

    private fun changeView() {
        for (i in mBinding.boardsettingContainer.children) {
            i as GasStorageBoardSettingView
            i.mViewType_Rg.setOnCheckedChangeListener { group, checkedId ->
                Log.d("테스트", "group : $group // checked: $checkedId")
//                val findAqInfo = aqInfo_ViewMap.filterValues {
//                    it === i
//
//                }.keys
                val key = getKey(aqInfo_ViewMap, i)
                Log.d("테스트", "key : $key")
                if (key != null) {
                    val value1 = aqInfo_ViewMap.get(SetAqInfo(key.model, key.id, 1))
                    val value2 = aqInfo_ViewMap.get(SetAqInfo(key.model, key.id, 2))
                    val value3 = aqInfo_ViewMap.get(SetAqInfo(key.model, key.id, 3))
                    val value4 = aqInfo_ViewMap.get(SetAqInfo(key.model, key.id, 4))

                    when (checkedId) {
                        R.id.btn_single -> {
                            Log.d("테스트", "key : $key")
                            if (key!!.port == 1 || key.port == 2) {
                                (value1 as GasStorageBoardSettingView).btn_single.isChecked = true
                                (value1 as GasStorageBoardSettingView).selected_ViewType = 0
                                (value2 as GasStorageBoardSettingView).btn_single.isChecked = true
                                (value2 as GasStorageBoardSettingView).selected_ViewType = 0
                            } else if (key!!.port == 3 || key.port == 4) {
                                (value3 as GasStorageBoardSettingView).btn_single.isChecked = true
                                (value3 as GasStorageBoardSettingView).selected_ViewType = 0
                                (value4 as GasStorageBoardSettingView).btn_single.isChecked = true
                                (value4 as GasStorageBoardSettingView).selected_ViewType = 0

                            }
                        }
                        R.id.btn_dual -> {
                            Log.d("테스트", "key : $key")
                            if (key!!.port == 1 || key.port == 2) {
                                (value1 as GasStorageBoardSettingView).btn_dual.isChecked = true
                                (value1 as GasStorageBoardSettingView).selected_ViewType = 1
                                (value2 as GasStorageBoardSettingView).btn_dual.isChecked = true
                                (value2 as GasStorageBoardSettingView).selected_ViewType = 1

                            } else if (key!!.port == 3 || key.port == 4) {
                                (value3 as GasStorageBoardSettingView).btn_dual.isChecked = true
                                (value3 as GasStorageBoardSettingView).selected_ViewType = 1
                                (value4 as GasStorageBoardSettingView).btn_dual.isChecked = true
                                (value4 as GasStorageBoardSettingView).selected_ViewType = 1

                            }

                        }
                        R.id.btn_autoChanger -> {
                            Log.d("테스트", "key : $key")
                            if (key!!.port == 1 || key.port == 2) {
                                (value1 as GasStorageBoardSettingView).btn_autoChanger.isChecked =
                                    true
                                (value1 as GasStorageBoardSettingView).selected_ViewType = 2

                                (value2 as GasStorageBoardSettingView).btn_autoChanger.isChecked =
                                    true
                                (value2 as GasStorageBoardSettingView).selected_ViewType = 2

                            } else if (key!!.port == 3 || key.port == 4) {
                                (value3 as GasStorageBoardSettingView).btn_autoChanger.isChecked =
                                    true
                                (value3 as GasStorageBoardSettingView).selected_ViewType = 2
                                (value4 as GasStorageBoardSettingView).btn_autoChanger.isChecked =
                                    true
                                (value4 as GasStorageBoardSettingView).selected_ViewType = 2

                            }

                        }
                        else -> {
                        }
                    }

                }

            }
        }
    }

    fun <K, V> getKey(map: Map<K, V>, value: V): K? {
        for (key in map.keys) {
            if (value == map[key]) {
                return key
            }
        }
        return null
    }

    fun setSaveData() {
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

                }else{
                    viewmodel.GasStorageDataLiveList.add(
                        SetGasStorageViewData(
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
            }else{
                viewmodel.GasStorageDataLiveList.add(
                    SetGasStorageViewData(
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
}
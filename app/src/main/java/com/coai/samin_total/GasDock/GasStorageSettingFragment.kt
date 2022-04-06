package com.coai.samin_total.GasDock

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
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
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.coai.samin_total.*
import com.coai.samin_total.CustomView.SpaceDecoration
import com.coai.samin_total.Logic.SaminSharedPreference
import com.coai.samin_total.databinding.FragmentGasStorageSettingBinding
import org.json.JSONArray

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
    // TODO: 2022-03-29 키보드 입력시 화면이 가려서 스크롤 가능하게 구현
    private var param1: String? = null
    private var param2: String? = null
    private lateinit var mBinding: FragmentGasStorageSettingBinding
    private lateinit var onBackPressed: OnBackPressedCallback
    private var activity: MainActivity? = null
    private val viewmodel by activityViewModels<MainViewModel>()
    private lateinit var recycleAdapter: GasStorageSetting_RecycleAdapter
    private val setGasSensorInfo = mutableListOf<SetGasStorageViewData>()
    var selectedSensor = SetGasStorageViewData("0", 0, 0)


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
        mBinding = FragmentGasStorageSettingBinding.inflate(inflater, container, false)
        initRecycler()
        initView()
        setGasTypeSpinner()
        setSensorTypeSpinner()
        changeView()

        recycleAdapter.setItemClickListener(object :
            GasStorageSetting_RecycleAdapter.OnItemClickListener {
            override fun onClick(v: View, position: Int) {
//                val view = (v as AQInfoView)
                selectedSensor = setGasSensorInfo[position]
                mBinding.gasStorageBoardSettingView.mSensorUsable_Sw.isChecked =
                    selectedSensor.usable

                mBinding.gasStorageBoardSettingView.mSensorType_Sp.setSelection(
                    viewmodel.gasSensorType.indexOf(
                        selectedSensor.sensorType
                    )
                )

                selectedSensor.pressure_Max =
                    viewmodel.maxPressureMap[selectedSensor.sensorType]


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
//                mBinding.gasStorageBoardSettingView.mMaxCapa_Et.setText(selectedSensor.pressure_Max.toString())
                mBinding.gasStorageBoardSettingView.mRewardValue_Et.setText(selectedSensor.rewardValue.toString())
                mBinding.gasStorageBoardSettingView.mZeroPoint_Et.setText(selectedSensor.zeroPoint.toString())


            }
        })

        mBinding.saveBtn.setOnClickListener {
            setSaveData()
        }
        mBinding.gasStorageSettingLayout.setOnClickListener {
            hideKeyboard()
        }
        mBinding.btnBack.setOnClickListener {
            activity?.onFragmentChange(MainViewModel.GASDOCKMAINFRAGMENT)
        }

        return mBinding.root
    }

    private val mCapaAlerttextWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            if (s != null && !s.toString().equals("")) {
                selectedSensor.pressure_Min = s.toString().toFloat()
            }

        }

        override fun afterTextChanged(s: Editable?) {
        }

    }
    private val mMaxCapatextWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            if (s != null && !s.toString().equals("")) {
                selectedSensor.pressure_Max = s.toString().toFloat()
            }

        }

        override fun afterTextChanged(s: Editable?) {
        }

    }
    private val mZeroPointtextWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            if (s != null && !s.toString().equals("")) {
                selectedSensor.zeroPoint = s.toString().toFloat()
            }

        }

        override fun afterTextChanged(s: Editable?) {
        }

    }
    private val mRewardValuetextWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            if (s != null && !s.toString().equals("")) {
                selectedSensor.rewardValue = s.toString().toFloat()
            }

        }

        override fun afterTextChanged(s: Editable?) {
        }

    }

    fun changeView() {
        mBinding.gasStorageBoardSettingView.mSensorUsable_Sw.setOnClickListener {
//            selectedSensor.usable = mBinding.gasStorageBoardSettingView.mSensorUsable_Sw.isChecked
            if (selectedSensor.ViewType == 1 || selectedSensor.ViewType == 2) {
                selectedSensor.usable =
                    mBinding.gasStorageBoardSettingView.mSensorUsable_Sw.isChecked

                if (selectedSensor.port == 1) {
                    for (i in setGasSensorInfo) {
                        if (i.port == 2) {
                            i.usable = selectedSensor.usable
                        }
                    }
                } else if (selectedSensor.port == 2) {
                    for (i in setGasSensorInfo) {
                        if (i.port == 1) {
                            i.usable = selectedSensor.usable
                        }
                    }
                } else if (selectedSensor.port == 3) {
                    for (i in setGasSensorInfo) {
                        if (i.port == 4) {
                            i.usable = selectedSensor.usable
                        }
                    }
                } else if (selectedSensor.port == 4) {
                    for (i in setGasSensorInfo) {
                        if (i.port == 3) {
                            i.usable = selectedSensor.usable
                        }
                    }
                }

            } else {
                selectedSensor.usable =
                    mBinding.gasStorageBoardSettingView.mSensorUsable_Sw.isChecked
            }
        }
        mBinding.gasStorageBoardSettingView.mViewType_Rg.setOnCheckedChangeListener { group, checkedId ->
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
        mBinding.gasStorageBoardSettingView.mCapaAlert_Et.addTextChangedListener(
            mCapaAlerttextWatcher
        )
        mBinding.gasStorageBoardSettingView.mMaxCapa_Et.addTextChangedListener(mMaxCapatextWatcher)
        mBinding.gasStorageBoardSettingView.mZeroPoint_Et.addTextChangedListener(
            mZeroPointtextWatcher
        )
        mBinding.gasStorageBoardSettingView.mRewardValue_Et.addTextChangedListener(
            mRewardValuetextWatcher
        )
    }


    private fun initView() {
        setGasSensorInfo.removeAll(setGasSensorInfo)
        viewmodel.GasStorageDataLiveList.clear(true)
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

    fun <K, V> getKey(map: Map<K, V>, value: V): K? {
        for (key in map.keys) {
            if (value == map[key]) {
                return key
            }
        }
        return null
    }

    private fun setSaveData() {
        viewmodel.GasStorageDataLiveList.clear(true)
        val iter = setGasSensorInfo.iterator()
        while (iter.hasNext()) {
            iter.forEach {
                if (it.usable) {
                    if (it.ViewType == 1 || it.ViewType == 2) {
                        if (it.port == 2 || it.port == 4) {
//                        Log.d("테스트", "it 2,4: $it")
                            iter.remove()
                        } else {
//                        Log.d("테스트", "iter 1,3: $it")
                            viewmodel.GasStorageDataLiveList.add(it)
                        }
                    } else {
                        viewmodel.GasStorageDataLiveList.add(it)
                    }

                } else {
                    iter.remove()
                }

            }
        }
        for (i in viewmodel.GasStorageDataLiveList.value!!) {
            Log.d("테스트", "viewmodel: $i")
        }
        //위에꺼
////        if (!activity?.isSending!!) {
////            activity?.callFeedback()
////            activity?.isSending = true
////        }
        val jsonArray = JSONArray()
        for (i in viewmodel.GasStorageDataLiveList.value!!){
            jsonArray.put(i)
        }
        val result = jsonArray.toString()
        val sharedPref = SaminSharedPreference(requireContext())
        sharedPref.saveBoardSetData("GasStorage",result)
        activity?.onFragmentChange(MainViewModel.GASDOCKMAINFRAGMENT)
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
//                        viewmodel.gasNameMap[viewmodel.gasType[position]]
                        selectedSensor.gasName = viewmodel.gasType[position]
                        selectedSensor.gasColor =
                            viewmodel.gasColorMap[selectedSensor.gasName]

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
                            viewmodel.gasColorMap[selectedSensor.gasName]
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
            viewmodel.gasSensorType
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
                    selectedSensor.sensorType = viewmodel.gasSensorType[position]
                    mBinding.gasStorageBoardSettingView.mMaxCapa_Et.setText(viewmodel.maxPressureMap[selectedSensor.sensorType].toString())
                    selectedSensor.pressure_Max =
                        viewmodel.maxPressureMap[selectedSensor.sensorType]
                    Log.d(
                        "테스트",
                        "index:${viewmodel.gasType[position]} // 값 :${selectedSensor.sensorType}"
                    )

                    if (selectedSensor.ViewType == 1 || selectedSensor.ViewType == 2) {

                        selectedSensor.sensorType = viewmodel.gasSensorType[position]
                        mBinding.gasStorageBoardSettingView.mMaxCapa_Et.setText(viewmodel.maxPressureMap[selectedSensor.sensorType].toString())
                        selectedSensor.pressure_Max =
                            viewmodel.maxPressureMap[selectedSensor.sensorType]

                        if (selectedSensor.port == 1) {
                            for (i in setGasSensorInfo) {
                                if (i.port == 2) {
                                    i.sensorType = selectedSensor.sensorType
                                    i.pressure_Max =
                                        viewmodel.maxPressureMap[selectedSensor.sensorType]
                                    mBinding.gasStorageBoardSettingView.mMaxCapa_Et.setText(
                                        viewmodel.maxPressureMap[selectedSensor.sensorType].toString()
                                    )

                                }
                            }
                        } else if (selectedSensor.port == 2) {
                            for (i in setGasSensorInfo) {
                                if (i.port == 1) {
                                    i.sensorType = selectedSensor.sensorType
                                    i.pressure_Max =
                                        viewmodel.maxPressureMap[selectedSensor.sensorType]
                                    mBinding.gasStorageBoardSettingView.mMaxCapa_Et.setText(
                                        viewmodel.maxPressureMap[selectedSensor.sensorType].toString()
                                    )

                                }
                            }
                        } else if (selectedSensor.port == 3) {
                            for (i in setGasSensorInfo) {
                                if (i.port == 4) {
                                    i.sensorType = selectedSensor.sensorType
                                    i.pressure_Max =
                                        viewmodel.maxPressureMap[selectedSensor.sensorType]
                                    mBinding.gasStorageBoardSettingView.mMaxCapa_Et.setText(
                                        viewmodel.maxPressureMap[selectedSensor.sensorType].toString()
                                    )

                                }
                            }
                        } else if (selectedSensor.port == 4) {
                            for (i in setGasSensorInfo) {
                                if (i.port == 3) {
                                    i.sensorType = selectedSensor.sensorType
                                    i.pressure_Max =
                                        viewmodel.maxPressureMap[selectedSensor.sensorType]
                                    mBinding.gasStorageBoardSettingView.mMaxCapa_Et.setText(
                                        viewmodel.maxPressureMap[selectedSensor.sensorType].toString()
                                    )

                                }
                            }
                        }

//                        change()

                    } else {
                        selectedSensor.sensorType = viewmodel.gasSensorType[position]
                        selectedSensor.pressure_Max =
                            viewmodel.maxPressureMap[selectedSensor.sensorType]
                        mBinding.gasStorageBoardSettingView.mMaxCapa_Et.setText(viewmodel.maxPressureMap[selectedSensor.sensorType].toString())

                    }
                    changeView()

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
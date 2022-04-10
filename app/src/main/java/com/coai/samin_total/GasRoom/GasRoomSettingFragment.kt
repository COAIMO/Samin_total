package com.coai.samin_total.GasRoom

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
import com.coai.samin_total.CustomView.SpaceDecoration
import com.coai.samin_total.GasDock.SetGasStorageViewData
import com.coai.samin_total.Logic.SaminSharedPreference
import com.coai.samin_total.MainActivity
import com.coai.samin_total.MainViewModel
import com.coai.samin_total.R
import com.coai.samin_total.databinding.FragmentGasRoomSettingBinding

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [GasRoomSettingFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class GasRoomSettingFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private lateinit var mBinding: FragmentGasRoomSettingBinding
    private lateinit var onBackPressed: OnBackPressedCallback
    private var activity: MainActivity? = null
    private val viewmodel by activityViewModels<MainViewModel>()
    private lateinit var recycleAdapter: GasRoomSetting_RecycleAdapter
    private val setGasSensorInfo = mutableListOf<SetGasRoomViewData>()
    var selectedSensor = SetGasRoomViewData("adsfsd", 0, 0)
    lateinit var shared: SaminSharedPreference

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
    private val mSlopeValuetextWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            if (s != null && !s.toString().equals("")) {
                selectedSensor.slopeValue = s.toString().toFloat()
            }

        }

        override fun afterTextChanged(s: Editable?) {
        }

    }

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
        mBinding = FragmentGasRoomSettingBinding.inflate(inflater, container, false)
        shared = SaminSharedPreference(requireContext())
        initRecycler()
        initView()
        setGasTypeSpinner()
        setSensorTypeSpinner()

        recycleAdapter.setItemClickListener(object :
            GasRoomSetting_RecycleAdapter.OnItemClickListener {
            override fun onClick(v: View, position: Int) {
                selectedSensor = setGasSensorInfo[position]
                mBinding.gasRoomBoardSettingView.mSensorUsable_Sw.isChecked =
                    selectedSensor.usable

                mBinding.gasRoomBoardSettingView.mSensorType_Sp.setSelection(
                    viewmodel.gasSensorType.indexOf(
                        selectedSensor.sensorType
                    )
                )
                mBinding.gasRoomBoardSettingView.mGasType_Sp.setSelection(
                    viewmodel.gasType.indexOf(
                        selectedSensor.gasName
                    )
                )
                mBinding.gasRoomBoardSettingView.mMaxCapa_Et.setText(selectedSensor.pressure_Max.toString())
                mBinding.gasRoomBoardSettingView.mRewardValue_Et.setText(selectedSensor.rewardValue.toString())
                mBinding.gasRoomBoardSettingView.mZeroPoint_Et.setText(selectedSensor.zeroPoint.toString())
                mBinding.gasRoomBoardSettingView.mSlopeValue_Et.setText(selectedSensor.slopeValue.toString())
            }
        })
        return mBinding.root
    }

    private fun initView() {
        setGasSensorInfo.removeAll(setGasSensorInfo)
        for ((key, ids) in viewmodel.modelMap) {
            //indices 배열을 인덱스 범위
            if (key == "GasRoom") {
                for (id in ids.indices) {
                    for (port in 1..4) {
                        setGasSensorInfo.add(SetGasRoomViewData(key, ids.get(id).toInt(), port))
                    }
                }
            }
        }
        //이전 저장된 설정 데이터 불러와서 적용.
        val exData =shared.loadBoardSetData(SaminSharedPreference.GASROOM) as MutableList<SetGasRoomViewData>
        if (exData.isNotEmpty()){
            for ((index, value) in exData.withIndex()){
                setGasSensorInfo.set(index, value)
            }
        }
        recycleAdapter.submitList(setGasSensorInfo)

        mBinding.gasRoomBoardSettingView.mSensorUsable_Sw.setOnClickListener {
            selectedSensor.usable = mBinding.gasRoomBoardSettingView.mSensorUsable_Sw.isChecked
        }
        mBinding.gasRoomBoardSettingView.mMaxCapa_Et.addTextChangedListener(mMaxCapatextWatcher)
        mBinding.gasRoomBoardSettingView.mZeroPoint_Et.addTextChangedListener(
            mZeroPointtextWatcher
        )
        mBinding.gasRoomBoardSettingView.mRewardValue_Et.addTextChangedListener(
            mRewardValuetextWatcher
        )
        mBinding.gasRoomBoardSettingView.mSlopeValue_Et.addTextChangedListener(
            mSlopeValuetextWatcher
        )
        mBinding.btnBack.setOnClickListener {
            activity?.onFragmentChange(MainViewModel.GASROOMMAINFRAGMENT)
        }
        mBinding.saveBtn.setOnClickListener {
            setSaveData()
        }
        mBinding.gasRoomSettingLayout.setOnClickListener {
            hideKeyboard()
        }
    }

    private fun initRecycler() {
        mBinding.boardRecyclerView.apply {
            layoutManager =
                LinearLayoutManager(context)

            //아이템 높이 간격 조절
            val decoration = SpaceDecoration(20, 20)
            addItemDecoration(decoration)

            recycleAdapter = GasRoomSetting_RecycleAdapter()

            adapter = recycleAdapter
        }

    }

    private fun setGasTypeSpinner() {
        val arrayAdapter = ArrayAdapter(
            requireContext(),
            R.layout.support_simple_spinner_dropdown_item,
            viewmodel.gasType
        )
        mBinding.gasRoomBoardSettingView.mGasType_Sp.adapter = arrayAdapter
        mBinding.gasRoomBoardSettingView.mGasType_Sp.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    selectedSensor.gasName = viewmodel.gasType[position]
                    selectedSensor.gasColor =
                        viewmodel.gasColorMap[selectedSensor.gasName]!!
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
        mBinding.gasRoomBoardSettingView.mSensorType_Sp.adapter = arrayAdapter
        mBinding.gasRoomBoardSettingView.mSensorType_Sp.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    selectedSensor.sensorType = viewmodel.gasSensorType[position]
                    mBinding.gasRoomBoardSettingView.mMaxCapa_Et.setText(viewmodel.maxPressureMap[selectedSensor.sensorType].toString())
                    selectedSensor.pressure_Max =
                        viewmodel.maxPressureMap[selectedSensor.sensorType]!!
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                    Toast.makeText(context, "센서 타입을 선택해주세요.", Toast.LENGTH_SHORT)
                        .show()
                }
            }
    }

    private fun setSaveData() {
        viewmodel.GasRoomDataLiveList.clear(true)
        val iter = setGasSensorInfo.iterator()
        while (iter.hasNext()) {
            iter.forEach {
                if (it.usable) {
                    viewmodel.GasRoomDataLiveList.add(it)
                } else iter.remove()
            }
        }
        //설정값 저장
        val buff = mutableListOf<SetGasRoomViewData>()
        for (i in viewmodel.GasRoomDataLiveList.value!!) {
            Log.d("테스트", "viewmodel: $i")
            buff.add(i)
        }
        shared.saveBoardSetData(SaminSharedPreference.GASROOM, buff)
        activity?.onFragmentChange(MainViewModel.GASROOMMAINFRAGMENT)
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment GasRoomSettingFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            GasRoomSettingFragment().apply {
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
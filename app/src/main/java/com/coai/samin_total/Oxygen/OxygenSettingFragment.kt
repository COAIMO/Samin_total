package com.coai.samin_total.Oxygen

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
import com.coai.samin_total.GasRoom.SetGasRoomViewData
import com.coai.samin_total.Logic.SaminSharedPreference
import com.coai.samin_total.MainActivity
import com.coai.samin_total.MainViewModel
import com.coai.samin_total.R
import com.coai.samin_total.databinding.FragmentOxygenSettingBinding

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [OxygenSettingFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class OxygenSettingFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private lateinit var mBinding: FragmentOxygenSettingBinding
    private lateinit var onBackPressed: OnBackPressedCallback
    private var activity: MainActivity? = null
    private val viewmodel by activityViewModels<MainViewModel>()
    private lateinit var recycleAdapter: OxygenSetting_RecyclerAdapter
    private val setOxygenInfo = mutableListOf<SetOxygenViewData>()
    var selectedSensor = SetOxygenViewData("adsfsd", 0, 0)
    lateinit var shared: SaminSharedPreference
    private val mOxygen_minValueWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            if (s != null && !s.toString().equals("")) {
                selectedSensor.setMinValue = s.toString().toInt()
            }
        }
        override fun afterTextChanged(s: Editable?) {
        }
    }
    private val mOxygen_maxValueWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            if (s != null && !s.toString().equals("")) {
                selectedSensor.setMaxValue = s.toString().toInt()
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
                activity!!.onFragmentChange(MainViewModel.OXYGENMAINFRAGMENT)
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
        mBinding = FragmentOxygenSettingBinding.inflate(inflater, container, false)
        shared = SaminSharedPreference(requireContext())
        initRecycler()
        initView()
        setSensorTypeSpinner()

        recycleAdapter.setItemClickListener(object :
            OxygenSetting_RecyclerAdapter.OnItemClickListener {
            override fun onClick(v: View, position: Int) {
                selectedSensor = setOxygenInfo[position]
                mBinding.oxygenBoardSettingView.mSensorUsable_Sw.isChecked =
                    selectedSensor.usable

                mBinding.oxygenBoardSettingView.mSensorType_Sp.setSelection(
                    viewmodel.oxygenSensorType.indexOf(
                        selectedSensor.sensorType
                    )
                )
                mBinding.oxygenBoardSettingView.mOxygen_minValue_et.setText(selectedSensor.setMinValue.toString())
                mBinding.oxygenBoardSettingView.mOxygen_maxValue_et.setText(selectedSensor.setMaxValue.toString())
            }
        })

        return mBinding.root
    }

    private fun initView() {
        setOxygenInfo.removeAll(setOxygenInfo)
        for ((key, ids) in viewmodel.modelMap) {
            //indices 배열을 인덱스 범위
            if (key == "Oxygen") {
                for (id in ids.indices) {
                    setOxygenInfo.add(SetOxygenViewData(key, ids.get(id).toInt(), 1))
                }
            }
        }
        //이전 저장된 설정 데이터 불러와서 적용.
        val exData =shared.loadBoardSetData(SaminSharedPreference.OXYGEN) as MutableList<SetOxygenViewData>
        if (exData.isNotEmpty()){
            for ((index, value) in exData.withIndex()){
                setOxygenInfo.set(index, value)
            }
        }
        recycleAdapter.submitList(setOxygenInfo)

        mBinding.oxygenBoardSettingView.mSensorUsable_Sw.setOnClickListener {
            selectedSensor.usable = mBinding.oxygenBoardSettingView.mSensorUsable_Sw.isChecked
        }
        mBinding.oxygenBoardSettingView.mOxygen_minValue_et.addTextChangedListener(mOxygen_minValueWatcher)
        mBinding.oxygenBoardSettingView.mOxygen_maxValue_et.addTextChangedListener(mOxygen_maxValueWatcher)
        mBinding.btnBack.setOnClickListener {
            activity?.onFragmentChange(MainViewModel.OXYGENMAINFRAGMENT)
        }
        mBinding.saveBtn.setOnClickListener {
            setSaveData()
        }
        mBinding.oxygenSettingLayout.setOnClickListener {
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

            recycleAdapter = OxygenSetting_RecyclerAdapter()

            adapter = recycleAdapter
        }

    }
    private fun setSensorTypeSpinner() {
        val arrayAdapter = ArrayAdapter(
            requireContext(),
            R.layout.support_simple_spinner_dropdown_item,
            viewmodel.oxygenSensorType
        )
        mBinding.oxygenBoardSettingView.mSensorType_Sp.adapter = arrayAdapter
        mBinding.oxygenBoardSettingView.mSensorType_Sp.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    selectedSensor.sensorType = viewmodel.oxygenSensorType[position]
                }
                override fun onNothingSelected(parent: AdapterView<*>?) {
                    Toast.makeText(context, "센서 타입을 선택해주세요.", Toast.LENGTH_SHORT)
                        .show()
                }
            }
    }

    private fun setSaveData() {
        viewmodel.OxygenDataLiveList.clear(true)
        val iter = setOxygenInfo.iterator()
        while (iter.hasNext()) {
            iter.forEach {
//                if (it.usable){
                    viewmodel.OxygenDataLiveList.add(it)
//                }else iter.remove()
            }
        }
        //설정값 저장
        val buff = mutableListOf<SetOxygenViewData>()
        for (i in viewmodel.OxygenDataLiveList.value!!) {
            buff.add(i)
        }
        shared.saveBoardSetData(SaminSharedPreference.OXYGEN, buff)
        activity?.tmp?.LoadSetting()
        activity?.onFragmentChange(MainViewModel.OXYGENMAINFRAGMENT)
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment OxygenSettingFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            OxygenSettingFragment().apply {
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
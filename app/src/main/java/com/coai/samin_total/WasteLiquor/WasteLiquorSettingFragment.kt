package com.coai.samin_total.WasteLiquor

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
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.coai.samin_total.CustomView.SpaceDecoration
import com.coai.samin_total.GasDock.SetGasStorageViewData
import com.coai.samin_total.Logic.SaminSharedPreference
import com.coai.samin_total.MainActivity
import com.coai.samin_total.MainViewModel
import com.coai.samin_total.Service.HexDump
import com.coai.samin_total.Steamer.SetSteamerViewData
import com.coai.samin_total.databinding.FragmentWasteLiquorSettingBinding

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [WasteWaterSettingFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class WasteWaterSettingFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private lateinit var mBinding: FragmentWasteLiquorSettingBinding
    private lateinit var onBackPressed: OnBackPressedCallback
    private var activity: MainActivity? = null
    private val viewmodel by activityViewModels<MainViewModel>()
    private lateinit var recycleAdapter: WasteLiquorSetting_RecyclerAdapter
    private val setWasteLiquorInfo = mutableListOf<SetWasteLiquorViewData>()
    var selectedSensor: SetWasteLiquorViewData? = null
    lateinit var shared: SaminSharedPreference
    private val mWasteNameWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            if (s != null && !s.toString().equals("")) {
                if (selectedSensor == null) return
                selectedSensor?.liquidName = s.toString()
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
                activity!!.onFragmentChange(MainViewModel.WASTELIQUORMAINFRAGMENT)
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(this, onBackPressed)
    }

    override fun onDetach() {
        super.onDetach()
        activity = null
        onBackPressed.remove()
        selectedSensor = null
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mBinding = FragmentWasteLiquorSettingBinding.inflate(inflater, container, false)
        shared = SaminSharedPreference(requireContext())
        initRecycler()
        initView()
//        setLevelSensorTypeSpinner()

        recycleAdapter.setItemClickListener(object :
            WasteLiquorSetting_RecyclerAdapter.OnItemClickListener {
            override fun onClick(v: View, position: Int) {
                selectedSensor = setWasteLiquorInfo[position]
                mBinding.wasteLiquorBoardSettingView.mSensorUsable_Sw.isChecked =
                    selectedSensor!!.usable

                mBinding.wasteLiquorBoardSettingView.mSensorType_Sp.setSelection(
                    viewmodel.tempSensorType.indexOf(
                        selectedSensor?.level_SensorType
                    )
                )
                mBinding.wasteLiquorBoardSettingView.mWasteName_Et.setText(selectedSensor?.liquidName)
            }
        })


        return mBinding.root
    }

    private fun initRecycler() {
        mBinding.boardRecyclerView.apply {
            layoutManager =
                LinearLayoutManager(context)

            //아이템 높이 간격 조절
            val decoration = SpaceDecoration(20, 20)
            addItemDecoration(decoration)

            recycleAdapter = WasteLiquorSetting_RecyclerAdapter()

            adapter = recycleAdapter
        }

    }

    private fun initView() {
        setWasteLiquorInfo.removeAll(setWasteLiquorInfo)
        for ((key, ids) in viewmodel.modelMap) {
            //indices 배열을 인덱스 범위
            if (key == "WasteLiquor") {
                for (id in ids.indices) {
                    for (port in 1..4) {
                        setWasteLiquorInfo.add(
                            SetWasteLiquorViewData(
                                key,
                                ids.get(id).toInt(),
                                port
                            )
                        )
                    }
                }
            }
        }
        //이전 저장된 설정 데이터 불러와서 적용.
        val exData =
            shared.loadBoardSetData(SaminSharedPreference.WASTELIQUOR) as MutableList<SetWasteLiquorViewData>
        if (exData.isNotEmpty()) {
            for ((index, value) in exData.withIndex()) {
                setWasteLiquorInfo.set(index, value)
            }
        }
        recycleAdapter.submitList(setWasteLiquorInfo)

        mBinding.wasteLiquorBoardSettingView.mSensorUsable_Sw.setOnClickListener {
            selectedSensor?.usable = mBinding.wasteLiquorBoardSettingView.mSensorUsable_Sw.isChecked
        }
        mBinding.wasteLiquorBoardSettingView.mWasteName_Et.addTextChangedListener(
            mWasteNameWatcher
        )
        mBinding.btnBack.setOnClickListener {
            activity?.onFragmentChange(MainViewModel.WASTELIQUORMAINFRAGMENT)
        }
        mBinding.saveBtn.setOnClickListener {
            setSaveData()
        }
        mBinding.wasteLiquorLayout.setOnClickListener {
            hideKeyboard()
        }
    }

    private fun setSaveData() {
        for (i in viewmodel.alertMap) {
            val aqInfo = HexDump.toByteArray(i.key)
            val model = aqInfo[3].toInt()
            if (model == 3) {
                viewmodel.alertMap.remove(i.key)
                viewmodel.popUpDataLiveList.remove(i.value)
                viewmodel.popUpDataLiveList.notifyChange()
            }
        }
        viewmodel.WasteLiquorDataLiveList.clear(true)
        val iter = setWasteLiquorInfo.iterator()
        while (iter.hasNext()) {
            iter.forEach {
//                if (it.usable) {
                viewmodel.WasteLiquorDataLiveList.add(it)
//                } else iter.remove()
            }
        }
        val buff = mutableListOf<SetWasteLiquorViewData>()
        for (i in viewmodel.WasteLiquorDataLiveList.value!!) {
            buff.add(i)
        }
        shared.saveBoardSetData(SaminSharedPreference.WASTELIQUOR, buff)
        activity?.tmp?.LoadSetting()
        activity?.onFragmentChange(MainViewModel.WASTELIQUORMAINFRAGMENT)
    }


    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment WasteWaterSettingFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            WasteWaterSettingFragment().apply {
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
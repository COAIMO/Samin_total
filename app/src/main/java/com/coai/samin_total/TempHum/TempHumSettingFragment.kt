package com.coai.samin_total.TempHum

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.coai.samin_total.CustomView.SpaceDecoration
import com.coai.samin_total.Logic.SaminSharedPreference
import com.coai.samin_total.MainActivity
import com.coai.samin_total.MainViewModel
import com.coai.samin_total.Oxygen.OxygenSetting_RecyclerAdapter
import com.coai.samin_total.R
import com.coai.samin_total.Service.HexDump
import com.coai.samin_total.WasteLiquor.SetWasteLiquorViewData
import com.coai.samin_total.WasteLiquor.WasteLiquorSetting_RecyclerAdapter
import com.coai.samin_total.databinding.FragmentTempHumSettingBinding
import com.coai.samin_total.databinding.FragmentWasteLiquorSettingBinding

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class TempHumSettingFragment : Fragment() {
    private var param1: String? = null
    private var param2: String? = null
    private lateinit var mBinding: FragmentTempHumSettingBinding
    private lateinit var onBackPressed: OnBackPressedCallback
    private var activity: MainActivity? = null
    private val viewmodel by activityViewModels<MainViewModel>()
    private lateinit var recycleAdapter: TempHumSetting_RecyclerAdapter
    private val setTempHumInfo = mutableListOf<SetTempHumViewData>()
    var selectedSensor: SetTempHumViewData? = null
    lateinit var shared: SaminSharedPreference
    private val mTemp_maxValueWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            if (s != null && !s.toString().equals("")) {
                if (selectedSensor == null) return
                selectedSensor?.setTempMax = s.toString().toFloat()
            }
        }

        override fun afterTextChanged(s: Editable?) {
        }
    }
    private val mTemp_minValueWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            if (s != null && !s.toString().equals("")) {
                if (selectedSensor == null) return
                selectedSensor?.setTempMin = s.toString().toFloat()
            }
        }

        override fun afterTextChanged(s: Editable?) {
        }
    }
    private val mHum_maxValueWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            if (s != null && !s.toString().equals("")) {
                if (selectedSensor == null) return
                selectedSensor?.setHumMax = s.toString().toFloat()
            }
        }

        override fun afterTextChanged(s: Editable?) {
        }
    }
    private val mHum_minValueWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            if (s != null && !s.toString().equals("")) {
                if (selectedSensor == null) return
                selectedSensor?.setHumMin = s.toString().toFloat()
            }
        }

        override fun afterTextChanged(s: Editable?) {
        }
    }
    private val mTempHum_NameWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            if (s != null && !s.toString().equals("")) {
                if (selectedSensor == null) return
                selectedSensor?.temphumName = s.toString()
            }
        }

        override fun afterTextChanged(s: Editable?) {
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        activity = getActivity() as MainActivity
        onBackPressed = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                activity!!.onFragmentChange(MainViewModel.TEMPHUMMAINFRAGMENT)
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
        mBinding = FragmentTempHumSettingBinding.inflate(inflater, container, false)
        shared = SaminSharedPreference(requireContext())
        initRecycler()
        initView()
        recycleAdapter.setItemClickListener(object :
            TempHumSetting_RecyclerAdapter.OnItemClickListener {
            override fun onClick(v: View, position: Int) {
                selectedSensor = setTempHumInfo[position]
                mBinding.tempHumBoardSettingView.mSensorUsable_Sw.isChecked =
                    selectedSensor!!.usable

                mBinding.tempHumBoardSettingView.mMaxTemp_Et.setText(selectedSensor?.setTempMax.toString())
                mBinding.tempHumBoardSettingView.mMinTemp_Et.setText(selectedSensor?.setTempMin.toString())
                mBinding.tempHumBoardSettingView.mMaxHum_Et.setText(selectedSensor?.setHumMax.toString())
                mBinding.tempHumBoardSettingView.mMinHUm_Et.setText(selectedSensor?.setHumMin.toString())
                mBinding.tempHumBoardSettingView.mName_Et.setText(selectedSensor?.temphumName.toString())

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

            recycleAdapter = TempHumSetting_RecyclerAdapter()

            adapter = recycleAdapter
        }

    }

    private fun initView() {
        setTempHumInfo.removeAll(setTempHumInfo)
        for ((key, ids) in viewmodel.modelMap) {
            //indices 배열을 인덱스 범위
            if (key == "TempHum") {
                for (id in ids.indices) {
                    setTempHumInfo.add(
                        SetTempHumViewData(
                            key,
                            ids.get(id).toInt(),
                            1
                        )
                    )
                }
            }
        }
        //이전 저장된 설정 데이터 불러와서 적용.
        val exData =
            shared.loadBoardSetData(SaminSharedPreference.TEMPHUM) as MutableList<SetTempHumViewData>
        if (exData.isNotEmpty()) {
            for ((index, value) in exData.withIndex()) {
                setTempHumInfo.set(index, value)
            }
        }
        recycleAdapter.submitList(setTempHumInfo)

        mBinding.tempHumBoardSettingView.mSensorUsable_Sw.setOnClickListener {
            selectedSensor!!.usable = mBinding.tempHumBoardSettingView.mSensorUsable_Sw.isChecked
        }
        mBinding.tempHumBoardSettingView.mMaxTemp_Et.addTextChangedListener(
            mTemp_maxValueWatcher
        )
        mBinding.tempHumBoardSettingView.mMinTemp_Et.addTextChangedListener(
            mTemp_minValueWatcher
        )
        mBinding.tempHumBoardSettingView.mMaxHum_Et.addTextChangedListener(
            mHum_maxValueWatcher
        )
        mBinding.tempHumBoardSettingView.mMinHUm_Et.addTextChangedListener(
            mHum_minValueWatcher
        )
        mBinding.tempHumBoardSettingView.mName_Et.addTextChangedListener(mTempHum_NameWatcher)
        mBinding.btnBack.setOnClickListener {
            activity?.onFragmentChange(MainViewModel.TEMPHUMMAINFRAGMENT)
        }
        mBinding.saveBtn.setOnClickListener {
            setSaveData()
        }
        mBinding.tempHumLayout.setOnClickListener {
            hideKeyboard()
        }
    }

    private fun setSaveData() {
        for (i in viewmodel.alertMap) {
            val aqInfo = HexDump.toByteArray(i.key)
            val model = aqInfo[3].toInt()
            if (model == 6) {
                viewmodel.alertMap.remove(i.key)
                viewmodel.popUpDataLiveList.remove(i.value)
                viewmodel.popUpDataLiveList.notifyChange()
            }
        }
        viewmodel.TempHumDataLiveList.clear(true)
        val iter = setTempHumInfo.iterator()
        while (iter.hasNext()) {
            iter.forEach {
                viewmodel.TempHumDataLiveList.add(it)
            }
        }
        val buff = mutableListOf<SetTempHumViewData>()
        for (i in viewmodel.TempHumDataLiveList.value!!) {
            buff.add(i)
        }
        shared.saveBoardSetData(SaminSharedPreference.TEMPHUM, buff)
        activity?.tmp?.LoadSetting()
        activity?.onFragmentChange(MainViewModel.TEMPHUMMAINFRAGMENT)
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

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment TempHumSettingFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            TempHumSettingFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}
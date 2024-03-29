package com.coai.samin_total.GasDock

import android.content.Context
import android.graphics.Color
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
import com.coai.samin_total.Logic.SpinnerColorAdapter
import com.coai.samin_total.Logic.SpinnerColorItem
import com.coai.samin_total.Service.HexDump
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
    private var param1: String? = null
    private var param2: String? = null
    private lateinit var mBinding: FragmentGasStorageSettingBinding
    private lateinit var onBackPressed: OnBackPressedCallback
    private var activity: MainActivity? = null
    private val viewmodel by activityViewModels<MainViewModel>()
    private lateinit var recycleAdapter: GasStorageSetting_RecycleAdapter
    private val setGasSensorInfo = mutableListOf<SetGasStorageViewData>()

    //    var selectedSensor = SetGasStorageViewData("0", 0, 0)
    var selectedSensor: SetGasStorageViewData? = null
    lateinit var shared: SaminSharedPreference
    private val mCapaAlerttextWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            if (s != null && !s.toString().equals("") && !s.toString().equals("-") && !s.toString()
                    .equals(".")
            ) {
                try {
                    if (selectedSensor == null) return
                    if (selectedSensor?.ViewType == 1 || selectedSensor?.ViewType == 2) {
                        selectedSensor?.pressure_Min = s.toString().toFloat()

                        if (selectedSensor?.port == 1) {
                            for (i in setGasSensorInfo) {
                                if (i.port == 2 && i.id == selectedSensor?.id) {
                                    i.pressure_Min = selectedSensor?.pressure_Min
                                }
                            }
                        } else if (selectedSensor?.port == 2) {
                            for (i in setGasSensorInfo) {
                                if (i.port == 1 && i.id == selectedSensor?.id) {
                                    i.pressure_Min = selectedSensor?.pressure_Min
                                }
                            }
                        } else if (selectedSensor?.port == 3) {
                            for (i in setGasSensorInfo) {
                                if (i.port == 4 && i.id == selectedSensor?.id) {
                                    i.pressure_Min = selectedSensor?.pressure_Min
                                }
                            }
                        } else if (selectedSensor?.port == 4) {
                            for (i in setGasSensorInfo) {
                                if (i.port == 3 && i.id == selectedSensor?.id) {
                                    i.pressure_Min = selectedSensor?.pressure_Min
                                }
                            }
                        }
                    } else {
                        selectedSensor?.pressure_Min = s.toString().toFloat()
                    }

                }catch (e:Exception){
                    e.printStackTrace()
                }
            }
        }

        override fun afterTextChanged(s: Editable?) {
        }

    }
    private val mMaxCapatextWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            if (s != null && !s.toString().equals("") && !s.toString().equals("-") && !s.toString()
                    .equals(".")) {
//                selectedSensor.pressure_Max = s.toString().toFloat()
                try {
                    if (selectedSensor == null) return
                    if (selectedSensor?.ViewType == 1 || selectedSensor?.ViewType == 2) {
                        selectedSensor?.pressure_Max = s.toString().toFloat()

                        if (selectedSensor?.port == 1) {
                            for (i in setGasSensorInfo) {
                                if (i.port == 2 && i.id == selectedSensor?.id) {
                                    i.pressure_Max = selectedSensor?.pressure_Max
                                }
                            }
                        } else if (selectedSensor?.port == 2) {
                            for (i in setGasSensorInfo) {
                                if (i.port == 1 && i.id == selectedSensor?.id) {
                                    i.pressure_Max = selectedSensor?.pressure_Max
                                }
                            }
                        } else if (selectedSensor?.port == 3) {
                            for (i in setGasSensorInfo) {
                                if (i.port == 4 && i.id == selectedSensor?.id) {
                                    i.pressure_Max = selectedSensor?.pressure_Max
                                }
                            }
                        } else if (selectedSensor?.port == 4) {
                            for (i in setGasSensorInfo) {
                                if (i.port == 3 && i.id == selectedSensor?.id) {
                                    i.pressure_Max = selectedSensor?.pressure_Max
                                }
                            }
                        }
                    } else {
                        selectedSensor?.pressure_Max = s.toString().toFloat()
                    }

                }catch (e:Exception){
                    e.printStackTrace()
                }

            }

        }

        override fun afterTextChanged(s: Editable?) {
        }

    }
    private val mZeroPointtextWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            if (s != null && !s.toString().equals("") && !s.toString().equals("-") && !s.toString()
                    .equals(".")) {
//                selectedSensor.zeroPoint = s.toString().toFloat()
                if (selectedSensor == null) return
                try {
//                    if (selectedSensor?.ViewType == 1 || selectedSensor?.ViewType == 2) {
//                        selectedSensor?.zeroPoint = s.toString().toFloat()
//
//                        if (selectedSensor?.port == 1) {
//                            for (i in setGasSensorInfo) {
//                                if (i.port == 2 && i.id == selectedSensor?.id) {
//                                    i.zeroPoint = selectedSensor!!.zeroPoint
//                                }
//                            }
//                        } else if (selectedSensor?.port == 2) {
//                            for (i in setGasSensorInfo) {
//                                if (i.port == 1 && i.id == selectedSensor?.id) {
//                                    i.zeroPoint = selectedSensor!!.zeroPoint
//                                }
//                            }
//                        } else if (selectedSensor?.port == 3) {
//                            for (i in setGasSensorInfo) {
//                                if (i.port == 4 && i.id == selectedSensor?.id) {
//                                    i.zeroPoint = selectedSensor!!.zeroPoint
//                                }
//                            }
//                        } else if (selectedSensor?.port == 4) {
//                            for (i in setGasSensorInfo) {
//                                if (i.port == 3 && i.id == selectedSensor?.id) {
//                                    i.zeroPoint = selectedSensor!!.zeroPoint
//                                }
//                            }
//                        }
//                    } else {
//                        selectedSensor?.zeroPoint = s.toString().toFloat()
//                    }

                    if (selectedSensor?.ViewType == 1 || selectedSensor?.ViewType == 2) {

                        if (selectedSensor?.port == 1) {
                            selectedSensor?.left_zeroPoint = s.toString().toFloat()

                        } else if (selectedSensor?.port == 2) {
                            selectedSensor?.right_zeroPoint = s.toString().toFloat()
                            for (i in setGasSensorInfo) {
                                if (i.port == 1 && i.id == selectedSensor?.id) {
                                    i.right_zeroPoint = s.toString().toFloat()
                                }
                            }

                        } else if (selectedSensor?.port == 3) {
                            selectedSensor?.left_zeroPoint = s.toString().toFloat()
                        } else if (selectedSensor?.port == 4) {
                            selectedSensor?.right_zeroPoint = s.toString().toFloat()
                            for (i in setGasSensorInfo) {
                                if (i.port == 3 && i.id == selectedSensor?.id) {
                                    i.right_zeroPoint = s.toString().toFloat()
                                }
                            }
                        }
                    } else {
                        selectedSensor?.left_zeroPoint = s.toString().toFloat()

                    }

                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

        }

        override fun afterTextChanged(s: Editable?) {
        }

    }
    private val mRewardValuetextWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            if (s != null && !s.toString().equals("") && !s.toString().equals("-") && !s.toString()
                    .equals(".")) {
//                selectedSensor.rewardValue = s.toString().toFloat()
                if (selectedSensor == null) return
                try {
//                    if (selectedSensor?.ViewType == 1 || selectedSensor?.ViewType == 2) {
//                        selectedSensor?.rewardValue = s.toString().toFloat()
//
//                        if (selectedSensor?.port == 1) {
//                            for (i in setGasSensorInfo) {
//                                if (i.port == 2 && i.id == selectedSensor?.id) {
//                                    i.rewardValue = selectedSensor!!.rewardValue
//                                }
//                            }
//                        } else if (selectedSensor?.port == 2) {
//                            for (i in setGasSensorInfo) {
//                                if (i.port == 1 && i.id == selectedSensor?.id) {
//                                    i.rewardValue = selectedSensor!!.rewardValue
//                                }
//                            }
//                        } else if (selectedSensor?.port == 3) {
//                            for (i in setGasSensorInfo) {
//                                if (i.port == 4 && i.id == selectedSensor?.id) {
//                                    i.rewardValue = selectedSensor!!.rewardValue
//                                }
//                            }
//                        } else if (selectedSensor?.port == 4) {
//                            for (i in setGasSensorInfo) {
//                                if (i.port == 3 && i.id == selectedSensor?.id) {
//                                    i.rewardValue = selectedSensor!!.rewardValue
//                                }
//                            }
//                        }
//                    } else {
//                        selectedSensor?.rewardValue = s.toString().toFloat()
//                    }

                    if (selectedSensor?.ViewType == 1 || selectedSensor?.ViewType == 2) {

                        if (selectedSensor?.port == 1) {
                            selectedSensor?.left_rewardValue = s.toString().toFloat()

                        } else if (selectedSensor?.port == 2) {
                            selectedSensor?.right_rewardValue = s.toString().toFloat()
                            for (i in setGasSensorInfo) {
                                if (i.port == 1 && i.id == selectedSensor?.id) {
                                    i.right_rewardValue = s.toString().toFloat()
                                }
                            }
                        } else if (selectedSensor?.port == 3) {
                            selectedSensor?.left_rewardValue = s.toString().toFloat()
                        } else if (selectedSensor?.port == 4) {
                            selectedSensor?.right_rewardValue = s.toString().toFloat()
                            for (i in setGasSensorInfo) {
                                if (i.port == 3 && i.id == selectedSensor?.id) {
                                    i.right_rewardValue = s.toString().toFloat()
                                }
                            }
                        }
                    } else {
                        selectedSensor?.left_rewardValue = s.toString().toFloat()
                    }

                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

        }

        override fun afterTextChanged(s: Editable?) {
        }

    }
    private val mSelectedGastextWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            if (s != null && !s.toString().equals("")) {
                if (selectedSensor == null) return
                selectedSensor?.gasName = s.toString()
                if (selectedSensor?.ViewType == 1 || selectedSensor?.ViewType == 2) {
                    if (selectedSensor?.port == 1) {
                        for (i in setGasSensorInfo) {
                            if (i.port == 2 && i.id == selectedSensor?.id) {
                                i.gasName = selectedSensor!!.gasName
                            }
                        }
                    } else if (selectedSensor?.port == 2) {
                        for (i in setGasSensorInfo) {
                            if (i.port == 1 && i.id == selectedSensor?.id) {
                                i.gasName = selectedSensor!!.gasName
                            }
                        }
                    } else if (selectedSensor?.port == 3) {
                        for (i in setGasSensorInfo) {
                            if (i.port == 4 && i.id == selectedSensor?.id) {
                                i.gasName = selectedSensor!!.gasName
                            }
                        }
                    } else if (selectedSensor?.port == 4) {
                        for (i in setGasSensorInfo) {
                            if (i.port == 3 && i.id == selectedSensor?.id) {
                                i.gasName = selectedSensor!!.gasName
                            }
                        }
                    }
                }
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
        colorList.clear()
        selectedSensor = null
        onBackPressed.remove()
        activity = null
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mBinding = FragmentGasStorageSettingBinding.inflate(inflater, container, false)
        shared = SaminSharedPreference(requireContext())
        initRecycler()
        while (true) {
            try {
                initView()
                break
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
        }
        setGasTypeSpinner()
        setSensorTypeSpinner()
        setGasColorSpinner()
        changeView()

        recycleAdapter.setItemClickListener(object :
            GasStorageSetting_RecycleAdapter.OnItemClickListener {
            override fun onClick(v: View, position: Int) {
                selectedSensor = setGasSensorInfo[position]
                mBinding.gasStorageBoardSettingView.mSensorUsable_Sw.isChecked =
                    selectedSensor!!.usable

                mBinding.gasStorageBoardSettingView.mSensorType_Sp.setSelection(
                    viewmodel.gasSensorType.indexOf(
                        selectedSensor!!.sensorType
                    )
                )


                mBinding.gasStorageBoardSettingView.setRadioButton(selectedSensor!!.ViewType)
                mBinding.gasStorageBoardSettingView.mCapaAlert_Et.setText(selectedSensor!!.pressure_Min.toString())
                mBinding.gasStorageBoardSettingView.mMaxCapa_Et.setText(selectedSensor!!.pressure_Max.toString())

                if (selectedSensor!!.port == 1 || selectedSensor!!.port == 3) {
                    mBinding.gasStorageBoardSettingView.mRewardValue_Et.setText(selectedSensor!!.left_rewardValue.toString())
                    mBinding.gasStorageBoardSettingView.mZeroPoint_Et.setText(selectedSensor!!.left_zeroPoint.toString())
                } else {
                    mBinding.gasStorageBoardSettingView.mRewardValue_Et.setText(selectedSensor!!.right_rewardValue.toString())
                    mBinding.gasStorageBoardSettingView.mZeroPoint_Et.setText(selectedSensor!!.right_zeroPoint.toString())
                }
//                mBinding.gasStorageBoardSettingView.mRewardValue_Et.setText(selectedSensor!!.rewardValue.toString())
//                mBinding.gasStorageBoardSettingView.mZeroPoint_Et.setText(selectedSensor!!.zeroPoint.toString())

                mBinding.gasStorageBoardSettingView.mSelectedGas_Et.setText(selectedSensor!!.gasName.toString())

                mBinding.gasStorageBoardSettingView.mSelectedGasColor_sp.setSelection(
                    viewmodel.gasColorValue.indexOf(selectedSensor!!.gasColor)
                )
                mBinding.gasStorageBoardSettingView.mGasType_Sp.setSelection(
                    viewmodel.gasType.indexOf(
                        selectedSensor!!.gasName
                    )
                )
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


    fun changeView() {
        mBinding.gasStorageBoardSettingView.mSensorUsable_Sw.setOnClickListener {
            if (selectedSensor?.ViewType == 1 || selectedSensor?.ViewType == 2) {
                selectedSensor?.usable =
                    mBinding.gasStorageBoardSettingView.mSensorUsable_Sw.isChecked

                if (selectedSensor?.port == 1) {
                    for (i in setGasSensorInfo) {
                        if (i.port == 2 && i.id == selectedSensor?.id) {
                            i.usable = selectedSensor!!.usable
                        }
                    }
                } else if (selectedSensor?.port == 2) {
                    for (i in setGasSensorInfo) {
                        if (i.port == 1 && i.id == selectedSensor?.id) {
                            i.usable = selectedSensor!!.usable
                        }
                    }
                } else if (selectedSensor?.port == 3) {
                    for (i in setGasSensorInfo) {
                        if (i.port == 4 && i.id == selectedSensor?.id) {
                            i.usable = selectedSensor!!.usable
                        }
                    }
                } else if (selectedSensor?.port == 4) {
                    for (i in setGasSensorInfo) {
                        if (i.port == 3 && i.id == selectedSensor?.id) {
                            i.usable = selectedSensor!!.usable
                        }
                    }
                }

            } else {
                selectedSensor?.usable =
                    mBinding.gasStorageBoardSettingView.mSensorUsable_Sw.isChecked
            }
        }
        mBinding.gasStorageBoardSettingView.mViewType_Rg.setOnCheckedChangeListener { group, checkedId ->
            when (checkedId) {
                R.id.btn_single -> {
                    if (selectedSensor?.port == 1) {
                        selectedSensor?.ViewType = 0
                        for (i in setGasSensorInfo) {
                            if (i.port == 2 && i.id == selectedSensor?.id) {
                                i.ViewType = 0
                            }
                        }
                    } else if (selectedSensor?.port == 2) {
                        selectedSensor?.ViewType = 0
                        for (i in setGasSensorInfo) {
                            if (i.port == 1 && i.id == selectedSensor?.id) {
                                i.ViewType = 0
                            }
                        }
                    } else if (selectedSensor?.port == 3) {
                        selectedSensor?.ViewType = 0
                        for (i in setGasSensorInfo) {
                            if (i.port == 4 && i.id == selectedSensor?.id) {
                                i.ViewType = 0
                            }
                        }
                    } else if (selectedSensor?.port == 4) {
                        selectedSensor?.ViewType = 0
                        for (i in setGasSensorInfo) {
                            if (i.port == 3 && i.id == selectedSensor?.id) {
                                i.ViewType = 0
                            }
                        }
                    }
                }
                R.id.btn_dual -> {
                    if (selectedSensor?.port == 1) {
                        selectedSensor?.ViewType = 1
                        for (i in setGasSensorInfo) {
                            if (i.port == 2 && i.id == selectedSensor?.id) {
                                i.ViewType = 1
                                i.usable = selectedSensor!!.usable
                            }
                        }
                    } else if (selectedSensor?.port == 2) {
                        selectedSensor?.ViewType = 1
                        for (i in setGasSensorInfo) {
                            if (i.port == 1 && i.id == selectedSensor?.id) {
                                i.ViewType = 1
                                i.usable = selectedSensor!!.usable
                            }
                        }
                    } else if (selectedSensor?.port == 3) {
                        selectedSensor?.ViewType = 1
                        for (i in setGasSensorInfo) {
                            if (i.port == 4 && i.id == selectedSensor?.id) {
                                i.ViewType = 1
                                i.usable = selectedSensor!!.usable
                            }
                        }
                    } else if (selectedSensor?.port == 4) {
                        selectedSensor?.ViewType = 1
                        for (i in setGasSensorInfo) {
                            if (i.port == 3 && i.id == selectedSensor?.id) {
                                i.ViewType = 1
                                i.usable = selectedSensor!!.usable
                            }
                        }
                    }
                }
                R.id.btn_autoChanger -> {
                    if (selectedSensor?.port == 1) {
                        selectedSensor?.ViewType = 2
                        for (i in setGasSensorInfo) {
                            if (i.port == 2 && i.id == selectedSensor?.id) {
                                i.ViewType = 2
                                i.usable = selectedSensor!!.usable
                            }
                        }
                    } else if (selectedSensor?.port == 2) {
                        selectedSensor?.ViewType = 2
                        for (i in setGasSensorInfo) {
                            if (i.port == 1 && i.id == selectedSensor?.id) {
                                i.ViewType = 2
                                i.usable = selectedSensor!!.usable
                            }
                        }
                    } else if (selectedSensor?.port == 3) {
                        selectedSensor?.ViewType = 2
                        for (i in setGasSensorInfo) {
                            if (i.port == 4 && i.id == selectedSensor?.id) {
                                i.ViewType = 2
                                i.usable = selectedSensor!!.usable
                            }
                        }
                    } else if (selectedSensor?.port == 4) {
                        selectedSensor?.ViewType = 2
                        for (i in setGasSensorInfo) {
                            if (i.port == 3 && i.id == selectedSensor?.id) {
                                i.ViewType = 2
                                i.usable = selectedSensor!!.usable
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

        mBinding.gasStorageBoardSettingView.mSelectedGas_Et.addTextChangedListener(
            mSelectedGastextWatcher
        )
    }


    private fun initView() {
        setGasSensorInfo.removeAll(setGasSensorInfo)
//        viewmodel.GasStorageDataLiveList.clear(true)

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
        var count = 0
        //이전 저장된 설정 데이터 불러와서 적용.
        val exData =
            shared.loadBoardSetData(SaminSharedPreference.GASSTORAGE) as MutableList<SetGasStorageViewData>
        if (exData.isNotEmpty()) {
            for ((index, value) in exData.withIndex()) {
                if (value.ViewType == 1 || value.ViewType == 2) {
                    setGasSensorInfo.set(index + count, value)
                    val temp = value.copy(port = value.port + 1)
                    setGasSensorInfo.set(index + count + 1, temp)
                    count++
                } else {
                    setGasSensorInfo.set(index + count, value)
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

    private fun setSaveData() {
        for (i in viewmodel.alertMap) {
            val aqInfo = HexDump.toByteArray(i.key)
            val model = aqInfo[3].toInt()
            if (model == 1) {
                viewmodel.alertMap.remove(i.key)
                viewmodel.popUpDataLiveList.remove(i.value)
                viewmodel.popUpDataLiveList.notifyChange()
            }
        }
        viewmodel.GasStorageDataLiveList.clear(true)
        val iter = setGasSensorInfo.iterator()
        while (iter.hasNext()) {
            iter.forEach {
//                if (it.usable) {
                if (it.ViewType == 1 || it.ViewType == 2) {
                    if (it.port == 2 || it.port == 4) {
                        iter.remove()
                    } else {
                        viewmodel.GasStorageDataLiveList.add(it)
                    }
                } else {
                    viewmodel.GasStorageDataLiveList.add(it)
                }
//
//                } else {
//                    iter.remove()
//                }

            }
        }
        //설정값 저장
        val buff = mutableListOf<SetGasStorageViewData>()
        for (i in viewmodel.GasStorageDataLiveList.value!!) {
            buff.add(i)
        }
        shared.saveBoardSetData(SaminSharedPreference.GASSTORAGE, buff)
        activity?.onFragmentChange(MainViewModel.GASDOCKMAINFRAGMENT)
        activity?.tmp?.LoadSetting()

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
                    mBinding.gasStorageBoardSettingView.mSelectedGas_Et.setText(viewmodel.gasType[position])
                    mBinding.gasStorageBoardSettingView.mSelectedGasColor_sp.setSelection(position)
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                    Toast.makeText(context, "센서 타입을 선택해주세요.", Toast.LENGTH_SHORT)
                        .show()
                }
            }
    }

    private fun setGasColorSpinner() {
//        val arrayAdapter = ArrayAdapter(
//            requireContext(),
//            R.layout.support_simple_spinner_dropdown_item,
//            viewmodel.gasColor
//        )
//        mBinding.gasStorageBoardSettingView.mSelectedGasColor_sp.adapter = arrayAdapter
//        mBinding.gasStorageBoardSettingView.mSelectedGasColor_sp.onItemSelectedListener =
//            object : AdapterView.OnItemSelectedListener {
//                override fun onItemSelected(
//                    parent: AdapterView<*>?,
//                    view: View?,
//                    position: Int,
//                    id: Long
//                ) {
//
//                    selectedSensor?.gasColor =
//                        viewmodel.gasColorMap[viewmodel.gasType[position]]!!
//                    if (selectedSensor?.ViewType == 1 || selectedSensor?.ViewType == 2) {
//                        if (selectedSensor?.port == 1) {
//                            for (i in setGasSensorInfo) {
//                                if (i.port == 2 && i.id == selectedSensor?.id) {
//                                    i.gasColor = selectedSensor!!.gasColor
//                                }
//                            }
//                        } else if (selectedSensor?.port == 2) {
//                            for (i in setGasSensorInfo) {
//                                if (i.port == 1 && i.id == selectedSensor?.id) {
//                                    i.gasColor = selectedSensor!!.gasColor
//                                }
//                            }
//                        } else if (selectedSensor?.port == 3) {
//                            for (i in setGasSensorInfo) {
//                                if (i.port == 4 && i.id == selectedSensor?.id) {
//                                    i.gasColor = selectedSensor!!.gasColor
//                                }
//                            }
//                        } else if (selectedSensor?.port == 4) {
//                            for (i in setGasSensorInfo) {
//                                if (i.port == 3 && i.id == selectedSensor?.id) {
//                                    i.gasColor = selectedSensor!!.gasColor
//                                }
//                            }
//                        }
//
//                    }
//                }
//
//                override fun onNothingSelected(parent: AdapterView<*>?) {
//                    Toast.makeText(context, "센서 컬러를 선택해주세요.", Toast.LENGTH_SHORT)
//                        .show()
//                }
//            }

        for (i in viewmodel.gasColor.withIndex()) {
            val color = viewmodel.gasColorValue[i.index]
            val tmp = SpinnerColorItem(i.value, color)
            colorList.add(tmp)
        }

        val adapter = SpinnerColorAdapter(requireContext(), colorList)
        mBinding.gasStorageBoardSettingView.mSelectedGasColor_sp.adapter = adapter

        mBinding.gasStorageBoardSettingView.mSelectedGasColor_sp.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {

                    selectedSensor?.gasColor =
                        viewmodel.gasColorMap[viewmodel.gasType[position]]!!
                    if (selectedSensor?.ViewType == 1 || selectedSensor?.ViewType == 2) {
                        if (selectedSensor?.port == 1) {
                            for (i in setGasSensorInfo) {
                                if (i.port == 2 && i.id == selectedSensor?.id) {
                                    i.gasColor = selectedSensor!!.gasColor
                                }
                            }
                        } else if (selectedSensor?.port == 2) {
                            for (i in setGasSensorInfo) {
                                if (i.port == 1 && i.id == selectedSensor?.id) {
                                    i.gasColor = selectedSensor!!.gasColor
                                }
                            }
                        } else if (selectedSensor?.port == 3) {
                            for (i in setGasSensorInfo) {
                                if (i.port == 4 && i.id == selectedSensor?.id) {
                                    i.gasColor = selectedSensor!!.gasColor
                                }
                            }
                        } else if (selectedSensor?.port == 4) {
                            for (i in setGasSensorInfo) {
                                if (i.port == 3 && i.id == selectedSensor?.id) {
                                    i.gasColor = selectedSensor!!.gasColor
                                }
                            }
                        }

                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                    Toast.makeText(context, "센서 컬러를 선택해주세요.", Toast.LENGTH_SHORT)
                        .show()
                }
            }

    }

    val colorList: ArrayList<SpinnerColorItem> = arrayListOf<SpinnerColorItem>()
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
                    selectedSensor?.sensorType = viewmodel.gasSensorType[position]
                    mBinding.gasStorageBoardSettingView.mMaxCapa_Et.setText(viewmodel.maxPressureMap[selectedSensor?.sensorType].toString())
                    selectedSensor?.pressure_Max =
                        viewmodel.maxPressureMap[selectedSensor!!.sensorType]

                    if (selectedSensor?.ViewType == 1 || selectedSensor?.ViewType == 2) {

                        selectedSensor?.sensorType = viewmodel.gasSensorType[position]
                        mBinding.gasStorageBoardSettingView.mMaxCapa_Et.setText(viewmodel.maxPressureMap[selectedSensor?.sensorType].toString())
                        selectedSensor?.pressure_Max =
                            viewmodel.maxPressureMap[selectedSensor?.sensorType]

                        if (selectedSensor?.port == 1) {
                            for (i in setGasSensorInfo) {
                                if (i.port == 2 && i.id == selectedSensor?.id) {
                                    i.sensorType = selectedSensor!!.sensorType
                                    i.pressure_Max =
                                        viewmodel.maxPressureMap[selectedSensor?.sensorType]
                                    mBinding.gasStorageBoardSettingView.mMaxCapa_Et.setText(
                                        viewmodel.maxPressureMap[selectedSensor?.sensorType].toString()
                                    )

                                }
                            }
                        } else if (selectedSensor?.port == 2) {
                            for (i in setGasSensorInfo) {
                                if (i.port == 1 && i.id == selectedSensor?.id) {
                                    i.sensorType = selectedSensor!!.sensorType
                                    i.pressure_Max =
                                        viewmodel.maxPressureMap[selectedSensor?.sensorType]
                                    mBinding.gasStorageBoardSettingView.mMaxCapa_Et.setText(
                                        viewmodel.maxPressureMap[selectedSensor?.sensorType].toString()
                                    )

                                }
                            }
                        } else if (selectedSensor?.port == 3) {
                            for (i in setGasSensorInfo) {
                                if (i.port == 4 && i.id == selectedSensor?.id) {
                                    i.sensorType = selectedSensor!!.sensorType
                                    i.pressure_Max =
                                        viewmodel.maxPressureMap[selectedSensor?.sensorType]
                                    mBinding.gasStorageBoardSettingView.mMaxCapa_Et.setText(
                                        viewmodel.maxPressureMap[selectedSensor?.sensorType].toString()
                                    )

                                }
                            }
                        } else if (selectedSensor?.port == 4) {
                            for (i in setGasSensorInfo) {
                                if (i.port == 3 && i.id == selectedSensor?.id) {
                                    i.sensorType = selectedSensor!!.sensorType
                                    i.pressure_Max =
                                        viewmodel.maxPressureMap[selectedSensor?.sensorType]
                                    mBinding.gasStorageBoardSettingView.mMaxCapa_Et.setText(
                                        viewmodel.maxPressureMap[selectedSensor?.sensorType].toString()
                                    )

                                }
                            }
                        }

                    } else {
                        selectedSensor?.sensorType = viewmodel.gasSensorType[position]
                        selectedSensor?.pressure_Max =
                            viewmodel.maxPressureMap[selectedSensor?.sensorType]
                        mBinding.gasStorageBoardSettingView.mMaxCapa_Et.setText(viewmodel.maxPressureMap[selectedSensor?.sensorType].toString())

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
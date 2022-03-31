package com.coai.samin_total.WasteLiquor

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.coai.samin_total.CustomView.SpaceDecoration
import com.coai.samin_total.MainActivity
import com.coai.samin_total.MainViewModel
import com.coai.samin_total.R
import com.coai.samin_total.Steamer.SetSteamerViewData
import com.coai.samin_total.Steamer.SteamerSetting_RecyclerAdapter
import com.coai.samin_total.databinding.FragmentSteamerSettingBinding
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
    var selectedSensor = SetWasteLiquorViewData("adsfsd", 0, 0)

    private val mWasteNameWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            if (s != null && !s.toString().equals("")) {
                selectedSensor.liquidName = s.toString()
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
        mBinding = FragmentWasteLiquorSettingBinding.inflate(inflater, container, false)
        initRecycler()
        initView()
//        setLevelSensorTypeSpinner()

        recycleAdapter.setItemClickListener(object :
            WasteLiquorSetting_RecyclerAdapter.OnItemClickListener {
            override fun onClick(v: View, position: Int) {
                selectedSensor = setWasteLiquorInfo[position]
                mBinding.wasteLiquorBoardSettingView.mSensorUsable_Sw.isChecked =
                    selectedSensor.usable

                mBinding.wasteLiquorBoardSettingView.mSensorType_Sp.setSelection(
                    viewmodel.tempSensorType.indexOf(
                        selectedSensor.level_SensorType
                    )
                )
                mBinding.wasteLiquorBoardSettingView.mWasteName_Et.setText(selectedSensor.liquidName)
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
        viewmodel.WasteLiquorDataLiveList.clear(true)
        for ((key, ids) in viewmodel.modelMap) {
            //indices 배열을 인덱스 범위
            if (key == "Steamer") {
                for (id in ids.indices) {
                    for (port in 1..4) {
                        setWasteLiquorInfo.add(SetWasteLiquorViewData(key, ids.get(id).toInt(), port))
                    }
                }
            }
        }
        recycleAdapter.submitList(setWasteLiquorInfo)

        mBinding.wasteLiquorBoardSettingView.mSensorUsable_Sw.setOnClickListener {
            selectedSensor.usable = mBinding.wasteLiquorBoardSettingView.mSensorUsable_Sw.isChecked
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
    }

    private fun setSaveData() {
        val iter = setWasteLiquorInfo.iterator()
        while (iter.hasNext()) {
            iter.forEach {
                if (it.usable){
                    viewmodel.WasteLiquorDataLiveList.add(it)
                }else iter.remove()
            }
        }
        if (!activity?.isSending!!) {
            activity?.callFeedback()
            activity?.isSending = true
        }
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
}
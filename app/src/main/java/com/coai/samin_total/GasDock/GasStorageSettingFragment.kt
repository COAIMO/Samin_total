package com.coai.samin_total.GasDock

import android.content.Context
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
import com.coai.samin_total.CustomView.GasStorageBoardSettingView
import com.coai.samin_total.CustomView.SpaceDecoration
import com.coai.samin_total.databinding.FragmentGasStorageSettingBinding

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


        return mBinding.root
    }

    private val sensorInfoData = mutableListOf<SetAqInfo>()
    val aqSettingViewList = mutableListOf<View>()
    val aqInfo_ViewMap = HashMap<SetAqInfo, View>()

    private fun initView() {
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
                when (checkedId) {
                    R.id.btn_single -> {
                        Log.d("테스트", "key : $key")

                    }
                    R.id.btn_dual -> {
                        Log.d("테스트", "key : $key")
                        if (key!!.port == 1 || key!!.port  == 2){
                            i.setRadioButton(2)
                        }

                    }
                    R.id.btn_autoChanger -> {
                        Log.d("테스트", "key : $key")

                    }
                    else -> {
                    }
                }
            }
        }


        for ((info, view) in aqInfo_ViewMap) {
            (view as GasStorageBoardSettingView).mViewType_Rg.setOnClickListener {
                Log.d("테스트", "$it")

            }
//            when ((view as GasStorageBoardSettingView).selected_ViewType) {
//                1 -> {
//                    //dual &&그리고 ||또는
//                    if (info.port == 1 || info.port ==2){
//                        view.setRadioButton(1)
//                    }
//                }
//                2 -> {
//                    //auto
//                }
//            }

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
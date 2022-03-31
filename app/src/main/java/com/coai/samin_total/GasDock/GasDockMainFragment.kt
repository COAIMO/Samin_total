package com.coai.samin_total.GasDock

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.coai.samin_total.*
import com.coai.samin_total.databinding.FragmentGasDockMainBinding

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [GasDockMainFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class GasDockMainFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    lateinit private var mBinding: FragmentGasDockMainBinding
    private var activity: MainActivity? = null
    private val gasStorageViewData = mutableListOf<SetGasStorageViewData>()
    private val viewData = mutableListOf<SetGasStorageViewData>()

    private lateinit var recycleAdapter: GasStorage_RecycleAdapter
    private lateinit var onBackPressed: OnBackPressedCallback
    val TAG = "GasDockMainFragment"
    private lateinit var sendThread: Thread
    var sending = false
    private val mainViewModel by activityViewModels<MainViewModel>()
    var btn_Count = 0


    override fun onAttach(context: Context) {
        super.onAttach(context)
        activity = getActivity() as MainActivity
        onBackPressed = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                activity!!.onFragmentChange(MainViewModel.MAINFRAGMENT)
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(this, onBackPressed)
    }

    override fun onDetach() {
        super.onDetach()
        activity = null
        onBackPressed.remove()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mBinding = FragmentGasDockMainBinding.inflate(inflater, container, false)
        initRecycler()
        initView()
        updateView()

        mBinding.btnSetting.setOnClickListener {
            activity?.onFragmentChange(MainViewModel.GASSTORAGESETTINGFRAGMENT)
        }
        mBinding.btnZoomInout.setOnClickListener {
            if (btn_Count % 2 == 0) {
                btn_Count++
                mBinding.gasStorageRecyclerView.apply {
                    layoutManager =
                        GridLayoutManager(context, 2, GridLayoutManager.VERTICAL, false)

                    //아이템 높이 간격 조절
                    val decoration_height = RecyclerDecoration_Height(25)
                    addItemDecoration(decoration_height)

                    recycleAdapter.submitList(gasStorageViewData)
                    adapter = recycleAdapter
                }
            } else {
                btn_Count++
                mBinding.gasStorageRecyclerView.apply {
                    layoutManager =
                        GridLayoutManager(context, 4, GridLayoutManager.VERTICAL, false)

                    //아이템 높이 간격 조절
                    val decoration_height = RecyclerDecoration_Height(25)
                    addItemDecoration(decoration_height)

                    recycleAdapter.submitList(gasStorageViewData)
                    adapter = recycleAdapter
                }

            }
        }
        mBinding.btnUnit.setOnClickListener {
            for ((index, data) in mainViewModel.GasStorageDataLiveList.value!!.sortedWith(compareBy(
                { it.id },
                { it.port })).withIndex()) {
                Log.d("테스트", "인텍스: $index" + "데이더 : $data")
                data.unit++
                mainViewModel.GasStorageDataLiveList.value!!.set(index, data)
                if(data.unit == 3) data.unit = 0
            }
            recycleAdapter.submitList(mainViewModel.GasStorageDataLiveList.value!!)
            recycleAdapter.notifyDataSetChanged()
        }

//        mBinding.gasStorageRecyclerView.apply {
//            gasStorageViewData.apply {
//                add(
//                    SetGasdockViewData(
//                        0,
//                        "O2",
//                        Color.parseColor("#6599CD"),
//                        200f,
//                        2000f,
//                        gasIndex = 0,
//                        pressure = 0f,
//                    )
//                )
//            }
//        }
//
//        mainViewModel.GasStorageData.observe(viewLifecycleOwner,{
//            recycleAdapter.setGasdockViewData.set(0, SetGasdockViewData(0,
//                "O2",
//                Color.parseColor("#6599CD"),
//                200f,
//                2000f,
//                gasIndex = 0,
//                pressure = it)
//            )
//            recycleAdapter.notifyDataSetChanged()
//
//        })

        return mBinding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        sending = false
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun initView() {
//        if (gasStorageViewData.isEmpty()){
//
//            for (i in mainViewModel.GasStorageDataLiveList.value!!
//
//            ) {
//                Log.d("테스트","Data: $i")
//
//                gasStorageViewData.add(i)
//            }
//            recycleAdapter.submitList(gasStorageViewData)
//            recycleAdapter.notifyDataSetChanged()
//        }

        val mm = mainViewModel.GasStorageDataLiveList.value!!.sortedWith(compareBy({ it.id },
            { it.port }))
        recycleAdapter.submitList(mm)
        recycleAdapter.notifyDataSetChanged()
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun updateView() {
        mainViewModel.GasStorageDataLiveList.observe(viewLifecycleOwner, {
//            for ((index, data) in it.sortedWith(compareBy({it.id},{it.port})).withIndex()){
//                Log.d("테스트","인텍스: $index" + "데이더 : $data")
//
//                gasStorageViewData.set(index, data)
//            }
//            recycleAdapter.submitList(gasStorageViewData)
//            recycleAdapter.notifyDataSetChanged()

            val mm = it.sortedWith(compareBy({ it.id }, { it.port }))
            recycleAdapter.submitList(mm)
            recycleAdapter.notifyDataSetChanged()
        })
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment GasDockMainFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            GasDockMainFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    private fun initRecycler() {
        recycleAdapter = GasStorage_RecycleAdapter()
        mBinding.gasStorageRecyclerView.apply {
            layoutManager =
                GridLayoutManager(context, 4, GridLayoutManager.VERTICAL, false)

            this.setHasFixedSize(true)
            //아이템 높이 간격 조절
            val decoration_height = RecyclerDecoration_Height(25)
            addItemDecoration(decoration_height)

            //페이지 넘기는 효과
//            val snapHelper = PagerSnapHelper()
//            snapHelper.attachToRecyclerView(this)

            //Indicator 추가
//            addItemDecoration(LinePagerIndicatorDecoration())


            recycleAdapter.submitList(gasStorageViewData)
            adapter = recycleAdapter
        }

    }
}
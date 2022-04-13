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
import com.coai.samin_total.Dialog.AlertDialogFragment
import com.coai.samin_total.GasRoom.SetGasRoomViewData
import com.coai.samin_total.Logic.SaminSharedPreference
import com.coai.samin_total.databinding.FragmentGasDockMainBinding
import java.util.*

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
    private var param1: String? = null
    private var param2: String? = null
    lateinit private var mBinding: FragmentGasDockMainBinding
    private var activity: MainActivity? = null
    private val gasStorageViewData = mutableListOf<SetGasStorageViewData>()
    private lateinit var recycleAdapter: GasStorage_RecycleAdapter
    private lateinit var onBackPressed: OnBackPressedCallback
    var sending = false
    private val mainViewModel by activityViewModels<MainViewModel>()
    private var btn_Count = 0
    lateinit var alertdialogFragment: AlertDialogFragment
    lateinit var shared: SaminSharedPreference
    private var timerTaskRefresh: Timer? = null
    var heartbeatCount: UByte = 0u
    val lockobj = object {}

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
        timerTaskRefresh = kotlin.concurrent.timer(period = 50) {
            heartbeatCount++
            for (tmp in mainViewModel.GasStorageDataLiveList.value!!.iterator()) {
                tmp.heartbeatCount = heartbeatCount
            }

            synchronized(lockobj) {
                val tmp = (mBinding.gasStorageRecyclerView.layoutManager as GridLayoutManager)
                activity?.runOnUiThread() {
                    try {
                        val start = tmp.findFirstVisibleItemPosition()
                        val end = tmp.findLastVisibleItemPosition()
                        recycleAdapter.notifyItemRangeChanged(start, end - start + 1)
                    } catch (ex: Exception) {
                    }
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        timerTaskRefresh?.cancel()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mBinding = FragmentGasDockMainBinding.inflate(inflater, container, false)
        shared = SaminSharedPreference(requireContext())
        initRecycler()
        initView()
        updateView()

        mBinding.btnSetting.setOnClickListener {
            activity?.onFragmentChange(MainViewModel.GASSTORAGESETTINGFRAGMENT)
        }
        mBinding.btnZoomInout.setOnClickListener {
            if (btn_Count % 2 == 0) {
                btn_Count++
                synchronized(lockobj) {
                    mBinding.gasStorageRecyclerView.apply {
                        layoutManager =
                            GridLayoutManager(context, 2, GridLayoutManager.VERTICAL, false)
                        adapter = recycleAdapter
                    }
                }
            } else {
                btn_Count++
                synchronized(lockobj){
                    mBinding.gasStorageRecyclerView.apply {
                        layoutManager =
                            GridLayoutManager(context, 4, GridLayoutManager.VERTICAL, false)
                        adapter = recycleAdapter
                    }
                }
            }
        }
        mBinding.btnUnit.setOnClickListener {
            for ((index, data) in mainViewModel.GasStorageDataLiveList.value!!.sortedWith(
                compareBy(
                    { it.id },
                    { it.port })
            ).withIndex()) {
                data.unit++
                mainViewModel.GasStorageDataLiveList.value!!.set(index, data)
                if (data.unit == 4) data.unit = 0
            }
            recycleAdapter.submitList(mainViewModel.GasStorageDataLiveList.value!!)
            recycleAdapter.notifyDataSetChanged()

            for ((index, data) in mainViewModel.GasStorageDataLiveList.value!!.sortedWith(
                compareBy(
                    { it.id },
                    { it.port })
            ).withIndex()) {
            }
        }
        mBinding.btnAlert.setOnClickListener {
            alertdialogFragment = AlertDialogFragment()
            val bundle = Bundle()
            bundle.putString("model", "GasStorage")
            alertdialogFragment.arguments = bundle
            alertdialogFragment.show(childFragmentManager, "GasStorage")
        }
        mBinding.btnBack.setOnClickListener {
            activity?.onFragmentChange(MainViewModel.MAINFRAGMENT)
        }

        updateAlert()
        return mBinding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        sending = false
    }

    private fun initView() {
        //셋팅 데이터 불러와서 뷰추가 할것!!!
//        val storgeDataSet =
//            shared.loadBoardSetData(SaminSharedPreference.GASSTORAGE) as MutableList<SetGasStorageViewData>
//        mainViewModel.GasStorageDataLiveList.clear(true)
//        if (storgeDataSet.isNotEmpty()) {
//            for (i in storgeDataSet) {
//                mainViewModel.GasStorageDataLiveList.add(i)
//            }
//        }

        val mm = mainViewModel.GasStorageDataLiveList.value!!.sortedWith(
            compareBy({ it.id },
                { it.port })
        )
        recycleAdapter.submitList(mm)
        recycleAdapter.notifyDataSetChanged()
        activity?.tmp?.LoadSetting()

    }

    @SuppressLint("NotifyDataSetChanged")
    private fun updateView() {
        mainViewModel.GasStorageDataLiveList.observe(viewLifecycleOwner) {
            val mm = it.sortedWith(compareBy({ it.id }, { it.port }))
            recycleAdapter.submitList(mm)
            recycleAdapter.notifyDataSetChanged()
        }
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

    private fun updateAlert() {
        mainViewModel.gasStorageAlert.observe(viewLifecycleOwner) {
            if (it) {
                mBinding.btnAlert.setImageResource(R.drawable.onalert_ic)
            }else{
                mBinding.btnAlert.setImageResource(R.drawable.nonalert_ic)
            }
        }

    }
}
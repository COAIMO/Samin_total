package com.coai.samin_total.Steamer

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.coai.samin_total.Dialog.AlertDialogFragment
import com.coai.samin_total.Logic.SaminSharedPreference
import com.coai.samin_total.Logic.SpacesItemDecoration
import com.coai.samin_total.MainActivity
import com.coai.samin_total.MainViewModel
import com.coai.samin_total.R
import com.coai.samin_total.RecyclerDecoration_Height
import com.coai.samin_total.databinding.FragmentSteamerMainBinding
import java.util.*

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [SteamerMainFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class SteamerMainFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    lateinit var mBinding: FragmentSteamerMainBinding
    private lateinit var recycleAdapter: Steamer_RecycleAdapter
    private val steamerViewData = mutableListOf<SetSteamerViewData>()
    private lateinit var onBackPressed: OnBackPressedCallback
    private var activity: MainActivity? = null
    private val viewmodel by activityViewModels<MainViewModel>()
    private lateinit var sendThread: Thread
    var sending = false
    var btn_Count = 0
    lateinit var alertdialogFragment: AlertDialogFragment
    lateinit var shared: SaminSharedPreference
    private var timerTaskRefresh: Timer? = null
    var heartbeatCount: UByte = 0u
    val lockobj = object {}
    lateinit var itemSpace: SpacesItemDecoration

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
    override fun onPause() {
        super.onPause()
        timerTaskRefresh?.cancel()
    }

    override fun onResume() {
        super.onResume()
        timerTaskRefresh = kotlin.concurrent.timer(period = 50) {
            heartbeatCount++
            for (tmp in viewmodel.SteamerDataLiveList.value!!.iterator()) {
                tmp.heartbeatCount = heartbeatCount
            }
            synchronized(lockobj) {
                val tmp = (mBinding.steamerRecyclerView.layoutManager as GridLayoutManager)
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

    @SuppressLint("NotifyDataSetChanged")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mBinding = FragmentSteamerMainBinding.inflate(inflater, container, false)
        shared = SaminSharedPreference(requireContext())
        itemSpace = SpacesItemDecoration(50)
        initRecycler()
        initView()
        updateView()

        mBinding.btnSetting.setOnClickListener {
            activity?.onFragmentChange(MainViewModel.STEAMERSETTINGFRAGMENT)
        }
        mBinding.btnZoomInout.setOnClickListener {
            if (btn_Count % 2 == 0) {
                btn_Count++
                synchronized(lockobj) {
                    mBinding.steamerRecyclerView.apply {
                        layoutManager =
                            GridLayoutManager(context, 2, GridLayoutManager.VERTICAL, false)
                        adapter = recycleAdapter
                    }
                }
            } else {
                btn_Count++
                synchronized(lockobj) {
                    mBinding.steamerRecyclerView.apply {
                        layoutManager =
                            GridLayoutManager(context, 4, GridLayoutManager.VERTICAL, false)
                        adapter = recycleAdapter
                    }
                }
            }
        }
        mBinding.btnUnit.setOnClickListener {
            for ((index, data) in viewmodel.SteamerDataLiveList.value!!.sortedWith(
                compareBy(
                    { it.id },
                    { it.port })
            ).withIndex()) {
                data.unit++
                viewmodel.SteamerDataLiveList.value!!.set(index, data)
//                Log.d("전", "인텍스: $index" + "데이더 : $data")
                if (data.unit > 1) data.unit = 0
            }

        }

        mBinding.btnAlert.setOnClickListener {
            alertdialogFragment = AlertDialogFragment()
            val bundle = Bundle()
            bundle.putString("model", "Steamer")
            alertdialogFragment.arguments = bundle
            alertdialogFragment.show(childFragmentManager, "Steamer")
        }

        mBinding.btnBack.setOnClickListener {
            activity?.onFragmentChange(MainViewModel.MAINFRAGMENT)
        }

        updateAlert()
        return mBinding.root
    }


    private fun initRecycler() {
        mBinding.steamerRecyclerView.apply {
            layoutManager =
                GridLayoutManager(context, 4, GridLayoutManager.VERTICAL, false)

//            //아이템 높이 간격 조절
            itemSpace.changeSpace(90,50,90,50)
            addItemDecoration(itemSpace)

            recycleAdapter = Steamer_RecycleAdapter()
            adapter = recycleAdapter
        }

    }

    @SuppressLint("NotifyDataSetChanged")
    private fun initView() {
//        viewmodel.SteamerDataLiveList.clear(true)
//        val steamerDataSet = shared.loadBoardSetData(SaminSharedPreference.STEAMER) as MutableList<SetSteamerViewData>
//        if (steamerDataSet.isNotEmpty()){
//            for (i in steamerDataSet){
//                viewmodel.SteamerDataLiveList.add(i)
//            }
//        }
        val mm = viewmodel.SteamerDataLiveList.value!!.sortedWith(compareBy({ it.id }, { it.port }))
        recycleAdapter.submitList(mm)
//        recycleAdapter.notifyDataSetChanged()
//        activity?.tmp?.LoadSetting()

    }

    @SuppressLint("NotifyDataSetChanged")
    private fun updateView() {
//        viewmodel.SteamerDataLiveList.observe(viewLifecycleOwner) {
//            val mm = it.sortedWith(compareBy({ it.id }, { it.port }))
//            recycleAdapter.submitList(mm)
//            recycleAdapter.notifyDataSetChanged()
//        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        sending = false
    }

    private fun updateAlert() {
        viewmodel.steamerAlert.observe(viewLifecycleOwner) {
            if (it) {
                mBinding.btnAlert.setImageResource(R.drawable.onalert_ic)
            }else{
                mBinding.btnAlert.setImageResource(R.drawable.nonalert_ic)
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
         * @return A new instance of fragment SteamerMainFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            SteamerMainFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}
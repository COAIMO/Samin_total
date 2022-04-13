package com.coai.samin_total.Oxygen

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
import androidx.recyclerview.widget.LinearLayoutManager
import com.coai.samin_total.Dialog.AlertDialogFragment
import com.coai.samin_total.Logic.SaminSharedPreference
import com.coai.samin_total.MainActivity
import com.coai.samin_total.MainViewModel
import com.coai.samin_total.R
import com.coai.samin_total.RecyclerDecoration_Height
import com.coai.samin_total.databinding.FragmentOxygenMainBinding
import java.util.*

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [OxygenMainFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class OxygenMainFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private lateinit var mBinding: FragmentOxygenMainBinding
    private val viewmodel by activityViewModels<MainViewModel>()
    private lateinit var recycleAdapter: Oxygen_RecycleAdapter
    private val oxygenViewData = mutableListOf<SetOxygenViewData>()
    private lateinit var onBackPressed: OnBackPressedCallback
    private var activity: MainActivity? = null
    private lateinit var sendThread: Thread
    var sending = false
    var btn_Count = 0
    lateinit var alertdialogFragment: AlertDialogFragment
    lateinit var shared: SaminSharedPreference
    private var timerTaskRefresh: Timer? = null
    var heartbeatCount: UByte = 0u
    val lockobj = object {}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
//        viewmodel = ViewModelProvider(this).get(OxygenViewModel::class.java)
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
        timerTaskRefresh?.cancel()
    }

    override fun onResume() {
        super.onResume()
        timerTaskRefresh = kotlin.concurrent.timer(period = 50) {
            heartbeatCount++
            for (tmp in viewmodel.OxygenDataLiveList.value!!.iterator()) {
                tmp.heartbeatCount = heartbeatCount
            }
            synchronized(lockobj) {
                val tmp = (mBinding.oxygenRecyclerView.layoutManager as GridLayoutManager)
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
        mBinding = FragmentOxygenMainBinding.inflate(inflater, container, false)
        shared = SaminSharedPreference(requireContext())
        initRecycler()
        initView()
        updateView()
        mBinding.btnSetting.setOnClickListener {
            activity?.onFragmentChange(MainViewModel.OXYGENSETTINGFRAGMENT)
        }
        mBinding.btnZoomInout.setOnClickListener {
            if (btn_Count % 2 == 0) {
                btn_Count++
                synchronized(lockobj) {
                    mBinding.oxygenRecyclerView.apply {
                        layoutManager =
                            GridLayoutManager(context, 2, GridLayoutManager.VERTICAL, false)
                        adapter = recycleAdapter
                    }
                }
            } else {
                btn_Count++
                synchronized(lockobj) {
                    mBinding.oxygenRecyclerView.apply {
                        layoutManager =
                            GridLayoutManager(context, 4, GridLayoutManager.VERTICAL, false)
                        adapter = recycleAdapter
                    }
                }
            }
        }

        mBinding.btnAlert.setOnClickListener {
            alertdialogFragment = AlertDialogFragment()
            val bundle = Bundle()
            bundle.putString("model", "Oxygen")
            alertdialogFragment.arguments = bundle
            alertdialogFragment.show(childFragmentManager, "Oxygen")
        }

        mBinding.btnBack.setOnClickListener {
            activity?.onFragmentChange(MainViewModel.MAINFRAGMENT)
        }
        updateAlert()
        return mBinding.root
    }

    private fun initRecycler() {
        mBinding.oxygenRecyclerView.apply {
            layoutManager =
                GridLayoutManager(context, 1, LinearLayoutManager.VERTICAL, false)

            //아이템 높이 간격 조절
//            val decoration_height = RecyclerDecoration_Height(70)
//            addItemDecoration(decoration_height)

            recycleAdapter = Oxygen_RecycleAdapter()
            adapter = recycleAdapter
        }

    }

    @SuppressLint("NotifyDataSetChanged")
    private fun initView() {
//        viewmodel.OxygenDataLiveList.clear(true)
//        val oxygenDataSet = shared.loadBoardSetData(SaminSharedPreference.OXYGEN) as MutableList<SetOxygenViewData>
//        if (oxygenDataSet.isNotEmpty()){
//            for (i in oxygenDataSet){
//                viewmodel.OxygenDataLiveList.add(i)
//            }
//        }
        val mm = viewmodel.OxygenDataLiveList.value!!.sortedWith(compareBy({ it.id }, { it.port }))
        recycleAdapter.submitList(mm)
        recycleAdapter.notifyDataSetChanged()
        activity?.tmp?.LoadSetting()
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun updateView() {
        viewmodel.OxygenDataLiveList.observe(viewLifecycleOwner) {
            val mm = it.sortedWith(compareBy({ it.id }, { it.port }))
            recycleAdapter.submitList(mm)
            recycleAdapter.notifyDataSetChanged()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        sending = false
    }

    private fun updateAlert() {
        viewmodel.oxyenAlert.observe(viewLifecycleOwner) {
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
         * @return A new instance of fragment OxygenMainFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            OxygenMainFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}
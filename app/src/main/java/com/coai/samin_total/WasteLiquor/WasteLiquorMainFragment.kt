package com.coai.samin_total.WasteLiquor

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
import com.coai.samin_total.databinding.FragmentWasteLiquorMainBinding
import java.util.*

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [WasteLiquorMainFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class WasteLiquorMainFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private lateinit var mBinding: FragmentWasteLiquorMainBinding
    private lateinit var recycleAdapter: WasteLiquor_RecycleAdapter
    private val wasteLiquorViewData = mutableListOf<SetWasteLiquorViewData>()
    private lateinit var onBackPressed: OnBackPressedCallback
    private var activity: MainActivity? = null
    private val viewmodel by activityViewModels<MainViewModel>()
    private lateinit var sendThread: Thread
    var btn_Count = 0
    lateinit var alertdialogFragment: AlertDialogFragment
    lateinit var alertThread: Thread
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

    override fun onResume() {
        super.onResume()
        timerTaskRefresh = kotlin.concurrent.timer(period = 50) {
            heartbeatCount++
            for (tmp in viewmodel.WasteLiquorDataLiveList.value!!.iterator()) {
                tmp.heartbeatCount = heartbeatCount
            }

            synchronized(lockobj) {
                val tmp = (mBinding.wasteLiquorRecyclerView.layoutManager as GridLayoutManager)
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

    @SuppressLint("NotifyDataSetChanged")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mBinding = FragmentWasteLiquorMainBinding.inflate(inflater, container, false)
        shared = SaminSharedPreference(requireContext())
        itemSpace = SpacesItemDecoration(50)
        initRecycler()
        initView()

        mBinding.btnSetting.setOnClickListener {
            activity?.onFragmentChange(MainViewModel.WASTELIQUORSETTINGFRAGMENT)
        }

        mBinding.btnZoomInout.setOnClickListener {
            if (btn_Count % 2 == 0) {
                btn_Count++
                synchronized(lockobj) {
                    mBinding.wasteLiquorRecyclerView.apply {
                        layoutManager =
                            GridLayoutManager(context, 2, GridLayoutManager.VERTICAL, false)
                        adapter = recycleAdapter
                    }
                }
            } else {
                btn_Count++
                synchronized(lockobj) {
                    mBinding.wasteLiquorRecyclerView.apply {
                        layoutManager =
                            GridLayoutManager(context, 4, GridLayoutManager.VERTICAL, false)
                        adapter = recycleAdapter
                    }
                }
            }
        }

        mBinding.btnAlert.setOnClickListener {
//            udateAlert()
            alertdialogFragment = AlertDialogFragment()
            val bundle = Bundle()
            bundle.putString("model", "WasteLiquor")
            alertdialogFragment.arguments = bundle
            alertdialogFragment.show(childFragmentManager, "WasteLiquor")
        }

        mBinding.btnBack.setOnClickListener {
            activity?.onFragmentChange(MainViewModel.MAINFRAGMENT)
        }
        updateAlert()

        return mBinding.root
    }

    private fun initRecycler() {
        mBinding.wasteLiquorRecyclerView.apply {
            layoutManager =
                GridLayoutManager(context, 4, GridLayoutManager.VERTICAL, false)

            //아이템 높이 간격 조절
            itemSpace.changeSpace(90,50,90,50)
            addItemDecoration(itemSpace)

            recycleAdapter = WasteLiquor_RecycleAdapter()
            adapter = recycleAdapter
        }

    }

    @SuppressLint("NotifyDataSetChanged")
    private fun initView() {
//        viewmodel.WasteLiquorDataLiveList.clear(true)
//        val wasteDataSet = shared.loadBoardSetData(SaminSharedPreference.WASTELIQUOR) as MutableList<SetWasteLiquorViewData>
//        if (wasteDataSet.isNotEmpty()){
//            for (i in wasteDataSet){
//                viewmodel.WasteLiquorDataLiveList.add(i)
//            }
//        }
        val mm =
            viewmodel.WasteLiquorDataLiveList.value!!.sortedWith(compareBy({ it.id }, { it.port }))
        val testList = mutableListOf<SetWasteLiquorViewData>()
        testList.apply {
            add(SetWasteLiquorViewData("1",1,1))
            add(SetWasteLiquorViewData("1",1,1))
            add(SetWasteLiquorViewData("1",1,1))
            add(SetWasteLiquorViewData("1",1,1))
            add(SetWasteLiquorViewData("1",1,1))
            add(SetWasteLiquorViewData("1",1,1))
            add(SetWasteLiquorViewData("1",1,1))
            add(SetWasteLiquorViewData("1",1,1))
            add(SetWasteLiquorViewData("1",1,1))
            add(SetWasteLiquorViewData("1",1,1))
            add(SetWasteLiquorViewData("1",1,1))

        }
        recycleAdapter.submitList(testList)
//        recycleAdapter.notifyDataSetChanged()
//        activity?.tmp?.LoadSetting()
    }


    private fun updateAlert() {
        viewmodel.wasteAlert.observe(viewLifecycleOwner) {
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
         * @return A new instance of fragment WasteLiquorMainFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            WasteLiquorMainFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}
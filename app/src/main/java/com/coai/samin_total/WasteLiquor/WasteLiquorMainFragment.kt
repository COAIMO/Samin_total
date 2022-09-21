package com.coai.samin_total.WasteLiquor

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
import androidx.recyclerview.widget.SimpleItemAnimator
import com.coai.samin_total.Dialog.AlertDialogFragment
import com.coai.samin_total.Logic.SaminSharedPreference
import com.coai.samin_total.Logic.SpacesItemDecoration
import com.coai.samin_total.Logic.Utils
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
    private val wasteLiquorViewData = arrayListOf<SetWasteLiquorViewData>()
    private val newwasteLiquorViewData = arrayListOf<SetWasteLiquorViewData>()

    private lateinit var onBackPressed: OnBackPressedCallback
    private var activity: MainActivity? = null
    private val viewmodel by activityViewModels<MainViewModel>()
    var btn_Count = 0
    lateinit var alertdialogFragment: AlertDialogFragment
    lateinit var shared: SaminSharedPreference
    private var timerTaskRefresh: Timer? = null
    var heartbeatCount: UByte = 0u
    val lockobj = object {}
    lateinit var itemSpace: SpacesItemDecoration
    private var taskRefresh: Thread? = null
    private var isOnTaskRefesh: Boolean = true

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
        isOnTaskRefesh = true
        taskRefresh = Thread() {
            try {
                var lastupdate: Long = System.currentTimeMillis()
                val lstvalue = mutableListOf<Int>()
                while (isOnTaskRefesh) {
                    lstvalue.clear()
                    heartbeatCount++

                    for (t in newwasteLiquorViewData) {
                        val idx = newwasteLiquorViewData.indexOf(t)
                        if (idx > -1) {
                            if (wasteLiquorViewData[idx].isAlert != t.isAlert)
                                lstvalue.add(idx)

                            if ((((heartbeatCount / 10u) % 2u) == 0u) != ((((heartbeatCount - 1u )/ 10u) % 2u) == 0u)) {
                                if (t.isAlert)
                                    if (!lstvalue.contains(idx))
                                        lstvalue.add(idx)
                            }
                            t.heartbeatCount = heartbeatCount
                            wasteLiquorViewData[idx] = t.copy()
                        }
                    }

                    val baseTime = System.currentTimeMillis() - 1000 * 2
                    if (lastupdate < baseTime) {
                        lastupdate = System.currentTimeMillis()
                        for (t in newwasteLiquorViewData) {
                            val idx = newwasteLiquorViewData.indexOf(t)
                            wasteLiquorViewData[idx] = t.copy()
                        }

                        synchronized(lockobj) {
                            activity?.runOnUiThread {
                                recycleAdapter.notifyItemRangeChanged(0, recycleAdapter.itemCount)
                            }
                        }
                    }
                    else {
                        val rlist = Utils.ToIntRange(lstvalue, wasteLiquorViewData.size)
                        if (rlist != null) {
                            Log.d("debug", "${rlist.size}")
                            synchronized(lockobj) {
                                activity?.runOnUiThread() {
                                    rlist.forEach {
                                        recycleAdapter.notifyItemRangeChanged(
                                            it.lower,
                                            1 + it.upper - it.lower
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Thread.sleep(50)

                }

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        taskRefresh?.start()
    }

    override fun onPause() {
        super.onPause()
        isOnTaskRefesh = false
        taskRefresh?.interrupt()
        taskRefresh?.join()
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
            if (!viewmodel.wasteViewZoomState) {
                viewmodel.wasteViewZoomState = true
                mBinding.btnZoomInout.setImageResource(R.drawable.screen_decrease_ic)
                synchronized(lockobj) {
                    mBinding.wasteLiquorRecyclerView.apply {
                        (layoutManager as GridLayoutManager).let {
                            it.spanCount = 2
                        }
                        itemSpace.changeSpace(180, 150, 180, 150)
                    }
                }
            } else {
                viewmodel.wasteViewZoomState = false
                mBinding.btnZoomInout.setImageResource(R.drawable.screen_increase_ic)
                synchronized(lockobj) {
                    mBinding.wasteLiquorRecyclerView.apply {
                        (layoutManager as GridLayoutManager).let {
                            it.spanCount = 4
                        }
                        itemSpace.changeSpace(30, 20, 70, 20)
                    }
                }
            }

            synchronized(lockobj) {
                activity?.runOnUiThread {
                    recycleAdapter.notifyItemRangeChanged(0, recycleAdapter.itemCount)
                }
            }
        }

        mBinding.btnAlert.setOnClickListener {
//            udateAlert()
            alertdialogFragment = viewmodel.alertDialogFragment
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
            if (!viewmodel.wasteViewZoomState) {
                layoutManager =
                    GridLayoutManager(context, 4, GridLayoutManager.VERTICAL, false)
                itemSpace.changeSpace(50, 50, 90, 50)
            } else {
                layoutManager =
                    GridLayoutManager(context, 2, GridLayoutManager.VERTICAL, false)
                itemSpace.changeSpace(180, 150, 180, 150)
            }
            addItemDecoration(itemSpace)
            recycleAdapter = WasteLiquor_RecycleAdapter()
            adapter = recycleAdapter
        }
        mBinding.wasteLiquorRecyclerView.itemAnimator = null
        mBinding.wasteLiquorRecyclerView.animation = null
//        (mBinding.wasteLiquorRecyclerView.itemAnimator as? SimpleItemAnimator)?.supportsChangeAnimations = false
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun initView() {
        if (!shared.loadLabNameData().isEmpty()) {
            mBinding.labNameTv.text = shared.loadLabNameData()
        }
        val mm =
            viewmodel.WasteLiquorDataLiveList.value!!.sortedWith(compareBy({ it.id }, { it.port }))
        newwasteLiquorViewData.clear()
        for(tmp in mm)
            if (tmp.usable)
                newwasteLiquorViewData.add(tmp)

        wasteLiquorViewData.clear()
        for(t in newwasteLiquorViewData)
            wasteLiquorViewData.add(t.copy())

        recycleAdapter.submitList(newwasteLiquorViewData)

        if (viewmodel.wasteViewZoomState) {
            mBinding.btnZoomInout.setImageResource(R.drawable.screen_decrease_ic)
        }else{
            mBinding.btnZoomInout.setImageResource(R.drawable.screen_increase_ic)
        }

        synchronized(lockobj) {
            activity?.runOnUiThread {
                recycleAdapter.notifyItemRangeChanged(0, recycleAdapter.itemCount)
            }
        }
    }


    private fun updateAlert() {
        viewmodel.wasteAlert.observe(viewLifecycleOwner) {
            if (it) {
                mBinding.btnAlert.setImageResource(R.drawable.onalert_ic)
            } else {
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
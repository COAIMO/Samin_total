package com.coai.samin_total.Steamer

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.*
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.coai.samin_total.Dialog.AlertDialogFragment
import com.coai.samin_total.Logic.SaminSharedPreference
import com.coai.samin_total.Logic.SpacesItemDecoration
import com.coai.samin_total.Logic.Utils
import com.coai.samin_total.MainActivity
import com.coai.samin_total.MainViewModel
import com.coai.samin_total.R
import com.coai.samin_total.databinding.FragmentSteamerMainBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean

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
    private lateinit var onBackPressed: OnBackPressedCallback
    private var activity: MainActivity? = null
    private val viewmodel by activityViewModels<MainViewModel>()
    var sending = false
    var btn_Count = 0
    lateinit var alertdialogFragment: AlertDialogFragment
    lateinit var shared: SaminSharedPreference
    private val steamerViewData = arrayListOf<SetSteamerViewData>()
    private val newsteamerViewData = arrayListOf<SetSteamerViewData>()

    //    private var timerTaskRefresh: Timer? = null
//    private var taskRefresh: Thread? = null
//    private var isOnTaskRefesh: Boolean = true
    var heartbeatCount: UByte = 0u
//    val lockobj = object {}
    lateinit var itemSpace: SpacesItemDecoration

    private var isOnTaskRefesh = AtomicBoolean(true)
    private var updateJob: Job? = null

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

        isOnTaskRefesh.set(true)
        startUpdateTask()
    }

    override fun onDetach() {
        super.onDetach()
        activity = null
        onBackPressed.remove()

        isOnTaskRefesh.set(false)
        stopUpdateTask()
    }

    override fun onPause() {
        super.onPause()

        activity?.shared?.setFragment(MainViewModel.STEAMERMAINFRAGMENT)
    }

    override fun onResume() {
        super.onResume()

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

        mBinding.btnSetting.setOnClickListener {
            activity?.onFragmentChange(MainViewModel.STEAMERSETTINGFRAGMENT)
        }
        mBinding.btnZoomInout.setOnClickListener {
            if (!viewmodel.steamerViewZoomState) {
                viewmodel.steamerViewZoomState = true
                mBinding.btnZoomInout.setImageResource(R.drawable.screen_decrease_ic)
                mBinding.steamerRecyclerView.apply {
                    (layoutManager as GridLayoutManager).let {
                        it.spanCount = 2
                    }
                    itemSpace.changeSpace(220, 150, 200, 150)
                }
            } else {
                viewmodel.steamerViewZoomState = false
                mBinding.btnZoomInout.setImageResource(R.drawable.screen_increase_ic)
                mBinding.steamerRecyclerView.apply {
                    (layoutManager as GridLayoutManager).let {
                        it.spanCount = 4
                    }
                    itemSpace.changeSpace(90, 50, 90, 50)
                }
            }
//            activity?.runOnUiThread {
//                recycleAdapter.notifyItemRangeChanged(0, recycleAdapter.itemCount)
//            }
            CoroutineScope(Dispatchers.Main).launch {
                recycleAdapter.notifyItemRangeChanged(0, recycleAdapter.itemCount)
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
                if (data.unit > 1) data.unit = 0
            }

        }

        mBinding.btnAlert.setOnClickListener {
            alertdialogFragment = viewmodel.alertDialogFragment
            val bundle = Bundle()
            bundle.putString("model", "Steamer")
            alertdialogFragment.arguments = bundle
            alertdialogFragment.show(childFragmentManager, "Steamer")
        }

        mBinding.btnBack.setOnClickListener {
            activity?.onFragmentChange(MainViewModel.MAINFRAGMENT)
        }

        updateAlert()
        viewmodel.date.observe(viewLifecycleOwner) {
            mBinding.tvCurruntTime.text = it
        }
        return mBinding.root
    }


    private fun initRecycler() {
        mBinding.steamerRecyclerView.apply {
            if (!viewmodel.steamerViewZoomState) {
                layoutManager =
                    GridLayoutManager(context, 4, GridLayoutManager.VERTICAL, false)
                itemSpace.changeSpace(50, 50, 90, 50)
            }else{
                layoutManager =
                    GridLayoutManager(context, 2, GridLayoutManager.VERTICAL, false)
                itemSpace.changeSpace(220, 150, 200, 150)
            }
            addItemDecoration(itemSpace)
            recycleAdapter = Steamer_RecycleAdapter()
            adapter = recycleAdapter
        }
//        (mBinding.steamerRecyclerView.itemAnimator as? SimpleItemAnimator)?.supportsChangeAnimations = false
        mBinding.steamerRecyclerView.itemAnimator = null
        mBinding.steamerRecyclerView.animation = null
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun initView() {
        if (!shared.loadLabNameData().isEmpty()) {
            mBinding.labNameTv.text = shared.loadLabNameData()
        }

        val mm = viewmodel.SteamerDataLiveList.value!!.sortedWith(compareBy({ it.id }, { it.port }))
        newsteamerViewData.clear()
        for (tmp in mm)
            if (tmp.usable)
                newsteamerViewData.add(tmp)

        steamerViewData.clear()
        for (t in newsteamerViewData)
            steamerViewData.add(t.copy())
        recycleAdapter.submitList(newsteamerViewData)

        if (viewmodel.steamerViewZoomState) {
            mBinding.btnZoomInout.setImageResource(R.drawable.screen_decrease_ic)
        }else{
            mBinding.btnZoomInout.setImageResource(R.drawable.screen_increase_ic)
        }

//        activity?.runOnUiThread {
//            recycleAdapter.notifyItemRangeChanged(0, recycleAdapter.itemCount)
//        }
        CoroutineScope(Dispatchers.Main).launch {
            recycleAdapter.notifyItemRangeChanged(0, recycleAdapter.itemCount)
        }
    }

//    @SuppressLint("NotifyDataSetChanged")
//    private fun updateView() {
////        viewmodel.SteamerDataLiveList.observe(viewLifecycleOwner) {
////            val mm = it.sortedWith(compareBy({ it.id }, { it.port }))
////            recycleAdapter.submitList(mm)
////            recycleAdapter.notifyDataSetChanged()
////        }
//    }

    override fun onDestroyView() {
        super.onDestroyView()
        sending = false
    }

    private fun updateAlert() {
        viewmodel.steamerAlert.observe(viewLifecycleOwner) {
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

    private fun startUpdateTask() {
        updateJob = CoroutineScope(Dispatchers.Main).launch {
            var lastupdate: Long = System.currentTimeMillis()
            val lstvalue = mutableListOf<Int>()
            while (isOnTaskRefesh.get()) {
                lstvalue.clear()
                heartbeatCount++
                try {
                    for (t in newsteamerViewData) {
                        val idx = newsteamerViewData.indexOf(t)
                        if (idx > -1) {
                            if (steamerViewData[idx].unit != t.unit ||
                                steamerViewData[idx].isAlertTemp != t.isAlertTemp ||
                                steamerViewData[idx].isAlertLow != t.isAlertLow ||
                                steamerViewData[idx].isTemp != t.isTemp){
                                if (!lstvalue.contains(idx))
                                    lstvalue.add(idx)
                            }

                            if ((((heartbeatCount / 10u) % 2u) == 0u) != ((((heartbeatCount - 1u )/ 10u) % 2u) == 0u)) {
                                if (t.isAlertTemp || t.isAlertLow) {
                                    if (!lstvalue.contains(idx))
                                        lstvalue.add(idx)
                                }
                                else if (steamerViewData[idx].isTemp == 0) {
                                    if (!lstvalue.contains(idx))
                                        lstvalue.add(idx)
                                }
                            }

                            t.heartbeatCount = heartbeatCount
                            steamerViewData[idx] = t.copy()
                        }
                    }

                    val baseTime = System.currentTimeMillis() - 1000 * 2
                    if (lastupdate < baseTime) {
                        lastupdate = System.currentTimeMillis()
                        for (t in newsteamerViewData) {
                            val idx = newsteamerViewData.indexOf(t)
                            steamerViewData[idx] = t.copy()
                        }

                        recycleAdapter.notifyItemRangeChanged(0, recycleAdapter.itemCount)
                    }
                    else {
                        Utils.ToIntRange(lstvalue, steamerViewData.size)?.forEach {
                            recycleAdapter.notifyItemRangeChanged(
                                it.lower,
                                1 + it.upper - it.lower
                            )
                        }
                    }
                }
                catch (ex: Exception) {
                    ex.printStackTrace()
                }

                delay(50)
            }
        }
    }

    private fun stopUpdateTask() {
        updateJob?.cancel()
    }
}
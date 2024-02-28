package com.coai.samin_total.Oxygen

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
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
import com.coai.samin_total.databinding.FragmentOxygenMainBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean

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
//    private val oxygenViewData = mutableListOf<SetOxygenViewData>()
    private var oxygenViewData:SetOxygenViewData? = null

    private val oxygenViewDataList = arrayListOf<SetOxygenViewData>()
    private val newOxygenViewDataList = arrayListOf<SetOxygenViewData>()

    private lateinit var onBackPressed: OnBackPressedCallback
    private var activity: MainActivity? = null
//    private lateinit var sendThread: Thread
    var sending = false
    var btn_Count = 0
    lateinit var alertdialogFragment: AlertDialogFragment
    lateinit var shared: SaminSharedPreference
//    private var timerTaskRefresh: Timer? = null
    var heartbeatCount: UByte = 0u
//    val lockobj = object {}
    lateinit var itemSpace: SpacesItemDecoration
//    private var taskRefresh: Thread? = null
//    private var isOnTaskRefesh: Boolean = true
    private val isOnTaskRefesh = AtomicBoolean(true)
    private var updateJob: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    @Deprecated("Deprecated in Java")
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

        isOnTaskRefesh.set(true)
        startUpdateTask()
    }

    override fun onDetach() {
        super.onDetach()
        activity = null
        onBackPressed.remove()
//        timerTaskRefresh?.cancel()

        isOnTaskRefesh.set(false)
        stopUpdateTask()
    }

    override fun onResume() {
        super.onResume()
//        isOnTaskRefesh = true
    }

    override fun onPause() {
        super.onPause()
        activity?.shared?.setFragment(MainViewModel.OXYGENMAINFRAGMENT)
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mBinding = FragmentOxygenMainBinding.inflate(inflater, container, false)
        shared = SaminSharedPreference(requireContext())
        itemSpace = SpacesItemDecoration(50)
        initRecycler()
        initView()
//        updateView()
        mBinding.btnSetting.setOnClickListener {
            activity?.onFragmentChange(MainViewModel.OXYGENSETTINGFRAGMENT)
        }
        mBinding.btnZoomInout.setOnClickListener {
            if (!viewmodel.oxygenViewZoomState) {
                viewmodel.oxygenViewZoomState = true
                mBinding.btnZoomInout.setImageResource(R.drawable.screen_decrease_ic)
                mBinding.oxygenRecyclerView.apply {
                    layoutManager =
                        GridLayoutManager(context, 2, GridLayoutManager.VERTICAL, false)
                    itemSpace.changeSpace(
                        180, 150, 180, 150
                    )
                }
            } else {
                viewmodel.oxygenViewZoomState = false
                mBinding.btnZoomInout.setImageResource(R.drawable.screen_increase_ic)
                mBinding.oxygenRecyclerView.apply {
                    layoutManager =
                        GridLayoutManager(context, 4, GridLayoutManager.VERTICAL, false)
                    itemSpace.changeSpace(
                        80, 50, 90, 50
                    )
                }
            }
//            activity?.runOnUiThread {
//                recycleAdapter.notifyItemRangeChanged(0, recycleAdapter.itemCount)
//            }
            CoroutineScope(Dispatchers.Main).launch {
                recycleAdapter.notifyItemRangeChanged(0, recycleAdapter.itemCount)
            }
        }

        mBinding.btnAlert.setOnClickListener {
            alertdialogFragment = viewmodel.alertDialogFragment
            val bundle = Bundle()
            bundle.putString("model", "Oxygen")
            alertdialogFragment.arguments = bundle
            alertdialogFragment.show(childFragmentManager, "Oxygen")
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
        mBinding.oxygenRecyclerView.apply {
            if (!viewmodel.oxygenViewZoomState) {
                layoutManager =
                    GridLayoutManager(context, 4, GridLayoutManager.VERTICAL, false)
                itemSpace.changeSpace(
                    80, 50, 50, 50
                )
            } else {
                layoutManager =
                    GridLayoutManager(context, 2, GridLayoutManager.VERTICAL, false)
                itemSpace.changeSpace(
                    180, 150, 180, 150
                )
            }
            addItemDecoration(itemSpace)
            recycleAdapter = Oxygen_RecycleAdapter()
            adapter = recycleAdapter
        }
        mBinding.oxygenRecyclerView.itemAnimator = null
        mBinding.oxygenRecyclerView.animation = null
//        (mBinding.oxygenRecyclerView.itemAnimator as? SimpleItemAnimator)?.supportsChangeAnimations = false
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun initView() {
        if (!shared.loadLabNameData().isEmpty()) {
            mBinding.labNameTv.text = shared.loadLabNameData()
        }
        val mm = viewmodel.OxygenDataLiveList.value!!.sortedWith(compareBy({ it.id }, { it.port }))
        newOxygenViewDataList.clear()
        for (tmp in mm)
            if (tmp.usable)
                newOxygenViewDataList.add(tmp)
        oxygenViewDataList.clear()
        for (t in newOxygenViewDataList){
            oxygenViewDataList.add(t.copy())
        }

        recycleAdapter.submitList(newOxygenViewDataList)


        if (viewmodel.oxygenViewZoomState) {
            mBinding.btnZoomInout.setImageResource(R.drawable.screen_decrease_ic)
        } else {
            mBinding.btnZoomInout.setImageResource(R.drawable.screen_increase_ic)
        }

//        activity?.runOnUiThread {
//            recycleAdapter.notifyItemRangeChanged(0, recycleAdapter.itemCount)
//        }
        CoroutineScope(Dispatchers.Main).launch {
            recycleAdapter.notifyItemRangeChanged(0, recycleAdapter.itemCount)
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

    private fun startUpdateTask() {
        updateJob = CoroutineScope(Dispatchers.Main).launch {
            var lastupdate: Long = System.currentTimeMillis()
            val lstvalue = mutableListOf<Int>()
            while (isOnTaskRefesh.get()){
                lstvalue.clear()
                heartbeatCount++

                try {
                    for (t in newOxygenViewDataList){
                        val idx = newOxygenViewDataList.indexOf(t)
                        if (idx > -1){
                            if (oxygenViewDataList[idx].isAlert != t.isAlert)
                                lstvalue.add(idx)

                            if ((((heartbeatCount / 10u) % 2u) == 0u) != ((((heartbeatCount - 1u )/ 10u) % 2u) == 0u)){
                                if (t.isAlert)
                                    if (!lstvalue.contains(idx))
                                        lstvalue.add(idx)
                            }
                            t.heartbeatCount = heartbeatCount
                            oxygenViewDataList[idx] = t.copy()
                        }
                    }

                    val baseTime = System.currentTimeMillis() - 1000 * 2
                    if (lastupdate < baseTime){
                        lastupdate = System.currentTimeMillis()
                        for (t in newOxygenViewDataList){
                            val idx =  newOxygenViewDataList.indexOf(t)
                            oxygenViewDataList[idx] = t.copy()
                        }

                        recycleAdapter.notifyItemRangeChanged(0, recycleAdapter.itemCount)
                    }else {
                        Utils.ToIntRange(lstvalue, oxygenViewDataList.size)?.forEach {
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
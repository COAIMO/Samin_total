package com.coai.samin_total.GasRoom

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
import com.coai.samin_total.Dialog.AlertDialogFragment
import com.coai.samin_total.Dialog.LeakTestDialogFragment
import com.coai.samin_total.Logic.SaminSharedPreference
import com.coai.samin_total.Logic.SpacesItemDecoration
import com.coai.samin_total.Logic.Utils
import com.coai.samin_total.MainActivity
import com.coai.samin_total.MainViewModel
import com.coai.samin_total.R
import com.coai.samin_total.databinding.FragmentGasRoomMainBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.job
import kotlinx.coroutines.launch
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [GasRoomMainFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class GasRoomMainFragment : Fragment() {
    private var param1: String? = null
    private var param2: String? = null
    private lateinit var mBinding: FragmentGasRoomMainBinding
    private val gasRoomViewData = arrayListOf<SetGasRoomViewData>()
    private val newgasRoomViewData = arrayListOf<SetGasRoomViewData>()
    private lateinit var recycleAdapter: GasRoom_RecycleAdapter
    private lateinit var onBackPressed: OnBackPressedCallback
    private var activity: MainActivity? = null
    private val viewmodel by activityViewModels<MainViewModel>()
//    var btn_Count = 0
    lateinit var alertdialogFragment: AlertDialogFragment
    lateinit var shared: SaminSharedPreference
    lateinit var itemSpace: SpacesItemDecoration
    private var isOnTaskRefesh = AtomicBoolean(true)
    lateinit var leaktestdialogFragment: LeakTestDialogFragment

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

//    val lockobj = object {}
    override fun onResume() {
        super.onResume()
//        isOnTaskRefesh = true

    }

    private fun startUpdateTask() {
        updateJob = CoroutineScope(Dispatchers.Main).launch {
            var heartbeatCount: UByte = 0u
            var lastupdate: Long = System.currentTimeMillis() - 1000 * 2
            val lstvalue = mutableListOf<Int>()

            while (isOnTaskRefesh.get()) {
                // UI 업데이트 로직...
                lstvalue.clear()
                heartbeatCount++

                for (t in newgasRoomViewData) {
                    val idx = newgasRoomViewData.indexOf(t)
                    if (idx > -1) {
                        if (gasRoomViewData[idx].pressure != t.pressure ||
                            gasRoomViewData[idx].isAlert != t.isAlert ||
                            gasRoomViewData[idx].unit != t.unit)
                        {
                            if (!lstvalue.contains(idx))
                                lstvalue.add(idx)
                        }

                        if ((((heartbeatCount / 10u) % 2u) == 0u) != ((((heartbeatCount - 1u )/ 10u) % 2u) == 0u)) {
                            if (t.isAlert) {
                                if (!lstvalue.contains(idx))
                                    lstvalue.add(idx)
                            }
                        }
                        t.heartbeatCount = heartbeatCount
                        gasRoomViewData[idx] = t.copy()
                    }
                }

                val baseTime = System.currentTimeMillis() - 1000 * 2
                if (lastupdate < baseTime) {
                    lastupdate = System.currentTimeMillis()
                    for (t in newgasRoomViewData) {
                        val idx = newgasRoomViewData.indexOf(t)
                        gasRoomViewData[idx] = t.copy()
                    }

                    recycleAdapter.notifyItemRangeChanged(0, recycleAdapter.itemCount)
                }
                else {
                    val rlist = Utils.ToIntRange(lstvalue, gasRoomViewData.size)
                    if (rlist != null) {
                        rlist.forEach {
                            recycleAdapter.notifyItemRangeChanged(
                                it.lower,
                                1 + it.upper - it.lower
                            )
                        }
                    }
                }

                delay(50) // Coroutine 내에서의 지연
            }
        }
//id : StandaloneCoroutine{Active}@66012bb
        Log.d("GasRoomMainFragment", "id : ${updateJob}")
    }

    private fun stopUpdateTask() {
        updateJob?.cancel()
    }


    override fun onPause() {
        super.onPause()

        activity?.shared?.setFragment(MainViewModel.GASROOMMAINFRAGMENT)
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mBinding = FragmentGasRoomMainBinding.inflate(inflater, container, false)
        shared = SaminSharedPreference(requireContext())
        itemSpace = SpacesItemDecoration(50)

        initRecycler()
        initView()
//        updateView()
        mBinding.btnSetting.setOnClickListener {
            activity?.onFragmentChange(MainViewModel.GASROOMSETTINGFRAGMENT)
        }

        mBinding.btnZoomInout.setOnClickListener {
            if (!viewmodel.roomViewZoomState) {
                viewmodel.roomViewZoomState = true
                mBinding.btnZoomInout.setImageResource(R.drawable.screen_decrease_ic)
                    mBinding.gasRoomRecyclerView.apply {
                        itemSpace.changeSpace(150, 60, 150, 60)
                    }
            } else {
                viewmodel.roomViewZoomState = false
                mBinding.btnZoomInout.setImageResource(R.drawable.screen_increase_ic)
                    mBinding.gasRoomRecyclerView.apply {
                        itemSpace.changeSpace(20, 150, 20, 150)
                    }
            }
            activity?.runOnUiThread {
                recycleAdapter.notifyItemRangeChanged(0, recycleAdapter.itemCount)
            }
        }
        mBinding.btnUnit.setOnClickListener {
            for ((index, data) in viewmodel.GasRoomDataLiveList.value!!.sortedWith(
                compareBy(
                    { it.id },
                    { it.port })
            ).withIndex()) {
                data.unit++
                viewmodel.GasRoomDataLiveList.value!!.set(index, data)
                if (data.unit == 3) data.unit = 0
            }
        }
        mBinding.btnAlert.setOnClickListener {
            alertdialogFragment = viewmodel.alertDialogFragment
            val bundle = Bundle()
            bundle.putString("model", "GasRoom")
            alertdialogFragment.arguments = bundle
            alertdialogFragment.show(childFragmentManager, "GasRoom")
        }

        mBinding.btnBack.setOnClickListener {
            activity?.onFragmentChange(MainViewModel.MAINFRAGMENT)
        }
        updateAlert()

        mBinding.btnLeakTest.setOnClickListener {
            leaktestdialogFragment = LeakTestDialogFragment()
            leaktestdialogFragment.show(childFragmentManager, "GasRoom")
        }
        viewmodel.date.observe(viewLifecycleOwner) {
            mBinding.tvCurruntTime.text = it
        }
        return mBinding.root
    }

    private fun initRecycler() {
//        recycleAdapter = GasRoom_RecycleAdapter()
        mBinding.gasRoomRecyclerView.apply {

            if (!viewmodel.roomViewZoomState){
                layoutManager =
                    GridLayoutManager(context, 2, GridLayoutManager.VERTICAL, false)
                itemSpace.changeSpace(20, 150, 20, 150)
            }else{
                layoutManager =
                    GridLayoutManager(context, 2, GridLayoutManager.VERTICAL, false)
                itemSpace.changeSpace(150, 60, 150, 60)
            }
            addItemDecoration(itemSpace)

            recycleAdapter = GasRoom_RecycleAdapter()
            adapter = recycleAdapter
        }
//        (mBinding.gasRoomRecyclerView.itemAnimator as? SimpleItemAnimator)?.supportsChangeAnimations = false
        mBinding.gasRoomRecyclerView.itemAnimator = null
        mBinding.gasRoomRecyclerView.animation = null
    }

//    private var timerTaskRefresh: Timer? = null


    @SuppressLint("NotifyDataSetChanged")
    private fun initView() {
        if (!shared.loadLabNameData().isEmpty()) {
            mBinding.labNameTv.text = shared.loadLabNameData()
        }

        val mm = viewmodel.GasRoomDataLiveList.value!!.sortedWith(compareBy({ it.id }, { it.port }))

        newgasRoomViewData.clear()
        for(tmp in mm){
            if (tmp.usable)
                newgasRoomViewData.add(tmp)
        }
        gasRoomViewData.clear()
        for (tmp in newgasRoomViewData) {
            gasRoomViewData.add(tmp.copy())
        }

        recycleAdapter.submitList(newgasRoomViewData)

        if (viewmodel.roomViewZoomState) {
            mBinding.btnZoomInout.setImageResource(R.drawable.screen_decrease_ic)
        }else{
            mBinding.btnZoomInout.setImageResource(R.drawable.screen_increase_ic)
        }

        activity?.runOnUiThread {
            recycleAdapter.notifyItemRangeChanged(0, recycleAdapter.itemCount)
        }
    }

    private fun updateAlert() {
        viewmodel.gasRoomAlert.observe(viewLifecycleOwner) {
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
         * @return A new instance of fragment GasRoomMainFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            GasRoomMainFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }


}
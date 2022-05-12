package com.coai.samin_total.GasDock

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.GridLayoutManager
import com.coai.samin_total.*
import com.coai.samin_total.Dialog.AlertDialogFragment
import com.coai.samin_total.Logic.SaminSharedPreference
import com.coai.samin_total.Logic.SpacesItemDecoration
import com.coai.samin_total.Logic.Utils
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
    private val newgasStorageViewData = arrayListOf<SetGasStorageViewData>()
    private val gasStorageViewData = arrayListOf<SetGasStorageViewData>()
    private lateinit var recycleAdapter: GasStorage_RecycleAdapter
    private lateinit var onBackPressed: OnBackPressedCallback
    var sending = false
    private val mainViewModel by activityViewModels<MainViewModel>()
    lateinit var alertdialogFragment: AlertDialogFragment
    lateinit var shared: SaminSharedPreference
    var heartbeatCount: UByte = 0u
    val lockobj = object {}
    lateinit var itemSpace: SpacesItemDecoration
    private var taskRefresh: Thread? = null
    private var isOnTaskRefesh: Boolean = true

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
    lateinit var thUIError : Thread
    var isrunthUIError = true

    inner class ThreadRefresh : Thread() {
        override fun run() {
            try {
                var lastupdate: Long = System.currentTimeMillis()
                val lstvalue = mutableListOf<Int>()
                while (isOnTaskRefesh) {
                    lstvalue.clear()
                    heartbeatCount++

                    for (t in newgasStorageViewData) {
                        val idx = newgasStorageViewData.indexOf(t)
                        if (idx > -1) {
                            if (gasStorageViewData[idx].pressure != t.pressure ||
                                gasStorageViewData[idx].pressureLeft != t.pressureLeft ||
                                gasStorageViewData[idx].pressureRight != t.pressureRight ||
                                gasStorageViewData[idx].isAlert != t.isAlert ||
                                gasStorageViewData[idx].isAlertRight != t.isAlertRight ||
                                gasStorageViewData[idx].isAlertLeft != t.isAlertLeft ||
                                gasStorageViewData[idx].unit != t.unit    )
                            {
                                lstvalue.add(idx)
                            }

                            if ((((heartbeatCount / 10u) % 2u) == 0u) != ((((heartbeatCount - 1u )/ 10u) % 2u) == 0u)) {
                                if (t.isAlert == true ||
                                    t.isAlertLeft == true ||
                                    t.isAlertRight == true) {
                                    if (!lstvalue.contains(idx))
                                        lstvalue.add(idx)
                                }
                            }
                            t.heartbeatCount = heartbeatCount
                            gasStorageViewData[idx] = t.copy()
                        }
                    }

                    val baseTime = System.currentTimeMillis() - 1000 * 2
                    if (lastupdate < baseTime) {
                        lastupdate = System.currentTimeMillis()
                        for (t in newgasStorageViewData) {
                            val idx = newgasStorageViewData.indexOf(t)
                            gasStorageViewData[idx] = t.copy()
                        }

                        synchronized(lockobj) {
                            activity?.runOnUiThread {
                                recycleAdapter.notifyItemRangeChanged(0, recycleAdapter.itemCount)
                            }
                        }
                    }
                    else {
                        val rlist = Utils.ToIntRange(lstvalue, gasStorageViewData.size)
                        if (rlist != null) {
//                            Log.d("debug", "${lstvalue.size}")
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
    }

    override fun onResume() {
        super.onResume()
        isOnTaskRefesh = true
        taskRefresh = ThreadRefresh()
        taskRefresh?.start()
    }

    override fun onPause() {
        super.onPause()
        isOnTaskRefesh = false
        taskRefresh?.interrupt()
        taskRefresh?.join()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mBinding = FragmentGasDockMainBinding.inflate(inflater, container, false)
        shared = SaminSharedPreference(requireContext())
        itemSpace = SpacesItemDecoration(50)
        initRecycler()
        initView()
        updateView()

        mBinding.btnSetting.setOnClickListener {
            activity?.onFragmentChange(MainViewModel.GASSTORAGESETTINGFRAGMENT)
        }
        mBinding.btnZoomInout.setOnClickListener {
            if (!mainViewModel.storageViewZoomState) {
                mainViewModel.storageViewZoomState = true
                mBinding.btnZoomInout.setImageResource(R.drawable.screen_decrease_ic)
                synchronized(lockobj) {
                    mBinding.gasStorageRecyclerView.apply {
                        (layoutManager as GridLayoutManager).let {
                            it.spanCount = 2
                        }
                        itemSpace.changeSpace(200, 300, 200, 300)
                    }
                }
            } else {
                mainViewModel.storageViewZoomState = false
                mBinding.btnZoomInout.setImageResource(R.drawable.screen_increase_ic)
                synchronized(lockobj) {
                    mBinding.gasStorageRecyclerView.apply {
                        (layoutManager as GridLayoutManager).let {
                            it.spanCount = 4
                        }
                        itemSpace.changeSpace(50, 100, 50, 100)
                    }
                }
            }
            synchronized(lockobj) {
                activity?.runOnUiThread {
                    recycleAdapter.notifyItemRangeChanged(0, recycleAdapter.itemCount)
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
                if (data.unit == 5) data.unit = 0
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
        val mm = mainViewModel.GasStorageDataLiveList.value!!.sortedWith(
            compareBy({ it.id },
                { it.port })
        )

        newgasStorageViewData.clear()
        for(tmp in mm) {
            newgasStorageViewData.add(tmp)
        }
//        newgasStorageViewData.addAll(mm)
        gasStorageViewData.clear()
        for(t in newgasStorageViewData) {
            gasStorageViewData.add(t.copy())
        }

        recycleAdapter.submitList(newgasStorageViewData)

        if (mainViewModel.storageViewZoomState) {
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

    @SuppressLint("NotifyDataSetChanged")
    private fun updateView() {
//        mainViewModel.GasStorageDataLiveList.observe(viewLifecycleOwner) {
//            val mm = it.sortedWith(compareBy({ it.id }, { it.port }))
//            recycleAdapter.submitList(mm)
//            recycleAdapter.notifyDataSetChanged()
//        }
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

            //아이템 높이 간격 조절
            if (!mainViewModel.storageViewZoomState) {
                layoutManager =
                    GridLayoutManager(context, 4, GridLayoutManager.VERTICAL, false)
                itemSpace.changeSpace(50, 100, 50, 100)
            } else {
                layoutManager =
                    GridLayoutManager(context, 2, GridLayoutManager.VERTICAL, false)
                itemSpace.changeSpace(200, 300, 200, 300)

            }
            addItemDecoration(itemSpace)
//            recycleAdapter.submitList(gasStorageViewData)
            adapter = recycleAdapter
        }
//        (mBinding.gasStorageRecyclerView.itemAnimator as? SimpleItemAnimator)?.supportsChangeAnimations = false
        mBinding.gasStorageRecyclerView.itemAnimator = null
        mBinding.gasStorageRecyclerView.animation = null
    }

    private fun updateAlert() {
        mainViewModel.gasStorageAlert.observe(viewLifecycleOwner) {
            if (it) {
                mBinding.btnAlert.setImageResource(R.drawable.onalert_ic)
            } else {
                mBinding.btnAlert.setImageResource(R.drawable.nonalert_ic)
            }
        }

    }
}
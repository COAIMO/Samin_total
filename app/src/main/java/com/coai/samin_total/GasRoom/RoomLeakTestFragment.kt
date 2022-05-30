package com.coai.samin_total.GasRoom

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
import com.coai.samin_total.Logic.SpacesItemDecoration
import com.coai.samin_total.Logic.Utils
import com.coai.samin_total.MainActivity
import com.coai.samin_total.MainViewModel
import com.coai.samin_total.R
import com.coai.samin_total.databinding.FragmentRoomLeakTestBinding
import com.github.mikephil.charting.data.Entry

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [RoomLeakTestFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class RoomLeakTestFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private lateinit var mBinding: FragmentRoomLeakTestBinding
    private lateinit var onBackPressed: OnBackPressedCallback
    private var activity: MainActivity? = null
    private val viewmodel by activityViewModels<MainViewModel>()
    private lateinit var recycleAdapter: GasRoomLeakTest_RecycleAdapter
    lateinit var itemSpace: SpacesItemDecoration
    private val lockobj = object {}
    private val newgasRoomViewData = arrayListOf<SetGasRoomViewData>()
    lateinit var alertdialogFragment: AlertDialogFragment
    private var taskRefresh: Thread? = null
    private var isOnTaskRefesh: Boolean = true
    var heartbeatCount: UByte = 0u
    private val gasRoomViewData = arrayListOf<SetGasRoomViewData>()

    override fun onAttach(context: Context) {
        super.onAttach(context)
        activity = getActivity() as MainActivity
        onBackPressed = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                activity!!.onFragmentChange(MainViewModel.GASROOMMAINFRAGMENT)
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
        isOnTaskRefesh = true
        taskRefresh = Thread() {
            try {
                var lastupdate: Long = System.currentTimeMillis()
                val lstvalue = mutableListOf<Int>()
                while (isOnTaskRefesh) {
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

                                val entry = Entry()
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

                        synchronized(lockobj) {
                            activity?.runOnUiThread {
                                recycleAdapter.notifyItemRangeChanged(0, recycleAdapter.itemCount)
                            }
                        }
                    }
                    else {
                        val rlist = Utils.ToIntRange(lstvalue, gasRoomViewData.size)
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
        taskRefresh?.start()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mBinding = FragmentRoomLeakTestBinding.inflate(inflater, container, false)
        itemSpace = SpacesItemDecoration(50)
        initRecycler()
        initView()
        setButtonClickEvent()
        updateAlert()
        return mBinding.root
    }

    private fun setButtonClickEvent() {
        mBinding.btnAlert.setOnClickListener {
            onClick(it)
        }
        mBinding.btnBack.setOnClickListener {
            onClick(it)
        }
        mBinding.btnUnit.setOnClickListener {
            onClick(it)
        }
        mBinding.btnZoomInout.setOnClickListener {
            onClick(it)
        }
    }

    private fun onClick(view: View) {
        when (view) {
            mBinding.btnAlert -> {
                alertdialogFragment = AlertDialogFragment()
                val bundle = Bundle()
                bundle.putString("model", "GasRoom")
                alertdialogFragment.arguments = bundle
                alertdialogFragment.show(childFragmentManager, "GasRoom")
            }
            mBinding.btnBack -> {
                activity?.onFragmentChange(MainViewModel.GASROOMMAINFRAGMENT)
            }
            mBinding.btnUnit -> {
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
            mBinding.btnZoomInout -> {
                if (!viewmodel.roomViewZoomState) {
                    viewmodel.roomViewZoomState = true
                    mBinding.btnZoomInout.setImageResource(R.drawable.screen_decrease_ic)
                    synchronized(lockobj) {
                        mBinding.gasRoomLeakTestRecyclerView.apply {
                            itemSpace.changeSpace(150, 60, 150, 60)
                        }
                    }
                } else {
                    viewmodel.roomViewZoomState = false
                    mBinding.btnZoomInout.setImageResource(R.drawable.screen_increase_ic)
                    synchronized(lockobj) {
                        mBinding.gasRoomLeakTestRecyclerView.apply {
                            itemSpace.changeSpace(10, 150, 10, 150)
                        }
                    }
                }
                synchronized(lockobj) {
                    activity?.runOnUiThread {
                        recycleAdapter.notifyItemRangeChanged(0, recycleAdapter.itemCount)
                    }
                }
            }
        }
    }

    private fun initRecycler() {
        mBinding.gasRoomLeakTestRecyclerView.apply {
            if (!viewmodel.roomViewZoomState) {
                layoutManager =
                    GridLayoutManager(context, 1, GridLayoutManager.VERTICAL, false)
                itemSpace.changeSpace(10, 150, 10, 150)
            } else {
                layoutManager =
                    GridLayoutManager(context, 1, GridLayoutManager.VERTICAL, false)
                itemSpace.changeSpace(150, 60, 150, 60)
            }
            addItemDecoration(itemSpace)

            recycleAdapter = GasRoomLeakTest_RecycleAdapter()
            adapter = recycleAdapter
        }
        mBinding.gasRoomLeakTestRecyclerView.itemAnimator = null
        mBinding.gasRoomLeakTestRecyclerView.animation = null
    }

    private fun initView() {
        val mm = viewmodel.GasRoomDataLiveList.value!!.sortedWith(
            compareBy({ it.id },
                { it.port })
        )
        newgasRoomViewData.clear()
        val testData = mm.filter {
            it.leakTest
        }
        for (tmp in testData) {
            if (tmp.usable)
                newgasRoomViewData.add(tmp)
        }
        gasRoomViewData.clear()
        for (tmp in newgasRoomViewData) {
            gasRoomViewData.add(tmp.copy())
        }
        recycleAdapter.setLeakTestTime(viewmodel.isLeakTestTime)
        recycleAdapter.submitList(newgasRoomViewData)

        if (viewmodel.roomViewZoomState) {
            mBinding.btnZoomInout.setImageResource(R.drawable.screen_decrease_ic)
        } else {
            mBinding.btnZoomInout.setImageResource(R.drawable.screen_increase_ic)
        }

        synchronized(lockobj) {
            activity?.runOnUiThread {
                recycleAdapter.notifyItemRangeChanged(0, recycleAdapter.itemCount)
            }
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

    override fun onPause() {
        super.onPause()
        isOnTaskRefesh = false
        taskRefresh?.interrupt()
        taskRefresh?.join()
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment RoomLeakTestFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            RoomLeakTestFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}
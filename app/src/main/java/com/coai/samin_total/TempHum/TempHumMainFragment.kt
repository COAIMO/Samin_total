package com.coai.samin_total.TempHum

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
import com.coai.samin_total.databinding.FragmentTempHumMainBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [TempHumMainFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class TempHumMainFragment : Fragment() {
    private var param1: String? = null
    private var param2: String? = null
    private lateinit var mBinding: FragmentTempHumMainBinding
    private lateinit var onBackPressed: OnBackPressedCallback
    private var activity: MainActivity? = null
    private val viewmodel by activityViewModels<MainViewModel>()
    lateinit var alertdialogFragment: AlertDialogFragment
    lateinit var shared: SaminSharedPreference
    var heartbeatCount: UByte = 0u
//    val lockobj = object {}
    lateinit var itemSpace: SpacesItemDecoration
//    private var taskRefresh: Thread? = null
//    private var isOnTaskRefesh: Boolean = true
    private lateinit var recycleAdapter: TempHum_RecycleAdapter

    private val temphumViewData = arrayListOf<SetTempHumViewData>()
    private val newtemphumViewData = arrayListOf<SetTempHumViewData>()

    private var isOnTaskRefesh = AtomicBoolean(true)
    private var updateJob: Job? = null

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onResume() {
        super.onResume()
        /*isOnTaskRefesh = true
        taskRefresh = Thread() {
            try {
                var lastupdate: Long = System.currentTimeMillis()
                val lstvalue = mutableListOf<Int>()
                while (isOnTaskRefesh) {
                    lstvalue.clear()
                    heartbeatCount++

                    for (t in newtemphumViewData) {
                        val idx = newtemphumViewData.indexOf(t)
                        if (idx > -1) {


                            if (temphumViewData[idx].unit != t.unit ||
                                temphumViewData[idx].isTempAlert != t.isTempAlert ||
                                temphumViewData[idx].isHumAlert != t.isHumAlert||
                                temphumViewData[idx].temp != t.temp ||
                                temphumViewData[idx].hum != t.hum
                            ) {
                                if (!lstvalue.contains(idx))
                                    lstvalue.add(idx)
                            }

                            if ((((heartbeatCount / 10u) % 2u) == 0u) != ((((heartbeatCount - 1u) / 10u) % 2u) == 0u)) {
                                if (t.isTempAlert || t.isHumAlert) {
                                    if (!lstvalue.contains(idx))
                                        lstvalue.add(idx)
                                }
//                                else if (temphumViewData[idx].isTemp == 0) {
//                                    if (!lstvalue.contains(idx))
//                                        lstvalue.add(idx)
//                                }
                            }

                            t.heartbeatCount = heartbeatCount
                            temphumViewData[idx] = t.copy()
                        }
                    }

                    val baseTime = System.currentTimeMillis() - 1000 * 2
                    if (lastupdate < baseTime) {
                        lastupdate = System.currentTimeMillis()
                        for (t in newtemphumViewData) {
                            val idx = newtemphumViewData.indexOf(t)
                            temphumViewData[idx] = t.copy()
                        }

                        synchronized(lockobj) {
                            activity?.runOnUiThread {
                                recycleAdapter.notifyItemRangeChanged(0, recycleAdapter.itemCount)
                            }
                        }
                    } else {
                        val rlist = Utils.ToIntRange(lstvalue, temphumViewData.size)
                        if (rlist != null) {
//                            Log.d("debug", "${rlist.size}")
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
        taskRefresh?.start()*/
    }

    override fun onPause() {
        super.onPause()
/*        isOnTaskRefesh = false
        taskRefresh?.interrupt()
        taskRefresh?.join()*/

        activity?.shared?.setFragment(MainViewModel.TEMPHUMMAINFRAGMENT)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mBinding = FragmentTempHumMainBinding.inflate(inflater, container, false)
        shared = SaminSharedPreference(requireContext())
        itemSpace = SpacesItemDecoration(50)
        initRecycler()
        initView()

        mBinding.btnSetting.setOnClickListener {
            activity?.onFragmentChange(MainViewModel.TEMPHUMSETTINGFRAGMENT)
        }

        mBinding.btnZoomInout.setOnClickListener {
            if (!viewmodel.tempHumViewZoomState) {
                viewmodel.tempHumViewZoomState = true
                mBinding.btnZoomInout.setImageResource(R.drawable.screen_decrease_ic)
                mBinding.tempHumRecyclerView.apply {
                    (layoutManager as GridLayoutManager).let {
                        it.spanCount = 2
                    }
                    itemSpace.changeSpace(180, 150, 180, 150)
                }
            } else {
                viewmodel.tempHumViewZoomState = false
                mBinding.btnZoomInout.setImageResource(R.drawable.screen_increase_ic)
                mBinding.tempHumRecyclerView.apply {
                    (layoutManager as GridLayoutManager).let {
                        it.spanCount = 4
                    }
                    itemSpace.changeSpace(80, 50, 50, 50)
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
            bundle.putString("model", "TempHum")
            alertdialogFragment.arguments = bundle
            alertdialogFragment.show(childFragmentManager, "TempHum")
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
        mBinding.tempHumRecyclerView.apply {
            if (!viewmodel.tempHumViewZoomState) {
                layoutManager =
                    GridLayoutManager(context, 4, GridLayoutManager.VERTICAL, false)
                itemSpace.changeSpace(80, 50, 50, 50)
            } else {
                layoutManager =
                    GridLayoutManager(context, 2, GridLayoutManager.VERTICAL, false)
                itemSpace.changeSpace(180, 150, 180, 150)
            }
            addItemDecoration(itemSpace)
            recycleAdapter = TempHum_RecycleAdapter()
            adapter = recycleAdapter
        }
        mBinding.tempHumRecyclerView.itemAnimator = null
        mBinding.tempHumRecyclerView.animation = null
    }

    private fun initView() {
        if (!shared.loadLabNameData().isEmpty()) {
            mBinding.labNameTv.text = shared.loadLabNameData()
        }
        val mm =
            viewmodel.TempHumDataLiveList.value!!.sortedWith(compareBy({ it.id }, { it.port }))
        newtemphumViewData.clear()
        for (tmp in mm)
            if (tmp.usable)
                newtemphumViewData.add(tmp)

        temphumViewData.clear()
        for (t in newtemphumViewData)
            temphumViewData.add(t.copy())

        recycleAdapter.submitList(newtemphumViewData)

        if (viewmodel.tempHumViewZoomState) {
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

    private fun updateAlert() {
        viewmodel.tempHumAlert.observe(viewLifecycleOwner) {
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
         * @return A new instance of fragment TempHumMainFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            TempHumMainFragment().apply {
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
                    for (t in newtemphumViewData) {
                        val idx = newtemphumViewData.indexOf(t)
                        if (idx > -1) {


                            if (temphumViewData[idx].unit != t.unit ||
                                temphumViewData[idx].isTempAlert != t.isTempAlert ||
                                temphumViewData[idx].isHumAlert != t.isHumAlert||
                                temphumViewData[idx].temp != t.temp ||
                                temphumViewData[idx].hum != t.hum
                            ) {
                                if (!lstvalue.contains(idx))
                                    lstvalue.add(idx)
                            }

                            if ((((heartbeatCount / 10u) % 2u) == 0u) != ((((heartbeatCount - 1u) / 10u) % 2u) == 0u)) {
                                if (t.isTempAlert || t.isHumAlert) {
                                    if (!lstvalue.contains(idx))
                                        lstvalue.add(idx)
                                }
                            }

                            t.heartbeatCount = heartbeatCount
                            temphumViewData[idx] = t.copy()
                        }
                    }

                    val baseTime = System.currentTimeMillis() - 1000 * 2
                    if (lastupdate < baseTime) {
                        lastupdate = System.currentTimeMillis()
                        for (t in newtemphumViewData) {
                            val idx = newtemphumViewData.indexOf(t)
                            temphumViewData[idx] = t.copy()
                        }

                        recycleAdapter.notifyItemRangeChanged(0, recycleAdapter.itemCount)
                    } else {
                        Utils.ToIntRange(lstvalue, temphumViewData.size)?.forEach {
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
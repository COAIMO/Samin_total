package com.coai.samin_total.GasRoom

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.*
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.GridLayout
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.activityViewModels
import com.coai.samin_total.CustomView.LeakTestView
import com.coai.samin_total.Dialog.AlertDialogFragment
import com.coai.samin_total.Logic.SpacesItemDecoration
import com.coai.samin_total.MainActivity
import com.coai.samin_total.MainViewModel
import com.coai.samin_total.R
import com.coai.samin_total.databinding.FragmentRoomLeakTestBinding
import com.github.mikephil.charting.data.Entry
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStreamWriter

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
    private val graphData = arrayListOf<Entry>()
    val END_LEAKTEST = 1
    val CREATE_FILE = 2
    override fun onAttach(context: Context) {
        super.onAttach(context)
        activity = getActivity() as MainActivity
        onBackPressed = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (viewmodel.isSaveLeakTestData) {
                    val msg = handler.obtainMessage(
                        END_LEAKTEST
                    )
                    handler.sendMessage(msg)
                } else {
                    activity?.onFragmentChange(MainViewModel.GASROOMMAINFRAGMENT)
                }
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(this, onBackPressed)
    }

    override fun onDetach() {
        super.onDetach()
        if (!viewmodel.GasRoomDataLiveList.value.isNullOrEmpty()) {
            for (i in viewmodel.GasRoomDataLiveList.value!!) {
                i.leakTest = false
            }
        }
        inVisibleView = null
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
                var startmill: Long = System.currentTimeMillis()
                var testTime: Float = 0f

                while (isOnTaskRefesh) {
                    lstvalue.clear()
                    heartbeatCount++
                    try {

                        for (t in gasRoomViewData) {
                            var idx = gasRoomViewData.indexOf(t)
                            if (idx > -1) {
                                if (newgasRoomViewData[idx].pressure != t.pressure ||
                                    newgasRoomViewData[idx].isAlert != t.isAlert ||
                                    newgasRoomViewData[idx].unit != t.unit ||
                                    newgasRoomViewData[idx].isSlopeAlert != t.isSlopeAlert ||
                                    newgasRoomViewData[idx].isPressAlert != t.isPressAlert
                                ) {
                                    if (!lstvalue.contains(idx))
                                        lstvalue.add(idx)
                                }

                                if ((((heartbeatCount / 10u) % 2u) == 0u) != ((((heartbeatCount - 1u) / 10u) % 2u) == 0u)) {
                                    if (t.isAlert) {
                                        if (!lstvalue.contains(idx))
                                            lstvalue.add(idx)
                                    }
                                }
                                newgasRoomViewData[idx].heartbeatCount = heartbeatCount
                            }
                        }

                        for (t in lstvalue) {
                            if (gasRoomViewData[t].pressure != newgasRoomViewData[t].pressure)
                                gasRoomViewData[t].pressure = newgasRoomViewData[t].pressure

                            if (gasRoomViewData[t].isSlopeAlert != newgasRoomViewData[t].isSlopeAlert)
                                gasRoomViewData[t].isSlopeAlert = newgasRoomViewData[t].isSlopeAlert

                            if (gasRoomViewData[t].isPressAlert != newgasRoomViewData[t].isPressAlert)
                                gasRoomViewData[t].isPressAlert = newgasRoomViewData[t].isPressAlert

                            if (gasRoomViewData[t].isAlert != newgasRoomViewData[t].isAlert)
                                gasRoomViewData[t].isAlert = newgasRoomViewData[t].isAlert

                            if (gasRoomViewData[t].unit != newgasRoomViewData[t].unit)
                                gasRoomViewData[t].unit = newgasRoomViewData[t].unit


                            gasRoomViewData[t].heartbeatCount = newgasRoomViewData[t].heartbeatCount
                            leakTestview_data_Hashmap[t]?.bind(gasRoomViewData[t])
                        }

                        testTime = ((System.currentTimeMillis() - startmill) / 1000f).toFloat()
                        if (lastupdate <= System.currentTimeMillis() - 200) {
                            if (viewmodel.isLeakTestTime * 60 > testTime) {
                                for (t in leakTestview_data_Hashmap) {
                                    t.value.addEntry(testTime, gasRoomViewData[t.key].pressure)
                                }
                            }
                            lastupdate = System.currentTimeMillis()
                        }
                    } catch (ex: Exception) {
                        ex.printStackTrace()
                    }


//                    for (t in leakTestview_data_Hashmap) {
////                        Log.d(
////                            "leak",
////                            "${t.value.graphData.dataSets}"
////                        )
//                        if ((((heartbeatCount / 10u) % 2u) == 0u) != ((((heartbeatCount - 1u) / 10u) % 2u) == 0u)) {
//                            if (t.key.isAlert) {
//
//                            }
//                        }
//                        testTime = ((System.currentTimeMillis() - startmill) / 1000).toFloat()
//                        //t.key.heartbeatCount = heartbeatCount
////                        synchronized(lockobj) {
//////                            activity?.runOnUiThread() {
////                                t.value.bind(t.key)
////                                if (viewmodel.isLeakTestTime * 60 > testTime) {
////                                    t.value.addEntry(testTime, t.key.pressure)
////                                }
//////                            }
////                        }
//
//                    }
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
        initGridLayout()
        initView()
        setButtonClickEvent()
        updateAlert()
        viewmodel.date.observe(viewLifecycleOwner) {
            mBinding.tvCurruntTime.text = it
        }
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
                if (viewmodel.isSaveLeakTestData) {
                    val msg = handler.obtainMessage(
                        END_LEAKTEST
                    )
                    handler.sendMessage(msg)
                } else {
                    activity?.onFragmentChange(MainViewModel.GASROOMMAINFRAGMENT)
                }
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
                zoomInAndOut(viewmodel.roomViewZoomState)
//                mBinding.btnZoomInout.isEnabled = false
//                if (!viewmodel.roomViewZoomState) {
//                    viewmodel.roomViewZoomState = true
//                    mBinding.btnZoomInout.setImageResource(R.drawable.screen_decrease_ic)
//                    synchronized(lockobj) {
//                        activity?.runOnUiThread {
//                            mBinding.gasRoomLeakTestGridLayout.apply {
//                                removeAllViews()
//                                columnCount = 1
//                            }
//                            mBinding.gasRoomLeakTestGridLayout.invalidate()
//                        }
//                    }
//                } else {
//                    viewmodel.roomViewZoomState = false
//                    mBinding.btnZoomInout.setImageResource(R.drawable.screen_increase_ic)
//                    synchronized(lockobj) {
//                        activity?.runOnUiThread {
//                            mBinding.gasRoomLeakTestGridLayout.apply {
//                                removeAllViews()
//                                columnCount = 2
//                            }
//                            mBinding.gasRoomLeakTestGridLayout.invalidate()
//                        }
//                    }
//                }
//                synchronized(lockobj) {
//                    activity?.runOnUiThread {
//                        for (i in leakTestview_data_Hashmap.toSortedMap(
//                            compareBy({ it.id },
//                                { it.port })
//                        )) {
//                            val param = i.value.layoutParams as GridLayout.LayoutParams
//                            param.rowSpec = GridLayout.spec(GridLayout.UNDEFINED, 1, 1f)
//                            param.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1, 1f)
//                            mBinding.gasRoomLeakTestGridLayout.addView(i.value)
//                        }
//                        mBinding.btnZoomInout.isEnabled = true
//                    }
//                }
            }
        }
    }

    private fun zoomInAndOut(zoomState: Boolean) {
        synchronized(lockobj) {
            activity?.runOnUiThread {
                mBinding.gasRoomLeakTestGridLayout.removeAllViews()
                mBinding.gasRoomLeakTestGridLayout.invalidate()
            }
            activity?.runOnUiThread {
                mBinding.gasRoomLeakTestGridLayout.removeAllViews()
                mBinding.gasRoomLeakTestGridLayout.invalidate()
            }
        }

        if (zoomState) {
            viewmodel.roomViewZoomState = false
            mBinding.btnZoomInout.setImageResource(R.drawable.screen_increase_ic)
            synchronized(lockobj) {
                activity?.runOnUiThread {
                    mBinding.gasRoomLeakTestGridLayout.apply {
                        columnCount = 2
                    }
                }
            }
        } else {
            viewmodel.roomViewZoomState = true
            mBinding.btnZoomInout.setImageResource(R.drawable.screen_decrease_ic)
            synchronized(lockobj) {
                activity?.runOnUiThread {
                    mBinding.gasRoomLeakTestGridLayout.apply {
                        columnCount = 1
                    }
                }
            }
        }
        synchronized(lockobj) {
            val sortmap = leakTestview_data_Hashmap.toSortedMap(
                compareBy { it }
            )
            activity?.runOnUiThread {
                for (i in sortmap) {
                    val param = i.value.layoutParams as GridLayout.LayoutParams
                    param.rowSpec = GridLayout.spec(GridLayout.UNDEFINED, 1, 1f)
                    param.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1, 1f)
                    mBinding.gasRoomLeakTestGridLayout.addView(i.value)
                }

                mBinding.btnZoomInout.isEnabled = true
            }
        }
    }

    private fun initGridLayout() {
        activity?.runOnUiThread {
            mBinding.gasRoomLeakTestGridLayout.apply {
                if (!viewmodel.roomViewZoomState) {
                    columnCount = 2
                } else {
                    columnCount = 1
                }
            }

        }
    }

    val leakTestview_data_Hashmap = HashMap<Int, LeakTestView>()
    var inVisibleView: LeakTestView? = null
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
            val temp = tmp.copy()
            temp.pressure = -1f
            temp.isAlert = false
            gasRoomViewData.add(temp)
        }
        //
        graphData.clear()

        if (gasRoomViewData.isNotEmpty() && gasRoomViewData.size < 2) {
            for ((index, value) in gasRoomViewData.withIndex()) {
                graphData.add(index, Entry(0f, value.pressure))
                val testView = LeakTestView(requireContext())
                val params = GridLayout.LayoutParams().apply {
                    height = 500
                    width = GridLayout.LayoutParams.WRAP_CONTENT
                    columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1, 1f)
                    rowSpec = GridLayout.spec(GridLayout.UNDEFINED, 1, 1f)
                }
                testView.layoutParams = params
                mBinding.gasRoomLeakTestGridLayout.addView(testView)
                leakTestview_data_Hashmap.put(index, testView)
            }
            inVisibleView = LeakTestView(requireContext())
            val params = GridLayout.LayoutParams().apply {
                height = 500
                width = GridLayout.LayoutParams.WRAP_CONTENT
                columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1, 1f)
                rowSpec = GridLayout.spec(GridLayout.UNDEFINED, 1, 1f)
            }
            inVisibleView!!.layoutParams = params
            inVisibleView!!.visibility = View.INVISIBLE
            mBinding.gasRoomLeakTestGridLayout.addView(inVisibleView)
            leakTestview_data_Hashmap.put(1, inVisibleView!!)

        } else {
            for ((index, value) in gasRoomViewData.withIndex()) {
                graphData.add(index, Entry(0f, value.pressure))
                val testView = LeakTestView(requireContext())
                val params = GridLayout.LayoutParams().apply {
                    height = 500
                    width = GridLayout.LayoutParams.WRAP_CONTENT
                    columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1, 1f)
                    rowSpec = GridLayout.spec(GridLayout.UNDEFINED, 1, 1f)
                }
//            params.height = 500
//            params.width = GridLayout.LayoutParams.WRAP_CONTENT
//            params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1, 1f)
//            params.rowSpec = GridLayout.spec(GridLayout.UNDEFINED, 1, 1f)
                testView.layoutParams = params
                mBinding.gasRoomLeakTestGridLayout.addView(testView)
                leakTestview_data_Hashmap.put(index, testView)
            }
        }


        if (viewmodel.roomViewZoomState) {
            mBinding.btnZoomInout.setImageResource(R.drawable.screen_decrease_ic)
        } else {
            mBinding.btnZoomInout.setImageResource(R.drawable.screen_increase_ic)
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

    val handler = object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                END_LEAKTEST -> {
                    isOnTaskRefesh = false
                    taskRefresh?.interrupt()
                    taskRefresh?.join()
                    if (viewmodel.isSaveLeakTestData) {
                        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                            addCategory(Intent.CATEGORY_OPENABLE)
                            type = "text/csv"
                            putExtra(Intent.EXTRA_TITLE, "leaktest.csv")
                        }
                        startActivityForResult(intent, CREATE_FILE)
                    }
//                    activity?.onFragmentChange(MainViewModel.GASROOMMAINFRAGMENT)
                }
            }
        }
    }


    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == CREATE_FILE && resultCode == Activity.RESULT_OK) {
            GlobalScope.launch {
                data?.data?.also {
                    alterDocument(it)

                }
            }
        }
        activity?.onFragmentChange(MainViewModel.GASROOMMAINFRAGMENT)
    }
    //lateinit var datas: List<ILineDataSet>

    private fun alterDocument(uri: Uri) {
        try {
            val contentResolver = requireContext().applicationContext.contentResolver

            contentResolver.openFileDescriptor(uri, "w")?.use {
                val tmp = OutputStreamWriter(FileOutputStream(it.fileDescriptor), "UTF-8")
                tmp.write("\uFEFF")
                tmp.write(String.format("id, port, 시간, 압력(psi)\n"))
                for (i in leakTestview_data_Hashmap.toSortedMap(
                    compareBy { it })
                ) {
//                    val datas = i.value.graphData.dataSets
                    val datas = i.value.graphMap.toSortedMap(compareBy { it })
                    for (t in datas) {
                        val keydata = gasRoomViewData[i.key]

                        tmp.write(String.format("${keydata.id},${keydata.port},${t.key}, ${t.value}\n"))
                    }

                }
                tmp.flush()
            }
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
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
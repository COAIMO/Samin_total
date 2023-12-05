package com.coai.samin_total.GasRoom

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.*
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.GridLayout
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.coai.samin_total.CustomView.LeakTestView
import com.coai.samin_total.Dialog.AlertDialogFragment
import com.coai.samin_total.Logic.SpacesItemDecoration
import com.coai.samin_total.Logic.Utils
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
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.collections.ArrayList

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
//    private var isOnTaskRefesh: Boolean = true
    private val isOnTaskRefesh = AtomicBoolean(true)

    private val gasRoomViewData = arrayListOf<SetGasRoomViewData>()
//    private val _graphData = arrayListOf<ChartDatas>()
    private val graphData = CopyOnWriteArrayList<ChartDatas>()
    val END_LEAKTEST = 1
    val CREATE_FILE = 2
    override fun onAttach(context: Context) {
        super.onAttach(context)
        activity = getActivity() as MainActivity
        onBackPressed = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (viewmodel.isSaveLeakTestData.get()) {
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
//        inVisibleView = null

        graphData.clear()
        newgasRoomViewData.clear()
        gasRoomViewData.clear()
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
//        for (t in leakTestview_data_Hashmap){
//            t.value.procTimeLimit(viewmodel.isLeakTestTime)
//        }
        recycleAdapter.setLeakTestTime(viewmodel.isLeakTestTime)

//        isOnTaskRefesh = true
        isOnTaskRefesh.set(true)
        taskRefresh = Thread() {
            try {
                var lastupdate: Long = System.currentTimeMillis()
                var lastupdate2: Long = System.currentTimeMillis()
                val lstvalue = mutableListOf<Int>()
                var startmill: Long = System.currentTimeMillis()
                var testTime: Float
                val collectTime: Int = Math.max(1000f, viewmodel.isLeakTestTime * 60 * 1000 / 600f).toInt()

//                val hashmapPsi = hashMapOf<Int, ArrayList<PSIData>>()
//                var firstChk = true
                var heartbeatCount: UByte = 0u

                while (isOnTaskRefesh.get()) {
                    lstvalue.clear()
                    heartbeatCount++
                    try {
                        for (t in newgasRoomViewData) {
                            val idx = newgasRoomViewData.indexOf(t)
                            val tmp = gasRoomViewData[idx]
//
                            if (idx > -1) {
                                if (tmp.pressure != t.pressure ||
                                    tmp.isAlert != t.isAlert ||
                                    tmp.unit != t.unit ||
                                    tmp.isSlopeAlert != t.isSlopeAlert ||
                                    tmp.isPressAlert != t.isPressAlert
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
                                t.heartbeatCount = heartbeatCount
                                gasRoomViewData[idx] = t.copy()
                            }
                        }

                        testTime = ((System.currentTimeMillis() - startmill) / 1000f)
                        if (lastupdate2 <= System.currentTimeMillis() - collectTime) {
                            if (viewmodel.isLeakTestTime * 60 > testTime) {
                                for (t in graphData) {
                                    val idx = graphData.indexOf(t)
                                    t.data.add(
                                        Entry(
                                            testTime,
                                            newgasRoomViewData[idx].pressure
                                        )
                                    )
                                }
                            }
                            lastupdate2 = System.currentTimeMillis()
                        }

                        val baseTime = System.currentTimeMillis() - 1000 * 2
                        if (lastupdate < baseTime) {
                            lastupdate = System.currentTimeMillis()
                            for (t in newgasRoomViewData) {
                                val idx = newgasRoomViewData.indexOf(t)
                                gasRoomViewData[idx] = t.copy()
                            }

//                            synchronized(lockobj) {
                                activity?.runOnUiThread {
                                    recycleAdapter.notifyItemRangeChanged(0, recycleAdapter.itemCount)
                                }
//                            }
                        }
                        else {


                            val rlist = Utils.ToIntRange(lstvalue, gasRoomViewData.size)
                            if (rlist != null) {
//                                synchronized(lockobj) {
                                    activity?.runOnUiThread() {
                                        rlist.forEach {
                                            recycleAdapter.notifyItemRangeChanged(
                                                it.lower,
                                                1 + it.upper - it.lower
                                            )
                                        }
                                    }
//                                }
                            } else {

                            }
                        }
                    } catch (ex: Exception) {
                        ex.printStackTrace()
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
//        initGridLayout()
        initRecycler()
        initView()
        setButtonClickEvent()
        updateAlert()
        viewmodel.date.observe(viewLifecycleOwner) {
            mBinding.tvCurruntTime.text = it
        }
        return mBinding.root
    }

    private fun initRecycler() {
        mBinding.gasRoomLeakTestRecyclerView.apply {

            if (!viewmodel.roomViewZoomState){
                layoutManager =
                    GridLayoutManager(context, 2, GridLayoutManager.VERTICAL, false)
//                itemSpace.changeSpace(20, 20, 20, 20)
//                itemSpace.changeSpace(150, 60, 150, 60)
            }else{
                layoutManager =
                    GridLayoutManager(context, 1, GridLayoutManager.VERTICAL, false)
//                itemSpace.changeSpace(150, 60, 150, 60)
            }
            itemSpace.changeSpace(60, 60, 60, 60)
            addItemDecoration(itemSpace)

            recycleAdapter = GasRoomLeakTest_RecycleAdapter()
            adapter = recycleAdapter
        }
//        (mBinding.gasRoomRecyclerView.itemAnimator as? SimpleItemAnimator)?.supportsChangeAnimations = false
        mBinding.gasRoomLeakTestRecyclerView.itemAnimator = null
        mBinding.gasRoomLeakTestRecyclerView.animation = null
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
                if (viewmodel.isSaveLeakTestData.get()) {
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
            }
        }
    }

    private fun zoomInAndOut(zoomState: Boolean) {
//        synchronized(lockobj) {
            mBinding.gasRoomLeakTestRecyclerView.apply {
                if (zoomState) {
                    mBinding.btnZoomInout.setImageResource(R.drawable.screen_increase_ic)

                    viewmodel.roomViewZoomState = false
                    layoutManager =
                        GridLayoutManager(context, 2, GridLayoutManager.VERTICAL, false)
                }
                else {
                    mBinding.btnZoomInout.setImageResource(R.drawable.screen_decrease_ic)
                    viewmodel.roomViewZoomState = true
                    layoutManager =
                        GridLayoutManager(context, 1, GridLayoutManager.VERTICAL, false)
                }
            }
//        }
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
        graphData.clear()
        for (tmp in newgasRoomViewData) {
            val temp = tmp.copy()
            temp.pressure = -1f
            temp.isAlert = false
            gasRoomViewData.add(temp)

            var tmplist = CopyOnWriteArrayList<Entry>()
            tmplist.add(0, Entry(0f, tmp.pressure))
            graphData.add(ChartDatas(tmplist))
        }

        recycleAdapter.submitList(newgasRoomViewData)
        recycleAdapter.setEntry(graphData)

        if (viewmodel.roomViewZoomState) {
            mBinding.btnZoomInout.setImageResource(R.drawable.screen_decrease_ic)
        } else {
            mBinding.btnZoomInout.setImageResource(R.drawable.screen_increase_ic)
        }

//        synchronized(lockobj) {
            activity?.runOnUiThread {
                recycleAdapter.notifyItemRangeChanged(0, recycleAdapter.itemCount)
            }
//        }
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
//        isOnTaskRefesh = false
        isOnTaskRefesh.set(false)
        taskRefresh?.interrupt()
        taskRefresh?.join()
    }

    val handler = object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                END_LEAKTEST -> {
//                    isOnTaskRefesh = false
                    isOnTaskRefesh.set(false)
                    taskRefresh?.interrupt()
                    taskRefresh?.join()

                    if (viewmodel.isSaveLeakTestData.get()) {
                        val current = LocalDateTime.now()
                        val formatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")
                        val formatted = current.format(formatter)
                        val fileName = "leaktest_${formatted}.csv"
                        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                            addCategory(Intent.CATEGORY_OPENABLE)
                            type = "text/csv"
                            putExtra(Intent.EXTRA_TITLE, fileName)
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

                activity?.onFragmentChange(MainViewModel.GASROOMMAINFRAGMENT)
            }
        }
        Log.d("graphData", "requestCode : ${requestCode}, resultCode : ${resultCode}")


    }
    //lateinit var datas: List<ILineDataSet>

    private fun alterDocument(uri: Uri) {
        try {
            val contentResolver = requireContext().applicationContext.contentResolver

            contentResolver.openFileDescriptor(uri, "w")?.use {
                val tmp = OutputStreamWriter(FileOutputStream(it.fileDescriptor), "UTF-8")
                tmp.write("\uFEFF")
                tmp.write(String.format("id, port, 시간, 압력(psi)\n"))

                Log.d("graphData", "size : ${graphData.size}")
                var count = 0
                var idx = 0
                for (t in graphData) {
                    try {
                        val keydata = newgasRoomViewData[idx++]
                        for (tt in t.data) {
                            tmp.write(String.format("${keydata.id},${keydata.port},${tt.x}, ${tt.y}\n"))
                            count += 1
                        }
                        tmp.flush()
                    } catch (en: java.lang.Exception) {
                        en.printStackTrace()
                        Log.e("graphData", en.toString())
                    }
                Log.d("graphData", "write size : ${count}")
                }
                tmp.flush()
            }
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
            Log.e("graphData", e.toString())
        } catch (e: IOException) {
            e.printStackTrace()
            Log.e("graphData", e.toString())
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e("graphData", e.toString())
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
package com.coai.samin_total.GasRoom

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.*
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import android.widget.GridLayout
import android.widget.LinearLayout
import androidx.activity.OnBackPressedCallback
import androidx.core.view.children
import androidx.core.view.marginStart
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.coai.samin_total.CustomView.LeakTestView
import com.coai.samin_total.Dialog.AlertDialogFragment
import com.coai.samin_total.Logic.SpacesItemDecoration
import com.coai.samin_total.Logic.Utils
import com.coai.samin_total.MainActivity
import com.coai.samin_total.MainViewModel
import com.coai.samin_total.R
import com.coai.samin_total.database.AlertData
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == CREATE_FILE && resultCode == Activity.RESULT_OK) {
            GlobalScope.launch {
                data?.data?.also {
                    alterDocument(it)
                }
            }
        }
    }

    lateinit var datas: List<Entry>

    private fun alterDocument(uri: Uri) {
        try {
            val contentResolver = requireContext().applicationContext.contentResolver
            for (i in leakTestview_data_Hashmap) {
                i.value.graphData.dataSets
                Log.d(
                    "leak",
                    "asdfasd}"
                )
            }
            contentResolver.openFileDescriptor(uri, "w")?.use {
//                FileOutputStream(it.fileDescriptor).use {
//                    it.write(datas.toString().toByteArray())
//                }
                val tmp = OutputStreamWriter(FileOutputStream(it.fileDescriptor), "UTF-8")
                tmp.write("\uFEFF" + datas.toString())
                tmp.flush()
            }
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
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
                var count: Float = 0f
                var startmill: Long = System.currentTimeMillis()
                var testTime: Float = 0f
                var isFirst = false
                while (isOnTaskRefesh) {
                    lstvalue.clear()
                    heartbeatCount++

                    if (viewmodel.isLeakTestTime * 60 < testTime) {
                        if (!isFirst){
                            val msg = handler.obtainMessage(
                                END_LEAKTEST
                            )
                            handler.sendMessage(msg)
                            isFirst = true
                        }
                        Log.d(
                            "break",
                            "걸림"
                        )
                    }else{
                        for (t in leakTestview_data_Hashmap) {
                            Log.d(
                                "leak",
                                "id:${t.key.id}\t, port:${t.key.port}\t ,pressure:${t.key.pressure}"
                            )
                            if ((((heartbeatCount / 10u) % 2u) == 0u) != ((((heartbeatCount - 1u) / 10u) % 2u) == 0u)) {
                                if (t.key.isAlert) {

                                }
                            }
                            testTime = ((System.currentTimeMillis() - startmill) / 1000).toFloat()
                            t.key.heartbeatCount = heartbeatCount
                            synchronized(lockobj) {
                                activity?.runOnUiThread() {
                                    t.value.bind(t.key)
                                    t.value.addEntry(testTime, t.key.pressure)
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
        initGridLayout()
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
                        mBinding.gasRoomLeakTestGridLayout.apply {
                            removeAllViews()
                            columnCount = 1
                        }

//                        setGridColumns(mBinding.gasRoomLeakTestGridLayout, 1)
                    }
                } else {
                    viewmodel.roomViewZoomState = false
                    mBinding.btnZoomInout.setImageResource(R.drawable.screen_increase_ic)
                    synchronized(lockobj) {
//                        mBinding.gasRoomLeakTestRecyclerView.apply {
//                            itemSpace.changeSpace(10, 150, 10, 150)
//                        }
                        mBinding.gasRoomLeakTestGridLayout.apply {
                            removeAllViews()
                            columnCount = 2
                        }
                    }
                }
                synchronized(lockobj) {
                    activity?.runOnUiThread {
                        for (i in leakTestview_data_Hashmap) {
                            val param = i.value.layoutParams as GridLayout.LayoutParams
                            param.rowSpec = GridLayout.spec(GridLayout.UNDEFINED, 1, 1f)
                            param.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1, 1f)
                            if (viewmodel.roomViewZoomState){
                                val gasroom_param = i.value.gasRoomView.layoutParams

                            }
                            mBinding.gasRoomLeakTestGridLayout.addView(i.value)
                        }
                    }
                }
            }
        }
    }


    private fun initGridLayout() {
        mBinding.gasRoomLeakTestGridLayout.apply {
            if (!viewmodel.roomViewZoomState) {
                columnCount = 2

            } else {
                columnCount = 1
            }
        }
    }

    val leakTestview_data_Hashmap = HashMap<SetGasRoomViewData, LeakTestView>()
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
        //
        graphData.clear()

        for ((index, value) in newgasRoomViewData.withIndex()) {
            graphData.add(index, Entry(0f, value.pressure))
            val testView = LeakTestView(requireContext())
            val params = GridLayout.LayoutParams().apply {

            }
            params.height = 500
            params.width = GridLayout.LayoutParams.WRAP_CONTENT
            params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1, 1f)
            params.rowSpec = GridLayout.spec(GridLayout.UNDEFINED, 1, 1f)
            testView.layoutParams = params
            mBinding.gasRoomLeakTestGridLayout.addView(testView)
            leakTestview_data_Hashmap.put(value, testView)
        }


        if (viewmodel.roomViewZoomState) {
            mBinding.btnZoomInout.setImageResource(R.drawable.screen_decrease_ic)
        } else {
            mBinding.btnZoomInout.setImageResource(R.drawable.screen_increase_ic)
        }

        synchronized(lockobj) {
            activity?.runOnUiThread {
//                recycleAdapter.notifyItemRangeChanged(0, recycleAdapter.itemCount)
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

    val handler = object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            when(msg.what){
                END_LEAKTEST ->{
                    isOnTaskRefesh = false
                    taskRefresh?.interrupt()
                    taskRefresh?.join()
                    if (viewmodel.isSaveLeakTestData){
                        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                            addCategory(Intent.CATEGORY_OPENABLE)
                            type = "application/txt"
                            putExtra(Intent.EXTRA_TITLE, "invoice.txt")
                        }
                        startActivityForResult(intent, CREATE_FILE)
                    }
                    activity?.onFragmentChange(MainViewModel.GASROOMMAINFRAGMENT)
                }
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
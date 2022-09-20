package com.coai.samin_total.Dialog

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.coai.samin_total.CustomView.SpaceDecoration
import com.coai.samin_total.MainViewModel
import com.coai.samin_total.R
import com.coai.samin_total.Service.HexDump
import com.coai.samin_total.databinding.FragmentAlertDialogBinding
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [AlertDialogFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class AlertDialogFragment : DialogFragment() {
    private var param1: String? = null
    private var param2: String? = null
    private var model: String? = null
    private lateinit var mBinding: FragmentAlertDialogBinding
    private lateinit var recycleAdapter: AlertDialog_RecyclerAdapter
    private val viewmodel by activityViewModels<MainViewModel>()
    private val alertData = mutableListOf<SetAlertData>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
            model = it.getString("model")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mBinding = FragmentAlertDialogBinding.inflate(inflater, container, false)
        initRecycler()
        initVieiw()

        return mBinding.root
    }

    override fun onResume() {
        val width: Int = resources.getDimensionPixelSize(R.dimen.DialogView_width)
        val height: Int = resources.getDimensionPixelSize(R.dimen.DialogView_height)
        dialog?.window?.setLayout(width, height)
        dialog?.window?.setBackgroundDrawableResource(R.drawable.border_layout)

        super.onResume()
    }

    private fun initVieiw() {
        when (model) {
            "Main" -> {
                mBinding.tvTitle.setText(R.string.title_event_log)
//                for ((key, value) in viewmodel.alertMap) {
//                    val aqInfo = HexDump.toByteArray(key)
//                    val portNum = aqInfo[1]
//                    val id = aqInfo[2]
//                    val model = aqInfo[3]
//
//                    if (value.isAlert) {
//                        alertData.add(value)
//                        recycleAdapter.submitList(alertData)
//                    }
//                }
                viewmodel.popUpDataLiveList.observe(viewLifecycleOwner) {
                    recycleAdapter.submitList(it)
                    recycleAdapter.notifyDataSetChanged()
                }
            }
            "GasStorage" -> {
                mBinding.tvTitle.setText(R.string.title_gasstorage_event_log)
//                for ((key, value) in viewmodel.alertMap) {
//                    val aqInfo = HexDump.toByteArray(key)
//                    val model = aqInfo[3]
//                    if (model == 1.toByte()) {
//                        if (value.isAlert) {
//                            alertData.add(value)
//                            recycleAdapter.submitList(alertData)
//                        }
//                    }
//                }
                viewmodel.popUpDataLiveList.observe(viewLifecycleOwner) {
                    val item = it.filter {
                        it.model == 1
                    }
                    recycleAdapter.submitList(item)
                    recycleAdapter.notifyDataSetChanged()
                }
            }
            "GasRoom" -> {
                mBinding.tvTitle.setText(R.string.title_gasroom_event_log)
//                for ((key, value) in viewmodel.alertMap) {
//                    val aqInfo = HexDump.toByteArray(key)
//                    val model = aqInfo[3]
//                    if (model == 2.toByte()) {
//                        if (value.isAlert) {
//                            alertData.add(value)
//                            recycleAdapter.submitList(alertData)
//                        }
//                    }
//                }
                viewmodel.popUpDataLiveList.observe(viewLifecycleOwner) {
                    val item = it.filter {
                        it.model == 2
                    }
                    recycleAdapter.submitList(item)
                    recycleAdapter.notifyDataSetChanged()
                }
            }
            "WasteLiquor" -> {
                mBinding.tvTitle.setText(R.string.title_wasteliquor_event_log)
//                for ((key, value) in viewmodel.alertMap) {
//                    val aqInfo = HexDump.toByteArray(key)
//                    val model = aqInfo[3]
//                    if (model == 3.toByte()) {
//                        if (value.isAlert) {
//                            alertData.add(value)
//                            recycleAdapter.submitList(alertData)
//                        }
//                    }
//                }
                viewmodel.popUpDataLiveList.observe(viewLifecycleOwner) {
                    val item = it.filter {
                        it.model == 3
                    }
                    recycleAdapter.submitList(item)
                    recycleAdapter.notifyDataSetChanged()
                }
            }
            "Oxygen" -> {
                mBinding.tvTitle.setText(R.string.title_oxygen_event_log)
//                for ((key, value) in viewmodel.alertMap) {
//                    val aqInfo = HexDump.toByteArray(key)
//                    val model = aqInfo[3]
//                    if (model == 4.toByte()) {
//                        if (value.isAlert) {
//                            alertData.add(value)
//                            recycleAdapter.submitList(alertData)
//                        }
//                    }
//                }

                viewmodel.popUpDataLiveList.observe(viewLifecycleOwner) {
                    val item = it.filter {
                        it.model == 4
                    }
                    recycleAdapter.submitList(item)
                    recycleAdapter.notifyDataSetChanged()
                }
            }
            "Steamer" -> {
                mBinding.tvTitle.setText(R.string.title_steamer_event_log)
//                for ((key, value) in viewmodel.alertMap) {
//                    val aqInfo = HexDump.toByteArray(key)
//                    val model = aqInfo[3]
//                    if (model == 5.toByte()) {
//                        if (value.isAlert) {
//                            alertData.add(value)
//                            recycleAdapter.submitList(alertData)
//                        }
//                    }
//                }
                viewmodel.popUpDataLiveList.observe(viewLifecycleOwner) {
                    val item = it.filter {
                        it.model == 5
                    }
                    recycleAdapter.submitList(item)
                    recycleAdapter.notifyDataSetChanged()

                }
            }
        }
//        recycleAdapter.notifyDataSetChanged()
    }

    private fun initRecycler() {
        mBinding.alertRecyclerView.apply {
            layoutManager =
                LinearLayoutManager(context)

            //아이템 높이 간격 조절
            val decoration = SpaceDecoration(20, 20)
            addItemDecoration(decoration)

            recycleAdapter = AlertDialog_RecyclerAdapter()
//            recycleAdapter.submitList(singleDockViewData)
            adapter = recycleAdapter
        }

    }

    fun getLatest_time(time: Long): String {
        val dateformat: SimpleDateFormat =
            SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale("ko", "KR"))
        val date: Date = Date(time)
        return dateformat.format(date)
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment AlertDialogFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            AlertDialogFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}
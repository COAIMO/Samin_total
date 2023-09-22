package com.coai.samin_total.Dialog

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.coai.samin_total.CustomView.SpaceDecoration
import com.coai.samin_total.Logic.Utils
import com.coai.samin_total.MainActivity
import com.coai.samin_total.MainViewModel
import com.coai.samin_total.R
import com.coai.samin_total.Service.HexDump
import com.coai.samin_total.databinding.FragmentAlertDialogBinding
import com.coai.samin_total.databinding.FragmentAlertPopUpBinding

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [AlertPopUpFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class AlertPopUpFragment : DialogFragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private lateinit var mBinding: FragmentAlertPopUpBinding
    private lateinit var recycleAdapter: AlertPopUP_RecyclerAdapter
    private val viewmodel by activityViewModels<MainViewModel>()
    private var alertData = mutableListOf<SetAlertData>()
    private var activity: MainActivity? = null
    private var taskRefresh: Thread? = null
    private var isOnTaskRefesh: Boolean = true

    override fun onAttach(context: Context) {
        super.onAttach(context)
        activity = getActivity() as MainActivity
    }

    override fun onDetach() {
        super.onDetach()
        activity = null
    }

    override fun onPause() {
        super.onPause()
        isOnTaskRefesh = false
        taskRefresh?.interrupt()
        taskRefresh?.join()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (viewmodel.alertDialogFragment.isAdded) {
            viewmodel.alertDialogFragment.dismiss()
        }
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mBinding = FragmentAlertPopUpBinding.inflate(inflater, container, false)
        initRecycler()
        initView()
        recycleAdapter.setButtonClickListener(object :
            AlertPopUP_RecyclerAdapter.OnButtonClickListener {
            override fun onClick(v: View, position: Int) {
                val aa = viewmodel.errorlivelist[position]
                when (aa.model) {
                    1 -> {
                        activity?.onFragmentChange(MainViewModel.GASDOCKMAINFRAGMENT)
                        activity?.alertPopUpFragment?.dismiss()
                    }
                    2 -> {
                        activity?.onFragmentChange(MainViewModel.GASROOMMAINFRAGMENT)
                        activity?.alertPopUpFragment?.dismiss()
                    }
                    3 -> {
                        activity?.onFragmentChange(MainViewModel.WASTELIQUORMAINFRAGMENT)
                        activity?.alertPopUpFragment?.dismiss()
                    }
                    4 -> {
                        activity?.onFragmentChange(MainViewModel.OXYGENMAINFRAGMENT)
                        activity?.alertPopUpFragment?.dismiss()
                    }
                    5 -> {
                        activity?.onFragmentChange(MainViewModel.STEAMERMAINFRAGMENT)
                        activity?.alertPopUpFragment?.dismiss()
                    }
                    6 ->{
                        activity?.onFragmentChange(MainViewModel.TEMPHUMMAINFRAGMENT)
                        activity?.alertPopUpFragment?.dismiss()
                    }
                }
            }
        })
//        viewmodel._popUpList.observe(viewLifecycleOwner){
//            Log.d("라이브","${it}")
//            alertData = it
//            recycleAdapter.submitList(alertData)
//        }
//        viewmodel.popUpDataLiveList.observe(viewLifecycleOwner) {
//            Log.d("라이브", "${it}")
////            synchronized(lockobj) {
//                recycleAdapter.submitList(it)
////            }
////            recycleAdapter.notifyDataSetChanged()
//        }
        return mBinding.root
    }

    val lockobj = object {}
    override fun onResume() {
        val width: Int = resources.getDimensionPixelSize(R.dimen.PopUpView_width)
        val height: Int = resources.getDimensionPixelSize(R.dimen.PopUpView_height)
        dialog?.window?.setLayout(width, height)
        dialog?.window?.setBackgroundDrawableResource(R.drawable.border_layout)
        super.onResume()
        taskRefresh = Thread{
            try {
//                var lastupdate: Long = System.currentTimeMillis()
//                val lstvalue = mutableListOf<Int>()
                while (isOnTaskRefesh){
                    activity?.runOnUiThread {
                        try {
                            recycleAdapter.notifyDataSetChanged()

                            val cnt = recycleAdapter.itemCount
                            if (cnt <= 0) {
                                recycleAdapter.notifyItemRemoved(0)
                            } else {
                                recycleAdapter.notifyItemRangeChanged(0, cnt)
                            }
                        } catch (ee: Exception){
                            ee.printStackTrace()
                        }
                    }

                    Thread.sleep(500)
                }
            }catch (e:Exception){
                e.printStackTrace()
            }
        }
        taskRefresh?.start()
    }

    private fun initRecycler() {
        mBinding.alertRecyclerView.apply {
            layoutManager =
                LinearLayoutManager(context)

            //아이템 높이 간격 조절
            val decoration = SpaceDecoration(40, 40)
            addItemDecoration(decoration)

            recycleAdapter = AlertPopUP_RecyclerAdapter()
            adapter = recycleAdapter

            recycleAdapter.submitList(viewmodel.errorlivelist)
        }

    }

    private fun initView() {
//        alertData.removeAll(alertData)
//        mBinding.tvTitle.setText(R.string.title_event_log)
//        for ((key, value) in viewmodel.alertMap) {
//            val aqInfo = HexDump.toByteArray(key)
//            val portNum = aqInfo[1]
//            val id = aqInfo[2]
//            val model = aqInfo[3]
//
//            if (value.isAlert) {
//                alertData.add(value)
//                recycleAdapter.submitList(alertData)
//            }
//        }
//        recycleAdapter.notifyDataSetChanged()

    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment AlertPopUpFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            AlertPopUpFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}
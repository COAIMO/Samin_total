package com.coai.samin_total.WasteLiquor

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
import com.coai.samin_total.Dialog.SetAlertData
import com.coai.samin_total.MainActivity
import com.coai.samin_total.MainViewModel
import com.coai.samin_total.R
import com.coai.samin_total.RecyclerDecoration_Height
import com.coai.samin_total.databinding.FragmentWasteLiquorMainBinding
import kotlin.concurrent.thread

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [WasteLiquorMainFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class WasteLiquorMainFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private lateinit var mBinding: FragmentWasteLiquorMainBinding
    private lateinit var recycleAdapter: WasteLiquor_RecycleAdapter
    private val wasteLiquorViewData = mutableListOf<SetWasteLiquorViewData>()
    private lateinit var onBackPressed: OnBackPressedCallback
    private var activity: MainActivity? = null
    private val viewmodel by activityViewModels<MainViewModel>()
    private lateinit var sendThread: Thread
    var sending = false
    var btn_Count = 0
    lateinit var alertdialogFragment: AlertDialogFragment
    lateinit var alertThread: Thread
    override fun onDestroyView() {
        super.onDestroyView()
        sending = false
    }

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
    }

    override fun onDetach() {
        super.onDetach()
        activity = null
        onBackPressed.remove()
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mBinding = FragmentWasteLiquorMainBinding.inflate(inflater, container, false)
        initRecycler()
        initView()
        updateView()

        mBinding.btnSetting.setOnClickListener {
            activity?.onFragmentChange(MainViewModel.WASTELIQUORSETTINGFRAGMENT)
        }

        mBinding.btnZoomInout.setOnClickListener {
            if (btn_Count % 2 == 0) {
                btn_Count++
                mBinding.wasteLiquorRecyclerView.apply {
                    layoutManager =
                        GridLayoutManager(context, 2, GridLayoutManager.VERTICAL, false)

                    //아이템 높이 간격 조절
                    val decoration_height = RecyclerDecoration_Height(25)
                    addItemDecoration(decoration_height)

//                    recycleAdapter.submitList(wasteLiquorViewData)
                    adapter = recycleAdapter
                }
            } else {
                btn_Count++
                mBinding.wasteLiquorRecyclerView.apply {
                    layoutManager =
                        GridLayoutManager(context, 4, GridLayoutManager.VERTICAL, false)

                    //아이템 높이 간격 조절
                    val decoration_height = RecyclerDecoration_Height(25)
                    addItemDecoration(decoration_height)

//                    recycleAdapter.submitList(wasteLiquorViewData)
                    adapter = recycleAdapter
                }

            }
        }

        mBinding.btnAlert.setOnClickListener {
//            udateAlert()
            alertdialogFragment = AlertDialogFragment()
            val bundle = Bundle()
            bundle.putString("model", "WasteLiquor")
            alertdialogFragment.arguments = bundle
            alertdialogFragment.show(childFragmentManager, "WasteLiquor")
            mBinding.btnAlert.setImageResource(R.drawable.nonalert_ic)
        }

        mBinding.btnBack.setOnClickListener {
            activity?.onFragmentChange(MainViewModel.MAINFRAGMENT)
        }
        udateAlert()

        return mBinding.root
    }

    private fun initRecycler() {
        mBinding.wasteLiquorRecyclerView.apply {
            layoutManager =
                GridLayoutManager(context, 4, GridLayoutManager.VERTICAL, false)

            //아이템 높이 간격 조절
            val decoration_height = RecyclerDecoration_Height(50)
            addItemDecoration(decoration_height)

            recycleAdapter = WasteLiquor_RecycleAdapter()
            adapter = recycleAdapter
        }

    }

    @SuppressLint("NotifyDataSetChanged")
    private fun initView() {
        val mm =
            viewmodel.WasteLiquorDataLiveList.value!!.sortedWith(compareBy({ it.id }, { it.port }))
        recycleAdapter.submitList(mm)
        recycleAdapter.notifyDataSetChanged()
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun updateView() {
        viewmodel.WasteLiquorDataLiveList.observe(viewLifecycleOwner) { it ->
            val mm = it.sortedWith(compareBy({ it.id }, { it.port }))
            recycleAdapter.submitList(mm)
            recycleAdapter.notifyDataSetChanged()
        }
    }


    private fun udateAlert() {
        viewmodel.wasteAlert.observe(viewLifecycleOwner) {
            if (it) {
                mBinding.btnAlert.setImageResource(R.drawable.onalert_ic)
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
         * @return A new instance of fragment WasteLiquorMainFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            WasteLiquorMainFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}
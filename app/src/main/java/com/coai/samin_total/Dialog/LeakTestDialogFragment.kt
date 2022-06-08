package com.coai.samin_total.Dialog

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.coai.samin_total.CustomView.SpaceDecoration
import com.coai.samin_total.GasRoom.SetGasRoomViewData
import com.coai.samin_total.MainActivity
import com.coai.samin_total.MainViewModel
import com.coai.samin_total.R
import com.coai.samin_total.databinding.FragmentLeakTestDialogBinding

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [LeakTestDialogFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class LeakTestDialogFragment : DialogFragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private lateinit var mBinding: FragmentLeakTestDialogBinding
    private val viewmodel by activityViewModels<MainViewModel>()
    private var activity: MainActivity? = null
    private lateinit var onBackPressed: OnBackPressedCallback

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
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(this, onBackPressed)
    }

    override fun onDetach() {
        super.onDetach()
        activity = null
        onBackPressed.remove()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mBinding = FragmentLeakTestDialogBinding.inflate(inflater, container, false)
        initRecycler()
        initView()
        setButtonClickEvent()

        recycleAdapter.setItemClickListener(object :
            LeakTest_RecycleAdapter.OnItemClickListener {
            override fun onClick(v: View, position: Int) {
                Log.d("ASF","ASDFasdf")
            }
        })
        return mBinding.root
    }

    override fun onResume() {
        val width: Int = resources.getDimensionPixelSize(R.dimen.DialogView_width)
        val height: Int = resources.getDimensionPixelSize(R.dimen.DialogView_height)
        dialog?.window?.setLayout(width, height)
        dialog?.window?.setBackgroundDrawableResource(R.drawable.border_layout)
        super.onResume()
    }

    private val setGasSensorInfo = mutableListOf<SetGasRoomViewData>()
    private fun initView() {
        if (!viewmodel.GasRoomDataLiveList.value.isNullOrEmpty()) {
            for (i in viewmodel.GasRoomDataLiveList.value!!) {
                setGasSensorInfo.add(i)
            }
            recycleAdapter.submitList(setGasSensorInfo)
        } else {
            Toast.makeText(requireContext(), "설정된 데이터가 없습니다.", Toast.LENGTH_SHORT).show()
        }
    }

    private lateinit var recycleAdapter: LeakTest_RecycleAdapter

    private fun initRecycler() {
        mBinding.LeakTestRecyclerView.apply {
            layoutManager =
                LinearLayoutManager(context)

            //아이템 높이 간격 조절
            val decoration = SpaceDecoration(20, 20)
            addItemDecoration(decoration)

            recycleAdapter = LeakTest_RecycleAdapter()

            adapter = recycleAdapter
        }
    }

    private fun setButtonClickEvent() {
        mBinding.btnSelectAll.setOnClickListener {
            onClick(it)
        }
        mBinding.btnTestStart.setOnClickListener {
            onClick(it)
        }
        mBinding.swExportData.setOnClickListener {
            onClick(it)
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun onClick(view: View) {
        when (view) {
            mBinding.btnSelectAll -> {
                for (i in recycleAdapter.gasRoomInfo){
                    i.leakTest = true
                }
                recycleAdapter.notifyDataSetChanged()
            }
            mBinding.btnTestStart -> {
                viewmodel.isLeakTestTime = mBinding.etTestTime.text.toString().toInt()
                activity?.onFragmentChange(MainViewModel.GASROOMLEAKTESTFRAGMENT)
                this.dismiss()
            }
            mBinding.swExportData ->{
                viewmodel.isSaveLeakTestData =  mBinding.swExportData.isChecked
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
         * @return A new instance of fragment LeakTestDialogFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            LeakTestDialogFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}
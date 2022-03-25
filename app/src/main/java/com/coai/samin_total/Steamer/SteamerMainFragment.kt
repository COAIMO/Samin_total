package com.coai.samin_total.Steamer

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
import com.coai.samin_total.GasRoom.GasRoom_RecycleAdapter
import com.coai.samin_total.Logic.SaminProtocol
import com.coai.samin_total.MainActivity
import com.coai.samin_total.MainViewModel
import com.coai.samin_total.Oxygen.Oxygen_RecycleAdapter
import com.coai.samin_total.Oxygen.SetOxygenViewData
import com.coai.samin_total.R
import com.coai.samin_total.RecyclerDecoration_Height
import com.coai.samin_total.databinding.FragmentSteamerMainBinding

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [SteamerMainFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class SteamerMainFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    lateinit var mBinding: FragmentSteamerMainBinding
    private lateinit var recycleAdapter: Steamer_RecycleAdapter
    private val steamerViewData = mutableListOf<SetSteamerViewData>()
    private lateinit var onBackPressed: OnBackPressedCallback
    private var activity: MainActivity? = null
    private val mainViewModel by activityViewModels<MainViewModel>()
    private lateinit var sendThread: Thread
    var sending = false

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


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mBinding = FragmentSteamerMainBinding.inflate(inflater, container, false)
        initRecycler()
        initView()
        updateView()
//        mBinding.titleTv.setOnClickListener {
//            mBinding.steamerRecyclerView.apply {
//                sending = true
//                sendThread = Thread {
//                    while (sending) {
//                        val protocol = SaminProtocol()
////                    for (i in 0..7){
//                        protocol.feedBack(MainViewModel.Steamer, 1.toByte())
//                        activity?.serialService?.sendData(protocol.mProtocol)
//                        Log.d("태그", "SendData: ${protocol.mProtocol}")
//                        Thread.sleep(200)
////                    }
//
//                    }
//
//                }
//                sendThread.start()
//            }
//        }
//
//        mBinding.steamerRecyclerView.apply {
//            steamerViewData.apply {
//                add(
//                    SetSteamerViewData(
//                        false,
//                        0,
//                        0
//                    )
//                )
//            }
//            recycleAdapter.submitList(steamerViewData)
//        }


//        mainViewModel.SteamerData.observe(viewLifecycleOwner,{
//            recycleAdapter.setSteamerViewData.set(0, it)
//            recycleAdapter.notifyDataSetChanged()
//        })

        return mBinding.root
    }


    private fun initRecycler() {
        mBinding.steamerRecyclerView.apply {
            layoutManager =
                GridLayoutManager(context, 4, GridLayoutManager.VERTICAL, false)

            //아이템 높이 간격 조절
            val decoration_height = RecyclerDecoration_Height(85)
            addItemDecoration(decoration_height)

            //페이지 넘기는 효과
//            val snapHelper = PagerSnapHelper()
//            snapHelper.attachToRecyclerView(this)

            //Indicator 추가
//            addItemDecoration(LinePagerIndicatorDecoration())


            recycleAdapter = Steamer_RecycleAdapter()
            adapter = recycleAdapter
        }

    }

    @SuppressLint("NotifyDataSetChanged")
    private fun initView() {
        if (steamerViewData.isEmpty()){
            for (i in mainViewModel.SteamerDataLiveList.value!!.sortedWith(compareBy({it.id},{it.port}))) {
                steamerViewData.add(i)
            }
            recycleAdapter.submitList(steamerViewData)
            recycleAdapter.notifyDataSetChanged()
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun updateView(){
        mainViewModel.SteamerDataLiveList.observe(viewLifecycleOwner, {
            for ((index, data) in it.sortedWith(compareBy({it.id},{it.port})).withIndex()){
                steamerViewData.set(index, data)
            }
            recycleAdapter.submitList(steamerViewData)
            recycleAdapter.notifyDataSetChanged()
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        sending = false
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment SteamerMainFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            SteamerMainFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}
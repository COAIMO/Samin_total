package com.coai.samin_total.WasteLiquor

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import com.coai.samin_total.GasRoom.GasRoom_RecycleAdapter
import com.coai.samin_total.GasRoom.SetGasRoomViewData
import com.coai.samin_total.Logic.SaminProtocol
import com.coai.samin_total.MainActivity
import com.coai.samin_total.MainViewModel
import com.coai.samin_total.Oxygen.OxygenViewModel
import com.coai.samin_total.R
import com.coai.samin_total.RecyclerDecoration_Height
import com.coai.samin_total.databinding.FragmentWasteLiquorMainBinding

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
    private val mainViewModel by activityViewModels<MainViewModel>()
    private lateinit var sendThread: Thread
    var sending = false


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

//        mBinding.titleTv.setOnClickListener {
//            mBinding.wasteLiquorRecyclerView.apply {
//                wasteLiquorViewData.apply {
//                    add(
//                        SetWasteLiquorViewData(
//                            liquidName = "폐액",
//                            isAlert = true
//                        )
//                    )
//
//                    add(
//                        SetWasteLiquorViewData(
//                            liquidName = "폐액",
//                            isAlert = false
//                        )
//                    )
//                }
//                recycleAdapter.submitList(wasteLiquorViewData)
//                recycleAdapter.notifyDataSetChanged()
//            }
//        }

        mainViewModel.LevelValue.observe(viewLifecycleOwner, Observer {
            Log.d("태그", "LevelValue:${mainViewModel.model_ID_Data}")
            Log.d("태그", "LevelValue:$it")
            if (it == 0) {
                recycleAdapter.setWasteLiquorViewData.set(0, SetWasteLiquorViewData("폐액", true))
            } else {
                recycleAdapter.setWasteLiquorViewData.set(0, SetWasteLiquorViewData("폐액", false))

            }
            recycleAdapter.notifyDataSetChanged()

        })

        mBinding.titleTv.setOnClickListener {
            sending = true
            sendThread = Thread {
                while (sending) {
                    val protocol = SaminProtocol()
//                    for (i in 0..7){
                    protocol.feedBack(MainViewModel.WasteLiquor, 1.toByte())
                    activity?.serialService?.sendData(protocol.mProtocol)
                    Log.d("태그", "SendData: ${protocol.mProtocol}")
                    Thread.sleep(200)
//                    }

                }

            }
            sendThread.start()
        }


        mBinding.wasteLiquorRecyclerView.apply {
            wasteLiquorViewData.apply {
                add(
                    SetWasteLiquorViewData(
                        isAlert = false
                    )
                )
            }
            recycleAdapter.submitList(wasteLiquorViewData)
        }


        return mBinding.root
    }

    override fun onDestroy() {
        sending = false
        super.onDestroy()
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
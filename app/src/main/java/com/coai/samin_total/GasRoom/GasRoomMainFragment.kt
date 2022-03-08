package com.coai.samin_total.GasRoom

import android.annotation.SuppressLint
import android.content.ClipData
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
import androidx.recyclerview.widget.GridLayoutManager
import com.coai.samin_total.GasDock.SetGasdockViewData
import com.coai.samin_total.Logic.SaminProtocol
import com.coai.samin_total.MainActivity
import com.coai.samin_total.MainViewModel
import com.coai.samin_total.RecyclerDecoration_Height
import com.coai.samin_total.databinding.FragmentGasRoomMainBinding

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [GasRoomMainFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class GasRoomMainFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private lateinit var mBinding: FragmentGasRoomMainBinding
    private val gasRoomViewData = mutableListOf<SetGasRoomViewData>()
    private lateinit var recycleAdapter: GasRoom_RecycleAdapter
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
        onBackPressed = object : OnBackPressedCallback(true){
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
        mBinding = FragmentGasRoomMainBinding.inflate(inflater, container, false)
        initRecycler()

        mBinding.titleTv.setOnClickListener {
            sending = true
            sendThread = Thread {
                while (sending) {
                    val protocol = SaminProtocol()
//                    for (i in 0..7){
                    protocol.feedBack(MainViewModel.GasRoom, 1.toByte())
                    activity?.serialService?.sendData(protocol.mProtocol)
                    Log.d("태그", "SendData: ${protocol.mProtocol}")
                    Thread.sleep(200)
//                    }

                }

            }
            sendThread.start()
        }

        mBinding.gasRoomRecyclerView.apply {
            gasRoomViewData.apply {
                add(
                    SetGasRoomViewData(
                        gasName = "asdfa",
                        gasColor = Color.parseColor("#DDDDDD"),
                        gasIndex = 0,
                        gasUnit = 0,
                        pressure = 10f,
                        pressureMax = 150f
                    )
                )
            }
            recycleAdapter.submitList(gasRoomViewData)
            recycleAdapter.notifyDataSetChanged()
        }

        mainViewModel.GasRoomData.observe(viewLifecycleOwner, {
            recycleAdapter.setGasRoomViewData.set(0, SetGasRoomViewData(
                gasName = "asdfa",
                gasColor = Color.parseColor("#DDDDDD"),
                gasIndex = 0,
                gasUnit = 0,
                pressure = it,
                pressureMax = 150f
            ))

            recycleAdapter.notifyDataSetChanged()
        })

        return mBinding.root
    }

    private fun initRecycler() {
//        recycleAdapter = GasRoom_RecycleAdapter()
        mBinding.gasRoomRecyclerView.apply {
            layoutManager =
                GridLayoutManager(context, 4, GridLayoutManager.VERTICAL, false)

            //아이템 높이 간격 조절
            val decoration_height = RecyclerDecoration_Height(25)
            addItemDecoration(decoration_height)

            //페이지 넘기는 효과
//            val snapHelper = PagerSnapHelper()
//            snapHelper.attachToRecyclerView(this)

            //Indicator 추가
//            addItemDecoration(LinePagerIndicatorDecoration())


            recycleAdapter = GasRoom_RecycleAdapter()
//            recycleAdapter.submitList(singleDockViewData)
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
         * @return A new instance of fragment GasRoomMainFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            GasRoomMainFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }


}
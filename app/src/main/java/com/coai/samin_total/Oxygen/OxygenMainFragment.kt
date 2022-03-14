package com.coai.samin_total.Oxygen

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
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.coai.samin_total.GasRoom.GasRoom_RecycleAdapter
import com.coai.samin_total.GasRoom.SetGasRoomViewData
import com.coai.samin_total.Logic.SaminProtocol
import com.coai.samin_total.MainActivity
import com.coai.samin_total.MainViewModel
import com.coai.samin_total.R
import com.coai.samin_total.RecyclerDecoration_Height
import com.coai.samin_total.Service.HexDump
import com.coai.samin_total.databinding.FragmentOxygenMainBinding
import kotlin.concurrent.thread

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [OxygenMainFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class OxygenMainFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private lateinit var mBinding: FragmentOxygenMainBinding

    //    private lateinit var viewmodel: OxygenViewModel
    private val viewmodel by activityViewModels<OxygenViewModel>()

    private lateinit var recycleAdapter: Oxygen_RecycleAdapter
    private val oxygenViewData = mutableListOf<SetOxygenViewData>()
    private lateinit var onBackPressed: OnBackPressedCallback
    private var activity: MainActivity? = null
    private lateinit var sendThread: Thread
    var sending = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
//        viewmodel = ViewModelProvider(this).get(OxygenViewModel::class.java)
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
        mBinding = FragmentOxygenMainBinding.inflate(inflater, container, false)
        initRecycler()

//        viewmodel = ViewModelProvider(this).get(OxygenViewModel::class.java)

        Log.d("태그", "11OxygenSensorID:${viewmodel.OxygenSensorID.value}")
        Log.d("태그", "11OxygenSensorIDs:${viewmodel.OxygenSensorID.value}")

        viewmodel.OxygenSensorID.observe(viewLifecycleOwner, Observer {
            Log.d("태그", "OxygenSensorID:${viewmodel.OxygenSensorID}")
            Log.d("태그", "OxygenSensorID:$it")

        })

        viewmodel.OxygenSensorIDs.observe(viewLifecycleOwner, Observer {
            Log.d("태그", "OxygenSensorIDs:${viewmodel.OxygenSensorIDs}")
            Log.d("태그", "OxygenSensorIDs:$it")

        })

        viewmodel.OxygenValue.observe(viewLifecycleOwner, Observer {
            Log.d("태그", "OxygenValue:${viewmodel.OxygenValue}")
            Log.d("태그", "OxygenValue:$it")
            Log.d("태그", "child:${recycleAdapter.getItemId(0)}")

            recycleAdapter.setOxygenViewData.set(0, SetOxygenViewData(0, false, it))
            recycleAdapter.notifyDataSetChanged()

//            mBinding.oxygenRecyclerView.apply {
//                oxygenViewData.apply {
//                    add(
//                        SetOxygenViewData(
//                            isAlert = false,
//                            setValue = it,
//                            setMinValue = 18
//                        )
//                    )
//                }
//                recycleAdapter.submitList(oxygenViewData)
//                recycleAdapter.notifyDataSetChanged()
//            }
        })

        mBinding.oxygenRecyclerView.apply {
            oxygenViewData.apply {
                add(
                    SetOxygenViewData(
                        isAlert = false,
                        setValue = 0,
                        setMinValue = 18
                    )
                )
//                add(
//                    SetOxygenViewData(
//                        isAlert = false,
//                        setValue = 0,
//                        setMinValue = 18
//                    )
//                )
            }
            recycleAdapter.submitList(oxygenViewData)
            recycleAdapter.notifyDataSetChanged()


        }
//        viewmodel.OxygenViewData.observe(viewLifecycleOwner, dataObserver)

        mBinding.titleTv.setOnClickListener {
            sending = true
            sendThread = Thread {
                while (sending) {
                    val protocol = SaminProtocol()
                    protocol.feedBack(MainViewModel.Oxygen, 1)
//                    Log.d("로그", "${protocol.mProtocol}")
                    activity?.serialService?.sendData(protocol.mProtocol)
                    Thread.sleep(200)
                }

            }
            sendThread.start()
        }

//        mBinding.titleTv.setOnClickListener {
//            mBinding.oxygenRecyclerView.apply {
//                oxygenViewData.apply {
//                    add(
//                        SetOxygenViewData(
//                            isAlert = false,
//                            setValue = 21,
//                            setMinValue = 18
//                        )
//                    )
//                    add(
//                        SetOxygenViewData(
//                            isAlert = true,
//                            setValue = 21,
//                            setMinValue = 18
//                        )
//                    )
//                }
//                recycleAdapter.submitList(oxygenViewData)
//                recycleAdapter.notifyDataSetChanged()
//            }
//        }


//        mBinding.titleTv.setOnClickListener {
//            val protocol = SaminProtocol()
//            protocol.feedBack(3, 0)
//            Log.d("로그", "${protocol.mProtocol}")
//            Log.d("로그", "send" + HexDump.dumpHexString(protocol.mProtocol))
//
//            activity?.serialService?.sendData(protocol.mProtocol)
//
//            mBinding.oxygenRecyclerView.apply {
//                oxygenViewData.apply {
//                    add(
//                        SetOxygenViewData(
//                            isAlert = false,
//                            setValue = 21f,
//                            setMinValue = 18
//                        )
//                    )
//                }
//                recycleAdapter.submitList(oxygenViewData)
//                recycleAdapter.notifyDataSetChanged()
//            }
//
//        }

        return mBinding.root
    }

    private fun initRecycler() {
        mBinding.oxygenRecyclerView.apply {
            layoutManager =
                GridLayoutManager(context, 4, GridLayoutManager.VERTICAL, false)

            //아이템 높이 간격 조절
            val decoration_height = RecyclerDecoration_Height(70)
            addItemDecoration(decoration_height)

            //페이지 넘기는 효과
//            val snapHelper = PagerSnapHelper()
//            snapHelper.attachToRecyclerView(this)

            //Indicator 추가
//            addItemDecoration(LinePagerIndicatorDecoration())


            recycleAdapter = Oxygen_RecycleAdapter()
//            recycleAdapter.submitList(singleDockViewData)
            adapter = recycleAdapter
        }

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
         * @return A new instance of fragment OxygenMainFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            OxygenMainFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}
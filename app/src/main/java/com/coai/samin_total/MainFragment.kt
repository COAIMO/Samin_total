package com.coai.samin_total

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import com.coai.samin_total.Logic.SaminProtocol
import com.coai.samin_total.Service.HexDump
import com.coai.samin_total.databinding.FragmentMainBinding
import kotlinx.coroutines.delay
import kotlin.concurrent.thread
import kotlin.math.log

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [MainFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class MainFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    lateinit var mBinding: FragmentMainBinding
    var activity: MainActivity? = null
    private val mainViewModel: MainViewModel by activityViewModels()

    override fun onAttach(context: Context) {
        activity = getActivity() as MainActivity
        super.onAttach(context)
    }

    override fun onDetach() {
        activity = null
        super.onDetach()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mBinding = FragmentMainBinding.inflate(inflater, container, false)
        setButtonClickEvent()

        mBinding.btnSound.setOnClickListener {
            Thread {
                for (model in 0..15) {
                    for (id in 0..15) {
                        val protocol = SaminProtocol()
                        protocol.checkModel(model.toByte(), id.toByte())
                        Log.d("로그", "${{ HexDump.dumpHexString(protocol.mProtocol)}}")
                        activity?.serialService?.sendData(protocol.mProtocol)
                        Thread.sleep(100)
                    }
                }
            }.start()

//            Thread{
//                while (true){
//
//                val protocol = SaminProtocol()
//                protocol.feedBack(3, 0)
//                Log.d("로그", "${protocol.mProtocol}")
//                activity?.serialService?.sendData(protocol.mProtocol)
//                    Thread.sleep(1000)
//                }
//
//            }.start()
        }

//        mainViewModel.model_ID_Data.observe(viewLifecycleOwner, Observer {
//            Log.d("태그", "model_ID_Data: $it")
//        })

//        mBinding.labIDTextView.setOnClickListener{
//            mainViewModel.model_ID_Data.observe(viewLifecycleOwner, Observer {
//                Log.d("태그", "model_ID_Data: $it")
//            })
//        }
        return mBinding.root
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment MainFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            MainFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    private fun setButtonClickEvent() {
        mBinding.gasDockMainStatus.setOnClickListener {
            onClick(mBinding.gasDockMainStatus)
        }
        mBinding.gasRoomMainStatus.setOnClickListener {
            onClick(mBinding.gasRoomMainStatus)
        }
        mBinding.wasteLiquorMainStatus.setOnClickListener {
            onClick(mBinding.wasteLiquorMainStatus)
        }
        mBinding.oxygenMainStatus.setOnClickListener {
            onClick(mBinding.oxygenMainStatus)
        }
        mBinding.steamerMainStatus.setOnClickListener {
            onClick(mBinding.steamerMainStatus)
        }
        mBinding.btnSetting.setOnClickListener {
            onClick(mBinding.btnSetting)
        }
        mBinding.btnAlert.setOnClickListener {
            onClick(mBinding.btnAlert)
        }
        mBinding.btnSound.setOnClickListener {
            onClick(mBinding.btnSound)
        }
        mBinding.btnScan.setOnClickListener {
            onClick(mBinding.btnScan)
        }
    }

    private fun onClick(view: View) {
        when (view) {
            mBinding.gasDockMainStatus -> {
                activity?.onFragmentChange(MainViewModel.GASDOCKMAINFRAGMENT)
            }
            mBinding.gasRoomMainStatus -> {
                activity?.onFragmentChange(MainViewModel.GASROOMMAINFRAGMENT)
            }
            mBinding.wasteLiquorMainStatus -> {
                activity?.onFragmentChange(MainViewModel.WASTELIQUORMAINFRAGMENT)
            }
            mBinding.oxygenMainStatus -> {
                activity?.onFragmentChange(MainViewModel.OXYGENMAINFRAGMENT)
            }
            mBinding.steamerMainStatus -> {
                activity?.onFragmentChange(MainViewModel.STEAMERMAINFRAGMENT)
            }
            mBinding.btnSetting -> {
                activity?.onFragmentChange(MainViewModel.MAINSETTINGFRAGMENT)
            }
            mBinding.btnAlert -> {
                activity?.onFragmentChange(MainViewModel.ALERTDIALOGFRAGMENT)
            }
            mBinding.btnSound -> {

            }
            mBinding.btnScan -> {
                activity?.onFragmentChange(MainViewModel.SCANDIALOGFRAGMENT)
            }
        }
    }
}
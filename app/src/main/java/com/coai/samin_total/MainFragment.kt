package com.coai.samin_total

import android.app.ProgressDialog
import android.content.Context
import android.content.DialogInterface
import android.media.SoundPool
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import com.coai.samin_total.Dialog.AlertDialogFragment
import com.coai.samin_total.Logic.SaminProtocol
import com.coai.samin_total.Logic.ThreadSynchronied
import com.coai.samin_total.Service.HexDump
import com.coai.samin_total.databinding.FragmentMainBinding
import com.coai.uikit.samin.status.TopStatusView

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
    private val viewmodel: MainViewModel by activityViewModels()
    lateinit var progress_Dialog: ProgressDialog
    lateinit var sendThread: Thread
    private val threadSync = ThreadSynchronied()
    private lateinit var soundPool: SoundPool
    lateinit var alertdialogFragment:AlertDialogFragment


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
        initView()

        mBinding.btnSound.setOnClickListener {
//            mBinding.gasDockMainStatus.setAlert(true)
            val mediaPlayer: android.media.MediaPlayer? =
                android.media.MediaPlayer.create(context, R.raw.tada)
            mediaPlayer?.start()
        }

        mBinding.labIDTextView.setOnClickListener {
            Thread {
                for (id in 0..7) {
                    val protocol = SaminProtocol()
                    protocol.feedBack(MainViewModel.Oxygen, id.toByte())
                    activity?.serialService?.sendData(protocol.mProtocol)
                    Thread.sleep(50)
                }
            }.start()
        }

//        mainViewModel.model_ID_Data.observe(viewLifecycleOwner, Observer {
//            Log.d("태그", "model_ID_Data: $it")
//        })

        val count = 0
//        mBinding.labIDTextView.setOnClickListener{
//
//            val protocol = SaminProtocol()
//            protocol.led_NormalState(0, 1)
//            activity?.serialService?.sendData(protocol.mProtocol)
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
                alertdialogFragment = AlertDialogFragment()
                val bundle =Bundle()
                bundle.putString("model", "Main")
                alertdialogFragment.arguments = bundle
                alertdialogFragment.show(requireActivity().supportFragmentManager, "GasRoom")
            }
            mBinding.btnSound -> {
                animate(mBinding.gasDockMainStatus)
            }
            mBinding.btnScan -> {
                scanModel()
            }
        }
    }

    fun animate(view: View) {
        val v: TopStatusView = view as TopStatusView
        v.setAlert(v.isAlert())
    }

    private fun scanModel() {
        getProgressShow()
        viewmodel.removeModelMap()

        sendThread = Thread {
            try {
                for (model in 1..5) {
                    for (id in 0..7) {
//                        for (count in 0..2) {
                            val protocol = SaminProtocol()
                            protocol.checkModel(model.toByte(), id.toByte())
                            activity?.serialService?.sendData(protocol.mProtocol)
                            Thread.sleep(40)
//                        }
                    }
                }
                Thread.sleep(400)

            } catch (e: Exception) {
            }

            activity?.runOnUiThread {
                if (viewmodel.modelMap.isEmpty()){
                    Toast.makeText(requireContext(),"연결된 AQ보드가 없습니다.", Toast.LENGTH_SHORT).show()
                }
                initView()
            }
            getProgressHidden()
//            activity?.callFeedback()
        }
        sendThread.start()

    }

    private fun initView() {
        mBinding.gasDockMainStatusLayout.visibility = View.GONE
        mBinding.gasRoomMainStatusLayout.visibility = View.GONE
        mBinding.wasteLiquorMainStatusLayout.visibility = View.GONE
        mBinding.oxygenMainStatusLayout.visibility = View.GONE
        mBinding.steamerMainStatusLayout.visibility = View.GONE
        for ((key, ids) in viewmodel.modelMap) {
            when (key) {
                "GasDock" -> {
                    mBinding.gasDockMainStatusLayout.visibility = View.VISIBLE
                }
                "GasRoom" -> {
                    mBinding.gasRoomMainStatusLayout.visibility = View.VISIBLE
                }
                "WasteLiquor" -> {
                    mBinding.wasteLiquorMainStatusLayout.visibility = View.VISIBLE
                }
                "Oxygen" -> {
                    mBinding.oxygenMainStatusLayout.visibility = View.VISIBLE
                }
                "Steamer" -> {
                    mBinding.steamerMainStatusLayout.visibility = View.VISIBLE
                }
            }
        }
    }

    private fun getProgressShow() {
        try {
            val str_tittle = "Please Wait ..."
            val str_message = "잠시만 기다려주세요 ...\n진행 중입니다 ..."
            val str_buttonOK = "종료"
            val str_buttonNO = "취소"

            progress_Dialog = ProgressDialog(context)
            progress_Dialog.setTitle(str_tittle) //팝업창 타이틀 지정
            progress_Dialog.setIcon(R.mipmap.ic_launcher) //팝업창 아이콘 지정
            progress_Dialog.setMessage(str_message) //팝업창 내용 지정
            progress_Dialog.setCancelable(false) //외부 레이아웃 클릭시도 팝업창이 사라지지않게 설정
            progress_Dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER) //프로그레스 원형 표시 설정
            progress_Dialog.setButton(
                DialogInterface.BUTTON_POSITIVE,
                str_buttonOK,
                DialogInterface.OnClickListener { dialog, which ->
                    try {
                        sendThread.interrupt()
                        viewmodel.removeModelMap()
                        getProgressHidden()
                    } catch (e: Exception) {
                        Log.d("interrupt", "$e")
                    }
                })
//            progress_Dialog.setButton(
//                DialogInterface.BUTTON_NEGATIVE,
//                str_buttonNO,
//                DialogInterface.OnClickListener { dialog, which ->
//                    getProgressHidden()
//                })
            try {
                progress_Dialog.show()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun getProgressHidden() {
        try {
            progress_Dialog.dismiss()
            progress_Dialog.cancel()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
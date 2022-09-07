package com.coai.samin_total

import android.app.ProgressDialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.app.ActivityCompat
import androidx.fragment.app.activityViewModels
import com.coai.samin_total.Logic.SaminProtocol
import com.coai.samin_total.Logic.SaminSharedPreference
import com.coai.samin_total.databinding.FragmentAqSettingBinding

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class AqSettingFragment : Fragment() {
    private var param1: String? = null
    private var param2: String? = null
    var activity: MainActivity? = null
    private lateinit var onBackPressed: OnBackPressedCallback
    private lateinit var mBinding: FragmentAqSettingBinding
    lateinit var shared: SaminSharedPreference
    private val viewmodel: MainViewModel by activityViewModels()
    lateinit var progress_Dialog: ProgressDialog
    lateinit var sendThread: Thread

    override fun onAttach(context: Context) {
        super.onAttach(context)
        activity = getActivity() as MainActivity
        onBackPressed = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                activity!!.onFragmentChange(MainViewModel.ADMINFRAGMENT)
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(this, onBackPressed)
    }

    override fun onDetach() {
        super.onDetach()
        activity = null
        onBackPressed.remove()
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
        mBinding = FragmentAqSettingBinding.inflate(inflater, container, false)
        shared = SaminSharedPreference(requireContext())

        initView()
        setButtonClickEvent()

        return mBinding.root
    }

    private fun initView() {
        if (!shared.loadLabNameData().isEmpty()) {
            mBinding.etNewName.setText(shared.loadLabNameData())
        }
        mBinding.swCheckTimeout.isChecked = shared.getTimeOutState()
        mBinding.etFeedbackTiming.setText(shared.getFeedbackTiming().toString())
    }

    private fun setButtonClickEvent() {
        mBinding.saveBtn.setOnClickListener {
            onClick(mBinding.saveBtn)
        }
        mBinding.cancelBtn.setOnClickListener {
            onClick(mBinding.cancelBtn)
        }
        mBinding.btnBack.setOnClickListener {
            onClick(mBinding.btnBack)
        }
        mBinding.btnScan.setOnClickListener {
            onClick(mBinding.btnScan)
        }
    }

    private fun onClick(view: View) {
        when (view) {
            mBinding.saveBtn -> {
                val labName = mBinding.etNewName.text.toString()
                activity?.shared?.labNameSave(SaminSharedPreference.LABNAME, labName)
                viewmodel.isCheckTimeOut = mBinding.swCheckTimeout.isChecked
                activity?.shared?.SavecheckTimeOutState(viewmodel.isCheckTimeOut)

                if (shared.getTimeOutState()) {
                    activity?.discallTimemout()
                } else {
                    activity?.callTimemout()
                }
                shared.SaveFeedbackTiming(mBinding.etFeedbackTiming.text.toString().toLong())
                Thread.sleep(500)
                getActivity()?.let { ActivityCompat.finishAffinity(it) }
                System.exit(0)
//                activity?.onFragmentChange(MainViewModel.ADMINFRAGMENT)
            }
            mBinding.cancelBtn -> {
                activity?.onFragmentChange(MainViewModel.ADMINFRAGMENT)
            }
            mBinding.btnBack -> {
                activity?.onFragmentChange(MainViewModel.ADMINFRAGMENT)
            }
            mBinding.btnScan -> {
                scanModel()
            }
        }

    }
    private fun scanModel() {
        getProgressShow()
        sendThread = Thread {
            try {
                viewmodel.isScanmode = true
                activity?.deleteExDataSet()
                activity?.feedBackThreadInterrupt()
                for (model in 1..5) {
                    for (id in 0..7) {
                        for (count in 0..2) {
                            val protocol = SaminProtocol()
                            protocol.checkModel(model.toByte(), id.toByte())
//                            activity?.serialService?.sendData(protocol.mProtocol)
                            sendAlertProtocol(protocol.mProtocol)
                            Thread.sleep(40)
                        }
                    }
                }
                Thread.sleep(400)
                viewmodel.isScanmode = false

            } catch (e: Exception) {
            }

            activity?.runOnUiThread {
                if (viewmodel.modelMap.isEmpty()) {
                    Toast.makeText(requireContext(), "연결된 AQ보드가 없습니다.", Toast.LENGTH_SHORT).show()
                }
                initView()
            }
//            createHasKey()
            if (!activity?.isSending!!) {
                activity?.callFeedback()
                activity?.isSending = true
            }
            shared.saveHashMap(viewmodel.modelMap)
            getProgressHidden()

        }
        sendThread.start()

    }
    private fun getProgressShow() {
        try {
            val str_tittle = "Please Wait ..."
            val str_message = "잠시만 기다려주세요 ...\n진행 중입니다 ..."
            val str_buttonOK = "종료"
            val str_buttonNO = "취소"

            progress_Dialog = ProgressDialog(context)
            progress_Dialog.setTitle(str_tittle) //팝업창 타이틀 지정
            progress_Dialog.setIcon(R.mipmap.samin_launcher_ic) //팝업창 아이콘 지정
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
                    }
                })

            try {
                progress_Dialog.show()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    private fun sendAlertProtocol(data: ByteArray) {
        activity?.sendProtocolToSerial(data)
    }

    private fun getProgressHidden() {
        try {
            progress_Dialog.dismiss()
            progress_Dialog.cancel()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment AqSettingFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            AqSettingFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}
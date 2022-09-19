package com.coai.samin_total.Dialog

import android.app.ProgressDialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.coai.samin_total.Logic.SaminProtocol
import com.coai.samin_total.Logic.SaminSharedPreference
import com.coai.samin_total.MainActivity
import com.coai.samin_total.MainViewModel
import com.coai.samin_total.R
import com.coai.samin_total.databinding.FragmentScanAlertDialogBinding

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [ScanAlertDialogFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ScanAlertDialogFragment : DialogFragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    var activity: MainActivity? = null
    private lateinit var mBinding:FragmentScanAlertDialogBinding
    lateinit var sendThread: Thread
    lateinit var progress_Dialog: ProgressDialog
    private val viewmodel: MainViewModel by activityViewModels()
    lateinit var shared: SaminSharedPreference

    override fun onAttach(context: Context) {
        super.onAttach(context)
        activity = getActivity() as MainActivity
    }

    override fun onDetach() {
        super.onDetach()
        activity = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onResume() {
        val width: Int = resources.getDimensionPixelSize(R.dimen.passwordDialogView_width)
        val height: Int = resources.getDimensionPixelSize(R.dimen.passwordDialogView_height)
        dialog?.window?.setLayout(width, height)
        dialog?.window?.setBackgroundDrawableResource(R.drawable.border_layout)
        super.onResume()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mBinding = FragmentScanAlertDialogBinding.inflate(inflater, container, false)
        shared = SaminSharedPreference(requireContext())

        setButtonClickEvent()
        return mBinding.root
    }

    private fun setButtonClickEvent() {
        mBinding.btnCancel.setOnClickListener {
            onClick(it)
        }
        mBinding.btnOkay.setOnClickListener {
            onClick(it)
        }
    }

    private fun onClick(view: View){
        when(view){
            mBinding.btnCancel ->{
                dismiss()
            }
            mBinding.btnOkay -> {
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
                    dismiss()
                }else{
                    dismiss()
                    (requireActivity() as MainActivity).onFragmentChange(MainViewModel.MAINFRAGMENT)
                }
            }
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
         * @return A new instance of fragment ScanAlertDialogFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            ScanAlertDialogFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}
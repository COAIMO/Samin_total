package com.coai.samin_total.Dialog

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.DialogFragment
import com.coai.samin_total.Logic.AdminLock
import com.coai.samin_total.MainActivity
import com.coai.samin_total.MainViewModel
import com.coai.samin_total.R
import com.coai.samin_total.databinding.FragmentPasswordDialogBinding

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [PasswordDialogFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class PasswordDialogFragment : DialogFragment() {
    private var param1: String? = null
    private var param2: String? = null
    private lateinit var mBinding: FragmentPasswordDialogBinding
    var activity: MainActivity? = null

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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mBinding = FragmentPasswordDialogBinding.inflate(inflater, container, false)

        mBinding.btnCancel.setOnClickListener {
            dialog?.dismiss()
        }

        mBinding.btnOkay.setOnClickListener {
            val inputPassword = mBinding.etPassword.text.toString()
            val adminLock = context?.let { it1 -> AdminLock(it1) }

            if (adminLock?.checkPassLockSet(AdminLock.PERSONAL_KEY,inputPassword)!!) {
                activity?.onFragmentChange(MainViewModel.ADMINFRAGMENT)
                dialog?.dismiss()
            } else if (adminLock.checkPassLockSet(AdminLock.MASTER_KEY,inputPassword)){
                activity?.onFragmentChange(MainViewModel.ADMINFRAGMENT)
                dialog?.dismiss()
            }
            else{
                Toast.makeText(context, "잘못된 비밀번호 입니다.", Toast.LENGTH_SHORT).show()
                dialog?.dismiss()
            }
        }

        return mBinding.root
    }

    override fun onResume() {
        val width: Int = resources.getDimensionPixelSize(R.dimen.passwordDialogView_width)
        val height: Int = resources.getDimensionPixelSize(R.dimen.passwordDialogView_height)
        dialog?.window?.setLayout(width, height)
        dialog?.window?.setBackgroundDrawableResource(R.drawable.border_layout)
        super.onResume()
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment PasswordDialogFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            PasswordDialogFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}
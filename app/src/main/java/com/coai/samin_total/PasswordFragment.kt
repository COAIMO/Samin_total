package com.coai.samin_total

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import com.coai.samin_total.Logic.AdminLock
import com.coai.samin_total.databinding.FragmentPasswordBinding

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [PasswordFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class PasswordFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    var activity:MainActivity? = null
    private lateinit var mBinding: FragmentPasswordBinding
    private lateinit var onBackPressed: OnBackPressedCallback

    override fun onAttach(context: Context) {
        super.onAttach(context)
        activity = getActivity() as MainActivity
        onBackPressed = object : OnBackPressedCallback(true){
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
        mBinding = FragmentPasswordBinding.inflate(inflater, container, false)
        textClear()

        mBinding.cancelBtn.setOnClickListener {
            activity?.onFragmentChange(MainViewModel.ADMINFRAGMENT)
        }

        mBinding.saveBtn.setOnClickListener {
            val adminLock = context?.let { it1 -> AdminLock(it1) }
            if (mBinding.etNewPassword.text.toString() == mBinding.etNewPasswordConfirm.text.toString()) {
                adminLock?.setPassLock(AdminLock.PERSONAL_KEY,mBinding.etNewPasswordConfirm.text.toString())
                textClear()
                Toast.makeText(context, "비밀번호가 변경되었습니다.", Toast.LENGTH_SHORT).show()
            }else{
                Toast.makeText(context, "비밀번호가 틀립니다.", Toast.LENGTH_SHORT).show()
                textClear()
            }
        }

        mBinding.btnBack.setOnClickListener {
            activity?.onFragmentChange(MainViewModel.ADMINFRAGMENT)
        }
        return mBinding.root
    }
    private fun textClear(){
        mBinding.etNewPassword.text?.clear()
        mBinding.etNewPasswordConfirm.text?.clear()
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment PasswordFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            PasswordFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}
package com.coai.samin_total.Dialog

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.coai.samin_total.CustomView.SpaceDecoration
import com.coai.samin_total.MainViewModel
import com.coai.samin_total.R
import com.coai.samin_total.databinding.FragmentAlertDialogBinding

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [AlertDialogFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class AlertDialogFragment : DialogFragment() {
    private var param1: String? = null
    private var param2: String? = null
    private var model: String? = null
    private lateinit var mBinding: FragmentAlertDialogBinding
    private lateinit var recycleAdapter: AlertDialog_RecyclerAdapter
    private val viewmodel by activityViewModels<MainViewModel>()
    private val alertData = mutableListOf<SetAlertData>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
            model = it.getString("model")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mBinding = FragmentAlertDialogBinding.inflate(inflater, container, false)
        initRecycler()
        initVieiw()

        return mBinding.root
    }

    override fun onResume() {
        val width: Int = resources.getDimensionPixelSize(R.dimen.DialogView_width)
        val height: Int = resources.getDimensionPixelSize(R.dimen.DialogView_height)
        dialog?.window?.setLayout(width, height)
        dialog?.window?.setBackgroundDrawableResource(R.drawable.border_layout)

        super.onResume()
    }

    private fun initVieiw() {
        when (model) {
            "Main" -> {
                mBinding.tvTitle.setText(R.string.title_event_log)
                for (i in viewmodel.alertInfo.value!!) {
                    alertData.add(i)
                    recycleAdapter.submitList(alertData)
                }
            }
            "GasStorage" -> {
                mBinding.tvTitle.setText(R.string.title_gasstorage_event_log)
                for (i in viewmodel.alertInfo.value!!) {
                    if (i.model == 1) {
                        alertData.add(i)
                        recycleAdapter.submitList(alertData)
                    }
                }
            }
            "GasRoom" -> {
                mBinding.tvTitle.setText(R.string.title_gasroom_event_log)
                for (i in viewmodel.alertInfo.value!!) {
                    if (i.model == 2) {
                        alertData.add(i)
                        recycleAdapter.submitList(alertData)
                    }
                }
            }
            "WasteLiquor" -> {
                mBinding.tvTitle.setText(R.string.title_wasteliquor_event_log)
                for (i in viewmodel.alertInfo.value!!) {
                    if (i.model == 3) {
                        alertData.add(i)
                        recycleAdapter.submitList(alertData)
                    }
                }
            }
            "Oxygen" -> {
                mBinding.tvTitle.setText(R.string.title_oxygen_event_log)
                for (i in viewmodel.alertInfo.value!!) {
                    if (i.model == 4) {
                        alertData.add(i)
                        recycleAdapter.submitList(alertData)
                    }
                }
            }
            "Steamer" -> {
                mBinding.tvTitle.setText(R.string.title_steamer_event_log)
                for (i in viewmodel.alertInfo.value!!) {
                    if (i.model == 5) {
                        alertData.add(i)
                        recycleAdapter.submitList(alertData)
                    }
                }
            }
        }
        recycleAdapter.notifyDataSetChanged()

    }

    private fun initRecycler() {
        mBinding.alertRecyclerView.apply {
            layoutManager =
                LinearLayoutManager(context)

            //아이템 높이 간격 조절
            val decoration = SpaceDecoration(20, 20)
            addItemDecoration(decoration)

            recycleAdapter = AlertDialog_RecyclerAdapter()
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
         * @return A new instance of fragment AlertDialogFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            AlertDialogFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}
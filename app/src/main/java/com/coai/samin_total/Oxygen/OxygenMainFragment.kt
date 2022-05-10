package com.coai.samin_total.Oxygen

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.SimpleItemAnimator
import com.coai.samin_total.Dialog.AlertDialogFragment
import com.coai.samin_total.Logic.SaminSharedPreference
import com.coai.samin_total.Logic.SpacesItemDecoration
import com.coai.samin_total.MainActivity
import com.coai.samin_total.MainViewModel
import com.coai.samin_total.R
import com.coai.samin_total.RecyclerDecoration_Height
import com.coai.samin_total.WasteLiquor.SetWasteLiquorViewData
import com.coai.samin_total.databinding.FragmentOxygenMainBinding
import java.util.*

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
    private val viewmodel by activityViewModels<MainViewModel>()
    private lateinit var recycleAdapter: Oxygen_RecycleAdapter
//    private val oxygenViewData = mutableListOf<SetOxygenViewData>()
    private var oxygenViewData:SetOxygenViewData? = null

    private lateinit var onBackPressed: OnBackPressedCallback
    private var activity: MainActivity? = null
    private lateinit var sendThread: Thread
    var sending = false
    var btn_Count = 0
    lateinit var alertdialogFragment: AlertDialogFragment
    lateinit var shared: SaminSharedPreference
    private var timerTaskRefresh: Timer? = null
    var heartbeatCount: UByte = 0u
    val lockobj = object {}
    lateinit var itemSpace: SpacesItemDecoration
    private var taskRefresh: Thread? = null
    private var isOnTaskRefesh: Boolean = true

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
        timerTaskRefresh?.cancel()
    }

    override fun onResume() {
        super.onResume()
        isOnTaskRefesh = true
        taskRefresh = Thread() {
            try {
                var lastupdate: Long = System.currentTimeMillis()
                var chk: Boolean = false
                while (isOnTaskRefesh) {
                    chk = false
                    heartbeatCount++

                    if (viewmodel.oxygenMasterData?.isAlert != oxygenViewData?.isAlert ||
                        viewmodel.oxygenMasterData?.setValue != oxygenViewData?.setValue)
                            chk = true

                    if ((((heartbeatCount / 10u) % 2u) == 0u) != ((((heartbeatCount - 1u )/ 10u) % 2u) == 0u)) {
                        if (viewmodel.oxygenMasterData?.isAlert == true) {
                            chk = true
                        }
                    }
                    viewmodel.oxygenMasterData?.heartbeatCount = heartbeatCount
                    oxygenViewData = viewmodel.oxygenMasterData?.copy()


                    if(chk) {
                        synchronized(lockobj) {
                            activity?.runOnUiThread() {
                                recycleAdapter.notifyItemRangeChanged(0, recycleAdapter.itemCount)
                            }
                        }
                    }
                    else {
                        val baseTime = System.currentTimeMillis() - 1000 * 5
                        if (lastupdate < baseTime) {
                            lastupdate = System.currentTimeMillis()
                            synchronized(lockobj) {
                                activity?.runOnUiThread {
                                    recycleAdapter.notifyItemRangeChanged(0, recycleAdapter.itemCount)
                                }
                            }
                        }
                    }

                    Thread.sleep(50)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        taskRefresh?.start()
    }

    override fun onPause() {
        super.onPause()
        isOnTaskRefesh = false
        taskRefresh?.interrupt()
        taskRefresh?.join()
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mBinding = FragmentOxygenMainBinding.inflate(inflater, container, false)
        shared = SaminSharedPreference(requireContext())
        itemSpace = SpacesItemDecoration(50)
        initRecycler()
        initView()
        updateView()
        mBinding.btnSetting.setOnClickListener {
            activity?.onFragmentChange(MainViewModel.OXYGENSETTINGFRAGMENT)
        }
        mBinding.btnZoomInout.setOnClickListener {
            if (!viewmodel.oxygenViewZoomState) {
                viewmodel.oxygenViewZoomState = true
                mBinding.btnZoomInout.setImageResource(R.drawable.screen_decrease_ic)
                synchronized(lockobj) {
                    mBinding.oxygenRecyclerView.apply {
                        itemSpace.changeSpace(
                            50, 650, 50, 650
                        )
                    }
                }
            } else {
                viewmodel.oxygenViewZoomState = false
                mBinding.btnZoomInout.setImageResource(R.drawable.screen_increase_ic)
                synchronized(lockobj) {
                    mBinding.oxygenRecyclerView.apply {
                        itemSpace.changeSpace(
                            220, 800, 200, 800
                        )
                    }
                }
            }
            synchronized(lockobj) {
                activity?.runOnUiThread {
                    recycleAdapter.notifyItemRangeChanged(0, recycleAdapter.itemCount)
                }
            }
        }

        mBinding.btnAlert.setOnClickListener {
            alertdialogFragment = AlertDialogFragment()
            val bundle = Bundle()
            bundle.putString("model", "Oxygen")
            alertdialogFragment.arguments = bundle
            alertdialogFragment.show(childFragmentManager, "Oxygen")
        }

        mBinding.btnBack.setOnClickListener {
            activity?.onFragmentChange(MainViewModel.MAINFRAGMENT)
        }
        updateAlert()
        return mBinding.root
    }

    private fun initRecycler() {
        mBinding.oxygenRecyclerView.apply {
            if (!viewmodel.oxygenViewZoomState) {
                layoutManager =
                    GridLayoutManager(context, 1, GridLayoutManager.VERTICAL, false)
                itemSpace.changeSpace(
                    220, 800, 200, 800
                )
            } else {
                layoutManager =
                    GridLayoutManager(context, 1, GridLayoutManager.VERTICAL, false)
                itemSpace.changeSpace(
                    80, 650, 80, 650
                )
            }
            addItemDecoration(itemSpace)

            recycleAdapter = Oxygen_RecycleAdapter()
            adapter = recycleAdapter
        }
        mBinding.oxygenRecyclerView.itemAnimator = null
        mBinding.oxygenRecyclerView.animation = null
//        (mBinding.oxygenRecyclerView.itemAnimator as? SimpleItemAnimator)?.supportsChangeAnimations = false
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun initView() {

//        val mm = viewmodel.OxygenDataLiveList.value!!.sortedWith(compareBy({ it.id }, { it.port }))
//        recycleAdapter.submitList(mm)

        if (viewmodel.oxygenMasterData != null) {
            oxygenViewData = viewmodel.oxygenMasterData?.copy()
            recycleAdapter.setData(viewmodel.oxygenMasterData!!)
        }

        if (viewmodel.oxygenViewZoomState) {
            mBinding.btnZoomInout.setImageResource(R.drawable.screen_decrease_ic)
        } else {
            mBinding.btnZoomInout.setImageResource(R.drawable.screen_increase_ic)
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun updateView() {
//        viewmodel.OxygenDataLiveList.observe(viewLifecycleOwner) {
//            val mm = it.sortedWith(compareBy({ it.id }, { it.port }))
//            recycleAdapter.submitList(mm)
//            recycleAdapter.notifyDataSetChanged()
//        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        sending = false
    }

    private fun updateAlert() {
        viewmodel.oxyenAlert.observe(viewLifecycleOwner) {
            if (it) {
                mBinding.btnAlert.setImageResource(R.drawable.onalert_ic)
            } else {
                mBinding.btnAlert.setImageResource(R.drawable.nonalert_ic)
            }
        }

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
package com.coai.samin_total

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.coai.samin_total.GasRoom.GasRoom_RecycleAdapter
import com.coai.samin_total.Logic.SaminProtocol
import com.coai.samin_total.Oxygen.OxygenViewModel
import com.coai.samin_total.databinding.FragmentAqSettingBinding
import kotlin.concurrent.thread

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"


// TODO: 2022-01-28 AQ ID는 앱시작 시 호출되서 저장되어있으며, 저장된 아이디값은 받아와야야됨 (알람켜기, 끄기, led경고, 정상만)UI 작업
class AqSettingFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    var activity: MainActivity? = null
    private lateinit var onBackPressed: OnBackPressedCallback
    private lateinit var mBinding: FragmentAqSettingBinding
    private val viewmodel by activityViewModels<MainViewModel>()
    private val aqInfoData = mutableListOf<SetAqInfo>()
    private lateinit var recycleAdapter: AqSetting_RecycleAdapter

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
        setButtonClickEvent()
        initRecycler()
        recycleAdapter.setItemClickListener(object : AqSetting_RecycleAdapter.OnItemClickListener{
            override fun onClick(v: View, position: Int) {
                // 클릭 시 이벤트 작성
                Toast.makeText(
                    view?.context,
                    "${aqInfoData[position].id}\n${aqInfoData[position].model}",
                    Toast.LENGTH_SHORT).show()
            }
        })

        return mBinding.root
    }

    private fun setButtonClickEvent() {
        mBinding.searchBtn.setOnClickListener {
            onClick(mBinding.searchBtn)
        }
    }

    private lateinit var sendThread: Thread
    private fun onClick(view: View) {
        when (view) {
            mBinding.searchBtn -> {
                sendThread = Thread {
                    try {
                        for (model in 0..5) {
                            for (id in 0..7) {
//                                for (count in 0..4){
                                    val protocol = SaminProtocol()
                                    protocol.checkModel(model.toByte(), id.toByte())
                                    activity?.serialService?.sendData(protocol.mProtocol)
                                    Thread.sleep(100)
//                                }
                            }
                        }

                    }catch (e:Exception){}

                }
                sendThread.start()

//                mBinding.boardRecyclerView.apply {
//                    aqInfoData.apply {
//                        add(
//                            SetAqInfo(1,1)
//                        )
//                    }
//                    recycleAdapter.submitList(aqInfoData)
//                    recycleAdapter.notifyDataSetChanged()
//                }
            }


        }

    }


    private fun initRecycler() {
        mBinding.boardRecyclerView.apply {
            layoutManager =
                LinearLayoutManager(context, )

            //아이템 높이 간격 조절
            val decoration_height = RecyclerDecoration_Height(25)
            addItemDecoration(decoration_height)

            recycleAdapter = AqSetting_RecycleAdapter()
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
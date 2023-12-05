package com.coai.samin_total

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Process
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.coai.samin_total.databinding.FragmentLoadingscreenBinding
import kotlin.system.exitProcess

/**
 * An example full-screen fragment that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
class LoadingscreenFragment : Fragment() {
  private val handler = Handler(Looper.getMainLooper())
  private var _binding: FragmentLoadingscreenBinding? = null
  private val binding get() = _binding!!
  private val viewmodel by activityViewModels<MainViewModel>()
  private var activity: MainActivity? = null

  private val runnable = Runnable {
    Log.d("로그", "Close ====================!!!!!!!!!")
//    activity?.finishAndRemoveTask()
//
//    Process.killProcess(Process.myPid())
//    exitProcess(10)
    val intent = Intent(context, AppRestartReceiver::class.java)
    val pendingIntent = PendingIntent.getBroadcast(context, 0, intent,
      PendingIntent.FLAG_IMMUTABLE)
    pendingIntent.send()
  }

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    _binding = FragmentLoadingscreenBinding.inflate(inflater, container, false)
    viewmodel.scanDone.observe(viewLifecycleOwner) {
      activity?.onFragmentChange(MainViewModel.MAINFRAGMENT)
    }
    return binding.root
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
  }

  override fun onResume() {
    super.onResume()
    activity?.window?.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
    handler.postDelayed(runnable, 6000)


  }

  override fun onPause() {
    super.onPause()
    activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
//    activity?.window?.decorView?.systemUiVisibility = 0
    handler.removeCallbacks(runnable)
  }

  override fun onDestroy() {
    super.onDestroy()
  }

  override fun onAttach(context: Context) {
    super.onAttach(context)
    activity = getActivity() as MainActivity
  }

  override fun onDetach() {
    super.onDetach()
  }

  companion object {
  }

  override fun onDestroyView() {
    super.onDestroyView()
    _binding = null
    activity = null
  }
}
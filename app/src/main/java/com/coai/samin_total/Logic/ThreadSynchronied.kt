//package com.coai.samin_total.Logic
//
//import android.util.Log
//import java.util.concurrent.atomic.AtomicBoolean
//
//class ThreadSynchronied {
//    val TAG = "로그"
//    private val _monitor = Object()
////    private var _isOpen = false
//    private val _isOpen = AtomicBoolean(false)
//    private var result = null
////    private var isWaiting = false
////    private var wasNotified = false
//    private val isWaiting = AtomicBoolean(false)
//    private val wasNotified = AtomicBoolean(false)
//
//
//    fun SynchroniedService(open: Boolean) {
//        _isOpen = open
//        _isOpen.set()
//    }
//
//    fun waitOne() {
//        synchronized(_monitor) {
//            while (!_isOpen) {
//                _monitor.wait()
//            }
//            _isOpen = false
//        }
//    }
//
////    fun waitOne(timeout: Long) {
////        synchronized(_monitor) {
////            try {
////                while (!_isOpen) {
////                    _monitor.wait(timeout)
////                }
////
////            }
////            catch (ex : Exception){
////                Log.d(TAG, ex.toString())
////            }
////            _isOpen = false
////        }
////    }
//
//    @Throws(InterruptedException::class)
//    fun waitOne(timeout: Long) {
//        synchronized(_monitor) {
//            val t = System.currentTimeMillis()
//            while (!_isOpen) {
//                _monitor.wait(timeout)
//                // Check for timeout
////                Log.d(TAG, "waitOne $timeout")
//
//
//                if (System.currentTimeMillis() - t >= timeout) {
//                    throw InterruptedException("assssssss!!!")
////                    break
//                }
//            }
//            _isOpen = false
//        }
//    }
////    fun waitOne(timeout: Long) {
////        synchronized(_monitor) {
////            val t = System.currentTimeMillis()
////            while (!_isOpen) {
////                //                    _monitor.wait(timeout)
////                //                    // Check for timeout
////                if (System.currentTimeMillis() - t >= timeout) break
////            }
////            _isOpen = false
////        }
////    }
//
//    fun set() {
//        synchronized(_monitor) {
//            _isOpen = true
//            _monitor.notify()
//        }
//    }
//
//    fun reset() {
//        _isOpen = false
//    }
//
//    fun setResultAndNotify(){
//        if (isWaiting){
//            synchronized(_monitor){
//                _monitor.notify()
//            }
//        }else{
//            wasNotified = true
//        }
//    }
//
//    fun waitAndGetResult(timeout: Long): Nothing? {
//        if (wasNotified){
//            wasNotified = false
//            return result
//        }
//        try {
//            synchronized(_monitor){
//                isWaiting = true
//                if (timeout < 0) _monitor.wait()
//                else _monitor.wait(timeout)
//                isWaiting = false
//            }
//        }catch (e: InterruptedException){
//            Log.getStackTraceString(e)
//        }
//        return this.result
//
//        fun waitAndGetResult() = waitAndGetResult(-1)
//    }
//
//    fun masterThreadWork() {
////        Log.d(TAG, "port1ThreadStart-masterThreadWork()")
//
//        synchronized(_monitor) {
////            Log.d(TAG, "port1ThreadStart-notify()")
//            _monitor.notify()
//
//            try {
//                Log.d(TAG, "port1ThreadStart-wait()")
//                _monitor.wait()
//            } catch (e: Exception) {
//            }
//        }
//    }
//
//    fun receivedWork() {
//        synchronized(_monitor) {
//            Log.d(TAG, "port1ThreadStart-notify()")
//            _monitor.notify()
//
//        }
//    }
//
//}
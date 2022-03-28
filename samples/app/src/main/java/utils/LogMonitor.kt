package utils

import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import android.util.Log
import java.util.*

/**
 * Description:打印耗时的堆栈
 *
 * @author n20241 Created by 杜小菜 on 2022/3/9 - 12:00 . E-mail: duqian2010@gmail.com
 */
class LogMonitor private constructor() {

    private val startMap = HashMap<String, Long>()
    private val durationMap = HashMap<String, Long>()

    /**
     * 开始计时
     */
    fun start(tag: String) {
        startMap[tag] = System.currentTimeMillis()
    }

    /**
     * 停止计时
     */
    fun end(tag: String) {
        val aLong = startMap[tag]
        val start = aLong ?: 0L
        val duration = System.currentTimeMillis() - start
        if (duration >= 8) {
            Log.e(TAG, "$tag,duration=$duration")
        }
        //durationMap.put(tag, duration);
    }


    private val mHandler: Handler

    init {
        val logThread = HandlerThread("dq-ui-log")
        logThread.start()
        mHandler = Handler(logThread.looper)
    }

    companion object {
        private const val TAG = "dq-ui-LogMonitor"
        val instance = LogMonitor()

        /**
         * 自定义超时的阈值，如果handle处理耗时超过100ms，打印堆栈
         */
        private const val TIME_BLOCK = 100L
        private val mLogRunnable = Runnable {
            //打印出执行的耗时方法的栈消息
            val sb = StringBuilder()
            val stackTrace = Looper.getMainLooper().thread.stackTrace
            for (s in stackTrace) {
                sb.append(s.toString())
                sb.append("\n")
            }
            Log.e(TAG, "main-ui stackTrace=$sb")
        }
    }

    /**
     * 开始计时
     */
    fun startMonitor() {
        mHandler.postDelayed(mLogRunnable, TIME_BLOCK)
    }

    /**
     * 停止计时
     */
    fun removeMonitor() {
        mHandler.removeCallbacks(mLogRunnable)
    }
}
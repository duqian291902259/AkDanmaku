package utils

import android.app.Activity
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.util.Log
import android.util.Printer
import android.view.Choreographer
import android.view.Choreographer.FrameCallback
import android.view.LayoutInflater.Factory2
import android.view.View
import kotlin.math.roundToInt


/**
 * Description:UI工具类
 * @author n20241 Created by 杜小菜 on 2022/2/24 - 12:01 .
 * E-mail: duqian2010@gmail.com
 */
object UIUtils {

    private const val TAG = "dq-ui"

    class HandlerLogger : Printer {
        companion object {
            private const val START = ">>>>> Dispatching to"
            private const val END = "<<<<< Finished to"
        }

        //打印log，可以统计时长，超过xxxms认为卡顿
        private var start = 0L

        override fun println(x: String?) {
            val isStartHandle = x?.startsWith(START) == true
            if (isStartHandle) {
                //开始计时
                LogMonitor.instance.startMonitor()
                start = System.currentTimeMillis()
            }
            if (x?.startsWith(END) == true) {
                //结束计时，并计算出方法执行时间
                LogMonitor.instance.removeMonitor()
                val end = System.currentTimeMillis()
                val diff = end - start
                if (diff > 100 && start > 0) {
                    Log.e(TAG, "handle忙死了，Please check your task,time=$diff" + "ms")
                }
            }
        }
    }

    private var mLastFrameTime = 0L //上次更新时间
    private var mFrameTimeNanos = 0L //当前onFrame时间
    private var mFpsCount = 0 //统计帧数

    private const val frameCountPerSecond = 1000 * 1f / 60 //16ms
    private var mLastFrameTimeNanos = 0L //回调中上次更新时间

    @JvmStatic
    fun traceFrame(mHandler: Handler) {
        frameMonitor()
        val handlerLogger = HandlerLogger()
        mHandler.looper.setMessageLogging(handlerLogger)
    }

    @JvmStatic
    fun frameMonitor() {//帧率检测
        mFpsCount = 0
        mLastFrameTime = 0
        mLastFrameTimeNanos = 0
        mFrameTimeNanos = 0
        Choreographer.getInstance().postFrameCallback(object : FrameCallback {
            override fun doFrame(frameTimeNanos: Long) {
                //Log.d("dq-log", "doFrame $frameTimeNanos")
                val diff = (frameTimeNanos - mLastFrameTimeNanos) / 1_000_000
                if (diff > frameCountPerSecond && mLastFrameTimeNanos > 0) {
                    val droppedFrames = (diff / frameCountPerSecond).roundToInt()
                    if (droppedFrames > 3) {
                        Log.d(TAG, "丢帧 droppedFrames=$droppedFrames")
                    }
                }
                mLastFrameTimeNanos = frameTimeNanos
                mFpsCount++
                mFrameTimeNanos = frameTimeNanos
                Choreographer.getInstance().postFrameCallback(this)
            }
        })

        mHandler.postDelayed(runnable, 1000)
    }

    private var mHandler = Handler(Looper.getMainLooper())

    //定时任务
    private val runnable: Runnable = object : Runnable {
        override fun run() {
            calculateFPS()
            mHandler.postDelayed(this, 1000)
        }
    }

    private fun calculateFPS() {
        if (mLastFrameTime == 0L) {
            mLastFrameTime = mFrameTimeNanos
            return
        }
        val costTime = (mFrameTimeNanos - mLastFrameTime) / 1000000000.0f //纳秒转成毫秒。
        if (mFpsCount <= 0 && costTime <= 0.0f) {
            return
        }
        val fpsResult = (mFpsCount / costTime).toInt()
        //if (fpsResult < 58) {
            Log.w(TAG, "当前帧率为：$fpsResult")
        //}
        mLastFrameTime = mFrameTimeNanos
        mFpsCount = 0
    }

    //setFactory2方法需在super.onCreate方法前调用，否则无效
    fun monitorUICreateView(activity: Activity) {
        // 使用LayoutInflaterCompat.Factory2全局监控Activity界面每一个控件的加载耗时，
        // 也可以做全局的自定义控件替换处理，比如：将TextView全局替换为自定义的TextView。
        androidx.core.view.LayoutInflaterCompat.setFactory2(activity.layoutInflater,
            object : Factory2 {
                override fun onCreateView(parent: View?, name: String, context: Context, attrs: AttributeSet): View? {
                    val time = System.currentTimeMillis()
                    val view: View? = activity.onCreateView(name, context, attrs)
                    val currentTimeMillis = System.currentTimeMillis()
                    Log.d(TAG, name + ",onCreateView cost time:" + (currentTimeMillis - time) + ",view=$view")
                    return view
                }

                override fun onCreateView(name: String, context: Context, attrs: AttributeSet): View? {
                    val time = System.currentTimeMillis()
                    val view: View? = activity.onCreateView(name, context, attrs)
                    val currentTimeMillis = System.currentTimeMillis()
                    Log.d(TAG, name + ",onCreateView22 cost time:" + (currentTimeMillis - time) + ",view=$view")
                    return view
                }
            })
    }
}
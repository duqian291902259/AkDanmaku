package com.kuaishou.akdanmaku.sample

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val entry = findViewById<View>(R.id.entryFullScreen)
        entry.setOnClickListener {
            startActivity(Intent(this, SampleFullScreenActivity::class.java))
        }

        findViewById<View>(R.id.entryFullScreenStep).setOnClickListener {
            startActivity(Intent(this, SampleFullScreenActivity::class.java).putExtra("byStep", true))
        }

        //startActivity(Intent(this, SampleFullScreenActivity::class.java))


        val iv = findViewById<ImageView>(R.id.imageView2)

        iv.setOnClickListener {

            val textView = TextView(this)
            textView.text = "Test 666666666666666666"
            textView.setTextColor(resources.getColor(R.color.white))
            val mLayoutParam =
                WindowManager.LayoutParams(
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.MATCH_PARENT
                )
            mLayoutParam.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY

            // 设置flag
            mLayoutParam.flags =
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
            // 不设置这个弹出框的透明遮罩显示为黑色
            mLayoutParam.format = PixelFormat.TRANSLUCENT
            mLayoutParam.width = WindowManager.LayoutParams.MATCH_PARENT
            mLayoutParam.height = WindowManager.LayoutParams.WRAP_CONTENT

            mLayoutParam.gravity = Gravity.TOP

            this.windowManager?.addView(textView, mLayoutParam)

            it.postDelayed({
                //val view = textView.parent as ViewGroup
                //view.removeView(textView)
                finish()
            }, 2000)

            iv.postDelayed({
                Log.e("dq-test", "removeView")
                /*if (isInvalidActivity(this)) {
                    Log.e("dq-test", "return@postDelayed")
                    return@postDelayed
                }*/
                //this.windowManager?.removeView(iv)
                //this.windowManager?.removeView(textView)

                val view = textView.parent as ViewGroup
                view.removeView(textView)
            }, 4000)

        }
    }

    /**
     * 判断activity上下文是否无效
     *
     * @return true 为无效
     */
    fun isInvalidActivity(context: Context?): Boolean {
        return context !is Activity || context.isFinishing || context
            .isDestroyed
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.e("dq-test", "onDestroy")
    }
}

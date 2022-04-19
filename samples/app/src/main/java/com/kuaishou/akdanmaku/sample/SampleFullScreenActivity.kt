package com.kuaishou.akdanmaku.sample

import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.BackgroundColorSpan
import android.text.style.ImageSpan
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.kuaishou.akdanmaku.data.DanmakuItemData
import com.kuaishou.akdanmaku.ecs.DanmakuEngine
import com.kuaishou.akdanmaku.render.SimpleRenderer
import com.kuaishou.akdanmaku.render.TypedDanmakuRenderer
import com.kuaishou.akdanmaku.ui.DanmakuPlayer
import com.kuaishou.akdanmaku.ui.DanmakuView
import org.json.JSONObject
import utils.UIUtils
import kotlin.random.Random


class SampleFullScreenActivity : AppCompatActivity() {

    companion object {
        private const val MSG_START = 1001
        private const val MSG_UPDATE_DATA = 2001
    }

    private lateinit var danmakuPlayer: DanmakuPlayer
    private lateinit var danmakuPlayController: DanmakuPlayController

    private val byStep by lazy { intent?.getBooleanExtra("byStep", false) ?: false }

    private var paused = false
    private val simpleRenderer = SimpleRenderer()
    private val renderer by lazy {
        TypedDanmakuRenderer(
            simpleRenderer,
            DanmakuItemData.DANMAKU_STYLE_ICON_UP to UpLogoRenderer(
                ResourcesCompat.getDrawable(resources, R.drawable.icon_danmaku_input_text_up_icon, theme)!!
            )
        )
    }

    private val danmakuView by lazy { findViewById<DanmakuView>(R.id.danmakuView) }

    private val mainHandler = object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                MSG_START -> start()
                MSG_UPDATE_DATA -> updateDanmakuData()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_sample_full_creen)

        renderer.registerRenderer(
            DanmakuItemData.DANMAKU_STYLE_USER_AVATAR, UserAvatarRenderer(
                ResourcesCompat.getDrawable(resources, R.drawable.dragon_ball_1, theme)!!
            )
        )

        danmakuPlayer = DanmakuPlayer(renderer).also {
            it.bindView(danmakuView)
        }
        danmakuPlayController = DanmakuPlayController(this, danmakuPlayer, byStep)
        mainHandler.sendEmptyMessageDelayed(MSG_UPDATE_DATA, 1000)
        mainHandler.sendEmptyMessageDelayed(MSG_START, 1500)


        UIUtils.traceFrame(mainHandler)
    }

    override fun onResume() {
        super.onResume()
        if (paused) {
            start()
            paused = false
        }
    }

    private fun start() {
        danmakuPlayController.start()// TODO-dq: 重新开始，应该不用显示已经过期的弹幕
        danmakuPlayer.getCurrentTimeMs()
        mockAddDanmaku()
    }

    private var test = true

    private fun mockAddDanmaku() {
        Thread {
            var i = 0
            val countPreSecond = 5
            while (test) {
                danmakuPlayController.sendDanmaku()
                Thread.sleep(10) //间隔一定的时间发送才不会层叠
                danmakuPlayController.sendDanmaku()
                Thread.sleep(10) //间隔一定的时间发送才不会层叠
                danmakuPlayController.sendDanmaku()
                Thread.sleep(10) //间隔一定的时间发送才不会层叠
                danmakuPlayController.sendDanmaku()
                Thread.sleep(10) //间隔一定的时间发送才不会层叠
                danmakuPlayController.sendDanmaku()
                i++
                try {
                    //Thread.sleep(if (i % countPreSecond == 0) 1000 else 500.toLong())
                    Thread.sleep(1000) //间隔一定的时间发送才不会层叠
                } catch (e: Exception) {
                }
                if (i >= 100000) {
                    test = false
                    try {
                        Thread.currentThread().interrupt()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }.start()
    }

    override fun onPause() {
        super.onPause()
        danmakuPlayController.pause()
        paused = true
    }

    override fun onDestroy() {
        super.onDestroy()

        danmakuPlayer.release()
    }

    private fun updateDanmakuData() {
        Thread {
            Log.d(DanmakuEngine.TAG, "[Sample] 开始加载数据")
            val jsonString = assets.open("test_danmaku_data.json").bufferedReader().use { it.readText() }
            val type = object : TypeToken<List<DanmakuItemData>>() {}.type
            Log.d(DanmakuEngine.TAG, "[Sample] 开始解析数据")
            //val dataList = Gson().fromJson<List<DanmakuItemData>>(jsonString, type)

            val dataList = mockDanmaku()
            danmakuPlayer.updateData(dataList)
            Log.d(DanmakuEngine.TAG, "[Sample] 数据已加载(count = ${dataList.size})")
            danmakuView.post {
                Toast.makeText(this, "数据已加载", Toast.LENGTH_SHORT).show()
            }
        }.start()
        danmakuView.post {
            Toast.makeText(this, "开始加载数据", Toast.LENGTH_SHORT).show()
        }
    }

    private fun mockDanmaku(): MutableList<DanmakuItemData> {
        /**
         *
        "createTime": 1598609868506,
        "userId": 33510000,
        "rank": 5,
        "textColor": 16777215,
        "roleId": 0,
        "danmakuId": 183675597,
        "danmakuType": 0,
        "content": "奥力给",
        "mode": 5,
        "position": 1216524,
        "textSize": 25
         */
        val dataList = mutableListOf<DanmakuItemData>()
        val drawable = resources.getDrawable(R.drawable.dragon_ball_1)
        drawable.setBounds(0, 0, 100, 100)
        for (index in 0..10) {
            val content = createSpannable(drawable, index).toString()
            val itemData = DanmakuItemData(
                1000L + index,
                danmakuPlayer.getCurrentTimeMs() + index,
                content,
                DanmakuItemData.DANMAKU_MODE_ROLLING,
                10+index,
                16777215,
                0+index,
                DanmakuItemData.DANMAKU_STYLE_NONE,
                0 + index,
                1000L + index,
                if(Random.nextBoolean()) DanmakuItemData.MERGED_TYPE_NORMAL else DanmakuItemData.MERGED_TYPE_MERGED
            )
            if (index == 3) {
                itemData.danmakuStyle = DanmakuItemData.DANMAKU_STYLE_SELF_SEND
            } else if (index == 5) {
                itemData.danmakuStyle = DanmakuItemData.DANMAKU_STYLE_ICON_UP
            } else if (index == 8) {
                itemData.danmakuStyle = DanmakuItemData.DANMAKU_STYLE_USER_AVATAR
            }
            dataList.add(itemData)
        }
        return dataList
    }

    private fun createSpannable(drawable: Drawable, index: Int): SpannableStringBuilder {
        val text = "bitmap"
        val spannableStringBuilder = SpannableStringBuilder(text)
        val span = ImageSpan(drawable) //ImageSpan.ALIGN_BOTTOM);
        spannableStringBuilder.setSpan(span, 0, text.length, Spannable.SPAN_INCLUSIVE_EXCLUSIVE)
        spannableStringBuilder.append("图文混排$index")
        spannableStringBuilder
            .setSpan(
                BackgroundColorSpan(Color.parseColor("#8A2233B1")), 0, spannableStringBuilder.length,
                Spannable.SPAN_INCLUSIVE_INCLUSIVE
            )
        return spannableStringBuilder
    }
}

package jp.myuser.supercatapp

import android.content.Context
import android.graphics.Color
import android.media.MediaPlayer
import android.os.Bundle
import android.view.Gravity
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {
    private var mediaPlayer: MediaPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val rootLayout = FrameLayout(this)
        rootLayout.setBackgroundColor(Color.BLACK)

        // 1. 「今日のねこ」と「今日の状態」の決定
        val prefs = getSharedPreferences("CatPrefs", Context.MODE_PRIVATE)
        val todayStr = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())
        
        var catNum = prefs.getInt("cat_num", 1)
        var catStatus = prefs.getString("cat_status", "sleep")

        if (todayStr != prefs.getString("last_date", "")) {
            catNum = (1..7).random()
            catStatus = listOf("sleep", "eat", "play").random()
            prefs.edit().apply {
                putString("last_date", todayStr)
                putInt("cat_num", catNum)
                putString("cat_status", catStatus)
                apply()
            }
        }

        // 2. 猫の画像表示
        val imageView = ImageView(this)
        val imageResId = resources.getIdentifier("cat${catNum}_$catStatus", "drawable", packageName)
        imageView.setImageResource(if (imageResId != 0) imageResId else android.R.drawable.ic_menu_gallery)
        imageView.scaleType = ImageView.ScaleType.CENTER_INSIDE
        rootLayout.addView(imageView)

        // 3. ★「撫でる」という文字をランダムな位置に複数配置
        val words = listOf("撫でて", "なでなで", "撫でる")
        for (i in 1..5) { // 5箇所に表示
            val strokeText = TextView(this)
            strokeText.text = words.random()
            strokeText.setTextColor(Color.parseColor("#80FFFFFF")) // 半透明の白
            strokeText.textSize = (18..28).random().toFloat()
            
            val params = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                // ランダムな位置（マージン）を設定
                gravity = Gravity.TOP or Gravity.START
                setMargins(
                    (50..800).random(), // 横位置
                    (100..1200).random(), // 縦位置
                    0, 0
                )
            }
            rootLayout.addView(strokeText, params)
        }

        // 4. 下部のステータス表示
        val statusText = when(catStatus) {
            "sleep" -> "お休み中"
            "eat" -> "お食事中"
            else -> "お遊び中"
        }
        val dayOfWeek = SimpleDateFormat("EEEE", Locale.JAPANESE).format(Date())
        val textView = TextView(this).apply {
            text = "今日は $dayOfWeek\n猫ちゃんは $statusText です"
            setTextColor(Color.WHITE)
            textSize = 20f
            gravity = Gravity.CENTER
        }
        val textParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.WRAP_CONTENT, 
            FrameLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            gravity = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
            setMargins(0, 0, 0, 80)
        }
        rootLayout.addView(textView, textParams)

        setContentView(rootLayout)

        // 5. タップで音を鳴らす
        rootLayout.setOnClickListener { playSound() }
    }

    private fun playSound() {
        try {
            mediaPlayer?.release()
            val resId = resources.getIdentifier("meow", "raw", packageName)
            if (resId != 0) {
                mediaPlayer = MediaPlayer.create(this, resId)
                mediaPlayer?.start()
                mediaPlayer?.setOnCompletionListener { it.release() }
            }
        } catch (e: Exception) { e.printStackTrace() }
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release()
    }
}

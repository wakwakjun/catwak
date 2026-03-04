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

        // --- 「今日のねこ」と「今日の状態」を決定 ---
        val prefs = getSharedPreferences("CatPrefs", Context.MODE_PRIVATE)
        val todayStr = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())
        
        val lastDate = prefs.getString("last_date", "")
        var catNum = prefs.getInt("cat_num", 1)
        var catStatus = prefs.getString("cat_status", "sleep")

        if (todayStr != lastDate) {
            // 1〜7の猫をランダム選択
            catNum = (1..7).random()
            // 3つの状態をランダム選択
            catStatus = listOf("sleep", "eat", "play").random()
            
            prefs.edit().apply {
                putString("last_date", todayStr)
                putInt("cat_num", catNum)
                putString("cat_status", catStatus)
                apply()
            }
        }

        // 1. 画像の表示 (例: cat1_sleep)
        val imageView = ImageView(this)
        val fileName = "cat${catNum}_$catStatus"
        val imageResId = resources.getIdentifier(fileName, "drawable", packageName)
        
        if (imageResId != 0) {
            imageView.setImageResource(imageResId)
        } else {
            imageView.setImageResource(android.R.drawable.ic_menu_gallery)
        }
        imageView.scaleType = ImageView.ScaleType.CENTER_INSIDE
        rootLayout.addView(imageView)

        // 2. 曜日の表示
        val textView = TextView(this)
        val dayOfWeek = SimpleDateFormat("EEEE", Locale.JAPANESE).format(Date())
        
        // 状態に合わせてメッセージを変える
        val statusText = when(catStatus) {
            "sleep" -> "お休み中"
            "eat" -> "お食事中"
            else -> "お遊び中"
        }
        
        textView.text = "今日は $dayOfWeek\n猫ちゃんは $statusText です"
        textView.setTextColor(Color.WHITE)
        textView.textSize = 22f
        textView.gravity = Gravity.CENTER
        
        val textParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.WRAP_CONTENT,
            FrameLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            gravity = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
            setMargins(0, 0, 0, 100)
        }
        rootLayout.addView(textView, textParams)

        setContentView(rootLayout)

        // 3. タップで音を鳴らす
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

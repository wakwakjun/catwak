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

        // --- 「今日のねこ」を決定するロジック ---
        val prefs = getSharedPreferences("CatPrefs", Context.MODE_PRIVATE)
        val todayStr = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())
        
        // 保存されている日付を取得
        val lastDate = prefs.getString("last_date", "")
        var catId = prefs.getInt("cat_id", 1)

        // もし今日初めてアプリを開いたなら、新しい猫をランダムで選んで保存する
        if (todayStr != lastDate) {
            catId = (1..7).random()
            prefs.edit().apply {
                putString("last_date", todayStr)
                putInt("cat_id", catId)
                apply()
            }
        }

        // 1. 保存された（または新しく選ばれた）猫を表示
        val imageView = ImageView(this)
        val imageResId = resources.getIdentifier("cat$catId", "drawable", packageName)
        
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
        textView.text = "今日は $dayOfWeek です"
        textView.setTextColor(Color.WHITE)
        textView.textSize = 24f
        textView.gravity = Gravity.CENTER
        
        val textParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.WRAP_CONTENT,
            FrameLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            gravity = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
            setMargins(0, 0, 0, 150)
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

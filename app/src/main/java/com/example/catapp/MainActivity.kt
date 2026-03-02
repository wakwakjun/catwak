package jp.myuser.supercatapp

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

        // 1. 土台の作成（ここが壊れることはありません）
        val rootLayout = FrameLayout(this)
        rootLayout.setBackgroundColor(Color.BLACK)

        // 2. 猫の画像表示（エラーが起きやすい場所を保護）
        val imageView = ImageView(this)
        try {
            val randomNum = (1..7).random()
            val imageResId = resources.getIdentifier("cat$randomNum", "drawable", packageName)
            
            if (imageResId != 0) {
                imageView.setImageResource(imageResId)
            } else {
                // 画像がない時はドロイド君を表示
                imageView.setImageResource(android.R.drawable.sym_def_app_icon)
            }
        } catch (e: Exception) {
            // エラーが起きても無視して次へ
        }
        
        imageView.scaleType = ImageView.ScaleType.CENTER_INSIDE
        rootLayout.addView(imageView)

        // 3. 曜日の表示（ここも保護）
        val textView = TextView(this)
        try {
            val sdf = SimpleDateFormat("EEEE", Locale.JAPANESE)
            val dayOfWeek = sdf.format(Date())
            textView.text = "今日は $dayOfWeek です"
        } catch (e: Exception) {
            textView.text = "Hello Cat!"
        }
        
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

        // 4. タップ処理
        rootLayout.setOnClickListener {
            playSound()
        }
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
        } catch (e: Exception) {
            // 音が鳴らなくてもアプリは落とさない
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release()
    }
}

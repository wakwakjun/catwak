package jp.myuser.supercatapp // あなたのパッケージ名に合わせてください

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

        // 1. ランダムな猫を表示（cat1 ~ cat7）
        val imageView = ImageView(this)
        val randomNum = (1..7).random()
        val imageResId = resources.getIdentifier("cat$randomNum", "drawable", packageName)
        
        if (imageResId != 0) {
            imageView.setImageResource(imageResId)
        } else {
            // もし画像がない場合は標準アイコンを表示してエラーを防ぐ
            imageView.setImageResource(android.R.drawable.ic_menu_gallery)
        }
        imageView.scaleType = ImageView.ScaleType.CENTER_INSIDE
        rootLayout.addView(imageView)

        // 2. 曜日を表示
        val textView = TextView(this)
        val sdf = SimpleDateFormat("EEEE", Locale.JAPANESE)
        val dayOfWeek = sdf.format(Date())
        
        textView.text = "今日は $dayOfWeek です"
        
        // ★ここを修正しました！ (textColor -> setTextColor)
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

        // 3. 画面のどこを触っても音が鳴る
        rootLayout.setOnClickListener {
            playSound()
        }
    }

    private fun playSound() {
        mediaPlayer?.release()
        try {
            val resId = resources.getIdentifier("meow", "raw", packageName)
            if (resId != 0) {
                mediaPlayer = MediaPlayer.create(this, resId)
                mediaPlayer?.start()
                mediaPlayer?.setOnCompletionListener { it.release() }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release()
    }
}

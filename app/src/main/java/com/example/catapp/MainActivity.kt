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

        // 1. 画面の土台（レイアウト）を作成
        val rootLayout = FrameLayout(this)
        rootLayout.setBackgroundColor(Color.BLACK) // 背景を黒にして画像を引き立てる

        // 2. ランダムに猫を表示する
        val imageView = ImageView(this)
        val randomNum = (1..7).random() // 1から7の数字をランダムに選ぶ
        val imageResId = resources.getIdentifier("cat$randomNum", "drawable", packageName)
        
        if (imageResId != 0) {
            imageView.setImageResource(imageResId)
        } else {
            imageView.setImageResource(android.R.drawable.ic_menu_gallery)
        }
        imageView.scaleType = ImageView.ScaleType.CENTER_INSIDE
        rootLayout.addView(imageView)

        // 3. 曜日を表示する
        val textView = TextView(this)
        val sdf = SimpleDateFormat("EEEE", Locale.JAPANESE) // 日本語で「〇曜日」と取得
        val dayOfWeek = sdf.format(Date())
        
        textView.text = "今日は $dayOfWeek です"
        textView.textColor = Color.WHITE
        textView.textSize = 24f
        textView.gravity = Gravity.CENTER
        
        // 文字の位置を画面下部に配置
        val textParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.WRAP_CONTENT,
            FrameLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            gravity = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
            setMargins(0, 0, 0, 100) // 下から100ピクセル浮かせる
        }
        rootLayout.addView(textView, textParams)

        setContentView(rootLayout)

        // 4. 画面タップで音を鳴らす
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

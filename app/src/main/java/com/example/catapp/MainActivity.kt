package jp.myuser.supercatapp

import android.media.MediaPlayer
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    private var mediaPlayer: MediaPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // 画面全体の土台（レイアウト）を作成
        val rootLayout = FrameLayout(this)
        rootLayout.layoutParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT
        )

        // 猫の画像を表示するビューを作成
        val imageView = ImageView(this)
        val imageResId = resources.getIdentifier("cat_image", "drawable", packageName)
        if (imageResId != 0) {
            imageView.setImageResource(imageResId)
        } else {
            imageView.setImageResource(android.R.drawable.ic_menu_gallery)
        }
        imageView.scaleType = ImageView.ScaleType.CENTER_INSIDE
        
        // 土台に画像を追加
        rootLayout.addView(imageView)
        setContentView(rootLayout)

        // ★ 修正ポイント：画像ではなく「土台（画面全体）」にタップイベントを設定
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

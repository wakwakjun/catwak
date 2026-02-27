package jp.myuser.supercatapp // あなたのパッケージ名に合わせてください

import android.media.MediaPlayer
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // 画像を表示するビューを作成
        val imageView = ImageView(this)
        
        // ★ ここを修正：drawableフォルダに入れた「cat_image」を表示するように指定
        val imageResId = resources.getIdentifier("cat_image", "drawable", packageName)
        if (imageResId != 0) {
            imageView.setImageResource(imageResId)
        } else {
            // 画像が見つからない場合は標準アイコン（予備）
            imageView.setImageResource(android.R.drawable.ic_menu_gallery)
        }
        
        // 画像を画面いっぱいに表示
        imageView.scaleType = ImageView.ScaleType.CENTER_INSIDE
        setContentView(imageView)

        // タップした時の処理
        imageView.setOnClickListener {
            // ★ ここを修正：rawフォルダに入れた「meow」を再生
            val soundResId = resources.getIdentifier("meow", "raw", packageName)
            if (soundResId != 0) {
                val mp = MediaPlayer.create(this, soundResId)
                mp.start()
                mp.setOnCompletionListener { it.release() }
            }
        }
    }
}

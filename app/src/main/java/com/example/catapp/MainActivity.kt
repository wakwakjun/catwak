package jp.myuser.supercatapp

import android.media.MediaPlayer
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val imageView = ImageView(this)
        // 標準のギャラリーアイコンを設定（これは確実に存在します）
        imageView.setImageResource(android.R.drawable.ic_menu_gallery) 
        setContentView(imageView)

        imageView.setOnClickListener {
            // 注意：標準の 'ok' という音声は存在しないため、
            // 自分の res/raw フォルダに音声を入れるまでは、このクリック処理をコメントアウトするか、
            // 以下のように「もしファイルがあれば再生する」という形にするのが安全です。
            
            /* 自分の音声ファイル（例: cat_meow.mp3）を res/raw に入れたら以下のコメントを解除してください
            val mp = MediaPlayer.create(this, R.raw.cat_meow) 
            mp?.start()
            mp?.setOnCompletionListener { it.release() }
            */
        }
    }
}

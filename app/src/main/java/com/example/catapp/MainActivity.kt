package com.example.catapp

import android.media.MediaPlayer
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val imageView = ImageView(this)
        // ここで画像を設定（あとでdrawableにアップロードするファイル名）
        imageView.setImageResource(android.R.drawable.ic_menu_gallery) 
        setContentView(imageView)

        imageView.setOnClickListener {
            // rawフォルダに置く音声ファイルを再生
            val mp = MediaPlayer.create(this, android.R.raw.ok) // 仮の音
            mp.start()
            mp.setOnCompletionListener { it.release() }
        }
    }
}

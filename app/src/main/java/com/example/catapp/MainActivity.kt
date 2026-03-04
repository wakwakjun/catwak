package jp.myuser.supercatapp

import android.content.Context
import android.graphics.Color
import android.media.MediaPlayer
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {
    private var mediaPlayer: MediaPlayer? = null
    private lateinit var rootLayout: FrameLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        showMainScreen()
    }

    // --- メイン画面（猫を表示する画面） ---
    private fun showMainScreen() {
        rootLayout = FrameLayout(this)
        rootLayout.setBackgroundColor(Color.BLACK)

        val prefs = getSharedPreferences("CatPrefs", Context.MODE_PRIVATE)
        val todayStr = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())
        
        var catNum = prefs.getInt("cat_num", 1)
        var catStatus = prefs.getString("cat_status", "sleep") ?: "sleep"

        // 1. 日付が変わっていたら新しい猫を決定
        if (todayStr != prefs.getString("last_date", "")) {
            catNum = (1..7).random()
            catStatus = listOf("sleep", "eat", "play").random()
            prefs.edit().apply {
                putString("last_date", todayStr)
                putInt("cat_num", catNum)
                putString("cat_status", catStatus)
                // 見た猫を記録（例: "cat1_sleep" を true に）
                putBoolean("seen_cat${catNum}_$catStatus", true)
                apply()
            }
        }

        // 2. 猫の画像表示
        val imageView = ImageView(this)
        val imageResId = resources.getIdentifier("cat${catNum}_$catStatus", "drawable", packageName)
        imageView.setImageResource(if (imageResId != 0) imageResId else android.R.drawable.ic_menu_gallery)
        imageView.scaleType = ImageView.ScaleType.CENTER_INSIDE
        rootLayout.addView(imageView)

        // 3. 「猫度」リンク（左上）
        val catDegreeBtn = TextView(this).apply {
            text = "猫度 >"
            setTextColor(Color.YELLOW)
            textSize = 18f
            setPadding(40, 40, 40, 40)
            setOnClickListener { showDegreeScreen() }
        }
        rootLayout.addView(catDegreeBtn)

        // 4. 下部テキスト（曜日と状態）
        val statusJP = when(catStatus) { "sleep" -> "お休み中" "eat" -> "お食事中" else -> "お遊び中" }
        val dayOfWeek = SimpleDateFormat("EEEE", Locale.JAPANESE).format(Date())
        val textView = TextView(this).apply {
            text = "今日は $dayOfWeek\n猫ちゃんは $statusJP です"
            setTextColor(Color.WHITE)
            textSize = 20f
            gravity = Gravity.CENTER
        }
        val textParams = FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT).apply {
            gravity = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
            setMargins(0, 0, 0, 100)
        }
        rootLayout.addView(textView, textParams)

        setContentView(rootLayout)
        rootLayout.setOnClickListener { playSound() }
    }

    // --- 猫度画面（グラフを表示する画面） ---
    private fun showDegreeScreen() {
        val degreeLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setBackgroundColor(Color.parseColor("#121212")) // 濃いグレー
        }

        val prefs = getSharedPreferences("CatPrefs", Context.MODE_PRIVATE)
        
        // 達成度の計算 (全21種類)
        var seenCount = 0
        for (i in 1..7) {
            for (status in listOf("sleep", "eat", "play")) {
                if (prefs.getBoolean("seen_cat${i}_$status", false)) seenCount++
            }
        }
        val progress = (seenCount.toFloat() / 21f * 100).toInt()

        // タイトル
        val titleView = TextView(this).apply {
            text = "現在の猫度"
            setTextColor(Color.WHITE)
            textSize = 28f
            setPadding(0, 0, 0, 50)
        }
        degreeLayout.addView(titleView)

        // 円形プログレスバー（簡易グラフ）
        val progressBar = ProgressBar(this, null, android.R.attr.progressBarStyleLarge).apply {
            isIndeterminate = false
            max = 100
            this.progress = progress
            scaleX = 3f
            scaleY = 3f
        }
        degreeLayout.addView(progressBar)

        // パーセント表示
        val percentView = TextView(this).apply {
            text = "$progress %"
            setTextColor(Color.YELLOW)
            textSize = 40f
            setPadding(0, 100, 0, 100)
        }
        degreeLayout.addView(percentView)

        // 戻るリンク
        val backBtn = TextView(this).apply {
            text = "< 戻る"
            setTextColor(Color.CYAN)
            textSize = 20f
            setPadding(40, 40, 40, 40)
            setOnClickListener { showMainScreen() }
        }
        degreeLayout.addView(backBtn)

        setContentView(degreeLayout)
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

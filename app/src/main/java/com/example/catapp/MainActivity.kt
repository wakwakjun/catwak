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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        showMainScreen()
    }

    // --- メイン画面 ---
    private fun showMainScreen() {
        // 土台となるLayout
        val rootLayout = FrameLayout(this)
        rootLayout.setBackgroundColor(Color.BLACK)

        val prefs = getSharedPreferences("CatPrefs", Context.MODE_PRIVATE)
        val todayStr = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())
        
        // 1. 【ロジック修正】猫の種類(1-7)は1日固定、ポーズ(sleep/eat/play)は毎回ランダム
        var catNum = prefs.getInt("cat_num", 1)
        if (todayStr != prefs.getString("last_date", "")) {
            catNum = (1..7).random()
            prefs.edit().putString("last_date", todayStr).putInt("cat_num", catNum).apply()
        }
        
        // ポーズは起動のたびにランダム
        val currentStatus = listOf("sleep", "eat", "play").random()
        
        // 見た猫のフラグを保存（猫度用）
        prefs.edit().putBoolean("seen_cat${catNum}_$currentStatus", true).apply()

        // 2. 猫の画像表示
        val imageView = ImageView(this)
        val imageResId = resources.getIdentifier("cat${catNum}_$currentStatus", "drawable", packageName)
        imageView.setImageResource(if (imageResId != 0) imageResId else android.R.drawable.ic_menu_gallery)
        imageView.scaleType = ImageView.ScaleType.CENTER_INSIDE
        
        // 【重要】画像自体にクリックリスナーをつけて音を鳴らす
        imageView.setOnClickListener { playSound() }
        rootLayout.addView(imageView)

        // 3. 猫度リンク（左上）
        val catDegreeBtn = TextView(this).apply {
            text = "● 猫度を確認"
            setTextColor(Color.YELLOW)
            textSize = 20f
            setPadding(50, 50, 50, 50)
            // ボタンとして独立させる
            setOnClickListener { showDegreeScreen() }
        }
        rootLayout.addView(catDegreeBtn)

        // 4. 下部テキスト
        val statusJP = when(currentStatus) { "sleep" -> "お休み中" "eat" -> "お食事中" else -> "お遊び中" }
        val dayOfWeek = SimpleDateFormat("EEEE", Locale.JAPANESE).format(Date())
        val textView = TextView(this).apply {
            text = "今日は $dayOfWeek\n猫ちゃんは $statusJP です\n(画面をタップしてね)"
            setTextColor(Color.WHITE)
            textSize = 18f
            gravity = Gravity.CENTER
        }
        val textParams = FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT).apply {
            gravity = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
            setMargins(0, 0, 0, 120)
        }
        rootLayout.addView(textView, textParams)

        setContentView(rootLayout)
    }

    // --- 猫度画面 ---
    private fun showDegreeScreen() {
        val degreeLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setBackgroundColor(Color.parseColor("#1A1A1A"))
            setPadding(40, 40, 40, 40)
        }

        val prefs = getSharedPreferences("CatPrefs", Context.MODE_PRIVATE)
        var seenCount = 0
        for (i in 1..7) {
            for (s in listOf("sleep", "eat", "play")) {
                if (prefs.getBoolean("seen_cat${i}_$s", false)) seenCount++
            }
        }
        val progressPercent = (seenCount.toFloat() / 21f * 100).toInt()

        // タイトル
        val titleView = TextView(this).apply {
            text = "コンプリートまであと少し！"
            setTextColor(Color.WHITE)
            textSize = 24f
            setPadding(0, 0, 0, 80)
        }
        degreeLayout.addView(titleView)

        // 円形グラフ（ProgressBarのスタイルを調整）
        val progressBar = ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal).apply {
            // 水平バーではなく、カスタムで円形っぽく見せるか、標準の大きな円を使う
            // 今回は確実に表示されるよう「大きな円形」をベースにします
        }
        val circleProgress = ProgressBar(this, null, android.R.attr.progressBarStyleLarge).apply {
            isIndeterminate = false
            max = 100
            progress = progressPercent
            // 見た目を大きくする
            scaleX = 2.5f
            scaleY = 2.5f
        }
        degreeLayout.addView(circleProgress)

        // パーセント表示
        val percentView = TextView(this).apply {
            text = "猫度：$progressPercent %"
            setTextColor(Color.YELLOW)
            textSize = 36f
            setPadding(0, 120, 0, 80)
        }
        degreeLayout.addView(percentView)

        // 戻るボタン（ボタンらしく背景をつける）
        val backBtn = Button(this).apply {
            text = "戻る"
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

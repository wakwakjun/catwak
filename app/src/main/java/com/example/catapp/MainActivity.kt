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

    // --- メイン画面（猫を表示する画面） ---
    private fun showMainScreen() {
        val rootLayout = FrameLayout(this)
        rootLayout.setBackgroundColor(Color.BLACK)

        val prefs = getSharedPreferences("CatPrefs", Context.MODE_PRIVATE)
        val todayStr = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())
        
        var catNum = prefs.getInt("cat_num", 1)
        if (todayStr != prefs.getString("last_date", "")) {
            catNum = (1..7).random()
            prefs.edit().putString("last_date", todayStr).putInt("cat_num", catNum).apply()
        }
        
        // 起動のたびにランダムでポーズを選択
        val currentStatus = listOf("sleep", "eat", "play").random()
        // コレクション保存
        prefs.edit().putBoolean("seen_cat${catNum}_$currentStatus", true).apply()

        // 1. 猫の画像（背景全体）
        val imageView = ImageView(this).apply {
            val imageResId = resources.getIdentifier("cat${catNum}_$currentStatus", "drawable", packageName)
            setImageResource(if (imageResId != 0) imageResId else android.R.drawable.ic_menu_gallery)
            scaleType = ImageView.ScaleType.CENTER_INSIDE
            isClickable = true
            setOnClickListener { playSound() }
        }
        rootLayout.addView(imageView, FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT)

        // 2. 猫度ボタンのコンテナ（左上に配置）
        val buttonContainer = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(30, 30, 30, 30)
            isClickable = true
            isFocusable = true
            setOnClickListener { showDegreeScreen() }
        }
        
        val catDegreeBtn = TextView(this).apply {
            text = "● 猫度を確認 >"
            setTextColor(Color.YELLOW)
            textSize = 20f
            setBackgroundColor(Color.parseColor("#88000000")) // 視認性のため少し濃く
            setPadding(25, 15, 25, 15)
        }
        buttonContainer.addView(catDegreeBtn)
        
        val btnParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.WRAP_CONTENT,
            FrameLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            topMargin = 40
            leftMargin = 40
        }
        rootLayout.addView(buttonContainer, btnParams)

        // 3. 下部テキスト（曜日と状態）
        val statusJP = when(currentStatus) { "sleep" -> "お休み中" "eat" -> "お食事中" else -> "お遊び中" }
        val dayOfWeek = SimpleDateFormat("EEEE", Locale.JAPANESE).format(Date())
        val textView = TextView(this).apply {
            text = "今日は $dayOfWeek\n猫ちゃんは $statusJP です"
            setTextColor(Color.WHITE)
            textSize = 18f
            gravity = Gravity.CENTER
        }
        val textParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.WRAP_CONTENT, 
            FrameLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            gravity = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
            setMargins(0, 0, 0, 120)
        }
        rootLayout.addView(textView, textParams)

        setContentView(rootLayout)
    }

    // --- 猫度画面（達成度グラフ） ---
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

        val titleView = TextView(this).apply {
            text = "コンプリートまであと少し！"
            setTextColor(Color.WHITE)
            textSize = 24f
            setPadding(0, 0, 0, 100)
        }
        degreeLayout.addView(titleView)

        // 円形グラフの表示（より安定したスタイルを使用）
        val circleProgress = ProgressBar(this, null, android.R.attr.progressBarStyleLarge).apply {
            isIndeterminate = false
            max = 100
            progress = progressPercent
            scaleX = 3.0f // サイズをさらに大きく
            scaleY = 3.0f
        }
        degreeLayout.addView(circleProgress)

        val percentView = TextView(this).apply {
            text = "猫度：$progressPercent %"
            setTextColor(Color.YELLOW)
            textSize = 36f
            setPadding(0, 150, 0, 80)
        }
        degreeLayout.addView(percentView)

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

package jp.myuser.supercatapp

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
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

    // --- 共通機能：アプリ終了ボタンを作成（反応を劇的に改善） ---
    private fun createExitButton(): View {
        // 1. ボタンを包む「座布団（コンテナ）」を作る
        val touchArea = FrameLayout(this).apply {
            // タップできる範囲を広げる（48dp相当以上の余白を確保）
            setPadding(40, 40, 40, 40) 
            setOnClickListener { finish() } // コンテナごとタップ可能にする
            isClickable = true
            isFocusable = true
        }

        // 2. 見た目としての「✕」テキスト
        val xText = TextView(this).apply {
            text = "✕"
            setTextColor(Color.WHITE)
            textSize = 22f
            gravity = Gravity.CENTER
            // 少し背景を濃くして、猫画像の上でも見やすく
            setBackgroundColor(Color.parseColor("#88000000")) 
            setPadding(20, 10, 20, 10)
        }

        touchArea.addView(xText)
        return touchArea
    }

    // --- メイン画面 ---
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
        
        val currentStatus = listOf("sleep", "eat", "play").random()
        prefs.edit().putBoolean("seen_cat${catNum}_$currentStatus", true).apply()

        // 1. 猫の画像
        val imageView = ImageView(this).apply {
            val imageResId = resources.getIdentifier("cat${catNum}_$currentStatus", "drawable", packageName)
            setImageResource(if (imageResId != 0) imageResId else android.R.drawable.ic_menu_gallery)
            scaleType = ImageView.ScaleType.CENTER_INSIDE
            isClickable = true
            setOnClickListener { playSound() }
        }
        rootLayout.addView(imageView, FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT)

        // 2. 猫度ボタン（タッチ範囲を拡大し、見た目を改善）
        val buttonContainer = FrameLayout(this).apply {
            setPadding(20, 20, 20, 20)
            setOnClickListener { showDegreeScreen() }
        }
        
        val catDegreeBtn = TextView(this).apply {
            text = "● 猫度を確認 >"
            setTextColor(Color.YELLOW)
            textSize = 18f
            setTypeface(null, Typeface.BOLD)
            setBackgroundColor(Color.parseColor("#AA000000")) // 濃いめの背景で視覚化
            setPadding(40, 30, 40, 30) // 内側の余白を増やしてタッチしやすく
        }
        buttonContainer.addView(catDegreeBtn)
        
        val btnParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.WRAP_CONTENT,
            FrameLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            topMargin = 30
            leftMargin = 30
        }
        rootLayout.addView(buttonContainer, btnParams)

        // 3. 終了ボタン（右上に配置）
        val exitBtnParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.WRAP_CONTENT,
            FrameLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            gravity = Gravity.TOP or Gravity.END
            topMargin = 30
            rightMargin = 30
        }
        rootLayout.addView(createExitButton(), exitBtnParams)

        // 4. 下部テキスト
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

    // --- 猫度画面 ---
    private fun showDegreeScreen() {
        val rootLayout = FrameLayout(this) // 重ね合わせのためにFrameLayoutを使用

        val degreeLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setBackgroundColor(Color.parseColor("#121212"))
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
            text = "Cat Collection Degree"
            setTextColor(Color.WHITE)
            textSize = 22f
            setPadding(0, 0, 0, 80)
            setTypeface(null, Typeface.BOLD)
        }
        degreeLayout.addView(titleView)

        val catGraph = CatDegreeView(this).apply {
            progress = progressPercent
        }
        val graphParams = LinearLayout.LayoutParams(600, 600)
        degreeLayout.addView(catGraph, graphParams)

        val percentView = TextView(this).apply {
            text = "$progressPercent%"
            setTextColor(Color.YELLOW)
            textSize = 48f
            setPadding(0, 60, 0, 60)
            setTypeface(Typeface.MONOSPACE)
        }
        degreeLayout.addView(percentView)

        val backBtn = Button(this).apply {
            text = "Back to Home"
            setOnClickListener { showMainScreen() }
        }
        degreeLayout.addView(backBtn)

        rootLayout.addView(degreeLayout)

        // 終了ボタン（右上）
        val exitBtnParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.WRAP_CONTENT,
            FrameLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            gravity = Gravity.TOP or Gravity.END
            topMargin = 30
            rightMargin = 30
        }
        rootLayout.addView(createExitButton(), exitBtnParams)

        setContentView(rootLayout)
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

// --- カスタム描画クラス（変更なし） ---
class CatDegreeView(context: Context) : View(context) {
    var progress: Int = 0
        set(value) {
            field = value
            invalidate()
        }

    private val paint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
        style = android.graphics.Paint.Style.STROKE
        strokeCap = android.graphics.Paint.Cap.ROUND
        strokeWidth = 60f
    }

    override fun onDraw(canvas: android.graphics.Canvas) {
        super.onDraw(canvas)
        val center = width / 2f
        val radius = (width / 2f) - 50f
        val rect = android.graphics.RectF(center - radius, center - radius, center + radius, center + radius)

        paint.shader = null
        paint.color = Color.parseColor("#333333")
        canvas.drawCircle(center, center, radius, paint)

        val gradient = android.graphics.SweepGradient(center, center, 
            intArrayOf(Color.YELLOW, Color.parseColor("#FF8C00"), Color.YELLOW), null)
        paint.shader = gradient
        
        canvas.save()
        canvas.rotate(-90f, center, center)
        val sweepAngle = (progress / 100f) * 360f
        canvas.drawArc(rect, 0f, sweepAngle, false, paint)
        canvas.restore()
        
        paint.shader = null
    }
}

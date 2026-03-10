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

    // --- 終了ボタン：タッチ範囲を大幅拡大 ---
    private fun createExitButton(): View {
        val touchArea = FrameLayout(this).apply {
            setPadding(80, 80, 80, 80) 
            setOnClickListener { finish() }
            isClickable = true
            isFocusable = true
            // エラーの原因だった箇所を最も安全な書き方に修正
            val outValue = android.util.TypedValue()
            theme.resolveAttribute(android.R.attr.selectableItemBackgroundBorderless, outValue, true)
            setBackgroundResource(outValue.resourceId)
        }

        val xText = TextView(this).apply {
            text = "✕"
            setTextColor(Color.WHITE)
            textSize = 24f 
            gravity = Gravity.CENTER
            setBackgroundColor(Color.parseColor("#AA000000")) 
            setPadding(30, 15, 30, 15) 
        }

        touchArea.addView(xText)
        return touchArea
    }

    private fun showMainScreen() {
        // 1. 時間とステータスの判定
        val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        val isNight = currentHour >= 22 || currentHour < 6

        val prefs = getSharedPreferences("CatPrefs", Context.MODE_PRIVATE)
        val todayStr = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())
        
        var catNum = prefs.getInt("cat_num", 1)
        if (todayStr != prefs.getString("last_date", "")) {
            catNum = (1..7).random()
            prefs.edit().putString("last_date", todayStr).putInt("cat_num", catNum).apply()
        }
        
        val randomStatus = listOf("sleep", "eat", "play").random()
        val currentStatus = if (isNight) "sleep" else randomStatus
        
        prefs.edit().putBoolean("seen_cat${catNum}_$currentStatus", true).apply()

        // 2. レイアウト構築
        val rootLayout = FrameLayout(this).apply {
            contentDescription = "main_screen"
            setBackgroundColor(if (isNight) Color.parseColor("#000011") else Color.BLACK)
        }

        // 1. 猫の画像
        val imageView = ImageView(this).apply {
            val imageResId = resources.getIdentifier("cat${catNum}_$currentStatus", "drawable", packageName)
            setImageResource(if (imageResId != 0) imageResId else android.R.drawable.ic_menu_gallery)
            scaleType = ImageView.ScaleType.CENTER_INSIDE
            
            if (isNight) {
                setColorFilter(Color.parseColor("#99BBBBBB"), android.graphics.PorterDuff.Mode.MULTIPLY)
            }

            // アニメーション（テスト中はオフ）
            val isTesting = try { Class.forName("androidx.test.espresso.Espresso"); true } catch (e: Exception) { false }
            if (!isTesting) {
                val breathing = android.view.animation.ScaleAnimation(
                    1.0f, 1.05f, 1.0f, 1.05f, 
                    android.view.animation.Animation.RELATIVE_TO_SELF, 0.5f,
                    android.view.animation.Animation.RELATIVE_TO_SELF, 0.5f
                ).apply {
                    duration = 3500
                    repeatCount = android.view.animation.Animation.INFINITE
                    repeatMode = android.view.animation.Animation.REVERSE
                    interpolator = android.view.animation.AccelerateDecelerateInterpolator()
                }
                startAnimation(breathing)
            }

            setOnClickListener { 
                playSound()
                val loveCount = prefs.getInt("love_count", 0) + 1
                prefs.edit().putInt("love_count", loveCount).apply()
                
                if (loveCount % 5 == 0) {
                    val jump = android.view.animation.TranslateAnimation(0f, 0f, 0f, -20f).apply {
                        duration = 100
                        repeatCount = 1
                        repeatMode = android.view.animation.Animation.REVERSE
                    }
                    startAnimation(jump)
                    Toast.makeText(context, "猫ちゃんが喜んでいます！ (Love: $loveCount)", Toast.LENGTH_SHORT).show()
                }
            }
        }
        rootLayout.addView(imageView, FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT)

        // 2. 猫度ボタン
        val buttonContainer = FrameLayout(this).apply {
            setPadding(60, 60, 60, 60)
            setOnClickListener { showDegreeScreen() }
            isClickable = true
            // 波紋エフェクト
            val outValue = android.util.TypedValue()
            theme.resolveAttribute(android.R.attr.selectableItemBackgroundBorderless, outValue, true)
            setBackgroundResource(outValue.resourceId)
        }
        
        val catDegreeBtn = TextView(this).apply {
            text = "● 猫度を確認 >"
            setTextColor(if (isNight) Color.GRAY else Color.YELLOW)
            textSize = 20f
            setTypeface(null, Typeface.BOLD)
            setBackgroundColor(Color.parseColor("#AA000000"))
            setPadding(50, 40, 50, 40)
        }
        buttonContainer.addView(catDegreeBtn)
        
        val btnParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            topMargin = 30
            leftMargin = 30
        }
        rootLayout.addView(buttonContainer, btnParams)

        // 3. 終了ボタンの配置
        val exitBtnParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT
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
            setTextColor(if (isNight) Color.LTGRAY else Color.WHITE)
            textSize = 18f
            gravity = Gravity.CENTER
        }
        val textParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            gravity = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
            setMargins(0, 0, 0, 120)
        }
        rootLayout.addView(textView, textParams)

        setContentView(rootLayout)
    }

    private fun showDegreeScreen() {
        val rootLayout = FrameLayout(this)
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

        val catGraph = CatDegreeView(this).apply { progress = progressPercent }
        degreeLayout.addView(catGraph, LinearLayout.LayoutParams(600, 600))

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
        
        val exitBtnParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT
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

class CatDegreeView(context: Context) : View(context) {
    var progress: Int = 0
        set(value) { field = value; invalidate() }

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
        canvas.drawArc(rect, 0f, (progress / 100f) * 360f, false, paint)
        canvas.restore()
        
        paint.shader = null
    }
}

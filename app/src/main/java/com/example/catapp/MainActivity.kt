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

    private fun createExitButton(): View {
        val touchArea = FrameLayout(this).apply {
            setPadding(40, 40, 40, 40) 
            setOnClickListener { finish() }
            isClickable = true
            isFocusable = true
        }

        val xText = TextView(this).apply {
            text = "✕"
            setTextColor(Color.WHITE)
            textSize = 22f
            gravity = Gravity.CENTER
            setBackgroundColor(Color.parseColor("#88000000")) 
            setPadding(20, 10, 20, 10)
        }

        touchArea.addView(xText)
        return touchArea
    }

    private fun showMainScreen() {
        // --- 1. 時間とステータスの判定 ---
        val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        val isNight = currentHour >= 22 || currentHour < 6

        val prefs = getSharedPreferences("CatPrefs", Context.MODE_PRIVATE)
        val todayStr = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())
        
        var catNum = prefs.getInt("cat_num", 1)
        if (todayStr != prefs.getString("last_date", "")) {
            catNum = (1..7).random()
            prefs.edit().putString("last_date", todayStr).putInt("cat_num", catNum).apply()
        }
        
        // 夜間は強制的に「sleep」、それ以外はランダム
        val randomStatus = listOf("sleep", "eat", "play").random()
        val currentStatus = if (isNight) "sleep" else randomStatus
        
        prefs.edit().putBoolean("seen_cat${catNum}_$currentStatus", true).apply()

        // --- 2. レイアウト構築 ---
        val rootLayout = FrameLayout(this).apply {
            contentDescription = "main_screen"
            // 夜間はさらに深い紺色にする
            setBackgroundColor(if (isNight) Color.parseColor("#000011") else Color.BLACK)
        }

        // 1. 猫の画像
        val imageView = ImageView(this).apply {
            val imageResId = resources.getIdentifier("cat${catNum}_$currentStatus", "drawable", packageName)
            setImageResource(if (imageResId != 0) imageResId else android.R.drawable.ic_menu_gallery)
            scaleType = ImageView.ScaleType.CENTER_INSIDE
            
            // 夜間は画像を少し暗くして目に優しくする
            if (isNight) {
                setColorFilter(Color.parseColor("#99BBBBBB"), android.graphics.PorterDuff.Mode.MULTIPLY)
            }

            // テスト中でなければアニメーション開始
            val isTesting = try { Class.forName("androidx.test.espresso.Espresso"); true } catch (e: Exception) { false }
            if (!isTesting) {
                val breathing = android.view.animation.ScaleAnimation(
                    1.0f, 1.05f, 1.0f, 1.05f, 
                    android.view.animation.Animation.RELATIVE_TO_SELF, 0.5f,
                    android.view.animation.Animation.RELATIVE_TO_SELF, 0.5f
                ).apply {
                    duration = 3500 // 夜は少しゆっくり呼吸
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
            // ここもPaddingを60まで広げ、ボタンの周囲も反応するようにします
            setPadding(60, 60, 60, 60)
            setOnClickListener { showDegreeScreen() }
            isClickable = true
            setBackgroundResource(android.R.resource.selectableItemBackgroundBorderless)
        }
        
        val catDegreeBtn = TextView(this).apply {
            text = "● 猫度を確認 >"
            setTextColor(if (isNight) Color.GRAY else Color.YELLOW)
            textSize = 20f // 読みやすく少しアップ
            setTypeface(null, Typeface.BOLD)
            setBackgroundColor(Color.parseColor("#AA000000"))
            setPadding(50, 40, 50, 40) // 内側の余白も増やして「太らせる」
        }
        buttonContainer.addView(catDegreeBtn)
        rootLayout.addView(buttonContainer, FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT
        ).apply { gravity = Gravity.TOP or Gravity.START; topMargin = 30; leftMargin = 30 })

        // 3. 終了ボタン
        private fun createExitButton(): View {
        val touchArea = FrameLayout(this).apply {
            // Paddingを80に広げ、画面の隅を適当に叩いても反応するようにします
            setPadding(80, 80, 80, 80) 
            setOnClickListener { finish() }
            isClickable = true
            isFocusable = true
            // タップ時に波紋が出るように（任意）
            setBackgroundResource(android.R.resource.selectableItemBackgroundBorderless)
        }

        val xText = TextView(this).apply {
            text = "✕"
            setTextColor(Color.WHITE)
            textSize = 24f // 少し大きく
            gravity = Gravity.CENTER
            setBackgroundColor(Color.parseColor("#AA000000")) 
            setPadding(30, 15, 30, 15) // 見た目のボタンも少し大きく
        }

        touchArea.addView(xText)
        return touchArea
    }

        // 4. 下部テキスト
        val statusJP = when(currentStatus) { "sleep" -> "お休み中" "eat" -> "お食事中" else -> "お遊び中" }
        val dayOfWeek = SimpleDateFormat("EEEE", Locale.JAPANESE).format(Date())
        val textView = TextView(this).apply {
            text = "今日は $dayOfWeek\n猫ちゃんは $statusJP です"
            setTextColor(if (isNight) Color.LTGRAY else Color.WHITE)
            textSize = 18f
            gravity = Gravity.CENTER
        }
        rootLayout.addView(textView, FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT
        ).apply { gravity = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL; setMargins(0, 0, 0, 120) })

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

        val prefs = getSharedPreferences("CatPrefs", Context

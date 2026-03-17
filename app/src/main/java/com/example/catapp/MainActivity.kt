package jp.myuser.supercatapp

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.media.MediaPlayer
import android.os.Bundle
import android.util.TypedValue
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

    // --- 共通機能：巨大な当たり判定を持つ終了ボタン ---
    private fun createExitButton(): View {
        return FrameLayout(this).apply {
            setPadding(80, 80, 80, 80) // 80dpの巨大な反応範囲
            setOnClickListener { finish() }
            isClickable = true
            
            // タップ時の波紋エフェクト設定
            val outValue = TypedValue()
            theme.resolveAttribute(android.R.attr.selectableItemBackgroundBorderless, outValue, true)
            setBackgroundResource(outValue.resourceId)

            addView(TextView(context).apply {
                text = "✕"
                setTextColor(Color.WHITE)
                textSize = 24f
                gravity = Gravity.CENTER
                setBackgroundColor(Color.parseColor("#AA000000"))
                setPadding(30, 15, 30, 15)
            })
        }
    }

    private fun showMainScreen() {
        // 1. 環境判定（ナイトモード：22時〜6時）
        val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        val isNight = currentHour >= 22 || currentHour < 6

        // 2. データ管理（SharedPreferences）
        val prefs = getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
        val todayStr = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())
        
        var animalNum = prefs.getInt("animal_num", 1)
        if (todayStr != prefs.getString("last_date", "")) {
            animalNum = (1..7).random()
            prefs.edit().putString("last_date", todayStr).putInt("animal_num", animalNum).apply()
        }
        
        val randomStatus = listOf("sleep", "eat", "play").random()
        val currentStatus = if (isNight) "sleep" else randomStatus
        
        // 3. レイアウト（背景色切り替え）
        val rootLayout = FrameLayout(this).apply {
            setBackgroundColor(if (isNight) Color.parseColor("#000011") else Color.BLACK)
        }

        // 4. メイン画像（呼吸アニメーション付き）
        val imageView = ImageView(this).apply {
            val resName = "cat${animalNum}_$currentStatus" // ★ここを parrot に変えればインコ版！
            val imageResId = resources.getIdentifier(resName, "drawable", packageName)
            setImageResource(if (imageResId != 0) imageResId else android.R.drawable.ic_menu_gallery)
            scaleType = ImageView.ScaleType.CENTER_INSIDE
            
            if (isNight) setColorFilter(Color.parseColor("#99BBBBBB"), android.graphics.PorterDuff.Mode.MULTIPLY)

            // 呼吸アニメーション
            val isTesting = try { Class.forName("androidx.test.espresso.Espresso"); true } catch (e: Exception) { false }
            if (!isTesting) {
                startAnimation(android.view.animation.ScaleAnimation(
                    1.0f, 1.05f, 1.0f, 1.05f, 
                    android.view.animation.Animation.RELATIVE_TO_SELF, 0.5f,
                    android.view.animation.Animation.RELATIVE_TO_SELF, 0.5f
                ).apply {
                    duration = if (isNight) 4000 else 3000
                    repeatCount = android.view.animation.Animation.INFINITE
                    repeatMode = android.view.animation.Animation.REVERSE
                    interpolator = android.view.animation.AccelerateDecelerateInterpolator()
                })
            }

            setOnClickListener { 
                playSound()
                val love = prefs.getInt("love_count", 0) + 1
                prefs.edit().putInt("love_count", love).apply()
                if (love % 5 == 0) {
                    startAnimation(android.view.animation.TranslateAnimation(0f, 0f, 0f, -20f).apply {
                        duration = 100; repeatCount = 1; repeatMode = android.view.animation.Animation.REVERSE
                    })
                    Toast.makeText(context, "Love: $love", Toast.LENGTH_SHORT).show()
                }
            }
        }
        rootLayout.addView(imageView, FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT)

        // 5. コレクションボタン（巨大判定 60dp）
        val btnContainer = FrameLayout(this).apply {
            setPadding(60, 60, 60, 60)
            setOnClickListener { showCollectionScreen() }
            val outValue = TypedValue()
            theme.resolveAttribute(android.R.attr.selectableItemBackgroundBorderless, outValue, true)
            setBackgroundResource(outValue.resourceId)
        }
        btnContainer.addView(TextView(this).apply {
            text = "● Collection >"
            setTextColor(if (isNight) Color.GRAY else Color.YELLOW)
            textSize = 20f
            setTypeface(null, Typeface.BOLD)
            setBackgroundColor(Color.parseColor("#AA000000"))
            setPadding(50, 40, 50, 40)
        })
        rootLayout.addView(btnContainer, FrameLayout.LayoutParams(-2, -2).apply { topMargin = 30; leftMargin = 30 })

        // 6. 終了ボタン（右上に配置）
        rootLayout.addView(createExitButton(), FrameLayout.LayoutParams(-2, -2).apply { 
            gravity = Gravity.TOP or Gravity.END; topMargin = 30; rightMargin = 30 
        })

        // 7. 下部テキスト（ブラジル対応：Locale.getDefault）
        val statusText = when(currentStatus) { "sleep" -> "Zzz..." "eat" -> "Yum!" else -> "Play!" }
        val dayText = SimpleDateFormat("EEEE", Locale.getDefault()).format(Date())
        rootLayout.addView(TextView(this).apply {
            text = "$dayText\n$statusText"
            setTextColor(if (isNight) Color.LTGRAY else Color.WHITE)
            textSize = 18f
            gravity = Gravity.CENTER
        }, FrameLayout.LayoutParams(-2, -2).apply { 
            gravity = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL; setMargins(0, 0, 0, 120) 
        })

        setContentView(rootLayout)
    }

    private fun showCollectionScreen() {
        val rootLayout = FrameLayout(this)
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL; gravity = Gravity.CENTER
            setBackgroundColor(Color.parseColor("#121212")); setPadding(40, 40, 40, 40)
        }

        val prefs = getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
        var count = 0
        for (i in 1..7) for (s in listOf("sleep", "eat", "play")) if (prefs.getBoolean("seen_${i}_$s", false)) count++
        val progress = (count.toFloat() / 21f * 100).toInt()

        layout.addView(TextView(this).apply { text = "Collection"; setTextColor(Color.WHITE); textSize = 22f; setPadding(0,0,0,80) })
        layout.addView(View(this).apply { /* グラフ描画クラスは下に分離 */ }, LinearLayout.LayoutParams(600, 600))
        layout.addView(TextView(this).apply { text = "$progress%"; setTextColor(Color.YELLOW); textSize = 48f; setPadding(0,60,0,60) })
        layout.addView(Button(this).apply { text = "Back"; setOnClickListener { showMainScreen() } })

        rootLayout.addView(layout)
        rootLayout.addView(createExitButton(), FrameLayout.LayoutParams(-2, -2).apply { gravity = Gravity.TOP or Gravity.END; topMargin = 30; rightMargin = 30 })
        setContentView(rootLayout)
    }

    private fun playSound() {
        try {
            mediaPlayer?.release()
            val resId = resources.getIdentifier("sound_effect", "raw", packageName)
            if (resId != 0) {
                mediaPlayer = MediaPlayer.create(this, resId)
                mediaPlayer?.start()
            }
        } catch (e: Exception) {}
    }

    override fun onDestroy() { super.onDestroy(); mediaPlayer?.release() }
}

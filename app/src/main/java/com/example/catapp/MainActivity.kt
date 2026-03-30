package jp.myuser.supercatapp

import android.content.Context
import android.graphics.* // ★重要：描画クラスを一括インポート
import android.media.MediaPlayer
import android.os.Bundle
import android.util.TypedValue
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.animation.*
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {
    private var mediaPlayer: MediaPlayer? = null
    private var lastTapTime: Long = 0
    private lateinit var effectLayer: EffectView // ★追加

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        showMainScreen()
    }

    private fun createExitButton(): View {
        return FrameLayout(this).apply {
            setPadding(80, 80, 80, 80)
            setOnClickListener { finish() }
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
        val prefs = getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
        val now = System.currentTimeMillis()

        // --- A. 放置減少ロジック ---
        val lastTime = prefs.getLong("last_open_time", now)
        val hoursPassed = (now - lastTime) / (1000 * 60 * 60)
        if (hoursPassed > 0) {
            val currentLove = prefs.getInt("love_count", 0)
            prefs.edit().putInt("love_count", Math.max(0, currentLove - (hoursPassed * 5).toInt())).apply()
        }
        prefs.edit().putLong("last_open_time", now).apply()

        // --- B. 環境判定 ---
        val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        val isNight = currentHour >= 22 || currentHour < 6
        val todayStr = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())
        
        var animalNum = prefs.getInt("animal_num", 1)
        if (todayStr != prefs.getString("last_date", "")) {
            animalNum = (1..7).random()
            prefs.edit().putString("last_date", todayStr).putInt("animal_num", animalNum).apply()
        }
        val currentStatus = if (isNight) "sleep" else listOf("sleep", "eat", "play").random()
        prefs.edit().putBoolean("seen_${animalNum}_$currentStatus", true).apply()

        // --- C. UI構築 ---
        val rootLayout = FrameLayout(this).apply {
            setBackgroundColor(if (isNight) Color.parseColor("#000011") else Color.BLACK)
        }

        val imageView = ImageView(this).apply {
            val resName = "cat${animalNum}_$currentStatus"
            val imageResId = resources.getIdentifier(resName, "drawable", packageName)
            setImageResource(if (imageResId != 0) imageResId else android.R.drawable.ic_menu_gallery)
            scaleType = ImageView.ScaleType.CENTER_INSIDE
            if (isNight) setColorFilter(Color.parseColor("#99BBBBBB"), android.graphics.PorterDuff.Mode.MULTIPLY)
        }

        // ★ エフェクトレイヤーの初期化
        effectLayer = EffectView(this)

        // 高度なタッチ判定を rootLayout に設定（画面全体で星を出すため）
        rootLayout.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                // 星を発生
                effectLayer.addStar(event.x, event.y)
                
                val currentTime = System.currentTimeMillis()
                val isDoubleTap = (currentTime - lastTapTime < 300)
                performReaction(imageView, isDoubleTap)
                lastTapTime = currentTime
                playSound()
                
                val love = prefs.getInt("love_count", 0) + 1
                prefs.edit().putInt("love_count", love).apply()
            }
            true
        }

        rootLayout.addView(imageView, FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT)
        rootLayout.addView(effectLayer, FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT) // ★追加

        // ボタン類
        val btnContainer = FrameLayout(this).apply {
            setPadding(60, 60, 60, 60)
            setOnClickListener { showCollectionScreen() }
            val outValue = TypedValue()
            theme.resolveAttribute(android.R.attr.selectableItemBackgroundBorderless, outValue, true)
            setBackgroundResource(outValue.resourceId)
            addView(TextView(context).apply {
                text = "● Collection >"
                setTextColor(if (isNight) Color.GRAY else Color.YELLOW)
                textSize = 20f
                setTypeface(null, Typeface.BOLD)
                setBackgroundColor(Color.parseColor("#AA000000"))
                setPadding(50, 40, 50, 40)
            })
        }
        rootLayout.addView(btnContainer, FrameLayout.LayoutParams(-2, -2).apply { topMargin = 30; leftMargin = 30 })
        rootLayout.addView(createExitButton(), FrameLayout.LayoutParams(-2, -2).apply { gravity = Gravity.TOP or Gravity.END; topMargin = 30; rightMargin = 30 })

        setContentView(rootLayout)
    }

    private fun performReaction(view: View, isBig: Boolean) {
        val animation = if (isBig) {
            TranslateAnimation(0f, 0f, 0f, -120f).apply {
                duration = 150; repeatCount = 1; repeatMode = Animation.REVERSE; interpolator = DecelerateInterpolator()
            }
        } else {
            AnimationSet(true).apply {
                addAnimation(TranslateAnimation(-15f, 15f, -8f, 8f).apply {
                    duration = 60; repeatCount = 3; repeatMode = Animation.REVERSE
                })
            }
        }
        view.startAnimation(animation)
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
        layout.addView(CatDegreeView(this).apply { this.progress = progress }, LinearLayout.LayoutParams(600, 600))
        layout.addView(TextView(this).apply { text = "$progress%"; setTextColor(Color.YELLOW); textSize = 48f; setPadding(0,60,0,60) })
        layout.addView(Button(this).apply { text = "Back"; setOnClickListener { showMainScreen() } })
        rootLayout.addView(layout)
        rootLayout.addView(createExitButton(), FrameLayout.LayoutParams(-2, -2).apply { gravity = Gravity.TOP or Gravity.END; topMargin = 30; rightMargin = 30 })
        setContentView(rootLayout)
    }

    private fun playSound() {
        try {
            mediaPlayer?.release()
            val resId = resources.getIdentifier("meow", "raw", packageName)
            if (resId != 0) {
                mediaPlayer = MediaPlayer.create(this, resId)
                mediaPlayer?.start()
            }
        } catch (e: Exception) {}
    }

    override fun onDestroy() { super.onDestroy(); mediaPlayer?.release() }
}

// --- エフェクト描画クラス ---
class EffectView(context: Context) : View(context) {
    private val stars = mutableListOf<Star>()
    private val paint = Paint().apply {
        color = Color.YELLOW
        style = Paint.Style.FILL
    }

    class Star(val x: Float, var y: Float, var alpha: Int)

    fun addStar(x: Float, y: Float) {
        stars.add(Star(x, y, 255))
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        val iterator = stars.iterator()
        while (iterator.hasNext()) {
            val s = iterator.next()
            paint.alpha = s.alpha
            
            val size = 30f
            val path = Path().apply {
                moveTo(s.x, s.y - size)
                lineTo(s.x + size, s.y)
                lineTo(s.x, s.y + size)
                lineTo(s.x - size, s.y)
                close()
            }
            canvas.drawPath(path, paint)

            s.y -= 5f
            s.alpha -= 15
            if (s.alpha <= 0) iterator.remove()
        }
        if (stars.isNotEmpty()) postInvalidateDelayed(30)
    }
}

// --- グラフ描画クラス ---
class CatDegreeView(context: Context) : View(context) {
    var progress: Int = 0
        set(value) { field = value; invalidate() }
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE; strokeCap = Paint.Cap.ROUND; strokeWidth = 60f
    }
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val center = width / 2f
        val radius = (width / 2f) - 50f
        val rect = RectF(center - radius, center - radius, center + radius, center + radius)
        paint.shader = null; paint.color = Color.parseColor("#333333")
        canvas.drawCircle(center, center, radius, paint)
        val gradient = SweepGradient(center, center, intArrayOf(Color.YELLOW, Color.parseColor("#FF8C00"), Color.YELLOW), null)
        paint.shader = gradient
        canvas.save(); canvas.rotate(-90f, center, center)
        canvas.drawArc(rect, 0f, (progress / 100f) * 360f, false, paint)
        canvas.restore()
        paint.shader = null
    }
}

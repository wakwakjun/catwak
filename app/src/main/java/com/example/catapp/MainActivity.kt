package jp.myuser.supercatapp

import android.content.Context
import android.graphics.*
import android.media.MediaPlayer
import android.os.Bundle
import android.util.TypedValue
import android.view.*
import android.view.animation.*
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {
    private var mediaPlayer: MediaPlayer? = null
    private lateinit var effectLayer: EffectView
    private lateinit var gestureDetector: GestureDetector
    private var currentScreen = 0 // 0:メイン, 1:好感度一覧, -1:猫度

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // スワイプ検知の設定
        gestureDetector = GestureDetector(this, object : GestureDetector.SimpleOnGestureListener() {
            override fun onFling(e1: MotionEvent?, e2: MotionEvent, velocityX: Float, velocityY: Float): Boolean {
                val diffX = e2.x - (e1?.x ?: 0f)
                if (Math.abs(diffX) > 100) {
                    if (diffX > 0) onSwipeRight() else onSwipeLeft()
                    return true
                }
                return false
            }
            override fun onSingleTapUp(e: MotionEvent): Boolean {
                handleTap(e.x, e.y)
                return true
            }
        })
        
        showMainScreen()
    }

    private fun onSwipeLeft() {
        if (currentScreen == 0) showCollectionScreen()
        else if (currentScreen == 1) showMainScreen()
    }

    private fun onSwipeRight() {
        if (currentScreen == 0) showLoveListScreen()
        else if (currentScreen == -1) showMainScreen()
    }

    private fun handleTap(x: Float, y: Float) {
        if (currentScreen != 0) return 
        
        for (i in 1..5) {
            effectLayer.addStar(x, y)
        }
        playSound()
        
        val prefs = getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
        val animalNum = prefs.getInt("animal_num", 1)
        val loveKey = "love_cat_$animalNum"
        val currentLove = prefs.getInt(loveKey, 0)
        prefs.edit().putInt(loveKey, currentLove + 1).apply()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        return gestureDetector.onTouchEvent(event) || super.onTouchEvent(event)
    }

    private fun showMainScreen() {
        currentScreen = 0
        val prefs = getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
        val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        val isNight = currentHour >= 22 || currentHour < 6
        
        val rootLayout = FrameLayout(this).apply {
            setBackgroundColor(if (isNight) Color.parseColor("#000011") else Color.BLACK)
        }

        val animalNum = prefs.getInt("animal_num", 1)
        val currentStatus = if (isNight) "sleep" else listOf("sleep", "eat", "play").random()
        
        val imageView = ImageView(this).apply {
            val resName = "cat${animalNum}_$currentStatus"
            val imageResId = resources.getIdentifier(resName, "drawable", packageName)
            setImageResource(if (imageResId != 0) imageResId else android.R.drawable.ic_menu_gallery)
            scaleType = ImageView.ScaleType.CENTER_INSIDE
        }

        effectLayer = EffectView(this)
        rootLayout.addView(imageView)
        rootLayout.addView(effectLayer)
        
        rootLayout.addView(TextView(this).apply {
            text = "< 猫度　　好感度 >"
            setTextColor(Color.DKGRAY)
            gravity = Gravity.CENTER
        }, FrameLayout.LayoutParams(-1, -2).apply { gravity = Gravity.BOTTOM; bottomMargin = 50 })

        setContentView(rootLayout)
    }

    private fun showLoveListScreen() {
        currentScreen = 1
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.parseColor("#121212"))
            setPadding(50, 100, 50, 50)
            gravity = Gravity.CENTER_HORIZONTAL
        }

        layout.addView(TextView(this).apply {
            text = "Cat Love List"
            setTextColor(Color.WHITE)
            textSize = 24f
            setPadding(0, 0, 0, 50)
        })

        val prefs = getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
        for (i in 1..7) {
            val love = prefs.getInt("love_cat_$i", 0)
            val row = TextView(this).apply {
                text = "Cat #$i : " + "❤".repeat(Math.min(love / 10 + 1, 10)) + " ($love)"
                setTextColor(Color.LTGRAY)
                textSize = 18f
                setPadding(0, 20, 0, 20)
            }
            layout.addView(row)
        }

        layout.addView(TextView(this).apply {
            text = "← Swipe Left to Back"
            setTextColor(Color.GRAY)
            setPadding(0, 100, 0, 0)
        })

        setContentView(layout)
    }

    private fun showCollectionScreen() {
        currentScreen = -1
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setBackgroundColor(Color.parseColor("#121212"))
        }
        layout.addView(TextView(this).apply { text = "Collection"; setTextColor(Color.WHITE); textSize = 22f })
        
        val prefs = getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
        var count = 0
        for (i in 1..7) for (s in listOf("sleep", "eat", "play")) if (prefs.getBoolean("seen_${i}_$s", false)) count++
        val progress = (count.toFloat() / 21f * 100).toInt()
        
        layout.addView(CatDegreeView(this).apply { this.progress = progress }, LinearLayout.LayoutParams(500, 500))
        layout.addView(TextView(this).apply { text = "$progress%"; setTextColor(Color.YELLOW); textSize = 30f })
        layout.addView(TextView(this).apply { text = "Swipe Right to Back →"; setTextColor(Color.GRAY); setPadding(0, 50, 0, 0) })
        
        setContentView(layout)
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

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release()
    }
}

// --- エフェクトクラス ---
class EffectView(context: Context) : View(context) {
    private val stars = mutableListOf<StarData>()
    private val random = Random()

    class StarData(val x: Float, var y: Float, var size: Float, var alpha: Int, val vx: Float, val vy: Float)

    fun addStar(x: Float, y: Float) {
        val s = StarData(
            x, y, 
            random.nextFloat() * 40f + 10f,
            255, 
            (random.nextFloat() - 0.5f) * 20f,
            (random.nextFloat() - 0.5f) * 20f
        )
        stars.add(s)
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        val paint = Paint().apply { color = Color.YELLOW; style = Paint.Style.FILL }
        val iterator = stars.iterator()
        while (iterator.hasNext()) {
            val s = iterator.next()
            paint.alpha = s.alpha
            drawStarShape(canvas, s.x, s.y, s.size, paint)

            s.y += s.vy
            s.x += s.vx
            s.alpha -= 10
            if (s.alpha <= 0) iterator.remove()
        }
        if (stars.isNotEmpty()) postInvalidateDelayed(30)
    }

    private fun drawStarShape(canvas: Canvas, cx: Float, cy: Float, radius: Float, paint: Paint) {
        val path = Path()
        val innerRadius = radius / 2.5f
        for (i in 0 until 10) {
            val r = if (i % 2 == 0) radius else innerRadius
            val angle = Math.toRadians((i * 36 + 270).toDouble())
            val x = cx + (r * Math.cos(angle)).toFloat()
            val y = cy + (r * Math.sin(angle)).toFloat()
            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        path.close()
        canvas.drawPath(path, paint)
    }
}

// --- グラフ表示クラス ---
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
        canvas.save()
        canvas.rotate(-90f, center, center)
        canvas.drawArc(rect, 0f, (progress / 100f) * 360f, false, paint)
        canvas.restore()
        paint.shader = null
    }
}

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

    // --- スワイプ処理 ---
    private fun onSwipeLeft() {
        if (currentScreen == 0) showCollectionScreen() // メイン→猫度
        else if (currentScreen == 1) showMainScreen() // 好感度→メイン
    }

    private fun onSwipeRight() {
        if (currentScreen == 0) showLoveListScreen() // メイン→好感度
        else if (currentScreen == -1) showMainScreen() // 猫度→メイン
    }

    // --- タップ処理（星の発生） ---
    private fun handleTap(x: Float, y: Float) {
        if (currentScreen != 0) return 
        
        // 大小さまざまな星を5つほど発生させる
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

    // --- メイン画面 ---
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
        
        // ガイドテキスト
        rootLayout.addView(TextView(this).apply {
            text = "< 猫度　　好感度 >"
            setTextColor(Color.DKGRAY)
            gravity = Gravity.CENTER
        }, FrameLayout.LayoutParams(-1, -2).apply { gravity = Gravity.BOTTOM; bottomMargin = 50 })

        setContentView(rootLayout)
    }

    // --- 新機能：好感度一覧画面 ---
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

    // --- 猫度画面（以前のものを流用） ---
    private fun showCollectionScreen() {
        currentScreen = -1
        // (以前の showCollectionScreen のロジックをここに配置)
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

    private fun playSound() { /* 既存の再生処理 */ }
}

// --- ★進化した星型エフェクトクラス ---
class EffectView(context: Context) : View(context) {
    private val stars = mutableListOf<StarData>()
    private val random = Random()

    class StarData(val x: Float, var y: Float, var size: Float, var alpha: Int, val vx: Float, val vy: Float)

    fun addStar(x: Float, y: Float) {
        val s = StarData(
            x, y, 
            random.nextFloat() * 40f + 10f, // サイズ 10〜50
            255, 
            (random.nextFloat() - 0.5f) * 20f, // 左右の散らばり
            (random.nextFloat() - 0.5f) * 20f  // 上下の散らばり
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
            s.alpha -= 10 // フェードアウト速度
            if (s.alpha <= 0) iterator.remove()
        }
        if (stars.isNotEmpty()) postInvalidateDelayed(30)
    }

    // 数学的に5角星を描く関数
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

class CatDegreeView(context: Context) : View(context) { /* 既存のコード */ }

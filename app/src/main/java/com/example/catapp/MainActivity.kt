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
    private var currentScreen = 0 // 0:メイン, 1:好感度, -1:猫度

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // ジェスチャー検知の設定
        val listener = object : GestureDetector.SimpleOnGestureListener() {
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
        }
        gestureDetector = GestureDetector(this, listener)
        
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
        val loveVal = prefs.getInt(loveKey, 0)
        prefs.edit().putInt(loveKey, loveVal + 1).apply()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        return gestureDetector.onTouchEvent(event) || super.onTouchEvent(event)
    }

    private fun showMainScreen() {
        currentScreen = 0
        val prefs = getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
        val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        val isNight = currentHour >= 22 || currentHour < 6
        
        val rootLayout = FrameLayout(this)
        rootLayout.setBackgroundColor(if (isNight) Color.parseColor("#000011") else Color.BLACK)

        val animalNum = prefs.getInt("animal_num", 1)
        val currentStatus = if (isNight) "sleep" else listOf("sleep", "eat", "play").random()
        
        val imageView = ImageView(this)
        val resName = "cat${animalNum}_$currentStatus"
        val imageResId = resources.getIdentifier(resName, "drawable", packageName)
        imageView.setImageResource(if (imageResId != 0) imageResId else android.R.drawable.ic_menu_gallery)
        imageView.scaleType = ImageView.ScaleType.CENTER_INSIDE

        effectLayer = EffectView(this)
        rootLayout.addView(imageView)
        rootLayout.addView(effectLayer)
        
        val guide = TextView(this)
        guide.text = "< 猫度　　好感度 >"
        guide.setTextColor(Color.DKGRAY)
        guide.gravity = Gravity.CENTER
        val guideParams = FrameLayout.LayoutParams(-1, -2)
        guideParams.gravity = Gravity.BOTTOM
        guideParams.bottomMargin = 50
        rootLayout.addView(guide, guideParams)

        setContentView(rootLayout)
    }

    private fun showLoveListScreen() {
        currentScreen = 1
        val layout = LinearLayout(this)
        layout.orientation = LinearLayout.VERTICAL
        layout.setBackgroundColor(Color.parseColor("#121212"))
        layout.setPadding(50, 100, 50, 50)
        layout.gravity = Gravity.CENTER_HORIZONTAL

        val title = TextView(this)
        title.text = "Cat Love List"
        title.setTextColor(Color.WHITE)
        title.textSize = 24f
        title.setPadding(0, 0, 0, 50)
        layout.addView(title)

        val prefs = getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
        for (i in 1..7) {
            val love = prefs.getInt("love_cat_$i", 0)
            val row = TextView(this)
            val hearts = "❤".repeat(Math.min(love / 10 + 1, 10))
            row.text = "Cat #$i : $hearts ($love)"
            row.setTextColor(Color.LTGRAY)
            row.textSize = 18f
            row.setPadding(0, 20, 0, 20)
            layout.addView(row)
        }

        val backGuide = TextView(this)
        backGuide.text = "← Swipe Left to Back"
        backGuide.setTextColor(Color.GRAY)
        backGuide.setPadding(0, 100, 0, 0)
        layout.addView(backGuide)

        setContentView(layout)
    }

    private fun showCollectionScreen() {
        currentScreen = -1
        val layout = LinearLayout(this)
        layout.orientation = LinearLayout.VERTICAL
        layout.gravity = Gravity.CENTER
        layout.setBackgroundColor(Color.parseColor("#121212"))

        val title = TextView(this)
        title.text = "Collection"
        title.setTextColor(Color.WHITE)
        title.textSize = 22f
        layout.addView(title)
        
        val prefs = getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
        var countFound = 0
        for (i in 1..7) {
            for (s in listOf("sleep", "eat", "play")) {
                if (prefs.getBoolean("seen_${i}_$s", false)) countFound++
            }
        }
        
        val percentValue = (countFound.toFloat() / 21f * 100).toInt()
        
        val chart = CatDegreeView(this)
        chart.updateProgress(percentValue)
        layout.addView(chart, LinearLayout.LayoutParams(500, 500))
        
        val percentText = TextView(this)
        percentText.text = "$percentValue%"
        percentText.setTextColor(Color.YELLOW)
        percentText.textSize = 30f
        layout.addView(percentText)
        
        val swipeBack = TextView(this)
        swipeBack.text = "Swipe Right to Back →"
        swipeBack.setTextColor(Color.GRAY)
        swipeBack.setPadding(0, 50, 0, 0)
        layout.addView(swipeBack)
        
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

class EffectView(context: Context) : View(context) {
    private val stars = mutableListOf<StarData>()
    private val random = Random()
    class StarData(val x: Float, var y: Float, var size: Float, var alpha: Int, val vx: Float, val vy: Float)

    fun addStar(x: Float, y: Float) {
        val s = StarData(x, y, random.nextFloat() * 40f + 10f, 255, (random.nextFloat() - 0.5f) * 20f, (random.nextFloat() - 0.5f) * 20f)
        stars.add(s)
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        val paint = Paint()
        paint.color = Color.YELLOW
        paint.style = Paint.Style.FILL
        val iterator = stars.iterator()
        while (iterator.hasNext()) {
            val s = iterator.next()
            paint.alpha = s.alpha
            
            val path = Path()
            val radius = s.size
            val inner = radius / 2.5f
            for (i in 0 until 10) {
                val r = if (i % 2 == 0) radius else inner
                val angle = Math.toRadians((i * 36 + 270).toDouble())
                val px = s.x + (r * Math.cos(angle)).toFloat()
                val py = s.y + (r * Math.sin(angle)).toFloat()
                if (i == 0) path.moveTo(px, py) else path.lineTo(px, py)
            }
            path.close()
            canvas.drawPath(path, paint)

            s.y += s.vy
            s.x += s.vx
            s.alpha -= 10
            if (s.alpha <= 0) iterator.remove()
        }
        if (stars.isNotEmpty()) postInvalidateDelayed(30)
    }
}

class CatDegreeView(context: Context) : View(context) {
    private var pVal: Int = 0
    fun updateProgress(v: Int) {
        pVal = v
        invalidate()
    }
    
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        paint.style = Paint.Style.STROKE
        paint.strokeCap = Paint.Cap.ROUND
        paint.strokeWidth = 60f
        
        val center = width / 2f
        val radius = (width / 2f) - 50f
        val rect = RectF(center - radius, center - radius, center + radius, center + radius)
        
        paint.shader = null
        paint.color = Color.parseColor("#333333")
        canvas.drawCircle(center, center, radius, paint)
        
        val grad = SweepGradient(center, center, intArrayOf(Color.YELLOW, Color.parseColor("#FF8C00"), Color.YELLOW), null)
        paint.shader = grad
        canvas.save()
        canvas.rotate(-90f, center, center)
        canvas.drawArc(rect, 0f, (pVal / 100f) * 360f, false, paint)
        canvas.restore()
    }
}

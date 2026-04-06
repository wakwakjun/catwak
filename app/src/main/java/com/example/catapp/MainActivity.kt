package jp.myuser.supercatapp

import android.content.Context
import android.graphics.*
import android.media.MediaPlayer
import android.os.Bundle
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
    private var currentScreen = 0 // 0:Main, 1:Love, -1:Collection

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
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
        for (i in 1..5) effectLayer.addStar(x, y)
        playSound()
        val prefs = getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
        val catId = prefs.getInt("animal_num", 1)
        val key = "love_cat_$catId"
        prefs.edit().putInt(key, prefs.getInt(key, 0) + 1).apply()
    }

    override fun onTouchEvent(e: MotionEvent): Boolean {
        return gestureDetector.onTouchEvent(e) || super.onTouchEvent(e)
    }

    private fun showMainScreen() {
        currentScreen = 0
        val prefs = getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        val isNight = hour >= 22 || hour < 6
        
        val root = FrameLayout(this)
        root.setBackgroundColor(if (isNight) Color.parseColor("#000011") else Color.BLACK)

        val catId = prefs.getInt("animal_num", 1)
        val status = if (isNight) "sleep" else listOf("sleep", "eat", "play").random()
        
        val img = ImageView(this)
        val resId = resources.getIdentifier("cat${catId}_$status", "drawable", packageName)
        img.setImageResource(if (resId != 0) resId else android.R.drawable.ic_menu_gallery)
        img.scaleType = ImageView.ScaleType.CENTER_INSIDE

        effectLayer = EffectView(this)
        root.addView(img)
        root.addView(effectLayer)
        
        val guide = TextView(this)
        guide.text = "< 猫度　　好感度 >"
        guide.setTextColor(Color.DKGRAY)
        guide.gravity = Gravity.CENTER
        val params = FrameLayout.LayoutParams(-1, -2)
        params.gravity = Gravity.BOTTOM
        params.bottomMargin = 50
        root.addView(guide, params)

        setContentView(root)
    }

    private fun showLoveListScreen() {
        currentScreen = 1
        val layout = LinearLayout(this)
        layout.orientation = LinearLayout.VERTICAL
        layout.setBackgroundColor(Color.parseColor("#121212"))
        layout.setPadding(50, 100, 50, 50)
        layout.gravity = Gravity.CENTER_HORIZONTAL

        val tv = TextView(this)
        tv.text = "Cat Love List"
        tv.setTextColor(Color.WHITE)
        tv.textSize = 24f
        layout.addView(tv)

        val prefs = getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
        for (i in 1..7) {
            val love = prefs.getInt("love_cat_$i", 0)
            val row = TextView(this)
            row.text = "Cat #$i : " + "❤".repeat(Math.min(love / 10 + 1, 10)) + " ($love)"
            row.setTextColor(Color.LTGRAY)
            row.setPadding(0, 20, 0, 20)
            layout.addView(row)
        }
        setContentView(layout)
    }

    private fun showCollectionScreen() {
        currentScreen = -1
        val layout = LinearLayout(this)
        layout.orientation = LinearLayout.VERTICAL
        layout.gravity = Gravity.CENTER
        layout.setBackgroundColor(Color.parseColor("#121212"))

        val prefs = getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
        var seenCount = 0
        for (i in 1..7) {
            for (s in listOf("sleep", "eat", "play")) {
                if (prefs.getBoolean("seen_${i}_$s", false)) seenCount++
            }
        }
        val pct = (seenCount.toFloat() / 21f * 100).toInt()
        
        val chart = CatDegreeView(this)
        chart.setVal(pct)
        layout.addView(chart, LinearLayout.LayoutParams(500, 500))
        
        val tv = TextView(this)
        tv.text = "$pct%"
        tv.setTextColor(Color.YELLOW)
        tv.textSize = 30f
        layout.addView(tv)
        
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
    private val stars = mutableListOf<Star>()
    private val random = Random()
    class Star(val x: Float, var y: Float, var size: Float, var alpha: Int, val vx: Float, val vy: Float)

    fun addStar(x: Float, y: Float) {
        stars.add(Star(x, y, random.nextFloat() * 40f + 10f, 255, (random.nextFloat() - 0.5f) * 20f, (random.nextFloat() - 0.5f) * 20f))
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        val paint = Paint()
        paint.color = Color.YELLOW
        val it = stars.iterator()
        while (it.hasNext()) {
            val s = it.next()
            paint.alpha = s.alpha
            val path = Path()
            for (i in 0 until 10) {
                val r = if (i % 2 == 0) s.size else s.size / 2.5f
                val ang = Math.toRadians((i * 36 + 270).toDouble())
                val px = s.x + (r * Math.cos(ang)).toFloat()
                val py = s.y + (r * Math.sin(ang)).toFloat()
                if (i == 0) path.moveTo(px, py) else path.lineTo(px, py)
            }
            path.close()
            canvas.drawPath(path, paint)
            s.y += s.vy; s.x += s.vx; s.alpha -= 10
            if (s.alpha <= 0) it.remove()
        }
        if (stars.isNotEmpty()) postInvalidateDelayed(30)
    }
}

class CatDegreeView(context: Context) : View(context) {
    private var p: Int = 0
    fun setVal(v: Int) { p = v; invalidate() }
    override fun onDraw(canvas: Canvas) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 60f
        val rect = RectF(50f, 50f, width - 50f, height - 50f)
        paint.color = Color.parseColor("#333333")
        canvas.drawArc(rect, 0f, 360f, false, paint)
        paint.color = Color.YELLOW
        canvas.drawArc(rect, -90f, (p / 100f) * 360f, false, paint)
    }
}

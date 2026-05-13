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
import com.google.android.play.core.review.ReviewManagerFactory
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.material.snackbar.Snackbar

class MainActivity : AppCompatActivity() {
    private var mediaPlayer: MediaPlayer? = null
    private lateinit var effectLayer: EffectView
    private lateinit var gestureDetector: GestureDetector
    private lateinit var appUpdateManager: AppUpdateManager
    
    private val installListener = InstallStateUpdatedListener { state ->
        if (state.installStatus() == InstallStatus.DOWNLOADED) {
            showUpdateSnackbar()
        }
    }

    private fun checkForUpdate() {
        val appUpdateInfoTask = appUpdateManager.appUpdateInfo
        appUpdateInfoTask.addOnSuccessListener { info ->
            if (info.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                && info.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)
            ) {
                appUpdateManager.startUpdateFlowForResult(info, AppUpdateType.FLEXIBLE, this, 999)
            }
        }
        appUpdateManager.registerListener(installListener)
    }

    private fun showUpdateSnackbar() {
        val root = findViewById<View>(android.R.id.content)
        Snackbar.make(root, "アップデートの準備完了！", Snackbar.LENGTH_INDEFINITE).apply {
            setAction("再起動") { 
                appUpdateManager.completeUpdate() 
            }
            show()
        }
    }

    override fun onResume() {
        super.onResume()
        checkForUpdate()
    }

    private var currentScreen = 0 
    private val MAIN_IMAGE_ID = View.generateViewId()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        appUpdateManager = AppUpdateManagerFactory.create(this)

        val listener = object : GestureDetector.SimpleOnGestureListener() {
            override fun onFling(e1: MotionEvent?, e2: MotionEvent, velocityX: Float, velocityY: Float): Boolean {
                val e1X = e1?.x ?: 0f
                val diffX = e2.x - e1X
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

        val screenWidth = resources.displayMetrics.widthPixels
        val direction = if (x > screenWidth / 2) 1.0f else -1.0f
        
        val imageView = findViewById<ImageView>(MAIN_IMAGE_ID)
        
        imageView?.animate()?.apply {
            scaleX(direction)
            scaleY(0.85f)
            duration = 100
            withEndAction {
                imageView.animate().scaleY(1.0f).setDuration(200).start()
            }
            start()
        }

        for (i in 1..5) effectLayer.addStar(x, y)
        playSound()
        
        val prefs = getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
        val catId = prefs.getInt("animal_num", 1)
        val key = "love_cat_$catId"
        val nextLove = prefs.getInt(key, 0) + 1
        prefs.edit().putInt(key, nextLove).apply()

        if (nextLove % 50 == 0) {
            requestReviewIfAppropriate()
        }
    }

    override fun onTouchEvent(e: MotionEvent): Boolean {
        return gestureDetector.onTouchEvent(e) || super.onTouchEvent(e)
    }

    private fun showMainScreen() {
        currentScreen = 0
        val prefs = getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        val isNight = hour >= 22 || hour < 6
        val catId = (1..7).random() 
        prefs.edit().putInt("animal_num", catId).apply()

        val root = FrameLayout(this)
        root.setBackgroundColor(if (isNight) Color.parseColor("#000011") else Color.BLACK)
        val status = if (isNight) "sleep" else listOf("sleep", "eat", "play").random()
        prefs.edit().putBoolean("seen_${catId}_$status", true).apply()

        val img = ImageView(this)
        img.id = MAIN_IMAGE_ID
        val resId = resources.getIdentifier("cat${catId}_$status", "drawable", packageName)
        img.setImageResource(if (resId != 0) resId else android.R.drawable.ic_menu_gallery)
        img.scaleType = ImageView.ScaleType.CENTER_INSIDE

        effectLayer = EffectView(this)
        root.addView(img)
        root.addView(effectLayer)
        
        val guide = TextView(this)
        guide.text = "< 猫達成度　　猫密度 >"
        guide.setTextColor(Color.DKGRAY)
        guide.gravity = Gravity.CENTER
        val params = FrameLayout.LayoutParams(-1, -2)
        params.gravity = Gravity.BOTTOM
        params.bottomMargin = 120
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
        tv.text = "猫密度 (Love Level)"
        tv.setTextColor(Color.WHITE)
        tv.textSize = 28f
        tv.setTypeface(null, Typeface.BOLD)
        tv.setPadding(0, 0, 0, 50)
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

        val title = TextView(this)
        title.text = "猫達成度"
        title.textSize = 28f
        title.setTypeface(null, Typeface.BOLD)
        title.setPadding(0, 0, 0, 40)
        if (pct >= 100) title.setTextColor(Color.YELLOW) else title.setTextColor(Color.WHITE)
        layout.addView(title)
        
        val chart = CatDegreeView(this)
        chart.setVal(pct)
        layout.addView(chart, LinearLayout.LayoutParams(500, 500))
        
        val pt = TextView(this)
        pt.text = "$pct%"
        pt.setTextColor(if (pct >= 100) Color.YELLOW else Color.WHITE)
        pt.textSize = 30f
        pt.setPadding(0, 40, 0, 0)
        layout.addView(pt)
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

    private fun requestReviewIfAppropriate() {
        val manager = ReviewManagerFactory.create(this)
        val request = manager.requestReviewFlow()
        request.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val reviewInfo = task.result
                manager.launchReviewFlow(this, reviewInfo)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::appUpdateManager.isInitialized) {
            appUpdateManager.unregisterListener(installListener)
        }
        mediaPlayer?.release()
    }
}

// --- カスタムビュークラス ---

class EffectView(context: Context) : View(context) {
    private val stars = mutableListOf<Star>()
    private val random = Random()
    class Star(val x: Float, var y: Float, val s: Float, var a: Int, val vx: Float, val vy: Float)

    fun addStar(px: Float, py: Float) {
        stars.add(Star(px, py, random.nextFloat() * 40f + 10f, 255, (random.nextFloat() - 0.5f) * 20f, (random.nextFloat() - 0.5f) * 20f))
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        val paint = Paint().apply { color = Color.YELLOW; style = Paint.Style.FILL }
        val it = stars.iterator()
        while (it.hasNext()) {
            val s = it.next()
            paint.alpha = s.a
            val path = Path()
            val r = s.s
            for (i in 0 until 10) {
                val dist = if (i % 2 == 0) r else r / 2.5f
                val ang = Math.toRadians((i * 36 + 270).toDouble())
                val px = s.x + (dist * Math.cos(ang)).toFloat()
                val py = s.y + (dist * Math.sin(ang)).toFloat()
                if (i == 0) path.moveTo(px, py) else path.lineTo(px, py)
            }
            path.close()
            canvas.drawPath(path, paint)
            s.y += s.vy
            s.a -= 10
            if (s.a <= 0) it.remove()
        }
        if (stars.isNotEmpty()) postInvalidateDelayed(30)
    }
}

class CatDegreeView(context: Context) : View(context) {
    private var pVal: Int = 0
    fun setVal(v: Int) { pVal = v; invalidate() }
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        paint.style = Paint.Style.STROKE
        paint.strokeCap = Paint.Cap.ROUND
        paint.strokeWidth = 60f
        val rect = RectF(100f, 100f, width.toFloat() - 100f, height.toFloat() - 100f)
        paint.color = Color.parseColor("#333333")
        canvas.drawCircle(width / 2f, height / 2f, (width / 2f) - 100f, paint)
        paint.color = Color.YELLOW
        canvas.drawArc(rect, -90f, (pVal / 100f) * 360f, false, paint)
    }
}

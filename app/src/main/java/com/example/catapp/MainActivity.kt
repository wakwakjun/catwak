package jp.myuser.supercatapp

import android.content.Context
import android.graphics.*
import android.media.MediaPlayer
import android.os.Bundle
import android.view.*
import android.view.animation.*
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.android.play.core.review.ReviewManagerFactory
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.material.snackbar.Snackbar
import java.util.*

class MainActivity : AppCompatActivity() {
    private var mediaPlayer: MediaPlayer? = null
    private lateinit var effectLayer: EffectView
    private lateinit var gestureDetector: GestureDetector
    private lateinit var appUpdateManager: AppUpdateManager
    private var currentScreen = 0 
    private val MAIN_IMAGE_ID = View.generateViewId()

    private val installListener = InstallStateUpdatedListener { state ->
        if (state.installStatus() == InstallStatus.DOWNLOADED) {
            showUpdateSnackbar()
        }
    }

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
        
        val meshView = findViewById<MeshImageView>(MAIN_IMAGE_ID)
        meshView?.animate()?.scaleX(direction)?.scaleY(0.85f)?.setDuration(100)?.withEndAction {
            meshView.animate().scaleY(1.0f).setDuration(200).start()
        }?.start()

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

    override fun onTouchEvent(e: MotionEvent): Boolean = gestureDetector.onTouchEvent(e) || super.onTouchEvent(e)

    private fun showMainScreen() {
        currentScreen = 0
        val prefs = getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        val isNight = hour >= 22 || hour < 6
        val catId = (1..7).random()
        prefs.edit().putInt("animal_num", catId).apply()

        val root = FrameLayout(this)
        val bgColor = if (isNight) Color.parseColor("#000011") else Color.BLACK
        root.setBackgroundColor(bgColor)

        val statusList = listOf("sleep", "eat", "play")
        val status = if (isNight) "sleep" else statusList.random()
        prefs.edit().putBoolean("seen_${catId}_$status", true).apply()

        val resId = resources.getIdentifier("cat${catId}_$status", "drawable", packageName)
        val bitmap = BitmapFactory.decodeResource(resources, if (resId != 0) resId else android.R.drawable.ic_menu_gallery)
        
        val img = MeshImageView(this)
        img.id = MAIN_IMAGE_ID
        img.setBitmap(bitmap)

        effectLayer = EffectView(this)
        root.addView(img)
        root.addView(effectLayer)
        
        val guide = TextView(this)
        guide.text = "< 猫達成度　　猫密度 >"
        guide.setTextColor(Color.DKGRAY)
        guide.gravity = Gravity.CENTER
        val params = FrameLayout.LayoutParams(-1, -2)
        params.gravity = Gravity.BOTTOM
        params.bottomMargin = 150
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
            val heartCount = Math.min(love / 10 + 1, 10)
            val hearts = (1..heartCount).joinToString("") { "❤" }
            row.text = "Cat #$i : $hearts ($love)"
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
        
        val pctTv = TextView(this)
        pctTv.text = "$pct%"
        val pColor = if (pct >= 100) Color.YELLOW else Color.WHITE
        pctTv.setTextColor(pColor)
        pctTv.textSize = 30f
        pctTv.setPadding(0, 40, 0, 0)
        layout.addView(pctTv)
        
        setContentView(layout)
    }

    private fun requestReviewIfAppropriate() {
        val manager = ReviewManagerFactory.create(this)
        manager.requestReviewFlow().addOnCompleteListener { task ->
            if (task.isSuccessful) manager.launchReviewFlow(this, task.result)
        }
    }

    private fun checkForUpdate() {
        appUpdateManager.appUpdateInfo.addOnSuccessListener { info ->
            if (info.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE && info.isUpdateTypeAllowed(AppUpdateType.FLEX

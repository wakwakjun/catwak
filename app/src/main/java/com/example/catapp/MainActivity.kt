package jp.myuser.supercatapp

import android.content.Context
import android.graphics.*
import android.media.MediaPlayer
import android.os.Bundle
import android.view.*
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
        if (state.installStatus() == InstallStatus.DOWNLOADED) showUpdateSnackbar()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        appUpdateManager = AppUpdateManagerFactory.create(this)
        
        val listener = object : GestureDetector.SimpleOnGestureListener() {
            override fun onFling(e1: MotionEvent?, e2: MotionEvent, velocityX: Float, velocityY: Float): Boolean {
                val diffX = (e2.x) - (e1?.x ?: 0f)
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

    private fun onSwipeLeft() { if (currentScreen == 0) showCollectionScreen() else if (currentScreen == 1) showMainScreen() }
    private fun onSwipeRight() { if (currentScreen == 0) showLoveListScreen() else if (currentScreen == -1) showMainScreen() }

    private fun handleTap(x: Float, y: Float) {
        if (currentScreen != 0) return 
        val screenWidth = resources.displayMetrics.widthPixels
        val direction = if (x > screenWidth / 2) 1.0f else -1.0f
        
        // メッシュビューに対しても反転とぷるぷるを適用
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
        if (nextLove % 50 == 0) requestReviewIfAppropriate()
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

        // ★ ImageViewの代わりにMeshImageViewを使用
        val resId = resources.getIdentifier("cat${catId}_$status", "drawable", packageName)
        val bitmap = BitmapFactory.decodeResource(resources, if (resId != 0) resId else android.R.drawable.ic_menu_gallery)
        val img = MeshImageView(this).apply {
            id = MAIN_IMAGE_ID
            setBitmap(bitmap)
        }

        effectLayer = EffectView(this)
        root.addView(img)
        root.addView(effectLayer)
        
        val guide = TextView(this).apply {
            text = "< 猫達成度　　猫密度 >"
            setTextColor(Color.DKGRAY)
            gravity = Gravity.CENTER
        }
        val params = FrameLayout.LayoutParams(-1, -2).apply {
            gravity = Gravity.BOTTOM
            bottomMargin = 150
        }
        root.addView(guide, params)
        setContentView(root)
    }

    private fun showLoveListScreen() { /* 以前のコードと同じため省略 */ }
    private fun showCollectionScreen() { /* 以前のコードと同じため省略 */ }
    
    private fun requestReviewIfAppropriate() { /* 省略 */ }
    private fun checkForUpdate() { /* 省略 */ }
    private fun showUpdateSnackbar() { /* 省略 */ }
    private fun playSound() { /* 省略 */ }
    override fun onResume() { super.onResume() ; checkForUpdate() }
    override fun onTouchEvent(e: MotionEvent): Boolean = gestureDetector.onTouchEvent(e) || super.onTouchEvent(e)
}

// --- プランBの核心：メッシュ変形カスタムビュー ---

class MeshImageView(context: Context) : View(context) {
    private var bitmap: Bitmap? = null
    private val MESH_WIDTH = 20
    private val MESH_HEIGHT = 20
    private val COUNT = (MESH_WIDTH + 1) * (MESH_HEIGHT + 1)
    private val verts = FloatArray(COUNT * 2)
    private val orig = FloatArray(COUNT * 2)
    private var time = 0f

    fun setBitmap(bm: Bitmap) {
        bitmap = bm
        val w = bm.width.toFloat()
        val h = bm.height.toFloat()
        var index = 0
        for (y in 0..MESH_HEIGHT) {
            val fy = h * y / MESH_HEIGHT
            for (x in 0..MESH_WIDTH) {
                val fx = w * x / MESH_WIDTH
                orig[index * 2 + 0] = fx
                verts[index * 2 + 0] = fx
                orig[index * 2 + 1] = fy
                verts[index * 2 + 1] = fy
                index++
            }
        }
    }

    override fun onDraw(canvas: Canvas) {
        val bm = bitmap ?: return
        time += 0.1f
        
        // 中央付近（お腹）を呼吸に合わせて動かすロジック
        for (i in 0 until COUNT) {
            val ox = orig[i * 2 + 0]
            val oy = orig[i * 2 + 1]
            
            // サイン波でY座標を揺らす（中央に近いほど大きく揺れる）
            val distort = Math.sin((time + ox * 0.01).toDouble()).toFloat() * 15f
            val weight = 1.0f - (Math.abs(ox - bm.width/2f) / (bm.width/2f)) // 端にいくほど影響を弱める
            
            verts[i * 2 + 0] = ox
            verts[i * 2 + 1] = oy + (distort * weight)
        }

        // 画面中央に描画するための計算
        canvas.save()
        canvas.translate((width - bm.width) / 2f, (height - bm.height) / 2f)
        canvas.drawBitmapMesh(bm, MESH_WIDTH, MESH_HEIGHT, verts, 0, null, 0, null)
        canvas.restore()
        
        postInvalidateOnAnimation()
    }
}

// --- 他のViewクラス（EffectView, CatDegreeView）は維持 ---

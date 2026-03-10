package jp.myuser.supercatapp

import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import org.junit.Rule
import org.junit.Test
import java.io.File

class ScreenshotTest {
    @get:Rule
    val activityRule = ActivityScenarioRule(MainActivity::class.java)

    @Test
    fun takeScreenshot() {
        val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        // 画面が安定するまで少し待つ
        Thread.sleep(2000)
        
        // GitHub Actionsの保存先にスクリーンショットを撮る
        val file = File("/sdcard/main_screen.png")
        device.takeScreenshot(file)
    }
}

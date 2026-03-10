package jp.myuser.supercatapp

import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import org.junit.Rule
import org.junit.Test
import java.io.File

class ExampleInstrumentedTest {
    @get:Rule
    val activityRule = ActivityScenarioRule(MainActivity::class.java)

    @Test
    fun takeScreenshot() {
        val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        Thread.sleep(3000)
        
        // 確実に保存できる場所を指定
        val file = File("/sdcard/main_screen.png")
        device.takeScreenshot(file)
    }
}

package jp.myuser.supercatapp

import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import org.junit.Rule
import org.junit.Test
import java.io.File

class ExampleInstrumentedTest {
    // launchActivity を false にして、手動で起動をコントロールします
    @get:Rule
    val activityRule = ActivityScenarioRule(MainActivity::class.java)

    @Test
    fun takeScreenshot() {
        val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        
        // アニメーションが動いていても、5秒待ってから強制撮影
        Thread.sleep(5000)
        
        val file = File("/sdcard/main_screen.png")
        device.takeScreenshot(file)
    }
}

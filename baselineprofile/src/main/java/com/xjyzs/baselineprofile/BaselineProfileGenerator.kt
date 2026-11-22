package com.xjyzs.baselineprofile

import android.app.UiAutomation
import android.widget.EditText
import androidx.benchmark.macro.junit4.BaselineProfileRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Configurator
import androidx.test.uiautomator.Until
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.regex.Pattern

/**
 * This test class generates a basic startup baseline profile for the target package.
 *
 * We recommend you start with this but add important user flows to the profile to improve their performance.
 * Refer to the [baseline profile documentation](https://d.android.com/topic/performance/baselineprofiles)
 * for more information.
 *
 * You can run the generator with the "Generate Baseline Profile" run configuration in Android Studio or
 * the equivalent `generateBaselineProfile` gradle task:
 * ```
 * ./gradlew :app:generateReleaseBaselineProfile
 * ```
 * The run configuration runs the Gradle task and applies filtering to run only the generators.
 *
 * Check [documentation](https://d.android.com/topic/performance/benchmarking/macrobenchmark-instrumentation-args)
 * for more information about available instrumentation arguments.
 *
 * After you run the generator, you can verify the improvements running the [StartupBenchmarks] benchmark.
 *
 * When using this class to generate a baseline profile, only API 33+ or rooted API 28+ are supported.
 *
 * The minimum required version of androidx.benchmark to generate a baseline profile is 1.2.0.
 **/
@RunWith(AndroidJUnit4::class)
@LargeTest
class BaselineProfileGenerator {
    companion object {
        init {
            Configurator.getInstance().uiAutomationFlags =
                UiAutomation.FLAG_DONT_SUPPRESS_ACCESSIBILITY_SERVICES
        }
    }

    @get:Rule
    val rule = BaselineProfileRule()

    val appPackage = InstrumentationRegistry.getArguments().getString("targetAppId")
        ?: throw Exception("targetAppId not passed as instrumentation runner arg")

    @Test
    fun generateStartupProfile() {
        rule.collect(
            packageName = appPackage,
            includeInStartupProfile = true
        ) {
            pressHome()
            startActivityAndWait()

            // 等待连接就绪
            device.wait(
                Until.hasObject(By.text(Pattern.compile("已连接|Connected"))),
                5_000
            )
        }
    }

    @Test
    fun generateInteractionProfile() {
        rule.collect(
            packageName = appPackage,
            includeInStartupProfile = false
        ) {
            pressHome()
            startActivityAndWait()

            // 等待连接就绪
            device.wait(
                Until.hasObject(By.text(Pattern.compile("已连接|Connected"))),
                5_000
            )

            // 生成足够长的多行文
            val longMessage = buildString {
                repeat(40) { index ->
                    append("Baseline Profile 长消息测试第 ${index + 1} 行：用于撑满屏幕并触发垂直滚动与模糊重绘。\n")
                }
            }.trimEnd()

            // 直接通过无障碍设值，不会弹软键盘遮挡下方内容区
            val inputField = device.wait(Until.findObject(By.clazz(EditText::class.java)), 2_000)
            inputField.text = longMessage

            // 发送
            val sendBtn = device.findObject(By.desc(Pattern.compile("发送|Send")))
            sendBtn.click()

            // 等待长消息回显渲染到页面
            device.wait(Until.hasObject(By.textContains("Baseline Profile 长消息测试第 1 行")), 2_000)

            // 滑动
            val width = device.displayWidth
            val height = device.displayHeight
            repeat(2) {
                device.swipe(width / 2, height * 3 / 4, width / 2, height / 4, 20)
                device.waitForIdle()

                device.swipe(width / 2, height / 4, width / 2, height * 3 / 4, 20)
                device.waitForIdle()
            }
        }
    }
}
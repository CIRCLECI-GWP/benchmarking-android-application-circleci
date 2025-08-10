/*
 * Copyright 2023 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.architecture.blueprints.todoapp.benchmark

import androidx.benchmark.macro.CompilationMode
import androidx.benchmark.macro.FrameTimingMetric
import androidx.benchmark.macro.StartupMode
import androidx.benchmark.macro.junit4.MacrobenchmarkRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Direction
import androidx.test.uiautomator.Until
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * This is a performance test for scrolling through the tasks list.
 *
 * It measures frame timing metrics while scrolling through a list of tasks
 * to ensure smooth scrolling performance.
 */
@RunWith(AndroidJUnit4::class)
@LargeTest
class ScrollBenchmark {
    @get:Rule
    val benchmarkRule = MacrobenchmarkRule()

    @Test
    fun scrollCompilationNone() = scroll(CompilationMode.None())

    @Test
    fun scrollCompilationPartial() = scroll(CompilationMode.Partial())

    private fun scroll(compilationMode: CompilationMode) = benchmarkRule.measureRepeated(
        packageName = TARGET_PACKAGE,
        metrics = listOf(FrameTimingMetric()),
        iterations = 5,
        startupMode = StartupMode.WARM,
        compilationMode = compilationMode,
        setupBlock = {
            // Start the app
            pressHome()
            startActivityAndWait()
            
            // Wait for content to load
            device.wait(Until.hasObject(By.res("android:id/content")), 5_000)
            device.waitForIdle()
        }
    ) {
        val scrollableElement = device.findObject(By.scrollable(true))
        
        // If we have a scrollable element, perform scrolling
        if (scrollableElement != null) {
            // Scroll down several times
            repeat(5) {
                scrollableElement.scroll(Direction.DOWN, 1.0f)
                device.waitForIdle(100)
            }
            
            // Scroll back up
            repeat(5) {
                scrollableElement.scroll(Direction.UP, 1.0f)
                device.waitForIdle(100)
            }
        } else {
            // If no scrollable content is found, try to simulate scrolling by
            // swiping on the main content area
            val content = device.findObject(By.res("android:id/content"))
            if (content != null) {
                repeat(3) {
                    content.swipe(Direction.DOWN, 1.0f)
                    device.waitForIdle(100)
                }
                repeat(3) {
                    content.swipe(Direction.UP, 1.0f)
                    device.waitForIdle(100)
                }
            }
        }
    }

    @Test
    fun scrollWithFiltering() = benchmarkRule.measureRepeated(
        packageName = TARGET_PACKAGE,
        metrics = listOf(FrameTimingMetric()),
        iterations = 3,
        startupMode = StartupMode.WARM,
        compilationMode = CompilationMode.Partial(),
        setupBlock = {
            pressHome()
            startActivityAndWait()
            device.wait(Until.hasObject(By.res("android:id/content")), 5_000)
            device.waitForIdle()
        }
    ) {
        // Test scrolling performance with different filters
        
        // Try to access filter menu (usually in toolbar/menu)
        device.findObject(By.desc("More options"))?.let { menu ->
            menu.click()
            device.waitForIdle(500)
            
            // Try to click on "Active tasks" filter if available
            device.findObject(By.textContains("Active"))?.let { activeFilter ->
                activeFilter.click()
                device.waitForIdle(500)
                
                // Scroll with active filter
                val scrollable = device.findObject(By.scrollable(true))
                scrollable?.let {
                    repeat(3) {
                        scrollable.scroll(Direction.DOWN, 1.0f)
                        device.waitForIdle(100)
                    }
                }
            }
            
            // Go back to show all tasks
            device.pressBack()
            device.waitForIdle(500)
        }
        
        // Final scroll test
        val scrollableElement = device.findObject(By.scrollable(true))
        scrollableElement?.let {
            repeat(3) {
                scrollableElement.scroll(Direction.DOWN, 1.0f)
                device.waitForIdle(100)
            }
        }
    }
}

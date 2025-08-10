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
import androidx.test.uiautomator.Until
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * This is a performance test for navigation between different screens in the Todo app.
 *
 * It measures frame timing metrics while navigating between:
 * - Tasks list
 * - Add/Edit task screen
 * - Task details
 * - Statistics
 */
@RunWith(AndroidJUnit4::class)
@LargeTest
class NavigationBenchmark {
    @get:Rule
    val benchmarkRule = MacrobenchmarkRule()

    @Test
    fun navigationCompilationNone() = navigation(CompilationMode.None())

    @Test
    fun navigationCompilationPartial() = navigation(CompilationMode.Partial())

    private fun navigation(compilationMode: CompilationMode) = benchmarkRule.measureRepeated(
        packageName = TARGET_PACKAGE,
        metrics = listOf(FrameTimingMetric()),
        iterations = 5,
        startupMode = StartupMode.WARM,
        compilationMode = compilationMode,
        setupBlock = {
            pressHome()
            startActivityAndWait()
            device.wait(Until.hasObject(By.res("android:id/content")), 5_000)
            device.waitForIdle(2_000)
        }
    ) {
        // Navigate to Add Task screen
        device.findObject(By.desc("Add task"))?.let { addTaskButton ->
            if (addTaskButton.isEnabled) {
                addTaskButton.click()
                device.wait(Until.hasObject(By.res("android:id/content")), 3_000)
                device.waitForIdle(1_000)
                
                // Navigate back to tasks list
                device.pressBack()
                device.wait(Until.hasObject(By.res("android:id/content")), 3_000)
                device.waitForIdle(1_000)
            }
        }

        // Try to open navigation drawer and navigate to Statistics
        // First try to find a menu button or drawer toggle
        val menuButton = device.findObject(By.desc("Open navigation drawer")) 
            ?: device.findObject(By.desc("Menu"))
            
        menuButton?.let {
            it.click()
            device.waitForIdle(1_000)
            
            // Look for Statistics option
            device.findObject(By.text("Statistics"))?.let { statistics ->
                statistics.click()
                device.wait(Until.hasObject(By.res("android:id/content")), 3_000)
                device.waitForIdle(1_000)
                
                // Navigate back to tasks
                device.findObject(By.desc("Open navigation drawer"))?.let { drawer ->
                    drawer.click()
                    device.waitForIdle(500)
                    device.findObject(By.textContains("Tasks"))?.let { tasks ->
                        tasks.click()
                        device.wait(Until.hasObject(By.res("android:id/content")), 3_000)
                        device.waitForIdle(1_000)
                    }
                } ?: run {
                    // If drawer navigation doesn't work, use back button
                    device.pressBack()
                    device.waitForIdle(1_000)
                }
            }
        }

        // Try to navigate to a task detail (if there are tasks)
        device.findObjects(By.clickable(true)).firstOrNull { obj ->
            val text = obj.text
            text != null && 
            text.isNotEmpty() && 
            !text.contains("Add") && 
            !text.contains("Statistics") &&
            !text.contains("Menu") &&
            text.length > 3
        }?.let { taskItem ->
            taskItem.click()
            device.wait(Until.hasObject(By.res("android:id/content")), 3_000)
            device.waitForIdle(1_000)
            
            // Navigate back to tasks list
            device.pressBack()
            device.wait(Until.hasObject(By.res("android:id/content")), 3_000)
            device.waitForIdle(1_000)
        }

        // Test filter navigation
        device.findObject(By.desc("More options"))?.let { moreOptions ->
            moreOptions.click()
            device.waitForIdle(500)
            
            // Try different filters
            device.findObject(By.textContains("Active"))?.let { activeFilter ->
                activeFilter.click()
                device.waitForIdle(1_000)
                
                // Go back to all tasks
                device.findObject(By.desc("More options"))?.click()
                device.waitForIdle(500)
                device.findObject(By.textContains("All"))?.click()
                device.waitForIdle(1_000)
            }
        }
    }

    @Test
    fun deepNavigationFlow() = benchmarkRule.measureRepeated(
        packageName = TARGET_PACKAGE,
        metrics = listOf(FrameTimingMetric()),
        iterations = 3,
        startupMode = StartupMode.WARM,
        compilationMode = CompilationMode.Partial(),
        setupBlock = {
            pressHome()
            startActivityAndWait()
            device.wait(Until.hasObject(By.res("android:id/content")), 5_000)
            device.waitForIdle(2_000)
        }
    ) {
        // Perform a deep navigation flow that simulates real user behavior:
        // Tasks -> Add Task -> Back -> Statistics -> Back -> Task Detail -> Edit -> Back -> Back
        
        // 1. Navigate to Add Task
        device.findObject(By.desc("Add task"))?.click()
        device.wait(Until.hasObject(By.res("android:id/content")), 3_000)
        device.waitForIdle(1_000)
        
        // 2. Go back to tasks
        device.pressBack()
        device.waitForIdle(1_000)
        
        // 3. Try to open drawer and go to Statistics
        device.findObject(By.desc("Open navigation drawer"))?.let {
            it.click()
            device.waitForIdle(500)
            device.findObject(By.text("Statistics"))?.click()
            device.waitForIdle(1_500)
            
            // 4. Go back to tasks
            device.findObject(By.desc("Open navigation drawer"))?.let { drawer ->
                drawer.click()
                device.waitForIdle(500)
                device.findObject(By.textContains("Tasks"))?.click()
                device.waitForIdle(1_000)
            }
        }
        
        // 5. Try to click on a task to see details
        device.findObjects(By.clickable(true)).firstOrNull { obj ->
            val text = obj.text
            text != null && text.isNotEmpty() && !text.contains("Add") && text.length > 3
        }?.let { task ->
            task.click()
            device.waitForIdle(1_500)
            
            // 6. Try to edit the task
            device.findObject(By.desc("Edit task"))?.let { editButton ->
                editButton.click()
                device.waitForIdle(1_000)
                
                // 7. Go back from edit
                device.pressBack()
                device.waitForIdle(1_000)
            }
            
            // 8. Go back to tasks list
            device.pressBack()
            device.waitForIdle(1_000)
        }
    }
}

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

import androidx.benchmark.macro.ExperimentalBaselineProfilesApi
import androidx.benchmark.macro.junit4.BaselineProfileRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Direction
import androidx.test.uiautomator.UiObject2
import androidx.test.uiautomator.Until
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Generates a baseline profile which can be copied to `app/src/main/baseline-prof.txt`.
 */
@OptIn(ExperimentalBaselineProfilesApi::class)
@RunWith(AndroidJUnit4::class)
@LargeTest
class BaselineProfileGenerator {
    @get:Rule
    val baselineProfileRule = BaselineProfileRule()

    @Test
    fun generateBaselineProfile() = baselineProfileRule.collectBaselineProfile(
        packageName = TARGET_PACKAGE
    ) {
        pressHome()
        startActivityAndWait()

        // TODO: Add more interactions to cover critical user journeys.
        // This could include:
        // - Opening and navigating between different sections
        // - Creating, editing, and deleting tasks
        // - Filtering tasks by different criteria
        // - Viewing task details and statistics

        // Basic interactions for Todo app
        
        // Wait for the app to be ready and find the main content
        device.wait(Until.hasObject(By.res("android:id/content")), TIMEOUT)

        // Navigate through key app sections to warm up the code paths
        
        // Add a new task
        device.findObject(By.desc("Add task"))?.let { button: UiObject2 ->
            if (button.isEnabled) {
                button.click()
                device.wait(Until.hasObject(By.res("android:id/content")), TIMEOUT)
                
                // Go back to main screen
                device.pressBack()
                device.wait(Until.hasObject(By.res("android:id/content")), TIMEOUT)
            }
        }

        // Try to access drawer menu if available
        device.findObject(By.desc("Open navigation drawer"))?.let { drawer: UiObject2 ->
            drawer.click()
            device.wait(Until.hasObject(By.text("Statistics")), TIMEOUT)
            
            // Click on statistics
            device.findObject(By.text("Statistics"))?.let { stats: UiObject2 ->
                stats.click()
                device.wait(Until.hasObject(By.res("android:id/content")), TIMEOUT)
                
                // Go back to main screen
                device.findObject(By.desc("Open navigation drawer"))?.click()
                device.wait(Until.hasObject(By.textContains("Tasks")), TIMEOUT)
                device.findObject(By.textContains("Tasks"))?.click()
                device.wait(Until.hasObject(By.res("android:id/content")), TIMEOUT)
            }
        }

        // Scroll through tasks if list is present
        val tasksList = device.findObject(By.scrollable(true))
        tasksList?.let { list: UiObject2 ->
            repeat(3) {
                list.scroll(Direction.DOWN, 1.0f)
                Thread.sleep(500)
            }
            repeat(3) {
                list.scroll(Direction.UP, 1.0f)
                Thread.sleep(500)
            }
        }

        // Try to interact with filter menu
        device.findObject(By.desc("More options"))?.let { menu: UiObject2 ->
            menu.click()
            device.wait(Until.hasObject(By.textContains("Active")), TIMEOUT)
            
            // Click on active tasks filter
            device.findObject(By.textContains("Active"))?.click()
            device.wait(Until.hasObject(By.res("android:id/content")), TIMEOUT)
            
            // Switch back to all tasks
            device.findObject(By.desc("More options"))?.click()
            device.findObject(By.textContains("All"))?.click()
            device.wait(Until.hasObject(By.res("android:id/content")), TIMEOUT)
        }
    }

    companion object {
        private const val TIMEOUT = 5_000L
    }
}

/*
 * Copyright 2020 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.hilt) apply false
    alias(libs.plugins.compose.compiler) apply false
    alias(libs.plugins.android.test) apply false
}

// Convenience tasks for running macrobenchmarks
tasks.register("benchmarkStartup") {
    description = "Run startup benchmarks"
    group = "benchmark"
    dependsOn(":macrobenchmark:connectedBenchmarkAndroidTest")
    doFirst {
        project.gradle.startParameter.projectProperties["android.testInstrumentationRunnerArguments.class"] = 
            "com.example.android.architecture.blueprints.todoapp.benchmark.StartupBenchmark"
    }
}

tasks.register("benchmarkScroll") {
    description = "Run scroll performance benchmarks"
    group = "benchmark"
    dependsOn(":macrobenchmark:connectedBenchmarkAndroidTest")
    doFirst {
        project.gradle.startParameter.projectProperties["android.testInstrumentationRunnerArguments.class"] = 
            "com.example.android.architecture.blueprints.todoapp.benchmark.ScrollBenchmark"
    }
}

tasks.register("benchmarkNavigation") {
    description = "Run navigation performance benchmarks"
    group = "benchmark"
    dependsOn(":macrobenchmark:connectedBenchmarkAndroidTest")
    doFirst {
        project.gradle.startParameter.projectProperties["android.testInstrumentationRunnerArguments.class"] = 
            "com.example.android.architecture.blueprints.todoapp.benchmark.NavigationBenchmark"
    }
}

tasks.register("generateBaselineProfile") {
    description = "Generate baseline profile"
    group = "benchmark"
    dependsOn(":macrobenchmark:connectedBenchmarkAndroidTest")
    doFirst {
        project.gradle.startParameter.projectProperties["android.testInstrumentationRunnerArguments.class"] = 
            "com.example.android.architecture.blueprints.todoapp.benchmark.BaselineProfileGenerator"
    }
}

tasks.register("benchmarkAll") {
    description = "Run all benchmarks"
    group = "benchmark"
    dependsOn(":macrobenchmark:connectedBenchmarkAndroidTest")
}

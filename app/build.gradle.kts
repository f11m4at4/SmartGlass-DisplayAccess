/*
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 * All rights reserved.
 *
 * This source code is licensed under the license found in the
 * LICENSE file in the root directory of this source tree.
 */

import java.util.Properties
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
  alias(libs.plugins.android.application)
  alias(libs.plugins.jetbrains.kotlin.android)
  alias(libs.plugins.compose.compiler)
  alias(libs.plugins.kotlin.serialization)
}

val localProperties =
    Properties().apply {
      val localPropertiesFile = rootProject.file("local.properties")
      if (localPropertiesFile.exists()) {
        localPropertiesFile.inputStream().use(::load)
      }
    }

android {
  namespace = "com.meta.wearable.dat.externalsampleapps.displayaccess"
  compileSdk = 36

  buildFeatures { buildConfig = true }

  defaultConfig {
    applicationId = "com.meta.wearable.dat.externalsampleapps.displayaccess"
    minSdk = 31
    targetSdk = 36
    versionCode = 1
    versionName = "1.0"

    manifestPlaceholders["mwdat_application_id"] =
        providers.gradleProperty("mwdat_application_id").orNull
            ?: localProperties.getProperty("mwdat_application_id", "")
    manifestPlaceholders["mwdat_client_token"] =
        providers.gradleProperty("mwdat_client_token").orNull
            ?: localProperties.getProperty("mwdat_client_token", "")

    buildConfigField(
        "String",
        "OPENAI_API_KEY",
        "\"${providers.gradleProperty("openai_api_key").orNull ?: localProperties.getProperty("openai_api_key", "")}\"",
    )
    buildConfigField(
        "String",
        "GOOGLE_MAPS_API_KEY",
        "\"${providers.gradleProperty("google_maps_api_key").orNull ?: localProperties.getProperty("google_maps_api_key", "")}\"",
    )
  }

  buildTypes {
    release {
      isMinifyEnabled = true
      proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
    }
  }

  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
  }

  packaging { resources { excludes += "/META-INF/{AL2.0,LGPL2.1}" } }
}

kotlin { compilerOptions { jvmTarget = JvmTarget.JVM_17 } }

dependencies {
  implementation(libs.androidx.activity.compose)
  implementation(platform(libs.androidx.compose.bom))
  implementation(libs.androidx.lifecycle.runtime.compose)
  implementation(libs.androidx.lifecycle.viewmodel.compose)
  implementation(libs.androidx.material.icons.extended)
  implementation(libs.androidx.material3)
  implementation(libs.androidx.navigation.compose)
  implementation(libs.google.play.services.location)
  implementation(libs.kotlinx.serialization.json)
  implementation(libs.mwdat.core)
  implementation(libs.mwdat.display)
  implementation(libs.okhttp)
}

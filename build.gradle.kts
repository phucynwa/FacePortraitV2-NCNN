buildscript {
    val agp_version by extra("8.2.0-beta01")
}
// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    id("com.android.application") version "8.2.0-beta01" apply false
    id("org.jetbrains.kotlin.android") version "1.9.10" apply false
}
val buildToolsVersion by extra("34.0.0")
val ndkVersion by extra("26.0.10636728 rc2")

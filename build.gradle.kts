buildscript {
    repositories {
        google()
        mavenCentral()
    }

    dependencies {
        classpath("com.google.devtools.ksp:symbol-processing-gradle-plugin:2.3.6")
    }
}

plugins {
    alias(libs.plugins.android.application) apply false
}
object Versions {
    const val minSdkVersion = 23
    const val targetSdkVersion = 29

    const val kotlin = "1.3.60-eap-76"
    const val coroutinesVersion = "1.3.2"

    const val androidxAppcompat = "1.1.0"
    const val androidxCore = "1.1.0"
    const val androidxUiLayout = "0.1.0-dev02"
    const val androidxUiTooling = "0.1.0-dev02"
    const val androidxMaterial = "0.1.0-dev02"
    const val androidxActivity = "1.1.0-rc02"
    const val androidxFragment = "1.2.0-rc02"
    const val androidxLifecycle = "2.2.0-rc02"

    const val daggerVersion = "2.25.2"
    const val okhttpVersion = "4.2.2"
    const val retrofitVersion = "2.6.2"
    const val moshiVersion = "1.9.1"

    const val junit = "4.12"
    const val androidTestJunit = "1.1.1"
}

object ProjectDependencies {
    const val mavenUrl = "https://dl.bintray.com/kotlin/kotlin-eap"
}

object Dependencies {
    const val kotlinStdlib = "org.jetbrains.kotlin:kotlin-stdlib-jdk7:${Versions.kotlin}"
    const val coroutinesCore = "org.jetbrains.kotlinx:kotlinx-coroutines-core:${Versions.coroutinesVersion}"
    const val coroutinesAndroid =  "org.jetbrains.kotlinx:kotlinx-coroutines-android:${Versions.coroutinesVersion}"

    const val androidxAppcompat = "androidx.appcompat:appcompat:${Versions.androidxAppcompat}"
    const val androidxCore = "androidx.core:core-ktx:${Versions.androidxCore}"
    const val androidxUiLayout = "androidx.ui:ui-layout:${Versions.androidxUiLayout}"
    const val androidxUiTooling = "androidx.ui:ui-tooling:${Versions.androidxUiTooling}"
    const val androidxMaterial = "androidx.ui:ui-material:${Versions.androidxMaterial}"
    const val androidxActivity = "androidx.activity:activity-ktx:${Versions.androidxActivity}"
    const val androidxFragment = "androidx.fragment:fragment-ktx:${Versions.androidxFragment}"
    const val androidxLifecycle = "androidx.lifecycle:lifecycle-extensions:${Versions.androidxLifecycle}"
    const val androidxLifecycleExt = "androidx.lifecycle:lifecycle-runtime-ktx:${Versions.androidxLifecycle}"
    const val androidxLifecycleViewModelExt = "androidx.lifecycle:lifecycle-viewmodel-ktx:${Versions.androidxLifecycle}"

    const val dagger = "com.google.dagger:dagger:${Versions.daggerVersion}"
    const val daggerCompiler = "com.google.dagger:dagger-compiler:${Versions.daggerVersion}"
    const val daggerAndroid = "com.google.dagger:dagger-android-support:${Versions.daggerVersion}"
    const val daggerAndroidCompiler = "com.google.dagger:dagger-android-processor:${Versions.daggerVersion}"

    const val okhttpCore = "com.squareup.okhttp3:okhttp:${Versions.okhttpVersion}"
    const val okhttpLoggingInterceptor =  "com.squareup.okhttp3:logging-interceptor:${Versions.okhttpVersion}"
    const val retrofitCore = "com.squareup.retrofit2:retrofit:${Versions.retrofitVersion}"
    const val retrofitMoshiConverter = "com.squareup.retrofit2:converter-moshi:${Versions.retrofitVersion}"
    const val moshi = "com.squareup.moshi:moshi:${Versions.moshiVersion}"
    const val moshiCodegen = "com.squareup.moshi:moshi-kotlin-codegen:${Versions.moshiVersion}"
    const val leakcanary = "com.squareup.leakcanary:leakcanary-android:2.0-beta-3"
    const val timber = "com.jakewharton.timber:timber:4.7.1"

    const val junit = "junit:junit:${Versions.junit}"
    const val androidTestJunit = "androidx.test.ext:junit:${Versions.androidTestJunit}"
}
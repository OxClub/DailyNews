plugins {
 id("com.android.application")
 id("org.jetbrains.kotlin.android")
 id("org.jetbrains.kotlin.kapt")
 id("org.jetbrains.kotlin.plugin.compose")
}
android { namespace="com.oxclub.dailynews"; compileSdk=35
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }
 defaultConfig { applicationId="com.oxclub.dailynews"; minSdk=24; targetSdk=35; versionCode=2; versionName="2.0" }
 buildTypes { release { isMinifyEnabled=false; proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"),"proguard-rules.pro") } }
 buildFeatures { compose=true; buildConfig=true }
 buildTypes.configureEach {
  val key = providers.gradleProperty("NEWS_API_KEY").orElse(providers.environmentVariable("NEWS_API_KEY")).orElse("").get()
  buildConfigField("String", "NEWS_API_KEY", "\"$key\"")
 }
}
dependencies {
 implementation(platform("androidx.compose:compose-bom:2025.01.00"))
 implementation("androidx.activity:activity-compose:1.10.1")
 implementation("androidx.compose.ui:ui")
 implementation("androidx.compose.ui:ui-tooling-preview")
 implementation("androidx.compose.material3:material3")
 implementation("androidx.compose.material:material-icons-extended")
 implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.7")
 implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")
 implementation("androidx.room:room-runtime:2.6.1")
 implementation("androidx.room:room-ktx:2.6.1")
 kapt("androidx.room:room-compiler:2.6.1")
 implementation("com.squareup.retrofit2:retrofit:2.11.0")
 implementation("com.squareup.retrofit2:converter-gson:2.11.0")
 implementation("io.coil-kt:coil-compose:2.7.0")
}

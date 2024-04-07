plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("kotlin-kapt")
    id("com.google.gms.google-services")
    id("com.google.devtools.ksp")
    id("com.google.android.libraries.mapsplatform.secrets-gradle-plugin") version "2.0.1" apply false
}

android {


    namespace = "com.streetox.streetox"
    compileSdk = 34
    buildToolsVersion = "34.0.0"

    defaultConfig {
        applicationId = "com.streetox.streetox"
        minSdk = 28
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    buildFeatures{
        viewBinding = true
    }
    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }


}


tasks.register<Wrapper>("wrapper") {
    gradleVersion = "7.2"


}

tasks.register("prepareKotlinBuildScriptModel"){}

dependencies {

    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.room:room-common:2.6.1")
    implementation("com.android.car.ui:car-ui-lib:2.6.0")
    implementation("com.google.firebase:firebase-crashlytics-buildtools:2.9.9")
    implementation("androidx.datastore:datastore-core-android:1.1.0-beta02")
    implementation("androidx.work:work-runtime-ktx:2.9.0")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    implementation("androidx.activity:activity:1.8.2")

    //firebase
    implementation("com.google.firebase:firebase-auth:22.3.1")
    implementation("com.google.firebase:firebase-database:20.3.1")
    implementation("com.google.android.gms:play-services-auth:21.0.0")
    implementation(platform("com.google.firebase:firebase-bom:32.7.4"))
    implementation("com.google.firebase:firebase-storage")

    //text dimension
    implementation("com.intuit.sdp:sdp-android:1.1.0")
    implementation("com.intuit.ssp:ssp-android:1.1.0")

    // Navigation Component
    implementation("androidx.navigation:navigation-fragment-ktx:2.7.7")
    implementation("androidx.navigation:navigation-ui-ktx:2.7.7")

    //lifecycle
    implementation("androidx.lifecycle:lifecycle-extensions:2.2.0")
    implementation("androidx.lifecycle:lifecycle-common-java8:2.7.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0")

    //loading button
    implementation("com.github.leandroborgesferreira:loading-button-android:2.3.0")

    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0")

    //facebook
    implementation ("com.facebook.android:facebook-android-sdk:latest.release")


    //circular image
    implementation ("de.hdodenhof:circleimageview:3.1.0")

    //glide
    implementation("com.github.bumptech.glide:glide:4.16.0")

    //room
    val room_version = "2.6.1"

    implementation("androidx.room:room-runtime:$room_version")
    annotationProcessor("androidx.room:room-compiler:$room_version")

    // To use Kotlin annotation processing tool (kapt)
    ksp("androidx.room:room-compiler:$room_version")

    // optional - Kotlin Extensions and Coroutines support for Room
    implementation("androidx.room:room-ktx:$room_version")

    //corotines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")


    implementation("com.theartofdev.edmodo:android-image-cropper:2.8.0")

//material.io
    implementation("com.google.android.material:material:1.11.0")

    //google maps
    implementation("com.google.android.gms:play-services-maps:18.2.0")

    //places api
    implementation("com.google.android.libraries.places:places:3.3.0")

    //libraries
    implementation("com.firebase:geofire-android:3.2.0")
    implementation("com.google.firebase:firebase-database:20.3.1")
    implementation("com.google.android.gms:play-services-location:21.2.0")
    implementation("com.karumi:dexter:6.2.3")

    //event bus
    implementation("org.greenrobot:eventbus:3.3.1")

    implementation("com.airbnb.android:lottie:6.1.0")

    //shimmer effect
    implementation("com.facebook.shimmer:shimmer:0.5.0@aar")

    //notification
    implementation("com.google.firebase:firebase-messaging:23.4.1")
    implementation("com.squareup.retrofit2:retrofit:2.6.2")
    implementation("com.squareup.retrofit2:converter-gson:2.6.0")
    implementation("com.squareup.retrofit2:converter-simplexml:2.1.0")
    implementation("com.squareup.retrofit2:adapter-rxjava:2.1.0")

    //coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-collections-immutable-jvm:0.3.4")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.3.9")



    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")

    implementation ("androidx.activity:activity-ktx:1.4.0")


    implementation("com.squareup.picasso:picasso:2.71828")

    //razorpay
    implementation("com.razorpay:checkout:1.6.36")
}
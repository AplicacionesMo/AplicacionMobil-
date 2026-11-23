import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
}

// Datos de la firma: se leen de keystore.properties (en la raiz del proyecto)
val propiedadesFirma = Properties().apply {
    val archivo = rootProject.file("keystore.properties")
    if (archivo.exists()) archivo.inputStream().use { load(it) }
}

android {
    namespace = "cl.duoc.serviciotecnico"
    compileSdk = 35

    defaultConfig {
        applicationId = "cl.duoc.serviciotecnico"
        minSdk = 26
        targetSdk = 35
        versionCode = 2
        versionName = "2.0"

        // Direcciones de los microservicios. 10.0.2.2 es "localhost del PC" visto desde el emulador.
        // En un telefono fisico, reemplazar por la IP del PC en la misma red (ej. 192.168.1.10).
        buildConfigField("String", "URL_USUARIOS", "\"http://10.0.2.2:8081/\"")
        buildConfigField("String", "URL_TICKETS", "\"http://10.0.2.2:8082/\"")
        buildConfigField("String", "URL_GUIAS", "\"http://10.0.2.2:8083/\"")
        // API externa (Open-Meteo, sin llave)
        buildConfigField("String", "URL_CLIMA", "\"https://api.open-meteo.com/\"")
    }

    signingConfigs {
        create("release") {
            storeFile = rootProject.file(propiedadesFirma.getProperty("storeFile", "app/keystore/tacticaldev-release.jks"))
            storePassword = propiedadesFirma.getProperty("storePassword")
            keyAlias = propiedadesFirma.getProperty("keyAlias")
            keyPassword = propiedadesFirma.getProperty("keyPassword")
        }
    }

    buildTypes {
        debug {
            // Habilita el informe de cobertura: ./gradlew createDebugUnitTestCoverageReport
            enableUnitTestCoverage = true
        }
        release {
            isMinifyEnabled = false
            signingConfig = signingConfigs.getByName("release")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    testOptions {
        // Las clases de Android (Log, Uri...) devuelven valores por defecto en las pruebas JVM
        unitTests.isReturnDefaultValues = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material.icons.extended)
    debugImplementation(libs.androidx.ui.tooling)

    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.navigation.compose)

    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    implementation(libs.play.services.location)
    implementation(libs.coil.compose)

    // Consumo de microservicios y API externa
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.gson)
    implementation(libs.okhttp.logging)

    // Pruebas unitarias: JUnit 4 + MockK + corrutinas de prueba
    testImplementation(libs.junit)
    testImplementation(libs.mockk)
    testImplementation(libs.kotlinx.coroutines.test)
}

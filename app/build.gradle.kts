import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    jacoco
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

        // Direcciones de los microservicios publicados en Railway. Asi la app funciona en un
        // telefono fisico con datos o WiFi, sin depender del PC.
        // Para probar contra los microservicios locales en el emulador, cambiar por
        // http://10.0.2.2:8081/, :8082 y :8083 (10.0.2.2 es "localhost del PC" visto por el emulador).
        buildConfigField("String", "URL_USUARIOS", "\"https://aplicacionmobil-production.up.railway.app/\"")
        buildConfigField("String", "URL_TICKETS", "\"https://beneficial-contentment-production.up.railway.app/\"")
        buildConfigField("String", "URL_GUIAS", "\"https://determined-empathy-production-177878af.up.railway.app/\"")
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

// ---------- Cobertura de la LOGICA (la pauta pide 80 %) ----------
// Ejecutar:  ./gradlew coberturaLogica
// Informe:   app/build/reports/jacoco/coberturaLogica/html/index.html
// Mide ViewModel, repositorios, dominio y la capa de red. Deja fuera lo que no es logica:
// pantallas Compose, tema, navegacion, clases generadas, DTO/entidades (solo datos)
// y clases que dependen del sistema Android (se prueban a mano en la app).
jacoco {
    toolVersion = "0.8.12"
}

tasks.register<JacocoReport>("coberturaLogica") {
    group = "verification"
    description = "Cobertura de las pruebas unitarias sobre la logica de la app"
    dependsOn("testDebugUnitTest")

    reports {
        html.required.set(true)
        xml.required.set(true)
    }

    val fueraDeLaLogica = listOf(
        "**/R.class", "**/R$*.class", "**/BuildConfig.*", "**/Manifest*.*",
        "**/*_Impl*", "**/*ComposableSingletons*",
        "**/ui/screens/**", "**/ui/components/**", "**/ui/theme/**", "**/ui/navigation/**",
        "**/MainActivity*", "**/ServicioTecnicoApp*",
        "**/data/remote/dto/**", "**/data/local/entity/**", "**/data/local/AppDatabase*",
        "**/data/remote/RetrofitService*", "**/PreferenciasSesion*", "**/util/**"
    )

    classDirectories.setFrom(
        fileTree(layout.buildDirectory.dir("tmp/kotlin-classes/debug")) { exclude(fueraDeLaLogica) }
    )
    sourceDirectories.setFrom(files("src/main/java"))
    executionData.setFrom(
        fileTree(layout.buildDirectory) {
            include("outputs/unit_test_code_coverage/debugUnitTest/testDebugUnitTest.exec")
        }
    )
}

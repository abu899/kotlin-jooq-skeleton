import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent

plugins {
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.spring.dependencyManagement) apply false
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.spring) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    idea
}



repositories {
    mavenCentral()
}

val lib = rootProject.libs

subprojects {
    group = "com.ktp"
    version = "1.0-SNAPSHOT"

    apply {
        plugin(lib.plugins.kotlin.jvm.get().pluginId)
        plugin(lib.plugins.kotlin.serialization.get().pluginId)
        plugin(lib.plugins.test.fixtures.get().pluginId)
        plugin(lib.plugins.spring.boot.get().pluginId)
        plugin(lib.plugins.spring.dependencyManagement.get().pluginId)
        plugin(lib.plugins.kotlin.spring.get().pluginId)
    }

    dependencies {
        implementation(platform(lib.kotlin.bom))
        implementation(platform(lib.kotlinx.coroutines.bom))
        implementation(platform(lib.kotest.bom))
        implementation(lib.bundles.kotlinx)

        testImplementation(lib.kotlin.test)
        testImplementation(lib.bundles.kotest)
        testImplementation(lib.mockk)
    }

    tasks {
        kotlin {
            compilerOptions {
                freeCompilerArgs.addAll("-opt-in=kotlin.RequiresOptIn", "-Xcontext-receivers")
            }
        }

        test {
            useJUnitPlatform()
            testLogging {
                showStandardStreams = true
                events.add(TestLogEvent.FAILED)
                exceptionFormat = TestExceptionFormat.FULL
            }
        }
    }

    kotlin {
        jvmToolchain(21)
    }
}

allprojects {
    tasks {
        bootJar {
            enabled = false
        }

        if (project.name == "presentation") {
            bootJar {
                archiveVersion.set("")
                enabled = true
            }
        } else {
            bootJar {
                enabled = false
            }
            jar { enabled = true }
        }
    }
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(21)
}

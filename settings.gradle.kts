plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}
rootProject.name = "kotlin-jooq-skeleton"

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
include(
    "domain",
    "infra",
    "application",
    "presentation",
)

dependencyResolutionManagement {
    repositories {
        mavenCentral()
    }
}

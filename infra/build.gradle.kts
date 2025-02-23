import org.testcontainers.containers.MySQLContainer

plugins {
    alias(libs.plugins.jooq)
    alias(libs.plugins.flyway.gradle)
}

buildscript {
    dependencies {
        classpath(libs.flyway.mysql)
        classpath(libs.mysql.testcontainer)
        classpath(libs.mysql.connector)
    }
}

dependencies {
    api(libs.arrow)

    api(libs.spring.boot.starter)
    api(libs.spring.boot.starter.jooq)

    implementation(libs.flyway.core)
    implementation(libs.flyway.mysql)
    implementation(libs.mysql.testcontainer)

    runtimeOnly(libs.mysql.connector)
    jooqGenerator(libs.mysql.connector)
}

jooq {
    version = libs.versions.jooq.asProvider().get()
    configurations {
        create("main") {
            jooqConfiguration.apply {
                jdbc.apply {
                    driver = "com.mysql.cj.jdbc.Driver"
                    // url = "jdbc:mysql://localhost:3306/sample"
                    // user = "sa"
                    // password = ""
                }
                generator.apply {
                    name = "org.jooq.codegen.DefaultGenerator"
                    database.apply {
                        name = "org.jooq.meta.mysql.MySQLDatabase"
                        inputSchema = "sample"
                    }
                    generate.apply {
                        isDaos = true
                        isUdts = true
                        isDeprecated = false
                        isRecords = true
                        isFluentSetters = true

                        //            isPojosAsKotlinDataClasses = false
                        //            isImmutablePojos = false
                        //            isPojos = false
                        isPojosAsKotlinDataClasses = false
                        isImmutablePojos = false
                        isPojos = false
                        //            isPojosAsKotlinDataClasses = true
                        //            isImmutablePojos = true
                        //            isPojos = true

                        // cause https://github.com/jOOQ/jOOQ/issues/14785
                        isRecordsImplementingRecordN = false
                        isKotlinNotNullRecordAttributes = true
                        isKotlinNotNullPojoAttributes = true
                        isKotlinNotNullInterfaceAttributes = true
                    }
                    target.apply {
                        packageName = "com.brett.sample.persistence.model"
                        directory = "build/generated-src/jooq/main"
                    }
                    strategy.name = "org.jooq.codegen.example.JPrefixGeneratorStrategy"
                }
            }
        }
    }
}

tasks.named("generateJooq") {
    lateinit var mySqlContainer: MySQLContainer<Nothing>

    doFirst {
        // run MySQL container
        mySqlContainer = MySQLContainer<Nothing>("mysql:8.0").apply {
            withDatabaseName("sample")
            withUsername("root")
            withPassword("root")
            withEnv("TZ", "Asia/Seoul")
            withCommand("mysqld", "--character-set-server=utf8mb4")
            withReuse(false)
            start()
        }

        // setup flyway plugin configuration
        flyway.url = mySqlContainer.jdbcUrl
        flyway.user = mySqlContainer.username
        flyway.password = mySqlContainer.password
        flyway.locations = arrayOf("filesystem:src/main/resources/db/migration")

        // run `flywayMigration` task
        val flywayMigrateTask = tasks.named("flywayMigrate").get()
        flywayMigrateTask.actions.forEach { it.execute(flywayMigrateTask) }

        // setup jooq plugin configuration
        jooq.configurations["main"].jooqConfiguration.apply {
            jdbc.url = mySqlContainer.jdbcUrl
            jdbc.user = mySqlContainer.username
            jdbc.password = mySqlContainer.password

            generator.database.inputSchema = mySqlContainer.databaseName
        }
    }

    doLast {
        // shutdown MySQL container after code generation
        mySqlContainer.stop()
    }
}

plugins {
    id("application")
}

group = "top.toobee.modpack"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.google.code.gson:gson:2.12.1")
    implementation("org.jetbrains:annotations:26.0.2")
}

application {
    mainClass = "top.toobee.modpack.Main"
}

val dir = "run"

tasks.withType<JavaExec> {
    file(dir).apply {
        if (!exists()) {
            if (mkdir())
                println("Created directory: $dir")
            else
                println("Failed to create directory: $dir")
        }
    }

    standardInput = System.`in`
    workingDir = file(dir)
}

fun runWithParameters(arg: String) {
    tasks.register(arg) {
        group = "application"
        description = "Run with parameters: $arg"
        dependsOn("classes")
        doLast {
            tasks.named<JavaExec>("run") {
                args = listOf(arg)
            }.get().exec()
        }
    }
}

runWithParameters("getProjects")

plugins {
    application
    id("java")
    id("org.beryx.runtime") version "1.13.1"
}

group = "s3jsync"
version = "1.1.0"

repositories { mavenCentral() }

dependencies {
    implementation(platform("software.amazon.awssdk:bom:2.32.3"))
    implementation("software.amazon.awssdk:s3")
    implementation("software.amazon.awssdk:apache-client")

    implementation("com.google.guava:guava:33.4.8-jre")
    implementation("org.slf4j:slf4j-api:2.0.13")
    runtimeOnly("org.slf4j:slf4j-nop:2.0.13")
}

configurations.all {
    exclude(group = "io.netty")
    exclude(group = "io.projectreactor")
}

application {
    mainClass.set("s3jsync.Main")
}

java {
    toolchain { languageVersion.set(JavaLanguageVersion.of(21)) }
}

tasks.jar {
    manifest { attributes["Main-Class"] = "s3jsync.Main" }
}

tasks.withType<Test>().configureEach { enabled = false }

runtime {
    options = listOf("--strip-debug", "--no-header-files", "--no-man-pages")
    modules = listOf("jdk.crypto.ec", "jdk.crypto.cryptoki", "java.naming", "java.xml")

    launcher {
        jvmArgs = listOf()
    }

    jpackage {
        val pkgType = (project.findProperty("pkg") as String?) ?: "deb"
        installerType = pkgType

        outputDir = "jpackage"
        imageName = "s3-jsync"
        installerName = "s3-jsync"
        appVersion = project.version.toString()

        // Common metadata
        installerOptions = installerOptions + listOf(
            "--vendor", "s3jsync",
            "--verbose"
        )

        when (pkgType) {
            // debian
            "deb" -> {
                resourceDir = file("packaging/deb")
                installerOptions = installerOptions + listOf("--resource-dir", file("packaging/deb").absolutePath)

                installerOptions = installerOptions + listOf(
                    "--linux-menu-group", "Utilities",
                    "--linux-shortcut",
                    "--install-dir", "/usr/lib"
                )
            }

            // rpm
            "rpm" -> {
                resourceDir = file("packaging/rpm")
                installerOptions = installerOptions + listOf("--resource-dir", file("packaging/rpm").absolutePath)

                installerOptions = installerOptions + listOf(
                    "--linux-menu-group", "Utilities",
                    "--linux-shortcut"
                )
            }

            // windows (exe/msi)
            "exe", "msi" -> {
                installerOptions = installerOptions + listOf("--win-dir-chooser", "--win-per-user-install")
            }

            // app image
            "app-image" -> {
            }
        }
    }

}

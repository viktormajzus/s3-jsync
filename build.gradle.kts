plugins {
    application
    id("java")
    id("org.beryx.runtime") version "1.13.1"
}

group = "s3jsync"
version = "1.0.0"

repositories { mavenCentral() }

dependencies {
    implementation(platform("software.amazon.awssdk:bom:2.32.3"))
    implementation("software.amazon.awssdk:s3")
    // Use Apache HTTP client (avoids Netty/BlockHound module headaches)
    implementation("software.amazon.awssdk:apache-client")

    implementation("com.google.guava:guava:33.4.8-jre")
    implementation("org.slf4j:slf4j-api:2.0.13")
    runtimeOnly("org.slf4j:slf4j-nop:2.0.13")
}

// Keep Netty/reactor off the classpath
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

// ---- badass-runtime (non-modular app, jlink+jpackage) ----
runtime {
    // jlink options
    options = listOf("--strip-debug", "--no-header-files", "--no-man-pages")
    // Add crypto modules often needed by AWS TLS
    modules = listOf("jdk.crypto.ec", "jdk.crypto.cryptoki", "java.naming", "java.xml")

    launcher {
        // Only jvmArgs etc. are supported here (no `name` in this plugin)
        jvmArgs = listOf()
    }

    jpackage {
        // Choose output: deb | rpm | app-image  (defaults to deb)
        val pkgType = (project.findProperty("pkg") as String?) ?: "deb"
        installerType = pkgType

        val resDir = when (pkgType) {
            "deb" -> file("packaging/deb")
            "rpm" -> file("packaging/rpm")
            else  -> null
        }
        if (resDir != null) {
            resourceDir = resDir
            // Avoid '+=' ambiguity by rebuilding the list
            installerOptions = installerOptions + listOf("--resource-dir", resDir.absolutePath)
        }

        // Where to write images/installers
        outputDir = "jpackage"

        // Names shown in the produced image/installer
        imageName = "s3-jsync"
        installerName = "s3-jsync"

        // Pass extra raw jpackage flags here
        installerOptions = installerOptions + listOf(
            "--linux-menu-group", "Utilities",
            "--linux-shortcut",
            "--vendor", "s3jsync",
            "--verbose"
        )

        // If you add an icon or license later:
        // resourceDir = file("packaging/resources")
        // imageOptions = listOf("--icon", file("packaging/icon.png").absolutePath)
        // installerOptions += listOf("--license-file", file("LICENSE").absolutePath)
        appVersion = project.version.toString()
    }
}

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
        val pkgType = (project.findProperty("pkg") as String?) ?: "deb"
        installerType = pkgType

        installerOptions = installerOptions + listOf("--install-dir", "/usr/lib")

        // point to the folder that contains s3-jsync.spec
        if (pkgType == "rpm") {
            resourceDir = file("packaging/rpm")
        } else if (pkgType == "deb") {
            resourceDir = file("packaging/deb")
            installerOptions = installerOptions + listOf("--resource-dir", file("packaging/deb").absolutePath)
        }

        outputDir = "jpackage"
        imageName = "s3-jsync"
        installerName = "s3-jsync"
        appVersion = project.version.toString()

        // verbose + temp just to inspect what jpackage uses
        installerOptions = listOf(
            "--linux-menu-group","Utilities",
            "--linux-shortcut",
            "--vendor","s3jsync",
            "--verbose",
            "--temp", file("build/jpkg-tmp").absolutePath
        )
    }
}

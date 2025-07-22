plugins {
    application
    id("java")
}

group = "s3jsync"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(platform("software.amazon.awssdk:bom:2.32.3"))
    implementation("software.amazon.awssdk:s3")
    implementation("com.google.guava:guava:33.4.8-jre")
}

application {
    mainClass.set("s3jsync.Main")
}

tasks.jar {
    manifest {
        attributes["Main-Class"] = "s3jsync.Main"
    }
}

tasks.withType<Test>().configureEach {
    enabled = false
}
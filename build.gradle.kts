plugins {
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
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks.test {
    useJUnitPlatform()
}
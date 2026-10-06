plugins {
    java
    jacoco
    checkstyle
    pmd
    id("com.github.spotbugs") version "6.5.11"
}

group = "com.pramora.testable"
version = "1.0.0"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation("junit:junit:4.13.2")
    implementation("commons-collections:commons-collections:3.2.1")
    implementation("org.apache.commons:commons-text:1.9")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.9.10.1")
    implementation("log4j:log4j:1.2.17")
    implementation("org.yaml:snakeyaml:1.30")
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

jacoco {
    toolVersion = "0.8.14"
}

tasks.test {
    useJUnit()
    finalizedBy(tasks.jacocoTestReport)
}

checkstyle {
    toolVersion = "14.1.0"
    configFile = file("tools/checkstyle/checkstyle.xml")
}

pmd {
    toolVersion = "7.26.0"
    ruleSetFiles = files("tools/pmd/ruleset.xml")
    ruleSets = emptyList()
}

spotbugs {
    toolVersion.set("4.10.3")
    excludeFilter.set(file("tools/spotbugs/exclude.xml"))
}

tasks.jar {
    manifest {
        attributes(mapOf("Main-Class" to "com.pramora.testable.app.Main"))
    }
}

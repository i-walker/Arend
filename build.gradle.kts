import com.google.protobuf.gradle.ExecutableLocator

plugins {
    java
    idea
    antlr
    id("com.google.protobuf") version "0.8.6"
}

repositories {
    jcenter()
    mavenCentral()
}

dependencies {
    compile("net.harawata:appdirs:1.0.1")
    compile("commons-cli:commons-cli:1.4")
    compile("com.google.guava:guava:26.0-jre")
    compile("com.google.protobuf:protobuf-java:3.6.1")
    compile("com.google.code.findbugs:jsr305:3.0.2")

    testCompile("junit:junit:4.12")
    testCompile("org.hamcrest:hamcrest-library:1.3")

    antlr("org.antlr:antlr4:4.7.1")
    implementation("org.antlr:antlr4-runtime:4.7.1")
}

configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

val vclangPackage = "com.jetbrains.jetpad.vclang"

tasks.withType<Jar> {
    manifest.attributes["Main-Class"] = "$vclangPackage.frontend.ConsoleMain"
    from(configurations.runtimeClasspath.map { if (it.isDirectory) it else zipTree(it) })
    from(java.sourceSets["main"].output)
    dependsOn("prelude")
}

val genSrcDir = file("src/gen")

java.sourceSets {
    getByName("main").java.srcDirs(genSrcDir)
}

idea {
    module {
        generatedSourceDirs.add(genSrcDir)
        outputDir = file("$buildDir/classes/main")
        testOutputDir = file("$buildDir/classes/test")
    }
}

tasks.withType<AntlrTask> {
    outputDirectory = genSrcDir
    arguments.addAll(listOf(
            "-package", "$vclangPackage.frontend.parser",
            "-no-listener",
            "-visitor"
    ))
}

protobuf.protobuf.run {
    generatedFilesBaseDir = genSrcDir.toString()
    protoc(closureOf<ExecutableLocator> {
        artifact = "com.google.protobuf:protoc:3.1.0"
    })
}

tasks.withType<Wrapper> {
    gradleVersion = "4.9"
}


// Prelude stuff

val preludeOutputDir = "$buildDir/classes/main"

task<Copy>("copyPrelude") {
    from("lib/Prelude.vc")
    into("$preludeOutputDir/lib")
}

task<JavaExec>("prelude") {
    description = "Builds the prelude cache"
    group = "Build"
    main = "$vclangPackage.prelude.PreludeBinaryGenerator"
    classpath = java.sourceSets["main"].runtimeClasspath
    args = listOf(preludeOutputDir)
    dependsOn("copyPrelude")
}

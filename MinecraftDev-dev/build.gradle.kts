/*
 * Minecraft Dev for IntelliJ
 *
 * https://minecraftdev.org
 *
 * Copyright (c) 2017 minecraft-dev
 *
 * MIT License
 */

import org.gradle.api.tasks.bundling.Jar
import org.gradle.internal.jvm.Jvm
import org.jetbrains.intellij.tasks.PublishTask
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.io.File

buildscript {
    repositories {
        maven {
            name = "intellij-plugin-service"
            setUrl("https://dl.bintray.com/jetbrains/intellij-plugin-service")
        }
    }
}

plugins {
    id("org.jetbrains.kotlin.jvm") version "1.1.3-2" // kept in sync with IntelliJ's bundled dep
    groovy
    idea
    id("org.jetbrains.intellij") version "0.2.15"
    id("net.minecrell.licenser") version "0.3"
}

defaultTasks("build")

val CI = System.getenv("CI") != null

val ideaVersion: String by extra
val javaVersion: String by extra
val kotlinVersion: String by extra
val downloadIdeaSources: String by extra

// for publishing nightlies
val repoUsername: String by extra
val repoPassword: String by extra
val repoChannel: String by extra

val compileKotlin by tasks
val processResources: AbstractCopyTask by tasks
val test: Test by tasks
val runIde: JavaExec by tasks
val publishPlugin: PublishTask by tasks
val clean: Task by tasks

configurations {
    "kotlin"()
    "compileOnly" { extendsFrom("kotlin"()) }
    "testCompile" { extendsFrom("kotlin"()) }

    "gradle-tooling-extension" { extendsFrom("idea"()) }
    "jflex"()
    "jflex-skeleton"()
    "grammar-kit"()
    "testLibs" { isTransitive = false }
}

repositories {
    mavenCentral()
    maven {
        name = "minecraft-dev"
        setUrl("https://dl.bintray.com/minecraft-dev/maven")
    }
    maven {
        name = "sponge"
        setUrl("https://repo.spongepowered.org/maven")
    }
}

java {
    setSourceCompatibility(javaVersion)
    setTargetCompatibility(javaVersion)

    sourceSets {
        "gradle-tooling-extension" {
            configurations[compileOnlyConfigurationName].extendsFrom(configurations["gradle-tooling-extension"])
        }
    }
}

val gradleToolingExtension = java.sourceSets["gradle-tooling-extension"]!!
val gradleToolingExtensionJar = task<Jar>(gradleToolingExtension.jarTaskName) {
    from(gradleToolingExtension.output)
    classifier = "gradle-tooling-extension"
}

dependencies {
    "kotlin"(kotlinModule("stdlib")) { isTransitive = false }
    compile(kotlinModule("stdlib-jre7")) { isTransitive = false }
    compile(kotlinModule("stdlib-jre8")) { isTransitive = false }

    // Add tools.jar for the JDI API
    compile(files(Jvm.current().toolsJar))

    compile(files(gradleToolingExtensionJar))
    "gradle-tooling-extension"(intellijPlugin("gradle"))

    "jflex"("org.jetbrains.idea:jflex:1.7.0-b7f882a")
    "jflex-skeleton"("org.jetbrains.idea:jflex:1.7.0-c1fdf11:idea@skeleton")
    "grammar-kit"("org.jetbrains.idea:grammar-kit:1.5.1")

    "testLibs"("org.jetbrains.idea:mockJDK:1.7-4d76c50")
    "testLibs"("org.spongepowered:mixin:0.7-SNAPSHOT:thin")
}

intellij {
    // IntelliJ IDEA dependency
    version = ideaVersion
    // Bundled plugin dependencies
    setPlugins("maven", "gradle", "Groovy",
        // needed dependencies for unit tests
        "properties", "junit")

    pluginName = "Minecraft Development"
    updateSinceUntilBuild = false

    downloadSources = !CI && downloadIdeaSources.toBoolean()

    sandboxDirectory = project.rootDir.canonicalPath + "/.sandbox"
}

publishPlugin {
    if (properties["publish"] != null) {
        project.version = "${project.version}-${properties["buildNumber"]}"

        username(repoUsername)
        password(repoPassword)
        channels(repoChannel)
    }
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = javaVersion
}

processResources {
    for (lang in arrayOf("", "_en")) {
        from("src/main/resources/messages.MinecraftDevelopment_en_US.properties") {
            rename { "messages.MinecraftDevelopment$lang.properties" }
        }
    }
}

test {
    if (CI) {
        systemProperty("slowCI", "true")
    }

    dependsOn(configurations["testLibs"])
    doFirst {
        configurations["testLibs"].resolvedConfiguration.resolvedArtifacts.forEach {
            systemProperty("testLibs.${it.name}", it.file.absolutePath)
        }
    }
}

idea {
    module {
        generatedSourceDirs.add(file("gen"))
        excludeDirs.add(file(intellij.sandboxDirectory))
    }
}

// License header formatting
license {
    header = file("copyright.txt")
    include("**/*.java", "**/*.kt", "**/*.groovy", "**/*.gradle", "**/*.xml", "**/*.properties", "**/*.html")
    exclude("com/demonwav/mcdev/platform/mcp/at/gen/**", "com/demonwav/mcdev/nbt/lang/gen/**")
}

// Credit for this intellij-rust
// https://github.com/intellij-rust/intellij-rust/blob/d6b82e6aa2f64b877a95afdd86ec7b84394678c3/build.gradle#L131-L181
fun generateLexer(name: String, flex: String, pack: String) = task<JavaExec>(name) {
    val src = "src/main/grammars/$flex.flex"
    val dst = "gen/com/demonwav/mcdev/$pack"
    val output = "$dst/$flex.java"

    classpath = configurations["jflex"]
    main = "jflex.Main"

    doFirst {
        args(
            "--skel", configurations["jflex-skeleton"].singleFile.absolutePath,
            "-d", dst,
            src
        )

        // Delete current lexer
        delete(output)
    }

    inputs.files(src, configurations["jflex-skeleton"])
    outputs.file(output)
}

fun generatePsiAndParser(name: String, bnf: String, pack: String) = task<JavaExec>(name) {
    val src = "src/main/grammars/$bnf.bnf".replace("/", File.separator)
    val dstRoot = "gen"
    val dst = "$dstRoot/com/demonwav/mcdev/$pack".replace("/", File.separator)
    val psiDir = "$dst/psi/".replace("/", File.separator)
    val parserDir = "$dst/parser/".replace("/", File.separator)

    doFirst {
        delete(psiDir, parserDir)
    }

    classpath = configurations["grammar-kit"]
    main = "org.intellij.grammar.Main"

    args(dstRoot, src)

    inputs.file(src)
    outputs.dirs(mapOf(
        "psi" to psiDir,
        "parser" to parserDir
    ))
}

val generateAtLexer = generateLexer("generateAtLexer", "AtLexer", "platform/mcp/at/gen/")
val generateAtPsiAndParser = generatePsiAndParser("generateAtPsiAndParser", "AtParser", "platform/mcp/at/gen")

val generateNbttLexer = generateLexer("generateNbttLexer", "NbttLexer", "nbt/lang/gen/")
val generateNbttPsiAndParser = generatePsiAndParser("generateNbttPsiAndParser", "NbttParser", "nbt/lang/gen")

val generate = task("generate") {
    group = "minecraft"
    description = "Generates sources needed to compile the plugin."
    dependsOn(generateAtLexer, generateAtPsiAndParser, generateNbttLexer, generateNbttPsiAndParser)
    outputs.dir("gen")
}

java.sourceSets[SourceSet.MAIN_SOURCE_SET_NAME].java.srcDir(generate)

// Workaround for KT-16764
compileKotlin.inputs.dir(generate)

// Workaround problems caused by separate output directories for classes in Gradle 4.0
// gradle-intellij-plugin needs to be updated to support them properly
java.sourceSets.all {
    output.classesDir = File(buildDir, "classes/$name")
}

runIde {
    maxHeapSize = "2G"

    (findProperty("intellijJre") as? String)?.let(this::setExecutable)

    System.getProperty("debug")?.let {
        systemProperty("idea.ProcessCanceledException", "disabled")
        systemProperty("idea.debug.mode", "true")
    }
}

val cleanGen = task<Delete>("cleanGen") {
    delete = setOf(file("gen"))
}

clean.dependsOn(cleanGen)

inline operator fun <T : Task> T.invoke(a: T.() -> Unit): T = apply(a)
fun DependencyHandlerScope.kotlinModule(module: String) = kotlinModule(module, kotlinVersion) as String
fun intellijPlugin(name: String) = mapOf(
    "group" to "org.jetbrains.plugins",
    "name" to name,
    "version" to ideaVersion,
    "configuration" to "compile"
)

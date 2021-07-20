import net.md_5.specialsource.JarMapping
import net.md_5.specialsource.JarRemapper
import net.md_5.specialsource.provider.JarProvider
import net.md_5.specialsource.provider.JointProvider
import net.md_5.specialsource.Jar as SpecialJar

plugins {
    kotlin("jvm") version "1.5.21"
    id("org.jetbrains.dokka") version "1.5.0"
    `maven-publish`
    signing
}

group = "io.github.teamcheeze"
version = "0.0.1"

buildscript {
    repositories {
        mavenCentral()
    }

    dependencies {
        classpath("net.md-5:SpecialSource:1.10.0")
    }
}

subprojects {
    apply(plugin = "org.jetbrains.kotlin.jvm")
    repositories {
        mavenLocal()
        mavenCentral()
        maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
        maven("https://libraries.minecraft.net/")
    }
    dependencies {
        compileOnly("io.github.teamcheeze:jaw:1.0.1")
        compileOnly("io.github.teamcheeze:plum:0.0.3")
        compileOnly("org.spigotmc:spigot-api:1.17.1-R0.1-SNAPSHOT")
        compileOnly("com.mojang:authlib:1.5.21")
    }
    if (this != project(":common")) {
        dependencies {
            compileOnly(project(":common"))
        }
    }
}
val excludeSource = arrayOf(
    ":plugin"
)
val nms = arrayOf(
    ":1_17_R1"
)
val hasDoc = arrayOf(
    ":common"
)
nms.forEach {
    // by monun https://github.com/monun/tap/blob/master/tap-core/build.gradle.kts
    project(it).configurations {
        create("mojangMapping")
        create("spigotMapping")
    }
    project(it).tasks {
        jar {
            doLast {
                fun remap(jarFile: File, outputFile: File, mappingFile: File, reversed: Boolean = false) {
                    val inputJar = SpecialJar.init(jarFile)
                    val mapping = JarMapping()
                    mapping.loadMappings(mappingFile.canonicalPath, reversed, false, null, null)
                    val provider = JointProvider()
                    provider.add(JarProvider(inputJar))
                    mapping.setFallbackInheritanceProvider(provider)
                    val mapper = JarRemapper(mapping)
                    mapper.remapJar(inputJar, outputFile)
                    inputJar.close()
                }

                val archiveFile = archiveFile.get().asFile
                val obfOutput = File(archiveFile.parentFile, "remapped-obf.jar")
                val spigotOutput = File(archiveFile.parentFile, "remapped-spigot.jar")
                val configurations = project.configurations
                val mojangMapping = configurations.named("mojangMapping").get().firstOrNull()
                val spigotMapping = configurations.named("spigotMapping").get().firstOrNull()

                if (mojangMapping != null && spigotMapping != null) {
                    remap(archiveFile, obfOutput, mojangMapping, true)
                    remap(obfOutput, spigotOutput, spigotMapping)

                    spigotOutput.copyTo(archiveFile, true)
                    obfOutput.delete()
                    spigotOutput.delete()
                } else {
                    throw IllegalStateException("Mojang and Spigot mapping should be specified for ${project.path}")
                }
            }
        }
    }
}

repositories {
    mavenCentral()
    mavenLocal()
}

dependencies {
    implementation("io.github.teamcheeze:jaw:1.0.1")
    implementation("io.github.teamcheeze:plum:0.0.3")
    implementation(project(":common"))
    implementation(project(":plugin"))
}

tasks {
    register<Jar>("javadocJar") {
        archiveClassifier.set("javadoc")
        hasDoc.forEach {
            dependsOn((project(it).tasks["dokkaHtml"]))
            from("${project(it).buildDir}/dokka/html")
            include("**")
        }
    }
    register<Jar>("sourcesJar") {
        archiveClassifier.set("sources")
        subprojects.filter { !nms.contains(":" + it.name) && !excludeSource.contains(":" + it.name) }.forEach {
            from(it.sourceSets["main"].allSource)
            from(it.sourceSets["main"].output)
        }
        nms.forEach { nmsProjectName ->
            val jarTask = project(nmsProjectName).tasks.jar.get()
            dependsOn(jarTask)
            from(zipTree(jarTask.archiveFile))
        }
    }
    register<Jar>("common") {
        archiveClassifier.set("common")
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
        subprojects.filter { !nms.contains(":" + it.name) && !excludeSource.contains(":" + it.name) }.forEach {
            from(it.sourceSets["main"].allSource)
            from(it.sourceSets["main"].output)
        }
    }

    jar {
        from((project.tasks["sourcesJar"] as Jar).source)
    }

    register<Jar>("bukkit") {
        dependsOn(project.tasks.build)
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
        // Shade all dependencies
        from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
        nms.forEach {
            val a = project(it).tasks.jar.get()
            dependsOn(a)
            from(zipTree(a.archiveFile))
        }
    }
}

publishing {
    publications {
        create<MavenPublication>("mavenPublication") {
            from(components["java"])
            artifact(tasks["sourcesJar"])
            artifact(tasks["javadocJar"])
            artifact(tasks["common"])
            repositories {
                mavenLocal()
                maven {
                    name = "sonatype"
                    credentials.runCatching {
                        val nexusUsername: String by project
                        val nexusPassword: String by project
                        username = nexusUsername
                        password = nexusPassword
                    }.onFailure {
                        logger.warn("Failed to load nexus credentials, Check the gradle.properties")
                    }
                    url = uri(
                        if (version.endsWith("-SNAPSHOT") || version.endsWith(".Beta") || version.endsWith(".beta")) {
                            "https://s01.oss.sonatype.org/content/repositories/snapshots/"
                        } else {
                            "https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/"
                        }
                    )
                }
            }
            pom {
                name.set("jaw")
                description.set("Welcome to the Jaw library!")
                url.set("https://github.com/TeamCheeeze/jaw")
                licenses {
                    license {
                        name.set("The MIT License")
                        url.set("http://www.opensource.org/licenses/mit-license.php")
                    }
                }
                developers {
                    developer {
                        id.set("dolphin2410")
                        name.set("dolphin2410")
                        email.set("teamcheeze@outlook.kr")
                        timezone.set("GMT+9")
                    }
                }
                scm {
                    connection.set("scm:git:git://github.com/TeamCheeze/jaw.git")
                    developerConnection.set("scm:git:ssh://github.com/TeamCheeze/jaw.git")
                    url.set("https://github.com/TeamCheeze/jaw")
                }
            }
        }
    }
}

signing {
    isRequired = true
    sign(tasks["sourcesJar"], tasks["javadocJar"], tasks["common"])
    sign(publishing.publications["mavenPublication"])
}
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import dev.architectury.plugin.ArchitectPluginExtension
import me.shedaniel.unifiedpublishing.UnifiedPublishingExtension
import net.fabricmc.loom.api.LoomGradleExtensionAPI
import net.fabricmc.loom.task.RemapJarTask

plugins {
  java
  `maven-publish`
  kotlin("jvm") version "1.8.10"
  id("architectury-plugin") version "3.4+"
  id("dev.architectury.loom") version "1.2+" apply false
  id("com.github.johnrengelman.shadow") version "7.1.2" apply false
  id("me.shedaniel.unified-publishing") version "0.1.+" apply false
  id("com.github.jmongard.git-semver-plugin") version "0.4+"
  id("org.jlleitschuh.gradle.ktlint") version "11.3+"
}

val javaVersion: String = property("java_version").toString()
val mavenGroup: String = property("maven_group").toString()
val minecraftVersion: String = property("minecraft_version").toString()
val forgeVersion: String = property("forge_version").toString()
val kotlinVersion: String = property("kotlin_version").toString()
val kotlinFabricVersion: String = property("kotlin_fabric_version").toString()
val kotlinForgeVersion: String = property("kotlin_forge_version").toString()
val architecturyVersion: String = property("architectury_version").toString()
val modId: String = property("archives_base_name").toString()
val platforms by extra {
  property("enabled_platforms").toString().split(',')
}

semver {
  releaseTagNameFormat = "$minecraftVersion-%s"
  minorPattern = "\\Afeat(ure)?(?:\\(\\w+\\))?:"
}

val buildVersion = semver.version

dependencies {
  implementation(kotlin("stdlib", kotlinVersion))
  implementation(kotlin("reflect", kotlinVersion))
}

subprojects {
  apply(plugin = "java")
  apply(plugin = "org.jetbrains.kotlin.jvm")
  apply(plugin = "maven-publish")
  apply(plugin = "architectury-plugin")
  apply(plugin = "dev.architectury.loom")
  apply(plugin = "org.jlleitschuh.gradle.ktlint")

  version = buildVersion
  group = mavenGroup

  java {
    withSourcesJar()
  }

  kotlin {
    jvmToolchain(javaVersion.toInt())
  }

  configure<ArchitectPluginExtension> {
    minecraft = minecraftVersion
  }

  configure<LoomGradleExtensionAPI> {
    silentMojangMappingsLicense()

    val accessWidenerFile = project(":common").file("src/main/resources/$modId.accesswidener")

    if (accessWidenerFile.exists()) {
      accessWidenerPath.set(accessWidenerFile)
    }
  }

  configure<PublishingExtension> {
    repositories {
      maven {
        name = "impleri-dev"
        url = uri("https://maven.impleri.org/minecraft")
        credentials {
          username = System.getenv("MAVEN_USER")
          password = System.getenv("MAVEN_TOKEN")
        }
        authentication {
          create<BasicAuthentication>("basic")
        }
      }
    }
  }

  repositories {
    maven("https://maven.impleri.org/minecraft")
    maven {
      url = uri("https://maven.architectury.dev")
      content {
        includeGroup("dev.architectury")
      }
    }
    maven("https://thedarkcolour.github.io/KotlinForForge/")
    maven("https://maven.blamejared.com")
    maven {
      url = uri("https://maven.saps.dev/minecraft")
      content {
        includeGroup("dev.latvian.mods")
        includeGroup("dev.ftb.mods")
      }
    }
    maven("https://maven.shedaniel.me")
    maven {
      // location of the maven that hosts JEI files
      name = "Progwml6 maven"
      url = uri("https://dvs1.progwml6.com/files/maven/")
    }
    maven {
      // location of a maven mirror for JEI files, as a fallback
      name = "ModMaven"
      url = uri("https://modmaven.dev")
    }

    mavenCentral()
  }

  dependencies {
    "minecraft"("com.mojang:minecraft:$minecraftVersion")
    "mappings"(project.extensions.getByName<LoomGradleExtensionAPI>("loom").officialMojangMappings())
    compileOnly("me.shedaniel:REIPluginCompatibilities-forge-annotations:9+")
  }

  tasks {
    withType<JavaCompile> {
      options.encoding = "UTF-8"
      options.release.set(javaVersion.toInt())
    }
  }
}

project(":common") {
  configure<ArchitectPluginExtension> {
    common(platforms)
  }
  project.base.archivesName.set(modId)

  configure<PublishingExtension> {
    publications {
      create<MavenPublication>("maven${project.name.replaceFirstChar { it.uppercaseChar() }}") {
        artifactId = modId
        groupId = project.group.toString()
        version = "$minecraftVersion-${project.version}"

        from(components["java"])
      }
    }
  }
}

for (platform in platforms) {
  project(":$platform") {
    apply(plugin = "com.github.johnrengelman.shadow")
    apply(plugin = "me.shedaniel.unified-publishing")

    project.base.archivesName.set("$modId-${project.name}")

    configure<ArchitectPluginExtension> {
      platformSetupLoomIde()
      loader(platform)
    }

    configure<PublishingExtension> {
      publications {
        create<MavenPublication>("maven${project.name.replaceFirstChar { it.uppercaseChar() }}") {
          artifactId = modId
          groupId = project.group.toString()
          version = "$minecraftVersion-${project.name}-${project.version}"

          from(components["java"])
        }
      }
    }

    configure<UnifiedPublishingExtension> {
      project {
        displayName.set("[${project.name.replaceFirstChar { it.uppercaseChar() }} $minecraftVersion] v$buildVersion")
        gameVersions.add(minecraftVersion)
        gameLoaders.add(project.name)

        mainPublication(project.tasks.getByName("remapJar"))

        relations {
          depends {
            curseforge.set("architectury-api")
            modrinth.set("architectury-api")
          }

          optional {
            curseforge.set("roughly-enough-items")
            modrinth.set("rei")
          }

          optional {
            curseforge.set("jei")
            modrinth.set("jei")
          }

          optional {
            curseforge.set("crafttweaker")
            modrinth.set("crafttweaker")
          }

          optional {
            curseforge.set("kubejs")
            modrinth.set("kubejs")
          }
        }

        val curseId = System.getenv("CURSEFORGE_ID")
        val curseToken = System.getenv("CURSEFORGE_TOKEN")
        if (curseToken != null) {
          curseforge {
            token.set(curseToken)
            id.set(curseId)
          }
        }

        val modrinthId = System.getenv("MODRINTH_ID")
        val modrinthToken = System.getenv("MODRINTH_TOKEN")
        if (modrinthToken != null) {
          modrinth {
            token.set(modrinthToken)
            id.set(modrinthId)
          }
        }
      }
    }

    val common: Configuration by configurations.creating
    val shadowCommon: Configuration by configurations.creating

    configurations {
      compileClasspath.get().extendsFrom(common)
      runtimeClasspath.get().extendsFrom(common)
      getByName("development${platform.replaceFirstChar { it.uppercaseChar() }}").extendsFrom(common)
    }

    dependencies {
      common(project(path = ":common", configuration = "namedElements")) { isTransitive = false }
      shadowCommon(
        project(
          path = ":common",
          configuration = "transformProduction${platform.replaceFirstChar { it.uppercaseChar() }}",
        ),
      ) { isTransitive = false }
    }

    tasks {
      processResources {
        val commonProps = mapOf(
          "modId" to modId,
          "version" to project.version,
          "kotlinVersion" to kotlinVersion,
          "minecraftVersion" to minecraftVersion,
          "forgeVersion" to forgeVersion,
          "kotlinForgeVersion" to kotlinForgeVersion,
          "kotlinFabricVersion" to kotlinFabricVersion,
          "architecturyVersion" to architecturyVersion,
        ) + project.properties
        filesMatching("fabric.mod.json") {
          expand(commonProps)
        }
        filesMatching("META-INF/mods.toml") {
          expand(commonProps)
        }
      }

      withType<ShadowJar> {
        exclude("architectury.common.json")
        configurations = listOf(shadowCommon)
        archiveClassifier.set("dev-shadow")
      }

      withType<RemapJarTask> {
        val shadowJar: ShadowJar by project.tasks
        inputFile.set(shadowJar.archiveFile)
        dependsOn(shadowJar)
        archiveClassifier.set(null as String?)
        injectAccessWidener.set(true)
      }

      jar {
        archiveClassifier.set("dev")
      }

      getByName<Jar>("sourcesJar") {
        val commonSources = project(":common").tasks.getByName<Jar>("sourcesJar")
        dependsOn(commonSources)
        from(commonSources.archiveFile.map { zipTree(it) })
      }
    }

    val javaComponent = components["java"] as AdhocComponentWithVariants
    javaComponent.withVariantsFromConfiguration(configurations["shadowRuntimeElements"]) {
      skip()
    }
  }
}

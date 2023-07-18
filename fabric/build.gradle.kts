import me.shedaniel.unifiedpublishing.UnifiedPublishingExtension

val modId: String = property("archives_base_name").toString()
val minecraftVersion: String = property("minecraft_version").toString()
val kotlinVersion: String = property("kotlin_version").toString()
val kotlinFabricVersion: String = property("kotlin_fabric_version").toString()
val architecturyVersion: String = property("architectury_version").toString()
val fabricLoaderVersion: String = property("fabric_loader_version").toString()
val fabricApiVersion: String = property("fabric_api_version").toString()
val kubejsVersion: String = property("kubejs_version").toString()
val craftTweakerVersion: String = property("crafttweaker_version").toString()
val ftbQuestsVersion: String = property("ftb_quests_version").toString()
val ftbTeamsVersion: String = property("ftb_teams_version").toString()
val reiVersion: String = property("rei_version").toString()
val jeiVersion: String = property("jei_version").toString()
val trinketsVersion: String = property("trinkets_version").toString()

configure<UnifiedPublishingExtension> {
  project {
    relations {
      depends {
        curseforge.set("fabric-api")
        modrinth.set("fabric-api")
      }

      optional {
        curseforge.set("ftb-quests-fabric")
      }

      optional {
        curseforge.set("ftb-teams-fabric")
      }

      optional {
        curseforge.set("trinkets")
        modrinth.set("trinkets")
      }
    }
  }
}

repositories {
  maven {
    name = "TerraformersMC"
    url = uri("https://maven.terraformersmc.com/")
  }
  maven {
    name = "Ladysnake Libs"
    url = uri("https://ladysnake.jfrog.io/artifactory/mods")
  }
}

dependencies {
  modImplementation("net.fabricmc:fabric-language-kotlin:$kotlinFabricVersion+kotlin.$kotlinVersion")

  modImplementation("net.fabricmc:fabric-loader:$fabricLoaderVersion")
  modApi("net.fabricmc.fabric-api:fabric-api:$fabricApiVersion")

  modApi("dev.architectury:architectury-fabric:$architecturyVersion")

  modImplementation("dev.latvian.mods:kubejs-fabric:$kubejsVersion")
  modApi("com.blamejared.crafttweaker:CraftTweaker-fabric-$minecraftVersion:$craftTweakerVersion")

  modImplementation("dev.ftb.mods:ftb-quests-fabric:$ftbQuestsVersion")
  modImplementation("dev.ftb.mods:ftb-teams-fabric:$ftbTeamsVersion")

  modCompileOnly("me.shedaniel:RoughlyEnoughItems-api-fabric:$reiVersion")
  modRuntimeOnly("me.shedaniel:RoughlyEnoughItems-fabric:$reiVersion")
  modCompileOnly("me.shedaniel:RoughlyEnoughItems-default-plugin-fabric:$reiVersion")

  modCompileOnly("mezz.jei:jei-$minecraftVersion-common-api:$jeiVersion")
  modCompileOnly("mezz.jei:jei-$minecraftVersion-fabric-api:$jeiVersion")
  modRuntimeOnly("mezz.jei:jei-$minecraftVersion-fabric:$jeiVersion")

  // TODO: Temorarily add this dependency until we can update to 1.20 which has the fix to bundle it correctly for trinkets
  modImplementation("dev.onyxstudios.cardinal-components-api:cardinal-components-base:5.0.0-beta.1")
  modApi("dev.emi:trinkets:$trinketsVersion")
}

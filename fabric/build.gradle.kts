import me.shedaniel.unifiedpublishing.UnifiedPublishingExtension

val modId: String = property("archives_base_name").toString()
val minecraftVersion: String = property("minecraft_version").toString()
val kotlinVersion: String = property("kotlin_version").toString()
val kotlinFabricVersion: String = property("kotlin_fabric_version").toString()
val architecturyVersion: String = property("architectury_version").toString()
val fabricLoaderVersion: String = property("fabric_loader_version").toString()
val fabricApiVersion: String = property("fabric_api_version").toString()
val kubejsVersion: String = property("kubejs_version").toString()
val ftbQuestsVersion: String = property("ftb_quests_version").toString()
val ftbTeamsVersion: String = property("ftb_teams_version").toString()

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
    }
  }
}

dependencies {
  modImplementation("net.fabricmc:fabric-loader:$fabricLoaderVersion")
  modApi("net.fabricmc.fabric-api:fabric-api:$fabricApiVersion")

  modImplementation("net.fabricmc:fabric-language-kotlin:$kotlinFabricVersion+kotlin.$kotlinVersion")

  modApi("dev.architectury:architectury-fabric:$architecturyVersion")
  modApi("dev.latvian.mods:kubejs-fabric:$kubejsVersion")

  modImplementation("dev.ftb.mods:ftb-quests-fabric:$ftbQuestsVersion")
  modImplementation("dev.ftb.mods:ftb-teams-fabric:$ftbTeamsVersion")
}

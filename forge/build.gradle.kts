import me.shedaniel.unifiedpublishing.UnifiedPublishingExtension

val modId: String = property("archives_base_name").toString()
val kotlinVersion: String = property("kotlin_version").toString()
val minecraftVersion: String = property("minecraft_version").toString()
val forgeVersion: String = property("forge_version").toString()
val kotlinForgeVersion: String = property("kotlin_forge_version").toString()
val architecturyVersion: String = property("architectury_version").toString()
val kubejsVersion: String = property("kubejs_version").toString()
val craftTweakerVersion: String = property("crafttweaker_version").toString()
val ftbQuestsVersion: String = property("ftb_quests_version").toString()
val ftbTeamsVersion: String = property("ftb_teams_version").toString()
val reiVersion: String = property("rei_version").toString()
val jeiVersion: String = property("jei_version").toString()
val curiosVersion: String = property("curios_version").toString()

configure<UnifiedPublishingExtension> {
  project {
    relations {
      optional {
        curseforge.set("ftb-quests")
      }

      optional {
        curseforge.set("ftb-teams")
      }

      optional {
        curseforge.set("curios")
        modrinth.set("curios")
      }
    }
  }
}

dependencies {
  implementation("thedarkcolour:kotlinforforge:$kotlinForgeVersion")
  forgeRuntimeLibrary(kotlin("stdlib", kotlinVersion))
  forgeRuntimeLibrary(kotlin("reflect", kotlinVersion))

  forge("net.minecraftforge:forge:$forgeVersion")
  modImplementation("dev.architectury:architectury-forge:$architecturyVersion")

  modImplementation("dev.latvian.mods:kubejs-forge:$kubejsVersion")
  modApi("com.blamejared.crafttweaker:CraftTweaker-forge-$minecraftVersion:$craftTweakerVersion")

  modImplementation("dev.ftb.mods:ftb-quests-forge:$ftbQuestsVersion")
  modImplementation("dev.ftb.mods:ftb-teams-forge:$ftbTeamsVersion")

  compileOnly("me.shedaniel:RoughlyEnoughItems-api-forge:$reiVersion")
  compileOnly("me.shedaniel:RoughlyEnoughItems-default-plugin-forge:$reiVersion")
  modImplementation("me.shedaniel:RoughlyEnoughItems-forge:$reiVersion")

  compileOnly("mezz.jei:jei-$minecraftVersion-common-api:$jeiVersion")
  compileOnly("mezz.jei:jei-$minecraftVersion-forge-api:$jeiVersion")
  modImplementation("mezz.jei:jei-$minecraftVersion-forge:$jeiVersion")

  compileOnly("top.theillusivec4.curios:curios-forge:$curiosVersion:api")
  modRuntimeOnly("top.theillusivec4.curios:curios-forge:$curiosVersion")
}

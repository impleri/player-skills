val modId: String = property("archives_base_name").toString()
val kotlinVersion: String = property("kotlin_version").toString()
val minecraftVersion: String = property("minecraft_version").toString()
val fabricLoaderVersion: String = property("fabric_loader_version").toString()
val architecturyVersion: String = property("architectury_version").toString()
val kubejsVersion: String = property("kubejs_version").toString()
val craftTweakerVersion: String = property("crafttweaker_version").toString()
val ftbQuestsVersion: String = property("ftb_quests_version").toString()
val ftbTeamsVersion: String = property("ftb_teams_version").toString()
val reiVersion: String = property("rei_version").toString()
val jeiVersion: String = property("jei_version").toString()

dependencies {
  implementation(kotlin("stdlib", kotlinVersion))
  implementation(kotlin("reflect", kotlinVersion))

  modImplementation("net.fabricmc:fabric-loader:$fabricLoaderVersion")
  modApi("dev.architectury:architectury:$architecturyVersion")

  modCompileOnly("dev.latvian.mods:kubejs:$kubejsVersion")
  modCompileOnly("com.blamejared.crafttweaker:CraftTweaker-common-$minecraftVersion:$craftTweakerVersion")

  modCompileOnly("dev.ftb.mods:ftb-teams:$ftbTeamsVersion")
  modCompileOnly("dev.ftb.mods:ftb-quests:$ftbQuestsVersion")

  modCompileOnly("me.shedaniel:RoughlyEnoughItems-api:$reiVersion")
  modCompileOnly("me.shedaniel:RoughlyEnoughItems-default-plugin:$reiVersion")

  modCompileOnly("mezz.jei:jei-$minecraftVersion-common-api:$jeiVersion")
}

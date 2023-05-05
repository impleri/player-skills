val modId: String = property("archives_base_name").toString()
val kotlinVersion: String = property("kotlin_version").toString()
val fabricLoaderVersion: String = property("fabric_loader_version").toString()
val architecturyVersion: String = property("architectury_version").toString()
val kubejsVersion: String = property("kubejs_version").toString()
val ftbQuestsVersion: String = property("ftb_quests_version").toString()
val ftbTeamsVersion: String = property("ftb_teams_version").toString()

dependencies {
  implementation(kotlin("stdlib", kotlinVersion))
  implementation(kotlin("reflect", kotlinVersion))

  modImplementation("net.fabricmc:fabric-loader:$fabricLoaderVersion")
  modApi("dev.architectury:architectury:$architecturyVersion")
  modImplementation("dev.latvian.mods:kubejs:$kubejsVersion")

  modImplementation("dev.ftb.mods:ftb-teams:$ftbTeamsVersion")
  modImplementation("dev.ftb.mods:ftb-quests:$ftbQuestsVersion")
}

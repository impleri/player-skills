package net.impleri.playerskills.restrictions.conditions

trait BiomeConditions {
  var includeBiomes: Seq[String] = Seq.empty
  var excludeBiomes: Seq[String] = Seq.empty

  def inBiome(biome: String): Unit = {
    includeBiomes = includeBiomes :+ biome
  }

  def notInBiome(biome: String): Unit = {
    excludeBiomes = excludeBiomes :+ biome
  }
}

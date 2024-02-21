package net.impleri.playerskills.facades.minecraft.world

import net.impleri.playerskills.facades.minecraft.core.ResourceLocation
import net.minecraft.core.Holder
import net.minecraft.tags.TagKey
import net.minecraft.world.level.biome.{Biome => McBiome}

import scala.jdk.OptionConverters._

case class Biome(private val holder: Holder[McBiome]) {
  val name: Option[ResourceLocation] = holder.unwrapKey()
    .toScala
    .map(_.location())
    .map(ResourceLocation(_))

  def isTagged(tag: TagKey[McBiome]): Boolean = holder.is(tag)

  def isNamed(n: ResourceLocation): Boolean = name.contains(n)

  def isNamespaced(ns: String): Boolean = name.exists(_.getNamespace == ns)
}

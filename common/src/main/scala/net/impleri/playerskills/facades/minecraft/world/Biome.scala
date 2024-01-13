package net.impleri.playerskills.facades.minecraft.world

import net.minecraft.core.Holder
import net.minecraft.resources.ResourceLocation
import net.minecraft.tags.TagKey
import net.minecraft.world.level.biome.{Biome => McBiome}

import scala.jdk.javaapi.OptionConverters

case class Biome(private val holder: Holder[McBiome]) {
  val name: Option[ResourceLocation] = OptionConverters.toScala(holder.unwrapKey()).map(_.location())

  def isTagged(tag: TagKey[McBiome]): Boolean = holder.is(tag)

  def isNamed(n: ResourceLocation): Boolean = name.contains(n)

  def isNamespaced(ns: String): Boolean = name.exists(_.getNamespace == ns)
}

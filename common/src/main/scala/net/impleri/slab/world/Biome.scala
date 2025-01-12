package net.impleri.slab.world

import net.impleri.slab.registry.Tag
import net.impleri.slab.resources.{ResourceLocation, ResourceWrapper}
import net.minecraft.core.Holder
import net.minecraft.world.level.biome.{Biome => McBiome}

import scala.jdk.OptionConverters._

case class Biome(private val holder: Holder[Biome.Vanilla]) extends ResourceWrapper[Biome.Vanilla] {
  override val name: Option[ResourceLocation] =
    holder
      .unwrapKey()
      .toScala
      .map(_.location())
      .flatMap(ResourceLocation(_))

  override val underlying: Biome.Vanilla = holder.value()

  override def toString: String = name.fold("None")(_.toString)

  private[slab]  def toHolder: Holder[Biome.Vanilla] = holder
  
  def isTagged(tag: Tag[Biome, Biome.Vanilla]): Boolean = holder.is(tag.value)

  def isNamed(n: ResourceLocation): Boolean = name.contains(n)

  def isNamespaced(ns: String): Boolean = name.exists(_.namespace == ns)
}

object Biome {
  type Vanilla = McBiome
}

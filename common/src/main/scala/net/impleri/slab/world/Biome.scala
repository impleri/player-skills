package net.impleri.slab.world

import net.impleri.slab.registry.{BuiltinRegistry, Tag}
import net.impleri.slab.resources.{ResourceKey, ResourceLocation, ResourceWrapper}
import net.minecraft.core.Holder
import net.minecraft.world.level.biome.{FixedBiomeSource, Biome => McBiome}

import scala.jdk.OptionConverters._

case class Biome(private val holder: Holder[Biome.Vanilla]) extends ResourceWrapper[Biome.Vanilla] {
  override protected val underlying: Biome.Vanilla = holder.value()

  override val name: Option[ResourceLocation] =
    for {
      key <- holder.unwrapKey().toScala
      location = key.location()
      name <- ResourceLocation(location)
    } yield name

  override def toString: String = name.fold("None")(_.toString)

  private[slab] def asSource = new FixedBiomeSource(holder)

  def isTagged(tag: Tag[Biome, Biome.Vanilla]): Boolean = holder.is(tag.value)

  def isNamed(n: ResourceLocation): Boolean = name.contains(n)

  def isNamespaced(ns: String): Boolean = name.exists(_.namespace == ns)
}

object Biome {
  type Vanilla = McBiome

  def apply(name: ResourceLocation): Biome = {
    val resourceKey: ResourceKey[Biome.Vanilla] = ResourceKey.forResource(name, ResourceKey.BIOME_REGISTRY)
    val holder: Holder[Biome.Vanilla] = Holder.Reference.createStandAlone(BuiltinRegistry.BIOME_VANILLA, resourceKey.value)

    Biome(holder)
  }

  def apply(value: Vanilla): Biome = {
    val holder = Holder.Reference.createIntrusive(BuiltinRegistry.BIOME_VANILLA, value)

    Biome(holder)
  }
}

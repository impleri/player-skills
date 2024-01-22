package net.impleri.playerskills.facades.minecraft.core

import net.minecraft.core.{Registry => McRegistry}
import net.minecraft.core.HolderLookup
import net.minecraft.resources.{ResourceLocation => McResourceLocation}
import net.minecraft.resources.ResourceKey
import net.minecraft.tags.TagKey
import net.minecraft.world.item.Item
import net.minecraft.world.level.block.Block

import scala.jdk.javaapi.CollectionConverters
import scala.jdk.javaapi.OptionConverters

case class Registry[T](private val registry: McRegistry[T]) {
  def name: ResourceKey[McRegistry[T]] = registry.key().asInstanceOf

  def get(key: ResourceLocation): Option[T] = Option(registry.get(key.name))

  def getKey(value: T): Option[ResourceLocation] = Option(registry.getKey(value)).map(ResourceLocation(_))

  def entries: Map[McResourceLocation, T] = {
    CollectionConverters
      .asScala(registry.entrySet())
      .map(e => e.getKey.location() -> e.getValue)
      .toMap
  }

  def keys: Seq[ResourceLocation] = entries.keys.toList.map(ResourceLocation(_))

  def matchingNamespace(ns: String): Seq[ResourceLocation] = keys.filter(_.getNamespace == ns)

  def matchingTag(key: TagKey[T]): Seq[ResourceLocation] = {
    OptionConverters.toScala(registry.getTag(key))
      .toList
      .flatMap(h => CollectionConverters.asScala(h.iterator()).toList)
      .flatMap(h => OptionConverters.toScala(h.unwrapKey()))
      .map(_.location())
      .map(ResourceLocation(_))
  }

  def getHolder: HolderLookup[T] = HolderLookup.forRegistry(registry)
}

object Registry {
  lazy val Items: Registry[Item] = Registry(McRegistry.ITEM)

  lazy val Blocks: Registry[Block] = Registry(McRegistry.BLOCK)
}

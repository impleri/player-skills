package net.impleri.slab.registry

import net.impleri.slab.block.Block
import net.impleri.slab.item.Item
import net.impleri.slab.item.crafting.RecipeType
import net.impleri.slab.resources.ResourceLocation
import net.minecraft.core.{Registry => McRegistry}
import net.minecraft.core.HolderLookup
import net.minecraft.resources.ResourceKey
import net.minecraft.world.item.{Item => McItem}
import net.minecraft.world.item.crafting.{RecipeType => McRecipeType}
import net.minecraft.world.level.block.{Block => McBlock}

import scala.jdk.CollectionConverters._
import scala.jdk.OptionConverters._

case class Registry[T <: IsRegistered[U], U](private val underlying: McRegistry[U], f: U => T) {
  def name: ResourceKey[McRegistry[T]] = underlying.key().asInstanceOf

  def nameKey: RegistryKey[T] = RegistryKey(name)

  def get(key: ResourceLocation): Option[T] = Option(underlying.get(key.value)).map(f)

  def getKey(value: T): Option[ResourceLocation] = Option(underlying.getKey(value.value)).flatMap(ResourceLocation(_))

  def isValid(key: ResourceLocation): Boolean = get(key).nonEmpty

  def entries: Map[ResourceLocation, T] = {
    underlying.entrySet()
      .asScala
      .flatMap(e => ResourceLocation(e.getKey.location()).map(_ -> f(e.getValue)))
      .toMap
  }

  def keys: Seq[ResourceLocation] = entries.keys.toList

  def matchingNamespace(ns: String): Seq[ResourceLocation] = keys.filter(_.namespace == ns)

  def matchingTag(key: Tag[T]): Seq[ResourceLocation] = {
    underlying.getTag[U](key.value).toScala
      .toList
      .flatMap(_.asScala.toList)
      .flatMap(_.unwrapKey.toScala)
      .map(_.location())
      .flatMap(ResourceLocation(_))
  }

  def getHolder: HolderLookup[U] = HolderLookup.forRegistry(underlying)
}

object Registry {
  type ANY = Registry[_, _]
  type ITEM = Registry[Item, McItem]

  lazy val Items: ITEM = Registry(McRegistry.ITEM, Item(_))

  type BLOCK = Registry[Block, McBlock]
  lazy val Blocks: BLOCK = Registry(McRegistry.BLOCK, Block(_))

  type RECIPE_TYPE = Registry[RecipeType[_, _], McRecipeType[_]]
  lazy val RecipeTypes: RECIPE_TYPE = Registry(McRegistry.RECIPE_TYPE, RecipeType(_))
}

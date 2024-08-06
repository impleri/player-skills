package net.impleri.slab.registry

import net.impleri.slab.block.Block
import net.impleri.slab.entity.EntityType
import net.impleri.slab.item.Item
import net.impleri.slab.item.crafting.RecipeType
import net.impleri.slab.resources.ResourceKey
import net.impleri.slab.resources.ResourceLocation
import net.impleri.slab.resources.ResourceWrapper
import net.minecraft.core.{Registry => McRegistry}
import net.minecraft.core.HolderLookup

import scala.jdk.CollectionConverters._
import scala.jdk.OptionConverters._

class Registry[T <: ResourceWrapper[U], U](
  protected val underlying: Registry.Vanilla[U],
  protected val f: U => T,
) {
  def name: ResourceKey.VanillaRegistry[U] = underlying.key().asInstanceOf

  def get(key: ResourceLocation): Option[T] =
    Option(underlying.get(key.value)).map(f)

  def getKey(value: T): Option[ResourceLocation] =
    Option(underlying.getKey(value.value)).flatMap(ResourceLocation(_))

  def isValid(key: ResourceLocation): Boolean = get(key).nonEmpty

  def entries: Map[ResourceLocation, T] = {
    underlying
      .entrySet()
      .asScala
      .flatMap(e =>
        ResourceLocation(e.getKey.location()).map(_ -> f(e.getValue)),
      )
      .toMap
  }

  def keys: Seq[ResourceLocation] = entries.keys.toList

  def matchingNamespace(ns: String): Seq[ResourceLocation] =
    keys.filter(_.namespace == ns)

  def matchingTag(key: Tag[T, U]): Seq[ResourceLocation] = {
    underlying
      .getTag(key.value)
      .toScala
      .toList
      .flatMap(_.asScala.toList)
      .flatMap(_.unwrapKey.toScala)
      .map(_.location())
      .flatMap(ResourceLocation(_))
  }

  def getHolder: HolderLookup[U] = HolderLookup.forRegistry(underlying)
}

object Registry {
  type Vanilla[T] = McRegistry[T]
  type AnyVanilla = Vanilla[_]

  type Any = Registry[_, _]

  private[slab] val BIOME_REGISTRY = McRegistry.BIOME_REGISTRY

  type ENTITY_TYPE =
    Registry[EntityType[EntityType.AnyVanilla], EntityType.AnyVanilla]
  lazy val Entities: ENTITY_TYPE =
    new Registry(McRegistry.ENTITY_TYPE, EntityType(_))

  type ITEM = Registry[Item, Item.Vanilla]
  lazy val Items: ITEM = new Registry(McRegistry.ITEM, Item(_))

  type BLOCK = Registry[Block, Block.Vanilla]
  lazy val Blocks: BLOCK = new Registry(McRegistry.BLOCK, Block(_))

  type RECIPE_TYPE =
    Registry[RecipeType[RecipeType.AnyVanilla], RecipeType.AnyVanilla]
  lazy val RecipeTypes: RECIPE_TYPE =
    new Registry(McRegistry.RECIPE_TYPE, RecipeType(_))
}

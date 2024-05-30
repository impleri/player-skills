package net.impleri.slab.registry

import net.impleri.slab.item.Item
import net.impleri.slab.resources.ResourceLocation
import net.minecraft.tags.TagKey
import net.minecraft.world.item.{Item => McItem}

class Tag[T](private val underlying: TagKey[T]) extends HasName {
  def name: String = underlying.toString

  lazy val value: TagKey[T] = underlying

  def location: Option[ResourceLocation] = ResourceLocation(underlying.location())

  override def getName: Option[ResourceLocation] = location
}

case class ItemTag(tag: TagKey[McItem]) extends Tag[McItem](tag) with IsIngredient {
  override def inList(ingredients: Seq[Item]): Boolean = {
    ingredients.exists(_.getStack.is(tag))
  }
}

object ItemTag {
  def apply(tag: Tag[_]): ItemTag = new ItemTag(tag.value.asInstanceOf[TagKey[McItem]])
}

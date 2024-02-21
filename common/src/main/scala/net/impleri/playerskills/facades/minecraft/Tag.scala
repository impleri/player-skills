package net.impleri.playerskills.facades.minecraft

import net.impleri.playerskills.facades.minecraft.core.ResourceLocation
import net.impleri.playerskills.facades.minecraft.world.Item
import net.minecraft.tags.TagKey
import net.minecraft.world.item.{Item => McItem}

class Tag[T](tag: TagKey[T]) extends HasName {
  def name: String = tag.toString

  override def getName: Option[ResourceLocation] = Option(tag.location()).map(ResourceLocation(_))
}

case class ItemTag(tag: TagKey[McItem]) extends Tag[McItem](tag) with IsIngredient {
  override def inList(ingredients: Seq[Item]): Boolean = {
    ingredients.exists(_.getStack.is(tag))
  }
}

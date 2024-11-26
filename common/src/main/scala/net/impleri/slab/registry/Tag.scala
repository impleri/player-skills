package net.impleri.slab.registry

import net.impleri.slab.item.Item
import net.impleri.slab.resources.ResourceLocation
import net.impleri.slab.resources.ResourceWrapper
import net.minecraft.tags.TagKey
import net.minecraft.world.item.{Item => McItem}

class Tag[T <: ResourceWrapper[U], U](protected val underlying: TagKey[U])
    extends ResourceWrapper[TagKey[U]] {
  def asString: String = underlying.toString

  def location: Option[ResourceLocation] = ResourceLocation(
    underlying.location(),
  )

  override val name: Option[ResourceLocation] = location

  override def equals(obj: Any): Boolean =
    obj match {
      case t: Tag[_, _] => t.asString == asString
      case _            => false
    }
}

case class ItemTag(tag: TagKey[McItem])
    extends Tag[Item, McItem](tag)
    with IsIngredient {
  override def inList(ingredients: Seq[Item]): Boolean =
    ingredients.exists(_.getStack.is(tag))
}

object ItemTag {
  def apply(tag: Tag[_, _]): ItemTag = new ItemTag(
    tag.value.asInstanceOf[TagKey[McItem]],
  )
}

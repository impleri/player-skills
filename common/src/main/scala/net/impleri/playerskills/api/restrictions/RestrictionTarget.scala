package net.impleri.playerskills.api.restrictions

import net.impleri.playerskills.facades.minecraft.core.ResourceLocation
import net.minecraft.core.Registry
import net.minecraft.resources.ResourceKey
import net.minecraft.tags.TagKey

sealed abstract class RestrictionTarget

object RestrictionTarget {
  case class Namespace private (target: String) extends RestrictionTarget

  case class Tag[T] private (target: TagKey[T]) extends RestrictionTarget

  case class Single private (target: ResourceLocation) extends RestrictionTarget

  case class SingleString private (target: String) extends RestrictionTarget

  def apply[T](
    value: String,
    registryKey: Option[ResourceKey[Registry[T]]] = None,
    singleAsString: Boolean = false,
  ): Option[RestrictionTarget] = {
    value.trim match {
      case s"@$namespace" => Option(RestrictionTarget.Namespace(namespace))
      case s"$namespace:*" => Option(RestrictionTarget.Namespace(namespace))

      case s"#$tag" if registryKey.nonEmpty => {
        ResourceLocation(tag, isSkill = false)
          .flatMap(rl => registryKey.map(rl.getTagKey))
          .map(RestrictionTarget.Tag(_))
      }

      case s if !singleAsString => ResourceLocation(s, isSkill = false).map(Single)
      case s if singleAsString => Option(RestrictionTarget.SingleString(s))
      case _ => None
    }
  }
}

package net.impleri.playerskills.api.restrictions

import net.impleri.playerskills.utils.SkillResourceLocation
import net.minecraft.core.Registry
import net.minecraft.resources.ResourceKey
import net.minecraft.resources.ResourceLocation
import net.minecraft.tags.TagKey

sealed abstract class RestrictionTarget

object RestrictionTarget {
  case class Namespace(target: String) extends RestrictionTarget

  case class Tag[T](target: TagKey[T]) extends RestrictionTarget

  case class Single(target: ResourceLocation) extends RestrictionTarget

  case class SingleString(target: String) extends RestrictionTarget

  def apply[T](
    value: String,
    registryKey: ResourceKey[Registry[T]],
    singleAsString: Boolean = false,
  ): Option[RestrictionTarget] = {
    value.trim match {
      case s"@$namespace" => Option(RestrictionTarget.Namespace(namespace))
      case s"$namespace:*" => Option(RestrictionTarget.Namespace(namespace))

      case s"#$tag" => {
        SkillResourceLocation.ofMinecraft(tag)
          .map(TagKey.create(registryKey, _))
          .map(RestrictionTarget.Tag(_))
      }

      case s if singleAsString => SkillResourceLocation.ofMinecraft(s).map(Single)
      case s if !singleAsString => Option(RestrictionTarget.SingleString(s))
    }
  }
}

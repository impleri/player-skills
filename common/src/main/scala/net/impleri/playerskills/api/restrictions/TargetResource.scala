package net.impleri.playerskills.api.restrictions

import net.impleri.playerskills.facades.minecraft.core.ResourceLocation
import net.minecraft.core.Registry
import net.minecraft.resources.ResourceKey
import net.minecraft.tags.TagKey

sealed abstract class TargetResource

object TargetResource {
  case class Namespace private (target: String) extends TargetResource

  case class Tag[T] private (target: TagKey[T]) extends TargetResource

  case class Single private (target: ResourceLocation) extends TargetResource

  case class SingleString private (target: String) extends TargetResource

  def apply[T](
    value: String,
    registryKey: Option[ResourceKey[Registry[T]]] = None,
    singleAsString: Boolean = false,
  ): Option[TargetResource] = {
    value.trim match {
      case s"@$namespace" => Option(TargetResource.Namespace(namespace))
      case s"$namespace:*" => Option(TargetResource.Namespace(namespace))

      case s"#$tag" if registryKey.nonEmpty => {
        ResourceLocation(tag, isSkill = false)
          .flatMap(rl => registryKey.map(rl.getTagKey))
          .map(TargetResource.Tag(_))
      }

      case s if !singleAsString => ResourceLocation(s, isSkill = false).map(Single)
      case s if singleAsString => Option(TargetResource.SingleString(s))
      case _ => None
    }
  }
}

package net.impleri.playerskills.api.restrictions

import net.impleri.playerskills.facades.minecraft.core.ResourceLocation
import net.minecraft.core.Registry
import net.minecraft.resources.ResourceKey
import net.minecraft.tags.TagKey

sealed abstract class TargetResource

object TargetResource {
  case class Namespace private[restrictions] (target: String) extends TargetResource

  case class Tag[T] private[restrictions] (target: TagKey[T]) extends TargetResource

  case class Single private[restrictions] (target: ResourceLocation) extends TargetResource

  case class SingleString private[restrictions] (target: String) extends TargetResource

  def apply[T](
    value: String,
    registryKey: Option[ResourceKey[Registry[T]]] = None,
    singleAsString: Boolean = false,
  ): Option[TargetResource] = {
    value.trim match {
      case s"@$namespace" => Option(Namespace(namespace))
      case s"$namespace:*" => Option(Namespace(namespace))

      case s"#$tag" if registryKey.nonEmpty => {
        ResourceLocation(tag, isSkill = false)
          .flatMap(rl => registryKey.map(rl.getTagKey))
          .map(Tag(_))
      }

      case s if !singleAsString => ResourceLocation(s, isSkill = false).map(Single.apply)
      case s if singleAsString => Option(SingleString(s))
      case _ => None
    }
  }
}

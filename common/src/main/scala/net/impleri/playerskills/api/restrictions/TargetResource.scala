package net.impleri.playerskills.api.restrictions

import net.impleri.playerskills.PlayerSkills
import net.impleri.slab.registry.{Tag => TagKey}
import net.impleri.slab.registry.RegistryKey
import net.impleri.slab.resources.ResourceLocation

sealed abstract class TargetResource

object TargetResource {
  case class Namespace private[restrictions] (target: String) extends TargetResource

  case class Tag[T] private[restrictions] (target: TagKey[T]) extends TargetResource

  case class Single private[restrictions] (target: ResourceLocation) extends TargetResource

  case class SingleString private[restrictions] (target: String) extends TargetResource

  def apply[T](
    value: String,
    registryKey: Option[RegistryKey[T]] = None,
    singleAsString: Boolean = false,
  ): Option[TargetResource] = {
    value.trim match {
      case s"@$namespace" => Option(Namespace(namespace))
      case s"$namespace:*" => Option(Namespace(namespace))

      case s"#$tag" if registryKey.nonEmpty =>
      PlayerSkills.RESOURCE_FACTORY.create(tag)
        .flatMap(rl => registryKey.map(rl.getTagKey))
        .map(Tag(_))

      case s if !singleAsString => PlayerSkills.RESOURCE_FACTORY.create(s, useDefaultNS = false).map(Single.apply)
      case s if singleAsString => Option(SingleString(s))
      case _ => None
    }
  }
}

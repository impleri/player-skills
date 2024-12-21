package net.impleri.playerskills.api.restrictions

import net.impleri.slab.registry.{Tag => TagKey}
import net.impleri.slab.resources.ResourceKey
import net.impleri.slab.resources.ResourceLocation
import net.impleri.slab.resources.ResourceWrapper

sealed abstract class TargetResource

object TargetResource {
  case class Namespace private[restrictions] (target: String)
      extends TargetResource {
    override def toString: String = s"Target(Namespace{$target})"
  }

  case class Tag[T <: ResourceWrapper[U], U] private[restrictions] (
    target: TagKey[T, U],
  ) extends TargetResource {
    override def toString: String = s"Target(Tag{${target.asString}})"
  }

  case class Single private[restrictions] (target: ResourceLocation)
      extends TargetResource {
    override def toString: String = s"Target(Resource{${target.toString}})"
  }

  case class SingleString private[restrictions] (target: String)
      extends TargetResource {
    override def toString: String = s"Target(String{$target})"
  }

  def create[T <: ResourceWrapper[U], U](
    value: String,
    registryKey: Option[ResourceKey.Registry[U]] = None,
    singleAsString: Boolean = false,
  ): Option[TargetResource] =
    value.trim match {
      case s"@$namespace"  => Option(Namespace(namespace))
      case s"$namespace:*" => Option(Namespace(namespace))

      case s"#$tag" if registryKey.nonEmpty =>
        for {
          key <- registryKey
          resource <- ResourceLocation(tag)
          tagKey = resource.getTagKey[T, U](key)
        } yield Tag(tagKey)

      case s if !singleAsString => ResourceLocation(s).map(Single.apply)
      case s if singleAsString  => Option(SingleString(s))
      case _                    => None
    }
}

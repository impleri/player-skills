package net.impleri.slab.resources

import net.impleri.slab.logging.Logger
import net.impleri.slab.registry.Tag
import net.minecraft.resources.{ResourceLocation => McResourceLocation}
import net.minecraft.tags.TagKey

import scala.util.Failure
import scala.util.Try
import scala.util.chaining.scalaUtilChainingOps

case class ResourceLocation(private val underlying: McResourceLocation) {
  def value: McResourceLocation = underlying

  def asString: String = underlying.toString

  def namespace: String = underlying.getNamespace

  def path: String = underlying.getPath

  def getTagKey[T <: ResourceWrapper[U], U](registryKey: ResourceKey.Registry[U]): Tag[T, U] = new Tag(
    TagKey.create[U](registryKey.value, underlying),
  )

  override def equals(obj: Any): Boolean = {
    obj match {
      case r: ResourceLocation => underlying.equals(r.underlying)
      case r: McResourceLocation => underlying.equals(r)
      case _ => false
    }
  }

  override def toString: String = asString
}

object ResourceLocation {
  type Vanilla = McResourceLocation

  def apply(resource: Option[McResourceLocation]): Option[ResourceLocation] = resource.map(r => new ResourceLocation(r))

  def apply(resource: McResourceLocation): Option[ResourceLocation] = Option(resource).pipe(apply)

  def apply(namespace: String, path: String): Option[ResourceLocation] = {
    Try(
      new McResourceLocation(namespace, path),
    ).tap {
        case Failure(_) => Logger.DEFAULT
          .error(s"Could not parse resource location $namespace${McResourceLocation.NAMESPACE_SEPARATOR}$path")
        case _ => ()
      }
      .toOption
      .flatMap(apply)
  }

  def apply(resource: String): Option[ResourceLocation] = {
    Try(
      new McResourceLocation(resource),
    ).tap {
        case Failure(_) => Logger.DEFAULT.error(s"Could not parse resource location $resource")
        case _ => ()
      }
      .toOption
      .flatMap(apply)
  }
}

package net.impleri.playerskills.restrictions

import net.impleri.playerskills.api.restrictions.Restriction
import net.impleri.playerskills.api.restrictions.TargetResource
import net.impleri.playerskills.restrictions.conditions.RestrictionConditionsBuilder
import net.impleri.slab.logging.Logger
import net.impleri.slab.registry.Registry
import net.impleri.slab.registry.Tag
import net.impleri.slab.resources.ResourceKey
import net.impleri.slab.resources.ResourceLocation
import net.impleri.slab.resources.ResourceWrapper

import scala.util.chaining.scalaUtilChainingOps

trait RestrictionBuilder[T <: ResourceWrapper[U], U, C <: RestrictionConditionsBuilder] {
  protected def registry: Option[Registry[T, U]] = None

  protected def logger: Logger

  private[restrictions] var restrictions: Map[String, C] = Map.empty

  protected def singleAsString: Boolean = false

  private def registryKey: Option[ResourceKey[Registry.Vanilla[U]]] = registry.map { r =>
    ResourceKey(r.name)
  }

  def add(restrictionName: String, builder: C): Unit =
    restrictions += restrictionName -> builder

  def commit(): Unit = {
    restrictions.foreach(restrict)
    restrictions = Map.empty
  }

  protected def restrict(data: (String, C)): Unit = {
    val (resourceName, builder) = data

    logger.debug(s"Saving restriction ${builder.name} for ${builder.getTarget}")

    TargetResource.create(
      builder.getTarget,
      registryKey,
      singleAsString,
    ) match {
      case Some(ns: TargetResource.Namespace) =>
        restrictNamespace(ns.target, builder)
      case Some(s: TargetResource.Single) =>
        restrictOne(s.target, builder)
      case Some(s: TargetResource.SingleString) =>
        restrictString(s.target, builder)
      case Some(t: TargetResource.Tag[_, _]) =>
        restrictTag(t.target.asInstanceOf[Tag[T, U]], builder)
      case e =>
        logger.warn(s"Could not identify resource type for $resourceName: $e")
        ()
    }
  }

  protected def restrictString(
    targetName: String,
    builder: C,
  ): Unit

  protected def restrictOne(
    targetName: ResourceLocation,
    builder: C,
  ): Unit

  private def restrictNamespace(
    namespace: String,
    builder: C,
  ): Unit =
    for {
      reg <- registry.toList
      _ = logger.info(s"Creating restriction for $namespace namespace")
      value <- reg.matchingNamespace(namespace)
    } yield restrictOne(value, builder)

  private def restrictTag(
    tag: Tag[T, U],
    builder: C,
  ): Unit =
    for {
      reg <- registry.toList
      _ = logger.info(s"Creating restriction for ${tag.location} tag")
      value <- reg.matchingTag(tag)
    } yield restrictOne(value, builder)

  private def createLogPiece(prefix: String, values: String): String =
    if (values.isBlank) values else s"$prefix $values"

  protected[restrictions] def logRestriction(
    name: String,
    restriction: Restriction[_, _],
    settings: Option[String] = None,
  ): Unit =
    List(
      createLogPiece("in biomes", restriction.includeBiomes.mkString(",")),
      createLogPiece("not in biomes", restriction.excludeBiomes.mkString(",")),
      createLogPiece("in dimensions", restriction.includeDimensions.mkString(",")),
      createLogPiece("not in dimensions", restriction.excludeDimensions.mkString(",")),
      settings.getOrElse(""),
    ).filterNot(_.isBlank)
      .mkString("; ")
      .tap(logger.infoP(details => s"Created restriction for $name $details"))
}

package net.impleri.playerskills.restrictions

import net.impleri.playerskills.api.restrictions.Restriction
import net.impleri.playerskills.api.restrictions.RestrictionTarget
import net.impleri.playerskills.facades.minecraft.core.Registry
import net.impleri.playerskills.restrictions.conditions.RestrictionConditionsBuilder
import net.impleri.playerskills.utils.PlayerSkillsLogger
import net.minecraft.resources.ResourceLocation
import net.minecraft.tags.TagKey

trait RestrictionBuilder[T, C <: RestrictionConditionsBuilder] {
  protected def registry: Registry[T]

  protected def logger: PlayerSkillsLogger

  private var restrictions: Map[String, C] = Map.empty

  protected def singleAsString: Boolean = false

  def add(restrictionName: String, builder: C): Unit = {
    restrictions += restrictionName -> builder
  }

  def commit(): Unit = {
    restrictions.foreach(restrict)

    restrictions = Map.empty
  }

  private def restrict(data: (String, C)): Unit = {
    val (resourceName, builder) = data

    RestrictionTarget(resourceName, registry.name, singleAsString) match {
      case Some(ns: RestrictionTarget.Namespace) => restrictNamespace(ns.target, builder)
      case Some(s: RestrictionTarget.Single) => restrictOne(s.target, builder)
      case Some(s: RestrictionTarget.SingleString) => restrictString(s.target, builder)
      case Some(t: RestrictionTarget.Tag[_]) => restrictTag(t.target.asInstanceOf, builder)
      case _ =>
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
  ): Unit = {
    logger.info(s"Creating restriction for $namespace namespace")
    registry.matchingNamespace(namespace)
      .foreach(restrictOne(_, builder))
  }

  private def restrictTag(
    tag: TagKey[T],
    builder: C,
  ): Unit = {
    logger.info(s"Creating restriction for ${tag.location} tag")

    registry.matchingTag(tag)
      .foreach(restrictOne(_, builder))
  }

  protected def logRestriction(
    name: String,
    restriction: Restriction[_],
    settings: Option[String] = None,
  ): Unit = {
    val details = List(
      s"in biomes ${restriction.includeBiomes.mkString(",")}",
      s"not in biomes ${restriction.excludeBiomes.mkString(",")}",
      s"in dimensions ${restriction.includeDimensions.mkString(",")}",
      s"not in dimensions ${restriction.excludeDimensions.mkString(",")}",
      settings.getOrElse(""),
    ).filterNot(_.isBlank)
      .mkString("; ")

    logger.info(s"Created restriction for $name $details")
  }
}

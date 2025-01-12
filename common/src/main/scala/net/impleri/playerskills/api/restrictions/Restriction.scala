package net.impleri.playerskills.api.restrictions

import net.impleri.playerskills.utils.PlayerSkillsLogger
import net.impleri.slab.entity.Player
import net.impleri.slab.registry.Tag
import net.impleri.slab.resources.ResourceKey
import net.impleri.slab.resources.ResourceLocation
import net.impleri.slab.resources.ResourceWrapper
import net.impleri.slab.world.Biome

trait Restriction[T <: ResourceWrapper[U], U] {
  def restrictionType: RestrictionType

  def target: T

  def condition: Player => Boolean

  def includeDimensions: Seq[String]

  def excludeDimensions: Seq[String]

  def includeBiomes: Seq[String]

  def excludeBiomes: Seq[String]

  def replacement: Option[T]

  def isType(input: RestrictionType): Boolean = restrictionType == input

  def targets(value: ResourceLocation): Boolean =
    target.name.exists(value.equals)

  def hasReplacement: Boolean = replacement.nonEmpty

  def isApplicableDimension(dimension: ResourceLocation): Boolean =
    (includeDimensions.isEmpty || includeDimensions.exists(matchDimension(dimension))) &&
      !excludeDimensions.exists(matchDimension(dimension))

  def isApplicableBiome(biome: Biome): Boolean =
    (includeBiomes.isEmpty || includeBiomes.exists(matchBiome(biome))) &&
      !excludeBiomes.exists(matchBiome(biome))

  private def matchDimension(dimension: ResourceLocation)(target: String): Boolean =
    TargetResource.create(target, None) match {
      case Some(n: TargetResource.Namespace) => dimension.namespace == n.target
      case Some(n: TargetResource.Single) => dimension == n.target
      case _ => Restriction.DEFAULT_CONDITION_RESPONSE
    }

  private def matchBiome(biome: Biome)(target: String): Boolean =
      TargetResource.create(target, Option(ResourceKey.BIOME_REGISTRY)) match {
        case Some(n: TargetResource.Namespace) => biome.isNamespaced(n.target)
        case Some(n: TargetResource.Tag[_, _]) => biome.isTagged(n.target.asInstanceOf[Tag[Biome, Biome.Vanilla]])
        case Some(n: TargetResource.Single) => biome.isNamed(n.target)
        case _ => Restriction.DEFAULT_CONDITION_RESPONSE
      }
}

object Restriction {
  val DEFAULT_RESPONSE: Boolean = true

  val DEFAULT_CONDITION_RESPONSE: Boolean = false

  val DEFAULT_CONDITION: Player => Boolean = _ => DEFAULT_RESPONSE
}

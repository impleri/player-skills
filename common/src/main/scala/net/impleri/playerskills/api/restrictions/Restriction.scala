package net.impleri.playerskills.api.restrictions

import net.impleri.playerskills.facades.minecraft.HasName
import net.impleri.playerskills.facades.minecraft.Player
import net.impleri.playerskills.facades.minecraft.world.Biome
import net.minecraft.core.Registry
import net.minecraft.resources.ResourceLocation

trait Restriction[T <: HasName] {
  def restrictionType: RestrictionType

  def target: T

  def condition: Player[_] => Boolean

  def includeDimensions: Seq[String]

  def excludeDimensions: Seq[String]

  def includeBiomes: Seq[String]

  def excludeBiomes: Seq[String]

  def replacement: Option[T]

  def isType(input: RestrictionType): Boolean = restrictionType == input

  def targets(value: ResourceLocation): Boolean = target.getName.contains(value)

  def hasReplacement: Boolean = replacement.nonEmpty

  def isAllowedDimension(dimension: ResourceLocation): Boolean = {
    dimensionListIncludes(includeDimensions, dimension) && !dimensionListIncludes(excludeDimensions, dimension)
  }

  def isAllowedBiome(biome: Biome): Boolean = {
    biomeListIncludes(includeBiomes, biome) && !biomeListIncludes(excludeBiomes, biome)
  }

  private def dimensionListIncludes(list: Seq[String], dimension: ResourceLocation): Boolean = {
    list.exists(d =>
      RestrictionTarget(d, Registry.DIMENSION_REGISTRY) match {
        case Some(n: RestrictionTarget.Namespace) => dimension.getNamespace == n.target
        case Some(n: RestrictionTarget.Single) => dimension == n.target
        case _ => false
      },
    )
  }

  private def biomeListIncludes(list: Seq[String], biome: Biome): Boolean = {
    list.exists(d =>
      RestrictionTarget(d, Registry.BIOME_REGISTRY) match {
        case Some(n: RestrictionTarget.Namespace) => biome.isNamespaced(n.target)
        case Some(n: RestrictionTarget.Tag[_]) => biome.isTagged(n.target.asInstanceOf)
        case Some(n: RestrictionTarget.Single) => biome.isNamed(n.target)
        case _ => false
      },
    )
  }
}

object Restriction {
  val DEFAULT_CONDITION: Player[_] => Boolean = _ => true

  val DEFAULT_RESPONSE: Boolean = true
}

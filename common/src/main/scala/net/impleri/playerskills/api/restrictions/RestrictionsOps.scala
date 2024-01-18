package net.impleri.playerskills.api.restrictions

import net.impleri.playerskills.facades.minecraft.HasName
import net.impleri.playerskills.facades.minecraft.Player
import net.impleri.playerskills.facades.minecraft.core.Position
import net.impleri.playerskills.facades.minecraft.core.ResourceLocation
import net.impleri.playerskills.facades.minecraft.world.Biome
import net.impleri.playerskills.restrictions.RestrictionRegistry
import net.impleri.playerskills.utils.PlayerSkillsLogger

import scala.collection.View

trait PlayerRestriction {
  protected def matchesPlayer(player: Player[_])(restriction: Restriction[_]): Boolean = restriction.condition(player)
}

trait TargetRestriction {
  protected def restrictionType: RestrictionType

  protected def matchesTarget(name: ResourceLocation)(restriction: Restriction[_]): Boolean = {
    restriction.isType(restrictionType) && restriction.targets(name)
  }
}

trait RestrictionsOps[T <: HasName, R <: Restriction[T]]
  extends TargetRestriction
    with PlayerRestriction {
  protected def registry: RestrictionRegistry

  protected def logger: PlayerSkillsLogger

  private def getRestrictionsFor(
    player: Player[_],
    target: ResourceLocation,
    dimension: Option[ResourceLocation],
    biome: Option[Biome],
  ): View[R] = {
    registry.entries.view
      .filter(matchesTarget(target))
      .filter(r => dimension.forall(r.isAllowedDimension))
      .filter(r => biome.forall(r.isAllowedBiome))
      .filter(matchesPlayer(player))
      .asInstanceOf[View[R]]
  }

  private def canHelper(
    player: Player[_],
    target: ResourceLocation,
    getFieldValue: R => Boolean,
    fieldName: String,
    pos: Option[Position] = None,
    dimension: Option[ResourceLocation] = None,
    biome: Option[Biome] = None,
    f: R => Boolean = _ => true,
  ): Boolean = {
    val hasRestrictions = getRestrictionsFor(
      player,
      target,
      dimension.orElse(player.dimension),
      biome.orElse(player.biomeAt(pos)),
    )
      .filter(f)
      .map(getFieldValue)
      .exists(!_)

    logger
      .debug(
        s"Does ${player.name} have $fieldName restrictions with $target in  $dimension/$biome? $hasRestrictions",
      )

    !hasRestrictions
  }

  protected def canPlayer(
    player: Player[_],
    target: T,
    getFieldValue: R => Boolean,
    fieldName: String,
    pos: Option[Position] = None,
    dimension: Option[ResourceLocation] = None,
    biome: Option[Biome] = None,
    f: R => Boolean = _ => true,
  ): Boolean = {
    if (player.isEmpty) {
      logger
        .warn(s"Attempted to determine if null player can $fieldName on target $target in $dimension/${
          biome
            .flatMap(_.name)
        }",
        )
      RestrictionsOps.DEFAULT_RESPONSE
    } else {
      val targetName = target.getName

      if (targetName.isEmpty) {
        RestrictionsOps.DEFAULT_RESPONSE
      } else {
        canHelper(
          player,
          targetName.get,
          getFieldValue,
          fieldName,
          pos,
          dimension,
          biome,
          f,
        )
      }
    }
  }


  private def getReplacementsFor(
    player: Player[_],
    target: ResourceLocation,
    dimension: Option[ResourceLocation] = None,
    biome: Option[Biome] = None,
    f: R => Boolean = _ => true,
  ): View[R] = {
    getRestrictionsFor(player, target, dimension, biome)
      .filter(_.hasReplacement)
      .filter(f)
  }

  protected def getReplacementFor(
    player: Player[_],
    target: ResourceLocation,
    dimension: Option[ResourceLocation] = None,
    biome: Option[Biome] = None,
    f: R => Boolean = _ => true,
  ): Option[T] = {
    val restriction = getReplacementsFor(player, target, dimension, biome, f)
      .headOption

    logger
      .debug(
        s"$target should be replaced with ${
          restriction
            .flatMap(_.replacement)
            .flatMap(_.getName)
        } in $dimension/${biome.flatMap(_.name)} for ${player.name}",
      )

    restriction.flatMap(_.replacement)
  }
}

object RestrictionsOps {
  val DEFAULT_RESPONSE = true
}

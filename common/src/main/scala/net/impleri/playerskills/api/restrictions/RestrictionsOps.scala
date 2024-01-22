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
  protected[restrictions] def matchesPlayer(player: Player[_])(restriction: Restriction[_]): Boolean = {
    restriction
      .condition(player)
  }
}

trait TargetRestriction {
  protected def restrictionType: RestrictionType

  protected[restrictions] def matchesTarget(name: ResourceLocation)(restriction: Restriction[_]): Boolean = {
    restriction.isType(restrictionType) && restriction.targets(name)
  }
}

trait RestrictionsOps[T <: HasName, R <: Restriction[T]]
  extends TargetRestriction
    with PlayerRestriction {
  protected def registry: RestrictionRegistry

  protected def logger: PlayerSkillsLogger

  private[restrictions] def getRestrictionsFor(
    player: Player[_],
    target: ResourceLocation,
    dimension: Option[ResourceLocation],
    biome: Option[Biome],
  ): View[R] = {
    registry.entries.view
      .filter(matchesPlayer(player))
      .filter(matchesTarget(target))
      .filter(r => dimension.forall(r.isAllowedDimension))
      .filter(r => biome.forall(r.isAllowedBiome))
      .asInstanceOf[View[R]]
  }

  private def canHelper(
    player: Player[_],
    target: ResourceLocation,
    getFieldValue: R => Boolean,
    fieldName: String,
    pos: Option[Position],
    dimension: Option[ResourceLocation],
    biome: Option[Biome],
    f: R => Boolean,
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

  protected[restrictions] def canPlayer(
    player: Player[_],
    target: T,
    getFieldValue: R => Boolean,
    fieldName: String,
    pos: Option[Position] = None,
    dimension: Option[ResourceLocation] = None,
    biome: Option[Biome] = None,
    f: R => Boolean = _ => true,
  ): Boolean = {
    (player.asOption, target.getName) match {
      case (Some(p), Some(t)) => {
        canHelper(
          p.asPlayer,
          t,
          getFieldValue,
          fieldName,
          pos,
          dimension,
          biome,
          f,
        )
      }
      case (None, _) => {
        logger.warn(
          s"Attempted to determine if null player can $fieldName on target $target in $dimension/${
            biome.flatMap(_.name)
          }",
        )
        RestrictionsOps.DEFAULT_RESPONSE
      }
      case (_, None) => {
        logger
          .warn(
            s"Attempted to determine if player ${player.name} can $fieldName on a non-target in $dimension/${
              biome.flatMap(_.name)
            }",
          )
        RestrictionsOps.DEFAULT_RESPONSE
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

  protected[restrictions] def getReplacementFor(
    player: Player[_],
    target: ResourceLocation,
    dimension: Option[ResourceLocation] = None,
    biome: Option[Biome] = None,
    f: R => Boolean = _ => true,
  ): Option[T] = {
    val replacement = getReplacementsFor(player, target, dimension, biome, f)
      .headOption
      .flatMap(_.replacement)

    logger
      .debug(
        s"$target should be replaced with ${
          replacement
            .flatMap(_.getName)
        } in $dimension/${biome.flatMap(_.name)} for ${player.name}",
      )

    replacement
  }
}

object RestrictionsOps {
  val DEFAULT_RESPONSE = true
}

package net.impleri.playerskills.api.restrictions

import net.impleri.playerskills.restrictions.RestrictionRegistry
import net.impleri.slab.entity.Player
import net.impleri.slab.logging.Logger
import net.impleri.slab.resources.ResourceLocation
import net.impleri.slab.resources.ResourceWrapper
import net.impleri.slab.world.Biome
import net.impleri.slab.world.Position

import scala.collection.View
import scala.util.chaining.scalaUtilChainingOps

trait PlayerRestriction {
  protected[restrictions] def matchesPlayer(player: Player)(restriction: Restriction[_, _]): Boolean =
    restriction.condition(player)
}

trait TargetRestriction {
  protected def restrictionType: RestrictionType

  protected[restrictions] def matchesTarget(name: ResourceLocation)(restriction: Restriction[_, _]): Boolean =
    restriction.isType(restrictionType) &&
      restriction.targets(name)
}

trait RestrictionsOps[T <: ResourceWrapper[U], U, R <: Restriction[T, U]]
    extends TargetRestriction
    with PlayerRestriction {
  protected def registry: RestrictionRegistry

  protected def logger: Logger

  private[restrictions] def getRestrictionsFor(
    player: Player,
    target: ResourceLocation,
    dimension: Option[ResourceLocation],
    biome: Option[Biome],
  ): View[R] =
    registry.entries.view
      .filter(matchesTarget(target))
      .tap(logger.traceP(rs => s"Found ${rs.size} restrictions for $target"))
      .filter(r => dimension.forall(r.isAllowedDimension))
      .tap(logger.traceP(rs => s"Found ${rs.size} restrictions for $target in dimension $dimension"))
      .filter(r => biome.forall(r.isAllowedBiome))
      .tap(logger.traceP(rs => s"Found ${rs.size} restrictions for $target in biome $biome"))
      .filter(matchesPlayer(player))
      .asInstanceOf[View[R]]
      .tap(logger.traceP(rs => s"Found ${rs.size} restrictions for $target affecting $player"))

  private def IsAllowedTo(
    player: Player,
    target: ResourceLocation,
    getFieldValue: R => Boolean,
    fieldName: String,
    pos: Option[Position],
    dimension: Option[ResourceLocation],
    biome: Option[Biome],
    f: R => Boolean,
  ): Boolean = {
    val actualDimension = dimension.orElse(player.dimension)
    val actualBiome = biome.orElse(player.biomeAt(pos))

    val hasRestrictions = getRestrictionsFor(
      player,
      target,
      actualDimension,
      actualBiome,
    )
      .filter(f)
      .map(getFieldValue)
      .exists(!_) // We only care if there's a $value = false

    // We purposely change the log level to reduce noise in the logs
    val logMessage = s"Does ${player.handle} have $fieldName restrictions with $target in $actualDimension/$actualBiome? $hasRestrictions"
    if (hasRestrictions) logger.debug(logMessage) else logger.trace(logMessage)

    // Invert the value so that true = player can
    !hasRestrictions
  }

  protected[restrictions] def canPlayer(
    player: Player,
    target: T,
    getFieldValue: R => Boolean,
    fieldName: String,
    pos: Option[Position] = None,
    dimension: Option[ResourceLocation] = None,
    biome: Option[Biome] = None,
    f: R => Boolean = _ => true,
  ): Boolean =
    (player.asOption, target.name) match {
      case (Some(p), Some(t)) =>
        IsAllowedTo(
          p.asPlayer,
          t,
          getFieldValue,
          fieldName,
          pos,
          dimension,
          biome,
          f,
        )

      case (None, _) =>
        logger.warn(
          s"Attempted to determine if null player can $fieldName on target $target in $dimension/${biome.flatMap(_.name)}",
        )
        RestrictionsOps.DEFAULT_RESPONSE

      case (_, None) =>
        logger
          .warn(
            s"Attempted to determine if player ${player.handle} can $fieldName on a non-target in $dimension/${biome.flatMap(_.name)}",
          )
        RestrictionsOps.DEFAULT_RESPONSE

    }

  private def getReplacementsFor(
    player: Player,
    target: ResourceLocation,
    dimension: Option[ResourceLocation] = None,
    biome: Option[Biome] = None,
    f: R => Boolean = _ => true,
  ): View[R] =
    getRestrictionsFor(player, target, dimension, biome)
      .filter(_.hasReplacement)
      .filter(f)

  protected[restrictions] def getReplacementFor(
    player: Player,
    target: ResourceLocation,
    dimension: Option[ResourceLocation] = None,
    biome: Option[Biome] = None,
    f: R => Boolean = _ => true,
  ): Option[T] =
    getReplacementsFor(player, target, dimension, biome, f)
      .headOption
      .flatMap(_.replacement)
      .tap(logger.debugP(
        replacement =>  {
          val rName = replacement.flatMap(_.name)
          val bName = biome.flatMap(_.name)
          s"$target should be replaced with $rName in $dimension/$bName for ${player.handle}"
        },
      ))
}

object RestrictionsOps {
  val DEFAULT_RESPONSE = true
}

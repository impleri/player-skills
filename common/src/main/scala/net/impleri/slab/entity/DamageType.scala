package net.impleri.slab.entity

import net.minecraft.world.damagesource.DamageSource
import net.minecraft.world.damagesource.EntityDamageSource
import net.minecraft.world.damagesource.IndirectEntityDamageSource

sealed trait DamageType {
  def name: String
}

sealed trait HasSource extends DamageType {
  def source: Option[Entity.Any]

  val isPlayer: Boolean = source.fold(false)(_.isPlayer)
}

sealed trait HasIndirectSource extends DamageType {
  def indirectSource: Option[Entity.Any]
}

sealed trait BypassArmor extends DamageType

sealed trait BypassMagic extends DamageType

sealed trait BypassEnchantments extends DamageType

sealed trait BypassInvulnerability extends DamageType

sealed trait DamagesHelmet extends DamageType

sealed trait Explosive extends DamageType

sealed trait ScalesWithDifficulty extends DamageType

sealed trait Thorns extends DamageType

sealed trait Fall extends DamageType

sealed trait Fire extends DamageType

sealed trait Magic extends DamageType

sealed trait Projectile extends DamageType

protected case class SimpleDamage(name: String) extends DamageType

protected case class WithFire(name: String)
    extends DamageType
    with BypassArmor
    with Fire

protected case class Lightning(name: String) extends DamageType

protected case class Lava(name: String) extends DamageType with Fire

protected case class Suffocation(name: String)
    extends DamageType
    with BypassArmor

protected case class Starve(name: String)
    extends DamageType
    with BypassMagic
    with BypassArmor

protected case class Falling(name: String)
    extends DamageType
    with BypassArmor
    with Fall

protected case class DirectedDamage(name: String)
    extends DamageType
    with BypassArmor

protected case class MagicDamage(name: String) extends DamageType with Magic

protected case class FallenDamage(name: String)
    extends DamageType
    with DamagesHelmet

protected case class OutOfWorld(name: String)
    extends DamageType
    with BypassArmor
    with BypassInvulnerability

protected case class EntityDamage(name: String, source: Option[Entity.Any])
    extends DamageType
    with HasSource

protected case class IndirectEntityDamage(
  name: String,
  source: Option[Entity.Any],
  indirectSource: Option[Entity.Any],
) extends DamageType
    with HasSource
    with HasIndirectSource

protected case class ProjectileDamage(
  name: String,
  source: Option[Entity.Any],
  indirectSource: Option[Entity.Any],
) extends DamageType
    with HasSource
    with HasIndirectSource
    with Projectile

protected case class FireProjectileDamage(
  name: String,
  source: Option[Entity.Any],
  indirectSource: Option[Entity.Any],
) extends DamageType
    with HasSource
    with HasIndirectSource
    with Projectile
    with Fire

protected case class IndirectExplosionDamage(
  name: String,
  indirectSource: Option[Entity.Any],
) extends DamageType
    with HasIndirectSource
    with Explosive
    with ScalesWithDifficulty

protected case class ExplosionDamage(name: String)
    extends DamageType
    with Explosive
    with ScalesWithDifficulty

protected case class FireworksDamage(
  name: String,
  source: Option[Entity.Any],
  indirectSource: Option[Entity.Any],
) extends DamageType
    with HasSource
    with HasIndirectSource
    with Explosive

protected case class MagicAttackDamage(
  name: String,
  source: Option[Entity.Any],
  indirectSource: Option[Entity.Any],
) extends DamageType
    with HasSource
    with HasIndirectSource
    with BypassArmor
    with Magic

protected case class ThornsDamage(
  name: String,
  source: Option[Entity.Any],
) extends DamageType
    with HasSource
    with Thorns
    with Magic

protected case class SonicBoomDamage(
  name: String,
  source: Option[Entity.Any],
) extends DamageType
    with HasSource
    with BypassArmor
    with BypassEnchantments
    with Magic

protected case class UnknownDamage(private val underlying: DamageSource)
    extends DamageType {
  def name: String = underlying.getMsgId
}

object DamageType {
  private val simpleTypes: Map[String, DamageType] = Map(
    "inFire" -> WithFire("inFire"),
    "lightningBolt" -> Lightning("lightningBolt"),
    "onFire" -> WithFire("onFire"),
    "lava" -> Lava("lava"),
    "hotFloor" -> Lava("hotFloor"),
    "inWall" -> Suffocation("inWall"),
    "cramming" -> Suffocation("cramming"),
    "drown" -> Suffocation("drown"),
    "starve" -> Starve("starve"),
    "cactus" -> SimpleDamage("cactus"),
    "fall" -> Falling("fall"),
    "flyIntoWall" -> DirectedDamage("flyIntoWall"),
    "outOfWorld" -> OutOfWorld("outOfWorld"),
    "generic" -> DirectedDamage("generic"),
    "magic" -> MagicDamage("magic"),
    "wither" -> DirectedDamage("wither"),
    "anvil" -> FallenDamage("anvil"),
    "fallingBlock" -> FallenDamage("fallingBlock"),
    "dragonBreath" -> DirectedDamage("dragonBreath"),
    "dryout" -> SimpleDamage("dryout"),
    "sweetBerryBush" -> SimpleDamage("sweetBerryBush"),
    "freeze" -> DirectedDamage("freeze"),
    "fallingStalactite" -> FallenDamage("fallingStalactite"),
    "stalagmite" -> Falling("stalagmite"),
  )

  private val entities = Set("sting", "mob", "player")

  private val projectiles = Set("arrow", "trident", "witherSkull", "thrown")

  private val fireProjectiles = Set("onFire", "fireball")

  def apply(source: DamageSource): DamageType = {
    source match {
      // Direct Sources
      case e: EntityDamageSource if e.getMsgId == "sonic_boom" =>
        SonicBoomDamage(
          e.getMsgId,
          Option(e.getEntity).map(Entity(_)),
        )

      case e: EntityDamageSource if e.getMsgId == "thorns" =>
        ThornsDamage(
          e.getMsgId,
          Option(e.getEntity).map(Entity(_)),
        )

      case e: EntityDamageSource if entities.contains(e.getMsgId) =>
        EntityDamage(
          e.getMsgId,
          Option(e.getEntity).map(Entity(_)),
        )

      // Indirect Sources
      case e: EntityDamageSource if e.getMsgId == "explosion.player" =>
        IndirectExplosionDamage(
          e.getMsgId,
          Option(e.getEntity).map(Entity(_)),
        )

      case p: IndirectEntityDamageSource if p.getMsgId == "fireworks" =>
        FireworksDamage(
          p.getMsgId,
          Option(p.getDirectEntity).map(Entity(_)),
          Option(p.getEntity).map(Entity(_)),
        )

      case p: IndirectEntityDamageSource if p.getMsgId == "indirectMagic" =>
        MagicAttackDamage(
          p.getMsgId,
          Option(p.getDirectEntity).map(Entity(_)),
          Option(p.getEntity).map(Entity(_)),
        )

      case p: IndirectEntityDamageSource
          if fireProjectiles.contains(p.getMsgId) =>
        FireProjectileDamage(
          p.getMsgId,
          Option(p.getDirectEntity).map(Entity(_)),
          Option(p.getEntity).map(Entity(_)),
        )

      case p: IndirectEntityDamageSource if projectiles.contains(p.getMsgId) =>
        ProjectileDamage(
          p.getMsgId,
          Option(p.getDirectEntity).map(Entity(_)),
          Option(p.getEntity).map(Entity(_)),
        )

      case p: IndirectEntityDamageSource if entities.contains(p.getMsgId) =>
        IndirectEntityDamage(
          p.getMsgId,
          Option(p.getDirectEntity).map(Entity(_)),
          Option(p.getEntity).map(Entity(_)),
        )

      // Simple Sources
      case d: DamageSource if d.getMsgId == "explosion" =>
        ExplosionDamage(d.getMsgId)

      case d: DamageSource if simpleTypes.keySet.contains(d.getMsgId) =>
        simpleTypes(d.getMsgId)

      // Catchall

      case u: DamageSource => UnknownDamage(u)
    }
  }
}

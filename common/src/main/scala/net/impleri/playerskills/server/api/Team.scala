package net.impleri.playerskills.server.api

import net.impleri.playerskills.PlayerSkills
import net.impleri.playerskills.api.skills.Skill
import net.impleri.playerskills.api.skills.SkillOps
import net.impleri.playerskills.api.skills.TeamMode
import net.impleri.playerskills.server.ServerStateContainer
import net.impleri.playerskills.utils.PlayerSkillsLogger
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.player.{Player => MinePlayer}

import java.util.UUID
import scala.util.chaining.scalaUtilChainingOps

trait Team {
  protected[server] def getTeamMembersFor(player: MinePlayer): List[UUID]
}

case class StubTeam() extends Team {
  override def getTeamMembersFor(player: MinePlayer): List[UUID] = {
    List(player.getUUID)
  }
}

trait TeamSkillCalculator {
  protected def playerOps: Player

  protected def getSharedSkills(player: MinePlayer): Seq[Skill[_]] = {
    playerOps.get(player)
      .filter(_.teamMode == TeamMode.Shared())
  }

  private def getMaxSkill[T](players: Seq[UUID], skill: Skill[T]): Option[Skill[T]] = {
    players
      .flatMap(playerOps.get[T](_, skill.name))
      .maxOption(ord = Skill().sortHelper[T])
  }

  protected def getMaxTeamSkills(players: Seq[UUID], skills: Seq[Skill[_]]): Seq[Skill[_]] = {
    skills.flatMap(getMaxSkill(players, _))
  }
}

trait TeamUpdater {
  protected def playerOps: Player

  protected def emitHelper[T](player: ServerPlayer, next: Skill[T], prev: Option[Skill[_]]): Unit = {
    PlayerSkills
      .emitSkillChanged(player, next, prev.asInstanceOf[Option[Skill[T]]])
  }

  protected def ensurePlayerOpen[V](playerId: UUID)(f: () => V): V = {
    playerOps.isOnline(playerId)
      .tap(a => if (!a) playerOps.open(playerId))
      .pipe(a => (a, f()))
      .tap(t => if (!t._1) playerOps.close(playerId))
      ._2
  }

  protected def updateMemberSkill[T](skill: Skill[T])(playerId: UUID): Option[(UUID, Option[Skill[T]])] = {
    ensurePlayerOpen(playerId) { () =>
      playerOps.can(playerId, skill, skill.value)
        .pipe(c => (c, playerOps.get[T](playerId, skill.name)))
        .pipe(t => (t._2, if (!t._1) playerOps.upsert(playerId, skill) else Seq.empty))
        .pipe(t => if (t._2.nonEmpty) Some(playerId, t._1) else None)
    }
  }
}

class TeamOps(
  override val playerOps: Player,
  protected val skillOps: SkillOps,
  protected val instance: Team,
  protected val logger: PlayerSkillsLogger,
) extends TeamUpdater with TeamSkillCalculator {
  private def countPlayerIf[T](skill: Skill[T])(playerId: UUID): Boolean = {
    ensurePlayerOpen(playerId)(() => playerOps.can(playerId, skill, skill.value))
  }

  private def countWith(playerIds: Seq[UUID], skill: Skill[_]): Int = {
    playerIds.count(countPlayerIf(skill))
  }

  private def getTeamLimit[T](players: Seq[UUID], skill: Skill[T]) = {
    Option(players.size)
      .filter(_ > 1)
      .filter(_ => skill.teamMode match {
        case TeamMode.Off() => false
        case TeamMode.Shared() => false
        case _ => true
      },
      )
      .map(skill.teamMode.getLimit(skill, _))
  }

  private def hasRoomToGrow(count: Int, limit: Option[Int]) = {
    limit.forall(count < _)
  }

  private def allows[T](player: MinePlayer, skill: Skill[T]): Boolean = {
    player
      .pipe(instance.getTeamMembersFor)
      .pipe(s => (countWith(s, skill), getTeamLimit(s, skill)))
      .tap(logger.infoP(t => s"Does the team allow updating skill? (${t._1} < ${t._2})"))
      .pipe(t => hasRoomToGrow(t._1, t._2))
  }

  private def updateTeamSkill[T](player: MinePlayer, skill: Skill[T], emit: Boolean = true) = {
    player
      .pipe(instance.getTeamMembersFor)
      .flatMap(updateMemberSkill(skill))
      .map(t => (player.getServer.getPlayerList.getPlayer(t._1), t._2))
      .tap(e => if (emit) e.foreach(t => emitHelper(t._1, skill, t._2)))
      .nonEmpty
  }

  private def updateSkill[T](player: MinePlayer)(skill: Skill[T]) = {
    if (skill.teamMode == TeamMode.Shared()) {
      updateTeamSkill(player, skill)
    } else {
      playerOps
        .upsert(player, skill)
        .nonEmpty
    }
  }

  def changeSkill[T](player: MinePlayer, skill: Skill[T], value: Option[T]): Option[Boolean] = {
    value.orElse(skill.value)
      .pipe(playerOps.calculateValue(player, skill, _))
      .tap(_ => logger.info(s"Changing skill ${skill.name} to $value for ${player.getName.getString}"))
      .filter(_ => allows(player, skill))
      .tap(a => logger.info(s"Is skill change allowed? $a"))
      .map(updateSkill(player))
  }

  def degrade[T](player: MinePlayer, skill: Skill[T], min: Option[T], max: Option[T]): Option[Boolean] = {
    skillOps.calculatePrev(skill, min, max)
      .flatMap(v => changeSkill(player, skill, Option(v)))
  }

  def improve[T](player: MinePlayer, skill: Skill[T], min: Option[T], max: Option[T]): Option[Boolean] = {
    skillOps.calculateNext(skill, min, max)
      .tap(_ => logger.info(s"Improving skill ${skill.name} for ${player.getName.getString}"))
      .flatMap(v => changeSkill(player, skill, Option(v)))
  }

  def syncFromPlayer(player: ServerPlayer): Boolean = {
    player
      .tap(p => logger.debug(s"Syncing skills from ${p.getName}"))
      .pipe(getSharedSkills)
      .map(updateTeamSkill(player, _))
      .forall(_ == true)
  }

  def syncEntireTeam(player: ServerPlayer): Boolean = {
    logger.debug(s"Syncing entire team connected to ${player.getName}")
    val skills = getSharedSkills(player)
    val allPlayers = instance.getTeamMembersFor(player)
    val offlinePlayers = playerOps.open(allPlayers)
    val maxSkills = getMaxTeamSkills(allPlayers, skills)

    val updated = maxSkills.flatMap(s => allPlayers.map(updateMemberSkill(s)))
      .flatten

    playerOps.close(offlinePlayers)
    updated
      .filterNot(t => offlinePlayers.contains(t._1))
      .filter(t => t._2.nonEmpty)
      .map(t => (player.server.getPlayerList.getPlayer(t._1), t._2.flatMap(s => maxSkills.find(_.name == s.name)), t
        ._2),
      )
      .foreach(t => emitHelper(t._1, t._2.get, t._3))

    updated.nonEmpty
  }
}

object Team {
  def apply(
    instance: Team = ServerStateContainer.TEAM,
    playerOps: Player = Player(),
    skillOps: SkillOps = Skill(),
    logger: PlayerSkillsLogger = PlayerSkillsLogger.SKILLS,
  ): TeamOps = {
    new TeamOps(playerOps, skillOps, instance, logger)
  }
}

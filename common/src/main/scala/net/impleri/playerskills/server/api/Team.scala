package net.impleri.playerskills.server.api

import net.impleri.playerskills.PlayerSkills
import net.impleri.playerskills.api.skills.Skill
import net.impleri.playerskills.api.skills.SkillOps
import net.impleri.playerskills.api.skills.TeamMode
import net.impleri.playerskills.facades.MinecraftPlayer
import net.impleri.playerskills.server.ServerStateContainer
import net.impleri.playerskills.utils.PlayerSkillsLogger

import java.util.UUID
import scala.util.chaining.scalaUtilChainingOps

trait Team {
  protected[server] def getTeamMembersFor(player: UUID): List[UUID]

}

case class StubTeam() extends Team {
  override def getTeamMembersFor(player: UUID): List[UUID] = {
    List(player)
  }
}

trait TeamSkillCalculator {
  protected def playerOps: Player

  protected[api] def getSharedSkills(player: UUID): Seq[Skill[_]] = {
    playerOps.get(player)
      .filter(_.teamMode == TeamMode.Shared())
  }

  private def getMaxSkill[T](players: Seq[UUID], skill: Skill[T]): Option[Skill[T]] = {
    players
      .flatMap(playerOps.get[T](_, skill.name))
      .maxOption(ord = Skill().sortHelper[T])
  }

  protected[api] def getMaxTeamSkills(players: Seq[UUID], skills: Seq[Skill[_]]): Seq[Skill[_]] = {
    skills.flatMap(getMaxSkill(players, _))
  }
}

trait TeamUpdater extends TeamSkillCalculator {
  protected def logger: PlayerSkillsLogger

  protected def playerOps: Player

  protected def team: Team

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

  protected def updateSkillsForTeam(player: UUID): (Seq[Skill[_]], List[UUID], Seq[(UUID, Option[Skill[_]])]) = {
    logger.debug(s"Syncing entire team connected to $player")
    val skills = getSharedSkills(player)
    val allPlayers = team.getTeamMembersFor(player)
    val offlinePlayers = playerOps.open(allPlayers)
    val maxSkills = getMaxTeamSkills(allPlayers, skills)

    val updated = maxSkills
      .flatMap(s => allPlayers.map(updateMemberSkill(s)))
      .flatten

    playerOps.close(offlinePlayers)

    (maxSkills, offlinePlayers, updated)
  }
}

trait TeamLimit extends TeamUpdater {
  protected def playerOps: Player

  protected def team: Team

  protected def logger: PlayerSkillsLogger

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

  protected def allows[T](player: UUID, skill: Skill[T]): Boolean = {
    team.getTeamMembersFor(player)
      .pipe(s => (countWith(s, skill), getTeamLimit(s, skill)))
      .tap(logger.infoP(t => s"Does the team allow updating skill? (${t._1} < ${t._2})"))
      .pipe(t => hasRoomToGrow(t._1, t._2))
  }
}

trait TeamNotifier {
  protected def playerOps: Player

  protected def notifyPlayers[T](player: MinecraftPlayer[_], originalSkill: Skill[T], emit: Boolean = true)
    (updates: List[(UUID, Option[Skill[_]])]): Unit = {
    if (emit) {
      updates
        .foreach(tuple =>
          player.server.getPlayer(tuple._1)
            .foreach(emitHelper(_, originalSkill, tuple._2)),
        )
    }
  }

  protected def emitHelper[T](player: MinecraftPlayer[_], next: Skill[T], prev: Option[Skill[_]]): Unit = {
    PlayerSkills
      .emitSkillChanged(player, next, prev.asInstanceOf[Option[Skill[T]]])
  }
}

class TeamOps(
  override val playerOps: Player,
  protected val skillOps: SkillOps,
  override val team: Team,
  override val logger: PlayerSkillsLogger,
) extends TeamUpdater with TeamSkillCalculator with TeamNotifier with TeamLimit {
  def changeSkill[T](player: UUID, skill: Skill[T], value: Option[T]): Option[Skill[T]] = {
    value.orElse(skill.value)
      .pipe(playerOps.calculateValue(player, skill, _))
      .tap(_ => logger.info(s"Changing skill ${skill.name} to $value for $player"))
      .filter(_ => allows(player, skill))
      .tap(a => logger.info(s"Is skill change allowed? $a"))
  }

  private def updateTeamSkill[T](player: MinecraftPlayer[_], skill: Skill[T], emit: Boolean = true): Boolean = {
    team.getTeamMembersFor(player.uuid)
      .flatMap(updateMemberSkill(skill))
      .tap(notifyPlayers(player, skill, emit))
      .nonEmpty
  }

  private def updateSkill[T](player: MinecraftPlayer[_])(skill: Option[Skill[T]]): Option[Boolean] = {
    skill match {
      case Some(s) if s.teamMode == TeamMode.Shared() => Option(updateTeamSkill(player, s))
      case Some(s) => Option(playerOps.upsert(player.uuid, s).nonEmpty)
      case None => None
    }
  }

  def degrade[T](player: MinecraftPlayer[_], skill: Skill[T], min: Option[T], max: Option[T]): Option[Boolean] = {
    skillOps.calculatePrev(skill, min, max)
      .pipe(v => changeSkill(player.uuid, skill, v))
      .pipe(updateSkill[T](player))
  }

  def improve[T](player: MinecraftPlayer[_], skill: Skill[T], min: Option[T], max: Option[T]): Option[Boolean] = {
    skillOps.calculateNext(skill, min, max)
      .tap(_ => logger.info(s"Improving skill ${skill.name} for ${player.name}"))
      .pipe(v => changeSkill(player.uuid, skill, v))
      .pipe(updateSkill[T](player))
  }

  def syncFromPlayer(player: MinecraftPlayer[_]): Boolean = {
    logger.debug(s"Syncing skills from ${player.name}")
    getSharedSkills(player.uuid)
      .map(updateTeamSkill(player, _))
      .forall(_ == true)
  }

  def syncEntireTeam(player: MinecraftPlayer[_]): Boolean = {
    logger.debug(s"Syncing entire team connected to ${player.name}")
    val (maxSkills, offlinePlayers, updated) = updateSkillsForTeam(player.uuid)

    updated
      .filterNot(t => offlinePlayers.contains(t._1))
      .filter(t => t._2.nonEmpty)
      .foreach(t => notifyPlayers(player, t._2.flatMap(s => maxSkills.find(_.name == s.name)).get)(List(t)))

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

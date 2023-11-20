package net.impleri.playerskills.server.api

import net.impleri.playerskills.api.skills.Skill
import net.impleri.playerskills.api.skills.SkillOps
import net.impleri.playerskills.api.skills.TeamMode
import net.impleri.playerskills.facades.minecraft.{Player => MinecraftPlayer}
import net.impleri.playerskills.facades.minecraft.Server
import net.impleri.playerskills.server.EventHandler
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

  protected def skillOps: SkillOps

  protected[api] def getSharedSkills(player: UUID): Seq[Skill[_]] = {
    playerOps.get(player)
      .filter(_.teamMode == TeamMode.Shared())
  }

  private def getMaxSkill[T](players: Seq[UUID], skill: Skill[T]): Option[Skill[T]] = {
    players
      .flatMap(playerOps.get[T](_, skill.name))
      .maxOption(ord = skillOps.sortHelper[T])
  }

  protected[api] def getMaxTeamSkills(players: Seq[UUID])(skills: Seq[Skill[_]]): Seq[Skill[_]] = {
    skills
      .filterNot(_.teamMode == TeamMode.Off())
      .flatMap(getMaxSkill(players, _))
  }
}

trait TeamUpdater {
  protected def logger: PlayerSkillsLogger

  protected def playerOps: Player

  protected def team: Team

  protected def eventHandler: EventHandler

  protected[api] def withFullTeam[T](player: UUID)(f: List[UUID] => T): T = {
    val allPlayers = team.getTeamMembersFor(player)
    val offlinePlayers = playerOps.open(allPlayers)

    val response = f(allPlayers)

    playerOps.close(offlinePlayers)

    response
  }

  protected[api] def updateMemberSkill[T](skill: Skill[T])(playerId: UUID): Option[(UUID, Option[Skill[T]])] = {
    playerOps.get[T](playerId, skill.name)
      .map(s => (playerOps.can(playerId, s, skill.value), s))
      .map(t => (t._2, if (!t._1) playerOps.upsert(playerId, skill) else Seq.empty))
      .flatMap(t => if (t._2.nonEmpty) Some(playerId, Option(t._1)) else None)
  }

  protected[api] def syncSkills(team: Seq[UUID])
    (skills: Seq[Skill[_]]): Seq[(UUID, Option[Skill[_]], Option[Skill[_]])] = {
    skills
      .flatMap(s => team.flatMap(updateMemberSkill(s)))
      .map(t => (t._1, t._2, t._2.flatMap(s => skills.find(_.name == s.name))))
  }

  protected[api] def notifyPlayers[T](server: Server, originalSkill: Skill[T], emit: Boolean = true)(
    updates: List[(UUID, Option[Skill[_]])],
  ): Unit = {
    if (emit) {
      updates
        .filter(t => playerOps.isOnline(t._1))
        .foreach(tuple =>
          server
            .getPlayer(tuple._1)
            .foreach(
              player => {
                eventHandler.emitSkillChanged(player, originalSkill, tuple._2.asInstanceOf[Option[Skill[T]]])
                originalSkill
                  .getNotification(tuple._2.asInstanceOf[Option[Skill[T]]].flatMap(_.value))
                  .foreach(player.sendMessage(_))
              },
            ),
        )
    }
  }
}

trait TeamLimit {
  protected def playerOps: Player

  protected def logger: PlayerSkillsLogger

  private[api] def countWith[T](playerIds: Seq[UUID], skill: Skill[T]): Int = {
    playerIds.map(p => (p, playerOps.get[T](p, skill.name)))
      .flatMap(t => t._2.map(v => (t._1, v)))
      .count(t => playerOps.can(t._1, t._2, skill.value))
  }

  private[api] def getTeamLimit[T](players: Seq[UUID], skill: Skill[T]) = {
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

  protected[api] def allows[T](players: Seq[UUID], skill: Skill[T]): Boolean = {
    (countWith(players, skill), getTeamLimit(players, skill))
      .tap(logger.infoP(t => s"Does the team allow updating skill? (${t._1} < ${t._2})"))
      .pipe(t => t._2.forall(t._1 < _))
  }
}

class TeamOps(
  override val playerOps: Player,
  override val skillOps: SkillOps,
  override val team: Team,
  override val eventHandler: EventHandler,
  override val logger: PlayerSkillsLogger,
) extends TeamUpdater with TeamSkillCalculator with TeamLimit {
  private def calculateNextValue[T](
    player: UUID,
    skill: Skill[T],
    value: Option[T],
    team: Seq[UUID],
  ): Option[Skill[T]] = {
    logger.info(s"Changing skill ${skill.name} to $value for $player")
    playerOps.calculateValue(player, skill, value)
      .filter(_ => allows(team, skill))
      .tap(logger.infoP(a => s"Is skill change allowed? $a"))
  }

  private def updateTeamSkill[T](player: MinecraftPlayer[_], skill: Skill[T], emit: Boolean = true): Boolean = {
    team.getTeamMembersFor(player.uuid)
      .flatMap(updateMemberSkill(skill))
      .tap(notifyPlayers(player.server, skill, emit))
      .nonEmpty
  }

  private def updatePlayerSkill[T](player: MinecraftPlayer[_], skill: Skill[T], emit: Boolean = true): Boolean = {
    updateMemberSkill(skill)(player.uuid)
      .toList
      .tap(notifyPlayers(player.server, skill, emit))
      .nonEmpty
  }

  private def updateSkill[T](player: MinecraftPlayer[_])(skill: Option[Skill[T]]): Option[Boolean] = {
    skill match {
      case Some(s) if s.teamMode == TeamMode.Shared() => Option(updateTeamSkill(player, s))
      case Some(s) => Option(updatePlayerSkill(player, s))
      case None => None
    }
  }

  def degrade[T](
    player: MinecraftPlayer[_],
    skill: Skill[T],
    min: Option[T] = None,
    max: Option[T] = None,
  ): Option[Boolean] = {
    withFullTeam(player.uuid) { team =>
      skillOps.calculatePrev(skill, min, max)
        .pipe(v => calculateNextValue(player.uuid, skill, v, team))
        .pipe(updateSkill[T](player))
    }
  }

  def improve[T](
    player: MinecraftPlayer[_],
    skill: Skill[T],
    min: Option[T] = None,
    max: Option[T] = None,
  ): Option[Boolean] = {
    withFullTeam(player.uuid) { team =>
      skillOps.calculateNext(skill, min, max)
        .tap(_ => logger.info(s"Improving skill ${skill.name} for ${player.name}"))
        .pipe(v => calculateNextValue(player.uuid, skill, v, team))
        .pipe(updateSkill[T](player))
    }
  }

  def syncFromPlayer(player: MinecraftPlayer[_]): Boolean = {
    logger.debug(s"Syncing skills from ${player.name}")
    withFullTeam(player.uuid) { team =>
      getSharedSkills(player.uuid)
        .pipe(syncSkills(team))
        .flatMap(_._2)
        .map(updateTeamSkill(player, _))
        .forall(_ == true)
    }
  }

  def syncEntireTeam(player: MinecraftPlayer[_]): Boolean = {
    logger.debug(s"Syncing entire team connected to ${player.name}")
    val updates = withFullTeam(player.uuid) { team =>
      getSharedSkills(player.uuid)
        .pipe(getMaxTeamSkills(team))
        .pipe(syncSkills(team))
    }
    updates.foreach(t => notifyPlayers(player.server, t._3.get)(List((t._1, t._3))))

    updates.nonEmpty
  }
}

object Team {
  def apply(
    instance: Team = StubTeam(),
    playerOps: Player = Player(),
    skillOps: SkillOps = Skill(),
    eventHandler: EventHandler = EventHandler(),
    logger: PlayerSkillsLogger = PlayerSkillsLogger.SKILLS,
  ): TeamOps = {
    new TeamOps(playerOps, skillOps, instance, eventHandler, logger)
  }
}

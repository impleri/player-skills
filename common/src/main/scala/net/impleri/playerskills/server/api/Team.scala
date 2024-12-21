package net.impleri.playerskills.server.api

import net.impleri.playerskills.api.skills.Skill
import net.impleri.playerskills.api.skills.SkillOps
import net.impleri.playerskills.api.skills.TeamMode
import net.impleri.playerskills.server.EventHandler
import net.impleri.playerskills.utils.PlayerSkillsLogger
import net.impleri.slab.entity.{Player => MinecraftPlayer}
import net.impleri.slab.logging.Logger
import net.impleri.slab.server.Server

import java.util.UUID
import scala.util.chaining.scalaUtilChainingOps

trait Team {
  protected[playerskills] def getTeamMembersFor(player: UUID): List[UUID]

}

case class StubTeam() extends Team {
  override def getTeamMembersFor(player: UUID): List[UUID] = {
    List(player)
  }
}

trait TeamSkillCalculator {
  protected def playerOps: Player

  protected def skillOps: SkillOps

  protected[api] def getSharedSkills(player: UUID): Seq[Skill[_]] =
    playerOps
      .get(player)
      .filter(_.teamMode == TeamMode.Shared())

  private def getMaxSkill[T](
    players: Seq[UUID],
    skill: Skill[T],
  ): Option[Skill[T]] =
    players
      .flatMap(playerOps.get[T](_, skill.name))
      .maxOption(ord = skillOps.sortHelper[T])

  protected[api] def getMaxTeamSkills(
    players: Seq[UUID],
  )(skills: Seq[Skill[_]]): Seq[Skill[_]] =
    skills
      .filterNot(_.teamMode == TeamMode.Off())
      .flatMap(getMaxSkill(players, _))
}

trait TeamUpdater {
  protected def logger: Logger

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

  protected[api] def updateMemberSkill[T](
    skill: Skill[T],
  )(playerId: UUID): Option[(UUID, Option[Skill[T]])] =
    playerOps
      .get[T](playerId, skill.name)
      .tap(
        logger.debugP(o =>
          s"Updating ${skill.name} from ${o.flatMap(_.value)} to ${skill.value} for $playerId",
        ),
      )
      .map(o => (o, playerOps.upsert(playerId, skill)))
      .flatMap(t => if (t._2.nonEmpty) Some(playerId, Option(t._1)) else None)

  protected[api] def syncSkills(
    team: Seq[UUID],
  )(skills: Seq[Skill[_]]): Seq[(UUID, Option[Skill[_]], Option[Skill[_]])] =
    for {
      skill <- skills
      teammate <- team
      (playerId, updatedSkill) <- updateMemberSkill(skill)(teammate)
    } yield (playerId, updatedSkill, Option(skill))

  protected[api] def notifyPlayers[T](
    server: Option[Server],
    originalSkill: Skill[T],
    emit: Boolean = true,
  )(
    updates: List[(UUID, Option[Skill[_]])],
  ): Unit =
    if (emit) {
      for {
        (playerUuid, skill) <- updates.filter(t => playerOps.isOnline(t._1))
        player <- server.flatMap(_.getPlayer(playerUuid))
        castedSkill = skill.asInstanceOf[Option[Skill[T]]]
        _ = eventHandler.emitSkillChanged(player, originalSkill, castedSkill)
        originalValue = castedSkill.flatMap(_.value)
        message <- originalSkill.getNotification(originalValue)
      } yield player.sendMessage(message)
    }
}

trait TeamLimit {
  protected def playerOps: Player

  protected def logger: Logger

  private[api] def countWith[T](playerIds: Seq[UUID], skill: Skill[T]): Int =
    playerIds
      .map(p => (p, playerOps.get[T](p, skill.name)))
      .flatMap(t => t._2.map(v => (t._1, v)))
      .count(t => playerOps.can(t._1, t._2.name, skill.value))

  private[api] def getTeamLimit[T](players: Seq[UUID], skill: Skill[T]) =
    Option(players.size)
      .filter(_ > 1)
      .filter(_ =>
        skill.teamMode match {
          case TeamMode.Off()    => false
          case TeamMode.Shared() => false
          case _                 => true
        },
      )
      .map(skill.teamMode.getLimit(skill, _))

  private def hasMoreAllowedChanges[T](players: Seq[UUID], skill: Skill[T]): Option[Boolean] =
    for {
      limit <- getTeamLimit(players, skill)
      count = countWith(players, skill)
    } yield {
      logger.debug(s"Does the team allow updating skill? ($count < $limit)")
      count < limit
    }

  protected[api] def allows[T](players: Seq[UUID], skill: Skill[T]): Boolean =
    hasMoreAllowedChanges(players, skill).getOrElse(true)
}

case class TeamOps(
  override val playerOps: Player,
  override val skillOps: SkillOps,
  private var teamInstance: Team,
  override val eventHandler: EventHandler,
  override val logger: Logger,
) extends TeamUpdater
    with TeamSkillCalculator
    with TeamLimit {
  override def team: Team = teamInstance

  def changeTeam(next: Team): Unit =
    teamInstance = next

  private def calculateNextValue[T](
    player: UUID,
    skill: Skill[T],
    value: Option[T],
    team: Seq[UUID],
  ): Option[Skill[T]] = {
    logger.debug(
      s"Changing skill ${skill.name} from ${skill.value} to $value for $player.handle",
    )
    playerOps
      .calculateValue(player, skill, value)
      .filter(_ => allows(team, skill))
      .tap(logger.infoP(a => s"Is skill change allowed? $a"))
  }

  private def updateTeamSkill[T](
    player: MinecraftPlayer,
    skill: Skill[T],
    emit: Boolean = true,
  ): Boolean =
    team
      .getTeamMembersFor(player.uuid)
      .flatMap(updateMemberSkill(skill))
      .tap(notifyPlayers(player.server, skill, emit))
      .nonEmpty

  private def updatePlayerSkill[T](
    player: MinecraftPlayer,
    skill: Skill[T],
    emit: Boolean = true,
  ): Boolean =
    updateMemberSkill(skill)(player.uuid)
      .toList
      .tap(notifyPlayers(player.server, skill, emit))
      .nonEmpty

  private def updateSkill[T](
    player: MinecraftPlayer,
  )(skill: Option[Skill[T]]): Option[Boolean] =
    skill match {
      case Some(s) if s.teamMode == TeamMode.Shared() =>
        Option(updateTeamSkill(player, s))
      case Some(s) => Option(updatePlayerSkill(player, s))
      case None    => None
    }

  def change[T](
    player: MinecraftPlayer,
    skill: Skill[T],
  ): Option[Boolean] =
    withFullTeam(player.uuid) { team =>
      for {
        c <- playerOps.get[T](player.uuid, skill.name)
        s = calculateNextValue[T](player.uuid, c, skill.value, team)
        u <- updateSkill[T](player)(s)
      } yield u
    }

  def degrade[T](
    player: MinecraftPlayer,
    skill: Skill[T],
    min: Option[T] = None,
    max: Option[T] = None,
  ): Option[Boolean] =
    withFullTeam(player.uuid) { team =>
      for {
        c <- playerOps.get[T](player.uuid, skill.name)
        v = skillOps.calculatePrev[T](c, min, max)
        s = calculateNextValue[T](player.uuid, c, v, team)
        u <- updateSkill[T](player)(s)
      } yield u
    }

  def improve[T](
    player: MinecraftPlayer,
    skill: Skill[T],
    min: Option[T] = None,
    max: Option[T] = None,
  ): Option[Boolean] =
    withFullTeam(player.uuid) { team =>
      for {
        c <- playerOps.get[T](player.uuid, skill.name)
        v = skillOps.calculateNext[T](c, min, max)
        s = calculateNextValue[T](player.uuid, c, v, team)
        u <- updateSkill[T](player)(s)
      } yield u
    }

  def reset[T](
    player: MinecraftPlayer,
    skill: Skill[T],
  ): Option[Boolean] =
    withFullTeam(player.uuid) { _ =>
      skillOps
        .get[T](skill.name)
        .tap(_ =>
          logger.info(s"Resetting skill ${skill.name} for ${player.handle}"),
        )
        .pipe(updateSkill[T](player))
    }

  def syncFromPlayer(player: MinecraftPlayer): Boolean = {
    logger.info(s"Syncing skills from ${player.handle} to team")
    withFullTeam(player.uuid) { team =>
      getSharedSkills(player.uuid)
        .pipe(syncSkills(team))
        .flatMap(_._2)
        .map(updateTeamSkill(player, _))
        .forall(_ == true)
    }
  }

  def syncEntireTeam(player: MinecraftPlayer): Boolean = {
    logger.info(s"Syncing entire team connected to ${player.handle}")

    val updates = withFullTeam(player.uuid) { team =>
      getSharedSkills(player.uuid)
        .pipe(getMaxTeamSkills(team))
        .pipe(syncSkills(team))
    }

    updates.foreach(t =>
      notifyPlayers(player.server, t._3.get)(List((t._1, t._3))),
    )

    updates.nonEmpty
  }
}

object Team {
  def apply(
    instance: Team = StubTeam(),
    playerOps: Player = Player(),
    skillOps: SkillOps = Skill(),
    eventHandler: EventHandler = EventHandler(),
    logger: Logger = PlayerSkillsLogger.SKILLS,
  ): TeamOps =
    TeamOps(playerOps, skillOps, instance, eventHandler, logger)
}

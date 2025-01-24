package net.impleri.playerskills.server.commands

import net.impleri.playerskills.api.skills.Skill
import net.impleri.playerskills.api.skills.SkillOps
import net.impleri.playerskills.api.skills.SkillType
import net.impleri.playerskills.api.skills.SkillTypeOps
import net.impleri.playerskills.restrictions.RestrictionRegistry
import net.impleri.playerskills.server.api.Player
import net.impleri.playerskills.server.api.Team
import net.impleri.playerskills.server.api.TeamOps
import net.impleri.slab.commands.BaseCommand
import net.impleri.slab.commands.CommandSegment
import net.impleri.slab.commands.CommandString

case class PlayerSkillsCommands(
  override val skillOps: SkillOps,
  override val skillTypeOps: SkillTypeOps,
  override val playerOps: Player,
  override val teamOps: TeamOps,
  override val restrictions: RestrictionRegistry,
) extends BaseCommand
    with ListTypesCommand
    with ListSkillsCommand
    with ListAcquiredCommand
    with SkillValueCommand
    with SyncTeamCommands
    with DebugCommands
    with SetSkillCommand
    with ImproveSkillCommand
    with DegradeSkillCommand
    with ResetSkillCommand
    with ListRestrictionsCommand {
  protected def builders[T <: CommandSegment.Any]: List[T => T] =
    List(
      registerTypesCommand,
      registerAllCommand,
      registerMineCommand,
      registerValueCommand,
      registerTeamCommands,
      registerRestrictionsCommand,
      registerSetCommand,
      registerImproveCommand,
      registerDegradeCommand,
      registerResetCommand,
      registerDebugCommands,
    )

  val command: CommandString = buildCommands(CommandString("skills"))
}

object PlayerSkillsCommands {
  def apply(
    skillOps: SkillOps = Skill(),
    skillTypeOps: SkillTypeOps = SkillType(),
    playerOps: Player = Player(),
    teamOps: TeamOps = Team(),
    restrictions: RestrictionRegistry = RestrictionRegistry(),
  ): PlayerSkillsCommands =
    new PlayerSkillsCommands(
      skillOps,
      skillTypeOps,
      playerOps,
      teamOps,
      restrictions,
    )
}

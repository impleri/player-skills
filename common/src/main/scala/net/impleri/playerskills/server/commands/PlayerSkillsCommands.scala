package net.impleri.playerskills.server.commands

import com.mojang.brigadier.tree.LiteralCommandNode
import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import net.impleri.playerskills.api.skills.Skill
import net.impleri.playerskills.api.skills.SkillOps
import net.impleri.playerskills.api.skills.SkillType
import net.impleri.playerskills.api.skills.SkillTypeOps
import net.impleri.playerskills.server.api.Player
import net.impleri.playerskills.server.api.Team
import net.impleri.playerskills.server.api.TeamOps
import net.impleri.playerskills.utils.PlayerSkillsLogger
import net.minecraft.commands.Commands
import net.minecraft.commands.CommandSourceStack

case class PlayerSkillsCommands(
  override val skillOps: SkillOps,
  override val skillTypeOps: SkillTypeOps,
  override val playerOps: Player,
  override val teamOps: TeamOps,
  override val logger: PlayerSkillsLogger = PlayerSkillsLogger.SKILLS,
  override val blockLogger: PlayerSkillsLogger = PlayerSkillsLogger.BLOCKS,
  override val fluidLogger: PlayerSkillsLogger = PlayerSkillsLogger.FLUIDS,
  override val itemLogger: PlayerSkillsLogger = PlayerSkillsLogger.ITEMS,
  override val mobLogger: PlayerSkillsLogger = PlayerSkillsLogger.MOBS,
) extends ListTypesCommand
  with ListSkillsCommand
  with ListAcquiredCommand
  with SkillValueCommand
  with SyncTeamCommands
  with DebugCommands
  with SetSkillCommand
  with ImproveSkillCommand
  with DegradeSkillCommand {

  def register(
    dispatcher: CommandDispatcher[CommandSourceStack],
  ): LiteralCommandNode[CommandSourceStack] = {
    dispatcher.register(buildCommands(Commands.literal("skills")))
  }

  private val builders: List[Function[LiteralArgumentBuilder[CommandSourceStack], LiteralArgumentBuilder[CommandSourceStack]]] =
    List(
      registerTypesCommand,
      registerAllCommand,
      registerMineCommand,
      registerValueCommand,
      registerTeamCommands,
      registerSetCommand,
      registerImproveCommand,
      registerDegradeCommand,
      registerDebugCommands,
    )

  private def buildCommands = Function.chain(builders)
}

object PlayerSkillsCommands {
  def apply(
    skillOps: SkillOps = Skill(),
    skillTypeOps: SkillTypeOps = SkillType(),
    playerOps: Player = Player(),
    teamOps: TeamOps = Team(),
    logger: PlayerSkillsLogger = PlayerSkillsLogger.SKILLS,
    blockLogger: PlayerSkillsLogger = PlayerSkillsLogger.BLOCKS,
    fluidLogger: PlayerSkillsLogger = PlayerSkillsLogger.FLUIDS,
    itemLogger: PlayerSkillsLogger = PlayerSkillsLogger.ITEMS,
    mobLogger: PlayerSkillsLogger = PlayerSkillsLogger.MOBS,
  ): PlayerSkillsCommands = {
    new PlayerSkillsCommands(
      skillOps,
      skillTypeOps,
      playerOps,
      teamOps,
      logger,
      blockLogger,
      fluidLogger,
      itemLogger,
      mobLogger,
    )
  }
}

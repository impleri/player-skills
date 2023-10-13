package net.impleri.playerskills.commands

import com.mojang.brigadier.tree.LiteralCommandNode
import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import net.minecraft.commands.Commands
import net.minecraft.commands.CommandSourceStack

object PlayerSkillsCommands extends ListTypesCommand
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

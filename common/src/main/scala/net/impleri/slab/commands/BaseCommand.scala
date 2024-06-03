package net.impleri.slab.commands

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.tree.LiteralCommandNode

trait BaseCommand {
  def command: CommandString

  protected def builders: List[CommandSegment.Any => CommandSegment.Any]

  protected def buildCommands: CommandSegment.Any => CommandSegment.Any = Function.chain(builders)

  def register(
    dispatcher: CommandDispatcher[Command.Source],
  ): LiteralCommandNode[Command.Source] = {
    dispatcher.register(command.asRoot)
  }
}

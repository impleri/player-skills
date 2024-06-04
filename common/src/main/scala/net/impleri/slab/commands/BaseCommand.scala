package net.impleri.slab.commands

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.tree.LiteralCommandNode

trait BaseCommand {
  def command: CommandString

  protected def builders[T <: CommandSegment.Any]: List[T => T]

  protected def buildCommands[T <: CommandSegment.Any]: T => T = Function.chain(builders)

  def register(
    dispatcher: CommandDispatcher[Command.Source],
  ): LiteralCommandNode[Command.Source] = {
    dispatcher.register(command.asRoot)
  }
}

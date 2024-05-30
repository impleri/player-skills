package net.impleri.slab.commands

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.tree.LiteralCommandNode
import net.minecraft.commands.CommandSourceStack

trait BaseCommand {
  def command: CommandString

  protected def builders: List[Function[CommandSegment.Any, CommandSegment.Any]]

  protected def buildCommands: CommandString => CommandString = Function.chain(builders)

  def register(
    dispatcher: CommandDispatcher[CommandSourceStack],
  ): LiteralCommandNode[CommandSourceStack] = {
    dispatcher.register(command.asRoot)
  }
}

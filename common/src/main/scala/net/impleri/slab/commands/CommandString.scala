package net.impleri.slab.commands

import net.minecraft.commands.Commands

class CommandString(override val underlying: TextCommand)
  extends CommandSegment[TextCommand, CommandString](underlying) {
  def asRoot: RootCommand = {
    underlying.asInstanceOf[RootCommand]
  }
}

object CommandString {
  def apply(segment: String): CommandString = new CommandString(Commands.literal(segment))
}

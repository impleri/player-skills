package net.impleri.slab.commands

import com.mojang.brigadier.arguments.StringArgumentType
import net.minecraft.commands.Commands

class StringArgument(override val underlying: CommandArgument[StringArgumentType])
  extends CommandSegment[CommandArgument[StringArgumentType], StringArgument](underlying)

object StringArgument {
  def apply(name: String): StringArgument = new StringArgument(Commands.argument(name, StringArgumentType.string()))
}

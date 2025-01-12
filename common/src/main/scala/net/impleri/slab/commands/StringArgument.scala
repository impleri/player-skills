package net.impleri.slab.commands

import com.mojang.brigadier.arguments.StringArgumentType
import net.minecraft.commands.Commands

class StringArgument(override val underlying: Command.Argument[String])
    extends CommandSegment[Command.Argument[String], StringArgument](underlying)

object StringArgument extends ArgumentUtils {
  def apply(name: String): StringArgument = new StringArgument(
    Commands.argument(name, StringArgumentType.string()),
  )

  def getValue(name: String, context: Command.Context): Option[String] =
    wrapParser[String, String]("entity", name, identity) {
      StringArgumentType.getString(context, name)
    }
}

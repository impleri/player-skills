package net.impleri.slab.commands

import net.impleri.slab.entity.Player
import net.minecraft.commands.Commands
import net.minecraft.commands.arguments.{EntityArgument => McEntityArgument}
import net.minecraft.commands.arguments.selector.EntitySelector

import scala.util.Try

class PlayerArgument(
  override val underlying: Command.Argument[EntityArgument.Vanilla],
) extends CommandSegment[Command.Argument[EntitySelector], PlayerArgument](
      underlying,
    )

object PlayerArgument {
  private final val DEFAULT_ARGUMENT = "player"

  def apply(name: String = DEFAULT_ARGUMENT): PlayerArgument =
    new PlayerArgument(
      Commands.argument(name, McEntityArgument.player()),
    )

  def getValue(
    context: Command.Context,
    name: String = DEFAULT_ARGUMENT,
  ): Option[Player] =
    Try(McEntityArgument.getPlayer(context, name)).toOption
      .map(Player(_))
}

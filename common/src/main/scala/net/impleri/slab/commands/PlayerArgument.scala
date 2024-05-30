package net.impleri.slab.commands

import net.minecraft.commands.Commands
import net.minecraft.commands.arguments.EntityArgument

class PlayerArgument(override val underlying: CommandArgument[EntityArgument])
  extends CommandSegment[CommandArgument[EntityArgument], PlayerArgument](underlying)

object PlayerArgument {
  def apply(): PlayerArgument = new PlayerArgument(Commands.argument("player", EntityArgument.player()))
}

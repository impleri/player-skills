package net.impleri.slab.commands

import net.minecraft.commands.Commands
import net.minecraft.commands.arguments.{ResourceLocationArgument => McResourceLocationArgument}

class ResourceLocationArgument(override val underlying: CommandArgument[McResourceLocationArgument])
  extends CommandSegment[CommandArgument[McResourceLocationArgument], ResourceLocationArgument](underlying)

object ResourceLocationArgument {
  def apply(name: String): ResourceLocationArgument = {
    new ResourceLocationArgument(Commands
      .argument(name, McResourceLocationArgument.id()),
    )
  }
}

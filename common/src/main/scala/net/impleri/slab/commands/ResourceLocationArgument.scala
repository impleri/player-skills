package net.impleri.slab.commands

import net.impleri.slab.resources.ResourceLocation
import net.minecraft.commands.Commands
import net.minecraft.commands.arguments.{ResourceLocationArgument => McResourceLocationArgument}

import scala.util.Try

class ResourceLocationArgument(override val underlying: Command.Argument[ResourceLocation.Vanilla])
  extends CommandSegment[Command.Argument[ResourceLocation.Vanilla], ResourceLocationArgument](underlying)

object ResourceLocationArgument {
  def apply(name: String): ResourceLocationArgument = {
    new ResourceLocationArgument(Commands
      .argument(name, McResourceLocationArgument.id()),
    )
  }

  def getValue(name: String, context: Command.Context): Option[ResourceLocation] = {
    Try(McResourceLocationArgument.getId(context, name))
      .toOption
      .flatMap(ResourceLocation(_))
  }
}

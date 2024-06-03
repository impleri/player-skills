package net.impleri.slab.commands

import net.impleri.slab.entity.Entity
import net.minecraft.commands.Commands
import net.minecraft.commands.arguments.{EntityArgument => McEntityArgument}
import net.minecraft.commands.arguments.selector.EntitySelector

import scala.util.Try

class EntityArgument(override val underlying: Command.Argument[EntityArgument.Vanilla])
  extends CommandSegment[Command.Argument[EntityArgument.Vanilla], EntityArgument](underlying)

object EntityArgument {
  type Vanilla = EntitySelector

  private final val DEFAULT_ARGUMENT = "target"

  def apply(name: String = DEFAULT_ARGUMENT): EntityArgument = new EntityArgument(
    Commands.argument(name, McEntityArgument.entity()),
  )

  def getValue(context: Command.Context, name: String = DEFAULT_ARGUMENT): Option[Entity.Any] = {
    Try(McEntityArgument.getEntity(context, name))
      .toOption
      .map(Entity(_))
  }
}

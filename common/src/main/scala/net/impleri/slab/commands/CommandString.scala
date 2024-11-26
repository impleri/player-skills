package net.impleri.slab.commands

import com.mojang.brigadier.builder.LiteralArgumentBuilder
import net.minecraft.commands.Commands

class CommandString(override val underlying: CommandString.Vanilla)
    extends CommandSegment[CommandString.Vanilla, CommandString](underlying) {
  def asRoot: CommandString.Root =
    underlying.asInstanceOf[CommandString.Root]

  override def copyAs(nextUnderlying: CommandString.Vanilla): CommandString =
    new CommandString(
      nextUnderlying,
    )
}

object CommandString {
  type Vanilla = CommandSegment.Vanilla[LiteralArgumentBuilder[Command.Source]]

  private type Root = LiteralArgumentBuilder[Command.Source]

  def apply(segment: String): CommandString = new CommandString(
    Commands.literal(segment),
  )
}

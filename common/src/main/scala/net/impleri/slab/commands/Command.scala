package net.impleri.slab.commands

import com.mojang.brigadier.builder.RequiredArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import net.minecraft.commands.CommandSourceStack

object Command {
  type Argument[T] = CommandSegment.Vanilla[RequiredArgumentBuilder[Source, T]]
  type Context = CommandContext[Source]
  type Source = CommandSourceStack
}

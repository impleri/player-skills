package net.impleri.slab

import com.mojang.brigadier.Command
import com.mojang.brigadier.builder.ArgumentBuilder
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.builder.RequiredArgumentBuilder
import com.mojang.brigadier.context.{CommandContext => McCommandContext}
import net.impleri.slab.chat.Message
import net.minecraft.commands.CommandSourceStack

package object commands {
  type CommandBuilder[T <: ArgumentBuilder[CommandSourceStack, T]] = ArgumentBuilder[CommandSourceStack, T]
  type CommandContext = McCommandContext[CommandSourceStack]
  type CommandExecution = Command[CommandSourceStack]
  type CommandFilter = CommandSourceStack => Boolean
  type CommandCallback = CommandContext => Either[Message[_], Message[_]]

  type TextCommand = CommandBuilder[LiteralArgumentBuilder[CommandSourceStack]]
  type CommandArgument[T] = CommandBuilder[RequiredArgumentBuilder[CommandSourceStack, T]]
  type RootCommand = LiteralArgumentBuilder[CommandSourceStack]
}

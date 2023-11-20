package net.impleri.playerskills.server.commands

import com.mojang.brigadier.builder.RequiredArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import net.impleri.playerskills.facades.minecraft.Player
import net.minecraft.commands.Commands
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.arguments.EntityArgument
import net.minecraft.commands.arguments.selector.EntitySelector
import net.minecraft.server.level.ServerPlayer

import scala.util.Try

trait WithPlayer {
  protected def getPlayerArg: RequiredArgumentBuilder[CommandSourceStack, EntitySelector] = {
    Commands
      .argument("player", EntityArgument.player())
  }

  protected def getPlayer(context: CommandContext[CommandSourceStack]): Option[Player[ServerPlayer]] = {
    Try(context.getSource.getPlayerOrException)
      .toOption
      .map(Player(_))
  }
}

package net.impleri.playerskills.server.commands

import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.Command
import net.impleri.playerskills.facades.minecraft.Player
import net.minecraft.commands.CommandSourceStack
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer

trait CommandHelpers {
  protected val MOD = 1

  protected val GAMEMASTER = 2

  protected val ADMIN = 3

  protected val OWNER = 4

  protected def hasPermission(permission: Int = MOD): CommandSourceStack => Boolean = {
    (source: CommandSourceStack) =>
    source
      .hasPermission(permission)
  }

  protected def getCurrentPlayer(source: CommandSourceStack): Player[ServerPlayer] = Player(source.getPlayer)

  protected def withCurrentPlayer[T](f: Player[ServerPlayer] => T): CommandSourceStack => T = {
    (source: CommandSourceStack) =>
    f(getCurrentPlayer(
      source,
    ),
    )
  }

  protected def withSuccessMessage(f: () => Component): Command[CommandSourceStack] = {
    (context: CommandContext[CommandSourceStack]) => {
      context.getSource.sendSuccess(f(), false)

      Command.SINGLE_SUCCESS
    }
  }
}

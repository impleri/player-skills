package net.impleri.playerskills.server.commands

import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.Command
import net.impleri.playerskills.facades.minecraft.Player
import net.minecraft.commands.CommandSourceStack
import net.minecraft.network.chat.Component

trait CommandHelpers {
  protected val MOD = 1

  protected val GAMEMASTER = 2

  protected val ADMIN = 3

  protected val OWNER = 4

  protected[commands] def hasPermission(permission: Int = MOD): CommandSourceStack => Boolean = {
    (source: CommandSourceStack) => source.hasPermission(permission)
  }

  protected[commands] def getCurrentPlayer(source: CommandSourceStack): Player[_] = {
    Player(source.getPlayer)
  }

  protected def withCurrentPlayer[T](f: Player[_] => T): CommandSourceStack => T = {
    (source: CommandSourceStack) => f(getCurrentPlayer(source))
  }

  protected[commands] def withCurrentPlayerCommand(f: Player[_] => Int): Command[CommandSourceStack] = {
    (context: CommandContext[CommandSourceStack]) => withCurrentPlayer(f)(context.getSource)
  }

  protected[commands] def withSuccessMessage(f: () => Component): Command[CommandSourceStack] = {
    (context: CommandContext[CommandSourceStack]) => {
      context.getSource.sendSuccess(f(), false)

      Command.SINGLE_SUCCESS
    }
  }
}

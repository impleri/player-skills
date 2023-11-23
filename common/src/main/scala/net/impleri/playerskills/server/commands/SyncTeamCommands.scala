package net.impleri.playerskills.server.commands

import com.mojang.brigadier.Command
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import net.impleri.playerskills.facades.minecraft.Player
import net.impleri.playerskills.server.api.TeamOps
import net.minecraft.commands.Commands
import net.minecraft.commands.CommandSourceStack

import scala.jdk.FunctionConverters.enrichAsJavaPredicate

trait SyncTeamCommands extends CommandHelpers with WithPlayer {
  protected def teamOps: TeamOps

  protected def registerTeamCommands(builder: LiteralArgumentBuilder[CommandSourceStack]): LiteralArgumentBuilder[CommandSourceStack] = {
    builder.`then`(
      Commands.literal("team")
        .`then`(
          Commands.literal("share")
            .executes(withCurrentPlayerCommand(syncToTeam)),
        )
        .`then`(
          Commands.literal("sync")
            .requires(hasPermission(GAMEMASTER).asJavaPredicate)
            .`then`(getPlayerArg.executes(c => syncTeamFor(getPlayer(c)))),
        ),
    )
  }

  private[commands] def syncTeamFor(player: Option[Player[_]]): Int = {
    player.map(teamOps.syncEntireTeam).fold(2)(s => if (s) Command.SINGLE_SUCCESS else 3)
  }

  private[commands] def syncToTeam(player: Player[_]): Int = {
    if (teamOps.syncFromPlayer(player)) Command.SINGLE_SUCCESS else 3
  }
}

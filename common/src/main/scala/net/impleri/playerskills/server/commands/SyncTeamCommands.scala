package net.impleri.playerskills.server.commands

import net.impleri.playerskills.server.api.TeamOps
import net.impleri.slab.chat.StaticText
import net.impleri.slab.commands.CommandAction
import net.impleri.slab.commands.CommandCallback
import net.impleri.slab.commands.CommandSegment
import net.impleri.slab.commands.CommandString
import net.impleri.slab.commands.PlayerArgument

trait SyncTeamCommands {
  protected def teamOps: TeamOps

  protected def registerTeamCommands(builder: CommandSegment.Any): CommandSegment.Any = {
    builder.option(
      CommandString("team")
        .option(
          CommandString("share").executes(CommandAction(syncToTeam)),
        )
        .option(
          CommandString("sync")
            .requireGm()
            .option(PlayerArgument().executes(CommandAction(syncTeamForPlayer))),
        ),
    )
  }

  private[commands] val syncTeamForPlayer: CommandCallback = {
    context => {
      CommandAction.getPlayerArgument(context)
        .map(teamOps.syncEntireTeam)
        .toRight(StaticText("Player Not Found"))
        .filterOrElse(_ == true, StaticText("Sync Failed"))
        .map(_ => StaticText("Success"))
    }
  }

  private[commands] val syncToTeam: CommandCallback = {
    context => {
      CommandAction.getCurrentPlayer(context)
        .map(teamOps.syncFromPlayer)
        .toRight(StaticText("Player Not Found"))
        .filterOrElse(_ == true, StaticText("Sync Failed"))
        .map(_ => StaticText("Success"))
    }
  }
}

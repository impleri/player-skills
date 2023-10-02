package net.impleri.playerskills.commands

import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.Command
import net.impleri.playerskills.api.skills.Team
import net.minecraft.commands.arguments.EntityArgument
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands
import net.minecraft.server.level.ServerPlayer

import scala.util.Try

trait SyncTeamCommands {
  private val REQUIRED_PERMISSION = 2

  protected def registerTeamCommands(builder: LiteralArgumentBuilder[CommandSourceStack]): LiteralArgumentBuilder[CommandSourceStack] =
    builder.`then`(
      Commands.literal("team")
        .`then`(
          Commands
            .literal("share")
            .executes(c => syncToTeam(Try(c.getSource.getPlayerOrException).toOption)),
        )
        .`then`(
          Commands.literal("sync")
            .requires(c => c.hasPermission(REQUIRED_PERMISSION))
            .`then`(
              Commands.argument("player", EntityArgument.player())
                .executes(c => syncTeamFor(Option(EntityArgument.getPlayer(c, "player")))),
            )
        )
    )

  private def syncTeamFor(player: Option[ServerPlayer]): Int =
    player
      .map(Team.syncEntireTeam)
      .map(s => if (s) Command.SINGLE_SUCCESS else 3)
      .getOrElse(2)


  private def syncToTeam(player: Option[ServerPlayer]): Int =
    player
      .map(Team.syncFromPlayer)
      .map(s => if (s) Command.SINGLE_SUCCESS else 3)
      .getOrElse(2)
}
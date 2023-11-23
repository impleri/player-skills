package net.impleri.playerskills.server.commands

import com.mojang.brigadier.builder.LiteralArgumentBuilder
import net.impleri.playerskills.facades.minecraft.{Player => MinecraftPlayer}
import net.impleri.playerskills.server.api.Player
import net.minecraft.commands.Commands
import net.minecraft.commands.CommandSourceStack
import net.minecraft.network.chat.Component

trait ListAcquiredCommand extends ValuesCommandUtils with CommandHelpers {
  protected def playerOps: Player

  protected def registerMineCommand(builder: LiteralArgumentBuilder[CommandSourceStack]): LiteralArgumentBuilder[CommandSourceStack] = {
    builder.`then`(
      Commands.literal("mine").executes(withListValuesSource(withCurrentPlayer(listOwnSkills))),
    )
  }

  private[commands] def listOwnSkills(player: MinecraftPlayer[_]): (Component, List[String]) = {
    val acquiredSkills = playerOps.get(player).filter(playerOps.can(player.uuid, _))
    val message = if (acquiredSkills.nonEmpty) {
      Component.translatable("commands.playerskills.acquired_skills", acquiredSkills.size)
    } else {
      Component.translatable("commands.playerskills.no_acquired_skills")
    }

    (message, acquiredSkills.map(s => s"${s.name} = ${s.value.getOrElse("None")}"))
  }
}

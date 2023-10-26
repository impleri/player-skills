package net.impleri.playerskills.commands

import com.mojang.brigadier.builder.LiteralArgumentBuilder
import net.impleri.playerskills.server.api.Player
import net.minecraft.commands.Commands
import net.minecraft.commands.CommandSourceStack
import net.minecraft.network.chat.Component

trait ListAcquiredCommand extends ValuesCommandUtils {
  protected def registerMineCommand(builder: LiteralArgumentBuilder[CommandSourceStack]): LiteralArgumentBuilder[CommandSourceStack] = {
    builder.`then`(
      Commands
        .literal("mine")
        .executes(c => listOwnSkills(c.getSource)),
    )
  }

  private def listOwnSkills(source: CommandSourceStack): Int = {
    val player = Player()
    val acquiredSkills = player.get(source.getPlayer).filter(player.can(source.getPlayer.getUUID, _))
    val message = if (acquiredSkills.nonEmpty) {
      Component
        .translatable("commands.playerskills.acquired_skills", acquiredSkills.size)
    } else {
      Component
        .translatable("commands.playerskills.no_acquired_skills")
    }
    listValues(source, message, acquiredSkills.map(s => s"${s.name} = ${s.value.getOrElse("None")}"))
  }
}

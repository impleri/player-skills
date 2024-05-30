package net.impleri.playerskills.server.commands

import net.impleri.playerskills.server.api.Player
import net.impleri.slab.chat.ListMessage
import net.impleri.slab.chat.StaticText
import net.impleri.slab.chat.TranslatableText
import net.impleri.slab.commands.CommandAction
import net.impleri.slab.commands.CommandCallback
import net.impleri.slab.commands.CommandSegment
import net.impleri.slab.commands.CommandString
import net.impleri.slab.entity.{Player => MinecraftPlayer}

trait ListAcquiredCommand {
  protected def playerOps: Player

  protected def registerMineCommand(builder: CommandSegment.Any): CommandSegment.Any = {
    builder.option(CommandString("mine").executes(CommandAction(handler).message()))
  }

  protected val handler: CommandCallback = {
    context => {
      CommandAction
        .getCurrentPlayer(context)
        .map(getPlayerSkills)
        .toRight(StaticText(""))
    }
  }

  private[commands] def getPlayerSkills(player: MinecraftPlayer.Any): ListMessage = {
    val acquiredSkills = playerOps.get(player).filter(s => playerOps.can(player.uuid, s.name))
    val message = if (acquiredSkills.nonEmpty) {
      TranslatableText("commands.playerskills.acquired_skills", acquiredSkills.size)
    } else {
      TranslatableText("commands.playerskills.no_acquired_skills")
    }

    val children = acquiredSkills.map(s => s"${s.name} = ${s.value.getOrElse("None")}").map(StaticText(_))

    ListMessage(message, children)
  }
}

package net.impleri.playerskills.server.commands

import net.impleri.playerskills.server.api.Player
import net.impleri.slab.chat.ListMessage
import net.impleri.slab.chat.StaticText
import net.impleri.slab.chat.TranslatableText
import net.impleri.slab.commands.CommandAction
import net.impleri.slab.commands.CommandSegment
import net.impleri.slab.commands.CommandString
import net.impleri.slab.commands.PlayerArgument

trait SkillValueCommand {
  protected def playerOps: Player

  protected def registerValueCommand[T <: CommandSegment.Any](builder: T): T = {
    builder
      .option(
        CommandString("value")
          .option(
            PlayerArgument()
              .requireMod()
              .option(
                SkillHandler.getArgument.executes(CommandAction(handler())),
              ),
          )
          .option(
            SkillHandler.getArgument.executes(CommandAction(handler(true))),
          ),
      )
      .asInstanceOf[T]
  }

  private def handler(
    useCurrentUser: Boolean = false,
  ): CommandAction.Callback = { context =>
    {
      val player = CommandAction.getPlayer(context, useCurrentUser)
      val skillName = SkillHandler.getValue(context)

      player
        .flatMap(p => skillName.map(playerOps.get(p, _)))
        .toRight(
          TranslatableText(
            "commands.playerskills.skill_not_found",
            skillName.fold("")(_.asString),
          ),
        )
        .filterOrElse(
          _.nonEmpty,
          TranslatableText("commands.playerskills.no_acquired_skills"),
        )
        .map(_.toSeq)
        .map(_.map(s => s"${s.name} = ${s.value.getOrElse("None")}"))
        .map(_.map(StaticText(_)))
        .map(
          ListMessage(
            TranslatableText("commands.playerskills.acquired_skills", 1),
            _,
          ),
        )
    }
  }
}

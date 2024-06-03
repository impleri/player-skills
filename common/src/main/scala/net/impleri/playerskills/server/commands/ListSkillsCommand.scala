package net.impleri.playerskills.server.commands

import net.impleri.playerskills.api.skills.SkillOps
import net.impleri.slab.chat.ListMessage
import net.impleri.slab.chat.StaticText
import net.impleri.slab.chat.TranslatableText
import net.impleri.slab.commands.CommandAction
import net.impleri.slab.commands.CommandSegment
import net.impleri.slab.commands.CommandString

trait ListSkillsCommand {
  protected def skillOps: SkillOps

  protected def registerAllCommand(builder: CommandSegment.Any): CommandSegment.Any = {
    builder.option(CommandString("all").executes(CommandAction(handler).message()))
  }

  private val handler: CommandAction.Callback = {
    _ => {
      val skills = skillOps.all()
      val message = if (skills.nonEmpty) {
        TranslatableText("commands.playerskills.registered_skills", skills.size)
      } else {
        TranslatableText("commands.playerskills.no_registered_skills")
      }

      val children = skills
        .map(_.name)
        .map(_.asString)
        .map(StaticText(_))

      Right(ListMessage(message, children))
    }
  }
}

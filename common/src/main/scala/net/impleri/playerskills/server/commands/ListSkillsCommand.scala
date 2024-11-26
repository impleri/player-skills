package net.impleri.playerskills.server.commands

import net.impleri.playerskills.api.skills.SkillOps
import net.impleri.slab.chat.ListMessage
import net.impleri.slab.chat.TranslatableText
import net.impleri.slab.commands.CommandAction
import net.impleri.slab.commands.CommandSegment
import net.impleri.slab.commands.CommandString

trait ListSkillsCommand extends WithNames {
  protected def skillOps: SkillOps

  protected def registerAllCommand[T <: CommandSegment.Any](builder: T): T =
    builder
      .option(CommandString("all").executes(CommandAction(handler).message()))
      .asInstanceOf[T]

  private val handler: CommandAction.Callback = _ =>
    {
      val skills = skillOps.all()
      val message = if (skills.nonEmpty) {
        TranslatableText("commands.playerskills.registered_skills", skills.size)
      } else {
        TranslatableText("commands.playerskills.no_registered_skills")
      }

      Right(ListMessage(message, renderNames(skills)))
    }
}

package net.impleri.playerskills.server.commands

import net.impleri.playerskills.api.skills.SkillTypeOps
import net.impleri.slab.chat.ListMessage
import net.impleri.slab.chat.StaticText
import net.impleri.slab.chat.TranslatableText
import net.impleri.slab.commands.CommandAction
import net.impleri.slab.commands.CommandSegment
import net.impleri.slab.commands.CommandString

trait ListTypesCommand {
  protected def skillTypeOps: SkillTypeOps

  protected def registerTypesCommand[T <: CommandSegment.Any](builder: T): T = {
    builder.option(CommandString("types").executes(CommandAction(handler).message())).asInstanceOf[T]
  }

  private val handler: CommandAction.Callback = {
    _ => {
      val types = skillTypeOps.all()
      val message = if (types.nonEmpty) {
        TranslatableText("commands.playerskills.registered_types", types.size)
      } else {
        TranslatableText("commands.playerskills.no_registered_types")
      }

      val children = types
        .map(_.name)
        .map(_.asString)
        .map(StaticText(_))

      Right(ListMessage(message, children))
    }
  }
}

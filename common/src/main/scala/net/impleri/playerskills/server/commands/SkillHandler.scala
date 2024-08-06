package net.impleri.playerskills.server.commands

import net.impleri.slab.commands.Command
import net.impleri.slab.commands.ResourceLocationArgument
import net.impleri.slab.resources.ResourceLocation

object SkillHandler {
  def getArgument: ResourceLocationArgument = ResourceLocationArgument("skill")

  def getValue(context: Command.Context): Option[ResourceLocation] =
    ResourceLocationArgument.getValue("skill", context)
}

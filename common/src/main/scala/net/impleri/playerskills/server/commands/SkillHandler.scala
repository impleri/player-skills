package net.impleri.playerskills.server.commands

import net.impleri.slab.commands.CommandAction
import net.impleri.slab.commands.CommandContext
import net.impleri.slab.commands.ResourceLocationArgument
import net.impleri.slab.resources.ResourceLocation

object SkillHandler {
  def getArgument: ResourceLocationArgument = ResourceLocationArgument("skill")

  def getValue(context: CommandContext): Option[ResourceLocation] = CommandAction.getResourceLocation("skill", context)
}

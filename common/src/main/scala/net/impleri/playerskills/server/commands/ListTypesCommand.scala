package net.impleri.playerskills.server.commands

import com.mojang.brigadier.builder.LiteralArgumentBuilder
import net.impleri.playerskills.api.skills.SkillTypeOps
import net.minecraft.commands.Commands
import net.minecraft.commands.CommandSourceStack
import net.minecraft.network.chat.Component

trait ListTypesCommand extends ValuesCommandUtils {
  protected def skillTypeOps: SkillTypeOps

  protected def registerTypesCommand(builder: LiteralArgumentBuilder[CommandSourceStack]): LiteralArgumentBuilder[CommandSourceStack] = {
    builder.`then`(
      Commands.literal("types").executes(withListValues(listTypes)),
    )
  }

  private[commands] def listTypes(): (Component, List[String]) = {
    val types = skillTypeOps.all()
    val message = if (types.nonEmpty) {
      Component.translatable("commands.playerskills.registered_types", types.size)
    } else {
      Component.translatable("commands.playerskills.no_registered_types")
    }

    (message, types.map(_.name.toString))
  }
}

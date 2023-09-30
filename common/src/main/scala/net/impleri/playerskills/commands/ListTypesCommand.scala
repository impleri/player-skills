package net.impleri.playerskills.commands

import com.mojang.brigadier.builder.LiteralArgumentBuilder
import net.impleri.playerskills.api.skills.SkillType
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands
import net.minecraft.network.chat.Component

trait ListTypesCommand extends ValuesCommandUtils {
  protected def registerTypesCommand(builder: LiteralArgumentBuilder[CommandSourceStack]): LiteralArgumentBuilder[CommandSourceStack] =
    builder
      .`then`(
        Commands
          .literal("types")
          .executes(c => listTypes(c.getSource))
      )

  private def listTypes(source: CommandSourceStack): Int = {
    val types = SkillType.all()
    val message = if (types.nonEmpty) Component.translatable("commands.playerskills.registered_types", types.size) else Component.translatable("commands.playerskills.no_registered_types")

    listValues(source, message, types.map(_.name.toString))
  }
}

package net.impleri.playerskills.commands

import com.mojang.brigadier.builder.LiteralArgumentBuilder
import net.impleri.playerskills.api.skills.Skill
import net.minecraft.commands.Commands
import net.minecraft.commands.CommandSourceStack
import net.minecraft.network.chat.Component

trait ListSkillsCommand extends ValuesCommandUtils {
  protected def registerAllCommand(builder: LiteralArgumentBuilder[CommandSourceStack]): LiteralArgumentBuilder[CommandSourceStack] = {
    builder.`then`(
      Commands
        .literal("all")
        .executes(c => listSkills(c.getSource)),
    )
  }

  private def listSkills(source: CommandSourceStack): Int = {
    val skills = Skill().all()
    val message = if (skills.nonEmpty) {
      Component
        .translatable("commands.playerskills.registered_skills", skills.size)
    } else {
      Component
        .translatable("commands.playerskills.no_registered_skills")
    }
    listValues(source, message, skills.map(_.name.toString))
  }
}

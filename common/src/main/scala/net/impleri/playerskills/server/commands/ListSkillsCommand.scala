package net.impleri.playerskills.server.commands

import com.mojang.brigadier.builder.LiteralArgumentBuilder
import net.impleri.playerskills.api.skills.Skill
import net.minecraft.commands.Commands
import net.minecraft.commands.CommandSourceStack
import net.minecraft.network.chat.Component

trait ListSkillsCommand extends ValuesCommandUtils {
  protected def registerAllCommand(builder: LiteralArgumentBuilder[CommandSourceStack]): LiteralArgumentBuilder[CommandSourceStack] = {
    builder.`then`(
      Commands.literal("all").executes(withListValues(listSkills)),
    )
  }

  private def listSkills(): (Component, List[String]) = {
    val skills = Skill().all()
    val message = if (skills.nonEmpty) {
      Component.translatable("commands.playerskills.registered_skills", skills.size)
    } else {
      Component.translatable("commands.playerskills.no_registered_skills")
    }

    (message, skills.map(_.name.toString))
  }
}

package net.impleri.playerskills.server.commands

import com.mojang.brigadier.builder.RequiredArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import net.impleri.playerskills.api.skills.Skill
import net.impleri.playerskills.api.skills.SkillOps
import net.minecraft.commands.Commands
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.arguments.ResourceLocationArgument
import net.minecraft.resources.ResourceLocation

import scala.util.Try

trait WithSkill {
  protected def skillOps: SkillOps

  protected def getSkillArg: RequiredArgumentBuilder[CommandSourceStack, ResourceLocation] = {
    Commands.argument("skill", ResourceLocationArgument.id())
  }

  protected def getSkillName(context: CommandContext[CommandSourceStack]): Option[ResourceLocation] = {
    Try(ResourceLocationArgument
      .getId(context, "skill"),
    ).toOption
  }

  protected def getSkill[T](context: CommandContext[CommandSourceStack]): Option[Skill[T]] = {
    getSkillName(context)
      .flatMap(skillOps.get[T](_))
  }
}

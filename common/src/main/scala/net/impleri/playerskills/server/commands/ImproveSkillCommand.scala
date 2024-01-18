package net.impleri.playerskills.server.commands

import com.mojang.brigadier.builder.LiteralArgumentBuilder
import net.impleri.playerskills.facades.minecraft.Player
import net.impleri.playerskills.facades.minecraft.core.ResourceLocation
import net.minecraft.commands.Commands
import net.minecraft.commands.CommandSourceStack

import scala.jdk.FunctionConverters.enrichAsJavaPredicate

trait ImproveSkillCommand extends SetCommandUtils with CommandHelpers {
  protected def registerImproveCommand(builder: LiteralArgumentBuilder[CommandSourceStack]): LiteralArgumentBuilder[CommandSourceStack] = {
    builder.`then`(
      Commands.literal("improve")
        .requires(hasPermission().asJavaPredicate)
        .`then`(
          getPlayerArg.`then`(
            getSkillArg.executes(
              withNotification(
                "commands.playerskills.skill_improved",
                "commands.playerskills.skill_improve_failed",
                false,
                improvePlayerSkill,
              ),
            ),
          ),
        ).`then`(
          getSkillArg.executes(
            withNotification(
              "commands.playerskills.skill_improved",
              "commands.playerskills.skill_improve_failed",
              true,
              improvePlayerSkill,
            ),
          ),
        ),
    )
  }

  private[commands] def improvePlayerSkill[T](
    player: Option[Player[_]],
    skillName: Option[ResourceLocation],
  ): Option[Boolean] = {
    skillName
      .flatMap(skillOps.get[T])
      .flatMap(s => player.flatMap(p => teamOps.improve(p, s, None, None)))
  }
}

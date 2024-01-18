package net.impleri.playerskills.server.commands

import com.mojang.brigadier.builder.LiteralArgumentBuilder
import net.impleri.playerskills.facades.minecraft.Player
import net.impleri.playerskills.facades.minecraft.core.ResourceLocation
import net.minecraft.commands.Commands
import net.minecraft.commands.CommandSourceStack

import scala.jdk.FunctionConverters.enrichAsJavaPredicate

trait DegradeSkillCommand extends SetCommandUtils with CommandHelpers {
  protected def registerDegradeCommand(builder: LiteralArgumentBuilder[CommandSourceStack]): LiteralArgumentBuilder[CommandSourceStack] = {
    builder.`then`(
      Commands.literal("degrade")
        .requires(hasPermission().asJavaPredicate)
        .`then`(
          getPlayerArg.`then`(
            getSkillArg.executes(
              withNotification(
                "commands.playerskills.skill_degraded",
                "commands.playerskills.skill_degrade_failed",
                false,
                degradePlayerSkill,
              ),
            ),
          ),
        ).`then`(
          getSkillArg.executes(
            withNotification(
              "commands.playerskills.skill_degraded",
              "commands.playerskills.skill_degrade_failed",
              true,
              degradePlayerSkill,
            ),
          ),
        ),
    )
  }

  private[commands] def degradePlayerSkill[T](
    player: Option[Player[_]],
    skillName: Option[ResourceLocation],
  ): Option[Boolean] = {
    skillName.flatMap(skillOps.get[T])
      .flatMap(s => player.flatMap(teamOps.degrade(_, s, None, None)))
  }
}

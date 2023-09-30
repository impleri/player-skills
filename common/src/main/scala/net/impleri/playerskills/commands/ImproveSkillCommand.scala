package net.impleri.playerskills.commands

import com.mojang.brigadier.builder.LiteralArgumentBuilder
import net.impleri.playerskills.api.skills.Skill
import net.impleri.playerskills.api.skills.Team
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands
import net.minecraft.commands.arguments.EntityArgument
import net.minecraft.commands.arguments.ResourceLocationArgument
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.entity.player.{Player => MinePlayer}

import scala.util.Try
import scala.util.chaining._

trait ImproveSkillCommand extends SetCommandUtils {
  private val REQUIRED_PERMISSION = 2

  protected def registerImproveCommand(builder: LiteralArgumentBuilder[CommandSourceStack]): LiteralArgumentBuilder[CommandSourceStack] =
    builder.`then`(
      Commands.literal("improve")
        .requires(c => c.hasPermission(REQUIRED_PERMISSION))
        .`then`(
          Commands.argument("player", EntityArgument.player())
            .`then`(
              Commands.argument("skill", ResourceLocationArgument.id())
                .executes(c => improvePlayerSkill(
                  c.getSource,
                  Try(EntityArgument.getPlayer(c, "player")).toOption,
                  Try(ResourceLocationArgument.getId(c, "skill")).toOption,
                ))
            )
        )
        .`then`(
          Commands.argument("skill", ResourceLocationArgument.id())
            .executes(c => improvePlayerSkill(
              c.getSource,
              Try(c.getSource.getPlayerOrException).toOption,
              Try(ResourceLocationArgument.getId(c, "skill")).toOption,
            ))
        )
    )

  private def improvePlayerSkill[T](
    source: CommandSourceStack,
    player: Option[MinePlayer],
    skillName: Option[ResourceLocation],
  ): Int =
    skillName
      .flatMap(Skill.get[T])
      .flatMap(s => player.flatMap(p => Team.improve(p, s, None, None)))
      .pipe(notifyPlayer(
        source,
        player,
        skillName,
        successMessage = "commands.playerskills.skill_improved",
        failureMessage = "commands.playerskills.skill_improve_failed"
      ))
}

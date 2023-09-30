package net.impleri.playerskills.commands

import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import net.impleri.playerskills.api.skills.ChangeableSkillOps
import net.impleri.playerskills.api.skills.Player
import net.impleri.playerskills.api.skills.Skill
import net.impleri.playerskills.api.skills.SkillType
import net.minecraft.commands.arguments.EntityArgument
import net.minecraft.commands.arguments.ResourceLocationArgument
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.entity.player.{Player => MinePlayer}

import scala.util.Try
import scala.util.chaining._

trait SetSkillCommand extends SetCommandUtils {
  private val REQUIRED_PERMISSION = 2

  protected def registerSetCommand(builder: LiteralArgumentBuilder[CommandSourceStack]): LiteralArgumentBuilder[CommandSourceStack] =
    builder.`then`(
      Commands.literal("set")
        .requires(c => c.hasPermission(REQUIRED_PERMISSION))
        .`then`(
          Commands.argument("player", EntityArgument.player())
            .`then`(
              Commands.argument("skill", ResourceLocationArgument.id())
                .`then`(
                  Commands.argument("value", StringArgumentType.string())
                    .executes(c => grantPlayerSkill(
                      c.getSource,
                      Try(EntityArgument.getPlayer(c, "player")).toOption,
                      Try(ResourceLocationArgument.getId(c, "skill")).toOption,
                      StringArgumentType.getString(c, "value"),
                    ))
                )
            )
        )
        .`then`(
          Commands.argument("skill", ResourceLocationArgument.id())
            .`then`(
              Commands.argument("value", StringArgumentType.string())
                .executes(c => grantPlayerSkill(
                  c.getSource,
                  Try(c.getSource.getPlayerOrException).toOption,
                  Try(ResourceLocationArgument.getId(c, "skill")).toOption,
                  StringArgumentType.getString(c, "value"),
                ))
            )
        )
    )

  private def grantFoundSkillTo[T](player: Option[MinePlayer], skill: Skill[T], value: String) =
    SkillType.get(skill)
      .map(_.castFromString(Option(value)))
      .map(v => skill.asInstanceOf[Skill[T] with ChangeableSkillOps[T, Skill[T]]].mutate(v))
      .map(s => player.map(Player.upsert(_, s)))
      .pipe(_.nonEmpty)

  private def grantPlayerSkill[T](
    source: CommandSourceStack,
    player: Option[MinePlayer],
    skillName: Option[ResourceLocation],
    value: String,
  ): Int = {
    skillName
      .flatMap(Skill.get[T])
      .map(grantFoundSkillTo[T](player, _, value))
      .pipe(notifyPlayer(
        source,
        player,
        skillName,
        successMessage = "commands.playerskills.skill_changed",
        failureMessage = "commands.playerskills.skill_change_failed",
      ))
  }

}

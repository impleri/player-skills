package net.impleri.playerskills.server.commands

import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import net.impleri.playerskills.api.skills.ChangeableSkillOps
import net.impleri.playerskills.api.skills.Skill
import net.impleri.playerskills.api.skills.SkillOps
import net.impleri.playerskills.api.skills.SkillTypeOps
import net.impleri.playerskills.facades.minecraft.{Player => MinecraftPlayer}
import net.impleri.playerskills.facades.minecraft.core.ResourceLocation
import net.impleri.playerskills.server.api.Player
import net.minecraft.commands.Commands
import net.minecraft.commands.CommandSourceStack

import scala.jdk.FunctionConverters.enrichAsJavaPredicate
import scala.util.chaining.scalaUtilChainingOps

trait SetSkillCommand extends SetCommandUtils {
  protected def playerOps: Player

  protected def skillOps: SkillOps

  protected def skillTypeOps: SkillTypeOps

  protected def registerSetCommand(builder: LiteralArgumentBuilder[CommandSourceStack]): LiteralArgumentBuilder[CommandSourceStack] = {
    builder.`then`(
      Commands.literal("set")
        .requires(hasPermission().asJavaPredicate)
        .`then`(
          getPlayerArg.`then`(
            getSkillArg.`then`(
              Commands.argument("value", StringArgumentType.string()).executes(
                c => grantPlayerSkill(
                  c.getSource,
                  getPlayer(c),
                  getSkillName(c),
                  StringArgumentType.getString(c, "value"),
                ),
              ),
            ),
          ),
        ).`then`(
          getSkillArg.`then`(
            Commands.argument("value", StringArgumentType.string()).executes(
              c => grantPlayerSkill(
                c.getSource,
                Option(getCurrentPlayer(c.getSource)),
                getSkillName(c),
                StringArgumentType.getString(c, "value"),
              ),
            ),
          ),
        ),
    )
  }

  private def grantFoundSkillTo[T](player: Option[MinecraftPlayer[_]], skill: Skill[T], value: String) = {
    skillTypeOps.get(skill)
      .map(_.castFromString(value))
      .map(v => skill.asInstanceOf[ChangeableSkillOps[T, Skill[T]]].mutate(v))
      .map(s => player.map(playerOps.upsert(_, s)))
      .forall(_.nonEmpty)
  }

  private[commands] def grantPlayerSkill[T](
    source: CommandSourceStack,
    player: Option[MinecraftPlayer[_]],
    skillName: Option[ResourceLocation],
    value: String,
  ): Int = {
    skillName.flatMap(skillOps.get[T])
      .map(grantFoundSkillTo[T](player, _, value))
      .pipe(
        notifyPlayer(
          source,
          player,
          skillName,
          successMessage = "commands.playerskills.skill_changed",
          failureMessage = "commands.playerskills.skill_change_failed",
        ),
      )
  }

}

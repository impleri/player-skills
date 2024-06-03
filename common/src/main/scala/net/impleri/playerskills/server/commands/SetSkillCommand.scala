package net.impleri.playerskills.server.commands

import net.impleri.playerskills.api.skills.ChangeableSkillOps
import net.impleri.playerskills.api.skills.Skill
import net.impleri.playerskills.api.skills.SkillOps
import net.impleri.playerskills.api.skills.SkillTypeOps
import net.impleri.playerskills.server.api.{Player => PlayerOps}
import net.impleri.slab.commands.CommandAction
import net.impleri.slab.commands.CommandSegment
import net.impleri.slab.commands.CommandString
import net.impleri.slab.commands.PlayerArgument
import net.impleri.slab.commands.StringArgument
import net.impleri.slab.entity.Player

trait SetSkillCommand extends CommandUtils {
  protected def playerOps: PlayerOps

  protected def skillOps: SkillOps

  protected def skillTypeOps: SkillTypeOps

  protected def registerSetCommand(builder: CommandSegment.Any): CommandSegment.Any = {

    builder.option(
      CommandString("set")
        .requireMod()
        .option(
          PlayerArgument().option(
            SkillHandler.getArgument.option(
              StringArgument("value").executes(CommandAction(handler(false)).message()),
            ),
          ),
        )
        .option(
          SkillHandler.getArgument.option(
            StringArgument("value").executes(CommandAction(handler(true)).message()),
          ),
        ),
    )
  }

  private def successMessage: String = "commands.playerskills.skill_changed"

  private def failureMessage: String = "commands.playerskills.skill_change_failed"

  private def grantFoundSkillTo[T](player: Player.Any, skill: Skill[T], value: String) = {
    skillTypeOps.get(skill)
      .map(_.castFromString(value))
      .map(v => skill.asInstanceOf[ChangeableSkillOps[T, Skill[T]]].mutate(v))
      .map(s => playerOps.upsert(player, s))
      .forall(_.nonEmpty)
  }

  private def handler(useCurrentUser: Boolean): CommandAction.Callback = {
    context => {
      val player = CommandAction.getPlayer(context, useCurrentUser)
      val skillName = SkillHandler.getValue(context)
      val value = StringArgument.getValue("value", context)

      skillName.flatMap(skillOps.get)
        .flatMap(s => player.map(grantFoundSkillTo(_, s, value.getOrElse(""))))
        .toRight(skillNotFound(skillName))
        .filterOrElse(_ == true, formatMessage(failureMessage, skillName, player))
        .map(_ => formatMessage(successMessage, skillName, player))
    }
  }
}

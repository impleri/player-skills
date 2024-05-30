package net.impleri.playerskills.server.commands

import net.impleri.playerskills.api.skills.Skill
import net.impleri.playerskills.api.skills.SkillOps
import net.impleri.playerskills.server.api.TeamOps
import net.impleri.slab.chat.Message
import net.impleri.slab.chat.StaticText
import net.impleri.slab.chat.TranslatableText
import net.impleri.slab.commands.CommandAction
import net.impleri.slab.commands.CommandCallback
import net.impleri.slab.entity.Player
import net.impleri.slab.resources.ResourceLocation

trait SetCommandUtils {
  protected def skillOps: SkillOps

  protected def teamOps: TeamOps

  protected def successMessage: String

  protected def failureMessage: String

  protected def handler(useCurrentUser: Boolean): CommandCallback = {
    context => {
      val player = CommandAction.getPlayer(context, useCurrentUser)
      val skillName = SkillHandler.getValue(context)

      callback(player, skillName)
    }
  }

  protected def action[T](player: Player.Any, skill: Skill[T]): Option[Boolean]

  protected def callback[T](
    player: Option[Player[_]],
    skillName: Option[ResourceLocation],
  ): Either[Message[_], Message[_]] = {
    skillName.flatMap(skillOps.get[T])
      .flatMap(s => player.flatMap(action(_, s)))
      .toRight(TranslatableText("commands.playerskills.skill_not_found", getSkillName(skillName)))
      .filterOrElse(_ == true, formatMessage(failureMessage, skillName, player))
      .map(_ => formatMessage(successMessage, skillName, player))
  }

  protected def getSkillName(skillName: Option[ResourceLocation]): String = {
    skillName
      .fold("[Unknown skill]")(_.toString)
  }

  protected[commands] def formatMessage(
    message: String,
    skillName: Option[ResourceLocation],
    player: Option[Player.Any],
  ): TranslatableText = {
    TranslatableText(
      message,
      StaticText(getSkillName(skillName)).darkAqua(),
      StaticText(player.fold("")(_.name)).bold().green(),
    )
  }
}

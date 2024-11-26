package net.impleri.playerskills.server.commands

import net.impleri.playerskills.api.skills.Skill
import net.impleri.playerskills.api.skills.SkillOps
import net.impleri.playerskills.server.api.TeamOps
import net.impleri.slab.chat.Message
import net.impleri.slab.commands.CommandAction
import net.impleri.slab.entity.Player
import net.impleri.slab.resources.ResourceLocation

case class SetCommandFactory(
  protected val skillOps: SkillOps,
  protected val teamOps: TeamOps,
  protected val action: (Player, Skill[_]) => Option[Boolean],
  protected val successMessage: String,
  protected val failureMessage: String,
) extends CommandUtils {

  def createCallback(
    useCurrentUser: Boolean,
  ): CommandAction.Callback = context =>
    {
      val player = CommandAction.getPlayer(context, useCurrentUser)
      val skillName = SkillHandler.getValue(context)

      callback(player, skillName, successMessage, failureMessage, action)
    }

  private def callback[T](
    player: Option[Player],
    skillName: Option[ResourceLocation],
    successMessage: String,
    failureMessage: String,
    action: (Player, Skill[_]) => Option[Boolean],
  ): Either[Message[_], Message[_]] =
    skillName
      .flatMap(skillOps.get[T])
      .flatMap(s => player.flatMap(action(_, s)))
      .toRight(skillNotFound(skillName))
      .filterOrElse(_ == true, formatMessage(failureMessage, skillName, player))
      .map(_ => formatMessage(successMessage, skillName, player))
}

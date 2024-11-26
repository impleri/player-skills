package net.impleri.playerskills.server.commands

import net.impleri.slab.chat.StaticText
import net.impleri.slab.chat.TranslatableText
import net.impleri.slab.entity.Player
import net.impleri.slab.resources.ResourceLocation

trait CommandUtils {
  protected def skillNotFound(
    skillName: Option[ResourceLocation],
  ): TranslatableText = TranslatableText(
    "commands.playerskills.skill_not_found",
    getSkillName(skillName),
  )

  private def getSkillName(skillName: Option[ResourceLocation]): String =
    skillName
      .fold("[Unknown skill]")(_.toString)

  protected[commands] def formatMessage(
    message: String,
    skillName: Option[ResourceLocation],
    player: Option[Player],
  ): TranslatableText =
    TranslatableText(
      message,
      StaticText(getSkillName(skillName)).darkAqua(),
      StaticText(player.fold("")(_.handle)).bold().green(),
    )
}

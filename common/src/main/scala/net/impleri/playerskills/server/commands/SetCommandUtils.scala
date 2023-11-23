package net.impleri.playerskills.server.commands

import com.mojang.brigadier.Command
import com.mojang.brigadier.context.CommandContext
import net.impleri.playerskills.facades.minecraft.Player
import net.impleri.playerskills.server.api.TeamOps
import net.minecraft.ChatFormatting
import net.minecraft.commands.CommandSourceStack
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation

import scala.util.chaining.scalaUtilChainingOps

trait SetCommandUtils extends WithPlayer with WithSkill with CommandHelpers {
  protected def teamOps: TeamOps

  private val COMMAND_FAILURE = 0

  protected[commands] def notifyPlayer(
    source: CommandSourceStack,
    player: Option[Player[_]],
    skillName: Option[ResourceLocation],
    successMessage: String,
    failureMessage: String,
  )(
    result: Option[Boolean],
  ): Int = {
    result match {
      case Some(v) => {
        Component.translatable(
            if (v) successMessage else failureMessage,
            Component.literal(skillName.fold("[Unknown skill]")(_.toString)).withStyle(ChatFormatting.DARK_AQUA),
            player.map(p => Component.literal(p.name).withStyle(ChatFormatting.BOLD, ChatFormatting.GREEN)),
          ).pipe(m => if (v) source.sendSuccess(m, false) else source.sendFailure(m))
          .pipe(_ => if (v) Command.SINGLE_SUCCESS else COMMAND_FAILURE)
      }
      case None => {
        source.sendFailure(Component.translatable("commands.playerskills.skill_not_found", skillName.toString))
          .pipe(_ => COMMAND_FAILURE)
      }
    }
  }

  protected def withNotification(
    successMessage: String,
    failureMessage: String,
    useCurrentPlayer: Boolean = false,
    f: (Option[Player[_]], Option[ResourceLocation]) => Option[Boolean],
  ): Command[CommandSourceStack] = {
    (context: CommandContext[CommandSourceStack]) => {
      val player = if (useCurrentPlayer) Option(getCurrentPlayer(context.getSource)) else getPlayer(context)
      val skill = getSkillName(context)

      f(player, skill)
        .pipe(notifyPlayer(context.getSource, player, skill, successMessage, failureMessage))
    }
  }
}

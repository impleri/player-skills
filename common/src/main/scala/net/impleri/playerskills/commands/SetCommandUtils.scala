package net.impleri.playerskills.commands

import com.mojang.brigadier.Command
import net.minecraft.ChatFormatting
import net.minecraft.commands.CommandSourceStack
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.entity.player.{Player => MinePlayer}

import scala.util.chaining._

trait SetCommandUtils {
  private val COMMAND_FAILURE = 0

  protected def notifyPlayer(
    source: CommandSourceStack,
    player: Option[MinePlayer],
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
            player.map(p => Component.literal(p.getName.getString).withStyle(ChatFormatting.BOLD, ChatFormatting.GREEN)),
          ).pipe(m => if (v) source.sendSuccess(m, false) else source.sendFailure(m))
          .pipe(_ => if (v) Command.SINGLE_SUCCESS else COMMAND_FAILURE)
      }
      case None => {
        source.sendFailure(Component.translatable("commands.playerskills.skill_not_found", skillName.toString))
          .pipe(_ => COMMAND_FAILURE)
      }
    }
  }
}

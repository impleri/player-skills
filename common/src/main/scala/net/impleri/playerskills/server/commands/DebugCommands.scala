package net.impleri.playerskills.server.commands

import com.mojang.brigadier.builder.LiteralArgumentBuilder
import net.impleri.playerskills.utils.PlayerSkillsLogger
import net.minecraft.ChatFormatting
import net.minecraft.commands.Commands
import net.minecraft.commands.CommandSourceStack
import net.minecraft.network.chat.Component

import scala.jdk.FunctionConverters.enrichAsJavaPredicate

trait DebugCommands extends CommandHelpers {
  protected def logger: PlayerSkillsLogger

  protected def itemLogger: PlayerSkillsLogger

  protected def blockLogger: PlayerSkillsLogger

  protected def fluidLogger: PlayerSkillsLogger

  protected def mobLogger: PlayerSkillsLogger

  protected def registerDebugCommands(builder: LiteralArgumentBuilder[CommandSourceStack]): LiteralArgumentBuilder[CommandSourceStack] = {
    builder.`then`(
      Commands.literal("debug")
        .requires(hasPermission().asJavaPredicate)
        .`then`(Commands.literal("skills").executes(withSuccessMessage(toggleDebug("Skills", logger))))
        .`then`(Commands.literal("blocks").executes(withSuccessMessage(toggleDebug("Block Restrictions", blockLogger))))
        .`then`(Commands.literal("fluids").executes(withSuccessMessage(toggleDebug("Fluid Restrictions", fluidLogger))))
        .`then`(Commands.literal("items").executes(withSuccessMessage(toggleDebug("Item Restrictions", itemLogger))))
        .`then`(Commands.literal("mobs").executes(withSuccessMessage(toggleDebug("Mob Restrictions", mobLogger)))),
    )
  }

  private[commands] def toggleDebug(modLabel: String, logInstance: PlayerSkillsLogger): () => Component = {
    () => {
      if (logInstance.toggleDebug()) {
        Component.translatable("commands.playerskills.debug_enabled", modLabel)
          .withStyle(ChatFormatting.RED, ChatFormatting.BOLD)
      } else {
        Component.translatable("commands.playerskills.debug_disabled", modLabel)
          .withStyle(ChatFormatting.GREEN, ChatFormatting.ITALIC)
      }
    }
  }
}

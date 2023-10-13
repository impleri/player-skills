package net.impleri.playerskills.commands

import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.Command
import net.impleri.playerskills.utils.PlayerSkillsLogger
import net.minecraft.ChatFormatting
import net.minecraft.commands.Commands
import net.minecraft.commands.CommandSourceStack
import net.minecraft.network.chat.Component

trait DebugCommands {
  private val REQUIRED_PERMISSION = 2

  protected def registerDebugCommands(builder: LiteralArgumentBuilder[CommandSourceStack]): LiteralArgumentBuilder[CommandSourceStack] = {
    builder.`then`(
      Commands.literal("debug")
        .requires(c => c.hasPermission(REQUIRED_PERMISSION))
        .`then`(
          Commands.literal("skills")
            .executes(c => toggleDebug("Skills", c.getSource, () => PlayerSkillsLogger.SKILLS.toggleDebug())),
        )
        .`then`(
          Commands.literal("blocks")
            .executes(c => toggleDebug("Block Restrictions",
              c.getSource,
              () => PlayerSkillsLogger.BLOCKS.toggleDebug(),
            ),
            ),
        )
        .`then`(
          Commands.literal("fluids")
            .executes(c => toggleDebug("Fluid Restrictions",
              c.getSource,
              () => PlayerSkillsLogger.FLUIDS.toggleDebug(),
            ),
            ),
        )
        .`then`(
          Commands.literal("items")
            .executes(c => toggleDebug("Item Restrictions", c.getSource, () => PlayerSkillsLogger.ITEMS.toggleDebug())),
        )
        .`then`(
          Commands.literal("mobs")
            .executes(c => toggleDebug("Mob Restrictions", c.getSource, () => PlayerSkillsLogger.MOBS.toggleDebug())),
        ),
    )
  }

  private def toggleDebug(modLabel: String, source: CommandSourceStack, supplier: () => Boolean): Int = {
    val message = if (supplier()) {
      Component
        .translatable("commands.playerskills.debug_enabled", modLabel)
        .withStyle(ChatFormatting.RED, ChatFormatting.BOLD)
    } else {
      Component
        .translatable("commands.playerskills.debug_disabled", modLabel)
        .withStyle(ChatFormatting.GREEN, ChatFormatting.ITALIC)
    }
    source.sendSuccess(message, false)

    Command.SINGLE_SUCCESS
  }
}

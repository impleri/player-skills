package net.impleri.playerskills.commands

import com.mojang.brigadier.Command
import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.StringArgumentType
import net.impleri.playerskills.PlayerSkills
import net.impleri.playerskills.api.Player
import net.impleri.playerskills.api.Skill
import net.impleri.playerskills.api.SkillType
import net.impleri.playerskills.api.Team
import net.minecraft.ChatFormatting
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands
import net.minecraft.commands.arguments.EntityArgument
import net.minecraft.commands.arguments.ResourceLocationArgument
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerPlayer
import java.util.function.Supplier

object PlayerSkillsCommands {
  private const val REQUIRED_PERMISSION = 2
  fun register(
    dispatcher: CommandDispatcher<CommandSourceStack>,
  ) {
    val debugCommand = toggleDebug("Player Skills") { PlayerSkills.toggleDebug() }

    dispatcher.register(
      Commands.literal("skills")
        .then(
          Commands
            .literal("types")
            .executes { listTypes(it.source) },
        )
        .then(
          Commands
            .literal("all")
            .executes { listSkills(it.source) },
        )
        .then(
          Commands
            .literal("mine")
            .executes { listOwnSkills(it.source) },
        )
        .then(
          Commands.literal("team")
            .then(
              Commands
                .literal("share")
                .executes { syncToTeam(it.source) },
            )
            .then(
              Commands.literal("sync")
                .requires { it.hasPermission(REQUIRED_PERMISSION) }
                .then(
                  Commands.argument("player", EntityArgument.player())
                    .executes {
                      syncTeamFor(
                        EntityArgument.getPlayer(it, "player"),
                      )
                    },
                ),
            ),
        )
        .then(
          Commands.literal("debug")
            .requires { it.hasPermission(REQUIRED_PERMISSION) }
            .executes { debugCommand(it.source) },
        )
        .then(
          Commands.literal("set")
            .requires { it.hasPermission(REQUIRED_PERMISSION) }
            .then(
              Commands.argument("player", EntityArgument.player())
                .then(
                  Commands.argument("skill", ResourceLocationArgument.id())
                    .then(
                      Commands.argument("value", StringArgumentType.string())
                        .executes {
                          grantPlayerSkill<Any>(
                            it.source,
                            EntityArgument.getPlayer(it, "player"),
                            ResourceLocationArgument.getId(it, "skill"),
                            StringArgumentType.getString(it, "value"),
                          )
                        },
                    ),
                ),
            )
            .then(
              Commands.argument("skill", ResourceLocationArgument.id())
                .then(
                  Commands.argument("value", StringArgumentType.string())
                    .executes {
                      grantPlayerSkill<Any>(
                        it.source,
                        it.source.player!!,
                        ResourceLocationArgument.getId(it, "skill"),
                        StringArgumentType.getString(it, "value"),
                      )
                    },
                ),
            ),
        ),
    )
  }

  fun registerDebug(
    dispatcher: CommandDispatcher<CommandSourceStack>,
    commandParent: String,
    debugCommand: (CommandSourceStack) -> Int,
  ) {
    dispatcher.register(
      Commands.literal(commandParent)
        .then(
          Commands.literal("debug")
            .requires { it.hasPermission(REQUIRED_PERMISSION) }
            .executes { debugCommand(it.source) },
        ),
    )
  }

  fun toggleDebug(modLabel: String, supplier: Supplier<Boolean>): (CommandSourceStack) -> Int {
    return {
      val enabled = supplier.get()

      val message = if (enabled) "commands.playerskills.debug_enabled" else "commands.playerskills.debug_disabled"
      val color = if (enabled) ChatFormatting.RED else ChatFormatting.GREEN
      val style = if (enabled) ChatFormatting.BOLD else ChatFormatting.ITALIC

      it.sendSuccess(Component.translatable(message, modLabel).withStyle(color, style), false)

      Command.SINGLE_SUCCESS
    }
  }

  private fun listTypes(source: CommandSourceStack): Int {
    val types = SkillType.all()
    val count = types.size
    if (count == 0) {
      source.sendSuccess(Component.translatable("commands.playerskills.no_registered_types"), false)
    } else {
      source.sendSuccess(Component.translatable("commands.playerskills.registered_types", count), false)
      types.forEach { source.sendSystemMessage(Component.literal(it.name.toString())) }
    }
    return Command.SINGLE_SUCCESS
  }

  private fun listSkills(source: CommandSourceStack): Int {
    val skills = Skill.all()
    val count = skills.size
    if (count == 0) {
      source.sendSuccess(Component.translatable("commands.playerskills.no_registered_skills"), false)
    } else {
      source.sendSuccess(Component.translatable("commands.playerskills.registered_skills", count), false)
      skills.forEach { source.sendSystemMessage(Component.literal(it.name.toString())) }
    }
    return Command.SINGLE_SUCCESS
  }

  private fun syncTeamFor(player: ServerPlayer?): Int {
    if (player == null) {
      return 2
    }
    val success = Team.syncEntireTeam(player)
    return if (success) Command.SINGLE_SUCCESS else 3
  }

  private fun syncToTeam(source: CommandSourceStack): Int {
    val player = source.player ?: return 2
    val success = Team.syncFromPlayer(player)
    return if (success) Command.SINGLE_SUCCESS else 3
  }

  private fun listOwnSkills(source: CommandSourceStack): Int {
    val player = source.player ?: return 2
    val skills = Player.get(player)
    val acquiredSkills = skills.filter { Player.can(it, null) }
    val count = acquiredSkills.size
    if (count == 0) {
      source.sendSuccess(Component.translatable("commands.playerskills.no_acquired_skills"), false)
    } else {
      source.sendSuccess(Component.translatable("commands.playerskills.acquired_skills", count), false)
      skills.forEach {
        source.sendSystemMessage(
          Component.literal("${it.name} = ${it.value ?: "EMPTY"}"),
        )
      }
    }

    return Command.SINGLE_SUCCESS
  }

  private fun <T> grantPlayerSkill(
    source: CommandSourceStack,
    player: net.minecraft.world.entity.player.Player,
    skillName: ResourceLocation,
    value: String,
  ): Int {
    Skill.find<T>(skillName)?.let {
      val castValue = SkillType.find(it)?.castFromString(value)
      val success = Player.set(player, it, castValue)

      val message = Component.translatable(
        if (success) "commands.playerskills.skill_changed" else "commands.playerskills.skill_change_failed",
        Component.literal(skillName.toString()).withStyle(ChatFormatting.DARK_AQUA),
        Component.literal(value).withStyle(ChatFormatting.RED, ChatFormatting.ITALIC),
        Component.literal(player.name.string).withStyle(ChatFormatting.BOLD, ChatFormatting.GREEN),
      )
      if (success) {
        source.sendSuccess(message, true)
      } else {
        source.sendFailure(message)
      }

      return Command.SINGLE_SUCCESS
    }

    source.sendFailure(Component.translatable("commands.playerskills.skill_not_found", skillName.toString()))

    return Command.SINGLE_SUCCESS
  }
}

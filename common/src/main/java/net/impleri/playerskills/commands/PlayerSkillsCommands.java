package net.impleri.playerskills.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.impleri.playerskills.PlayerSkills;
import net.impleri.playerskills.api.SkillType;
import net.impleri.playerskills.registry.RegistryItemNotFound;
import net.impleri.playerskills.server.ServerApi;
import net.impleri.playerskills.server.TeamApi;
import net.impleri.playerskills.server.api.Skill;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

import java.util.function.Function;
import java.util.function.Supplier;

public class PlayerSkillsCommands {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext registry, Commands.CommandSelection selection) {
        var debugCommand = toggleDebug("Player Skills", PlayerSkills::toggleDebug);

        dispatcher.register(Commands.literal("skills")
                .then(Commands.literal("types").executes(context -> listTypes(context.getSource())))
                .then(Commands.literal("all").executes(context -> listSkills(context.getSource())))
                .then(Commands.literal("mine").executes(context -> listOwnSkills(context.getSource())))
                .then(Commands.literal("team")
                        .then(Commands.literal("share").executes(context -> syncToTeam(context.getSource())))
                        .then(Commands.literal("sync")
                                .requires(source -> source.hasPermission(2))
                                .then(Commands.argument("player", EntityArgument.player())
                                        .executes(context -> syncTeamFor(
                                                context.getSource(),
                                                EntityArgument.getPlayer(context, "player")
                                        ))
                                )
                        )
                )
                .then(Commands.literal("debug")
                        .requires(source -> source.hasPermission(2))
                        .executes(context -> debugCommand.apply(context.getSource()))
                )
                .then(Commands.literal("set")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.argument("player", EntityArgument.player())
                                .then(Commands.argument("skill", ResourceLocationArgument.id())
                                        .then(Commands.argument("value", StringArgumentType.string())
                                                .executes(context -> grantPlayerSkill(
                                                        context.getSource(),
                                                        EntityArgument.getPlayer(context, "player"),
                                                        ResourceLocationArgument.getId(context, "skill"),
                                                        StringArgumentType.getString(context, "value")
                                                ))
                                        )
                                )
                        )
                        .then(Commands.argument("skill", ResourceLocationArgument.id())
                                .then(Commands.argument("value", StringArgumentType.string())
                                        .executes(context -> grantPlayerSkill(
                                                context.getSource(),
                                                context.getSource().getPlayer(),
                                                ResourceLocationArgument.getId(context, "skill"),
                                                StringArgumentType.getString(context, "value")
                                        ))
                                )
                        )
                )
        );
    }

    public static void registerDebug(CommandDispatcher<CommandSourceStack> dispatcher, String commandParent, Function<CommandSourceStack, Integer> debugCommand) {
        dispatcher.register(Commands.literal(commandParent)
                .then(Commands.literal("debug")
                        .requires(source -> source.hasPermission(2))
                        .executes(context -> debugCommand.apply(context.getSource()))
                )
        );
    }

    public static Function<CommandSourceStack, Integer> toggleDebug(String modLabel, Supplier<Boolean> supplier) {
        return (CommandSourceStack source) -> {
            var enabled = supplier.get();
            var message = enabled ? "commands.playerskills.debug_enabled" : "commands.playerskills.debug_disabled";
            var color = enabled ? ChatFormatting.RED : ChatFormatting.GREEN;
            var style = enabled ? ChatFormatting.BOLD : ChatFormatting.ITALIC;

            source.sendSuccess(Component.translatable(message, modLabel).withStyle(color, style), false);
            return Command.SINGLE_SUCCESS;
        };
    }

    private static int listTypes(CommandSourceStack source) {
        var types = SkillType.all();
        var count = types.size();

        if (count == 0) {
            source.sendSuccess(Component.translatable("commands.playerskills.no_registered_types"), false);
        } else {
            source.sendSuccess(Component.translatable("commands.playerskills.registered_types", count), false);
            types.forEach(type -> source.sendSystemMessage(Component.literal(type.getName().toString())));
        }

        return Command.SINGLE_SUCCESS;
    }

    private static int listSkills(CommandSourceStack source) {
        var skills = Skill.all();
        var count = skills.size();

        if (count == 0) {
            source.sendSuccess(Component.translatable("commands.playerskills.no_registered_skills"), false);
        } else {
            source.sendSuccess(Component.translatable("commands.playerskills.registered_skills", count), false);
            skills.forEach(skill -> source.sendSystemMessage(Component.literal(skill.getName().toString())));
        }

        return Command.SINGLE_SUCCESS;
    }

    private static int syncTeamFor(CommandSourceStack source, ServerPlayer player) {
        if (player == null) {
            return 2;
        }

        boolean success = TeamApi.syncEntireTeam(player);

        return success ? Command.SINGLE_SUCCESS : 3;
    }

    private static int syncToTeam(CommandSourceStack source) {
        var player = source.getPlayer();

        if (player == null) {
            return 2;
        }

        boolean success = TeamApi.syncFromPlayer(player);

        return success ? Command.SINGLE_SUCCESS : 3;
    }

    private static int listOwnSkills(CommandSourceStack source) {
        var player = source.getPlayer();

        if (player == null) {
            return 2;
        }

        var skills = ServerApi.getAllSkills(player);
        var acquiredSkills = skills.stream()
                .filter(skill -> ServerApi.can(player, skill)).toList();
        var count = acquiredSkills.size();

        if (count == 0) {
            source.sendSuccess(Component.translatable("commands.playerskills.no_acquired_skills"), false);
        } else {
            source.sendSuccess(Component.translatable("commands.playerskills.acquired_skills", count), false);
            skills.forEach(skill -> source.sendSystemMessage(Component.literal("" + skill.getName().toString() + " = " + (skill.getValue() == null ? "EMPTY" : skill.getValue().toString()))));
        }

        return Command.SINGLE_SUCCESS;
    }

    private static <T> int grantPlayerSkill(CommandSourceStack source, Player player, ResourceLocation skillName, String value) {
        try {
            var skill = Skill.find(skillName);
            var type = SkillType.forSkill(skill);
            var castValue = type.castFromString(value);
            var success = ServerApi.set(player, skill, castValue);

            var message = Component.translatable(
                    success ? "commands.playerskills.skill_changed" : "commands.playerskills.skill_change_failed",
                    Component.literal(skillName.toString()).withStyle(ChatFormatting.DARK_AQUA),
                    Component.literal(value).withStyle(ChatFormatting.RED, ChatFormatting.ITALIC),
                    Component.literal(player.getName().getString()).withStyle(ChatFormatting.BOLD, ChatFormatting.GREEN)
            );
            if (success) {
                source.sendSuccess(message, true);
            } else {
                source.sendFailure(message);
            }
        } catch (RegistryItemNotFound e) {
            // do something
            source.sendFailure(Component.translatable("commands.playerskills.skill_not_found", skillName.toString()));
        }

        return Command.SINGLE_SUCCESS;
    }
}

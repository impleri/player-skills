package net.impleri.playerskills.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.impleri.playerskills.PlayerSkills;
import net.impleri.playerskills.api.SkillType;
import net.impleri.playerskills.registry.RegistryItemNotFound;
import net.impleri.playerskills.server.ServerApi;
import net.impleri.playerskills.server.api.Skill;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class PlayerSkillsCommands {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, Commands.CommandSelection selection) {
        var debugCommand = toggleDebug("Player Skills", PlayerSkills::toggleDebug);

        dispatcher.register(Commands.literal("skills")
                .then(Commands.literal("types").executes(context -> listTypes(context.getSource())))
                .then(Commands.literal("all").executes(context -> listSkills(context.getSource())))
                .then(Commands.literal("mine").executes(context -> listOwnSkills(context.getSource())))
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
                                                context.getSource().getPlayerOrException(),
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

            source.sendSuccess(new TranslatableComponent(message, modLabel).withStyle(color, style), false);
            return Command.SINGLE_SUCCESS;
        };
    }

    private static int listTypes(CommandSourceStack source) {
        var types = SkillType.all();
        var count = types.size();

        if (count == 0) {
            source.sendSuccess(new TranslatableComponent("commands.playerskills.no_registered_types"), false);
        } else {
            var values = types.stream()
                    .map(type -> new TextComponent(type.getName().toString()));
            var response = Stream.concat(Stream.of(new TranslatableComponent("commands.playerskills.registered_types", count)), values).toList();

            source.sendSuccess(CommonComponents.joinLines(response), false);

        }

        return Command.SINGLE_SUCCESS;
    }

    private static int listSkills(CommandSourceStack source) {
        var skills = Skill.all();
        var count = skills.size();

        if (count == 0) {
            source.sendSuccess(new TranslatableComponent("commands.playerskills.no_registered_skills"), false);
        } else {
            var values = skills.stream()
                    .map(skill -> new TextComponent(skill.getName().toString()));
            var response = Stream.concat(Stream.of(new TranslatableComponent("commands.playerskills.registered_skills", count)), values).toList();

            source.sendSuccess(CommonComponents.joinLines(response), false);
        }

        return Command.SINGLE_SUCCESS;
    }

    private static int listOwnSkills(CommandSourceStack source) {
        ServerPlayer player;
        try {
            player = source.getPlayerOrException();
        } catch (CommandSyntaxException error) {
            return 2;
        }

        var skills = ServerApi.getAllSkills(player);
        var acquiredSkills = skills.stream()
                .filter(skill -> ServerApi.can(player, skill)).toList();
        var count = acquiredSkills.size();

        if (count == 0) {
            source.sendSuccess(new TranslatableComponent("commands.playerskills.no_acquired_skills"), false);
        } else {
            var values = skills.stream()
                    .map(skill -> new TextComponent("" + skill.getName().toString() + " = " + (skill.getValue() == null ? "EMPTY" : skill.getValue().toString())));
            var response = Stream.concat(Stream.of(new TranslatableComponent("commands.playerskills.acquired_skills", count)), values).toList();

            source.sendSuccess(CommonComponents.joinLines(response), false);
        }

        return Command.SINGLE_SUCCESS;
    }

    private static int grantPlayerSkill(CommandSourceStack source, Player player, ResourceLocation skillName, String value) {
        try {
            var skill = Skill.find(skillName);
            var type = SkillType.forSkill(skill);
            var castValue = type.castValue(value);
            var success = ServerApi.set(player, skill, castValue);

            var message = new TranslatableComponent(
                    success ? "commands.playerskills.skill_changed" : "commands.playerskills.skill_change_failed",
                    new TextComponent(skillName.toString()).withStyle(ChatFormatting.DARK_AQUA),
                    new TextComponent(value).withStyle(ChatFormatting.RED, ChatFormatting.ITALIC),
                    new TextComponent(player.getName().getString()).withStyle(ChatFormatting.BOLD, ChatFormatting.GREEN)
            );
            if (success) {
                source.sendSuccess(message, true);
            } else {
                source.sendFailure(message);
            }
        } catch (RegistryItemNotFound e) {
            // do something
            source.sendFailure(new TranslatableComponent("commands.playerskills.skill_not_found", skillName.toString()));
        }

        return Command.SINGLE_SUCCESS;
    }
}

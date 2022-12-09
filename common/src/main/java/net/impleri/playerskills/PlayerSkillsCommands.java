package net.impleri.playerskills;

import com.mojang.brigadier.CommandDispatcher;
import net.impleri.playerskills.api.ServerApi;
import net.impleri.playerskills.api.Skill;
import net.impleri.playerskills.api.SkillType;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

public class PlayerSkillsCommands {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext registry, Commands.CommandSelection selection) {
        dispatcher.register(Commands.literal("playerskills")
                .then(Commands.literal("types").executes(context -> listTypes(context.getSource())))
                .then(Commands.literal("skills")
                        .then(Commands.literal("all").executes(context -> listSkills(context.getSource())))
                        .then(Commands.literal("mine").executes(context -> listOwnSkills(context.getSource())))
                )
        );
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

        return 1;
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

        return 1;
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
            skills.forEach(skill -> source.sendSystemMessage(Component.literal("" + skill.getName().toString() + " = " + (skill.getValue() == null ? "NULL" : skill.getValue().toString()))));
        }

        return 1;
    }
}

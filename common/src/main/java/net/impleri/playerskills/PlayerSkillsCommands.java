package net.impleri.playerskills;

import com.mojang.brigadier.CommandDispatcher;
import net.impleri.playerskills.api.PlayerSkill;
import net.impleri.playerskills.api.Skill;
import net.impleri.playerskills.api.SkillType;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

import java.util.stream.Collectors;

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
            source.sendSuccess(Component.literal("There are no types registered"), false);
        } else {
            var list = types.stream().map(type -> type.getName().toString()).collect(Collectors.joining(","));
            source.sendSuccess(Component.literal("The following " + count + " type(s) are registered: " + list), false);
        }

        return 1;
    }

    private static int listSkills(CommandSourceStack source) {
        var skills = Skill.all();
        var count = skills.size();

        if (count == 0) {
            source.sendSuccess(Component.literal("There are no skills registered"), false);
        } else {
            var list = skills.stream()
                    .map(skill -> skill.getName().toString())
                    .collect(Collectors.joining(","));
            source.sendSuccess(Component.literal("The following " + count + " skills(s) are registered: " + list), false);
        }

        return 1;
    }

    private static int listOwnSkills(CommandSourceStack source) {
        var player = source.getPlayer();

        if (player == null) {
            return 2;
        }

        var skills = PlayerSkill.getAllSkills(player);
        var acquiredSkills = skills.stream()
                .filter(skill -> PlayerSkill.can(player, skill)).toList();
        var count = acquiredSkills.size();

        if (count == 0) {
            source.sendSuccess(Component.literal("You have not acquired any skills!"), false);
        } else {
            var list = acquiredSkills.stream()
                    .map(skill -> skill.getName().toString())
                    .collect(Collectors.joining(","));
            source.sendSuccess(Component.literal("The following " + count + " skills(s) are registered: " + list), false);
        }

        return 1;
    }
}

package net.impleri.playerskills.client;

import com.google.common.collect.ImmutableList;
import net.impleri.playerskills.PlayerSkills;
import net.impleri.playerskills.api.Skill;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Internal client-side registry
 */
final class Registry {
    private static final List<Skill<?>> playerSkills = new ArrayList<>();

    public static void syncFromServer(ImmutableList<Skill<?>> skills, boolean force) {
        var prev = get();

        playerSkills.clear();

        PlayerSkills.LOGGER.info("Syncing Client-side skills: {}", skills.stream().map(s -> {
            var value = s.getValue() == null ? "NULL" : s.getValue();
            return "" + s.getName() + "=" + value;
        }).collect(Collectors.joining(", ")));

        playerSkills.addAll(skills);

        PlayerSkillsClient.emitSkillsUpdated(skills, prev, force);
    }

    public static ImmutableList<Skill<?>> get() {
        return ImmutableList.copyOf(playerSkills);
    }
}
